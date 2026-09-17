package red.line.pet.presentation.health

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.pet.core.theme.BurgundyPrimary
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.theme.StatusWarning
import red.line.pet.core.ui.EmptyStateView
import red.line.pet.core.ui.RedLineButton
import red.line.pet.core.ui.RedLineCard
import red.line.pet.core.ui.RedLineHeader
import red.line.pet.core.ui.TomanBadge
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.HealthRecord
import red.line.pet.domain.model.HealthRecordType

@Composable
fun HealthScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier,
    onNavigateToPassport: () -> Unit = {},
    onNavigateToVip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddRecordClick() },
                containerColor = RedLinePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_health_record")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "ثبت واکسن یا چکاپ")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RedLineHeader(
                title = "پرونده سلامت",
                subtitle = "سوابق واکسیناسیون، داروها و یادآورهای پزشکی",
                badgeText = "سوابق سلامت"
            )

            if (uiState.pets.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.pets) { pet ->
                        val isSelected = uiState.selectedPet?.id == pet.id
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { viewModel.selectPet(pet) }
                                .testTag("health_pet_chip_${pet.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pet.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Medical Passport Export Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clickable { onNavigateToPassport() }
                    .testTag("btn_export_passport_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LuxuryGold.copy(alpha = 0.12f)
                ),
                border = BorderStroke(1.2.dp, LuxuryGold.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryGold.copy(alpha = 0.25f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.MedicalServices,
                                    contentDescription = null,
                                    tint = BurgundyPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "صدور شناسنامه و پرونده پزشکی (PDF)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BurgundyPrimary
                            )
                            Text(
                                text = "خروجی چاپی و رسمی برای کلینیک و دامپزشک",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BurgundyPrimary
                    ) {
                        Text(
                            text = "خروجی PDF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Upcoming Reminders Card
            if (uiState.upcomingReminders.isNotEmpty()) {
                UpcomingRemindersBanner(reminders = uiState.upcomingReminders)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RedLinePrimary)
                }
            } else if (uiState.records.isEmpty()) {
                EmptyStateView(
                    imageAssetPath = red.line.pet.core.ui.AssetImageHelper.EMPTY_NO_DATA,
                    title = "سابقه‌ای ثبت نشده است",
                    description = "برای ثبت واکسیناسیون، دارو یا چکاپ دوره‌ای دکمه زیر را لمس کنید.",
                    actionButtonText = "افزودن نوبت جدید",
                    onActionClick = { viewModel.onAddRecordClick() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.records, key = { it.id }) { record ->
                        HealthRecordCard(record = record)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddHealthRecordDialog(
            onDismiss = { viewModel.onDismissAddDialog() },
            onConfirm = { title, type, date, nextDue, cost, clinic, notes ->
                viewModel.addNewRecord(title, type, date, nextDue, cost, clinic, notes)
            }
        )
    }

    if (uiState.showVipDialog) {
        red.line.pet.presentation.vip.VipUpgradeRequiredDialog(
            onDismiss = { viewModel.onDismissVipDialog() },
            onUpgradeClick = {
                viewModel.onDismissVipDialog()
                onNavigateToVip()
            }
        )
    }
}

@Composable
fun UpcomingRemindersBanner(
    reminders: List<HealthRecord>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("upcoming_reminders_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = StatusWarning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "یادآور نوبت‌های پیش‌رو (تقویم شمسی)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            reminders.take(2).forEach { reminder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "• ${reminder.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = reminder.nextDueJalaliDate ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusWarning
                    )
                }
            }
        }
    }
}

@Composable
fun HealthRecordCard(
    record: HealthRecord,
    modifier: Modifier = Modifier
) {
    RedLineCard(
        modifier = modifier.testTag("health_card_${record.id}"),
        showRedLineAccent = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = record.clinicOrDoctor.ifBlank { "کلینیک ثبت نشده" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = RedLinePrimary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = record.type.titleFa,
                    style = MaterialTheme.typography.labelSmall,
                    color = RedLinePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تاریخ: ${record.jalaliDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (record.costToman > 0) {
                TomanBadge(amount = record.costToman)
            }
        }

        if (record.notes.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = record.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AddHealthRecordDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        type: HealthRecordType,
        date: String,
        nextDueDate: String?,
        cost: Long,
        clinic: String,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(HealthRecordType.VACCINE) }
    var jalaliDate by remember { mutableStateOf(JalaliDateHelper.now().toStandardString(false)) }
    var nextDueDate by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }
    var clinic by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "ثبت نوبت سلامت و واکسن",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "دسته‌بندی نوبت:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HealthRecordType.values()) { type ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedType == type) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clip(RoundedCornerShape(10.dp))
                            ) {
                                TextButton(onClick = { selectedType = type }) {
                                    Text(
                                        text = type.titleFa,
                                        color = if (selectedType == type) Color.White else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان (مثال: واکسن چندگانه)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_health_title"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = jalaliDate,
                            onValueChange = { jalaliDate = it },
                            label = { Text("تاریخ انجام (شمسی)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_health_date"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = nextDueDate,
                            onValueChange = { nextDueDate = it },
                            label = { Text("نوبت بعدی (اختیاری)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_health_next_due"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("هزینه پرداختی (تومان)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_health_cost"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = clinic,
                        onValueChange = { clinic = it },
                        label = { Text("نام کلینیک یا پزشک معالج") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_health_clinic"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توضیحات و دستورات پزشک") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_health_notes"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            RedLineButton(
                text = "ثبت پرونده",
                onClick = {
                    val cost = costText.replace(",", "").toLongOrNull() ?: 0L
                    onConfirm(
                        title,
                        selectedType,
                        PersianNumberFormatter.toPersian(jalaliDate),
                        if (nextDueDate.isBlank()) null else PersianNumberFormatter.toPersian(nextDueDate),
                        cost,
                        clinic,
                        notes
                    )
                },
                modifier = Modifier.width(130.dp)
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_health")
            ) {
                Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
