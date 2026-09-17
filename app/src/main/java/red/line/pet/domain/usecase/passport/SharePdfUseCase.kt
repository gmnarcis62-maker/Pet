package red.line.pet.domain.usecase.passport

import android.content.Context
import red.line.pet.core.pdf.PdfShareHelper
import java.io.File

class SharePdfUseCase {
    operator fun invoke(context: Context, file: File, petName: String) {
        PdfShareHelper.sharePdf(context, file, petName)
    }

    fun view(context: Context, file: File) {
        PdfShareHelper.viewPdf(context, file)
    }
}
