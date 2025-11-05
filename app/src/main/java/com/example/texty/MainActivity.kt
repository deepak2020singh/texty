package com.example.texty

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.texty.ui.screen.CommentScreen
import com.example.texty.ui.screen.ComposeTextArea
import com.example.texty.ui.screen.FollowersScreen
import com.example.texty.ui.screen.FollowingScreen1
import com.example.texty.ui.screen.LoginScreen
import com.example.texty.ui.screen.MainScreen
import com.example.texty.ui.screen.ProfileScreen
import com.example.texty.ui.screen.SettingScreen
import com.example.texty.ui.screen.SignUpScreen
import com.example.texty.ui.screen.SplashScreen
import com.example.texty.ui.theme.TextyTheme
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val coroutineScope = rememberCoroutineScope()
            val userPreferencesManager: UserPreferencesManager = koinInject()
            var theme by remember { mutableStateOf(AppTheme.SYSTEM) }

            // Load saved theme
            LaunchedEffect(Unit) {
                theme = userPreferencesManager.getUserPreferences().first().theme
            }

            TextyTheme(
                darkTheme = when (theme) {
                    AppTheme.LIGHT -> false
                    AppTheme.DARK -> true
                    AppTheme.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavHost(
                        innerPadding,
                        onThemeChanged = { newTheme ->
                            theme = newTheme
                            coroutineScope.launch {
                                userPreferencesManager.saveThemePreference(newTheme)
                            }
                        },
                        userPreferencesManager = userPreferencesManager
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    innerPadding: PaddingValues,
    onThemeChanged: (AppTheme) -> Unit,
    userPreferencesManager: UserPreferencesManager
) {
    val backStack = remember { mutableStateListOf<Any>(Routes.Splash) }
    val authViewModel: AuthViewModel = koinInject()
    val postViewModel: PostViewModel = koinInject()
    val realDb = koinInject<FirebaseDatabase>()


    NavDisplay(
        backStack = backStack,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        },
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        onBack = {
            if (backStack.size > 1) backStack.removeAt(
                backStack.lastIndex
            )
        },
        entryProvider = { key ->
            when (key) {

                is Routes.Splash -> NavEntry(key) {
                    SplashScreen(
                        onNavigateToMain = {
                            backStack.clear()
                            backStack.add(Routes.Main)
                        },
                        onNavigateToLogin = {
                            backStack.clear()
                            backStack.add(Routes.Login)
                        }
                    )
                }

                is Routes.Login -> NavEntry(key) {
                    LoginScreen(
                        onNavigateToMain = { backStack.add(Routes.Main) },
                        onNavigateToSignUp = { backStack.add(Routes.SignUp) },
                        userPreferencesManager = userPreferencesManager,
                        authViewModel = authViewModel,
                        realDb = realDb
                    )
                }

                is Routes.SignUp -> NavEntry(key) {
                    SignUpScreen(
                        onNavigateToLogin = { backStack.add(Routes.Login) },
                        authViewModel = authViewModel,
                        userPreferencesManager = userPreferencesManager

                    )
                }

                is Routes.Setting -> NavEntry(key) {
                    SettingScreen(
                        onLogoutNavigate = { backStack.add(Routes.Login) },
                        authViewModel = authViewModel,
                        userPreferencesManager = userPreferencesManager,
                        onThemeChanged = onThemeChanged,
                        onBackNavigate = { backStack.removeLastOrNull() }
                    )
                }

                is Routes.AddPost -> NavEntry(key) {
                    ComposeTextArea(
                        postViewModel = postViewModel,
                        onCancel = { backStack.removeLastOrNull() },
                        userPreferencesManager = userPreferencesManager,
                        )
                }

                is Routes.Profile -> NavEntry(key) {
                    ProfileScreen(
                        userId = key.userId,
                        innerPadding = innerPadding,
                        onNavigateToSetting = { backStack.add(Routes.Setting) },
                        authViewModel = authViewModel,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        postViewModel = postViewModel,
                        userPreferencesManager = userPreferencesManager,
                        onNavigateToFollowers = { backStack.add(Routes.Follower(it))},
                        onNavigateToFollowing = { backStack.add(Routes.Following(it))},
                        onNavigateToComposePost = { backStack.add(Routes.AddPost)}
                    )
                }

                is Routes.Follower -> NavEntry(key) {
                    FollowersScreen(
                        userId = key.userId,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToProfile = { backStack.add(Routes.Profile(it)) },
                        viewModel = authViewModel,
                    )
                }

                is Routes.Following -> NavEntry(key) {
                    FollowingScreen1(
                        userId = key.userId,
                        onBack = { backStack.removeLastOrNull() },
                        onNavigateToProfile = { backStack.add(Routes.Profile(it)) },
                        viewModel = authViewModel,
                    )
                }
                is Routes.Main -> NavEntry(key) {
                    MainScreen(
                        onThemeChanged = onThemeChanged,
                        userPreferencesManager = userPreferencesManager,
                        authViewModel = authViewModel,
                        postViewModel = postViewModel,
                        onNavigateToLogin = { backStack.add(Routes.Login) },
                        onNavigateToComposePost = { backStack.add(Routes.AddPost) },
                        onNavigateToProfile = { backStack.add(Routes.Profile(it)) },
                        onNavigateToSearch = { },
                        onNavigateToMessages = { backStack.add(Routes.AddComment(it))},
                        onNavigateToReply = { backStack.add(Routes.AddComment) }
                    )
                }

                is Routes.AddComment -> NavEntry(key) {
                    CommentScreen(
                        postId = key.postId,
                        onBackClick = { backStack.removeLastOrNull() },
                        viewModel = postViewModel,
                        authViewModel = authViewModel
                    )
                }

                else -> NavEntry(key) { Text(text = "${key.javaClass.simpleName}") }
            }
        }
    )
}

@Serializable
sealed class Routes {
    @Serializable
    data object Splash : Routes()

    @Serializable
    data object Login : Routes()

    @Serializable
    data object SignUp : Routes()

    @Serializable
    data object Main : Routes()

    @Serializable
    data object AddPost : Routes()

    @Serializable
    data object Setting : Routes()


    @Serializable
    data class AddComment(val postId: String) : Routes()

    @Serializable
    data class Profile(val userId: String) : Routes()

    @Serializable
    data class Follower(val userId: String) : Routes()

    @Serializable
    data class Following(val userId: String) : Routes()
}


