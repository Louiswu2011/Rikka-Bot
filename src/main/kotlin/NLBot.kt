import kotlinx.coroutines.*
import listeners.CommandListener
import listeners.MaiDataListener
import listeners.MaimaiListener
import listeners.PrivateListener
import loaders.LocationLoader
import loaders.SubscriptionLoader
import maimai.Location
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration
import subscription.User
import kotlin.coroutines.CoroutineContext

class NLBot(qq: Long, password: String) {
    private var job = Job()
    private var userList = mutableListOf<User>()
    private var locations = mutableListOf<Location>()

    val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    // val subscriptionTask: SubscriptionTask

    val subscriptionLoader = SubscriptionLoader()
    val locationLoader = LocationLoader()

    val bot = BotFactory.newBot(qq, password) {
        fileBasedDeviceInfo(System.getProperty("user.dir") + "\\deviceInfo.json")
        protocol = BotConfiguration.MiraiProtocol.ANDROID_PAD
    }

    init {
        userList = subscriptionLoader.getList(System.getProperty("user.dir") + "\\subList.json")

        // subscriptionTask = SubscriptionTask(bot, userList)

        bot.eventChannel.registerListenerHost(MaimaiListener(locations))
        bot.eventChannel.registerListenerHost(PrivateListener(userList))
        bot.eventChannel.registerListenerHost(CommandListener())
        bot.eventChannel.registerListenerHost(MaiDataListener())
    }

    suspend fun start() {
        bot.login()
    }


}