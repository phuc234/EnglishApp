//package com.example.englishapp // TODO: Thay thế bằng package thực tế
//
//import android.content.Intent
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import com.example.englishapp.databinding.ActivityTopicBinding // TODO: Import binding cho layout chọn chủ đề
//
//class TopicSelectionActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityTopicBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityTopicBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // Đặt Listener cho các mục Chủ đề
//        // Sử dụng ID của include và truy cập View gốc (.root)
//        binding.itemLuyenTriNao.root.setOnClickListener {
//            // Khi click, chuyển sang màn hình LevelModuleActivity
//            val intent = Intent(this, LevelModuleActivity::class.java) // TODO: Thay LevelModuleActivity bằng tên Activity bạn tạo
//            // Tùy chọn: Truyền ID chủ đề đã chọn sang màn hình tiếp theo
//            intent.putExtra("SELECTED_TOPIC_ID", "luyentrinao")
//            startActivity(intent)
//        }
//
//        binding.itemSuNghiep.root.setOnClickListener {
//            val intent = Intent(this, LevelModuleActivity::class.java)
//            intent.putExtra("SELECTED_TOPIC_ID", "sunghiep")
//            startActivity(intent)
//        }
//
//        binding.itemHocDuong.root.setOnClickListener {
//            val intent = Intent(this, LevelModuleActivity::class.java)
//            intent.putExtra("SELECTED_TOPIC_ID", "hocduong")
//            startActivity(intent)
//        }
//
//        binding.itemDuLich.root.setOnClickListener {
//            val intent = Intent(this, LevelModuleActivity::class.java)
//            intent.putExtra("SELECTED_TOPIC_ID", "dulich")
//            startActivity(intent)
//        }
//
//        // TODO: Thêm Listener cho các chủ đề khác nếu có trong layout
//    }
//}