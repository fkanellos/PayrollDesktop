package com.payroll.app.desktop.domain.service

import com.payroll.app.desktop.domain.models.MatchConfidence
import kotlin.test.*

/**
 * Unit Tests for ClientMatchingService
 * Tests fuzzy matching logic for client name matching
 */
class ClientMatchingServiceTest {

    private lateinit var service: ClientMatchingService

    @BeforeTest
    fun setup() {
        service = ClientMatchingService()
    }

    @Test
    fun `exact match returns client name`() {
        // Given
        val clientNames = listOf("John Doe", "Jane Smith")
        val eventTitle = "Session with John Doe"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertEquals(1, matches.size, "Should find 1 exact match")
        assertEquals("John Doe", matches[0])
    }

    @Test
    fun `case insensitive match`() {
        // Given
        val clientNames = listOf("John Doe")
        val eventTitle = "session with JOHN DOE"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertEquals(1, matches.size)
        assertEquals("John Doe", matches[0])
    }

    @Test
    fun `partial name match - first name only`() {
        // Given
        val clientNames = listOf("John Doe Smith")
        val eventTitle = "Session with John"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertTrue(matches.isNotEmpty(), "Should match partial first name")
        assertEquals("John Doe Smith", matches[0])
    }

    @Test
    fun `partial name match - last name only`() {
        // Given
        val clientNames = listOf("John Doe Smith")
        val eventTitle = "Session with Smith"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertTrue(matches.isNotEmpty(), "Should match partial last name")
        assertEquals("John Doe Smith", matches[0])
    }

    @Test
    fun `fuzzy match with typo`() {
        // Given
        val clientNames = listOf("Konstantinos")
        val eventTitle = "Session with Konstntinos" // Missing 'a'

        // When
        val matchesWithConfidence = service.findClientMatchesWithConfidence(eventTitle, clientNames)

        // Then
        assertTrue(matchesWithConfidence.isNotEmpty(), "Should find fuzzy match")
        assertEquals("Konstantinos", matchesWithConfidence[0].clientName)
        assertTrue(
            matchesWithConfidence[0].confidence in listOf(MatchConfidence.HIGH, MatchConfidence.MEDIUM),
            "Should have HIGH or MEDIUM confidence for small typo"
        )
    }

    @Test
    fun `no match for completely different name`() {
        // Given
        val clientNames = listOf("John Doe")
        val eventTitle = "Session with Alice Brown"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertEquals(0, matches.size, "Should not match completely different name")
    }

    @Test
    fun `special keyword match`() {
        // Given
        val clientNames = listOf("John Doe")
        val specialKeywords = listOf("supervision", "εποπτεία")
        val eventTitle = "Supervision meeting"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames, specialKeywords)

        // Then
        assertEquals(1, matches.size, "Should match special keyword")
        assertEquals("supervision", matches[0])
    }

    @Test
    fun `Greek name matching`() {
        // Given
        val clientNames = listOf("Κωνσταντίνος Παπαδόπουλος")
        val eventTitle = "Συνεδρία με Κωνσταντίνος"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertTrue(matches.isNotEmpty(), "Should match Greek names")
        assertEquals("Κωνσταντίνος Παπαδόπουλος", matches[0])
    }

    @Test
    fun `confidence scoring - EXACT vs HIGH vs MEDIUM`() {
        // Given
        val clientNames = listOf("John Doe")

        // When
        val exactMatch = service.findClientMatchesWithConfidence("John Doe", clientNames)
        val highMatch = service.findClientMatchesWithConfidence("John", clientNames)
        val mediumMatch = service.findClientMatchesWithConfidence("Joh", clientNames)

        // Then
        assertEquals(MatchConfidence.EXACT, exactMatch[0].confidence, "Should be EXACT")
        assertEquals(MatchConfidence.HIGH, highMatch[0].confidence, "Should be HIGH")
        assertTrue(
            mediumMatch[0].confidence == MatchConfidence.MEDIUM || mediumMatch[0].confidence == MatchConfidence.HIGH,
            "Should be MEDIUM or HIGH"
        )
    }

    @Test
    fun `empty event title returns no matches`() {
        // Given
        val clientNames = listOf("John Doe")
        val eventTitle = ""

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertEquals(0, matches.size, "Empty title should not match")
    }

    @Test
    fun `whitespace-only event title returns no matches`() {
        // Given
        val clientNames = listOf("John Doe")
        val eventTitle = "   "

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertEquals(0, matches.size, "Whitespace-only title should not match")
    }

    @Test
    fun `multiple possible matches returns all candidates`() {
        // Given
        val clientNames = listOf("John Doe", "John Smith", "John Williams")
        val eventTitle = "Meeting with John"

        // When
        val matches = service.findClientMatches(eventTitle, clientNames)

        // Then
        assertTrue(matches.size >= 1, "Should find at least one match")
        assertTrue(matches.all { it.startsWith("John") }, "All matches should start with 'John'")
    }

    @Test
    fun `accent insensitive matching`() {
        // Given
        val clientNames = listOf("José García")
        val eventTitle = "Session with Jose Garcia" // Without accents

        // When
        val matches = service.findClientMatchesWithConfidence(eventTitle, clientNames)

        // Then
        assertTrue(matches.isNotEmpty(), "Should match despite missing accents")
        assertEquals("José García", matches[0].clientName)
    }

    @Test
    fun `punctuation and special characters ignored`() {
        // Given
        val clientNames = listOf("O'Brien")
        val eventTitle = "Session with OBrien"

        // When
        val matches = service.findClientMatchesWithConfidence(eventTitle, clientNames)

        // Then
        assertTrue(matches.isNotEmpty(), "Should match ignoring punctuation")
        assertEquals("O'Brien", matches[0].clientName)
    }

    @Test
    fun `match with confidence returns proper result`() {
        // Given
        val clientNames = listOf("John Doe")
        val eventTitle = "John Doe session"

        // When
        val matches = service.findClientMatchesWithConfidence(eventTitle, clientNames)

        // Then
        assertTrue(matches.isNotEmpty(), "Should find match")
        assertEquals("John Doe", matches[0].clientName)
        assertEquals(MatchConfidence.EXACT, matches[0].confidence)
        assertTrue(matches[0].matchedText.isNotEmpty(), "Should have matched text")
        assertTrue(matches[0].reason.isNotEmpty(), "Should have reason")
    }
}
