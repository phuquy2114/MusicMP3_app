package com.uits.music.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uits.baseproject.base.BaseActivity
import com.uits.music.R
import com.uits.music.common.Constant
import com.uits.music.model.Music
import com.uits.music.service.MyMusicServices
import com.uits.music.ui.MusicAdapter
import com.uits.music.ui.playmusic.PlayMusicActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseActivity(), MusicAdapter.OnClickItemMusicListener {
    private var musicList: MutableList<Music>? = mutableListOf()
    private var adapter: MusicAdapter? = null


    override fun getLayoutId(): Int {
      return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = MusicAdapter(this)

        mRecycleView.adapter = adapter
        mRecycleView.setHasFixedSize(true)
        mRecycleView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        addMusic()
        //when start app, init services
        val intentService = Intent(this, MyMusicServices::class.java)
        startService(intentService)
        adapter!!.setOnClickItemMusicListener(this)
    }

    private fun addMusic() {
        musicList!!.add(Music("Bạc Phận", "K-ICM ft. JACK", "https://cdn.tuoitre.vn/thumb_w/640/2019/6/19/jack-1560931851558668237008.jpg"))
        musicList!!.add(Music("Đừng nói tôi điên", "Hiền Hồ", "https://vcdn-ione.vnecdn.net/2018/12/13/43623062-928967060639978-82410-4074-2366-1544693013.png"))
        musicList!!.add(Music("Em ngày xưa khác rồi", "Hiền Hồ", "https://vcdn-ione.vnecdn.net/2018/12/13/43623062-928967060639978-82410-4074-2366-1544693013.png"))
        musicList!!.add(Music("Hồng Nhan", "Jack", "https://kenh14cdn.com/zoom/700_438/2019/4/16/520385336113309193300413143308017856937984n-15554316885891494708426-crop-15554316943631888232929.jpg"))
        musicList!!.add(Music("Mây và núi", "The Bells", "https://www.pngkey.com/png/detail/129-1296419_cartoon-mountains-png-mountain-animation-png.png"))
        musicList!!.add(Music("Rồi người thương cũng hóa người dưng", "Hiền Hồ", "https://vcdn-ione.vnecdn.net/2018/12/13/43623062-928967060639978-82410-4074-2366-1544693013.png"))
        adapter!!.addMusicList(musicList)
    }

    //when click item recyclerView
    override fun onclickItem(position: Int, music: Music) {
        val intent = Intent(this@MainActivity, PlayMusicActivity::class.java)
        val bundle = Bundle()
        bundle.putInt(Constant.POSITION_KEY, position)
        intent.putExtra(Constant.BUNDLE_KEY, bundle)
        //send broadcastCurrentTimeBroadcast position,nơi nhận broadcast là services
        val intentPlayMusic = Intent()
        intentPlayMusic.action = MyMusicServices.CurrentTimeBroadcast.SEND_PLAY_MP3_ACTION
        intentPlayMusic.putExtra(Constant.POSITION_PLAY_MP3, position)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentPlayMusic)
        startActivityForResult(intent, REQUEST_CODE_PLAY_MUSIC)
    }

    //when click button play
    override fun onClickButtonPlay(music: Music, position: Int) {
        checkPlayMusic(music)
        //send position qua broadcast nơi nhân là service
        val intent = Intent()
        intent.action = MyMusicServices.CurrentTimeBroadcast.SEND_PLAY_ACTION
        intent.putExtra(Constant.POS_KEY, position)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        adapter!!.notifyDataSetChanged()
    }

    //get data when back from PlayMusicActivity to Mp3Activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PLAY_MUSIC && resultCode == Activity.RESULT_OK && data != null) {
            val music: Music = data.getParcelableExtra(Constant.POSITION_RESULTS)
            checkPlayMusicState(music)
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun checkPlayMusic(music: Music) {
        for (i in 0 until musicList!!.size){
            if (musicList!![i].musicName == music.musicName){
                musicList!![i].isPlay = !music.isPlay
            }else{
                musicList!![i].isPlay = false
            }
        }
    }
    private fun checkPlayMusicState(music: Music) {
        for (i in 0 until musicList!!.size){
            if (musicList!![i].musicName == music.musicName){
                musicList!![i].isPlay = music.isPlay
            }else{
                musicList!![i].isPlay = false
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    companion object {
        private const val TAG = "Mp3Activity"
        private const val REQUEST_CODE_PLAY_MUSIC = 1001
    }
}