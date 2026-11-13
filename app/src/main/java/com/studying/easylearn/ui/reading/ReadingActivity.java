package com.studying.easylearn.ui.reading;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.Story;
import com.studying.easylearn.data.entities.UserStoryProgress;
import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.data.entities.Vocabulary;
import com.studying.easylearn.databinding.ActivityReadingBinding;
import com.studying.easylearn.ui.review.ReviewActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadingActivity extends AppCompatActivity {

    private ActivityReadingBinding binding;
    private AppDatabase database;
    private SessionManager sessionManager;
    private int currentUserId = 1;
    private int storyId = 1;
    private TextToSpeech textToSpeech;
    private String currentStoryContent = "";
    private boolean isReadingAloud = false;
    private int currentProgress = 15;

    private final Set<String> learnedWordsInSession = new HashSet<>();
    private SpannableString storySpannable;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReadingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;
        storyId = getIntent().getIntExtra("STORY_ID", 1);

        initTextToSpeech();
        setupScrollProgressTracker();

        binding.btnBackReader.setOnClickListener(v -> finish());
        binding.btnAudio.setOnClickListener(v -> toggleStorySpeech());

        binding.btnCompleteReading.setOnClickListener(v -> {
            stopSpeech();
            currentProgress = 100;
            saveProgressForCurrentUser(100);

            Intent intent = new Intent(ReadingActivity.this, ReviewActivity.class);
            intent.putExtra("STORY_ID", storyId);
            startActivity(intent);
        });

        loadStoryFromDatabase();
    }

    private void saveProgressForCurrentUser(int progressValue) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserStoryProgress existing = database.userStoryProgressDao().getProgress(currentUserId, storyId);
            if (existing != null) {
                existing.setReadProgress(progressValue);
                existing.setLastReadTime(System.currentTimeMillis());
                database.userStoryProgressDao().insertOrUpdate(existing);
            } else {
                UserStoryProgress newProgress = new UserStoryProgress(
                        currentUserId,
                        storyId,
                        progressValue,
                        System.currentTimeMillis()
                );
                database.userStoryProgressDao().insertOrUpdate(newProgress);
            }
        });
    }

    private void setupScrollProgressTracker() {
        binding.scrollViewReading.setOnScrollChangeListener(
                (NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (v.getChildCount() > 0) {
                        int maxScroll = v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight();
                        if (maxScroll > 0) {
                            int percent = (int) ((scrollY * 100.0f) / maxScroll);
                            currentProgress = Math.min(90, Math.max(currentProgress, percent));
                        }
                    }
                }
        );
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setSpeechRate(0.95f);
                textToSpeech.setPitch(1.05f);
            }
        });
    }

    private void toggleStorySpeech() {
        if (textToSpeech == null || currentStoryContent.isEmpty()) return;

        if (isReadingAloud) {
            stopSpeech();
        } else {
            isReadingAloud = true;
            binding.btnAudio.setAlpha(0.6f);
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
            textToSpeech.speak(currentStoryContent, TextToSpeech.QUEUE_FLUSH, params, "STORY_TTS_ID");
        }
    }

    private void stopSpeech() {
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        isReadingAloud = false;
        binding.btnAudio.setAlpha(1.0f);
    }

    private void loadStoryFromDatabase() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Story story = database.storyDao().getStoryById(storyId);

            if (story == null) {
                story = new Story(
                        "The Secret in the Attic",
                        "Mia found an old wooden box covered in thick dust inside her grandmother's attic. She carefully opened the fragile brass lock with a rusty key. Inside the ancient chest, there was a stack of yellow handwritten letters tied with a delicate silk ribbon, describing a forgotten adventure in a distant land across the blue ocean.",
                        "Bí mật trên căn gác mái",
                        "A1",
                        "Đời sống",
                        3,
                        false
                );
            }

            // Lấy tiến độ đọc riêng của currentUserId
            UserStoryProgress userProgress = database.userStoryProgressDao().getProgress(currentUserId, storyId);
            int savedPercent = (userProgress != null) ? userProgress.getReadProgress() : 0;
            currentProgress = Math.max(savedPercent, 15);

            if (currentProgress < 100) {
                saveProgressForCurrentUser(currentProgress);
            }

            // Nạp lại các từ userId này đã từng tra trong truyện này để tô sáng sẵn
            List<UserWord> previouslySavedWords = database.userWordDao().getWordsByUserAndStory(currentUserId, storyId);
            if (previouslySavedWords != null) {
                for (UserWord uw : previouslySavedWords) {
                    learnedWordsInSession.add(uw.getWord().toLowerCase());
                }
            }

            final Story currentStory = story;
            currentStoryContent = currentStory.getContent();

            AppExecutors.getInstance().mainThread().post(() -> {
                binding.tvStoryTitle.setText(currentStory.getTitle());
                binding.tvStoryBadge.setText("📖 " + currentStory.getCategory() + " • " + currentStory.getReadTimeMinutes() + " min read");
                setupTapToTranslate(currentStory.getContent());
            });
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTapToTranslate(String rawText) {
        storySpannable = new SpannableString(rawText);
        Matcher matcher = Pattern.compile("[a-zA-Z]+").matcher(rawText);

        while (matcher.find()) {
            final int start = matcher.start();
            final int end = matcher.end();
            final String word = rawText.substring(start, end).toLowerCase().trim();

            WordClickableSpan span = new WordClickableSpan(word, start, end);
            storySpannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Tô nền xanh nhạt sẵn cho các từ user này đã tra trước đó
            if (learnedWordsInSession.contains(word)) {
                storySpannable.setSpan(
                        new BackgroundColorSpan(Color.parseColor("#DBEAFE")),
                        start,
                        end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        binding.tvStoryContent.setText(storySpannable, TextView.BufferType.SPANNABLE);
        binding.tvStoryContent.setHighlightColor(Color.TRANSPARENT);

        binding.tvStoryContent.setOnTouchListener(new View.OnTouchListener() {
            private BackgroundColorSpan hoverSpan;
            private float downX, downY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextView widget = (TextView) v;
                CharSequence text = widget.getText();
                if (!(text instanceof Spannable)) return false;
                Spannable buffer = (Spannable) text;

                int action = event.getAction();
                int x = (int) event.getX() - widget.getTotalPaddingLeft() + widget.getScrollX();
                int y = (int) event.getY() - widget.getTotalPaddingTop() + widget.getScrollY();

                Layout layout = widget.getLayout();
                if (layout == null) return false;

                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                WordClickableSpan[] links = buffer.getSpans(off, off, WordClickableSpan.class);

                if (action == MotionEvent.ACTION_DOWN) {
                    downX = event.getX();
                    downY = event.getY();
                    if (links.length > 0) {
                        hoverSpan = new BackgroundColorSpan(Color.parseColor("#BFDBFE"));
                        buffer.setSpan(hoverSpan, links[0].start, links[0].end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        widget.invalidate();
                        return true;
                    }
                } else if (action == MotionEvent.ACTION_UP) {
                    if (hoverSpan != null) {
                        buffer.removeSpan(hoverSpan);
                        hoverSpan = null;
                        widget.invalidate();
                    }
                    if (links.length > 0 && Math.abs(event.getX() - downX) < 30 && Math.abs(event.getY() - downY) < 30) {
                        onWordTapped(links[0], buffer, widget);
                        return true;
                    }
                } else if (action == MotionEvent.ACTION_CANCEL) {
                    if (hoverSpan != null) {
                        buffer.removeSpan(hoverSpan);
                        hoverSpan = null;
                        widget.invalidate();
                    }
                }
                return false;
            }
        });
    }

    private void onWordTapped(WordClickableSpan span, Spannable buffer, TextView widget) {
        long now = System.currentTimeMillis();
        if (now - lastClickTime < 250) return;
        lastClickTime = now;

        stopSpeech();

        if (currentProgress < 90) {
            currentProgress = Math.min(90, currentProgress + 5);
        }

        if (!learnedWordsInSession.contains(span.word)) {
            learnedWordsInSession.add(span.word);
            BackgroundColorSpan permanentBg = new BackgroundColorSpan(Color.parseColor("#DBEAFE"));
            buffer.setSpan(permanentBg, span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            widget.invalidate();
        }

        lookupWord(span.word);
    }

    private void lookupWord(String word) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Vocabulary vocab = database.vocabularyDao().findByWord(word);

            if (vocab != null) {
                AppExecutors.getInstance().mainThread().post(() -> displayWordBottomSheet(vocab));
            } else {
                Vocabulary apiVocab = fetchWordDataFast(word);

                List<Vocabulary> list = new ArrayList<>();
                list.add(apiVocab);
                database.vocabularyDao().insertVocabularies(list);

                AppExecutors.getInstance().mainThread().post(() -> displayWordBottomSheet(apiVocab));
            }
        });
    }

    private Vocabulary fetchWordDataFast(String word) {
        String phonetic = "/" + word + "/";
        String example = "Tap the speaker icon to listen to pronunciation.";
        String meaningVi = "";

        try {
            String encoded = URLEncoder.encode(word, "UTF-8");
            URL url = new URL("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=vi&dt=t&q=" + encoded);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray rootArray = new JSONArray(sb.toString());
                JSONArray firstBlock = rootArray.getJSONArray(0);
                StringBuilder translatedBuilder = new StringBuilder();
                for (int i = 0; i < firstBlock.length(); i++) {
                    translatedBuilder.append(firstBlock.getJSONArray(i).getString(0));
                }
                meaningVi = translatedBuilder.toString().trim();
            }
        } catch (Exception ignored) {}

        try {
            URL dictUrl = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
            HttpURLConnection connDict = (HttpURLConnection) dictUrl.openConnection();
            connDict.setRequestMethod("GET");
            connDict.setConnectTimeout(2500);
            connDict.setReadTimeout(2500);

            if (connDict.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connDict.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray jsonArray = new JSONArray(sb.toString());
                JSONObject entry = jsonArray.getJSONObject(0);
                if (entry.has("phonetic") && !entry.getString("phonetic").isEmpty()) {
                    phonetic = entry.getString("phonetic");
                }
                JSONArray meanings = entry.getJSONArray("meanings");
                JSONArray defs = meanings.getJSONObject(0).getJSONArray("definitions");
                if (defs.getJSONObject(0).has("example")) {
                    example = defs.getJSONObject(0).getString("example");
                }
            }
        } catch (Exception ignored) {}

        if (meaningVi.isEmpty()) {
            meaningVi = "Từ vựng tiếng Anh (" + word + ")";
        }

        meaningVi = meaningVi.substring(0, 1).toUpperCase() + meaningVi.substring(1);

        return new Vocabulary(word, phonetic, meaningVi, example);
    }

    private void displayWordBottomSheet(Vocabulary vocab) {
        if (isFinishing() || isDestroyed()) return;

        // Lưu từ vựng gắn với chính xác currentUserId
        AppExecutors.getInstance().diskIO().execute(() -> {
            UserWord userWord = new UserWord(
                    currentUserId,
                    vocab.getWord(),
                    vocab.getPhonetic(),
                    vocab.getMeaning(),
                    false,
                    storyId
            );
            database.userWordDao().insertUserWord(userWord);
        });

        Fragment prev = getSupportFragmentManager().findFragmentByTag("WordDetailBottomSheet");
        if (prev instanceof WordDetailBottomSheet) {
            ((WordDetailBottomSheet) prev).dismissAllowingStateLoss();
        }

        WordDetailBottomSheet bottomSheet = WordDetailBottomSheet.newInstance(
                vocab.getWord(),
                vocab.getPhonetic(),
                vocab.getMeaning(),
                vocab.getExample()
        );
        bottomSheet.show(getSupportFragmentManager(), "WordDetailBottomSheet");
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSpeech();
        if (currentProgress < 100) {
            saveProgressForCurrentUser(currentProgress);
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

    private class WordClickableSpan extends ClickableSpan {
        final String word;
        final int start;
        final int end;

        WordClickableSpan(String word, int start, int end) {
            this.word = word;
            this.start = start;
            this.end = end;
        }

        @Override
        public void onClick(@NonNull View widget) {
        }

        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setUnderlineText(false);
            if (learnedWordsInSession.contains(word)) {
                ds.setColor(Color.parseColor("#1D61F2"));
                ds.setFakeBoldText(true);
            } else {
                ds.setColor(Color.parseColor("#1E293B"));
                ds.setFakeBoldText(false);
            }
        }
    }
}