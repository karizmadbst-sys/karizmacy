package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ImageCodec
import com.example.data.PresetArtwork
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianSurfaceHigh

fun PresetArtwork.drawableResId(): Int = when (this) {
    PresetArtwork.CYBER_DRIFT -> R.drawable.img_store_cyber_drift_1795180172567
    PresetArtwork.NOVA_STRIKE -> R.drawable.img_store_nova_strike_1795180172005
    PresetArtwork.ECLIPSE_REALM -> R.drawable.img_store_eclipse_realm_1795180172229
    PresetArtwork.NEON_APPS -> R.drawable.img_store_neon_apps_1795180172718
    PresetArtwork.KARIZMA_EMBLEM -> R.drawable.img_karizma_icon_1795180171265
}

/**
 * Renders any catalog artwork seamlessly:
 * - Built-in futuristic store presets (`preset:...`)
 * - Base64 compressed uploads from Android Photo Picker (`data:image/...`)
 * - Remote HTTP/HTTPS URLs via Coil AsyncImage
 */
@Composable
fun StoreArtwork(
    uri: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val trimmed = uri.trim()
    val preset = remember(trimmed) { PresetArtwork.fromUri(trimmed) }

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(ObsidianSurfaceHigh, NeonPurple.copy(alpha = 0.22f), ElectricBlue.copy(alpha = 0.16f))
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        when {
            preset != null -> {
                Image(
                    painter = painterResource(id = preset.drawableResId()),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            trimmed.startsWith("data:image/", ignoreCase = true) -> {
                val bitmap = remember(trimmed) { ImageCodec.decodeDataUriToBitmap(trimmed) }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = contentDescription,
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    ArtworkFallbackIcon()
                }
            }
            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> {
                AsyncImage(
                    model = trimmed,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                ArtworkFallbackIcon()
            }
        }
    }
}

@Composable
private fun ArtworkFallbackIcon() {
    Icon(
        imageVector = Icons.Rounded.SportsEsports,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        modifier = Modifier.size(36.dp)
    )
}
