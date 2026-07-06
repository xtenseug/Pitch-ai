package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PredictionResponse(
    @Json(name = "prediction") val prediction: String,
    @Json(name = "prediction_type") val predictionType: String,
    @Json(name = "confidence_score") val confidenceScore: Int,
    @Json(name = "confidence_rating") val confidenceRating: String,
    @Json(name = "suggested_bet") val suggestedBet: String,
    @Json(name = "home_win_probability") val homeWinProbability: Int,
    @Json(name = "draw_probability") val drawProbability: Int,
    @Json(name = "away_win_probability") val awayWinProbability: Int,
    @Json(name = "analysis_head_to_head") val analysisHeadToHead: String,
    @Json(name = "analysis_form") val analysisForm: String,
    @Json(name = "analysis_injuries") val analysisInjuries: String,
    @Json(name = "analysis_tactical") val analysisTactical: String,
    @Json(name = "key_insights") val keyInsights: List<String>
)
