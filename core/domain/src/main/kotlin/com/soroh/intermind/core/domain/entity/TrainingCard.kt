package com.soroh.intermind.core.domain.entity

/**
 * Представляет карточку для тренировок в системе интервального повторения.
 *
 * Карточка содержит вопрос, отображаемый пользователю, и правильный ответ. Она используется
 * в процессе тренировки, чтобы оценить, как хорошо пользователь запомнил материал.
 *
 * @property id Уникальный идентификатор карточки.
 * @property testType Режим тренировки, который определяет тип карточки (множественный выбор, правда/неправда, дополнить ответ).
 * @property question Вопрос, отображаемый пользователю.
 * @property answer Правильный ответ на вопрос.
 * @property wrongAnswers Список неверных ответов для режима множественного выбора (по умолчанию пустой).
 * @property displayedAnswer Отображаемый ответ в режиме "Правильно или нет".
 * @property partialAnswer Частичный ответ в режиме "Дополнить ответ", где часть ответа скрыта.
 * @property missingWords Список слов, которые необходимо дополнить в режиме "Дополнить ответ".
 * @property missingWordStartIndex Индекс в ответе, с которого начинаются пропуски.
 */
data class TrainingCard(
    val id: String,
    val testType: TestType,
    val question: String,
    val answer: String,
    val wrongAnswers: List<String> = emptyList(),
    val displayedAnswer: String? = null,
    val partialAnswer: String? = null,
    val attachment: String? = null,
    val missingWords: List<String> = emptyList(),
    val missingWordStartIndex: Int = -1,
)

/**
 * Тип тестового задания, используемый при сборе объективных метрик.
 */
enum class TestType {
    /** Тест с выбором одного правильного ответа из нескольких вариантов */
    CHOICE,

    /** Тест с вводом текста (пользователь вводит ответ) */
    INPUT,

    /** Тест "правда/ложь" */
    TRUE_FALSE
}
