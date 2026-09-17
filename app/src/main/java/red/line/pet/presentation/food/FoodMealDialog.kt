package red.line.pet.presentation.food

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import red.line.pet.domain.model.AmountUnit
import red.line.pet.domain.model.DayOfWeekFa
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.MealType
import red.line.pet.domain.model.Pet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodMealDialog(
    pet: Pet,
    existingSchedule: FoodSchedule?,
    onDismiss: () -> Unit,
    onSave: (FoodSchedule) -> Unit
) {
    var selectedMealType by remember {
        mutableStateOf(existingSchedule?.mealType ?: MealType.BREAKFAST)
    }
    var mealTitle by remember {
        mutableStateOf(existingSchedule?.mealTitle ?: existingSchedule?.mealType?.titleFa ?: MealType.BREAKFAST.titleFa)
    }
    var mealTime by remember {
        mutableStateOf(existingSchedule?.mealTime ?: selectedMealType.defaultHour)
    }
    var foodName by remember {
        mutableStateOf(existingSchedule?.foodName ?: "")
    }
    var amount by remember {
        mutableStateOf(existingSchedule?.amount ?: "")
    }
    var selectedAmountUnit by remember {
        mutableStateOf(existingSchedule?.amountUnit ?: AmountUnit.GRAM)
    }
    var selectedDays by remember {
        mutableStateOf(existingSchedule?.repeatDays ?: DayOfWeekFa.entries.toSet())
    }
    var hasSupplement by remember {
        mutableStateOf(
            existingSchedule?.supplementName?.isNotBlank() == true ||
            existingSchedule?.supplementAmount?.isNotBlank() == true
        )
    }
    var supplementName by remember {
        mutableStateOf(existingSchedule?.supplementName ?: "")
    }
    var supplementAmount by remember {
        mutableStateOf(existingSchedule?.supplementAmount ?: "")
    }
    var supplementNotes by remember {
        mutableStateOf(existingSchedule?.supplementNotes ?: "")
    }
    var notes by remember {
        mutableStateOf(existingSchedule?.notes ?: "")
    }
    var isActive by remember {
        mutableStateOf(existingSchedule?.isActive ?: true)
    }

    var showError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("food_meal_dialog"),
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
                            text = if (existingSchedule == null) "افزودن وعده غذایی" else "ویرایش وعده غذایی",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "برای پت عزیز: ${pet.name}",
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

                Spacer(modifier = Modifier.height(18.dp))

                // Meal Type Selector Chips
                Text(
                    text = "نوع وعده",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { type ->
                        val isSelected = selectedMealType == type
                        Surface(
                            modifier = Modifier.clickable {
                                selectedMealType = type
                                if (mealTitle.isBlank() || MealType.entries.any { it.titleFa == mealTitle }) {
                                    mealTitle = type.titleFa
                                }
                                if (mealTime.isBlank() || MealType.entries.any { it.defaultHour == mealTime }) {
                                    mealTime = type.defaultHour
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = type.titleFa,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meal Title & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = mealTitle,
                        onValueChange = { mealTitle = it },
                        label = { Text("عنوان وعده") },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = RedLineBright)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("food_dialog_title_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )

                    OutlinedTextField(
                        value = mealTime,
                        onValueChange = { mealTime = it },
                        label = { Text("ساعت") },
                        placeholder = { Text("08:00") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = LuxuryGold)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("food_dialog_time_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Food Name
                OutlinedTextField(
                    value = foodName,
                    onValueChange = {
                        foodName = it
                        showError = false
                    },
                    label = { Text("نام و برند غذا *") },
                    placeholder = { Text("مثال: رویال کنین رژیمی، غذای مرطوب، مرغ و کدو") },
                    leadingIcon = {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = RedLineBright)
                    },
                    isError = showError && foodName.isBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("food_dialog_food_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Amount & Amount Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                            showError = false
                        },
                        label = { Text("مقدار غذا *") },
                        placeholder = { Text("مثال: 120") },
                        leadingIcon = {
                            Icon(Icons.Default.Scale, contentDescription = null, tint = LuxuryGold)
                        },
                        isError = showError && amount.isBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("food_dialog_amount_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Units selection chips
                Text(
                    text = "واحد مقدار",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AmountUnit.entries.forEach { unit ->
                        val isSelected = selectedAmountUnit == unit
                        Surface(
                            modifier = Modifier.clickable { selectedAmountUnit = unit },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) LuxuryGold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = unit.titleFa,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) LuxuryGoldLight else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recurring Days Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "روزهای تکرار در هفته",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val isAllSelected = selectedDays.size == DayOfWeekFa.entries.size
                    Text(
                        text = if (isAllSelected) "لغو همه" else "انتخاب همه",
                        style = MaterialTheme.typography.labelSmall,
                        color = RedLinePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            selectedDays = if (isAllSelected) emptySet() else DayOfWeekFa.entries.toSet()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DayOfWeekFa.entries.forEach { day ->
                        val isDaySelected = selectedDays.contains(day)
                        Surface(
                            modifier = Modifier.clickable {
                                selectedDays = if (isDaySelected) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDaySelected) RedLinePrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (isDaySelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                if (isDaySelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = LuxuryGoldLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = day.titleFa,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDaySelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Supplement Section Toggle
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "مکمل یا داروی همراه این وعده",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = hasSupplement,
                                onCheckedChange = { hasSupplement = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LuxuryGold,
                                    checkedTrackColor = RedLinePrimary
                                )
                            )
                        }

                        AnimatedVisibility(visible = hasSupplement) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = supplementName,
                                        onValueChange = { supplementName = it },
                                        label = { Text("نام مکمل") },
                                        placeholder = { Text("مثال: روغن سالمون، کلسیم") },
                                        modifier = Modifier.weight(1.5f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LuxuryGold
                                        )
                                    )

                                    OutlinedTextField(
                                        value = supplementAmount,
                                        onValueChange = { supplementAmount = it },
                                        label = { Text("مقدار") },
                                        placeholder = { Text("مثال: ۱ پمپ") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LuxuryGold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = supplementNotes,
                                    onValueChange = { supplementNotes = it },
                                    label = { Text("توضیحات مکمل") },
                                    placeholder = { Text("دستور مصرف یا توصیه‌های دامپزشک") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LuxuryGold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات و یادداشت‌ها (اختیاری)") },
                    placeholder = { Text("نکات نگهداری یا شیوه مصرف") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "برنامه غذایی فعال است",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LuxuryGold,
                            checkedTrackColor = RedLinePrimary
                        )
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لطفاً نام غذا و مقدار آن را وارد کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

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
                            if (foodName.isBlank() || amount.isBlank()) {
                                showError = true
                            } else {
                                val schedule = FoodSchedule(
                                    id = existingSchedule?.id ?: 0,
                                    petId = pet.id,
                                    mealTitle = mealTitle.ifBlank { selectedMealType.titleFa },
                                    mealType = selectedMealType,
                                    foodName = foodName.trim(),
                                    amount = amount.trim(),
                                    amountUnit = selectedAmountUnit,
                                    mealTime = mealTime.ifBlank { selectedMealType.defaultHour },
                                    repeatDays = if (selectedDays.isEmpty()) DayOfWeekFa.entries.toSet() else selectedDays,
                                    isCompleted = existingSchedule?.isCompleted ?: false,
                                    isActive = isActive,
                                    supplementName = if (hasSupplement) supplementName.trim() else "",
                                    supplementAmount = if (hasSupplement) supplementAmount.trim() else "",
                                    supplementNotes = if (hasSupplement) supplementNotes.trim() else "",
                                    notes = notes.trim()
                                )
                                onSave(schedule)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("food_dialog_save_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedLinePrimary)
                    ) {
                        Text(
                            text = if (existingSchedule == null) "ثبت وعده غذایی" else "ذخیره تغییرات",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
