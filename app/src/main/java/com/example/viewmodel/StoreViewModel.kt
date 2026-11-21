package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CatalogItem
import com.example.data.CatalogItemDraft
import com.example.data.DownloadLog
import com.example.data.FavoriteEntry
import com.example.data.StoreRepository
import com.example.data.UiState
import com.example.data.UserProfile
import com.example.ui.i18n.AppLanguage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StoreViewModel(
    private val repository: StoreRepository,
    val currentUserId: String,
    private val userDisplayName: String,
    private val userEmail: String?,
    private val userPhotoUrl: String?,
    private val initialLanguage: AppLanguage
) : ViewModel() {

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _adminBusy = MutableStateFlow(false)
    val adminBusy: StateFlow<Boolean> = _adminBusy.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var hasCheckedInitialSeed = false

    // Published catalog visible to all users
    val publishedCatalogState: StateFlow<UiState<List<CatalogItem>>> =
        repository.observePublishedCatalog()
            .map<List<CatalogItem>, UiState<List<CatalogItem>>> { items ->
                val sorted = items.sortedWith(
                    compareByDescending<CatalogItem> { it.featured }
                        .thenByDescending { it.downloadsCount }
                        .thenByDescending { it.createdAt ?: Timestamp(0, 0) }
                )
                UiState.Success(sorted)
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreError(error)
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing catalog", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to sync catalog"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    // Combined admin catalog (all published items + any unpublished drafts created by this admin)
    val adminCatalogState: StateFlow<UiState<List<CatalogItem>>> = combine(
        repository.observePublishedCatalog(),
        repository.observeAdminCreatedCatalog(currentUserId)
    ) { published, createdByMe ->
        val mergedById = LinkedHashMap<String, CatalogItem>()
        published.forEach { mergedById[it.id] = it }
        createdByMe.forEach { mergedById[it.id] = it }
        mergedById.values.sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
    }
        .map<List<CatalogItem>, UiState<List<CatalogItem>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            Log.w(TAG, "Error observing admin catalog", rootError)
            emit(UiState.Error(rootError.message ?: "Failed to sync admin catalog"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val userProfile: StateFlow<UserProfile?> = repository.observeUserProfile(currentUserId)
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing user profile", firestoreError ?: error)
            emit(null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    val favorites: StateFlow<List<FavoriteEntry>> = repository.observeFavorites(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing favorites", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val favoriteIds: StateFlow<Set<String>> = favorites
        .map { entries -> entries.map { it.itemId }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptySet()
        )

    val downloads: StateFlow<List<DownloadLog>> = repository.observeDownloads(currentUserId)
        .map { list -> list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) } }
        .catch { error ->
            val firestoreError = unwrapFirestoreError(error)
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing downloads", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(
                userId = currentUserId,
                displayName = userDisplayName,
                email = userEmail,
                photoUrl = userPhotoUrl,
                preferredLanguage = initialLanguage
            )
            val admin = repository.checkIsAdmin(currentUserId)
            _isAdmin.value = admin
            if (admin && !hasCheckedInitialSeed) {
                hasCheckedInitialSeed = true
                repository.seedStarterCatalog(forceWhenNonEmpty = false)
            }
        }
    }

    fun syncLanguageToCloud(language: AppLanguage) {
        viewModelScope.launch {
            repository.updatePreferredLanguage(currentUserId, language)
        }
    }

    fun toggleFavorite(item: CatalogItem) {
        val currentlyFav = favoriteIds.value.contains(item.id)
        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, item, currentlyFav)
                .onFailure { err ->
                    _events.tryEmit(err.localizedMessage ?: "Failed to update favorites")
                }
        }
    }

    fun recordDownload(item: CatalogItem) {
        viewModelScope.launch {
            repository.recordDownloadAndIncrement(currentUserId, item)
                .onFailure { err ->
                    Log.w(TAG, "Download tracking failed", err)
                }
        }
    }

    fun saveCatalogItem(
        editingItemId: String?,
        draft: CatalogItemDraft,
        successMessage: String,
        onSuccess: () -> Unit
    ) {
        if (!_isAdmin.value) {
            _events.tryEmit("Administrator privileges required.")
            return
        }
        viewModelScope.launch {
            _adminBusy.value = true
            val result = if (editingItemId.isNullOrBlank()) {
                repository.createCatalogItem(draft).map { Unit }
            } else {
                repository.updateCatalogItem(editingItemId, draft)
            }
            _adminBusy.value = false
            result
                .onSuccess {
                    _events.tryEmit(successMessage)
                    onSuccess()
                }
                .onFailure { err ->
                    _events.tryEmit(err.localizedMessage ?: "Failed to save catalog item")
                }
        }
    }

    fun toggleFeatured(item: CatalogItem) {
        if (!_isAdmin.value) return
        viewModelScope.launch {
            repository.setItemFeatured(item.id, !item.featured)
                .onFailure { err ->
                    _events.tryEmit(err.localizedMessage ?: "Failed to update featured status")
                }
        }
    }

    fun deleteCatalogItem(itemId: String, deletedMessage: String) {
        if (!_isAdmin.value) return
        viewModelScope.launch {
            _adminBusy.value = true
            val result = repository.deleteCatalogItem(itemId)
            _adminBusy.value = false
            result
                .onSuccess { _events.tryEmit(deletedMessage) }
                .onFailure { err ->
                    _events.tryEmit(err.localizedMessage ?: "Failed to delete item")
                }
        }
    }

    fun seedStarterCatalog(seededMessage: String) {
        if (!_isAdmin.value) return
        viewModelScope.launch {
            _adminBusy.value = true
            val result = repository.seedStarterCatalog(forceWhenNonEmpty = true)
            _adminBusy.value = false
            result
                .onSuccess { _events.tryEmit(seededMessage) }
                .onFailure { err ->
                    _events.tryEmit(err.localizedMessage ?: "Failed to populate catalog")
                }
        }
    }

    private fun unwrapFirestoreError(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    private companion object {
        const val TAG = "StoreViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
