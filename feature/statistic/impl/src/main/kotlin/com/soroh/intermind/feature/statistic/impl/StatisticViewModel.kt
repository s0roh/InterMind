package com.soroh.intermind.feature.statistic.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soroh.intermind.core.data.dto.statistic.AggregatedModeStatistic
import com.soroh.intermind.core.data.dto.statistic.CardPhaseStat
import com.soroh.intermind.core.data.dto.statistic.ForecastStat
import com.soroh.intermind.core.data.repository.StatisticsRepository
import com.soroh.intermind.feature.statistic.api.navigation.StatisticNavKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@HiltViewModel(assistedFactory = StatisticViewModel.Factory::class)
class StatisticViewModel @AssistedInject constructor(
    private val statisticsRepository: StatisticsRepository,
    @Assisted val key: StatisticNavKey,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        refreshTrigger.tryEmit(Unit)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticUiState> = refreshTrigger.flatMapLatest {
        val modesFlow = flow {
            emit(statisticsRepository.getModesStatisticsForDeck(key.deckId))
        }

        val phasesFlow = flow {
            emit(statisticsRepository.getCardPhasesStats(key.deckId))
        }

        val forecastFlow = flow {
            emit(statisticsRepository.getFutureDueForecast(key.deckId))
        }

        combine(
            modesFlow,
            phasesFlow,
            forecastFlow
        ) { modes, phases, forecast ->

            if (modes.isEmpty()) {
                StatisticUiState.Error("Пока нет данных для этой колоды. Пройдите пару тренировок!")
            } else {
                StatisticUiState.Success(
                    modesStats = modes,
                    phasesStats = phases,
                    forecastStats = forecast,
                )
            }
        }
            .onStart {
                emit(StatisticUiState.Loading)
            }
            .catch { e ->
                emit(StatisticUiState.Error("Ошибка загрузки: ${e.message}"))
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticUiState.Loading
    )

    @AssistedFactory
    interface Factory {
        fun create(key: StatisticNavKey): StatisticViewModel
    }
}

sealed interface StatisticUiState {
    data object Loading : StatisticUiState
    data class Error(val message: String) : StatisticUiState
    data class Success(
        val modesStats: List<AggregatedModeStatistic>,
        val phasesStats: List<CardPhaseStat>,
        val forecastStats: List<ForecastStat>,
    ) : StatisticUiState
}