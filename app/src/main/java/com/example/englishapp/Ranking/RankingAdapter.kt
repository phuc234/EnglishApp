package com.example.englishapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Để tải ảnh đại diện
import com.example.englishapp.R
import com.example.englishapp.data.UserRank
import com.example.englishapp.databinding.ItemRankBinding // Import binding cho item_rank.xml
import com.google.firebase.auth.FirebaseAuth // Để kiểm tra người dùng hiện tại

class RankingAdapter(private val userList: List<UserRank>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    private val currentUserUid: String? = FirebaseAuth.getInstance().currentUser?.uid

    inner class RankingViewHolder(val binding: ItemRankBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(userRank: UserRank, position: Int) {
            binding.rankNumber.text = (position + 1).toString() // Hạng bắt đầu từ 1
            binding.userName.text = userRank.fullName
            binding.userScore.text = userRank.totalScore.toString()

            // Tải ảnh đại diện bằng Glide
            if (!userRank.profileImageUrl.isNullOrEmpty()) {
                Glide.with(binding.userAvatar.context)
                    .load(userRank.profileImageUrl)
                    .placeholder(R.drawable.logo) // Ảnh placeholder khi đang tải hoặc lỗi
                    .error(R.drawable.logo) // Ảnh khi có lỗi
                    .into(binding.userAvatar)
            } else {
                binding.userAvatar.setImageResource(R.drawable.logo)
            }

            // Đánh dấu item của người dùng hiện tại
            binding.rankItemLayout.isActivated = userRank.uid == currentUserUid

            // Thay đổi màu số hạng cho top 3
            when (position) {
                0 -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.gold_rank))
                1 -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.silver_rank))
                2 -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.bronze_rank))
                else -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.black))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val binding = ItemRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val userRank = userList[position]
        holder.bind(userRank, position)
    }

    override fun getItemCount(): Int = userList.size
}