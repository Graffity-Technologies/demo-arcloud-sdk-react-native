package com.graffity.arcloud.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import coil.ImageLoader
import coil.request.SuccessResult

internal suspend fun getBitmapFromURL(url: String, context: Context): Bitmap? {
    Log.d("getBitmapFromURL", url)

    val imageLoader = ImageLoader(context)
    val request: coil.request.ImageRequest = coil.request.ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false) // Disable hardware bitmaps. else get error
        .build()

//    var result: Drawable
    if (imageLoader.execute(request) is SuccessResult) {
        Log.d("getBitmapFromURL", "imageLoader.execute(request) is SuccessResult")
        var result = (imageLoader.execute(request) as SuccessResult).drawable

        return (result as BitmapDrawable).bitmap
    }

    return null
}