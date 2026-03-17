package com.soroh.intermind.core.ui.util

fun getCardWordForm(count: Int): String {
    val lastDigit = count % 10
    val lastTwoDigits = count % 100

    return when {
        lastTwoDigits in 11..19 -> "карточек"
        lastDigit == 1 -> "карточка"
        lastDigit in 2..4 -> "карточки"
        else -> "карточек"
    }
}