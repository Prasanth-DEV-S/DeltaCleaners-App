package com.example.deltacleaners.ui.force_update

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.deltacleaners.R
import com.example.deltacleaners.ui.components.PrimaryButton

@Composable
fun ForceUpdateScreen(
    title: String,
    message: String,
    playStoreUrl: String
) {
    val context = LocalContext.current

    // Prevent going back
    BackHandler(enabled = true) {
        // Do nothing - user must update
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Logo
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // Replace with actual logo
            contentDescription = "Delta Cleaners Logo",
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        PrimaryButton(
            text = "Update Now",
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
