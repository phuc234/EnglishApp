package com.example.englishapp.adapter // TODO: Thay thế bằng package thực tế của bạn

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.data.Question // TODO: Import Data Class Question
import com.example.englishapp.databinding.ItemQuestionBinding // TODO: Import lớp binding cho layout item

class QuestionAdapter(
    private val questions: MutableList<Question> // Sử dụng MutableList để lưu trữ đáp án người dùng
) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    // ViewHolder: Giữ các tham chiếu đến View của một item
    class ViewHolder(val binding: ItemQuestionBinding) : RecyclerView.ViewHolder(binding.root) {
        // Biến để giữ tham chiếu đến TextWatcher hiện tại để tránh lỗi khi RecyclerView tái sử dụng View
        var textWatcher: TextWatcher? = null
    }

    // Tạo ViewHolder mới khi cần
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout item bằng View Binding
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Gán dữ liệu vào View của ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val question = questions[position]

        // Gán các phần của câu hỏi vào TextViews
        // Giả định format là: Phần trước chỗ trống, Phần sau chỗ trống
        // Nếu format khác (ví dụ: nhiều chỗ trống), cần điều chỉnh logic này
        if (question.parts.size > question.blankIndex) {
            holder.binding.questionPartBeforeBlank.text = question.parts[0] // Phần đầu tiên
        } else {
            holder.binding.questionPartBeforeBlank.text = "" // Không có phần trước
        }

        if (question.parts.size > question.blankIndex + 1) {
            holder.binding.questionPartAfterBlank.text = question.parts[1] // Phần thứ hai
        } else {
            holder.binding.questionPartAfterBlank.text = "" // Không có phần sau
        }


        // Xóa TextWatcher cũ trước khi thêm cái mới (để tránh lỗi tái sử dụng View)
        holder.binding.blankInput.removeTextChangedListener(holder.textWatcher)

        // Gán đáp án đã nhập (nếu có)
        // TODO: Cần một cách để lưu trữ đáp án người dùng, ví dụ thêm vào Data Class Question
        // Hiện tại, chúng ta sẽ thêm một thuộc tính tạm vào Data Class hoặc dùng Map riêng
        // Để đơn giản, chúng ta sẽ thêm một thuộc tính tạm vào Data Class Question
        // (Bạn cần sửa Data Class Question để thêm 'var userAnswer: String? = null')
        // holder.binding.blankInput.setText(question.userAnswer)


        // Thêm TextWatcher để theo dõi nhập liệu của người dùng
        holder.textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Lưu đáp án của người dùng vào danh sách dữ liệu
                // TODO: Cần sửa Data Class Question để thêm 'var userAnswer: String? = null'
                // questions[position].userAnswer = s.toString()
            }
        }
        holder.binding.blankInput.addTextChangedListener(holder.textWatcher)

        // TODO: Có thể thêm logic để hiển thị phản hồi (đúng/sai)
        // holder.binding.feedbackText.visibility = if (question.showFeedback) View.VISIBLE else View.GONE
        // holder.binding.feedbackText.text = question.feedbackText
    }

    // Trả về tổng số item trong danh sách
    override fun getItemCount(): Int {
        return questions.size
    }

    // Hàm để lấy tất cả đáp án người dùng đã nhập
    // TODO: Cần sửa Data Class Question để thêm 'var userAnswer: String? = null'
    /*
    fun getUserAnswers(): List<String?> {
        return questions.map { it.userAnswer }
    }
    */

    // TODO: Hàm để cập nhật trạng thái feedback sau khi kiểm tra
    /*
    fun updateFeedback(position: Int, isCorrect: Boolean) {
        questions[position].showFeedback = true
        questions[position].feedbackText = if (isCorrect) "Đúng!" else "Sai!"
        notifyItemChanged(position)
    }
    */
}
