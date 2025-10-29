package com.example.texty.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.texty.R
import com.example.texty.data.ImageUtils
import com.example.texty.data.User
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun SearchScreen(
    innerPadding: PaddingValues,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    onClearSearch: () -> Unit,
    onNavigateToProfile: (String) -> Unit = {},
    onNavigateToComposePost: () -> Unit = {},
    isSearchFocused: Boolean,
    onSearchFocusChange: (Boolean) -> Unit
) {
    val users by authViewModel.users.observeAsState()
    val error by authViewModel.error.observeAsState()
    val firebaseUser by authViewModel.firebaseUser.observeAsState()
    val scope = rememberCoroutineScope()


    // Fetch users when the composable is first displayed
    LaunchedEffect(Unit) {
        authViewModel.fetchAllUsers()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("For You", "Trending", "News", "Sports", "Entertainment")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 10.dp)
    ) {
        Column {
            // Show user list only when search is focused and there are users
            if (isSearchFocused && !users.isNullOrEmpty()) {
                UserSearchList(
                    users = users!!,
                    firebaseUser = firebaseUser,
                    searchQuery = searchQuery,
                    scope = scope,
                    onNavigateToProfile = onNavigateToProfile,
                    authViewModel = authViewModel,
                    postViewModel = postViewModel
                )
            }
            // Tabs (only show when search is not focused)
            if (!isSearchFocused) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                // Content based on selected tab (only show when search is not focused)
                when (selectedTabIndex) {
                    0 -> ForYouContent()
                    1 -> TrendingContent()
                    2 -> NewsContent()
                    3 -> SportsContent()
                    4 -> EntertainmentContent()
                }
            }
        }
        // Floating Action Button (only show when search is not focused)
        if (!isSearchFocused) {
            FloatingActionButtonWithOptions(
                onNavigateToComposePost = onNavigateToComposePost,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}


// User Search List Composable
@Composable
fun UserSearchList(
    users: List<User>,
    modifier: Modifier = Modifier,
    firebaseUser: FirebaseUser?,
    searchQuery: TextFieldValue,
    scope: CoroutineScope,
    onNavigateToProfile: (String) -> Unit,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel
) {
    // Handle loading, empty, and data states
    if (firebaseUser == null) {
        // Show message if user is not logged in
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Please log in to search users")
        }
    }

    if (users.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No users found")
        }
    }

    val filteredUsers = users?.filter { user ->
        user.userId != firebaseUser!!.uid && // Exclude current user
                (searchQuery.text.isEmpty() ||
                        user.name.contains(searchQuery.text, ignoreCase = true) ||
                        user.username.contains(searchQuery.text, ignoreCase = true))
    } ?: emptyList()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(filteredUsers, key = { it.userId }) { user ->
            UserItem(
                user = user,
                isFollowing = user.followers?.contains(firebaseUser!!.uid) == true,
                onFollowToggle = {
                    scope.launch {
                        if (user.followers?.contains(firebaseUser!!.uid) == true) {
                            authViewModel.unfollowUser(user.userId)
                        } else {
                            authViewModel.followUser(user.userId)
                        }
                        postViewModel.loadFeed(reset = true)
                    }
                },
                onProfileClick = { onNavigateToProfile(user.userId) }
            )
        }
    }
}


@Composable
fun UserItem(
    user: User,
    isFollowing: Boolean,
    onFollowToggle: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation for button color and scale
    val buttonColor by animateColorAsState(
        targetValue = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
        label = "Button color animation"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isFollowing) 0.95f else 1f,
        label = "Button scale animation"
    )

    // Interaction source for ripple effect
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null // Custom ripple handled below
            ) { onProfileClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Picture
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!user.profilePicture.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.profilePicture.let { ImageUtils.base64ToBitmap(it) },
                        contentDescription = "${user.name}'s profile picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.user),
                        error = painterResource(id = R.drawable.user)
                    )
                } else {
                    // Placeholder with user's initial
                    Text(
                        text = user.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!user.bio.isNullOrEmpty()) {
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Follow/Unfollow Button
            FilledTonalButton(
                onClick = onFollowToggle,
                modifier = Modifier
                    .scale(buttonScale)
                    .height(36.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// The rest of your composables remain the same...
@Composable
fun FloatingActionButton(
    onNavigateToComposePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable { onNavigateToComposePost() }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Compose Post",
            modifier = Modifier
                .align(Alignment.Center)
                .size(24.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun EntertainmentContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Entertainment Content")
    }
}

@Composable
fun ForYouContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Promoted Card 1
        PromotedCard(
            title = "Get it",
            subtitle = "Tews action with Mind Space in Google Gemini",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Promoted Card 2
        PromotedCard(
            title = "OxygenOS16",
            subtitle = "Intelligently Yours. That's the new OxygenOS 16",
            promoText = "Promoted by OnePlus India",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Today's News Section
        Text(
            text = "Today's News",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // News Items
        NewsItem(
            title = "Telegram Runs $30 Billion",
            description = "Valuation with Just 30 Remote Employees",
            tags = listOf("Trending now", "News"),
            engagement = "485",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        NewsItem(
            title = "Delhi High Court Stays FSSAI Ban on High-Sugar ORSL Drinks",
            description = "",
            timeAgo = "2 hours ago",
            tags = listOf("News"),
            engagement = "3.7K",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        NewsItem(
            title = "Bhad Bhabie Draws Mixed Fan Reactions to Bold Appearance Change",
            description = "",
            timeAgo = "6 hours ago",
            tags = listOf("Entertainment"),
            engagement = "",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun PromotedCard(
    title: String,
    subtitle: String,
    promoText: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            promoText?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun NewsItem(
    title: String,
    description: String,
    timeAgo: String? = null,
    tags: List<String>,
    engagement: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tags
            tags.forEach { tag ->
                Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Engagement or time
            if (engagement.isNotEmpty()) {
                Text(
                    text = engagement,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (timeAgo != null) {
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Placeholder composables for other tabs
@Composable
fun TrendingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Trending Content")
    }
}

@Composable
fun NewsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("News Content")
    }
}

@Composable
fun SportsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Sports Content")
    }
}

