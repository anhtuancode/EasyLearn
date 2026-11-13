package com.studying.easylearn.ui.reading;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.studying.easylearn.databinding.DialogBottomSheetWordBinding;
import java.util.Locale;

public class WordDetailBottomSheet extends BottomSheetDialogFragment {

    private DialogBottomSheetWordBinding binding;
    private TextToSpeech textToSpeech;

    private static final String ARG_WORD = "word";
    private static final String ARG_PHONETIC = "phonetic";
    private static final String ARG_MEANING = "meaning";
    private static final String ARG_EXAMPLE = "example";

    public static WordDetailBottomSheet newInstance(String word, String phonetic, String meaning, String example) {
        WordDetailBottomSheet fragment = new WordDetailBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        args.putString(ARG_PHONETIC, phonetic);
        args.putString(ARG_MEANING, meaning);
        args.putString(ARG_EXAMPLE, example);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogBottomSheetWordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            String word = getArguments().getString(ARG_WORD, "");
            String phonetic = getArguments().getString(ARG_PHONETIC, "");
            String meaning = getArguments().getString(ARG_MEANING, "");
            String example = getArguments().getString(ARG_EXAMPLE, "");

            binding.tvSheetWord.setText(word);
            binding.tvSheetPhonetic.setText(phonetic.isEmpty() ? "/.../" : phonetic);
            binding.tvSheetMeaning.setText(meaning);
            binding.tvSheetExample.setText(example.isEmpty() ? "No example available." : example);

            // Tích hợp TextToSpeech phát âm tiếng Anh chuẩn khi bấm loa
            textToSpeech = new TextToSpeech(requireContext(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech.setLanguage(Locale.US);
                }
            });

            binding.btnPronounce.setOnClickListener(v -> {
                if (textToSpeech != null && !word.isEmpty()) {
                    textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroyView();
        binding = null;
    }
}