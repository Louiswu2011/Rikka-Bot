package maimai

import io.ktor.util.date.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import utils.exception.ThisIsNotAnException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.max

class Location(val name: String, val keyWords: Array<String>, val groupID: Int) {

    private val format = SimpleDateFormat("HH:mm:ss")
    private val matcherBuilder = StringBuilder()

    val historyList = mutableListOf<History>()

    var player = 0
    var total = 0
    var peak = 0

    var currentCalendar = Calendar.getInstance()

    var pattern: Pattern

    init {
        keyWords.forEach { keyword ->
            matcherBuilder.append(keyword)
            matcherBuilder.append("|")
        }

        pattern = Pattern.compile(
            "(" + matcherBuilder.substring(
                0,
                matcherBuilder.length - 1
            ) + ")([加减+\\-])(\\d+)(?=卡)"
        )


    }

    private fun refresh(){
        currentCalendar = Calendar.getInstance()
    }

    fun reset() {
        currentCalendar.set(
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
        player = 0
        peak = 0
        total = 0
        historyList.clear()
    }

    // Automatically refresh calendar, peak and total when modified

    fun incrementPlayer(int: Int, nickname: String) {
        historyList.add(History(Calendar.getInstance(), nickname, "加", int))
        peak = max(player, player + int)
        total += int
        player += int
        refresh()
    }

    fun decrementPlayer(int: Int, nickname: String) {
        historyList.add(History(Calendar.getInstance(), nickname, "减", int))
        player -= int
        refresh()
    }

    fun history(): Message{
        val buffer = StringBuffer()

        if (historyList.isNotEmpty()) {
            buffer.append("历史记录\n")
            historyList.forEach {
                with(it) {
                    buffer.append("${date.get(Calendar.HOUR_OF_DAY)}:${date.get(Calendar.MINUTE)} $nickname ${operation}了${amount}卡")
                }
            }
        } else {
            buffer.append("暂无历史记录！")
        }

        return PlainText(buffer.toString())
    }

    fun report(): Message = PlainText(name + "共计" + total + "人出勤，峰值" + peak + "人出勤。")

    fun toMessage(): Message = PlainText(name + "当前人数: $player\n更新时间：" + format.format(currentCalendar.time))

    fun dateToString(): String = format.format(currentCalendar.time)

    fun register(command: String, nickname: String) {
        val matcher = pattern.matcher(command)
        if(matcher.find()){
            val location = matcher.group(1)
            if(!keyWords.contains(location)){
                naughty()
            }
            val operation = matcher.group(2)
            val num = matcher.group(3)
            if(num.toInt() >= 10){
                naughty()
            }
            if(operation == "加" || operation == "+"){
                incrementPlayer(num.toInt(), nickname)
                bingo()
            }
            if(player - num.toInt() < 0) {
                naughty()
            }
            decrementPlayer(num.toInt(), nickname)
            bingo()
        }
    }

    private fun bingo(): Nothing = throw ThisIsNotAnException("Bingo!")
    private fun naughty(): Nothing = throw IllegalArgumentException("Oopsie woopsie! Naughty boy!")


}