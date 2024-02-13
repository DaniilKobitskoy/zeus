package com.app.thediamondage.view

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.app.thediamondage.R
import com.app.thediamondage.databinding.ActivityGameViewBinding
import com.app.thediamondage.model.Record
import com.google.gson.Gson

class GameView : AppCompatActivity() {
    lateinit var recordsList: MutableList<Record>
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var buttons: List<AppCompatButton>
    lateinit var binding: ActivityGameViewBinding
    var motion: TextView? = null
    var playTime: TextView? = null
    var highestMotion: TextView? = null
    var highestTime: TextView? = null
    var pauseButton: ImageView? = null
    var arrBtnImages = arrayOfNulls<String>(12)
    var openCards = arrayOfNulls<String>(2)
    var cardsDisableListeners = ArrayList<String?>()
    var stopCounting = false
    private var count = 0
    private var motionCount = 0
    private var sec = 0
    private var min = 0
    private var noOfMatches = 0
    private var secondsToShow = 0
    private var highestMotionStored = 0
    private var highestTimeStored = 0
    private var isHighScoreBroke = false
    private var highestMinutesStored = 0
    var removeListener = false
    var IMain: Intent? = null
    var IGameView: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeViews()
        initializeIntents()
        setScore()
        recordsList = mutableListOf()
        mediaPlayer = MediaPlayer.create(this, R.raw.gamevolume)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.start()
        }
        val images = ArrayList(
            mutableListOf(
                "card1", "card1", "card2",
                "card2", "card3", "card3",
                "card4", "card4", "card5",
                "card5", "card6", "card6",
                "card7", "card7", "card8",
                "card8", "card9", "card9",
            )
        )
        val min = 0
        var max = 11
        var random: Int
        for (i in arrBtnImages.indices) {
            random = (Math.random() * (max - min + 1)).toInt()
            arrBtnImages[i] = images[random]
            images.removeAt(random)
            max--
        }

        pauseButton!!.setOnClickListener(View.OnClickListener {
            pauseButton!!.setBackgroundResource(R.drawable.play)
            stopCounting = true
            openPauseDialog()
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        startActivity(Intent(this, MainActivity::class.java))
//        openPauseDialog()
    }
    private fun initializeIntents() {
        IMain = Intent(this@GameView, MainActivity::class.java)
        IGameView = Intent(intent)
        IMain!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
        IGameView!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY)
    }
    private fun initializeViews() {
        buttons = listOf(
            binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8,
            binding.btn9, binding.btn10, binding.btn11, binding.btn12
        )
        pauseButton = binding.pauseBtn
        motion = binding.moves
        playTime = binding.time
        highestMotion = binding.highestMoves
        highestTime = binding.highestTime
    }

    fun btnClick(v: View) {
        if (count == 0) {
            timerStart()
        }
        if (!removeListener && !cardsDisableListeners.contains(v.tag)) {
            val buttonCards = v as AppCompatButton
            buttonCards.text = ""
            val idButton = v.getTag() as String
            val idIndexSubstring = idButton.substring(3).toInt()
            val idIndex = idIndexSubstring - 1
            val resId = resources.getIdentifier(
                arrBtnImages[idIndex], "drawable",
                packageName
            )
            buttonCards.background = resources.getDrawable(resId, null)
            count++
            if (count % 2 != 0) {
                openCards[0] = idButton
            } else {
                if (openCards[0] === idButton) {
                    count--
                } else {
                    openCards[1] = idButton
                    removeListener = true
                    incrementMoves()
                    checkCardMatch()
                }
            }
        }
    }

    private fun checkCardMatch() {
        if (arrBtnImages[openCards[0]!!.substring(3)
                .toInt() - 1] != arrBtnImages[openCards[1]!!.substring(3).toInt() - 1]
        ) {
            val openCardId1 = resources.getIdentifier(openCards[0], "id", packageName)
            val openCardId2 = resources.getIdentifier(openCards[1], "id", packageName)
            val openCard1 = findViewById<AppCompatButton>(openCardId1)
            val openBtn2 = findViewById<AppCompatButton>(openCardId2)
            Handler().postDelayed({
                openCard1?.setBackgroundResource(R.drawable.bg_btn)
                openBtn2?.setBackgroundResource(R.drawable.bg_btn)
                removeListener = false
            }, 1300)
        } else {
            cardsDisableListeners.add(openCards[0])
            cardsDisableListeners.add(openCards[1])
            noOfMatches++
            removeListener = false
        }

        if (noOfMatches == 6) {
            removeListener = true
            isHighScoreBroke = updateScore()
            stopCounting = true
            startWinDialog()
        }
    }

    private fun incrementMoves() {
        if (!stopCounting) {
            motionCount++
            motion!!.text = "Motion : $motionCount"
        }
    }

    private fun timerStart() {
        Handler().postDelayed({
            if (!stopCounting) {
                sec++
            }
            updateTimer()
            timerStart()
        }, 1000)
    }

    private fun updateTimer() {
        if (sec >= 60) {
            min = sec / 60
            secondsToShow = sec % 60
            if (sec > min * 60 && sec < min * 60 + 10 || sec % 60 == 0) {
                playTime!!.text = "Time : $min:0$secondsToShow"
            } else {
                playTime!!.text = "Time : $min:$secondsToShow"
            }
        } else {
            if (sec < 10) {
                playTime!!.text = "Time : $min:0$sec"
            } else {
                playTime!!.text = "Time : $min:$sec"
            }
        }
    }

    private fun startWinDialog() {
        val winDialog = Dialog(this)
        winDialog.setContentView(R.layout.win_dialog)
        winDialog.setCanceledOnTouchOutside(false)
        val window = winDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val winDialogMoves = winDialog.findViewById<TextView>(R.id.winDialogMoves)
        val winDialogTime = winDialog.findViewById<TextView>(R.id.winDialogTime)
        val winDialogRestart = winDialog.findViewById<ImageView>(R.id.winDialogRestart)
        val winDialogHome = winDialog.findViewById<ImageView>(R.id.winDialogHome)
        val newHighScore = winDialog.findViewById<TextView>(R.id.newHighScore)
        if (!isHighScoreBroke) {

            newHighScore.visibility = View.INVISIBLE
        } else {

            newHighScore.visibility = View.VISIBLE
        }
        winDialogMoves.text = motion!!.text
        winDialogTime.text = playTime!!.text
        winDialogRestart.setOnClickListener {
            winDialog.dismiss()
            startActivity(IGameView)
            restartGame()
        }
        winDialogHome.setOnClickListener {
            startActivity(IMain)
            restartGame()

        }
        winDialog.show()
    }

    private fun openPauseDialog() {
        val pauseDialog = Dialog(this)
        pauseDialog.setContentView(R.layout.dialog_pause)
        pauseDialog.setCanceledOnTouchOutside(false)
        val window = pauseDialog.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val resumeBtn = pauseDialog.findViewById<AppCompatButton>(R.id.resumeBtn)
        val restartBtn = pauseDialog.findViewById<AppCompatButton>(R.id.restartBtn)
        val quitBtn = pauseDialog.findViewById<AppCompatButton>(R.id.quitBtn)
        resumeBtn.setOnClickListener {
            pauseDialog.dismiss()
            pauseButton!!.setBackgroundResource(R.drawable.pause)
            stopCounting = false
        }
        restartBtn.setOnClickListener {
            startActivity(IGameView)
            restartGame()
        }
        quitBtn.setOnClickListener {
            startActivity(IMain)
            restartGame()
        }
        pauseDialog.show()
    }

    private fun restartGame() {
        isHighScoreBroke = false
        count = 0
        noOfMatches = 0
        sec = 0
        min = 0
        motionCount = 0
        removeListener = false
        cardsDisableListeners.clear()
    }

    private fun setScore() {
        var highestSecondsStoredToShow = 0
        val preferences: SharedPreferences = getSharedPreferences("Score", MODE_PRIVATE)
        highestMotionStored = preferences.getInt("motion", 0)
        highestTimeStored = preferences.getInt("time", 0)
        highestMinutesStored = highestTimeStored / 60
        highestSecondsStoredToShow = highestTimeStored % 60
        highestMotion!!.text = "Motion : $highestMotionStored"
        if (highestTimeStored > highestMinutesStored * 60 && highestTimeStored < highestMinutesStored * 60 + 10 || highestTimeStored % 60 == 0) {
            highestTime!!.text = "Time : $highestMinutesStored:0$highestSecondsStoredToShow"
        } else {
            highestTime!!.text = "Time : $highestMinutesStored:$highestSecondsStoredToShow"
        }
    }

    private fun updateScore(): Boolean {
        val preferences: SharedPreferences = getSharedPreferences("Score", MODE_PRIVATE)
        if (motionCount <= highestMotionStored || sec <= highestTimeStored || highestTimeStored == 0 && highestMotionStored == 0) {
            val editor = preferences.edit()
            editor.putInt("motion", motionCount)
            editor.putInt("time", sec)
            editor.apply()

            // Сохранение результатов в SharedPreferences в виде строки
            val recordString = "$motionCount, $sec"
            val oldRecords = preferences.getString("recordsList", "")
            val newRecords = if (oldRecords.isNullOrEmpty()) {
                recordString
            } else {
                "$oldRecords;$recordString"
            }
            editor.putString("recordsList", newRecords)
            editor.apply()

            return true
        }
        return false
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer.start()
    }
}