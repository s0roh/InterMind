package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.statistic.AggregatedModeStatistic
import com.soroh.intermind.core.data.dto.statistic.CardPhaseStat
import com.soroh.intermind.core.data.dto.statistic.ForecastStat

/**
 * Data layer interface for the statistic feature.
 */
interface StatisticsRepository {

    /**
     * Возвращает агрегированную статистику по режимам для конкретной колоды.
     */
    suspend fun getModesStatisticsForDeck(deckId: String): List<AggregatedModeStatistic>

    suspend fun getCardPhasesStats(deckId: String): List<CardPhaseStat>

    suspend fun getFutureDueForecast(deckId: String): List<ForecastStat>
}