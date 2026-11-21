package com.example.data

import androidx.annotation.Keep
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.StoreStrings
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ItemType(val raw: String) {
    GAME("GAME"),
    APP("APP");

    companion object {
        fun fromRaw(raw: String?): ItemType =
            entries.firstOrNull { it.raw.equals(raw, ignoreCase = true) } ?: GAME
    }
}

enum class StoreCategory(val key: String, val itemType: ItemType) {
    ACTION("ACTION", ItemType.GAME),
    RACING("RACING", ItemType.GAME),
    RPG("RPG", ItemType.GAME),
    STRATEGY("STRATEGY", ItemType.GAME),
    ARCADE("ARCADE", ItemType.GAME),
    TOOLS("TOOLS", ItemType.APP),
    MEDIA("MEDIA", ItemType.APP),
    PRODUCTIVITY("PRODUCTIVITY", ItemType.APP),
    SECURITY("SECURITY", ItemType.APP),
    SOCIAL("SOCIAL", ItemType.APP);

    fun label(strings: StoreStrings): String = when (this) {
        ACTION -> strings.catAction
        RACING -> strings.catRacing
        RPG -> strings.catRpg
        STRATEGY -> strings.catStrategy
        ARCADE -> strings.catArcade
        TOOLS -> strings.catTools
        MEDIA -> strings.catMedia
        PRODUCTIVITY -> strings.catProductivity
        SECURITY -> strings.catSecurity
        SOCIAL -> strings.catSocial
    }

    companion object {
        fun fromKey(key: String?): StoreCategory =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: ACTION

        fun forType(type: ItemType): List<StoreCategory> =
            entries.filter { it.itemType == type }
    }
}

enum class SortOption {
    FEATURED,
    TOP_RATED,
    MOST_DOWNLOADED,
    NEWEST
}

@Keep
@IgnoreExtraProperties
data class CatalogItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val titleAr: String = "",
    val itemType: String = ItemType.GAME.raw,
    val category: String = StoreCategory.ACTION.key,
    val developer: String = "",
    val summary: String = "",
    val summaryAr: String = "",
    val description: String = "",
    val descriptionAr: String = "",
    val version: String = "1.0.0",
    val fileSize: String = "0 MB",
    val downloadUrl: String = "",
    val coverImageUrl: String = "",
    val screenshotUrls: List<String> = emptyList(),
    val rating: Double = 4.5,
    val downloadsCount: Long = 0L,
    val featured: Boolean = false,
    val published: Boolean = true,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val parsedType: ItemType
        get() = ItemType.fromRaw(itemType)

    val parsedCategory: StoreCategory
        get() = StoreCategory.fromKey(category)

    fun localizedTitle(language: AppLanguage): String =
        if (language == AppLanguage.AR && titleAr.isNotBlank()) titleAr else title

    fun localizedSummary(language: AppLanguage): String =
        if (language == AppLanguage.AR && summaryAr.isNotBlank()) summaryAr else summary

    fun localizedDescription(language: AppLanguage): String =
        if (language == AppLanguage.AR && descriptionAr.isNotBlank()) descriptionAr else description
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val preferredLanguage: String = AppLanguage.EN.code,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FavoriteEntry(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemType: String = ItemType.GAME.raw,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class DownloadLog(
    @DocumentId val id: String = "",
    val itemId: String = "",
    val itemTitle: String = "",
    val itemType: String = ItemType.GAME.raw,
    val version: String = "",
    val downloadUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class CatalogItemDraft(
    val title: String = "",
    val titleAr: String = "",
    val itemType: ItemType = ItemType.GAME,
    val category: StoreCategory = StoreCategory.ACTION,
    val developer: String = "",
    val summary: String = "",
    val summaryAr: String = "",
    val description: String = "",
    val descriptionAr: String = "",
    val version: String = "1.0.0",
    val fileSize: String = "120 MB",
    val downloadUrl: String = "",
    val coverImageUrl: String = PresetArtwork.CYBER_DRIFT.uri,
    val screenshotUrls: List<String> = emptyList(),
    val rating: Double = 4.7,
    val featured: Boolean = false,
    val published: Boolean = true
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
