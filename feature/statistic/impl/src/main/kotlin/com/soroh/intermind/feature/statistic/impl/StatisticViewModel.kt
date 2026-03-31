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
        // 1. Создаем Flow для каждого графика.
        // Они будут выполняться параллельно при вызове combine.
        val modesFlow = flow {
            emit(statisticsRepository.getModesStatisticsForDeck(key.deckId))
        }

        val phasesFlow = flow {
            emit(statisticsRepository.getCardPhasesStats(key.deckId))
        }

        val forecastFlow = flow {
            emit(statisticsRepository.getFutureDueForecast(key.deckId))
        }


        // ЗАГОТОВКИ ДЛЯ БУДУЩИХ ГРАФИКОВ:
        // val forecastFlow = flow { emit(statisticsRepository.getForecast(key.deckId)) }
        // val phasesFlow = flow { emit(statisticsRepository.getCardPhases(key.deckId)) }

        // 2. Объединяем все потоки данных
        combine(
            modesFlow,
            phasesFlow,
            forecastFlow
            // forecastFlow,
            // phasesFlow
        ) { modes, phases, forecast ->

            // Проверяем, есть ли вообще хоть какие-то данные
            if (modes.isEmpty() /* && forecast.isEmpty() */) {
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
                // При каждом новом refreshTrigger сначала эмитим Loading
                emit(StatisticUiState.Loading)
            }
            .catch { e ->
                // Перехватываем любые ошибки из репозитория
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
        // Сюда будем добавлять новые данные по мере реализации
        // val forecastStats: List<ForecastStatistic> = emptyList(),
        // val phasesStats: List<PhaseStatistic> = emptyList()
    ) : StatisticUiState
}