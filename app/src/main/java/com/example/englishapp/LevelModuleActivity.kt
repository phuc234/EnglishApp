package com.example.englishapp // TODO: Thay thế bằng package thực tế

import android.content.Intent
import android.os.Bundle
import android.widget.Toast // Import Toast nếu cần
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.databinding.ActivityHomescreenBinding // TODO: Import binding cho layout cấp độ/module
import com.example.englishapp.VocabularyScreen.VocabularyQuestionActivity // TODO: Import Activity đích

class LevelModuleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomescreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomescreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tùy chọn: Nhận ID chủ đề từ màn hình trước nếu bạn đã truyền nó
        // val selectedTopicId = intent.getStringExtra("SELECTED_TOPIC_ID")
        // TODO: Dựa vào selectedTopicId để hiển thị các module và level phù hợp

        // Đặt Listener cho mục Module "Từ vựng"
        binding.itemTuVung.root.setOnClickListener {
            // Khi click, chuyển sang màn hình VocabularyQuestionActivity
            val intent = Intent(this, VocabularyQuestionActivity::class.java) // TODO: Thay VocabularyQuestionActivity bằng tên Activity bạn tạo
            // Tùy chọn: Truyền thông tin về module hoặc level nếu cần cho màn hình câu hỏi
            intent.putExtra("SELECTED_MODULE_ID", "tuvung")
            // intent.putExtra("CURRENT_LEVEL", binding.levelNumber.text.toString().toIntOrNull() ?: 1) // Ví dụ lấy level hiện tại

            startActivity(intent)
        }

        // TODO: Thêm Listener cho các module khác (Viết, Nghe) nếu bạn muốn chúng chuyển sang màn hình khác hoặc xử lý sự kiện
        // binding.itemViet.root.setOnClickListener { Toast.makeText(this, "Clicked Viết", Toast.LENGTH_SHORT).show() }
        // binding.itemnghe.root.setOnClickListener { Toast.makeText(this, "Clicked Nghe", Toast.LENGTH_SHORT).show() }

        // TODO: Xử lý sự kiện cho Bottom Navigation nếu có listener trong Activity này
        // binding.bottomNavigationView.setOnItemSelectedListener { item -> ... }
    }
}