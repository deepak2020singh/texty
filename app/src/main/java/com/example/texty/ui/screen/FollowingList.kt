package com.example.texty.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.texty.ui.view_model.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingScreen1(
    userId: String,
    viewModel: AuthViewModel,
    onNavigateToProfile: (String) -> Unit,
    onBack: ()-> Unit
) {
    val context = LocalContext.current
    val following by viewModel.following.observeAsState()
    val error by viewModel.error.observeAsState()
    var isLoading by remember { mutableStateOf(true) }

    // Fetch following when the screen is displayed
    LaunchedEffect(userId) {
        viewModel.fetchFollowing(userId)
        isLoading = false
    }

    // Show error as a Toast
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Following") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            UserList(
                users = following ?: emptyList(),
                onNavigateToProfile = onNavigateToProfile,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// Reuse UserList, UserItem, and PlaceholderProfilePicture from FollowersScreen