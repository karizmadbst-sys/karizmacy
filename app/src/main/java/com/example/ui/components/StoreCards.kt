package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CatalogItem
import com.example.data.ItemType
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SnowPrimary
import java.util.Locale

@Composable
fun StoreHorizontalCard(
    item: CatalogItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val accent = if (item.parsedType == ItemType.GAME) NeonPurple else ElectricBlue

    Surface(
        modifier = modifier
            .width(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("store_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
            ) {
                StoreArtwork(
                    uri = item.coverImageUrl,
                    contentDescription = item.localizedTitle(strings.language),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, ObsidianSurface.copy(alpha = 0.85f))
                            )
                        )
                )
                // Rating pill
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianVoid.copy(alpha = 0.82f),
                    border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", item.rating),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SnowPrimary
                        )
                    }
                }
                // Favorite heart
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(40.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ObsidianVoid.copy(alpha = 0.72f)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = strings.addToFavorites,
                            tint = if (isFavorite) NeonMagenta else SnowPrimary,
                            modifier = Modifier
                                .padding(7.dp)
                                .size(18.dp)
                        )
                    }
                }
                // Version & size bottom tags
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accent.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = item.parsedCategory.label(strings),
                            style = MaterialTheme.typography.labelSmall,
                            color = SnowPrimary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ObsidianVoid.copy(alpha = 0.78f)
                    ) {
                        Text(
                            text = item.fileSize,
                            style = MaterialTheme.typography.labelSmall,
                            color = LavenderSecondary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.localizedTitle(strings.language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SnowPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.developer,
                    style = MaterialTheme.typography.bodySmall,
                    color = LavenderSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "v${item.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = LavenderSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = formatDownloadCount(item.downloadsCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = LavenderSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreListRowCard(
    item: CatalogItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onQuickDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val accent = if (item.parsedType == ItemType.GAME) NeonPurple else ElectricBlue

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("store_row_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StoreArtwork(
                uri = item.coverImageUrl,
                contentDescription = item.localizedTitle(strings.language),
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.localizedTitle(strings.language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SnowPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Text(
                    text = "${item.developer} • ${item.parsedCategory.label(strings)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = LavenderSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", item.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = SnowPrimary
                        )
                    }
                    Text(
                        text = item.fileSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderSecondary
                    )
                    Text(
                        text = "v${item.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = strings.addToFavorites,
                        tint = if (isFavorite) NeonMagenta else LavenderSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Surface(
                    onClick = onQuickDownload,
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = strings.downloadNow,
                            tint = accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = strings.openLinkAgain,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SnowPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreGridCard(
    item: CatalogItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val accent = if (item.parsedType == ItemType.GAME) NeonPurple else ElectricBlue

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("store_grid_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.45f)
            ) {
                StoreArtwork(
                    uri = item.coverImageUrl,
                    contentDescription = item.localizedTitle(strings.language),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, ObsidianSurface.copy(alpha = 0.9f))
                            )
                        )
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(38.dp)
                ) {
                    Surface(shape = CircleShape, color = ObsidianVoid.copy(alpha = 0.72f)) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = strings.addToFavorites,
                            tint = if (isFavorite) NeonMagenta else SnowPrimary,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(17.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", item.rating),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SnowPrimary
                    )
                }
            }
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.localizedTitle(strings.language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SnowPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.parsedCategory.label(strings),
                    style = MaterialTheme.typography.bodySmall,
                    color = LavenderSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.fileSize,
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderSecondary
                    )
                    Text(
                        text = "v${item.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                }
            }
        }
    }
}
