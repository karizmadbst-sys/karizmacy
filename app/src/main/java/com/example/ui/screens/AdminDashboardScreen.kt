package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CatalogItem
import com.example.data.ItemType
import com.example.data.UiState
import com.example.ui.components.NeonFilterChip
import com.example.ui.components.StoreArtwork
import com.example.ui.components.TypePillBadge
import com.example.ui.components.formatDownloadCount
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

@Composable
fun AdminDashboardScreen(
    isAdmin: Boolean,
    adminBusy: Boolean,
    catalogState: UiState<List<CatalogItem>>,
    onBack: () -> Unit,
    onCreateNew: () -> Unit,
    onEditItem: (String) -> Unit,
    onToggleFeatured: (CatalogItem) -> Unit,
    onDeleteItem: (CatalogItem) -> Unit,
    onSeedStarterCatalog: () -> Unit
) {
    val strings = LocalStrings.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterMode by rememberSaveable { mutableStateOf("ALL") } // ALL, GAME, APP, DRAFT
    var pendingDelete by remember { mutableStateOf<CatalogItem?>(null) }

    if (!isAdmin) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.dp, CyberDanger.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = CyberDanger,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = strings.adminAccessDeniedTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = SnowPrimary
                    )
                    Text(
                        text = strings.adminAccessDeniedBody,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LavenderSecondary
                    )
                    OutlinedButton(onClick = onBack) {
                        Text(strings.back)
                    }
                }
            }
        }
        return
    }

    val allItems = (catalogState as? UiState.Success)?.data.orEmpty()
    val totalDownloads = allItems.sumOf { it.downloadsCount }
    val gamesCount = allItems.count { it.parsedType == ItemType.GAME }
    val appsCount = allItems.count { it.parsedType == ItemType.APP }

    val filtered = allItems.filter { item ->
        val matchesMode = when (filterMode) {
            "GAME" -> item.parsedType == ItemType.GAME
            "APP" -> item.parsedType == ItemType.APP
            "DRAFT" -> !item.published
            else -> true
        }
        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
            item.title.lowercase().contains(q) ||
            item.titleAr.lowercase().contains(q) ||
            item.developer.lowercase().contains(q)
        matchesMode && matchesQuery
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNew,
                containerColor = NeonPurple,
                contentColor = ObsidianVoid,
                modifier = Modifier.testTag("admin_add_item_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = strings.adminPublishNew
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("admin_dashboard_screen"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 92.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.back,
                            tint = SnowPrimary
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AdminPanelSettings,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = strings.adminTitle,
                                style = MaterialTheme.typography.headlineMedium,
                                color = SnowPrimary
                            )
                        }
                        Text(
                            text = strings.adminSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = LavenderSecondary
                        )
                    }
                    if (adminBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ElectricBlue,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // KPI Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminKpiCard(
                        value = "${allItems.size}",
                        label = strings.adminTotalItems,
                        accent = NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        value = "$gamesCount / $appsCount",
                        label = "${strings.navGames} / ${strings.navApps}",
                        accent = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        value = formatDownloadCount(totalDownloads),
                        label = strings.adminTotalDownloads,
                        accent = CyberAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Row (Publish New + Seed Starter Catalog)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCreateNew,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_publish_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, NeonPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(strings.adminPublishNew, color = SnowPrimary, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onSeedStarterCatalog,
                        enabled = !adminBusy,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_seed_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(strings.adminSeedStarter, color = SnowPrimary, maxLines = 1)
                    }
                }
            }

            // Filter Bar + Search
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(strings.adminSearchHint, color = LavenderSecondary) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = ElectricBlue)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ObsidianSurface,
                        unfocusedContainerColor = ObsidianSurface,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = SnowPrimary,
                        unfocusedTextColor = SnowPrimary
                    )
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeonFilterChip(
                        label = strings.filterAll,
                        selected = filterMode == "ALL",
                        accentColor = ElectricBlue,
                        onClick = { filterMode = "ALL" }
                    )
                    NeonFilterChip(
                        label = strings.navGames,
                        selected = filterMode == "GAME",
                        accentColor = NeonPurple,
                        onClick = { filterMode = "GAME" }
                    )
                    NeonFilterChip(
                        label = strings.navApps,
                        selected = filterMode == "APP",
                        accentColor = ElectricBlue,
                        onClick = { filterMode = "APP" }
                    )
                    NeonFilterChip(
                        label = strings.adminFilterDrafts,
                        selected = filterMode == "DRAFT",
                        accentColor = CyberAmber,
                        onClick = { filterMode = "DRAFT" }
                    )
                }
            }

            when (catalogState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonPurple)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        StoreErrorBanner(message = catalogState.message, onRetry = null)
                    }
                }
                is UiState.Success -> {
                    items(filtered, key = { "admin_${it.id}" }) { item ->
                        AdminCatalogItemCard(
                            item = item,
                            onEdit = { onEditItem(item.id) },
                            onToggleFeatured = { onToggleFeatured(item) },
                            onRequestDelete = { pendingDelete = item }
                        )
                    }
                }
            }
        }
    }

    pendingDelete?.let { itemToDelete ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = ObsidianSurface,
            title = { Text(strings.adminDeleteTitle, color = SnowPrimary) },
            text = {
                Text(
                    text = "${itemToDelete.localizedTitle(strings.language)}\n\n${strings.adminDeleteConfirm}",
                    color = LavenderSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteItem(itemToDelete)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_item_button")
                ) {
                    Text(strings.adminDelete, color = CyberDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(strings.adminCancel, color = LavenderSecondary)
                }
            }
        )
    }
}

@Composable
private fun AdminKpiCard(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = LavenderSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminCatalogItemCard(
    item: CatalogItem,
    onEdit: () -> Unit,
    onToggleFeatured: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val strings = LocalStrings.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_item_card_${item.id}"),
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
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.localizedTitle(strings.language),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SnowPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypePillBadge(
                        itemType = item.parsedType,
                        label = if (item.parsedType == ItemType.GAME) strings.typeGame else strings.typeApp
                    )
                    if (!item.published) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberAmber.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = strings.draftTag,
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "v${item.version} • ${item.fileSize}",
                        style = MaterialTheme.typography.labelSmall,
                        color = LavenderSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFeatured) {
                    Icon(
                        imageVector = if (item.featured) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                        contentDescription = strings.featuredTag,
                        tint = if (item.featured) CyberAmber else LavenderSecondary
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("admin_edit_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = strings.adminEdit,
                        tint = ElectricBlue
                    )
                }
                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier.testTag("admin_delete_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = strings.adminDelete,
                        tint = CyberDanger
                    )
                }
            }
        }
    }
}
