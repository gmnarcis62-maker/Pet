package red.line.pet.presentation.weight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.LuxuryGoldLight
import red.line.pet.core.theme.RedLineBright
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.WeightRecord
import red.line.pet.domain.model.WeightUnit

@Composable
fun AddEditWeightDialog(
    pet: Pet,
    existingRecord: WeightRecord?,
    onDismiss: () -> Unit,
    onSave: (Double, WeightUnit, String, String) -> Unit
) {
    var selectedUnit by remember {
        mutableStateOf(existingRecord?.unit ?: WeightUnit.KILOGRAM)
    }

    val initialWeightStr = existingRecord?.let {
        if (it.unit == WeightUnit.GRAM) {
            (it.weightKg * 1000).toInt().toString()
        } else {
            if (it.weightKg % 1.0 == 0.0) it.weightKg.toInt().toString() else it.weightKg.toString()
        }
    } ?: ""

    var weightInput by remember { mutableStateOf(initialWeightStr) }
    var jalaliDateInput by remember {
        mutableStateOf(existingRecord?.getEffectiveJalaliDate() ?: JalaliDateHelper.now().toStandardString(usePersianDigits = false))
    }
    var notesInput by remember { mutableStateOf(existingRecord?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("add_weight_dialog"),
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(LuxuryGold.copy(alpha = 0.5f), RedLinePrimary.copy(alpha = 0.3f))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (existingRecord == null) "ثبت وزن جدید" else "ویرایش رکورد وزن",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "پایش رشد: ${pet.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryGold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Unit selection tabs
                Text(
                    text = "واحد وزن",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WeightUnit.entries.forEach { unit ->
                        val isSelected = selectedUnit == unit
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (selectedUnit != unit) {
                                        // Optional unit conversion on switch
                                        val currentVal = weightInput.replace('/', '.').toDoubleOrNull()
                                        if (currentVal != null && currentVal > 0) {
                                            if (unit == WeightUnit.GRAM && selectedUnit == WeightUnit.KILOGRAM) {
                                                weightInput = (currentVal * 1000).toInt().toString()
                                            } else if (unit == WeightUnit.KILOGRAM && selectedUnit == WeightUnit.GRAM) {
                                                val inKg = currentVal / 1000.0
                                                weightInput = if (inKg % 1.0 == 0.0) inKg.toInt().toString() else inKg.toString()
                                            }
                                        }
                                        selectedUnit = unit
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = unit.titleFa,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weight Input Field
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = {
                        weightInput = it
                        errorMessage = null
                    },
                    label = { Text("مقدار وزن *") },
                    placeholder = {
                        Text(if (selectedUnit == WeightUnit.KILOGRAM) "مثال: 4.2" else "مثال: 4200")
                    },
                    trailingIcon = {
                        Text(
                            text = selectedUnit.shortFa,
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryGold,
                            modifier = Modifier.padding(end = 12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Scale, contentDescription = null, tint = RedLineBright)
                    },
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Solar Hijri Date Input
                OutlinedTextField(
                    value = jalaliDateInput,
                    onValueChange = {
                        jalaliDateInput = it
                        errorMessage = null
                    },
                    label = { Text("تاریخ ثبت (شمسی) *") },
                    placeholder = { Text("۱۴۰۳/۰۶/۲۲") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = LuxuryGold)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_date_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Notes Input
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("یادداشت و وضعیت (اختیاری)") },
                    placeholder = { Text("مثال: بعد از دوره رژیم، چکاپ قبل واکسن...") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_notes_input_field"),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                AnimatedVisibility(visible = errorMessage != null) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Text("انصراف", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            val cleanVal = weightInput
                                .replace('۰', '0').replace('۱', '1').replace('۲', '2')
                                .replace('۳', '3').replace('۴', '4').replace('۵', '5')
                                .replace('۶', '6').replace('۷', '7').replace('۸', '8')
                                .replace('۹', '9').replace(',', '.').replace('/', '.').trim()

                            val parsedWeight = cleanVal.toDoubleOrNull()
                            if (parsedWeight == null || parsedWeight <= 0.0) {
                                errorMessage = "لطفاً مقدار معتبر و مثبت برای وزن وارد نمایید."
                                return@Button
                            }

                            if (selectedUnit == WeightUnit.KILOGRAM && (parsedWeight < 0.05 || parsedWeight > 300.0)) {
                                errorMessage = "مقدار وزن کیلوگرم خارج از بازه منطقی است."
                                return@Button
                            }

                            if (selectedUnit == WeightUnit.GRAM && (parsedWeight < 5.0 || parsedWeight > 300000.0)) {
                                errorMessage = "مقدار وزن گرم خارج از بازه منطقی است."
                                return@Button
                            }

                            if (jalaliDateInput.isBlank()) {
                                errorMessage = "لطفاً تاریخ شمسی را وارد کنید."
                                return@Button
                            }

                            onSave(parsedWeight, selectedUnit, jalaliDateInput.trim(), notesInput.trim())
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_weight_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedLinePrimary)
                    ) {
                        Text(
                            text = if (existingRecord == null) "ثبت وزن" else "ذخیره تغییرات",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
