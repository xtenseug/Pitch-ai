package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.RetrofitClient
import com.example.data.local.PredictionEntity
import com.example.data.model.PredictionResponse
import com.example.ui.viewmodel.MatchTemplate
import com.example.ui.viewmodel.UpcomingMatch
import com.example.ui.viewmodel.PredictionUiState
import com.example.ui.viewmodel.PredictionViewModel
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

// Styling Color Palette - Professional Polish Light Theme
val ProfessionalBg = Color(0xFFFDF8F6)          // Warm cream white
val ProfessionalCardBg = Color(0xFFF3EDF7)      // Soft lavender grey
val ProfessionalAccentBg = Color(0xFFECE0DB)    // Warm clay tint
val ProfessionalText = Color(0xFF1C1B1F)        // Near black body text
val ProfessionalSecondaryText = Color(0xFF49454F) // Medium charcoal grey
val ProfessionalPurple = Color(0xFF6750A4)       // Core deep violet
val ProfessionalBorder = Color(0xFFE6E0E9)       // Subtle lavender divider

// Soft Status Colors matching design HTML
val SoftGreenBg = Color(0xFFDCFCE7)
val SoftGreenText = Color(0xFF15803D)
val SoftBlueBg = Color(0xFFDBEAFE)
val SoftBlueText = Color(0xFF1E3A8A)
val SoftOrangeBg = Color(0xFFFFEDD5)
val SoftOrangeText = Color(0xFF9A3412)

// Soft Badge Status Colors for Form History (W-D-L)
val FormWinBg = Color(0xFFDCFCE7)
val FormWinText = Color(0xFF15803D)
val FormDrawBg = Color(0xFFFEF9C3)
val FormDrawText = Color(0xFFA16207)
val FormLossBg = Color(0xFFFEE2E2)
val FormLossText = Color(0xFFB91C1C)

fun getBase64FromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    viewModel: PredictionViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.predictionHistory.collectAsStateWithLifecycle()

    val homeTeam by viewModel.homeTeam.collectAsStateWithLifecycle()
    val awayTeam by viewModel.awayTeam.collectAsStateWithLifecycle()
    val homeForm by viewModel.homeForm.collectAsStateWithLifecycle()
    val awayForm by viewModel.awayForm.collectAsStateWithLifecycle()
    val injuries by viewModel.injuries.collectAsStateWithLifecycle()
    val context by viewModel.context.collectAsStateWithLifecycle()

    val homeWins by viewModel.homeWins.collectAsStateWithLifecycle()
    val awayWins by viewModel.awayWins.collectAsStateWithLifecycle()
    val draws by viewModel.draws.collectAsStateWithLifecycle()
    val homeAvgGoals by viewModel.homeAvgGoals.collectAsStateWithLifecycle()
    val awayAvgGoals by viewModel.awayAvgGoals.collectAsStateWithLifecycle()
    val recentEncounters by viewModel.recentEncounters.collectAsStateWithLifecycle()

    val weatherCondition by viewModel.weatherCondition.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val precipitation by viewModel.precipitation.collectAsStateWithLifecycle()
    val windSpeed by viewModel.windSpeed.collectAsStateWithLifecycle()

    val betslipAnalysisState by viewModel.betslipAnalysisState.collectAsStateWithLifecycle()
    val isAnalyzingBetslip by viewModel.isAnalyzingBetslip.collectAsStateWithLifecycle()

    var showHistoryDialog by remember { mutableStateOf(false) }
    var h2hExpanded by remember { mutableStateOf(false) }
    var weatherExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsSoccer,
                            contentDescription = "Soccer Ball Icon",
                            tint = ProfessionalPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "Machine Learning Core",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = ProfessionalPurple,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Pitch AI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ProfessionalText
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHistoryDialog = true },
                        modifier = Modifier.testTag("history_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (history.isNotEmpty()) {
                                    Badge(containerColor = ProfessionalPurple) {
                                        Text(
                                            text = history.size.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "Prediction History",
                                tint = ProfessionalPurple
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfessionalBg,
                    titleContentColor = ProfessionalText
                )
            )
        },
        containerColor = ProfessionalBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero Banner Illustration
            item {
                HeroBanner()
            }

            // Upcoming Matches Dashboard (Next 7 Days)
            item {
                UpcomingMatchesDashboard(
                    viewModel = viewModel,
                    onPredictNowClick = { match ->
                        viewModel.getPrediction()
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Quick Match Templates Row
            item {
                Text(
                    text = "Quick Match Templates",
                    color = ProfessionalSecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(viewModel.templates) { template ->
                        FilterChip(
                            selected = homeTeam == template.homeTeam && awayTeam == template.awayTeam,
                            onClick = { viewModel.applyTemplate(template) },
                            label = { Text(template.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = ProfessionalSecondaryText,
                                selectedContainerColor = ProfessionalPurple,
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (homeTeam == template.homeTeam && awayTeam == template.awayTeam) ProfessionalPurple else ProfessionalBorder
                            ),
                            modifier = Modifier.testTag("template_${template.homeTeam}_chip")
                        )
                    }
                }
            }

            // Betslip Visual Analysis Card
            item {
                val context = LocalContext.current
                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    if (uri != null) {
                        val base64 = getBase64FromUri(context, uri)
                        if (base64 != null) {
                            viewModel.analyzeUploadedBetslip(base64)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("betslip_analysis_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, ProfessionalBorder)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = "Scan Betslip",
                                tint = ProfessionalPurple,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Betslip Visual Analysis",
                                color = ProfessionalText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Upload a screenshot or photo of your betslip to instantly parse match parameters, select markets, and review expert AI recommendations.",
                            color = ProfessionalSecondaryText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f).height(44.dp).testTag("upload_betslip_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ProfessionalPurple,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Upload Betslip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Simulation Button to make container testing flawless
                            OutlinedButton(
                                onClick = {
                                    // Simulated betslip photo analysis using a real API call via standard black pixel Base64
                                    viewModel.analyzeUploadedBetslip("/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA=")
                                },
                                modifier = Modifier.weight(1f).height(44.dp).testTag("simulate_betslip_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ProfessionalPurple),
                                border = BorderStroke(1.dp, ProfessionalPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Simulate Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (isAnalyzingBetslip) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(color = ProfessionalPurple, modifier = Modifier.fillMaxWidth())
                                Text(
                                    text = "Processing and extracting parameters...",
                                    color = ProfessionalSecondaryText,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        betslipAnalysisState?.let { result ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ProfessionalBg)
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Extracted Intelligence Result:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = ProfessionalPurple
                                        )
                                        IconButton(
                                            onClick = { viewModel.clearBetslipAnalysis() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = ProfessionalSecondaryText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = result,
                                        color = ProfessionalText,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )

                                    if (!isAnalyzingBetslip && !result.startsWith("Error")) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SoftGreenBg)
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = SoftGreenText,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Match parameters auto-extracted!",
                                                color = SoftGreenText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Form inputs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ProfessionalCardBg),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, ProfessionalBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Match Parameters",
                            color = ProfessionalPurple,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Teams Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = homeTeam,
                                onValueChange = { viewModel.homeTeam.value = it },
                                label = { Text("Home Team") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = ProfessionalPurple,
                                    unfocusedBorderColor = ProfessionalBorder,
                                    focusedLabelColor = ProfessionalPurple,
                                    unfocusedLabelColor = ProfessionalSecondaryText,
                                    focusedTextColor = ProfessionalText,
                                    unfocusedTextColor = ProfessionalText
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("home_team_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = awayTeam,
                                onValueChange = { viewModel.awayTeam.value = it },
                                label = { Text("Away Team") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = ProfessionalPurple,
                                    unfocusedBorderColor = ProfessionalBorder,
                                    focusedLabelColor = ProfessionalPurple,
                                    unfocusedLabelColor = ProfessionalSecondaryText,
                                    focusedTextColor = ProfessionalText,
                                    unfocusedTextColor = ProfessionalText
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("away_team_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Recent Form Inputs with Instant Badge Preview
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = homeForm,
                                onValueChange = { viewModel.homeForm.value = it },
                                label = { Text("Home Form (e.g. W-W-D-L-W)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = ProfessionalPurple,
                                    unfocusedBorderColor = ProfessionalBorder,
                                    focusedLabelColor = ProfessionalPurple,
                                    unfocusedLabelColor = ProfessionalSecondaryText,
                                    focusedTextColor = ProfessionalText,
                                    unfocusedTextColor = ProfessionalText
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("home_form_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            FormBadgePreview(formString = viewModel.homeForm, teamName = homeTeam)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = awayForm,
                                onValueChange = { viewModel.awayForm.value = it },
                                label = { Text("Away Form (e.g. L-W-W-D-W)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = ProfessionalPurple,
                                    unfocusedBorderColor = ProfessionalBorder,
                                    focusedLabelColor = ProfessionalPurple,
                                    unfocusedLabelColor = ProfessionalSecondaryText,
                                    focusedTextColor = ProfessionalText,
                                    unfocusedTextColor = ProfessionalText
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("away_form_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            FormBadgePreview(formString = viewModel.awayForm, teamName = awayTeam)
                        }

                        // Injury and Suspensions Box
                        OutlinedTextField(
                            value = injuries,
                            onValueChange = { viewModel.injuries.value = it },
                            label = { Text("Key Player Injuries & Team News") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = ProfessionalPurple,
                                unfocusedBorderColor = ProfessionalBorder,
                                focusedLabelColor = ProfessionalPurple,
                                unfocusedLabelColor = ProfessionalSecondaryText,
                                focusedTextColor = ProfessionalText,
                                unfocusedTextColor = ProfessionalText
                            ),
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("injuries_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Context Stakes Box
                        OutlinedTextField(
                            value = context,
                            onValueChange = { viewModel.context.value = it },
                            label = { Text("Match Importance & Context") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = ProfessionalPurple,
                                unfocusedBorderColor = ProfessionalBorder,
                                focusedLabelColor = ProfessionalPurple,
                                unfocusedLabelColor = ProfessionalSecondaryText,
                                focusedTextColor = ProfessionalText,
                                unfocusedTextColor = ProfessionalText
                            ),
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("context_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Collapsible Head-to-Head Statistics Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ProfessionalBorder)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { h2hExpanded = !h2hExpanded }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.BarChart,
                                            contentDescription = "H2H",
                                            tint = ProfessionalPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Head-to-Head Records (Optional)",
                                            color = ProfessionalText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = if (h2hExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle H2H",
                                        tint = ProfessionalSecondaryText
                                    )
                                }

                                AnimatedVisibility(visible = h2hExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // WLD Wins Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = homeWins,
                                                onValueChange = { viewModel.homeWins.value = it },
                                                label = { Text("Home Wins") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1f).testTag("home_wins_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = draws,
                                                onValueChange = { viewModel.draws.value = it },
                                                label = { Text("Draws") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1f).testTag("draws_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = awayWins,
                                                onValueChange = { viewModel.awayWins.value = it },
                                                label = { Text("Away Wins") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1f).testTag("away_wins_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                        }

                                        // Average Goals Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = homeAvgGoals,
                                                onValueChange = { viewModel.homeAvgGoals.value = it },
                                                label = { Text("Home Avg Goals") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1f).testTag("home_avg_goals_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = awayAvgGoals,
                                                onValueChange = { viewModel.awayAvgGoals.value = it },
                                                label = { Text("Away Avg Goals") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1f).testTag("away_avg_goals_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                        }

                                        // Recent encounters (scores / info)
                                        OutlinedTextField(
                                            value = recentEncounters,
                                            onValueChange = { viewModel.recentEncounters.value = it },
                                            label = { Text("Recent Encounters (e.g. Madrid 3-2 Barca)") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = ProfessionalPurple,
                                                unfocusedBorderColor = ProfessionalBorder,
                                                focusedTextColor = ProfessionalText,
                                                unfocusedTextColor = ProfessionalText
                                            ),
                                            modifier = Modifier.fillMaxWidth().testTag("recent_encounters_input"),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Collapsible Weather Forecast Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ProfessionalBorder)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { weatherExpanded = !weatherExpanded }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Cloud,
                                            contentDescription = "Weather",
                                            tint = ProfessionalPurple,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Weather Conditions (Optional)",
                                            color = ProfessionalText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Icon(
                                        imageVector = if (weatherExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle Weather",
                                        tint = ProfessionalSecondaryText
                                    )
                                }

                                AnimatedVisibility(visible = weatherExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = weatherCondition,
                                                onValueChange = { viewModel.weatherCondition.value = it },
                                                label = { Text("Condition (e.g. Clear, Rain)") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1.2f).testTag("weather_condition_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = temperature,
                                                onValueChange = { viewModel.temperature.value = it },
                                                label = { Text("Temp (e.g. 15°C)") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(0.8f).testTag("temperature_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = precipitation,
                                                onValueChange = { viewModel.precipitation.value = it },
                                                label = { Text("Precipitation (e.g. 20%)") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1f).testTag("precipitation_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = windSpeed,
                                                onValueChange = { viewModel.windSpeed.value = it },
                                                label = { Text("Wind (e.g. 10 km/h)") },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = ProfessionalPurple,
                                                    unfocusedBorderColor = ProfessionalBorder,
                                                    focusedTextColor = ProfessionalText,
                                                    unfocusedTextColor = ProfessionalText
                                                ),
                                                modifier = Modifier.weight(1f).testTag("wind_speed_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Predict action button
                        Button(
                            onClick = { viewModel.getPrediction() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfessionalPurple,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "AI Magic Sparkles"
                                )
                                Text(
                                    text = "ANALYZE WITH AI ENGINE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            // Results UI Area
            item {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                    },
                    label = "PredictionUIStateAnimation"
                ) { state ->
                    when (state) {
                        is PredictionUiState.Idle -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Analytics,
                                        contentDescription = "Analysis Idle Icon",
                                        tint = ProfessionalSecondaryText,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Awaiting match analysis parameters...",
                                        color = ProfessionalText,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Fill details or select a template above and click Analyze.",
                                        color = ProfessionalSecondaryText,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        is PredictionUiState.Loading -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, ProfessionalBorder)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = ProfessionalPurple,
                                        modifier = Modifier.size(56.dp),
                                        strokeWidth = 5.dp
                                    )
                                    Text(
                                        text = "CALCULATING PROBABILITIES...",
                                        fontWeight = FontWeight.Bold,
                                        color = ProfessionalPurple,
                                        fontSize = 15.sp,
                                        letterSpacing = 2.sp
                                    )
                                    Text(
                                        text = "Synthesizing head-to-head logs, player form matrices, and team sheets with Pitch AI's expert prediction weights...",
                                        color = ProfessionalSecondaryText,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        is PredictionUiState.Success -> {
                            val currentId by viewModel.currentPredictionId.collectAsStateWithLifecycle()
                            PredictionResultsCard(
                                response = state.response,
                                homeTeamName = homeTeam,
                                awayTeamName = awayTeam,
                                currentPredictionId = currentId,
                                onSubmitFeedback = { id, isAccurate, comment ->
                                    viewModel.submitPredictionFeedback(id, isAccurate, comment)
                                },
                                onDismiss = { viewModel.resetPredictionState() }
                            )
                        }
                        is PredictionUiState.Error -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(1.dp, Color(0xFFF87171))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = "Error Icon",
                                        tint = Color(0xFFB91C1C),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "PREDICTION CALCULATION FAILED",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7F1D1D),
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = state.message,
                                        color = Color(0xFF991B1B),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = { viewModel.getPrediction() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Retry Calculation", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Security warning block styled professionally with orange alert tones
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Warning Logo",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Developer Notice",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Security Warning: I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                                color = Color(0xFF78350F).copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Historical Predictions Dialog Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close", color = ProfessionalPurple, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                if (history.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.clearAllHistory()
                            showHistoryDialog = false
                        }
                    ) {
                        Text("Clear All", color = Color(0xFFB91C1C))
                    }
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.History, contentDescription = "History Logo", tint = ProfessionalPurple)
                    Text("Prediction History", color = ProfessionalText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HistoryToggleOff,
                                    contentDescription = "No History Icon",
                                    tint = ProfessionalSecondaryText,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No stored predictions yet.",
                                    color = ProfessionalSecondaryText,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(history) { record ->
                                HistoryRecordItem(
                                    record = record,
                                    onLoad = {
                                        viewModel.applyTemplate(
                                            MatchTemplate(
                                                name = "${record.homeTeam} vs ${record.awayTeam}",
                                                homeTeam = record.homeTeam,
                                                awayTeam = record.awayTeam,
                                                homeForm = record.homeForm,
                                                awayForm = record.awayForm,
                                                injuries = record.injuries,
                                                context = record.context
                                            )
                                        )
                                        // Try to parse raw json back to show as success state
                                        try {
                                            val adapter = RetrofitClient.moshiParser.adapter(PredictionResponse::class.java)
                                            val resp = adapter.fromJson(record.rawPredictionJson)
                                            if (resp != null) {
                                                viewModel.loadStoredPrediction(resp)
                                            }
                                        } catch (e: Exception) {}
                                        showHistoryDialog = false
                                    },
                                    onDelete = {
                                        viewModel.deletePrediction(record.id)
                                    }
                                )
                            }
                        }
                    }
                }
            },
            containerColor = ProfessionalBg,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(ProfessionalCardBg, ProfessionalAccentBg)
                )
            )
            .drawBehind {
                // Drawing modern vector graphic subtle outlines
                drawCircle(
                    color = ProfessionalPurple.copy(alpha = 0.03f),
                    radius = 160.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawLine(
                    color = ProfessionalPurple.copy(alpha = 0.03f),
                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Badge(
                    containerColor = ProfessionalPurple.copy(alpha = 0.12f),
                    contentColor = ProfessionalPurple,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "ALGORITHMIC FORECAST BOT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "High Confidence Models",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfessionalText
                )
                Text(
                    text = "Powered by Gemini 3.5 Flash & Historical Form Weights.",
                    fontSize = 11.sp,
                    color = ProfessionalSecondaryText,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Outlined.Analytics,
                contentDescription = "Data Analytics",
                tint = ProfessionalPurple.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(70.dp)
                    .weight(0.7f)
            )
        }
    }
}

@Composable
fun FormBadgePreview(formString: StateFlow<String>, teamName: String) {
    val form by formString.collectAsStateWithLifecycle()
    if (form.isNotBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = "$teamName form:",
                fontSize = 10.sp,
                color = ProfessionalSecondaryText,
                fontWeight = FontWeight.Medium
            )
            val formItems = form.replace(" ", "").split("-")
            formItems.take(6).forEach { item ->
                val char = item.uppercase(Locale.ROOT).firstOrNull() ?: ' '
                if (char == 'W' || char == 'D' || char == 'L') {
                    val (bg, txt) = when (char) {
                        'W' -> FormWinBg to FormWinText
                        'D' -> FormDrawBg to FormDrawText
                        'L' -> FormLossBg to FormLossText
                        else -> Color.White to ProfessionalSecondaryText
                    }
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(bg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            color = txt,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionResultsCard(
    response: PredictionResponse,
    homeTeamName: String,
    awayTeamName: String,
    currentPredictionId: Int?,
    onSubmitFeedback: (id: Int, isAccurate: Boolean, comment: String) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("prediction_results_card"),
        colors = CardDefaults.cardColors(containerColor = ProfessionalCardBg),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, ProfessionalBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Badge(
                        containerColor = ProfessionalPurple,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "${response.confidenceRating.uppercase()} CONFIDENCE • ${response.confidenceScore}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "ID: ${response.confidenceScore * 77}-ML",
                    color = ProfessionalSecondaryText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Teams Row matching HTML with visual avatar circles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .drawBehind {
                                drawCircle(
                                    color = ProfessionalBorder,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚪",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = homeTeamName,
                        color = ProfessionalText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // VS Division
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = "VS",
                        color = ProfessionalPurple,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "AI FORECAST",
                        color = ProfessionalSecondaryText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Away Team
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .drawBehind {
                                drawCircle(
                                    color = ProfessionalBorder,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔴",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = awayTeamName,
                        color = ProfessionalText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Primary Prediction Bar block matching design HTML bg-white/50
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Primary Prediction",
                            color = ProfessionalSecondaryText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = response.prediction,
                            color = ProfessionalText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Clean progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(ProfessionalBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(response.confidenceScore.toFloat() / 100f)
                                .background(ProfessionalPurple)
                        )
                    }
                }
            }

            // Tri-Columns block matching green-100 / blue-100 / orange-100
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Player Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftGreenBg)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PLAYER FORM",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGreenText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "EXCELLENT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftGreenText,
                        textAlign = TextAlign.Center
                    )
                }

                // Weather Context
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftBlueBg)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CONTEXT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftBlueText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "HIGH STAKES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftBlueText,
                        textAlign = TextAlign.Center
                    )
                }

                // Injuries
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftOrangeBg)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "INJURIES",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftOrangeText,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "KEY OUT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SoftOrangeText,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Probability Spread Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "PROBABILITY SPREAD (%)",
                    color = ProfessionalSecondaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(ProfessionalBorder)
                ) {
                    val homeWeight = response.homeWinProbability.toFloat() / 100f
                    val drawWeight = response.drawProbability.toFloat() / 100f
                    val awayWeight = response.awayWinProbability.toFloat() / 100f

                    if (homeWeight > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(homeWeight.coerceAtLeast(0.01f))
                                .background(SoftGreenText)
                        )
                    }
                    if (drawWeight > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(drawWeight.coerceAtLeast(0.01f))
                                .background(FormDrawText)
                        )
                    }
                    if (awayWeight > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(awayWeight.coerceAtLeast(0.01f))
                                .background(FormLossText)
                        )
                    }
                }

                // Probability Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(SoftGreenText, CircleShape))
                        Text("Home: ${response.homeWinProbability}%", fontSize = 10.sp, color = ProfessionalText, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(FormDrawText, CircleShape))
                        Text("Draw: ${response.drawProbability}%", fontSize = 10.sp, color = ProfessionalText, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(FormLossText, CircleShape))
                        Text("Away: ${response.awayWinProbability}%", fontSize = 10.sp, color = ProfessionalText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Value Pick Suggested Bet Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ProfessionalBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalActivity,
                        contentDescription = "Suggested Bet",
                        tint = ProfessionalPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "VALUE PICK / SUGGESTED BET",
                            color = ProfessionalPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = response.suggestedBet,
                            color = ProfessionalText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Expandable Detail Sections
            ExpandableDetailSection(
                title = "🤜 Head-to-Head (H2H) History",
                content = response.analysisHeadToHead
            )
            ExpandableDetailSection(
                title = "📈 Team Form & Match Metrics",
                content = response.analysisForm
            )
            ExpandableDetailSection(
                title = "🚑 Injuries & News Breakdown",
                content = response.analysisInjuries
            )
            ExpandableDetailSection(
                title = "🛡️ Tactical Formations",
                content = response.analysisTactical
            )

            // Key Insights List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "KEY MATCH INSIGHTS",
                    color = ProfessionalPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                response.keyInsights.forEach { insight ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowForward,
                            contentDescription = "Arrow",
                            tint = ProfessionalPurple,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Text(
                            text = insight,
                            color = ProfessionalText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Divider(color = ProfessionalBorder, thickness = 1.dp)

            // Copysummary button
            Button(
                onClick = {
                    val shareText = """
                        🏆 PITCH AI MATCH FORECAST: $homeTeamName VS $awayTeamName
                        🎯 PREDICTION: ${response.prediction} (${response.confidenceScore}% Confidence)
                        🎫 SUGGESTED BET: ${response.suggestedBet}
                        📊 PROBABILITY: Home ${response.homeWinProbability}% | Draw ${response.drawProbability}% | Away ${response.awayWinProbability}%
                    """.trimIndent()
                    clipboardManager.setText(AnnotatedString(shareText))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy_prediction_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = ProfessionalPurple
                ),
                border = BorderStroke(1.dp, ProfessionalBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Text("Copy Analysis Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Divider(color = ProfessionalBorder, thickness = 1.dp)

            // ML Refinement Continuous Feedback Section
            var feedbackSelected by remember { mutableStateOf<Boolean?>(null) }
            var feedbackComment by remember { mutableStateOf("") }
            var feedbackSubmitted by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("ml_refinement_feedback_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ProfessionalBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Refine Pitch AI's Model Weights",
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalPurple,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Once the match finishes, log actual outcomes to continuously refine neural weights and improve future confidence scoring.",
                        fontSize = 11.sp,
                        color = ProfessionalSecondaryText,
                        lineHeight = 16.sp
                    )

                    if (!feedbackSubmitted) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Accurate (Success) Button
                            OutlinedButton(
                                onClick = { feedbackSelected = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (feedbackSelected == true) SoftGreenBg else Color.White,
                                    contentColor = if (feedbackSelected == true) SoftGreenText else ProfessionalText
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (feedbackSelected == true) SoftGreenText else ProfessionalBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("feedback_accurate_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = "Accurate",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("Accurate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Inaccurate (Failure) Button
                            OutlinedButton(
                                onClick = { feedbackSelected = false },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (feedbackSelected == false) FormLossBg else Color.White,
                                    contentColor = if (feedbackSelected == false) FormLossText else ProfessionalText
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (feedbackSelected == false) FormLossText else ProfessionalBorder
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("feedback_inaccurate_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbDown,
                                        contentDescription = "Inaccurate",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("Inaccurate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (feedbackSelected != null) {
                            OutlinedTextField(
                                value = feedbackComment,
                                onValueChange = { feedbackComment = it },
                                label = { Text("Add details (e.g. final score, player updates)") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ProfessionalPurple,
                                    unfocusedBorderColor = ProfessionalBorder,
                                    focusedTextColor = ProfessionalText,
                                    unfocusedTextColor = ProfessionalText
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("feedback_comment_input"),
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    currentPredictionId?.let { id ->
                                        onSubmitFeedback(id, feedbackSelected!!, feedbackComment)
                                        feedbackSubmitted = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalPurple),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("submit_feedback_button")
                            ) {
                                Text("Submit Learning Feedback", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SoftGreenBg)
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Feedback Recorded",
                                tint = SoftGreenText,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Feedback logged successfully! Pitch AI is utilizing this result to continuously optimize its model weights.",
                                color = SoftGreenText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Dismiss Card Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Dismiss Model Result", color = ProfessionalSecondaryText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ExpandableDetailSection(title: String, content: String) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { isExpanded = !isExpanded }
            .drawBehind {
                drawRoundRect(
                    color = ProfessionalBorder,
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = ProfessionalText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle Section",
                tint = ProfessionalPurple
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = content,
                color = ProfessionalSecondaryText,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun HistoryRecordItem(
    record: PredictionEntity,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val date = remember(record.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, ProfessionalBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${record.homeTeam} VS ${record.awayTeam}",
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalText,
                    fontSize = 14.sp
                )
                Text(
                    text = "Prediction: ${record.prediction} (${record.confidenceScore}%)",
                    color = ProfessionalPurple,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = date,
                    fontSize = 10.sp,
                    color = ProfessionalSecondaryText
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onLoad) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Match Details",
                        tint = ProfessionalPurple
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Record",
                        tint = Color(0xFFB91C1C)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingMatchesDashboard(
    viewModel: PredictionViewModel,
    onPredictNowClick: (UpcomingMatch) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedUpcomingMatchId by viewModel.selectedUpcomingMatchId.collectAsStateWithLifecycle()
    var selectedLeagueFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    
    val leagues = listOf("All", "Premier League", "La Liga", "Serie A", "Bundesliga", "Ligue 1")
    
    val filteredMatches = remember(selectedLeagueFilter, searchQuery, viewModel.upcomingMatches) {
        viewModel.upcomingMatches.filter { match ->
            val matchesLeague = selectedLeagueFilter == "All" || match.league.contains(selectedLeagueFilter, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || 
                    match.homeTeam.contains(searchQuery, ignoreCase = true) ||
                    match.awayTeam.contains(searchQuery, ignoreCase = true) ||
                    match.league.contains(searchQuery, ignoreCase = true)
            matchesLeague && matchesSearch
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "📅 Upcoming Fixtures (Next 7 Days)",
                color = ProfessionalText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Text(
                text = "${filteredMatches.size} Matches",
                color = ProfessionalSecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Search Bar Component
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by team or league...", fontSize = 13.sp, color = ProfessionalSecondaryText) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = ProfessionalSecondaryText,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = ProfessionalSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = ProfessionalPurple,
                unfocusedBorderColor = ProfessionalBorder,
                focusedTextColor = ProfessionalText,
                unfocusedTextColor = ProfessionalText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upcoming_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // League Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(leagues) { league ->
                FilterChip(
                    selected = selectedLeagueFilter == league,
                    onClick = { selectedLeagueFilter = league },
                    label = { Text(league, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        labelColor = ProfessionalSecondaryText,
                        selectedContainerColor = ProfessionalPurple,
                        selectedLabelColor = Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedLeagueFilter == league) ProfessionalPurple else ProfessionalBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("league_filter_${league.replace(" ", "_")}_chip")
                )
            }
        }

        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, ProfessionalBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No Matches",
                        tint = ProfessionalSecondaryText.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "No matches scheduled for this league.",
                        color = ProfessionalSecondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Horizontal Fixtures List
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredMatches) { match ->
                    val isSelected = selectedUpcomingMatchId == match.id
                    UpcomingMatchCard(
                        match = match,
                        isSelected = isSelected,
                        onSelectClick = { viewModel.selectUpcomingMatch(match) },
                        onPredictNowClick = { onPredictNowClick(match) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingMatchCard(
    match: UpcomingMatch,
    isSelected: Boolean,
    onSelectClick: () -> Unit,
    onPredictNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSelectClick,
        modifier = modifier
            .width(280.dp)
            .testTag("upcoming_match_card_${match.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ProfessionalCardBg else Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) ProfessionalPurple else ProfessionalBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: League & Date info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // League Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ProfessionalAccentBg.copy(alpha = 0.5f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = match.league,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfessionalPurple
                    )
                }
                
                // Date Text
                Text(
                    text = match.dateString,
                    fontSize = 11.sp,
                    color = ProfessionalSecondaryText,
                    fontWeight = FontWeight.Medium
                )
            }

            // Teams Row (Visual design centered match layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Home Team Block
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val formattedHome = formatTeamEmoji(match.homeTeam)
                    Text(
                        text = formattedHome,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ProfessionalText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    FormBadgeList(match.template.homeForm)
                }

                // VS Indicator
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(CircleShape)
                        .background(ProfessionalBorder)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "VS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalSecondaryText
                    )
                }

                // Away Team Block
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val formattedAway = formatTeamEmoji(match.awayTeam)
                    Text(
                        text = formattedAway,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ProfessionalText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    FormBadgeList(match.template.awayForm)
                }
            }

            // Weather Preview & Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ProfessionalBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val weatherIcon = when {
                        match.template.weatherCondition.contains("Rain", ignoreCase = true) -> Icons.Default.Umbrella
                        match.template.weatherCondition.contains("Snow", ignoreCase = true) -> Icons.Default.AcUnit
                        match.template.weatherCondition.contains("Cloud", ignoreCase = true) -> Icons.Default.Cloud
                        else -> Icons.Default.WbSunny
                    }
                    Icon(
                        imageVector = weatherIcon,
                        contentDescription = match.template.weatherCondition,
                        tint = ProfessionalPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = match.template.weatherCondition,
                        fontSize = 10.sp,
                        color = ProfessionalSecondaryText,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${match.template.temperature} | 💧 ${match.template.precipitation}",
                    fontSize = 10.sp,
                    color = ProfessionalSecondaryText,
                    fontWeight = FontWeight.Medium
                )
            }

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("upcoming_select_btn_${match.id}"),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) Color.White else ProfessionalBg
                    ),
                    border = BorderStroke(1.dp, if (isSelected) ProfessionalPurple else ProfessionalBorder)
                ) {
                    Text(
                        text = if (isSelected) "Selected" else "Load Data",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) ProfessionalPurple else ProfessionalSecondaryText
                    )
                }

                Button(
                    onClick = {
                        onSelectClick()
                        onPredictNowClick()
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(34.dp)
                        .testTag("upcoming_predict_btn_${match.id}"),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfessionalPurple)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Predict Now",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Instant Predict",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormBadgeList(formString: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        val formChars = formString.split("-")
        formChars.forEach { char ->
            val (bgColor, textColor) = when (char.uppercase()) {
                "W" -> Pair(FormWinBg, FormWinText)
                "D" -> Pair(FormDrawBg, FormDrawText)
                "L" -> Pair(FormLossBg, FormLossText)
                else -> Pair(ProfessionalBorder, ProfessionalSecondaryText)
            }
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

fun formatTeamEmoji(teamName: String): String {
    return when (teamName) {
        "Arsenal" -> "🔴 Arsenal"
        "Chelsea" -> "🔵 Chelsea"
        "Manchester City" -> "🩵 Man City"
        "Liverpool" -> "🔴 Liverpool"
        "Inter Milan" -> "🔵 Inter"
        "AC Milan" -> "🔴 Milan"
        "Bayern Munich" -> "🔴 Bayern"
        "Borussia Dortmund" -> "🟡 Dortmund"
        "Real Madrid" -> "⚪ R. Madrid"
        "Atletico Madrid" -> "🔴 Atletico"
        "Paris Saint-Germain" -> "🔵 PSG"
        "Marseille" -> "🩵 Marseille"
        "Juventus" -> "⚪ Juventus"
        "Napoli" -> "🩵 Napoli"
        "Barcelona" -> "🔴🔵 Barça"
        else -> teamName
    }
}
