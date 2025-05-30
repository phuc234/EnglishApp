package com.example.englishapp.Listening

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.englishapp.adapter.OptionAdapter
import com.example.englishapp.data.ListeningQuestion
import com.example.englishapp.data.OptionItem
import com.example.englishapp.databinding.ActivityListenBinding
import com.example.englishapp.repository.ListeningQuestionRepository
import com.example.englishapp.R
import com.example.englishapp.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class ListeningQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListenBinding
    private lateinit var optionAdapter: OptionAdapter
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var listeningQuestionRepository: ListeningQuestionRepository

    private lateinit var auth: FirebaseAuth

    private var allQuestions: List<ListeningQuestion> = emptyList()
    private lateinit var currentQuestion: ListeningQuestion
    private var currentQuestionIndex: Int = 0

    // Biến tích lũy điểm trong phiên làm bài hiện tại
    private var totalScoreEarnedInThisSession: Long = 0L // Đặt biến này để tích lũy điểm cục bộ

    // Số điểm người dùng nhận được cho mỗi câu trả lời đúng
    private val POINTS_PER_CORRECT_ANSWER = 15L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để làm bài test nghe.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        listeningQuestionRepository = ListeningQuestionRepository(applicationContext)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            // Khi người dùng bấm nút Back trên toolbar, kết thúc bài và trả điểm về
            Log.d("ListeningActivity", "Người dùng bấm nút Back. Trả về điểm: $totalScoreEarnedInThisSession")
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
        }

        // Lấy tên file bài tập từ Intent
        val exercisesFile = intent.getStringExtra("exercises_file")
        if (exercisesFile != null) {
            allQuestions = listeningQuestionRepository.getAllListeningQuestions(exercisesFile)
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy file bài tập nghe.", Toast.LENGTH_SHORT).show()
            Log.e("ListeningActivity", "Không nhận được tên file bài tập từ Intent.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không có file bài tập
            return
        }

        if (allQuestions.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)
        } else {
            Toast.makeText(this, "Lỗi: Không tải được dữ liệu câu hỏi nghe. Danh sách rỗng.", Toast.LENGTH_LONG).show()
            Log.e("ListeningActivity", "Không tải được dữ liệu câu hỏi nghe. Danh sách rỗng hoặc file không đúng.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không có câu hỏi
        }

        binding.speakerIcon.setOnClickListener {
            playAudio()
        }

        binding.checkButton.setOnClickListener {
            handleCheckButtonClick()
        }
    }

    // Hàm này được đổi tên và chỉ cộng điểm vào biến cục bộ
    private fun addScoreLocally(pointsToAdd: Long) {
        totalScoreEarnedInThisSession += pointsToAdd
        Log.d("ListeningTest", "Điểm tích lũy trong phiên: $totalScoreEarnedInThisSession")
        // Không cập nhật Firestore trực tiếp từ đây
    }

    // Hàm hiển thị câu hỏi tại một chỉ số cụ thể
    private fun displayQuestion(index: Int) {
        if (index >= 0 && index < allQuestions.size) {
            currentQuestion = allQuestions[index]

            binding.instructionText.text = "Nghe và hoàn thành câu sau :"
            binding.sentenceText.text = "${currentQuestion.sentenceBeforeBlank} ___${currentQuestion.sentenceAfterBlank}"

            setupOptionsRecyclerView(currentQuestion.options.toMutableList())

            optionAdapter.clearSelection()

            // Reset trạng thái lựa chọn của Adapter cho câu hỏi mới
            binding.checkButton.backgroundTintList = getColorStateList(R.color.button_gray_background)
            binding.checkButton.setTextColor(getColorStateList(android.R.color.black))
            binding.checkButton.isEnabled = true
        } else {
            Toast.makeText(this, "Bạn đã hoàn thành tất cả câu hỏi nghe!", Toast.LENGTH_LONG).show()
            Log.d("ListeningActivity", "Đã hoàn thành tất cả câu hỏi nghe. Trả về $totalScoreEarnedInThisSession điểm.")
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Kết thúc và trả điểm về
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
        binding.checkButton.isEnabled = true // Bật nút kiểm tra khi có lựa chọn
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
            Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show()
            addScoreLocally(POINTS_PER_CORRECT_ANSWER) // Cộng điểm vào biến cục bộ
            currentQuestionIndex++
            displayQuestion(currentQuestionIndex) // Hiển thị câu hỏi tiếp theo
        } else {
            val correctOption = currentQuestion.options.find { it.id == currentQuestion.correctAnswerId }
            val correctOptionText = correctOption?.text ?: "Đáp án đúng"
            Toast.makeText(this, "Sai rồi. Đáp án đúng là: ${correctOptionText}", Toast.LENGTH_LONG).show()
            // Vẫn chuyển câu hỏi khi sai, không cộng điểm
            currentQuestionIndex++
            displayQuestion(currentQuestionIndex)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}