package com.example.texty.ui.screen

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Topic
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.scene.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import coil.compose.AsyncImage
import com.example.texty.AppTheme
import com.example.texty.R
import com.example.texty.UserPreferencesManager
import com.example.texty.data.ImageUtils
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
sealed class MainRoutes {
    @Serializable
    data object HomeWithTabs : MainRoutes()

    @Serializable
    data object Search : MainRoutes()

    @Serializable
    data object Notification : MainRoutes()

    @Serializable
    data object Bookmarks : MainRoutes()

    @Serializable
    data object TwitterBlue : MainRoutes()

    @Serializable
    data object Topics : MainRoutes()

    @Serializable
    data object Lists : MainRoutes()

    @Serializable
    data object Profile : MainRoutes()

    @Serializable
    data object Messages : MainRoutes()

    @Serializable
    data object Settings : MainRoutes()
}

data class NavItem(
    val icon: Int,
    val title: String
)

data class DrawerItem(
    val icon: ImageVector,
    val title: String,
    val route: MainRoutes
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(userProfile: String? = null, onDrawerClick: () -> Unit) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_twitter),
                    contentDescription = "Twitter Logo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        navigationIcon = {
            Row {
                Spacer(modifier = Modifier.width(10.dp))
                AsyncImage(
                    model = userProfile?.let { ImageUtils.base64ToBitmap(it) },
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable { onDrawerClick.invoke() },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.user)
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Settings action */ }) {
                Icon(
                    painter = painterResource(R.drawable.communications),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    userProfile: String? = null,
    onDrawerClick: () -> Unit,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    onClearSearch: () -> Unit,
    onFocusChanged: (Boolean) -> Unit // Add this parameter
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChanged(focusState.isFocused) // Call the callback
                    },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.text.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                singleLine = true
            )
        },
        navigationIcon = {
            Row {
                Spacer(modifier = Modifier.width(10.dp))
                AsyncImage(
                    model = userProfile?.let { ImageUtils.base64ToBitmap(it) },
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable { onDrawerClick.invoke() },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.user)
                )
            }
        },
        actions = {
            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(R.drawable.file),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksTopBar(userProfile: String? = null, onDrawerClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Bookmarks",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            Row {
                Spacer(modifier = Modifier.width(10.dp))
                AsyncImage(
                    model = userProfile?.let { ImageUtils.base64ToBitmap(it) },
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable { onDrawerClick.invoke() },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.user)
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Settings action */ }) {
                Icon(
                    painter = painterResource(R.drawable.file),
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun DrawerContent(
    userProfile: String? = null,
    userName: String? = null,
    userHandle: String? = null,
    onItemClick: (MainRoutes) -> Unit,
    onProfileClick: () -> Unit,
    authViewModel: AuthViewModel
) {

    val user by authViewModel.userById.observeAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    LaunchedEffect(true) {
        authViewModel.fetchUserById(FirebaseAuth.getInstance().currentUser!!.uid)
    }




    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp)
        ) {
            // Profile Section
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() }
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = userProfile?.let { ImageUtils.base64ToBitmap(it) },
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.user)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = userName ?: "User Name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = userHandle ?: "@username",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Followers/Following Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "${user?.following?.size?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Following",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "${user?.followers?.size?: 0}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Followers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Navigation Items
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(getDrawerItems()) { item ->
                    DrawerItem(
                        icon = item.icon,
                        label = item.title,
                        route = item.route,
                        onItemClick = onItemClick
                    )
                }
            }

            // Footer Section
            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Theme toggle or other settings could go here
            Text(
                text = "© 2024 Texty App",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    route: MainRoutes,
    onItemClick: (MainRoutes) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(route) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun getDrawerItems(): List<DrawerItem> {
    return listOf(
        DrawerItem(Icons.AutoMirrored.Outlined.List, "Lists", MainRoutes.Lists),
        DrawerItem(Icons.Outlined.BookmarkBorder, "Bookmarks", MainRoutes.Bookmarks),
        DrawerItem(Icons.Outlined.Topic, "Topics", MainRoutes.Topics),
        DrawerItem(Icons.Outlined.Star, "Twitter Blue", MainRoutes.TwitterBlue),
        DrawerItem(Icons.Outlined.Email, "Messages", MainRoutes.Messages)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onThemeChanged: (AppTheme) -> Unit,
    userPreferencesManager: UserPreferencesManager,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToComposePost: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToMessages: (String) -> Unit,
    onNavigateToReply: () -> Unit
) {
    val backStack1 = remember { mutableStateListOf<Any>(MainRoutes.HomeWithTabs) }
    val navList = listOf(
        NavItem(R.drawable.home, "Home"),
        NavItem(R.drawable.search_interface_symbol, "Search"),
        NavItem(R.drawable.bell, "Notifications"),
        NavItem(R.drawable.communications, "Bookmarks")
    )

    val user by authViewModel.currentUser.collectAsState()
    val userProfile by userPreferencesManager.getUserPreferences().collectAsState(null)

    val coroutineScope = rememberCoroutineScope()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Search query state
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
//   val focusManager = LocalFocusManager.current


    // Track search bar focus state at MainScreen level
    var isSearchFocused by remember { mutableStateOf(false) }

    // Clear search query
    fun clearSearch() {
        searchQuery = TextFieldValue("")
        isSearchFocused = false
    }

    // Handle focus change
    fun onSearchFocusChanged(focused: Boolean) {
        isSearchFocused = focused
    }

    // Fetch current user data
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { authViewModel.fetchUser(it) }
    }

    // Auto-focus search field when navigating to Search screen
//    LaunchedEffect(backStack1.lastOrNull()) {
//        if (backStack1.lastOrNull() == MainRoutes.Search) {
//            focusRequester.requestFocus()
//        }
//    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                userProfile = userProfile?.profilePictureUrl,
                userName = userProfile?.name ?: user?.displayName,
                userHandle = userProfile?.name ?: user?.email?.split("@")?.firstOrNull()
                    ?.let { "@$it" },
                onItemClick = { selectedRoute ->
                    backStack1.clear()
                    backStack1.add(selectedRoute)
                    coroutineScope.launch { drawerState.close() }
                },
                onProfileClick = {
                    currentUser?.uid?.let { onNavigateToProfile(it) }
                    coroutineScope.launch { drawerState.close() }
                },
                authViewModel = authViewModel
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                when (val currentRoute = backStack1.lastOrNull() ?: MainRoutes.HomeWithTabs) {
                    is MainRoutes.HomeWithTabs -> HomeTopBar(
                        userProfile = userProfile?.profilePictureUrl,
                        onDrawerClick = { coroutineScope.launch { drawerState.open() } }
                    )

                    is MainRoutes.Search -> SearchTopBar(
                        userProfile = userProfile?.profilePictureUrl,
                        onDrawerClick = { coroutineScope.launch { drawerState.open() } },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        focusRequester = focusRequester,
                        onClearSearch = { clearSearch() },
                        onFocusChanged = { focused ->
                            onSearchFocusChanged(focused)
                        }
                    )

                    is MainRoutes.Bookmarks -> BookmarksTopBar(
                        userProfile = userProfile?.profilePictureUrl,
                        onDrawerClick = { coroutineScope.launch { drawerState.open() } }
                    )

                    else -> TopAppBar(
                        title = {
                            Text(
                                text = getTitleForRoute(currentRoute as MainRoutes),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 16.dp),
                                textAlign = TextAlign.Start
                            )
                        },
                        navigationIcon = {
                            AsyncImage(
                                model = userProfile?.let { ImageUtils.base64ToBitmap(it.profilePictureUrl) },
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable { coroutineScope.launch { drawerState.open() } },
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.user)
                            )
                        },
                        actions = {
                            IconButton(onClick = { /* Settings action */ }) {
                                Icon(
                                    painter = painterResource(R.drawable.user),
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            navList.forEachIndexed { index, navItem ->
                                val isSelected = when (backStack1.last()) {
                                    MainRoutes.HomeWithTabs -> navItem.title == "Home"
                                    MainRoutes.Search -> navItem.title == "Search"
                                    MainRoutes.Notification -> navItem.title == "Notifications"
                                    MainRoutes.Bookmarks -> navItem.title == "Bookmarks"
                                    else -> false
                                }
                                val iconScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.2f else 1f,
                                    animationSpec = tween(durationMillis = 200)
                                )
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        val targetRoute = when (navItem.title) {
                                            "Home" -> MainRoutes.HomeWithTabs
                                            "Search" -> MainRoutes.Search
                                            "Notifications" -> MainRoutes.Notification
                                            "Bookmarks" -> MainRoutes.Bookmarks
                                            else -> MainRoutes.HomeWithTabs
                                        }
                                        if (backStack1.last() != targetRoute) {
                                            backStack1.clear()
                                            backStack1.add(targetRoute)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            painter = painterResource(navItem.icon),
                                            contentDescription = navItem.title,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .scale(iconScale),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack1,
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator()
                ),
                onBack = {
                    if (backStack1.size > 1) {
                        backStack1.removeLastOrNull()
                    } else {
                        backStack1.clear()
                        backStack1.add(MainRoutes.HomeWithTabs)
                    }
                },
                entryProvider = { key ->
                    when (key) {
                        is MainRoutes.HomeWithTabs -> NavEntry(key) {
                            HomeScreenWithTabs(
                                innerPadding = innerPadding,
                                authViewModel = authViewModel,
                                postViewModel = postViewModel,
                                onNavigateToComposePost = onNavigateToComposePost,
                                onNavigateToProfile = onNavigateToProfile,
                                onNavigateToSearch = onNavigateToSearch,
                                onNavigateToMessages = onNavigateToMessages,
                                onNavigateToReply = onNavigateToReply
                            )
                        }

                        is MainRoutes.Search -> NavEntry(key) {
                            SearchScreen(
                                innerPadding = innerPadding,
                                authViewModel = authViewModel,
                                postViewModel = postViewModel,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                focusRequester = focusRequester,
                                onClearSearch = { clearSearch() },
                                onNavigateToProfile = onNavigateToProfile,
                                onNavigateToComposePost = onNavigateToComposePost,
                                isSearchFocused = isSearchFocused,
                                onSearchFocusChange = { focused ->
                                    onSearchFocusChanged(focused)
                                }
                            )
                        }

                        is MainRoutes.Bookmarks -> NavEntry(key) {
                            BookmarksScreen(
                                innerPadding = innerPadding,
                                authViewModel = authViewModel,
                                postViewModel = postViewModel,
                                onNavigateBack = {
                                    if (backStack1.size > 1) {
                                        backStack1.removeLastOrNull()
                                    }
                                }
                            )
                        }

                        is MainRoutes.TwitterBlue -> NavEntry(key) {
                            TwitterBlueScreen(
                                innerPadding = innerPadding,
                                onNavigateBack = {
                                    if (backStack1.size > 1) {
                                        backStack1.removeLastOrNull()
                                    }
                                }
                            )
                        }

                        is MainRoutes.Notification -> NavEntry(key) {
                            FollowNotificationScreen(
                                innerPadding = innerPadding,
                                authViewModel = authViewModel,
                                firebaseUser = currentUser,
                                onNavigateToProfile = { onNavigateToProfile(it) }
                            )
                        }

                        is MainRoutes.Topics -> NavEntry(key) {
                            TopicsScreen(
                                innerPadding = innerPadding,
                                onNavigateBack = {
                                    if (backStack1.size > 1) {
                                        backStack1.removeLastOrNull()
                                    }
                                }
                            )
                        }

                        is MainRoutes.Lists -> NavEntry(key) {
                            ListsScreen(
                                innerPadding = innerPadding,
                                onNavigateBack = {
                                    if (backStack1.size > 1) {
                                        backStack1.removeLastOrNull()
                                    }
                                }
                            )
                        }

                        is MainRoutes.Profile -> NavEntry(key) {
                            // You'll need to implement ProfileScreen or navigate to existing profile
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Profile Screen - Coming Soon")
                            }
                        }

                        is MainRoutes.Messages -> NavEntry(key) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Messages Screen - Coming Soon")
                            }
                        }

                        is MainRoutes.Settings -> NavEntry(key) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Settings Screen - Coming Soon")
                            }
                        }

                        else -> NavEntry(key) {
                            Text(
                                text = "Coming Soon",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            )
        }
    }
}

// Helper function to get title for route
private fun getTitleForRoute(route: MainRoutes): String {
    return when (route) {
        is MainRoutes.HomeWithTabs -> "Home"
        is MainRoutes.Search -> "Search"
        is MainRoutes.Notification -> "Notifications"
        is MainRoutes.Bookmarks -> "Bookmarks"
        is MainRoutes.TwitterBlue -> "Twitter Blue"
        is MainRoutes.Topics -> "Topics"
        is MainRoutes.Lists -> "Lists"
        is MainRoutes.Profile -> "Profile"
        is MainRoutes.Messages -> "Messages"
        is MainRoutes.Settings -> "Settings"
    }
}

// Rest of your existing screens (BookmarksScreen, TwitterBlueScreen, TopicsScreen, ListsScreen, ProfileIcon) remain the same...
@Composable
fun BookmarksScreen(
    innerPadding: PaddingValues,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Bookmarks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Your saved posts will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun TwitterBlueScreen(
    innerPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = "Twitter Blue",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Subscribe to unlock premium features",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun TopicsScreen(
    innerPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = "Topics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Explore topics you're interested in",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ListsScreen(
    innerPadding: PaddingValues,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text(
            text = "Lists",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Create and manage your lists",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}


























