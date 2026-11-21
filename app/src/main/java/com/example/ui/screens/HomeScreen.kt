package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CatalogItem
import com.example.data.ItemType
import com.example.data.StoreCategory
import com.example.data.UiState
import com.example.ui.components.LanguageToggleChip
import com.example.ui.components.NeonFilterChip
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.StoreArtwork
import com.example.ui.components.StoreHorizontalCard
import com.example.ui.components.StoreListRowCard
import com.example.ui.components.TypePillBadge
import com.example.ui.components.icon
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SnowPrimary
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun HomeScreen(
    catalogState: UiState<List<CatalogItem>>,
    favoriteIds: Set<String>,
    isAdmin: Boolean,
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onOpenItem: (String) -> Unit,
    onToggleFavorite: (CatalogItem) -> Unit,
    onQuickDownload: (CatalogItem) -> Unit,
    onSeeAllGames: () -> Unit,
    onSeeAllApps: () -> Unit,
    onOpenSearch: (StoreCategory?) -> Unit,
    onOpenAdmin: () -> Unit,
    onSeedCatalog: () -> Unit
) {
    val strings = LocalStrings.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Futuristic Top Brand Bar
        item {
            HomeTopHeader(
                currentLanguage = currentLanguage,
                isAdmin = isAdmin,
                onToggleLanguage = onToggleLanguage,
                onOpenSearch = { onOpenSearch(null) },
                onOpenAdmin = onOpenAdmin
            )
        }

        when (catalogState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = ElectricBlue)
                            Text(
                                text = strings.loadingStore,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LavenderSecondary
                            )
                        }
                    }
                }
            }

            is UiState.Error -> {
                item {
                    StoreErrorBanner(
                        message = catalogState.message,
                        onRetry = null
                    )
                }
            }

            is UiState.Success -> {
                val allItems = catalogState.data
                if (allItems.isEmpty()) {
                    item {
                        EmptyCatalogCard(
                            isAdmin = isAdmin,
                            onSeedCatalog = onSeedCatalog
                        )
                    }
                } else {
                    val featured = allItems.filter { it.featured }.ifEmpty { allItems.take(4) }
                    val games = allItems.filter { it.parsedType == ItemType.GAME }
                    val apps = allItems.filter { it.parsedType == ItemType.APP }

                    // 1. Featured Hero Carousel
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.featuredSpotlight,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ElectricBlue
                                )
                            }
                            FeaturedHeroCarousel(
                                items = featured,
                                onOpenItem = onOpenItem,
                                onQuickDownload = onQuickDownload
                            )
                        }
                    }

                    // 2. Quick Categories Strip
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SectionHeader(
                                title = strings.browseCategories,
                                actionLabel = strings.seeAll,
                                onActionClick = { onOpenSearch(null) }
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(StoreCategory.entries) { category ->
                                    NeonFilterChip(
                                        label = category.label(strings),
                                        selected = false,
                                        icon = category.icon(),
                                        accentColor = if (category.itemType == ItemType.GAME) NeonPurple else ElectricBlue,
                                        onClick = { onOpenSearch(category) }
                                    )
                                }
                            }
                        }
                    }

                    // 3. Trending Games Horizontal Showcase
                    if (games.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SectionHeader(
                                    title = strings.trendingGames,
                                    actionLabel = strings.seeAll,
                                    onActionClick = onSeeAllGames
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(games.take(8), key = { it.id }) { item ->
                                        StoreHorizontalCard(
                                            item = item,
                                            isFavorite = favoriteIds.contains(item.id),
                                            onClick = { onOpenItem(item.id) },
                                            onToggleFavorite = { onToggleFavorite(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Essential Apps Horizontal Showcase
                    if (apps.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SectionHeader(
                                    title = strings.essentialApps,
                                    actionLabel = strings.seeAll,
                                    onActionClick = onSeeAllApps
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(apps.take(8), key = { it.id }) { item ->
                                        StoreHorizontalCard(
                                            item = item,
                                            isFavorite = favoriteIds.contains(item.id),
                                            onClick = { onOpenItem(item.id) },
                                            onToggleFavorite = { onToggleFavorite(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Latest Releases Vertical Feed
                    item {
                        SectionHeader(
                            title = strings.newlyAdded,
                            actionLabel = null,
                            onActionClick = null
                        )
                    }
                    items(allItems.take(6), key = { "latest_${it.id}" }) { item ->
                        StoreListRowCard(
                            item = item,
                            isFavorite = favoriteIds.contains(item.id),
                            onClick = { onOpenItem(item.id) },
                            onToggleFavorite = { onToggleFavorite(item) },
                            onQuickDownload = { onQuickDownload(item) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopHeader(
    currentLanguage: AppLanguage,
    isAdmin: Boolean,
    onToggleLanguage: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_karizma_icon_1795180171265),
                contentDescription = strings.brandName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(NeonPurple, ElectricBlue)),
                        shape = RoundedCornerShape(13.dp)
                    )
            )
            Column {
                Text(
                    text = strings.brandName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SnowPrimary
                )
                Text(
                    text = strings.brandTagline,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isAdmin) {
                IconButton(
                    onClick = onOpenAdmin,
                    modifier = Modifier.testTag("home_admin_shortcut")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NeonPurple.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, NeonPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AdminPanelSettings,
                            contentDescription = strings.adminTitle,
                            tint = NeonPurple,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = onOpenSearch,
                modifier = Modifier.testTag("home_search_shortcut")
            ) {
                Surface(
                    shape = CircleShape,
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = strings.navSearch,
                        tint = SnowPrimary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
            }
            LanguageToggleChip(
                currentLanguage = currentLanguage,
                onToggle = onToggleLanguage
            )
        }
    }
}

@Composable
private fun FeaturedHeroCarousel(
    items: List<CatalogItem>,
    onOpenItem: (String) -> Unit,
    onQuickDownload: (CatalogItem) -> Unit
) {
    val strings = LocalStrings.current
    val pagerState = rememberPagerState(pageCount = { items.size })

    LaunchedEffect(items.size) {
        if (items.size > 1) {
            while (true) {
                delay(5500L)
                val next = (pagerState.currentPage + 1) % items.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = items[page]
            val accent = if (item.parsedType == ItemType.GAME) NeonPurple else ElectricBlue
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(228.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onOpenItem(item.id) }
                    .testTag("featured_card_${item.id}"),
                shape = RoundedCornerShape(24.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.5.dp, accent.copy(alpha = 0.55f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
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
                                    colors = listOf(
                                        ObsidianVoid.copy(alpha = 0.25f),
                                        ObsidianVoid.copy(alpha = 0.55f),
                                        ObsidianVoid.copy(alpha = 0.96f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopStart)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TypePillBadge(
                            itemType = item.parsedType,
                            label = if (item.parsedType == ItemType.GAME) strings.typeGame else strings.typeApp
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianVoid.copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = CyberAmber,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f", item.rating),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SnowPrimary
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.localizedTitle(strings.language),
                            style = MaterialTheme.typography.headlineMedium,
                            color = SnowPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.localizedSummary(strings.language),
                            style = MaterialTheme.typography.bodySmall,
                            color = LavenderSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.developer} • ${item.fileSize}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricBlue
                            )
                            Surface(
                                onClick = { onQuickDownload(item) },
                                shape = RoundedCornerShape(12.dp),
                                color = accent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Download,
                                        contentDescription = strings.downloadNow,
                                        tint = ObsidianVoid,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = strings.openLinkAgain,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ObsidianVoid
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (items.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(items.size) { idx ->
                    val active = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (active) 22.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) ElectricBlue else ObsidianBorder)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String?,
    onActionClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = SnowPrimary
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = ElectricBlue
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyCatalogCard(
    isAdmin: Boolean,
    onSeedCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(22.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = strings.emptyCatalogTitle,
                style = MaterialTheme.typography.titleLarge,
                color = SnowPrimary
            )
            Text(
                text = strings.emptyCatalogSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = LavenderSecondary
            )
            if (isAdmin) {
                NeonGradientButton(
                    text = strings.seedCatalogNow,
                    icon = Icons.Rounded.Refresh,
                    onClick = onSeedCatalog,
                    modifier = Modifier.testTag("seed_catalog_button")
                )
            }
        }
    }
}

@Composable
fun StoreErrorBanner(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(20.dp),
        color = CyberDanger.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, CyberDanger.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = CyberDanger,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = strings.errorGeneric,
                style = MaterialTheme.typography.titleMedium,
                color = SnowPrimary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = LavenderSecondary
            )
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(strings.retry, color = ElectricBlue)
                }
            }
        }
    }
}
