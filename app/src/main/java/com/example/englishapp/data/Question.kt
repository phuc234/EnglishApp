package com.example.englishapp.data

data class Question(
    val id: String, // ID duy nhất cho câu hỏi
    val parts: List<String>, // Các phần của câu hỏi, bao gồm cả phần trước và sau chỗ trống
    val blankIndex: Int // Chỉ số của phần tử trong 'parts' mà chỗ trống nằm sau nó

)