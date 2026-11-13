package com.studying.easylearn.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.data.entities.UserWord;
import com.studying.easylearn.databinding.ActivityProfileBinding;
import com.studying.easylearn.ui.auth.LoginActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.LevelManager;
import com.studying.easylearn.utils.SessionManager;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private AppDatabase database;
    private User currentUser;
    private int currentUserId = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        database = AppDatabase.getInstance(this);
        currentUserId = sessionManager.getUserId() > 0 ? sessionManager.getUserId() : 1;

        loadProfileFromUsersTable();
        setupListeners();
    }

    private void loadProfileFromUsersTable() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            currentUser = database.userDao().getUserById(currentUserId);

            // Đếm số lượng từ vựng đã thuộc của user này
            List<UserWord> userWords = database.userWordDao().getAllWordsByUser(currentUserId);
            int masteredCount = 0;
            if (userWords != null) {
                for (UserWord w : userWords) {
                    if (w.isMastered()) {
                        masteredCount++;
                    }
                }
            }
            final int finalMasteredCount = masteredCount;

            AppExecutors.getInstance().mainThread().post(() -> {
                if (currentUser != null) {
                    binding.tvProfileName.setText(currentUser.getFullName());
                    binding.tvProfileEmail.setText(currentUser.getEmail());

                    int stories = currentUser.getStoriesCompleted();
                    String level = currentUser.getCurrentLevel();

                    // Tính phần trăm thăng hạng tới cấp tiếp theo
                    int progress = LevelManager.calculateProgressToNextLevel(stories, finalMasteredCount);

                    if ("C2".equals(level)) {
                        binding.tvProfileLevelBadge.setText("🎖 " + level + " (Max Level 🎉)");
                    } else {
                        binding.tvProfileLevelBadge.setText("🎖 " + level + " (Tiến tới cấp mới: " + progress + "%)");
                    }

                    // 3 chỉ số thống kê
                    binding.tvStatStreak.setText(currentUser.getStreakCount() + " ngày");
                    binding.tvStatStoriesCompleted.setText(stories + " truyện");
                    binding.tvStatDailyGoal.setText(currentUser.getDailyGoalMinutes() + " phút");

                    binding.tvDailyGoalValue.setText(currentUser.getDailyGoalMinutes() + " phút / ngày ❯");
                }
            });
        });
    }

    private void setupListeners() {
        binding.btnBackProfile.setOnClickListener(v -> finish());
        binding.btnEditName.setOnClickListener(v -> showEditNameDialog());
        // Đã gỡ bỏ binding.tvProfileLevelBadge.setOnClickListener() để chặn việc tự chọn cấp độ
        binding.rowDailyGoal.setOnClickListener(v -> showDailyGoalDialog());
        binding.btnLogoutProfile.setOnClickListener(v -> showLogoutDialog());
    }

    private void showEditNameDialog() {
        EditText input = new EditText(this);
        input.setText(binding.tvProfileName.getText().toString());
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Cập nhật tên hiển thị")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        sessionManager.updateFullName(newName);
                        binding.tvProfileName.setText(newName);

                        AppExecutors.getInstance().diskIO().execute(() ->
                                database.userDao().updateUserName(currentUserId, newName)
                        );
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDailyGoalDialog() {
        String[] options = {"3 phút / ngày (Nhẹ nhàng)", "5 phút / ngày (Tiêu chuẩn)", "10 phút / ngày (Nâng cao)"};
        int[] values = {3, 5, 10};

        new AlertDialog.Builder(this)
                .setTitle("Chọn mục tiêu đọc mỗi ngày")
                .setItems(options, (dialog, which) -> {
                    int chosenMinutes = values[which];
                    sessionManager.setDailyGoalMinutes(chosenMinutes);
                    binding.tvDailyGoalValue.setText(chosenMinutes + " phút / ngày ❯");
                    binding.tvStatDailyGoal.setText(chosenMinutes + " phút");

                    AppExecutors.getInstance().diskIO().execute(() ->
                            database.userDao().updateDailyGoal(currentUserId, chosenMinutes)
                    );
                })
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    sessionManager.logoutUser();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileFromUsersTable();
    }
}