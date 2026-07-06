package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): Flow<List<PredictionEntity>>

    @Query("SELECT * FROM predictions WHERE id = :id LIMIT 1")
    suspend fun getPredictionById(id: Int): PredictionEntity?

    @Query("SELECT * FROM predictions WHERE feedbackAccuracy IS NOT NULL ORDER BY timestamp DESC LIMIT 5")
    suspend fun getPredictionsWithFeedback(): List<PredictionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity): Long

    @Query("DELETE FROM predictions WHERE id = :id")
    suspend fun deletePredictionById(id: Int)

    @Query("DELETE FROM predictions")
    suspend fun clearAllPredictions()
}
