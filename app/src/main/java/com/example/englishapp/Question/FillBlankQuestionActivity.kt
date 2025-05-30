package com.example.englishapp.Question

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.englishapp.adapter.QuestionAdapter
import com.example.englishapp.data.Question
import com.example.englishapp.databinding.ActivityQuestionBinding
import com.example.englishapp.repository.FillBlankQuestionRepository
import com.example.englishapp.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class FillBlankQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionBinding
    private lateinit var questionAdapter: QuestionAdapter
    private lateinit var fillBlankQuestionRepository: FillBlankQuestionRepository

    private lateinit var auth: FirebaseAuth

    private var allQuestions: MutableList<Question> = mutableListOf()

    // Biến tích lũy điểm trong phiên làm bài hiện tại
    private var totalScoreEarnedInThisSession: Long = 0L // Đặt biến này để tích lũy điểm cục bộ

    // Số điểm người dùng nhận được cho mỗi câu trả lời đúng
    private val POINTS_PER_CORRECT_ANSWER = 10L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để làm bài test điền từ.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        fillBlankQuestionRepository = FillBlankQuestionRepository(applicationContext)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            // Khi người dùng bấm nút Back trên toolbar, kết thúc bài và trả điểm về
            Log.d("FillBlankActivity", "Người dùng bấm nút Back. Trả về điểm: $totalScoreEarnedInThisSession")
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
        }

        // Lấy tên file bài tập từ Intent
        val exercisesFile = intent.getStringExtra("exercises_file")
        if (exercisesFile != null) {
            allQuestions = fillBlankQuestionRepository.getAllFillBlankQuestions(exercisesFile).toMutableList()
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy file bài tập điền từ.", Toast.LENGTH_SHORT).show()
            Log.e("FillBlankActivity", "Không nhận được tên file bài tập từ Intent.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không có file bài tập
            return
        }


        if (allQuestions.isNotEmpty()) {
            setupRecyclerView()
        } else {
            Toast.makeText(this, "Lỗi: Không tải được dữ liệu câu hỏi điền từ. Danh sách rỗng.", Toast.LENGTH_LONG).show()
            Log.e("FillBlankActivity", "Không tải được dữ liệu câu hỏi điền từ. Danh sách rỗng hoặc file không đúng.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không có câu hỏi
        }

        binding.checkButton.setOnClickListener {
            handleCheckButtonClick()
        }
    }

    // Hàm này được đổi tên và chỉ cộng điểm vào biến cục bộ
    private fun addScoreLocally(pointsToAdd: Long) {
        totalScoreEarnedInThisSession += pointsToAdd
        Log.d("FillBlankTest", "Điểm tích lũy trong phiên: $totalScoreEarnedInThisSession")
        // Không cập nhật Firestore trực tiếp từ đây
    }

    private fun setupRecyclerView() {
        questionAdapter = QuestionAdapter(allQuestions)
        binding.questionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.questionsRecyclerView.adapter = questionAdapter
        questionAdapter.questionsRecyclerView = binding.questionsRecyclerView
    }

    // Hàm xử lý khi click vào nút KIỂM TRA
    private fun handleCheckButtonClick() {
        var correctCount = 0
        val questionsWithUserAnswers = questionAdapter.getQuestionsWithUserAnswers()

        for (i in questionsWithUserAnswers.indices) {
            val question = questionsWithUserAnswers[i]
            val userAnswer = question.userAnswer?.trim() // Lấy đáp án đã nhập và bỏ khoảng trắng
            val correctAnswer = question.correctAnswer.trim() // Lấy đáp án đúng

            if (userAnswer.equals(correctAnswer, ignoreCase = true)) { // So sánh không phân biệt chữ hoa/thường
                correctCount++
                question.isCorrect = true // Cập nhật trạng thái đúng
                addScoreLocally(POINTS_PER_CORRECT_ANSWER) // Cộng điểm vào biến cục bộ
            } else {
                question.isCorrect = false // Cập nhật trạng thái sai
            }

            // Cập nhật UI item để hiển thị feedback (ví dụ: đổi màu ô nhập)
            questionAdapter.updateFeedback(i)
        }

        Toast.makeText(this, "Bạn đã trả lời đúng $correctCount / ${questionsWithUserAnswers.size} câu.", Toast.LENGTH_LONG).show()

        // Khi tất cả câu hỏi đã được kiểm tra, kết thúc Activity và trả điểm về HomeActivity
        // Nếu bạn muốn người dùng có thể xem lại hoặc sửa, thì chỉ kết thúc khi họ bấm nút "Hoàn thành"
        // Hiện tại, tôi sẽ kết thúc ngay sau khi kiểm tra tất cả câu hỏi
        finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
    }

    // Hàm này sẽ được gọi khi Activity bài tập kết thúc (bao gồm cả khi bấm nút back)
    private fun finishExerciseAndReturnScore(score: Long) {
        val resultIntent = Intent()
        resultIntent.putExtra("score_earned", score) // Đặt điểm đã kiếm được vào Intent
        setResult(RESULT_OK, resultIntent) // Đặt kết quả là OK
        finish() // Đóng Activity bài tập
    }

    // Override onSupportNavigateUp để đảm bảo điểm được trả về khi người dùng bấm nút back trên Toolbar
    override fun onSupportNavigateUp(): Boolean {
        // Gọi hàm finishExerciseAndReturnScore để trả điểm về HomeActivity
        finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
        return true // Trả về true để hệ thống tự động xử lý nút back trên toolbar
    }
}