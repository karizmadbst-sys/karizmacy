package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CatalogItem
import com.example.data.ItemType
import com.example.data.SortOption
import com.example.data.StoreCategory
import com.example.data.UiState
import com.example.ui.components.NeonFilterChip
import com.example.ui.components.StoreGridCard
import com.example.ui.components.icon
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SnowPrimary
import com.google.firebase.Timestamp

/**
 * Powers both the Games Section (ItemType.GAME) and Applications Section (ItemType.APP)
 * with tailored neon purple / electric blue styling, adaptive grid columns, category filters, and sorting.
 */
@Composable
fun CatalogBrowseScreen(
    itemType: ItemType,
    catalogState: UiState<List<CatalogItem>>,
    favoriteIds: Set<String>,
    isAdmin: Boolean,
    onOpenItem: (String) -> Unit,
    onToggleFavorite: (CatalogItem) -> Unit,
    onSeedCatalog: () -> Unit
) {
    val strings = LocalStrings.current
    val accent = if (itemType == ItemType.GAME) NeonPurple else ElectricBlue
    val categories = remember(itemType) { StoreCategory.forType(itemType) }

    var selectedCategoryKey by rememberSaveable(itemType) { mutableStateOf<String?>(null) }
    var selectedSort by rememberSaveable(itemType) { mutableStateOf(SortOption.FEATURED) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag(if (itemType == ItemType.GAME) "games_screen" else "apps_screen")
    ) {
        val columnCount = when {
            maxWidth >= 840.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Section Header Banner
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = accent.copy(alpha = 0.18f),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (itemType == ItemType.GAME) Icons.Rounded.SportsEsports else Icons.Rounded.Apps,
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (itemType == ItemType.GAME) strings.gamesHubTitle else strings.appsHubTitle,
                            style = MaterialTheme.typography.headlineLarge,
                            color = SnowPrimary
                        )
                        Text(
                            text = if (itemType == ItemType.GAME) strings.gamesHubSubtitle else strings.appsHubSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = LavenderSecondary
                        )
                    }
                }
            }

            // Category Filter Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        NeonFilterChip(
                            label = strings.filterAll,
                            selected = selectedCategoryKey == null,
                            accentColor = accent,
                            onClick = { selectedCategoryKey = null }
                        )
                    }
                    items(categories) { category ->
                        NeonFilterChip(
                            label = category.label(strings),
                            selected = selectedCategoryKey == category.key,
                            icon = category.icon(),
                            accentColor = accent,
                            onClick = {
                                selectedCategoryKey = if (selectedCategoryKey == category.key) null else category.key
                            }
                        )
                    }
                }
            }

            // Sort Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sortChoices = listOf(
                        SortOption.FEATURED to strings.sortFeatured,
                        SortOption.TOP_RATED to strings.sortTopRated,
                        SortOption.MOST_DOWNLOADED to strings.sortMostDownloaded,
                        SortOption.NEWEST to strings.sortNewest
                    )
                    items(sortChoices) { (option, label) ->
                        NeonFilterChip(
                            label = label,
                            selected = selectedSort == option,
                            accentColor = if (itemType == ItemType.GAME) ElectricBlue else NeonPurple,
                            onClick = { selectedSort = option }
                        )
                    }
                }
            }

            when (catalogState) {
                is UiState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = accent)
                        }
                    }
                }

                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        StoreErrorBanner(message = catalogState.message, onRetry = null)
                    }
                }

                is UiState.Success -> {
                    val filtered = catalogState.data
                        .filter { it.parsedType == itemType }
                        .filter { selectedCategoryKey == null || it.category.equals(selectedCategoryKey, ignoreCase = true) }
                        .let { list ->
                            when (selectedSort) {
                                SortOption.FEATURED -> list.sortedWith(
                                    compareByDescending<CatalogItem> { it.featured }
                                        .thenByDescending { it.rating }
                                )
                                SortOption.TOP_RATED -> list.sortedByDescending { it.rating }
                                SortOption.MOST_DOWNLOADED -> list.sortedByDescending { it.downloadsCount }
                                SortOption.NEWEST -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                            }
                        }

                    if (filtered.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyCatalogCard(
                                isAdmin = isAdmin,
                                onSeedCatalog = onSeedCatalog
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { item ->
                            StoreGridCard(
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
    }
}
