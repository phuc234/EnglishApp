package com.example.englishapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.englishapp.TranslateActivity
import com.example.englishapp.VocabularyScreen.VocabularyQuestionActivity
import com.example.englishapp.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Kiểm tra xem người dùng đã đăng nhập chưa
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Nếu chưa đăng nhập, chuyển hướng về LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserProfile(currentUser.uid)

        // --- Listener cho nút Lưu thay đổi ---
        binding.saveProfileButton.setOnClickListener {
            saveProfileChanges()
        }

        // Xử lý sự kiện nút Đăng xuất
        binding.logoutButton.setOnClickListener {
            auth.signOut() // Đăng xuất khỏi Firebase Auth
            Toast.makeText(this, "Bạn đã đăng xuất.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java)) // Chuyển về màn hình đăng nhập
            finish() // Đóng ProfileActivity
        }

        // Cấu hình Bottom Navigation View
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_trainslate -> {
                    startActivity(Intent(this, TranslateActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_rank -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_profile -> {
                    // Đang ở màn hình Profile, không làm gì
                    true
                }
                else -> false
            }
        }
        // Đặt mục Profile làm mục được chọn khi ở màn hình Profile
        binding.bottomNavigationView.menu.findItem(R.id.navigation_profile)?.isChecked = true
    }

    private fun loadUserProfile(uid: String) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fullName = document.getString("fullName")
                    val username = document.getString("username")
                    val email = document.getString("email")
                    // totalScore cũng có thể lấy nếu bạn muốn hiển thị

                    // Cập nhật TextView và EditText
                    binding.userNameTextView.text = fullName ?: "Người dùng" // Hiển thị "Người dùng" nếu fullName null
                    binding.usernameEditText.setText(username ?: "")
                    binding.emailEditText.setText(email ?: "")
                    binding.passwordEditText.setText("********") // Giữ ở dạng ẩn


                } else {
                    Log.d("ProfileActivity", "Không tìm thấy thông tin người dùng trong Firestore.")
                    Toast.makeText(this, "Không tìm thấy thông tin hồ sơ.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileActivity", "Lỗi khi tải thông tin người dùng: ${exception.message}", exception)
                Toast.makeText(this, "Lỗi khi tải hồ sơ: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun saveProfileChanges() {
//        val fullName = binding.usernameEditText.text.toString().trim()
//        val location = binding.userLocationEditText.text.toString().trim()
//        val currentUser = auth.currentUser
//
//        if (currentUser == null) {
//            Toast.makeText(this, "Người dùng chưa đăng nhập.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val userDocRef = db.collection("users").document(currentUser.uid)
//
//
//            val updates = mutableMapOf<String, Any>(
//                "fullName" to fullName,
//                "location" to location
//            )
//            updateFirestoreUserProfile(userDocRef, updates)

    }
    private fun updateFirestoreUserProfile(userDocRef: com.google.firebase.firestore.DocumentReference, updates: Map<String, Any>) {
        userDocRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Hồ sơ đã được cập nhật thành công!", Toast.LENGTH_SHORT).show()
                Log.d("ProfileActivity", "Hồ sơ đã được cập nhật thành công.")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi khi cập nhật hồ sơ: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("ProfileActivity", "Lỗi khi cập nhật hồ sơ:", e)
            }
    }
}