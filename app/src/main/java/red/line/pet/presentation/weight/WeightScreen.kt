package red.line.pet.presentation.weight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import red.line.pet.core.theme.BurgundyLight
import red.line.pet.core.theme.BurgundyPrimary
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.LuxuryGoldLight
import red.line.pet.core.theme.RedLineBright
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.theme.StatusSuccess
import red.line.pet.core.ui.AssetImageHelper
import red.line.pet.core.ui.EmptyStateView
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.WeightRecord
import red.line.pet.domain.model.WeightStatistics
import red.line.pet.domain.model.WeightTimeRange
import red.line.pet.domain.model.WeightUnit

@Composable
fun WeightScreen(
    viewModel: WeightViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToVip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("weight_growth_screen"),
        floatingActionButton = {
            if (uiState.selectedPet != null) {
                FloatingActionButton(
                    onClick = { viewModel.openAddWeightDialog() },
                    containerColor = RedLinePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_add_weight")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ثبت وزن جدید")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with Back Button and Pet Title
            WeightScreenHeader(onBack = onBack)

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LuxuryGold)
                }
            } else if (uiState.pets.isEmpty()) {
                EmptyStateView(
                    imageAssetPath = AssetImageHelper.EMPTY_NO_PET,
                    title = "حیوان خانگی یافت نشد",
                    description = "ابتدا یک پت در بخش حیوانات من اضافه کنید تا بتوانید روند رشد و وزن آن را پایش نمایید.",
                    actionButtonText = "بازگشت",
                    onActionClick = onBack
                )
            } else {
                val selectedPet = uiState.selectedPet ?: uiState.pets.first()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Pet Horizontal Switcher Bar
                    item {
                        PetSelectorBar(
                            pets = uiState.pets,
                            selectedPet = selectedPet,
                            onSelectPet = { viewModel.selectPet(it) }
                        )
                    }

                    // Selected Pet Header Hero Card (Pet name, avatar, current weight, last record date)
                    item {
                        PetWeightHeroCard(
                            pet = selectedPet,
                            stats = uiState.statistics
                        )
                    }

                    if (uiState.allRecords.isEmpty()) {
                        item {
                            EmptyStateView(
                                imageAssetPath = AssetImageHelper.EMPTY_NO_DATA,
                                title = "هنوز وزنی برای این پت ثبت نشده است",
                                description = "با ثبت دوره‌ای وزن، سلامت، رژیم غذایی و روند رشد دوست کوچک خود را زیر نظر بگیرید.",
                                actionButtonText = "ثبت اولین وزن",
                                onActionClick = { viewModel.openAddWeightDialog() }
                            )
                        }
                    } else {
                        // Time Range Filters (7 days, 30 days, 3 months, 6 months, 1 year, all)
                        item {
                            TimeRangeFilterRow(
                                selectedRange = uiState.selectedRange,
                                onSelectRange = { viewModel.setTimeRange(it) }
                            )
                        }

                        // Canvas Growth & Weight Chart
                        item {
                            WeightGrowthChart(
                                records = uiState.filteredRecords,
                                selectedRecord = uiState.selectedRecordPoint,
                                onSelectPoint = { viewModel.selectChartPoint(it) }
                            )
                        }

                        // Statistics Summary Card
                        item {
                            WeightStatisticsCard(
                                stats = uiState.statistics,
                                unit = WeightUnit.KILOGRAM
                            )
                        }

                        // History Section Title
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تاریخچه رکوردهای وزن",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${PersianNumberFormatter.toPersian(uiState.filteredRecords.size)} رکورد",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Weight History List Items (Newest to Oldest)
                        val records = uiState.filteredRecords
                        items(records, key = { it.id }) { record ->
                            // Calculate change compared to previous record in sequence
                            val currentIndex = records.indexOf(record)
                            val diffKg = if (currentIndex < records.size - 1) {
                                val olderRecord = records[currentIndex + 1]
                                record.weightKg - olderRecord.weightKg
                            } else null

                            WeightHistoryItemCard(
                                record = record,
                                diffFromPrevKg = diffKg,
                                onEditClick = { viewModel.openEditWeightDialog(record) },
                                onDeleteClick = { viewModel.promptDeleteRecord(record) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(70.dp))
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (uiState.isAddEditOpen && uiState.selectedPet != null) {
        AddEditWeightDialog(
            pet = uiState.selectedPet!!,
            existingRecord = uiState.editingRecord,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { weight, unit, dateStr, notes ->
                viewModel.saveWeightRecord(weight, unit, dateStr, notes)
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

    // Delete Confirmation Dialog
    uiState.deletingRecord?.let { record ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = "حذف رکورد وزن",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "آیا از حذف رکورد وزن ${record.getFormattedWeight()} ثبت‌شده در تاریخ ${record.getEffectiveJalaliDate()} اطمینان دارید؟",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDeleteRecord() },
                    modifier = Modifier.testTag("confirm_delete_weight_button")
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun WeightScreenHeader(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = 0.8.dp,
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, LuxuryGold.copy(alpha = 0.25f))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "پایش وزن و رشد",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "نمودار تحلیلی و کنترل سلامت",
                        style = MaterialTheme.typography.bodySmall,
                        color = LuxuryGold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BurgundyPrimary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Growth Pro",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LuxuryGold
                    )
                }
            }
        }
    }
}

@Composable
private fun PetSelectorBar(
    pets: List<Pet>,
    selectedPet: Pet,
    onSelectPet: (Pet) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "انتخاب پت",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(pets, key = { it.id }) { pet ->
                val isSelected = pet.id == selectedPet.id
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectPet(pet) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avatarUri = pet.avatarUri.ifBlank { pet.imagePath.ifBlank { AssetImageHelper.getSpeciesPlaceholder(pet.species) } }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(avatarUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .border(1.dp, LuxuryGold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PetWeightHeroCard(
    pet: Pet,
    stats: WeightStatistics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                listOf(LuxuryGold.copy(alpha = 0.55f), BurgundyPrimary.copy(alpha = 0.25f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BurgundyPrimary.copy(alpha = 0.09f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarUri = pet.avatarUri.ifBlank { pet.imagePath.ifBlank { AssetImageHelper.getSpeciesPlaceholder(pet.species) } }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = pet.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(2.dp, LuxuryGold, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${pet.species.emoji} ${pet.species.titleFa} ${if (pet.breed.isNotBlank()) "• ${pet.breed}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Current Weight Highlight Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BurgundyPrimary,
                    border = BorderStroke(1.dp, LuxuryGold)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "وزن فعلی",
                            style = MaterialTheme.typography.labelSmall,
                            color = LuxuryGoldLight
                        )
                        val currentW = if (stats.currentWeightKg > 0) {
                            val formatted = if (stats.currentWeightKg % 1.0 == 0.0) stats.currentWeightKg.toInt().toString() else String.format("%.2f", stats.currentWeightKg).trimEnd('0').trimEnd('.')
                            "${PersianNumberFormatter.toPersian(formatted)} کیلوگرم"
                        } else if (pet.weightKg > 0) {
                            "${PersianNumberFormatter.toPersian(pet.weightKg.toString())} کیلوگرم"
                        } else {
                            "—"
                        }
                        Text(
                            text = currentW,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (stats.lastRecordedDateJalali.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تاریخ آخرین ثبت: ${stats.lastRecordedDateJalali}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeRangeFilterRow(
    selectedRange: WeightTimeRange,
    onSelectRange: (WeightTimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeightTimeRange.entries.forEach { range ->
            val isSelected = selectedRange == range
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectRange(range) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            ) {
                Text(
                    text = range.titleFa,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun WeightStatisticsCard(
    stats: WeightStatistics,
    unit: WeightUnit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "خلاصه آماری وزن",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Min Weight Box
                StatMiniBox(
                    label = "کمترین وزن",
                    value = formatKg(stats.minWeightKg),
                    modifier = Modifier.weight(1f),
                    accentColor = BurgundyLight
                )

                // Max Weight Box
                StatMiniBox(
                    label = "بیشترین وزن",
                    value = formatKg(stats.maxWeightKg),
                    modifier = Modifier.weight(1f),
                    accentColor = LuxuryGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Change vs First
                StatMiniBox(
                    label = "تغییر از ابتدا",
                    value = formatChange(stats.totalChangeKg),
                    modifier = Modifier.weight(1f),
                    accentColor = if (stats.totalChangeKg >= 0) StatusSuccess else Color(0xFFD32F2F),
                    changeSign = stats.totalChangeKg
                )

                // Change vs Prev
                StatMiniBox(
                    label = "تغییر نسبت به قبل",
                    value = formatChange(stats.lastChangeKg),
                    modifier = Modifier.weight(1f),
                    accentColor = if (stats.lastChangeKg >= 0) StatusSuccess else Color(0xFFD32F2F),
                    changeSign = stats.lastChangeKg
                )
            }
        }
    }
}

@Composable
private fun StatMiniBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color,
    changeSign: Double? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.8.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (changeSign != null) {
                    val icon = when {
                        changeSign > 0.0 -> Icons.Default.ArrowUpward
                        changeSign < 0.0 -> Icons.Default.ArrowDownward
                        else -> Icons.Default.TrendingFlat
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WeightHistoryItemCard(
    record: WeightRecord,
    diffFromPrevKg: Double?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_history_item_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Weight Icon badge
                Surface(
                    shape = CircleShape,
                    color = BurgundyPrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.4f)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = record.getFormattedWeight(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (diffFromPrevKg != null && diffFromPrevKg != 0.0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (diffFromPrevKg > 0) StatusSuccess.copy(alpha = 0.15f) else Color(0xFFD32F2F).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = if (diffFromPrevKg > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (diffFromPrevKg > 0) StatusSuccess else Color(0xFFD32F2F),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = formatChange(diffFromPrevKg),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (diffFromPrevKg > 0) StatusSuccess else Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = record.getEffectiveJalaliDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (record.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = record.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Edit & Delete Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun formatKg(kg: Double): String {
    if (kg <= 0.0) return "۰ ک.گ"
    val formatted = if (kg % 1.0 == 0.0) kg.toInt().toString() else String.format("%.2f", kg).trimEnd('0').trimEnd('.')
    return "${PersianNumberFormatter.toPersian(formatted)} ک.گ"
}

private fun formatChange(kg: Double): String {
    val absVal = kotlin.math.abs(kg)
    val formatted = if (absVal % 1.0 == 0.0) absVal.toInt().toString() else String.format("%.2f", absVal).trimEnd('0').trimEnd('.')
    val sign = if (kg > 0) "+" else if (kg < 0) "-" else ""
    return "$sign${PersianNumberFormatter.toPersian(formatted)} ک.گ"
}
