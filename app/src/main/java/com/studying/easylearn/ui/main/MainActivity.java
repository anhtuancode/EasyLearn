package com.studying.easylearn.ui.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.studying.easylearn.R;
import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.Story;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.data.entities.UserStoryProgress;
import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.databinding.ActivityMainBinding;
import com.studying.easylearn.ui.auth.LoginActivity;
import com.studying.easylearn.ui.profile.ProfileActivity;
import com.studying.easylearn.ui.reading.ReadingActivity;
import com.studying.easylearn.ui.reading.StoriesActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppDatabase database;
    private SessionManager sessionManager;
    private StoryAdapter storyAdapter;
    private Story recentStory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        setupRecyclerView();
        setupNavigationAndActions();
        loadHomeDataFromDatabase();
    }

    private void setupRecyclerView() {
        storyAdapter = new StoryAdapter(story -> {
            Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
            intent.putExtra("STORY_ID", story.getId());
            startActivity(intent);
        });

        binding.rcvSuggestedStories.setLayoutManager(new LinearLayoutManager(this));
        binding.rcvSuggestedStories.setAdapter(storyAdapter);
    }

    private void setupNavigationAndActions() {
        binding.tabStories.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StoriesActivity.class))
        );

        binding.tvViewAll.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, StoriesActivity.class))
        );

        binding.btnContinueReadingFast.setOnClickListener(v -> {
            int storyId = (recentStory != null) ? recentStory.getId() : 1;
            Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
            intent.putExtra("STORY_ID", storyId);
            startActivity(intent);
        });

        binding.btnContinueRecent.setOnClickListener(v -> {
            int storyId = (recentStory != null) ? recentStory.getId() : 1;
            Intent intent = new Intent(MainActivity.this, ReadingActivity.class);
            intent.putExtra("STORY_ID", storyId);
            startActivity(intent);
        });

        // Tạm thời để Toast, bước sau sẽ chuyển sang FlashcardActivity
        binding.btnFlashcard.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, com.studying.easylearn.ui.flashcard.FlashcardActivity.class))
        );

        binding.tabHome.setOnClickListener(v -> {
        });

        binding.tabVocabulary.setOnClickListener(v ->
                startActivity(new Intent(this, com.studying.easylearn.ui.vocabulary.VocabularyActivity.class))
        );

        binding.tabProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class))
        );

        binding.btnUserProfile.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, v);
            popup.getMenu().add(0, 1, 0, "👤 Hồ sơ cá nhân");
            popup.getMenu().add(0, 2, 1, "🚪 Đăng xuất");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == 2) {
                    showLogoutConfirmDialog();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void loadHomeDataFromDatabase() {
        int uid = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;

        AppExecutors.getInstance().diskIO().execute(() -> {
            User currentUser = database.userDao().getUserById(uid);
            int uniqueCompletedStories = database.userStoryProgressDao().getCompletedStoriesCount(uid);
            UserStoryProgress inProgress = database.userStoryProgressDao().getLatestInProgressStory(uid);
            List<Story> allStories = database.storyDao().getAllStories();
            List<UserStoryProgress> allUserProgress = database.userStoryProgressDao().getAllProgressByUser(uid);

            int currentReadPercent = 0;

            if (inProgress != null) {
                recentStory = database.storyDao().getStoryById(inProgress.getStoryId());
                currentReadPercent = inProgress.getReadProgress();
            } else {
                Set<Integer> completedIds = new LinkedHashSet<>();
                if (allUserProgress != null) {
                    for (UserStoryProgress p : allUserProgress) {
                        if (p.getReadProgress() >= 100) {
                            completedIds.add(p.getStoryId());
                        }
                    }
                }

                recentStory = null;
                if (allStories != null) {
                    for (Story s : allStories) {
                        if (!completedIds.contains(s.getId())) {
                            recentStory = s;
                            currentReadPercent = 0;
                            break;
                        }
                    }
                    if (recentStory == null && !allStories.isEmpty()) {
                        recentStory = allStories.get(allStories.size() - 1);
                        currentReadPercent = 100;
                    }
                }
            }

            List<UserWord> recentStoryWords = (recentStory != null)
                    ? database.userWordDao().getWordsByUserAndStory(uid, recentStory.getId())
                    : new ArrayList<>();
            List<Story> suggestedList = database.storyDao().getSuggestedStoriesForUser(uid);

            // XỬ LÝ LOGIC SRS MỚI THÊM VÀO DB
            long now = System.currentTimeMillis();
            int srsDueCount = database.userWordDao().countSrsWordsDueToday(uid, now);
            List<UserWord> srsDueList = database.userWordDao().getSrsWordsDueToday(uid, now, 4);
            int masteredCount = database.userWordDao().countMasteredWords(uid);

            final int finalPercent = currentReadPercent;

            AppExecutors.getInstance().mainThread().post(() -> {
                if (currentUser != null) {
                    int streak = currentUser.getStreakCount();
                    int goalMinutes = currentUser.getDailyGoalMinutes();

                    // Đã thêm dấu *
                    int studiedMinutes = Math.min(goalMinutes, uniqueCompletedStories * 3);
                    int goalPercent = Math.min(100, (int) Math.round((studiedMinutes * 100.0) / goalMinutes));

                    binding.tvWelcomeGreeting.setText("Chào buổi sáng,\n" + currentUser.getFullName() + " 👋");
                    binding.tvTopStreak.setText("🔥 " + streak);
                    binding.tvCardStreak.setText("🔥 " + streak + " ngày liên tiếp");
                    binding.tvLevelProgress.setText("⚑ " + goalPercent + "%");

                    binding.tvCardGoalHint.setText("Duy trì chuỗi đọc " + goalMinutes + " phút hôm nay nhé!");
                    binding.tvDailyProgressText.setText(studiedMinutes + " / " + goalMinutes + " phút (" + goalPercent + "%)");
                    binding.progressDailyGoal.setProgress(goalPercent);

                    binding.tvHomeStatStories.setText(String.valueOf(uniqueCompletedStories));
                    binding.tvHomeStatLevel.setText(currentUser.getCurrentLevel());
                    binding.tvHomeStatLevelSub.setText(getLevelDescription(currentUser.getCurrentLevel()));
                }

                if (recentStory != null) {
                    // Đã thêm dấu -
                    int remainingMins = (finalPercent >= 100) ? 0 : Math.max(1, (int) Math.ceil(recentStory.getReadTimeMinutes() * (100 - finalPercent) / 100.0));

                    binding.tvRecentCategoryLevel.setText(recentStory.getCategory() + " • " + recentStory.getLevel());
                    binding.tvRecentTitle.setText(recentStory.getTitle());

                    if (finalPercent >= 100) {
                        binding.tvRecentProgressText.setText("Đã hoàn thành 100% 🎉");
                        binding.btnContinueRecent.setText("Đọc lại  ↻");
                    } else {
                        binding.tvRecentProgressText.setText("Đã đọc " + finalPercent + "% • Còn " + remainingMins + " phút");
                        binding.btnContinueRecent.setText("Đọc tiếp  ➔");
                    }

                    binding.progressRecentStory.setProgress(finalPercent);
                    binding.tvRecentSubtitle.setText(recentStory.getDescription());

                    List<String> distinctRecentWords = extractUniqueWords(recentStoryWords);
                    binding.tvRecentWordsCount.setText("🔖 " + distinctRecentWords.size() + " từ mới vừa lưu từ truyện:");
                    renderWordChips(binding.layoutRecentWordChips, distinctRecentWords, true);
                }

                // CẬP NHẬT GIAO DIỆN THEO SRS
                binding.tvHomeStatMastered.setText(String.valueOf(masteredCount));
                binding.tvSrsBadgeCount.setText(srsDueCount + " từ");
                binding.tvSrsDescription.setText(srsDueCount + " từ đến hạn ôn tập ngắt quãng");

                int estimatedMinutes = Math.max(1, (int) Math.ceil(srsDueCount * 0.1));
                binding.btnFlashcard.setText("🔔 Luyện Flashcard (" + estimatedMinutes + " phút)");

                List<String> srsWordsToDisplay = extractUniqueWords(srsDueList);
                renderWordChips(binding.layoutSrsWordChips, srsWordsToDisplay, false);

                if (suggestedList != null && !suggestedList.isEmpty()) {
                    storyAdapter.setStories(suggestedList);
                }
            });
        });
    }

    private List<String> extractUniqueWords(List<UserWord> wordEntities) {
        Set<String> unique = new LinkedHashSet<>();
        if (wordEntities != null) {
            for (UserWord item : wordEntities) {
                if (item.getWord() != null && !item.getWord().isEmpty()) {
                    unique.add(item.getWord().toLowerCase());
                }
            }
        }
        return new ArrayList<>(unique);
    }

    private void renderWordChips(LinearLayout container, List<String> words, boolean isBlueStyle) {
        container.removeAllViews();
        if (words.isEmpty()) {
            TextView emptyHint = new TextView(this);
            emptyHint.setText("Chạm vào từ khi đọc truyện để lưu tại đây");
            emptyHint.setTextColor(Color.parseColor("#94A3B8"));
            emptyHint.setTextSize(12f);
            container.addView(emptyHint);
            return;
        }

        int maxChips = Math.min(4, words.size());
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < maxChips; i++) {
            TextView chip = new TextView(this);
            chip.setText(isBlueStyle ? words.get(i) : words.get(i) + " •");
            chip.setTextSize(12f);
            chip.setBackgroundResource(R.drawable.bg_circle_button);

            if (isBlueStyle) {
                chip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
                chip.setTextColor(Color.parseColor("#1D61F2"));
            } else {
                chip.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8FAFC")));
                chip.setTextColor(Color.parseColor("#0E1B38"));
            }

            // Đã thêm dấu *
            int padH = (int) (8 * density);
            int padV = (int) (4 * density);
            chip.setPadding(padH, padV, padH, padV);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                // Đã thêm dấu *
                params.setMarginStart((int) (6 * density));
            }
            chip.setLayoutParams(params);
            container.addView(chip);
        }
    }

    private String getLevelDescription(String level) {
        if ("B1".equalsIgnoreCase(level)) return "Intermediate";
        if ("B2".equalsIgnoreCase(level)) return "Upper-Intermediate";
        if ("C1".equalsIgnoreCase(level)) return "Advanced";
        if ("C2".equalsIgnoreCase(level)) return "Mastery";
        if ("A2".equalsIgnoreCase(level)) return "Elementary";
        return "Beginner";
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHomeDataFromDatabase();
    }

    private void showLogoutConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản EasyLearn không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}