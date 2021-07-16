package maimai

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import java.io.File

class Song(val name: String,
                val category: String,
                val bpm: String,
                val level_basic: String,
                val level_advanced: String,
                val level_expert: String,
                val level_master: String,
                val level_remaster: String,
                val isDX: Boolean,
                val image: String){

    private val dir = System.getProperty("user.dir")

    suspend fun toMessage(user: Contact): Message {
        return user.uploadImage(File(System.getProperty("user.dir") + "\\pic\\$image"))
            .plus(PlainText("\n"))
            .plus(PlainText("[" + if (isDX) ("DX") else ("标准")))
            .plus("] ")
            .plus(PlainText("$name\n"))
            .plus(PlainText("BPM ${bpm}\n"))
            .plus("B").plus(level_basic).plus(" ")
            .plus("A").plus(level_advanced).plus(" ")
            .plus("E").plus(level_expert).plus(" ")
            .plus("M").plus(level_master).plus(" ")
            .plus("R").plus(level_remaster)
    }
}