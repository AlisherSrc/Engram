package com.example.engram

import com.example.engram.model.ConstantCategory
import com.example.engram.viewmodel.PresqueVuViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PresqueVuViewModel verifying StateFlow architecture,
 * progressive obfuscation grading, wordsmith sentence practice history,
 * retention metrics computation, and reactive filtering.
 */
class PresqueVuViewModelTest {

    private lateinit var viewModel: PresqueVuViewModel

    @Before
    fun setUp() {
        viewModel = PresqueVuViewModel()
    }

    @Test
    fun initialData_containsRequiredMinimumItems() {
        val state = viewModel.uiState.value
        assertTrue("Constants must have at least 4 initial items", state.constants.size >= 4)
        assertTrue("Lexicon must have at least 4 initial items", state.lexicon.size >= 4)
        assertTrue("Total items should reflect combined sum", state.totalItems >= 8)
        assertNotNull("Spotlight phrase should not be null", state.spotlightPhrase)
    }

    @Test
    fun addConstant_insertsItemAtFrontAndUpdatesUiState() {
        val initialSize = viewModel.uiState.value.constants.size
        viewModel.addConstant(
            title = "Office Alarm Code",
            secretValue = "7391#",
            category = ConstantCategory.ADDRESS,
            hint = "Back entrance keypad",
            initialMastery = 2
        )

        val updatedList = viewModel.uiState.value.constants
        assertEquals(initialSize + 1, updatedList.size)
        assertEquals("Office Alarm Code", updatedList.first().title)
        assertEquals("7391#", updatedList.first().secretValue)
        assertEquals(ConstantCategory.ADDRESS, updatedList.first().category)
        assertEquals(2, updatedList.first().masteryLevel)
    }

    @Test
    fun addLexicon_insertsItemWithHashTagAndEmptyPracticeHistory() {
        val initialSize = viewModel.uiState.value.lexicon.size
        viewModel.addLexicon(
            title = "Solaris Ocean",
            phrase = "The living ocean was dreaming mathematical structures",
            meaningOrContext = "An incomprehensible alien intelligence manifesting through physical geometry",
            language = "en",
            tag = "Sci-Fi", // without hash, should auto-prefix
            sourceBook = "Solaris (Stanislaw Lem)",
            initialMastery = 1
        )

        val updatedList = viewModel.uiState.value.lexicon
        assertEquals(initialSize + 1, updatedList.size)
        assertEquals("Solaris Ocean", updatedList.first().title)
        assertEquals("#Sci-Fi", updatedList.first().tag)
        assertEquals("EN", updatedList.first().language)
        assertTrue("Newly added lexicon should have empty practice history", updatedList.first().practiceHistory.isEmpty())
    }

    @Test
    fun incrementMastery_cyclesThroughLevels() {
        val testItem = viewModel.uiState.value.constants.first()
        val originalMastery = testItem.masteryLevel

        viewModel.incrementConstantMastery(testItem.id)
        val updatedMastery = viewModel.uiState.value.constants.first { it.id == testItem.id }.masteryLevel

        val expectedNext = if (originalMastery >= 5) 0 else originalMastery + 1
        assertEquals(expectedNext, updatedMastery)
    }

    @Test
    fun recordConstantRecallResult_incrementsOnCorrectAndDecrementsOnFailure() {
        val testItem = viewModel.uiState.value.constants.first { it.masteryLevel in 1..4 }
        val startMastery = testItem.masteryLevel

        // Correct answer increases mastery
        viewModel.recordConstantRecallResult(testItem.id, isCorrect = true)
        val afterSuccess = viewModel.uiState.value.constants.first { it.id == testItem.id }.masteryLevel
        assertEquals(startMastery + 1, afterSuccess)

        // Incorrect answer decreases mastery
        viewModel.recordConstantRecallResult(testItem.id, isCorrect = false)
        val afterFail = viewModel.uiState.value.constants.first { it.id == testItem.id }.masteryLevel
        assertEquals(startMastery, afterFail)
    }

    @Test
    fun recordLexiconPracticeSentence_savesIntoPracticeHistoryAndIncrementsMastery() {
        val testItem = viewModel.uiState.value.lexicon.first()
        val initialSentenceCount = testItem.practiceHistory.size
        val initialMastery = testItem.masteryLevel

        val testSentence = "We sailed toward the horizon where the neon signs cast reflections."
        viewModel.recordLexiconPracticeSentence(testItem.id, testSentence)

        val updatedItem = viewModel.uiState.value.lexicon.first { it.id == testItem.id }
        assertEquals(initialSentenceCount + 1, updatedItem.practiceHistory.size)
        assertEquals(testSentence, updatedItem.practiceHistory.first())
        assertEquals((initialMastery + 1).coerceAtMost(5), updatedItem.masteryLevel)
    }

    @Test
    fun searchFiltering_constantsMatchesQueryReactively() {
        viewModel.setConstantsSearchQuery("Kcell")
        val filtered = viewModel.uiState.value.filteredConstants
        assertFalse("Should find mom's phone with hint mentioning Kcell", filtered.isEmpty())
        assertTrue(filtered.any { it.hint.contains("Kcell") })
    }

    @Test
    fun categoryFiltering_constantsLimitsToCategory() {
        viewModel.setConstantCategoryFilter(ConstantCategory.CONTACT)
        val filtered = viewModel.uiState.value.filteredConstants
        assertTrue(filtered.all { it.category == ConstantCategory.CONTACT })
    }

    @Test
    fun lexiconFiltering_byLanguageAndTag() {
        viewModel.setLexiconFilter("KZ")
        val kzItems = viewModel.uiState.value.filteredLexicon
        assertTrue(kzItems.all { it.language.equals("KZ", ignoreCase = true) })

        viewModel.setLexiconFilter("#Sci-Fi")
        val sciFiItems = viewModel.uiState.value.filteredLexicon
        assertTrue(sciFiItems.all { it.tag.equals("#Sci-Fi", ignoreCase = true) })
    }

    @Test
    fun deleteItem_removesItemFromUiState() {
        val firstConstant = viewModel.uiState.value.constants.first()
        val originalCount = viewModel.uiState.value.constants.size

        viewModel.deleteConstant(firstConstant.id)
        assertEquals(originalCount - 1, viewModel.uiState.value.constants.size)
        assertFalse(viewModel.uiState.value.constants.any { it.id == firstConstant.id })
    }

    @Test
    fun refreshSpotlightPhrase_changesSpotlightOrMaintainsValidPhrase() {
        val originalId = viewModel.uiState.value.spotlightLexiconId
        viewModel.refreshSpotlightPhrase()
        val newSpotlight = viewModel.uiState.value.spotlightPhrase
        assertNotNull(newSpotlight)
    }
}
