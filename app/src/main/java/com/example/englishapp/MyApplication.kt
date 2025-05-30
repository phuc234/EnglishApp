package com.example.englishapp // Đảm bảo đây là package gốc của ứng dụng của bạn

import android.app.Application
import com.google.firebase.FirebaseApp
import android.util.Log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo FirebaseApp khi ứng dụng bắt đầu
        try {
            FirebaseApp.initializeApp(this)
            Log.d("MyApplication", "FirebaseApp initialized successfully!")
        } catch (e: IllegalStateException) {
            Log.e("MyApplication", "FirebaseApp is already initialized or failed to initialize: ${e.message}")
            // Xử lý trường hợp đã khởi tạo hoặc lỗi khác
        } catch (e: Exception) {
            Log.e("MyApplication", "Unknown error during FirebaseApp initialization: ${e.message}", e)
        }
    }
}
