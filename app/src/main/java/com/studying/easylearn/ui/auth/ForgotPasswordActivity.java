package com.studying.easylearn.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.databinding.ActivityForgotPasswordBinding;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.EmailSender;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLogin.setOnClickListener(v -> finish());
        binding.btnSendCode.setOnClickListener(v -> handleSendCode());
    }

    private void handleSendCode() {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Vui lòng nhập đúng email đã đăng ký");
            return;
        }
        binding.tilEmail.setError(null);

        binding.btnSendCode.setEnabled(false);
        Toast.makeText(this, "Đang gửi mã xác nhận đến email...", Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().diskIO().execute(() -> {
            User user = database.userDao().getUserByEmail(email);

            if (user == null) {
                AppExecutors.getInstance().mainThread().post(() -> {
                    binding.btnSendCode.setEnabled(true);
                    binding.tilEmail.setError("Email này chưa đăng ký tài khoản nào");
                });
                return;
            }

            // Tạo mã OTP 6 số và gửi qua Gmail
            String otpCode = EmailSender.generateOtp();
            boolean isEmailSent = EmailSender.sendOtpEmail(email, otpCode);

            AppExecutors.getInstance().mainThread().post(() -> {
                binding.btnSendCode.setEnabled(true);
                if (isEmailSent) {
                    Toast.makeText(ForgotPasswordActivity.this, "Đã gửi mã xác thực tới email!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpActivity.class);
                    intent.putExtra("TARGET_EMAIL", email);
                    intent.putExtra("OTP_CODE", otpCode);
                    startActivity(intent);
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Gửi email thất bại. Hãy kiểm tra kết nối mạng và tài khoản SMTP!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}