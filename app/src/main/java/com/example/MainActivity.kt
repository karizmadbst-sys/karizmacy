package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.AuthScreen
import com.example.auth.authStateFlow
import com.example.auth.signOutUser
import com.example.data.LanguagePreferences
import com.example.data.StoreRepository
import com.example.ui.KarizmaStoreApp
import com.example.ui.i18n.AppLanguage
import com.example.ui.theme.KarizmaGameTheme
import com.example.viewmodel.StoreViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            KarizmaRoot()
        }
    }
}

@Composable
private fun KarizmaRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val languagePrefs = remember(context) { LanguagePreferences(context.applicationContext) }
    val currentLanguage by languagePrefs.languageFlow.collectAsStateWithLifecycle(
        initialValue = AppLanguage.EN
    )
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val auth = Firebase.auth
    val currentUser by remember(auth) { auth.authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = auth.currentUser)

    KarizmaGameTheme(language = currentLanguage) {
        val user = currentUser
        if (user == null) {
            AuthScreen(
                currentLanguage = currentLanguage,
                onToggleLanguage = {
                    val next = if (currentLanguage == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
                    scope.launch { languagePrefs.setLanguage(next) }
                },
                onAuthSuccess = {
                    // Reactive authStateFlow transitions automatically when FirebaseAuth updates
                }
            )
        } else {
            val uid = user.uid
            key(uid) {
                val storeViewModel: StoreViewModel = viewModel(
                    key = uid,
                    factory = viewModelFactory {
                        initializer {
                            val app = checkNotNull(this[APPLICATION_KEY]) {
                                "APPLICATION_KEY missing from CreationExtras"
                            }
                            val databaseId = app.getString(R.string.firestore_database_id)
                            val db = FirebaseFirestore.getInstance(databaseId)
                            StoreViewModel(
                                repository = StoreRepository(db),
                                currentUserId = uid,
                                userDisplayName = user.displayName ?: "Karizma Player",
                                userEmail = user.email,
                                userPhotoUrl = user.photoUrl?.toString(),
                                initialLanguage = currentLanguage
                            )
                        }
                    }
                )

                KarizmaStoreApp(
                    viewModel = storeViewModel,
                    userDisplayName = user.displayName ?: "Karizma Player",
                    userEmail = user.email,
                    userPhotoUrl = user.photoUrl?.toString(),
                    currentLanguage = currentLanguage,
                    onSelectLanguage = { lang ->
                        scope.launch { languagePrefs.setLanguage(lang) }
                    },
                    onSignOut = {
                        signOutUser(
                            credentialManager = credentialManager,
                            scope = scope
                        )
                    }
                )
            }
        }
    }
}
