package com.uits.music.model

import android.os.Parcel
import android.os.Parcelable

data class Music(
        var musicName: String? = null,
        var musicSinger: String? = null,
        var musicImage: String? = null,
        var fileSong: Int? = 0,
        var isPlay: Boolean = false) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Int::class.java.classLoader) as? Int,
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(musicName)
        parcel.writeString(musicSinger)
        parcel.writeString(musicImage)
        parcel.writeValue(fileSong)
        parcel.writeByte(if (isPlay) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }
}