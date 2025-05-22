package com.example.englishapp.adapter // TODO: Thay thế bằng package thực tế của bạn

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.data.OptionItem // TODO: Import Data Class OptionItem
import com.example.englishapp.databinding.ItemListenBinding // TODO: Import lớp binding cho layout item
import com.google.android.material.card.MaterialCardView

class OptionAdapter(
    private val options: MutableList<OptionItem>, // Sử dụng MutableList để dễ dàng cập nhật trạng thái chọn
    private val onItemClick: (OptionItem, Int) -> Unit // Lambda xử lý click, truyền item và vị trí
) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {

    // ViewHolder: Giữ các tham chiếu đến View của một item
    class ViewHolder(val binding: ItemListenBinding) : RecyclerView.ViewHolder(binding.root)

    // Tạo ViewHolder mới khi cần
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout item bằng View Binding
        val binding = ItemListenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Gán dữ liệu vào View của ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]

        // Gán văn bản cho tùy chọn (bao gồm tiền tố A/B/C/D)
        holder.binding.optionText.text = "${option.prefix}. ${option.text}"

        // Thiết lập trạng thái UI dựa trên isSelected
        val context = holder.itemView.context
        val cardView = holder.binding.root as MaterialCardView

        if (option.isSelected) {
            // TODO: Sử dụng màu sắc từ res/values/colors.xml thay vì mã màu cứng
            cardView.setStrokeColor(Color.parseColor("#70E000")) // Màu viền xanh lá khi chọn
            // Sử dụng phương thức setCardBackgroundColor()
            cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Màu nền xanh nhạt khi chọn
        } else {
            cardView.setStrokeColor(Color.TRANSPARENT) // Viền trong suốt khi không chọn
            // Sử dụng phương thức setCardBackgroundColor()
            cardView.setCardBackgroundColor(Color.WHITE) // Nền trắng khi không chọn
        }

        // Thiết lập Listener cho sự kiện click trên View gốc của item (MaterialCardView)
        holder.itemView.setOnClickListener {
            onItemClick(option, position) // Gọi lambda khi click, truyền item và vị trí
        }
    }

    // Trả về tổng số item trong danh sách
    override fun getItemCount(): Int {
        return options.size
    }

    // Hàm đơn giản để tìm và cập nhật trạng thái chọn
    fun selectItem(selectedPosition: Int) {
        // Trước hết, bỏ chọn item cũ (nếu có item nào đang được chọn)
        val currentlySelectedItemIndex = options.indexOfFirst { it.isSelected }
        if (currentlySelectedItemIndex != -1 && currentlySelectedItemIndex != selectedPosition) {
            options[currentlySelectedItemIndex].isSelected = false // Cập nhật dữ liệu
            notifyItemChanged(currentlySelectedItemIndex) // Thông báo Adapter cập nhật UI
        }

        // Chọn item mới (nếu nó chưa được chọn)
        if (currentlySelectedItemIndex != selectedPosition) {
            options[selectedPosition].isSelected = true // Cập nhật dữ liệu
            notifyItemChanged(selectedPosition) // Thông báo Adapter cập nhật UI
        }
        // Nếu click lại vào item đã chọn, có thể bỏ chọn nó (tùy logic game)
        // else {
        //    options[selectedPosition].isSelected = false
        //    notifyItemChanged(selectedPosition)
        // }
    }

    // Hàm lấy item hiện tại đang được chọn
    fun getSelectedItem(): OptionItem? {
        return options.find { it.isSelected }
    }
}
