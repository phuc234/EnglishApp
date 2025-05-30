package com.example.englishapp.utils // TODO: Thay thế bằng package thực tế của bạn

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.InputStream

// Đối tượng tiện ích để đọc file JSON từ thư mục assets
object JsonAssetReader {

    // Hàm đọc nội dung file JSON từ thư mục assets
    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            // Mở file từ thư mục assets
            val inputStream: InputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charsets.UTF_8)
            Log.d("JsonAssetReader", "Đọc file JSON thành công: $fileName")
            return jsonString
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            Log.e("JsonAssetReader", "Lỗi đọc file JSON từ assets: $fileName", ioException)
            // Hiển thị Toast trên Main Thread nếu được gọi từ Coroutine/Background Thread
            // Hoặc xử lý lỗi ở lớp gọi nếu không muốn Toast ở đây
            Toast.makeText(context, "Lỗi: Không tìm thấy file JSON '$fileName'.", Toast.LENGTH_LONG).show()
            return null
        }
    }
}
