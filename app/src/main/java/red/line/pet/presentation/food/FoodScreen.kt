package red.line.pet.presentation.food

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.LuxuryGoldLight
import red.line.pet.core.theme.RedLineBright
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.theme.StatusSuccess
import red.line.pet.core.ui.AnimalAvatar
import red.line.pet.core.ui.AssetImageHelper
import red.line.pet.core.ui.EmptyStateView
import red.line.pet.core.ui.PremiumCard
import red.line.pet.core.ui.SectionHeader
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.DayOfWeekFa
import red.line.pet.domain.model.FoodSchedule
import red.line.pet.domain.model.Pet

@Composable
fun FoodScreen(
    viewModel: FoodViewModel,
    onBack: (() -> Unit)? = null,
    onNavigateToVip: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var scheduleToDelete by remember { mutableStateOf<FoodSchedule?>(null) }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.selectedPet != null) {
                FloatingActionButton(
                    onClick = { viewModel.openAddMealDialog() },
                    containerColor = RedLinePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("food_fab_add_meal")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "افزودن وعده غذایی",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = LuxuryGold)
                }
            } else if (state.pets.isEmpty()) {
                // No pets registered at all
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        imageAssetPath = AssetImageHelper.EMPTY_NO_PET,
                        title = "ابتدا حیوان خانگی خود را ثبت کنید",
                        description = "برای تعریف برنامه غذایی و وعده‌ها، ابتدا باید حداقل یک حیوان خانگی در برنامه ثبت کرده باشید.",
                        actionButtonText = "بازگشت به خانه",
                        onActionClick = { onBack?.invoke() }
                    )
                }
            } else {
                val currentPet = state.selectedPet ?: state.pets.first()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top App Bar / Header
                    item {
                        FoodTopBar(
                            onBack = onBack,
                            onAddClick = { viewModel.openAddMealDialog() }
                        )
                    }

                    // Pet Selector Horizontal Chips
                    item {
                        PetSelectorSection(
                            pets = state.pets,
                            selectedPet = currentPet,
                            onPetSelected = { viewModel.selectPet(it) }
                        )
                    }

                    // Selected Pet Quick Card & Today's Nutrition Summary
                    item {
                        FoodTodaySummaryCard(
                            pet = currentPet,
                            totalMeals = state.totalTodayMealsCount,
                            completedMeals = state.completedTodayMealsCount,
                            progress = state.todayProgress
                        )
                    }

                    // Day of Week Filter Bar
                    item {
                        DayFilterSection(
                            selectedDay = state.selectedDayFilter,
                            onDaySelected = { viewModel.setDayFilter(it) }
                        )
                    }

                    // Meal Cards Section Header
                    item {
                        val headerTitle = if (state.selectedDayFilter == null) {
                            "همه وعده‌های غذایی (${PersianNumberFormatter.toPersian(state.displayedSchedules.size)})"
                        } else {
                            "برنامه غذایی ${state.selectedDayFilter?.titleFa ?: ""} (${PersianNumberFormatter.toPersian(state.displayedSchedules.size)})"
                        }

                        SectionHeader(
                            title = headerTitle,
                            subtitle = if (state.selectedDayFilter == null) "مدیریت و مشاهده کل برنامه هفتگی" else "وعده‌های فعال در این روز",
                            actionText = "افزودن وعده",
                            onActionClick = { viewModel.openAddMealDialog() }
                        )
                    }

                    // Meal List or Empty State
                    if (state.displayedSchedules.isEmpty()) {
                        item {
                            EmptyFoodState(
                                petName = currentPet.name,
                                isFiltered = state.selectedDayFilter != null,
                                onAddClick = { viewModel.openAddMealDialog() }
                            )
                        }
                    } else {
                        items(state.displayedSchedules, key = { it.id }) { schedule ->
                            MealScheduleCard(
                                schedule = schedule,
                                onToggleCompleted = { viewModel.toggleMealCompleted(schedule) },
                                onToggleActive = { viewModel.toggleMealActive(schedule) },
                                onEditClick = { viewModel.openEditMealDialog(schedule) },
                                onDeleteClick = { scheduleToDelete = schedule }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Meal Dialog
    if (state.isAddEditOpen && state.selectedPet != null) {
        FoodMealDialog(
            pet = state.selectedPet!!,
            existingSchedule = state.editingSchedule,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { schedule -> viewModel.saveMeal(schedule) }
        )
    }

    if (state.showVipDialog) {
        red.line.pet.presentation.vip.VipUpgradeRequiredDialog(
            onDismiss = { viewModel.onDismissVipDialog() },
            onUpgradeClick = {
                viewModel.onDismissVipDialog()
                onNavigateToVip()
            }
        )
    }

    // Delete Confirmation Dialog
    scheduleToDelete?.let { schedule ->
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("حذف وعده غذایی", fontWeight = FontWeight.Bold) },
            text = { Text("آیا از حذف وعده «${schedule.mealTitle.ifBlank { schedule.foodName }}» برای ${state.selectedPet?.name ?: ""} اطمینان دارید؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMeal(schedule.id)
                        scheduleToDelete = null
                    }
                ) {
                    Text("حذف", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun FoodTopBar(
    onBack: (() -> Unit)?,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column {
                Text(
                    text = "برنامه غذایی و تغذیه",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "مدیریت هوشمند رژیم، مکمل‌ها و ساعات مصرف",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(RedLinePrimary, LuxuryGold.copy(alpha = 0.8f)))
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "افزودن وعده",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun PetSelectorSection(
    pets: List<Pet>,
    selectedPet: Pet,
    onPetSelected: (Pet) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "انتخاب حیوان خانگی:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pets, key = { it.id }) { pet ->
                val isSelected = pet.id == selectedPet.id
                Surface(
                    modifier = Modifier
                        .clickable { onPetSelected(pet) }
                        .testTag("food_pet_chip_${pet.id}"),
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) RedLinePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        brush = if (isSelected) {
                            Brush.linearGradient(listOf(LuxuryGold, RedLinePrimary))
                        } else {
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), Color.Transparent))
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimalAvatar(
                            imageUri = pet.avatarUri.ifBlank { pet.imagePath },
                            species = pet.species,
                            size = 38.dp,
                            showBorder = isSelected,
                            borderColor = LuxuryGold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = pet.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = pet.species.titleFa,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodTodaySummaryCard(
    pet: Pet,
    totalMeals: Int,
    completedMeals: Int,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    val todayJalali = JalaliDateHelper.now().toFullString()
    val currentDayFa = JalaliDateHelper.getCurrentDayOfWeekFa().titleFa

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .testTag("food_today_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(LuxuryGold.copy(alpha = 0.5f), RedLinePrimary.copy(alpha = 0.25f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(RedLinePrimary.copy(alpha = 0.85f), LuxuryGold.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "خلاصه تغذیه امروز ($currentDayFa)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = todayJalali,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Progress Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (totalMeals > 0 && completedMeals == totalMeals) {
                        StatusSuccess.copy(alpha = 0.18f)
                    } else {
                        LuxuryGold.copy(alpha = 0.18f)
                    },
                    border = BorderStroke(
                        1.dp,
                        if (totalMeals > 0 && completedMeals == totalMeals) StatusSuccess else LuxuryGold
                    )
                ) {
                    Text(
                        text = if (totalMeals == 0) "بدون وعده" else "${PersianNumberFormatter.toPersian((progress * 100).toInt())}% انجام‌شده",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (totalMeals > 0 && completedMeals == totalMeals) StatusSuccess else LuxuryGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "میزان مصرف امروز:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${PersianNumberFormatter.toPersian(completedMeals)} از ${PersianNumberFormatter.toPersian(totalMeals)} وعده",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (completedMeals == totalMeals && totalMeals > 0) StatusSuccess else RedLinePrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayFilterSection(
    selectedDay: DayOfWeekFa?,
    onDaySelected: (DayOfWeekFa?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All Days" pill
        val isAllSelected = selectedDay == null
        Surface(
            modifier = Modifier.clickable { onDaySelected(null) },
            shape = RoundedCornerShape(12.dp),
            color = if (isAllSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(
                1.dp,
                if (isAllSelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            )
        ) {
            Text(
                text = "همه روزها",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }

        // Each Day of week pill
        DayOfWeekFa.entries.forEach { day ->
            val isSelected = selectedDay == day
            Surface(
                modifier = Modifier.clickable { onDaySelected(day) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) LuxuryGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Text(
                    text = day.titleFa,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
private fun MealScheduleCard(
    schedule: FoodSchedule,
    onToggleCompleted: () -> Unit,
    onToggleActive: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isCompleted = schedule.isCompleted
    val isAllDays = schedule.repeatDays.size == DayOfWeekFa.entries.size

    val cardBorderBrush = if (isCompleted) {
        Brush.linearGradient(listOf(StatusSuccess.copy(alpha = 0.6f), Color.Transparent))
    } else {
        Brush.linearGradient(listOf(LuxuryGold.copy(alpha = 0.4f), RedLinePrimary.copy(alpha = 0.2f)))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .testTag("meal_schedule_card_${schedule.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
        border = BorderStroke(1.dp, cardBorderBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Time + Meal Title + Status Checkbox & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Time pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(0.8.dp, LuxuryGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = PersianNumberFormatter.toPersian(schedule.mealTime),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = schedule.mealTitle.ifBlank { schedule.mealType.titleFa },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.None else null
                    )
                }

                // Completion Toggle Button
                Surface(
                    modifier = Modifier
                        .clickable { onToggleCompleted() }
                        .testTag("toggle_meal_completed_${schedule.id}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCompleted) StatusSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isCompleted) StatusSuccess else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isCompleted) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isCompleted) "انجام شده" else "انجام نشده",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) StatusSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Food Name & Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.foodName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (schedule.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = schedule.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = RedLinePrimary.copy(alpha = 0.1f),
                    border = BorderStroke(0.8.dp, RedLinePrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${PersianNumberFormatter.toPersian(schedule.amount)} ${schedule.amountUnit.titleFa}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = RedLinePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Supplement Row if present
            if (schedule.supplementName.isNotBlank() || schedule.supplementAmount.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LuxuryGold.copy(alpha = 0.1f),
                    border = BorderStroke(0.8.dp, LuxuryGold.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مکمل: ${schedule.supplementName}${if (schedule.supplementAmount.isNotBlank()) " (${schedule.supplementAmount})" else ""}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (schedule.supplementNotes.isNotBlank()) {
                            Text(
                                text = " • ${schedule.supplementNotes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Days & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Repeat Days Summary
                Text(
                    text = if (isAllDays) {
                        "تکرار: همه روزهای هفته"
                    } else {
                        "تکرار: ${schedule.repeatDays.joinToString("، ") { it.titleFa }}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Edit & Delete Action Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
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
                        modifier = Modifier.size(32.dp)
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
}

@Composable
private fun EmptyFoodState(
    petName: String,
    isFiltered: Boolean,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LuxuryGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isFiltered) "هیچ وعده‌ای برای این روز ثبت نشده است" else "هنوز برنامه غذایی برای $petName تعریف نشده است",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isFiltered) "می‌توانید وعده‌های جدیدی برای این روز مشخص اضافه کنید." else "با ثبت وعده‌های منظم، ساعت، نوع غذا و مکمل‌های غذایی، سلامت تغذیه حیوان خود را بهبود بخشید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                modifier = Modifier
                    .clickable { onAddClick() }
                    .testTag("empty_state_add_meal_button"),
                shape = RoundedCornerShape(14.dp),
                color = RedLinePrimary
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "افزودن اولین وعده غذایی",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
