package loaders

import maimai.Location
import java.util.*

class BufferLoader : Loader() {
    fun load(pathToFile: String, locationList: MutableList<Location>) {
        val current = Calendar.getInstance()
        var statsGroup: List<String>
        readBytes(pathToFile).lines().forEachIndexed { i, s ->
            statsGroup = s.split(",")
            locationList[i].player = statsGroup[0].toInt()
            locationList[i].total = statsGroup[1].toInt()
            locationList[i].peak = statsGroup[2].toInt()
            statsGroup = statsGroup[3].split(":")
            locationList[i].currentCalendar.set(
                current.get(Calendar.YEAR),
                current.get(Calendar.MONTH),
                current.get(Calendar.DAY_OF_MONTH),
                statsGroup[0].toInt(),
                statsGroup[1].toInt(),
                statsGroup[2].toInt()
            )
        }
    }

    override fun getList(pathToFile: String): MutableList<*> {
        throw IllegalStateException("Not compatible.")
    }
}