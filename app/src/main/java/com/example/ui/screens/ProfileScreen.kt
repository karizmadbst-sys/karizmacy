package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.CatalogItem
import com.example.data.DownloadLog
import com.example.data.FavoriteEntry
import com.example.data.ItemType
import com.example.data.UiState
import com.example.ui.components.NeonFilterChip
import com.example.ui.components.StoreListRowCard
import com.example.ui.components.TypePillBadge
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SnowPrimary

@Composable
fun ProfileScreen(
    displayName: String,
    email: String?,
    photoUrl: String?,
    isAdmin: Boolean,
    currentLanguage: AppLanguage,
    catalogState: UiState<List<CatalogItem>>,
    favorites: List<FavoriteEntry>,
    downloads: List<DownloadLog>,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenItem: (String) -> Unit,
    onToggleFavorite: (CatalogItem) -> Unit,
    onQuickDownload: (CatalogItem) -> Unit,
    onOpenDownloadUrl: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val strings = LocalStrings.current
    var activeTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Favorites, 1 = Downloads

    val catalogById = ((catalogState as? UiState.Success)?.data.orEmpty()).associateBy { it.id }
    val favoriteItems = favorites.mapNotNull { catalogById[it.itemId] }
    val favoriteIdSet = favorites.map { it.itemId }.toSet()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = strings.profileTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = SnowPrimary
            )
        }

        // 1. User Identity Hero Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = ObsidianSurface,
                border = BorderStroke(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.7f), ElectricBlue.copy(alpha = 0.7f)))
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(CircleShape)
                                .background(ObsidianSurfaceHigh)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(listOf(NeonPurple, ElectricBlue)),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = displayName.ifBlank { "Karizma Player" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SnowPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!email.isNullOrBlank()) {
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LavenderSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAdmin) NeonPurple.copy(alpha = 0.22f) else CyberEmerald.copy(alpha = 0.18f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isAdmin) NeonPurple else CyberEmerald
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAdmin) Icons.Rounded.AdminPanelSettings else Icons.Rounded.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (isAdmin) NeonPurple else CyberEmerald,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (isAdmin) strings.adminBadge else strings.memberBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SnowPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileStatTile(
                            value = favorites.size.toString(),
                            label = strings.statFavorites,
                            accent = NeonMagenta,
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatTile(
                            value = downloads.size.toString(),
                            label = strings.statDownloads,
                            accent = ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Bilingual English / Arabic RTL Switcher Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                        Column {
                            Text(
                                text = strings.languageSettingTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = SnowPrimary
                            )
                            Text(
                                text = strings.languageSettingSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = LavenderSecondary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeonFilterChip(
                            label = strings.englishOption,
                            selected = currentLanguage == AppLanguage.EN,
                            accentColor = ElectricBlue,
                            onClick = { onSelectLanguage(AppLanguage.EN) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_lang_en")
                        )
                        NeonFilterChip(
                            label = strings.arabicOption,
                            selected = currentLanguage == AppLanguage.AR,
                            accentColor = NeonPurple,
                            onClick = { onSelectLanguage(AppLanguage.AR) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_lang_ar")
                        )
                    }
                }
            }
        }

        // 3. Private Administrator Dashboard Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onOpenAdminDashboard)
                    .testTag("open_admin_dashboard_card"),
                shape = RoundedCornerShape(18.dp),
                color = if (isAdmin) NeonPurple.copy(alpha = 0.14f) else ObsidianSurface,
                border = BorderStroke(
                    1.dp,
                    if (isAdmin) NeonPurple else ObsidianBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAdmin) NeonPurple.copy(alpha = 0.25f) else ObsidianSurfaceHigh
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Rounded.AdminPanelSettings else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = if (isAdmin) NeonPurple else LavenderSecondary,
                            modifier = Modifier
                                .padding(12.dp)
                                .size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.openAdminDashboard,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SnowPrimary
                        )
                        Text(
                            text = if (isAdmin) strings.adminDashboardSubtitle else strings.adminRestrictedNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = LavenderSecondary
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = if (isAdmin) ElectricBlue else LavenderSecondary
                    )
                }
            }
        }

        // 4. Favorites & Download History Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NeonFilterChip(
                    label = "${strings.tabFavorites} (${favorites.size})",
                    selected = activeTab == 0,
                    icon = Icons.Rounded.Favorite,
                    accentColor = NeonMagenta,
                    onClick = { activeTab = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_tab_favorites")
                )
                NeonFilterChip(
                    label = "${strings.tabHistory} (${downloads.size})",
                    selected = activeTab == 1,
                    icon = Icons.Rounded.History,
                    accentColor = ElectricBlue,
                    onClick = { activeTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("profile_tab_downloads")
                )
            }
        }

        if (activeTab == 0) {
            if (favoriteItems.isEmpty()) {
                item {
                    ProfileEmptyPanel(
                        icon = Icons.Rounded.FavoriteBorder,
                        title = strings.noFavoritesYet,
                        subtitle = strings.noFavoritesHint
                    )
                }
            } else {
                items(favoriteItems, key = { "fav_${it.id}" }) { item ->
                    StoreListRowCard(
                        item = item,
                        isFavorite = favoriteIdSet.contains(item.id),
                        onClick = { onOpenItem(item.id) },
                        onToggleFavorite = { onToggleFavorite(item) },
                        onQuickDownload = { onQuickDownload(item) }
                    )
                }
            }
        } else {
            if (downloads.isEmpty()) {
                item {
                    ProfileEmptyPanel(
                        icon = Icons.Rounded.Download,
                        title = strings.noDownloadsYet,
                        subtitle = strings.noDownloadsHint
                    )
                }
            } else {
                items(downloads, key = { "dl_${it.id}" }) { log ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenItem(log.itemId) },
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianSurface,
                        border = BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = log.itemTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SnowPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TypePillBadge(
                                        itemType = ItemType.fromRaw(log.itemType),
                                        label = if (ItemType.fromRaw(log.itemType) == ItemType.GAME) {
                                            strings.typeGame
                                        } else {
                                            strings.typeApp
                                        }
                                    )
                                    Text(
                                        text = "v${log.version}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricBlue
                                    )
                                }
                            }
                            Surface(
                                onClick = { onOpenDownloadUrl(log.downloadUrl) },
                                shape = RoundedCornerShape(10.dp),
                                color = ElectricBlue.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = strings.openLinkAgain,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(15.dp)
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
        }

        // 5. Sign Out Button
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("sign_out_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberDanger.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Logout,
                    contentDescription = null,
                    tint = CyberDanger,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = strings.signOut,
                    style = MaterialTheme.typography.labelLarge,
                    color = CyberDanger
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileStatTile(
    value: String,
    label: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ObsidianVoid.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LavenderSecondary
            )
        }
    }
}

@Composable
private fun ProfileEmptyPanel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(34.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = SnowPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = LavenderSecondary
            )
        }
    }
}
