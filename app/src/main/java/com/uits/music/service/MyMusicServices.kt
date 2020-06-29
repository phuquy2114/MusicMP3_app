package com.uits.music.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.uits.music.R
import com.uits.music.broadcast.MediaPlayerBroadcast
import com.uits.music.common.Constant
import com.uits.music.model.Music
import com.uits.music.ui.playmusic.PlayMusicActivity
import com.uits.music.utils.BitmapUtil
import java.util.*

class MyMusicServices : Service(), MediaPlayer.OnErrorListener {
    private var oldPosition = 0
    private var position = 0
    private var repeatOne = ""
    private var musicList: MutableList<Music>? = mutableListOf()
    private var broadcast: CurrentTimeBroadcast? = null
    private var notificationLayout: RemoteViews? = null
    private var confirmShuffleOke = ""
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        addMusic()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        broadcast = CurrentTimeBroadcast()
        //get broadcast from Mp3Activity and PlayMusicActivity
        val intentFilter = IntentFilter()
        intentFilter.addAction(CurrentTimeBroadcast.SEND_PLAY_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_PREVIOUS_PLAY_MUSIC_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_NEXT_PLAY_MUSIC_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_PLAY_PLAY_MUSIC_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_SEEK_BAR_MUSIC_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_PLAY_MP3_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_SHUFFLE_MUSIC_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_REPEAT_ONE_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_UN_SHUFFLE_MUSIC_ACTION)
        intentFilter.addAction(CurrentTimeBroadcast.SEND_REPEAT_PLAY_MUSIC_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcast!!, intentFilter)
        broadcast!!.setListener(object : OnClickBroadcast {
            //button play Mp3Activity
            override fun onClickPlay(position: Int) {
                if (mMediaPlayer != null) {
                    if (oldPosition != position) {
                        stopMusic()
                        mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                        mMediaPlayer?.start()
                    } else {
                        if (mMediaPlayer!!.isPlaying) {
                            pauseMusic()
                        } else {
                            resumeMusic()
                        }
                    }
                } else {
                    mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                    mMediaPlayer?.start()
                }
                oldPosition = position
                this@MyMusicServices.position = position
                mediaPlayerCurrentTime()
                initNotifyMusic(position)
            }

            //button Previous PlayMusicActivity and notification
            override fun onClickPreviousMusic() {
                if (mMediaPlayer != null) {
                    if (confirmShuffleOke == "confirmShuffle") {
                        randomPosition()
                    } else {
                        position--
                        if (position < 0) {
                            position = musicList!!.size - 1
                        }
                        if (mMediaPlayer!!.isPlaying) {
                            mMediaPlayer!!.stop()
                        }
                        mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                        mMediaPlayer?.start()
                    }
                } else {
                    mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                    mMediaPlayer?.start()
                }
                sendPosition()
            }

            //button Next PlayMusicActivity and notification
            override fun onClickNextMusic() {
                if (mMediaPlayer != null) {
                    if (confirmShuffleOke == "confirmShuffle") {
                        randomPosition()
                    } else {
                        position++
                        if (position > musicList!!.size - 1) {
                            position = 0
                        }
                        if (mMediaPlayer!!.isPlaying) {
                            mMediaPlayer!!.stop()
                        }
                        mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                        mMediaPlayer?.start()
                    }
                } else {
                    mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                    mMediaPlayer?.start()
                }
                sendPosition()
            }

            //button Play PlayMusicActivity and notification
            override fun onClickPlayMusic() {
                if (mMediaPlayer != null) {
                    if (mMediaPlayer!!.isPlaying) {
                        pauseMusic()
                    } else {
                        resumeMusic()
                    }
                } else {
                    mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                    mMediaPlayer?.start()
                }
                mediaPlayerCurrentTime()
                initNotifyMusic(position)
            }

            //seekBar PlayMusicActivity
            override fun seekBarChange(seekBarChange: Int) {
                mMediaPlayer!!.seekTo(seekBarChange)
            }

            //clickItem Mp3Activity
            override fun onClickPlayMp3(position: Int) {
                if (mMediaPlayer != null) {
                    if (oldPosition != position) {
                        stopMusic()
                        mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                        mMediaPlayer?.start()
                    } else {
                        if (!mMediaPlayer!!.isPlaying) {
                            mMediaPlayer!!.start()
                        }
                    }
                } else {
                    mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                    mMediaPlayer?.start()
                }
                oldPosition = position
                this@MyMusicServices.position = position
                initNotifyMusic(position)
                mediaPlayerCurrentTime()
            }

            //button shuffle
            override fun shufflePlayMusic(confirm: String) {
                //String confirm shuffle
                confirmShuffleOke = confirm
                Log.d(TAG, "shufflePlayMusic:confirm $confirmShuffleOke")
            }

            //button repeatOne Music
            override fun repeatOnePlayMusic(oke: String, pos: Int) {
                //String confirm repeat One
                repeatOne = oke
                Log.d(TAG, "repeatOnePlayMusic: repeat one $repeatOne")
                //position position repeat one music
                position = pos
                Log.d(TAG, "repeatOnePlayMusic: repeat one position $position")
            }

            // button unShuffle
            override fun unShufflePlayMusic() {
                confirmShuffleOke = ""
            }

            //button repeat
            override fun repeatPlayMusic() {
                repeatOne = ""
            }
        })
        return START_NOT_STICKY
    }

    //random position when shuffle play music
    private fun randomPosition() {
        while (true) {
            val random = Random()
            val ranPosition = random.nextInt(musicList!!.size - 1)
            if (position != ranPosition) {
                position = ranPosition
                break
            }
        }
        if (mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
        }
        mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
        mMediaPlayer?.start()
    }

    //send position by broadcast mediaPlayerBroadcast every click next,previous music
    private fun sendPosition() {
        val intentPosition = Intent()
        intentPosition.action = MediaPlayerBroadcast.SEND_POSITION_MUSIC_PLAY
        intentPosition.putExtra(Constant.POSITION_PLAY_MUSIC, position)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentPosition)
        mediaPlayerCurrentTime()
        initNotifyMusic(position)
    }

    //when destroy services , stop music and CurrentBroadcast
    override fun onDestroy() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer!!.stop()
        }
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcast!!)
    }

    // hàm lấy currentPosition overtime
    fun mediaPlayerCurrentTime() {
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentMediaPlayer = mMediaPlayer!!.currentPosition
                val durationMediaPlayer = mMediaPlayer!!.duration
                val intentCurrent = Intent()
                intentCurrent.action = MediaPlayerBroadcast.SEND_TIME_MEDIA_PLAYER
                intentCurrent.putExtra(Constant.CURRENT_KEY, currentMediaPlayer)
                intentCurrent.putExtra(Constant.DURATION_KEY, durationMediaPlayer)
                //send position currentMediaplayer and duration 500/1000 update 1 lần
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentCurrent)
                // kiểm tra thời gian của bài hát khi hết bài --->next
                mMediaPlayer!!.setOnCompletionListener {
                    //repeat one music
                    if (repeatOne == "OKE") {
                        mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                        Log.d(TAG, "onCompletion: position media $position")
                        mMediaPlayer?.start()
                        val durationMediaPlayer = mMediaPlayer?.duration
                        val intentPositionPlayMusic = Intent()
                        intentPositionPlayMusic.action = MediaPlayerBroadcast.SEND_MUSIC_PLAY_AGAIN
                        intentPositionPlayMusic.putExtra(Constant.POSITION_MUSIC_PLAY_KEY, position)
                        intentPositionPlayMusic.putExtra(Constant.DURATION_MUSIC_AGAIN, durationMediaPlayer)
                        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentPositionPlayMusic)
                        initNotifyMusic(position)
                    } else {
                        //shuffle music
                        if (confirmShuffleOke == "confirmShuffle") {
                            while (true) {
                                val random = Random()
                                val ranPosition = random.nextInt(musicList!!.size - 1)
                                if (position != ranPosition) {
                                    position = ranPosition
                                    break
                                }
                            }
                            mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                            mMediaPlayer?.start()
                            val durationMediaPlayer = mMediaPlayer?.duration
                            val intentPositionPlayMusic = Intent()
                            intentPositionPlayMusic.action = MediaPlayerBroadcast.SEND_MUSIC_PLAY_AGAIN
                            intentPositionPlayMusic.putExtra(Constant.POSITION_MUSIC_PLAY_KEY, position)
                            intentPositionPlayMusic.putExtra(Constant.DURATION_MUSIC_AGAIN, durationMediaPlayer)
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentPositionPlayMusic)
                            initNotifyMusic(position)
                        } else {
                            position++
                            if (position > musicList!!.size - 1) {
                                position = 0
                            }
                            if (mMediaPlayer!!.isPlaying) {
                                mMediaPlayer!!.stop()
                            }
                            mMediaPlayer = MediaPlayer.create(applicationContext, musicList!![position].fileSong!!)
                            mMediaPlayer?.start()
                            val durationMediaPlayer = mMediaPlayer?.duration
                            val intentPositionPlayMusic = Intent()
                            intentPositionPlayMusic.action = MediaPlayerBroadcast.SEND_MUSIC_PLAY_AGAIN
                            intentPositionPlayMusic.putExtra(Constant.POSITION_MUSIC_PLAY_KEY, position)
                            intentPositionPlayMusic.putExtra(Constant.DURATION_MUSIC_AGAIN, durationMediaPlayer)
                            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intentPositionPlayMusic)
                            initNotifyMusic(position)
                        }
                    }
                }
                handler.postDelayed(this, 500)
            }
        }, 100)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initNotifyMusic(position: Int) {
        createNotificationChannel()
        notificationLayout = RemoteViews(packageName, R.layout.custum_notifymusic)
        notificationLayout!!.setTextViewText(R.id.txtMusicNameNotify, musicList!![position].musicName)
        notificationLayout!!.setTextViewText(R.id.txtSingerNotify, musicList!![position].musicSinger)
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying) {
                notificationLayout!!.setImageViewResource(R.id.btnPlayNotify, R.drawable.ic_pause_white_48dp)
            } else {
                notificationLayout!!.setImageViewResource(R.id.btnPlayNotify, R.drawable.ic_pause_white_48dp)
            }
        }
        val intent = Intent(applicationContext, PlayMusicActivity::class.java)
        intent.putExtra(Constant.SERVICE_POSITION_NOTIFICATION, position)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val intentPrevious = Intent(Constant.BUTTON_PREVIOUS)
        val pendingIntentPrevious = PendingIntent.getBroadcast(this, 1, intentPrevious, PendingIntent.FLAG_CANCEL_CURRENT)
        val intentPlay = Intent(Constant.BUTTON_PLAY)
        val pendingIntentPlay = PendingIntent.getBroadcast(this, 2, intentPlay, PendingIntent.FLAG_CANCEL_CURRENT)
        val intentNext = Intent(Constant.BUTTON_NEXT)
        val pendingIntentNext = PendingIntent.getBroadcast(this, 3, intentNext, PendingIntent.FLAG_CANCEL_CURRENT)
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icons8_music)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        //pending intent to broadcast
        notificationLayout!!.setOnClickPendingIntent(R.id.btnPreviousNotify, pendingIntentPrevious)
        notificationLayout!!.setOnClickPendingIntent(R.id.btnPlayNotify, pendingIntentPlay)
        notificationLayout!!.setOnClickPendingIntent(R.id.btnNextNotify, pendingIntentNext)
        //chuyển image uri sang bitmap
        Glide.with(this)
                .asBitmap()
                .load(musicList!![position].musicImage)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                        notificationLayout!!.setImageViewBitmap(R.id.imgAvatarNotify, BitmapUtil.getCircleBitmap(resource))
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        startForeground(1, builder)
    }

    private fun addMusic() {
        musicList!!.add(Music("Bạc Phận", "K-ICM ft. JACK", "https://cdn.tuoitre.vn/thumb_w/640/2019/6/19/jack-1560931851558668237008.jpg", R.raw.bac_phan))
        musicList!!.add(Music("Đừng nói tôi điên", "Hiền Hồ", "https://vcdn-ione.vnecdn.net/2018/12/13/43623062-928967060639978-82410-4074-2366-1544693013.png", R.raw.dung_noi_toi_dien))
        musicList!!.add(Music("Em ngày xưa khác rồi", "Hiền Hồ", "https://vcdn-ione.vnecdn.net/2018/12/13/43623062-928967060639978-82410-4074-2366-1544693013.png", R.raw.em_ngay_xua_khac_roi))
        musicList!!.add(Music("Hồng Nhan", "Jack", "https://kenh14cdn.com/zoom/700_438/2019/4/16/520385336113309193300413143308017856937984n-15554316885891494708426-crop-15554316943631888232929.jpg", R.raw.hong_nhan_jack))
        musicList!!.add(Music("Mây và núi", "The Bells", "https://www.pngkey.com/png/detail/129-1296419_cartoon-mountains-png-mountain-animation-png.png", R.raw.may_va_nui))
        musicList!!.add(Music("Rồi người thương cũng hóa người dưng", "Hiền Hồ", "https://vcdn-ione.vnecdn.net/2018/12/13/43623062-928967060639978-82410-4074-2366-1544693013.png", R.raw.roi_nguoi_thuong_cung_hoa_nguoi_dung))
    }

    fun pauseMusic() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
            }
        }
    }

    fun resumeMusic() {
        if (mMediaPlayer != null) {
            if (!mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.start()
            }
        }
    }

    fun stopMusic() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        Toast.makeText(this, "Music player failed", Toast.LENGTH_SHORT).show()
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer!!.stop()
                mMediaPlayer!!.release()
            } finally {
                mMediaPlayer = null
            }
        }
        return false
    }

    interface OnClickBroadcast {
        fun onClickPlay(pos: Int)
        fun onClickPreviousMusic()
        fun onClickNextMusic()
        fun onClickPlayMusic()
        fun seekBarChange(seekBarChange: Int)
        fun onClickPlayMp3(position: Int)
        fun shufflePlayMusic(confirm: String)
        fun repeatOnePlayMusic(oke: String, position: Int)
        fun unShufflePlayMusic()
        fun repeatPlayMusic()
    }

    class CurrentTimeBroadcast : BroadcastReceiver() {
        private var listener: OnClickBroadcast? = null
        fun setListener(listener: OnClickBroadcast?) {
            this.listener = listener
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        override fun onReceive(context: Context, intent: Intent) {
            when (Objects.requireNonNull(intent.action)) {
                SEND_PLAY_ACTION -> {
                    val pos = intent.getIntExtra(Constant.POS_KEY, 0)
                    listener!!.onClickPlay(pos)
                }
                SEND_PREVIOUS_PLAY_MUSIC_ACTION -> listener!!.onClickPreviousMusic()
                SEND_NEXT_PLAY_MUSIC_ACTION -> listener!!.onClickNextMusic()
                SEND_PLAY_PLAY_MUSIC_ACTION -> listener!!.onClickPlayMusic()
                SEND_SEEK_BAR_MUSIC_ACTION -> {
                    val seekbarChange = intent.getIntExtra(Constant.SEEK_BAR_PLAY_MUSIC, 0)
                    listener!!.seekBarChange(seekbarChange)
                }
                SEND_PLAY_MP3_ACTION -> {
                    val positionMp3 = intent.getIntExtra(Constant.POSITION_PLAY_MP3, 0)
                    listener!!.onClickPlayMp3(positionMp3)
                }
                SEND_SHUFFLE_MUSIC_ACTION -> {
                    val confirmShuffle = intent.getStringExtra(Constant.CONFIRM_SHUFFLE_OKE)
                    listener!!.shufflePlayMusic(confirmShuffle)
                }
                SEND_REPEAT_ONE_ACTION -> {
                    val repeatOne = intent.getStringExtra(Constant.OKE_REPEAT_ONE)
                    val positionRepeatOne = intent.getIntExtra(Constant.POSITION_REPEAT_ONE, 0)
                    listener!!.repeatOnePlayMusic(repeatOne, positionRepeatOne)
                }
                SEND_UN_SHUFFLE_MUSIC_ACTION -> listener!!.unShufflePlayMusic()
                SEND_REPEAT_PLAY_MUSIC_ACTION -> listener!!.repeatPlayMusic()
            }
        }

        companion object {
            const val SEND_PLAY_ACTION = "play_action"
            const val SEND_PREVIOUS_PLAY_MUSIC_ACTION = "previous_play_music_action"
            const val SEND_NEXT_PLAY_MUSIC_ACTION = "next_play_music_action"
            const val SEND_PLAY_PLAY_MUSIC_ACTION = "play_play_music_action"
            const val SEND_SEEK_BAR_MUSIC_ACTION = "seeBar_music_action"
            const val SEND_PLAY_MP3_ACTION = "send_play_mp3_action"
            const val SEND_SHUFFLE_MUSIC_ACTION = "send_shuffle_music_action"
            const val SEND_REPEAT_ONE_ACTION = "send_repeat_one_action"
            const val SEND_UN_SHUFFLE_MUSIC_ACTION = "send_repeat_music_action"
            const val SEND_REPEAT_PLAY_MUSIC_ACTION = "send_repeat_play_music_action"
        }
    }

    companion object {
        private const val TAG = "MyMusicServices"
        private const val CHANNEL_ID = "channel_id"
        var mMediaPlayer: MediaPlayer? = null
    }
}