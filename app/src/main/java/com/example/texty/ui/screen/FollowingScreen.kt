@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.example.texty.ui.screen

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.texty.data.Post
import com.example.texty.data.User
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FollowingScreen(
    innerPadding: PaddingValues,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    selectedTabIndex: Int,
    onNavigateToComposePost: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToMessages: (String) -> Unit = { }
) {
    val followingPosts by postViewModel.followingPosts.collectAsState()
    val uiState by postViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Use the user cache from AuthViewModel
    val userCache by authViewModel.userFlowCache.collectAsState()

    // State for full-screen media viewer
    var showFullScreenMedia by remember { mutableStateOf(false) }
    var selectedPostForMedia by remember { mutableStateOf<Post?>(null) }
    var selectedMediaIndex by remember { mutableStateOf(0) }

    // Load following posts when this screen is active
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) { // Following tab
            postViewModel.loadFollowingFeed(reset = true, authViewModel = authViewModel)
        }
    }

    Log.d("FollowingScreen", "Posts: $followingPosts")

    // Fetch users for all posts when posts change
    LaunchedEffect(followingPosts) {
        val userIds = followingPosts.map { it.userId }.distinct()
        authViewModel.fetchUsersForPosts(userIds)
    }

    // Simulate loading for better UX
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(800)
        isLoading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        // Main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if ((uiState.isLoading || isLoading) && followingPosts.isEmpty()) {
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
                            text = "Loading posts from people you follow...",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else if (followingPosts.isEmpty()) {
                EmptyFollowingState(
                    onNavigateToSearch = { /* You might want to add navigation to search */ },
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

                // Following PostList
                FollowingPostList(
                    posts = followingPosts,
                    onLoadMore = { postViewModel.loadFollowingFeed(authViewModel = authViewModel) },
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
                    }
                )
            }
        }

        // FloatingActionButton
        FloatingActionButtonWithOptions(
            onNavigateToComposePost = onNavigateToComposePost,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // Full-screen media viewer
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
                    .zIndex(1f)
            )
        }
    }
}

@Composable
fun EmptyFollowingState(
    onNavigateToSearch: () -> Unit,
    onNavigateToComposePost: () -> Unit
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
                imageVector = Icons.Outlined.PersonOff,
                contentDescription = "No followed users",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(64.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No posts to show",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "When you follow people, you'll see their posts here. Start by exploring and following some users!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToSearch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Find people to follow",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                TextButton(
                    onClick = onNavigateToComposePost
                ) {
                    Text(
                        text = "Or compose your own post",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FollowingPostList(
    posts: List<Post>,
    onLoadMore: () -> Unit,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    userCache: Map<String, User>,
    context: Context,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToMessages: (String) -> Unit = { },
    onMediaClick: (Post, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(posts, key = { post -> "post_${post.postId}" }) { post ->
            TwitterPostItem(
                post = post,
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

        item {
            if (posts.isNotEmpty()) {
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