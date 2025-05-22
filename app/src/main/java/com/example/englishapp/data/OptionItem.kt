package com.example.englishapp.data // TODO: Thay thế bằng package thực tế của bạn

import com.google.gson.annotations.SerializedName // Import nếu cần tùy chỉnh tên trường

// Lớp dữ liệu biểu diễn một tùy chọn đáp án
data class OptionItem(
    val id: String,
    val prefix: String,
    val text: String,
    // Không cần SerializedName nếu tên trường trong JSON khớp với tên biến Kotlin
    var isSelected: Boolean = false // Trạng thái chọn/bỏ chọn (không cần trong JSON, chỉ dùng trong code)
)


