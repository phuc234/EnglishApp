package com.example.englishapp // TODO: Thay thế bằng package thực tế của bạn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.LoginActivity // TODO: Import lớp LoginActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Không cần setContentView() cho MainActivity này vì nó không hiển thị giao diện

        // Tạo Intent để chuyển sang LoginActivity
        val intent = Intent(this, LoginActivity::class.java)

        // TODO: Tại đây, bạn có thể thêm logic kiểm tra xem người dùng đã đăng nhập chưa
        // Nếu đã đăng nhập, chuyển hướng sang TopicSelectionActivity thay vì LoginActivity
        // Ví dụ:
        // val isLoggedIn = checkUserLoginStatus() // Hàm kiểm tra trạng thái đăng nhập
        // if (isLoggedIn) {
        //     val mainIntent = Intent(this, TopicSelectionActivity::class.java) // Thay TopicSelectionActivity bằng Activity chính sau đăng nhập
        //     startActivity(mainIntent)
        // } else {
        //     val loginIntent = Intent(this, LoginActivity::class.java)
        //     startActivity(loginIntent)
        // }


        // Với yêu cầu hiện tại là luôn chạy LoginActivity đầu tiên:
        startActivity(intent)

        // Kết thúc MainActivity để nó không còn tồn tại trên Back Stack
        finish()
    }

    // TODO: Có thể thêm hàm checkUserLoginStatus() nếu cần kiểm tra trạng thái đăng nhập
    // private fun checkUserLoginStatus(): Boolean {
    //     // Logic kiểm tra SharedPreferences, Room Database, Session, v.v.
    //     return false // Trả về true nếu đã đăng nhập
    // }
}