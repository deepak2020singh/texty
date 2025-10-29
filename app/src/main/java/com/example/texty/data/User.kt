package com.example.texty.data

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val password: String = "",
    val username: String = "",
    val displayName: String = "",
    val profilePicture: String? = null,
    val bio: String = "",
    val location: String? = null,
    val joinDate: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val followers: List<String> = emptyList(), // List of user IDs
    val following: List<String> = emptyList(), // List of user IDs
    val posts: List<String> = emptyList(), // List of post IDs
) {
    // Add this to handle potential Firebase type issues
    constructor() : this(
        userId = "",
        email = "",
        name = "",
        password = "",
        username = "",
        displayName = "",
        profilePicture = null,
        bio = "",
        location = null,
        joinDate = System.currentTimeMillis(),
        isVerified = false,
        isPrivate = false,
        followers = emptyList(),
        following = emptyList(),
        posts = emptyList()
    )
}

