package com.app.thediamondage.view
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.thediamondage.R
import com.app.thediamondage.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("VolumePrefs", Context.MODE_PRIVATE)
        val savedVolume = sharedPreferences.getInt("VolumeLevel", 100)

        val gameView = Intent(this@MainActivity, GameView::class.java)
        gameView.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        binding.playBtn.setOnClickListener(View.OnClickListener { startActivity(gameView) })
        binding.settingsBtn.setOnClickListener {
            onClearButtonClick()
        }
        binding.recordBtn.setOnClickListener {
            startActivity(Intent(this, HighScoresActivity::class.java))
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.gamevolume)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.start()
        }

        setVolume(savedVolume)

    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.start()
    }

    fun onClearButtonClick() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Clear Records")
        alertDialogBuilder.setMessage("Are you sure you want to clear all records?")
        alertDialogBuilder.setPositiveButton("Clear") { dialog, which ->
            clearRecords()
        }
        alertDialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val dialogView = layoutInflater.inflate(R.layout.sound_control_dialog, null)
        val soundSeekBar = dialogView.findViewById<SeekBar>(R.id.soundSeekBar)
        val soundValueTextView = dialogView.findViewById<TextView>(R.id.soundValueTextView)

        val savedVolume = sharedPreferences.getInt("VolumeLevel", 100)
        soundSeekBar.progress = savedVolume
        soundValueTextView.text = "Volume: $savedVolume"

        soundSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                soundValueTextView.text = "Volume: $progress"
                setVolume(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        alertDialogBuilder.setView(dialogView)

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun clearRecords() {
        val sharedPreferences1: SharedPreferences = getSharedPreferences("Score", Context.MODE_PRIVATE)
        val editor = sharedPreferences1.edit()
        editor.remove("recordsList")
        editor.remove("motion")
        editor.remove("time")
        editor.apply()
    }

    private fun setVolume(volume: Int) {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumeLevel = (maxVolume * volume) / 100
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0)
        val editor = sharedPreferences.edit()
        editor.putInt("VolumeLevel", volume)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
    }
}
