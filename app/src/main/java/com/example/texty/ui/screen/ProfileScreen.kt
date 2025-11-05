package com.example.texty.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.texty.R
import com.example.texty.UserPreferencesManager
import com.example.texty.data.ImageUtils
import com.example.texty.data.User
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    onNavigateToSetting: () -> Unit,
    onNavigateBack: () -> Unit,
    userId: String,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    userPreferencesManager: UserPreferencesManager,
    onNavigateToPostDetail: (String) -> Unit = {},
    onNavigateToComposePost: () -> Unit = {},
    onNavigateToFollowers: (String) -> Unit = {},
    onNavigateToFollowing: (String) -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val user by authViewModel.userById.observeAsState()
    val error by authViewModel.error.observeAsState()
    val userPosts by postViewModel.userPosts.collectAsState(emptyList())
    val postsLoading by postViewModel.uiState.collectAsState()
    val currentUserId = authViewModel.firebaseUser.value?.uid
    val isCurrentUser = user?.userId == currentUserId

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Posts", "Replies", "Media", "Likes")

    // Fetch user and their posts when screen is launched or userId changes
    LaunchedEffect(userId) {
        authViewModel.fetchUserById(userId)
        postViewModel.loadUserPosts(userId)
    }

    // Calculate scroll progress for animations (0f to 1f)
    val scrollProgress by remember {
        derivedStateOf {
            val maxScroll = 150f
            val currentScroll = scrollState.firstVisibleItemScrollOffset.toFloat()
            (currentScroll / maxScroll).coerceIn(0f, 1f)
        }
    }

    // Animate header height
    val headerHeight by animateDpAsState(
        targetValue = (200.dp * (1f - scrollProgress)).coerceAtLeast(0.dp),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "headerHeight"
    )

    // Animate profile image size
    val profileImageSize by animateDpAsState(
        targetValue = (120.dp - (70.dp * scrollProgress)).coerceAtLeast(50.dp),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "profileImageSize"
    )

    // Adjust profile image offset to show bottom half inside cover image
    val profileImageOffset by animateDpAsState(
        targetValue = (60.dp - (60.dp * scrollProgress)).coerceAtLeast(0.dp),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "profileImageOffset"
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                // Collapsible Header Section
                ProfileHeader(
                    authViewModel = authViewModel,
                    user = user,
                    headerHeight = headerHeight,
                    profileImageSize = profileImageSize,
                    profileImageOffset = profileImageOffset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface),
                    onNavigateToComposePost = onNavigateToComposePost,
                    onNavigateToFollowers = onNavigateToFollowers,
                    onNavigateToFollowing = onNavigateToFollowing
                )
            }

            item {
                // Tab Row
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = false).height(2.dp)
                        )
                    },
                    divider = {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
            when (selectedTabIndex) {
                0 -> { // Posts tab
                    if (postsLoading.isLoading && userPosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    } else if (userPosts.isEmpty()) {
                        item {
                            EmptyPostsState(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                isCurrentUser = isCurrentUser,
                                onComposePost = onNavigateToComposePost
                            )
                        }
                    } else {
                        items(userPosts.size) { index ->
                            val post = userPosts[index]
                            TwitterPostItem(
                                post = post,
                                postViewModel = postViewModel,
                                authViewModel = authViewModel,
                                userCache = mapOf(post.userId to (user ?: User())),
                                context = androidx.compose.ui.platform.LocalContext.current,
                                onNavigateToProfile = { /* Stay on profile */ },
                                onNavigateToMessages = onNavigateToPostDetail,
                                onMediaClick = { _, _ -> },
                                modifier = Modifier.clickable {
                                    onNavigateToPostDetail(post.postId)
                                }
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
                1 ->{
                    item {
                        EmptyPostsState(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            isCurrentUser = isCurrentUser
                        )
                    }
                }

                2 -> { // Media tab
                    val mediaPosts = userPosts.filter { it.mediaBase64.isNotEmpty() }
                    if (mediaPosts.isEmpty()) {
                        item {
                            EmptyMediaState(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                isCurrentUser = isCurrentUser
                            )
                        }
                    } else {
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(1.dp),
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                items(mediaPosts.flatMap { it.mediaBase64 }.take(9)) { media ->
                                    MediaGridItem(
                                        imageUrl = media,
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clickable { /* Open media viewer */ }
                                    )
                                }
                            }
                        }
                    }
                }

                // Add similar implementations for Replies (1) and Likes (3) tabs
                else -> {
                    item {
                        ComingSoonTab(
                            tabName = tabs[selectedTabIndex],
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }
        }

        // Custom Collapsible Top Bar
        CustomTopBar(
            scrollProgress = scrollProgress,
            user = user,
            profileImageSize = profileImageSize,
            modifier = Modifier.align(Alignment.TopCenter),
            onNavigateBack = onNavigateBack,
            onNavigateToSetting = onNavigateToSetting
        )

        // Floating Action Button for current user
        if (isCurrentUser) {
            FloatingActionButtonWithOptions(
                onNavigateToComposePost = onNavigateToComposePost,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun MediaGridItem(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    // Memoize the bitmap to prevent reloading
    val bitmap = remember(imageUrl) { ImageUtils.base64ToBitmap(imageUrl) }
    AsyncImage(
        model = bitmap,
        contentDescription = "Post media",
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clip(RoundedCornerShape(1.dp)),
        contentScale = ContentScale.Crop,
        error = painterResource(id = R.drawable.user),
        placeholder = painterResource(id = R.drawable.user)
    )
}

@Composable
private fun ComingSoonTab(
    tabName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$tabName coming soon",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "This feature is under development",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyMediaState(
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.user),
            contentDescription = "No media",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isCurrentUser) "You haven't posted any media yet"
            else "No media posts yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isCurrentUser) "When you post photos or videos, they'll appear here"
            else "When this user posts media, you'll see it here",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun CustomTopBar(
    onNavigateBack: () -> Unit,
    onNavigateToSetting: () -> Unit,
    scrollProgress: Float,
    user: User?,
    profileImageSize: Dp,
    modifier: Modifier = Modifier
) {
    // Smooth background opacity transition
    val backgroundAlpha = scrollProgress.coerceIn(0f, 1f)
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = backgroundAlpha)
    // Elevation increases with scroll
    val elevation = (scrollProgress * 6).dp.coerceAtMost(6.dp)
    // Content fades in earlier for smoother effect
    val contentAlpha = (scrollProgress * 2f).coerceIn(0f, 1f)

    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = backgroundColor,
        shadowElevation = elevation,
        tonalElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (scrollProgress < 0.1f) Color.Black.copy(alpha = 0.3f)
                        else Color.Transparent
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Center: Profile picture, username, and post count
            if (user != null) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .alpha(contentAlpha),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Picture
                    val bitmap = remember(user.profilePicture) {
                        user.profilePicture?.let { ImageUtils.base64ToBitmap(it) }
                            ?: "https://via.placeholder.com/400"
                    }
                    AsyncImage(
                        model = bitmap,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = CircleShape
                            ),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.user),
                        placeholder = painterResource(id = R.drawable.user)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        // Username
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Post Count
                        Text(
                            text = "${formatCount(user.posts.size)} posts",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f)) // Placeholder when user is null
            }

            // Right: Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verified Badge (if applicable)
                if (user?.isVerified == true) {
                    Icon(
                        painter = painterResource(id = R.drawable.verified),
                        contentDescription = "Verified",
                        tint = Color(0xFF1D9BF0),
                        modifier = Modifier.size(20.dp)
                    )
                }
                // Share Button
                IconButton(
                    onClick = { /* Share profile */ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (scrollProgress < 0.1f) Color.Black.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // More Options Button
                IconButton(
                    onClick = onNavigateToSetting,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (scrollProgress < 0.1f) Color.Black.copy(alpha = 0.3f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    user: User?,
    headerHeight: Dp,
    profileImageSize: Dp,
    profileImageOffset: Dp,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onNavigateToComposePost: () -> Unit = {},
    onNavigateToFollowers: (String) -> Unit = {},
    onNavigateToFollowing: (String) -> Unit = {}
) {
    if (user == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(headerHeight + profileImageSize / 2),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
        return
    }

    val currentUserId = authViewModel.firebaseUser.value?.uid
    val isCurrentUser = user.userId == currentUserId
    val isFollowing = user.followers?.contains(currentUserId) == true

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight + profileImageSize / 2)
        ) {
            // Cover Photo
            AsyncImage(
                model = "https://picsum.photos/800/300",
                contentDescription = "Cover photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.user),
                placeholder = painterResource(id = R.drawable.user)
            )

            // Profile Image
            Box(
                modifier = Modifier
                    .size(profileImageSize)
                    .align(Alignment.BottomStart)
                    .offset(x = 16.dp, y = profileImageOffset)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = 4.dp,
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                )

                // Memoize the bitmap to prevent reloading
                val bitmap = remember(user.profilePicture) {
                    user.profilePicture?.let { ImageUtils.base64ToBitmap(it) }
                        ?: "https://via.placeholder.com/400"
                }
                AsyncImage(
                    model = bitmap,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .matchParentSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.user),
                    placeholder = painterResource(id = R.drawable.user)
                )
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-16).dp, y = profileImageSize / 2),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isCurrentUser) {
                    OutlinedButton(
                        onClick = { /* Edit profile */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Edit profile",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Message button
                    OutlinedButton(
                        onClick = { /* Send message */ },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(36.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.communications),
                            contentDescription = "Message",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Follow/Unfollow button
                    Button(
                        onClick = {
                            if (isFollowing) {
                                authViewModel.unfollowUser(targetUserId = user.userId)
                            } else {
                                authViewModel.followUser(targetUserId = user.userId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurface,
                            contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(36.dp),
                        border = if (isFollowing) ButtonDefaults.outlinedButtonBorder else null
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // More options button
                IconButton(
                    onClick = { /* More options */ },
                    modifier = Modifier
                        .size(36.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Profile content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(profileImageSize / 2))

            // Name and username
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "@${user.username.ifEmpty { user.userId.take(8) }}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Bio
            if (user.bio.isNotEmpty()) {
                Text(
                    text = user.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Location and join date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (user.location?.isNotEmpty() == true) {
                    ProfileInfoItem(
                        icon = Icons.Outlined.LocationOn,
                        text = user.location
                    )
                }
                ProfileInfoItem(
                    icon = Icons.Outlined.CalendarToday,
                    text = "Joined ${getJoinDateDisplay(user.joinDate ?: System.currentTimeMillis())}"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Follow stats
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FollowStatItem(
                    count = user.following?.size ?: 0,
                    label = "Following",
                    onClick = { user.userId?.let { onNavigateToFollowing(it) } }
                )
                FollowStatItem(
                    count = user.followers?.size ?: 0,
                    label = "Followers",
                    onClick = { user.userId?.let { onNavigateToFollowers(it) } }
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun FollowStatItem(
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onClick() }
    ) {
        Text(
            text = formatCount(count),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun EmptyPostsState(
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean,
    onComposePost: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "No posts",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isCurrentUser) "You haven't posted anything yet"
            else "No posts yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isCurrentUser) "When you post, it'll show up here"
            else "When this user posts, you'll see it here",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (isCurrentUser) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onComposePost,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Post something",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Helper functions
private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}

private fun getJoinDateDisplay(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    return formatter.format(date)
}















