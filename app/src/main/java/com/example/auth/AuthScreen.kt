package com.example.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.components.CyberBackdrop
import com.example.ui.components.LanguageToggleChip
import com.example.ui.components.NeonGradientButton
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.LavenderSecondary
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.SnowPrimary

@Composable
fun AuthScreen(
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onAuthSuccess: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var visible by remember { mutableStateOf(false) }

    // Silent startup auto sign-in via GetGoogleIdOption (separate request)
    LaunchedEffect(Unit) {
        visible = true
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = { /* Stay on interactive AuthScreen */ },
            scope = scope
        )
    }

    CyberBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NeonPurple.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = strings.liveDatabaseBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SnowPrimary
                        )
                    }
                }
                LanguageToggleChip(
                    currentLanguage = currentLanguage,
                    onToggle = onToggleLanguage
                )
            }

            Spacer(Modifier.height(36.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 4 }
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 460.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Glowing Karizma Emblem
                    Box(
                        modifier = Modifier
                            .size(124.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(listOf(NeonPurple, ElectricBlue)),
                                shape = RoundedCornerShape(30.dp)
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_karizma_icon_1795180171265),
                            contentDescription = strings.brandName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(22.dp))

                    Text(
                        text = strings.brandName,
                        style = MaterialTheme.typography.displayMedium,
                        color = SnowPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = strings.brandTagline,
                        style = MaterialTheme.typography.titleSmall,
                        color = ElectricBlue,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(28.dp))

                    // Glassmorphic Entry Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = ObsidianSurface.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = strings.authHeadline,
                                style = MaterialTheme.typography.headlineMedium,
                                color = SnowPrimary,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = strings.authSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LavenderSecondary,
                                textAlign = TextAlign.Center
                            )

                            // Feature pills
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AuthFeatureRow(
                                    icon = Icons.Rounded.CloudDone,
                                    label = strings.featureCloudCatalog,
                                    tint = NeonPurple
                                )
                                AuthFeatureRow(
                                    icon = Icons.Rounded.Download,
                                    label = strings.featureDirectDownloads,
                                    tint = ElectricBlue
                                )
                                AuthFeatureRow(
                                    icon = Icons.Rounded.Language,
                                    label = strings.featureBilingualRtl,
                                    tint = NeonPurple
                                )
                            }

                            if (!errorMessage.isNullOrBlank()) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = CyberDanger.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, CyberDanger.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = errorMessage!!,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyberDanger,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            NeonGradientButton(
                                text = if (isLoading) strings.signingIn else strings.signInWithGoogle,
                                icon = Icons.Rounded.Login,
                                loading = isLoading,
                                enabled = !isLoading,
                                onClick = {
                                    errorMessage = null
                                    isLoading = true
                                    onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {
                                            isLoading = false
                                            onAuthSuccess()
                                        },
                                        onAuthError = { msg ->
                                            isLoading = false
                                            errorMessage = msg
                                        },
                                        scope = scope,
                                        onAuthCancelled = {
                                            isLoading = false
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_sign_in_button")
                            )

                            Text(
                                text = strings.authFooterNote,
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderSecondary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: ImageVector,
    label: String,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = tint.copy(alpha = 0.16f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .padding(7.dp)
                    .size(18.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = SnowPrimary
        )
    }
}
