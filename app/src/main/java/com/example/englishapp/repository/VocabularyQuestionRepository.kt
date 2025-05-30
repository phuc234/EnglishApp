package com.example.englishapp.repository // TODO: Thay thế bằng package thực tế của bạn

import android.content.Context
import android.util.Log
import com.example.englishapp.data.VocabularyQuestion // Import data class
import com.example.englishapp.utils.JsonAssetReader // Import JsonAssetReader
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException

// Repository để tải và xử lý dữ liệu câu hỏi từ vựng
class VocabularyQuestionRepository(private val context: Context) {

    // Tên file JSON chứa câu hỏi từ vựng trong thư mục assets
    private val JSON_FILE_NAME = "vocabulary_questions.json"

    // Hàm tải tất cả câu hỏi từ vựng từ file JSON
    fun getAllVocabularyQuestions(fileName: String): List<VocabularyQuestion> {
        val jsonString = JsonAssetReader.getJsonDataFromAsset(context, fileName)

        if (jsonString == null) {
            Log.e("VocabRepo", "Không thể đọc file JSON: $JSON_FILE_NAME")
            return emptyList()
        }

        val gson = Gson()
        val listType = object : TypeToken<List<VocabularyQuestion>>() {}.type

        return try {
            gson.fromJson(jsonString, listType)
        } catch (e: JsonSyntaxException) {
            Log.e("VocabRepo", "Lỗi cú pháp JSON khi parsing câu hỏi từ vựng: ", e)
            emptyList()
        } catch (e: IOException) {
            Log.e("VocabRepo", "Lỗi I/O khi parsing câu hỏi từ vựng: ", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("VocabRepo", "Lỗi không xác định khi parsing câu hỏi từ vựng: ", e)
            emptyList()
        }
    }
}
