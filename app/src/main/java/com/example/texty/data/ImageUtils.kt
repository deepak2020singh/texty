package com.example.texty.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream

object ImageUtils {
    private const val TAG = "ImageUtils"

    @SuppressLint("UseKtx")
    fun uriToBase64(context: Context, uri: Uri, maxSize: Int = 800, quality: Int = 80): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { stream ->
                var bitmap = BitmapFactory.decodeStream(stream)

                if (bitmap.width > maxSize || bitmap.height > maxSize) {
                    val aspectRatio =
                        maxSize.toFloat() / maxOf(bitmap.width, bitmap.height).toFloat()
                    bitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        (bitmap.width * aspectRatio).toInt(),
                        (bitmap.height * aspectRatio).toInt(),
                        true
                    )
                }
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                return Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert URI to Base64", e)
        }
        return null
    }

    fun base64ToBitmap(base64String: String?): Bitmap? {
        if (base64String.isNullOrBlank()) {
            Log.w(TAG, "Base64 string is null or blank")
            return null
        }

        var cleanStr = base64String.trim()

        val prefixJpeg = "data:image/jpeg;base64,"
        val prefixPng = "data:image/png;base64,"
        if (cleanStr.startsWith(prefixJpeg)) {
            cleanStr = cleanStr.substring(prefixJpeg.length)
            Log.d(TAG, "Stripped JPEG Data URI prefix")
        } else if (cleanStr.startsWith(prefixPng)) {
            cleanStr = cleanStr.substring(prefixPng.length)
            Log.d(TAG, "Stripped PNG Data URI prefix")
        }

        cleanStr = cleanStr.replace(Regex("[\n\r\t ]"), "")

        if (!isValidBase64Format(cleanStr)) {
            Log.e(
                TAG,
                "Invalid Base64 format: Length=${cleanStr.length}, First 50 chars: ${
                    cleanStr.take(50)
                }"
            )
            return null
        }

        return try {
            val decodedBytes = Base64.decode(cleanStr, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            if (bitmap != null) bitmap else {
                Log.w(TAG, "Decoded bytes but BitmapFactory failed")
                null
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "DEFAULT decode failed: ${e.message}. Trying URL_SAFE...", e)
            try {
                val decodedBytes = Base64.decode(cleanStr, Base64.URL_SAFE)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                if (bitmap != null) bitmap else {
                    Log.w(TAG, "URL_SAFE decoded bytes but BitmapFactory failed")
                    null
                }
            } catch (e2: Exception) {
                Log.e(TAG, "URL_SAFE decode also failed: ${e2.message}", e2)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in base64ToBitmap", e)
            null
        }
    }


    private fun isValidBase64Format(str: String): Boolean {
        if (str.length % 4 != 0) return false
        val validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
        return str.all { it in validChars }
    }
}