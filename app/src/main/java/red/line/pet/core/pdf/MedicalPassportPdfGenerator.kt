package red.line.pet.core.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.MedicalPassportReport
import red.line.pet.domain.model.PassportSectionsConfig
import red.line.pet.domain.model.WeightRecord
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

/**
 * High-craft, offline Native PDF generator for the Petora Medical Passport.
 * Generates official, multi-page, veterinary-ready Persian/RTL medical passports
 * with luxury Petora styling (Burgundy, Gold, Warm Beige).
 */
class MedicalPassportPdfGenerator(
    private val context: Context
) {
    // Standard A4 dimensions in Points (72 DPI)
    private val pageWidth = 595
    private val pageHeight = 842

    private val marginHorizontal = 36f
    private val contentWidth = pageWidth - (marginHorizontal * 2)
    private val rightEdge = pageWidth - marginHorizontal
    private val leftEdge = marginHorizontal
    private val marginTop = 36f
    private val marginBottom = 40f
    private val printableBottom = pageHeight - marginBottom

    // Luxury Palette
    private val colorBurgundy = Color.rgb(107, 15, 36)      // #6B0F24
    private val colorBurgundyDark = Color.rgb(74, 10, 25)   // #4A0A19
    private val colorGold = Color.rgb(212, 175, 55)         // #D4AF37
    private val colorGoldLight = Color.rgb(244, 232, 194)   // #F4E8C2
    private val colorBeige = Color.rgb(253, 251, 247)       // #FDFBF7
    private val colorBeigeCard = Color.rgb(247, 243, 237)   // #F7F3ED
    private val colorBorder = Color.rgb(224, 216, 208)      // #E0D8D0
    private val colorDarkText = Color.rgb(26, 26, 26)       // #1A1A1A
    private val colorMutedText = Color.rgb(102, 102, 102)   // #666666
    private val colorWhite = Color.WHITE
    private val colorSuccess = Color.rgb(46, 125, 50)       // #2E7D32

    // Paints
    private val bgPaint = Paint().apply { isAntiAlias = true }
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }
    private val textPaint = TextPaint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    /**
     * Generates a complete PDF document and saves it to the target file.
     */
    fun generatePdf(
        report: MedicalPassportReport,
        config: PassportSectionsConfig,
        outputFile: File
    ): File {
        val document = PdfDocument()

        try {
            // First pass: We render the pages.
            // Page 1: Official Luxury Cover & Pet Identity
            renderCoverPage(document, report, 1)

            // Dynamic continuation pages for chosen sections
            var currentPageNumber = 2
            var currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var currentPage = document.startPage(currentPageInfo)
            var currentCanvas = currentPage.canvas
            var currentY = marginTop + 45f // Leave room for running header

            drawRunningHeader(currentCanvas, report)

            // Function to check if we need to break page
            fun ensureSpace(neededHeight: Float) {
                if (currentY + neededHeight > printableBottom) {
                    drawRunningFooter(currentCanvas, currentPageNumber)
                    document.finishPage(currentPage)

                    currentPageNumber++
                    currentPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    currentPage = document.startPage(currentPageInfo)
                    currentCanvas = currentPage.canvas
                    drawRunningHeader(currentCanvas, report)
                    currentY = marginTop + 45f
                }
            }

            // 1. Health Records Section
            if (config.includeHealth) {
                val records = report.healthRecords
                ensureSpace(70f)
                drawSectionBanner(currentCanvas, "سوابق واکسیناسیون و پرونده سلامت", "واکسن، دارو، چکاپ و جراحی", currentY)
                currentY += 40f

                if (records.isEmpty()) {
                    ensureSpace(40f)
                    drawEmptyBox(currentCanvas, "هیچ سابقه پزشکی یا واکسیناسیونی ثبت نشده است.", currentY)
                    currentY += 45f
                } else {
                    val rowHeight = 32f
                    // Table Header
                    ensureSpace(rowHeight + 35f)
                    drawHealthTableHeader(currentCanvas, currentY)
                    currentY += 24f

                    records.forEachIndexed { index, record ->
                        ensureSpace(rowHeight)
                        drawHealthTableRow(currentCanvas, record, currentY, index % 2 == 0)
                        currentY += rowHeight
                    }
                    currentY += 15f
                }
            }

            // 2. Weight Tracking Section
            if (config.includeWeight) {
                ensureSpace(120f)
                drawSectionBanner(currentCanvas, "پایش وزن و شاخص رشد", "روند تغییرات و آمار وزن حیوان", currentY)
                currentY += 40f

                // Weight summary stat cards
                drawWeightStatCards(currentCanvas, report, currentY)
                currentY += 65f

                // Weight Chart if multiple records exist
                if (report.weightRecords.size >= 2) {
                    ensureSpace(150f)
                    drawWeightChart(currentCanvas, report.weightRecords, currentY, 130f)
                    currentY += 140f
                } else if (report.weightRecords.size == 1) {
                    ensureSpace(40f)
                    drawInfoNotice(currentCanvas, "تنها یک سابقه وزن ثبت شده است. برای رسم نمودار، رکوردهای بیشتری ثبت کنید.", currentY)
                    currentY += 45f
                }
                currentY += 10f
            }

            // 3. Nutrition & Food Schedule Section
            if (config.includeFood) {
                val schedules = report.foodSchedules
                ensureSpace(70f)
                drawSectionBanner(currentCanvas, "برنامه تغذیه و رژیم غذایی", "وعده‌های فعال، میزان مصرف و مکمل‌ها", currentY)
                currentY += 40f

                if (schedules.isEmpty()) {
                    ensureSpace(40f)
                    drawEmptyBox(currentCanvas, "برنامه غذایی فعالی برای این حیوان تنظیم نشده است.", currentY)
                    currentY += 45f
                } else {
                    val rowHeight = 30f
                    ensureSpace(rowHeight + 35f)
                    drawFoodTableHeader(currentCanvas, currentY)
                    currentY += 24f

                    schedules.forEachIndexed { index, schedule ->
                        ensureSpace(rowHeight)
                        drawFoodTableRow(currentCanvas, schedule, currentY, index % 2 == 0)
                        currentY += rowHeight
                    }
                    currentY += 15f
                }
            }

            // 4. Reminders Section
            if (config.includeReminders) {
                val reminders = report.reminders
                ensureSpace(70f)
                drawSectionBanner(currentCanvas, "یادآورها و مراقبت‌های پیش‌رو", "نوبت‌های واکسن، دارو و بهداشت", currentY)
                currentY += 40f

                if (reminders.isEmpty()) {
                    ensureSpace(40f)
                    drawEmptyBox(currentCanvas, "هیچ یادآور یا نوبت فعالی ثبت نشده است.", currentY)
                    currentY += 45f
                } else {
                    val rowHeight = 28f
                    ensureSpace(rowHeight + 35f)
                    drawReminderTableHeader(currentCanvas, currentY)
                    currentY += 24f

                    reminders.forEachIndexed { index, rem ->
                        ensureSpace(rowHeight)
                        drawReminderTableRow(currentCanvas, rem, currentY, index % 2 == 0)
                        currentY += rowHeight
                    }
                    currentY += 15f
                }
            }

            // 5. Memories Photo Gallery Section
            if (config.includeMemories && report.memories.isNotEmpty()) {
                ensureSpace(160f)
                drawSectionBanner(currentCanvas, "آلبوم لحظات و خاطرات برگزیده", "منتخبی از تصاویر و خاطرات ثبت‌شده", currentY)
                currentY += 40f

                val drawnHeight = drawMemoriesGrid(currentCanvas, report.memories.take(4), currentY)
                currentY += drawnHeight + 15f
            }

            // 6. Expenses Section
            if (config.includeExpenses) {
                ensureSpace(90f)
                drawSectionBanner(currentCanvas, "خلاصه هزینه‌های پزشکی و مراقبت", "مجموع مخارج و تفکیک سلامت", currentY)
                currentY += 40f

                drawExpensesSummaryCards(currentCanvas, report, currentY)
                currentY += 60f

                if (report.expenses.isNotEmpty()) {
                    val recentExpenses = report.expenses.take(5)
                    val rowHeight = 26f
                    ensureSpace(rowHeight * recentExpenses.size + 40f)
                    drawExpenseTableHeader(currentCanvas, currentY)
                    currentY += 22f

                    recentExpenses.forEachIndexed { index, exp ->
                        ensureSpace(rowHeight)
                        drawExpenseTableRow(currentCanvas, exp, currentY, index % 2 == 0)
                        currentY += rowHeight
                    }
                    currentY += 15f
                }
            }

            // Finish the last page
            drawRunningFooter(currentCanvas, currentPageNumber)
            document.finishPage(currentPage)

            // Write to file safely
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { fos ->
                document.writeTo(fos)
            }

            return outputFile
        } finally {
            document.close()
        }
    }

    // =========================================================================
    // PAGE 1: COVER PAGE
    // =========================================================================

    private fun renderCoverPage(document: PdfDocument, report: MedicalPassportReport, pageNumber: Int) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // Background Warm Beige
        bgPaint.color = colorBeige
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // Outer Luxury Gold Border
        strokePaint.color = colorGold
        strokePaint.strokeWidth = 2.5f
        canvas.drawRoundRect(
            RectF(marginHorizontal - 8f, marginTop - 8f, rightEdge + 8f, printableBottom + 8f),
            12f, 12f, strokePaint
        )

        // Inner Thin Border
        strokePaint.strokeWidth = 0.8f
        strokePaint.color = colorBurgundy
        canvas.drawRoundRect(
            RectF(marginHorizontal - 4f, marginTop - 4f, rightEdge + 4f, printableBottom + 4f),
            10f, 10f, strokePaint
        )

        // Top Luxury Header Banner (Burgundy)
        val bannerTop = marginTop + 10f
        val bannerHeight = 85f
        bgPaint.color = colorBurgundy
        canvas.drawRoundRect(
            RectF(marginHorizontal, bannerTop, rightEdge, bannerTop + bannerHeight),
            8f, 8f, bgPaint
        )

        // Gold decorative accent stripe at bottom of banner
        bgPaint.color = colorGold
        canvas.drawRect(marginHorizontal, bannerTop + bannerHeight - 4f, rightEdge, bannerTop + bannerHeight, bgPaint)

        // Header Titles
        textPaint.color = colorGold
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("PETORA OFFICIAL VETERINARY HEALTH PASSPORT", pageWidth / 2f, bannerTop + 24f, textPaint)

        textPaint.color = colorWhite
        textPaint.textSize = 20f
        textPaint.isFakeBoldText = true
        canvas.drawText("شناسنامه رسمی سلامت و پرونده پزشکی", pageWidth / 2f, bannerTop + 52f, textPaint)

        textPaint.color = colorGoldLight
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = false
        canvas.drawText("سامانه هوشمند پایش و مراقبت سلامت حیوانات خانگی پتورا", pageWidth / 2f, bannerTop + 72f, textPaint)

        // Pet Profile Photo & Basic Identity Layout
        var currentY = bannerTop + bannerHeight + 25f

        // Pet Photo / Avatar Box
        val photoSize = 100f
        val photoLeft = marginHorizontal + 20f
        val photoTop = currentY
        val photoRect = RectF(photoLeft, photoTop, photoLeft + photoSize, photoTop + photoSize)

        // Photo circle/rounded card
        bgPaint.color = colorBeigeCard
        canvas.drawRoundRect(photoRect, 14f, 14f, bgPaint)
        strokePaint.color = colorGold
        strokePaint.strokeWidth = 2f
        canvas.drawRoundRect(photoRect, 14f, 14f, strokePaint)

        // Load Pet Bitmap or Draw Species Icon
        val petBitmap = loadPetBitmap(report.pet.imagePath.ifBlank { report.pet.avatarUri })
        if (petBitmap != null) {
            val srcRect = Rect(0, 0, petBitmap.width, petBitmap.height)
            val destRect = Rect(
                (photoLeft + 4f).toInt(),
                (photoTop + 4f).toInt(),
                (photoLeft + photoSize - 4f).toInt(),
                (photoTop + photoSize - 4f).toInt()
            )
            canvas.drawBitmap(petBitmap, srcRect, destRect, null)
        } else {
            // Species Emoji / Symbol
            textPaint.color = colorBurgundy
            textPaint.textSize = 34f
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(report.pet.species.emoji, photoRect.centerX(), photoRect.centerY() + 12f, textPaint)
        }

        // Pet Name & Species Badge to the right of Photo
        val nameRight = rightEdge - 15f
        textPaint.color = colorBurgundy
        textPaint.textSize = 22f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(report.pet.name, nameRight, photoTop + 32f, textPaint)

        // Species & Breed
        textPaint.color = colorMutedText
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = false
        val breedText = if (report.pet.breed.isNotBlank()) "نژاد: ${report.pet.breed}" else "نژاد نامشخص"
        canvas.drawText("${report.pet.species.titleFa} | $breedText", nameRight, photoTop + 54f, textPaint)

        // Gender & Age Chip
        val genderAge = "${report.pet.gender.titleFa} • سن: ${report.ageStringFa}"
        textPaint.color = colorBurgundyDark
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = true
        canvas.drawText(genderAge, nameRight, photoTop + 76f, textPaint)

        currentY += photoSize + 25f

        // Table / Grid of Official Details
        drawIdentityDetailsTable(canvas, report, currentY)
        currentY += 210f

        // Clinic & Veterinary Certification Box
        val certBoxTop = currentY + 15f
        val certBoxHeight = 135f
        val certRect = RectF(marginHorizontal, certBoxTop, rightEdge, certBoxTop + certBoxHeight)

        bgPaint.color = colorBeigeCard
        canvas.drawRoundRect(certRect, 8f, 8f, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 1f
        canvas.drawRoundRect(certRect, 8f, 8f, strokePaint)

        // Title of certification box
        textPaint.color = colorBurgundy
        textPaint.textSize = 11f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("بخش تاییدیه و یادداشت کلینیک دامپزشکی:", rightEdge - 15f, certBoxTop + 24f, textPaint)

        // Dotted lines for vet notes
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        strokePaint.pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)

        val line1Y = certBoxTop + 48f
        canvas.drawLine(marginHorizontal + 15f, line1Y, rightEdge - 15f, line1Y, strokePaint)
        val line2Y = certBoxTop + 72f
        canvas.drawLine(marginHorizontal + 15f, line2Y, rightEdge - 15f, line2Y, strokePaint)

        strokePaint.pathEffect = null // reset path effect

        // Signature & Stamp spots
        textPaint.color = colorMutedText
        textPaint.textSize = 9.5f
        textPaint.isFakeBoldText = false
        canvas.drawText("نام و امضای دامپزشک:", rightEdge - 20f, certBoxTop + 112f, textPaint)

        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("مهر و تایید کلینیک:", marginHorizontal + 20f, certBoxTop + 112f, textPaint)

        // Bottom Cover Footer
        drawRunningFooter(canvas, 1)

        document.finishPage(page)
    }

    private fun drawIdentityDetailsTable(canvas: Canvas, report: MedicalPassportReport, startY: Float) {
        val pet = report.pet
        val tableRect = RectF(marginHorizontal, startY, rightEdge, startY + 195f)

        // Card Container
        bgPaint.color = colorWhite
        canvas.drawRoundRect(tableRect, 8f, 8f, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 1f
        canvas.drawRoundRect(tableRect, 8f, 8f, strokePaint)

        // Table Header
        val headerHeight = 28f
        bgPaint.color = colorGoldLight
        canvas.drawRoundRect(RectF(marginHorizontal, startY, rightEdge, startY + headerHeight), 8f, 8f, bgPaint)
        // flatten bottom corners
        canvas.drawRect(marginHorizontal, startY + 15f, rightEdge, startY + headerHeight, bgPaint)

        textPaint.color = colorBurgundy
        textPaint.textSize = 11.5f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("مشخصات هویتی و ثبت در سامانه", rightEdge - 15f, startY + 18f, textPaint)

        // Grid Rows (Two Columns)
        val colWidth = contentWidth / 2f
        val col1Right = rightEdge - 15f
        val col2Right = rightEdge - colWidth - 10f
        var rowY = startY + headerHeight + 22f
        val rowGap = 26f

        fun drawField(label: String, value: String, rightX: Float, y: Float) {
            textPaint.color = colorMutedText
            textPaint.textSize = 10f
            textPaint.isFakeBoldText = false
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$label: ", rightX, y, textPaint)

            val labelWidth = textPaint.measureText("$label: ")
            textPaint.color = colorDarkText
            textPaint.isFakeBoldText = true
            canvas.drawText(value, rightX - labelWidth, y, textPaint)
        }

        // Row 1
        drawField("نام حیوان", pet.name, col1Right, rowY)
        drawField("گونه", "${pet.species.emoji} ${pet.species.titleFa}", col2Right, rowY)

        // Row 2
        rowY += rowGap
        drawField("نژاد", pet.breed.ifBlank { "ثبت نشده" }, col1Right, rowY)
        drawField("جنسیت", pet.gender.titleFa, col2Right, rowY)

        // Row 3
        rowY += rowGap
        val birthStr = pet.birthDateJalali.ifBlank { pet.birthDate }.ifBlank { "ثبت نشده" }
        drawField("تاریخ تولد", birthStr, col1Right, rowY)
        drawField("سن تقریبی", report.ageStringFa, col2Right, rowY)

        // Row 4
        rowY += rowGap
        val currentWeight = if (report.weightStats.currentWeightKg > 0) {
            "${PersianNumberFormatter.toPersian(report.weightStats.currentWeightKg)} کیلوگرم"
        } else {
            "ثبت نشده"
        }
        drawField("وزن فعلی", currentWeight, col1Right, rowY)
        drawField("رنگ / علامت خاص", pet.color.ifBlank { "ثبت نشده" }, col2Right, rowY)

        // Row 5
        rowY += rowGap
        drawField("شماره میکروچیپ", pet.microchipId.ifBlank { "ثبت نشده" }, col1Right, rowY)
        drawField("تاریخ صدور گزارش", report.generatedAtJalali, col2Right, rowY)

        // Row 6: Notes
        rowY += rowGap
        val notes = pet.notes.ifBlank { "توضیح خاصی ثبت نشده است." }
        drawField("یادداشت مالک", notes.take(55), col1Right, rowY)
    }

    // =========================================================================
    // SECTION HEADERS & RUNNING HEADERS
    // =========================================================================

    private fun drawRunningHeader(canvas: Canvas, report: MedicalPassportReport) {
        val headerY = marginTop + 14f

        textPaint.color = colorBurgundy
        textPaint.textSize = 10f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("پتورا | شناسنامه سلامت: ${report.pet.name} (${report.pet.species.titleFa})", rightEdge, headerY, textPaint)

        textPaint.color = colorMutedText
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(report.generatedAtJalali, leftEdge, headerY, textPaint)

        // Thin Gold Accent Line
        strokePaint.color = colorGold
        strokePaint.strokeWidth = 1.2f
        canvas.drawLine(leftEdge, headerY + 8f, rightEdge, headerY + 8f, strokePaint)
    }

    private fun drawRunningFooter(canvas: Canvas, pageNumber: Int) {
        val footerY = printableBottom + 16f

        // Thin Border Line
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawLine(leftEdge, footerY - 8f, rightEdge, footerY - 8f, strokePaint)

        // Footer Text
        textPaint.color = colorMutedText
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("سامانه پایش هوشمند حیوانات خانگی پتورا (Petora) - تمام سوابق به صورت آفلاین ذخیره شده است.", rightEdge, footerY, textPaint)

        textPaint.color = colorBurgundy
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.LEFT
        val pageStr = "صفحه ${PersianNumberFormatter.toPersian(pageNumber)}"
        canvas.drawText(pageStr, leftEdge, footerY, textPaint)
    }

    private fun drawSectionBanner(canvas: Canvas, title: String, subtitle: String, startY: Float) {
        val bannerHeight = 32f
        val rect = RectF(leftEdge, startY, rightEdge, startY + bannerHeight)

        bgPaint.color = colorBurgundy
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)

        // Left gold accent tag
        bgPaint.color = colorGold
        canvas.drawRect(leftEdge, startY, leftEdge + 5f, startY + bannerHeight, bgPaint)

        textPaint.color = colorWhite
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(title, rightEdge - 12f, startY + 20f, textPaint)

        textPaint.color = colorGoldLight
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(subtitle, leftEdge + 15f, startY + 20f, textPaint)
    }

    private fun drawEmptyBox(canvas: Canvas, message: String, startY: Float) {
        val rect = RectF(leftEdge, startY, rightEdge, startY + 34f)
        bgPaint.color = colorBeigeCard
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRoundRect(rect, 6f, 6f, strokePaint)

        textPaint.color = colorMutedText
        textPaint.textSize = 9.5f
        textPaint.isFakeBoldText = false
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText(message, pageWidth / 2f, startY + 21f, textPaint)
    }

    private fun drawInfoNotice(canvas: Canvas, message: String, startY: Float) {
        val rect = RectF(leftEdge, startY, rightEdge, startY + 28f)
        bgPaint.color = colorBeigeCard
        canvas.drawRoundRect(rect, 4f, 4f, bgPaint)

        textPaint.color = colorBurgundy
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = false
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("ℹ $message", rightEdge - 10f, startY + 18f, textPaint)
    }

    // =========================================================================
    // HEALTH SECTION
    // =========================================================================

    private fun drawHealthTableHeader(canvas: Canvas, startY: Float) {
        val h = 22f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = colorBeigeCard
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRect(rect, strokePaint)

        textPaint.color = colorBurgundyDark
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = true

        // Columns from right to left
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("نوع / عنوان سابقه", rightEdge - 8f, startY + 15f, textPaint)
        canvas.drawText("تاریخ", rightEdge - 145f, startY + 15f, textPaint)
        canvas.drawText("کلینیک / پزشک", rightEdge - 235f, startY + 15f, textPaint)
        canvas.drawText("وضعیت", rightEdge - 340f, startY + 15f, textPaint)
        canvas.drawText("توضیحات و نکات", rightEdge - 410f, startY + 15f, textPaint)
    }

    private fun drawHealthTableRow(canvas: Canvas, record: red.line.pet.domain.model.HealthRecord, startY: Float, isEven: Boolean) {
        val h = 30f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = if (isEven) colorWhite else colorBeige
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.5f
        canvas.drawLine(leftEdge, startY + h, rightEdge, startY + h, strokePaint)

        textPaint.color = colorDarkText
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT

        // Title + Type
        val titleCombined = "${record.type.titleFa}: ${record.title}"
        canvas.drawText(titleCombined.take(24), rightEdge - 8f, startY + 19f, textPaint)

        // Date
        textPaint.isFakeBoldText = false
        textPaint.color = colorMutedText
        canvas.drawText(record.jalaliDate, rightEdge - 145f, startY + 19f, textPaint)

        // Clinic
        val clinic = record.clinicOrDoctor.ifBlank { "---" }
        canvas.drawText(clinic.take(16), rightEdge - 235f, startY + 19f, textPaint)

        // Status
        if (record.isCompleted) {
            textPaint.color = colorSuccess
            textPaint.isFakeBoldText = true
            canvas.drawText("انجام شده ✓", rightEdge - 340f, startY + 19f, textPaint)
        } else {
            textPaint.color = colorGold
            textPaint.isFakeBoldText = true
            canvas.drawText("برنامه‌ریزی ⏱", rightEdge - 340f, startY + 19f, textPaint)
        }

        // Notes
        textPaint.color = colorMutedText
        textPaint.isFakeBoldText = false
        val note = record.notes.ifBlank { "---" }
        canvas.drawText(note.take(22), rightEdge - 410f, startY + 19f, textPaint)
    }

    // =========================================================================
    // WEIGHT SECTION & CHART
    // =========================================================================

    private fun drawWeightStatCards(canvas: Canvas, report: MedicalPassportReport, startY: Float) {
        val cardCount = 4
        val gap = 8f
        val cardWidth = (contentWidth - (gap * (cardCount - 1))) / cardCount
        val cardHeight = 52f

        val stats = report.weightStats
        val currentStr = if (stats.currentWeightKg > 0) "${PersianNumberFormatter.toPersian(stats.currentWeightKg)} kg" else "---"
        val minMaxStr = if (stats.minWeightKg > 0) "${PersianNumberFormatter.toPersian(stats.minWeightKg)} - ${PersianNumberFormatter.toPersian(stats.maxWeightKg)}" else "---"
        val totalChangeStr = if (stats.totalRecordsCount >= 2) {
            val sign = if (stats.totalChangeKg >= 0) "+" else ""
            "$sign${PersianNumberFormatter.toPersian(String.format("%.2f", stats.totalChangeKg))} kg"
        } else {
            "---"
        }
        val recordsCountStr = "${PersianNumberFormatter.toPersian(stats.totalRecordsCount)} نوبت"

        val cardsData = listOf(
            Triple("وزن فعلی", currentStr, colorBurgundy),
            Triple("تغییرات کلی", totalChangeStr, if (stats.totalChangeKg >= 0) colorSuccess else colorBurgundy),
            Triple("کمترین / بیشترین", minMaxStr, colorMutedText),
            Triple("تعداد ثبت", recordsCountStr, colorDarkText)
        )

        cardsData.forEachIndexed { index, (label, value, valueColor) ->
            val cardRight = rightEdge - (index * (cardWidth + gap))
            val cardLeft = cardRight - cardWidth
            val rect = RectF(cardLeft, startY, cardRight, startY + cardHeight)

            bgPaint.color = colorWhite
            canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
            strokePaint.color = colorBorder
            strokePaint.strokeWidth = 0.8f
            canvas.drawRoundRect(rect, 6f, 6f, strokePaint)

            // Top gold bar for first card
            if (index == 0) {
                bgPaint.color = colorGold
                canvas.drawRoundRect(RectF(cardLeft, startY, cardRight, startY + 4f), 2f, 2f, bgPaint)
            }

            textPaint.color = colorMutedText
            textPaint.textSize = 8.5f
            textPaint.isFakeBoldText = false
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(label, rect.centerX(), startY + 20f, textPaint)

            textPaint.color = valueColor
            textPaint.textSize = 11f
            textPaint.isFakeBoldText = true
            canvas.drawText(value, rect.centerX(), startY + 40f, textPaint)
        }
    }

    private fun drawWeightChart(canvas: Canvas, records: List<WeightRecord>, startY: Float, height: Float) {
        val chartRect = RectF(leftEdge, startY, rightEdge, startY + height)

        // Background
        bgPaint.color = colorWhite
        canvas.drawRoundRect(chartRect, 8f, 8f, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRoundRect(chartRect, 8f, 8f, strokePaint)

        val paddingH = 35f
        val paddingV = 25f
        val plotLeft = leftEdge + paddingH
        val plotRight = rightEdge - paddingH
        val plotTop = startY + paddingV
        val plotBottom = startY + height - paddingV

        val weights = records.map { it.weightKg.toFloat() }
        val minW = (weights.minOrNull() ?: 0f) * 0.95f
        val maxW = (weights.maxOrNull() ?: 1f) * 1.05f
        val rangeW = max(maxW - minW, 0.1f)

        // Draw horizontal grid lines
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.6f
        strokePaint.pathEffect = DashPathEffect(floatArrayOf(3f, 3f), 0f)

        val gridSteps = 3
        for (i in 0..gridSteps) {
            val y = plotBottom - (i * (plotBottom - plotTop) / gridSteps)
            canvas.drawLine(plotLeft, y, plotRight, y, strokePaint)

            val gridWeightVal = minW + (i * rangeW / gridSteps)
            textPaint.color = colorMutedText
            textPaint.textSize = 7.5f
            textPaint.isFakeBoldText = false
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("${PersianNumberFormatter.toPersian(String.format("%.1f", gridWeightVal))}k", leftEdge + 5f, y + 3f, textPaint)
        }
        strokePaint.pathEffect = null // reset

        // Calculate points
        val pointCount = records.size
        val points = mutableListOf<Pair<Float, Float>>()

        records.forEachIndexed { index, record ->
            val x = if (pointCount > 1) {
                plotLeft + (index * (plotRight - plotLeft) / (pointCount - 1))
            } else {
                (plotLeft + plotRight) / 2f
            }
            val y = plotBottom - ((record.weightKg.toFloat() - minW) / rangeW * (plotBottom - plotTop))
            points.add(Pair(x, y))
        }

        // Draw gradient area under line
        val path = Path()
        if (points.isNotEmpty()) {
            path.moveTo(points.first().first, plotBottom)
            points.forEach { path.lineTo(it.first, it.second) }
            path.lineTo(points.last().first, plotBottom)
            path.close()

            bgPaint.color = Color.argb(30, 107, 15, 36) // transparent Burgundy
            canvas.drawPath(path, bgPaint)
        }

        // Draw Line
        strokePaint.color = colorBurgundy
        strokePaint.strokeWidth = 2.2f
        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second, strokePaint)
        }

        // Draw Points & Labels
        points.forEachIndexed { i, (x, y) ->
            // Outer Gold dot
            bgPaint.color = colorGold
            canvas.drawCircle(x, y, 4.5f, bgPaint)
            // Inner Burgundy dot
            bgPaint.color = colorBurgundy
            canvas.drawCircle(x, y, 2.5f, bgPaint)

            // Value label above point
            textPaint.color = colorBurgundyDark
            textPaint.textSize = 8f
            textPaint.isFakeBoldText = true
            textPaint.textAlign = Paint.Align.CENTER
            val valText = PersianNumberFormatter.toPersian(String.format("%.1f", records[i].weightKg))
            canvas.drawText(valText, x, y - 7f, textPaint)

            // Date label below point (if space permits)
            if (pointCount <= 8 || i % 2 == 0) {
                textPaint.color = colorMutedText
                textPaint.textSize = 7f
                textPaint.isFakeBoldText = false
                val dateStr = records[i].getEffectiveJalaliDate().takeLast(5)
                canvas.drawText(dateStr, x, plotBottom + 12f, textPaint)
            }
        }
    }

    // =========================================================================
    // FOOD SECTION
    // =========================================================================

    private fun drawFoodTableHeader(canvas: Canvas, startY: Float) {
        val h = 22f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = colorBeigeCard
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRect(rect, strokePaint)

        textPaint.color = colorBurgundyDark
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT

        canvas.drawText("وعده غذایی", rightEdge - 8f, startY + 15f, textPaint)
        canvas.drawText("نوع و عنوان غذا", rightEdge - 130f, startY + 15f, textPaint)
        canvas.drawText("مقدار مصرف", rightEdge - 250f, startY + 15f, textPaint)
        canvas.drawText("ساعت سرو", rightEdge - 340f, startY + 15f, textPaint)
        canvas.drawText("مکمل و توضیحات", rightEdge - 420f, startY + 15f, textPaint)
    }

    private fun drawFoodTableRow(canvas: Canvas, schedule: red.line.pet.domain.model.FoodSchedule, startY: Float, isEven: Boolean) {
        val h = 28f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = if (isEven) colorWhite else colorBeige
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.5f
        canvas.drawLine(leftEdge, startY + h, rightEdge, startY + h, strokePaint)

        textPaint.color = colorDarkText
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT

        // Meal Title/Type
        val meal = schedule.mealTitle.ifBlank { schedule.mealType.titleFa }
        canvas.drawText(meal.take(18), rightEdge - 8f, startY + 18f, textPaint)

        // Food Name
        textPaint.isFakeBoldText = false
        textPaint.color = colorDarkText
        canvas.drawText(schedule.foodName.take(20), rightEdge - 130f, startY + 18f, textPaint)

        // Amount
        val amount = "${schedule.amount} ${schedule.amountUnit.titleFa}".trim()
        canvas.drawText(amount, rightEdge - 250f, startY + 18f, textPaint)

        // Time
        textPaint.color = colorBurgundy
        textPaint.isFakeBoldText = true
        canvas.drawText(schedule.mealTime, rightEdge - 340f, startY + 18f, textPaint)

        // Supplement & notes
        textPaint.color = colorMutedText
        textPaint.isFakeBoldText = false
        val supp = if (schedule.supplementName.isNotBlank()) "${schedule.supplementName} (${schedule.supplementAmount})" else schedule.notes.ifBlank { "---" }
        canvas.drawText(supp.take(20), rightEdge - 420f, startY + 18f, textPaint)
    }

    // =========================================================================
    // REMINDERS SECTION
    // =========================================================================

    private fun drawReminderTableHeader(canvas: Canvas, startY: Float) {
        val h = 22f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = colorBeigeCard
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRect(rect, strokePaint)

        textPaint.color = colorBurgundyDark
        textPaint.textSize = 9f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT

        canvas.drawText("نوع و عنوان یادآور", rightEdge - 8f, startY + 15f, textPaint)
        canvas.drawText("تاریخ شمسی", rightEdge - 160f, startY + 15f, textPaint)
        canvas.drawText("ساعت", rightEdge - 270f, startY + 15f, textPaint)
        canvas.drawText("تکرار", rightEdge - 350f, startY + 15f, textPaint)
        canvas.drawText("وضعیت", rightEdge - 430f, startY + 15f, textPaint)
    }

    private fun drawReminderTableRow(canvas: Canvas, reminder: red.line.pet.domain.model.Reminder, startY: Float, isEven: Boolean) {
        val h = 26f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = if (isEven) colorWhite else colorBeige
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.5f
        canvas.drawLine(leftEdge, startY + h, rightEdge, startY + h, strokePaint)

        textPaint.color = colorDarkText
        textPaint.textSize = 8.8f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT

        val title = reminder.title
        canvas.drawText(title.take(24), rightEdge - 8f, startY + 17f, textPaint)

        textPaint.isFakeBoldText = false
        textPaint.color = colorMutedText
        canvas.drawText(reminder.jalaliDate, rightEdge - 160f, startY + 17f, textPaint)
        canvas.drawText(reminder.timeString, rightEdge - 270f, startY + 17f, textPaint)
        canvas.drawText(reminder.repeatType.titleFa, rightEdge - 350f, startY + 17f, textPaint)

        if (reminder.isCompleted) {
            textPaint.color = colorSuccess
            canvas.drawText("انجام شده ✓", rightEdge - 430f, startY + 17f, textPaint)
        } else {
            textPaint.color = colorBurgundy
            canvas.drawText("فعال 🔔", rightEdge - 430f, startY + 17f, textPaint)
        }
    }

    // =========================================================================
    // MEMORIES SECTION
    // =========================================================================

    private fun drawMemoriesGrid(canvas: Canvas, memories: List<red.line.pet.domain.model.Memory>, startY: Float): Float {
        if (memories.isEmpty()) return 0f

        val itemWidth = (contentWidth - 14f) / 2f
        val itemHeight = 110f

        memories.forEachIndexed { index, memory ->
            val row = index / 2
            val col = index % 2

            val itemRight = rightEdge - (col * (itemWidth + 14f))
            val itemLeft = itemRight - itemWidth
            val itemTop = startY + (row * (itemHeight + 12f))
            val itemBottom = itemTop + itemHeight
            val rect = RectF(itemLeft, itemTop, itemRight, itemBottom)

            // Card
            bgPaint.color = colorWhite
            canvas.drawRoundRect(rect, 8f, 8f, bgPaint)
            strokePaint.color = colorBorder
            strokePaint.strokeWidth = 0.8f
            canvas.drawRoundRect(rect, 8f, 8f, strokePaint)

            // Thumbnail on the right inside card
            val thumbSize = 90f
            val thumbRight = itemRight - 10f
            val thumbLeft = thumbRight - thumbSize
            val thumbTop = itemTop + 10f
            val thumbBottom = thumbTop + thumbSize
            val thumbRect = RectF(thumbLeft, thumbTop, thumbRight, thumbBottom)

            val bmp = loadPetBitmap(memory.imagePath)
            if (bmp != null) {
                val src = Rect(0, 0, bmp.width, bmp.height)
                val dest = Rect(thumbLeft.toInt(), thumbTop.toInt(), thumbRight.toInt(), thumbBottom.toInt())
                canvas.drawBitmap(bmp, src, dest, null)
                strokePaint.color = colorBorder
                canvas.drawRoundRect(thumbRect, 4f, 4f, strokePaint)
            } else {
                bgPaint.color = colorBeigeCard
                canvas.drawRoundRect(thumbRect, 4f, 4f, bgPaint)
                textPaint.color = colorGold
                textPaint.textSize = 24f
                textPaint.textAlign = Paint.Align.CENTER
                canvas.drawText("📸", thumbRect.centerX(), thumbRect.centerY() + 8f, textPaint)
            }

            // Text on the left of thumbnail
            val textRight = thumbLeft - 10f
            textPaint.color = colorBurgundy
            textPaint.textSize = 10.5f
            textPaint.isFakeBoldText = true
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(memory.title.take(18), textRight, itemTop + 30f, textPaint)

            textPaint.color = colorMutedText
            textPaint.textSize = 8.5f
            textPaint.isFakeBoldText = false
            canvas.drawText(memory.getFormattedJalaliDate(), textRight, itemTop + 50f, textPaint)

            if (memory.description.isNotBlank()) {
                canvas.drawText(memory.description.take(24), textRight, itemTop + 70f, textPaint)
            }
        }

        val rows = (memories.size + 1) / 2
        return (rows * (itemHeight + 12f))
    }

    // =========================================================================
    // EXPENSES SECTION
    // =========================================================================

    private fun drawExpensesSummaryCards(canvas: Canvas, report: MedicalPassportReport, startY: Float) {
        val gap = 12f
        val cardWidth = (contentWidth - gap) / 2f
        val cardHeight = 48f

        // Card 1: Health Expenses
        val card1Right = rightEdge
        val card1Left = card1Right - cardWidth
        val rect1 = RectF(card1Left, startY, card1Right, startY + cardHeight)
        bgPaint.color = colorWhite
        canvas.drawRoundRect(rect1, 6f, 6f, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRoundRect(rect1, 6f, 6f, strokePaint)

        // Accent tag
        bgPaint.color = colorBurgundy
        canvas.drawRoundRect(RectF(card1Left, startY, card1Right, startY + 3.5f), 2f, 2f, bgPaint)

        textPaint.color = colorMutedText
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("مجموع هزینه‌های سلامت و کلینیک", rect1.centerX(), startY + 18f, textPaint)

        textPaint.color = colorBurgundy
        textPaint.textSize = 11.5f
        textPaint.isFakeBoldText = true
        val healthExpStr = "${PersianNumberFormatter.toPersian(report.healthExpenseAmount)} تومان"
        canvas.drawText(healthExpStr, rect1.centerX(), startY + 37f, textPaint)

        // Card 2: Total Expenses
        val card2Right = card1Left - gap
        val card2Left = card2Right - cardWidth
        val rect2 = RectF(card2Left, startY, card2Right, startY + cardHeight)
        bgPaint.color = colorWhite
        canvas.drawRoundRect(rect2, 6f, 6f, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRoundRect(rect2, 6f, 6f, strokePaint)

        bgPaint.color = colorGold
        canvas.drawRoundRect(RectF(card2Left, startY, card2Right, startY + 3.5f), 2f, 2f, bgPaint)

        textPaint.color = colorMutedText
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = false
        canvas.drawText("کل هزینه‌های ثبت‌شده", rect2.centerX(), startY + 18f, textPaint)

        textPaint.color = colorDarkText
        textPaint.textSize = 11.5f
        textPaint.isFakeBoldText = true
        val totalExpStr = "${PersianNumberFormatter.toPersian(report.totalExpenseAmount)} تومان"
        canvas.drawText(totalExpStr, rect2.centerX(), startY + 37f, textPaint)
    }

    private fun drawExpenseTableHeader(canvas: Canvas, startY: Float) {
        val h = 20f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = colorBeigeCard
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.8f
        canvas.drawRect(rect, strokePaint)

        textPaint.color = colorBurgundyDark
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT

        canvas.drawText("عنوان هزینه", rightEdge - 8f, startY + 14f, textPaint)
        canvas.drawText("دسته‌بندی", rightEdge - 160f, startY + 14f, textPaint)
        canvas.drawText("تاریخ", rightEdge - 280f, startY + 14f, textPaint)
        canvas.drawText("مبلغ (تومان)", rightEdge - 380f, startY + 14f, textPaint)
    }

    private fun drawExpenseTableRow(canvas: Canvas, expense: red.line.pet.domain.model.Expense, startY: Float, isEven: Boolean) {
        val h = 24f
        val rect = RectF(leftEdge, startY, rightEdge, startY + h)
        bgPaint.color = if (isEven) colorWhite else colorBeige
        canvas.drawRect(rect, bgPaint)
        strokePaint.color = colorBorder
        strokePaint.strokeWidth = 0.5f
        canvas.drawLine(leftEdge, startY + h, rightEdge, startY + h, strokePaint)

        textPaint.color = colorDarkText
        textPaint.textSize = 8.5f
        textPaint.isFakeBoldText = true
        textPaint.textAlign = Paint.Align.RIGHT

        canvas.drawText(expense.title.take(22), rightEdge - 8f, startY + 16f, textPaint)

        textPaint.isFakeBoldText = false
        textPaint.color = colorMutedText
        canvas.drawText(expense.category.titleFa, rightEdge - 160f, startY + 16f, textPaint)

        val dateStr = expense.jalaliDate.ifBlank { "---" }
        canvas.drawText(dateStr, rightEdge - 280f, startY + 16f, textPaint)

        textPaint.color = colorBurgundy
        textPaint.isFakeBoldText = true
        val amountStr = "${PersianNumberFormatter.toPersian(expense.amountToman)}"
        canvas.drawText(amountStr, rightEdge - 380f, startY + 16f, textPaint)
    }

    // =========================================================================
    // BITMAP LOADING HELPER
    // =========================================================================

    private fun loadPetBitmap(pathOrUri: String): Bitmap? {
        if (pathOrUri.isBlank()) return null
        return try {
            when {
                pathOrUri.startsWith("content://") -> {
                    val uri = Uri.parse(pathOrUri)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        decodeSampledBitmap(inputStream, 200, 200)
                    }
                }
                pathOrUri.startsWith("file://") -> {
                    val file = File(Uri.parse(pathOrUri).path ?: "")
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else null
                }
                pathOrUri.startsWith("images/") || pathOrUri.startsWith("cards/") -> {
                    context.assets.open(pathOrUri).use { inputStream ->
                        decodeSampledBitmap(inputStream, 200, 200)
                    }
                }
                File(pathOrUri).exists() -> {
                    BitmapFactory.decodeFile(pathOrUri)
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeSampledBitmap(inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val bytes = inputStream.readBytes()
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
