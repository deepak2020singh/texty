package com.example.texty.ui.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.texty.R
import com.example.texty.common.formatTwitterTimestamp
import com.example.texty.data.Comment
import com.example.texty.data.ImageUtils
import com.example.texty.data.Post
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentScreen(
    postId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val post by viewModel.posts.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Load the post and its comments
    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    val currentPost by viewModel.currentPost.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Comments",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            CommentBottomBar(
                commentText = commentText,
                onCommentTextChange = { commentText = it },
                onSendClick = {
                    if (commentText.isNotBlank()) {
                        viewModel.addComment(postId, commentText)
                        commentText = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Original post
                currentPost?.let { post ->
                    item {
                        OriginalPostPreview(
                            authViewModel = authViewModel,
                            post = post,
                            onLikeClick = { viewModel.toggleLike(post.postId) },
                            onCommentClick = { /* Already on comment screen, no action needed */ },
                            onRepostClick = { viewModel.repost(post.postId) },
                            onShareClick = { /* shareViaTwitter(context = context, userName = "deepak") */ },
                            onDeleteClick = {
                                coroutineScope.launch {
                                    viewModel.deletePost(post.postId)
                                    onBackClick()
                                }
                            },
                            currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                    }
                }

                // Comments section
                val comments = currentPost?.comments ?: emptyList()
                if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No comments yet\nBe the first to comment!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(comments, key = { comment -> comment.commentId }) { comment ->
                        CommentItem(
                            authViewModel = authViewModel,
                            comment = comment,
                            onLikeClick = { viewModel.toggleCommentLike(postId, comment.commentId) },
                            onReplyClick = {
                                replyingToCommentId = comment.commentId
                                replyText = "@${comment.commenterId} "
                                keyboardController?.show()
                            },
                            onDeleteClick = {
                                viewModel.deleteComment(postId, comment.commentId)
                            },
                            currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        // Display nested replies
                        if (comment.replies.isNotEmpty()) {
                            this@LazyColumn.items(comment.replies, key = { reply -> reply.commentId }) { reply ->
                                CommentItem(
                                    authViewModel = authViewModel,
                                    comment = reply,
                                    onLikeClick = { viewModel.toggleCommentLike(postId, reply.commentId) },
                                    onReplyClick = {
                                        replyingToCommentId = reply.commentId
                                        replyText = "@${reply.commenterId} "
                                        keyboardController?.show()
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteComment(postId, reply.commentId)
                                    },
                                    currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .padding(start = 16.dp) // Indent replies
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = DividerDefaults.Thickness,
                            color = DividerDefaults.color
                        )
                    }
                }

                // Reply input field (shown when replying to a specific comment)
                if (replyingToCommentId != null) {
                    item {
                        ReplyInputField(
                            replyText = replyText,
                            onReplyTextChange = { replyText = it },
                            onSubmitReply = {
                                if (replyText.isNotBlank()) {
                                    viewModel.addCommentReply(postId, replyingToCommentId!!, replyText)
                                    replyText = ""
                                    replyingToCommentId = null
                                    keyboardController?.hide()
                                }
                            },
                            onCancel = {
                                replyText = ""
                                replyingToCommentId = null
                                keyboardController?.hide()
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OriginalPostPreview(
    authViewModel: AuthViewModel,
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRepostClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    currentUserId: String,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(post.postId) {
        authViewModel.fetchUser(userId = post.userId)
    }

    val userCache by authViewModel.userFlowCache.collectAsState()
    val user = userCache[post.userId]
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ){
                AsyncImage(
                    model = user?.profilePicture?.let { ImageUtils.base64ToBitmap(it) },
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.user),
                    placeholder = painterResource(id = R.drawable.user)
                )

            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user?.name?: "User Name", // You'd fetch actual user data
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${user!!.username} · ${formatTwitterTimestamp(post.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (post.isEdited) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "· Edited",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium
                )

                TwitterPostContent(
                    post = post,
                    onMediaClick = {  }
                )
            }

            // More options menu (only show if current user owns the post)
            if (post.userId == currentUserId) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Post") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Engagement stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${post.comments.size} Comments",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "${post.reposts.size} Reposts",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "${post.likes.size} Likes",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "${post.viewCount} Views",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Post actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Comment button
            IconButton(onClick = onCommentClick) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color.Gray
                )
            }

            // Repost button
            IconButton(onClick = onRepostClick) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    tint = Color.Gray
                )
            }

            // Like button
            IconButton(onClick = onLikeClick) {
                Icon(
                    imageVector = if (post.likes.contains(currentUserId)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.likes.contains(currentUserId)) Color.Red else Color.Gray
                )
            }

            // Share button
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    authViewModel: AuthViewModel,
    comment: Comment,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    currentUserId: String,
    modifier: Modifier = Modifier
) {

    LaunchedEffect(true) {
        authViewModel.fetchUser(userId = comment.commenterId)
    }

    val commenterId by authViewModel.userFlowCache.collectAsState()
    val commenter = commenterId[comment.commenterId]


    var showMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ){
                AsyncImage(
                    model = commenter?.profilePicture.let { ImageUtils.base64ToBitmap(it) },
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.user),
                    placeholder = painterResource(id = R.drawable.user
                )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${commenter?.name}", // You'd fetch actual user data
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "@${commenter?.username} · ${formatTwitterTimestamp(comment.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    if (comment.isEdited == true) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "· Edited",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Comment actions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Reply button
                    IconButton(
                        onClick = onReplyClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Reply",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onLikeClick,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = if (comment.likes.contains(currentUserId)) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (comment.likes.contains(currentUserId)) Color.Red else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = comment.likes.size.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (comment.likes.contains(currentUserId)) Color.Red else Color.Gray
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = { /* Handle share comment */ },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Show replies count
                if (comment.replies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${comment.replies.size} ${if (comment.replies.size == 1) "reply" else "replies"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // More options menu for comments (only show if current user owns the comment)
            if (comment.commenterId == currentUserId) {
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Comment") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyInputField(
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    onSubmitReply: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        OutlinedTextField(
            value = replyText,
            onValueChange = onReplyTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = "Post your reply",
                    color = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onSubmitReply,
                    enabled = replyText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (replyText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            ),
            maxLines = 5
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun CommentBottomBar(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Comment input field
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = "Post your comment",
                    color = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onSendClick,
                    enabled = commentText.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (commentText.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.background,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text
            ),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
