package com.howell.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

object ScaleImageUtils {
    // decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
    fun decodeFile(requiredWidthSize: Int, requiredHeightSize: Int, f: File): Bitmap? {
        try {
            // decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, o)

            // Find the correct scale value. It should be the power of 2.
            //final int REQUIRED_SIZE = 70;
            var width_tmp = o.outWidth
            var height_tmp = o.outHeight

            var scale = 1
            while (true) {
                if (width_tmp / 2 < requiredWidthSize || height_tmp / 2 < requiredHeightSize)
                    break
                width_tmp /= 2
                height_tmp /= 2
                scale *= 2
            }

            // decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            return BitmapFactory.decodeStream(FileInputStream(f), null, o2)
        } catch (e: FileNotFoundException) {
        }

        return null
    }

    fun decodeByteArray(requiredWidthSize: Int, requiredHeightSize: Int, data: ByteArray): Bitmap {
        // decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, o)

        // Find the correct scale value. It should be the power of 2.
        //final int REQUIRED_SIZE = 70;
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1
        while (true) {
            if (width_tmp < requiredWidthSize || height_tmp < requiredHeightSize)
                break
            println("缩小")
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2

        }

        // decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        o2.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(data, 0, data.size, o2)
    }

    fun zoomInFile(requiredWidthSize: Int, requiredHeightSize: Int, path: String): Bitmap {
        // decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, o)

        // Find the correct scale value. It should be the power of 2.
        //final int REQUIRED_SIZE = 70;
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight

        Log.e("", "放大之前的宽：$width_tmp,放大之前的高：$height_tmp")
        var scale = 1
        while (true) {
            if (width_tmp < requiredWidthSize || height_tmp < requiredHeightSize)
                break
            width_tmp *= 2
            height_tmp *= 2
            scale /= 2
        }

        // decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        val bmp = BitmapFactory.decodeFile(path, o2)
        Log.e("", "压缩之后的宽：$width_tmp,压缩之后的高：$height_tmp")
        return bmp
    }

    fun resizeImage(bitmap: Bitmap, w: Int, h: Int): Bitmap {

        // load the origial Bitmap

        val width = bitmap.width
        val height = bitmap.height

        // calculate the scale
        val scaleWidth = w.toFloat() / width
        val scaleHeight = h.toFloat() / height

        // create a matrix for the manipulation
        val matrix = Matrix()
        // resize the Bitmap
        matrix.postScale(scaleWidth, scaleHeight)
        // if you want to rotate the Bitmap
        // recreate the new Bitmap

        return Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true)

    }

}
