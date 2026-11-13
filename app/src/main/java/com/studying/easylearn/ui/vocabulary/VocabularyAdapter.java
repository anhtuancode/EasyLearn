package com.studying.easylearn.ui.vocabulary;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.databinding.ItemVocabularyWordBinding;

import java.util.ArrayList;
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabViewHolder> {

    public interface OnWordActionListener {
        void onSpeakWord(String word);
        void onToggleMastered(UserWord word);
        void onDeleteWord(UserWord word);
    }

    private List<UserWord> wordList = new ArrayList<>();
    private final OnWordActionListener listener;

    public VocabularyAdapter(OnWordActionListener listener) {
        this.listener = listener;
    }

    public void setWords(List<UserWord> words) {
        this.wordList = (words != null) ? words : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VocabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVocabularyWordBinding binding = ItemVocabularyWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new VocabViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabViewHolder holder, int position) {
        UserWord item = wordList.get(position);
        holder.binding.tvItemWord.setText(item.getWord());
        holder.binding.tvItemPhonetic.setText(item.getPhonetic());
        holder.binding.tvItemMeaning.setText(item.getMeaning());

        if (item.isMastered()) {
            holder.binding.tvMasteredToggle.setText("✓ Đã thuộc");
            holder.binding.tvMasteredToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
            holder.binding.tvMasteredToggle.setTextColor(Color.parseColor("#15803D"));
        } else {
            holder.binding.tvMasteredToggle.setText("Đang học");
            holder.binding.tvMasteredToggle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
            holder.binding.tvMasteredToggle.setTextColor(Color.parseColor("#D97706"));
        }

        holder.binding.btnSpeakItemWord.setOnClickListener(v -> {
            if (listener != null) listener.onSpeakWord(item.getWord());
        });

        holder.binding.tvMasteredToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleMastered(item);
        });

        holder.binding.btnDeleteItemWord.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteWord(item);
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    static class VocabViewHolder extends RecyclerView.ViewHolder {
        final ItemVocabularyWordBinding binding;

        VocabViewHolder(ItemVocabularyWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}