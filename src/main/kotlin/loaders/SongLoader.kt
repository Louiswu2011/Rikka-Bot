package loaders

import com.beust.klaxon.*
import maimai.Song

class SongLoader : Loader() {

    private var songList = mutableListOf<Song>()

    override fun getList(pathToFile: String): MutableList<Song> {
        val block = Parser.default().parse(readBytes(pathToFile)) as JsonObject
        block.array<JsonObject>("曲目列表")?.forEach {
            with(it) {
                val diff = get("等级") as JsonObject
                songList.add(
                    Song(
                        get("曲名").toString(),
                        get("分类").toString(),
                        get("BPM").toString(),
                        diff["B"].toString(),
                        diff["A"].toString(),
                        diff["E"].toString(),
                        diff["M"].toString(),
                        diff["R"].toString(),
                        get("类型").toString() == "DX",
                        get("封面").toString() + ".jpg"
                    )
                )
            }
        }
        return songList
    }
}