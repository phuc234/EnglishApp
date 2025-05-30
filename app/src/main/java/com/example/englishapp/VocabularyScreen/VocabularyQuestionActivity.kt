package com.example.englishapp.VocabularyScreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.englishapp.databinding.ActivityVocabularyQuestionBinding
import com.example.englishapp.data.VocabularyQuestion
import com.example.englishapp.data.VocabularyOption
import com.example.englishapp.repository.VocabularyQuestionRepository
import com.example.englishapp.adapter.VocabularyOptionAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.englishapp.LoginActivity // Import LoginActivity để điều hướng nếu chưa đăng nhập

class VocabularyQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVocabularyQuestionBinding
    private lateinit var optionsAdapter: VocabularyOptionAdapter
    private lateinit var vocabularyQuestionRepository: VocabularyQuestionRepository
    private lateinit var auth: FirebaseAuth
    // private lateinit var db: FirebaseFirestore // Không cần dùng Firestore trực tiếp ở đây nữa

    private var allQuestions: List<VocabularyQuestion> = emptyList()
    private lateinit var currentQuestion: VocabularyQuestion
    private var currentQuestionIndex: Int = 0

    // Biến tích lũy điểm trong phiên làm bài hiện tại
    private var totalScoreEarnedInThisSession: Long = 0L

    // Số điểm người dùng nhận được cho mỗi câu trả lời đúng
    private val POINTS_PER_CORRECT_ANSWER = 10L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVocabularyQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        // db = FirebaseFirestore.getInstance() // Không cần khởi tạo ở đây nữa

        if (auth.currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để làm bài test.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        vocabularyQuestionRepository = VocabularyQuestionRepository(applicationContext)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            // Khi người dùng bấm nút Back trên toolbar, kết thúc bài và trả điểm về
            Log.d("VocabularyActivity", "Người dùng bấm nút Back. Trả về điểm: $totalScoreEarnedInThisSession")
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
        }

        // Lấy tên file bài tập từ Intent
        val exercisesFile = intent.getStringExtra("exercises_file")
        if (exercisesFile != null) {
            allQuestions = vocabularyQuestionRepository.getAllVocabularyQuestions(exercisesFile)
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy file bài tập.", Toast.LENGTH_SHORT).show()
            Log.e("VocabularyActivity", "Không nhận được tên file bài tập từ Intent.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không có file bài tập
            return
        }

        if (allQuestions.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)
        } else {
            Toast.makeText(this, "Lỗi: Không tải được dữ liệu câu hỏi từ vựng. Danh sách rỗng.", Toast.LENGTH_LONG).show()
            Log.e("VocabularyActivity", "Không tải được dữ liệu câu hỏi từ vựng. Danh sách rỗng hoặc file không đúng.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không có câu hỏi
        }

        binding.checkButton.setOnClickListener {
            handleCheckButtonClick()
        }
    }

    // Hàm này được đổi tên và chỉ cộng điểm vào biến cục bộ
    private fun addScoreLocally(pointsToAdd: Long) {
        totalScoreEarnedInThisSession += pointsToAdd
        Log.d("VocabularyTest", "Điểm tích lũy trong phiên: $totalScoreEarnedInThisSession")
        // Không cập nhật Firestore trực tiếp từ đây
    }

    private fun displayQuestion(index: Int) {
        if (index >= 0 && index < allQuestions.size) {
            currentQuestion = allQuestions[index]
            binding.questionText.text = currentQuestion.questionText
            setupOptionsRecyclerView(currentQuestion.options.toMutableList())
            optionsAdapter.clearSelection()
        } else {
            // Khi hết câu hỏi, gửi tổng điểm về HomeActivity và đóng Activity này
            Toast.makeText(this, "Bạn đã hoàn thành tất cả câu hỏi từ vựng!", Toast.LENGTH_LONG).show()
            Log.d("VocabularyActivity", "Đã hoàn thành tất cả câu hỏi từ vựng. Trả về $totalScoreEarnedInThisSession điểm.")
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
        }
    }

    private fun setupOptionsRecyclerView(options: MutableList<VocabularyOption>) {
        optionsAdapter = VocabularyOptionAdapter(options) { option, position ->
            handleOptionClick(option, position)
        }
        val layoutManager = GridLayoutManager(this, 2)
        binding.optionsRecyclerView.layoutManager = layoutManager
        binding.optionsRecyclerView.adapter = optionsAdapter
    }

    private fun handleOptionClick(option: VocabularyOption, position: Int) {
        optionsAdapter.selectItem(position)
    }

    private fun handleCheckButtonClick() {
        val selectedOption = optionsAdapter.getSelectedItem()

        if (selectedOption == null) {
            Toast.makeText(this, "Vui lòng chọn một đáp án.", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedOption.id == currentQuestion.correctAnswerId) {
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show()
            addScoreLocally(POINTS_PER_CORRECT_ANSWER) // Gọi hàm cộng điểm cục bộ
            currentQuestionIndex++
            displayQuestion(currentQuestionIndex)

        } else {
            val correctOption = currentQuestion.options.find { it.id == currentQuestion.correctAnswerId }
            val correctOptionText = correctOption?.text ?: "Đáp án đúng"
            Toast.makeText(this, "Sai rồi. Đáp án đúng là: ${correctOptionText}", Toast.LENGTH_LONG).show()
            // Vẫn chuyển câu hỏi khi sai, nhưng không cộng điểm
            currentQuestionIndex++
            displayQuestion(currentQuestionIndex)
        }
    }

    // Hàm này sẽ được gọi khi Activity bài tập kết thúc (bao gồm cả khi bấm nút back)
    private fun finishExerciseAndReturnScore(score: Long) {
        val resultIntent = Intent()
        resultIntent.putExtra("score_earned", score)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    // Override onSupportNavigateUp để đảm bảo điểm được trả về khi người dùng bấm nút back trên Toolbar
    override fun onSupportNavigateUp(): Boolean {
        // Gọi hàm finishExerciseAndReturnScore để trả điểm về HomeActivity
        finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
        return true // Trả về true để hệ thống tự động xử lý nút back trên toolbar
    }
}