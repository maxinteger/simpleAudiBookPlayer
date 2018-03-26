package com.example.vadasz.simpleaudibookplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.github.angads25.filepicker.model.DialogConfigs
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*
import com.github.angads25.filepicker.model.DialogProperties
import java.io.File
import com.github.angads25.filepicker.view.FilePickerDialog
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.PRIMARY_KEY
import org.jetbrains.anko.db.createTable


class MainActivity : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    val PREFS_NAME = "MyPrefsFile"
    var booksRootDir = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Ask for the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        } else {

            val settings = getSharedPreferences(PREFS_NAME, 0)
            booksRootDir = settings.getString("booksRootDir", "")

            if (booksRootDir.isBlank()) {
                setupBookRoot()
            } else {
                //createPlayer()
                bookFinder(booksRootDir)
                createList()
            }

            // Start creating the user interface
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun setupBookRoot (){
        val properties = DialogProperties()
        properties.selection_mode = DialogConfigs.SINGLE_MODE
        properties.selection_type = DialogConfigs.DIR_SELECT
        properties.root = File(DialogConfigs.DEFAULT_DIR)
        properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
        properties.offset = File(DialogConfigs.DEFAULT_DIR)
        properties.extensions = null

        val dialog = FilePickerDialog(this@MainActivity, properties)
        dialog.setTitle("Hangos könyv könyvtár kiválasztása")

        dialog.setDialogSelectionListener {(dir: String) ->
            val settings = getSharedPreferences(PREFS_NAME, 0)
            val editor = settings.edit()
            editor.putString("booksRootDir", dir)
            editor.apply()

            booksRootDir = dir
        }
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createPlayer()
        } else {
            longToast("Permission not granted. Shutting down.")
            finish()
        }
    }

    private fun createList(){
        val listUI = object:AnkoComponent<MainActivity> {
            override fun createView(ui : AnkoContext<MainActivity>) = with(ui) {
                val mAdapter = ListExampleAdapter(owner)
                verticalLayout {
                    relativeLayout {
                        textView("Students").lparams {
                            centerHorizontally()
                        }
                    }
                    listView {
                        adapter = mAdapter
                    }
                }
            }
        }

        listUI.setContentView(this@MainActivity)
    }

    private fun createPlayer() {
        val songsJob = async {
            val songFinder = SongFinder(contentResolver)
            songFinder.prepare()
            songFinder.allSongs

        }

        launch(kotlinx.coroutines.experimental.android.UI) {
            val songs = songsJob.await()

            val playerUI = object:AnkoComponent<MainActivity> {
                var albumArt: ImageView? = null

                var playButton: ImageButton? = null
                var shuffleButton: ImageButton? = null

                var songTitle: TextView? = null
                var songArtist: TextView? = null


                fun playRandom() {
                    Collections.shuffle(songs)
                    val song = songs[0]

                    albumArt?.imageURI = song.albumArt
                    songTitle?.text = song.title
                    songArtist?.text = song.artist

                    mediaPlayer?.reset()
                    mediaPlayer = MediaPlayer.create(ctx, song.uri)
                    mediaPlayer?.setOnCompletionListener {
                        playRandom()
                    }

                    mediaPlayer?.start()
                    playButton?.imageResource = R.drawable.ic_pause_black_24dp
                }

                fun playOrPause() {
                    val songPlaying: Boolean? = mediaPlayer?.isPlaying

                    if(songPlaying == true) {
                        mediaPlayer?.pause()
                        playButton?.imageResource = R.drawable.ic_play_arrow_black_24dp
                    } else {
                        mediaPlayer?.start()
                        playButton?.imageResource = R.drawable.ic_pause_black_24dp
                    }
                }

                override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
                    relativeLayout {
                        backgroundColor = Color.BLACK
                        albumArt = imageView {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                        }.lparams(matchParent, matchParent)

                        verticalLayout {
                            backgroundColor = Color.parseColor("#99000000")

                            songTitle = textView {
                                textColor = Color.WHITE
                                typeface = Typeface.DEFAULT_BOLD
                                textSize = 18f
                            }

                            songArtist = textView {
                                textColor = Color.WHITE
                            }

                            linearLayout {

                                playButton = imageButton {
                                    imageResource = R.drawable.ic_play_arrow_black_24dp
                                    onClick {
                                        playOrPause()
                                    }
                                }.lparams(0, wrapContent, 0.5f)

                                shuffleButton = imageButton {
                                    imageResource = R.drawable.ic_shuffle_black_24dp
                                    onClick {
                                        playRandom()
                                    }
                                }.lparams(0, wrapContent, 0.5f)

                            }.lparams(matchParent, wrapContent) {
                                topMargin = dip(5)
                            }

                        }.lparams(matchParent, wrapContent) {
                            alignParentBottom()
                        }
                    }
                }
            }

            playerUI.setContentView(this@MainActivity)
            playerUI.playRandom()
        }
    }
}
