package com.example.englishapp

import android.content.Intent // Dùng để chuyển đổi giữa các Activity
import android.os.Bundle // Dùng để lưu trữ trạng thái Activity
import android.util.Log // Dùng để ghi log để debug
import android.widget.Toast // Dùng để hiển thị thông báo ngắn cho người dùng
import androidx.appcompat.app.AppCompatActivity // Lớp cơ sở cho các Activity
import com.example.englishapp.databinding.ActivityTranslateBinding // Import View Binding cho layout màn hình dịch thuật

import kotlinx.coroutines.CoroutineScope // Dùng cho Coroutines để thực hiện tác vụ bất đồng bộ
import kotlinx.coroutines.Dispatchers // Quản lý luồng (Main, IO, Default)
import kotlinx.coroutines.launch // Khởi chạy một Coroutine
import kotlinx.coroutines.withContext // Chuyển đổi ngữ cảnh Coroutine
import org.json.JSONArray // Dùng để làm việc với mảng JSON
import org.json.JSONObject // Dùng để làm việc với đối tượng JSON
import java.io.BufferedReader // Dùng để đọc dữ liệu từ luồng input
import java.io.InputStreamReader // Dùng để đọc byte thành ký tự
import java.io.OutputStreamWriter // Dùng để ghi ký tự thành byte
import java.net.HttpURLConnection // Dùng để tạo kết nối HTTP
import java.net.URL // Dùng để tạo URL

// Activity này chịu trách nhiệm cho chức năng dịch thuật trong ứng dụng
class TranslateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTranslateBinding // Biến lateinit để giữ tham chiếu đến layout binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Khởi tạo binding cho layout activity_translate.xml
        binding = ActivityTranslateBinding.inflate(layoutInflater)
        // Đặt layout cho Activity
        setContentView(binding.root)

        // Thiết lập sự kiện click cho nút "Dịch Anh-Việt"
        binding.buttonEngViet.setOnClickListener {
            // Lấy văn bản từ editTextSource và loại bỏ khoảng trắng thừa
            val sourceText = binding.editTextSource.text.toString().trim()
            if (sourceText.isNotEmpty()) {
                // Gọi hàm dịch với ngôn ngữ nguồn là tiếng Anh (en) và đích là tiếng Việt (vi)
                translateText(sourceText, "en", "vi")
            } else {
                // Thông báo nếu người dùng chưa nhập văn bản
                Toast.makeText(this, "Vui lòng nhập văn bản để dịch.", Toast.LENGTH_SHORT).show()
            }
        }

        // Thiết lập sự kiện click cho nút "Dịch Việt-Anh"
        binding.buttonVietEng.setOnClickListener {
            val sourceText = binding.editTextSource.text.toString().trim()
            if (sourceText.isNotEmpty()) {
                // Gọi hàm dịch với ngôn ngữ nguồn là tiếng Việt (vi) và đích là tiếng Anh (en)
                translateText(sourceText, "vi", "en")
            } else {
                // Thông báo nếu người dùng chưa nhập văn bản
                Toast.makeText(this, "Vui lòng nhập văn bản để dịch.", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý sự kiện khi một mục trên Bottom Navigation được chọn
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Khi nhấn vào mục Home, chuyển về HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true // Trả về true để đánh dấu sự kiện đã được xử lý
                }
                R.id.navigation_trainslate -> {
                    // Đang ở TranslateActivity, không cần làm gì cả (hoặc có thể thêm logic refresh nếu cần)
                    Toast.makeText(this, "Bạn đang ở màn hình Dịch.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_rank -> {
                    // Chuyển sang RankActivity
                    val intent = Intent(this, RankingActivity::class.java)
                    // Thêm cờ để xóa các Activity phía trên và đảm bảo RankActivity là top single instance
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    Toast.makeText(this, "Chuyển sang màn hình Xếp hạng.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_profile -> {
                    // Chuyển sang ProfileActivity
                    val intent = Intent(this, ProfileActivity::class.java)
                    // Thêm cờ để xóa các Activity phía trên và đảm bảo ProfileActivity là top single instance
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    Toast.makeText(this, "Chuyển sang màn hình Hồ sơ.", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false // Trả về false nếu itemId không khớp với bất kỳ case nào
            }
        }

        // Điều này quan trọng để khi người dùng vào TranslateActivity, mục Translate sẽ được highlight
        binding.bottomNavigationView.selectedItemId = R.id.navigation_trainslate
    }

    // Hàm thực hiện dịch văn bản bằng API Gemini
    private fun translateText(text: String, sourceLang: String, targetLang: String) {
        // Hiển thị trạng thái "Đang dịch..." trên TextView đích
        binding.textViewTranslated.text = "Đang dịch..."

        // Khởi chạy một Coroutine trên luồng IO (input/output) để thực hiện tác vụ mạng
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Xây dựng prompt cho API Gemini
                val prompt = "Translate the following text from $sourceLang to $targetLang: \"$text\""
                // Tạo cấu trúc JSON cho lịch sử chat (chỉ có một tin nhắn user)
                val chatHistory = JSONArray().put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt))))

                // Tạo payload JSON cho yêu cầu API
                val payload = JSONObject().put("contents", chatHistory)
                // Lấy API Key từ BuildConfig (đã được cấu hình trong build.gradle)
                val apiKey = BuildConfig.GEMINI_API_KEY
                // Xây dựng URL của API Gemini
                val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

                // Tạo đối tượng URL
                val url = URL(apiUrl)
                // Mở kết nối HTTP
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST" // Đặt phương thức là POST
                connection.setRequestProperty("Content-Type", "application/json") // Đặt header Content-Type
                connection.doOutput = true // Cho phép gửi dữ liệu ra

                // Ghi payload JSON vào luồng output của kết nối
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                }

                // Lấy mã phản hồi HTTP
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) { // Nếu phản hồi là OK (200)
                    // Đọc phản hồi từ luồng input của kết nối
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        val response = reader.readText() // Đọc toàn bộ phản hồi thành chuỗi
                        val jsonResponse = JSONObject(response) // Chuyển chuỗi phản hồi thành đối tượng JSON
                        // Trích xuất văn bản dịch từ cấu trúc JSON của phản hồi Gemini
                        val translatedText = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        // Chuyển sang luồng Main để cập nhật UI
                        withContext(Dispatchers.Main) {
                            binding.textViewTranslated.text = translatedText // Hiển thị văn bản đã dịch
                        }
                    }
                } else {
                    // Nếu phản hồi không phải OK, đọc luồng lỗi
                    val errorStream = BufferedReader(InputStreamReader(connection.errorStream)).readText()
                    Log.e("TranslateActivity", "API Error: $responseCode - $errorStream") // Ghi log lỗi API
                    // Chuyển sang luồng Main để hiển thị lỗi cho người dùng
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TranslateActivity, "Lỗi dịch: $responseCode", Toast.LENGTH_SHORT).show()
                        binding.textViewTranslated.text = "Lỗi dịch." // Hiển thị thông báo lỗi trên UI
                    }
                }
            } catch (e: Exception) {
                // Bắt các ngoại lệ có thể xảy ra trong quá trình dịch (mạng, JSON parsing, v.v.)
                Log.e("TranslateActivity", "Translation failed: ", e) // Ghi log lỗi chi tiết
                // Chuyển sang luồng Main để hiển thị lỗi cho người dùng
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TranslateActivity, "Lỗi kết nối hoặc xử lý dịch.", Toast.LENGTH_SHORT).show()
                    binding.textViewTranslated.text = "Không thể dịch." // Hiển thị thông báo lỗi trên UI
                }
            }
        }
    }
}