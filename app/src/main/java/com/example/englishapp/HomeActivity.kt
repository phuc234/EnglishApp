package com.example.englishapp

import android.content.Intent // Import lớp Intent để khởi chạy các Activity và truyền dữ liệu
import android.os.Bundle // Import lớp Bundle để lưu và khôi phục trạng thái Activity
import android.util.Log // Import lớp Log để ghi nhật ký (log) thông báo debug/error
import android.view.LayoutInflater // Import LayoutInflater để "phồng" (inflate) các layout XML thành đối tượng View
import android.widget.ImageView // Import ImageView để hiển thị hình ảnh
import android.widget.LinearLayout // Import LinearLayout để tạo bố cục tuyến tính
import android.widget.TextView // Import TextView để hiển thị văn bản
import android.widget.Toast // Import Toast để hiển thị thông báo ngắn trên màn hình
import androidx.appcompat.app.AppCompatActivity // Lớp cơ sở cho các Activity có hỗ trợ các tính năng tương thích ngược
import com.example.englishapp.Listening.ListeningQuestionActivity // Activity cho bài nghe
import com.example.englishapp.Question.FillBlankQuestionActivity // Activity cho bài điền từ
import com.example.englishapp.Reading.ReadingQuestionActivity // Activity cho bài đọc hiểu
import com.example.englishapp.VocabularyScreen.VocabularyQuestionActivity // Activity cho bài từ vựng
import com.example.englishapp.data.ExerciseModule // Data class biểu diễn một module bài tập
import com.example.englishapp.data.LearningLevel // Data class biểu diễn một cấp độ học tập
import com.example.englishapp.databinding.ActivityHomeBinding // Lớp binding được tạo tự động cho layout của HomeActivity
import com.example.englishapp.repository.LevelsRepository // Repository để lấy dữ liệu các cấp độ
import com.google.firebase.auth.FirebaseAuth // Để quản lý xác thực người dùng Firebase
import com.google.firebase.firestore.FirebaseFirestore // Để tương tác với cơ sở dữ liệu Firestore

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding // Biến để quản lý view binding
    private lateinit var auth: FirebaseAuth // Biến cho Firebase Authentication
    private lateinit var db: FirebaseFirestore // Biến cho Firebase Firestore
    private lateinit var levelsRepository: LevelsRepository // Biến cho LevelsRepository

    private val REQUEST_CODE_EXERCISE = 101
    private var allLevels: List<LearningLevel>? = null
    private var currentUserLevelId: String = "level_1"
    private var currentUserTotalScore: Long = 0
    private var currentLevelNumber: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Gọi onCreate của lớp cha
        binding = ActivityHomeBinding.inflate(layoutInflater) // Khởi tạo binding từ layout
        setContentView(binding.root) // Đặt layout làm nội dung của Activity

        auth = FirebaseAuth.getInstance() // Lấy thể hiện của FirebaseAuth
        db = FirebaseFirestore.getInstance() // Lấy thể hiện của FirebaseFirestore
        levelsRepository = LevelsRepository(this) // Khởi tạo LevelsRepository

        setupBottomNavigationView() // Thiết lập Bottom Navigation View

        allLevels = levelsRepository.getAllLevels() // Tải tất cả các cấp độ từ repository
        if (allLevels == null || allLevels!!.isEmpty()) { // Kiểm tra nếu dữ liệu cấp độ không tải được hoặc rỗng
            Toast.makeText(this, "Không thể tải dữ liệu cấp độ hoặc dữ liệu rỗng.", Toast.LENGTH_LONG).show() // Thông báo lỗi
            Log.e("HomeActivity", "Không thể tải dữ liệu cấp độ từ LevelsRepository hoặc danh sách rỗng.") // Ghi log lỗi
            // Xử lý lỗi nghiêm trọng ở đây nếu cần, ví dụ: tắt ứng dụng
        } else {
            Log.d("HomeActivity", "Đã tải ${allLevels!!.size} cấp độ.") // Ghi log số lượng cấp độ đã tải
        }
    }

    override fun onResume() { // Phương thức được gọi khi Activity tiếp tục hoạt động hoặc lần đầu hiển thị
        super.onResume() // Gọi onResume của lớp cha
        checkUserAndDisplayLevel() // Kiểm tra người dùng và hiển thị cấp độ
    }

    private fun checkUserAndDisplayLevel() { // Hàm kiểm tra người dùng và hiển thị thông tin cấp độ
        val currentUser = auth.currentUser // Lấy thông tin người dùng hiện tại
        if (currentUser == null) { // Nếu không có người dùng nào đăng nhập
            Toast.makeText(this, "Bạn cần đăng nhập.", Toast.LENGTH_SHORT).show() // Thông báo yêu cầu đăng nhập
            startActivity(Intent(this, LoginActivity::class.java)) // Chuyển đến màn hình đăng nhập
            finish() // Kết thúc HomeActivity
            return // Thoát hàm
        }

        db.collection("users").document(currentUser.uid).get() // Truy vấn dữ liệu người dùng từ Firestore
            .addOnSuccessListener { document -> // Khi truy vấn thành công
                currentUserLevelId = document.getString("currentLevelId") ?: "level_1" // Lấy ID cấp độ hiện tại, mặc định là "level_1"
                currentUserTotalScore = document.getLong("totalScore") ?: 0L // Lấy tổng điểm của người dùng, mặc định là 0
                displayCurrentLevelAndModules() // Hiển thị cấp độ và các module bài tập
            }
            .addOnFailureListener { e -> // Khi truy vấn thất bại
                Log.e("HomeActivity", "Lỗi khi tải dữ liệu người dùng: ${e.message}", e) // Ghi log lỗi
                Toast.makeText(this, "Lỗi khi tải dữ liệu người dùng.", Toast.LENGTH_SHORT).show() // Thông báo lỗi
                displayCurrentLevelAndModules() // Vẫn cố gắng hiển thị cấp độ và module (có thể là mặc định)
            }
    }

    private fun displayCurrentLevelAndModules() { // Hàm hiển thị cấp độ hiện tại và các module bài tập
        val currentLevel = allLevels?.find { it.id == currentUserLevelId } // Tìm cấp độ hiện tại trong danh sách tất cả các cấp độ

        if (currentLevel != null) { // Nếu tìm thấy cấp độ hiện tại
            currentLevelNumber = currentLevel.id.replace("level_", "").toIntOrNull() ?: 1 // Trích xuất số cấp độ từ ID (ví dụ: "level_1" -> 1)
            binding.levelNumber.text = currentLevelNumber.toString() // Hiển thị số cấp độ lên UI
            Log.d("HomeActivity", "Đang hiển thị Level: ${currentLevel.name} (${currentLevel.id})") // Ghi log cấp độ đang hiển thị

            binding.modulesContainer.removeAllViews() // Xóa tất cả các module cũ khỏi container

            val moduleChunks = currentLevel.modules.chunked(2) // Chia danh sách module thành các "khúc" (chunk) 2 module mỗi khúc

            moduleChunks.forEach { chunk -> // Lặp qua từng khúc module
                val horizontalLayout = LinearLayout(this).apply { // Tạo một LinearLayout ngang cho mỗi khúc (chứa 2 module)
                    layoutParams = LinearLayout.LayoutParams( // Thiết lập LayoutParams
                        LinearLayout.LayoutParams.MATCH_PARENT, // Chiều rộng đầy đủ
                        LinearLayout.LayoutParams.WRAP_CONTENT // Chiều cao tự điều chỉnh
                    ).apply {
                        gravity = android.view.Gravity.CENTER_HORIZONTAL // Căn giữa theo chiều ngang
                        if (chunk != moduleChunks.last()) { // Nếu đây không phải là khúc cuối cùng
                            bottomMargin = resources.getDimensionPixelSize(R.dimen.module_row_margin_bottom) // Thêm margin dưới
                        }
                    }
                    orientation = LinearLayout.HORIZONTAL // Đặt hướng là ngang
                    weightSum = 2.0f // Đặt tổng trọng số là 2 (cho 2 module trong hàng)
                    gravity = android.view.Gravity.CENTER // Căn giữa các module trong hàng
                }

                chunk.forEachIndexed { innerIndex, module -> // Lặp qua từng module trong khúc hiện tại
                    // "Phồng" (inflate) layout item_exercise_module cho mỗi module
                    val moduleItem = LayoutInflater.from(this).inflate(R.layout.item_exercise_module, horizontalLayout, false)
                    val moduleImageView: ImageView = moduleItem.findViewById(R.id.moduleIcon) // Lấy ImageView
                    val moduleNameTextView: TextView = moduleItem.findViewById(R.id.moduleName) // Lấy TextView tên module

                    moduleNameTextView.text = module.name // Đặt tên module

                    // Lấy ID tài nguyên hình ảnh dựa trên tên ảnh từ module
                    val imageResId = resources.getIdentifier(module.imageName, "drawable", packageName)
                    if (imageResId != 0) { // Nếu tìm thấy ID tài nguyên ảnh
                        moduleImageView.setImageResource(imageResId) // Đặt ảnh cho ImageView
                    } else { // Nếu không tìm thấy
                        moduleImageView.setImageResource(R.drawable.ic_default_module_icon) // Đặt ảnh mặc định
                        Log.w("HomeActivity", "Không tìm thấy icon cho module: ${module.imageName}") // Cảnh báo log
                    }

                    moduleItem.setOnClickListener { // Đặt trình nghe sự kiện click cho mỗi module
                        navigateToExerciseActivity(module.type, module.exercisesFile) // Chuyển đến Activity bài tập tương ứng
                    }

                    val itemLayoutParams = LinearLayout.LayoutParams( // Thiết lập LayoutParams cho từng module item
                        0, // Chiều rộng ban đầu là 0
                        LinearLayout.LayoutParams.WRAP_CONTENT // Chiều cao tự điều chỉnh
                    ).apply {
                        weight = 1.0f // Đặt trọng số là 1.0f (để chia đều không gian trong hàng)
                        if (innerIndex == 0) { // Nếu là module đầu tiên trong hàng
                            rightMargin = resources.getDimensionPixelSize(R.dimen.module_item_spacing) // Thêm margin phải
                        } else { // Nếu là module thứ hai
                            leftMargin = resources.getDimensionPixelSize(R.dimen.module_item_spacing) // Thêm margin trái
                        }
                    }
                    moduleItem.layoutParams = itemLayoutParams // Áp dụng LayoutParams
                    horizontalLayout.addView(moduleItem) // Thêm module item vào LinearLayout ngang
                }
                binding.modulesContainer.addView(horizontalLayout) // Thêm LinearLayout ngang (chứa 2 module) vào container chính
            }

        } else { // Nếu không tìm thấy thông tin cho cấp độ hiện tại (lỗi)
            Log.e("HomeActivity", "Không tìm thấy thông tin cho cấp độ: $currentUserLevelId. Hiển thị mặc định Level 1.") // Ghi log lỗi
            binding.levelNumber.text = "1" // Hiển thị mặc định Level 1 trên UI
            binding.modulesContainer.removeAllViews() // Xóa các module cũ
            allLevels?.find { it.id == "level_1" }?.let { level1 -> // Tìm cấp độ 1 trong danh sách
                currentLevelNumber = 1 // Đặt số cấp độ hiện tại là 1
            }
            Toast.makeText(this, "Không tìm thấy thông tin cấp độ hiện tại, hiển thị Level 1.", Toast.LENGTH_LONG).show() // Thông báo cho người dùng
        }
    }

    private fun navigateToExerciseActivity(moduleType: String, exercisesFile: String) { // Hàm điều hướng đến Activity bài tập tương ứng
        val intent: Intent // Khai báo Intent
        when (moduleType) { // Kiểm tra loại module
            "vocabulary" -> intent = Intent(this, VocabularyQuestionActivity::class.java) // Nếu là từ vựng, tạo Intent đến VocabularyQuestionActivity
            "question" -> intent = Intent(this, FillBlankQuestionActivity::class.java) // Nếu là điền từ, tạo Intent đến FillBlankQuestionActivity
            "listening" -> intent = Intent(this, ListeningQuestionActivity::class.java) // Nếu là nghe, tạo Intent đến ListeningQuestionActivity
            "reading" -> intent = Intent(this, ReadingQuestionActivity::class.java) // Nếu là đọc hiểu, tạo Intent đến ReadingQuestionActivity
            else -> { // Nếu loại bài tập không hợp lệ
                Toast.makeText(this, "Loại bài tập không hợp lệ: $moduleType", Toast.LENGTH_SHORT).show() // Thông báo lỗi
                return // Thoát hàm
            }
        }
        intent.putExtra("exercises_file", exercisesFile) // Đính kèm tên file bài tập vào Intent
        startActivityForResult(intent, REQUEST_CODE_EXERCISE) // Bắt đầu Activity và mong đợi kết quả trả về
    }

    // --- Bắt đầu phần thêm mới cho logic chuyển level ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { // Phương thức này được gọi khi một Activity con kết thúc và trả về kết quả
        super.onActivityResult(requestCode, resultCode, data) // Gọi onActivityResult của lớp cha
        if (requestCode == REQUEST_CODE_EXERCISE && resultCode == RESULT_OK) { // Kiểm tra nếu kết quả trả về từ bài tập và thành công
            val scoreEarned = data?.getLongExtra("score_earned", 0L) ?: 0L // Lấy điểm kiếm được từ Intent, mặc định là 0
            if (scoreEarned > 0) { // Nếu có điểm kiếm được
                updateUserScoreAndCheckLevel(scoreEarned) // Cập nhật điểm và kiểm tra lên cấp
            }
        }
    }

    // Hàm này được gọi từ các Activity bài tập khi người dùng hoàn thành một bài và nhận điểm
    // (Lưu ý: "Trong HomeActivity.kt, hàm updateUserScoreAndCheckLevel" là comment của bạn, không phải code)
    fun updateUserScoreAndCheckLevel(scoreToAdd: Long) { // Hàm cập nhật điểm và kiểm tra logic lên cấp
        val currentUser = auth.currentUser ?: return // Lấy người dùng hiện tại, nếu null thì thoát
        val userId = currentUser.uid // Lấy UID của người dùng

        Log.d("UpdateLevel", "Calling updateUserScoreAndCheckLevel with scoreToAdd: $scoreToAdd") // Ghi log điểm cộng thêm

        db.collection("users").document(userId).get() // Lấy dữ liệu người dùng từ Firestore
            .addOnSuccessListener { document -> // Khi thành công
                val currentScore = document.getLong("totalScore") ?: 0L // Lấy tổng điểm hiện tại từ Firestore
                val currentLevelFirestoreId = document.getString("currentLevelId") ?: "level_1" // Lấy ID cấp độ hiện tại từ Firestore
                val newTotalScore = currentScore + scoreToAdd // Tính tổng điểm mới

                Log.d("UpdateLevel", "Current Score (Firestore): $currentScore, Score Added: $scoreToAdd, New Total Score (calculated): $newTotalScore") // Ghi log chi tiết điểm
                Log.d("UpdateLevel", "Current Level ID (Firestore): $currentLevelFirestoreId") // Ghi log ID cấp độ hiện tại

                var nextLevelId = currentLevelFirestoreId // Khởi tạo ID cấp độ tiếp theo bằng ID cấp độ hiện tại

                val currentLevel = allLevels?.find { it.id == currentLevelFirestoreId } // Tìm đối tượng cấp độ hiện tại trong danh sách đã tải

                if (currentLevel != null) { // Nếu tìm thấy cấp độ hiện tại
                    val nextLevel = levelsRepository.getNextLevel(currentLevel.id, allLevels) // Lấy cấp độ tiếp theo dựa trên logic trong LevelsRepository

                    if (nextLevel != null) { // Nếu có cấp độ tiếp theo
                        Log.d("UpdateLevel", "Next Level available: ${nextLevel.name} (${nextLevel.id}), Required Score for next: ${nextLevel.requiredScoreToUnlockNext}") // Ghi log thông tin cấp độ tiếp theo
                        // ĐIỂM KIỂM TRA LÊN CẤP BẰNG nextLevel.requiredScoreToUnlockNext
                        // So sánh newTotalScore với requiredScoreToUnlockNext của nextLevel
                        if (newTotalScore >= nextLevel.requiredScoreToUnlockNext) { // Kiểm tra xem tổng điểm mới có đủ để lên cấp tiếp theo không
                            nextLevelId = nextLevel.id // Cập nhật ID cấp độ mới
                            Toast.makeText(this, "Chúc mừng! Bạn đã lên cấp độ ${nextLevel.name}!", Toast.LENGTH_LONG).show() // Thông báo lên cấp
                            Log.d("LevelUp", "Người dùng $userId ĐÃ LÊN CẤP ĐỘ ${nextLevel.name} (${nextLevel.id})") // Ghi log lên cấp
                        } else {
                            Log.d("LevelUp", "Người dùng $userId vẫn ở cấp độ ${currentLevel.name}. Điểm: $newTotalScore (Chưa đủ ${nextLevel.requiredScoreToUnlockNext})") // Ghi log chưa đủ điểm lên cấp
                        }
                    } else {
                        Log.d("LevelUp", "Không có cấp độ tiếp theo cho ID: ${currentLevel.id}") // Ghi log nếu không có cấp độ tiếp theo
                    }
                } else {
                    Log.w("LevelUp", "Không tìm thấy thông tin level cho ID: $currentLevelFirestoreId trong allLevels.") // Cảnh báo nếu không tìm thấy cấp độ trong danh sách đã tải
                }

                // Tạo một Map chứa các trường cần cập nhật trong Firestore
                val updates = hashMapOf(
                    "totalScore" to newTotalScore, // Cập nhật tổng điểm mới
                    "currentLevelId" to nextLevelId // Cập nhật ID cấp độ mới (có thể là cấp độ cũ nếu chưa đủ điểm)
                ) as Map<String, Any> // Ép kiểu thành Map<String, Any>

                db.collection("users").document(userId) // Truy cập tài liệu người dùng trong Firestore
                    .update(updates) // Cập nhật các trường đã định nghĩa
                    .addOnSuccessListener { // Khi cập nhật thành công
                        Log.d("FirestoreUpdate", "Điểm ($newTotalScore) và cấp độ ($nextLevelId) ĐÃ CẬP NHẬT THÀNH CÔNG vào Firestore.") // Ghi log thành công
                        checkUserAndDisplayLevel() // Gọi lại hàm này để làm mới giao diện với thông tin cấp độ và điểm số mới
                    }
                    .addOnFailureListener { e -> // Khi cập nhật thất bại
                        Log.e("FirestoreUpdate", "LỖI CẬP NHẬT điểm/cấp độ vào Firestore: ${e.message}", e) // Ghi log lỗi
                    }
            }
            .addOnFailureListener { e -> // Khi lấy dữ liệu người dùng ban đầu thất bại
                Log.e("HomeActivity", "LỖI KHI LẤY DỮ LIỆU NGƯỜI DÙNG để cập nhật điểm: ${e.message}", e) // Ghi log lỗi
            }
    }
    // --- Kết thúc phần thêm mới ---

    private fun setupBottomNavigationView() { // Hàm thiết lập Bottom Navigation View
        binding.bottomNavigationView.setOnItemSelectedListener { item -> // Đặt trình nghe sự kiện khi một mục được chọn
            when (item.itemId) { // Kiểm tra ID của mục được chọn
                R.id.navigation_home -> { // Nếu là mục Home
                    true // Đang ở HomeActivity, không làm gì
                }
                R.id.navigation_trainslate -> { // Nếu là mục Translate
                    startActivity(Intent(this, TranslateActivity::class.java)) // Chuyển đến TranslateActivity
                    // finish() // Có thể bỏ finish() nếu bạn muốn giữ HomeActivity trên stack
                    true
                }
                R.id.navigation_rank -> { // Nếu là mục Rank
                    startActivity(Intent(this, RankingActivity::class.java)) // Chuyển đến RankingActivity
                    // finish()
                    true
                }
                R.id.navigation_profile -> { // Nếu là mục Profile
                    startActivity(Intent(this, ProfileActivity::class.java)) // Chuyển đến ProfileActivity
                    // finish()
                    true
                }
                else -> false // Mặc định trả về false
            }
        }
        binding.bottomNavigationView.menu.findItem(R.id.navigation_home)?.isChecked = true // Đánh dấu mục Home là đã chọn khi khởi tạo
    }
}