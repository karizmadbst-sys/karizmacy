package com.example.data

import android.content.Context
import com.example.R
import com.example.ui.i18n.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class StoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val BOOTSTRAPPED_ADMIN_EMAIL = "aistudio.gamer.2@gmail.com"
        private const val COLLECTION_CATALOG = "catalog_items"
        private const val COLLECTION_ADMINS = "admins"
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_FAVORITES = "favorites"
        private const val SUBCOLLECTION_DOWNLOADS = "downloads"
    }

    private fun requireUserId(): String {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
        }
        return uid
    }

    // --- REALTIME OBSERVATION STREAMS ---

    fun observePublishedCatalog(): Flow<List<CatalogItem>> = flow {
        val uid = requireUserId()
        emitAll(
            db.collection(COLLECTION_CATALOG)
                .whereEqualTo("published", true)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CatalogItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, COLLECTION_CATALOG, uid)
                    throw error
                }
        )
    }

    fun observeAdminCreatedCatalog(userId: String): Flow<List<CatalogItem>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for admin catalog stream." }
        emitAll(
            db.collection(COLLECTION_CATALOG)
                .whereEqualTo("createdBy", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CatalogItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, COLLECTION_CATALOG, uid)
                    throw error
                }
        )
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for profile stream." }
        val path = "$COLLECTION_USERS/$userId"
        emitAll(
            db.collection(COLLECTION_USERS)
                .document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else null
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.GET, path, uid)
                    throw error
                }
        )
    }

    fun observeFavorites(userId: String): Flow<List<FavoriteEntry>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for favorites stream." }
        val path = "$COLLECTION_USERS/$userId/$SUBCOLLECTION_FAVORITES"
        emitAll(
            db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_FAVORITES)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        FavoriteEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    fun observeDownloads(userId: String): Flow<List<DownloadLog>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch for downloads stream." }
        val path = "$COLLECTION_USERS/$userId/$SUBCOLLECTION_DOWNLOADS"
        emitAll(
            db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_DOWNLOADS)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        DownloadLog::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    logStreamErrorIfActive(error, OperationType.LIST, path, uid)
                    throw error
                }
        )
    }

    // --- ADMIN VERIFICATION ---

    suspend fun checkIsAdmin(userId: String): Boolean {
        val currentUser = auth.currentUser ?: return false
        if (currentUser.uid != userId) return false
        if (currentUser.isEmailVerified &&
            currentUser.email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)
        ) {
            return true
        }
        val path = "$COLLECTION_ADMINS/$userId"
        return try {
            val doc = db.collection(COLLECTION_ADMINS).document(userId).get().await()
            doc.exists() && doc.getString("role") == "admin"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            false
        }
    }

    // --- USER PROFILE & PREFERENCES ---

    suspend fun ensureUserProfile(
        userId: String,
        displayName: String,
        email: String?,
        photoUrl: String?,
        preferredLanguage: AppLanguage
    ): Result<Unit> {
        val path = "$COLLECTION_USERS/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user profile write." }
            val docRef = db.collection(COLLECTION_USERS).document(userId)
            val existing = docRef.get().await()
            val safeName = displayName.trim().ifBlank { "Karizma Player" }.take(80)
            val safeEmail = email?.trim()?.take(120)
            val safePhoto = photoUrl?.trim()?.take(500)

            if (!existing.exists()) {
                val payload = buildMap<String, Any> {
                    put("userId", uid)
                    put("displayName", safeName)
                    if (!safeEmail.isNullOrBlank()) put("email", safeEmail)
                    if (!safePhoto.isNullOrBlank()) put("photoUrl", safePhoto)
                    put("preferredLanguage", preferredLanguage.code)
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.set(payload).await()
            } else {
                val updates = buildMap<String, Any> {
                    put("displayName", safeName)
                    if (!safeEmail.isNullOrBlank()) put("email", safeEmail)
                    if (!safePhoto.isNullOrBlank()) put("photoUrl", safePhoto)
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updatePreferredLanguage(userId: String, language: AppLanguage): Result<Unit> {
        val path = "$COLLECTION_USERS/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user language update." }
            val docRef = db.collection(COLLECTION_USERS).document(userId)
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "preferredLanguage" to language.code,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- FAVORITES & DOWNLOADS ---

    suspend fun toggleFavorite(
        userId: String,
        item: CatalogItem,
        currentlyFavorited: Boolean
    ): Result<Boolean> {
        val path = "$COLLECTION_USERS/$userId/$SUBCOLLECTION_FAVORITES/${item.id}"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user favorite write." }
            require(item.id.isNotBlank()) { "Catalog item ID must not be blank." }
            val docRef = db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_FAVORITES)
                .document(item.id)
            if (currentlyFavorited) {
                docRef.delete().await()
                Result.success(false)
            } else {
                val payload = mapOf(
                    "itemId" to item.id,
                    "itemTitle" to item.title.trim().ifBlank { "Item" }.take(100),
                    "itemType" to item.parsedType.raw,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                Result.success(true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(
                e,
                if (currentlyFavorited) OperationType.DELETE else OperationType.CREATE,
                path
            )
            Result.failure(e)
        }
    }

    suspend fun recordDownloadAndIncrement(userId: String, item: CatalogItem): Result<Unit> {
        val logPath = "$COLLECTION_USERS/$userId/$SUBCOLLECTION_DOWNLOADS"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Refusing cross-user download write." }
            require(item.id.isNotBlank()) { "Catalog item ID must not be blank." }
            require(isValidHttpUrl(item.downloadUrl)) { "Download URL must be valid http/https." }

            val logRef = db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(SUBCOLLECTION_DOWNLOADS)
                .document()

            val logPayload = mapOf(
                "itemId" to item.id,
                "itemTitle" to item.title.trim().ifBlank { "Item" }.take(100),
                "itemType" to item.parsedType.raw,
                "version" to item.version.trim().ifBlank { "1.0" }.take(32),
                "downloadUrl" to item.downloadUrl.trim().take(500),
                "createdAt" to FieldValue.serverTimestamp()
            )
            logRef.set(logPayload).await()

            if (item.published) {
                val itemRef = db.collection(COLLECTION_CATALOG).document(item.id)
                itemRef.update(
                    mapOf(
                        "downloadsCount" to FieldValue.increment(1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, logPath)
            Result.failure(e)
        }
    }

    // --- ADMIN CATALOG CRUD ---

    suspend fun createCatalogItem(draft: CatalogItemDraft): Result<String> {
        var docPath = COLLECTION_CATALOG
        return try {
            val uid = requireUserId()
            validateDraft(draft)
            val docRef = db.collection(COLLECTION_CATALOG).document()
            docPath = docRef.path
            val payload = buildCreatePayload(draft, uid)
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateCatalogItem(itemId: String, draft: CatalogItemDraft): Result<Unit> {
        val docPath = "$COLLECTION_CATALOG/$itemId"
        return try {
            requireUserId()
            require(itemId.isNotBlank()) { "Item ID must not be blank." }
            validateDraft(draft)
            val docRef = db.collection(COLLECTION_CATALOG).document(itemId)
            val updates = buildUpdatePayload(draft)
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun setItemFeatured(itemId: String, featured: Boolean): Result<Unit> {
        val docPath = "$COLLECTION_CATALOG/$itemId"
        return try {
            requireUserId()
            require(itemId.isNotBlank()) { "Item ID must not be blank." }
            db.collection(COLLECTION_CATALOG).document(itemId)
                .update(
                    mapOf(
                        "featured" to featured,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCatalogItem(itemId: String): Result<Unit> {
        val docPath = "$COLLECTION_CATALOG/$itemId"
        return try {
            requireUserId()
            require(itemId.isNotBlank()) { "Item ID must not be blank." }
            db.collection(COLLECTION_CATALOG).document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun seedStarterCatalog(forceWhenNonEmpty: Boolean = false): Result<Int> {
        return try {
            val uid = requireUserId()
            val existing = db.collection(COLLECTION_CATALOG)
                .whereEqualTo("published", true)
                .limit(1)
                .get()
                .await()

            if (!forceWhenNonEmpty && !existing.isEmpty) {
                return Result.success(0)
            }

            val batch = db.batch()
            SeedCatalogData.starterItems.forEachIndexed { idx, draft ->
                val slug = draft.title
                    .lowercase()
                    .replace(Regex("[^a-z0-9]+"), "_")
                    .trim('_')
                    .take(40)
                val docId = "starter_${idx + 1}_$slug"
                val ref = db.collection(COLLECTION_CATALOG).document(docId)
                val initialDownloads = when (idx) {
                    0 -> 14820L
                    1 -> 9640L
                    2 -> 18350L
                    6 -> 32410L
                    11 -> 41200L
                    else -> 5400L + (idx * 730L)
                }
                val payload = buildCreatePayload(draft, uid, initialDownloads)
                batch.set(ref, payload)
            }
            batch.commit().await()
            Result.success(SeedCatalogData.starterItems.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, COLLECTION_CATALOG)
            Result.failure(e)
        }
    }

    // --- HELPERS & VALIDATION PARITY WITH firestore.rules ---

    fun isValidHttpUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.length in 8..500 &&
            (trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("http://", ignoreCase = true))
    }

    private fun validateDraft(draft: CatalogItemDraft) {
        require(draft.title.trim().length in 1..100) { "Title must be 1..100 characters." }
        require(draft.titleAr.trim().length <= 100) { "Arabic title must be <= 100 characters." }
        require(draft.developer.trim().length in 1..80) { "Developer must be 1..80 characters." }
        require(draft.summary.trim().length in 1..240) { "Summary must be 1..240 characters." }
        require(draft.summaryAr.trim().length <= 240) { "Arabic summary must be <= 240 characters." }
        require(draft.description.trim().length in 1..3000) { "Description must be 1..3000 characters." }
        require(draft.descriptionAr.trim().length <= 3000) { "Arabic description must be <= 3000 characters." }
        require(draft.version.trim().length in 1..32) { "Version must be 1..32 characters." }
        require(draft.fileSize.trim().length in 1..32) { "File size must be 1..32 characters." }
        require(isValidHttpUrl(draft.downloadUrl)) { "Download URL must start with http:// or https://" }
        require(draft.coverImageUrl.length <= 250_000) { "Cover image exceeds maximum size." }
        require(draft.screenshotUrls.size <= 5) { "Maximum 5 screenshots allowed." }
        require(draft.screenshotUrls.all { it.length <= 200_000 }) { "Screenshot exceeds maximum size." }
        require(draft.rating in 0.0..5.0) { "Rating must be between 0.0 and 5.0." }
    }

    private fun buildCreatePayload(
        draft: CatalogItemDraft,
        uid: String,
        downloadsCount: Long = 0L
    ): Map<String, Any> = buildMap {
        put("title", draft.title.trim())
        if (draft.titleAr.isNotBlank()) put("titleAr", draft.titleAr.trim())
        put("itemType", draft.itemType.raw)
        put("category", draft.category.key)
        put("developer", draft.developer.trim())
        put("summary", draft.summary.trim())
        if (draft.summaryAr.isNotBlank()) put("summaryAr", draft.summaryAr.trim())
        put("description", draft.description.trim())
        if (draft.descriptionAr.isNotBlank()) put("descriptionAr", draft.descriptionAr.trim())
        put("version", draft.version.trim())
        put("fileSize", draft.fileSize.trim())
        put("downloadUrl", draft.downloadUrl.trim())
        put("coverImageUrl", draft.coverImageUrl.trim())
        put("screenshotUrls", draft.screenshotUrls.map { it.trim() }.filter { it.isNotBlank() }.take(5))
        put("rating", draft.rating.coerceIn(0.0, 5.0))
        put("downloadsCount", downloadsCount.coerceAtLeast(0L))
        put("featured", draft.featured)
        put("published", draft.published)
        put("createdBy", uid)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }

    private fun buildUpdatePayload(draft: CatalogItemDraft): Map<String, Any> = mapOf(
        "title" to draft.title.trim(),
        "titleAr" to draft.titleAr.trim(),
        "itemType" to draft.itemType.raw,
        "category" to draft.category.key,
        "developer" to draft.developer.trim(),
        "summary" to draft.summary.trim(),
        "summaryAr" to draft.summaryAr.trim(),
        "description" to draft.description.trim(),
        "descriptionAr" to draft.descriptionAr.trim(),
        "version" to draft.version.trim(),
        "fileSize" to draft.fileSize.trim(),
        "downloadUrl" to draft.downloadUrl.trim(),
        "coverImageUrl" to draft.coverImageUrl.trim(),
        "screenshotUrls" to draft.screenshotUrls.map { it.trim() }.filter { it.isNotBlank() }.take(5),
        "rating" to draft.rating.coerceIn(0.0, 5.0),
        "featured" to draft.featured,
        "published" to draft.published,
        "updatedAt" to FieldValue.serverTimestamp()
    )

    private fun logStreamErrorIfActive(
        error: Throwable,
        operationType: OperationType,
        path: String,
        expectedUid: String
    ) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) return
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == expectedUid) {
            handleFirestoreError(exceptionToLog, operationType, path)
        }
    }
}
