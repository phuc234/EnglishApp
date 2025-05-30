package com.example.englishapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.englishapp.Listening.ListeningQuestionActivity
import com.example.englishapp.LoginActivity // Sửa lại thành Auth.LoginActivity nếu cần
import com.example.englishapp.Question.FillBlankQuestionActivity
import com.example.englishapp.VocabularyScreen.VocabularyQuestionActivity
import com.example.englishapp.data.ExerciseModule
import com.example.englishapp.data.LearningLevel
import com.example.englishapp.databinding.ActivityHomeBinding
import com.example.englishapp.repository.LevelsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var levelsRepository: LevelsRepository

    private val REQUEST_CODE_EXERCISE = 101
    private var allLevels: List<LearningLevel>? = null
    private var currentUserLevelId: String = "level_1"
    private var currentUserTotalScore: Long = 0 // Thêm biến này để lưu điểm
    private var currentLevelNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        levelsRepository = LevelsRepository(this)

        setupBottomNavigationView()

        allLevels = levelsRepository.getAllLevels()
        if (allLevels == null || allLevels!!.isEmpty()) { // Thêm kiểm tra isEmpty
            Toast.makeText(this, "Không thể tải dữ liệu cấp độ hoặc dữ liệu rỗng.", Toast.LENGTH_LONG).show()
            Log.e("HomeActivity", "Không thể tải dữ liệu cấp độ từ LevelsRepository hoặc danh sách rỗng.")
            // Bạn có thể cần xử lý lỗi nghiêm trọng ở đây, ví dụ: tắt ứng dụng hoặc hiển thị thông báo lỗi rõ ràng hơn
        } else {
            Log.d("HomeActivity", "Đã tải ${allLevels!!.size} cấp độ.")
        }
    }

    override fun onResume() {
        super.onResume()
        checkUserAndDisplayLevel()
    }

    private fun checkUserAndDisplayLevel() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                currentUserLevelId = document.getString("currentLevelId") ?: "level_1"
                currentUserTotalScore = document.getLong("totalScore") ?: 0L // Lấy điểm người dùng
                displayCurrentLevelAndModules()
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "Lỗi khi tải dữ liệu người dùng: ${e.message}", e)
                Toast.makeText(this, "Lỗi khi tải dữ liệu người dùng.", Toast.LENGTH_SHORT).show()
                displayCurrentLevelAndModules()
            }
    }

    private fun displayCurrentLevelAndModules() {
        val currentLevel = allLevels?.find { it.id == currentUserLevelId }

        if (currentLevel != null) {
            currentLevelNumber = currentLevel.id.replace("level_", "").toIntOrNull() ?: 1
            binding.levelNumber.text = currentLevelNumber.toString()
            Log.d("HomeActivity", "Đang hiển thị Level: ${currentLevel.name} (${currentLevel.id})")

            binding.modulesContainer.removeAllViews()

            val moduleChunks = currentLevel.modules.chunked(2)

            moduleChunks.forEach { chunk ->
                val horizontalLayout = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        gravity = android.view.Gravity.CENTER_HORIZONTAL
                        if (chunk != moduleChunks.last()) {
                            bottomMargin = resources.getDimensionPixelSize(R.dimen.module_row_margin_bottom)
                        }
                    }
                    orientation = LinearLayout.HORIZONTAL
                    weightSum = 2.0f
                    gravity = android.view.Gravity.CENTER
                }

                chunk.forEachIndexed { innerIndex, module ->
                    val moduleItem = LayoutInflater.from(this).inflate(R.layout.item_exercise_module, horizontalLayout, false)
                    val moduleImageView: ImageView = moduleItem.findViewById(R.id.moduleIcon)
                    val moduleNameTextView: TextView = moduleItem.findViewById(R.id.moduleName)

                    moduleNameTextView.text = module.name

                    val imageResId = resources.getIdentifier(module.imageName, "drawable", packageName)
                    if (imageResId != 0) {
                        moduleImageView.setImageResource(imageResId)
                    } else {
                        moduleImageView.setImageResource(R.drawable.ic_default_module_icon)
                        Log.w("HomeActivity", "Không tìm thấy icon cho module: ${module.imageName}")
                    }

                    moduleItem.setOnClickListener {
                        navigateToExerciseActivity(module.type, module.exercisesFile)
                    }

                    val itemLayoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        weight = 1.0f
                        if (innerIndex == 0) {
                            rightMargin = resources.getDimensionPixelSize(R.dimen.module_item_spacing)
                        } else {
                            leftMargin = resources.getDimensionPixelSize(R.dimen.module_item_spacing)
                        }
                    }
                    moduleItem.layoutParams = itemLayoutParams
                    horizontalLayout.addView(moduleItem)
                }
                binding.modulesContainer.addView(horizontalLayout)
            }

        } else {
            Log.e("HomeActivity", "Không tìm thấy thông tin cho cấp độ: $currentUserLevelId. Hiển thị mặc định Level 1.")
            binding.levelNumber.text = "1"
            binding.modulesContainer.removeAllViews()
            allLevels?.find { it.id == "level_1" }?.let { level1 ->
                currentLevelNumber = 1
                displayModules(level1.modules, binding.modulesContainer) // Gọi lại để hiển thị modules của level 1
            }
            Toast.makeText(this, "Không tìm thấy thông tin cấp độ hiện tại, hiển thị Level 1.", Toast.LENGTH_LONG).show()
        }
    }

    // Hàm này sẽ không được gọi trực tiếp nữa vì logic đã ở displayCurrentLevelAndModules
    private fun displayModules(modules: List<ExerciseModule>, container: LinearLayout) {
        // Có thể để trống hoặc xóa nếu không còn được sử dụng trực tiếp.
        // Logic hiển thị chính đã được đưa vào displayCurrentLevelAndModules
    }


    private fun navigateToExerciseActivity(moduleType: String, exercisesFile: String) {
        val intent: Intent
        when (moduleType) {
            "vocabulary" -> intent = Intent(this, VocabularyQuestionActivity::class.java)
            "question" -> intent = Intent(this, FillBlankQuestionActivity::class.java)
            "listening" -> intent = Intent(this, ListeningQuestionActivity::class.java)
            // Thêm các loại bài tập khác nếu có (ví dụ: "grammar", "writing", "reading")
            else -> {
                Toast.makeText(this, "Loại bài tập không hợp lệ: $moduleType", Toast.LENGTH_SHORT).show()
                return
            }
        }
        intent.putExtra("exercises_file", exercisesFile)
        startActivityForResult(intent, REQUEST_CODE_EXERCISE) // Định nghĩa REQUEST_CODE_EXERCISE
    }

    // --- Bắt đầu phần thêm mới cho logic chuyển level ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EXERCISE && resultCode == RESULT_OK) {
            val scoreEarned = data?.getLongExtra("score_earned", 0L) ?: 0L
            if (scoreEarned > 0) {
                updateUserScoreAndCheckLevel(scoreEarned)
            }
        }
    }
    // Hàm này được gọi từ các Activity bài tập khi người dùng hoàn thành một bài và nhận điểm
    // Sẽ được gọi thông qua Broadcast/Interface hoặc bằng cách truyền callback
    // Trong HomeActivity.kt, hàm updateUserScoreAndCheckLevel
    fun updateUserScoreAndCheckLevel(scoreToAdd: Long) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        Log.d("UpdateLevel", "Calling updateUserScoreAndCheckLevel with scoreToAdd: $scoreToAdd")

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val currentScore = document.getLong("totalScore") ?: 0L
                val currentLevelFirestoreId = document.getString("currentLevelId") ?: "level_1"
                val newTotalScore = currentScore + scoreToAdd

                Log.d("UpdateLevel", "Current Score (Firestore): $currentScore, Score Added: $scoreToAdd, New Total Score (calculated): $newTotalScore")
                Log.d("UpdateLevel", "Current Level ID (Firestore): $currentLevelFirestoreId")

                var nextLevelId = currentLevelFirestoreId // Khởi tạo với level hiện tại

                val currentLevel = allLevels?.find { it.id == currentLevelFirestoreId }

                if (currentLevel != null) {
                    val nextLevel = levelsRepository.getNextLevel(currentLevel.id, allLevels) // Lấy level tiếp theo

                    if (nextLevel != null) {
                        Log.d("UpdateLevel", "Next Level available: ${nextLevel.name} (${nextLevel.id}), Required Score for next: ${nextLevel.requiredScoreToUnlockNext}")
                        // ĐIỂM KIỂM TRA LÊN CẤP BẰNG nextLevel.requiredScoreToUnlockNext
                        // So sánh newTotalScore với requiredScoreToUnlockNext của nextLevel
                        if (newTotalScore >= nextLevel.requiredScoreToUnlockNext) { // <-- Dòng quan trọng để kiểm tra
                            nextLevelId = nextLevel.id // Cập nhật ID level mới
                            Toast.makeText(this, "Chúc mừng! Bạn đã lên cấp độ ${nextLevel.name}!", Toast.LENGTH_LONG).show()
                            Log.d("LevelUp", "Người dùng $userId ĐÃ LÊN CẤP ĐỘ ${nextLevel.name} (${nextLevel.id})")
                        } else {
                            Log.d("LevelUp", "Người dùng $userId vẫn ở cấp độ ${currentLevel.name}. Điểm: $newTotalScore (Chưa đủ ${nextLevel.requiredScoreToUnlockNext})")
                        }
                    } else {
                        Log.d("LevelUp", "Không có cấp độ tiếp theo cho ID: ${currentLevel.id}")
                    }
                } else {
                    Log.w("LevelUp", "Không tìm thấy thông tin level cho ID: $currentLevelFirestoreId trong allLevels.")
                }

                val updates = hashMapOf(
                    "totalScore" to newTotalScore,
                    "currentLevelId" to nextLevelId // Cập nhật level ID vào Firestore
                ) as Map<String, Any>

                db.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener {
                        Log.d("FirestoreUpdate", "Điểm ($newTotalScore) và cấp độ ($nextLevelId) ĐÃ CẬP NHẬT THÀNH CÔNG vào Firestore.")
                        checkUserAndDisplayLevel() // Kích hoạt làm mới UI
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreUpdate", "LỖI CẬP NHẬT điểm/cấp độ vào Firestore: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "LỖI KHI LẤY DỮ LIỆU NGƯỜI DÙNG để cập nhật điểm: ${e.message}", e)
            }
    }
    // --- Kết thúc phần thêm mới ---

    private fun setupBottomNavigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    true
                }
                R.id.navigation_trainslate -> {
                    startActivity(Intent(this, TranslateActivity::class.java))
                    // finish() // Có thể bỏ finish() nếu bạn muốn giữ HomeActivity trên stack
                    true
                }
                R.id.navigation_rank -> {
                    startActivity(Intent(this, RankingActivity::class.java))
                    // finish()
                    true
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java)) // Nên là ProfileActivity
                    // finish()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigationView.menu.findItem(R.id.navigation_home)?.isChecked = true
    }
}