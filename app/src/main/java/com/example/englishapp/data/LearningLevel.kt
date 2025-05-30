package com.example.englishapp.data

import com.google.gson.annotations.SerializedName

data class LearningLevel(
    val id: String,
    val name: String,
    @SerializedName("required_score_to_unlock_next")
    val requiredScoreToUnlockNext: Long,
    val modules: List<ExerciseModule>
)