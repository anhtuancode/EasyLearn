package com.studying.easylearn.ui.vocabulary;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.databinding.ActivityVocabularyBinding;
import com.studying.easylearn.ui.main.MainActivity;
import com.studying.easylearn.ui.profile.ProfileActivity;
import com.studying.easylearn.ui.reading.StoriesActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.SessionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VocabularyActivity extends AppCompatActivity {

    private ActivityVocabularyBinding binding;
    private AppDatabase database;
    private SessionManager sessionManager;
    private int currentUserId = 1;
    private TextToSpeech textToSpeech;
    private VocabularyAdapter adapter;

    private List<UserWord> allUniqueWords = new ArrayList<>();
    private String currentFilter = "ALL"; // "ALL", "LEARNING", "MASTERED"
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVocabularyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;

        initTextToSpeech();
        setupRecyclerView();
        setupFilterAndSearch();
        setupBottomNavigation();
        loadUserVocabulary();
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
            textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "VOCAB_TTS");
        }
    }

    private void setupRecyclerView() {
        adapter = new VocabularyAdapter(new VocabularyAdapter.OnWordActionListener() {
            @Override
            public void onSpeakWord(String word) {
                speakWord(word);
            }

            @Override
            public void onToggleMastered(UserWord word) {
                boolean newStatus = !word.isMastered();
                word.setMastered(newStatus);
                applyFilterAndSearch();

                AppExecutors.getInstance().diskIO().execute(() ->
                        database.userWordDao().updateMasteredStatus(word.getId(), newStatus)
                );
            }

            @Override
            public void onDeleteWord(UserWord word) {
                new AlertDialog.Builder(VocabularyActivity.this)
                        .setTitle("Xóa từ vựng")
                        .setMessage("Bạn muốn xóa từ \"" + word.getWord() + "\" khỏi sổ tay?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            allUniqueWords.remove(word);
                            binding.tvVocabTotalBadge.setText(allUniqueWords.size() + " từ");
                            applyFilterAndSearch();

                            AppExecutors.getInstance().diskIO().execute(() ->
                                    database.userWordDao().deleteUserWordById(word.getId())
                            );
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        binding.rvVocabularyList.setLayoutManager(new LinearLayoutManager(this));
        binding.rvVocabularyList.setAdapter(adapter);
    }

    private void setupFilterAndSearch() {
        binding.chipFilterAll.setOnClickListener(v -> selectFilterChip("ALL", binding.chipFilterAll));
        binding.chipFilterLearning.setOnClickListener(v -> selectFilterChip("LEARNING", binding.chipFilterLearning));
        binding.chipFilterMastered.setOnClickListener(v -> selectFilterChip("MASTERED", binding.chipFilterMastered));

        binding.edtSearchVocabulary.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                applyFilterAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void selectFilterChip(String filterCode, MaterialButton selectedBtn) {
        currentFilter = filterCode;
        MaterialButton[] chips = {binding.chipFilterAll, binding.chipFilterLearning, binding.chipFilterMastered};
        for (MaterialButton btn : chips) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EFF6FF")));
            btn.setTextColor(Color.parseColor("#1D61F2"));
        }
        selectedBtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1D61F2")));
        selectedBtn.setTextColor(Color.WHITE);

        applyFilterAndSearch();
    }

    private void loadUserVocabulary() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<UserWord> rawWords = database.userWordDao().getAllWordsByUser(currentUserId);

            // Lọc bỏ từ trùng lặp (ưu tiên giữ bản ghi mới nhất)
            Map<String, UserWord> uniqueMap = new LinkedHashMap<>();
            if (rawWords != null) {
                for (UserWord uw : rawWords) {
                    if (uw.getWord() != null) {
                        String key = uw.getWord().toLowerCase().trim();
                        if (!uniqueMap.containsKey(key)) {
                            uniqueMap.put(key, uw);
                        }
                    }
                }
            }

            allUniqueWords = new ArrayList<>(uniqueMap.values());

            AppExecutors.getInstance().mainThread().post(() -> {
                binding.tvVocabTotalBadge.setText(allUniqueWords.size() + " từ");
                applyFilterAndSearch();
            });
        });
    }

    private void applyFilterAndSearch() {
        List<UserWord> filtered = new ArrayList<>();
        for (UserWord uw : allUniqueWords) {
            boolean matchesStatus = true;
            if ("LEARNING".equals(currentFilter)) {
                matchesStatus = !uw.isMastered();
            } else if ("MASTERED".equals(currentFilter)) {
                matchesStatus = uw.isMastered();
            }

            boolean matchesQuery = currentSearchQuery.isEmpty()
                    || uw.getWord().toLowerCase().contains(currentSearchQuery)
                    || (uw.getMeaning() != null && uw.getMeaning().toLowerCase().contains(currentSearchQuery));

            if (matchesStatus && matchesQuery) {
                filtered.add(uw);
            }
        }

        adapter.setWords(filtered);
        binding.layoutEmptyVocabulary.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvVocabularyList.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupBottomNavigation() {
        binding.tabHome.setOnClickListener(v -> {
            Intent intent = new Intent(VocabularyActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        binding.tabStories.setOnClickListener(v -> {
            startActivity(new Intent(VocabularyActivity.this, StoriesActivity.class));
            finish();
        });

        binding.tabVocabulary.setOnClickListener(v -> {
            // Đang ở màn hình Sổ từ
        });

        binding.tabProfile.setOnClickListener(v ->
                startActivity(new Intent(VocabularyActivity.this, ProfileActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserVocabulary();
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