package com.example.vadasz.simpleaudibookplayer

import android.content.ContentUris
import android.net.Uri
import java.io.File

fun bookFinder(baseName: String, rootDir: String) {
    val files = File(rootDir).listFiles().filter({ it.isFile })
    val dirs = File(rootDir).listFiles().filter({ it.isDirectory })

    if (dirs.isNotEmpty()) {
        dirs.flatMap {  bookFinder("$baseName / ${it.name}", it.path) }
    } else {
        return BookEntry(baseName, files)
    }
}

class BookEntry(val name: String, files: List<File>)

class Book(
        val id: Long,
        val name: String,
        private val bookCoverId: Long
) {
    fun albumArt(): Uri? {
        return try {
            val genericArtUri = Uri.parse("content://media/external/audio/albumart")
            ContentUris.withAppendedId(genericArtUri, bookCoverId)
        } catch (e: Exception) {
            null
        }
    }

    fun getNextChapter(): BookChapter {
        return BookChapter(1L, 1L, "asd", 1L)
    }
}

class BookChapter(
        val id: Long,
        val audioId: Long,
        val title: String,
        val duration: Long
) {
    fun getUri(): Uri {
        return ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
    }
}
