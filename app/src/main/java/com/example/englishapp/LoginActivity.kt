package com.example.englishapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.databinding.ActivityLoginBinding // Import binding cho layout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log // Import Log

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

//         Kiểm tra nếu người dùng đã đăng nhập, chuyển hướng ngay lập tức
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish() // Đóng LoginActivity để không quay lại
        }

        // Xử lý sự kiện nút Đăng nhập
        binding.buttonLogin.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("LoginActivity", "Đăng nhập thành công: ${auth.currentUser?.email}")
                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish() // Đóng LoginActivity
                    } else {
                        Log.e("LoginActivity", "Đăng nhập thất bại: ${task.exception?.message}")
                        Toast.makeText(this, "Đăng nhập thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Xử lý sự kiện TextView "Đăng ký ngay"
        binding.textViewSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java) // Chuyển sang màn hình Đăng ký
            startActivity(intent)
        }

        binding.textViewForgotPassword.setOnClickListener {
            Toast.makeText(this, "Chức năng quên mật khẩu đang được phát triển.", Toast.LENGTH_SHORT).show()
        }
    }
}
