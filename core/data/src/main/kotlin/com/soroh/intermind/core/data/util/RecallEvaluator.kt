package com.soroh.intermind.core.data.util

import android.util.Log
import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.Rating
import com.soroh.intermind.core.data.model.UserCardProgress
import kotlin.math.exp

object RecallEvaluator {

    fun evaluate(
        progress: UserCardProgress,
        result: ObjectiveResult,
        averageTimeMs: Long
    ): Rating {
        val continuous = autoRating(
            responseTimeMs = result.responseTimeMs,
            averageTimeMs = averageTimeMs,
            difficulty = progress.difficulty
        )
        val adjusted = adjustForAttempts(continuous, result.attempts)
        return toDiscreteRating(adjusted)
    }

    private fun autoRating(
        responseTimeMs: Long,
        averageTimeMs: Long,
        difficulty: Double,
        steepness: Double = 2.0
    ): Double {
        if (averageTimeMs <= 0) return 3.0
        val ratio = responseTimeMs.toDouble() / averageTimeMs.toDouble()
        val difficultyFactor = 1.0 - (difficulty - 5.5) / 6.0
        val adjustedRatio = ratio * difficultyFactor
        val sigmoid = 1.0 / (1.0 + exp(-steepness * (adjustedRatio - 1.0)))
        val grade = 5.0 - 4.0 * sigmoid
        return grade.coerceIn(1.0, 4.0)
    }

    private fun adjustForAttempts(grade: Double, attempts: Int): Double {
        // Штраф за дополнительные попытки (каждая попытка снижает оценку на 0.1)
        val penalty = (attempts - 1) * 0.1
        return (grade - penalty).coerceAtLeast(1.0)
    }

    private fun toDiscreteRating(continuous: Double): Rating {
        Log.d("!@#", continuous.toString())
        return when (continuous) {
            in 1.0..<1.5 -> Rating.Again
            in 1.5..<2.5 -> Rating.Hard
            in 2.5..<3.5 -> Rating.Good
            in 3.5..4.0 -> Rating.Easy
            else -> Rating.Good
        }
    }
}