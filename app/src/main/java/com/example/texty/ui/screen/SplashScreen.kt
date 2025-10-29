package com.example.texty.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.texty.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxSize()
    ) {
        val imageRef = createRef()

        // Center the image in the layout
        Image(
            painter = painterResource(id = R.drawable.ic_twitter),
            contentDescription = "Splash Screen Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .constrainAs(imageRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .size(80.dp)
        )

        // Check if user is already logged in and navigate accordingly
        LaunchedEffect(Unit) {
            delay(2000L) // 2-second delay

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // User is already logged in, navigate to main screen
                onNavigateToMain()
            } else {
                // User is not logged in, navigate to login screen
                onNavigateToLogin()
            }
        }
    }
}

