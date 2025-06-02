package com.example.englishapp.data

import com.google.gson.annotations.SerializedName

// Lớp chính cho một bài đọc hiểu
data class ReadingQuestion(
    val id: String, // ID duy nhất cho mỗi bài đọc
    val passage: String, // Đoạn văn để đọc
    val questions: List<SubQuestion> // Danh sách các câu hỏi con
)

// Lớp cha cho các loại câu hỏi con (có thể là trắc nghiệm hoặc điền từ)
data class SubQuestion(
    @SerializedName("question_id")
    val questionId: String,
    @SerializedName("question_text")
    val questionText: String,
    val type: String, // "multiple_choice" hoặc "fill_in_the_blank"
    val options: List<OptionItem>?, // Chỉ có nếu type là "multiple_choice"
    @SerializedName("correct_answer_id")
    val correctAnswerId: String?, // ID của đáp án đúng cho trắc nghiệm
    @SerializedName("correct_answer")
    val correctAnswer: String? // Đáp án đúng cho điền từ (dạng text)
)
