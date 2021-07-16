package listeners

import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.FriendMessageEvent

class CommandListener : SimpleListenerHost() {
    @EventHandler
    suspend fun FriendMessageEvent.onMessage() {
        if (sender.id == 166513504L) {    // Check Owner
            val message = message.contentToString()
            if (message.startsWith("/")) {
                val commandGroup = message.substringAfter("/").split(" ")
                when (commandGroup[0]) {
                    "announce" -> bot.groups.forEach { group -> group.sendMessage(commandGroup[1].removeSurrounding("\"")) }
                }
            }
        }
    }


}