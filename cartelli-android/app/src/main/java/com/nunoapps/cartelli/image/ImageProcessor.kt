package com.nunoapps.cartelli.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

/** Carica la foto del prodotto, la ridimensiona e corregge l'orientamento EXIF. */
object ImageProcessor {

    /** Carica una bitmap da [uri] limitandone il lato massimo a [maxDim] px. */
    fun loadScaled(context: Context, uri: Uri, maxDim: Int = 1600): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        val maxSrc = maxOf(bounds.outWidth, bounds.outHeight)
        while (maxSrc / sample > maxDim) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null

        val rotation = readRotation(context, uri)
        return if (rotation != 0f) rotate(bmp, rotation) else bmp
    }

    private fun readRotation(context: Context, uri: Uri): Float {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input: InputStream ->
                when (ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (_: Exception) {
            0f
        }
    }

    private fun rotate(bmp: Bitmap, degrees: Float): Bitmap {
        val m = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }
}
