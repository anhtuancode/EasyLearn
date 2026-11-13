package com.studying.easylearn.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.databinding.ActivityLoginBinding;
import com.studying.easylearn.ui.main.MainActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AppDatabase database;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        // Kiểm tra nếu phiên trước đã đăng nhập thành công thì đi thẳng vào trang chủ
        if (sessionManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        setupActions();
    }

    private void setupActions() {
        // Nút Đăng nhập
        binding.btnSignIn.setOnClickListener(v -> performLogin());

        // Chuyển sang màn hình Đăng ký
        binding.tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Chuyển sang màn hình Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Đăng nhập chế độ Khách (Guest)
        binding.tvContinueAsGuest.setOnClickListener(v -> {
            sessionManager.createLoginSession(-1, "guest@easylearn.local", "Guest Learner");
            Toast.makeText(this, "Đang vào ứng dụng với quyền Khách", Toast.LENGTH_SHORT).show();
            goToMainActivity();
        });
    }

    private void performLogin() {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";
        String password = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString().trim() : "";

        // Validate đầu vào
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email không được để trống");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Định dạng email không hợp lệ");
            return;
        }
        binding.tilEmail.setError(null);

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }
        binding.tilPassword.setError(null);

        binding.btnSignIn.setEnabled(false);

        // Truy vấn database Room trên Background Thread
        AppExecutors.getInstance().diskIO().execute(() -> {
            User user = database.userDao().login(email, password);

            AppExecutors.getInstance().mainThread().post(() -> {
                binding.btnSignIn.setEnabled(true);

                if (user != null) {
                    // Lưu phiên đăng nhập
                    sessionManager.createLoginSession(user.getId(), user.getEmail(), user.getFullName());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công! Chào " + user.getFullName(), Toast.LENGTH_SHORT).show();
                    goToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Email hoặc mật khẩu không chính xác!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Đóng LoginActivity
    }
}