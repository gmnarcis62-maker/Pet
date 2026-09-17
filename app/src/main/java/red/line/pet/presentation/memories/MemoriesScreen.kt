package red.line.pet.presentation.memories

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import red.line.pet.core.theme.BurgundyDeep
import red.line.pet.core.theme.BurgundyPrimary
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.ui.AssetImageHelper
import red.line.pet.core.ui.EmptyStateView
import red.line.pet.core.ui.RedLineHeader
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.Memory
import red.line.pet.domain.model.MemorySortType
import red.line.pet.domain.model.Pet

@Composable
fun MemoriesScreen(
    viewModel: MemoriesViewModel,
    onBack: () -> Unit = {},
    onNavigateToVip: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isSortMenuOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (uiState.selectedPet != null) {
                FloatingActionButton(
                    onClick = { viewModel.openAddMemoryDialog() },
                    containerColor = RedLinePrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.testTag("fab_add_memory")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "ثبت خاطره جدید"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen Header with Back & Sort Actions
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
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
                                text = "آلبوم خاطرات تصویری",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ثبت و مرور زیباترین لحظات زندگی با پت",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Search Toggle Button
                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) viewModel.onSearchQueryChanged("")
                            }
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "جستجو",
                                tint = if (isSearchExpanded) RedLinePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Sort Menu Button
                        Box {
                            IconButton(onClick = { isSortMenuOpen = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "مرتب‌سازی",
                                    tint = LuxuryGold
                                )
                            }
                            DropdownMenu(
                                expanded = isSortMenuOpen,
                                onDismissRequest = { isSortMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "جدیدترین‌ها",
                                            fontWeight = if (uiState.sortType == MemorySortType.NEWEST) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sortType == MemorySortType.NEWEST) RedLinePrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortType(MemorySortType.NEWEST)
                                        isSortMenuOpen = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "قدیمی‌ترین‌ها",
                                            fontWeight = if (uiState.sortType == MemorySortType.OLDEST) FontWeight.Bold else FontWeight.Normal,
                                            color = if (uiState.sortType == MemorySortType.OLDEST) RedLinePrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortType(MemorySortType.OLDEST)
                                        isSortMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar (Expanded state)
            AnimatedVisibility(
                visible = isSearchExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("جستجو در عنوان و متن خاطرات...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_memories"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = LuxuryGold
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "پاک کردن",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LuxuryGold,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            if (uiState.pets.isEmpty() && !uiState.isLoading) {
                EmptyStateView(
                    imageAssetPath = AssetImageHelper.EMPTY_NO_PET,
                    title = "هنوز حیوانی ثبت نشده است",
                    description = "برای ثبت خاطرات و عکس‌ها ابتدا باید یک پت به برنامه اضافه کنید.",
                    actionButtonText = "بازگشت به خانه",
                    onActionClick = onBack
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Pet Selector & Info Header Card
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            // Pet Selector Row (if multiple pets)
                            if (uiState.pets.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.pets.forEach { pet ->
                                        FilterChip(
                                            selected = uiState.selectedPet?.id == pet.id,
                                            onClick = { viewModel.selectPet(pet) },
                                            label = { Text(pet.name) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Pets,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = RedLinePrimary,
                                                selectedLabelColor = Color.White,
                                                selectedLeadingIconColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Active Pet Hero Banner
                            uiState.selectedPet?.let { pet ->
                                MemoryPetHeroCard(
                                    pet = pet,
                                    memoriesCount = uiState.allMemoriesCountForPet
                                )
                            }
                        }
                    }

                    // Content: Loading / Empty / Grid Items
                    if (uiState.isLoading) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = RedLinePrimary)
                            }
                        }
                    } else if (uiState.memories.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            if (uiState.searchQuery.isNotEmpty()) {
                                EmptyStateView(
                                    imageAssetPath = AssetImageHelper.EMPTY_NO_DATA,
                                    title = "خاطره‌ای یافت نشد",
                                    description = "با عبارت جستجوی «${uiState.searchQuery}» موردی پیدا نشد.",
                                    actionButtonText = "پاک کردن جستجو",
                                    onActionClick = { viewModel.onSearchQueryChanged("") }
                                )
                            } else {
                                EmptyStateView(
                                    imageAssetPath = AssetImageHelper.CARD_MEMORY,
                                    title = "لحظه‌های دوست‌داشتنی پت خود را ثبت کنید",
                                    description = "اولین خاطره تصویری و شیرین را در آلبوم اختصاصی ثبت و برای همیشه ماندگار کنید.",
                                    actionButtonText = "ثبت اولین خاطره",
                                    onActionClick = { viewModel.openAddMemoryDialog() }
                                )
                            }
                        }
                    } else {
                        // Section Header
                        item(span = { GridItemSpan(2) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "گالری خاطرات (${PersianNumberFormatter.toPersian(uiState.memories.size)})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = uiState.sortType.titleFa,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuxuryGold
                                )
                            }
                        }

                        // Memory Grid Cards
                        items(uiState.memories, key = { it.id }) { memory ->
                            MemoryGridCard(
                                memory = memory,
                                onClick = { viewModel.openDetailDialog(memory) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs & Sheets
    when (uiState.dialogState) {
        MemoryDialogState.ADD -> {
            AddEditMemoryBottomSheet(
                pets = uiState.pets,
                selectedPet = uiState.selectedPet,
                memoryToEdit = null,
                isSavingImage = uiState.isSavingImage,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.closeDialog() },
                onSave = { uri, existingPath, title, desc, petId, _ ->
                    viewModel.saveMemoryWithUri(
                        context = context,
                        imageUri = uri,
                        existingImagePath = existingPath,
                        title = title,
                        description = desc,
                        targetPetId = petId,
                        memoryIdToUpdate = null
                    )
                }
            )
        }
        MemoryDialogState.EDIT -> {
            AddEditMemoryBottomSheet(
                pets = uiState.pets,
                selectedPet = uiState.selectedPet,
                memoryToEdit = uiState.selectedMemory,
                isSavingImage = uiState.isSavingImage,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.closeDialog() },
                onSave = { uri, existingPath, title, desc, petId, memoryId ->
                    viewModel.saveMemoryWithUri(
                        context = context,
                        imageUri = uri,
                        existingImagePath = existingPath,
                        title = title,
                        description = desc,
                        targetPetId = petId,
                        memoryIdToUpdate = memoryId
                    )
                }
            )
        }
        MemoryDialogState.DETAIL -> {
            uiState.selectedMemory?.let { memory ->
                val memoryPet = uiState.pets.find { it.id == memory.petId } ?: uiState.selectedPet
                MemoryDetailDialog(
                    memory = memory,
                    pet = memoryPet,
                    onDismiss = { viewModel.closeDialog() },
                    onEdit = { viewModel.openEditMemoryDialog(it) },
                    onDelete = { viewModel.openDeleteConfirmDialog(it) }
                )
            }
        }
        MemoryDialogState.DELETE_CONFIRM -> {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                title = {
                    Text(
                        text = "حذف خاطره از آلبوم",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("آیا از حذف این خاطره و تصویر آن مطمئن هستید؟ این عملیات غیرقابل بازگشت است.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.confirmDeleteSelectedMemory() }
                    ) {
                        Text(
                            text = "حذف قطعی",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("انصراف")
                    }
                }
            )
        }
        MemoryDialogState.NONE -> {}
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
fun MemoryPetHeroCard(
    pet: Pet,
    memoriesCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            BurgundyPrimary.copy(alpha = 0.85f),
                            BurgundyDeep.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pet Avatar
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(2.dp, LuxuryGold, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(AssetImageHelper.getPetImage(pet))
                                .crossfade(true)
                                .build(),
                            contentDescription = pet.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "آلبوم اختصاصی ${pet.name}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${pet.breed.ifBlank { pet.species.titleFa }} • ${PersianNumberFormatter.toPersian(memoriesCount)} خاطره ثبت شده",
                            style = MaterialTheme.typography.bodySmall,
                            color = LuxuryGold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoAlbum,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryGridCard(
    memory: Memory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("memory_card_${memory.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.8.dp, LuxuryGold.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Photo Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(memory.imagePath)
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_report_image)
                        .build(),
                    contentDescription = memory.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Date Overlay Tag
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = memory.getFormattedJalaliDate(),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White
                        )
                    }
                }
            }

            // Title & Snippet Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = memory.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (memory.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memory.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
