package com.example.deltacleaners.admin.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.deltacleaners.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationScreen(
    viewModel: AdminNotificationViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var targetUser by remember { mutableStateOf("All Users") } // For demo

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Send Notification", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notification Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message Body") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Text("Target Audience: $targetUser", style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Send Notification",
                onClick = { 
                    viewModel.sendNotification(title, message)
                    title = ""
                    message = ""
                }
            )
        }
    }
}
