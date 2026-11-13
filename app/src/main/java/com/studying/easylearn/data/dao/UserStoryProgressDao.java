package com.studying.easylearn.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.studying.easylearn.data.entities.UserStoryProgress;

import java.util.List;

@Dao
public interface UserStoryProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(UserStoryProgress progress);

    @Query("SELECT * FROM user_story_progress WHERE user_id = :userId AND story_id = :storyId LIMIT 1")
    UserStoryProgress getProgress(int userId, int storyId);

    // Lấy truyện đang đọc dở (< 100%) gần nhất của riêng userId này
    @Query("SELECT * FROM user_story_progress WHERE user_id = :userId AND read_progress < 100 ORDER BY last_read_time DESC LIMIT 1")
    UserStoryProgress getLatestInProgressStory(int userId);

    // Đếm chính xác số truyện KHÁC NHAU đã đọc xong (100%) -> Đọc lại truyện cũ không bị cộng trùng
    @Query("SELECT COUNT(DISTINCT story_id) FROM user_story_progress WHERE user_id = :userId AND read_progress >= 100")
    int getCompletedStoriesCount(int userId);

    // Lấy danh sách tất cả tiến độ truyện của user (dùng để hiển thị trạng thái Đã đọc/Đang đọc bên StoriesActivity)
    @Query("SELECT * FROM user_story_progress WHERE user_id = :userId")
    List<UserStoryProgress> getAllProgressByUser(int userId);
}