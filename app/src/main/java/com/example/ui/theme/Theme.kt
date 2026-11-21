package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.stringsFor

private val KarizmaDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color(0xFF0A0514),
    primaryContainer = NeonPurpleContainer,
    onPrimaryContainer = NeonPurpleBright,
    secondary = ElectricBlue,
    onSecondary = Color(0xFF021622),
    secondaryContainer = ElectricBlueContainer,
    onSecondaryContainer = ElectricBlueBright,
    tertiary = NeonMagenta,
    onTertiary = Color(0xFF1A0412),
    tertiaryContainer = NeonMagentaContainer,
    onTertiaryContainer = Color(0xFFFFB2D8),
    background = ObsidianVoid,
    onBackground = SnowPrimary,
    surface = ObsidianBase,
    onSurface = SnowPrimary,
    surfaceVariant = ObsidianSurface,
    onSurfaceVariant = LavenderSecondary,
    surfaceContainerLowest = ObsidianVoid,
    surfaceContainerLow = ObsidianBase,
    surfaceContainer = ObsidianSurface,
    surfaceContainerHigh = ObsidianSurfaceHigh,
    surfaceContainerHighest = ObsidianSurfaceHighest,
    outline = ObsidianBorder,
    outlineVariant = Color(0xFF201A3B),
    error = CyberDanger,
    onError = Color(0xFF1F0309)
)

private val KarizmaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun KarizmaGameTheme(
    language: AppLanguage = AppLanguage.EN,
    content: @Composable () -> Unit
) {
    val isArabic = language == AppLanguage.AR
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val strings = stringsFor(language)

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalStrings provides strings
    ) {
        MaterialTheme(
            colorScheme = KarizmaDarkColorScheme,
            typography = karizmaTypography(isArabic = isArabic),
            shapes = KarizmaShapes,
            content = content
        )
    }
}
