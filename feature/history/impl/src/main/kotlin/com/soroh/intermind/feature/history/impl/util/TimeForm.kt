package com.soroh.intermind.feature.history.impl.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/**
 * Форматирует [Instant] из kotlinx.datetime в заголовок даты (Сегодня, Вчера, и т.д.)
 */
internal fun Instant.formatToDateHeader(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val javaInstant = this.toJavaInstant()
    val itemDate = javaInstant.atZone(zoneId).toLocalDate()
    val today = LocalDate.now(zoneId)

    return when (itemDate) {
        today -> "Сегодня"
        today.minusDays(1) -> "Вчера"
        else -> {
            val pattern = if (itemDate.year == today.year) {
                "d MMMM"
            } else {
                "d MMMM yyyy"
            }

            DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("ru"))
                .format(itemDate)
        }
    }
}