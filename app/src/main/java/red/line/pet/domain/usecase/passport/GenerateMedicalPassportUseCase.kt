package red.line.pet.domain.usecase.passport

import red.line.pet.core.pdf.MedicalPassportPdfGenerator
import red.line.pet.domain.model.MedicalPassportReport
import red.line.pet.domain.model.PassportSectionsConfig
import java.io.File

class GenerateMedicalPassportUseCase(
    private val pdfGenerator: MedicalPassportPdfGenerator
) {
    operator fun invoke(
        report: MedicalPassportReport,
        config: PassportSectionsConfig,
        outputFile: File
    ): File {
        return pdfGenerator.generatePdf(report, config, outputFile)
    }
}
