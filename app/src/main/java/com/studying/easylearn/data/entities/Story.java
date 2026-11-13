package com.studying.easylearn.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "stories")
public class Story {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "content")
    private String content;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "level")
    private String level; // "A1", "A2", "B1"

    @ColumnInfo(name = "category")
    private String category; // Đời sống, Viễn tưởng, Văn hóa...

    @ColumnInfo(name = "read_time")
    private int readTimeMinutes;

    @ColumnInfo(name = "is_recent", defaultValue = "0")
    private boolean isRecent;

    // Constructor rỗng bắt buộc cho Room Database
    public Story() {
    }

    // Constructor tiện lợi 6 tham số nạp dữ liệu mẫu
    @Ignore
    public Story(String title, String content, String description, String level, String category, int readTimeMinutes, boolean isRecent) {
        this.title = title;
        this.content = content;
        this.description = description;
        this.level = level;
        this.category = category;
        this.readTimeMinutes = readTimeMinutes;
        this.isRecent = isRecent;
    }

    // --- GETTERS & SETTERS ĐƯỢC ĐỒNG BỘ ĐẦY ĐỦ ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level != null ? level : "A1"; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category != null ? category : "Chung"; }
    public void setCategory(String category) { this.category = category; }

    // Hỗ trợ thêm alias getGenre() để Adapter gọi không bị lỗi
    public String getGenre() { return getCategory(); }

    public int getReadTimeMinutes() { return readTimeMinutes; }
    public void setReadTimeMinutes(int readTimeMinutes) { this.readTimeMinutes = readTimeMinutes; }

    // Hỗ trợ thêm alias getReadMinutes() để Adapter gọi không bị lỗi
    public int getReadMinutes() { return getReadTimeMinutes(); }

    public boolean isRecent() { return isRecent; }
    public void setRecent(boolean recent) { isRecent = recent; }

    @ColumnInfo(name = "read_progress")
    private int readProgress; // Từ 0 đến 100 (%)

    @ColumnInfo(name = "last_read_time")
    private long lastReadTime; // Thời điểm mở đọc gần nhất (timestamp)

    // Thêm Getter & Setter cho 2 trường này:
    public int getReadProgress() { return readProgress; }
    public void setReadProgress(int readProgress) { this.readProgress = readProgress; }

    public long getLastReadTime() { return lastReadTime; }
    public void setLastReadTime(long lastReadTime) { this.lastReadTime = lastReadTime; }
}