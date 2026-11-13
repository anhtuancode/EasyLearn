package com.studying.easylearn.ui.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studying.easylearn.data.entities.Story;
import com.studying.easylearn.databinding.ItemStoryBinding;

import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }

    private List<Story> storyList = new ArrayList<>();
    private final OnStoryClickListener listener;

    public StoryAdapter(OnStoryClickListener listener) {
        this.listener = listener;
    }

    public void setStories(List<Story> stories) {
        this.storyList = stories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = storyList.get(position);

        holder.binding.tvStoryTitle.setText(story.getTitle());
        holder.binding.tvStoryDesc.setText(story.getDescription());
        holder.binding.tvStoryLevelBadge.setText(story.getLevel());
        holder.binding.tvStoryMeta.setText(story.getReadTimeMinutes() + " phút đọc • " + story.getCategory());

        // Xử lý sự kiện bấm vào nút Đọc thử hoặc bấm cả thẻ
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStoryClick(story);
        });

        holder.binding.btnReadTrial.setOnClickListener(v -> {
            if (listener != null) listener.onStoryClick(story);
        });
    }

    @Override
    public int getItemCount() {
        return storyList != null ? storyList.size() : 0;
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        final ItemStoryBinding binding;

        public StoryViewHolder(@NonNull ItemStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}