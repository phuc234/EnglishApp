package com.example.englishapp // TODO: Thay thế bằng package thực tế của bạn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager // Hoặc GridLayoutManager nếu muốn lưới
import com.example.englishapp.adapter.TopicAdapter // TODO: Import Adapter
import com.example.englishapp.data.Topic // TODO: Import Data Class
import com.example.englishapp.databinding.ActivityTopicBinding // TODO: Import lớp binding cho layout Activity

class TopicSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicBinding
    private lateinit var topicAdapter: TopicAdapter

    // TODO: Dữ liệu mẫu cho danh sách chủ đề. Trong thực tế, bạn có thể tải từ nguồn khác.
    private val topicList: List<Topic> = listOf(
        Topic("luyentrinao", "Luyện trí não", R.drawable.brain), // TODO: Thay ID icon thực tế
        Topic("sunghiep", "Sự nghiệp", R.drawable.career),       // TODO: Thay ID icon thực tế
        Topic("hocduong", "Học đường", R.drawable.study),     // TODO: Thay ID icon thực tế
        Topic("dulich", "Du lịch", R.drawable.travel)        // TODO: Thay ID icon thực tế
        // TODO: Thêm các chủ đề khác
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo binding và gán layout
        binding = ActivityTopicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Cấu hình RecyclerView ---
        setupRecyclerView()

        // TODO: Thêm cấu hình cho Bottom Navigation nếu nó nằm trong layout này
        // binding.bottomNavigationView.setOnItemSelectedListener { item -> ... }
    }

    private fun setupRecyclerView() {
        // Khởi tạo Adapter, truyền danh sách dữ liệu và lambda xử lý click
        topicAdapter = TopicAdapter(topicList) { topic ->
            // Lambda này được gọi khi một item chủ đề trong RecyclerView được click
            handleTopicClick(topic) // Gọi hàm xử lý click
        }

        // Thiết lập LayoutManager (LinearLayoutManager cho danh sách dọc)
        binding.topicsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Gán Adapter cho RecyclerView
        binding.topicsRecyclerView.adapter = topicAdapter

        // TODO: Có thể thêm ItemDecoration để tùy chỉnh khoảng cách giữa các item nếu margin trong item_topic.xml chưa đủ
        // binding.topicsRecyclerView.addItemDecoration(...)
    }

    // Hàm xử lý khi một Chủ đề được click
    private fun handleTopicClick(topic: Topic) {
        // TODO: Xử lý logic khi chọn chủ đề.
        val intent = Intent(this, HomeActivity::class.java) // TODO: Thay LevelModuleActivity bằng tên Activity bạn tạo
        // Tùy chọn: Truyền ID hoặc thông tin của chủ đề đã chọn sang màn hình tiếp theo
        intent.putExtra("SELECTED_TOPIC_ID", topic.id)
        intent.putExtra("SELECTED_TOPIC_NAME", topic.name)
        startActivity(intent)

        // Hiển thị thông báo tạm thời để kiểm tra
        // Toast.makeText(this, "Clicked ${topic.name}", Toast.LENGTH_SHORT).show()
    }

    // TODO: Override onSupportNavigateUp() nếu bạn có nút Back trên Toolbar
    // override fun onSupportNavigateUp(): Boolean {
    //     onBackPressedDispatcher.onBackPressed()
    //     return true
    // }
}
