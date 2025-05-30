package com.example.englishapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.R // Đảm bảo import R đúng
import com.example.englishapp.data.VocabularyOption // Import data class VocabularyOption
import com.example.englishapp.databinding.ItemVocabularyOptionBinding // Import lớp binding cho layout item
import com.google.android.material.card.MaterialCardView // Import MaterialCardView

// Adapter cho RecyclerView hiển thị các tùy chọn từ vựng
class VocabularyOptionAdapter(
    private val options: MutableList<VocabularyOption>, // Danh sách các tùy chọn
    private val onItemClick: (VocabularyOption, Int) -> Unit // Lambda xử lý click item
) : RecyclerView.Adapter<VocabularyOptionAdapter.OptionViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION // Lưu vị trí của item được chọn

    // ViewHolder chứa các View của một item
    inner class OptionViewHolder(val binding: ItemVocabularyOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.optionImageView
        val textView: TextView = binding.optionTextView
    }

    // Tạo ViewHolder mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemVocabularyOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionViewHolder(binding)
    }

    // Gán dữ liệu vào ViewHolder
    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = options[position]

        // Lấy ID resource của ảnh từ tên file ảnh
        val imageResId = holder.itemView.context.resources.getIdentifier(
            option.imageFileName, "drawable", holder.itemView.context.packageName
        )

        if (imageResId != 0) {
            holder.imageView.setImageResource(imageResId)
        } else {
            // Xử lý trường hợp không tìm thấy ảnh, ví dụ: hiển thị ảnh placeholder
//            holder.imageView.setImageResource(R.drawable.ic_image_placeholder) // Đảm bảo bạn có một placeholder icon
        }

        holder.textView.text = option.text

        // Xử lý trạng thái được chọn/bỏ chọn
        val cardView = holder.binding.root as MaterialCardView
        if (position == selectedPosition) {
            cardView.setStrokeColor(holder.itemView.context.getColor(R.color.selected_option_stroke_color))
            cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.selected_option_color))
        } else {
            cardView.setStrokeColor(holder.itemView.context.getColor(R.color.unselected_option_stroke_color))
            cardView.setCardBackgroundColor(holder.itemView.context.getColor(R.color.white))
        }

        // Thiết lập Listener cho item
        holder.itemView.setOnClickListener {
            onItemClick(option, position) // Gọi lambda khi click
        }
    }

    // Trả về số lượng item trong danh sách
    override fun getItemCount(): Int {
        return options.size
    }

    // Hàm chọn một item và cập nhật RecyclerView
    fun selectItem(position: Int) {
        val oldSelectedPosition = selectedPosition
        if (selectedPosition != position) {
            selectedPosition = position
            notifyItemChanged(oldSelectedPosition) // Cập nhật trạng thái của item cũ
            notifyItemChanged(selectedPosition) // Cập nhật trạng thái của item mới
        } else {
            // Nếu click lại vào item đã chọn, có thể bỏ chọn nó (tùy logic game)
            // selectedPosition = RecyclerView.NO_POSITION
            // notifyItemChanged(oldSelectedPosition)
        }
    }

    // Hàm trả về item đang được chọn
    fun getSelectedItem(): VocabularyOption? {
        return if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < options.size) {
            options[selectedPosition]
        } else {
            null
        }
    }

    // Hàm để reset lựa chọn (ví dụ: khi chuyển câu hỏi mới)
    fun clearSelection() {
        val oldSelectedPosition = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        if (oldSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldSelectedPosition)
        }
    }
}
