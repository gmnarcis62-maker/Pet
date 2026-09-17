package red.line.pet.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

object PetImageManager {

    private const val DIRECTORY_NAME = "pet_images"
    private const val MAX_IMAGE_DIMENSION = 1200
    private const val COMPRESS_QUALITY = 85

    fun getImagesDirectory(context: Context): File {
        val dir = File(context.filesDir, DIRECTORY_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getCacheImagesDirectory(context: Context): File {
        val dir = File(context.cacheDir, DIRECTORY_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Creates a temporary file and content Uri for camera capture.
     */
    fun createCameraTempUri(context: Context): Pair<Uri, File> {
        val cacheDir = getCacheImagesDirectory(context)
        val file = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        return Pair(uri, file)
    }

    /**
     * Reads an image from source Uri (Gallery, Camera temp file, etc.),
     * rotates according to EXIF, resizes down to MAX_IMAGE_DIMENSION,
     * compresses to JPEG/WEBP, and stores in internal storage sandbox.
     * Returns the file URI string ("file:///path/to/image.jpg") or null on failure.
     */
    suspend fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // First read bounds
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, options)
                }

                val originalWidth = options.outWidth
                val originalHeight = options.outHeight
                if (originalWidth <= 0 || originalHeight <= 0) {
                    return@withContext null
                }

                // Calculate inSampleSize
                var sampleSize = 1
                val maxDim = max(originalWidth, originalHeight)
                while (maxDim / (sampleSize * 2) >= MAX_IMAGE_DIMENSION) {
                    sampleSize *= 2
                }

                // Decode bitmap with calculated sampleSize
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                val decodedBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, decodeOptions)
                } ?: return@withContext null

                // Detect EXIF orientation
                val rotationDegrees = getExifRotation(context, sourceUri)
                val finalBitmap = if (rotationDegrees != 0) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    val rotated = Bitmap.createBitmap(
                        decodedBitmap, 0, 0,
                        decodedBitmap.width, decodedBitmap.height,
                        matrix, true
                    )
                    if (rotated != decodedBitmap) {
                        decodedBitmap.recycle()
                    }
                    rotated
                } else {
                    decodedBitmap
                }

                // Save to internal app files directory
                val targetDir = getImagesDirectory(context)
                val targetFile = File(targetDir, "pet_${System.currentTimeMillis()}.jpg")

                FileOutputStream(targetFile).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out)
                    out.flush()
                }

                finalBitmap.recycle()

                "file://${targetFile.absolutePath}"
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun getExifRotation(context: Context, uri: Uri): Int {
        return try {
            val input: InputStream? = context.contentResolver.openInputStream(uri)
            input?.use { stream ->
                val exifInterface = ExifInterface(stream)
                when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Deletes a local file if it is in the internal storage directory.
     */
    fun deleteLocalImageIfPresent(imagePath: String) {
        try {
            val cleanPath = imagePath.removePrefix("file://")
            if (cleanPath.contains(DIRECTORY_NAME)) {
                val file = File(cleanPath)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
