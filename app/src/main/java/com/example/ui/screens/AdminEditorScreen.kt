package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CatalogItem
import com.example.data.CatalogItemDraft
import com.example.data.ImageCodec
import com.example.data.ItemType
import com.example.data.PresetArtwork
import com.example.data.StoreCategory
import com.example.ui.components.NeonFilterChip
import com.example.ui.components.NeonGradientButton
import com.example.ui.components.StoreArtwork
import com.example.ui.components.icon
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SnowPrimary
import kotlinx.coroutines.launch

@Composable
fun AdminEditorScreen(
    existingItem: CatalogItem?,
    busy: Boolean,
    onBack: () -> Unit,
    onSave: (CatalogItemDraft) -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isEdit = existingItem != null
    var itemType by remember(existingItem) { mutableStateOf(existingItem?.parsedType ?: ItemType.GAME) }
    var category by remember(existingItem) {
        mutableStateOf(existingItem?.parsedCategory ?: StoreCategory.ACTION)
    }
    var titleEn by remember(existingItem) { mutableStateOf(existingItem?.title ?: "") }
    var titleAr by remember(existingItem) { mutableStateOf(existingItem?.titleAr ?: "") }
    var developer by remember(existingItem) { mutableStateOf(existingItem?.developer ?: "") }
    var version by remember(existingItem) { mutableStateOf(existingItem?.version ?: "1.0.0") }
    var fileSize by remember(existingItem) { mutableStateOf(existingItem?.fileSize ?: "95 MB") }
    var ratingText by remember(existingItem) {
        mutableStateOf((existingItem?.rating ?: 4.8).toString())
    }
    var downloadUrl by remember(existingItem) { mutableStateOf(existingItem?.downloadUrl ?: "https://") }
    var summaryEn by remember(existingItem) { mutableStateOf(existingItem?.summary ?: "") }
    var summaryAr by remember(existingItem) { mutableStateOf(existingItem?.summaryAr ?: "") }
    var descriptionEn by remember(existingItem) { mutableStateOf(existingItem?.description ?: "") }
    var descriptionAr by remember(existingItem) { mutableStateOf(existingItem?.descriptionAr ?: "") }
    var coverImageUrl by remember(existingItem) {
        mutableStateOf(existingItem?.coverImageUrl ?: PresetArtwork.CYBER_DRIFT.uri)
    }
    var coverUrlInput by remember(existingItem) {
        mutableStateOf(
            if (existingItem?.coverImageUrl?.startsWith("http") == true) existingItem.coverImageUrl else ""
        )
    }
    var screenshots by remember(existingItem) {
        mutableStateOf(existingItem?.screenshotUrls ?: emptyList())
    }
    var screenshotUrlInput by remember { mutableStateOf("") }
    var featured by remember(existingItem) { mutableStateOf(existingItem?.featured ?: false) }
    var published by remember(existingItem) { mutableStateOf(existingItem?.published ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker for Cover Image
    val coverPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                ImageCodec.encodeUriToDataUri(context, uri)
                    .onSuccess { dataUri ->
                        coverImageUrl = dataUri
                        coverUrlInput = ""
                        validationError = null
                    }
                    .onFailure {
                        validationError = it.localizedMessage ?: strings.errorGeneric
                    }
            }
        }
    }

    // Zero-permission Android Photo Picker for Screenshots
    val screenshotPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && screenshots.size < 5) {
            scope.launch {
                ImageCodec.encodeUriToDataUri(context, uri)
                    .onSuccess { dataUri ->
                        screenshots = (screenshots + dataUri).take(5)
                        validationError = null
                    }
                    .onFailure {
                        validationError = it.localizedMessage ?: strings.errorGeneric
                    }
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = ObsidianSurface,
        unfocusedContainerColor = ObsidianSurface,
        focusedBorderColor = ElectricBlue,
        unfocusedBorderColor = ObsidianBorder,
        focusedTextColor = SnowPrimary,
        unfocusedTextColor = SnowPrimary,
        focusedLabelColor = ElectricBlue,
        unfocusedLabelColor = LavenderSecondary,
        cursorColor = ElectricBlue
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_editor_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = strings.back,
                            tint = SnowPrimary
                        )
                    }
                    Text(
                        text = if (isEdit) strings.editorEditTitle else strings.editorNewTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = SnowPrimary
                    )
                }
            }

            // Item Type Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.editorItemType,
                        style = MaterialTheme.typography.titleSmall,
                        color = LavenderSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NeonFilterChip(
                            label = strings.typeGame,
                            selected = itemType == ItemType.GAME,
                            icon = Icons.Rounded.SportsEsports,
                            accentColor = NeonPurple,
                            onClick = {
                                itemType = ItemType.GAME
                                if (category.itemType != ItemType.GAME) {
                                    category = StoreCategory.ACTION
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_type_game")
                        )
                        NeonFilterChip(
                            label = strings.typeApp,
                            selected = itemType == ItemType.APP,
                            icon = Icons.Rounded.Apps,
                            accentColor = ElectricBlue,
                            onClick = {
                                itemType = ItemType.APP
                                if (category.itemType != ItemType.APP) {
                                    category = StoreCategory.TOOLS
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_type_app")
                        )
                    }
                }
            }

            // Cover Image Upload + Presets + URL
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.editorCoverSection,
                            style = MaterialTheme.typography.titleMedium,
                            color = SnowPrimary
                        )
                        StoreArtwork(
                            uri = coverImageUrl,
                            contentDescription = strings.editorCoverSection,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        )
                        OutlinedButton(
                            onClick = {
                                coverPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("editor_pick_cover_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ElectricBlue)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddPhotoAlternate,
                                contentDescription = null,
                                tint = ElectricBlue
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(strings.editorPickCoverPhoto, color = SnowPrimary)
                        }

                        OutlinedTextField(
                            value = coverUrlInput,
                            onValueChange = {
                                coverUrlInput = it
                                if (it.trim().startsWith("http://") || it.trim().startsWith("https://")) {
                                    coverImageUrl = it.trim()
                                }
                            },
                            label = { Text(strings.editorCoverUrlHint) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = strings.editorPresetArtwork,
                            style = MaterialTheme.typography.labelMedium,
                            color = LavenderSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(PresetArtwork.entries) { preset ->
                                val selected = coverImageUrl == preset.uri
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(96.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            coverImageUrl = preset.uri
                                            coverUrlInput = ""
                                        }
                                ) {
                                    StoreArtwork(
                                        uri = preset.uri,
                                        contentDescription = preset.labelEn,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) ElectricBlue else ObsidianBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                    )
                                    Text(
                                        text = if (strings.language == AppLanguage.AR) preset.labelAr else preset.labelEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) ElectricBlue else LavenderSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Screenshots Upload & List (Max 5)
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${strings.editorScreenshotsSection} (${screenshots.size}/5)",
                            style = MaterialTheme.typography.titleMedium,
                            color = SnowPrimary
                        )
                        if (screenshots.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(screenshots.indices.toList()) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .width(148.dp)
                                            .height(88.dp)
                                    ) {
                                        StoreArtwork(
                                            uri = screenshots[idx],
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                                        )
                                        IconButton(
                                            onClick = {
                                                screenshots = screenshots.toMutableList().apply { removeAt(idx) }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(30.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = ObsidianVoid.copy(alpha = 0.85f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Close,
                                                    contentDescription = strings.adminDelete,
                                                    tint = CyberDanger,
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (screenshots.size < 5) {
                            OutlinedButton(
                                onClick = {
                                    screenshotPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("editor_add_screenshot_photo"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, NeonPurple)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = NeonPurple
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(strings.editorAddScreenshotPhoto, color = SnowPrimary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = screenshotUrlInput,
                                    onValueChange = { screenshotUrlInput = it },
                                    label = { Text(strings.editorScreenshotUrlHint) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(
                                    onClick = {
                                        val clean = screenshotUrlInput.trim()
                                        if (clean.startsWith("http://") || clean.startsWith("https://")) {
                                            screenshots = (screenshots + clean).take(5)
                                            screenshotUrlInput = ""
                                        }
                                    },
                                    modifier = Modifier.height(52.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = strings.editorAddButton)
                                }
                            }
                        }
                    }
                }
            }

            // Category Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = strings.editorCategory,
                        style = MaterialTheme.typography.titleSmall,
                        color = LavenderSecondary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(StoreCategory.forType(itemType)) { cat ->
                            NeonFilterChip(
                                label = cat.label(strings),
                                selected = category == cat,
                                icon = cat.icon(),
                                accentColor = if (itemType == ItemType.GAME) NeonPurple else ElectricBlue,
                                onClick = { category = cat }
                            )
                        }
                    }
                }
            }

            // Core Metadata Fields
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = titleEn,
                        onValueChange = { titleEn = it.take(100) },
                        label = { Text(strings.editorTitleEn) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_title_en_input")
                    )
                    OutlinedTextField(
                        value = titleAr,
                        onValueChange = { titleAr = it.take(100) },
                        label = { Text(strings.editorTitleAr) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = developer,
                        onValueChange = { developer = it.take(80) },
                        label = { Text(strings.editorDeveloper) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_developer_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = version,
                            onValueChange = { version = it.take(32) },
                            label = { Text(strings.editorVersion) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fileSize,
                            onValueChange = { fileSize = it.take(32) },
                            label = { Text(strings.editorFileSize) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = ratingText,
                            onValueChange = { ratingText = it.take(4) },
                            label = { Text(strings.editorRating) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier.weight(0.45f)
                        )
                        OutlinedTextField(
                            value = downloadUrl,
                            onValueChange = { downloadUrl = it.take(500) },
                            label = { Text(strings.editorDownloadUrl) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            shape = RoundedCornerShape(14.dp),
                            colors = fieldColors,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("editor_download_url_input")
                        )
                    }
                    OutlinedTextField(
                        value = summaryEn,
                        onValueChange = { summaryEn = it.take(240) },
                        label = { Text(strings.editorSummaryEn) },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = summaryAr,
                        onValueChange = { summaryAr = it.take(240) },
                        label = { Text(strings.editorSummaryAr) },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descriptionEn,
                        onValueChange = { descriptionEn = it.take(3000) },
                        label = { Text(strings.editorDescriptionEn) },
                        minLines = 4,
                        maxLines = 7,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = descriptionAr,
                        onValueChange = { descriptionAr = it.take(3000) },
                        label = { Text(strings.editorDescriptionAr) },
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(14.dp),
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Switches
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.editorFeaturedToggle, color = SnowPrimary)
                            Switch(
                                checked = featured,
                                onCheckedChange = { featured = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ObsidianVoid,
                                    checkedTrackColor = NeonPurple
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(strings.editorPublishedToggle, color = SnowPrimary)
                            Switch(
                                checked = published,
                                onCheckedChange = { published = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ObsidianVoid,
                                    checkedTrackColor = ElectricBlue
                                )
                            )
                        }
                    }
                }
            }

            // Validation Error Banner
            if (!validationError.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = CyberDanger.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, CyberDanger.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberDanger,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Save CTA
            item {
                NeonGradientButton(
                    text = if (isEdit) strings.editorSaveChanges else strings.editorSavePublish,
                    icon = Icons.Rounded.Save,
                    loading = busy,
                    enabled = !busy,
                    onClick = {
                        val cleanTitle = titleEn.trim()
                        val cleanDev = developer.trim()
                        val cleanVersion = version.trim()
                        val cleanSize = fileSize.trim()
                        val cleanSummary = summaryEn.trim()
                        val cleanDesc = descriptionEn.trim()
                        val cleanUrl = downloadUrl.trim()
                        val parsedRating = ratingText.toDoubleOrNull()?.coerceIn(0.0, 5.0) ?: 4.7

                        if (cleanTitle.isEmpty() || cleanDev.isEmpty() || cleanVersion.isEmpty() ||
                            cleanSize.isEmpty() || cleanSummary.isEmpty() || cleanDesc.isEmpty()
                        ) {
                            validationError = strings.editorValidationError
                            return@NeonGradientButton
                        }
                        if (cleanUrl.length < 8 ||
                            (!cleanUrl.startsWith("https://", ignoreCase = true) &&
                                !cleanUrl.startsWith("http://", ignoreCase = true))
                        ) {
                            validationError = strings.editorInvalidUrlError
                            return@NeonGradientButton
                        }

                        validationError = null
                        onSave(
                            CatalogItemDraft(
                                title = cleanTitle,
                                titleAr = titleAr.trim(),
                                itemType = itemType,
                                category = category,
                                developer = cleanDev,
                                summary = cleanSummary,
                                summaryAr = summaryAr.trim(),
                                description = cleanDesc,
                                descriptionAr = descriptionAr.trim(),
                                version = cleanVersion,
                                fileSize = cleanSize,
                                downloadUrl = cleanUrl,
                                coverImageUrl = coverImageUrl.ifBlank { PresetArtwork.CYBER_DRIFT.uri },
                                screenshotUrls = screenshots.take(5),
                                rating = parsedRating,
                                featured = featured,
                                published = published
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_save_button")
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
