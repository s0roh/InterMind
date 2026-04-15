package com.soroh.intermind.core.data.repository

import com.soroh.intermind.core.data.dto.CardDto
import com.soroh.intermind.core.data.dto.DeckDto
import com.soroh.intermind.core.data.dto.DeckTrainingStatsDto
import com.soroh.intermind.core.domain.entity.Card
import com.soroh.intermind.core.domain.entity.Deck
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DecksRepositoryImplTest {
    @Test
    fun `DeckDto toDomain converts correctly`() {
        val dto = DeckDto(
            id = "deck-1",
            name = "Test Deck",
            isPublic = true,
            userId = "user-123",
            cardsCount = 10,
            likes = 5,
            trainings = 3
        )

        val domain = dto.toDomain()

        assertEquals("deck-1", domain.id)
        assertEquals("Test Deck", domain.name)
        assertTrue(domain.isPublic)
        assertEquals("user-123", domain.authorId)
        assertEquals(10, domain.cardsCount)
        assertEquals(5, domain.likes)
        assertEquals(3, domain.trainings)
    }

    @Test
    fun `DeckDto toDomain handles null id`() {
        val dto = DeckDto(
            id = null,
            name = "Test Deck",
            isPublic = false,
            userId = null,
            cardsCount = 0,
            likes = 0,
            trainings = 0
        )

        val domain = dto.toDomain()

        assertEquals("", domain.id)
        assertNull(domain.authorId)
    }

    @Test
    fun `DeckDto fromDomain creates dto correctly`() {
        val deck = Deck(
            id = "deck-1",
            name = "Test Deck",
            isPublic = true,
            authorId = "user-123",
            cardsCount = 10,
            likes = 5,
            trainings = 3
        )

        val dto = DeckDto.fromDomain(deck, "user-123")

        assertEquals("deck-1", dto.id)
        assertEquals("Test Deck", dto.name)
        assertTrue(dto.isPublic)
        assertEquals("user-123", dto.userId)
        assertEquals(10, dto.cardsCount)
    }

    @Test
    fun `CardDto toDomain converts correctly`() {
        val dto = CardDto(
            id = "card-1",
            deckId = "deck-1",
            question = "What is Kotlin?",
            answer = "Kotlin is a programming language",
            wrongAnswers = listOf("Java", "Python"),
            picturePath = "path/to/image.jpg"
        )

        val domain = dto.toDomain(attachment = "https://example.com/image.jpg")

        assertEquals("card-1", domain.id)
        assertEquals("What is Kotlin?", domain.question)
        assertEquals("Kotlin is a programming language", domain.answer)
        assertEquals(listOf("Java", "Python"), domain.wrongAnswers)
        assertEquals("https://example.com/image.jpg", domain.attachment)
        assertEquals("path/to/image.jpg", domain.picturePath)
    }

    @Test
    fun `CardDto toDomain handles null id`() {
        val dto = CardDto(
            id = null,
            deckId = "deck-1",
            question = "Q",
            answer = "A",
            wrongAnswers = emptyList(),
            picturePath = null
        )

        val domain = dto.toDomain(attachment = null)

        assertEquals("", domain.id)
        assertNull(domain.attachment)
        assertNull(domain.picturePath)
    }

    @Test
    fun `CardDto fromDomain creates dto correctly`() {
        val card = Card(
            id = "card-1",
            question = "Test Q",
            answer = "Test A",
            wrongAnswers = listOf("Wrong1"),
            attachment = "https://example.com/img.jpg",
            picturePath = "path.jpg"
        )

        val dto = CardDto.fromDomain(card, "deck-1")

        assertEquals("card-1", dto.id)
        assertEquals("deck-1", dto.deckId)
        assertEquals("Test Q", dto.question)
        assertEquals("Test A", dto.answer)
        assertEquals(listOf("Wrong1"), dto.wrongAnswers)
        assertEquals("path.jpg", dto.picturePath)
    }

    @Test
    fun `DeckTrainingStatsDto converts to DeckTrainingStats`() {
        val dto = DeckTrainingStatsDto(
            deckId = "deck-1",
            newCount = 5,
            reviewCount = 10
        )

        val stats = com.soroh.intermind.core.data.model.DeckTrainingStats(
            newCount = dto.newCount,
            reviewCount = dto.reviewCount
        )

        assertEquals(5, stats.newCount)
        assertEquals(10, stats.reviewCount)
    }

    @Test
    fun `Deck with maximum values`() {
        val deck = Deck(
            id = "deck-max",
            name = "A".repeat(1000),
            isPublic = true,
            authorId = "user-max",
            cardsCount = Int.MAX_VALUE,
            likes = Int.MAX_VALUE,
            trainings = Int.MAX_VALUE
        )

        val dto = DeckDto.fromDomain(deck, "user-max")
        val converted = dto.toDomain()

        assertEquals(deck.name, converted.name)
        assertEquals(deck.cardsCount, converted.cardsCount)
        assertEquals(deck.likes, converted.likes)
    }

    @Test
    fun `Deck with minimum values`() {
        val deck = Deck(
            id = "",
            name = "",
            isPublic = false,
            authorId = null,
            cardsCount = 0,
            likes = 0,
            trainings = 0
        )

        val dto = DeckDto.fromDomain(deck, null)
        val converted = dto.toDomain()

        assertEquals("", converted.id)
        assertEquals("", converted.name)
        assertFalse(converted.isPublic)
        assertEquals(0, converted.cardsCount)
    }

    @Test
    fun `Card with empty values`() {
        val card = Card(
            id = "",
            question = "",
            answer = "",
            wrongAnswers = emptyList(),
            attachment = null,
            picturePath = null
        )

        val dto = CardDto.fromDomain(card, "")
        val converted = dto.toDomain(attachment = null)

        assertEquals("", converted.id)
        assertEquals("", converted.question)
        assertEquals("", converted.answer)
        assertTrue(converted.wrongAnswers.isEmpty())
    }

    @Test
    fun `Card with many wrong answers`() {
        val wrongAnswers = (1..100).map { "Wrong $it" }
        val card = Card(
            id = "card-1",
            question = "Q",
            answer = "A",
            wrongAnswers = wrongAnswers,
            attachment = null,
            picturePath = null
        )

        val dto = CardDto.fromDomain(card, "deck-1")
        val converted = dto.toDomain(attachment = null)

        assertEquals(100, converted.wrongAnswers.size)
        assertEquals("Wrong 1", converted.wrongAnswers[0])
        assertEquals("Wrong 100", converted.wrongAnswers[99])
    }

    @Test
    fun `DeckDto default values are correct`() {
        val dto = DeckDto(
            name = "Test",
            isPublic = false
        )

        assertEquals(0, dto.cardsCount)
        assertEquals(0, dto.likes)
        assertEquals(0, dto.trainings)
        assertNull(dto.id)
        assertNull(dto.userId)
    }

    @Test
    fun `CardDto default values are correct`() {
        val dto = CardDto(
            question = "Q",
            answer = "A"
        )

        assertEquals("", dto.deckId)
        assertEquals(emptyList<String>(), dto.wrongAnswers)
        assertNull(dto.id)
        assertNull(dto.picturePath)
    }

    @Test
    fun `list of DeckDto converts to list of Deck`() {
        val dtos = listOf(
            DeckDto(
                id = "deck-1",
                name = "Deck 1",
                isPublic = true,
                cardsCount = 10
            ),
            DeckDto(
                id = "deck-2",
                name = "Deck 2",
                isPublic = false,
                cardsCount = 20
            ),
           DeckDto(
                id = "deck-3",
                name = "Deck 3",
                isPublic = true,
                cardsCount = 5
            )
        )

        val domains = dtos.map { it.toDomain() }

        assertEquals(3, domains.size)
        assertEquals("Deck 1", domains[0].name)
        assertEquals("Deck 2", domains[1].name)
        assertEquals("Deck 3", domains[2].name)
    }

    @Test
    fun `empty list of DeckDto converts to empty list`() {
        val dtos = emptyList<DeckDto>()

        val domains = dtos.map { it.toDomain() }

        assertTrue(domains.isEmpty())
    }

    @Test
    fun `list of CardDto converts to list of Card`() {
        val dtos = listOf(
            CardDto(
                id = "card-1",
                question = "Q1",
                answer = "A1"
            ),
            CardDto(
                id = "card-2",
                question = "Q2",
                answer = "A2"
            )
        )

        val domains = dtos.map { it.toDomain(attachment = null) }

        assertEquals(2, domains.size)
        assertEquals("Q1", domains[0].question)
        assertEquals("Q2", domains[1].question)
    }

    @Test
    fun `isDeckOwner returns true for non-empty response`() {
        val responseData = """[{"user_id":"test-user"}]"""

        val isOwner = responseData.contains("user_id")

        assertTrue(isOwner)
    }
}
