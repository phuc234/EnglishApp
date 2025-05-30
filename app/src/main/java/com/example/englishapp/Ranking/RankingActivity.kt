package com.example.englishapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.englishapp.adapter.RankingAdapter
import com.example.englishapp.data.UserRank
import com.example.englishapp.databinding.ActivityRankingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.englishapp.LoginActivity // Để điều hướng nếu chưa đăng nhập

class RankingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRankingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rankingAdapter: RankingAdapter
    private val userRankList = mutableListOf<UserRank>() // Danh sách người dùng để hiển thị

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRankingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (auth.currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem bảng xếp hạng.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // --- Cấu hình Toolbar ---
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút Back
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Xử lý sự kiện khi bấm nút Back
        }

        // --- Cấu hình RecyclerView ---
        setupRecyclerView()

        // --- Lấy dữ liệu từ Firestore ---
        fetchRankingData()

        // Cấu hình Bottom Navigation View (tương tự như ProfileActivity)
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_trainslate -> {
                    startActivity(Intent(this, TranslateActivity::class.java))
                    finish()
                    true
                }

                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                // Thêm mục cho Ranking nếu bạn muốn có icon riêng
                R.id.navigation_rank -> { // Giả sử bạn có một item menu với id nav_ranking
                    // Đang ở màn hình Ranking, không làm gì
                    true
                }
                else -> false
            }
        }
        // Đặt mục Ranking làm mục được chọn khi ở màn hình Ranking
        // Đảm bảo R.id.nav_ranking tồn tại trong bottom_nav_menu.xml
        binding.bottomNavigationView.menu.findItem(R.id.navigation_rank)?.isChecked = true
    }

    private fun setupRecyclerView() {
        rankingAdapter = RankingAdapter(userRankList)
        binding.rankingRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.rankingRecyclerView.adapter = rankingAdapter
    }

    private fun fetchRankingData() {
        // Lấy dữ liệu từ collection "users"
        db.collection("users")
            // Sắp xếp theo totalScore giảm dần
            .orderBy("totalScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
            // Lấy giới hạn (ví dụ: top 50, hoặc không giới hạn nếu muốn tất cả)
            // .limit(50)
            .get()
            .addOnSuccessListener { result ->
                userRankList.clear() // Xóa dữ liệu cũ
                for (document in result) {
                    val uid = document.id
                    val fullName = document.getString("fullName") ?: "Người dùng ẩn danh"
                    val totalScore = document.getLong("totalScore") ?: 0L
                    val profileImageUrl = document.getString("profileImageUrl") // Lấy URL ảnh nếu có

                    userRankList.add(UserRank(uid, fullName, totalScore, profileImageUrl))
                }
                // Sắp xếp lại danh sách (nếu không dùng orderBy của Firestore hoặc muốn sắp xếp lại sau khi lọc)
                // userRankList.sortByDescending { it.totalScore }

                rankingAdapter.notifyDataSetChanged() // Cập nhật RecyclerView

                Log.d("RankingActivity", "Đã tải ${userRankList.size} người dùng vào bảng xếp hạng.")
            }
            .addOnFailureListener { exception ->
                Log.e("RankingActivity", "Lỗi khi tải dữ liệu bảng xếp hạng: ${exception.message}", exception)
                Toast.makeText(this, "Lỗi khi tải bảng xếp hạng: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}