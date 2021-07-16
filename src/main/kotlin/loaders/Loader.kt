package loaders

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

abstract class Loader {
    val CHARSET = StandardCharsets.UTF_8

    fun readBytes(pathToFile: String): StringBuilder{
        val file = File(pathToFile)
        val bytes = Files.readAllBytes(Paths.get(pathToFile))
        return StringBuilder(String(bytes, CHARSET))
    }

    abstract fun getList(pathToFile: String): MutableList<*>
}