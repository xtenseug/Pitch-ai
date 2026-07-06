package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val homeTeam: String,
    val awayTeam: String,
    val homeForm: String,
    val awayForm: String,
    val injuries: String,
    val context: String,
    val prediction: String,
    val confidenceScore: Int,
    val suggestedBet: String,
    val rawPredictionJson: String,
    
    // Head-to-Head Statistics
    val homeWins: String? = null,
    val awayWins: String? = null,
    val draws: String? = null,
    val homeAvgGoals: String? = null,
    val awayAvgGoals: String? = null,
    val recentEncounters: String? = null,

    // Weather Data
    val weatherCondition: String? = null,
    val temperature: String? = null,
    val precipitation: String? = null,
    val windSpeed: String? = null,

    // Feedback
    val feedbackAccuracy: String? = null, // "ACCURATE" or "INACCURATE"
    val feedbackComment: String? = null,

    // Betslip analysis
    val betslipAnalysisResult: String? = null
)
