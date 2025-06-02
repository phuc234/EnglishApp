package com.example.englishapp.adapter // Khai báo tên gói (package) cho tệp này

import android.text.Editable // Import lớp Editable để làm việc với nội dung văn bản có thể chỉnh sửa
import android.text.TextWatcher // Import giao diện TextWatcher để theo dõi các thay đổi văn bản
import android.view.LayoutInflater // Import LayoutInflater để khởi tạo các tệp bố cục XML thành các đối tượng View
import android.view.ViewGroup // Import ViewGroup, lớp cơ sở cho các bố cục chứa các View khác
import androidx.recyclerview.widget.RecyclerView // Import RecyclerView, một thành phần UI mạnh mẽ để hiển thị danh sách lớn
import com.example.englishapp.R // Đảm bảo import R đúng, đây là lớp chứa ID tài nguyên của ứng dụng
import com.example.englishapp.data.Question // Import data class Question, định nghĩa cấu trúc dữ liệu cho một câu hỏi
import com.example.englishapp.databinding.ItemQuestionBinding // Import lớp binding cho layout item, được tạo tự động bởi View Binding

// Adapter cho RecyclerView hiển thị các câu hỏi điền từ
class QuestionAdapter(
    private val questions: MutableList<Question> // Khai báo một danh sách (có thể thay đổi) các đối tượng Question
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() { // Kế thừa từ RecyclerView.Adapter và chỉ định ViewHolder của nó

    // ViewHolder chứa các View của một item
    inner class QuestionViewHolder(val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        // Ánh xạ các View từ binding đến các biến cục bộ để dễ truy cập
        val part1TextView = binding.part1TextView // Phần đầu tiên của câu hỏi
        val answerEditText = binding.answerEditText // Ô nhập liệu cho câu trả lời của người dùng
        val part2TextView = binding.part2TextView // Phần thứ hai của câu hỏi (sau chỗ trống)
        val feedbackTextView = binding.feedbackTextView // TextView để hiển thị phản hồi (đúng/sai, đáp án đúng)

        // TextWatcher để cập nhật userAnswer khi người dùng nhập
        private var textWatcher: TextWatcher? = null // Biến để lưu trữ TextWatcher, có thể null

        fun bind(question: Question, position: Int) { // Hàm để liên kết dữ liệu câu hỏi với các View trong ViewHolder
            // Loại bỏ TextWatcher cũ để tránh lỗi khi RecyclerView tái sử dụng ViewHolder
            // Khi ViewHolder được tái sử dụng, nó có thể đã có một TextWatcher gắn liền từ lần trước.
            // Nếu không loại bỏ, TextWatcher cũ sẽ vẫn hoạt động và cập nhật sai vị trí dữ liệu.
            textWatcher?.let { answerEditText.removeTextChangedListener(it) }

            // Hiển thị các phần của câu hỏi
            part1TextView.text = question.parts[0] // Đặt văn bản cho phần đầu tiên của câu hỏi (trước chỗ trống)
            if (question.parts.size > question.blankIndex + 1) { // Kiểm tra xem có phần thứ hai của câu hỏi hay không
                part2TextView.text = question.parts[question.blankIndex + 1] // Đặt văn bản cho phần thứ hai
                part2TextView.visibility = ViewGroup.VISIBLE // Hiển thị phần thứ hai nếu có
            } else {
                part2TextView.visibility = ViewGroup.GONE // Ẩn phần thứ hai nếu không có
            }

            // Đặt văn bản đã nhập nếu có
            answerEditText.setText(question.userAnswer) // Hiển thị câu trả lời mà người dùng đã nhập trước đó (nếu có)

            // Thiết lập TextWatcher mới
            textWatcher = object : TextWatcher { // Tạo một đối tượng TextWatcher mới
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {} // Phương thức gọi trước khi văn bản thay đổi
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { // Phương thức gọi khi văn bản thay đổi
                    questions[position].userAnswer = s.toString() // Cập nhật userAnswer trong Data Model của câu hỏi tương ứng với vị trí hiện tại
                    questions[position].isCorrect = null // Đặt lại trạng thái feedback (về null) khi người dùng nhập lại, để ẩn phản hồi cũ
                    updateFeedback(position) // Gọi hàm để cập nhật feedback ngay lập tức (để ẩn nó nếu đang hiển thị)
                }
                override fun afterTextChanged(s: Editable?) {} // Phương thức gọi sau khi văn bản đã thay đổi
            }
            answerEditText.addTextChangedListener(textWatcher) // Gắn TextWatcher mới vào EditText

            // Cập nhật giao diện feedback
            updateFeedback(position) // Gọi hàm để hiển thị hoặc ẩn phản hồi ban đầu
        }
    }

    // Tạo ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        // Khởi tạo layout item_question.xml thành một đối tượng View bằng cách sử dụng LayoutInflater
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding) // Trả về một thể hiện mới của QuestionViewHolder
    }

    // Gán dữ liệu vào ViewHolder
    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position) // Gọi phương thức bind của ViewHolder để thiết lập dữ liệu cho item ở vị trí 'position'
    }

    // Trả về số lượng item trong danh sách
    override fun getItemCount(): Int {
        return questions.size // Trả về tổng số câu hỏi trong danh sách
    }

    // Hàm cập nhật feedback cho một item cụ thể
    fun updateFeedback(position: Int) {
        if (position >= 0 && position < questions.size) { // Đảm bảo vị trí hợp lệ
            val question = questions[position] // Lấy đối tượng Question tại vị trí đó
            // Tìm ViewHolder đang hiển thị cho vị trí cụ thể này.
            // "as? QuestionViewHolder" là một kiểu ép kiểu an toàn, trả về null nếu không thành công.
            val holder = (questionsRecyclerView.findViewHolderForAdapterPosition(position) as? QuestionViewHolder)
            holder?.let { // Chỉ cập nhật nếu ViewHolder còn hiển thị (tức là holder không phải là null)
                if (question.isCorrect != null) { // Kiểm tra xem đã có trạng thái đúng/sai hay chưa
                    it.feedbackTextView.visibility = ViewGroup.VISIBLE // Hiển thị TextView phản hồi
                    if (question.isCorrect == true) { // Nếu câu trả lời đúng
                        it.feedbackTextView.text = "Chính xác!" // Đặt văn bản là "Chính xác!"
                        // Đặt màu văn bản là màu xanh lá cây được định nghĩa trong R.color
                        it.feedbackTextView.setTextColor(it.itemView.context.getColor(R.color.correct_answer_color))
                    } else { // Nếu câu trả lời sai
                        it.feedbackTextView.text = "Sai rồi. Đáp án đúng: ${question.correctAnswer}" // Đặt văn bản hiển thị đáp án đúng
                        // Đặt màu văn bản là màu đỏ được định nghĩa trong R.color
                        it.feedbackTextView.setTextColor(it.itemView.context.getColor(R.color.wrong_answer_color))
                    }
                } else {
                    it.feedbackTextView.visibility = ViewGroup.GONE // Ẩn TextView phản hồi nếu chưa có trạng thái đúng/sai
                }
            }
        }
    }

    // Hàm để lấy tất cả câu trả lời của người dùng
    fun getQuestionsWithUserAnswers(): List<Question> {
        return questions // Trả về danh sách các câu hỏi đã được cập nhật với câu trả lời của người dùng
    }

    // Biến tạm để truy cập RecyclerView từ hàm updateFeedback (chỉ sử dụng cho mục đích demo)
    // Trong một ứng dụng thực tế, bạn có thể truyền RecyclerView vào Adapter hoặc sử dụng callback
    lateinit var questionsRecyclerView: RecyclerView // Khai báo biến lateinit cho RecyclerView, sẽ được gán trong Activity
}