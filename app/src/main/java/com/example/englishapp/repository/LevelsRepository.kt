package com.example.englishapp.repository

import android.content.Context
import android.util.Log
import com.example.englishapp.data.LearningLevel
import com.example.englishapp.utils.JsonAssetReader
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException

class LevelsRepository(private val context: Context) {

    private val LEVELS_DATA_FILE_NAME = "levels_data.json"

    fun getAllLevels(): List<LearningLevel>? {
        val jsonString = JsonAssetReader.getJsonDataFromAsset(context, LEVELS_DATA_FILE_NAME)

        if (jsonString == null) {
            Log.e("LevelsRepository", "Không thể đọc file JSON: $LEVELS_DATA_FILE_NAME")
            return null
        }

        val gson = Gson()
        val listType = object : TypeToken<List<LearningLevel>>() {}.type

        return try {
            gson.fromJson(jsonString, listType)
        } catch (e: JsonSyntaxException) {
            Log.e("LevelsRepository", "Lỗi cú pháp JSON khi parsing dữ liệu cấp độ: ", e)
            null
        } catch (e: IOException) {
            Log.e("LevelsRepository", "Lỗi I/O khi parsing dữ liệu cấp độ: ", e)
            null
        } catch (e: Exception) {
            Log.e("LevelsRepository", "Lỗi không xác định khi parsing dữ liệu cấp độ: ", e)
            null
        }
    }

    // Hàm mới: Lấy cấp độ tiếp theo dựa trên ID cấp độ hiện tại
    fun getNextLevel(currentLevelId: String, allLevels: List<LearningLevel>?): LearningLevel? {
        if (allLevels == null) return null
        val currentIndex = allLevels.indexOfFirst { it.id == currentLevelId }
        return if (currentIndex != -1 && currentIndex < allLevels.size - 1) {
            allLevels[currentIndex + 1]
        } else {
            null // Không có cấp độ tiếp theo
        }
    }

    // Hàm mới: Lấy cấp độ theo ID
    fun getLevelById(levelId: String, allLevels: List<LearningLevel>?): LearningLevel? {
        return allLevels?.find { it.id == levelId }
    }
}