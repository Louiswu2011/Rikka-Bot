package listeners

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import loaders.BufferLoader
import loaders.LocationLoader
import loaders.SongLoader
import maimai.Location
import maimai.Song
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import utils.SongRandom
import utils.exception.ThisIsNotAnException
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.regex.Pattern

class MaimaiListener(var locations: MutableList<Location>) : SimpleListenerHost() {

    val DEBUG_MODE = false

    val NLGroupID = 913642213L

    val CURRENT_WORKING_DIR = System.getProperty("user.dir")
    val r: SongRandom
    val bufferFile = "$CURRENT_WORKING_DIR\\stats.txt"

    val registerPattern = Pattern.compile(
        "([加减+\\-])(\\d+)(?=卡)"
    )

    val timerDelay = 1000 * 30L

    var currentCalendar: Calendar = Calendar.getInstance()

    var isFlushed = true

    var groupMap = mutableMapOf<Int, Int>()
    var songs = mutableListOf<Song>()

    init {
        val locationLoader = LocationLoader()
        val songLoader = SongLoader()
        val bufferLoader = BufferLoader()

        // Read group ids and locations

        with(locationLoader) {
            locations = getList("$CURRENT_WORKING_DIR\\locations.json")
            groupMap = getMap("$CURRENT_WORKING_DIR\\locations.json")
        }

        songs = songLoader.getList("$CURRENT_WORKING_DIR\\songs.json")

        if (File(bufferFile).exists() && BufferedReader(FileReader(bufferFile)).readLine() != null) {
            bufferLoader.load(bufferFile, locations)
        } else {
            updateBuffer()
        }

        r = SongRandom(System.currentTimeMillis().toInt(), songs)

        launch {
            while (true) {
                // println("Checking time...")
                delay(timerDelay)
                currentCalendar = Calendar.getInstance()
                isFlushed =
                    if (currentCalendar.get(Calendar.HOUR_OF_DAY) == 0 && currentCalendar.get(Calendar.MINUTE) == 0 && !isFlushed) {
                        locations.forEach {
                            it.reset()
                        }
                        flushFile()
                        // println("Buffer flushed.")
                        true
                    } else {
                        // println("No need to flush buffer.")
                        false
                    }
            }
        }
    }

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {
        if (!DEBUG_MODE) {
            onMessage(message.contentToString(), sender)
        }
    }

    @EventHandler
    suspend fun FriendMessageEvent.onMessage() {
        onMessage(message.contentToString(), sender)
    }

    @EventHandler
    suspend fun StrangerMessageEvent.onMessage() {
        onMessage(message.contentToString(), sender)
    }

    private suspend fun onMessage(text: String, sender: Any) {
        when {
            text.startsWith("随个") -> {
                try {
                    r.requestRandomSong(text)?.let {
                        send(sender, it.toMessage(sender as Contact))
                        return
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    send(sender, PlainText("没有找到符合条件的歌曲！"))
                    return
                }
            }

            text.endsWith("j") || text.endsWith("几卡") -> {
                val requestLocation: String =
                    if (text.endsWith("j")) text.substring(0, text.length - 1) else text.substring(0, text.length - 2)
                if (sender is Member) {
                    getReachableLocationList(sender.group.id).forEach { location ->
                        with(location) {
                            if (keyWords.contains(requestLocation)) {
                                send(sender, location.toMessage())
                                return
                            }
                        }
                    }
                } else if (sender is Friend) {
                    locations.forEach { location ->
                        with(location) {
                            if (keyWords.contains(requestLocation)) {
                                send(sender, location.toMessage())
                                return
                            }
                        }
                    }
                }
            }

            text.startsWith("今天maimai打什么", true) -> send(sender, r.getRandomSong().toMessage(sender as Contact))

            // 加减卡
            registerPattern.matcher(text).find() -> {
                if (sender is Member) {
                    getReachableLocationList(sender.group.id).forEach { location ->
                        try {
                            location.register(text)
                        } catch (e: ThisIsNotAnException) {
                            send(sender, location.toMessage())
                            updateBuffer()
                            return
                        } catch (e: IllegalArgumentException) {
                            return
                        }
                    }
                } else if (sender is Friend) {
                    locations.forEach { location ->
                        try {
                            location.register(text)
                        } catch (e: ThisIsNotAnException) {
                            send(sender, location.toMessage())
                            updateBuffer()
                            return
                        } catch (e: IllegalArgumentException) {
                            return
                        }
                    }
                }
            }

            /*text == "刷新" -> {
                locations.forEach {
                    it.reset()
                }
                flushFile()
            }*/
        }
    }

    private suspend fun send(sender: Any, message: Message) {
        when (sender) {
            is Friend -> sender.sendMessage(message)
            is Member -> sender.group.sendMessage(message)
            else -> throw IllegalArgumentException("Sender type not acceptable!")
        }
    }

    private fun getReachableLocationList(groupID: Long): MutableList<Location> {
        return if (groupID != NLGroupID) {
            locations.filter { it -> groupMap.filterValues { it == groupID.toInt() }.keys.single() == it.groupID }
                .toMutableList()
        } else {
            locations
        }
    }

    private fun updateBuffer() {
        val builder = StringBuilder()
        File(bufferFile).bufferedWriter().use { out ->
            locations.forEach {
                builder.append("${it.player},")
                builder.append("${it.total},")
                builder.append("${it.peak},")
                builder.append("${it.dateToString()}\n")
            }
            out.write(builder.toString().removeSuffix("\n"))
            out.close()
        }
    }

    suspend fun flushFile(){
        withContext(Dispatchers.IO) {
            val builder = StringBuilder()
            val writer = File(bufferFile).bufferedWriter()
            locations.forEach {
                builder.append("${it.player},")
                builder.append("${it.total},")
                builder.append("${it.peak},")
                builder.append("${it.dateToString()}\n")
            }
            writer.write(builder.toString().removeSuffix("\n"))
            writer.close()
        }
    }

}