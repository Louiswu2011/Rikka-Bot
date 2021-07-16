package listeners

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.StrangerMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import providers.SubscriptionProvider
import providers.TranslationProvider
import utils.exception.UserNotFoundException
import java.io.File
import java.util.regex.Pattern
import kotlin.coroutines.CoroutineContext
import kotlin.math.sqrt
import kotlin.random.Random

class PrivateListener(var userList: MutableList<subscription.User>) : SimpleListenerHost() {

    private val nlid = 913642213L
    private val dir = System.getProperty("user.dir")
    private val sprovider = SubscriptionProvider()
    private val tprovider = TranslationProvider()
    private val jsonFile = File("$dir\\sublist.json")
    private val spacePattern = Pattern.compile(" ")
    private val translateList = arrayOf("el", "bul", "rom", "slo", "est", "zh")

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        super.handleException(context, exception)
    }

    @EventHandler
    suspend fun GroupMessageEvent.onMessage() {
        if (group.id == nlid) {
            val text = message.contentToString()
            onMessage(text, group)
        }
    }

    @EventHandler
    suspend fun FriendMessageEvent.onMessage() {
        // TODO: Test only
        if (sender.id == 1665135004L) {
            onMessage(message.contentToString(), sender)
        }
    }

    private suspend fun onMessage(message: String, sender: Any) {
        when {
            // Subscription
            /*message.contains("订阅") -> {
                when {
                    message.startsWith("添加订阅 ") -> {
                        val mid = message.split(spacePattern, 2)[1]
                        if (userList.filter { it.mid.toString() == mid }.any()) {
                            send(sender, PlainText("请勿重复添加！"))
                            return
                        }

                        try {
                            val user = sprovider.getUserFromMid(mid.toInt())
                            userList.add(user)
                            sprovider.updateFile(jsonFile, userList)
                            send(sender, PlainText("成功添加对${user.name}的订阅！"))
                        } catch (e: UserNotFoundException) {
                            send(sender, PlainText("未找到对应的用户！"))
                            return
                        }
                    }
                    message.startsWith("删除订阅 ") -> {
                        val mid = message.split(spacePattern, 2)[1]
                        if (!userList.filter { it.mid.toString() == mid }.any()) {
                            send(sender, PlainText("未添加对该用户的订阅！"))
                            return
                        }

                        val obj = userList.single { it.mid.toString() == mid }

                        userList.remove(obj)
                        sprovider.updateFile(jsonFile, userList)
                        send(sender, PlainText("成功删除对${obj.name}的订阅！"))
                    }
                    message.startsWith("订阅列表") -> {
                        val list = userList.toListString()
                        send(sender, PlainText(list))
                    }
                }
            }*/

            // Translation
            message.contains("翻译") -> {
                val request = message.split(spacePattern, 2)[1]
                when {
                    message.startsWith("翻译 ") -> {
                        send(sender, PlainText(tprovider.getTranslation(request, "en")))
                    }
                    message.startsWith("粤语 ") -> {
                        send(sender, PlainText(tprovider.getTranslation(request, "yue")))
                    }
                    message.startsWith("翻译翻译 ") -> {
                        var temp = request
                        translateList.forEach {
                            temp = tprovider.getTranslation(temp, it)
                        }
                        send(sender, PlainText(temp))
                    }
                }
            }

            message.startsWith("排 ") -> {
                val sort = message.split(spacePattern, 2)[1].toCharArray()
                sort.sort()
                send(sender, PlainText(sort.toString()))
            }

            message.startsWith("鸡巴 ") -> {
                var pointer = 0
                var msg = message.split(spacePattern, 2)[1]
                val limit = msg.length
                pointer += Random.nextInt(3)
                while (pointer < limit){
                    msg = msg.insert(pointer, "鸡巴")
                    pointer += pointer + Random.nextInt(sqrt(limit.toDouble()).toInt())
                }
                send(sender, PlainText(msg))
            }

        }
    }

    private suspend fun send(sender: Any, message: Message) {
        when (sender) {
            is User -> sender.sendMessage(message)
            is Group -> sender.sendMessage(message)
            else -> throw IllegalArgumentException("Sender type not acceptable!")
        }
    }

    private fun MutableList<subscription.User>.toListString(): String {
        val builder = StringBuilder()
        builder.append("订阅列表：\n")
        forEachIndexed { index, user ->
            run {
                builder.append("${index+1}.${user.name} (${user.mid})\n")
            }
        }

        return builder.removeSuffix("\n").toString()
    }

    private fun String.insert(index: Int, string: String): String {
        return this.substring(0, index) + string + this.substring(index, this.length)
    }



}