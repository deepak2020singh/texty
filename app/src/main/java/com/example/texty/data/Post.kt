package com.example.texty.data

import java.util.UUID

data class Post(
    val postId: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val content: String = "",
    val mediaBase64: List<String> = emptyList(),
    val mediaTypes: List<String> = emptyList(),
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val reposts: List<String> = emptyList(),
    val viewCount: Int = 0,
    val viewers: List<String> = emptyList(), // New field to track viewers
    val isEdited: Boolean = false,
    val editHistory: List<EditRecord> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val mentions: List<String> = emptyList(),
    val postType: PostType = PostType.ORIGINAL,
    val parentPostId: String? = null,
    val location: String? = null
) {
    constructor() : this(
        postId = UUID.randomUUID().toString(),
        userId = "",
        content = "",
        mediaBase64 = emptyList(),
        mediaTypes = emptyList(),
        message = null,
        timestamp = System.currentTimeMillis(),
        likes = emptyList(),
        comments = emptyList(),
        reposts = emptyList(),
        viewCount = 0,
        viewers = emptyList(), // Initialize new field
        isEdited = false,
        editHistory = emptyList(),
        hashtags = emptyList(),
        mentions = emptyList(),
        postType = PostType.ORIGINAL,
        parentPostId = null,
        location = null
    )
}


data class Repost(
    val repostId: String = UUID.randomUUID().toString(),
    val originalPostId: String = "",
    val reposterId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val additionalComment: String? = null,
    val repostType: RepostType = RepostType.RETWEET
) {
    // No-arg constructor for Firebase deserialization
    constructor() : this(
        repostId = UUID.randomUUID().toString(),
        originalPostId = "",
        reposterId = "",
        timestamp = System.currentTimeMillis(),
        additionalComment = null,
        repostType = RepostType.RETWEET
    )
}

data class EditRecord(
    val editTimestamp: Long = System.currentTimeMillis(),
    val previousContent: String = "",
    val previousMediaBase64: List<String> = emptyList(),
) {
    // No-arg constructor for Firebase deserialization
    constructor() : this(
        editTimestamp = System.currentTimeMillis(),
        previousContent = "",
        previousMediaBase64 = emptyList()
    )
}

enum class PostType {
    ORIGINAL, REPLY, QUOTE
}

enum class RepostType {
    RETWEET, QUOTE
}
