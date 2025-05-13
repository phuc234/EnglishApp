package com.example.englishapp.VocabularyScreen

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.englishapp.R
import com.example.englishapp.databinding.ActivityVocabularyQuestionBinding

class VocabularyQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVocabularyQuestionBinding
    private lateinit var optionsAdapter: VocabularyOptionAdapter

    // TODO: Đây là dữ liệu mẫu. Trong thực tế, bạn sẽ tải dữ liệu câu hỏi từ nguồn khác.
    private val vocabularyOptions: MutableList<VocabularyOption> = mutableListOf(
        VocabularyOption("strawberry", R.drawable.strawberry, "Strawberry"),
        VocabularyOption("orange", R.drawable.orange, "Orange"),
        VocabularyOption("banana", R.drawable.banana, "Banana"),
        VocabularyOption("apple", R.drawable.apple, "Apple")
    )
    // TODO: Xác định đáp án đúng cho câu hỏi này (dựa vào ID)
    private val correctAnswerId = "strawberry"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo binding và gán layout
        binding = ActivityVocabularyQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Cấu hình Toolbar ---
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Hiển thị nút Back
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Xử lý sự kiện khi bấm nút Back
        }

        // --- Cấu hình RecyclerView ---
        setupRecyclerView()

        // --- Cấu hình Listener cho nút KIỂM TRA ---
        binding.checkButton.setOnClickListener {
            handleCheckButtonClick()
        }

        // TODO: Hiển thị câu hỏi thực tế (nếu nó thay đổi)
        // binding.questionText.text = "Đâu là \"quả dâu\" ?"
    }

    private fun setupRecyclerView() {
        // Khởi tạo Adapter, truyền dữ liệu và lambda xử lý click
        optionsAdapter = VocabularyOptionAdapter(vocabularyOptions) { option, position ->
            // Xử lý khi một item trong RecyclerView được click
            handleOptionClick(option, position)
        }

        // Thiết lập LayoutManager (GridLayoutManager cho 2 cột)
        val layoutManager = GridLayoutManager(this, 2) // 2 cột

        // Gán LayoutManager và Adapter cho RecyclerView
        binding.optionsRecyclerView.layoutManager = layoutManager
        binding.optionsRecyclerView.adapter = optionsAdapter

        // TODO: Có thể thêm ItemDecoration để tùy chỉnh khoảng cách giữa các item nếu margin trong item_vocabulary_option.xml chưa đủ
        // binding.optionsRecyclerView.addItemDecoration(...)
    }


    // --- Xử lý khi click vào một tùy chọn đáp án ---
    private fun handleOptionClick(option: VocabularyOption, position: Int) {
        // Logic: Chọn/bỏ chọn item trong Adapter
        optionsAdapter.selectItem(position)

        // TODO: Cập nhật UI khác nếu cần (ví dụ: enable/disable nút KIỂM TRA)
    }

    // --- Xử lý khi click vào nút KIỂM TRA ---
    private fun handleCheckButtonClick() {
        val selectedOption = optionsAdapter.getSelectedItem() // Lấy item đang được chọn từ Adapter

        if (selectedOption == null) {
            Toast.makeText(this, "Vui lòng chọn một đáp án.", Toast.LENGTH_SHORT).show()
            return
        }

        // So sánh ID của tùy chọn được chọn với ID đáp án đúng
        if (selectedOption.id == correctAnswerId) {
            Toast.makeText(this, "Chính xác! Đáp án đúng là ${selectedOption.text}", Toast.LENGTH_SHORT).show()
            // TODO: Logic khi trả lời đúng (ví dụ: chuyển câu hỏi, cộng điểm)
        } else {
            val correctOptionText = vocabularyOptions.find { it.id == correctAnswerId }?.text ?: "Đáp án đúng"
            Toast.makeText(this, "Sai rồi. Đáp án đúng là: ${correctOptionText}", Toast.LENGTH_SHORT).show()
            // TODO: Logic khi trả lời sai (ví dụ: hiển thị đáp án đúng, cho thử lại)
        }
    }
}