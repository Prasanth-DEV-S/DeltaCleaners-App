package com.example.deltacleaners.ui.cleaner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.ui.history.StatusChip
import com.example.deltacleaners.ui.history.getServiceIcon
import com.example.deltacleaners.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanerDashboardScreen(
    navController: NavController,
    viewModel: CleanerViewModel = hiltViewModel()
) {
    val availableBookings by viewModel.availableBookings.collectAsState()
    val myBookings by viewModel.myBookings.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cleaner Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Available Jobs") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("My Active Jobs") }
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (selectedTab == 0) {
                    BookingList(
                        bookings = availableBookings,
                        onAction = { viewModel.acceptBooking(it.bookingId) },
                        actionText = "Accept Job"
                    )
                } else {
                    BookingList(
                        bookings = myBookings,
                        onAction = { booking ->
                            val nextStatus = when(booking.status) {
                                "Confirmed" -> "In Progress"
                                "In Progress" -> "Completed"
                                else -> null
                            }
                            nextStatus?.let { viewModel.updateStatus(booking.bookingId, it) }
                        },
                        actionText = "Update Status"
                    )
                }
            }
        }
    }
}

@Composable
fun BookingList(
    bookings: List<Booking>,
    onAction: (Booking) -> Unit,
    actionText: String
) {
    if (bookings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No bookings found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(bookings) { booking ->
                CleanerBookingCard(booking, onAction, actionText)
            }
        }
    }
}

@Composable
fun CleanerBookingCard(
    booking: Booking,
    onAction: (Booking) -> Unit,
    actionText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(getServiceIcon(booking.serviceName), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(booking.serviceName, fontWeight = FontWeight.Bold)
                        Text("₹${booking.price}", style = MaterialTheme.typography.labelSmall)
                    }
                }
                StatusChip(booking.status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${booking.date} at ${booking.time}", style = MaterialTheme.typography.bodySmall)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text(booking.address, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { onAction(booking) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(actionText)
            }
        }
    }
}
