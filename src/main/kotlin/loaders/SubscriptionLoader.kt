package loaders

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import subscription.User

class SubscriptionLoader : Loader() {
    override fun getList(pathToFile: String): MutableList<User> {
        val userList = mutableListOf<User>()
        val builder = readBytes(pathToFile)

        val subJson = Parser.default().parse(builder) as JsonObject
        val listArray = subJson.array<JsonObject>("users")

        listArray?.forEach {
            with(it) {
                userList.add(User(get("mid") as Int, get("displayName").toString(), get("latestVid").toString()))
            }
        }

        return userList
    }
}