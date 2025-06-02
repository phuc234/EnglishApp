package com.example.englishapp.Reading

import android.content.Intent // Import lớp Intent để chuyển đổi giữa các Activity
import android.os.Bundle // Import Bundle để lưu và khôi phục trạng thái Activity
import android.util.Log // Import Log để ghi nhật ký debug/error
import android.widget.Toast // Import Toast để hiển thị thông báo ngắn
import androidx.appcompat.app.AppCompatActivity // Lớp cơ sở cho Activity với các tính năng tương thích ngược
import androidx.recyclerview.widget.LinearLayoutManager // Quản lý bố cục tuyến tính cho RecyclerView
import com.example.englishapp.LoginActivity // Activity đăng nhập, dùng để chuyển hướng nếu chưa đăng nhập
import com.example.englishapp.data.ReadingQuestion // Data class cho một bài đọc hiểu hoàn chỉnh
import com.example.englishapp.data.SubQuestion // Data class cho một câu hỏi con trong bài đọc
import com.example.englishapp.databinding.ActivityReadingBinding // Lớp binding cho layout của Activity này
import com.example.englishapp.repository.ReadingRepository // Repository để lấy dữ liệu bài đọc hiểu
import com.google.firebase.auth.FirebaseAuth // Để kiểm tra trạng thái xác thực của người dùng Firebase

class ReadingQuestionActivity : AppCompatActivity() {

    // Khai báo các biến thành viên, sẽ được khởi tạo sau (lateinit)
    private lateinit var binding: ActivityReadingBinding
    private lateinit var readingQuestionRepository: ReadingRepository
    private lateinit var auth: FirebaseAuth

    private var allReadingQuestions: List<ReadingQuestion> = emptyList() // Danh sách tất cả các bài đọc hiểu được tải từ file
    private lateinit var currentReadingQuestion: ReadingQuestion // Bài đọc hiện tại đang hiển thị
    private var currentReadingQuestionIndex: Int = 0 // Chỉ số của bài đọc hiện tại trong danh sách

    private lateinit var subQuestionAdapter: ReadingSubQuestionAdapter // Adapter cho RecyclerView hiển thị các câu hỏi con
    private var isAnswerChecked: Boolean = false // Cờ theo dõi trạng thái kiểm tra đáp án (đã kiểm tra hay chưa)
    private var totalScoreEarnedInThisSession: Long = 0L // Tổng điểm kiếm được trong phiên làm bài hiện tại
    private val POINTS_PER_CORRECT_ANSWER = 20L // Số điểm cho mỗi câu hỏi con trả lời đúng

    override fun onCreate(savedInstanceState: Bundle?) { // Phương thức được gọi khi Activity được tạo lần đầu
        super.onCreate(savedInstanceState) // Gọi onCreate của lớp cha
        binding = ActivityReadingBinding.inflate(layoutInflater) // Khởi tạo binding từ layout
        setContentView(binding.root) // Đặt layout làm nội dung của Activity

        auth = FirebaseAuth.getInstance() // Lấy thể hiện của FirebaseAuth
        if (auth.currentUser == null) { // Kiểm tra nếu người dùng chưa đăng nhập
            Toast.makeText(this, "Bạn cần đăng nhập để làm bài đọc hiểu.", Toast.LENGTH_SHORT).show() // Thông báo yêu cầu đăng nhập
            startActivity(Intent(this, LoginActivity::class.java)) // Chuyển đến màn hình đăng nhập
            finish() // Kết thúc Activity hiện tại
            return // Thoát khỏi onCreate
        }

        readingQuestionRepository = ReadingRepository(applicationContext) // Khởi tạo repository

        setSupportActionBar(binding.toolbar) // Đặt toolbar làm action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút quay lại trên toolbar
        binding.toolbar.setNavigationOnClickListener { // Xử lý sự kiện khi nút quay lại được bấm
            Log.d("ReadingActivity", "Người dùng bấm nút Back. Trả về điểm: $totalScoreEarnedInThisSession") // Ghi log
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Kết thúc bài tập và trả về điểm
        }

        // Lấy tên file bài tập từ Intent
        val exercisesFile = intent.getStringExtra("exercises_file") // Lấy tên file được truyền từ Activity trước
        if (exercisesFile != null) { // Nếu có tên file
            allReadingQuestions = readingQuestionRepository.getAllReadingQuestions(exercisesFile) // Tải tất cả các bài đọc từ file
        } else { // Nếu không có tên file
            Toast.makeText(this, "Lỗi: Không tìm thấy file bài tập đọc hiểu.", Toast.LENGTH_SHORT).show() // Thông báo lỗi
            Log.e("ReadingActivity", "Không nhận được tên file bài tập từ Intent.") // Ghi log lỗi
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm
            return // Thoát khỏi onCreate
        }

        if (allReadingQuestions.isNotEmpty()) { // Nếu danh sách bài đọc không rỗng
            displayReadingQuestion(currentReadingQuestionIndex) // Hiển thị bài đọc đầu tiên
        } else { // Nếu danh sách bài đọc rỗng
            Toast.makeText(this, "Lỗi: Không tải được dữ liệu câu hỏi đọc hiểu. Danh sách rỗng.", Toast.LENGTH_LONG).show() // Thông báo lỗi
            Log.e("ReadingActivity", "Không tải được dữ liệu câu hỏi đọc hiểu. Danh sách rỗng hoặc file không đúng.") // Ghi log lỗi
            finishExerciseAndReturnScore(0L) // Trả về 0 điểm
        }

        binding.checkButton.setOnClickListener { // Thiết lập sự kiện click cho nút "Kiểm tra"
            handleCheckButtonClick() // Gọi hàm xử lý khi nút được bấm
        }
    }

    private fun addScoreLocally(pointsToAdd: Long) { // Hàm cộng điểm vào tổng điểm của phiên hiện tại
        totalScoreEarnedInThisSession += pointsToAdd // Cộng điểm
        Log.d("ReadingTest", "Điểm tích lũy trong phiên: $totalScoreEarnedInThisSession") // Ghi log điểm tích lũy
    }

    private fun displayReadingQuestion(index: Int) { // Hàm hiển thị một bài đọc hiểu dựa trên chỉ số
        if (index >= 0 && index < allReadingQuestions.size) { // Nếu chỉ số hợp lệ
            currentReadingQuestion = allReadingQuestions[index] // Lấy bài đọc tại chỉ số đó
            binding.passageText.text = currentReadingQuestion.passage // Đặt đoạn văn vào TextView
            setupSubQuestionsRecyclerView(currentReadingQuestion.questions) // Thiết lập RecyclerView cho các câu hỏi con
        } else { // Nếu đã hết bài đọc
            Toast.makeText(this, "Bạn đã hoàn thành tất cả bài đọc hiểu!", Toast.LENGTH_LONG).show() // Thông báo hoàn thành
            Log.d("ReadingActivity", "Đã hoàn thành tất cả bài đọc hiểu. Trả về $totalScoreEarnedInThisSession điểm.") // Ghi log
            finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Kết thúc bài và trả điểm
        }
    }

    private fun setupSubQuestionsRecyclerView(subQuestions: List<SubQuestion>) { // Hàm thiết lập RecyclerView cho các câu hỏi con
        // Luôn tạo adapter mới khi hiển thị một bài đọc hiểu mới.
        // Điều này đảm bảo adapter được reset hoàn toàn.
        subQuestionAdapter = ReadingSubQuestionAdapter(subQuestions.toMutableList()) // Tạo adapter mới với danh sách câu hỏi con
        binding.subQuestionsRecyclerView.layoutManager = LinearLayoutManager(this) // Đặt LinearLayoutManager
        binding.subQuestionsRecyclerView.adapter = subQuestionAdapter // Gán adapter cho RecyclerView
        subQuestionAdapter.setRecyclerView(binding.subQuestionsRecyclerView) // Cung cấp tham chiếu RecyclerView cho adapter (để adapter có thể cập nhật view)
    }

    private fun handleCheckButtonClick() { // Hàm xử lý khi nút "Kiểm tra" được bấm
        Log.d("ActivityDebug", "handleCheckButtonClick: isAnswerChecked=$isAnswerChecked") // Ghi log trạng thái kiểm tra
        if (!isAnswerChecked) { // Nếu chưa kiểm tra đáp án (lần bấm đầu tiên)
            // Đây là lần nhấn đầu tiên: Kiểm tra đáp án và hiển thị feedback
            var correctSubQuestionsCount = 0 // Đếm số câu trả lời con đúng
            val subQuestionsToEvaluate = currentReadingQuestion.questions // Lấy danh sách câu hỏi con của bài đọc hiện tại

            for (i in subQuestionsToEvaluate.indices) { // Duyệt qua từng câu hỏi con
                val subQuestion = subQuestionsToEvaluate[i] // Lấy câu hỏi con hiện tại
                var isSubQuestionCorrect = false // Cờ kiểm tra câu hỏi con có đúng không

                // Chỉ xử lý multiple_choice vì các loại khác đã bị loại bỏ
                if (subQuestion.type == "multiple_choice") { // Nếu là câu hỏi trắc nghiệm
                    val selectedOptionId = subQuestionAdapter.getSelectedOptionId(i) // Lấy ID của lựa chọn mà người dùng đã chọn
                    if (selectedOptionId != null && selectedOptionId == subQuestion.correctAnswerId) { // So sánh với ID đáp án đúng
                        isSubQuestionCorrect = true // Đặt cờ là đúng
                    }
                }

                // Cập nhật feedback cho từng câu hỏi con
                if (isSubQuestionCorrect) { // Nếu câu hỏi con đúng
                    correctSubQuestionsCount++ // Tăng số câu đúng
                    addScoreLocally(POINTS_PER_CORRECT_ANSWER) // Cộng điểm
                    subQuestionAdapter.updateFeedback(i, true) // Cập nhật feedback là đúng
                } else { // Nếu câu hỏi con sai
                    subQuestionAdapter.updateFeedback(i, false) // Cập nhật feedback là sai
                }
            }

            Toast.makeText(this, "Bạn đã trả lời đúng $correctSubQuestionsCount / ${subQuestionsToEvaluate.size} câu hỏi con.", Toast.LENGTH_LONG).show() // Thông báo kết quả
            binding.checkButton.text = "Tiếp tục" // Đổi văn bản nút thành "Tiếp tục"
            isAnswerChecked = true // Đặt cờ đã kiểm tra thành true
            Log.d("ActivityDebug", "handleCheckButtonClick: Answers processed. Set button to 'Tiếp tục', isAnswerChecked=true.") // Ghi log

        } else { // Nếu đã kiểm tra đáp án (lần bấm thứ hai)
            // Đây là lần nhấn thứ hai: Chuyển sang bài đọc/câu hỏi tiếp theo
            Log.d("ActivityDebug", "handleCheckButtonClick: Second click, moving to next question.") // Ghi log
            currentReadingQuestionIndex++ // Tăng chỉ số để chuyển sang bài đọc tiếp theo
            isAnswerChecked = false // Đặt lại cờ đã kiểm tra về false cho bài mới
            binding.checkButton.text = "Kiểm tra" // Đổi văn bản nút về "Kiểm tra"

            subQuestionAdapter.clearFeedback() // Xóa feedback và lựa chọn cũ của bài đọc trước
            displayReadingQuestion(currentReadingQuestionIndex) // Hiển thị bài đọc tiếp theo
        }
    }

    private fun finishExerciseAndReturnScore(score: Long) { // Hàm kết thúc bài tập và trả về điểm số
        val resultIntent = Intent() // Tạo Intent mới
        resultIntent.putExtra("score_earned", score) // Đặt điểm số vào Intent
        setResult(RESULT_OK, resultIntent) // Đặt kết quả Activity là OK
        finish() // Kết thúc Activity
    }

    override fun onSupportNavigateUp(): Boolean { // Xử lý khi nhấn nút "Up" (Back) trên Toolbar
        finishExerciseAndReturnScore(totalScoreEarnedInThisSession) // Kết thúc bài tập và trả về điểm
        return true // Đánh dấu sự kiện đã được xử lý
    }
}