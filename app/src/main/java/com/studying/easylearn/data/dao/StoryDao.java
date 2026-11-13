package com.studying.easylearn.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.studying.easylearn.data.entities.Story;
import java.util.List;

@Dao
public interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStories(List<Story> stories);

    // Lấy 1 truyện theo ID
    @Query("SELECT * FROM stories WHERE id = :storyId LIMIT 1")
    Story getStoryById(int storyId);

    // =========================================================================
    // CÁC HÀM CŨ (Giữ lại tạm để tránh lỗi đỏ ở các màn hình chưa kịp sửa)
    // =========================================================================
    @Query("SELECT * FROM stories ORDER BY id ASC")
    List<Story> getAllStories();

    @Query("SELECT * FROM stories WHERE level = :level ORDER BY id ASC")
    List<Story> getStoriesByLevel(String level);

    @Query("SELECT * FROM stories WHERE is_recent = 0 ORDER BY id ASC")
    List<Story> getSuggestedStories();

    @Query("SELECT * FROM stories WHERE is_recent = 1 LIMIT 1")
    Story getRecentStory();

    // Lấy toàn bộ truyện cho 1 user, đẩy truyện đã hoàn thành (>=100%) xuống cuối
    @Query("SELECT s.*, COALESCE(p.read_progress, 0) AS read_progress " +
            "FROM stories s " +
            "LEFT JOIN user_story_progress p ON s.id = p.story_id AND p.user_id = :userId " +
            "ORDER BY (COALESCE(p.read_progress, 0) >= 100) ASC, s.id ASC")
    List<Story> getAllStoriesForUser(int userId);

    // Lọc truyện theo Level cho 1 user, đẩy truyện hoàn thành xuống cuối
    @Query("SELECT s.*, COALESCE(p.read_progress, 0) AS read_progress " +
            "FROM stories s " +
            "LEFT JOIN user_story_progress p ON s.id = p.story_id AND p.user_id = :userId " +
            "WHERE s.level = :level " +
            "ORDER BY (COALESCE(p.read_progress, 0) >= 100) ASC, s.id ASC")
    List<Story> getStoriesByLevelForUser(String level, int userId);

    // Lấy danh sách truyện gợi ý (chỉ lấy những truyện user CHƯA đọc xong)
    @Query("SELECT s.*, COALESCE(p.read_progress, 0) AS read_progress " +
            "FROM stories s " +
            "LEFT JOIN user_story_progress p ON s.id = p.story_id AND p.user_id = :userId " +
            "WHERE COALESCE(p.read_progress, 0) < 100 " +
            "ORDER BY s.id ASC")
    List<Story> getSuggestedStoriesForUser(int userId);
}