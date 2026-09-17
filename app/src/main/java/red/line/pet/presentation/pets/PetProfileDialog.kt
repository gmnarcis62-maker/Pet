package red.line.pet.presentation.pets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import red.line.pet.core.theme.BurgundyLight
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.LuxuryGoldLight
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.theme.StatusSuccess
import red.line.pet.core.ui.AssetImageHelper
import red.line.pet.core.ui.ImageCard
import red.line.pet.core.ui.RedLineButton
import red.line.pet.core.ui.SectionHeader
import red.line.pet.core.ui.calculateAgePersian
import red.line.pet.core.util.PersianNumberFormatter
import red.line.pet.domain.model.Pet

@Composable
fun PetProfileDialog(
    pet: Pet,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdateAvatar: (String) -> Unit = {},
    onNavigateToHealth: () -> Unit = {},
    onNavigateToFood: () -> Unit = {},
    onNavigateToWeight: () -> Unit = {},
    onNavigateToExpenses: () -> Unit = {},
    onNavigateToMemories: () -> Unit = {},
    onNavigateToMedicalPassport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val imageUri = AssetImageHelper.getPetImage(pet)
    var showImagePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Top Hero Section with Pet Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = pet.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.5f),
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                    )

                    // Top Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "بستن",
                                tint = Color.White
                            )
                        }

                        IconButton(
                            onClick = { showDeleteConfirmDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .testTag("btn_delete_pet_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "حذف حیوان",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Floating Change Avatar Button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.85f)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .clickable { showImagePicker = true }
                            .testTag("btn_change_avatar_profile")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "تغییر عکس",
                                tint = LuxuryGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تغییر تصویر",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Pet Name & Species Floating Badge
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = pet.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RedLinePrimary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, RedLinePrimary)
                            ) {
                                Text(
                                    text = pet.gender.titleFa,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = RedLinePrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${pet.species.titleFa} • ${pet.breed.ifBlank { "نژاد مشخص نشده" }}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stats Bar (Weight, Age, Microchip)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Scale,
                                    contentDescription = null,
                                    tint = RedLinePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "وزن",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${PersianNumberFormatter.toPersian(String.format("%.1f", pet.weightKg))} کیلو",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = null,
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "سن",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = calculateAgePersian(pet.birthDateJalali),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.QrCode,
                                    contentDescription = null,
                                    tint = StatusSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "میکروچیپ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pet.microchipId.ifBlank { "ثبت نشده" },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (pet.notes.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "یادداشت‌ها و توضیحات",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pet.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Luxury Cards Sections
                SectionHeader(
                    title = "بخش‌های تخصصی و مراقبت",
                    subtitle = "امکانات اختصاصی مدیریت این حیوان"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ImageCard(
                        imageAssetPath = AssetImageHelper.CARD_MEDICAL,
                        title = "شناسنامه رسمی سلامت (PDF)",
                        subtitle = "خروجی پرونده درمانی برای ارائه به کلینیک و دامپزشک",
                        badgeText = "PDF رسمی",
                        accentColor = LuxuryGold,
                        onClick = {
                            onDismiss()
                            onNavigateToMedicalPassport()
                        }
                    )

                    ImageCard(
                        imageAssetPath = AssetImageHelper.CARD_MEDICAL,
                        title = "پرونده سلامت و واکسن‌ها",
                        subtitle = "سوابق بیماری، نسخ دارویی و موعد نوبت‌های پزشکی",
                        badgeText = "پزشکی",
                        accentColor = RedLinePrimary,
                        onClick = {
                            onDismiss()
                            onNavigateToHealth()
                        }
                    )

                    ImageCard(
                        imageAssetPath = AssetImageHelper.CARD_FOOD,
                        title = "برنامه غذایی و رژیم",
                        subtitle = "وعده‌های صبح و عصر، مکمل‌ها و میزان تغذیه روزانه",
                        badgeText = "تغذیه",
                        accentColor = LuxuryGold,
                        onClick = {
                            onDismiss()
                            onNavigateToFood()
                        }
                    )

                    ImageCard(
                        imageAssetPath = AssetImageHelper.CARD_WEIGHT,
                        title = "نمودار و ثبت وزن",
                        subtitle = "کنترل اضافه وزن و نمودار رشد سلامت در طول زمان",
                        badgeText = "سلامت",
                        accentColor = StatusSuccess,
                        onClick = {
                            onDismiss()
                            onNavigateToWeight()
                        }
                    )

                    ImageCard(
                        imageAssetPath = AssetImageHelper.CARD_EXPENSE,
                        title = "مدیریت هزینه‌ها",
                        subtitle = "ریز مخارج، هزینه‌های درمان، خوراک و اسباب‌بازی",
                        badgeText = "مالی",
                        accentColor = BurgundyLight,
                        onClick = {
                            onDismiss()
                            onNavigateToExpenses()
                        }
                    )

                    ImageCard(
                        imageAssetPath = AssetImageHelper.CARD_MEMORY,
                        title = "آلبوم خاطرات و لحظات",
                        subtitle = "عکس‌ها و یادداشت‌های روزمره از روزهای زیبای با هم بودن",
                        badgeText = "خاطرات",
                        accentColor = Color(0xFFAB47BC),
                        onClick = {
                            onDismiss()
                            onNavigateToMemories()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showImagePicker) {
        PetImagePickerBottomSheet(
            currentImageUri = imageUri,
            onImageSelected = { newUri ->
                onUpdateAvatar(newUri)
                showImagePicker = false
            },
            onDismiss = { showImagePicker = false }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "حذف پرونده حیوان خانگی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "آیا از حذف پرونده «${pet.name}» مطمئن هستید؟ تمامی سوابق درمانی، وزن و یادآورهای این حیوان حذف خواهند شد.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                RedLineButton(
                    text = "بله، حذف کن",
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                        onDismiss()
                    },
                    modifier = Modifier.width(130.dp)
                )
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
