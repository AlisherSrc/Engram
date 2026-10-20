package com.example.engram

import com.example.engram.model.ConstantCategory
import com.example.engram.viewmodel.PresqueVuViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PresqueVuViewModel verifying business logic,
 * filtering calculations, mastery mutation, and initial dataset integrity.
 */
class PresqueVuViewModelTest {

    private lateinit var viewModel: PresqueVuViewModel

    @Before
    fun setUp() {
        viewModel = PresqueVuViewModel()
    }

    @Test
    fun initialData_containsRequiredMinimumItems() {
        // Spec requires at least 4 Constants and 4 Lexicon items
        assertTrue("Constants must have at least 4 initial items", viewModel.constants.value.size >= 4)
        assertTrue("Lexicon must have at least 4 initial items", viewModel.lexicon.value.size >= 4)
    }

    @Test
    fun addConstant_insertsItemAtFront() {
        val initialSize = viewModel.constants.value.size
        viewModel.addConstant(
            title = "Office Alarm Code",
            secretValue = "7391#",
            category = ConstantCategory.ADDRESS,
            hint = "Back entrance keypad",
            initialMastery = 2
        )

        val updatedList = viewModel.constants.value
        assertEquals(initialSize + 1, updatedList.size)
        assertEquals("Office Alarm Code", updatedList.first().title)
        assertEquals("7391#", updatedList.first().secretValue)
        assertEquals(ConstantCategory.ADDRESS, updatedList.first().category)
        assertEquals(2, updatedList.first().masteryLevel)
    }

    @Test
    fun addLexicon_insertsItemWithHashTag() {
        val initialSize = viewModel.lexicon.value.size
        viewModel.addLexicon(
            title = "Solaris Ocean",
            phrase = "The living ocean was dreaming mathematical structures",
            meaningOrContext = "An incomprehensible alien intelligence manifesting through physical geometry",
            language = "en",
            tag = "Sci-Fi", // without hash, should auto-prefix
            sourceBook = "Solaris (Stanislaw Lem)",
            initialMastery = 1
        )

        val updatedList = viewModel.lexicon.value
        assertEquals(initialSize + 1, updatedList.size)
        assertEquals("Solaris Ocean", updatedList.first().title)
        assertEquals("#Sci-Fi", updatedList.first().tag)
        assertEquals("EN", updatedList.first().language)
    }

    @Test
    fun incrementMastery_cyclesThroughLevels() {
        val testItem = viewModel.constants.value.first()
        val originalMastery = testItem.masteryLevel

        viewModel.incrementConstantMastery(testItem.id)
        val updatedMastery = viewModel.constants.value.first { it.id == testItem.id }.masteryLevel

        val expectedNext = if (originalMastery >= 5) 0 else originalMastery + 1
        assertEquals(expectedNext, updatedMastery)
    }

    @Test
    fun searchFiltering_constantsMatchesQuery() {
        val matches = viewModel.getFilteredConstants("Kcell")
        assertFalse("Should find mom's phone with hint mentioning Kcell", matches.isEmpty())
        assertTrue(matches.any { it.hint.contains("Kcell") })
    }

    @Test
    fun categoryFiltering_constantsLimitsToCategory() {
        viewModel.setConstantCategoryFilter(ConstantCategory.CONTACT)
        val filtered = viewModel.getFilteredConstants("", ConstantCategory.CONTACT)
        assertTrue(filtered.all { it.category == ConstantCategory.CONTACT })
    }

    @Test
    fun lexiconFiltering_byLanguageAndTag() {
        val kzItems = viewModel.getFilteredLexicon("", "KZ")
        assertTrue(kzItems.all { it.language.equals("KZ", ignoreCase = true) })

        val sciFiItems = viewModel.getFilteredLexicon("", "#Sci-Fi")
        assertTrue(sciFiItems.all { it.tag.equals("#Sci-Fi", ignoreCase = true) })
    }

    @Test
    fun deleteItem_removesItem() {
        val firstConstant = viewModel.constants.value.first()
        val originalCount = viewModel.constants.value.size

        viewModel.deleteConstant(firstConstant.id)
        assertEquals(originalCount - 1, viewModel.constants.value.size)
        assertFalse(viewModel.constants.value.any { it.id == firstConstant.id })
    }
}
