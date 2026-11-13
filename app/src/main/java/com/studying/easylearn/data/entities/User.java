package com.studying.easylearn.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"email"}, unique = true)}
)
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password")
    private String password;

    @ColumnInfo(name = "full_name")
    private String fullName;

    @ColumnInfo(name = "auth_provider")
    private String authProvider;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // --- CÁC TRƯỜNG MỚI LƯU TRỮ TIẾN ĐỘ CÁ NHÂN ---
    @ColumnInfo(name = "streak_count")
    private int streakCount;

    @ColumnInfo(name = "current_level")
    private String currentLevel; // "A1", "A2", "B1"

    @ColumnInfo(name = "daily_goal_minutes")
    private int dailyGoalMinutes;

    @ColumnInfo(name = "stories_completed")
    private int storiesCompleted;

    @ColumnInfo(name = "last_study_date")
    private String lastStudyDate; // Định dạng "yyyy-MM-dd" để tính chuỗi ngày liên tiếp

    // Constructor chính Room sử dụng
    public User(String email, String password, String fullName, String authProvider, long createdAt,
                int streakCount, String currentLevel, int dailyGoalMinutes, int storiesCompleted, String lastStudyDate) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.authProvider = authProvider;
        this.createdAt = createdAt;
        this.streakCount = streakCount;
        this.currentLevel = currentLevel;
        this.dailyGoalMinutes = dailyGoalMinutes;
        this.storiesCompleted = storiesCompleted;
        this.lastStudyDate = lastStudyDate;
    }

    // Giữ lại Constructor cũ với @Ignore để tương thích ngược với LoginActivity / SignUpActivity
    @Ignore
    public User(String email, String password, String fullName, String authProvider, long createdAt) {
        this(email, password, fullName, authProvider, createdAt, 1, "A1", 3, 0, "");
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public String getCurrentLevel() { return currentLevel != null ? currentLevel : "A1"; }
    public void setCurrentLevel(String currentLevel) { this.currentLevel = currentLevel; }

    public int getDailyGoalMinutes() { return dailyGoalMinutes > 0 ? dailyGoalMinutes : 3; }
    public void setDailyGoalMinutes(int dailyGoalMinutes) { this.dailyGoalMinutes = dailyGoalMinutes; }

    public int getStoriesCompleted() { return storiesCompleted; }
    public void setStoriesCompleted(int storiesCompleted) { this.storiesCompleted = storiesCompleted; }

    public String getLastStudyDate() { return lastStudyDate != null ? lastStudyDate : ""; }
    public void setLastStudyDate(String lastStudyDate) { this.lastStudyDate = lastStudyDate; }
}