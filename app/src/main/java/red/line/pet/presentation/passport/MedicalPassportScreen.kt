package red.line.pet.presentation.passport

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.pet.core.theme.BurgundyPrimary
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.theme.StatusSuccess
import red.line.pet.core.ui.RedLineButton
import red.line.pet.core.ui.RedLineCard
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.PassportSection
import red.line.pet.domain.model.Pet

@Composable
fun MedicalPassportScreen(
    viewModel: MedicalPassportViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToVip: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("passport_btn_back")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "شناسنامه سلامت پتورا (PDF)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "خروجی رسمی پرونده پزشکی برای کلینیک و دامپزشک",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = LuxuryGold.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "PDF Export",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BurgundyPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pet Picker Section
                if (uiState.pets.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "انتخاب حیوان خانگی:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.pets) { pet ->
                                    val isSelected = pet.id == uiState.selectedPet?.id
                                    PetSelectorChip(
                                        pet = pet,
                                        isSelected = isSelected,
                                        onClick = { viewModel.selectPet(pet) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected Pet Profile & Metrics Card
                val pet = uiState.selectedPet
                val report = uiState.reportData
                if (pet != null) {
                    item {
                        PetPassportSummaryCard(
                            pet = pet,
                            report = report,
                            isLoading = uiState.isLoadingData
                        )
                    }
                }

                // Section Checklist
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "بخش‌های مندرج در شناسنامه:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${PersianNumberFormatter.toPersian(uiState.selectedSections.selectedCount)} از ۶ بخش",
                                style = MaterialTheme.typography.labelMedium,
                                color = BurgundyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 6 Section Cards
                        PassportSectionCheckboxCard(
                            section = PassportSection.HEALTH,
                            icon = Icons.Filled.MedicalServices,
                            countBadge = report?.healthRecords?.size?.let { "${PersianNumberFormatter.toPersian(it)} مورد" },
                            isChecked = uiState.selectedSections.includeHealth,
                            onToggle = { viewModel.toggleSection(PassportSection.HEALTH) }
                        )

                        PassportSectionCheckboxCard(
                            section = PassportSection.WEIGHT,
                            icon = Icons.Filled.FitnessCenter,
                            countBadge = report?.weightRecords?.size?.let { "${PersianNumberFormatter.toPersian(it)} رکورد" },
                            isChecked = uiState.selectedSections.includeWeight,
                            onToggle = { viewModel.toggleSection(PassportSection.WEIGHT) }
                        )

                        PassportSectionCheckboxCard(
                            section = PassportSection.FOOD,
                            icon = Icons.Filled.Restaurant,
                            countBadge = report?.foodSchedules?.size?.let { "${PersianNumberFormatter.toPersian(it)} وعده" },
                            isChecked = uiState.selectedSections.includeFood,
                            onToggle = { viewModel.toggleSection(PassportSection.FOOD) }
                        )

                        PassportSectionCheckboxCard(
                            section = PassportSection.REMINDERS,
                            icon = Icons.Filled.NotificationsActive,
                            countBadge = report?.reminders?.size?.let { "${PersianNumberFormatter.toPersian(it)} نوبت" },
                            isChecked = uiState.selectedSections.includeReminders,
                            onToggle = { viewModel.toggleSection(PassportSection.REMINDERS) }
                        )

                        PassportSectionCheckboxCard(
                            section = PassportSection.MEMORIES,
                            icon = Icons.Filled.PhotoLibrary,
                            countBadge = report?.memories?.size?.let { "${PersianNumberFormatter.toPersian(it)} تصویر" },
                            isChecked = uiState.selectedSections.includeMemories,
                            onToggle = { viewModel.toggleSection(PassportSection.MEMORIES) }
                        )

                        PassportSectionCheckboxCard(
                            section = PassportSection.EXPENSES,
                            icon = Icons.Filled.Payments,
                            countBadge = report?.expenses?.size?.let { "${PersianNumberFormatter.toPersian(it)} سابقه" },
                            isChecked = uiState.selectedSections.includeExpenses,
                            onToggle = { viewModel.toggleSection(PassportSection.EXPENSES) }
                        )
                    }
                }

                // Generated PDF Actions Card
                if (uiState.generatedFile != null) {
                    item {
                        GeneratedPdfResultCard(
                            fileName = uiState.generatedFile!!.name,
                            fileSizeBytes = uiState.generatedFile!!.length(),
                            onView = { viewModel.viewGeneratedPdf(context) },
                            onShare = { viewModel.shareGeneratedPdf(context) }
                        )
                    }
                }

                // Generate Button
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    if (uiState.isGenerating) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = BurgundyPrimary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, BurgundyPrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = BurgundyPrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "در حال نگارش و تولید شناسنامه سلامت PDF...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BurgundyPrimary
                                )
                            }
                        }
                    } else {
                        RedLineButton(
                            text = "ساخت و صدور پرونده PDF",
                            onClick = { viewModel.generatePdf(context) },
                            icon = Icons.Filled.Download,
                            enabled = uiState.selectedPet != null && uiState.selectedSections.selectedCount > 0,
                            modifier = Modifier.testTag("btn_generate_pdf")
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
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
}

@Composable
private fun PetSelectorChip(
    pet: Pet,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) BurgundyPrimary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) BurgundyPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, borderColor),
        modifier = Modifier.testTag("pet_chip_${pet.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = pet.species.emoji,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) BurgundyPrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = pet.species.titleFa,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PetPassportSummaryCard(
    pet: Pet,
    report: red.line.pet.domain.model.MedicalPassportReport?,
    isLoading: Boolean
) {
    RedLineCard(
        showRedLineAccent = true,
        modifier = Modifier.testTag("pet_passport_summary_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = LuxuryGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, LuxuryGold)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = pet.species.emoji, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BurgundyPrimary
                        )
                        val breedStr = if (pet.breed.isNotBlank()) "${pet.species.titleFa} | نژاد ${pet.breed}" else pet.species.titleFa
                        Text(
                            text = breedStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BurgundyPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = pet.gender.titleFa,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BurgundyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info rows: Age, Weight, Microchip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoBadgeItem(
                    title = "سن تقریبی",
                    value = report?.ageStringFa ?: "در حال محاسبه..."
                )
                val weightStr = if ((report?.weightStats?.currentWeightKg ?: 0.0) > 0) {
                    "${PersianNumberFormatter.toPersian(report?.weightStats?.currentWeightKg ?: 0.0)} کیلوگرم"
                } else {
                    "ثبت نشده"
                }
                InfoBadgeItem(
                    title = "وزن فعلی",
                    value = weightStr
                )
                InfoBadgeItem(
                    title = "شماره میکروچیپ",
                    value = pet.microchipId.ifBlank { "ندارد" }
                )
            }
        }
    }
}

@Composable
private fun InfoBadgeItem(
    title: String,
    value: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PassportSectionCheckboxCard(
    section: PassportSection,
    icon: ImageVector,
    countBadge: String?,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("section_card_${section.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (isChecked) BurgundyPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = BurgundyPrimary,
                    checkmarkColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isChecked) BurgundyPrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isChecked) BurgundyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = section.titleFa,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = section.descriptionFa,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (countBadge != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = countBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GeneratedPdfResultCard(
    fileName: String,
    fileSizeBytes: Long,
    onView: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_pdf_result"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.08f)),
        border = BorderStroke(1.2.dp, StatusSuccess.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = StatusSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "پرونده PDF با موفقیت صادر شد",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusSuccess
                    )
                    val sizeKb = (fileSizeBytes / 1024).coerceAtLeast(1)
                    Text(
                        text = "$fileName (${PersianNumberFormatter.toPersian(sizeKb)} کیلوبایت)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onView,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_view_pdf"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BurgundyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = BurgundyPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "مشاهده فایل",
                        style = MaterialTheme.typography.labelMedium,
                        color = BurgundyPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                RedLineButton(
                    text = "اشتراک‌گذاری",
                    onClick = onShare,
                    icon = Icons.Filled.Share,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_share_pdf")
                )
            }
        }
    }
}
