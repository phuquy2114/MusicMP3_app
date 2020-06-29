package com.uits.music.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.uits.music.common.Constant
import java.util.*

/*
broadcast bắn từ pending intent of notification to PlayMusicActivity
 */
class BroadcastMusic : BroadcastReceiver() {
    var onclickNotifyBroadcast: OnclickNotifyBroadcast? = null
    fun setMyBroadcastCall(myBroadcastCall: OnclickNotifyBroadcast?) {
        onclickNotifyBroadcast = myBroadcastCall
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onReceive(context: Context, intent: Intent) {
        when (Objects.requireNonNull(intent.action)) {
            Constant.BUTTON_PREVIOUS -> onclickNotifyBroadcast!!.onClickPrevious()
            Constant.BUTTON_PLAY -> onclickNotifyBroadcast!!.onClickPlay()
            Constant.BUTTON_NEXT -> onclickNotifyBroadcast!!.onClickNext()
        }
    }

    interface OnclickNotifyBroadcast {
        fun onClickPrevious()
        fun onClickPlay()
        fun onClickNext()
    }
}