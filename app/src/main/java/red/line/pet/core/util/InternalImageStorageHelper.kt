package red.line.pet.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object InternalImageStorageHelper {

    private const val MEMORIES_DIRECTORY = "pet_memories"

    /**
     * Copies and safely compresses an image from Uri (e.g. Photo Picker) into the app's internal storage.
     * Encodes as WebP (or JPEG fallback) to minimize disk usage and avoid external permission issues.
     */
    suspend fun saveImageToInternalStorage(
        context: Context,
        imageUri: Uri,
        maxWidth: Int = 1280,
        maxHeight: Int = 1280,
        quality: Int = 85
    ): String = withContext(Dispatchers.IO) {
        try {
            val directory = File(context.filesDir, MEMORIES_DIRECTORY)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val filename = "memory_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.webp"
            val targetFile = File(directory, filename)

            // Decode dimensions first
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            // Calculate sample size
            var sampleSize = 1
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / sampleSize) >= maxHeight && (halfWidth / sampleSize) >= maxWidth) {
                    sampleSize *= 2
                }
            }

            // Decode with calculated inSampleSize
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val secondInputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(secondInputStream, null, decodeOptions)
            secondInputStream?.close()

            if (originalBitmap != null) {
                val outputStream = FileOutputStream(targetFile)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    originalBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, outputStream)
                } else {
                    @Suppress("DEPRECATION")
                    originalBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
                }
                outputStream.flush()
                outputStream.close()
                originalBitmap.recycle()
                return@withContext targetFile.absolutePath
            }

            // If bitmap decode failed, copy raw stream
            val rawInputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext ""
            val rawFile = File(directory, "raw_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg")
            val rawOutputStream = FileOutputStream(rawFile)
            rawInputStream.copyTo(rawOutputStream)
            rawInputStream.close()
            rawOutputStream.close()
            return@withContext rawFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to Uri string if file save encountered error
            return@withContext imageUri.toString()
        }
    }

    /**
     * Safely deletes a file from internal storage if it exists.
     */
    fun deleteInternalImage(filePath: String): Boolean {
        return try {
            if (filePath.startsWith("/") && !filePath.startsWith("file:///android_asset")) {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                } else false
            } else false
        } catch (e: Exception) {
            false
        }
    }
}
