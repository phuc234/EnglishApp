package com.example.englishapp.utils // Khai báo tên gói (package) của tệp này. TODO: Bạn nên thay thế bằng tên gói thực tế của dự án.

import android.content.Context // Import lớp Context, cung cấp quyền truy cập vào các tài nguyên và dịch vụ của ứng dụng
import android.util.Log // Import lớp Log để ghi nhật ký (logging) các thông báo debug hoặc lỗi
import android.widget.Toast // Import lớp Toast để hiển thị các thông báo ngắn gọn trên màn hình
import java.io.IOException // Import lớp IOException, một ngoại lệ (exception) cho các lỗi nhập/xuất (I/O)
import java.io.InputStream // Import lớp InputStream để đọc dữ liệu dạng byte từ một nguồn

object JsonAssetReader { // Khai báo một 'object' (singleton) tên là JsonAssetReader. Điều này có nghĩa là chỉ có một thể hiện duy nhất của lớp này tồn tại trong ứng dụng.

    // Hàm đọc nội dung file JSON từ thư mục assets
    // context: Ngữ cảnh của ứng dụng, cần thiết để truy cập thư mục assets
    // fileName: Tên của tệp JSON cần đọc (ví dụ: "questions.json")
    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            val inputStream: InputStream = context.assets.open(fileName) // Sử dụng context để mở InputStream đến tệp có tên 'fileName' trong thư mục assets
            val size = inputStream.available() // Lấy số byte có sẵn trong luồng (kích thước của tệp)
            val buffer = ByteArray(size) // Tạo một mảng byte có kích thước bằng kích thước tệp
            inputStream.read(buffer) // Đọc toàn bộ nội dung từ InputStream vào mảng byte
            inputStream.close() // Đóng InputStream để giải phóng tài nguyên
            jsonString = String(buffer, Charsets.UTF_8) // Chuyển đổi mảng byte thành chuỗi bằng cách sử dụng mã hóa UTF-8
            Log.d("JsonAssetReader", "Đọc file JSON thành công: $fileName")
            return jsonString
        } catch (ioException: IOException) {
            ioException.printStackTrace() // In dấu vết ngăn xếp (stack trace) của ngoại lệ ra Logcat, hữu ích cho việc debug
            Log.e("JsonAssetReader", "Lỗi đọc file JSON từ assets: $fileName", ioException) // Ghi nhật ký lỗi với thông báo và ngoại lệ
            Toast.makeText(context, "Lỗi: Không tìm thấy file JSON '$fileName'.", Toast.LENGTH_LONG).show() // Hiển thị một thông báo Toast cho người dùng về lỗi
            return null // Trả về null để chỉ ra rằng không đọc được dữ liệu
        }
    }
}