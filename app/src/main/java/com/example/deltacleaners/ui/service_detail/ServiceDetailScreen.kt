package com.example.deltacleaners.ui.service_detail

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.deltacleaners.ui.components.PrimaryButton
import com.example.deltacleaners.ui.navigation.Screen
import com.example.deltacleaners.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailScreen(
    navController: NavController,
    serviceName: String,
    viewModel: ServiceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(serviceName) {
        viewModel.initService(serviceName)
    }

    val icon = uiState.icon
    val iconModel = remember(icon) {
        if (icon.startsWith("http") || icon.contains("/") || icon.endsWith(".jpeg") || icon.endsWith(".png") || icon.endsWith(".jpg")) {
            if (icon.startsWith("http") || icon.contains("/")) {
                icon
            } else {
                val resourceName = icon.substringBeforeLast(".")
                val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                if (resId != 0) resId else icon
            }
        } else {
            icon
        }
    }

    Scaffold(
        bottomBar = {
            StickyBookingBar(
                price = uiState.calculatedPrice,
                duration = uiState.estimatedDuration,
                isCalculated = uiState.isPriceCalculated,
                onProceed = {
                    navController.navigate(Screen.Booking.createRoute(serviceName, uiState.calculatedPrice))
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {

            // 1. SERVICE HEADER
            item {
                ServiceHeader(
                    serviceName = serviceName,
                    bannerImage = uiState.bannerImage,
                    icon = iconModel,
                    startingPrice = uiState.basePrice,
                    duration = uiState.estimatedDuration,
                    rating = uiState.averageRating,
                    reviewCount = uiState.reviewCount,
                    onBack = { navController.popBackStack() }
                )
            }

            // 8. PROPERTY CONFIGURATION SECTION
            if (uiState.propertyOptions.isNotEmpty()) {
                item {
                    PropertyConfigurationSection(
                        selectedType = uiState.propertyType,
                        options = uiState.propertyOptions.keys.toList(),
                        bathrooms = uiState.bathrooms,
                        onTypeSelect = { viewModel.updatePropertyType(it) },
                        onBathroomChange = { viewModel.updateBathrooms(it) }
                    )
                }
            }

            // 2. WHAT’S INCLUDED SECTION
            if (uiState.included.isNotEmpty()) {
                item {
                    ExpandableSection(
                        title = "What's included",
                        isExpanded = uiState.inclusionsExpanded,
                        onToggle = { viewModel.toggleInclusions() },
                        icon = Icons.Default.CheckCircle,
                        iconColor = Color(0xFF4CAF50)
                    ) {
                        InclusionList(uiState.included)
                    }
                }
            }

            // 3. WHAT’S NOT INCLUDED SECTION
            if (uiState.notIncluded.isNotEmpty()) {
                item {
                    ExpandableSection(
                        title = "What's not included",
                        isExpanded = uiState.exclusionsExpanded,
                        onToggle = { viewModel.toggleExclusions() },
                        icon = Icons.Default.Cancel,
                        iconColor = MaterialTheme.colorScheme.error
                    ) {
                        ExclusionList(uiState.notIncluded)
                    }
                }
            }

            // 5. FAQ SECTION
            if (uiState.faq.isNotEmpty()) {
                item {
                    FAQSection(
                        faqs = uiState.faq,
                        expandedIndex = uiState.expandedFaqIndex,
                        onToggle = { viewModel.toggleFaq(it) }
                    )
                }
            }

            // 6. CUSTOMER REVIEWS SECTION
            item {
                ReviewsSection(uiState.reviews)
            }

            // Price Summary Card
            item {
                PriceSummaryCard(
                    serviceName = serviceName,
                    propertyType = uiState.propertyType,
                    bathrooms = uiState.bathrooms,
                    basePrice = uiState.propertyOptions[uiState.propertyType] ?: uiState.basePrice,
                    totalPrice = uiState.calculatedPrice
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ServiceHeader(
    serviceName: String,
    bannerImage: String,
    icon: Any,
    startingPrice: Double,
    duration: String,
    rating: Double,
    reviewCount: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    Box(modifier = Modifier.height(280.dp)) {
        // Banner Image
        if (bannerImage.isNotEmpty()) {
            val bannerModel = remember(bannerImage) {
                if (bannerImage.startsWith("http") || bannerImage.contains("/")) {
                    bannerImage
                } else {
                    val resourceName = bannerImage.substringBeforeLast(".")
                    val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                    if (resId != 0) resId else bannerImage
                }
            }
            
            AsyncImage(
                model = icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay gradient for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }

        // Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Header Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Section
//                Box(
//                    modifier = Modifier
//                        .size(64.dp)
//                        .clip(RoundedCornerShape(16.dp))
//                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
//                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (icon is String && !icon.startsWith("http") && !icon.contains("/") && !icon.endsWith(".jpeg") && !icon.endsWith(".png") && !icon.endsWith(".jpg")) {
//                        Icon(
//                            imageVector = getServiceIcon(icon),
//                            contentDescription = null,
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(32.dp)
//                        )
//                    } else {
//                        AsyncImage(
//                            model = icon,
//                            contentDescription = null,
//                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = serviceName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Star, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(14.dp), 
                                    tint = Color(0xFFFFB400)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val ratingText = if (rating > 0) String.format("%.1f", rating) else "New"
                                val countText = if (reviewCount > 0) " ($reviewCount)" else ""
                                Text("$ratingText$countText", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp), 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Starts at ₹${startingPrice.toInt()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            Icons.Default.Timer, 
                            contentDescription = null, 
                            modifier = Modifier.size(16.dp), 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(duration, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyConfigurationSection(
    selectedType: String,
    options: List<String>,
    bathrooms: Int,
    onTypeSelect: (String) -> Unit,
    onBathroomChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Select Property Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Dynamic options grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            options.forEach { type ->
                val isSelected = selectedType == type
                Surface(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clickable { onTypeSelect(type) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Bathrooms", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "₹300 per additional bathroom", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            QuantitySelector(
                count = bathrooms,
                onIncrement = { onBathroomChange(bathrooms + 1) },
                onDecrement = { if (bathrooms > 1) onBathroomChange(bathrooms - 1) }
            )
        }
    }
}

@Composable
fun QuantitySelector(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(
            text = count.toString(),
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "rotation")

    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onToggle() }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun InclusionList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row {
                Text("•", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun ExclusionList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row {
                Text("✕", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FAQSection(
    faqs: List<com.example.deltacleaners.data.model.FaqItem>,
    expandedIndex: Int?,
    onToggle: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Frequently Asked Questions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        faqs.forEachIndexed { index, faq ->
            FAQItem(
                question = faq.question,
                answer = faq.answer,
                isExpanded = expandedIndex == index,
                onToggle = { onToggle(index) }
            )
            if (index < faqs.size - 1) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FAQItem(
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .clickable { onToggle() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = question, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = isExpanded) {
                Text(
                    text = answer,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ReviewsSection(reviews: List<com.example.deltacleaners.data.model.Review>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Customer Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (reviews.isNotEmpty()) {
                TextButton(onClick = { }) {
                    Text("View All")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        if (reviews.isEmpty()) {
            Text(
                text = "No reviews yet for this service.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            reviews.take(3).forEachIndexed { index, review ->
                ReviewCard(review)
                if (index < reviews.take(3).size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ReviewCard(review: com.example.deltacleaners.data.model.Review) {
    val dateString = remember(review.createdAt) {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
        val date = review.createdAtTimestamp?.toDate() ?: java.util.Date()
        sdf.format(date)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = review.userName.takeIf { it.isNotEmpty() }?.take(1) ?: "U",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = review.userName.ifEmpty { "User" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (index < review.rating.toInt()) Color(0xFFFFB400) else Color.LightGray
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateString, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = review.review, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun PriceSummaryCard(
    serviceName: String,
    propertyType: String,
    bathrooms: Int,
    basePrice: Double,
    totalPrice: Double
) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Price Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            PriceRow(label = "$serviceName ($propertyType)", value = "₹${basePrice.toInt()}")
            if (bathrooms > 1) {
                PriceRow(label = "Additional Bathrooms (x${bathrooms - 1})", value = "₹${(bathrooms - 1) * 300}")
            }
            PriceRow(label = "Taxes & Fees", value = "Included")
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("₹${totalPrice.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun PriceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Helper: map icon name → ImageVector ──────────────────────────────────────

private fun getServiceIcon(iconName: String): ImageVector = when (iconName.lowercase()) {
    "house_cleaning", "home"       -> Icons.Outlined.Home
    "deep_cleaning"                -> Icons.Outlined.CleaningServices
    "bathroom"                     -> Icons.Outlined.Bathroom
    "sofa", "furniture"            -> Icons.Outlined.Chair
    "kitchen"                      -> Icons.Outlined.Kitchen
    "office"                       -> Icons.Outlined.Business
    "carpet"                       -> Icons.Outlined.Layers
    "window"                       -> Icons.Outlined.Window
    "laundry"                      -> Icons.Outlined.LocalLaundryService
    "pest"                         -> Icons.Outlined.BugReport
    else                           -> Icons.Outlined.CleaningServices
}

@Composable
fun StickyBookingBar(
    price: Double,
    duration: String,
    isCalculated: Boolean,
    onProceed: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (isCalculated) {
                    Text(text = "₹${price.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(text = "Est. Duration: $duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(text = "Select details", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            PrimaryButton(
                text = "Proceed to Booking",
                onClick = onProceed,
                modifier = Modifier.width(200.dp),
                enabled = isCalculated
            )
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Service Header - Light")
@Composable
private fun ServiceHeaderPreview() {
    DeltaCleanersTheme {
        ServiceHeader(
            serviceName = "Full House Cleaning",
            bannerImage = "clean_house.jpeg", // Mock local resource
            icon = "clean_house.jpeg",
            startingPrice = 1499.0,
            duration = "4-5 hours",
            rating = 4.8,
            reviewCount = 124,
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Booking Bar - Calculated")
@Composable
private fun StickyBookingBarPreview() {
    DeltaCleanersTheme {
        StickyBookingBar(
            price = 1799.0,
            duration = "4 hours",
            isCalculated = true,
            onProceed = {}
        )
    }
}

@Preview(showBackground = true, name = "Price Summary")
@Composable
private fun PriceSummaryPreview() {
    DeltaCleanersTheme {
        PriceSummaryCard(
            serviceName = "Deep Cleaning",
            propertyType = "2 BHK",
            bathrooms = 2,
            basePrice = 1200.0,
            totalPrice = 1500.0
        )
    }
}
