package com.soroh.intermind.core.data.util

import android.util.Log
import com.soroh.intermind.core.data.model.ObjectiveResult
import com.soroh.intermind.core.data.model.Rating
import com.soroh.intermind.core.data.model.UserCardProgress
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.time.Clock

object RecallEvaluator {
    // Обученные параметры модели
    private const val S = 0.1790       // Крутизна сигмоиды
    private const val SH = 0.0326      // Сдвиг (shift)

    private const val DC = -0.5949     // Вес сложности (Difficulty)
    private const val SC = 0.0889      // Вес стабильности (Stability)
    private const val IC = -1.2792     // Вес интервала (Interval)

    private const val ISC = -0.8624    // Взаимодействие Stability * TimeRatio
    private const val IIC = 0.3203     // Взаимодействие Interval * TimeRatio
    private const val IDSC = -0.2534   // Взаимодействие Difficulty * Stability

    private const val W_ATT = -0.5080  // Вес взаимодействия Attempts * Time
    private const val BIAS = -0.6525   // Смещение (Bias)

    private const val THRESHOLD_AGAIN_HARD = 1.0000
    private const val THRESHOLD_HARD_GOOD  = 1.3482
    private const val THRESHOLD_GOOD_EASY  = 1.3794

    fun evaluate(
        progress: UserCardProgress,
        result: ObjectiveResult,
        averageTimeMs: Long
    ): Rating {
        // Вычисляем прошедшее время с последнего повторения в днях
        val now = Clock.System.now()
        val duration = now - progress.lastReview
        val elapsedSeconds = duration.inWholeSeconds.toDouble()

        val elapsedDays = elapsedSeconds / 86400.0 // 24 * 60 * 60

        // Если карточка новая или интервал еще не прошел (edge case), ставим минимальное значение
        val safeElapsedDays = max(elapsedDays, 0.0)

        // Расчет непрерывной оценки
        val continuous = autoRating(
            responseTimeMs = result.responseTimeMs,
            averageTimeMs = averageTimeMs,
            difficulty = progress.difficulty,
            stability = progress.stability,
            elapsedDays = safeElapsedDays,
            attempts = result.attempts
        )

        Log.d(
            "AutoRating",
            "Continuous score: $continuous | Args: D=${progress.difficulty}, S=${progress.stability}, Days=$safeElapsedDays, Att=${result.attempts}"
        )

        // Преобразование в дискретный класс
        return toDiscreteRating(continuous)
    }

    /**
     * Основная функция расчета оценки.
     */
    private fun autoRating(
        responseTimeMs: Long,
        averageTimeMs: Long,
        difficulty: Double,
        stability: Double,
        elapsedDays: Double,
        attempts: Int
    ): Double {
        if (averageTimeMs <= 0L) return 3.0 // Защита от деления на ноль

        // Подготовка базовых величин
        val timeRatio = responseTimeMs.toDouble() / averageTimeMs.toDouble()

        // Логарифмы: ln(1 + x)
        val logTR = ln(1.0 + timeRatio)
        val logS = ln(1.0 + stability)
        val logE = ln(1.0 + elapsedDays)
        val dCentered = difficulty - 5.5

        val logAtt = if (attempts > 1) ln(attempts.toDouble()) else 0.0

        // Расчет корректирующих множителей (Factors)
        val fDiff = (1.0 + DC * dCentered).coerceIn(0.5, 1.5)
        val fStab = (1.0 + SC * logS).coerceIn(0.5, 1.5)
        val fInt = (1.0 + IC * logE).coerceIn(0.5, 1.5)

        // Расчет взаимодействий (Interactions)
        val intTS = logS * (timeRatio - 1.0)
        val intTE = logE * (1.0 - timeRatio)
        val intDS = dCentered * logS
        val intAttTR = logAtt * logTR

        // Множители взаимодействий (с ограничением диапазона)
        val interStab = (1.0 + ISC * intTS).coerceIn(0.7, 1.3)
        val interInt = (1.0 + IIC * intTE).coerceIn(0.7, 1.3)
        val interDS = (1.0 + IDSC * intDS).coerceIn(0.7, 1.3)
        val interAtt = (1.0 + W_ATT * intAttTR).coerceIn(0.5, 2.0)

        //Сборка основного аргумента (adj)
        val adj = logTR * fDiff * fStab * fInt * interStab * interInt * interDS * interAtt

        // Применение сигмоиды и смещений
        val sigmoidArg = -S * (adj - SH)
        val pAgain = 1.0 / (1.0 + exp(-sigmoidArg))

        var score = 5.0 - 4.0 * pAgain + BIAS

        // Линейный штраф за попытки
        val linearPenalty = 1.5 * logAtt
        score -= linearPenalty

        return score.coerceIn(1.0, 4.0)
    }

    /**
     * Преобразование непрерывной оценки в дискретный рейтинг.
     */
    private fun toDiscreteRating(continuous: Double): Rating {
        return when {
            continuous < THRESHOLD_AGAIN_HARD -> Rating.Again
            continuous < THRESHOLD_HARD_GOOD  -> Rating.Hard
            continuous < THRESHOLD_GOOD_EASY  -> Rating.Good
            else -> Rating.Easy
        }
    }
}