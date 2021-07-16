package maimai.maidata

data class MaiUser(
    val username: String,
    val rating: Int,
    val maxRating: Int,
    val playCount: Int,
    val trophyTitle: String,
    val starCount: Int,
    val avatarUrl: String
)