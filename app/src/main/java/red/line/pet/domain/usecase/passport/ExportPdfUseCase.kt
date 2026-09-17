package red.line.pet.domain.usecase.passport

import android.content.Context
import red.line.pet.core.pdf.PdfShareHelper
import red.line.pet.domain.model.MedicalPassportReport
import red.line.pet.domain.model.PassportSectionsConfig
import java.io.File

class ExportPdfUseCase(
    private val generateMedicalPassportUseCase: GenerateMedicalPassportUseCase
) {
    operator fun invoke(
        context: Context,
        report: MedicalPassportReport,
        config: PassportSectionsConfig
    ): File {
        val outputFile = PdfShareHelper.createOutputFile(context, report.pet.name)
        return generateMedicalPassportUseCase(report, config, outputFile)
    }
}
