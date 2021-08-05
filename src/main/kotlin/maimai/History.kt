package maimai

import java.util.*

data class History(
    val date: Calendar,
    val nickname: String,
    val operation: String,
    val amount: Int
)