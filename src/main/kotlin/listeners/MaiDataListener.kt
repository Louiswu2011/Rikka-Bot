package listeners

import io.ktor.http.cio.*
import maimai.maidata.MaiObjectStats
import maimai.maidata.MaiRecentTrack
import maimai.maidata.MaiTrackDetail
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import providers.MaiDataProvider
import java.lang.IllegalArgumentException
import java.nio.file.Paths
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.math.abs
import kotlin.math.log10


class MaiDataListener : SimpleListenerHost() {

    /*
    绑定用户信息                          使用方法：/绑定 <UserID> <Token>   /bind <UserID> <Token>
    查询用户信息                          使用方法：/用户信息                 /info
    查询最近一次游玩记录（详细）             使用方法：/最近一首                 /recent1
    查询最近50次游玩记录（概要）             使用方法：/最近记录                /recent
    查询最近50次记录中某一首游玩记录（详细）   使用方法：/最近记录 <ID>           /recent <ID>
    查询某一首歌曲排行榜                    暂未实现
    查询某一首歌曲各难度数据与游玩次数        暂未实现
     */

    val provider = MaiDataProvider()

    val credentialsFile = Paths.get("${System.getProperty("user.dir")}\\credentials.txt")
    val credentialsMap = mutableMapOf<Long, MutableList<String>>()

    val hashFile = Paths.get("${System.getProperty("user.dir")}\\database.txt")
    val hashMap = mutableMapOf<String, String>()

    init {
        // TODO: Get user binding information.
        credentialsFile.inputStream().bufferedReader().useLines { lines ->
            lines.forEach {
                val user = it.split(":")[0].toLong()
                val id = it.split(":")[1].split(";")[0]
                val token = it.split(":")[1].split(";")[1]

                val tokenPair = mutableListOf<String>()

                tokenPair.add(id)
                tokenPair.add(token)

                credentialsMap[user] = tokenPair
            }
        }
    }

    @EventHandler
    suspend fun FriendMessageEvent.onMessage() {
        onMessage(message.contentToString(), sender)
    }

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {
        onMessage(message.contentToString(), sender)
    }

    // Primitive output for now. Try to compose a picture?
    private suspend fun onMessage(msg: String, sender: Contact) {
        when {
            msg.startsWith("/绑定 ") || msg.startsWith("/bind ") -> {
                val tokens = msg.split(" ")

                if (tokens.size < 3) {
                    send("绑定所需的参数不足！\n使用方法：绑定 <UserID> <Token>", sender)
                    return
                }

                if (tokens.size > 3) {
                    send("输入的参数过多！\n使用方法：绑定 <UserID> <Token>", sender)
                    return
                }

                if (!credentialsMap.containsKey(sender.id)) {
                    credentialsMap[sender.id] = mutableListOf(tokens[1], tokens[2])
                    send("绑定成功！", sender)
                } else {
                    credentialsMap[sender.id] = mutableListOf(tokens[1], tokens[2])
                    send("更新成功！", sender)
                }
            }

            msg == "/用户信息" || msg == "/info" -> {
                if (!credentialsMap.containsKey(sender.id)) {
                    send("暂未绑定用户数据！\n使用 /绑定 来绑定用户", sender)
                    return
                }

                val user = provider.getMaiUser(credentialsMap[sender.id]!![0], credentialsMap[sender.id]!![1])

                val str = StringBuffer()

                with(user) {
                    str.append("[$username] Rating $rating\n")
                    str.append("$trophyTitle\n")
                    str.append("总游玩次数: ${playCount}次 星星×$starCount\n")
                }

                send(str.toString(), sender)

                credentialsMap[sender.id] = mutableListOf(credentialsMap[sender.id]!![0], provider.latestToken)
                updateCredentials()
            }

            msg == "/最近一首" || msg == "/recent1" -> {
                if (!credentialsMap.containsKey(sender.id)) {
                    send("暂未绑定用户数据！\n使用 /绑定 来绑定用户", sender)
                    return
                }

                val recent1 =
                    provider.getRecentRecord(credentialsMap[sender.id]!![0], credentialsMap[sender.id]!![1])[0]

                val str = StringBuffer()

                with(recent1) {
                    str.append("id$id [$title]\n")
                    str.append("游玩时间：$playtime $trackNum\n")
                    str.append("达成率：$achievement $badgeAchievement $badgeMultiplayer\n")
                    str.append("DX分数：$DXScore")
                }

                send(str.toString(), sender)

                credentialsMap[sender.id] = mutableListOf(credentialsMap[sender.id]!![0], provider.latestToken)
                updateCredentials()
            }

            msg == "/最近记录" || msg == "/recent" -> {
                if (!credentialsMap.containsKey(sender.id)) {
                    send("暂未绑定用户数据！\n使用 /绑定 来绑定用户", sender)
                    return
                }

                val recentList =
                    provider.getRecentRecord(credentialsMap[sender.id]!![0], credentialsMap[sender.id]!![1])

                val msg = StringBuffer()

                recentList.forEach {
                    msg.append("ID:${it.id} ${it.title}\n")
                }
                msg.removeSuffix("\n")

                sender.sendMessage(msg.toString())

                credentialsMap[sender.id] = mutableListOf(credentialsMap[sender.id]!![0], provider.latestToken)
                updateCredentials()
            }

            msg.startsWith("/最近记录 ") || msg.startsWith("/recent ") -> {
                if (!credentialsMap.containsKey(sender.id)) {
                    send("暂未绑定用户数据！\n使用 /绑定 来绑定用户", sender)
                    return
                }

                val args = msg.split(" ")

                if (args.size < 2) {
                    send("查询所需的参数不足！\n使用方法：/最近记录 <记录ID>", sender)
                    return
                }

                if (args.size > 2) {
                    send("输入的参数过多！\n使用方法：/最近记录 <记录ID>", sender)
                    return
                }

                try {
                    val item = provider.getTrackDetailFromRecent(
                        credentialsMap[sender.id]!![0],
                        credentialsMap[sender.id]!![1],
                        args[1]
                    )

                    val msg = StringBuffer()

                    /*
                               CP   P   GR   GD   MS
                        Tap    100  100 100  100  100
                        Hold   90   1   100  100  100
                        Slide  100  100 100  100  100
                        Touch  100  100 100  100  100
                        Break  100  100 100  100  100
                     */

                    with(item) {
                        msg.append("[$title]\n")
                        msg.append("$trackNum $playtime\n")
                        msg.append("成绩：$achievement $badgeAchievement $badgeMultiplayer\n")
                        msg.append("DX分数：$DXScore\n")
                        msg.append("连击：$maxCombo Sync：$maxSync\n")
                        msg.append("\n")

                        msg.append(
                            " ".repeat(7) + "CP" + " ".repeat(3) + "P" + " ".repeat(3) + "GR" + " ".repeat(3) + "GD" + " ".repeat(
                                3
                            ) + "MS" + " ".repeat(1) + "\n"
                        )
                        msg.append(tapStats.prettyPrintStats())
                        msg.append(holdStats.prettyPrintStats())
                        msg.append(slideStats.prettyPrintStats())

                        if(touchStats != null){
                            msg.append(touchStats.prettyPrintStats())
                        }

                        msg.append(breakStats.prettyPrintStats() + "\n")

                        msg.append("拼机人：")
                        if (isMulti()){
                            matchingPlayers.forEach {
                                if (it != "―"){
                                    msg.append("$it ")
                                }
                            }
                            msg.append("\n")
                        } else {
                            msg.append("单刷的\n")
                        }
                        msg.append("\n")

                        msg.append("段位Ra：$danRating 底Ra：$baseRating $ratingChanges")
                    }

                    send(msg.toString(), sender)

                } catch (e: Exception) {
                    send("输入的记录ID有误！", sender)

                    credentialsMap[sender.id] = mutableListOf(credentialsMap[sender.id]!![0], provider.latestToken)
                    updateCredentials()
                }

                credentialsMap[sender.id] = mutableListOf(credentialsMap[sender.id]!![0], provider.latestToken)
                updateCredentials()
            }

            else -> return
        }
    }

    private fun MaiObjectStats.prettyPrintStats() =
        objectName + " ".repeat(7 - objectName.length) + criticalPerfect + " ".repeat(4 - criticalPerfect.length()) + perfect + " ".repeat(
            4 - perfect.length()
        ) + great + " ".repeat(4 - great.length()) + good + " ".repeat(
            4 - good.length()
        ) + miss + " ".repeat(4 - miss.length()) + "\n"

    private fun MaiTrackDetail.isMulti(): Boolean{
        var i = 0
        matchingPlayers.forEach {
            if (it == "-"){
                i++
            }
        }

        return i != 3
    }

    private suspend fun send(msg: String, to: Contact) {
        when (to) {
            is Friend -> to.sendMessage(msg)
            is Member -> to.group.sendMessage(msg)
            else -> throw IllegalArgumentException("Sender type not acceptable!")
        }
    }

    private fun updateCredentials() {
        val str = StringBuffer()

        credentialsMap.forEach { id, tokenPair ->
            str.append("$id:${tokenPair[0]};${tokenPair[1]}\n")
        }

        str.removeSuffix("\n")

        credentialsFile.outputStream().bufferedWriter().use { writer ->
            writer.write(str.toString())
            writer.close()
        }
    }

    private fun Int.length() = when (this) {
        0 -> 1
        else -> log10(abs(toDouble())).toInt() + 1
    }
}