package loaders

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

abstract class Loader {
    private val charset: Charset = StandardCharsets.UTF_8

    fun readBytes(pathToFile: String): StringBuilder{
        val bytes = Files.readAllBytes(Paths.get(pathToFile))
        return StringBuilder(String(bytes, charset))
    }

    abstract fun getList(pathToFile: String): MutableList<*>
}