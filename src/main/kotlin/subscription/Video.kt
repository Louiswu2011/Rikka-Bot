package subscription

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import java.net.URL

class Video(
    val BVid: String,
    val title: String,
    val description: String,
    val cover: String,
    val link: String
) {
    suspend fun toMessage(user: Contact): Message {
        return user.uploadImage(URL(cover).openStream())
            .plus(PlainText("\n"))
            .plus("$title\n")
            .plus("简介：$description\n")
            .plus("链接：$link")
    }
}
