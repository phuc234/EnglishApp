package com.example.englishapp.adapter // Khai báo tên gói (package) của tệp Kotlin này

import android.view.LayoutInflater // Import lớp LayoutInflater để chuyển đổi các tệp bố cục XML thành các đối tượng View
import android.view.ViewGroup // Import lớp ViewGroup, một View chứa các View khác
import androidx.recyclerview.widget.RecyclerView // Import RecyclerView, một thành phần UI mạnh mẽ để hiển thị danh sách lớn dữ liệu
import com.bumptech.glide.Glide // Import Glide, một thư viện phổ biến để tải và hiển thị hình ảnh một cách hiệu quả
import com.example.englishapp.R // Import lớp R, chứa các ID tài nguyên của ứng dụng (ví dụ: drawable, color)
import com.example.englishapp.data.UserRank // Import data class UserRank, định nghĩa cấu trúc dữ liệu cho một người dùng trong bảng xếp hạng
import com.example.englishapp.databinding.ItemRankBinding // Import lớp binding được tạo tự động từ layout item_rank.xml, giúp truy cập các View an toàn hơn
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth để tương tác với hệ thống xác thực người dùng của Firebase

class RankingAdapter(private val userList: List<UserRank>) : // Constructor nhận vào một danh sách các đối tượng UserRank
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() { // Chỉ định loại ViewHolder mà Adapter này sẽ sử dụng

    // Lấy UID của người dùng hiện tại từ FirebaseAuth. Biến này được khởi tạo một lần khi Adapter được tạo.
    private val currentUserUid: String? = FirebaseAuth.getInstance().currentUser?.uid // Lấy UID của người dùng đang đăng nhập (có thể là null nếu chưa đăng nhập)

    // Lớp RankingViewHolder đại diện cho một khung nhìn của một mục (item) trong RecyclerView.
    // Nó giữ các tham chiếu đến các View con trong layout item_rank.xml.
    inner class RankingViewHolder(val binding: ItemRankBinding) : RecyclerView.ViewHolder(binding.root) {
        // Hàm 'bind' dùng để gán dữ liệu từ đối tượng UserRank vào các View trong ViewHolder
        fun bind(userRank: UserRank, position: Int) {
            binding.rankNumber.text = (position + 1).toString() // Hiển thị số thứ tự (hạng) của người dùng. 'position' là chỉ mục dựa trên 0, nên cộng 1 để bắt đầu từ 1.
            binding.userName.text = userRank.fullName // Đặt tên người dùng vào TextView
            binding.userScore.text = userRank.totalScore.toString() // Đặt tổng điểm của người dùng vào TextView

            // Tải ảnh đại diện bằng Glide
            if (!userRank.profileImageUrl.isNullOrEmpty()) { // Kiểm tra nếu URL ảnh hồ sơ không rỗng hoặc null
                Glide.with(binding.userAvatar.context) // Bắt đầu tải ảnh với ngữ cảnh của View userAvatar
                    .load(userRank.profileImageUrl) // Tải ảnh từ URL đã cho
                    .placeholder(R.drawable.logo) // Hiển thị ảnh logo khi đang tải ảnh
                    .error(R.drawable.logo) // Hiển thị ảnh logo nếu có lỗi khi tải ảnh
                    .into(binding.userAvatar) // Đặt ảnh đã tải vào ImageView userAvatar
            } else {
                binding.userAvatar.setImageResource(R.drawable.logo) // Nếu không có URL ảnh, đặt ảnh logo mặc định
            }

            // Đánh dấu item của người dùng hiện tại
            // 'isActivated' là một trạng thái View có thể dùng để thay đổi giao diện (ví dụ: màu nền)
            binding.rankItemLayout.isActivated = userRank.uid == currentUserUid // Đặt trạng thái kích hoạt cho layout item nếu UID của người dùng khớp với UID của người dùng hiện tại

            // Thay đổi màu số hạng cho top 3
            when (position) { // Sử dụng biểu thức 'when' để kiểm tra vị trí của item
                0 -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.gold_rank)) // Nếu là hạng 1 (position 0), đặt màu vàng kim
                1 -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.silver_rank)) // Nếu là hạng 2 (position 1), đặt màu bạc
                2 -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.bronze_rank)) // Nếu là hạng 3 (position 2), đặt màu đồng
                else -> binding.rankNumber.setTextColor(binding.root.context.getColor(R.color.black)) // Đối với các hạng khác, đặt màu đen
            }
        }
    }

    // Phương thức này được gọi khi RecyclerView cần tạo một ViewHolder mới.
    // Nó khởi tạo layout cho một item và trả về một ViewHolder mới.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        // Sử dụng LayoutInflater để "phồng" (inflate) layout item_rank.xml thành một đối tượng View
        val binding = ItemRankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RankingViewHolder(binding) // Trả về một thể hiện (instance) mới của RankingViewHolder với binding đã tạo
    }

    // Phương thức này được gọi bởi RecyclerView để hiển thị dữ liệu tại một vị trí cụ thể.
    // Nó cập nhật nội dung của ViewHolder để phản ánh item tại vị trí đã cho.
    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val userRank = userList[position] // Lấy đối tượng UserRank từ danh sách tại vị trí hiện tại
        holder.bind(userRank, position) // Gọi hàm bind của ViewHolder để gán dữ liệu và thiết lập giao diện
    }

    // Phương thức này trả về tổng số item trong tập dữ liệu.
    override fun getItemCount(): Int = userList.size // Trả về kích thước của danh sách người dùng, tức là số lượng item sẽ hiển thị
}