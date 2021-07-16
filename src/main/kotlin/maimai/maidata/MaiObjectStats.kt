package maimai.maidata

data class MaiObjectStats(
    val objectName: String,
    val criticalPerfect: Int,
    val perfect: Int,
    val great: Int,
    val good: Int,
    val miss: Int
)