package utils

import maimai.Song
import java.util.regex.Pattern
import kotlin.random.Random

class SongRandom(seed: Int, val songList: MutableList<Song>) {
    private val r = Random(seed)
    private val singleQueryPattern =
        Pattern.compile("(?<=随个)(v家|动漫|原创|东方|联动|综艺)?(标准|DX)?(\\d+)(\\+)?", Pattern.CASE_INSENSITIVE)
    private val rangeQueryPattern =
        Pattern.compile("(?<=随个)(v家|动漫|原创|东方|联动|综艺)?(标准|DX)?(\\d+)(\\+)?-(\\d+)(\\+)?", Pattern.CASE_INSENSITIVE)

    private var temp = mutableListOf<Song>()

    fun getRandomSong(): Song {
        return songList[r.nextInt(songList.size)]
    }

    // Pattern Group: (Category)(isDX)(MinLevel)(+)(MaxLevel)(+)

    fun requestRandomSong(request: String): Song? {
        if (request.contains("-")) {
            // Range query
            val matcher = rangeQueryPattern.matcher(request)
            if (!matcher.find()) {
                return null
            }
            val category = matcher.group(1)
            val isDX = matcher.group(2)
            val minLevel = matcher.group(3) + if (matcher.group(4) == null) "" else matcher.group(4)
            val maxLevel = matcher.group(5) + if (matcher.group(6) == null) "" else matcher.group(6)


            if (category == null) {
                // Category not specified
                return if (isDX == null) {
                    // Version not specified
                    getRandomSongInRange(minLevel, maxLevel)
                } else {
                    // Version specified
                    getRandomSongInRange(minLevel, maxLevel, isDX == "DX")
                }
            } else {
                // Category specified
                return if (isDX == null) {
                    // Version not specified
                    getRandomSongInRange(minLevel, maxLevel, category)
                } else {
                    // Version specified
                    getRandomSongInRange(minLevel, maxLevel, category, isDX == "DX")
                }
            }

        } else {
            // Single query
            val matcher = singleQueryPattern.matcher(request)
            if (!matcher.find()) {
                return null
            }
            val category = matcher.group(1)
            val isDX = matcher.group(2)
            val level = matcher.group(3) + if (matcher.group(4) == null) "" else matcher.group(4)

            if (category == null) {
                // Category not specified
                return if (isDX == null) {
                    // Version not specified
                    getRandomSong(level)
                } else {
                    // Version specified
                    getRandomSong(level, isDX == "DX")
                }
            } else {
                // Category specified
                return if (isDX == null) {
                    // Version not specified
                    getRandomSong(level, category)
                } else {
                    // Version specified
                    getRandomSong(level, category, isDX == "DX")
                }
            }
        }
    }

    fun getRandomSong(level: String): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                if (level_basic == level || level_advanced == level || level_expert == level || level_master == level || level_remaster == level) {
                    temp.add(this)
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    fun getRandomSong(level: String, category: String): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                if (category.getCategoryKey() == song.category) {
                    if (level_basic == level || level_advanced == level || level_expert == level || level_master == level || level_remaster == level) {
                        temp.add(this)
                    }
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    fun getRandomSong(level: String, isDX: Boolean): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                if (song.isDX == isDX) {
                    if (level_basic == level || level_advanced == level || level_expert == level || level_master == level || level_remaster == level) {
                        println(toString())
                        temp.add(this)
                    }
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    fun getRandomSong(level: String, category: String, isDX: Boolean): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                if (category.getCategoryKey() == song.category && isDX == song.isDX) {
                    if (level_basic == level || level_advanced == level || level_expert == level || level_master == level || level_remaster == level) {
                        temp.add(this)
                    }
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    fun getRandomSongInRange(min: String, max: String): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                val diffTable =
                    mutableListOf(level_basic, level_advanced, level_expert, level_master, level_remaster)
                diffTable.forEach { diff ->
                    if (diff.getLevelValue() >= min.toDouble() && diff.getLevelValue() <= max.toDouble()) temp.add(song)
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    fun getRandomSongInRange(min: String, max: String, category: String): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                if (category.getCategoryKey() == song.category) {
                    val diffTable =
                        mutableListOf(level_basic, level_advanced, level_expert, level_master, level_remaster)
                    diffTable.forEach { diff ->
                        if (diff.getLevelValue() >= min.toDouble() && diff.getLevelValue() <= max.toDouble()) temp.add(
                            song
                        )
                    }
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    fun getRandomSongInRange(min: String, max: String, isDX: Boolean): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                if (isDX == song.isDX) {
                    val diffTable =
                        mutableListOf(level_basic, level_advanced, level_expert, level_master, level_remaster)
                    diffTable.forEach { diff ->
                        if (diff.getLevelValue() >= min.toDouble() && diff.getLevelValue() <= max.toDouble()) temp.add(
                            song
                        )
                    }
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    fun getRandomSongInRange(min: String, max: String, category: String, isDX: Boolean): Song? {
        temp.clear()
        songList.forEach { song ->
            with(song) {
                if (category.getCategoryKey() == song.category && isDX == song.isDX) {
                    val diffTable =
                        mutableListOf(level_basic, level_advanced, level_expert, level_master, level_remaster)
                    diffTable.forEach { diff ->
                        if (diff.getLevelValue() >= min.toDouble() && diff.getLevelValue() <= max.toDouble()) temp.add(
                            song
                        )
                    }
                }
            }
        }
        if (temp.isEmpty()) {
            throw Exception("Song not found.")
        }
        return temp[r.nextInt(temp.size)]
    }

    private fun String.getLevelValue(): Double {
        return if (contains("+")) {
            split("+")[0].toDouble() + 0.5
        } else if (equals("-")) {
            (-1).toDouble()
        } else {
            toDouble()
        }
    }

    private fun String.getCategoryKey(): String {
        return when (this) {
            "东方" -> "toho"
            "综艺" -> "variety"
            "联动" -> "gekichu"
            "原创" -> "maimai"
            "动漫" -> "pops_anime"
            else -> "niconico"
        }
    }
}