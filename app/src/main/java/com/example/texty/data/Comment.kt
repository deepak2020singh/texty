package com.example.texty.data

data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val commenterId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList(),
    val replies: List<Comment> = emptyList(),
    val parentCommentId: String? = null,
    val isEdited: Boolean? = null // Added optional isEdited field
) {
    // No-arg constructor for Firebase deserialization
    constructor() : this(
        commentId = "",
        postId = "",
        commenterId = "",
        content = "",
        timestamp = System.currentTimeMillis(),
        likes = emptyList(),
        replies = emptyList(),
        parentCommentId = null,
        isEdited = null
    )
}