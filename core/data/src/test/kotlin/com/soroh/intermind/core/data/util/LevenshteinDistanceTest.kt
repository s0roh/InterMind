package com.soroh.intermind.core.data.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class LevenshteinDistanceTest {

    @Test
    fun shouldReturnZeroWhenBothStringsAreEmpty() {
        assertEquals(0, levenshteinDistance("", ""))
    }

    @Test
    fun shouldReturnLengthOfNonEmptyStringWhenOneStringIsEmpty() {
        assertEquals(5, levenshteinDistance("", "hello"))
        assertEquals(5, levenshteinDistance("hello", ""))
    }

    @Test
    fun shouldReturnZeroWhenStringsAreIdentical() {
        assertEquals(0, levenshteinDistance("kitten", "kitten"))
    }

    @Test
    fun shouldReturnCorrectDistanceForDifferentStrings() {
        assertEquals(3, levenshteinDistance("kitten", "sitting"))
    }

    @Test
    fun shouldCalculateDistanceCorrectlyForMultipleWords() {
        assertEquals(1, levenshteinDistance("hello world", "hell world"))
        assertEquals(7, levenshteinDistance("one two tree four", "uno tvo triii for"))
    }

    @Test
    fun shouldReturnCorrectDistanceForMixedOperations() {
        assertEquals(2, levenshteinDistance("flaw", "lawn"))
    }
}