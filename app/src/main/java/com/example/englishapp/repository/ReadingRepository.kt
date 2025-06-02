package com.example.englishapp.repository

import android.content.Context
import android.util.Log
import com.example.englishapp.data.ReadingQuestion
import com.example.englishapp.utils.JsonAssetReader
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException

class ReadingRepository(private val context: Context) {

    // Hàm để lấy tất cả các bài đọc từ một file JSON cụ thể
    fun getAllReadingQuestions(fileName: String): List<ReadingQuestion> {
        val jsonString = JsonAssetReader.getJsonDataFromAsset(context, fileName)

        if (jsonString == null) {
            Log.e("ReadingQuestionRepo", "Không thể đọc file JSON: $fileName")
            return emptyList()
        }

        val gson = Gson()
        val listType = object : TypeToken<List<ReadingQuestion>>() {}.type

        return try {
            val questions: List<ReadingQuestion> = gson.fromJson(jsonString, listType)
            Log.d("ReadingQuestionRepo", "Đã tải ${questions.size} bài đọc từ file: $fileName")
            questions
        } catch (e: JsonSyntaxException) {
            Log.e("ReadingQuestionRepo", "Lỗi cú pháp JSON khi parsing dữ liệu đọc hiểu từ $fileName: ", e)
            emptyList()
        } catch (e: IOException) {
            Log.e("ReadingQuestionRepo", "Lỗi I/O khi parsing dữ liệu đọc hiểu từ $fileName: ", e)
            emptyList()
        } catch (e: Exception) {
            Log.e("ReadingQuestionRepo", "Lỗi không xác định khi parsing dữ liệu đọc hiểu từ $fileName: ", e)
            emptyList()
        }
    }
}