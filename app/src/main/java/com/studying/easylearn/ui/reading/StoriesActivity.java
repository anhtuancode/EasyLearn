package com.studying.easylearn.ui.reading;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.Story;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.databinding.ActivityStoriesBinding;
import com.studying.easylearn.ui.profile.ProfileActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.SessionManager;

import java.util.List;

public class StoriesActivity extends AppCompatActivity {

    private ActivityStoriesBinding binding;
    private AppDatabase database;
    private SessionManager sessionManager;
    private StoryCatalogAdapter adapter;
    private String currentSelectedLevel = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        setupRecyclerView();
        setupFilterChips();
        setupBottomNavigation();
        syncUserInfoFromDatabase();
        loadStoriesFromDatabase("ALL");
    }

    private void setupBottomNavigation() {
        binding.tabHome.setOnClickListener(v -> finish());

        binding.tabStories.setOnClickListener(v -> {
            // Đang ở màn hình Truyện ngắn
        });

        binding.tabVocabulary.setOnClickListener(v ->
                startActivity(new Intent(this, com.studying.easylearn.ui.vocabulary.VocabularyActivity.class))
        );

        binding.tabProfile.setOnClickListener(v ->
                startActivity(new Intent(StoriesActivity.this, ProfileActivity.class))
        );

        binding.imgUserAvatar.setOnClickListener(v ->
                startActivity(new Intent(StoriesActivity.this, ProfileActivity.class))
        );
    }

    private void syncUserInfoFromDatabase() {
        int uid = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;

        AppExecutors.getInstance().diskIO().execute(() -> {
            User currentUser = database.userDao().getUserById(uid);

            AppExecutors.getInstance().mainThread().post(() -> {
                if (currentUser != null) {
                    int streak = currentUser.getStreakCount();
                    int goalMins = currentUser.getDailyGoalMinutes();

                    binding.tvGreetingName.setText("Good morning, " + currentUser.getFullName());
                    binding.tvReadyGoalSub.setText("Ready for your " + goalMins + "-min read?");
                    binding.tvTopStreakStories.setText("🔥 " + streak);
                    binding.tvCardStreakStories.setText("🔥 " + streak);
                    binding.tvStreakBannerHint.setText("Pick 1 bite-sized story to keep your " + streak + "-day streak glowing!");

                    sessionManager.updateFullName(currentUser.getFullName());
                }
            });
        });
    }

    private void setupRecyclerView() {
        adapter = new StoryCatalogAdapter(story -> {
            Intent intent = new Intent(StoriesActivity.this, ReadingActivity.class);
            intent.putExtra("STORY_ID", story.getId());
            startActivity(intent);
        });

        binding.rvStoryCatalog.setLayoutManager(new LinearLayoutManager(this));
        binding.rvStoryCatalog.setAdapter(adapter);
    }

    private void setupFilterChips() {
        resetChipStyles();
        highlightSelectedChip(binding.chipAll);

        binding.chipAll.setOnClickListener(v -> filterByLevel("ALL", binding.chipAll));
        binding.chipA1.setOnClickListener(v -> filterByLevel("A1", binding.chipA1));
        binding.chipA2.setOnClickListener(v -> filterByLevel("A2", binding.chipA2));
        binding.chipB1.setOnClickListener(v -> filterByLevel("B1", binding.chipB1));
    }

    private void filterByLevel(String level, MaterialButton selectedBtn) {
        currentSelectedLevel = level;
        resetChipStyles();
        highlightSelectedChip(selectedBtn);
        loadStoriesFromDatabase(level);
    }

    private void highlightSelectedChip(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1D61F2")));
        btn.setTextColor(Color.WHITE);
    }

    private void resetChipStyles() {
        MaterialButton[] chips = new MaterialButton[]{binding.chipAll, binding.chipA1, binding.chipA2, binding.chipB1};
        for (MaterialButton btn : chips) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
            btn.setTextColor(Color.parseColor("#1D61F2"));
        }
    }

    private void loadStoriesFromDatabase(String level) {
        int uid = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;

        AppExecutors.getInstance().diskIO().execute(() -> {
            List<Story> displayList;
            if ("ALL".equalsIgnoreCase(level)) {
                // SỬ DỤNG HÀM MỚI CÓ TRUYỀN UID
                displayList = database.storyDao().getAllStoriesForUser(uid);
            } else {
                // SỬ DỤNG HÀM MỚI CÓ TRUYỀN UID
                displayList = database.storyDao().getStoriesByLevelForUser(level, uid);
            }

            AppExecutors.getInstance().mainThread().post(() -> {
                if (adapter != null && displayList != null) {
                    adapter.setData(displayList);
                }
            });
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        syncUserInfoFromDatabase();
        loadStoriesFromDatabase(currentSelectedLevel);
    }
}