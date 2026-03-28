package com.soroh.intermind.core.data.model

import com.soroh.intermind.core.domain.entity.TestType
import java.time.LocalDateTime
import kotlin.time.Instant

/**
 * Оценка качества ответа пользователя, используемая алгоритмом FSRS.
 * Значения соответствуют классическим кнопкам в системах интервальных повторений.
 *
 * @property value числовое значение оценки (1-4), используемое в формулах FSRS.
 */
enum class Rating(val value: Int) {
    /** Ответ неправильный (забыл) */
    Again(1),

    /** Ответ правильный, но с трудом / медленно */
    Hard(2),

    /** Ответ правильный, нормально */
    Good(3),

    /** Ответ правильный, легко и быстро */
    Easy(4)
}

/**
 * Фаза жизненного цикла карточки для пользователя.
 * Определяет, как алгоритм FSRS обрабатывает карточку.
 *
 * @property value числовое значение
 */
enum class CardPhase(val value: Int) {
    /** Новая карточка, ещё ни разу не отвеченная */
    Added(0),

    /** Карточка в процессе переучивания (после неверного ответа) */
    ReLearning(1),

    /** Карточка на обычном повторении (основная фаза) */
    Review(2)
}

/**
 * Результат расчёта FSRS для одного варианта ответа.
 * Содержит новые параметры карточки и интервал до следующего показа.
 *
 * @param durationMillis длительность до следующего показа в миллисекундах.
 * @param interval интервал в днях (целое число).
 * @param choice оценка, к которой относится данный результат (Again, Hard, Good, Easy).
 * @param stability новая стабильность (параметр S в FSRS).
 * @param difficulty новая сложность (параметр D в FSRS).
 */
data class Grade(
    val durationMillis: Long = 0,
    val interval: Int = 0,
    val choice: Rating,
    val stability: Double = 0.0,
    val difficulty: Double = 0.0
)

/**
 * Прогресс пользователя по конкретной карточке.
 * Соответствует записи в таблице `user_card_progress` Supabase.
 * Содержит все поля, необходимые алгоритму FSRS для расчёта следующего интервала.
 *
 * @param id UUID записи (генерируется БД).
 * @param userId UUID пользователя (из auth.users).
 * @param cardId UUID карточки (из таблицы cards).
 * @param stability стабильность (параметр S).
 * @param difficulty сложность (параметр D).
 * @param interval текущий интервал в днях (0 для новой карточки).
 * @param dueDate дата и время, когда карточку нужно показать.
 * @param reviewCount общее количество повторений карточки.
 * @param lastReview дата и время последнего повторения.
 * @param phase текущая фаза (см. [CardPhase]).
 */
data class UserCardProgress(
    val id: String? = null,
    val userId: String,
    val cardId: String,
    var stability: Double = 2.5,
    var difficulty: Double = 2.5,
    var interval: Int = 0,
    var dueDate: Instant,
    var reviewCount: Int = 0,
    var lastReview: Instant,
    var phase: Int = 0
)

/**
 * Объективный результат ответа пользователя на тестовое задание.
 * Используется для автоматического определения оценки (Rating) без субъективной самооценки.
 *
 * @param accuracy точность ответа в диапазоне [0.0, 1.0]:
 *   - Для CHOICE, TRUE_FALSE: отношение 1/attempts.
 *   - Для INPUT: степень совпадения с эталоном (например, 0.8 — 80% совпадения).
 * @param responseTimeMs время ответа в миллисекундах.
 * @param testType тип задания.
 * @param attempts количество попыток.
 */
data class ObjectiveResult(
    val accuracy: Double,
    val responseTimeMs: Long,
    val testType: TestType,
    val attempts: Int
)