package com.example.englishapp.Question // Khai báo tên gói (package) cho tệp Kotlin này

import android.content.Intent // Import lớp Intent để khởi chạy các hoạt động mới và truyền dữ liệu
import android.os.Bundle // Import lớp Bundle để lưu và khôi phục trạng thái hoạt động
import android.util.Log // Import lớp Log để ghi nhật ký (log) thông báo vào Logcat
import android.widget.Toast // Import lớp Toast để hiển thị các thông báo ngắn gọn
import androidx.appcompat.app.AppCompatActivity // Import AppCompatActivity, lớp cơ sở cho các hoạt động sử dụng thư viện AppCompat
import androidx.core.content.ContextCompat // Import ContextCompat để làm việc với màu sắc và drawable một cách tương thích
import androidx.recyclerview.widget.LinearLayoutManager // Import LinearLayoutManager để sắp xếp các mục theo danh sách tuyến tính trong RecyclerView
import com.example.englishapp.adapter.QuestionAdapter // Import bộ điều hợp (adapter) tùy chỉnh để hiển thị câu hỏi trong RecyclerView
import com.example.englishapp.data.Question // Import lớp dữ liệu biểu thị một câu hỏi
import com.example.englishapp.databinding.ActivityQuestionBinding // Import lớp binding được tạo tự động cho bố cục của hoạt động
import com.example.englishapp.repository.FillBlankQuestionRepository // Import lớp repository để quản lý các câu hỏi điền vào chỗ trống
import com.example.englishapp.LoginActivity // Import lớp LoginActivity để chuyển hướng đến màn hình đăng nhập
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth để xác thực Firebase

class FillBlankQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuestionBinding
    private lateinit var questionAdapter: QuestionAdapter
    private lateinit var fillBlankQuestionRepository: FillBlankQuestionRepository

    private lateinit var auth: FirebaseAuth

    private var allQuestions: MutableList<Question> = mutableListOf()


    private var totalScoreEarnedInThisSession: Long = 0L
    private val POINTS_PER_CORRECT_ANSWER = 10L

    private var isAnswerChecked: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) { // Được gọi khi hoạt động được tạo lần đầu tiên
        super.onCreate(savedInstanceState) // Gọi phương thức onCreate của lớp cha

        binding = ActivityQuestionBinding.inflate(layoutInflater) // Khởi tạo layout cho hoạt động này bằng cách sử dụng view binding
        setContentView(binding.root) // Đặt layout đã được khởi tạo làm nội dung hiển thị của hoạt động

        auth = FirebaseAuth.getInstance() // Lấy một thể hiện của FirebaseAuth

        if (auth.currentUser == null) { // Kiểm tra xem người dùng hiện tại có đang đăng nhập hay không
            Toast.makeText(this, "Bạn cần đăng nhập để làm bài test điền từ.", Toast.LENGTH_SHORT).show() // Hiển thị thông báo toast nếu không có người dùng nào đăng nhập
            startActivity(Intent(this, LoginActivity::class.java)) // Khởi chạy LoginActivity
            finish() // Kết thúc hoạt động hiện tại
            return // Thoát khỏi phương thức onCreate
        }

        fillBlankQuestionRepository = FillBlankQuestionRepository(applicationContext) // Khởi tạo FillBlankQuestionRepository với ngữ cảnh ứng dụng

        setSupportActionBar(binding.toolbar) // Đặt thanh công cụ tùy chỉnh làm thanh hành động của hoạt động
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Bật nút Up (mũi tên quay lại) trên thanh hành động
        binding.toolbar.setNavigationOnClickListener { // Đặt trình nghe click cho biểu tượng điều hướng (nút quay lại) trên thanh công cụ
            // Khi người dùng bấm nút Back trên toolbar, kết thúc bài và trả điểm về
            Log.d("FillBlankActivity", "Người dùng bấm nút Back. Trả về điểm: $totalScoreEarnedInThisSession") // Ghi nhật ký thông báo debug
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Gọi hàm để kết thúc bài tập và trả về điểm số
        }

        // Lấy tên file bài tập từ Intent
        val exercisesFile = intent.getStringExtra("exercises_file") // Lấy tên tệp bài tập được truyền qua Intent
        if (exercisesFile != null) { // Kiểm tra xem tên tệp bài tập có rỗng hay không
            allQuestions = fillBlankQuestionRepository.getAllFillBlankQuestions(exercisesFile).toMutableList() // Tải tất cả các câu hỏi điền vào chỗ trống từ repository
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy file bài tập điền từ.", Toast.LENGTH_SHORT).show() // Hiển thị thông báo toast nếu không tìm thấy tệp
            Log.e("FillBlankActivity", "Không nhận được tên file bài tập từ Intent.") // Ghi nhật ký thông báo lỗi
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không tìm thấy tệp bài tập
            return // Thoát khỏi phương thức onCreate
        }

        if (allQuestions.isNotEmpty()) { // Kiểm tra xem danh sách câu hỏi có rỗng hay không
            setupRecyclerView() // Gọi hàm để thiết lập RecyclerView
        } else {
            Toast.makeText(this, "Lỗi: Không tải được dữ liệu câu hỏi điền từ. Danh sách rỗng.", Toast.LENGTH_LONG).show() // Hiển thị thông báo toast nếu không tải được câu hỏi
            Log.e("FillBlankActivity", "Không tải được dữ liệu câu hỏi điền từ. Danh sách rỗng hoặc file không đúng.") // Ghi nhật ký thông báo lỗi
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm nếu không có câu hỏi nào được tải
        }

        binding.checkButton.setOnClickListener { // Đặt OnClickListener cho nút "Kiểm tra"
            handleCheckButtonClick() // Gọi hàm để xử lý click nút
        }
    }

    // Hàm này được đổi tên và chỉ cộng điểm vào biến cục bộ
    private fun addScoreLocally(pointsToAdd: Long) { // Hàm riêng tư để cộng điểm vào điểm số phiên cục bộ
        totalScoreEarnedInThisSession += pointsToAdd // Cộng số điểm đã cho vào totalScoreEarnedInThisSession
        Log.d("FillBlankTest", "Điểm tích lũy trong phiên: $totalScoreEarnedInThisSession") // Ghi nhật ký điểm số tích lũy hiện tại
    }

    private fun setupRecyclerView() { // Hàm riêng tư để thiết lập RecyclerView
        questionAdapter = QuestionAdapter(allQuestions) // Khởi tạo QuestionAdapter với danh sách tất cả các câu hỏi
        binding.questionsRecyclerView.layoutManager = LinearLayoutManager(this) // Đặt trình quản lý bố cục cho RecyclerView thành LinearLayoutManager
        binding.questionsRecyclerView.adapter = questionAdapter // Đặt bộ điều hợp cho RecyclerView
        questionAdapter.questionsRecyclerView = binding.questionsRecyclerView // Truyền thể hiện RecyclerView cho bộ điều hợp (có thể để truy cập/cập nhật mục trực tiếp)

        binding.checkButton.isEnabled = true // Bật nút "Kiểm tra"
        binding.checkButton.text = "Kiểm tra" // Đặt văn bản của nút "Kiểm tra" thành "Kiểm tra"
        binding.checkButton.backgroundTintList = ContextCompat.getColorStateList(this, com.example.englishapp.R.color.green_header) // Đặt màu nền của nút
        binding.checkButton.setTextColor(ContextCompat.getColor(this, com.example.englishapp.R.color.white)) // Đặt màu văn bản của nút
        isAnswerChecked = false // Đặt lại cờ isAnswerChecked thành false
    }

    // Hàm xử lý khi click vào nút KIỂM TRA
    private fun handleCheckButtonClick() { // Hàm riêng tư để xử lý các lần nhấp vào nút "Kiểm tra"
        if (!isAnswerChecked) { // Kiểm tra xem các câu trả lời đã được kiểm tra hay chưa
            var correctCount = 0 // Khởi tạo bộ đếm cho các câu trả lời đúng
            val questionsWithUserAnswers = questionAdapter.getQuestionsWithUserAnswers() // Lấy danh sách các câu hỏi cùng với câu trả lời của người dùng

            for (i in questionsWithUserAnswers.indices) { // Lặp qua danh sách các câu hỏi
                val question = questionsWithUserAnswers[i] // Lấy câu hỏi hiện tại
                val userAnswer =
                    question.userAnswer?.trim() // Lấy câu trả lời của người dùng, bỏ khoảng trắng
                val correctAnswer = question.correctAnswer.trim() // Lấy câu trả lời đúng, bỏ khoảng trắng

                if (userAnswer.equals(
                        correctAnswer,
                        ignoreCase = true
                    )
                ) { // So sánh câu trả lời của người dùng với câu trả lời đúng, bỏ qua trường hợp chữ hoa/thường
                    correctCount++ // Tăng bộ đếm câu trả lời đúng
                    question.isCorrect = true // Đặt thuộc tính isCorrect của câu hỏi thành true
                    addScoreLocally(POINTS_PER_CORRECT_ANSWER) // Cộng điểm vào điểm số phiên cục bộ
                } else {
                    question.isCorrect = false // Đặt thuộc tính isCorrect của câu hỏi thành false
                }

                // Cập nhật UI item để hiển thị feedback (ví dụ: đổi màu ô nhập)
                questionAdapter.updateFeedback(i) // Cập nhật giao diện người dùng của mục câu hỏi cụ thể để hiển thị phản hồi (ví dụ: thay đổi màu sắc)
            }

            Toast.makeText(
                this,
                "Bạn đã trả lời đúng $correctCount / ${questionsWithUserAnswers.size} câu.", // Tạo thông báo toast hiển thị số câu trả lời đúng
                Toast.LENGTH_LONG
            ).show() // Hiển thị thông báo toast
            binding.checkButton.setText("Hoàn thành") // Thay đổi văn bản nút thành "Hoàn thành"
            binding.checkButton.backgroundTintList = ContextCompat.getColorStateList(this, com.example.englishapp.R.color.blue_selected) // Thay đổi màu nền của nút thành màu xanh
            binding.checkButton.setTextColor(ContextCompat.getColor(this, com.example.englishapp.R.color.white)) // Đặt màu văn bản của nút thành màu trắng
            isAnswerChecked = true // Đặt cờ isAnswerChecked thành true
        }
        else {
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Nếu câu trả lời đã được kiểm tra, kết thúc bài tập và trả về điểm số
        }
    }

    // Hàm này sẽ được gọi khi Activity bài tập kết thúc (bao gồm cả khi bấm nút back)
    private fun finishExerciseAndReturnScore(score: Long) { // Hàm riêng tư để kết thúc bài tập và trả về điểm số
        val resultIntent = Intent() // Tạo một đối tượng Intent mới
        resultIntent.putExtra("score_earned", score) // Đặt điểm kiếm được vào Intent
        setResult(RESULT_OK, resultIntent) // Đặt kết quả của hoạt động là OK và bao gồm Intent
        finish() // Kết thúc hoạt động hiện tại
    }

    // Override onSupportNavigateUp để đảm bảo điểm được trả về khi người dùng bấm nút back trên Toolbar
    override fun onSupportNavigateUp(): Boolean { // Ghi đè phương thức onSupportNavigateUp, được gọi khi nút Up (mũi tên quay lại) được nhấn
        // Gọi hàm finishExerciseAndReturnScore để trả điểm về HomeActivity
        finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Gọi hàm để kết thúc bài tập và trả về điểm số
        return true // Trả về true để chỉ ra rằng sự kiện Up đã được xử lý
    }
}