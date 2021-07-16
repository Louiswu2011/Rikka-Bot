package providers

import com.beust.klaxon.*
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.random.Random

class TranslationProvider {

    /* Translation provider: Baidu Translation Free API */

    private val CHARSET = StandardCharsets.UTF_8
    private val baseUrl = "http://api.fanyi.baidu.com/api/trans/vip/translate"
    private val APPID = "20200406000412864"
    private val KEY = "4NpUz5e2Wzo1c8Vl45iL"

    fun getTranslation(text: String, targetLanguage: String): String {
        val salt = getSalt()
        val queryURL = String.format("q=%s&from=auto&to=%s&appid=%s&salt=%s&sign=%s",
            URLEncoder.encode(text, CHARSET.name()),
            URLEncoder.encode(targetLanguage, CHARSET.name()),
            URLEncoder.encode(APPID, CHARSET.name()),
            URLEncoder.encode(salt, CHARSET.name()),
            URLEncoder.encode(getSign(text, salt), CHARSET.name()))

        val url = URL("$baseUrl?$queryURL")

        val builder = StringBuilder()
        with(url.openConnection()){
            setRequestProperty("Accept_Charset", CHARSET.name())

            getInputStream().bufferedReader().use {
                it.lines().forEach {
                    line -> builder.append(line)
                }
            }
        }

        val returnJson = Parser.default().parse(builder) as JsonObject
        val array = returnJson.array<JsonObject>("trans_result")
        val obj = array?.get(0)
        return obj?.get("dst").toString()
    }

    private fun getSalt(): String {
        return (Random.nextInt(204060) + 4521845).toString()
    }

    private fun getSign(text: String, salt: String): String = String(getMD5(APPID + String(text.toByteArray(CHARSET), CHARSET) + salt + KEY).toByteArray(CHARSET), CHARSET)

    private fun getMD5(text: String): String = MessageDigest.getInstance("MD5").digest(text.toByteArray(CHARSET)).toHex()

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }


}