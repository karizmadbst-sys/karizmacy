package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.CatalogItem
import com.example.data.ItemType
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.StoreArtwork
import com.example.ui.components.TypePillBadge
import com.example.ui.components.extractHostLabel
import com.example.ui.components.formatDownloadCount
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SnowPrimary
import java.util.Locale

@Composable
fun ItemDetailScreen(
    item: CatalogItem?,
    isFavorite: Boolean,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: (CatalogItem) -> Unit,
    onDownloadClick: (CatalogItem) -> Unit,
    onEditInAdmin: (String) -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    var previewScreenshot by remember { mutableStateOf<String?>(null) }

    if (item == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = strings.itemNotFound,
                    style = MaterialTheme.typography.titleLarge,
                    color = SnowPrimary
                )
                OutlinedButton(onClick = onBack) {
                    Text(strings.back)
                }
            }
        }
        return
    }

    val isGame = item.parsedType == ItemType.GAME
    val primaryAccent = if (isGame) NeonPurple else ElectricBlue
    val secondaryAccent = if (isGame) ElectricBlue else NeonPurple
    val title = item.localizedTitle(strings.language)
    val summary = item.localizedSummary(strings.language)
    val description = item.localizedDescription(strings.language)
    val gallery = remember(item.screenshotUrls, item.coverImageUrl) {
        item.screenshotUrls.ifEmpty {
            if (item.coverImageUrl.isNotBlank()) listOf(item.coverImageUrl) else emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("item_detail_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Hero Cover Art with Floating Action Controls
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    StoreArtwork(
                        uri = item.coverImageUrl,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        ObsidianVoid.copy(alpha = 0.65f),
                                        Color.Transparent,
                                        ObsidianVoid.copy(alpha = 0.82f),
                                        ObsidianVoid
                                    )
                                )
                            )
                    )

                    // Top Navigation & Actions Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("detail_back_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ObsidianVoid.copy(alpha = 0.78f),
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = strings.back,
                                    tint = SnowPrimary,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(20.dp)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, title)
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "$title (v${item.version}) — ${strings.brandName}\n${item.downloadUrl}"
                                        )
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, strings.shareItem)
                                    )
                                },
                                modifier = Modifier.testTag("detail_share_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = ObsidianVoid.copy(alpha = 0.78f),
                                    border = BorderStroke(1.dp, ObsidianBorder)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = strings.shareItem,
                                        tint = SnowPrimary,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onToggleFavorite(item) },
                                modifier = Modifier.testTag("detail_favorite_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = ObsidianVoid.copy(alpha = 0.78f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isFavorite) NeonMagenta else ObsidianBorder
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = strings.addToFavorites,
                                        tint = if (isFavorite) NeonMagenta else SnowPrimary,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Title & Developer Overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TypePillBadge(
                                itemType = item.parsedType,
                                label = if (isGame) strings.typeGame else strings.typeApp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ObsidianSurfaceHigh,
                                border = BorderStroke(1.dp, ObsidianBorder)
                            ) {
                                Text(
                                    text = item.parsedCategory.label(strings),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LavenderSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.displayMedium,
                            color = SnowPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = item.developer,
                                style = MaterialTheme.typography.titleSmall,
                                color = ElectricBlue
                            )
                        }
                    }
                }
            }

            // 2. Neon Glass Telemetry Metrics Strip
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DetailMetricCell(
                            value = String.format(Locale.US, "%.1f", item.rating),
                            label = strings.ratingLabel,
                            accent = CyberAmber,
                            leadingStar = true
                        )
                        DetailMetricCell(
                            value = "v${item.version}",
                            label = strings.versionLabel,
                            accent = primaryAccent
                        )
                        DetailMetricCell(
                            value = item.fileSize,
                            label = strings.sizeLabel,
                            accent = secondaryAccent
                        )
                        DetailMetricCell(
                            value = formatDownloadCount(item.downloadsCount),
                            label = strings.downloadsLabel,
                            accent = SnowPrimary
                        )
                    }
                }
            }

            // 3. Primary Actual Download CTA + Admin Edit Shortcut
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeonGradientButton(
                        text = "${strings.downloadNow}  •  ${item.fileSize}",
                        icon = Icons.Rounded.Download,
                        accentGradient = listOf(primaryAccent, secondaryAccent),
                        onClick = { onDownloadClick(item) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_button")
                    )

                    if (isAdmin) {
                        OutlinedButton(
                            onClick = { onEditInAdmin(item.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("detail_edit_admin_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = strings.editInAdmin,
                                style = MaterialTheme.typography.labelLarge,
                                color = SnowPrimary
                            )
                        }
                    }
                }
            }

            // 4. Screenshots Gallery
            if (gallery.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = strings.screenshotsTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = SnowPrimary,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(gallery) { shotUri ->
                                StoreArtwork(
                                    uri = shotUri,
                                    contentDescription = strings.screenshotsTitle,
                                    modifier = Modifier
                                        .width(260.dp)
                                        .aspectRatio(16f / 9f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                                        .clickable { previewScreenshot = shotUri }
                                )
                            }
                        }
                    }
                }
            }

            // 5. About / Full Description Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isGame) strings.aboutThisGame else strings.aboutThisApp,
                            style = MaterialTheme.typography.titleLarge,
                            color = SnowPrimary
                        )
                        if (summary.isNotBlank()) {
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.titleSmall,
                                color = ElectricBlue
                            )
                        }
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = LavenderSecondary
                        )
                    }
                }
            }

            // 6. Release Specifications Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.releaseSpecsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = SnowPrimary
                        )
                        SpecRow(label = strings.developerLabel, value = item.developer)
                        HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.6f))
                        SpecRow(label = strings.categoryLabel, value = item.parsedCategory.label(strings))
                        HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.6f))
                        SpecRow(label = strings.versionLabel, value = item.version)
                        HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.6f))
                        SpecRow(label = strings.sizeLabel, value = item.fileSize)
                        HorizontalDivider(color = ObsidianBorder.copy(alpha = 0.6f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.downloadSourceLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = LavenderSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable { onDownloadClick(item) }
                            ) {
                                Text(
                                    text = extractHostLabel(item.downloadUrl),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ElectricBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Rounded.OpenInNew,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Screenshot Preview Dialog
    previewScreenshot?.let { shotUri ->
        Dialog(
            onDismissRequest = { previewScreenshot = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ObsidianVoid.copy(alpha = 0.94f))
                    .clickable { previewScreenshot = null },
                contentAlignment = Alignment.Center
            ) {
                StoreArtwork(
                    uri = shotUri,
                    contentDescription = strings.screenshotsTitle,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                )
                IconButton(
                    onClick = { previewScreenshot = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                ) {
                    Surface(shape = CircleShape, color = ObsidianSurface) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.close,
                            tint = SnowPrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetricCell(
    value: String,
    label: String,
    accent: Color,
    leadingStar: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (leadingStar) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = CyberAmber,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LavenderSecondary
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = LavenderSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = SnowPrimary
        )
    }
}
