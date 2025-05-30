package com.example.englishapp.repository

import android.content.Context
import android.util.Log
import com.example.englishapp.data.FillBlankDataClasses // Import data class cho câu hỏi điền từ
import com.example.englishapp.utils.JsonAssetReader // Import JsonAssetReader
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException

// Repository để tải và xử lý dữ liệu câu hỏi điền từ
class QuestionRepository(private val context: Context) {

    // Tên file JSON chứa câu hỏi điền từ trong thư mục assets
    private val JSON_FILE_NAME = "question.json"

    // Hàm tải tất cả câu hỏi điền từ từ file JSON
    fun getAllFillBlankQuestions(): List<FillBlankDataClasses> {
        val jsonString = JsonAssetReader.getJsonDataFromAsset(context, JSON_FILE_NAME)

        if (jsonString == null) {
            Log.e("FillBlankRepo", "Không thể đọc file JSON: $JSON_FILE_NAME")
            return emptyList()
        }

        val gson = Gson()
        val listType = object : TypeToken<List<FillBlankDataClasses>>() {}.type

        return try {
            gson.fromJson(jsonString, listType)
        } catch (e: JsonSyntaxException) {
            Log.e("FillBlankRepo", "Lỗi cú pháp JSON khi parsing câu hỏi điền từ: ", e)
            emptyList()
        } catch (e: IOException) {
            Log.e("FillBlankRepo", "Lỗi I/O khi parsing câu hỏi điền từ: ", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("FillBlankRepo", "Lỗi không xác định khi parsing câu hỏi điền từ: ", e)
            emptyList()
        }
    }
}
