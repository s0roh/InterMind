package com.soroh.intermind.core.data.util

internal fun generatePartialAnswer(answer: String): Triple<String, List<String>, Int> {
    val words = answer.trim().split(Regex("\\s+"))

    // Если слов мало, скрываем всё или ничего
    if (words.size < MIN_WORDS_FOR_BLANK) {
        return Triple("_".repeat(answer.length), listOf(answer), 0)
    }

    val maxMissing = (words.size * MAX_BLANK_PERCENT / 100).coerceIn(1, MAX_MISSING_WORDS)
    val missingCount = (1..maxMissing).random()
    val startIndex = (0..(words.size - missingCount)).random()

    val missingWords = words.subList(startIndex, startIndex + missingCount)
    
    val partialAnswer = words.toMutableList().apply {
        for (i in startIndex until (startIndex + missingCount)) {
            // Вместо простого "_", сохраняем визуальную длину слова
            this[i] = "_".repeat(this[i].length)
        }
    }.joinToString(" ")

    return Triple(partialAnswer, missingWords, startIndex)
}

private const val MIN_WORDS_FOR_BLANK = 3
private const val MAX_BLANK_PERCENT = 50
private const val MAX_MISSING_WORDS = 5