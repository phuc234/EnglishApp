package com.example.englishapp.data

// Lớp dữ liệu biểu diễn một câu hỏi điền từ
data class Question(
    val id: String, // ID duy nhất cho câu hỏi
    val parts: List<String>, // Các phần của câu hỏi, bao gồm cả phần trước và sau chỗ trống
    val blankIndex: Int, // Chỉ số của phần tử trong 'parts' mà chỗ trống nằm sau nó
    val correctAnswer: String, // Đáp án đúng cho chỗ trống
    var userAnswer: String? = null, // Thêm trường này để lưu đáp án của người dùng
    var isCorrect: Boolean? = null // Thêm trường này để lưu trạng thái đúng/sai cho UI feedback
)
