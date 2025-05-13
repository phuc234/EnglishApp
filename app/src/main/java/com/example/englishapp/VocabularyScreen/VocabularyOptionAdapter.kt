package com.example.englishapp.VocabularyScreen

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.databinding.ItemVocabularyOptionBinding

class VocabularyOptionAdapter(
    private val options: List<VocabularyOption>, // Danh sách dữ liệu
    private val onItemClick: (VocabularyOption, Int) -> Unit // Lambda xử lý click, truyền cả item và vị trí
) : RecyclerView.Adapter<VocabularyOptionAdapter.ViewHolder>() {

    // ViewHolder: Giữ các tham chiếu đến View của một item
    class ViewHolder(val binding: ItemVocabularyOptionBinding) : RecyclerView.ViewHolder(binding.root)

    // Tạo ViewHolder mới khi cần
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout item bằng View Binding
        val binding = ItemVocabularyOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Gán dữ liệu vào View của ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]

        // Gán dữ liệu vào các View
        holder.binding.optionImage.setImageResource(option.imageResId)
        holder.binding.optionText.text = option.text

        // Thiết lập trạng thái UI dựa trên isSelected
        if (option.isSelected) {
            holder.binding.root.strokeColor = Color.parseColor("#70E000") // Màu viền xanh lá khi chọn
            holder.binding.root.setCardBackgroundColor(Color.parseColor("#E8F5E9")) // Sử dụng phương thức setter
        } else {
            holder.binding.root.strokeColor = Color.TRANSPARENT // Viền trong suốt khi không chọn
            holder.binding.root.setCardBackgroundColor(Color.WHITE) // Sử dụng phương thức setter
        }


        // Thiết lập Listener cho sự kiện click
        holder.itemView.setOnClickListener {
            onItemClick(option, position) // Gọi lambda khi click, truyền item và vị trí
        }
    }

    // Trả về tổng số item trong danh sách
    override fun getItemCount(): Int {
        return options.size
    }

    // Hàm cập nhật một item cụ thể (hữu ích khi đổi trạng thái isSelected)
    fun updateItem(position: Int, updatedOption: VocabularyOption) {
        // Trong thực tế, bạn nên cập nhật danh sách dữ liệu gốc
        // và sau đó thông báo cho adapter. Vì List là immutable,
        // bạn có thể cần sử dụng MutableList hoặc các thư viện diffing.
        // Với ví dụ đơn giản này, chúng ta giả định có cách cập nhật dữ liệu.
        // Sau khi cập nhật dữ liệu, gọi notifyItemChanged:
        // notifyItemChanged(position)
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
    fun getSelectedItem(): VocabularyOption? {
        return options.find { it.isSelected }
    }
}