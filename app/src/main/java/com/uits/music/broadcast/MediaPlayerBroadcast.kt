package com.uits.music.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.uits.music.common.Constant
import com.uits.music.ui.playmusic.PlayMusicActivity
import java.util.*

class MediaPlayerBroadcast : BroadcastReceiver() {
    companion object {
        const val SEND_TIME_MEDIA_PLAYER = "time_mediaPlayer"
        const val SEND_MUSIC_PLAY_AGAIN = "send_music_play_again"
        const val SEND_POSITION_MUSIC_PLAY = "send_position_play_music"
    }

    private var takeMediaPlayer: PlayMusicActivity.TakeMediaPlayer? = null

    fun setTakeMediaPlayer(takeMediaPlayer: PlayMusicActivity.TakeMediaPlayer?) {
        this.takeMediaPlayer = takeMediaPlayer
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            when (Objects.requireNonNull(intent.action)) {
                SEND_TIME_MEDIA_PLAYER -> {
                    val current = intent.getIntExtra(Constant.CURRENT_KEY, 0)
                    val durationMedia = intent.getIntExtra(Constant.DURATION_KEY, 0)
                    takeMediaPlayer!!.takeCurrentMediaPlayer(current, durationMedia)
                }
                SEND_MUSIC_PLAY_AGAIN -> {
                    val positionMusic = intent.getIntExtra(Constant.POSITION_MUSIC_PLAY_KEY, 0)
                    val durationMusic = intent.getIntExtra(Constant.DURATION_MUSIC_AGAIN, 0)
                    takeMediaPlayer!!.playMusicAgain(positionMusic, durationMusic)
                }
                SEND_POSITION_MUSIC_PLAY -> {
                    val position = intent.getIntExtra(Constant.POSITION_PLAY_MUSIC, 0)
                    takeMediaPlayer!!.takePositionMusic(position)
                }
            }
        }
    }


}