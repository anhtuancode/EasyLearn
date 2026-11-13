package com.studying.easylearn.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.studying.easylearn.data.db.AppDatabase;
import com.studying.easylearn.data.entities.User;
import com.studying.easylearn.databinding.ActivitySignUpBinding;
import com.studying.easylearn.utils.AppExecutors;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private AppDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng ViewBinding theo đúng yêu cầu dự án
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo Room Database Singleton
        database = AppDatabase.getInstance(this);

        initListeners();
    }

    private void initListeners() {
        // Nút Back mũi tên bo tròn: Đóng Activity quay về màn hình trước
        binding.btnBack.setOnClickListener(v -> finish());

        // Dòng chữ Log In ở dưới cùng: Đóng Activity quay về LoginActivity
        binding.tvLogin.setOnClickListener(v -> finish());

        // Nút Get Started Free
        binding.btnSignUp.setOnClickListener(v -> handleRegistration());

        // Nút Google & Apple (Demo giao diện)
        binding.btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập Google đang được tích hợp", Toast.LENGTH_SHORT).show()
        );
        binding.btnApple.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập Apple đang được tích hợp", Toast.LENGTH_SHORT).show()
        );
    }

    private void handleRegistration() {
        // 1. Trích xuất dữ liệu từ các ô Input và làm sạch khoảng trắng thừa
        String fullName = binding.edtFullName.getText() != null ? binding.edtFullName.getText().toString().trim() : "";
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString().trim() : "";
        String password = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString().trim() : "";

        // 2. Validate Họ và tên
        if (TextUtils.isEmpty(fullName)) {
            binding.tilFullName.setError("Vui lòng nhập họ và tên của bạn");
            binding.edtFullName.requestFocus();
            return;
        }
        binding.tilFullName.setError(null);

        // 3. Validate Email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email không được để trống");
            binding.edtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Định dạng email không đúng (ví dụ: name@example.com)");
            binding.edtEmail.requestFocus();
            return;
        }
        binding.tilEmail.setError(null);

        // 4. Validate Mật khẩu (theo tiêu chí hiển thị trên UI: ít nhất 8 ký tự)
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Vui lòng nhập mật khẩu");
            binding.edtPassword.requestFocus();
            return;
        }
        if (password.length() < 8) {
            binding.tilPassword.setError("Mật khẩu phải chứa ít nhất 8 ký tự");
            binding.edtPassword.requestFocus();
            return;
        }
        binding.tilPassword.setError(null);

        // 5. Kiểm tra chấp thuận điều khoản
        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(this, "Bạn cần đồng ý với Điều khoản dịch vụ và Chính sách bảo mật", Toast.LENGTH_LONG).show();
            return;
        }

        // Tạm thời vô hiệu hóa nút bấm để tránh người dùng nhấn nhiều lần (chống duplicate click)
        binding.btnSignUp.setEnabled(false);

        // 6. Thực thi truy vấn và lưu vào Room Database trên Background Thread
        AppExecutors.getInstance().diskIO().execute(() -> {
            // Kiểm tra xem email này đã tồn tại trong SQLite hay chưa
            User existingUser = database.userDao().getUserByEmail(email);

            if (existingUser != null) {
                // Email đã có người sử dụng -> Báo lỗi trên Main UI Thread
                AppExecutors.getInstance().mainThread().post(() -> {
                    binding.btnSignUp.setEnabled(true);
                    binding.tilEmail.setError("Email này đã được sử dụng. Vui lòng đăng nhập hoặc dùng email khác.");
                    binding.edtEmail.requestFocus();
                });
                return;
            }

            // Tạo đối tượng User mới
            User newUser = new User(
                    email,
                    password,
                    fullName,
                    "LOCAL",
                    System.currentTimeMillis()
            );

            // Ghi vào bảng users trong SQLite
            long insertedId = database.userDao().insertUser(newUser);

            // Đưa kết quả phản hồi về Main UI Thread
            AppExecutors.getInstance().mainThread().post(() -> {
                binding.btnSignUp.setEnabled(true);

                if (insertedId > 0) {
                    Toast.makeText(SignUpActivity.this, "Đăng ký thành công! Hãy đăng nhập để bắt đầu.", Toast.LENGTH_SHORT).show();
                    finish(); // Kết thúc màn hình đăng ký, tự động trả người dùng về LoginActivity
                } else {
                    Toast.makeText(SignUpActivity.this, "Đã xảy ra lỗi trong quá trình lưu tài khoản. Vui lòng thử lại!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}