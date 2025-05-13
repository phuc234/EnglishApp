package com.example.englishapp.adapter // TODO: Thay thế bằng package thực tế của bạn

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.englishapp.data.Topic // TODO: Import Data Class Topic
import com.example.englishapp.databinding.ActivityTopicBinding // TODO: Import lớp binding cho layout item

class TopicAdapter(
    private val topics: List<Topic>, // Danh sách dữ liệu chủ đề
    private val onItemClick: (Topic) -> Unit // Lambda xử lý sự kiện click, truyền đối tượng Topic
) : RecyclerView.Adapter<TopicAdapter.ViewHolder>() {

    // ViewHolder: Giữ các tham chiếu đến View của một item
    class ViewHolder(val binding: ActivityTopicBinding) : RecyclerView.ViewHolder(binding.root)

    // Tạo ViewHolder mới khi cần
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate layout item bằng View Binding
        val binding = ActivityTopicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Gán dữ liệu vào View của ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val topic = topics[position]

        // Gán dữ liệu vào các View trong item
        holder.binding.topicIcon.setImageResource(topic.iconResId)
        holder.binding.topicName.text = topic.name

        // Thiết lập Listener cho sự kiện click trên View gốc của item (MaterialCardView)
        holder.itemView.setOnClickListener {
            onItemClick(topic) // Gọi lambda khi click, truyền đối tượng Topic
        }
    }

    // Trả về tổng số item trong danh sách
    override fun getItemCount(): Int {
        return topics.size
    }
}
