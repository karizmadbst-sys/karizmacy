package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Castle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ItemType
import com.example.data.StoreCategory
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SnowPrimary
import java.util.Locale

fun StoreCategory.icon(): ImageVector = when (this) {
    StoreCategory.ACTION -> Icons.Rounded.LocalFireDepartment
    StoreCategory.RACING -> Icons.Rounded.DirectionsCar
    StoreCategory.RPG -> Icons.Rounded.Castle
    StoreCategory.STRATEGY -> Icons.Rounded.Extension
    StoreCategory.ARCADE -> Icons.Rounded.SportsEsports
    StoreCategory.TOOLS -> Icons.Rounded.Build
    StoreCategory.MEDIA -> Icons.Rounded.PlayCircle
    StoreCategory.PRODUCTIVITY -> Icons.Rounded.Workspaces
    StoreCategory.SECURITY -> Icons.Rounded.Shield
    StoreCategory.SOCIAL -> Icons.Rounded.Forum
}

fun formatDownloadCount(count: Long): String = when {
    count >= 1_000_000L -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
    count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000.0)
    else -> count.toString()
}

fun extractHostLabel(url: String): String = try {
    Uri.parse(url).host?.removePrefix("www.") ?: url
} catch (_: Exception) {
    url
}

fun launchDownloadUrl(context: Context, url: String): Boolean {
    val trimmed = url.trim()
    if (!trimmed.startsWith("http://", ignoreCase = true) &&
        !trimmed.startsWith("https://", ignoreCase = true)
    ) {
        return false
    }
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}

/**
 * Atmospheric pitch-black background with glowing radial neon purple and electric blue ambient light.
 */
@Composable
fun CyberBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPurple.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.08f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.85f, size.height * 0.08f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricBlue.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.52f),
                        radius = size.width * 0.72f
                    ),
                    radius = size.width * 0.72f,
                    center = Offset(size.width * 0.12f, size.height * 0.52f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonMagenta.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.88f),
                        radius = size.width * 0.65f
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width * 0.9f, size.height * 0.88f)
                )
            },
        content = content
    )
}

@Composable
fun NeonGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    accentGradient: List<Color> = listOf(NeonPurple, ElectricBlue),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !loading) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "neon_btn_scale"
    )
    val shape = RoundedCornerShape(16.dp)
    val brush = if (enabled) {
        Brush.horizontalGradient(accentGradient)
    } else {
        Brush.horizontalGradient(listOf(ObsidianBorder, ObsidianSurface))
    }

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = 50.dp)
            .clip(shape)
            .background(brush)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (enabled) 0.26f else 0.08f),
                shape = shape
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = Color.White),
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = ObsidianVoid,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ObsidianVoid,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianVoid
                )
            }
        }
    }
}

@Composable
fun TypePillBadge(
    itemType: ItemType,
    label: String,
    modifier: Modifier = Modifier
) {
    val accent = if (itemType == ItemType.GAME) NeonPurple else ElectricBlue
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = accent.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (itemType == ItemType.GAME) Icons.Rounded.SportsEsports else Icons.Rounded.Apps,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SnowPrimary
            )
        }
    }
}

@Composable
fun LanguageToggleChip(
    currentLanguage: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
            .testTag("language_toggle_button"),
        shape = CircleShape,
        color = ObsidianSurface.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Language,
                contentDescription = "Switch Language",
                tint = ElectricBlue,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (currentLanguage == AppLanguage.EN) "عربي" else "EN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = SnowPrimary
            )
        }
    }
}

@Composable
fun NeonFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = NeonPurple
) {
    val bgColor = if (selected) accentColor.copy(alpha = 0.22f) else ObsidianBase.copy(alpha = 0.85f)
    val borderColor = if (selected) accentColor else ObsidianBorder
    val contentColor = if (selected) SnowPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 40.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) accentColor else contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
