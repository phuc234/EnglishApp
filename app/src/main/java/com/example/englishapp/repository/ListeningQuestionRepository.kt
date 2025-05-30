package com.example.englishapp.repository // TODO: Thay thế bằng package thực tế của bạn

import android.content.Context
import android.util.Log
import com.example.englishapp.data.ListeningQuestion
import com.example.englishapp.utils.JsonAssetReader // Import JsonAssetReader
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException

// Repository để tải và xử lý dữ liệu câu hỏi nghe
class ListeningQuestionRepository(private val context: Context) {

    // Tên file JSON chứa câu hỏi nghe trong thư mục assets
    private val JSON_FILE_NAME = "listening_questions.json"

    // Hàm tải tất cả câu hỏi nghe từ file JSON
    fun getAllListeningQuestions(fileName: String): List<ListeningQuestion> {
        val jsonString = JsonAssetReader.getJsonDataFromAsset(context, fileName)

        if (jsonString == null) {
            Log.e("ListenRepo", "Không thể đọc file JSON: $JSON_FILE_NAME")
            return emptyList()
        }

        val gson = Gson()
        val listType = object : TypeToken<List<ListeningQuestion>>() {}.type

        return try {
            gson.fromJson(jsonString, listType)
        } catch (e: JsonSyntaxException) {
            Log.e("ListenRepo", "Lỗi cú pháp JSON khi parsing câu hỏi nghe: ", e)
            emptyList()
        } catch (e: IOException) {
            Log.e("ListenRepo", "Lỗi I/O khi parsing câu hỏi nghe: ", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("ListenRepo", "Lỗi không xác định khi parsing câu hỏi nghe: ", e)
            emptyList()
        }
    }
}
