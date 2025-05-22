package com.example.englishapp.data // TODO: Thay thế bằng package thực tế của bạn

// Lớp dữ liệu biểu diễn một câu hỏi nghe và hoàn thành câu
data class ListeningQuestion(
    val id: String,
    val sentenceBeforeBlank: String,
    val sentenceAfterBlank: String,
    // Tên trường trong JSON là "audioFileName", trong Kotlin là "audioFileName"
    val audioFileName: String, // Lưu tên file âm thanh (ví dụ: "i_am_a_man")

    val options: List<OptionItem>, // Danh sách các tùy chọn

    val correctAnswerId: String // ID của đáp án đúng
    // Không cần SerializedName nếu tên trường trong JSON khớp với tên biến Kotlin
)
