package com.example.deltacleaners.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deltacleaners.data.model.Booking
import com.example.deltacleaners.ui.NavigationItem
import com.example.deltacleaners.ui.navigation.Screen
import com.example.deltacleaners.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    navController: NavController,
    viewModel: BookingHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Booking History", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {  navController.navigate(Screen.Home.route) {
                        // Clears the history so the user can't "go back" to history
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading && uiState.bookings.isEmpty() -> {
                    LoadingShimmer()
                }
                uiState.bookings.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    BookingList(
                        bookings = uiState.bookings,
                        navController = navController,
                        onCancel = { viewModel.cancelBooking(it) },
                        onSubmitReview = { b, r, rev -> viewModel.submitReview(b, r, rev) }
                    )
                }
            }
        }
    }
}

@Composable
fun BookingList(
    bookings: List<Booking>,
    navController: NavController,
    onCancel: (String) -> Unit,
    onSubmitReview: (Booking, Int, String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(bookings) { booking ->
            if(!booking.status.contains("Cancelled"))
            BookingCard(booking, navController, onCancel, onSubmitReview)
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    navController: NavController,
    onCancel: (String) -> Unit,
    onSubmitReview: (Booking, Int, String) -> Unit
) {
    var showRatingDialog by remember { mutableStateOf(false) }

    if (showRatingDialog) {
        RatingDialog(
            booking = booking,
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, review ->
                onSubmitReview(booking, rating, review)
                showRatingDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getServiceIcon(booking.serviceName),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = booking.serviceName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "#${booking.bookingId.take(8).uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusChip(booking.status)
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            InfoRow(Icons.Default.Event, "${booking.date} • ${booking.time}")
            Spacer(modifier = Modifier.height(12.dp))
            InfoRow(Icons.Default.LocationOn, booking.address)

            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Total Price",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${booking.price}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (booking.status) {
                        "Pending", "Confirmed" -> {
                            OutlinedButton(
                                onClick = { onCancel(booking.bookingId) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                            ) {
                                Text("Cancel")
                            }
                        }
                        "In Progress", "Cleaner Assigned" -> {
                            Button(
                                onClick = { /* TODO */ },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Track")
                            }
                        }
                        "Completed" -> {
                            OutlinedButton(
                                onClick = {
                                    navController.navigate(
                                        Screen.ServiceDetail.createRoute(booking.serviceName)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Rebook")
                            }
                            Button(
                                onClick = { showRatingDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Rate")
                            }
                        }
                        else -> {
                            Button(
                                onClick = { /* TODO */ },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("View Details")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate Service: ${booking.serviceName}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        IconButton(onClick = { rating = starIndex }) {
                            Icon(
                                imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (starIndex <= rating) Color(0xFFFFB300) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Share your experience (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, reviewText) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Submit")
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
fun StatusChip(status: String) {
    val color = when (status) {
        "Completed" -> SuccessGreen
        "Cancelled" -> ErrorRed
        "In Progress" -> Color(0xFF2196F3)
        "Confirmed" -> WarningAmber
        "Cleaner Assigned" -> Color(0xFF9C27B0)
        else -> PrimaryBlue
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun LoadingShimmer() {
    Column(modifier = Modifier.padding(16.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Bookings Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your booking history will appear here once you make your first booking.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

fun getServiceIcon(serviceName: String): ImageVector {
    return when {
        serviceName.contains("House", ignoreCase = true) -> Icons.Default.HomeWork
        serviceName.contains("Office", ignoreCase = true) -> Icons.Default.Business
        serviceName.contains("Deep", ignoreCase = true) -> Icons.Default.CleaningServices
        serviceName.contains("Sofa", ignoreCase = true) -> Icons.Default.Weekend
        serviceName.contains("Kitchen", ignoreCase = true) -> Icons.Default.Countertops
        serviceName.contains("Bathroom", ignoreCase = true) -> Icons.Default.Bathtub
        else -> Icons.Default.CleaningServices
    }
}

fun getServiceIconByName(iconName: String): ImageVector {
    return when (iconName) {
        "HomeWork" -> Icons.Default.HomeWork
        "Business" -> Icons.Default.Business
        "CleaningServices" -> Icons.Default.CleaningServices
        "Weekend" -> Icons.Default.Weekend
        "Countertops" -> Icons.Default.Countertops
        "Bathtub" -> Icons.Default.Bathtub
        else -> Icons.Default.CleaningServices
    }
}
