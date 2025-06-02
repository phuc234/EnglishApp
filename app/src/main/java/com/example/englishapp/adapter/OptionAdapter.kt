package com.example.englishapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat // Import để dễ dàng lấy màu và drawable
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R // Import file R để truy cập tài nguyên (colors, drawables)
import com.example.englishapp.data.OptionItem // Import data class OptionItem

// OptionAdapter quản lý việc hiển thị danh sách các tùy chọn đáp án trong RecyclerView
// Nó cũng xử lý trạng thái lựa chọn và hiển thị feedback đúng/sai.
class OptionAdapter(
    // Danh sách các đối tượng OptionItem sẽ được hiển thị
    private val options: MutableList<OptionItem>,
    // Callback được gọi khi một tùy chọn được click
    private val onItemClick: (OptionItem, Int) -> Unit
) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() { // Kế thừa từ RecyclerView.Adapter

    // Biến lưu trữ vị trí của tùy chọn hiện đang được chọn
    // RecyclerView.NO_POSITION (-1) nghĩa là không có tùy chọn nào được chọn
    var selectedItemPosition: Int = RecyclerView.NO_POSITION
        private set // Chỉ cho phép set giá trị này từ bên trong adapter

    // Biến cờ để kiểm soát chế độ hiển thị feedback (đúng/sai)
    // true: đang ở chế độ feedback, false: chế độ bình thường (chọn đáp án)
    var feedbackMode: Boolean = false
        private set // Chỉ cho phép set giá trị này từ bên trong adapter

    // ID của tùy chọn mà người dùng đã chọn (trong chế độ feedback)
    var userSelectedOptionId: String? = null
        private set

    // ID của tùy chọn đúng cho câu hỏi hiện tại (trong chế độ feedback)
    var correctOptionId: String? = null
        private set

    // ViewHolder đại diện cho mỗi item (tùy chọn) trong RecyclerView
    inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // TextView để hiển thị nội dung của tùy chọn
        val optionText: TextView = itemView.findViewById(R.id.optionText)
    }

    // Được gọi khi RecyclerView cần tạo một ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        // Inflate layout cho từng item (item_option.xml)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_option, parent, false)
        // Trả về một thể hiện mới của OptionViewHolder
        return OptionViewHolder(view)
    }

    // Được gọi để hiển thị dữ liệu tại một vị trí cụ thể trong danh sách
    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        // Lấy đối tượng OptionItem tại vị trí hiện tại
        val option = options[position]
        // Đặt văn bản cho TextView của tùy chọn
        holder.optionText.text = option.text

        // Log để debug trạng thái hiện tại của item
        Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, OptionId=${option.id}, selectedItemPosition=$selectedItemPosition, feedbackMode=$feedbackMode, userSelectedOptionId=$userSelectedOptionId, correctOptionId=$correctOptionId")

        // 1. Áp dụng màu sắc và trạng thái click dựa trên feedbackMode
        if (feedbackMode) {
            // Lấy Context để truy cập tài nguyên
            val context = holder.itemView.context
            // ID của tùy chọn hiện tại đang được bind
            val currentOptionId = option.id

            when {
                // Case 1: Đây là đáp án đúng VÀ cũng là đáp án người dùng đã chọn (màu xanh lá đậm)
                currentOptionId == correctOptionId && currentOptionId == userSelectedOptionId -> {
                    Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, Correct & User Selected (GREEN)")
                    holder.optionText.background = ContextCompat.getDrawable(context, R.drawable.option_background_correct)
                    holder.optionText.setTextColor(ContextCompat.getColor(context, R.color.white)) // Chữ trắng để nổi bật
                }
                // Case 2: Đây là đáp án đúng NHƯNG không được người dùng chọn (màu xanh lá nhạt hơn)
                currentOptionId == correctOptionId && currentOptionId != userSelectedOptionId -> {
                    Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, Correct & Not User Selected (GREEN_UNSELECTED)")
                    holder.optionText.background = ContextCompat.getDrawable(context, R.drawable.option_background_correct_unselected)
                    holder.optionText.setTextColor(ContextCompat.getColor(context, R.color.white)) // Chữ trắng
                }
                // Case 3: Đây là đáp án sai VÀ được người dùng chọn (màu đỏ)
                currentOptionId != correctOptionId && currentOptionId == userSelectedOptionId -> {
                    Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, INCORRECT & User Selected (RED)")
                    holder.optionText.background = ContextCompat.getDrawable(context, R.drawable.option_background_incorrect)
                    holder.optionText.setTextColor(ContextCompat.getColor(context, R.color.white)) // Chữ trắng
                }
                // Case 4: Các tùy chọn khác (không đúng, không được chọn bởi người dùng)
                else -> {
                    Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, Other option (Default white)")
                    // Sử dụng background mặc định (ví dụ: trắng với viền xám)
                    holder.optionText.background = ContextCompat.getDrawable(context, R.drawable.rounded_background_white)
                    holder.optionText.setTextColor(ContextCompat.getColor(context, R.color.black)) // Chữ đen mặc định
                }
            }
            // Khi ở chế độ feedback, không cho phép người dùng click vào các tùy chọn nữa
            holder.itemView.isClickable = false
            Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, isClickable set to FALSE (feedbackMode).")
        } else {
            // Chế độ bình thường (chưa kiểm tra feedback)
            Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, NOT in feedbackMode. Applying default selector.")

            // Áp dụng selector cho background (để tự động đổi màu khi chọn)
            holder.optionText.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.option_background_selector)
            // Áp dụng color state list cho màu chữ (để tự động đổi màu khi chọn)
            holder.optionText.setTextColor(ContextCompat.getColorStateList(holder.itemView.context, R.color.option_text_color_selector))
            // Kích hoạt click khi không ở chế độ feedback
            holder.itemView.isClickable = true
            Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, isClickable set to TRUE (not feedbackMode).")
        }

        // 2. Cập nhật trạng thái "được chọn" của View (phải luôn được đặt)
        // Điều này giúp selector trong option_background_selector hoạt động đúng
        holder.itemView.isSelected = (position == selectedItemPosition)
        Log.d("OptionAdapterDebug", "onBindViewHolder: Pos=$position, holder.itemView.isSelected=${holder.itemView.isSelected}")

        // 3. Đặt OnClickListener chỉ khi không ở chế độ feedback
        holder.itemView.setOnClickListener {
            Log.d("OptionAdapterDebug", "onClickListener: Clicked Pos=$position, feedbackMode=$feedbackMode, isClickable=${holder.itemView.isClickable}")
            // Chỉ cho phép xử lý click khi KHÔNG ở chế độ feedback
            if (!feedbackMode) {
                // Lưu trữ vị trí của tùy chọn đã chọn trước đó
                val previousSelectedPosition = selectedItemPosition
                // Cập nhật vị trí tùy chọn mới được chọn
                selectedItemPosition = holder.adapterPosition

                // Nếu có một lựa chọn cũ, thông báo cho RecyclerView biết để vẽ lại item đó
                // (để nó mất đi trạng thái 'selected' và màu sắc tương ứng)
                if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                    notifyItemChanged(previousSelectedPosition)
                }
                // Thông báo cho RecyclerView biết item mới được chọn cần được vẽ lại
                // (để nó có trạng thái 'selected' và màu sắc tương ứng)
                notifyItemChanged(selectedItemPosition)

                // Gọi callback để thông báo cho Activity biết tùy chọn nào đã được người dùng click
                onItemClick(option, holder.adapterPosition)
            }
        }
    }

    // Trả về tổng số lượng item trong danh sách tùy chọn
    override fun getItemCount(): Int = options.size

    // Hàm public để chọn một item tại vị trí cụ thể (được gọi từ Activity)
    fun selectItem(position: Int) {
        if (position >= 0 && position < options.size) {
            val previousSelectedPosition = selectedItemPosition
            selectedItemPosition = position // Cập nhật vị trí được chọn
            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition) // Vẽ lại item cũ để bỏ chọn
            }
            notifyItemChanged(selectedItemPosition) // Vẽ lại item mới để chọn
            Log.d("OptionAdapterDebug", "selectItem: Position $position selected. Previous: $previousSelectedPosition")
        }
    }

    // Hàm public để cập nhật adapter vào chế độ feedback và hiển thị màu sắc đúng/sai
    fun updateFeedbackColors(userSelectedId: String?, correctId: String?, isCorrectAnswer: Boolean) {
        Log.d("OptionAdapterDebug", "updateFeedbackColors: userSelectedId=$userSelectedId, correctId=$correctId, isCorrectAnswer=$isCorrectAnswer")
        this.feedbackMode = true // Bật chế độ feedback
        this.correctOptionId = correctId // Lưu ID của đáp án đúng
        this.userSelectedOptionId = userSelectedId // Lưu ID của đáp án người dùng đã chọn
        notifyDataSetChanged() // Thông báo cho adapter vẽ lại TẤT CẢ các item để áp dụng màu feedback mới
        Log.d("OptionAdapterDebug", "updateFeedbackColors: feedbackMode set to TRUE. Notifying data set changed.")
    }

    // Hàm public để xóa lựa chọn và tắt chế độ feedback (ví dụ: khi chuyển câu hỏi mới)
    fun clearSelection() {
        val previousSelectedPosition = selectedItemPosition
        selectedItemPosition = RecyclerView.NO_POSITION // Reset không có item nào được chọn
        if (previousSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelectedPosition) // Vẽ lại item cũ để bỏ chọn (nếu có)
        }
        // Reset feedback mode và các ID liên quan về trạng thái ban đầu
        feedbackMode = false
        correctOptionId = null
        userSelectedOptionId = null
        notifyDataSetChanged() // Thông báo cho adapter vẽ lại TẤT CẢ các item để reset màu sắc và trạng thái click
        Log.d("OptionAdapterDebug", "clearSelection: Cleared selection and feedback. feedbackMode set to FALSE.")
    }
}