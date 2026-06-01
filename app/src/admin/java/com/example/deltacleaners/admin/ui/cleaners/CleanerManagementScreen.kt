package com.example.deltacleaners.admin.ui.cleaners

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.deltacleaners.data.model.Cleaner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanerManagementScreen(
    viewModel: CleanerManagementViewModel = hiltViewModel()
) {
    val cleaners by viewModel.cleaners.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Employee", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Cleaner")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && cleaners.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (cleaners.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No cleaners found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F7FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cleaners, key = { it.cleanerId }) { cleaner ->
                    CleanerItem(
                        cleaner = cleaner,
                        onToggleStatus = { viewModel.toggleCleanerStatus(cleaner.cleanerId, !cleaner.isActive) },
                        onDelete = { viewModel.deleteCleaner(cleaner.cleanerId) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCleanerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, phone, city ->
                viewModel.addCleaner(name, phone, city)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddCleanerDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Cleaner") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        isLoading = true
                        onConfirm(name, phone, city) 
                    }
                },
                enabled = !isLoading
            ) { 
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Save") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") }
        }
    )
}

@Composable
fun CleanerItem(
    cleaner: Cleaner,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cleaner.profileImage.isNotEmpty()) {
                    AsyncImage(
                        model = cleaner.profileImage,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFEAF3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(cleaner.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB400), modifier = Modifier.size(14.dp))
                        Text(" ${String.format("%.1f", cleaner.rating)} • ${cleaner.totalJobs} jobs", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(cleaner.phone, fontSize = 12.sp, color = Color.Gray)
                    Text("Earnings: ₹${cleaner.earnings}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }

                Switch(
                    checked = cleaner.isActive,
                    onCheckedChange = { onToggleStatus() },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF2E7D32))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("Remove")
                }
                IconButton(onClick = { /* TODO: Call */ }) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF2E7D32))
                }
            }
        }
    }
}
