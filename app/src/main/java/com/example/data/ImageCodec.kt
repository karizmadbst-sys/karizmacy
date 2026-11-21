package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageCodec {
    private const val MAX_DIMENSION_PX = 640
    private const val MAX_BYTES_TARGET = 75_000

    /**
     * Reads a photo picked via the zero-permission Android Photo Picker, scales it cleanly,
     * compresses it into a compact JPEG data URI (< ~105KB base64 chars) suitable for Firestore storage.
     */
    suspend fun encodeUriToDataUri(context: Context, uri: Uri): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                } ?: return@withContext Result.failure(IllegalArgumentException("Cannot open image"))

                val rawWidth = boundsOptions.outWidth
                val rawHeight = boundsOptions.outHeight
                if (rawWidth <= 0 || rawHeight <= 0) {
                    return@withContext Result.failure(IllegalArgumentException("Invalid image dimensions"))
                }

                var sampleSize = 1
                while (max(rawWidth / sampleSize, rawHeight / sampleSize) > MAX_DIMENSION_PX * 2) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                } ?: return@withContext Result.failure(IllegalArgumentException("Failed to decode image"))

                val scale = minOf(
                    1f,
                    MAX_DIMENSION_PX.toFloat() / max(decoded.width, decoded.height).toFloat()
                )
                val scaled = if (scale < 1f) {
                    val w = max(1, (decoded.width * scale).toInt())
                    val h = max(1, (decoded.height * scale).toInt())
                    Bitmap.createScaledBitmap(decoded, w, h, true).also {
                        if (it != decoded) decoded.recycle()
                    }
                } else {
                    decoded
                }

                val out = ByteArrayOutputStream()
                var quality = 82
                do {
                    out.reset()
                    scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    quality -= 10
                } while (out.size() > MAX_BYTES_TARGET && quality >= 30)

                val bytes = out.toByteArray()
                scaled.recycle()

                val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
                Result.success("data:image/jpeg;base64,$encoded")
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun decodeDataUriToBitmap(uri: String): Bitmap? {
        if (!uri.startsWith("data:image/", ignoreCase = true)) return null
        val commaIdx = uri.indexOf(',')
        if (commaIdx == -1 || commaIdx + 1 >= uri.length) return null
        return try {
            val bytes = Base64.decode(uri.substring(commaIdx + 1), Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}
