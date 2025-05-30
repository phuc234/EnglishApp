package com.example.englishapp.data // TODO: Thay thế bằng package thực tế của bạn

// Lớp dữ liệu biểu diễn một câu hỏi Từ vựng
data class VocabularyQuestion(
    val id: String, // ID duy nhất cho câu hỏi
    val questionText: String, // Văn bản câu hỏi (ví dụ: "Đâu là 'quả dâu'?")
    val options: List<VocabularyOption>, // Danh sách các tùy chọn đáp án
    val correctAnswerId: String // ID của đáp án đúng (phải khớp với id trong VocabularyOption)
)

// Lớp dữ liệu cho một tùy chọn Từ vựng (có ảnh và text)
data class VocabularyOption(
    val id: String, // ID duy nhất cho tùy chọn (ví dụ: "strawberry")
    val imageFileName: String, // Tên file ảnh (ví dụ: "strawberry")
    val text: String // Văn bản hiển thị (ví dụ: "Strawberry")
)
