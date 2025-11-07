package com.example.texty.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.texty.UserPreferencesManager
import com.example.texty.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


//class AuthViewModel(
//    private val realDb: FirebaseDatabase,
//    private val userPreferencesManager: UserPreferencesManager,
//) : ViewModel() {
//    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
//    val firebaseUser: LiveData<FirebaseUser?> get() = _firebaseUser
//
//    private val _error = MutableLiveData<String>()
//    val error: LiveData<String> get() = _error
//
//    private val _users = MutableLiveData<List<User>>()
//    val users: LiveData<List<User>> get() = _users
//
//    private val _userById = MutableLiveData<User?>()
//    val userById: LiveData<User?> get() = _userById
//
//    // StateFlow for current user
//    private val _currentUser = MutableStateFlow<User?>(null)
//    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
//
//    // User cache for storing fetched users
//    private val _userCache = mutableMapOf<String, User>()
//    private val _userFlowCache = MutableStateFlow<Map<String, User>>(emptyMap())
//    val userFlowCache: StateFlow<Map<String, User>> = _userFlowCache.asStateFlow()
//
//    init {
//        _firebaseUser.value = FirebaseAuth.getInstance().currentUser
//        fetchCurrentUser()
//    }
//
//    fun login(email: String, password: String) {
//        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val firebaseUser = task.result?.user
//                    _firebaseUser.value = firebaseUser
//
//                    // Fetch user data and save to preferences
//                    firebaseUser?.uid?.let { userId ->
//                        viewModelScope.launch {
//                            try {
//                                userPreferencesManager.updateLoginStatus(true)
//                                // Fetch user from database
//                                fetchUserFromDatabase(userId) { user ->
//                                    if (user != null) {
//                                        // Save user to preferences
//                                        _currentUser.value = user
//                                    } else {
//                                        _error.value = "User data not found"
//                                    }
//                                }
//                            } catch (e: Exception) {
//                                _error.value = "Failed to load user data: ${e.message}"
//                            }
//                        }
//                    }
//                } else {
//                    _error.value = task.exception?.message ?: "Login failed"
//                }
//            }
//            .addOnFailureListener {
//                _error.value = it.message ?: "Login failed"
//            }
//    }
//
//    fun signUp(email: String, password: String, name: String, base64Image: String? = null) {
//        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val firebaseUser = task.result?.user
//                    _firebaseUser.value = firebaseUser
//
//                    // Save to Realtime Database and preferences
//                    firebaseUser?.let { user ->
//                        viewModelScope.launch {
//                            try {
//                                // Save to Realtime Database
//                                val user =  User(
//                                    userId = user.uid,
//                                    name = name,
//                                    email = email,
//                                    password = password,
//                                    username = generateUsername(name),
//                                    displayName = name,
//                                    profilePicture = base64Image,
//                                )
//
//                                val newUser = saveDataToRealTimeDatabase(userData = user, db = realDb)
//                                // Save to preferences
//                                userPreferencesManager.saveUserPreferences(user)
//                                userPreferencesManager.updateLoginStatus(true)
//
//                                // Update current user
//                                _currentUser.value = newUser
//                                fetchCurrentUser()
//                            } catch (e: Exception) {
//                                _error.value = "Failed to save user data: ${e.message}"
//                            }
//                        }
//                    }
//                } else {
//                    _error.value = task.exception?.message ?: "Sign up failed"
//                }
//            }
//            .addOnFailureListener {
//                _error.value = it.message ?: "Sign up failed"
//            }
//    }
//
//    private suspend fun saveDataToRealTimeDatabase(
//        userData: User,
//        db: FirebaseDatabase
//    ): User {
//        val userRef = realDb.reference.child("users").child(userData.userId)
//        val userData = User(
//            userId = userData.userId,
//            name = userData.name,
//            email = userData.email,
//            password = "", // Don't store password in plain text in real app
//            username = userData.username,
//            displayName = userData.name,
//            profilePicture = userData.profilePicture,
//            bio = "",
//            followers = emptyList(),
//            following = emptyList(),
//            posts = emptyList(),
//            joinDate = System.currentTimeMillis()
//        )
//        userRef.setValue(userData).await()
//        return userData
//    }
//
//    private fun generateUsername(name: String): String {
//        val baseUsername = name.replace("\\s+".toRegex(), "").lowercase()
//        val randomSuffix = (1000..9999).random()
//        return "$baseUsername$randomSuffix"
//    }
//
//    fun onLogOut() {
//        FirebaseAuth.getInstance().signOut()
//        _firebaseUser.value = null
//        _currentUser.value = null
//        _userCache.clear()
//        _userFlowCache.value = emptyMap()
//        viewModelScope.launch {
//            userPreferencesManager.clearUserPreferences()
//        }
//    }
//
//    // Helper function to fetch user from database with callback
//    private fun fetchUserFromDatabase(userId: String, onComplete: (User?) -> Unit) {
//        realDb.reference.child("users").child(userId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        val user = snapshot.getValue(User::class.java)
//                        onComplete(user)
//                    } else {
//                        onComplete(null)
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    _error.postValue(error.message)
//                    onComplete(null)
//                }
//            })
//    }
//
//    fun fetchAllUsers() {
//        realDb.reference.child("users")
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val userList = mutableListOf<User>()
//                    for (userSnapshot in snapshot.children) {
//                        val user = userSnapshot.getValue(User::class.java)
//                        if (user != null) {
//                            userList.add(user)
//                        } else {
//                            _error.postValue("User data is null")
//                        }
//                    }
//                    _users.postValue(userList)
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    _error.postValue(error.message)
//                }
//            })
//    }
//
//    // Fetch user and cache it
//    fun fetchUser(userId: String) {
//        if (_userCache.containsKey(userId)) {
//            _userById.postValue(_userCache[userId])
//            return
//        }
//
//        realDb.reference.child("users").child(userId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        val user = snapshot.getValue(User::class.java)
//                        user?.let {
//                            _userCache[userId] = it
//                            _userById.postValue(it)
//                            // Update the flow cache
//                            _userFlowCache.value = _userCache.toMap()
//                        } ?: run {
//                            _error.postValue("User data is null")
//                        }
//                    } else {
//                        _error.postValue("User not found")
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    _error.postValue(error.message)
//                }
//            })
//    }
//
//    // Get user from cache or return null
//    fun getUserFromCache(userId: String): User? {
//        return _userCache[userId]
//    }
//
//    // Fetch multiple users for posts
//    fun fetchUsersForPosts(userIds: List<String>) {
//        userIds.forEach { userId ->
//            if (!_userCache.containsKey(userId)) {
//                fetchUser(userId)
//            }
//        }
//    }
//
//    // Fetch current user
//    private fun fetchCurrentUser() {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        fetchUser(currentUserId)
//    }
//
//
//    fun followUser(targetUserId: String) {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        viewModelScope.launch {
//            try {
//                // Update current user's following list
//                val currentUserRef = realDb.reference.child("users").child(currentUserId)
//                val currentUserSnapshot = currentUserRef.get().await()
//                val currentUser = currentUserSnapshot.getValue(User::class.java)
//                if (currentUser != null && !currentUser.following.contains(targetUserId)) {
//                    val updatedFollowing = currentUser.following + targetUserId
//                    currentUserRef.child("following").setValue(updatedFollowing).await()
//
//                    // Update current user in cache and preferences if it's the logged-in user
//                    if (currentUserId == _firebaseUser.value?.uid) {
//                        val updatedCurrentUser = currentUser.copy(following = updatedFollowing)
//                        _userCache[currentUserId] = updatedCurrentUser
//                        _currentUser.value = updatedCurrentUser
//                        userPreferencesManager.saveUserPreferences(updatedCurrentUser)
//                    }
//                }
//
//                // Update target user's followers list
//                val targetUserRef = realDb.reference.child("users").child(targetUserId)
//                val targetUserSnapshot = targetUserRef.get().await()
//                val targetUser = targetUserSnapshot.getValue(User::class.java)
//                if (targetUser != null && !targetUser.followers.contains(currentUserId)) {
//                    val updatedFollowers = targetUser.followers + currentUserId
//                    targetUserRef.child("followers").setValue(updatedFollowers).await()
//
//                    // Update target user in cache
//                    _userCache[targetUserId] = targetUser.copy(followers = updatedFollowers)
//                }
//
//                // Refresh users list
//                fetchAllUsers()
//            } catch (e: Exception) {
//                _error.postValue("Failed to follow user: ${e.message}")
//            }
//        }
//    }
//
//
//
//
//    fun unfollowUser(targetUserId: String) {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        viewModelScope.launch {
//            try {
//                // Update current user's following list
//                val currentUserRef = realDb.reference.child("users").child(currentUserId)
//                val currentUserSnapshot = currentUserRef.get().await()
//                val currentUser = currentUserSnapshot.getValue(User::class.java)
//                if (currentUser != null && currentUser.following.contains(targetUserId)) {
//                    val updatedFollowing = currentUser.following - targetUserId
//                    currentUserRef.child("following").setValue(updatedFollowing).await()
//
//                    // Update current user in cache and preferences if it's the logged-in user
//                    if (currentUserId == _firebaseUser.value?.uid) {
//                        val updatedCurrentUser = currentUser.copy(following = updatedFollowing)
//                        _userCache[currentUserId] = updatedCurrentUser
//                        _currentUser.value = updatedCurrentUser
//                        userPreferencesManager.saveUserPreferences(updatedCurrentUser)
//                    }
//                }
//
//                // Update target user's followers list
//                val targetUserRef = realDb.reference.child("users").child(targetUserId)
//                val targetUserSnapshot = targetUserRef.get().await()
//                val targetUser = targetUserSnapshot.getValue(User::class.java)
//                if (targetUser != null && targetUser.followers.contains(currentUserId)) {
//                    val updatedFollowers = targetUser.followers - currentUserId
//                    targetUserRef.child("followers").setValue(updatedFollowers).await()
//
//                    // Update target user in cache
//                    _userCache[targetUserId] = targetUser.copy(followers = updatedFollowers)
//                }
//
//                // Refresh users list
//                fetchAllUsers()
//            } catch (e: Exception) {
//                _error.postValue("Failed to unfollow user: ${e.message}")
//            }
//        }
//    }
//
//    // Fetch a single user by their userId
//    fun fetchUserById(userId: String) {
//        fetchUser(userId)
//    }
//
//    // LiveData for followers and following
//    private val _followers = MutableLiveData<List<User>>()
//    val followers: LiveData<List<User>> get() = _followers
//
//    private val _following = MutableLiveData<List<User>>()
//    val following: LiveData<List<User>> get() = _following
//
//    fun fetchFollowers(userId: String) {
//        realDb.reference.child("users").child(userId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val user = snapshot.getValue(User::class.java)
//                    val followerIds = user?.followers ?: emptyList()
//
//                    if (followerIds.isEmpty()) {
//                        _followers.postValue(emptyList())
//                        return
//                    }
//
//                    fetchUsersDetails(followerIds) { users ->
//                        val followersList = users.map { user ->
//                            // Update cache
//                            _userCache[user.userId] = user
//                            user
//                        }
//                        _followers.postValue(followersList)
//                        // Update flow cache
//                        _userFlowCache.value = _userCache.toMap()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    _error.postValue(error.message)
//                }
//            })
//    }
//
//    fun fetchFollowing(userId: String) {
//        realDb.reference.child("users").child(userId)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val user = snapshot.getValue(User::class.java)
//                    val followingIds = user?.following ?: emptyList()
//
//                    if (followingIds.isEmpty()) {
//                        _following.postValue(emptyList())
//                        return
//                    }
//
//                    fetchUsersDetails(followingIds) { users ->
//                        val followingList = users.map { user ->
//                            // Update cache
//                            _userCache[user.userId] = user
//                            user
//                        }
//                        _following.postValue(followingList)
//                        // Update flow cache
//                        _userFlowCache.value = _userCache.toMap()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    _error.postValue(error.message)
//                }
//            })
//    }
//
//    private fun fetchUsersDetails(userIds: List<String>, onComplete: (List<User>) -> Unit) {
//        val users = mutableListOf<User>()
//        var completed = 0
//
//        if (userIds.isEmpty()) {
//            onComplete(emptyList())
//            return
//        }
//
//        userIds.forEach { userId ->
//            // Check cache first
//            if (_userCache.containsKey(userId)) {
//                _userCache[userId]?.let { users.add(it) }
//                completed++
//                if (completed == userIds.size) {
//                    onComplete(users)
//                }
//                return@forEach
//            }
//
//            realDb.reference.child("users").child(userId)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val user = snapshot.getValue(User::class.java)
//                        user?.let { users.add(it) }
//                        completed++
//                        if (completed == userIds.size) {
//                            onComplete(users)
//                        }
//                    }
//                    override fun onCancelled(error: DatabaseError) {
//                        completed++
//                        if (completed == userIds.size) {
//                            onComplete(users)
//                        }
//                    }
//                })
//        }
//    }
//
//}





















data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val type: String = "follow", // e.g., "follow", "like", "comment", "mention"
    val fromUserId: String = "",
    val targetUserId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false // Optional: to track if notification is read
)


class AuthViewModel(
    private val realDb: FirebaseDatabase,
    private val userPreferencesManager: UserPreferencesManager,
) : ViewModel() {
    private val _firebaseUser = MutableLiveData<FirebaseUser?>()
    val firebaseUser: LiveData<FirebaseUser?> get() = _firebaseUser

    private val _error = MutableLiveData<String?>()  // Changed to nullable
    val error: LiveData<String?> get() = _error       // Expose nullable

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _userById = MutableLiveData<User?>()
    val userById: LiveData<User?> get() = _userById

    // StateFlow for current user
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // User cache for storing fetched users
    private val _userCache = mutableMapOf<String, User>()
    private val _userFlowCache = MutableStateFlow<Map<String, User>>(emptyMap())
    val userFlowCache: StateFlow<Map<String, User>> = _userFlowCache.asStateFlow()

    // Notifications
    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications

       // Add this function to clear the error
    fun clearError() {
        _error.value = null
    }

    init {
        _firebaseUser.value = FirebaseAuth.getInstance().currentUser
        fetchCurrentUser()
    }

    fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    _firebaseUser.value = firebaseUser

                    // Fetch user data and save to preferences
                    firebaseUser?.uid?.let { userId ->
                        viewModelScope.launch {
                            try {
                                userPreferencesManager.updateLoginStatus(true)
                                // Fetch user from database
                                fetchUserFromDatabase(userId) { user ->
                                    if (user != null) {
                                        // Save user to preferences
                                        _currentUser.value = user
                                    } else {
                                        _error.value = "User data not found"
                                    }
                                }
                            } catch (e: Exception) {
                                _error.value = "Failed to load user data: ${e.message}"
                            }
                        }
                    }
                } else {
                    _error.value = task.exception?.message ?: "Login failed"
                }
            }
            .addOnFailureListener {
                _error.value = it.message ?: "Login failed"
            }
    }

    fun signUp(email: String, password: String, name: String, base64Image: String? = null) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    _firebaseUser.value = firebaseUser

                    // Save to Realtime Database and preferences
                    firebaseUser?.let { user ->
                        viewModelScope.launch {
                            try {
                                // Save to Realtime Database
                                val user =  User(
                                    userId = user.uid,
                                    name = name,
                                    email = email,
                                    password = password,
                                    username = generateUsername(name),
                                    displayName = name,
                                    profilePicture = base64Image,
                                )

                                val newUser = saveDataToRealTimeDatabase(userData = user, db = realDb)
                                // Save to preferences
                                userPreferencesManager.saveUserPreferences(user)
                                userPreferencesManager.updateLoginStatus(true)

                                // Update current user
                                _currentUser.value = newUser
                                fetchCurrentUser()
                            } catch (e: Exception) {
                                _error.value = "Failed to save user data: ${e.message}"
                            }
                        }
                    }
                } else {
                    _error.value = task.exception?.message ?: "Sign up failed"
                }
            }
            .addOnFailureListener {
                _error.value = it.message ?: "Sign up failed"
            }
    }

    private suspend fun saveDataToRealTimeDatabase(
        userData: User,
        db: FirebaseDatabase
    ): User {
        val userRef = realDb.reference.child("users").child(userData.userId)
        val userData = User(
            userId = userData.userId,
            name = userData.name,
            email = userData.email,
            password = "", // Don't store password in plain text in real app
            username = userData.username,
            displayName = userData.name,
            profilePicture = userData.profilePicture,
            bio = "",
            followers = emptyList(),
            following = emptyList(),
            posts = emptyList(),
            joinDate = System.currentTimeMillis()
        )
        userRef.setValue(userData).await()
        return userData
    }

    private fun generateUsername(name: String): String {
        val baseUsername = name.replace("\\s+".toRegex(), "").lowercase()
        val randomSuffix = (1000..9999).random()
        return "$baseUsername$randomSuffix"
    }

    fun onLogOut() {
        FirebaseAuth.getInstance().signOut()
        _firebaseUser.value = null
        _currentUser.value = null
        _userCache.clear()
        _userFlowCache.value = emptyMap()
        viewModelScope.launch {
            userPreferencesManager.clearUserPreferences()
        }
    }

    // Helper function to fetch user from database with callback
    private fun fetchUserFromDatabase(userId: String, onComplete: (User?) -> Unit) {
        realDb.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        onComplete(user)
                    } else {
                        onComplete(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.postValue(error.message)
                    onComplete(null)
                }
            })
    }

    fun fetchAllUsers() {
        realDb.reference.child("users")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userList = mutableListOf<User>()
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            userList.add(user)
                        } else {
                            _error.postValue("User data is null")
                        }
                    }
                    _users.postValue(userList)
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.postValue(error.message)
                }
            })
    }

    // Fetch user and cache it
    fun fetchUser(userId: String) {
        if (_userCache.containsKey(userId)) {
            _userById.postValue(_userCache[userId])
            return
        }

        realDb.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let {
                            _userCache[userId] = it
                            _userById.postValue(it)
                            // Update the flow cache
                            _userFlowCache.value = _userCache.toMap()
                        } ?: run {
                            _error.postValue("User data is null")
                        }
                    } else {
                        _error.postValue("User not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.postValue(error.message)
                }
            })
    }

    // Get user from cache or return null
    fun getUserFromCache(userId: String): User? {
        return _userCache[userId]
    }

    // Fetch multiple users for posts
    fun fetchUsersForPosts(userIds: List<String>) {
        userIds.forEach { userId ->
            if (!_userCache.containsKey(userId)) {
                fetchUser(userId)
            }
        }
    }

    // Fetch current user
    private fun fetchCurrentUser() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        fetchUser(currentUserId)
    }


    fun followUser(targetUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Update current user's following list
                val currentUserRef = realDb.reference.child("users").child(currentUserId)
                val currentUserSnapshot = currentUserRef.get().await()
                val currentUser = currentUserSnapshot.getValue(User::class.java)
                if (currentUser != null && !currentUser.following.contains(targetUserId)) {
                    val updatedFollowing = currentUser.following + targetUserId
                    currentUserRef.child("following").setValue(updatedFollowing).await()

                    // Update current user in cache and preferences if it's the logged-in user
                    if (currentUserId == _firebaseUser.value?.uid) {
                        val updatedCurrentUser = currentUser.copy(following = updatedFollowing)
                        _userCache[currentUserId] = updatedCurrentUser
                        _currentUser.value = updatedCurrentUser
                        userPreferencesManager.saveUserPreferences(updatedCurrentUser)
                    }
                }

                // Update target user's followers list
                val targetUserRef = realDb.reference.child("users").child(targetUserId)
                val targetUserSnapshot = targetUserRef.get().await()
                val targetUser = targetUserSnapshot.getValue(User::class.java)
                if (targetUser != null && !targetUser.followers.contains(currentUserId)) {
                    val updatedFollowers = targetUser.followers + currentUserId
                    targetUserRef.child("followers").setValue(updatedFollowers).await()

                    // Update target user in cache
                    _userCache[targetUserId] = targetUser.copy(followers = updatedFollowers)
                }

                // Create follow notification for target user
                createFollowNotification(currentUserId, targetUserId)

                // Refresh users list
                fetchAllUsers()
            } catch (e: Exception) {
                _error.postValue("Failed to follow user: ${e.message}")
            }
        }
    }

    private suspend fun createFollowNotification(fromUserId: String, targetUserId: String) {
        val notification = Notification(
            id = UUID.randomUUID().toString(),
            type = "follow",
            fromUserId = fromUserId,
            targetUserId = targetUserId,
            timestamp = System.currentTimeMillis()
        )
        val notificationsRef = realDb.reference.child("notifications").child(targetUserId).child(notification.id)
        notificationsRef.setValue(notification).await()
    }

    fun unfollowUser(targetUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                // Update current user's following list
                val currentUserRef = realDb.reference.child("users").child(currentUserId)
                val currentUserSnapshot = currentUserRef.get().await()
                val currentUser = currentUserSnapshot.getValue(User::class.java)
                if (currentUser != null && currentUser.following.contains(targetUserId)) {
                    val updatedFollowing = currentUser.following - targetUserId
                    currentUserRef.child("following").setValue(updatedFollowing).await()

                    // Update current user in cache and preferences if it's the logged-in user
                    if (currentUserId == _firebaseUser.value?.uid) {
                        val updatedCurrentUser = currentUser.copy(following = updatedFollowing)
                        _userCache[currentUserId] = updatedCurrentUser
                        _currentUser.value = updatedCurrentUser
                        userPreferencesManager.saveUserPreferences(updatedCurrentUser)
                    }
                }

                // Update target user's followers list
                val targetUserRef = realDb.reference.child("users").child(targetUserId)
                val targetUserSnapshot = targetUserRef.get().await()
                val targetUser = targetUserSnapshot.getValue(User::class.java)
                if (targetUser != null && targetUser.followers.contains(currentUserId)) {
                    val updatedFollowers = targetUser.followers - currentUserId
                    targetUserRef.child("followers").setValue(updatedFollowers).await()

                    // Update target user in cache
                    _userCache[targetUserId] = targetUser.copy(followers = updatedFollowers)
                }

                // Refresh users list
                fetchAllUsers()
            } catch (e: Exception) {
                _error.postValue("Failed to unfollow user: ${e.message}")
            }
        }
    }

    // Fetch a single user by their userId
    fun fetchUserById(userId: String) {
        fetchUser(userId)
    }

    // LiveData for followers and following
    private val _followers = MutableLiveData<List<User>>()
    val followers: LiveData<List<User>> get() = _followers

    private val _following = MutableLiveData<List<User>>()
    val following: LiveData<List<User>> get() = _following

    fun fetchFollowers(userId: String) {
        realDb.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    val followerIds = user?.followers ?: emptyList()

                    if (followerIds.isEmpty()) {
                        _followers.postValue(emptyList())
                        return
                    }

                    fetchUsersDetails(followerIds) { users ->
                        val followersList = users.map { user ->
                            // Update cache
                            _userCache[user.userId] = user
                            user
                        }
                        _followers.postValue(followersList)
                        // Update flow cache
                        _userFlowCache.value = _userCache.toMap()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.postValue(error.message)
                }
            })
    }

    fun fetchFollowing(userId: String) {
        realDb.reference.child("users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    val followingIds = user?.following ?: emptyList()

                    if (followingIds.isEmpty()) {
                        _following.postValue(emptyList())
                        return
                    }

                    fetchUsersDetails(followingIds) { users ->
                        val followingList = users.map { user ->
                            // Update cache
                            _userCache[user.userId] = user
                            user
                        }
                        _following.postValue(followingList)
                        // Update flow cache
                        _userFlowCache.value = _userCache.toMap()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.postValue(error.message)
                }
            })
    }

    private fun fetchUsersDetails(userIds: List<String>, onComplete: (List<User>) -> Unit) {
        val users = mutableListOf<User>()
        var completed = 0

        if (userIds.isEmpty()) {
            onComplete(emptyList())
            return
        }

        userIds.forEach { userId ->
            // Check cache first
            if (_userCache.containsKey(userId)) {
                _userCache[userId]?.let { users.add(it) }
                completed++
                if (completed == userIds.size) {
                    onComplete(users)
                }
                return@forEach
            }

            realDb.reference.child("users").child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue(User::class.java)
                        user?.let { users.add(it) }
                        completed++
                        if (completed == userIds.size) {
                            onComplete(users)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        completed++
                        if (completed == userIds.size) {
                            onComplete(users)
                        }
                    }
                })
        }
    }

    fun fetchNotifications(userId: String) {
        realDb.reference.child("notifications").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifList = mutableListOf<Notification>()
                    for (notifSnapshot in snapshot.children) {
                        val notification = notifSnapshot.getValue(Notification::class.java)
                        if (notification != null && notification.type == "follow") {
                            notifList.add(notification)
                        }
                    }
                    // Sort by timestamp descending
                    notifList.sortByDescending { it.timestamp }
                    _notifications.postValue(notifList)
                }

                override fun onCancelled(error: DatabaseError) {
                    _error.postValue(error.message)
                }
            })
    }
}
