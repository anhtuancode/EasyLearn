package com.studying.easylearn.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_words")
public class UserWord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    private String word;
    private String phonetic;
    private String meaning;
    private boolean isMastered;
    private int storyId;

    // --- CÁC TRƯỜNG DỮ LIỆU MỚI CHO SRS ---

    @ColumnInfo(name = "box_level", defaultValue = "0")
    private int boxLevel; // Cấp độ ghi nhớ (0 đến 5)

    @ColumnInfo(name = "interval_days", defaultValue = "0")
    private int intervalDays; // Khoảng cách ngày trước lần ôn tiếp theo

    @ColumnInfo(name = "next_review_time", defaultValue = "0")
    private long nextReviewTime; // Timestamp mốc thời gian cần ôn tập lại

    // Constructor đầy đủ
    public UserWord(int userId, String word, String phonetic, String meaning, boolean isMastered, int storyId, int boxLevel, int intervalDays, long nextReviewTime) {
        this.userId = userId;
        this.word = word;
        this.phonetic = phonetic;
        this.meaning = meaning;
        this.isMastered = isMastered;
        this.storyId = storyId;
        this.boxLevel = boxLevel;
        this.intervalDays = intervalDays;
        this.nextReviewTime = nextReviewTime;
    }

    // Constructor rút gọn cho code cũ (Khởi tạo mốc ôn tập là NGAY BÂY GIỜ)
    @Ignore
    public UserWord(int userId, String word, String phonetic, String meaning, boolean isMastered, int storyId) {
        this(userId, word, phonetic, meaning, isMastered, storyId, 0, 0, System.currentTimeMillis());
    }

    @Ignore
    public UserWord(String word, String phonetic, String meaning, boolean isMastered, int storyId) {
        this(1, word, phonetic, meaning, isMastered, storyId, 0, 0, System.currentTimeMillis());
    }

    // --- GETTERS & SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public boolean isMastered() { return isMastered; }
    public void setMastered(boolean mastered) { this.isMastered = mastered; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getBoxLevel() { return boxLevel; }
    public void setBoxLevel(int boxLevel) { this.boxLevel = boxLevel; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public long getNextReviewTime() { return nextReviewTime; }
    public void setNextReviewTime(long nextReviewTime) { this.nextReviewTime = nextReviewTime; }
}