package com.hs.accessibility.utils

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun saveImage(bitmap: Bitmap, context: Context, name: String) {
        val contextWrapper = ContextWrapper(context)
        val directory = contextWrapper.getDir(name, Context.MODE_PRIVATE)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN)
        val date = dateFormat.format(Calendar.getInstance().time)

        if (!directory.exists()) directory.mkdir()
        val fileName = "ScreenShotAccess $date .jpg"

        try {
            val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
