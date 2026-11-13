package com.studying.easylearn.ui.flashcard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.databinding.ActivityFlashcardBinding;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlashcardActivity extends AppCompatActivity {

    private ActivityFlashcardBinding binding;
    private AppDatabase database;
    private TextToSpeech textToSpeech;
    private int currentUserId = 1;

    private List<UserWord> quizWords = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isCardFlipped = false;
    private int masteredInSession = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlashcardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;

        initTTS();
        setupActions();
        loadSrsWords();
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "FLASHCARD_TTS");
        }
    }

    private void setupActions() {
        binding.btnCloseFlashcard.setOnClickListener(v -> finish());
        binding.btnReturnHome.setOnClickListener(v -> finish());

        // Chạm vào container thẻ để lật
        binding.cardContainer.setOnClickListener(v -> {
            if (!isCardFlipped && !quizWords.isEmpty()) {
                flipCard();
            }
        });

        binding.btnSpeakFlashcard.setOnClickListener(v -> {
            if (currentIndex < quizWords.size()) {
                speak(quizWords.get(currentIndex).getWord());
            }
        });

        // Nút đánh giá SRS
        binding.btnHard.setOnClickListener(v -> processAnswer(false));
        binding.btnEasy.setOnClickListener(v -> processAnswer(true));
    }

    private void loadSrsWords() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            long now = System.currentTimeMillis();
            // Lấy tối đa 20 từ đang tới hạn ôn tập hôm nay
            List<UserWord> dueWords = database.userWordDao().getSrsWordsDueToday(currentUserId, now, 20);

            AppExecutors.getInstance().mainThread().post(() -> {
                if (dueWords == null || dueWords.isEmpty()) {
                    showCompletionScreen();
                } else {
                    quizWords = dueWords;
                    binding.progressFlashcard.setMax(quizWords.size());
                    showCard(0);
                }
            });
        });
    }

    private void showCard(int index) {
        if (index >= quizWords.size()) {
            showCompletionScreen();
            return;
        }

        isCardFlipped = false;
        UserWord word = quizWords.get(index);

        binding.tvProgressText.setText((index + 1) + "/" + quizWords.size());
        binding.progressFlashcard.setProgress(index + 1);

        // Đổ dữ liệu mặt trước
        binding.tvFrontWord.setText(word.getWord());

        // Đổ dữ liệu mặt sau
        binding.tvBackWord.setText(word.getWord());
        binding.tvBackPhonetic.setText(word.getPhonetic());
        binding.tvBackMeaning.setText(word.getMeaning());

        // Đặt lại góc xoay ban đầu cho 2 mặt thẻ
        binding.cardFront.setAlpha(1f);
        binding.cardFront.setRotationY(0f);

        binding.cardBack.setAlpha(0f);
        binding.cardBack.setRotationY(90f); // Xoay giấu đi

        binding.layoutActions.setVisibility(View.INVISIBLE);

        // Tự động đọc từ khi thẻ vừa hiện ra
        speak(word.getWord());
    }

    private void flipCard() {
        isCardFlipped = true;

        // Xoay mặt trước đi -90 độ (biến mất nửa chừng)
        binding.cardFront.animate()
                .rotationY(-90f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.cardFront.setAlpha(0f);

                        // Sau đó xoay mặt sau từ 90 độ về 0 độ
                        binding.cardBack.setAlpha(1f);
                        binding.cardBack.animate()
                                .rotationY(0f)
                                .setDuration(150)
                                .setListener(null);
                    }
                });

        // Hiện nút đánh giá
        binding.layoutActions.setVisibility(View.VISIBLE);
    }

    // THUẬT TOÁN SPACED REPETITION ĐƯỢC KÍCH HOẠT TẠI ĐÂY
    private void processAnswer(boolean isRemembered) {
        UserWord currentWord = quizWords.get(currentIndex);

        int newBoxLevel = currentWord.getBoxLevel();
        int newIntervalDays;
        boolean isMastered = false;

        if (isRemembered) {
            // Nâng cấp thẻ
            newBoxLevel++;
            switch (newBoxLevel) {
                case 1: newIntervalDays = 1; break;
                case 2: newIntervalDays = 3; break;
                case 3: newIntervalDays = 7; break;
                case 4: newIntervalDays = 14; break;
                case 5:
                default:
                    newIntervalDays = 30;
                    isMastered = true;
                    masteredInSession++;
                    break;
            }
        } else {
            // Quên -> Đẩy về Hộp 0, bắt học lại ngày mai
            newBoxLevel = 0;
            newIntervalDays = 1;
        }

        // Tính timestamp mốc thời gian ôn tập tiếp theo (Millisecond)
        long nextReviewTime = System.currentTimeMillis() + (newIntervalDays * 24L * 60 * 60 * 1000L);

        // Lưu xuống DB ngầm
        final int finalBox = newBoxLevel;
        final int finalInterval = newIntervalDays;
        final boolean finalMastered = isMastered;

        AppExecutors.getInstance().diskIO().execute(() -> {
            database.userWordDao().updateSrsProgress(currentWord.getId(), finalBox, finalInterval, nextReviewTime, finalMastered);
        });

        // Chuyển sang thẻ tiếp theo
        currentIndex++;
        showCard(currentIndex);
    }

    private void showCompletionScreen() {
        binding.layoutHeader.setVisibility(View.GONE);
        binding.cardContainer.setVisibility(View.GONE);
        binding.layoutActions.setVisibility(View.GONE);
        binding.layoutCompleted.setVisibility(View.VISIBLE);

        if (quizWords.isEmpty()) {
            binding.tvCompletedSummary.setText("Bạn không có từ vựng nào cần ôn tập hôm nay.\nHãy đọc thêm truyện nhé!");
        } else {
            binding.tvCompletedSummary.setText("Bạn đã ôn tập xong " + quizWords.size() + " từ vựng.\n" +
                    (masteredInSession > 0 ? "Trong đó có " + masteredInSession + " từ đã được chuyển vào trí nhớ dài hạn (Đã thuộc)!" : ""));
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}