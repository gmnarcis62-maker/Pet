package red.line.pet.presentation.pets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import red.line.pet.core.ui.AnimalAvatar
import red.line.pet.core.ui.AssetImageHelper
import red.line.pet.core.ui.DashboardSkeletonLoading
import red.line.pet.core.ui.EmptyStateView
import red.line.pet.core.ui.ImageCard
import red.line.pet.core.ui.LuxuryStatCard
import red.line.pet.core.ui.PetCard
import red.line.pet.core.ui.PetSpeciesChip
import red.line.pet.core.ui.PremiumCard
import red.line.pet.core.ui.RedLineButton
import red.line.pet.core.ui.RedLineCard
import red.line.pet.core.ui.RedLineHeader
import red.line.pet.core.ui.SectionHeader
import red.line.pet.core.util.JalaliDateHelper
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.Pet
import red.line.pet.domain.model.PetGender
import red.line.pet.domain.model.PetSpecies

@Composable
fun PetsScreen(
    viewModel: PetsViewModel,
    modifier: Modifier = Modifier,
    onNavigateToFood: () -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToWeight: () -> Unit = {},
    onNavigateToMemories: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToMedicalPassport: () -> Unit = {},
    onNavigateToVip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPetForProfile by remember { mutableStateOf<Pet?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddPetClick() },
                containerColor = RedLinePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_add_pet")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن حیوان جدید")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RedLineHeader(
                title = "پتورا",
                subtitle = "مراقبت لوکس و هوشمند از حیوانات خانگی",
                badgeText = "Petora Premium"
            )

            if (uiState.isLoading) {
                DashboardSkeletonLoading()
            } else if (uiState.pets.isEmpty()) {
                EmptyStateView(
                    imageAssetPath = AssetImageHelper.EMPTY_NO_PET,
                    title = "هنوز حیوانی ثبت نکرده‌اید",
                    description = "پرونده سلامت دوست کوچک خود را ایجاد کنید و از مراقبت‌های هوشمند، یادآورها و ثبت لحظات لذت ببرید.",
                    actionButtonText = "افزودن اولین حیوان خانگی",
                    onActionClick = { viewModel.onAddPetClick() }
                )
            } else {
                val activePet = uiState.pets.first()

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Emotional Centerpiece Hero Card
                    item {
                        DashboardHeroCard(
                            pet = activePet,
                            onClick = { selectedPetForProfile = activePet }
                        )
                    }

                    // Quick Access Feature Cards (Horizontal Scrollable Image Cards)
                    item {
                        QuickAccessImageCardsRow(
                            onHealthClick = onNavigateToHealth,
                            onFoodClick = onNavigateToFood,
                            onExpensesClick = onNavigateToExpenses,
                            onWeightClick = onNavigateToWeight,
                            onMemoryClick = onNavigateToMemories,
                            onRemindersClick = onNavigateToReminders,
                            onMedicalPassportClick = onNavigateToMedicalPassport,
                            onDefaultClick = { selectedPetForProfile = activePet }
                        )
                    }

                    // Section Header for Pet Companions List
                    item {
                        SectionHeader(
                            title = "همراهان دوست‌داشتنی",
                            subtitle = "${PersianNumberFormatter.toPersian(uiState.pets.size)} پت تحت مراقبت",
                            actionText = "+ افزودن پت جدید",
                            onActionClick = { viewModel.onAddPetClick() }
                        )
                    }

                    items(uiState.pets, key = { it.id }) { pet ->
                        PetCard(
                            pet = pet,
                            onClick = { selectedPetForProfile = pet },
                            onDeleteClick = { viewModel.deletePet(pet.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddPetDialog(
            onDismiss = { viewModel.onDismissAddDialog() },
            onConfirm = { name, species, breed, birthDate, gender, weight, notes, avatarUri ->
                viewModel.addNewPet(name, species, breed, birthDate, gender, weight, notes, avatarUri)
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

    selectedPetForProfile?.let { pet ->
        PetProfileDialog(
            pet = pet,
            onDismiss = { selectedPetForProfile = null },
            onDelete = { viewModel.deletePet(pet.id) },
            onUpdateAvatar = { newUri -> viewModel.updatePetAvatar(pet.id, newUri) },
            onNavigateToHealth = onNavigateToHealth,
            onNavigateToFood = onNavigateToFood,
            onNavigateToWeight = onNavigateToWeight,
            onNavigateToExpenses = onNavigateToExpenses,
            onNavigateToMemories = onNavigateToMemories,
            onNavigateToMedicalPassport = onNavigateToMedicalPassport
        )
    }
}

/**
 * High-End Dashboard Hero Card.
 * Displays personal greeting, prominent pet portrait, health status, weight, and vaccine reminder.
 */
@Composable
fun DashboardHeroCard(
    pet: Pet,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveUri = pet.avatarUri.ifBlank {
        pet.imagePath.ifBlank {
            AssetImageHelper.getSpeciesPlaceholder(pet.species)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("dashboard_hero_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                listOf(
                    LuxuryGold.copy(alpha = 0.55f),
                    RedLinePrimary.copy(alpha = 0.2f),
                    Color.Transparent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Header Row: Greeting & Big Pet Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Avatar with Gold border & Coil
                Box(
                    modifier = Modifier.size(84.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                2.dp,
                                Brush.linearGradient(listOf(LuxuryGold, RedLinePrimary)),
                                RoundedCornerShape(24.dp)
                            )
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(effectiveUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LuxuryGold.copy(alpha = 0.15f),
                        border = BorderStroke(0.8.dp, LuxuryGold.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "● حیوان برگزیده امروز",
                            style = MaterialTheme.typography.labelSmall,
                            color = LuxuryGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "سلام،",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "امروز مراقب ${pet.name} باشید",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${pet.species.titleFa} • ${pet.breed.ifBlank { "نژاد مشخص نشده" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Two Care Metric Cards (Weight & Next Vaccine)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Weight Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Scale,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "آخرین وزن",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${PersianNumberFormatter.toPersian(String.format("%.1f", pet.weightKg))} کیلوگرم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Next Vaccine Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = BorderStroke(0.8.dp, LuxuryGold.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = RedLinePrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "واکسن بعدی",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "۱۵ روز دیگر",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RedLinePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Today's Status Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = StatusSuccess.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StatusSuccess)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "وضعیت امروز: سالم و پرانرژی",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusSuccess
                        )
                    }
                    Text(
                        text = "مشاهده پرونده ←",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Quick access image cards row showcasing specialized services with WebP illustrations.
 */
@Composable
fun QuickAccessImageCardsRow(
    onHealthClick: () -> Unit = {},
    onFoodClick: () -> Unit = {},
    onExpensesClick: () -> Unit = {},
    onWeightClick: () -> Unit = {},
    onMemoryClick: () -> Unit = {},
    onRemindersClick: () -> Unit = {},
    onMedicalPassportClick: () -> Unit = {},
    onDefaultClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = "خدمات و مراقبت‌های ویژه",
            subtitle = "دسترسی سریع به امکانات اختصاصی پتورا"
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ImageCard(
                    title = "شناسنامه سلامت (PDF)",
                    subtitle = "خروجی پرونده دامپزشک",
                    imageAssetPath = AssetImageHelper.CARD_MEDICAL,
                    badgeText = "رسمی",
                    accentColor = LuxuryGold,
                    modifier = Modifier.width(175.dp),
                    height = 125.dp,
                    onClick = onMedicalPassportClick
                )
            }
            item {
                ImageCard(
                    title = "یادآورها و اعلان‌ها",
                    subtitle = "واکسن، دارو و مراقبت",
                    imageAssetPath = AssetImageHelper.CARD_MEDICAL,
                    badgeText = "هوشمند",
                    accentColor = BurgundyPrimary,
                    modifier = Modifier.width(175.dp),
                    height = 125.dp,
                    onClick = onRemindersClick
                )
            }
            item {
                ImageCard(
                    title = "پرونده سلامت",
                    subtitle = "واکسن‌ها و چکاپ",
                    imageAssetPath = AssetImageHelper.CARD_MEDICAL,
                    badgeText = "پزشکی",
                    accentColor = BurgundyPrimary,
                    modifier = Modifier.width(175.dp),
                    height = 125.dp,
                    onClick = onHealthClick
                )
            }
            item {
                ImageCard(
                    title = "برنامه تغذیه",
                    subtitle = "رژیم و مکمل‌ها",
                    imageAssetPath = AssetImageHelper.CARD_FOOD,
                    badgeText = "تغذیه",
                    accentColor = LuxuryGold,
                    modifier = Modifier.width(175.dp),
                    height = 125.dp,
                    onClick = onFoodClick
                )
            }
            item {
                ImageCard(
                    title = "نمودار وزن",
                    subtitle = "پایش رشد روزانه",
                    imageAssetPath = AssetImageHelper.CARD_WEIGHT,
                    badgeText = "وزن",
                    accentColor = StatusSuccess,
                    modifier = Modifier.width(175.dp),
                    height = 125.dp,
                    onClick = onWeightClick
                )
            }
            item {
                ImageCard(
                    title = "مدیریت هزینه‌ها",
                    subtitle = "ریز مخارج و خدمات",
                    imageAssetPath = AssetImageHelper.CARD_EXPENSE,
                    badgeText = "مالی",
                    accentColor = BurgundyLight,
                    modifier = Modifier.width(175.dp),
                    height = 125.dp,
                    onClick = onExpensesClick
                )
            }
            item {
                ImageCard(
                    title = "ثبت خاطرات",
                    subtitle = "لحظات شیرین و عکس‌ها",
                    imageAssetPath = AssetImageHelper.CARD_MEMORY,
                    badgeText = "آلبوم",
                    accentColor = Color(0xFF6B4E71),
                    modifier = Modifier.width(175.dp),
                    height = 125.dp,
                    onClick = onMemoryClick
                )
            }
        }
    }
}

@Composable
fun PetItemCard(
    pet: Pet,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    RedLineCard(
        modifier = modifier.testTag("pet_card_${pet.id}"),
        showRedLineAccent = true
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animal Avatar from Asset/URI via Coil
            AnimalAvatar(
                imageUri = pet.avatarUri.ifBlank { pet.imagePath },
                species = pet.species,
                size = 56.dp,
                statusColor = StatusSuccess
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = RedLinePrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = pet.gender.titleFa,
                            style = MaterialTheme.typography.labelSmall,
                            color = RedLinePrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${pet.species.titleFa} • ${pet.breed}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = pet.birthDateJalali,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Scale,
                            contentDescription = null,
                            tint = RedLinePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = PersianNumberFormatter.formatWeight(pet.weightKg),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_pet_${pet.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "حذف",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        species: PetSpecies,
        breed: String,
        birthDateJalali: String,
        gender: PetGender,
        weightKg: Double,
        notes: String,
        avatarUri: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedSpecies by remember { mutableStateOf(PetSpecies.DOG) }
    var breed by remember { mutableStateOf("") }
    var birthDateJalali by remember { mutableStateOf(JalaliDateHelper.now().toStandardString(false)) }
    var gender by remember { mutableStateOf(PetGender.MALE) }
    var weightText by remember { mutableStateOf("3.5") }
    var notes by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf("") }
    var showAvatarPicker by remember { mutableStateOf(false) }

    val displayImage = if (avatarUri.isNotBlank()) {
        avatarUri
    } else {
        AssetImageHelper.getSpeciesPlaceholder(selectedSpecies)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "افزودن حیوان خانگی جدید",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Avatar Preview & Selector
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.linearGradient(listOf(RedLinePrimary, LuxuryGold)),
                                    shape = CircleShape
                                )
                                .clickable { showAvatarPicker = true }
                                .testTag("btn_select_pet_avatar_form"),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(displayImage)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "تصویر حیوان",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )

                            // Floating Camera Badge
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(RedLinePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "انتخاب تصویر",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (avatarUri.isNotBlank()) "تغییر تصویر انتخابی" else "افزودن عکس (دوربین، گالری یا آواتار)",
                            style = MaterialTheme.typography.labelSmall,
                            color = LuxuryGold,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showAvatarPicker = true }
                        )
                    }
                }

                item {
                    Text(
                        text = "نوع گونه حیوان:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PetSpecies.values()) { sp ->
                            PetSpeciesChip(
                                species = sp,
                                selected = selectedSpecies == sp,
                                onClick = { selectedSpecies = sp }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = null
                        },
                        label = { Text("نام حیوان (اجباری)") },
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) {
                                Text(text = nameError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_pet_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedLinePrimary,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("نژاد (اختیاری)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_pet_breed"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = birthDateJalali,
                            onValueChange = { birthDateJalali = it },
                            label = { Text("تاریخ تولد (شمسی)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_pet_birth_date"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("وزن (کیلوگرم)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_pet_weight"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "جنسیت:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PetGender.values().forEach { g ->
                            val isSelected = gender == g
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) RedLinePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) BorderStroke(1.dp, LuxuryGold) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .testTag("gender_option_${g.name}")
                            ) {
                                TextButton(onClick = { gender = g }) {
                                    Text(
                                        text = g.titleFa,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("توضیحات و نکات مهم سلامت یا رفتاری") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_pet_notes"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedLinePrimary),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 3
                    )
                }
            }
        },
        confirmButton = {
            RedLineButton(
                text = "ثبت و ایجاد پرونده",
                onClick = {
                    if (name.isBlank()) {
                        nameError = "لطفاً نام حیوان خانگی را وارد کنید"
                        return@RedLineButton
                    }
                    val weight = weightText.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        name.trim(),
                        selectedSpecies,
                        breed.trim(),
                        PersianNumberFormatter.toPersian(birthDateJalali),
                        gender,
                        weight,
                        notes.trim(),
                        avatarUri
                    )
                },
                modifier = Modifier.width(160.dp)
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_add_pet")
            ) {
                Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )

    if (showAvatarPicker) {
        PetImagePickerBottomSheet(
            currentImageUri = displayImage,
            onImageSelected = { chosenUri ->
                avatarUri = chosenUri
                showAvatarPicker = false
            },
            onDismiss = { showAvatarPicker = false }
        )
    }
}
