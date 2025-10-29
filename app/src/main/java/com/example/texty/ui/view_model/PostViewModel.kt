package com.example.texty.ui.view_model


import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.texty.data.Comment
import com.example.texty.data.EditRecord
import com.example.texty.data.ImageUtils
import com.example.texty.data.Post
import com.example.texty.data.PostType
import com.example.texty.data.Repost
import com.example.texty.data.RepostType
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val postsCollection = firestore.collection("posts")

    // State for managing UI state
    private val _uiState = MutableStateFlow(PostUiState())
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    // State for holding a list of posts (for feed)
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // State for posts from followed users
    private val _followingPosts = MutableStateFlow<List<Post>>(emptyList())
    val followingPosts: StateFlow<List<Post>> = _followingPosts.asStateFlow()

    // State for user-specific posts
    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    // State for a single post
    private val _currentPost = MutableStateFlow<Post?>(null)
    val currentPost: StateFlow<Post?> = _currentPost.asStateFlow()

    init {
        loadFeed()
    }

    /**
     * Loads the feed of posts, ordered by timestamp descending.
     */
    fun loadFeed(limit: Int = 20, reset: Boolean = false) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val snapshot = postsCollection
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()

                val postList = snapshot.documents.mapNotNull { doc ->
                    val post = doc.toObject(Post::class.java)
                    post?.copy(postId = doc.id)
                }
                _posts.value =
                    if (reset) postList else (_posts.value + postList).distinctBy { it.postId }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Loads posts from users the current user follows.
     */
    fun loadFollowingFeed(
        limit: Int = 20,
        reset: Boolean = false,
        lastPost: Post? = null,
        authViewModel: AuthViewModel
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (currentUserId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "You must be logged in to view posts from followed users."
                    )
                    _followingPosts.value = emptyList()
                    return@launch
                }

                // Fetch the list of followed user IDs from Realtime Database
                val currentUserRef = realtimeDb.reference.child("users").child(currentUserId)
                val currentUserSnapshot = currentUserRef.get().await()

                if (!currentUserSnapshot.exists()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    _followingPosts.value = emptyList()
                    return@launch
                }

                val followingList = currentUserSnapshot.child("following")
                    .getValue(object : GenericTypeIndicator<List<String>>() {})
                val followedUserIds = followingList ?: emptyList()

                if (followedUserIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    _followingPosts.value = emptyList()
                    return@launch
                }

                // Fetch user details for posts
                authViewModel.fetchUsersForPosts(followedUserIds)

                // Fetch posts from followed users from Firestore
                val postList = mutableListOf<Post>()
                val chunks = followedUserIds.chunked(10)
                for (chunk in chunks) {
                    val query = postsCollection
                        .whereIn("userId", chunk)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(limit.toLong())
                    val snapshot = if (lastPost != null) {
                        query.startAfter(lastPost.timestamp).get().await()
                    } else {
                        query.get().await()
                    }
                    postList.addAll(snapshot.documents.mapNotNull { doc ->
                        val post = doc.toObject(Post::class.java)
                        post?.copy(postId = doc.id)
                    })
                }

                val existingPostIds = _followingPosts.value.map { it.postId }.toSet()
                val newPosts = postList.filter { it.postId !in existingPostIds }
                _followingPosts.value = if (reset) postList else (_followingPosts.value + newPosts)
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (e: FirebaseException) {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, error = "Firebase error: ${e.message}")
                _followingPosts.value = emptyList()
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(isLoading = false, error = "Unexpected error: ${e.message}")
                _followingPosts.value = emptyList()
            }
        }
    }

    /**
     * Loads posts by a specific user ID (Twitter-like profile posts)
     */
    fun loadUserPosts(userId: String, limit: Int = 50) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val snapshot = postsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(limit.toLong())
                    .get()
                    .await()

                val postList = snapshot.documents.mapNotNull { doc ->
                    val post = doc.toObject(Post::class.java)
                    post?.copy(postId = doc.id)
                }
                _userPosts.value = postList
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    /**
     * Loads a single post by ID.
     */
    fun loadPost(postId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val doc = postsCollection.document(postId).get().await()
                val post = doc.toObject(Post::class.java)?.copy(postId = doc.id)
                _currentPost.value = post
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }



    /**
     * Creates a new post with optional message and/or media.
     */
    suspend fun createPost(
        postId: String,
        content: String = "",
        mediaUrls: List<Uri> = emptyList(),
        postType: PostType = PostType.ORIGINAL,
        parentPostId: String? = null,
        context: Context
    ) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            if (content.isBlank() && mediaUrls.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Post must contain a message or at least one image"
                )
                return
            }

            val mediaBase64 = mediaUrls.mapNotNull { uri ->
                ImageUtils.uriToBase64(context, uri, maxSize = 800, quality = 80)
            }

            val newPost = Post(
                postId = postId,
                userId = auth.currentUser?.uid ?: "",
                content = content.trim(),
                mediaBase64 = mediaBase64,
                postType = postType,
                parentPostId = parentPostId,
                timestamp = System.currentTimeMillis(),
                likes = emptyList(),
                comments = emptyList(),
                reposts = emptyList(), // Fixed: was reposts = emptyList() instead of reposts = Repost()
                isEdited = false,
                editHistory = emptyList()
            )

            postsCollection.document(postId).set(newPost).await()
            _currentPost.value = newPost
            loadFeed()
            loadFollowingFeed(authViewModel = authViewModel) // Refresh following feed if new post is created
            _uiState.value = _uiState.value.copy(isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    suspend fun editPost(
        postId: String,
        newContent: String,
        newMediaUrls: List<String> = emptyList()
    ) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val postRef = postsCollection.document(postId)
            val currentPost = postRef.get().await().toObject(Post::class.java)

            if (currentPost != null) {
                val editRecord = EditRecord(
                    editTimestamp = System.currentTimeMillis(),
                    previousContent = currentPost.content,
                    previousMediaBase64 = currentPost.mediaBase64
                )
                val updatedPost = currentPost.copy(
                    content = newContent,
                    mediaBase64 = newMediaUrls,
                    isEdited = true,
                    editHistory = currentPost.editHistory + editRecord
                )
                postRef.set(updatedPost).await()
                _currentPost.value = updatedPost.copy(postId = postId)
                loadFeed()
                loadFollowingFeed(authViewModel = authViewModel) // Refresh following feed if post is edited
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }


    fun toggleLike(postId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val postRef = postsCollection.document(postId)

                // First, get the current post to check like status
                val postSnapshot = postRef.get().await()
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null) {
                    val updatedLikes = if (post.likes.contains(currentUserId)) {
                        // User already liked - remove like
                        post.likes - currentUserId
                    } else {
                        // User hasn't liked - add like
                        post.likes + currentUserId
                    }

                    // Update in Firestore
                    postRef.update("likes", updatedLikes).await()

                    // Immediately update ALL local states for instant UI response
                    updateAllLocalPostStates(postId, currentUserId, updatedLikes)

                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Like failed: ${e.message}")
            }
        }
    }

    // Helper function to update all local post states immediately
    private fun updateAllLocalPostStates(postId: String, userId: String, updatedLikes: List<String>) {
        val updatePost: (Post) -> Post = { post ->
            if (post.postId == postId) {
                post.copy(likes = updatedLikes)
            } else {
                post
            }
        }

        // Update _posts
        _posts.value = _posts.value.map(updatePost)

        // Update _followingPosts
        _followingPosts.value = _followingPosts.value.map(updatePost)

        // Update _userPosts
        _userPosts.value = _userPosts.value.map(updatePost)

        // Update _currentPost
        _currentPost.value = _currentPost.value?.let { currentPost ->
            if (currentPost.postId == postId) {
                currentPost.copy(likes = updatedLikes)
            } else {
                currentPost
            }
        }
    }



    suspend fun deletePost(postId: String) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            postsCollection.document(postId).delete().await()
            loadFeed()
            loadFollowingFeed(authViewModel = authViewModel) // Refresh following feed if post is deleted
            _currentPost.value = null
            _uiState.value = _uiState.value.copy(isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }

    fun repost(
        postId: String,
        repostType: RepostType = RepostType.RETWEET,
        additionalComment: String? = null
    ) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val originalPostRef = postsCollection.document(postId)
                val originalSnapshot = originalPostRef.get().await()
                val originalPost = originalSnapshot.toObject(Post::class.java)

                if (originalPost != null) {
                    if (repostType == RepostType.RETWEET) {
                        // Handle simple retweet
                        val repostRef = realtimeDb.reference.child("reposts").child(postId).push()
                        val repostId = repostRef.key ?: return@launch
                        val newRepost = Repost(
                            repostId = repostId,
                            originalPostId = postId,
                            reposterId = currentUserId,
                            timestamp = System.currentTimeMillis(),
                            repostType = RepostType.RETWEET
                        )
                        repostRef.setValue(newRepost).await()

                        // Update the original post's reposts list
                        val updatedReposts = originalPost.reposts + repostId
                        originalPostRef.update("reposts", updatedReposts).await()
                    } else {
                        // Handle quote tweet
                        val newDocRef = postsCollection.document()
                        val quotePostId = newDocRef.id
                        val newQuotePost = Post(
                            postId = quotePostId,
                            userId = currentUserId,
                            content = additionalComment ?: "",
                            mediaBase64 = emptyList(),
                            postType = PostType.QUOTE,
                            parentPostId = postId,
                            timestamp = System.currentTimeMillis(),
                            likes = emptyList(),
                            comments = emptyList(),
                            reposts = emptyList(), // Fixed: was reposts = emptyList()
                            isEdited = false,
                            editHistory = emptyList()
                        )
                        newDocRef.set(newQuotePost).await()

                        // Update the original post's reposts list
                        val updatedReposts = originalPost.reposts + quotePostId
                        originalPostRef.update("reposts", updatedReposts).await()
                    }

                    // Refresh feeds
                    loadFeed(reset = true)
                    loadFollowingFeed(reset = true, authViewModel = authViewModel)
                    if (_userPosts.value.isNotEmpty()) {
                        loadUserPosts(originalPost.userId)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun clearUserPosts() {
        _userPosts.value = emptyList()
    }

    fun addComment(postId: String, commentContent: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val postRef = postsCollection.document(postId)
                val postSnapshot = postRef.get().await()
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null) {
                    val newComment = Comment(
                        commentId = "${System.currentTimeMillis()}_$currentUserId",
                        postId = postId,
                        commenterId = currentUserId,
                        content = commentContent,
                        timestamp = System.currentTimeMillis(),
                        likes = emptyList(),
                        replies = emptyList()
                    )

                    val updatedComments = post.comments + newComment
                    postRef.update("comments", updatedComments).await()

                    // Refresh the current post
                    loadPost(postId)
                    loadFeed(reset = true)
                    loadFollowingFeed(reset = true, authViewModel = authViewModel)
                    if (_userPosts.value.isNotEmpty()) {
                        loadUserPosts(post.userId)
                    }
                }
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val postRef = postsCollection.document(postId)
                val postSnapshot = postRef.get().await()
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null) {
                    val updatedComments = post.comments.map { comment ->
                        if (comment.commentId == commentId) {
                            val updatedLikes = if (comment.likes.contains(currentUserId)) {
                                comment.likes - currentUserId
                            } else {
                                comment.likes + currentUserId
                            }
                            comment.copy(likes = updatedLikes)
                        } else {
                            comment
                        }
                    }

                    postRef.update("comments", updatedComments).await()
                    loadPost(postId) // Refresh the current post
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun addCommentReply(postId: String, parentCommentId: String, replyContent: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val postRef = postsCollection.document(postId)
                val postSnapshot = postRef.get().await()
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null) {
                    val newReply = Comment(
                        commentId = "${System.currentTimeMillis()}_$currentUserId",
                        postId = postId,
                        commenterId = currentUserId,
                        content = replyContent,
                        timestamp = System.currentTimeMillis(),
                        likes = emptyList(),
                        replies = emptyList(),
                        parentCommentId = parentCommentId
                    )

                    val updatedComments = post.comments.map { comment ->
                        if (comment.commentId == parentCommentId) {
                            comment.copy(replies = comment.replies + newReply)
                        } else {
                            comment
                        }
                    }

                    postRef.update("comments", updatedComments).await()
                    loadPost(postId) // Refresh the current post
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val postRef = postsCollection.document(postId)
                val postSnapshot = postRef.get().await()
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null) {
                    val updatedComments = post.comments.filter {
                        it.commentId != commentId || it.commenterId == currentUserId
                    }
                    postRef.update("comments", updatedComments).await()
                    loadPost(postId)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }


    /**
     * Checks if the current user has retweeted the specified post.
     */
    suspend fun isPostRetweetedByUser(postId: String, userId: String): Boolean {
        return try {
            val repostSnapshot = realtimeDb.reference
                .child("reposts")
                .child(postId)
                .get()
                .await()

            repostSnapshot.children.any { snapshot ->
                val repost = snapshot.getValue(Repost::class.java)
                repost?.reposterId == userId && repost.repostType == RepostType.RETWEET
            }
        } catch (e: Exception) {
            // Handle errors silently (e.g., network issues)
            false
        }
    }

    fun incrementViewCount(postId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val postRef = postsCollection.document(postId)
                val postSnapshot = postRef.get().await()
                val post = postSnapshot.toObject(Post::class.java)

                if (post != null && !post.viewers.contains(currentUserId)) {
                    // Increment view count and add user to viewers
                    val updatedViewCount = post.viewCount + 1
                    val updatedViewers = post.viewers + currentUserId
                    postRef.update(
                        mapOf(
                            "viewCount" to updatedViewCount,
                            "viewers" to updatedViewers
                        )
                    ).await()

                    // Refresh feeds to reflect updated view count
                    loadFeed()
                    loadFollowingFeed(authViewModel = authViewModel)
                    if (_userPosts.value.isNotEmpty()) {
                        loadUserPosts(post.userId)
                    }
                    if (_currentPost.value?.postId == postId) {
                        loadPost(postId)
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

}

data class PostUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)