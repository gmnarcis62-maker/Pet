package red.line.pet.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import red.line.pet.core.theme.LuxuryGold
import red.line.pet.core.theme.RedLinePrimary
import red.line.pet.core.ui.RedLineCard
import red.line.pet.core.ui.RedLineHeader

/**
 * About Us (درباره پتورا) Screen.
 * Fully user-centric, professional presentation without developer/technical jargon.
 */
@Composable
fun AboutUsScreen(
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_us_screen")
    ) {
        // Luxury Top Header
        if (onNavigateBack != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "درباره پتورا",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            RedLineHeader(
                title = "درباره پتورا",
                subtitle = "همراه هوشمند شما برای مراقبت از حیوانات خانگی",
                badgeText = "Petora"
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AboutUsHeaderCard()
            }

            item {
                AboutUsIntroCard()
            }

            item {
                DeveloperInfoCard(context = context)
            }

            item {
                AboutUsActionButtons(context = context)
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

/**
 * Header Card with Petora luxury branding and icon
 */
@Composable
fun AboutUsHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, LuxuryGold.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(RedLinePrimary, LuxuryGold)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "درباره پتورا",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "همراه هوشمند شما برای مراقبت از دوستان وفادار",
                style = MaterialTheme.typography.bodySmall,
                color = LuxuryGold,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * App Introduction Card with official user-centric copy
 */
@Composable
fun AboutUsIntroCard() {
    RedLineCard(
        showRedLineAccent = true,
        modifier = Modifier.testTag("card_about_petora_intro")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = null,
                tint = LuxuryGold,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "درباره پتورا",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "پتورا همراه هوشمند شما برای مراقبت بهتر از حیوانات خانگی است.\n\n" +
                    "با پتورا میتوانید اطلاعات حیوانات خود را مدیریت کنید، سوابق سلامت، برنامه غذایی، وزن، هزینهها، خاطرات و یادآورهای مهم را همیشه در دسترس داشته باشید.\n\n" +
                    "هدف پتورا ایجاد تجربهای ساده، منظم و لذتبخش برای صاحبان حیوانات خانگی است تا بتوانند با آرامش بیشتری از دوستان دوستداشتنی خود مراقبت کنند.\n\n" +
                    "تمام اطلاعات شما بهصورت امن و آفلاین روی دستگاه ذخیره میشود و کنترل کامل اطلاعات حیوان خانگی همیشه در اختیار شما خواهد بود.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 24.sp,
            textAlign = TextAlign.Justify
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Offline security reassurance badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حفظ کامل حریم خصوصی و دسترسی ۱۰۰٪ آفلاین",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Developer Info Card displaying company, management, and support contact
 */
@Composable
fun DeveloperInfoCard(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_developer_info"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "اطلاعات توسعه‌دهنده",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            DeveloperInfoRow(
                icon = Icons.Filled.Business,
                label = "توسعه دهنده:",
                value = "تیم نرم افزاری ردلاین سافت البرز"
            )

            Spacer(modifier = Modifier.height(12.dp))

            DeveloperInfoRow(
                icon = Icons.Filled.Person,
                label = "مدیریت:",
                value = "مهندس مهدی رضایی"
            )

            Spacer(modifier = Modifier.height(12.dp))

            DeveloperInfoRow(
                icon = Icons.Filled.Email,
                label = "پشتیبانی:",
                value = "gmnarcis@gmail.com",
                isClickable = true,
                onClick = { openSupportEmailIntent(context) }
            )
        }
    }
}

@Composable
fun DeveloperInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier.clickable { onClick() } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(RedLinePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RedLinePrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isClickable) LuxuryGold else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Action Buttons: Rate in Myket & Contact Support Email Intent
 */
@Composable
fun AboutUsActionButtons(context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Button: Register comment in Myket
        Button(
            onClick = { openMyketCommentIntent(context) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RedLinePrimary,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_rate_myket")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ثبت نظر در مایکت",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Button: Contact Support via Email
        OutlinedButton(
            onClick = { openSupportEmailIntent(context) },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, LuxuryGold),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_contact_support")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SupportAgent,
                    contentDescription = null,
                    tint = LuxuryGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تماس با پشتیبانی",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Launches the Myket rate/comment page intent with graceful web browser fallback
 */
fun openMyketCommentIntent(context: Context) {
    val packageName = context.packageName
    val myketUri = Uri.parse("myket://comment?id=$packageName")
    val myketIntent = Intent(Intent.ACTION_VIEW, myketUri).apply {
        setPackage("ir.mservices.market")
    }

    try {
        context.startActivity(myketIntent)
    } catch (e: Exception) {
        // Fallback to web browser URL if Myket app is not installed
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://myket.ir/app/$packageName")
        )
        try {
            context.startActivity(webIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "برنامه مایکت یافت نشد", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Launches Email Intent to gmnarcis@gmail.com
 */
fun openSupportEmailIntent(context: Context) {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:gmnarcis@gmail.com")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("gmnarcis@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "پشتیبانی برنامه پتورا")
    }

    try {
        context.startActivity(
            Intent.createChooser(emailIntent, "ارسال پیام به پشتیبانی پتورا")
        )
    } catch (e: Exception) {
        Toast.makeText(context, "برنامه ایمیل در دستگاه یافت نشد", Toast.LENGTH_SHORT).show()
    }
}
