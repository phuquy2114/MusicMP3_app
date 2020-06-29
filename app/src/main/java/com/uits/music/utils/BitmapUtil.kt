package com.uits.music.utils

import android.graphics.*

object BitmapUtil {
    fun getCircleBitmap(bitmap: Bitmap): Bitmap {
        val tmp: Bitmap
        val srcRect: Rect
        val dstRect: Rect
        val r = 50f
        val width = bitmap.width
        val height = bitmap.height
        val output: Bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        if (width > height) {
            tmp = Bitmap.createScaledBitmap(bitmap, 100 * width / height, 100, false)
            val left = (tmp.width - tmp.height) / 2
            val right = left + tmp.height
            srcRect = Rect(left, 0, right, tmp.height)
            dstRect = Rect(0, 0, tmp.height, tmp.height)
        } else {
            tmp = Bitmap.createScaledBitmap(bitmap, 100, 100 * height / width, false)
            val top = (tmp.height - tmp.width) / 2
            val bottom = top + tmp.width
            srcRect = Rect(0, top, tmp.width, bottom)
            dstRect = Rect(0, 0, tmp.width, tmp.width)
        }
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(tmp, srcRect, dstRect, paint)
        return output
    }
}