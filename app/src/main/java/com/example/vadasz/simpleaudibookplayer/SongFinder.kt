package com.example.vadasz.simpleaudibookplayer

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

import java.util.ArrayList
import java.util.Random

class SongFinder(val contentResolver: ContentResolver) {
    private val mSongs = ArrayList<Song>()
    private val mRandom = Random()

    val randomSong: Song?
        get() = if (mSongs.size <= 0) null else mSongs[mRandom.nextInt(mSongs.size)]

    val allSongs: List<Song>
        get() = mSongs

    fun prepare() {
        val uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val cur = contentResolver.query(uri, null,
                MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null) ?: return

        if (!cur.moveToFirst()) {
            return
        }

        val artistColumn = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val titleColumn = cur.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val albumColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val albumArtColumn = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
        val durationColumn = cur.getColumnIndex(MediaStore.Audio.Media.DURATION)
        val idColumn = cur.getColumnIndex(MediaStore.Audio.Media._ID)

        do {
            mSongs.add(Song(
                    cur.getLong(idColumn),
                    cur.getString(artistColumn),
                    cur.getString(titleColumn),
                    cur.getString(albumColumn),
                    cur.getLong(durationColumn),
                    cur.getLong(albumArtColumn)))
        } while (cur.moveToNext())

    }

    class Song(id: Long, artist: String, title: String, album: String, duration: Long, albumId: Long) {
        var id: Long = 0
            internal set
        var artist: String
            internal set
        var title: String
            internal set
        var album: String
            internal set
        var albumId: Long = 0
            internal set
        var duration: Long = 0
            internal set

        val uri: Uri
            get() = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

        val albumArt: Uri?
            get() {
                try {
                    val genericArtUri = Uri.parse("content://media/external/audio/albumart")
                    return ContentUris.withAppendedId(genericArtUri, albumId)
                } catch (e: Exception) {
                    return null
                }

            }

        init {
            this.id = id
            this.artist = artist
            this.title = title
            this.album = album
            this.duration = duration
            this.albumId = albumId
        }
    }

    companion object {
        private val TAG = "DROID_MELODY_FINDER"
    }
}
