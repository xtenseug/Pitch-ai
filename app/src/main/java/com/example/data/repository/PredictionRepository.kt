package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.InlineData
import com.example.data.api.RetrofitClient
import com.example.data.local.PredictionDao
import com.example.data.local.PredictionEntity
import com.example.data.model.PredictionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PredictionRepository(private val predictionDao: PredictionDao) {

    val allPredictions: Flow<List<PredictionEntity>> = predictionDao.getAllPredictions()

    suspend fun insert(prediction: PredictionEntity): Long = withContext(Dispatchers.IO) {
        predictionDao.insertPrediction(prediction)
    }

    suspend fun getPredictionById(id: Int): PredictionEntity? = withContext(Dispatchers.IO) {
        predictionDao.getPredictionById(id)
    }

    suspend fun updatePrediction(prediction: PredictionEntity) = withContext(Dispatchers.IO) {
        predictionDao.insertPrediction(prediction)
    }

    suspend fun deleteById(id: Int) = withContext(Dispatchers.IO) {
        predictionDao.deletePredictionById(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        predictionDao.clearAllPredictions()
    }

    suspend fun generatePrediction(
        homeTeam: String,
        awayTeam: String,
        homeForm: String,
        awayForm: String,
        injuries: String,
        context: String,
        homeWins: String,
        awayWins: String,
        draws: String,
        homeAvgGoals: String,
        awayAvgGoals: String,
        recentEncounters: String,
        weatherCondition: String,
        temperature: String,
        precipitation: String,
        windSpeed: String
    ): PredictionResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API key is missing. Please add your GEMINI_API_KEY inside the Secrets panel.")
        }

        // Continuous learning feedback logs integration
        val feedbackMatches = predictionDao.getPredictionsWithFeedback()
        val learningContext = if (feedbackMatches.isNotEmpty()) {
            val logs = feedbackMatches.joinToString("\n") { match ->
                "Match: ${match.homeTeam} vs ${match.awayTeam} | Predicted: '${match.prediction}' (Confidence: ${match.confidenceScore}%) | Actual User Feedback: '${match.feedbackAccuracy}' (${match.feedbackComment ?: "No comment"})"
            }
            """
            ### MODEL CONTINUOUS LEARNING & PERFORMANCE LOGS
            Below are previous matches where the user gave accuracy feedback. You MUST analyze these outcomes to correct any systemic bias, overconfidence, or tactical oversights:
            $logs
            """.trimIndent()
        } else {
            ""
        }

        val systemPrompt = """
            You are a professional football analyst and algorithmic match predictor. Your goal is to analyze the provided match details (Teams, Recent Form, Injury Updates, H2H statistics, and Weather conditions) and generate a highly accurate, deep statistical prediction.
            You MUST respond with a single valid JSON object. Do not include any markdown formatting, backticks, or text outside the JSON.
            The JSON object must strictly have the following fields:
            - prediction (String): The primary outcome predicted, e.g., 'Manchester City Win', 'Draw', 'Over 2.5 Goals', 'BTTS - Yes'.
            - prediction_type (String): The category of the prediction, e.g., '1X2', 'Goals Over/Under', 'Both Teams to Score', 'Double Chance'.
            - confidence_score (Integer between 0 and 100): Your confidence in the predicted outcome based on data. Adjust based on weather factors, injury impact, and continuous learning feedback logs.
            - confidence_rating (String): 'HIGH', 'MEDIUM', or 'LOW' based on the score.
            - suggested_bet (String): A recommended betting market/value pick, e.g., 'Manchester City to Win & Over 2.5 Goals'.
            - home_win_probability (Integer): Probability of home win (0 to 100).
            - draw_probability (Integer): Probability of draw (0 to 100).
            - away_win_probability (Integer): Probability of away win (0 to 100).
            - analysis_head_to_head (String): Comprehensive historical and tactical breakdown of their recent meetings. Use provided Head-to-Head stats.
            - analysis_form (String): Deep analysis of both teams' recent form, tactics, and underlying metrics.
            - analysis_injuries (String): Precise evaluation of how key player absences will impact the game.
            - analysis_tactical (String): Overall tactical overview of how the match will play out (including how weather affects passing, speed, and defense).
            - key_insights (Array of Strings): 3 to 4 bullet points of crucial game-changing factors, data trends, weather variables, or continuous learning lessons.

            The probabilities home_win_probability, draw_probability, and away_win_probability MUST sum up to exactly 100.
        """.trimIndent()

        val promptText = """
            Please analyze this upcoming football match:
            - Home Team: $homeTeam
            - Away Team: $awayTeam
            - Home Team Recent Form: $homeForm (typically represented as W, D, L e.g., W-W-D-L-W)
            - Away Team Recent Form: $awayForm (typically represented as W, D, L e.g., D-L-W-W-D)
            - Key Player Injuries / Suspensions: $injuries
            - Match Context & Importance: $context

            ### HEAD-TO-HEAD (H2H) STATISTICS:
            - Home Team Wins: $homeWins
            - Away Team Wins: $awayWins
            - Draws: $draws
            - Home Team Avg Goals: $homeAvgGoals
            - Away Team Avg Goals: $awayAvgGoals
            - Recent Match Results: $recentEncounters

            ### WEATHER FORECAST:
            - Condition: $weatherCondition
            - Temperature: $temperature
            - Precipitation Probability: $precipitation
            - Wind Speed: $windSpeed

            $learningContext
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = promptText)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            )
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response received from the prediction model.")

        try {
            // Strip any markdown code block wrapper if present
            val cleanedJson = responseText
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val adapter = RetrofitClient.moshiParser.adapter(PredictionResponse::class.java)
            adapter.fromJson(cleanedJson) ?: throw Exception("Failed to parse prediction response JSON.")
        } catch (e: Exception) {
            throw Exception("Prediction response parsing failed: ${e.localizedMessage}. Raw response: $responseText")
        }
    }

    suspend fun analyzeBetslip(base64Image: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("API key is missing. Please add your GEMINI_API_KEY inside the Secrets panel.")
        }

        val systemPrompt = """
            You are an expert betslip parser and football match intelligence bot.
            Analyze the provided betslip image. Extract the following details into a well-formatted and beautiful markdown summary:
            1. **Competing Teams**: Extracted home and away teams.
            2. **Betting Market & Selection**: The pick made (e.g. Home Win, Over 2.5 Goals).
            3. **Odds**: The bookmaker odds of the selection.
            4. **Stake & Potential Payout**: If visible in the image.
            
            Finally, provide a clear 2-3 sentence strategic advice on the quality of this bet, and state what tactical or weather parameters should be analyzed to confirm its likelihood.
            Keep your response concise, professional, and completely realistic. Do not make up information that is not on the betslip, other than the expert recommendation.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(
                    Part(text = "Analyze this betslip image and extract match details, betting market, odds, and provide your recommendation."),
                    Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                ))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "text/plain",
                temperature = 0.2
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemPrompt))
            )
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response received from the betslip analyzer model.")
    }
}
