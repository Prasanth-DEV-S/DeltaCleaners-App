package com.example.deltacleaners.ui.auth

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.deltacleaners.ui.navigation.Screen

// ─── Color Palette ────────────────────────────────────────────────────────────

private val PrimaryBlue   = Color(0xFF1565D8)
private val LightBlue     = Color(0xFFEAF3FF)
private val AccentBlue    = Color(0xFF4DA3FF)
private val DeepNavy      = Color(0xFF0D3B8E)
private val SkyBlue       = Color(0xFF6BBFFF)
private val White         = Color(0xFFFFFFFF)
private val TextDark      = Color(0xFF172B4D)
private val TextSecondary = Color(0xFF6B778C)
private val ErrorRed      = Color(0xFFE53935)
private val CardShadow    = Color(0xFF1565D8).copy(alpha = 0.12f)

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }

    // Screen enter animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) {
            navController.navigate(Screen.OtpVerification.route)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlue)
    ) {
        // Decorative blurred circles in background
        DecorativeBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Hero Header ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(
                    tween(600, easing = EaseOutCubic)
                ) { -60 }
            ) {
                PremiumHeroHeader()
            }

            // ── Login Card ───────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 200)) + slideInVertically(
                    tween(700, delayMillis = 200, easing = EaseOutCubic)
                ) { 80 }
            ) {
                LoginCard(
                    name = name,
                    onNameChange = { name = it },
                    phoneNumber = phoneNumber,
                    onPhoneChange = { if (it.length <= 10) phoneNumber = it },
                    countryCode = countryCode,
                    onCountryCodeChange = { countryCode = it },
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onContinueClick = {
                        val fullNumber = "$countryCode$phoneNumber"
                        viewModel.sendOtp(fullNumber, name, context as Activity)
                    },
                    isEnabled = phoneNumber.length == 10 && name.isNotBlank()
                )
            }

            // ── Footer ───────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 400))
            ) {
                FooterSection()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── Decorative Background Circles ────────────────────────────────────────────

@Composable
private fun DecorativeBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-right large orb
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 160.dp, y = (-60).dp)
                .blur(60.dp)
                .background(AccentBlue.copy(alpha = 0.18f), CircleShape)
        )
        // Bottom-left accent orb
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .blur(50.dp)
                .background(PrimaryBlue.copy(alpha = 0.10f), CircleShape)
        )
    }
}

// ─── Hero Header ──────────────────────────────────────────────────────────────

@Composable
private fun PremiumHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Gradient hero background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(
                    RoundedCornerShape(
                        bottomStart = 48.dp,
                        bottomEnd = 48.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepNavy, PrimaryBlue, AccentBlue.copy(alpha = 0.85f)),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        ) {
            // Subtle grid/dot pattern overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotSpacing = 28.dp.toPx()
                val dotRadius = 1.5.dp.toPx()
                val cols = (size.width / dotSpacing).toInt() + 1
                val rows = (size.height / dotSpacing).toInt() + 1
                for (col in 0..cols) {
                    for (row in 0..rows) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.07f),
                            radius = dotRadius,
                            center = Offset(col * dotSpacing, row * dotSpacing)
                        )
                    }
                }
            }

            // Floating decorative ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-30).dp)
                    .border(
                        width = 1.5.dp,
                        color = White.copy(alpha = 0.10f),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = 10.dp)
                    .border(
                        width = 1.dp,
                        color = White.copy(alpha = 0.07f),
                        shape = CircleShape
                    )
            )

            // App identity content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(top = 56.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CleaningServices,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delta Cleaners",
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                }

                // Headline
                Column {
                    Text(
                        text = "Spotless Spaces,\nHappy Places.",
                        color = White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 38.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Professional Home & Office Cleaning",
                        color = White.copy(alpha = 0.75f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 0.2.sp
                    )
                }
            }
        }

        // Trust badges strip pinned to bottom of hero
        TrustBadgesRow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
        )
    }
}

// ─── Trust Badges ─────────────────────────────────────────────────────────────

@Composable
private fun TrustBadgesRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = CardShadow,
                spotColor = CardShadow
            )
            .clip(RoundedCornerShape(20.dp))
            .background(White)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TrustBadge(emoji = "✅", label = "Verified\nPros")
        TrustBadgesDivider()
        TrustBadge(emoji = "⭐", label = "4.9 Rated\nService")
        TrustBadgesDivider()
        TrustBadge(emoji = "🔒", label = "Insured &\nBonded")
    }
}

@Composable
private fun TrustBadge(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun TrustBadgesDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(LightBlue)
    )
}

// ─── Login Card ───────────────────────────────────────────────────────────────

@Composable
private fun LoginCard(
    name: String,
    onNameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    countryCode: String,
    onCountryCodeChange: (String) -> Unit,
    isLoading: Boolean,
    error: String?,
    onContinueClick: () -> Unit,
    isEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        // Section heading
        Text(
            text = "Sign in to continue",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark,
            letterSpacing = (-0.3).sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Enter your details to book your next clean",
            fontSize = 13.sp,
            color = TextSecondary,
            letterSpacing = 0.1.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Name field
        PremiumTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Full Name",
            icon = Icons.Default.Person,
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Country code chip
            CountryCodeField(
                value = countryCode,
                onValueChange = onCountryCodeChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            PremiumTextField(
                value = phoneNumber,
                onValueChange = onPhoneChange,
                label = "Phone Number",
                icon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Character counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "${phoneNumber.length}/10",
                fontSize = 11.sp,
                color = if (phoneNumber.length == 10) PrimaryBlue else TextSecondary
            )
        }

        // Error message
        AnimatedVisibility(visible = error != null) {
            error?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRed.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(text = "⚠", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it,
                        color = ErrorRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // CTA Button
        PremiumCTAButton(
            text = "Continue",
            onClick = onContinueClick,
            isLoading = isLoading,
            enabled = isEnabled
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Divider
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            HorizontalDivider(
//                modifier = Modifier.weight(1f),
//                color = TextSecondary.copy(alpha = 0.18f)
//            )
//            Text(
//                text = "  We'll send an OTP  ",
//                fontSize = 12.sp,
//                color = TextSecondary
//            )
//            HorizontalDivider(
//                modifier = Modifier.weight(1f),
//                color = TextSecondary.copy(alpha = 0.18f)
//            )
//        }
    }
}

// ─── Premium Text Field ───────────────────────────────────────────────────────

@Composable
private fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryBlue else TextSecondary.copy(alpha = 0.25f),
        animationSpec = tween(200),
        label = "border"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryBlue else TextSecondary,
        animationSpec = tween(200),
        label = "label"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isFocused) PrimaryBlue else TextSecondary.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "icon"
    )
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 8.dp else 2.dp,
        animationSpec = tween(200),
        label = "elevation"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PrimaryBlue.copy(alpha = 0.08f),
                spotColor = PrimaryBlue.copy(alpha = 0.08f)
            ),
        label = { Text(label, color = labelColor, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        },
        interactionSource = interactionSource,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            cursorColor = PrimaryBlue
        )
    )
}

// ─── Country Code Field ───────────────────────────────────────────────────────

@Composable
private fun CountryCodeField(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .width(88.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = PrimaryBlue.copy(alpha = 0.06f)
            ),
        label = { Text("Code", fontSize = 12.sp, color = TextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = TextSecondary.copy(alpha = 0.25f),
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedTextColor = TextDark,
            unfocusedTextColor = TextDark,
            cursorColor = PrimaryBlue
        )
    )
}

// ─── Premium CTA Button ───────────────────────────────────────────────────────

@Composable
private fun PremiumCTAButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isLoading) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .height(58.dp)
            .shadow(
                elevation = if (enabled) 14.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = PrimaryBlue.copy(alpha = 0.35f),
                spotColor = PrimaryBlue.copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (enabled)
                    Brush.horizontalGradient(listOf(PrimaryBlue, AccentBlue))
                else
                    Brush.horizontalGradient(
                        listOf(
                            TextSecondary.copy(alpha = 0.3f),
                            TextSecondary.copy(alpha = 0.3f)
                        )
                    )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Shine overlay
        if (enabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                White.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                color = White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─── Footer ───────────────────────────────────────────────────────────────────

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // New user row
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Text(
//                text = "New to Delta Cleaners? ",
//                fontSize = 13.sp,
//                color = TextSecondary
//            )
//            Text(
//                text = "Sign Up",
//                fontSize = 13.sp,
//                fontWeight = FontWeight.Bold,
//                color = PrimaryBlue,
//                modifier = Modifier.clickable { /* navigate to sign up */ }
//            )
//        }

        Spacer(modifier = Modifier.height(20.dp))

        // Terms
        Text(
            text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
            fontSize = 11.sp,
            color = TextSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Need help
        Text(
            text = "Need help? Contact Support",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AccentBlue,
            modifier = Modifier.clickable { /* open support */ }
        )
    }
}