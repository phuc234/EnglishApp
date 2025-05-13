package com.example.englishapp.VocabularyScreen

data class VocabularyOption(
    val id: String, // ID duy nhất cho tùy chọn (ví dụ: "strawberry") - hữu ích cho việc kiểm tra đáp án
    val imageResId: Int, // ID resource của ảnh (drawable)
    val text: String, // Văn bản hiển thị dưới ảnh (ví dụ: "Strawberry")
    var isSelected: Boolean = false // Thêm trạng thái chọn/bỏ chọn
)