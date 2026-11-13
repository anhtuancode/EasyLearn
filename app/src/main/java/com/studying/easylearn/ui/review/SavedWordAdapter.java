package com.studying.easylearn.ui.review;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.databinding.ItemSavedWordBinding;

import java.util.ArrayList;
import java.util.List;

public class SavedWordAdapter extends RecyclerView.Adapter<SavedWordAdapter.WordViewHolder> {

    public interface OnWordSpeakListener {
        void onSpeak(String word);
    }

    private List<UserWord> wordList = new ArrayList<>();
    private final OnWordSpeakListener speakListener;

    public SavedWordAdapter(OnWordSpeakListener speakListener) {
        this.speakListener = speakListener;
    }

    public void setWords(List<UserWord> words) {
        this.wordList = words;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSavedWordBinding binding = ItemSavedWordBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new WordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        UserWord item = wordList.get(position);
        holder.binding.tvSavedWord.setText(item.getWord());
        holder.binding.tvSavedPhonetic.setText(item.getPhonetic());
        holder.binding.tvSavedMeaning.setText(item.getMeaning());

        holder.binding.btnItemSpeak.setOnClickListener(v -> {
            if (speakListener != null) {
                speakListener.onSpeak(item.getWord());
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList != null ? wordList.size() : 0;
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        final ItemSavedWordBinding binding;

        WordViewHolder(@NonNull ItemSavedWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}