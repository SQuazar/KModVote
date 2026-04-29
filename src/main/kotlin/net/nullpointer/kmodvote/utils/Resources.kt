package net.nullpointer.kmodvote.utils

import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.util.jar.JarFile

object Resources {
    fun images(folder: String): List<Pair<String, InputStream>> {
        val url = Thread.currentThread().contextClassLoader.getResource(folder)
            ?: return emptyList()

        return when (url.protocol) {
            "file" -> {
                val folder = File(url.toURI())
                folder.listFiles()
                    ?.filter { it.isFile }
                    ?.map { Pair(it.nameWithoutExtension, it.inputStream()) }
                    ?: emptyList()
            }

            "jar" -> {
                val path = url.path.substringBefore("!").removePrefix("file:")
                val pathInJar = url.path.substringAfter("!/")
                val jar = JarFile(URLDecoder.decode(path, "UTF-8"))

                jar.entries().toList()
                    .filter { !it.isDirectory && it.name.startsWith(pathInJar) }
                    .map {
                        Pair(
                            it.name.substringAfterLast("/")
                                .substringBeforeLast("."),
                            jar.getInputStream(it)
                        )
                    }
            }

            else -> emptyList()
        }
    }
}