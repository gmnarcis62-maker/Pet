package red.line.pet.presentation.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.pet.core.theme.BurgundyLight
import red.line.pet.core.theme.BurgundyPrimary
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.LuxuryGoldLight
import red.line.pet.core.theme.RedLineBright
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.theme.StatusSuccess
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.WeightRecord

/**
 * Professional Canvas-based Growth & Weight Line Chart.
 * Handles 0 records, 1 record, 2 records, and n records with interactive tap point tooltips.
 */
@Composable
fun WeightGrowthChart(
    records: List<WeightRecord>,
    selectedRecord: WeightRecord?,
    onSelectPoint: (WeightRecord?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Chronological order for chart rendering
    val chronological = remember(records) {
        records.sortedBy { it.date }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weight_growth_chart_canvas"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(LuxuryGold.copy(alpha = 0.45f), BurgundyPrimary.copy(alpha = 0.2f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Chart Top Row / Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(LuxuryGold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نمودار خطی تغییرات وزن",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (chronological.isNotEmpty()) {
                    Text(
                        text = "${PersianNumberFormatter.toPersian(chronological.size)} نقطه ثبت‌شده",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Viewport
            if (chronological.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "داده‌ای در این بازه زمانی برای رسم نمودار وجود ندارد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val weights = chronological.map { it.weightKg }
                val minWeight = weights.minOrNull() ?: 0.0
                val maxWeight = weights.maxOrNull() ?: 1.0

                // Add nice breathing margin above & below min/max
                val weightDiff = (maxWeight - minWeight).coerceAtLeast(0.5)
                val yMin = (minWeight - weightDiff * 0.15).coerceAtLeast(0.0)
                val yMax = maxWeight + weightDiff * 0.15
                val yRange = (yMax - yMin).coerceAtLeast(0.1)

                // Render Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(chronological) {
                                detectTapGestures { offset ->
                                    val width = size.width
                                    val leftPadding = 50f
                                    val rightPadding = 40f
                                    val usableWidth = width - leftPadding - rightPadding

                                    if (chronological.size == 1) {
                                        onSelectPoint(chronological.first())
                                    } else {
                                        val stepX = usableWidth / (chronological.size - 1)
                                        val tappedIndex = ((offset.x - leftPadding + (stepX / 2f)) / stepX).toInt()
                                        if (tappedIndex in chronological.indices) {
                                            val tapped = chronological[tappedIndex]
                                            if (selectedRecord?.id == tapped.id) {
                                                onSelectPoint(null)
                                            } else {
                                                onSelectPoint(tapped)
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height

                        val topPadding = 30f
                        val bottomPadding = 40f
                        val leftPadding = 60f
                        val rightPadding = 40f

                        val chartWidth = width - leftPadding - rightPadding
                        val chartHeight = height - topPadding - bottomPadding

                        // Draw Grid Lines (Horizontal)
                        val gridCount = 4
                        for (i in 0..gridCount) {
                            val y = topPadding + (chartHeight / gridCount) * i
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.18f),
                                start = Offset(leftPadding, y),
                                end = Offset(width - rightPadding, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        // Coordinates mapping
                        val points = chronological.mapIndexed { index, record ->
                            val x = if (chronological.size == 1) {
                                leftPadding + chartWidth / 2f
                            } else {
                                leftPadding + (index.toFloat() / (chronological.size - 1)) * chartWidth
                            }
                            val normalizedY = ((record.weightKg - yMin) / yRange).toFloat()
                            val y = topPadding + chartHeight * (1f - normalizedY)
                            Offset(x, y)
                        }

                        // Gradient Area Path
                        if (points.size > 1) {
                            val fillPath = Path().apply {
                                moveTo(points.first().x, topPadding + chartHeight)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, topPadding + chartHeight)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        LuxuryGold.copy(alpha = 0.35f),
                                        RedLinePrimary.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    startY = topPadding,
                                    endY = topPadding + chartHeight
                                )
                            )

                            // Trend Line
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val controlX1 = (prev.x + curr.x) / 2f
                                    cubicTo(controlX1, prev.y, controlX1, curr.y, curr.x, curr.y)
                                }
                            }

                            drawPath(
                                path = linePath,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(LuxuryGold, RedLineBright, BurgundyPrimary)
                                ),
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // Draw points
                        points.forEachIndexed { index, pt ->
                            val record = chronological[index]
                            val isSelected = selectedRecord?.id == record.id

                            if (isSelected) {
                                // Outer pulsating ring
                                drawCircle(
                                    color = LuxuryGold.copy(alpha = 0.4f),
                                    radius = 12.dp.toPx(),
                                    center = pt
                                )
                                // Highlight line to bottom
                                drawLine(
                                    color = LuxuryGold.copy(alpha = 0.7f),
                                    start = pt,
                                    end = Offset(pt.x, topPadding + chartHeight),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                            }

                            // Inner circle
                            drawCircle(
                                color = if (isSelected) RedLineBright else LuxuryGold,
                                radius = if (isSelected) 6.5.dp.toPx() else 4.5.dp.toPx(),
                                center = pt
                            )

                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 3.5.dp.toPx() else 2.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }

                // Selected Point Inspection Details Card
                selectedRecord?.let { record ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "نقطه انتخاب‌شده: ${record.getEffectiveJalaliDate()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (record.notes.isNotBlank()) {
                                    Text(
                                        text = record.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RedLinePrimary
                            ) {
                                Text(
                                    text = record.getFormattedWeight(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
