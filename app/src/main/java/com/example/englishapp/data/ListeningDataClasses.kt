package com.example.englishapp.data

// Lớp dữ liệu biểu diễn một tùy chọn đáp án trong câu hỏi trắc nghiệm
data class OptionItem(
    val id: String, // ID duy nhất cho tùy chọn (ví dụ: "man")
    val prefix: String, // Tiền tố (A, B, C, D)
    val text: String, // Văn bản của tùy chọn (ví dụ: "Man")
    var isSelected: Boolean = false // Trạng thái chọn/bỏ chọn (không cần trong JSON, chỉ dùng trong code)
)

// Lớp dữ liệu biểu diễn một câu hỏi nghe và hoàn thành câu
data class ListeningQuestion(
    val id: String, // ID duy nhất cho câu hỏi
    val sentenceBeforeBlank: String, // Phần câu trước chỗ trống (ví dụ: "I am a")
    val sentenceAfterBlank: String, // Phần câu sau chỗ trống (ví dụ: ".")
    val audioFileName: String, // Tên file âm thanh (ví dụ: "i_am_a_man")
    val options: List<OptionItem>, // Danh sách các tùy chọn đáp án
    val correctAnswerId: String // ID của đáp án đúng (phải khớp với id trong OptionItem)
)
