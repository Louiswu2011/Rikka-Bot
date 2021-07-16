package subscription

import kotlinx.coroutines.delay
import loaders.SubscriptionLoader
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import providers.SubscriptionProvider
import java.io.File

class SubscriptionTask(val bot: Bot, var userList: MutableList<User>) {

    val dir = System.getProperty("user.dir")
    val provider = SubscriptionProvider()
    val loader = SubscriptionLoader()
    val delay: Long = 1000 * 60 * 5  // 5 minutes delay
    val jsonFile = File("$dir\\sublist.json")
    var isActive = true
    var shouldUpdate = false

    init {
        // userList = loader.getList("$dir\\sublist.json")
    }

    fun SubscriptionProvider.updateFile() {
        shouldUpdate = false
        jsonFile.bufferedWriter().use { out ->
            out.write(getJSONText(userList))
            out.close()
        }
    }

    suspend fun launch() {
        while (isActive) {
            run()
            delay(delay)
        }
    }

    private suspend fun run() {
        // val group = bot.getFriend(1665135004) // TODO: Change to group when finished
        val group = bot.getGroup(913642213L)
        userList = loader.getList("$dir\\sublist.json")
        userList.forEach { user ->
            with(user) {
                val bvid = provider.getLatestBVid(mid)
                if (bvid != latestBVid) {
                    group?.sendMessage(provider.getVidInfo(bvid).toMessage(group as Contact))
                    userList[userList.indexOf(this)] = User(mid, name, bvid)
                    shouldUpdate = true
                }
            }
        }
        if (shouldUpdate) {
            provider.updateFile()
        }
    }


}