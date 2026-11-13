package com.studying.easylearn.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.studying.easylearn.data.entities.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    // Cập nhật mật khẩu mới khi xác thực OTP thành công
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    int updatePassword(String email, String newPassword);

    @Query("UPDATE users SET full_name = :fullName WHERE id = :userId")
    void updateUserName(int userId, String fullName);

    @Query("UPDATE users SET daily_goal_minutes = :minutes WHERE id = :userId")
    void updateDailyGoal(int userId, int minutes);

    @Query("UPDATE users SET current_level = :level WHERE id = :userId")
    void updateUserLevel(int userId, String level);

    @Query("UPDATE users SET streak_count = :streak, stories_completed = :storiesCount, last_study_date = :todayDate WHERE id = :userId")
    void updateStudyProgress(int userId, int streak, int storiesCount, String todayDate);
}