package providers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import subscription.User
import subscription.Video
import utils.exception.UserNotFoundException
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets

class SubscriptionProvider {
    private val spaceUrl = "http://api.bilibili.com/x/space/acc/info"
    private val latestVidUrl = "http://api.bilibili.com/x/space/arc/search"
    private val videoUrl = "http://api.bilibili.com/x/web-interface/view"
    private val linkUrl = "https://www.bilibili.com/"

    fun getUserFromMid(mid: Int): User {
        val queryUrl = URL("$spaceUrl?mid=$mid")

        val builder = get(queryUrl)
        println(builder)

        val returnJson = Parser.default().parse(builder) as JsonObject
        if (returnJson["code"] == -404) {
            throw UserNotFoundException("Nope.")
        }
        if (returnJson["code"] != 0) {
            throw IllegalAccessException("Request refused by api.")
        }
        val dataObject = returnJson["data"] as JsonObject

        return User(mid, dataObject["name"].toString(), getLatestBVid(mid))

    }

    fun getLatestBVid(mid: Int): String {
        with(getLatestVidObject(mid)) {
            return get("bvid").toString()
        }
    }

    fun getVidInfo(bvid: String): Video {
        val returnJson = Parser.default().parse(get(URL("$videoUrl?bvid=$bvid"))) as JsonObject
        if (returnJson["code"] != 0) {
            throw IllegalAccessException("Request refused by api.")
        }

        val dataObject = returnJson["data"] as JsonObject
        return Video(
            dataObject["bvid"].toString(),
            dataObject["title"].toString(),
            dataObject["desc"].toString(),
            dataObject["pic"].toString(),
            linkUrl + dataObject["bvid"].toString()
        )
    }

    suspend fun updateFile(jsonFile: File, userList: MutableList<User>) {
        jsonFile.bufferedWriter().use { out ->
            out.write(getJSONText(userList))
            out.close()
        }
    }

    fun getJSONText(userList: MutableList<User>): String {
        val json = buildJsonObject {
            putJsonArray("users") {
                userList.forEach {
                    addJsonObject {
                        put("displayName", it.name)
                        put("mid", it.mid)
                        put("latestVid", it.latestBVid)
                    }
                }
            }
        }
        return json.toString()
    }


    private fun get(url: URL): StringBuilder {
        val builder = StringBuilder()

        with(url.openConnection()) {
            setRequestProperty("Accept_Charset", StandardCharsets.UTF_8.name())

            getInputStream().bufferedReader().use {
                it.lines().forEach { line -> builder.append(line) }
            }
        }

        return builder
    }

    private fun getLatestVidObject(mid: Int): JsonObject {
        val queryUrl = URL("$latestVidUrl?mid=$mid&pn=1&ps=1")

        val returnJson = Parser.default().parse(get(queryUrl)) as JsonObject
        if (returnJson["code"] != 0) {
            throw IllegalAccessException("Request refused by api.")
        }
        println(returnJson.toJsonString(true))
        val dataObject = returnJson["data"] as JsonObject
        val listObject = dataObject["list"] as JsonObject
        val vlistArray = listObject.array<JsonObject>("vlist")
        return vlistArray?.get(0) ?: throw Exception("This is impossible")
    }
}