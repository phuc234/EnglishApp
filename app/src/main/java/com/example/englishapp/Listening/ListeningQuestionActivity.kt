package com.example.englishapp.Listening

import android.content.Intent
import android.media.MediaPlayer // Import để phát âm thanh
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat // Import để dễ dàng lấy màu và drawable
import androidx.recyclerview.widget.GridLayoutManager // LayoutManager cho lưới
import androidx.recyclerview.widget.RecyclerView // RecyclerView cơ bản
import com.example.englishapp.data.ListeningQuestion // Data class cho câu hỏi nghe
import com.example.englishapp.data.OptionItem // Data class cho các tùy chọn
import com.example.englishapp.databinding.ActivityListenBinding // View Binding cho layout ActivityListen
import com.example.englishapp.repository.ListeningQuestionRepository // Repository để tải dữ liệu câu hỏi
import com.example.englishapp.R // Import file R để truy cập tài nguyên (colors, drawables)
import com.example.englishapp.LoginActivity // Activity đăng nhập
import com.example.englishapp.adapter.OptionAdapter // Adapter cho các tùy chọn

import com.google.firebase.auth.FirebaseAuth // Firebase Authentication

class ListeningQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListenBinding // Biến binding cho layout
    private lateinit var optionAdapter: OptionAdapter // Adapter cho RecyclerView hiển thị tùy chọn
    private var mediaPlayer: MediaPlayer? = null // Đối tượng để phát âm thanh
    private lateinit var listeningQuestionRepository: ListeningQuestionRepository // Đối tượng để lấy dữ liệu câu hỏi

    private lateinit var auth: FirebaseAuth // Đối tượng Firebase Authentication

    private var allQuestions: List<ListeningQuestion> = emptyList() // Danh sách tất cả câu hỏi nghe
    private lateinit var currentQuestion: ListeningQuestion // Câu hỏi hiện tại đang hiển thị
    private var currentQuestionIndex: Int = 0 // Chỉ số của câu hỏi hiện tại trong danh sách

    // Biến tích lũy tổng điểm người dùng kiếm được trong phiên làm bài hiện tại
    private var totalScoreEarnedInThisSession: Long = 0L

    // Số điểm người dùng nhận được cho mỗi câu trả lời đúng
    private val POINTS_PER_CORRECT_ANSWER = 15L

    private var isAnswerChecked: Boolean = false // Cờ kiểm soát trạng thái của nút "Kiểm tra"/"Tiếp tục"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo View Binding
        binding = ActivityListenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy thể hiện của Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (auth.currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để làm bài test nghe.", Toast.LENGTH_SHORT).show()
            // Chuyển hướng đến LoginActivity nếu chưa đăng nhập
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Đóng activity hiện tại
            return
        }

        // Khởi tạo Repository để tải dữ liệu câu hỏi nghe
        listeningQuestionRepository = ListeningQuestionRepository(applicationContext)

        // Cấu hình Toolbar (thanh tiêu đề)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút Back
        binding.toolbar.setNavigationOnClickListener {
            // Xử lý khi người dùng bấm nút Back trên toolbar
            Log.d("ListeningActivity", "Người dùng bấm nút Back. Trả về điểm: $totalScoreEarnedInThisSession")
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Kết thúc bài và trả điểm về
        }

        // Lấy tên file bài tập từ Intent (được truyền từ Activity gọi đến)
        val exercisesFile = intent.getStringExtra("exercises_file")
        if (exercisesFile != null) {
            // Tải tất cả câu hỏi nghe từ Repository dựa trên tên file
            allQuestions = listeningQuestionRepository.getAllListeningQuestions(exercisesFile)
        } else {
            // Xử lý lỗi nếu không tìm thấy tên file bài tập
            Toast.makeText(this, "Lỗi: Không tìm thấy file bài tập nghe.", Toast.LENGTH_SHORT).show()
            Log.e("ListeningActivity", "Không nhận được tên file bài tập từ Intent.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm
            return
        }

        // Kiểm tra nếu danh sách câu hỏi không rỗng thì hiển thị câu hỏi đầu tiên
        if (allQuestions.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)
        } else {
            // Xử lý lỗi nếu danh sách câu hỏi rỗng
            Toast.makeText(this, "Lỗi: Không tải được dữ liệu câu hỏi nghe. Danh sách rỗng.", Toast.LENGTH_LONG).show()
            Log.e("ListeningActivity", "Không tải được dữ liệu câu hỏi nghe. Danh sách rỗng hoặc file không đúng.")
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm
        }

        // Thiết lập Listener cho icon loa để phát âm thanh
        binding.speakerIcon.setOnClickListener {
            playAudio()
        }

        // Thiết lập Listener cho nút "Kiểm tra"/"Tiếp tục"
        binding.checkButton.setOnClickListener {
            handleCheckButtonClick()
        }
    }

    // Hàm để cộng điểm vào biến tích lũy cục bộ
    private fun addScoreLocally(pointsToAdd: Long) {
        totalScoreEarnedInThisSession += pointsToAdd
        Log.d("ListeningTest", "Điểm tích lũy trong phiên: $totalScoreEarnedInThisSession")
        // Lưu ý: Điểm này sẽ không được cập nhật lên Firebase/Database ở đây
    }

    // Hàm hiển thị câu hỏi tại một chỉ số cụ thể
    private fun displayQuestion(index: Int) {
        // Kiểm tra xem chỉ số câu hỏi có hợp lệ không
        if (index >= 0 && index < allQuestions.size) {
            currentQuestion = allQuestions[index] // Lấy câu hỏi hiện tại

            // Cập nhật UI với dữ liệu câu hỏi
            binding.instructionText.text = "Nghe và hoàn thành câu sau :"
            binding.sentenceText.text = "${currentQuestion.sentenceBeforeBlank} ___${currentQuestion.sentenceAfterBlank}"

            // Cấu hình RecyclerView cho các tùy chọn của câu hỏi hiện tại
            setupOptionsRecyclerView(currentQuestion.options.toMutableList())

            // Reset UI cho nút "Kiểm tra" và các trạng thái liên quan
            binding.checkButton.text = "Kiểm tra" // Đặt lại text của nút
            // Đặt lại màu nền của nút về màu mặc định (green_header)
            binding.checkButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_header)
            // Đặt lại màu chữ của nút về màu trắng
            binding.checkButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.checkButton.isEnabled = false // Vô hiệu hóa nút cho đến khi người dùng chọn đáp án
            isAnswerChecked = false // Đặt lại cờ đã kiểm tra về false
        } else {
            // Nếu đã hết tất cả câu hỏi, thông báo và kết thúc bài tập
            Toast.makeText(this, "Bạn đã hoàn thành tất cả câu hỏi nghe!", Toast.LENGTH_LONG).show()
            Log.d("ListeningActivity", "Đã hoàn thành tất cả câu hỏi nghe. Trả về $totalScoreEarnedInThisSession điểm.")
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Kết thúc và trả điểm
        }
    }

    // Hàm cấu hình RecyclerView cho tùy chọn
    private fun setupOptionsRecyclerView(options: MutableList<OptionItem>) {
        // Khởi tạo OptionAdapter với danh sách tùy chọn và callback khi click
        optionAdapter = OptionAdapter(options) { option, position ->
            handleOptionClick(option, position) // Gọi hàm xử lý click tùy chọn
        }

        // Thiết lập GridLayoutManager với 2 cột cho RecyclerView
        val layoutManager = GridLayoutManager(this, 2)
        binding.optionsRecyclerView.layoutManager = layoutManager
        binding.optionsRecyclerView.adapter = optionAdapter // Gán adapter cho RecyclerView
    }

    // Hàm xử lý khi một tùy chọn đáp án được click
    private fun handleOptionClick(option: OptionItem, position: Int) {
        // Chỉ cho phép xử lý click nếu chưa kiểm tra đáp án
        if (!isAnswerChecked) {
            optionAdapter.selectItem(position) // Gọi hàm selectItem trong adapter để cập nhật lựa chọn
            binding.checkButton.isEnabled = true // Kích hoạt nút "Kiểm tra" khi người dùng đã chọn một đáp án
        }
    }

    // Hàm phát âm thanh
    private fun playAudio() {
        // Giải phóng MediaPlayer cũ nếu có để tránh rò rỉ bộ nhớ
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            // Lấy ID tài nguyên âm thanh từ tên file (ví dụ: "audio_file_name" -> R.raw.audio_file_name)
            val audioResId = resources.getIdentifier(currentQuestion.audioFileName, "raw", packageName)

            if (audioResId != 0) {
                // Tạo và bắt đầu phát MediaPlayer
                mediaPlayer = MediaPlayer.create(this, audioResId)
                mediaPlayer?.start()
            } else {
                // Thông báo nếu không tìm thấy file âm thanh
                Toast.makeText(this, "Không tìm thấy file âm thanh: ${currentQuestion.audioFileName}", Toast.LENGTH_SHORT).show()
                Log.e("ListeningActivity", "Không tìm thấy resource âm thanh cho: ${currentQuestion.audioFileName}")
            }
        } catch (e: Exception) {
            // Xử lý lỗi nếu có vấn đề khi phát âm thanh
            Toast.makeText(this, "Lỗi khi phát âm thanh.", Toast.LENGTH_SHORT).show()
            Log.e("ListeningActivity", "Lỗi phát âm thanh: ", e)
            e.printStackTrace() // In stack trace để debug
        }
    }

    // Hàm xử lý khi click vào nút "Kiểm tra"/"Tiếp tục"
    private fun handleCheckButtonClick() {
        Log.d("ListeningActivity", "handleCheckButtonClick: isAnswerChecked=$isAnswerChecked")
        if (!isAnswerChecked) {
            // Lần nhấn đầu tiên: Kiểm tra đáp án
            val selectedOptionPosition = optionAdapter.selectedItemPosition
            // Kiểm tra xem người dùng đã chọn đáp án chưa
            if (selectedOptionPosition == RecyclerView.NO_POSITION) {
                Toast.makeText(this, "Vui lòng chọn một đáp án.", Toast.LENGTH_SHORT).show()
                return // Thoát khỏi hàm nếu chưa chọn
            }

            // Lấy tùy chọn mà người dùng đã chọn
            val selectedOption = currentQuestion.options.getOrNull(selectedOptionPosition)
            // So sánh ID của tùy chọn đã chọn với ID đáp án đúng
            val isCorrect: Boolean = selectedOption?.id == currentQuestion.correctAnswerId

            if (isCorrect) {
                addScoreLocally(POINTS_PER_CORRECT_ANSWER) // Cộng điểm nếu đúng
                Toast.makeText(this, "Chính xác!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Sai rồi.", Toast.LENGTH_SHORT).show()
            }

            // Gọi updateFeedbackColors trên OptionAdapter để hiển thị màu sắc phản hồi (đúng/sai)
            optionAdapter.updateFeedbackColors(selectedOption?.id, currentQuestion.correctAnswerId, isCorrect)

            // Cập nhật UI của nút: Đổi text thành "Tiếp tục"
            binding.checkButton.text = "Tiếp tục"
            // Đặt màu nền của nút sang màu xanh (blue_selected)
            binding.checkButton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.blue_selected)
            // Đặt màu chữ của nút về trắng
            binding.checkButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            isAnswerChecked = true // Đặt cờ đã kiểm tra là true
            Log.d("ListeningActivity", "handleCheckButtonClick: Answers processed. Set button to 'Tiếp tục', isAnswerChecked=true.")

        } else {
            // Lần nhấn thứ hai: Chuyển sang câu hỏi tiếp theo
            Log.d("ListeningActivity", "handleCheckButtonClick: Second click, moving to next question.")
            currentQuestionIndex++ // Tăng chỉ số câu hỏi
            isAnswerChecked = false // Đặt lại cờ cho câu hỏi mới
            binding.checkButton.text = "Kiểm tra" // Đổi text nút về "Kiểm tra"

            optionAdapter.clearSelection() // Xóa lựa chọn và feedback cho câu hỏi cũ trong adapter
            displayQuestion(currentQuestionIndex) // Hiển thị câu hỏi tiếp theo
        }
    }

    // Hàm này được gọi để kết thúc Activity và trả về tổng điểm
    private fun finishExerciseAndReturnScore(score: Long) {
        val resultIntent = Intent()
        resultIntent.putExtra("score_earned", score) // Đặt điểm đã kiếm được vào Intent
        setResult(RESULT_OK, resultIntent) // Đặt kết quả Activity là OK
        finish() // Đóng Activity hiện tại
    }

    // Override onSupportNavigateUp để xử lý nút Back trên Toolbar
    override fun onSupportNavigateUp(): Boolean {
        // Khi người dùng bấm nút back trên toolbar, gọi hàm kết thúc bài và trả điểm về
        finishExerciseAndReturnScore(totalScoreEarnedInThisSession)
        return true // Trả về true để hệ thống tự động xử lý (không cần gọi super.onBackPressed())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Giải phóng MediaPlayer khi Activity bị hủy để tránh rò rỉ bộ nhớ
        mediaPlayer?.release()
        mediaPlayer = null
    }
}