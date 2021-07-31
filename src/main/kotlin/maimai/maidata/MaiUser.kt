package maimai.maidata

data class MaiUser(
    val username: String,
    val rating: Int,
    val maxRating: Int,
    val playCount: String,
    val trophyTitle: String,
    val starCount: String,
    val avatarUrl: String
)