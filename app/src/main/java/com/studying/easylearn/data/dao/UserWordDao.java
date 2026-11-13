package com.studying.easylearn.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.studying.easylearn.data.entities.UserWord;

import java.util.List;

@Dao
public interface UserWordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserWord(UserWord userWord);

    @Query("SELECT * FROM user_words WHERE user_id = :userId AND storyId = :storyId ORDER BY id DESC")
    List<UserWord> getWordsByUserAndStory(int userId, int storyId);

    @Query("SELECT * FROM user_words WHERE user_id = :userId ORDER BY id DESC")
    List<UserWord> getAllWordsByUser(int userId);

    @Query("UPDATE user_words SET isMastered = :mastered WHERE id = :wordId")
    void updateMasteredStatus(int wordId, boolean mastered);

    @Query("DELETE FROM user_words WHERE id = :wordId")
    void deleteUserWordById(int wordId);

    // =========================================================================
    // CÁC HÀM XỬ LÝ FLASHCARD SRS
    // =========================================================================

    // 1. Đếm số lượng từ cần ôn tập hôm nay (Đã đến hạn và chưa thuộc hoàn toàn)
    @Query("SELECT COUNT(DISTINCT word) FROM user_words WHERE user_id = :userId AND isMastered = 0 AND next_review_time <= :currentTime")
    int countSrsWordsDueToday(int userId, long currentTime);

    // 2. Lấy danh sách các từ vựng cần ôn tập hôm nay để nạp vào màn hình Flashcard
    @Query("SELECT * FROM user_words WHERE user_id = :userId AND isMastered = 0 AND next_review_time <= :currentTime GROUP BY word ORDER BY next_review_time ASC LIMIT :limit")
    List<UserWord> getSrsWordsDueToday(int userId, long currentTime, int limit);

    // 3. Cập nhật lại chỉ số thuật toán khi người dùng lật thẻ (Nhớ/Quên)
    @Query("UPDATE user_words SET box_level = :boxLevel, interval_days = :intervalDays, next_review_time = :nextReviewTime, isMastered = :isMastered WHERE id = :wordId")
    void updateSrsProgress(int wordId, int boxLevel, int intervalDays, long nextReviewTime, boolean isMastered);

    // Đếm tổng số từ đã thuộc hoàn toàn để tính % thăng hạng Level
    @Query("SELECT COUNT(DISTINCT word) FROM user_words WHERE user_id = :userId AND isMastered = 1")
    int countMasteredWords(int userId);
}