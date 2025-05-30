package com.example.englishapp // TODO: Thay thế bằng package thực tế

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.databinding.ActivityTranslateBinding // TODO: Import binding cho layout màn hình dịch thuật
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

// TODO: Import các Activity khác nếu có (RankActivity, ProfileActivity)

class TranslateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTranslateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTranslateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonEngViet.setOnClickListener {
            val sourceText = binding.editTextSource.text.toString().trim()
            if (sourceText.isNotEmpty()) {
                // Gọi hàm dịch Anh-Việt
                translateText(sourceText, "en", "vi")
            } else {
                Toast.makeText(this, "Vui lòng nhập văn bản để dịch.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonVietEng.setOnClickListener {
            val sourceText = binding.editTextSource.text.toString().trim()
            if (sourceText.isNotEmpty()) {
                // Gọi hàm dịch Việt-Anh
                translateText(sourceText, "vi", "en")
            } else {
                Toast.makeText(this, "Vui lòng nhập văn bản để dịch.", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý sự kiện cho Bottom Navigation trong TranslateActivity
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Khi nhấn vào mục Home, chuyển về LevelModuleActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_trainslate -> {
                    // Đang ở TranslateActivity, không cần làm gì cả hoặc có thể refresh
                    Toast.makeText(this, "Bạn đang ở màn hình Dịch.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_rank -> {
                    // Chuyển sang RankActivity
                     val intent = Intent(this, RankingActivity::class.java)
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                     startActivity(intent)
                    Toast.makeText(this, "Chuyển sang màn hình Xếp hạng.", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_profile -> {
                    // Chuyển sang ProfileActivity
                     val intent = Intent(this, ProfileActivity::class.java)
                     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                     startActivity(intent)
                    Toast.makeText(this, "Chuyển sang màn hình Hồ sơ.", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Điều này quan trọng để khi người dùng vào TranslateActivity, mục Translate sẽ được highlight
        binding.bottomNavigationView.selectedItemId = R.id.navigation_trainslate
    }

    private fun translateText(text: String, sourceLang: String, targetLang: String) {
        binding.textViewTranslated.text = "Đang dịch..." // Hiển thị trạng thái đang dịch

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prompt = "Translate the following text from $sourceLang to $targetLang: \"$text\""
                val chatHistory = JSONArray().put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt))))

                val payload = JSONObject().put("contents", chatHistory)
                val apiKey = BuildConfig.GEMINI_API_KEY
                val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                        val response = reader.readText()
                        val jsonResponse = JSONObject(response)
                        val translatedText = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        withContext(Dispatchers.Main) {
                            binding.textViewTranslated.text = translatedText
                        }
                    }
                } else {
                    val errorStream = BufferedReader(InputStreamReader(connection.errorStream)).readText()
                    Log.e("TranslateActivity", "API Error: $responseCode - $errorStream")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TranslateActivity, "Lỗi dịch: $responseCode", Toast.LENGTH_SHORT).show()
                        binding.textViewTranslated.text = "Lỗi dịch."
                    }
                }
            } catch (e: Exception) {
                Log.e("TranslateActivity", "Translation failed: ", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TranslateActivity, "Lỗi kết nối hoặc xử lý dịch.", Toast.LENGTH_SHORT).show()
                    binding.textViewTranslated.text = "Không thể dịch."
                }
            }
        }
    }
}
