package com.studying.easylearn.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_story_progress")
public class UserStoryProgress {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "user_id")
    private int userId;

    @ColumnInfo(name = "story_id")
    private int storyId;

    @ColumnInfo(name = "read_progress")
    private int readProgress; // 0 -> 100 (%)

    @ColumnInfo(name = "last_read_time")
    private long lastReadTime;

    public UserStoryProgress(int userId, int storyId, int readProgress, long lastReadTime) {
        this.userId = userId;
        this.storyId = storyId;
        this.readProgress = readProgress;
        this.lastReadTime = lastReadTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getReadProgress() { return readProgress; }
    public void setReadProgress(int readProgress) { this.readProgress = readProgress; }

    public long getLastReadTime() { return lastReadTime; }
    public void setLastReadTime(long lastReadTime) { this.lastReadTime = lastReadTime; }
}