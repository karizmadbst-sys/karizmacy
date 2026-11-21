package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.CatalogItem
import com.example.data.ItemType
import com.example.data.StoreCategory
import com.example.data.UiState
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.launchDownloadUrl
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminEditorScreen
import com.example.ui.screens.CatalogBrowseScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ItemDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBase
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.SnowPrimary
import com.example.viewmodel.StoreViewModel
import kotlinx.coroutines.launch

object StoreRoutes {
    const val HOME = "home"
    const val GAMES = "games"
    const val APPS = "apps"
    const val SEARCH = "search"
    const val PROFILE = "profile"
    const val DETAIL = "detail/{itemId}"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_EDITOR = "admin_editor?itemId={itemId}"

    fun detailRoute(itemId: String) = "detail/$itemId"
    fun adminEditorRoute(itemId: String? = null) =
        if (itemId.isNullOrBlank()) "admin_editor" else "admin_editor?itemId=$itemId"
}

private data class TopNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun KarizmaStoreApp(
    viewModel: StoreViewModel,
    userDisplayName: String,
    userEmail: String?,
    userPhotoUrl: String?,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSignOut: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val catalogState by viewModel.publishedCatalogState.collectAsStateWithLifecycle()
    val adminCatalogState by viewModel.adminCatalogState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val adminBusy by viewModel.adminBusy.collectAsStateWithLifecycle()

    var pendingSearchCategory by remember { mutableStateOf<StoreCategory?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = setOf(
        StoreRoutes.HOME,
        StoreRoutes.GAMES,
        StoreRoutes.APPS,
        StoreRoutes.SEARCH,
        StoreRoutes.PROFILE
    )
    val showPrimaryNav = currentRoute in topLevelRoutes

    val navItems = listOf(
        TopNavItem(StoreRoutes.HOME, strings.navHome, Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
        TopNavItem(StoreRoutes.GAMES, strings.navGames, Icons.Rounded.SportsEsports, Icons.Outlined.SportsEsports, "nav_games"),
        TopNavItem(StoreRoutes.APPS, strings.navApps, Icons.Rounded.Apps, Icons.Outlined.Apps, "nav_apps"),
        TopNavItem(StoreRoutes.SEARCH, strings.navSearch, Icons.Rounded.Search, Icons.Outlined.Search, "nav_search"),
        TopNavItem(StoreRoutes.PROFILE, strings.navProfile, Icons.Rounded.Person, Icons.Outlined.Person, "nav_profile")
    )

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun triggerDownload(item: CatalogItem) {
        val launched = launchDownloadUrl(context, item.downloadUrl)
        if (launched) {
            viewModel.recordDownload(item)
            scope.launch { snackbarHostState.showSnackbar(strings.openingDownload) }
        } else {
            scope.launch { snackbarHostState.showSnackbar(strings.invalidDownloadLink) }
        }
    }

    fun toggleLanguage() {
        val next = if (currentLanguage == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
        onSelectLanguage(next)
        viewModel.syncLanguageToCloud(next)
    }

    CyberBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 680.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (showPrimaryNav && !useNavigationRail) {
                        NavigationBar(
                            containerColor = ObsidianBase.copy(alpha = 0.96f),
                            tonalElevation = 8.dp
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(item.route) },
                                    modifier = Modifier.testTag(item.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label, maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ElectricBlue,
                                        selectedTextColor = SnowPrimary,
                                        unselectedIconColor = LavenderSecondary,
                                        unselectedTextColor = LavenderSecondary,
                                        indicatorColor = NeonPurple.copy(alpha = 0.24f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (showPrimaryNav && useNavigationRail) {
                        NavigationRail(
                            containerColor = ObsidianBase.copy(alpha = 0.95f),
                            modifier = Modifier.statusBarsPadding()
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(item.route) },
                                    modifier = Modifier.testTag(item.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label
                                        )
                                    },
                                    label = { Text(item.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ElectricBlue,
                                        selectedTextColor = SnowPrimary,
                                        unselectedIconColor = LavenderSecondary,
                                        unselectedTextColor = LavenderSecondary,
                                        indicatorColor = ObsidianSurfaceHigh
                                    )
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        NavHost(
                            navController = navController,
                            startDestination = StoreRoutes.HOME
                        ) {
                            composable(StoreRoutes.HOME) {
                                HomeScreen(
                                    catalogState = catalogState,
                                    favoriteIds = favoriteIds,
                                    isAdmin = isAdmin,
                                    currentLanguage = currentLanguage,
                                    onToggleLanguage = ::toggleLanguage,
                                    onOpenItem = { id -> navController.navigate(StoreRoutes.detailRoute(id)) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onQuickDownload = ::triggerDownload,
                                    onSeeAllGames = { navigateTopLevel(StoreRoutes.GAMES) },
                                    onSeeAllApps = { navigateTopLevel(StoreRoutes.APPS) },
                                    onOpenSearch = { category ->
                                        pendingSearchCategory = category
                                        navigateTopLevel(StoreRoutes.SEARCH)
                                    },
                                    onOpenAdmin = { navController.navigate(StoreRoutes.ADMIN_DASHBOARD) },
                                    onSeedCatalog = { viewModel.seedStarterCatalog(strings.adminSeededMsg) }
                                )
                            }

                            composable(StoreRoutes.GAMES) {
                                CatalogBrowseScreen(
                                    itemType = ItemType.GAME,
                                    catalogState = catalogState,
                                    favoriteIds = favoriteIds,
                                    isAdmin = isAdmin,
                                    onOpenItem = { id -> navController.navigate(StoreRoutes.detailRoute(id)) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onSeedCatalog = { viewModel.seedStarterCatalog(strings.adminSeededMsg) }
                                )
                            }

                            composable(StoreRoutes.APPS) {
                                CatalogBrowseScreen(
                                    itemType = ItemType.APP,
                                    catalogState = catalogState,
                                    favoriteIds = favoriteIds,
                                    isAdmin = isAdmin,
                                    onOpenItem = { id -> navController.navigate(StoreRoutes.detailRoute(id)) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onSeedCatalog = { viewModel.seedStarterCatalog(strings.adminSeededMsg) }
                                )
                            }

                            composable(StoreRoutes.SEARCH) {
                                SearchScreen(
                                    catalogState = catalogState,
                                    favoriteIds = favoriteIds,
                                    initialCategory = pendingSearchCategory,
                                    onClearInitialCategory = { pendingSearchCategory = null },
                                    onOpenItem = { id -> navController.navigate(StoreRoutes.detailRoute(id)) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onQuickDownload = ::triggerDownload
                                )
                            }

                            composable(StoreRoutes.PROFILE) {
                                ProfileScreen(
                                    displayName = userDisplayName,
                                    email = userEmail,
                                    photoUrl = userPhotoUrl,
                                    isAdmin = isAdmin,
                                    currentLanguage = currentLanguage,
                                    catalogState = catalogState,
                                    favorites = favorites,
                                    downloads = downloads,
                                    onSelectLanguage = { lang ->
                                        onSelectLanguage(lang)
                                        viewModel.syncLanguageToCloud(lang)
                                    },
                                    onOpenAdminDashboard = { navController.navigate(StoreRoutes.ADMIN_DASHBOARD) },
                                    onOpenItem = { id -> navController.navigate(StoreRoutes.detailRoute(id)) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onQuickDownload = ::triggerDownload,
                                    onOpenDownloadUrl = { url ->
                                        val ok = launchDownloadUrl(context, url)
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                if (ok) strings.openingDownload else strings.invalidDownloadLink
                                            )
                                        }
                                    },
                                    onSignOut = onSignOut
                                )
                            }

                            composable(
                                route = StoreRoutes.DETAIL,
                                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                            ) { entry ->
                                val itemId = entry.arguments?.getString("itemId").orEmpty()
                                val publishedList = (catalogState as? UiState.Success)?.data.orEmpty()
                                val adminList = (adminCatalogState as? UiState.Success)?.data.orEmpty()
                                val item = publishedList.firstOrNull { it.id == itemId }
                                    ?: adminList.firstOrNull { it.id == itemId }

                                ItemDetailScreen(
                                    item = item,
                                    isFavorite = favoriteIds.contains(itemId),
                                    isAdmin = isAdmin,
                                    onBack = { navController.popBackStack() },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                                    onDownloadClick = ::triggerDownload,
                                    onEditInAdmin = { id ->
                                        navController.navigate(StoreRoutes.adminEditorRoute(id))
                                    }
                                )
                            }

                            composable(StoreRoutes.ADMIN_DASHBOARD) {
                                AdminDashboardScreen(
                                    isAdmin = isAdmin,
                                    adminBusy = adminBusy,
                                    catalogState = adminCatalogState,
                                    onBack = { navController.popBackStack() },
                                    onCreateNew = { navController.navigate(StoreRoutes.adminEditorRoute(null)) },
                                    onEditItem = { id -> navController.navigate(StoreRoutes.adminEditorRoute(id)) },
                                    onToggleFeatured = { viewModel.toggleFeatured(it) },
                                    onDeleteItem = { viewModel.deleteCatalogItem(it.id, strings.adminDeletedMsg) },
                                    onSeedStarterCatalog = { viewModel.seedStarterCatalog(strings.adminSeededMsg) }
                                )
                            }

                            composable(
                                route = StoreRoutes.ADMIN_EDITOR,
                                arguments = listOf(
                                    navArgument("itemId") {
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { entry ->
                                val editingId = entry.arguments?.getString("itemId")
                                val adminList = (adminCatalogState as? UiState.Success)?.data.orEmpty()
                                val existingItem = if (editingId.isNullOrBlank()) {
                                    null
                                } else {
                                    adminList.firstOrNull { it.id == editingId }
                                }
                                AdminEditorScreen(
                                    existingItem = existingItem,
                                    busy = adminBusy,
                                    onBack = { navController.popBackStack() },
                                    onSave = { draft ->
                                        viewModel.saveCatalogItem(
                                            editingItemId = editingId,
                                            draft = draft,
                                            successMessage = strings.adminSavedMsg,
                                            onSuccess = { navController.popBackStack() }
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
