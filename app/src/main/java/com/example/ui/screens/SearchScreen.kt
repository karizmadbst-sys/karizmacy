package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CatalogItem
import com.example.data.ItemType
import com.example.data.StoreCategory
import com.example.data.UiState
import com.example.ui.components.NeonFilterChip
import com.example.ui.components.StoreListRowCard
import com.example.ui.components.icon
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SnowPrimary

@Composable
fun SearchScreen(
    catalogState: UiState<List<CatalogItem>>,
    favoriteIds: Set<String>,
    initialCategory: StoreCategory?,
    onClearInitialCategory: () -> Unit,
    onOpenItem: (String) -> Unit,
    onToggleFavorite: (CatalogItem) -> Unit,
    onQuickDownload: (CatalogItem) -> Unit
) {
    val strings = LocalStrings.current
    var query by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategoryKey by rememberSaveable { mutableStateOf<String?>(initialCategory?.key) }

    LaunchedEffect(initialCategory) {
        if (initialCategory != null) {
            selectedCategoryKey = initialCategory.key
            onClearInitialCategory()
        }
    }

    val allItems = (catalogState as? UiState.Success)?.data.orEmpty()
    val filteredItems = allItems.filter { item ->
        val matchesType = selectedType == null || item.parsedType.raw == selectedType
        val matchesCat = selectedCategoryKey == null || item.category.equals(selectedCategoryKey, ignoreCase = true)
        val q = query.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
            item.title.lowercase().contains(q) ||
            item.titleAr.lowercase().contains(q) ||
            item.developer.lowercase().contains(q) ||
            item.summary.lowercase().contains(q) ||
            item.summaryAr.lowercase().contains(q) ||
            item.category.lowercase().contains(q)
        matchesType && matchesCat && matchesQuery
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = strings.navSearch,
                style = MaterialTheme.typography.headlineLarge,
                color = SnowPrimary
            )
        }

        // Neon Search Field
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                placeholder = {
                    Text(
                        text = strings.searchPlaceholder,
                        color = LavenderSecondary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = ElectricBlue
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.clearFilters,
                                tint = LavenderSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ObsidianSurface,
                    unfocusedContainerColor = ObsidianSurface,
                    focusedBorderColor = ElectricBlue,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = SnowPrimary,
                    unfocusedTextColor = SnowPrimary,
                    cursorColor = ElectricBlue
                )
            )
        }

        // Type Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonFilterChip(
                    label = strings.searchAllTypes,
                    selected = selectedType == null,
                    accentColor = ElectricBlue,
                    onClick = { selectedType = null }
                )
                NeonFilterChip(
                    label = strings.navGames,
                    selected = selectedType == ItemType.GAME.raw,
                    icon = Icons.Rounded.SportsEsports,
                    accentColor = NeonPurple,
                    onClick = {
                        selectedType = if (selectedType == ItemType.GAME.raw) null else ItemType.GAME.raw
                    }
                )
                NeonFilterChip(
                    label = strings.navApps,
                    selected = selectedType == ItemType.APP.raw,
                    icon = Icons.Rounded.Apps,
                    accentColor = ElectricBlue,
                    onClick = {
                        selectedType = if (selectedType == ItemType.APP.raw) null else ItemType.APP.raw
                    }
                )
            }
        }

        // Interactive Category Cards (2-per-row grid inside LazyColumn when no text query, or chips when searching)
        if (query.isBlank() && selectedCategoryKey == null) {
            item {
                Text(
                    text = strings.browseCategories,
                    style = MaterialTheme.typography.titleLarge,
                    color = SnowPrimary
                )
            }
            val visibleCategories = StoreCategory.entries.filter {
                selectedType == null || it.itemType.raw == selectedType
            }
            items(visibleCategories.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { category ->
                        val count = allItems.count { it.category.equals(category.key, ignoreCase = true) }
                        val accent = if (category.itemType == ItemType.GAME) NeonPurple else ElectricBlue
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedCategoryKey = category.key }
                                .testTag("category_card_${category.key}"),
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianSurface,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = accent.copy(alpha = 0.18f)
                                ) {
                                    Icon(
                                        imageVector = category.icon(),
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = category.label(strings),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SnowPrimary
                                    )
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LavenderSecondary
                                    )
                                }
                            }
                        }
                    }
                    if (pair.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            // Active Category Chips Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(StoreCategory.entries) { cat ->
                        NeonFilterChip(
                            label = cat.label(strings),
                            selected = selectedCategoryKey == cat.key,
                            icon = cat.icon(),
                            accentColor = if (cat.itemType == ItemType.GAME) NeonPurple else ElectricBlue,
                            onClick = {
                                selectedCategoryKey = if (selectedCategoryKey == cat.key) null else cat.key
                            }
                        )
                    }
                }
            }
        }

        // Results Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.searchResultsCount} (${filteredItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = SnowPrimary
                )
                if (query.isNotBlank() || selectedCategoryKey != null || selectedType != null) {
                    TextButton(
                        onClick = {
                            query = ""
                            selectedCategoryKey = null
                            selectedType = null
                        }
                    ) {
                        Text(strings.clearFilters, color = ElectricBlue)
                    }
                }
            }
        }

        if (filteredItems.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = strings.noResultsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = SnowPrimary
                        )
                        Text(
                            text = strings.noResultsSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = LavenderSecondary
                        )
                    }
                }
            }
        } else {
            items(filteredItems, key = { "search_${it.id}" }) { item ->
                StoreListRowCard(
                    item = item,
                    isFavorite = favoriteIds.contains(item.id),
                    onClick = { onOpenItem(item.id) },
                    onToggleFavorite = { onToggleFavorite(item) },
                    onQuickDownload = { onQuickDownload(item) }
                )
            }
        }
    }
}
