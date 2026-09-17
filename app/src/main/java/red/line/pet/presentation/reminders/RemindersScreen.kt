package red.line.pet.presentation.reminders

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import red.line.pet.core.theme.BurgundyDeep
import red.line.pet.core.theme.BurgundyLight
import red.line.pet.core.theme.BurgundyPrimary
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.LuxuryGoldLight
import red.line.pet.core.theme.StatusSuccess
import red.line.pet.core.theme.WarmBeige
import red.line.pet.core.theme.WarmBeigeBorder
import red.line.pet.core.theme.WarmBeigeVariant
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.Reminder
import red.line.pet.domain.model.ReminderRepeatType
import red.line.pet.domain.model.ReminderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToVip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }

    // Notification permission request for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("برای دریافت هشدارهای به موقع، مجوز اعلان را فعال کنید.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .testTag("reminders_screen"),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                RemindersTopBar(
                    onNavigateBack = onNavigateBack,
                    onAddClick = { viewModel.openAddDialog() }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openAddDialog() },
                    containerColor = BurgundyPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_add_reminder")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "افزودن یادآور"
                        )
                        Text(
                            text = "یادآور جدید",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(WarmBeige)
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BurgundyPrimary)
                    }
                } else {
                    val filteredReminders = remember(
                        uiState.reminders,
                        uiState.selectedPetId,
                        uiState.selectedTab
                    ) {
                        var list = uiState.reminders
                        if (uiState.selectedPetId != null && uiState.selectedPetId != 0L) {
                            list = list.filter { it.petId == uiState.selectedPetId }
                        }
                        when (uiState.selectedTab) {
                            ReminderFilterTab.ALL -> list
                            ReminderFilterTab.UPCOMING -> list.filter { it.isUpcoming() }
                            ReminderFilterTab.HEALTH -> list.filter {
                                it.reminderType in listOf(
                                    ReminderType.VACCINE,
                                    ReminderType.MEDICINE,
                                    ReminderType.CHECKUP
                                )
                            }
                            ReminderFilterTab.FOOD -> list.filter {
                                it.reminderType in listOf(
                                    ReminderType.FOOD,
                                    ReminderType.SUPPLEMENT
                                )
                            }
                            ReminderFilterTab.COMPLETED -> list.filter { it.isCompleted }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. Pet Selector Chips
                        item {
                            PetSelectionRow(
                                pets = uiState.pets,
                                selectedPetId = uiState.selectedPetId,
                                onSelectPet = { viewModel.selectPet(it) }
                            )
                        }

                        // 2. Upcoming Summary Banner
                        item {
                            UpcomingSummaryBanner(
                                upcomingList = uiState.upcomingReminders,
                                pets = uiState.pets
                            )
                        }

                        // 3. Filter Tabs
                        item {
                            FilterTabsRow(
                                selectedTab = uiState.selectedTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )
                        }

                        // 4. Reminders List
                        if (filteredReminders.isEmpty()) {
                            item {
                                EmptyRemindersState(
                                    tab = uiState.selectedTab,
                                    onAddClick = { viewModel.openAddDialog() }
                                )
                            }
                        } else {
                            items(filteredReminders, key = { it.id }) { reminder ->
                                val pet = uiState.pets.find { it.id == reminder.petId }
                                ReminderCard(
                                    reminder = reminder,
                                    pet = pet,
                                    onToggle = { isEnabled -> viewModel.toggleReminder(reminder.id, isEnabled) },
                                    onComplete = { isDone -> viewModel.completeReminder(reminder.id, isDone) },
                                    onSnooze = { viewModel.snoozeReminder(reminder.id, 30) },
                                    onEdit = { viewModel.openEditDialog(reminder) },
                                    onDelete = { reminderToDelete = reminder }
                                )
                            }
                        }
                    }
                }

                // Delete Confirmation Dialog
                reminderToDelete?.let { rem ->
                    AlertDialog(
                        onDismissRequest = { reminderToDelete = null },
                        title = {
                            Text(
                                text = "حذف یادآور",
                                fontWeight = FontWeight.Bold,
                                color = BurgundyDeep
                            )
                        },
                        text = {
                            Text(
                                text = "آیا از حذف یادآور «${rem.title}» مطمئن هستید؟",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.deleteReminder(rem.id)
                                    reminderToDelete = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary)
                            ) {
                                Text("حذف", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { reminderToDelete = null }) {
                                Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White
                    )
                }

                // Add / Edit BottomSheet
                if (uiState.showAddEditSheet) {
                    AddEditReminderSheet(
                        pets = uiState.pets,
                        editingReminder = uiState.editingReminder,
                        presetPetId = uiState.presetPetId,
                        presetType = uiState.presetType,
                        presetTitle = uiState.presetTitle,
                        onDismiss = { viewModel.dismissSheet() },
                        onSave = { id, petId, title, desc, type, date, time, repeat ->
                            viewModel.saveReminder(id, petId, title, desc, type, date, time, repeat)
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
        }
    }
}

@Composable
private fun RemindersTopBar(
    onNavigateBack: () -> Unit,
    onAddClick: () -> Unit
) {
    Surface(
        color = WarmBeige,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("btn_back_reminders")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = BurgundyDeep
                    )
                }
                Column {
                    Text(
                        text = "یادآورها و اعلان‌ها",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDeep
                        )
                    )
                    Text(
                        text = "مدیریت هوشمند و آفلاین مراقبت‌های پت",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BurgundyPrimary.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن یادآور",
                    tint = BurgundyPrimary
                )
            }
        }
    }
}

@Composable
private fun PetSelectionRow(
    pets: List<Pet>,
    selectedPetId: Long?,
    onSelectPet: (Long?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val isAllSelected = selectedPetId == null || selectedPetId == 0L
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { onSelectPet(null) }
                .testTag("chip_pet_all"),
            shape = RoundedCornerShape(20.dp),
            color = if (isAllSelected) BurgundyPrimary else Color.White,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAllSelected) BurgundyPrimary else WarmBeigeBorder
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pets,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isAllSelected) Color.White else BurgundyDeep
                )
                Text(
                    text = "همه حیوانات",
                    fontSize = 13.sp,
                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAllSelected) Color.White else BurgundyDeep
                )
            }
        }

        pets.forEach { pet ->
            val isSelected = selectedPetId == pet.id
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelectPet(pet.id) }
                    .testTag("chip_pet_${pet.id}"),
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) BurgundyPrimary else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) BurgundyPrimary else WarmBeigeBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = pet.species.emoji, fontSize = 14.sp)
                    Text(
                        text = pet.name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else BurgundyDeep
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingSummaryBanner(
    upcomingList: List<Reminder>,
    pets: List<Pet>
) {
    val countFa = PersianNumberFormatter.toPersian(upcomingList.size.toString())
    val nearest = upcomingList.firstOrNull()
    val nearestPet = if (nearest != null) pets.find { it.id == nearest.petId } else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_summary_banner"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(BurgundyDeep, BurgundyPrimary, BurgundyLight)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "نوبت‌های پیش‌رو ($countFa مورد)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (nearest != null) {
                        Text(
                            text = "نزدیک‌ترین: ${nearest.title}" + (if (nearestPet != null) " (${nearestPet.name})" else ""),
                            fontSize = 13.sp,
                            color = LuxuryGoldLight,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = nearest.getFormattedDateTimeFa(),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    } else {
                        Text(
                            text = "در حال حاضر هیچ نوبت معوق یا نزدیک ۴۸ ساعتی وجود ندارد.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(LuxuryGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterTabsRow(
    selectedTab: ReminderFilterTab,
    onTabSelected: (ReminderFilterTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReminderFilterTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onTabSelected(tab) }
                    .testTag("tab_${tab.name}"),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) BurgundyDeep else WarmBeigeVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) BurgundyDeep else WarmBeigeBorder
                )
            ) {
                Text(
                    text = tab.titleFa,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    pet: Pet?,
    onToggle: (Boolean) -> Unit,
    onComplete: (Boolean) -> Unit,
    onSnooze: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeColor, typeIcon) = getTypeColorAndIcon(reminder.reminderType)
    val isOverdue = reminder.isOverdue()
    val isUpcoming = reminder.isUpcoming()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Type Badge, Pet Name, Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(typeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = reminder.reminderType.titleFa,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                        if (pet != null) {
                            Text(
                                text = "${pet.species.emoji} ${pet.name}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (reminder.isCompleted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "تکمیل شده",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BurgundyLight.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "زمان گذشته",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BurgundyLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Switch(
                        checked = reminder.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BurgundyPrimary,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = WarmBeigeVariant
                        ),
                        modifier = Modifier.testTag("switch_reminder_${reminder.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title & Description
            Text(
                text = reminder.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            )

            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Repeat info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmBeigeVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = BurgundyPrimary
                    )
                    Text(
                        text = reminder.getFormattedDateTimeFa(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BurgundyDeep
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Text(
                        text = "تکرار: ${reminder.repeatType.titleFa}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mark Done / Undo
                    Button(
                        onClick = { onComplete(!reminder.isCompleted) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (reminder.isCompleted) WarmBeigeVariant else StatusSuccess
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_complete_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = if (reminder.isCompleted) Icons.Default.Refresh else Icons.Default.Check,
                            contentDescription = null,
                            tint = if (reminder.isCompleted) BurgundyDeep else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (reminder.isCompleted) "انجام نشده" else "انجام شد",
                            fontSize = 12.sp,
                            color = if (reminder.isCompleted) BurgundyDeep else Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Snooze Button
                    if (!reminder.isCompleted) {
                        OutlinedButton(
                            onClick = onSnooze,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_snooze_${reminder.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Snooze,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "۳۰ دقیقه بعد",
                                fontSize = 11.sp,
                                color = LuxuryGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Edit & Delete
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("btn_edit_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("btn_delete_${reminder.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = BurgundyLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRemindersState(
    tab: ReminderFilterTab,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(BurgundyPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = BurgundyPrimary,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (tab) {
                ReminderFilterTab.ALL -> "هنوز هیچ یادآوری ثبت نشده است"
                ReminderFilterTab.UPCOMING -> "هیچ یادآور پیش‌رویی ندارید"
                ReminderFilterTab.HEALTH -> "هیچ یادآور سلامت یا دارویی ثبت نشده است"
                ReminderFilterTab.FOOD -> "هیچ یادآور وعده غذایی یا مکمل ثبت نشده است"
                ReminderFilterTab.COMPLETED -> "هنوز یادآور تکمیل‌شده‌ای وجود ندارد"
            },
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BurgundyDeep
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "برای واکسیناسیون، مصرف دارو، نوبت غذا یا ثبت وزن، یادآور آفلاین تنظیم کنید تا به موقع مطلع شوید.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ثبت اولین یادآور", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditReminderSheet(
    pets: List<Pet>,
    editingReminder: Reminder?,
    presetPetId: Long?,
    presetType: ReminderType?,
    presetTitle: String,
    onDismiss: () -> Unit,
    onSave: (Long, Long, String, String, ReminderType, String, String, ReminderRepeatType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedPetId by remember {
        mutableLongStateOf(
            editingReminder?.petId ?: presetPetId ?: pets.firstOrNull()?.id ?: 0L
        )
    }
    var title by remember {
        mutableStateOf(editingReminder?.title ?: presetTitle)
    }
    var description by remember {
        mutableStateOf(editingReminder?.description ?: "")
    }
    var selectedType by remember {
        mutableStateOf(editingReminder?.reminderType ?: presetType ?: ReminderType.CUSTOM)
    }
    var jalaliDate by remember {
        mutableStateOf(
            editingReminder?.jalaliDate?.ifBlank { JalaliDateHelper.now().toStandardString(false) }
                ?: JalaliDateHelper.now().toStandardString(false)
        )
    }
    var timeString by remember {
        mutableStateOf(editingReminder?.timeString ?: "08:30")
    }
    var selectedRepeat by remember {
        mutableStateOf(editingReminder?.repeatType ?: ReminderRepeatType.ONCE)
    }

    var titleError by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("add_edit_reminder_sheet")
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (editingReminder == null) "ثبت یادآور جدید" else "ویرایش یادآور",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = BurgundyDeep
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Select Pet
                    item {
                        Text(
                            text = "انتخاب حیوان خانگی:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pets.forEach { pet ->
                                val isSelected = selectedPetId == pet.id
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedPetId = pet.id },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) BurgundyPrimary else WarmBeigeVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) BurgundyPrimary else WarmBeigeBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = pet.species.emoji)
                                        Text(
                                            text = pet.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else BurgundyDeep
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Select Type
                    item {
                        Text(
                            text = "نوع یادآور:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderType.entries.forEach { type ->
                                val isSelected = selectedType == type
                                val (color, icon) = getTypeColorAndIcon(type)
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedType = type },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) color else WarmBeigeVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) color else WarmBeigeBorder
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (isSelected) Color.White else color
                                        )
                                        Text(
                                            text = type.titleFa,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Title Input
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = false
                            },
                            label = { Text("عنوان یادآور (مثلاً واکسن هاری، غذای خشک، قرص...)") },
                            isError = titleError,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_reminder_title"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BurgundyPrimary,
                                focusedLabelColor = BurgundyPrimary
                            )
                        )
                    }

                    // 4. Description Input
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("توضیحات و نکات تکمیلی (اختیاری)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BurgundyPrimary,
                                focusedLabelColor = BurgundyPrimary
                            )
                        )
                    }

                    // 5. Jalali Date & Quick Presets
                    item {
                        Column {
                            Text(
                                text = "تاریخ شمسی یادآوری:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Quick Date Presets
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val now = System.currentTimeMillis()
                                val presets = listOf(
                                    "امروز" to JalaliDateHelper.formatTimestampToJalali(now, false),
                                    "فردا" to JalaliDateHelper.formatTimestampToJalali(now + 24 * 60 * 60 * 1000L, false),
                                    "هفته بعد" to JalaliDateHelper.formatTimestampToJalali(now + 7 * 24 * 60 * 60 * 1000L, false),
                                    "ماه بعد" to JalaliDateHelper.formatTimestampToJalali(now + 30 * 24 * 60 * 60 * 1000L, false)
                                )
                                presets.forEach { (label, dVal) ->
                                    val isSelected = jalaliDate == dVal
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { jalaliDate = dVal },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) BurgundyDeep else WarmBeigeVariant
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else BurgundyDeep,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = jalaliDate,
                                onValueChange = { jalaliDate = it },
                                label = { Text("تاریخ (مثال: ۱۴۰۳/۰۶/۲۵)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reminder_date"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BurgundyPrimary,
                                    focusedLabelColor = BurgundyPrimary
                                )
                            )
                        }
                    }

                    // 6. Time & Quick Presets
                    item {
                        Column {
                            Text(
                                text = "ساعت یادآوری:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val timePresets = listOf(
                                    "صبح ۰۸:۳۰" to "08:30",
                                    "ظهر ۱۳:۰۰" to "13:00",
                                    "عصر ۱۸:۰۰" to "18:00",
                                    "شب ۲۱:۳۰" to "21:30"
                                )
                                timePresets.forEach { (label, tVal) ->
                                    val isSelected = timeString == tVal
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { timeString = tVal },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) BurgundyDeep else WarmBeigeVariant
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else BurgundyDeep,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = timeString,
                                onValueChange = { timeString = it },
                                label = { Text("ساعت (مثال: 08:30)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_reminder_time"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BurgundyPrimary,
                                    focusedLabelColor = BurgundyPrimary
                                )
                            )
                        }
                    }

                    // 7. Repeat Selector
                    item {
                        Text(
                            text = "نحوه تکرار یادآور:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderRepeatType.entries.forEach { repeat ->
                                val isSelected = selectedRepeat == repeat
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedRepeat = repeat },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) BurgundyPrimary else WarmBeigeVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) BurgundyPrimary else WarmBeigeBorder
                                    )
                                ) {
                                    Text(
                                        text = repeat.titleFa,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else BurgundyDeep
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = true
                            return@Button
                        }
                        onSave(
                            editingReminder?.id ?: 0L,
                            selectedPetId,
                            title,
                            description,
                            selectedType,
                            jalaliDate,
                            timeString,
                            selectedRepeat
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_reminder"),
                    colors = ButtonDefaults.buttonColors(containerColor = BurgundyPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (editingReminder == null) "ثبت و فعال‌سازی یادآور" else "ذخیره تغییرات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun getTypeColorAndIcon(type: ReminderType): Pair<Color, ImageVector> {
    return when (type) {
        ReminderType.VACCINE -> Pair(Color(0xFF00897B), Icons.Default.MedicalServices)
        ReminderType.MEDICINE -> Pair(Color(0xFFD81B60), Icons.Default.LocalPharmacy)
        ReminderType.CHECKUP -> Pair(Color(0xFF1E88E5), Icons.Default.Favorite)
        ReminderType.FOOD -> Pair(Color(0xFFFB8C00), Icons.Default.Restaurant)
        ReminderType.SUPPLEMENT -> Pair(Color(0xFF43A047), Icons.Default.Spa)
        ReminderType.WEIGHT -> Pair(Color(0xFF8E24AA), Icons.Default.Scale)
        ReminderType.GROOMING -> Pair(Color(0xFF6D4C41), Icons.Default.Spa)
        ReminderType.CUSTOM -> Pair(BurgundyPrimary, Icons.Default.Notifications)
    }
}
