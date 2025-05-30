package com.example.englishapp.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R // Đảm bảo import R đúng
import com.example.englishapp.data.Question // Import data class Question
import com.example.englishapp.databinding.ItemQuestionBinding // Import lớp binding cho layout item

// Adapter cho RecyclerView hiển thị các câu hỏi điền từ
class QuestionAdapter(
    private val questions: MutableList<Question> // Danh sách các câu hỏi
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    // ViewHolder chứa các View của một item
    inner class QuestionViewHolder(val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        val part1TextView = binding.part1TextView
        val answerEditText = binding.answerEditText
        val part2TextView = binding.part2TextView
        val feedbackTextView = binding.feedbackTextView

        // TextWatcher để cập nhật userAnswer khi người dùng nhập
        private var textWatcher: TextWatcher? = null

        fun bind(question: Question, position: Int) {
            // Loại bỏ TextWatcher cũ để tránh lỗi khi RecyclerView tái sử dụng ViewHolder
            textWatcher?.let { answerEditText.removeTextChangedListener(it) }

            // Hiển thị các phần của câu hỏi
            part1TextView.text = question.parts[0]
            if (question.parts.size > question.blankIndex + 1) {
                part2TextView.text = question.parts[question.blankIndex + 1]
                part2TextView.visibility = ViewGroup.VISIBLE
            } else {
                part2TextView.visibility = ViewGroup.GONE
            }

            // Đặt văn bản đã nhập nếu có
            answerEditText.setText(question.userAnswer)

            // Thiết lập TextWatcher mới
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    questions[position].userAnswer = s.toString() // Cập nhật userAnswer trong Data Model
                    questions[position].isCorrect = null // Đặt lại trạng thái feedback khi người dùng nhập lại
                    updateFeedback(position) // Cập nhật feedback ngay lập tức để ẩn nếu đang hiển thị
                }
                override fun afterTextChanged(s: Editable?) {}
            }
            answerEditText.addTextChangedListener(textWatcher)

            // Cập nhật giao diện feedback
            updateFeedback(position)
        }
    }

    // Tạo ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    // Gán dữ liệu vào ViewHolder
    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    // Trả về số lượng item trong danh sách
    override fun getItemCount(): Int {
        return questions.size
    }

    // Hàm cập nhật feedback cho một item cụ thể
    fun updateFeedback(position: Int) {
        if (position >= 0 && position < questions.size) {
            val question = questions[position]
            val holder = (questionsRecyclerView.findViewHolderForAdapterPosition(position) as? QuestionViewHolder)
            holder?.let { // Chỉ cập nhật nếu ViewHolder còn hiển thị
                if (question.isCorrect != null) {
                    it.feedbackTextView.visibility = ViewGroup.VISIBLE
                    if (question.isCorrect == true) {
                        it.feedbackTextView.text = "Chính xác!"
                        it.feedbackTextView.setTextColor(it.itemView.context.getColor(R.color.correct_answer_color)) // Màu xanh lá
                    } else {
                        it.feedbackTextView.text = "Sai rồi. Đáp án đúng: ${question.correctAnswer}"
                        it.feedbackTextView.setTextColor(it.itemView.context.getColor(R.color.wrong_answer_color)) // Màu đỏ
                    }
                } else {
                    it.feedbackTextView.visibility = ViewGroup.GONE
                }
            }
        }
    }

    // Hàm để lấy tất cả câu trả lời của người dùng
    fun getQuestionsWithUserAnswers(): List<Question> {
        return questions // Trả về danh sách đã được cập nhật userAnswer
    }

    // Biến tạm để truy cập RecyclerView từ hàm updateFeedback (chỉ sử dụng cho mục đích demo)
    // Trong một ứng dụng thực tế, bạn có thể truyền RecyclerView vào Adapter hoặc sử dụng callback
    lateinit var questionsRecyclerView: RecyclerView // Sẽ được gán trong Activity
}
