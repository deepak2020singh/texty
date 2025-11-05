@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.example.texty.ui.screen

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.texty.R
import com.example.texty.common.formatTwitterTimestamp
import com.example.texty.data.ImageUtils
import com.example.texty.data.Post
import com.example.texty.data.PostType
import com.example.texty.data.RepostType
import com.example.texty.data.User
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class for suggested users
data class SuggestedUser(
    val id: String,
    val name: String,
    val username: String,
    val profileImage: String,
    val isVerified: Boolean = false,
    val description: String = "",
    val followersCount: String = ""
)

// Generate dummy suggested users
private val dummySuggestedUsers = listOf(
    SuggestedUser(
        id = "1",
        name = "Elon Musk",
        username = "elonmusk",
        profileImage = "https://pbs.twimg.com/profile_images/1683325380441128960/yRsRRjGO_400x400.jpg",
        isVerified = true,
        description = "Owner of Tesla, SpaceX, X",
        followersCount = "189.3M"
    ),
    SuggestedUser(
        id = "2",
        name = "MrBeast",
        username = "MrBeast",
        profileImage = "https://pbs.twimg.com/profile_images/1633827376827695104/Zf7kfLky_400x400.jpg",
        isVerified = true,
        description = "I give away money",
        followersCount = "45.2M"
    ),
    SuggestedUser(
        id = "3",
        name = "NASA",
        username = "NASA",
        profileImage = "https://pbs.twimg.com/profile_images/1321163587679784960/0ZxKlEKB_400x400.jpg",
        isVerified = true,
        description = "Explore the universe and discover our home planet",
        followersCount = "78.9M"
    ),
    SuggestedUser(
        id = "4",
        name = "Netflix",
        username = "netflix",
        profileImage = "https://pbs.twimg.com/profile_images/1542362341253926912/2n_m1Otw_400x400.jpg",
        isVerified = true,
        description = "Netflix",
        followersCount = "23.4M"
    ),
    SuggestedUser(
        id = "5",
        name = "Cristiano Ronaldo",
        username = "Cristiano",
        profileImage = "https://pbs.twimg.com/profile_images/1633506545764708352/7kGq-FWz_400x400.jpg",
        isVerified = true,
        description = "Footballer",
        followersCount = "111.2M"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreenWithTabs(
    innerPadding: PaddingValues,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    onNavigateToComposePost: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMessages: (String) -> Unit,
    onNavigateToReply: () -> Unit = {}
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("For You", "Following")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                )
            },
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = false).height(2.dp)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> HomeScreen(
                    innerPadding = PaddingValues(0.dp),
                    postViewModel = postViewModel,
                    authViewModel = authViewModel,
                    onNavigateToComposePost = onNavigateToComposePost,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToMessages = onNavigateToMessages,
                    onNavigateToReply = onNavigateToReply
                )
                1 -> FollowingScreen(
                    innerPadding = PaddingValues(0.dp),
                    postViewModel = postViewModel,
                    authViewModel = authViewModel,
                    selectedTabIndex = selectedTabIndex,
                    onNavigateToComposePost = onNavigateToComposePost,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToMessages = onNavigateToMessages
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    onNavigateToComposePost: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToMessages: (String) -> Unit = {},
    onNavigateToReply: () -> Unit = {}
) {
    val posts by postViewModel.posts.collectAsState()
    val uiState by postViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Use the user cache from AuthViewModel
    val userCache by authViewModel.userFlowCache.collectAsState()

    // State for full-screen media viewer (scoped here for HomeScreen)
    var showFullScreenMedia by remember { mutableStateOf(false) }
    var selectedPostForMedia by remember { mutableStateOf<Post?>(null) }
    var selectedMediaIndex by remember { mutableStateOf(0) }

    // Fetch users for all posts when posts change
    LaunchedEffect(posts) {
        val userIds = posts.map { it.userId }.distinct()
        authViewModel.fetchUsersForPosts(userIds)
    }

    // State for dismissed suggestions
    val dismissedSuggestions = remember { mutableStateSetOf<String>() }
    val visibleSuggestions = dummySuggestedUsers.filter { it.id !in dismissedSuggestions }

    // Only show suggestions when there are posts
    val postsWithSuggestions = remember(posts, visibleSuggestions) {
        if (posts.isEmpty() || visibleSuggestions.isEmpty()) {
            posts
        } else {
            insertSuggestionsInPosts(posts, visibleSuggestions)
        }
    }

    // Simulate loading for better UX
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    // Root Box for layering: Main content first, viewer on top
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Premium Banner (above content)
            PremiumBanner()

            if ((uiState.isLoading || isLoading) && posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LoadingIndicator()
                        Text(
                            text = "Loading posts...",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else if (posts.isEmpty()) {
                EmptyPostsState(
                    onNavigateToComposePost = onNavigateToComposePost
                )
            } else {
                uiState.error?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                // PostList
                PostList(
                    items = postsWithSuggestions,
                    onLoadMore = { postViewModel.loadFeed() },
                    postViewModel = postViewModel,
                    authViewModel = authViewModel,
                    userCache = userCache,
                    context = context,
                    onNavigateToProfile = onNavigateToProfile,
                    onNavigateToMessages = onNavigateToMessages,
                    onMediaClick = { post, index ->
                        selectedPostForMedia = post
                        selectedMediaIndex = index
                        showFullScreenMedia = true
                    },
                    onDismissSuggestion = { suggestionId ->
                        dismissedSuggestions.add(suggestionId)
                    }
                )
            }
        }

        // FloatingActionButton (drawn on top of main content, but below viewer)
        FloatingActionButtonWithOptions(
            onNavigateToComposePost = onNavigateToComposePost,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // Full-screen media viewer (last child = drawn on top of everything)
        if (showFullScreenMedia && selectedPostForMedia != null) {
            FullScreenMediaViewer(
                post = selectedPostForMedia!!,
                initialIndex = selectedMediaIndex,
                onDismiss = {
                    showFullScreenMedia = false
                    selectedPostForMedia = null
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f) // Force on top (optional, but ensures layering)
            )
        }
    }
}



@Composable
fun FullScreenMediaViewer(
    post: Post,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)) // Semi-transparent dark background
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Dismiss on tap outside the image (optional)
                    val screenWidth = size.width
                    if (offset.x < screenWidth * 0.2f || offset.x > screenWidth * 0.8f) {
                        onDismiss()
                    }
                }
            }
            .pointerInput(post.mediaBase64.size) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (post.mediaBase64.size > 1) {
                        if (dragAmount < -50f && currentIndex < post.mediaBase64.size - 1) {
                            currentIndex++ // Swipe left to next
                        } else if (dragAmount > 50f && currentIndex > 0) {
                            currentIndex-- // Swipe right to previous
                        }
                    }
                }
            }

    ) {
        // Close button
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Image counter
        if (post.mediaBase64.size > 1) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "${currentIndex + 1}/${post.mediaBase64.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Main image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        ) {
            val currentImage = post.mediaBase64[currentIndex]
            val bitmap = ImageUtils.base64ToBitmap(currentImage)

            AsyncImage(
                model = bitmap,
                contentDescription = "Full screen image",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            // Navigation arrows for multiple images
            if (post.mediaBase64.size > 1) {
                // Previous arrow
                if (currentIndex > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                            .size(40.dp)
                    ) {
                        IconButton(
                            onClick = { currentIndex-- },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_left),
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Next arrow
                if (currentIndex < post.mediaBase64.size - 1) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                            .size(40.dp)
                    ) {
                        IconButton(
                            onClick = { currentIndex++ },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_right),
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingActionButtonWithOptions(
    onNavigateToComposePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOptions by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    // Animation for FAB icon rotation
    val rotation by animateFloatAsState(
        targetValue = if (showOptions) 45f else 0f,
        label = "FAB rotation"
    )

    // Animation for FAB width (expands to pill shape when options shown)
    val fabWidth by animateDpAsState(
        targetValue = if (showOptions) 75.dp else 56.dp,
        label = "FAB width",
        animationSpec = tween(durationMillis = 300)
    )

    // Fixed radius for pill/circle shape (half of height)
    val fabRadius = 28.dp

    val options = listOf(
        Triple(Icons.Outlined.AutoAwesome, "Go Live", { /* Handle go live */ }),
        Triple(Icons.Outlined.ChatBubbleOutline, "Spaces", { /* Handle spaces */ }),
        Triple(Icons.Default.Edit, "Post", { onNavigateToComposePost() })
    )

    Column(
        modifier = modifier
            .padding(end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Options Column (appears above FAB when expanded)
        AnimatedVisibility(
            visible = showOptions,
            enter = slideInVertically(
                initialOffsetY = { it }, // Slide up from below
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it } // Slide down out of view
            ) + fadeOut(animationSpec = tween(300))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                options.forEachIndexed { index, (icon, text, onClick) ->
                    val delay = 50L * (options.size - 1 - index) // Staggered from top to bottom
                    LaunchedEffect(showOptions) {
                        // Optional: Add scale animation per option
                    }
                    FabOption(
                        icon = icon,
                        text = text,
                        onClick = {
                            coroutineScope.launch {
                                onClick()
                                showOptions = false
                            }
                        },
                        modifier = Modifier
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(25.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        // Main FAB at the bottom
        FloatingActionButton(
            onClick = { showOptions = !showOptions },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(fabRadius),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp,
                hoveredElevation = 8.dp
            ),
            interactionSource = interactionSource,
            modifier = Modifier
                .width(fabWidth)
                .height(56.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(fabRadius),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
        ) {
            Icon(
                imageVector = if (showOptions) Icons.Default.Close else Icons.Default.Edit,
                contentDescription = if (showOptions) "Close menu" else "Create content",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation)
            )
        }
    }
}

@Composable
fun FabOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp, // Material 3 elevation
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                onClick = onClick
            )
            .padding(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge, // Material 3 label style
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

//@Composable
//fun FloatingActionButtonWithOptions(
//    onNavigateToComposePost: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var showOptions by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//    val interactionSource = remember { MutableInteractionSource() }
//
//    // Animation for FAB icon rotation
//    val rotation by animateFloatAsState(
//        targetValue = if (showOptions) 45f else 0f,
//        label = "FAB rotation"
//    )
//
//    val options = listOf(
//        Triple(Icons.Outlined.AutoAwesome, "Go Live", { /* Handle go live */ }),
//        Triple(Icons.Outlined.ChatBubbleOutline, "Spaces", { /* Handle spaces */ }),
//        Triple(Icons.Default.Edit, "Post", { onNavigateToComposePost() })
//    )
//
//    Column(
//        modifier = modifier
//            .padding(end = 16.dp, bottom = 16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp),
//        horizontalAlignment = Alignment.End
//    ) {
//        // Options Column (appears above FAB when expanded)
//        AnimatedVisibility(
//            visible = showOptions,
//            enter = slideInVertically(
//                initialOffsetY = { it }, // Slide up from below
//                animationSpec = tween(300)
//            ) + fadeIn(animationSpec = tween(300)),
//            exit = slideOutVertically(
//                targetOffsetY = { it } // Slide down out of view
//            ) + fadeOut(animationSpec = tween(300))
//        ) {
//            Column(
//                verticalArrangement = Arrangement.spacedBy(8.dp),
//                horizontalAlignment = Alignment.End
//            ) {
//                options.forEachIndexed { index, (icon, text, onClick) ->
//                    val delay = 50L * (options.size - 1 - index) // Staggered from top to bottom
//                    LaunchedEffect(showOptions) {
//                        // Optional: Add scale animation per option
//                    }
//                    FabOption(
//                        icon = icon,
//                        text = text,
//                        onClick = {
//                            coroutineScope.launch {
//                                onClick()
//                                showOptions = false
//                            }
//                        },
//                        modifier = Modifier
//                            .shadow(
//                                elevation = 8.dp,
//                                shape = RoundedCornerShape(25.dp),
//                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
//                            )
//                    )
//                }
//            }
//        }
//
//        // Main FAB at the bottom
//        FloatingActionButton(
//            onClick = { showOptions = !showOptions },
//            containerColor = MaterialTheme.colorScheme.primary,
//            contentColor = MaterialTheme.colorScheme.onPrimary,
//            elevation = FloatingActionButtonDefaults.elevation(
//                defaultElevation = 6.dp,
//                pressedElevation = 12.dp,
//                hoveredElevation = 8.dp
//            ),
//            interactionSource = interactionSource,
//            modifier = Modifier
//                .size(56.dp)
//                .shadow(
//                    elevation = 6.dp,
//                    shape = androidx.compose.foundation.shape.CircleShape,
//                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
//                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
//                )
//        ) {
//            Icon(
//                imageVector = if (showOptions) Icons.Default.Close else Icons.Default.Edit,
//                contentDescription = if (showOptions) "Close menu" else "Create content",
//                modifier = Modifier
//                    .size(24.dp)
//                    .rotate(rotation)
//            )
//        }
//    }
//}
//
//@Composable
//fun FabOption(
//    icon: ImageVector,
//    text: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val interactionSource = remember { MutableInteractionSource() }
//    Surface(
//        shape = RoundedCornerShape(25.dp),
//        color = MaterialTheme.colorScheme.primary,
//        tonalElevation = 4.dp, // Material 3 elevation
//        modifier = modifier
//            .clickable(
//                interactionSource = interactionSource,
//                onClick = onClick
//            )
//            .padding(0.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Icon(
//                imageVector = icon,
//                contentDescription = text,
//                tint = Color.White,
//                modifier = Modifier.size(20.dp)
//            )
//            Text(
//                text = text,
//                color = Color.White,
//                style = MaterialTheme.typography.labelLarge, // Material 3 label style
//                fontWeight = FontWeight.Medium,
//                fontSize = 14.sp
//            )
//        }
//    }
//}

@Composable
fun EmptyPostsState(
    onNavigateToComposePost: () -> Unit,
    message: String = "Be the first to share your thoughts. Compose a post and start the conversation!"
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = "No posts",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(64.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Welcome to Texty!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }

            Button(
                onClick = onNavigateToComposePost,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Compose a post",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun insertSuggestionsInPosts(
    posts: List<Post>,
    suggestions: List<SuggestedUser>
): List<Any> {
    if (posts.isEmpty()) {
        return emptyList()
    }

    val result = mutableListOf<Any>()
    var postIndex = 0
    var suggestionIndex = 0

    while (postIndex < posts.size && suggestionIndex < suggestions.size) {
        val postsToAdd = (3..5).random().coerceAtMost(posts.size - postIndex)
        result.addAll(posts.subList(postIndex, postIndex + postsToAdd))
        postIndex += postsToAdd

        if (suggestionIndex < suggestions.size) {
            result.add(suggestions[suggestionIndex])
            suggestionIndex++
        }
    }

    if (postIndex < posts.size) {
        result.addAll(posts.subList(postIndex, posts.size))
    }

    return result
}

@Composable
fun PremiumBanner() {
    var showBanner by remember { mutableStateOf(true) }

    if (showBanner) {
        Surface(
            color = Color(0xFF1D9BF0).copy(alpha = 0.1f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Navigate to Twitter Blue */ }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Subscribe to unlock new features",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Learn more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = { showBanner = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostList(
    items: List<Any>,
    onLoadMore: () -> Unit,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    userCache: Map<String, User>,
    context: Context,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToMessages: (String) -> Unit,
    onMediaClick: (Post, Int) -> Unit,
    onDismissSuggestion: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(items, key = { item ->
            when (item) {
                is Post -> "post_${item.postId}"
                is SuggestedUser -> "suggestion_${item.id}"
                else -> "unknown_${System.identityHashCode(item)}"
            }
        }) { item ->
            when (item) {
                is Post -> {
                    TwitterPostItem(
                        post = item,
                        postViewModel = postViewModel,
                        authViewModel = authViewModel,
                        userCache = userCache,
                        context = context,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToMessages = onNavigateToMessages,
                        onMediaClick = onMediaClick
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }
                is SuggestedUser -> {
                    SuggestionItem(
                        suggestedUser = item,
                        onFollowClick = { /* Handle follow */ },
                        onDismissClick = { onDismissSuggestion(item.id) },
                        onProfileClick = { onNavigateToProfile(item.id) }
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }
            }
        }
        item {
            if (items.isNotEmpty()) {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    suggestedUser: SuggestedUser,
    onFollowClick: () -> Unit,
    onDismissClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    var isFollowing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Suggested for you",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onDismissClick,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss suggestion",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = suggestedUser.profileImage,
                contentDescription = "Profile picture of ${suggestedUser.name}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() },
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.user)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = suggestedUser.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    if (suggestedUser.isVerified) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Verified",
                            tint = Color(0xFF1D9BF0),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = "@${suggestedUser.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (suggestedUser.description.isNotEmpty()) {
                    Text(
                        text = suggestedUser.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${suggestedUser.followersCount} followers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    isFollowing = !isFollowing
                    onFollowClick()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.onSurface,
                    contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(32.dp),
                border = if (isFollowing) ButtonDefaults.outlinedButtonBorder() else null
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TwitterPostItem(
    post: Post,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    userCache: Map<String, User>,
    modifier: Modifier = Modifier,
    context: Context,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToMessages: (String) -> Unit,
    onMediaClick: (Post, Int) -> Unit
) {
    var showMoreOptions by remember { mutableStateOf(false) }
    var showRepostDialog by remember { mutableStateOf(false) }
    var isRetweetedByCurrentUser by remember { mutableStateOf(false) }
    val user = userCache[post.userId]
    val isLoadingUser = user == null
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUser = userCache[currentUserId]

    // Increment view count when the post is displayed
    LaunchedEffect(post.postId) {
        postViewModel.incrementViewCount(post.postId)
    }

    // Check if the current user retweeted this post
    LaunchedEffect(post.postId, currentUserId) {
        if (currentUserId.isNotEmpty() && post.reposts.isNotEmpty()) {
            isRetweetedByCurrentUser = postViewModel.isPostRetweetedByUser(post.postId, currentUserId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Repost header
        if (isRetweetedByCurrentUser && post.postType != PostType.QUOTE) {
            val reposterUsername = currentUser?.username ?: "Someone"
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Repeat,
                    contentDescription = "Retweet",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Retweeted by @$reposterUsername",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Row {
            AsyncImage(
                model = user?.profilePicture?.let { ImageUtils.base64ToBitmap(it) },
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable { onNavigateToProfile(post.userId) },
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.user),
                placeholder = painterResource(id = R.drawable.user)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // User info and timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoadingUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(16.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(12.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    } else {
                        Text(
                            text = user.name ?: "Unknown User",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { onNavigateToProfile(post.userId) }
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "@${user?.username ?: post.userId.take(8)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = formatTwitterTimestamp(post.timestamp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.clickable { /* Show detailed timestamp */ }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { showMoreOptions = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                TwitterPostContent(
                    post = post,
                    onMediaClick = { index -> onMediaClick(post, index) }
                )

                // Quote tweet
                if (post.postType == PostType.QUOTE && post.parentPostId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    QuotedPost(
                        parentPostId = post.parentPostId,
                        postViewModel = postViewModel,
                        authViewModel = authViewModel,
                        userCache = userCache,
                        onNavigateToProfile = onNavigateToProfile,
                        onMediaClick = onMediaClick
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TwitterEngagementBar(
                    post = post,
                    postViewModel = postViewModel,
                    context = context,
                    onNavigateToMessages = onNavigateToMessages,
                    onRepostClick = { showRepostDialog = true }
                )
            }
        }
    }

    // Modal Bottom Sheet for More Options (Twitter-style)
    if (showMoreOptions) {
        ModalBottomSheet(
            onDismissRequest = { showMoreOptions = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Destructive actions section
                MoreOptionsItem(
                    icon = Icons.Outlined.VisibilityOff,
                    text = "Not interested in this post",
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        // Handle not interested
                        showMoreOptions = false
                    }
                )

                MoreOptionsItem(
                    icon = Icons.Outlined.PersonOff,
                    text = "Mute @${user?.username ?: "user"}",
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        // Handle mute user
                        showMoreOptions = false
                    }
                )

                MoreOptionsItem(
                    icon = Icons.Outlined.Link,
                    text = "Copy link",
                    textColor = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        // Handle copy link
                        val link = "texty://post/${post.postId}"
                        // You can use ClipboardManager here
                        showMoreOptions = false
                    }
                )

                // Divider before destructive option
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                // Destructive option
                MoreOptionsItem(
                    icon = Icons.Outlined.Flag,
                    text = "Report post",
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = {
                        // Handle report
                        showMoreOptions = false
                    }
                )

                // Cancel button
                TextButton(
                    onClick = { showMoreOptions = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showRepostDialog) {
        RepostDialog(
            onDismiss = { showRepostDialog = false },
            onRetweet = { postViewModel.repost(post.postId, RepostType.RETWEET) },
            onQuote = { comment ->
                postViewModel.repost(post.postId, RepostType.QUOTE, comment)
            }
        )
    }
}

@Composable
fun MoreOptionsItem(
    icon: ImageVector,
    text: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun QuotedPost(
    parentPostId: String,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    userCache: Map<String, User>,
    onNavigateToProfile: (String) -> Unit,
    onMediaClick: (Post, Int) -> Unit
) {
    val parentPost by postViewModel.currentPost.collectAsState()

    LaunchedEffect(parentPostId) {
        postViewModel.loadPost(parentPostId)
    }

    parentPost?.let { post ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = userCache[post.userId]?.profilePicture?.let { ImageUtils.base64ToBitmap(it) },
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateToProfile(post.userId) },
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.user)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = userCache[post.userId]?.name ?: "Unknown User",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "@${userCache[post.userId]?.username ?: post.userId.take(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TwitterPostContent(
                    post = post,
                    onMediaClick = { index -> onMediaClick(post, index) }
                )
            }
        }
    }
}

@Composable
fun TwitterPostContent(
    post: Post,
    onMediaClick: (Int) -> Unit = {}
) {
    Column {
        if (post.content.isNotBlank()) {
            var isExpanded by remember { mutableStateOf(false) }
            val maxLines = if (isExpanded) Int.MAX_VALUE else 10
            val showShowMore = !isExpanded && post.content.length > 280

            SelectionContainer {
                Text(
                    text = if (showShowMore) post.content.take(280) + "..." else post.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (showShowMore) {
                Text(
                    text = "Show more",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { isExpanded = true }
                        .padding(top = 2.dp)
                )
            }
        }

        if (post.mediaBase64.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            when (post.mediaBase64.size) {
                1 -> SingleMediaItem(
                    post.mediaBase64.first(),
                    onClick = { onMediaClick(0) }
                )
                2 -> TwoMediaGrid(post.mediaBase64, onMediaClick)
                3 -> ThreeMediaGrid(post.mediaBase64, onMediaClick)
                4 -> FourMediaGrid(post.mediaBase64, onMediaClick)
                else -> MultipleMediaGrid(post.mediaBase64.take(4), onMediaClick)
            }
        }
    }
}

@Composable
fun SingleMediaItem(base64: String, onClick: () -> Unit) {
    val bitmap = ImageUtils.base64ToBitmap(base64)
    AsyncImage(
        model = bitmap,
        contentDescription = "Post image",
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.user)
    )
}

@Composable
fun TwoMediaGrid(mediaList: List<String>, onMediaClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        mediaList.forEachIndexed { index, base64 ->
            val bitmap = ImageUtils.base64ToBitmap(base64)
            AsyncImage(
                model = bitmap,
                contentDescription = "Post image",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMediaClick(index) },
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.user)
            )
        }
    }
}

@Composable
fun ThreeMediaGrid(mediaList: List<String>, onMediaClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            AsyncImage(
                model = ImageUtils.base64ToBitmap(mediaList[0]),
                contentDescription = "Post image",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp))
                    .clickable { onMediaClick(0) },
                contentScale = ContentScale.Crop
            )
            AsyncImage(
                model = ImageUtils.base64ToBitmap(mediaList[1]),
                contentDescription = "Post image",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 12.dp))
                    .clickable { onMediaClick(1) },
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        AsyncImage(
            model = ImageUtils.base64ToBitmap(mediaList[2]),
            contentDescription = "Post image",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .clickable { onMediaClick(2) },
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun FourMediaGrid(mediaList: List<String>, onMediaClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            mediaList.take(2).forEachIndexed { index, base64 ->
                AsyncImage(
                    model = ImageUtils.base64ToBitmap(base64),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(
                                topStart = if (index == 0) 12.dp else 0.dp,
                                topEnd = if (index == 1) 12.dp else 0.dp
                            )
                        )
                        .clickable { onMediaClick(index) },
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            mediaList.drop(2).forEachIndexed { index, base64 ->
                AsyncImage(
                    model = ImageUtils.base64ToBitmap(base64),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(
                                bottomStart = if (index == 0) 12.dp else 0.dp,
                                bottomEnd = if (index == 1) 12.dp else 0.dp
                            )
                        )
                        .clickable { onMediaClick(index + 2) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun MultipleMediaGrid(mediaList: List<String>, onMediaClick: (Int) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        FourMediaGrid(mediaList, onMediaClick)
        if (mediaList.size > 4) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMediaClick(3) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${mediaList.size - 4}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



@Composable
private fun TwitterEngagementBar(
    post: Post,
    postViewModel: PostViewModel,
    context: Context,
    onNavigateToMessages: (String) -> Unit,
    onRepostClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    val isLiked = post.likes.contains(currentUserId)
    val likeCount = post.likes.size

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        EngagementButton(
            count = post.comments.size,
            icon = Icons.Outlined.ChatBubbleOutline,
            selectedIcon = Icons.Filled.ChatBubble,
            isSelected = false,
            contentDescription = "Reply",
            selectedColor = MaterialTheme.colorScheme.primary,
            onClick = { onNavigateToMessages(post.postId) }
        )

        EngagementButton(
            count = post.reposts.size,
            icon = Icons.Outlined.Repeat,
            selectedIcon = Icons.Filled.Repeat,
            isSelected = false,
            contentDescription = "Repost",
            selectedColor = Color(0xFF00BA7C),
            onClick = onRepostClick
        )

        EngagementButton(
            count = likeCount, // Use the computed like count
            icon = Icons.Outlined.FavoriteBorder,
            selectedIcon = Icons.Filled.Favorite,
            isSelected = isLiked, // This will update immediately
            contentDescription = "Like",
            selectedColor = Color(0xFFF91880),
            onClick = {
                postViewModel.toggleLike(post.postId)
            }
        )

        EngagementButton(
            count = post.viewCount,
            icon = Icons.Outlined.Visibility,
            selectedIcon = Icons.Filled.Visibility,
            isSelected = false,
            contentDescription = "Views",
            selectedColor = MaterialTheme.colorScheme.primary,
            onClick = { /* View analytics */ }
        )

        EngagementButton(
            count = 0,
            icon = Icons.Outlined.Share,
            selectedIcon = Icons.Filled.Share,
            isSelected = false,
            contentDescription = "Share",
            selectedColor = MaterialTheme.colorScheme.primary,
            onClick = { shareViaTwitter(context, post.userId) }
        )
    }
}



@Composable
private fun EngagementButton(
    count: Int,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    contentDescription: String,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val buttonColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.secondary
    val displayCount = if (count > 0) formatCount(count) else ""

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp),
            tint = buttonColor
        )
        if (displayCount.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayCount,
                style = MaterialTheme.typography.bodySmall,
                color = buttonColor,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000000 -> "${count / 1000000000}B" // Billions
        count >= 1000000 -> "${count / 1000000}M"     // Millions
        count >= 1000 -> "${count / 1000}K"           // Thousands
        else -> count.toString()
    }
}

@Composable
fun RepostDialog(
    onDismiss: () -> Unit,
    onRetweet: () -> Unit,
    onQuote: (String) -> Unit
) {
    var quoteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Repost Options") },
        text = {
            Column {
                Text(
                    text = "Choose how you'd like to repost:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                ListItem(
                    headlineContent = { Text("Retweet") },
                    leadingContent = {
                        Icon(Icons.Outlined.Repeat, contentDescription = "Retweet")
                    },
                    modifier = Modifier.clickable {
                        onRetweet()
                        onDismiss()
                    }
                )
                ListItem(
                    headlineContent = { Text("Quote Tweet") },
                    leadingContent = {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Quote")
                    },
                    modifier = Modifier.clickable {
                        // Optionally navigate to a compose screen instead
                        if (quoteText.isNotBlank()) {
                            onQuote(quoteText)
                            onDismiss()
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = quoteText,
                    onValueChange = { quoteText = it },
                    placeholder = { Text("Add a comment for your quote tweet") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (quoteText.isNotBlank()) {
                    onQuote(quoteText)
                }
                onDismiss()
            }) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun shareViaTwitter(context: Context, userName: String) {
    val shareText = "Check out this post from @$userName on Texty!"
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share post"))
}








