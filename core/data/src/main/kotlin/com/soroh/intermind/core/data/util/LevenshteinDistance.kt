package com.soroh.intermind.core.data.util

import kotlin.math.min

/**
 * Функция для вычисления расстояния Левенштейна между двумя строками.
 *
 * Расстояние Левенштейна - это минимальное количество операций (вставка, удаление, замена),
 * необходимых для преобразования одной строки в другую.
 *
 * @param s1 Первая строка.
 * @param s2 Вторая строка.
 * @return Расстояние Левенштейна между строками s1 и s2.
 */
internal fun levenshteinDistance(s1: String, s2: String): Int {
    val m = s1.length
    val n = s2.length

    // Если длина первой строки меньше второй, меняем их местами
    if (m < n) return levenshteinDistance(s2, s1)

    var prevRow = IntArray(n + 1) { it } // Инициализация первой строки расстояний
    var currRow = IntArray(n + 1) // Текущая строка расстояний

    // Обрабатываем все символы первой строки
    for (i in 1..m) {
        currRow[INITIAL_INDEX] = i // Устанавливаем стоимость удаления всех символов до i
        // Обрабатываем все символы второй строки
        for (j in 1..n) {
            // Определяем стоимость замены символа
            val cost =
                if (s1[i - INDEX_OFFSET] == s2[j - INDEX_OFFSET]) MATCH_COST else MISMATCH_COST
            // Вычисляем минимальную стоимость среди вставки, удаления и замены
            currRow[j] = min(
                min(
                    currRow[j - INDEX_OFFSET] + INSERTION_COST,  // Вставка
                    prevRow[j] + DELETION_COST                   // Удаление
                ),
                prevRow[j - INDEX_OFFSET] + cost               // Замена
            )
        }
        prevRow = currRow.also { currRow = prevRow } // Меняем строки местами для экономии памяти
    }

    return prevRow[n] // Возвращаем последнее значение, которое является расстоянием Левенштейна
}

// Константы для стоимости операций
private const val INITIAL_INDEX = 0 // Начальный индекс массива
private const val INDEX_OFFSET = 1 // Смещение индекса для удобства работы с массивами
private const val MATCH_COST = 0 // Стоимость совпадения символов
private const val MISMATCH_COST = 1 // Стоимость замены символа
private const val INSERTION_COST = 1 // Стоимость вставки символа
private const val DELETION_COST = 1 // Стоимость удаления символа
