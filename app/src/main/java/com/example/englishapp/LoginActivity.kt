
package com.example.englishapp // TODO: Thay thế bằng package thực tế
import android.content.Intent
import android.os.Bundle
import android.widget.Toast // Import Toast nếu cần
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.databinding.ActivityLoginBinding // TODO: Import binding cho layout đăng nhập

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: Triển khai logic lấy thông tin từ EditText và kiểm tra đăng nhập
        // Ví dụ: val username = binding.editTextUsername.text.toString()
        // val password = binding.editTextPassword.text.toString()
        // ... kiểm tra username/password ...

        // Đặt Listener cho nút Đăng nhập
        binding.buttonLogin.setOnClickListener {
            // TODO: Thay thế bằng kết quả kiểm tra đăng nhập thực tế
            val isLoginSuccessful = true // Giả định đăng nhập thành công

            if (isLoginSuccessful) {
                // Tạo Intent để chuyển sang TopicSelectionActivity
                val intent = Intent(this, TopicSelectionActivity::class.java) // TODO: Thay TopicSelectionActivity bằng tên Activity bạn tạo
                startActivity(intent)

                // Tùy chọn: Kết thúc LoginActivity để người dùng không quay lại màn hình đăng nhập bằng nút Back
                finish()
            } else {
                // TODO: Hiển thị thông báo lỗi đăng nhập (ví dụ: Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show())
            }
        }

        // TODO: Thêm Listener cho text Quên mật khẩu và Đăng ký ngay nếu cần điều hướng
        // binding.textViewForgotPassword.setOnClickListener { ... }
        // binding.textViewSignUp.setOnClickListener { ... }
    }
}