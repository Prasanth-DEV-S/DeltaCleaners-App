package com.example.deltacleaners.admin.ui.bookings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.data.model.Cleaner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(
    bookingId: String,
    onBack: () -> Unit,
    viewModel: BookingDetailsViewModel = hiltViewModel()
) {
    val booking by viewModel.booking.collectAsState()
    val cleaners by viewModel.cleaners.collectAsState()
    var showAssignDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookingId) {
        viewModel.loadBooking(bookingId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        booking?.let { b ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BookingHeader(b)
                
                DetailCard("Customer Info") {
                    InfoRow(Icons.Default.Person, b.userId) // Should show name if we had User repo access here
                    InfoRow(Icons.Default.Phone, "Call Customer")
                    InfoRow(Icons.Default.LocationOn, b.address)
                }

                DetailCard("Service Info") {
                    InfoRow(Icons.Default.CleaningServices, b.serviceName)
                    InfoRow(Icons.Default.Event, "${b.date} • ${b.time}")
                    InfoRow(Icons.Default.Payments, "₹${b.price}")
                }

                if (b.notes.isNotEmpty()) {
                    DetailCard("Notes") {
                        Text(b.notes, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showAssignDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Assign Cleaner")
                    }
                    OutlinedButton(
                        onClick = { viewModel.updateStatus("Cancelled") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Cancel")
                    }
                }

                Button(
                    onClick = { viewModel.updateStatus("Completed") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Mark as Completed")
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    if (showAssignDialog) {
        AssignCleanerDialog(
            cleaners = cleaners,
            onDismiss = { showAssignDialog = false },
            onAssign = { cleanerId ->
                viewModel.assignCleaner(cleanerId)
                showAssignDialog = false
            }
        )
    }
}

@Composable
fun BookingHeader(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Booking #${booking.bookingId.take(8).uppercase()}", fontWeight = FontWeight.Bold)
            Text("Status: ${booking.status}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DetailCard(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun AssignCleanerDialog(cleaners: List<Cleaner>, onDismiss: () -> Unit, onAssign: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Cleaner") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                cleaners.forEach { cleaner ->
                    TextButton(
                        onClick = { onAssign(cleaner.cleanerId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(cleaner.name)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
