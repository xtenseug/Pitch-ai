package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.RetrofitClient
import com.example.data.local.AppDatabase
import com.example.data.local.PredictionEntity
import com.example.data.model.PredictionResponse
import com.example.data.repository.PredictionRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

sealed interface PredictionUiState {
    object Idle : PredictionUiState
    object Loading : PredictionUiState
    data class Success(val response: PredictionResponse) : PredictionUiState
    data class Error(val message: String) : PredictionUiState
}

data class MatchTemplate(
    val name: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeForm: String,
    val awayForm: String,
    val injuries: String,
    val context: String,
    // Head-to-Head Statistics
    val homeWins: String = "0",
    val awayWins: String = "0",
    val draws: String = "0",
    val homeAvgGoals: String = "1.0",
    val awayAvgGoals: String = "1.0",
    val recentEncounters: String = "",
    // Weather Data
    val weatherCondition: String = "Clear Sky",
    val temperature: String = "15°C",
    val precipitation: String = "0%",
    val windSpeed: String = "0 km/h"
)

data class UpcomingMatch(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val league: String,
    val dateString: String,
    val daysFromNow: Int,
    val template: MatchTemplate,
    val homeLogo: String? = null,
    val awayLogo: String? = null
)

class PredictionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PredictionRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = PredictionRepository(database.predictionDao())
    }

    // Historical predictions from local Room DB
    val predictionHistory: StateFlow<List<PredictionEntity>> = repository.allPredictions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current prediction request state
    private val _uiState = MutableStateFlow<PredictionUiState>(PredictionUiState.Idle)
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    // Form states
    var homeTeam = MutableStateFlow("Real Madrid")
    var awayTeam = MutableStateFlow("Barcelona")
    var homeForm = MutableStateFlow("W-W-D-W-L")
    var awayForm = MutableStateFlow("W-W-W-D-W")
    var injuries = MutableStateFlow("Real Madrid: Vinicius Jr. out (hamstring). Barcelona: Gavi doubtful.")
    var context = MutableStateFlow("La Liga Title decider, high-stakes match with intense pressure.")

    // Head-to-Head Statistics Form State
    var homeWins = MutableStateFlow("102")
    var awayWins = MutableStateFlow("100")
    var draws = MutableStateFlow("52")
    var homeAvgGoals = MutableStateFlow("1.7")
    var awayAvgGoals = MutableStateFlow("1.6")
    var recentEncounters = MutableStateFlow("Real Madrid 3-2 Barcelona, Real Madrid 4-1 Barcelona")

    // Weather Data Form State
    var weatherCondition = MutableStateFlow("Clear Sky")
    var temperature = MutableStateFlow("18°C")
    var precipitation = MutableStateFlow("5%")
    var windSpeed = MutableStateFlow("8 km/h")

    // Betslip Upload & Analysis State
    private val _betslipAnalysisState = MutableStateFlow<String?>(null)
    val betslipAnalysisState: StateFlow<String?> = _betslipAnalysisState.asStateFlow()

    private val _isAnalyzingBetslip = MutableStateFlow(false)
    val isAnalyzingBetslip: StateFlow<Boolean> = _isAnalyzingBetslip.asStateFlow()

    // ID of the last prediction generated to allow direct feedback
    private val _currentPredictionId = MutableStateFlow<Int?>(null)
    val currentPredictionId: StateFlow<Int?> = _currentPredictionId.asStateFlow()

    // Selected upcoming match tracking
    private val _selectedUpcomingMatchId = MutableStateFlow<String?>(null)
    val selectedUpcomingMatchId: StateFlow<String?> = _selectedUpcomingMatchId.asStateFlow()

    private fun getFormattedDate(daysAhead: Long): String {
        return try {
            val date = LocalDate.now().plusDays(daysAhead)
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US)
            val month = date.month.getDisplayName(TextStyle.SHORT, Locale.US)
            val dayOfMonth = date.dayOfMonth
            if (daysAhead == 1L) {
                "Tomorrow ($dayOfWeek)"
            } else {
                "$dayOfWeek, $month $dayOfMonth"
            }
        } catch (e: Exception) {
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.DAY_OF_YEAR, daysAhead.toInt())
            val sdf = java.text.SimpleDateFormat("EEE, MMM d", Locale.US)
            if (daysAhead == 1L) "Tomorrow" else sdf.format(calendar.time)
        }
    }

    val upcomingMatches: List<UpcomingMatch> by lazy {
        listOf(
            UpcomingMatch(
                id = "match_1",
                homeTeam = "Arsenal",
                awayTeam = "Chelsea",
                league = "🇬🇧 Premier League",
                dateString = getFormattedDate(1),
                daysFromNow = 1,
                template = MatchTemplate(
                    name = "London Derby",
                    homeTeam = "Arsenal",
                    awayTeam = "Chelsea",
                    homeForm = "W-D-W-W-L",
                    awayForm = "L-W-D-W-W",
                    injuries = "Arsenal: Odegaard is fully fit and pulling strings in midfield; Timber is doubtful with a minor knock. Chelsea: Enzo Fernandez is suspended, Reece James out with hamstring issue.",
                    context = "A high-stakes London Derby at the Emirates. Arsenal wants to keep pressure on the league leaders, while Chelsea is fighting for European spots.",
                    homeWins = "83",
                    awayWins = "66",
                    draws = "59",
                    homeAvgGoals = "1.6",
                    awayAvgGoals = "1.4",
                    recentEncounters = "Arsenal 5-0 Chelsea (Apr 2024), Chelsea 2-2 Arsenal (Oct 2023)",
                    weatherCondition = "Clear Sky",
                    temperature = "18°C",
                    precipitation = "10%",
                    windSpeed = "12 km/h"
                )
            ),
            UpcomingMatch(
                id = "match_2",
                homeTeam = "Manchester City",
                awayTeam = "Liverpool",
                league = "🇬🇧 Premier League",
                dateString = getFormattedDate(2),
                daysFromNow = 2,
                template = MatchTemplate(
                    name = "Premier League Title Clash",
                    homeTeam = "Manchester City",
                    awayTeam = "Liverpool",
                    homeForm = "W-L-W-D-W",
                    awayForm = "W-W-W-D-D",
                    injuries = "Man City: Rodri is suspended (red card), De Bruyne returning from minor muscle strain. Liverpool: Alisson is fit in goal, Jota out with knee sprain.",
                    context = "The modern classic. A battle for tactical supremacy. City's possession-based masterclass vs Liverpool's high-octane heavy metal counter-pressing.",
                    homeWins = "50",
                    awayWins = "93",
                    draws = "56",
                    homeAvgGoals = "1.4",
                    awayAvgGoals = "1.8",
                    recentEncounters = "Liverpool 1-1 Man City (Mar 2024), Man City 1-1 Liverpool (Nov 2023)",
                    weatherCondition = "Light Rain",
                    temperature = "12°C",
                    precipitation = "65%",
                    windSpeed = "22 km/h"
                )
            ),
            UpcomingMatch(
                id = "match_3",
                homeTeam = "Inter Milan",
                awayTeam = "AC Milan",
                league = "🇮🇹 Serie A",
                dateString = getFormattedDate(3),
                daysFromNow = 3,
                template = MatchTemplate(
                    name = "Derby della Madonnina",
                    homeTeam = "Inter Milan",
                    awayTeam = "AC Milan",
                    homeForm = "W-W-W-D-W",
                    awayForm = "L-W-D-W-L",
                    injuries = "Inter: Lautaro Martinez in peak physical condition; Acerbi doubtful in central defense. AC Milan: Maignan returning from shoulder injury, Bennacer out.",
                    context = "San Siro hosts the legendary Milan Derby. Inter wants to secure local bragging rights and extend their lead, while Milan seeks redemption.",
                    homeWins = "90",
                    awayWins = "79",
                    draws = "69",
                    homeAvgGoals = "1.5",
                    awayAvgGoals = "1.3",
                    recentEncounters = "AC Milan 1-2 Inter Milan (Apr 2024), Inter Milan 5-1 AC Milan (Sep 2023)",
                    weatherCondition = "Clear Sky",
                    temperature = "20°C",
                    precipitation = "0%",
                    windSpeed = "5 km/h"
                )
            ),
            UpcomingMatch(
                id = "match_4",
                homeTeam = "Bayern Munich",
                awayTeam = "Borussia Dortmund",
                league = "🇩🇪 Bundesliga",
                dateString = getFormattedDate(4),
                daysFromNow = 4,
                template = MatchTemplate(
                    name = "Der Klassiker",
                    homeTeam = "Bayern Munich",
                    awayTeam = "Borussia Dortmund",
                    homeForm = "W-W-W-W-D",
                    awayForm = "W-L-D-W-W",
                    injuries = "Bayern: Harry Kane in red-hot scoring form; Sane doubtful with groin issue. Dortmund: Guirassy leading the line; Brandt fit but Sabitzer suspended.",
                    context = "Der Klassiker at the Allianz Arena. Bayern Munich aims to assert absolute dominance at home, while Dortmund looks to spring an upset with rapid transitional wing-play.",
                    homeWins = "68",
                    awayWins = "34",
                    draws = "31",
                    homeAvgGoals = "2.1",
                    awayAvgGoals = "1.3",
                    recentEncounters = "Bayern 0-2 Dortmund (Mar 2024), Dortmund 0-4 Bayern (Nov 2023)",
                    weatherCondition = "Heavy Rain",
                    temperature = "8°C",
                    precipitation = "85%",
                    windSpeed = "18 km/h"
                )
            ),
            UpcomingMatch(
                id = "match_5",
                homeTeam = "Real Madrid",
                awayTeam = "Atletico Madrid",
                league = "🇪🇸 La Liga",
                dateString = getFormattedDate(5),
                daysFromNow = 5,
                template = MatchTemplate(
                    name = "Madrid Derby",
                    homeTeam = "Real Madrid",
                    awayTeam = "Atletico Madrid",
                    homeForm = "W-W-D-W-L",
                    awayForm = "W-W-D-D-W",
                    injuries = "Real Madrid: Bellingham fully fit, Mbappe returning from minor thigh issue. Atletico: Griezmann in excellent form, De Paul fully fit in midfield.",
                    context = "An intense tactical derby battle at the Santiago Bernabeu. Real Madrid's individual brilliance and lethal transition vs Atletico's defensive block and aggressive physical press.",
                    homeWins = "116",
                    awayWins = "58",
                    draws = "42",
                    homeAvgGoals = "1.8",
                    awayAvgGoals = "1.2",
                    recentEncounters = "Real Madrid 1-1 Atletico (Feb 2024), Atletico 1-1 Real Madrid (Sep 2024)",
                    weatherCondition = "Cloudy",
                    temperature = "15°C",
                    precipitation = "15%",
                    windSpeed = "14 km/h"
                )
            ),
            UpcomingMatch(
                id = "match_6",
                homeTeam = "Paris Saint-Germain",
                awayTeam = "Marseille",
                league = "🇫🇷 Ligue 1",
                dateString = getFormattedDate(6),
                daysFromNow = 6,
                template = MatchTemplate(
                    name = "Le Classique",
                    homeTeam = "Paris Saint-Germain",
                    awayTeam = "Marseille",
                    homeForm = "W-D-W-W-W",
                    awayForm = "L-W-W-D-W",
                    injuries = "PSG: Dembele and Barcola leading the attack, Vitinha fit in midfield. Marseille: Greenwood fit, Aubameyang departed, Rabiot key in midfield.",
                    context = "The biggest rivalry in French football. PSG is playing at the Parc des Princes under high expectation, while Marseille wants to prove their defensive resilience.",
                    homeWins = "49",
                    awayWins = "34",
                    draws = "23",
                    homeAvgGoals = "1.6",
                    awayAvgGoals = "1.2",
                    recentEncounters = "Marseille 0-2 PSG (Mar 2024), PSG 4-0 Marseille (Sep 2023)",
                    weatherCondition = "Foggy",
                    temperature = "11°C",
                    precipitation = "30%",
                    windSpeed = "6 km/h"
                )
            ),
            UpcomingMatch(
                id = "match_7",
                homeTeam = "Juventus",
                awayTeam = "Napoli",
                league = "🇮🇹 Serie A",
                dateString = getFormattedDate(7),
                daysFromNow = 7,
                template = MatchTemplate(
                    name = "Serie A Showdown",
                    homeTeam = "Juventus",
                    awayTeam = "Napoli",
                    homeForm = "W-D-D-W-W",
                    awayForm = "W-W-W-D-L",
                    injuries = "Juventus: Vlahovic fit, Bremer key in central defense. Napoli: Kvaratskhelia fully fit and dangerous; Lukaku leading the line.",
                    context = "Tactical masterclass in Turin. A defensive Juventus side matching up against a fast, high-pressing Napoli team. Every detail and set piece will count.",
                    homeWins = "71",
                    awayWins = "36",
                    draws = "48",
                    homeAvgGoals = "1.4",
                    awayAvgGoals = "1.1",
                    recentEncounters = "Napoli 2-1 Juventus (Mar 2024), Juventus 1-0 Napoli (Dec 2023)",
                    weatherCondition = "Clear Sky",
                    temperature = "24°C",
                    precipitation = "0%",
                    windSpeed = "8 km/h"
                )
            )
        )
    }

    fun selectUpcomingMatch(match: UpcomingMatch) {
        _selectedUpcomingMatchId.value = match.id
        applyTemplate(match.template)
    }

    // Preloaded match templates with comprehensive datasets
    val templates = listOf(
        MatchTemplate(
            name = "🇪🇸 El Clásico",
            homeTeam = "Real Madrid",
            awayTeam = "Barcelona",
            homeForm = "W-W-D-W-L",
            awayForm = "W-W-W-D-W",
            injuries = "Real Madrid: Vinicius Jr. out (hamstring), Courtois returning. Barcelona: Gavi doubtful, Lewandowski in great shape.",
            context = "La Liga Title Decider. Real Madrid leads by 1 point. Intense tactical showdown at Bernabeu.",
            homeWins = "102",
            awayWins = "100",
            draws = "52",
            homeAvgGoals = "1.7",
            awayAvgGoals = "1.6",
            recentEncounters = "Real Madrid 3-2 Barcelona (Apr 2024), Real Madrid 4-1 Barcelona (Jan 2024), Barcelona 1-2 Real Madrid (Oct 2023)",
            weatherCondition = "Clear Sky",
            temperature = "22°C",
            precipitation = "5%",
            windSpeed = "9 km/h"
        ),
        MatchTemplate(
            name = "🇬🇧 Premier League Clash",
            homeTeam = "Arsenal",
            awayTeam = "Manchester City",
            homeForm = "W-W-W-W-D",
            awayForm = "W-D-W-L-W",
            injuries = "Arsenal: Bukayo Saka fully fit, Saliba key defender. Man City: De Bruyne out (groin), Rodri doubtful.",
            context = "Premier League title decider. Arsenal wants to break City's dominance. High-intensity possession duel.",
            homeWins = "23",
            awayWins = "31",
            draws = "16",
            homeAvgGoals = "1.1",
            awayAvgGoals = "1.5",
            recentEncounters = "Manchester City 0-0 Arsenal (Mar 2024), Arsenal 1-0 Manchester City (Oct 2023), Arsenal 1-1 Man City (Community Shield)",
            weatherCondition = "Heavy Rain",
            temperature = "9°C",
            precipitation = "80%",
            windSpeed = "25 km/h"
        ),
        MatchTemplate(
            name = "🇪🇺 Champions League Night",
            homeTeam = "Bayern Munich",
            awayTeam = "Paris Saint-Germain",
            homeForm = "W-D-W-W-W",
            awayForm = "L-W-W-D-W",
            injuries = "Bayern Munich: Harry Kane leading line, Musiala fit. PSG: Dembele doubtful, Mbappe has departed.",
            context = "Champions League Quarterfinals. First leg. High tactical stakes with rapid transition threat from PSG.",
            homeWins = "7",
            awayWins = "6",
            draws = "1",
            homeAvgGoals = "1.8",
            awayAvgGoals = "1.8",
            recentEncounters = "Bayern Munich 2-0 PSG (Mar 2023), PSG 0-1 Bayern Munich (Feb 2023), PSG 0-1 Bayern Munich (Apr 2021)",
            weatherCondition = "Light Snow",
            temperature = "2°C",
            precipitation = "55%",
            windSpeed = "16 km/h"
        )
    )

    fun applyTemplate(template: MatchTemplate) {
        homeTeam.value = template.homeTeam
        awayTeam.value = template.awayTeam
        homeForm.value = template.homeForm
        awayForm.value = template.awayForm
        injuries.value = template.injuries
        context.value = template.context
        
        homeWins.value = template.homeWins
        awayWins.value = template.awayWins
        draws.value = template.draws
        homeAvgGoals.value = template.homeAvgGoals
        awayAvgGoals.value = template.awayAvgGoals
        recentEncounters.value = template.recentEncounters

        weatherCondition.value = template.weatherCondition
        temperature.value = template.temperature
        precipitation.value = template.precipitation
        windSpeed.value = template.windSpeed

        // Clear upcoming match selection if we are applying a standard template
        val isMatchFromUpcoming = upcomingMatches.any { it.template == template }
        if (!isMatchFromUpcoming) {
            _selectedUpcomingMatchId.value = null
        }
    }

    fun getPrediction() {
        viewModelScope.launch {
            _uiState.value = PredictionUiState.Loading
            try {
                val response = repository.generatePrediction(
                    homeTeam = homeTeam.value,
                    awayTeam = awayTeam.value,
                    homeForm = homeForm.value,
                    awayForm = awayForm.value,
                    injuries = injuries.value,
                    context = context.value,
                    homeWins = homeWins.value,
                    awayWins = awayWins.value,
                    draws = draws.value,
                    homeAvgGoals = homeAvgGoals.value,
                    awayAvgGoals = awayAvgGoals.value,
                    recentEncounters = recentEncounters.value,
                    weatherCondition = weatherCondition.value,
                    temperature = temperature.value,
                    precipitation = precipitation.value,
                    windSpeed = windSpeed.value
                )

                // Save to local Room database for historical tracking & reinforcement learning
                val entity = PredictionEntity(
                    homeTeam = homeTeam.value,
                    awayTeam = awayTeam.value,
                    homeForm = homeForm.value,
                    awayForm = awayForm.value,
                    injuries = injuries.value,
                    context = context.value,
                    prediction = response.prediction,
                    confidenceScore = response.confidenceScore,
                    suggestedBet = response.suggestedBet,
                    rawPredictionJson = RetrofitClient.moshiParser.adapter(PredictionResponse::class.java).toJson(response),
                    
                    // Stats
                    homeWins = homeWins.value,
                    awayWins = awayWins.value,
                    draws = draws.value,
                    homeAvgGoals = homeAvgGoals.value,
                    awayAvgGoals = awayAvgGoals.value,
                    recentEncounters = recentEncounters.value,

                    // Weather
                    weatherCondition = weatherCondition.value,
                    temperature = temperature.value,
                    precipitation = precipitation.value,
                    windSpeed = windSpeed.value
                )
                
                val insertedId = repository.insert(entity)
                _currentPredictionId.value = insertedId.toInt()

                _uiState.value = PredictionUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = PredictionUiState.Error(e.localizedMessage ?: "An unexpected error occurred")
            }
        }
    }

    fun submitPredictionFeedback(id: Int, isAccurate: Boolean, comment: String) {
        viewModelScope.launch {
            val prediction = repository.getPredictionById(id)
            if (prediction != null) {
                val updated = prediction.copy(
                    feedbackAccuracy = if (isAccurate) "ACCURATE" else "INACCURATE",
                    feedbackComment = comment
                )
                repository.updatePrediction(updated)
            }
        }
    }

    fun analyzeUploadedBetslip(base64Image: String) {
        viewModelScope.launch {
            _isAnalyzingBetslip.value = true
            _betslipAnalysisState.value = "Analyzing image with Pitch AI Vision models..."
            try {
                val analysisResult = repository.analyzeBetslip(base64Image)
                _betslipAnalysisState.value = analysisResult
                
                // Prefill team fields if the analysis outlines them
                // We ask the model in its instructions to output: "TEAMS: Home Team vs Away Team"
                val regex = Regex("TEAMS:\\s*(.+?)\\s*vs\\s*(.+?)(?:\\n|\$)", RegexOption.IGNORE_CASE)
                val match = regex.find(analysisResult)
                if (match != null) {
                    val home = match.groupValues[1].trim().replace("**", "").trim()
                    val away = match.groupValues[2].trim().replace("**", "").trim()
                    if (home.isNotBlank() && away.isNotBlank()) {
                        homeTeam.value = home
                        awayTeam.value = away
                    }
                }
            } catch (e: Exception) {
                _betslipAnalysisState.value = "Error parsing betslip: ${e.localizedMessage}"
            } finally {
                _isAnalyzingBetslip.value = false
            }
        }
    }

    fun clearBetslipAnalysis() {
        _betslipAnalysisState.value = null
    }

    fun resetPredictionState() {
        _uiState.value = PredictionUiState.Idle
        _currentPredictionId.value = null
    }

    fun loadStoredPrediction(response: PredictionResponse) {
        _uiState.value = PredictionUiState.Success(response)
    }

    fun deletePrediction(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
