package com.example.englishapp.data

data class UserRank(
    val uid: String = "",
    val fullName: String = "Người dùng ẩn danh",
    val totalScore: Long = 0L,
    val profileImageUrl: String? = null // Tùy chọn: URL ảnh đại diện nếu bạn lưu trên Firebase Storage
) : Comparable<UserRank> { // Triển khai Comparable để sắp xếp

    // Sắp xếp theo totalScore giảm dần
    override fun compareTo(other: UserRank): Int {
        return other.totalScore.compareTo(this.totalScore)
    }
}