package com.example.texty.ui.screen


import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.GifBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.texty.R
import com.example.texty.UserPreferencesManager
import com.example.texty.data.ImageUtils
import com.example.texty.ui.view_model.PostViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ComposeTextArea(
    userPreferencesManager: UserPreferencesManager,
    currentUserUsername: String = "your_username",
    onCancel: () -> Unit = {},
    postViewModel: PostViewModel
) {
    val userPref by userPreferencesManager.getUserPreferences().collectAsState(initial = null)

    Log.d("ComposeTextArea1", "Recomposing : ${userPref?.profilePictureUrl}")

    var textState by remember { mutableStateOf(TextFieldValue("")) }
    var charCount by remember { mutableIntStateOf(0) }
    var selectedMedia by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentLocation by remember { mutableStateOf<String?>(null) }
    var showLocationDialog by remember { mutableStateOf(false) }
    val maxChars = 280
    val maxMediaItems = 4
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by postViewModel.uiState.collectAsState()

    // Keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current
    val imePadding = WindowInsets.ime.asPaddingValues()

    // Media picker for images and videos
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxMediaItems)
    ) { uris ->
        val supportedMedia = uris.filter { uri ->
            val mimeType = context.contentResolver.getType(uri)
            mimeType?.startsWith("image/") == true || mimeType?.startsWith("video/") == true
        }
        selectedMedia = supportedMedia.take(maxMediaItems)
    }

    // Location launcher
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // Handle location result here
        // This is a simplified version - you'd integrate with your actual location service
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            currentLocation = "Current Location" // You'd get actual location name
        }
    }

    // Auto-focus text field on screen load
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Location dialog
    if (showLocationDialog) {
        LocationDialog(
            onDismiss = { showLocationDialog = false },
            onLocationSelected = { location ->
                currentLocation = location
                showLocationDialog = false
            },
            onRemoveLocation = {
                currentLocation = null
                showLocationDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top bar with Cancel and Post buttons
            TopAppBar(
                title = {},
                navigationIcon = {
                    TextButton(onClick = {
                        keyboardController?.hide()
                        onCancel()
                    }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                postViewModel.createPost(
                                    postId = UUID.randomUUID().toString(),
                                    content = textState.text,
                                    mediaUrls = selectedMedia,
                                    context = context
                                )
                                // Reset state after posting
                                textState = TextFieldValue("")
                                selectedMedia = emptyList()
                                charCount = 0
                                keyboardController?.hide()
                            }
                        },
                        enabled = (charCount > 0 || selectedMedia.isNotEmpty()) && charCount <= maxChars && !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .height(36.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Post", color = Color.White)
                        }
                    }
                }
            )

            // Main content area - profile and text field at top
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {


                    AsyncImage(
                        model = userPref?.profilePictureUrl?.let { ImageUtils.base64ToBitmap(it) },
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.user)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    // Text input and related UI
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Text input area with placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 400.dp)
                        ) {
                            if (textState.text.isEmpty()) {
                                Text(
                                    text = "What's happening?",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            BasicTextField(
                                value = textState,
                                onValueChange = { newValue ->
                                    val newText = newValue.text
                                    if (newText.length <= maxChars) {
                                        textState = newValue.copy(text = newText)
                                        charCount = newText.length
                                    } else {
                                        val trimmed = newText.take(maxChars)
                                        textState = TextFieldValue(
                                            text = trimmed,
                                            selection = TextRange(trimmed.length)
                                        )
                                        charCount = maxChars
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 24.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .focusRequester(focusRequester),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.TopStart
                                    ) {
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Display selected media (images and videos)
                        if (selectedMedia.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedMedia) { uri ->
                                    val mimeType =
                                        context.contentResolver.getType(uri) ?: "image/jpeg"
                                    Box(
                                        modifier = Modifier.size(100.dp)
                                    ) {
                                        if (mimeType.startsWith("image/")) {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = "Selected image",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(MaterialTheme.shapes.medium),
                                                contentScale = ContentScale.Crop,
                                                error = painterResource(id = R.drawable.sun_partially_covered_by_a_cloud)
                                            )
                                        } else if (mimeType.startsWith("video/")) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .background(Color.Gray),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Videocam,
                                                    contentDescription = "Selected video",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                            }
                                        }

                                        // Remove button
                                        IconButton(
                                            onClick = {
                                                selectedMedia = selectedMedia.filter { it != uri }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.6f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove media",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Character count - moved to bottom bar
                        Spacer(modifier = Modifier.height(16.dp))
                        // Error message
                        uiState.error?.let { error ->
                            Text(
                                text = error,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // Bottom bar with attachment icons and character count
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = imePadding.calculateBottomPadding()),
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Column {
                    // Visibility selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* TODO: Open visibility options */ }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AlternateEmail,
                            contentDescription = "Reply visibility",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Everyone can reply",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Attachment toolbar at bottom
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AttachmentIcon(Icons.Outlined.Image, "Add photo or video") {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                            }
                            AttachmentIcon(Icons.Outlined.GifBox, "Add GIF") { /* TODO */ }
                            AttachmentIcon(Icons.Outlined.BarChart, "Add poll") { /* TODO */ }
                            AttachmentIcon(Icons.Outlined.Schedule, "Schedule post") { /* TODO */ }
                            AttachmentIcon(
                                Icons.Outlined.LocationOn,
                                "Add location"
                            ) { showLocationDialog = true }
                            AttachmentIcon(
                                Icons.Outlined.AddCircleOutline,
                                "Add to thread"
                            ) { /* TODO */ }
                        }

                        // Character count circle at bottom
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = when {
                                        charCount > maxChars -> Color.Red.copy(alpha = 0.2f)
                                        charCount > maxChars - 20 -> Color.Yellow.copy(alpha = 0.2f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = CircleShape
                                )
                        ) {
                            Text(
                                text = (maxChars - charCount).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    charCount > maxChars -> Color.Red
                                    charCount > maxChars - 20 -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (String) -> Unit,
    onRemoveLocation: () -> Unit
) {
    // Simple location dialog - you can enhance this with actual location services
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Location") },
        text = {
            Column {
                Text("Choose a location to attach to your post")
                Spacer(modifier = Modifier.height(16.dp))
                // You can add location search, current location, etc. here
                Text(
                    "Current Location",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLocationSelected("Current Location") }
                        .padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Custom Location",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLocationSelected("Custom Place") }
                        .padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onRemoveLocation) {
                Text("Remove Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AttachmentIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}
