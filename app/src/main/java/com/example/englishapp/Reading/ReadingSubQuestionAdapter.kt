package com.example.englishapp.Reading

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R
import com.example.englishapp.data.SubQuestion
import com.example.englishapp.adapter.OptionAdapter

class ReadingSubQuestionAdapter(
    private val subQuestions: MutableList<SubQuestion> // Danh sách các câu hỏi con (ví dụ: câu hỏi trắc nghiệm, điền từ, v.v.)
) : RecyclerView.Adapter<ReadingSubQuestionAdapter.BaseViewHolder>() { // Kế thừa từ RecyclerView.Adapter, sử dụng BaseViewHolder làm kiểu ViewHolder cơ sở

    private val selectedOptions = mutableMapOf<Int, String?>() // Một Map để lưu trữ lựa chọn của người dùng cho từng câu hỏi con: Khóa là vị trí của câu hỏi con, Giá trị là ID của lựa chọn đã chọn.

    // Một Map để lưu trữ tham chiếu đến OptionAdapter con cho từng câu hỏi con.
    // Điều này quan trọng để có thể gọi phương thức của OptionAdapter con một cách trực tiếp từ Adapter cha.
    private val optionAdapters = mutableMapOf<Int, OptionAdapter>()

    // Định nghĩa các loại View Type. Hiện tại chỉ có một loại: trắc nghiệm.
    private val TYPE_MULTIPLE_CHOICE = 0

    // Phương thức này được gọi để xác định loại View Type cho một item tại vị trí cụ thể.
    // Điều này cho phép RecyclerView hiển thị các loại layout khác nhau nếu có nhiều loại câu hỏi.
    override fun getItemViewType(position: Int): Int {
        return TYPE_MULTIPLE_CHOICE // Hiện tại luôn trả về loại trắc nghiệm
    }

    // Phương thức này được gọi khi RecyclerView cần tạo một ViewHolder mới.
    // Nó "phồng" (inflate) layout XML của item và trả về một ViewHolder tương ứng.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context) // Lấy LayoutInflater từ ngữ cảnh của ViewGroup cha
        val view = inflater.inflate(R.layout.item_reading, parent, false) // Khởi tạo layout item_reading.xml
        return MultipleChoiceViewHolder(view) // Trả về một MultipleChoiceViewHolder mới
    }

    // Phương thức này được gọi để gán dữ liệu từ tập dữ liệu vào ViewHolder tại một vị trí cụ thể.
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(subQuestions[position], position) // Gọi phương thức bind của ViewHolder để thiết lập dữ liệu

        // LOẠI BỎ DÒNG NÀY:
        // applyFeedbackColorsToInput(holder.itemView, position, isCorrectAnswerPreviously(position))
        // Dòng này đã được loại bỏ vì phản hồi (feedback) chỉ nên được áp dụng khi người dùng nhấn nút "Kiểm tra",
        // không phải mỗi khi item được bind lại (điều này có thể xảy ra khi cuộn RecyclerView).
        // Feedback sẽ được quản lý trực tiếp thông qua hàm `updateFeedback`.
    }

    // Phương thức này trả về tổng số lượng item trong tập dữ liệu.
    override fun getItemCount(): Int = subQuestions.size // Trả về số lượng câu hỏi con

    // Lớp trừu tượng BaseViewHolder, là cơ sở cho tất cả các loại ViewHolder trong Adapter này.
    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Ánh xạ các View chung cho tất cả các loại câu hỏi con
        val subQuestionText: TextView = itemView.findViewById(R.id.subQuestionText) // TextView hiển thị nội dung câu hỏi con
        val answerInputContainer: LinearLayout = itemView.findViewById(R.id.answerInputContainer) // Container chứa phần nhập/chọn câu trả lời

        abstract fun bind(subQuestion: SubQuestion, position: Int) // Phương thức trừu tượng để liên kết dữ liệu, các lớp con phải triển khai
    }

    // Lớp MultipleChoiceViewHolder dành riêng cho việc hiển thị các câu hỏi trắc nghiệm.
    inner class MultipleChoiceViewHolder(itemView: View) : BaseViewHolder(itemView) {
        var currentOptionAdapter: OptionAdapter? = null // Biến để giữ tham chiếu đến OptionAdapter của câu hỏi trắc nghiệm hiện tại trong ViewHolder này

        override fun bind(subQuestion: SubQuestion, position: Int) {
            subQuestionText.text = "${position + 1}. ${subQuestion.questionText}" // Hiển thị số thứ tự và nội dung câu hỏi

            answerInputContainer.removeAllViews() // Xóa bỏ tất cả các View con cũ trong container để chuẩn bị cho các lựa chọn mới

            val optionsRecyclerView = RecyclerView(itemView.context).apply { // Tạo một RecyclerView mới để hiển thị các lựa chọn trắc nghiệm
                layoutParams = LinearLayout.LayoutParams( // Đặt các tham số bố cục
                    LinearLayout.LayoutParams.MATCH_PARENT, // Chiều rộng bằng cha
                    LinearLayout.LayoutParams.WRAP_CONTENT // Chiều cao tự điều chỉnh theo nội dung
                )
                layoutManager = GridLayoutManager(itemView.context, 2) // Đặt GridLayoutManager với 2 cột cho các lựa chọn
            }

            subQuestion.options?.let { options -> // Nếu câu hỏi có các lựa chọn (chắc chắn có với loại trắc nghiệm)
                // Khởi tạo OptionAdapter với danh sách các lựa chọn và một callback khi lựa chọn được chọn
                val adapter = OptionAdapter(options.toMutableList()) { option, optionPosition ->
                    selectedOptions[position] = option.id // Lưu ID của lựa chọn đã chọn vào map selectedOptions
                    Log.d("SubAdapterDebug", "Option selected for position $position: ${option.text} (ID: ${option.id})") // Ghi log lựa chọn
                }
                currentOptionAdapter = adapter // Lưu tham chiếu của OptionAdapter vào thuộc tính của ViewHolder
                optionAdapters[position] = adapter // Lưu tham chiếu của OptionAdapter vào map chung của Adapter cha, dùng để truy cập sau này
                optionsRecyclerView.adapter = adapter // Gán OptionAdapter cho RecyclerView của các lựa chọn

                // Nếu có lựa chọn đã được lưu từ trước (ví dụ: khi cuộn RecyclerView), thì chọn lại nó
                selectedOptions[position]?.let { savedSelectedId ->
                    val savedIndex = options.indexOfFirst { it.id == savedSelectedId } // Tìm chỉ số của lựa chọn đã lưu
                    if (savedIndex != -1) { // Nếu tìm thấy
                        adapter.selectItem(savedIndex) // Chọn item đó trong OptionAdapter
                    }
                }
            }
            answerInputContainer.addView(optionsRecyclerView) // Thêm RecyclerView chứa các lựa chọn vào container
        }
    }

    // Hàm trả về danh sách các câu hỏi con, bao gồm cả các thông tin người dùng đã nhập/chọn.
    fun getAnsweredSubQuestions(): List<SubQuestion> {
        return subQuestions // Trả về danh sách câu hỏi con (đã có thể được cập nhật trạng thái lựa chọn của người dùng)
    }

    // Hàm trả về ID của lựa chọn đã chọn cho một câu hỏi con tại vị trí cụ thể.
    fun getSelectedOptionId(position: Int): String? {
        return selectedOptions[position] // Trả về ID lựa chọn từ map selectedOptions
    }

    // Hàm này được gọi từ Activity (ReadingQuestionActivity) để áp dụng phản hồi (feedback)
    // khi người dùng nhấn nút "Kiểm tra".
    fun updateFeedback(position: Int, isCorrect: Boolean) {
        Log.d("SubAdapterDebug", "updateFeedback: Position=$position, isCorrect=$isCorrect. Notifying item changed.") // Ghi log

        val subQuestion = subQuestions[position] // Lấy câu hỏi con tại vị trí
        val selectedId = selectedOptions[position] // Lấy ID lựa chọn của người dùng
        val correctId = subQuestion.correctAnswerId // Lấy ID đáp án đúng

        // Lấy OptionAdapter của câu hỏi con này và gọi updateFeedbackColors trực tiếp
        // thay vì dựa vào onBindViewHolder của chính nó.
        val optionAdapter = optionAdapters[position] // Lấy OptionAdapter tương ứng từ map
        if (optionAdapter != null) { // Nếu OptionAdapter tồn tại
            Log.d("SubAdapterDebug", "updateFeedback: Calling OptionAdapter.updateFeedbackColors for position $position.") // Ghi log
            // Gọi hàm updateFeedbackColors của OptionAdapter để nó tự xử lý việc hiển thị màu sắc và phản hồi
            optionAdapter.updateFeedbackColors(selectedId, correctId, isCorrect)
        } else {
            Log.e("SubAdapterDebug", "updateFeedback: OptionAdapter for position $position is NULL. Cannot apply feedback.") // Ghi lỗi nếu không tìm thấy OptionAdapter
            // Nếu adapter không tồn tại (ví dụ: view chưa được bind),
            // chúng ta vẫn cần notifyItemChanged để khi nó được bind lại, feedback sẽ được xử lý.
            notifyItemChanged(position) // Thông báo item thay đổi để force re-bind (nếu cần)
        }
    }

    // Hàm này không cần thiết nữa vì feedback được áp dụng trực tiếp qua updateFeedback()
    // và OptionAdapter tự quản lý việc hiển thị đáp án đúng/sai.
    // private fun isCorrectAnswerPreviously(position: Int): Boolean { ... } // Hàm này đã được loại bỏ

    // Hàm này cũng không cần thiết nữa vì nó được thay thế bằng logic trực tiếp trong updateFeedback()
    // private fun applyFeedbackColorsToInput(itemView: View, position: Int, isCorrect: Boolean) { ... } // Hàm này đã được loại bỏ

    private var recyclerView: RecyclerView? = null // Tham chiếu đến RecyclerView chứa Adapter này
    fun setRecyclerView(recyclerView: RecyclerView) { // Hàm để gán tham chiếu RecyclerView từ bên ngoài
        this.recyclerView = recyclerView
    }

    private fun getRecyclerView(): RecyclerView? { // Hàm để lấy tham chiếu RecyclerView
        return recyclerView
    }

    // Hàm này được gọi từ Activity (ReadingQuestionActivity) khi chuyển sang bài đọc mới,
    // để xóa tất cả các lựa chọn và phản hồi của bài đọc cũ.
    fun clearFeedback() {
        Log.d("SubAdapterDebug", "clearFeedback: Clearing all selections and feedback.") // Ghi log
        selectedOptions.clear() // Xóa tất cả các lựa chọn đã lưu trong map

        // Reset feedbackMode cho tất cả OptionAdapter con
        // Duyệt qua tất cả các OptionAdapter đã được lưu trong map
        for (adapter in optionAdapters.values) {
            adapter.clearSelection() // Gọi hàm clearSelection trên từng OptionAdapter để xóa lựa chọn và feedback
        }
        // Sau khi reset các OptionAdapter, notifyDataSetChanged cho ReadingSubQuestionAdapter
        // để nó re-bind và đảm bảo tất cả các OptionAdapter đều ở chế độ feedbackMode = false.
        notifyDataSetChanged() // Thông báo cho RecyclerView rằng toàn bộ tập dữ liệu đã thay đổi để nó vẽ lại mọi thứ
        Log.d("SubAdapterDebug", "clearFeedback: Notified data set changed for ReadingSubQuestionAdapter.") // Ghi log
    }
}