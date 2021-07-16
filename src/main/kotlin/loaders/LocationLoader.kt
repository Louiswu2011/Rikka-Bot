package loaders

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import maimai.Location

class LocationLoader : Loader() {

    private var flag = false
    private lateinit var block: JsonObject

    private val groupMap = mutableMapOf<Int, Int>()
    private val locationList = mutableListOf<Location>()

    override fun getList(pathToFile: String): MutableList<Location> {
        block = Parser.default().parse(readBytes(pathToFile)) as JsonObject
        val locationArray = block["Locations"] as JsonArray<JsonObject>
        locationArray.forEach {
            with(it) {
                val aliasArray = get("aliases") as JsonArray<String>
                val aliases = append(aliasArray.toTypedArray(), get("name").toString())
                locationList.add(
                    Location(
                        get("name").toString(),
                        aliases,
                        get("groupID") as Int
                    )
                )
            }
        }
        flag = true
        return locationList
    }

    fun getMap(pathToFile: String): MutableMap<Int, Int> {
        block = Parser.default().parse(readBytes(pathToFile)) as JsonObject
            val mapArray = block["Map"] as JsonObject
            mapArray.forEach {
                with(it as Map.Entry<String, Int>) {
                    groupMap[key.toInt()] = value
                }
            }
            return groupMap

    }

    private fun append(arr: Array<String>, element: String): Array<String> {
        val list: MutableList<String> = arr.toMutableList()
        list.add(element)
        return list.toTypedArray()
    }

}