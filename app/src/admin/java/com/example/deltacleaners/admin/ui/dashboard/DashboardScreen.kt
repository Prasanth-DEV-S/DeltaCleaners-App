package com.example.deltacleaners.admin.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.deltacleaners.data.model.Booking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBookingClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val data by viewModel.dashboardData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delta Admin", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FA)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            item {
                StatsGrid(data)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Bookings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { /* Navigate to Bookings Tab */ }) {
                        Text("See All")
                    }
                }
            }

            if (isLoading && data.recentBookings.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (data.recentBookings.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No recent bookings", color = Color.Gray)
                    }
                }
            } else {
                items(data.recentBookings.take(5)) { booking ->
                    BookingListItem(booking, onClick = { onBookingClick(booking.bookingId) })
                }
            }
        }
    }
}

@Composable
fun StatsGrid(data: com.example.deltacleaners.data.repository.DashboardData) {
    val items = listOf(
        StatItem("Total Bookings", data.totalBookings.toString(), Icons.Default.Assignment, Color(0xFF1565D8)),
        StatItem("Revenue", "₹${String.format("%.0f", data.totalRevenue)}", Icons.Default.Payments, Color(0xFF2E7D32)),
        StatItem("Pending", data.pendingBookings.toString(), Icons.Default.Schedule, Color(0xFFEF6C00)),
        StatItem("Completed", data.completedBookings.toString(), Icons.Default.CheckCircle, Color(0xFF2E7D32)),
        StatItem("Users", data.totalUsers.toString(), Icons.Default.People, Color(0xFF6A1B9A)),
        StatItem("Active Cleaners", data.activeCleaners.toString(), Icons.Default.Group, Color(0xFF00ACC1)),
        StatItem("Reviews", data.totalReviews.toString(), Icons.Default.Star, Color(0xFFFFB400)),
        StatItem("Cancelled", data.cancelledBookings.toString(), Icons.Default.Cancel, Color(0xFFC62828))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in items.indices step 2) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(items[i], Modifier.weight(1f))
                if (i + 1 < items.size) {
                    StatCard(items[i + 1], Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatCard(item: StatItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = item.color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.padding(4.dp).size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.title, fontSize = 12.sp, color = Color.Gray)
            }
            Text(item.value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = item.color)
        }
    }
}

data class StatItem(val title: String, val value: String, val icon: ImageVector, val color: Color)

@Composable
fun BookingListItem(booking: Booking, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(booking.serviceName, fontWeight = FontWeight.Bold)
                Text("Booking ID: #${booking.bookingId.take(8).uppercase()}", fontSize = 11.sp, color = Color.Gray)
            }
            StatusBadge(booking.status)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Pending" -> Color(0xFFEF6C00)
        "Completed" -> Color(0xFF2E7D32)
        "Cancelled" -> Color(0xFFC62828)
        else -> Color(0xFF1565D8)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
