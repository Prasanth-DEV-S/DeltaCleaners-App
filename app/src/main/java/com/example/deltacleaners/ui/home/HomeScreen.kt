package com.example.deltacleaners.ui.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.deltacleaners.data.model.Banner
import com.example.deltacleaners.data.model.Service
import com.example.deltacleaners.ui.navigation.Screen
import kotlinx.coroutines.delay

// ─── Brand Colors ─────────────────────────────────────────────────────────────

private val PrimaryBlue   = Color(0xFF1565D8)
private val SoftBlue      = Color(0xFF4DA3FF)
private val DeepNavy      = Color(0xFF0D3B8E)
private val LightBlueBg   = Color(0xFFEAF3FF)
private val White         = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF172B4D)
private val TextSecondary = Color(0xFF6B778C)
private val BorderColor   = Color(0xFFDDE7F0)
private val CardShadow    = Color(0xFF1565D8).copy(alpha = 0.08f)
private val GreenBadge    = Color(0xFF27AE60)


// ─── HomeScreen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val services by viewModel.filteredServices.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddressSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    // Entrance animations
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.fetchLocation()
        }
    }

    if (showAddressSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddressSheet = false },
            sheetState = sheetState,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(BorderColor)
                )
            },
            containerColor = White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            tonalElevation = 0.dp
        ) {
            PremiumAddressSheet(
                currentAddress = uiState.selectedAddress,
                isLoading = uiState.isLocationLoading,
                onUseCurrentLocation = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onConfirm = { address ->
                    viewModel.updateSelectedAddress(address)
                    showAddressSheet = false
                }
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = LightBlueBg,
        topBar = {
            PremiumTopBar(
                address = uiState.selectedAddress,
                onAddressClick = { showAddressSheet = true },
                onNotificationClick = { navController.navigate(Screen.Notifications.route) }
            )
        },
        floatingActionButton = {
            val context = androidx.compose.ui.platform.LocalContext.current
            FloatingActionButton(
                onClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:+919345540171")
                    }
                    context.startActivity(intent)
                },
                containerColor = PrimaryBlue,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 24.dp, end = 20.dp) // Based on suggested spacing for premium look
                    .size(64.dp)
                    .shadow(12.dp, CircleShape, ambientColor = PrimaryBlue, spotColor = PrimaryBlue)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call Support",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 86.dp) // Increased to prevent FAB from blocking the last row
        ) {
            // ── Search Bar ───────────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -20 }
                ) {
                    PremiumSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }
            }

            // ── Promo Banner (only when not searching) ───────────────────────
            if (uiState.searchQuery.isEmpty() && uiState.banners.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(tween(600, 100)) + slideInVertically(tween(600, 100)) { 30 }
                    ) {
                        val pagerState = rememberPagerState(pageCount = { uiState.banners.size })
                        
                        // Auto-scroll logic
                        LaunchedEffect(Unit) {
                            while (true) {
                                delay(5000)
                                if (uiState.banners.size > 1) {
                                    val nextPage = (pagerState.currentPage + 1) % uiState.banners.size
                                    pagerState.animateScrollToPage(nextPage)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                pageSpacing = 16.dp
                            ) { page ->
                                PremiumPromoBanner(
                                    banner = uiState.banners[page],
                                    onClick = {
                                        val banner = uiState.banners[page]
                                        if (banner.bannerId.isNotEmpty()) {
                                            navController.navigate(Screen.ServiceDetail.createRoute(banner.bannerId))
                                        }
                                    }
                                )
                            }
                            
                            if (uiState.banners.size > 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(uiState.banners.size) { iteration ->
                                        val color = if (pagerState.currentPage == iteration) PrimaryBlue else PrimaryBlue.copy(alpha = 0.2f)
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 3.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .size(if (pagerState.currentPage == iteration) 8.dp else 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Section Header ───────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(tween(600, 250))
                ) {
                    PremiumSectionHeader(
                        title = if (uiState.searchQuery.isEmpty()) "Professional Services" else "Results for \"${uiState.searchQuery}\"",
                        subtitle = if (uiState.searchQuery.isEmpty()) "Choose from our top-rated services" else null,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                }
            }

            // ── Services Grid ────────────────────────────────────────────────
            if (uiState.isServicesLoading) {
                items(2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        repeat(2) {
                            ServiceCardSkeleton(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else if (services.isEmpty()) {
                item {
                    EmptyServicesState(query = uiState.searchQuery)
                }
            } else {
                items(services.chunked(2)) { rowServices ->
                    AnimatedVisibility(
                        visible = contentVisible,
                        enter = fadeIn(tween(700, 300)) + slideInVertically(tween(700, 300)) { 40 }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            rowServices.forEach { service ->
                                PremiumServiceCard(
                                    service = service,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        navController.navigate(Screen.ServiceDetail.createRoute(service.name))
                                    }
                                )
                            }
                            if (rowServices.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Premium Top Bar ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumTopBar(
    address: String,
    onAddressClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Surface(
        color = White,
        shadowElevation = 4.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left — address selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onAddressClick)
                    .padding(vertical = 4.dp, horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LightBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Deliver to",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue,
                        letterSpacing = 0.3.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = address,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 180.dp)
                        )
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Right — notification bell
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(LightBlueBg)
                    .clickable(onClick = onNotificationClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
                // Notification dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = 2.dp)
                )
            }
        }
    }
}

// ─── Search Bar ───────────────────────────────────────────────────────────────

@Composable
private fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Use collectIsFocusedAsState extension from interaction
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 10.dp else 4.dp,
        animationSpec = tween(200),
        label = "search_elevation"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryBlue.copy(alpha = 0.5f) else BorderColor,
        animationSpec = tween(200),
        label = "search_border"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, RoundedCornerShape(20.dp), ambientColor = CardShadow, spotColor = CardShadow),
        shape = RoundedCornerShape(20.dp),
        color = White,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Search cleaning services...",
                    fontSize = 14.sp,
                    color = TextSecondary.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isFocused) PrimaryBlue else TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                cursorColor = PrimaryBlue
            ),
            singleLine = true
        )
    }
}

// ─── Promo Banner ─────────────────────────────────────────────────────────────

@Composable
private fun PremiumPromoBanner(
    banner: Banner?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(188.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(DeepNavy, PrimaryBlue, SoftBlue.copy(alpha = 0.9f)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            // Background Image if available
            banner?.imageUrl?.let { imageUrl ->
                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.4f
                    )
                }
            }

            // Dot texture
            Canvas(Modifier.fillMaxSize()) {
                val sp = 24.dp.toPx()
                val r = 1.4.dp.toPx()
                val cols = (size.width / sp).toInt() + 1
                val rows = (size.height / sp).toInt() + 1
                for (c in 0..cols) for (row in 0..rows) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = r,
                        center = Offset(c * sp, row * sp)
                    )
                }
            }
            // Decorative ring
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 60.dp)
                    .border(1.5.dp, White.copy(alpha = 0.10f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 30.dp)
                    .border(1.dp, White.copy(alpha = 0.07f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 24.dp, top = 22.dp, bottom = 22.dp)
                    .fillMaxWidth(0.85f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Badge
                Surface(
                    color = White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "🎉  LIMITED OFFER",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = White,
                        letterSpacing = 0.8.sp
                    )
                }

                Column {
                    Text(
                        text = banner?.title ?: "Flat 30% OFF",
                        fontSize = if ((banner?.title?.length ?: 0) > 15) 28.sp else 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = White,
                        lineHeight = if ((banner?.title?.length ?: 0) > 15) 30.sp else 36.sp,
                        letterSpacing = (-1).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = banner?.subtitle ?: "on your first home cleaning",
                        fontSize = 13.sp,
                        color = White.copy(alpha = 0.85f),
                        lineHeight = 18.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // CTA
                Surface(
                    onClick = onClick,
                    color = White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Book Now  →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}

// ─── Quick Actions ────────────────────────────────────────────────────────────

private data class QuickAction(val icon: ImageVector, val label: String, val tint: Color)

@Composable
private fun QuickActionsRow(modifier: Modifier = Modifier) {
    val actions = listOf(
        QuickAction(Icons.Outlined.Replay, "Book Again", PrimaryBlue),
        QuickAction(Icons.Outlined.BookmarkBorder, "My Bookings", Color(0xFF9C27B0)),
        QuickAction(Icons.Outlined.LocalOffer, "Offers", Color(0xFFE65100)),
        QuickAction(Icons.Outlined.HeadsetMic, "Support", Color(0xFF00897B))
    )

    Column(modifier = modifier) {
        Text(
            text = "Quick Actions",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(actions) { action ->
                QuickActionChip(action = action)
            }
        }
    }
}

@Composable
private fun QuickActionChip(action: QuickAction) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = action.tint.copy(0.15f), spotColor = action.tint.copy(0.15f))
                .clip(RoundedCornerShape(18.dp))
                .background(White)
                .clickable(interactionSource = interactionSource, indication = null) {},
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(action.tint.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.label,
                    tint = action.tint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = action.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun PremiumSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark,
            letterSpacing = (-0.4).sp
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = it,
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// ─── Service Card ─────────────────────────────────────────────────────────────

@Composable
fun PremiumServiceCard(
    service: Service,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 7.dp,
        animationSpec = tween(150),
        label = "card_elev"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(28.dp),
                ambientColor = CardShadow,
                spotColor = CardShadow
            )
            .clip(RoundedCornerShape(28.dp))
            .background(White)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 16.dp, start = 12.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(68.dp) // Increased from 68.dp (approx 35% increase)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(LightBlueBg, LightBlueBg.copy(alpha = 0.4f))
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        color = BorderColor.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val icon = service.icon
                val context = LocalContext.current
                
                if (icon.startsWith("http") || icon.contains("/") || icon.endsWith(".jpeg") || icon.endsWith(".png") || icon.endsWith(".jpg")) {
                    val model = remember(icon) {
                        if (icon.startsWith("http") || icon.contains("/")) {
                            icon
                        } else {
                            // Try to find local drawable resource
                            val resourceName = icon.substringBeforeLast(".")
                            val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                            if (resId != 0) resId else icon
                        }
                    }
                    
                    AsyncImage(
                        model = model,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = getServiceIcon(icon),
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

//            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = service.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = TextDark,
                lineHeight = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.heightIn(min = 38.dp) // Ensures consistent height for name section
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Starts at ₹${service.startingPrice.toInt()}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Rating badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0FFF6))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = GreenBadge,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (service.rating > 0) String.format("%.1f", service.rating) else "New",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenBadge
                )
                if (service.totalReviews > 0) {
                    Text(
                        text = " (${service.totalReviews})",
                        fontSize = 10.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}

// ─── Skeleton Loader ──────────────────────────────────────────────────────────

@Composable
@Preview
private fun ServiceCardSkeleton(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color(0xFFE8F0FC),
        Color(0xFFF5F8FF),
        Color(0xFFE8F0FC)
    )
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Restart),
        label = "shimmer_offset"
    )

    Box(
        modifier = modifier
            .height(210.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(offset, 0f),
                    end = Offset(offset + 300f, 300f)
                )
            )
    )
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
private fun EmptyServicesState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = BorderColor
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (query.isEmpty()) "No services available" else "No results for \"$query\"",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (query.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Try a different search term",
                fontSize = 13.sp,
                color = TextSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Address Bottom Sheet ─────────────────────────────────────────────────────

@Composable
private fun PremiumAddressSheet(
    currentAddress: String,
    isLoading: Boolean,
    onUseCurrentLocation: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var addressInput by remember { mutableStateOf(currentAddress) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(currentAddress) { addressInput = currentAddress }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightBlueBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Delivery Address",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    "Choose where we should send our team",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        HorizontalDivider(color = BorderColor)

        // Address input
        OutlinedTextField(
            value = addressInput,
            onValueChange = { addressInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    "Enter your full address",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 14.sp
                )
            },
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryBlue)
            },
            trailingIcon = {
                if (addressInput.isNotEmpty()) {
                    IconButton(onClick = { addressInput = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                cursorColor = PrimaryBlue
            ),
            singleLine = false,
            maxLines = 3
        )

        // Action row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Current location
            OutlinedButton(
                onClick = onUseCurrentLocation,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = PrimaryBlue,
                    containerColor = LightBlueBg
                ),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryBlue
                    )
                } else {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use GPS", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Manual entry
            OutlinedButton(
                onClick = { focusRequester.requestFocus() },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary,
                    containerColor = White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(Icons.Default.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Type It", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Confirm CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .shadow(if (addressInput.isNotBlank()) 12.dp else 0.dp, RoundedCornerShape(16.dp),
                    ambientColor = PrimaryBlue.copy(0.3f), spotColor = PrimaryBlue.copy(0.3f))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (addressInput.isNotBlank())
                        Brush.horizontalGradient(listOf(PrimaryBlue, SoftBlue))
                    else
                        Brush.horizontalGradient(listOf(TextSecondary.copy(0.3f), TextSecondary.copy(0.3f)))
                )
                .clickable(enabled = addressInput.isNotBlank()) {
                    focusManager.clearFocus()
                    onConfirm(addressInput)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Confirm Address",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                letterSpacing = 0.3.sp
            )
        }
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

// Extension for focus collected from interaction
@Composable
private fun MutableInteractionSource.collectIsFocusedAsState(): State<Boolean> {
    val focused = remember { mutableStateOf(false) }
    LaunchedEffect(this) {
        interactions.collect { interaction ->
            when (interaction) {
                is androidx.compose.foundation.interaction.FocusInteraction.Focus   -> focused.value = true
                is androidx.compose.foundation.interaction.FocusInteraction.Unfocus -> focused.value = false
            }
        }
    }
    return focused
}
