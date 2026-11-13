package com.studying.easylearn.ui.review;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.Story;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.data.entities.UserStoryProgress;
import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.databinding.ActivityReviewBinding;
import com.studying.easylearn.ui.main.MainActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ReviewActivity extends AppCompatActivity {

    private ActivityReviewBinding binding;
    private AppDatabase database;
    private SessionManager sessionManager;
    private int currentUserId = 1;
    private int storyId = 1;
    private TextToSpeech textToSpeech;
    private SavedWordAdapter savedWordAdapter;

    private List<UserWord> quizWords = new ArrayList<>();
    private List<String> distractorPool = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int correctAnswersCount = 0;
    private boolean isAnswerLocked = false;

    private MaterialButton[] optionButtons;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;
        storyId = getIntent().getIntExtra("STORY_ID", 1);

        optionButtons = new MaterialButton[]{
                binding.btnOption1,
                binding.btnOption2,
                binding.btnOption3,
                binding.btnOption4
        };

        initTextToSpeech();
        setupRecyclerView();
        setupActions();
        recordUserStudyCompletion();
        loadReviewData();
    }

    private void recordUserStudyCompletion() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            // 1. Đảm bảo truyện hiện tại được đánh dấu 100% trong bảng user_story_progress của user này
            UserStoryProgress existing = database.userStoryProgressDao().getProgress(currentUserId, storyId);
            if (existing != null) {
                existing.setReadProgress(100);
                existing.setLastReadTime(System.currentTimeMillis());
                database.userStoryProgressDao().insertOrUpdate(existing);
            } else {
                database.userStoryProgressDao().insertOrUpdate(
                        new UserStoryProgress(currentUserId, storyId, 100, System.currentTimeMillis())
                );
            }

            // 2. Đếm chính xác số truyện KHÁC NHAU đã đạt 100% (chống cộng trùng khi đọc lại)
            int actualUniqueCompletedCount = database.userStoryProgressDao().getCompletedStoriesCount(currentUserId);

            // 3. Cập nhật vào bảng users
            User user = database.userDao().getUserById(currentUserId);
            if (user != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String today = sdf.format(new Date());

                int newStreak = user.getStreakCount();
                if (!today.equals(user.getLastStudyDate())) {
                    newStreak += 1;
                }

                database.userDao().updateStudyProgress(currentUserId, newStreak, actualUniqueCompletedCount, today);
            }
        });
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setSpeechRate(0.95f);
            }
        });
    }

    private void speakWord(String word) {
        if (textToSpeech != null && word != null && !word.isEmpty()) {
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "REVIEW_WORD_TTS");
        }
    }

    private void setupRecyclerView() {
        savedWordAdapter = new SavedWordAdapter(this::speakWord);
        binding.rvSavedWords.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSavedWords.setAdapter(savedWordAdapter);
    }

    private void setupActions() {
        binding.btnBackReview.setOnClickListener(v -> finish());

        binding.btnQuizSpeak.setOnClickListener(v -> {
            if (currentQuestionIndex < quizWords.size()) {
                speakWord(quizWords.get(currentQuestionIndex).getWord());
            }
        });

        binding.btnRetryQuiz.setOnClickListener(v -> restartQuiz());

        binding.btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(ReviewActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadReviewData() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Story story = database.storyDao().getStoryById(storyId);

            // Chỉ lấy từ vựng của đúng currentUserId trong bài truyện này
            List<UserWord> storyWords = database.userWordDao().getWordsByUserAndStory(currentUserId, storyId);

            Map<String, UserWord> uniqueMap = new LinkedHashMap<>();
            if (storyWords != null) {
                for (UserWord uw : storyWords) {
                    uniqueMap.put(uw.getWord().toLowerCase(), uw);
                }
            }

            List<UserWord> distinctWords = new ArrayList<>(uniqueMap.values());

            // Nếu user này chưa tra từ nào trong bài, lấy toàn bộ từ riêng của user này
            if (distinctWords.isEmpty()) {
                List<UserWord> allUserWords = database.userWordDao().getAllWordsByUser(currentUserId);
                for (UserWord uw : allUserWords) {
                    uniqueMap.put(uw.getWord().toLowerCase(), uw);
                }
                distinctWords = new ArrayList<>(uniqueMap.values());
            }

            buildDistractorPool(distinctWords);

            final List<UserWord> finalWords = distinctWords;
            final String title = (story != null) ? story.getTitle() : "Bài đọc tiếng Anh";

            AppExecutors.getInstance().mainThread().post(() -> {
                binding.tvReviewStoryTitle.setText(title);
                binding.tvWordCountSummary.setText("🔖 Đã lưu " + finalWords.size() + " từ vựng vào sổ tay");

                savedWordAdapter.setWords(finalWords);
                quizWords = new ArrayList<>(finalWords);
                restartQuiz();
            });
        });
    }

    private void buildDistractorPool(List<UserWord> currentWords) {
        Set<String> meaningsSet = new HashSet<>();
        for (UserWord w : currentWords) {
            meaningsSet.add(w.getMeaning());
        }
        meaningsSet.add("Mùi thơm dịu, hương vị");
        meaningsSet.add("Tiếng thì thầm, nói nhỏ");
        meaningsSet.add("Ấm cúng, thoải mái");
        meaningsSet.add("Cái nhìn thoáng qua");
        meaningsSet.add("Cổ xưa, lâu đời");
        meaningsSet.add("Bất ngờ, không lường trước");
        meaningsSet.add("Hoài niệm, nhớ nhung");
        meaningsSet.add("Sự khám phá, thám hiểm");

        distractorPool = new ArrayList<>(meaningsSet);
    }

    private void restartQuiz() {
        currentQuestionIndex = 0;
        correctAnswersCount = 0;
        binding.tvScoreBadge.setText("⭐ 0 điểm");
        binding.layoutQuizCompleted.setVisibility(View.GONE);

        if (quizWords.isEmpty()) {
            setQuizViewsVisibility(View.GONE);
            binding.layoutQuizCompleted.setVisibility(View.VISIBLE);
            binding.tvFinalQuizSummary.setText("Bạn chưa chạm tra từ vựng nào trong bài đọc này!");
        } else {
            setQuizViewsVisibility(View.VISIBLE);
            Collections.shuffle(quizWords);
            showQuestion(0);
        }
    }

    private void showQuestion(int index) {
        if (index >= quizWords.size()) {
            showQuizCompleted();
            return;
        }

        isAnswerLocked = false;
        UserWord currentWord = quizWords.get(index);

        binding.tvQuestionProgress.setText("Câu " + (index + 1) + " / " + quizWords.size());
        binding.tvQuizWord.setText(currentWord.getWord());
        binding.tvQuizPhonetic.setText(currentWord.getPhonetic());

        speakWord(currentWord.getWord());

        List<String> options = new ArrayList<>();
        String correctMeaning = currentWord.getMeaning();
        options.add(correctMeaning);

        List<String> shuffledPool = new ArrayList<>(distractorPool);
        Collections.shuffle(shuffledPool);
        for (String candidate : shuffledPool) {
            if (!candidate.equalsIgnoreCase(correctMeaning) && options.size() < 4) {
                options.add(candidate);
            }
        }
        Collections.shuffle(options);

        String[] prefixes = {"A. ", "B. ", "C. ", "D. "};
        for (int i = 0; i < 4; i++) {
            MaterialButton btn = optionButtons[i];
            String optionText = options.get(i);

            resetButtonStyle(btn);
            btn.setText(prefixes[i] + optionText);
            btn.setOnClickListener(v -> handleOptionSelected(btn, optionText, correctMeaning, currentWord));
        }
    }

    private void handleOptionSelected(MaterialButton selectedBtn, String selectedMeaning, String correctMeaning, UserWord wordEntity) {
        if (isAnswerLocked) return;
        isAnswerLocked = true;

        boolean isCorrect = selectedMeaning.equalsIgnoreCase(correctMeaning);

        if (isCorrect) {
            correctAnswersCount++;
            binding.tvScoreBadge.setText("⭐ " + (correctAnswersCount * 10) + " điểm");
            markButtonCorrect(selectedBtn);

            AppExecutors.getInstance().diskIO().execute(() ->
                    database.userWordDao().updateMasteredStatus(wordEntity.getId(), true)
            );
        } else {
            markButtonWrong(selectedBtn);
            for (MaterialButton btn : optionButtons) {
                String rawText = btn.getText().toString().substring(3);
                if (rawText.equalsIgnoreCase(correctMeaning)) {
                    markButtonCorrect(btn);
                    break;
                }
            }
        }

        handler.postDelayed(() -> {
            currentQuestionIndex++;
            showQuestion(currentQuestionIndex);
        }, 950);
    }

    private void resetButtonStyle(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#CBD5E1")));
        btn.setTextColor(Color.parseColor("#1E293B"));
    }

    private void markButtonCorrect(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
        btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#16A34A")));
        btn.setTextColor(Color.parseColor("#15803D"));
    }

    private void markButtonWrong(MaterialButton btn) {
        btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
        btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#DC2626")));
        btn.setTextColor(Color.parseColor("#B91C1C"));
    }

    private void showQuizCompleted() {
        setQuizViewsVisibility(View.GONE);
        binding.layoutQuizCompleted.setVisibility(View.VISIBLE);
        binding.tvFinalQuizSummary.setText("Bạn trả lời đúng " + correctAnswersCount + " / " + quizWords.size() + " từ vựng (+ " + (correctAnswersCount * 10) + " điểm)");
    }

    private void setQuizViewsVisibility(int visibility) {
        binding.tvQuestionProgress.setVisibility(visibility);
        binding.btnQuizSpeak.setVisibility(visibility);
        binding.tvQuizWord.setVisibility(visibility);
        binding.tvQuizPhonetic.setVisibility(visibility);
        for (MaterialButton btn : optionButtons) {
            btn.setVisibility(visibility);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}