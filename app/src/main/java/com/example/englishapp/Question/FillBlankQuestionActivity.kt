package com.example.englishapp // TODO: Thay thế bằng package thực tế của bạn

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.englishapp.adapter.QuestionAdapter // TODO: Import Adapter
import com.example.englishapp.data.Question // TODO: Import Data Class Question
import com.example.englishapp.databinding.ActivityQuestionBinding // TODO: Import lớp binding cho layout Activity

class FillBlankQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionBinding
    private lateinit var questionAdapter: QuestionAdapter

    // TODO: Dữ liệu mẫu cho danh sách câu hỏi. Trong thực tế, bạn sẽ tải từ nguồn khác.
    // Cần sửa Data Class Question để thêm 'var userAnswer: String? = null'
    private val questionList: MutableList<Question> = mutableListOf(
        Question("q1", listOf("1. He is", "student."), 0),
        Question("q2", listOf("2. We", "students."), 0),
        Question("q3", listOf("3. He", "a student."), 0),
        Question("q4", listOf("4. This is", "umbrella."), 0)
        // TODO: Thêm các câu hỏi khác
    )

    // TODO: Danh sách đáp án đúng tương ứng với questionList (dựa vào ID hoặc vị trí)
    private val correctAnswers = mapOf(
        "q1" to "a",
        "q2" to "are",
        "q3" to "is",
        "q4" to "an"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo binding và gán layout
        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Cấu hình Toolbar ---
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút Back
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Xử lý sự kiện khi bấm nút Back
        }

        // --- Cấu hình RecyclerView ---
        setupRecyclerView()

        // --- Cấu hình Listener cho nút KIỂM TRA ---
        binding.checkButton.setOnClickListener {
            handleCheckButtonClick()
        }
    }

    private fun setupRecyclerView() {
        // Khởi tạo Adapter, truyền danh sách câu hỏi
        questionAdapter = QuestionAdapter(questionList)

        // Thiết lập LayoutManager (LinearLayoutManager cho danh sách dọc)
        binding.questionsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Gán Adapter cho RecyclerView
        binding.questionsRecyclerView.adapter = questionAdapter

        // TODO: Có thể thêm ItemDecoration nếu cần
    }

    // Hàm xử lý khi click vào nút KIỂM TRA
    private fun handleCheckButtonClick() {
        // TODO: Cần sửa Data Class Question để thêm 'var userAnswer: String? = null'
        // và sửa Adapter để lưu userAnswer

        // Lấy đáp án người dùng từ Adapter (cần triển khai hàm getUserAnswers trong Adapter)
        // val userAnswers = questionAdapter.getUserAnswers()

        var correctCount = 0
        // TODO: Lặp qua danh sách câu hỏi và so sánh đáp án người dùng với đáp án đúng
        // Dựa trên cách bạn lưu trữ đáp án người dùng và đáp án đúng

        // Ví dụ logic kiểm tra (cần sửa Data Class và Adapter trước):
        /*
        for (i in questionList.indices) {
            val question = questionList[i]
            val userAnswer = userAnswers[i]?.trim() // Lấy đáp án đã nhập và bỏ khoảng trắng
            val correctAnswer = correctAnswers[question.id]?.trim() // Lấy đáp án đúng

            if (userAnswer.equals(correctAnswer, ignoreCase = true)) { // So sánh không phân biệt chữ hoa/thường
                correctCount++
                // TODO: Cập nhật UI item để hiển thị đúng
                // questionAdapter.updateFeedback(i, true)
            } else {
                 // TODO: Cập nhật UI item để hiển thị sai
                 // questionAdapter.updateFeedback(i, false)
            }
        }
        */

        // Hiển thị kết quả tổng quát
        // Toast.makeText(this, "Bạn đã trả lời đúng $correctCount / ${questionList.size} câu.", Toast.LENGTH_SHORT).show()

        // Hiện tại chỉ hiển thị thông báo khi click nút
        Toast.makeText(this, "Nút KIỂM TRA đã được bấm.", Toast.LENGTH_SHORT).show()
    }

    // Override onSupportNavigateUp() nếu bạn có nút Back trên Toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
