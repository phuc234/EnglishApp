package com.example.englishapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.databinding.ActivityRegisterBinding // Import binding cho layout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log // Import Log

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Xử lý sự kiện nút Đăng ký
        binding.buttonRegister.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val username = binding.usernameEditText.text.toString().trim()
            val fullName = binding.fullNameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()
            val rePassword = binding.rePasswordEditText.text.toString().trim()

            // Kiểm tra các trường nhập liệu
            if (email.isEmpty() || username.isEmpty() || fullName.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != rePassword) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Tạo tài khoản Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        user?.let { firebaseUser ->
                            // Lưu thông tin người dùng vào Firestore
                            val userMap = hashMapOf(
                                "email" to email,
                                "username" to username,
                                "fullName" to fullName,
                                "totalScore" to 0L ,// Khởi tạo điểm ban đầu là 0 (sử dụng Long)
                                "currentLevelId" to "level_1", // Khởi tạo cấp độ là level 1
                            )

                            db.collection("users").document(firebaseUser.uid).set(userMap)
                                .addOnSuccessListener {
                                    Log.d("RegisterActivity", "Đăng ký thành công và lưu Firestore.")
                                    Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish() // Đóng RegisterActivity
                                }
                                .addOnFailureListener { e ->
                                    Log.e("RegisterActivity", "Lỗi lưu user data vào Firestore: ${e.message}", e)
                                    Toast.makeText(this, "Đăng ký thành công nhưng lỗi lưu thông tin người dùng: ${e.message}", Toast.LENGTH_LONG).show()
                                    // Tùy chọn: Xóa tài khoản Auth nếu lưu Firestore thất bại
                                    firebaseUser.delete()
                                }
                        }
                    } else {
                        Log.e("RegisterActivity", "Đăng ký Firebase Auth thất bại: ${task.exception?.message}", task.exception)
                        Toast.makeText(this, "Đăng ký thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
