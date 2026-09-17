package red.line.pet.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Pets : Screen(
        route = "pets",
        title = "حیوانات من",
        selectedIcon = Icons.Filled.Pets,
        unselectedIcon = Icons.Outlined.Pets
    )

    data object Health : Screen(
        route = "health",
        title = "پرونده سلامت",
        selectedIcon = Icons.Filled.MedicalServices,
        unselectedIcon = Icons.Outlined.MedicalServices
    )

    data object Expenses : Screen(
        route = "expenses",
        title = "هزینه‌ها",
        selectedIcon = Icons.Filled.Payments,
        unselectedIcon = Icons.Outlined.Payments
    )

    data object Food : Screen(
        route = "food",
        title = "برنامه غذایی",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    data object Weight : Screen(
        route = "weight",
        title = "نمودار وزن",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    data object Memories : Screen(
        route = "memories",
        title = "آلبوم خاطرات",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    data object Reminders : Screen(
        route = "reminders",
        title = "یادآورها",
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications
    )

    data object MedicalPassport : Screen(
        route = "medical_passport",
        title = "شناسنامه سلامت",
        selectedIcon = Icons.Filled.MedicalServices,
        unselectedIcon = Icons.Outlined.MedicalServices
    )

    data object Settings : Screen(
        route = "settings",
        title = "تنظیمات",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    data object VipUpgrade : Screen(
        route = "vip_upgrade",
        title = "نسخه VIP",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder
    )

    data object BackupRestore : Screen(
        route = "backup_restore",
        title = "پشتیبان‌گیری و بازیابی",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Pets, Health, Expenses, Settings)
    }
}
