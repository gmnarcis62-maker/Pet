package red.line.pet.core.pdf

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PdfShareHelper {

    /**
     * Prepares the target output file in the app's internal reports directory.
     * Name format: Petora_<PetName>_Medical_Report.pdf
     */
    fun createOutputFile(context: Context, petName: String): File {
        val safePetName = petName.trim()
            .replace(Regex("[\\\\/:*?\"<>|\\s]"), "_")
            .ifBlank { "Pet" }

        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val fileName = "Petora_${safePetName}_Medical_Report.pdf"
        return File(reportsDir, fileName)
    }

    /**
     * Gets a secure content Uri for the PDF file via FileProvider.
     */
    fun getPdfUri(context: Context, file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Opens the PDF in a viewer app.
     */
    fun viewPdf(context: Context, file: File) {
        try {
            val uri = getPdfUri(context, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "برنامه‌ای برای مشاهده فایل PDF یافت نشد.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در باز کردن فایل: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares the PDF file via Android's native share sheet.
     */
    fun sharePdf(context: Context, file: File, petName: String) {
        try {
            val uri = getPdfUri(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "شناسنامه سلامت پتورا - $petName")
                putExtra(Intent.EXTRA_TEXT, "شناسنامه رسمی سلامت و پرونده پزشکی $petName تولید شده توسط پتورا")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "اشتراک‌گذاری شناسنامه سلامت $petName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اشتراک‌گذاری فایل: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
