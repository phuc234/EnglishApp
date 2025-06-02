package com.example.englishapp // Khai báo gói (package) của ứng dụng

import android.content.Intent // Import lớp Intent để khởi tạo các hoạt động và truyền dữ liệu
import android.os.Bundle // Import lớp Bundle để lưu và khôi phục trạng thái của hoạt động
import android.util.Log // Import lớp Log để ghi nhật ký (log) thông báo vào Logcat
import android.widget.Toast // Import lớp Toast để hiển thị các thông báo ngắn gọn trên màn hình
import androidx.appcompat.app.AppCompatActivity // Import AppCompatActivity, lớp cơ sở cho các Activity có hỗ trợ các tính năng tương thích ngược
import androidx.recyclerview.widget.LinearLayoutManager // Import LinearLayoutManager để sắp xếp các mục trong RecyclerView theo dạng danh sách tuyến tính
import com.example.englishapp.adapter.RankingAdapter // Import RankingAdapter, adapter tùy chỉnh để hiển thị dữ liệu bảng xếp hạng
import com.example.englishapp.data.UserRank // Import UserRank, lớp dữ liệu (data class) biểu diễn thông tin của một người dùng trên bảng xếp hạng
import com.example.englishapp.databinding.ActivityRankingBinding // Import lớp binding được tạo tự động từ layout ActivityRankingBinding.xml, giúp truy cập các View một cách an toàn và dễ dàng
import com.google.firebase.auth.FirebaseAuth // Import FirebaseAuth để quản lý xác thực người dùng Firebase
import com.google.firebase.firestore.FirebaseFirestore // Import FirebaseFirestore để tương tác với cơ sở dữ liệu Firestore

class RankingActivity : AppCompatActivity() { // Khai báo lớp RankingActivity, kế thừa từ AppCompatActivity

    private lateinit var binding: ActivityRankingBinding // Khai báo biến lateinit cho view binding, sẽ được khởi tạo trong onCreate
    private lateinit var auth: FirebaseAuth // Khai báo biến lateinit cho đối tượng FirebaseAuth
    private lateinit var db: FirebaseFirestore // Khai báo biến lateinit cho đối tượng FirebaseFirestore
    private lateinit var rankingAdapter: RankingAdapter // Khai báo biến lateinit cho RankingAdapter
    private val userRankList = mutableListOf<UserRank>() // Khai báo một danh sách (có thể thay đổi) để lưu trữ dữ liệu người dùng cho bảng xếp hạng

    override fun onCreate(savedInstanceState: Bundle?) { // Phương thức này được gọi khi Activity được tạo lần đầu tiên
        super.onCreate(savedInstanceState) // Gọi phương thức onCreate của lớp cha

        binding = ActivityRankingBinding.inflate(layoutInflater) // Khởi tạo đối tượng binding bằng cách "phồng" (inflate) layout
        setContentView(binding.root) // Đặt layout gốc của binding làm nội dung hiển thị cho Activity

        auth = FirebaseAuth.getInstance() // Lấy một thể hiện (instance) của FirebaseAuth
        db = FirebaseFirestore.getInstance() // Lấy một thể hiện (instance) của FirebaseFirestore

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (auth.currentUser == null) { // Nếu không có người dùng nào đang đăng nhập
            Toast.makeText(this, "Bạn cần đăng nhập để xem bảng xếp hạng.", Toast.LENGTH_SHORT).show() // Hiển thị thông báo yêu cầu đăng nhập
            startActivity(Intent(this, LoginActivity::class.java)) // Chuyển hướng đến LoginActivity
            finish() // Kết thúc RankingActivity hiện tại
            return // Thoát khỏi phương thức onCreate
        }

        // --- Cấu hình Toolbar ---
        setSupportActionBar(binding.toolbar) // Đặt Toolbar tùy chỉnh làm ActionBar của Activity
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút "Back" (mũi tên quay lại) trên Toolbar
        binding.toolbar.setNavigationOnClickListener { // Đặt trình nghe sự kiện click cho nút điều hướng (nút Back) trên Toolbar
            onBackPressedDispatcher.onBackPressed() // Khi nút Back được bấm, gọi hành động quay lại mặc định của hệ thống
        }

        // --- Cấu hình RecyclerView ---
        setupRecyclerView() // Gọi hàm để thiết lập RecyclerView

        // --- Lấy dữ liệu từ Firestore ---
        fetchRankingData() // Gọi hàm để tải dữ liệu bảng xếp hạng từ Firestore

        // Cấu hình Bottom Navigation View (tương tự như ProfileActivity)
        binding.bottomNavigationView.setOnItemSelectedListener { item -> // Đặt trình nghe sự kiện khi một mục trong Bottom Navigation được chọn
            when (item.itemId) { // Kiểm tra ID của mục được chọn
                R.id.navigation_home -> { // Nếu là mục "Trang chủ"
                    startActivity(Intent(this, HomeActivity::class.java)) // Khởi động HomeActivity
                    finish() // Kết thúc RankingActivity
                    true // Trả về true để cho biết sự kiện đã được xử lý
                }
                R.id.navigation_trainslate -> { // Nếu là mục "Dịch"
                    startActivity(Intent(this, TranslateActivity::class.java)) // Khởi động TranslateActivity
                    finish() // Kết thúc RankingActivity
                    true // Trả về true
                }
                R.id.navigation_profile -> { // Nếu là mục "Hồ sơ"
                    startActivity(Intent(this, ProfileActivity::class.java)) // Khởi động ProfileActivity
                    finish() // Kết thúc RankingActivity
                    true // Trả về true
                }
                // Thêm mục cho Ranking nếu bạn muốn có icon riêng
                R.id.navigation_rank -> { // Nếu là mục "Xếp hạng" (giả sử có id này trong menu)
                    // Đang ở màn hình Ranking, không làm gì cả
                    true // Trả về true
                }
                else -> false // Mặc định trả về false
            }
        }
        // Đặt mục Ranking làm mục được chọn khi ở màn hình Ranking
        // Đảm bảo R.id.nav_ranking tồn tại trong bottom_nav_menu.xml
        binding.bottomNavigationView.menu.findItem(R.id.navigation_rank)?.isChecked = true // Đánh dấu mục "Xếp hạng" là đã được chọn trên Bottom Navigation View
    }

    private fun setupRecyclerView() { // Hàm để thiết lập RecyclerView
        rankingAdapter = RankingAdapter(userRankList) // Khởi tạo RankingAdapter với danh sách userRankList
        binding.rankingRecyclerView.layoutManager = LinearLayoutManager(this) // Đặt LinearLayoutManager làm layout manager cho RecyclerView
        binding.rankingRecyclerView.adapter = rankingAdapter // Đặt RankingAdapter làm adapter cho RecyclerView
    }

    private fun fetchRankingData() { // Hàm để lấy dữ liệu bảng xếp hạng từ Firestore
        // Lấy dữ liệu từ collection "users"
        db.collection("users") // Truy cập collection "users" trong Firestore
            // Sắp xếp theo totalScore giảm dần
            .orderBy("totalScore", com.google.firebase.firestore.Query.Direction.DESCENDING) // Sắp xếp tài liệu theo trường "totalScore" theo thứ tự giảm dần
            // Lấy giới hạn (ví dụ: top 50, hoặc không giới hạn nếu muốn tất cả)
            // .limit(50) // Có thể bỏ comment dòng này để giới hạn số lượng kết quả (ví dụ: chỉ lấy top 50)
            .get() // Thực hiện truy vấn để lấy tất cả các tài liệu
            .addOnSuccessListener { result -> // Lắng nghe sự kiện thành công khi truy vấn dữ liệu
                userRankList.clear() // Xóa tất cả dữ liệu cũ trong danh sách trước khi thêm dữ liệu mới
                for (document in result) { // Lặp qua từng tài liệu (document) trong kết quả truy vấn
                    val uid = document.id // Lấy ID của tài liệu (chính là UID của người dùng)
                    val fullName = document.getString("fullName") ?: "Người dùng ẩn danh" // Lấy tên đầy đủ, nếu không có thì đặt là "Người dùng ẩn danh"
                    val totalScore = document.getLong("totalScore") ?: 0L // Lấy tổng điểm, nếu không có thì đặt là 0L
                    val profileImageUrl = document.getString("profileImageUrl") // Lấy URL ảnh hồ sơ nếu có

                    userRankList.add(UserRank(uid, fullName, totalScore, profileImageUrl)) // Thêm một đối tượng UserRank mới vào danh sách
                }
                // Sắp xếp lại danh sách (nếu không dùng orderBy của Firestore hoặc muốn sắp xếp lại sau khi lọc)
                // userRankList.sortByDescending { it.totalScore } // Có thể bỏ comment dòng này để sắp xếp lại theo totalScore nếu cần

                rankingAdapter.notifyDataSetChanged() // Thông báo cho adapter rằng dữ liệu đã thay đổi để RecyclerView cập nhật giao diện
                Log.d("RankingActivity", "Đã tải ${userRankList.size} người dùng vào bảng xếp hạng.") // Ghi nhật ký debug thông báo số lượng người dùng đã tải
            }
            .addOnFailureListener { exception -> // Lắng nghe sự kiện thất bại khi truy vấn dữ liệu
                Log.e("RankingActivity", "Lỗi khi tải dữ liệu bảng xếp hạng: ${exception.message}", exception) // Ghi nhật ký lỗi chi tiết
                Toast.makeText(this, "Lỗi khi tải bảng xếp hạng: ${exception.message}", Toast.LENGTH_LONG).show() // Hiển thị thông báo lỗi trên màn hình
            }
    }

    override fun onSupportNavigateUp(): Boolean { // Phương thức này được gọi khi người dùng nhấn nút Up (nút Back trên Toolbar)
        onBackPressedDispatcher.onBackPressed() // Gọi hành động quay lại mặc định của hệ thống
        return true // Trả về true để cho biết sự kiện đã được xử lý
    }
}