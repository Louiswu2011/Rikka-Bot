package maimai.maidata

data class MaiTrackDetail(
    val id: String,
    val title: String,
    val trackNum: String,
    val playtime: String,
    val achievement: String,
    val badgeAchievement: String,
    val badgeMultiplayer: String,
    val DXScore: String,
    val tapStats: MaiObjectStats,
    val holdStats: MaiObjectStats,
    val slideStats: MaiObjectStats,
    val touchStats: MaiObjectStats?,
    val breakStats: MaiObjectStats,
    val danRating: String,
    val baseRating: String,
    val ratingChanges: String,
    val maxCombo: String,
    val maxSync: String,
    val matchingPlayers: MutableList<String>,
    val trackHash: String,
    val trackDiff: String
)