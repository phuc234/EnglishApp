package com.example.englishapp // TODO: Thay thế bằng package thực tế của bạn

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log // Import Log để ghi log lỗi
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.englishapp.adapter.OptionAdapter
import com.example.englishapp.data.ListeningQuestion
import com.example.englishapp.data.OptionItem
import com.example.englishapp.databinding.ActivityListenBinding // TODO: Import lớp binding cho layout Activity
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException // Import để bắt lỗi cú pháp JSON
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.io.InputStream

class ListeningQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListenBinding
    private lateinit var optionAdapter: OptionAdapter
    private var mediaPlayer: MediaPlayer? = null

    private var allQuestions: List<ListeningQuestion> = emptyList()
    private lateinit var currentQuestion: ListeningQuestion
    private var currentQuestionIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Cấu hình Toolbar ---
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // --- Đọc và Parsing dữ liệu từ JSON ---
        loadQuestionsFromJson()

        // --- Hiển thị câu hỏi đầu tiên (nếu có) ---
        if (allQuestions.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)
        } else {
            Toast.makeText(this, "Lỗi: Không tải được dữ liệu câu hỏi. Kiểm tra Logcat.", Toast.LENGTH_LONG).show()
            Log.e("ListeningActivity", "Không tải được dữ liệu câu hỏi. Danh sách rỗng.")
            // Tùy chọn: Đóng Activity nếu không có dữ liệu để tránh crash tiếp
            // finish()
        }

        // --- Cấu hình Listener cho Icon Loa ---
        binding.speakerIcon.setOnClickListener {
            playAudio()
        }

        // TODO: Cấu hình Listener cho Icon Rùa nếu cần
        // binding.turtleIcon.setOnClickListener { ... }

        // --- Cấu hình Listener cho nút KIỂM TRA ---
        binding.checkButton.setOnClickListener {
            handleCheckButtonClick()
        }
    }

    // Hàm đọc nội dung file JSON từ thư mục assets
    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            // Mở file từ thư mục assets
            val inputStream: InputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charsets.UTF_8)
            Log.d("ListeningActivity", "Đọc file JSON thành công: $fileName")
            return jsonString
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            Log.e("ListeningActivity", "Lỗi đọc file JSON từ assets: $fileName", ioException)
            Toast.makeText(context, "Lỗi: Không tìm thấy file JSON '$fileName'.", Toast.LENGTH_LONG).show()
            return null
        }
    }

    // Hàm đọc và Parsing dữ liệu câu hỏi từ JSON
    private fun loadQuestionsFromJson() {
        val jsonFileString = getJsonDataFromAsset(applicationContext, "listening_questions.json")

        if (jsonFileString != null) {
            val gson = Gson()
            val listQuestionType = object : TypeToken<List<ListeningQuestion>>() {}.type
            try {
                allQuestions = gson.fromJson(jsonFileString, listQuestionType)
                Log.d("ListeningActivity", "Parsing JSON thành công. Số câu hỏi: ${allQuestions.size}")
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                Log.e("ListeningActivity", "Lỗi cú pháp JSON: ", e)
                Toast.makeText(this, "Lỗi: Cú pháp JSON không hợp lệ. Kiểm tra file.", Toast.LENGTH_LONG).show()
                allQuestions = emptyList()
            } catch (e: Exception) { // Bắt các lỗi khác có thể xảy ra trong quá trình parsing
                e.printStackTrace()
                Log.e("ListeningActivity", "Lỗi parsing JSON không xác định: ", e)
                Toast.makeText(this, "Lỗi: Không thể phân tích dữ liệu JSON.", Toast.LENGTH_LONG).show()
                allQuestions = emptyList()
            }
        } else {
            allQuestions = emptyList()
        }
    }

    // Hàm hiển thị câu hỏi tại một chỉ số cụ thể
    private fun displayQuestion(index: Int) {
        if (index >= 0 && index < allQuestions.size) {
            currentQuestion = allQuestions[index]

            binding.instructionText.text = "Nghe và hoàn thành câu sau :" // TODO: Sử dụng String Resource
            binding.sentenceText.text = "${currentQuestion.sentenceBeforeBlank} ___${currentQuestion.sentenceAfterBlank}"

            setupOptionsRecyclerView(currentQuestion.options.toMutableList())

            binding.checkButton.backgroundTintList = getColorStateList(R.color.button_gray_background) // TODO: Sử dụng màu từ colors.xml
            binding.checkButton.setTextColor(getColorStateList(android.R.color.black))
            binding.checkButton.isEnabled = true
        } else {
            Toast.makeText(this, "Đã hoàn thành tất cả câu hỏi!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Hàm cấu hình RecyclerView cho tùy chọn (nhận danh sách tùy chọn)
    private fun setupOptionsRecyclerView(options: MutableList<OptionItem>) {
        optionAdapter = OptionAdapter(options) { option, position ->
            handleOptionClick(option, position)
        }

        val layoutManager = GridLayoutManager(this, 2)
        binding.optionsRecyclerView.layoutManager = layoutManager
        binding.optionsRecyclerView.adapter = optionAdapter
    }

    // Hàm xử lý khi một tùy chọn đáp án được click
    private fun handleOptionClick(option: OptionItem, position: Int) {
        optionAdapter.selectItem(position)
        binding.checkButton.isEnabled = true
    }

    // Hàm phát âm thanh
    private fun playAudio() {
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            val audioResId = resources.getIdentifier(currentQuestion.audioFileName, "raw", packageName)

            if (audioResId != 0) {
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
            } else {
                Toast.makeText(this, "Không tìm thấy file âm thanh: ${currentQuestion.audioFileName}", Toast.LENGTH_SHORT).show()
                Log.e("ListeningActivity", "Không tìm thấy resource âm thanh cho: ${currentQuestion.audioFileName}")
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi khi phát âm thanh.", Toast.LENGTH_SHORT).show()
            Log.e("ListeningActivity", "Lỗi phát âm thanh: ", e)
            e.printStackTrace()
        }
    }

    // Hàm xử lý khi click vào nút KIỂM TRA
    private fun handleCheckButtonClick() {
        val selectedOption = optionAdapter.getSelectedItem()

        if (selectedOption == null) {
            Toast.makeText(this, "Vui lòng chọn một đáp án.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedOption.id == currentQuestion.correctAnswerId) {
            Toast.makeText(this, "Chính xác! Đáp án đúng là ${selectedOption.text}", Toast.LENGTH_SHORT).show()
            currentQuestionIndex++
            displayQuestion(currentQuestionIndex)
        } else {
            val correctOption = currentQuestion.options.find { it.id == currentQuestion.correctAnswerId }
            val correctOptionText = correctOption?.text ?: "Đáp án đúng"
            Toast.makeText(this, "Sai rồi. Đáp án đúng là: ${correctOptionText}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}
