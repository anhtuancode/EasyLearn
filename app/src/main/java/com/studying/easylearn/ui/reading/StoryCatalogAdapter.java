package com.studying.easylearn.ui.reading;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studying.easylearn.data.entities.Story;
import com.studying.easylearn.databinding.ItemStoryCardBinding;

import java.util.ArrayList;
import java.util.List;

public class StoryCatalogAdapter extends RecyclerView.Adapter<StoryCatalogAdapter.StoryViewHolder> {

    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }

    private List<Story> stories = new ArrayList<>();
    private final OnStoryClickListener listener;

    public StoryCatalogAdapter(OnStoryClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<Story> newStories) {
        this.stories = newStories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryCardBinding binding = ItemStoryCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story item = stories.get(position);

        holder.binding.tvCardTitle.setText(item.getTitle());
        holder.binding.tvCardSnippet.setText(item.getContent());
        holder.binding.tvCardLevel.setText(item.getLevel() + (item.getLevel().equals("A1") ? " Beginner" : item.getLevel().equals("A2") ? " Elementary" : " Intermediate"));
        holder.binding.tvCardReadTime.setText("⏱ " + item.getReadTimeMinutes() + " phút");
        holder.binding.tvCardCategory.setText(item.getCategory());

        // HIỂN THỊ TRẠNG THÁI "ĐÃ HOÀN THÀNH" DỰA VÀO READ PROGRESS
        if (item.getReadProgress() >= 100) {
            // Đổi nền thẻ thành xám nhạt (Giả sử Root của item là MaterialCardView)
            holder.binding.getRoot().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8FAFC")));

            // Text trạng thái màu xanh lá
            holder.binding.tvCardFooterStatus.setText("✓ Đã đọc xong");
            holder.binding.tvCardFooterStatus.setTextColor(Color.parseColor("#16A34A"));

            // Đổi text nút bấm
            holder.binding.btnCardAction.setText("Đọc lại ↻");
        } else {
            // Trạng thái bình thường (nền trắng)
            holder.binding.getRoot().setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));

            // Text trạng thái màu xám mặc định
            holder.binding.tvCardFooterStatus.setText(item.getDescription().isEmpty() ? "Gợi ý cho hôm nay" : item.getDescription());
            holder.binding.tvCardFooterStatus.setTextColor(Color.parseColor("#64748B"));

            holder.binding.btnCardAction.setText("Bắt đầu đọc ➔");
        }

        // Bấm vào item hoặc nút CTA để mở ReadingActivity
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onStoryClick(item);
        });

        holder.binding.btnCardAction.setOnClickListener(v -> {
            if (listener != null) listener.onStoryClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return stories != null ? stories.size() : 0;
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        final ItemStoryCardBinding binding;

        public StoryViewHolder(@NonNull ItemStoryCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}