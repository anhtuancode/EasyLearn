package com.studying.easylearn.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.databinding.ActivityVerifyOtpBinding;
import com.studying.easylearn.ui.main.MainActivity;
import com.studying.easylearn.utils.AppExecutors;
import com.studying.easylearn.utils.EmailSender;
import com.studying.easylearn.utils.SessionManager;

import java.util.Locale;

public class VerifyOtpActivity extends AppCompatActivity {

    private ActivityVerifyOtpBinding binding;
    private AppDatabase database;
    private SessionManager sessionManager;

    private String targetEmail;
    private String currentOtp;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = AppDatabase.getInstance(this);
        sessionManager = new SessionManager(this);

        // Nhận email và mã OTP từ ForgotPasswordActivity gửi sang
        targetEmail = getIntent().getStringExtra("TARGET_EMAIL");
        currentOtp = getIntent().getStringExtra("OTP_CODE");

        if (targetEmail != null) {
            binding.tvSentEmail.setText(targetEmail);
        }

        setupOtpAutoJump();
        startResendTimer(60);

        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());
        binding.imgEditEmail.setOnClickListener(v -> finish());

        // Bấm gửi lại mã OTP khi đồng hồ chạy hết
        binding.tvResendTimer.setOnClickListener(v -> {
            if (binding.tvResendTimer.getText().toString().contains("Resend code now")) {
                resendOtpCode();
            }
        });

        // Nút Set New Password & Log In
        binding.btnSetNewPassword.setOnClickListener(v -> handlePasswordReset());
    }

    // 1. Tự động nhảy con trỏ qua 6 ô OTP
    private void setupOtpAutoJump() {
        EditText[] boxes = new EditText[]{
                binding.otpBox1, binding.otpBox2, binding.otpBox3,
                binding.otpBox4, binding.otpBox5, binding.otpBox6
        };

        for (int i = 0; i < boxes.length; i++) {
            final int index = i;
            boxes[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < boxes.length - 1) {
                        boxes[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        boxes[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    // 2. Đồng hồ đếm ngược 60 giây
    private void startResendTimer(int seconds) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = millisUntilFinished / 1000;
                binding.tvResendTimer.setText(String.format(Locale.getDefault(), "🕒 Resend code in 0:%02d", sec));
            }

            @Override
            public void onFinish() {
                binding.tvResendTimer.setText("Resend code now");
            }
        }.start();
    }

    // 3. Gửi lại mã OTP qua Gmail
    private void resendOtpCode() {
        currentOtp = EmailSender.generateOtp();
        Toast.makeText(this, "Đang gửi lại mã xác nhận qua email...", Toast.LENGTH_SHORT).show();

        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean sent = EmailSender.sendOtpEmail(targetEmail, currentOtp);
            AppExecutors.getInstance().mainThread().post(() -> {
                if (sent) {
                    Toast.makeText(VerifyOtpActivity.this, "Đã gửi mã mới vào hộp thư!", Toast.LENGTH_SHORT).show();
                    startResendTimer(60);
                } else {
                    Toast.makeText(VerifyOtpActivity.this, "Gửi mail thất bại, hãy thử lại!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // 4. Xác thực OTP và cập nhật mật khẩu mới vào SQLite Room
    private void handlePasswordReset() {
        // Thu thập 6 số OTP
        String enteredOtp = binding.otpBox1.getText().toString().trim()
                + binding.otpBox2.getText().toString().trim()
                + binding.otpBox3.getText().toString().trim()
                + binding.otpBox4.getText().toString().trim()
                + binding.otpBox5.getText().toString().trim()
                + binding.otpBox6.getText().toString().trim();

        String newPassword = binding.edtNewPassword.getText() != null ? binding.edtNewPassword.getText().toString().trim() : "";
        String confirmPassword = binding.edtConfirmPassword.getText() != null ? binding.edtConfirmPassword.getText().toString().trim() : "";

        // Kiểm tra OTP
        if (enteredOtp.length() < 6 || !enteredOtp.equals(currentOtp)) {
            Toast.makeText(this, "Mã xác thực không chính xác hoặc chưa đủ 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate mật khẩu mới
        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 8) {
            binding.tilNewPassword.setError("Mật khẩu phải từ 8 ký tự trở lên");
            binding.edtNewPassword.requestFocus();
            return;
        }
        binding.tilNewPassword.setError(null);

        // Validate xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Mật khẩu xác nhận không trùng khớp");
            binding.edtConfirmPassword.requestFocus();
            return;
        }
        binding.tilConfirmPassword.setError(null);

        // Khóa nút tránh double-click
        binding.btnSetNewPassword.setEnabled(false);

        // Lưu mật khẩu mới vào cơ sở dữ liệu Room trên Background Thread
        AppExecutors.getInstance().diskIO().execute(() -> {
            int rowsUpdated = database.userDao().updatePassword(targetEmail, newPassword);

            AppExecutors.getInstance().mainThread().post(() -> {
                binding.btnSetNewPassword.setEnabled(true);
                if (rowsUpdated > 0) {
                    Toast.makeText(VerifyOtpActivity.this, "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();

                    // Điều hướng về LoginActivity và xóa toàn bộ Activity trước đó
                    Intent intent = new Intent(VerifyOtpActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(VerifyOtpActivity.this, "Lỗi cập nhật mật khẩu, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}