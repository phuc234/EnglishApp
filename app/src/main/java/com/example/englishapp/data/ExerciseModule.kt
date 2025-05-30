package com.example.englishapp.data

import com.google.gson.annotations.SerializedName

data class ExerciseModule(
    val id: String,
    val name: String,
    @SerializedName("image_name")
    val imageName: String, // Tên file icon
    val type: String,      // Loại bài tập: "grammar", "vocabulary", "reading_comprehension", "writing"
    val description: String,
    @SerializedName("exercises_file")
    val exercisesFile: String // Tên file JSON chứa bài tập cụ thể
)