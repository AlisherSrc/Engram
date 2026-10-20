package com.example.engram.viewmodel

import androidx.lifecycle.ViewModel
import com.example.engram.model.ConstantCategory
import com.example.engram.model.ConstantItem
import com.example.engram.model.LexiconItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * ViewModel for the Presque Vu application.
 * Manages the repository of memory nodes (Constants and Lexicon),
 * active category/tag filters, and provides filtering logic for live search.
 */
class PresqueVuViewModel : ViewModel() {

    // Master list of vital constants
    private val _constants = MutableStateFlow<List<ConstantItem>>(initialConstants())
    val constants: StateFlow<List<ConstantItem>> = _constants.asStateFlow()

    // Master list of lexicon / wordsmith phrases
    private val _lexicon = MutableStateFlow<List<LexiconItem>>(initialLexicon())
    val lexicon: StateFlow<List<LexiconItem>> = _lexicon.asStateFlow()

    // Active category filter for Constants (null = All)
    private val _selectedConstantCategory = MutableStateFlow<ConstantCategory?>(null)
    val selectedConstantCategory: StateFlow<ConstantCategory?> = _selectedConstantCategory.asStateFlow()

    // Active filter for Lexicon (Language e.g. "EN", "RU", "KZ" or Tag e.g. "#Sci-Fi", null = All)
    private val _selectedLexiconFilter = MutableStateFlow<String?>(null)
    val selectedLexiconFilter: StateFlow<String?> = _selectedLexiconFilter.asStateFlow()

    // --- Filter Management ---

    fun setConstantCategoryFilter(category: ConstantCategory?) {
        _selectedConstantCategory.value = category
    }

    fun setLexiconFilter(filter: String?) {
        _selectedLexiconFilter.value = filter
    }

    // --- Filtered List Computation (driven by UI search query + active filters) ---

    fun getFilteredConstants(query: String, category: ConstantCategory? = _selectedConstantCategory.value): List<ConstantItem> {
        val trimmed = query.trim().lowercase()
        return _constants.value.filter { item ->
            val matchesCategory = category == null || item.category == category
            val matchesQuery = trimmed.isEmpty() ||
                    item.title.lowercase().contains(trimmed) ||
                    item.hint.lowercase().contains(trimmed) ||
                    item.category.displayName.lowercase().contains(trimmed) ||
                    item.secretValue.lowercase().contains(trimmed)
            matchesCategory && matchesQuery
        }
    }

    fun getFilteredLexicon(query: String, filter: String? = _selectedLexiconFilter.value): List<LexiconItem> {
        val trimmed = query.trim().lowercase()
        return _lexicon.value.filter { item ->
            val matchesFilter = when {
                filter == null -> true
                filter.startsWith("#") -> item.tag.equals(filter, ignoreCase = true)
                else -> item.language.equals(filter, ignoreCase = true)
            }
            val matchesQuery = trimmed.isEmpty() ||
                    item.phrase.lowercase().contains(trimmed) ||
                    item.meaningOrContext.lowercase().contains(trimmed) ||
                    item.title.lowercase().contains(trimmed) ||
                    item.tag.lowercase().contains(trimmed) ||
                    item.sourceBook.lowercase().contains(trimmed) ||
                    item.language.lowercase().contains(trimmed)
            matchesFilter && matchesQuery
        }
    }

    // --- Item Mutations ---

    fun addConstant(
        title: String,
        secretValue: String,
        category: ConstantCategory,
        hint: String,
        initialMastery: Int = 0
    ) {
        val newItem = ConstantItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            dateAdded = System.currentTimeMillis(),
            masteryLevel = initialMastery.coerceIn(0, 5),
            secretValue = secretValue.trim(),
            category = category,
            hint = hint.trim()
        )
        _constants.value = listOf(newItem) + _constants.value
    }

    fun addLexicon(
        title: String,
        phrase: String,
        meaningOrContext: String,
        language: String,
        tag: String,
        sourceBook: String,
        initialMastery: Int = 0
    ) {
        val newItem = LexiconItem(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            dateAdded = System.currentTimeMillis(),
            masteryLevel = initialMastery.coerceIn(0, 5),
            phrase = phrase.trim(),
            meaningOrContext = meaningOrContext.trim(),
            language = language.trim().uppercase(),
            tag = if (tag.startsWith("#")) tag.trim() else "#${tag.trim()}",
            sourceBook = sourceBook.trim(),
            )
        _lexicon.value = listOf(newItem) + _lexicon.value
    }

    fun incrementConstantMastery(id: String) {
        _constants.value = _constants.value.map { item ->
            if (item.id == id) {
                // Cycle 0 -> 1 -> 2 -> 3 -> 4 -> 5 -> 0 for interactive practice
                val next = if (item.masteryLevel >= 5) 0 else item.masteryLevel + 1
                item.copy(masteryLevel = next)
            } else item
        }
    }

    fun incrementLexiconMastery(id: String) {
        _lexicon.value = _lexicon.value.map { item ->
            if (item.id == id) {
                val next = if (item.masteryLevel >= 5) 0 else item.masteryLevel + 1
                item.copy(masteryLevel = next)
            } else item
        }
    }

    fun deleteConstant(id: String) {
        _constants.value = _constants.value.filterNot { it.id == id }
    }

    fun deleteLexicon(id: String) {
        _lexicon.value = _lexicon.value.filterNot { it.id == id }
    }

    // --- Initial Mock Data ---

    private fun initialConstants(): List<ConstantItem> = listOf(
        ConstantItem(
            id = "c-1",
            title = "Mom's Primary Cell Phone",
            dateAdded = System.currentTimeMillis() - 86400000L * 14,
            masteryLevel = 4,
            secretValue = "+7 (701) 555-0142",
            category = ConstantCategory.CONTACT,
            hint = "Kcell operator prefix (701), finishes with the cosmic 42"
        ),
        ConstantItem(
            id = "c-2",
            title = "Home Gate & Intercom Code",
            dateAdded = System.currentTimeMillis() - 86400000L * 30,
            masteryLevel = 5,
            secretValue = "#4829*B",
            category = ConstantCategory.ADDRESS,
            hint = "Floor 4, Apartment 82, gate entrance code ending in B"
        ),
        ConstantItem(
            id = "c-3",
            title = "National ID / IIN Number",
            dateAdded = System.currentTimeMillis() - 86400000L * 60,
            masteryLevel = 3,
            secretValue = "021114501982",
            category = ConstantCategory.ID_NUMBER,
            hint = "Year 02, Month 11, Day 14, Century indicator 5"
        ),
        ConstantItem(
            id = "c-4",
            title = "Best Friend's Birthday",
            dateAdded = System.currentTimeMillis() - 86400000L * 45,
            masteryLevel = 2,
            secretValue = "November 14, 2002",
            category = ConstantCategory.DATE,
            hint = "Mid-November Scorpio season celebration"
        ),
        ConstantItem(
            id = "c-5",
            title = "Personal Physician Direct Line",
            dateAdded = System.currentTimeMillis() - 86400000L * 5,
            masteryLevel = 1,
            secretValue = "+7 (777) 839-2041",
            category = ConstantCategory.CONTACT,
            hint = "Dr. Aliyev at MedCenter, extension 4"
        )
    )

    private fun initialLexicon(): List<LexiconItem> = listOf(
        LexiconItem(
            id = "l-1",
            title = "Tannhäuser Gate Vision",
            dateAdded = System.currentTimeMillis() - 86400000L * 25,
            masteryLevel = 4,
            phrase = "C-beams glitter in the dark near the Tannhäuser Gate",
            meaningOrContext = "A transcendent vision of fleeting cosmic beauty disappearing like tears in rain.",
            language = "EN",
            tag = "#Sci-Fi",
            sourceBook = "Do Androids Dream of Electric Sheep? (Blade Runner)"
        ),
        LexiconItem(
            id = "l-2",
            title = "Dead Channel Horizon",
            dateAdded = System.currentTimeMillis() - 86400000L * 40,
            masteryLevel = 5,
            phrase = "The sky above the port was the color of television, tuned to a dead channel",
            meaningOrContext = "An iconic cyberpunk opening establishing an oppressive, electric, technological atmosphere.",
            language = "EN",
            tag = "#Atmosphere",
            sourceBook = "Neuromancer (William Gibson)"
        ),
        LexiconItem(
            id = "l-3",
            title = "Крик души у Золотого Шара",
            dateAdded = System.currentTimeMillis() - 86400000L * 18,
            masteryLevel = 3,
            phrase = "Счастье для всех, даром, и пусть никто не уйдет обиженным!",
            meaningOrContext = "Предельное выражение бескорыстной человеческой надежды перед лицом непостижимой Зоны.",
            language = "RU",
            tag = "#Sci-Fi",
            sourceBook = "Пикник на обочине (А. и Б. Стругацкие)"
        ),
        LexiconItem(
            id = "l-4",
            title = "Шабыт тұлпары",
            dateAdded = System.currentTimeMillis() - 86400000L * 10,
            masteryLevel = 4,
            phrase = "Көңіл көк дөнендей алып ұшып тұр",
            meaningOrContext = "Жас әрі қажырлы тұлпардай тыным таппай, еркіндікке ұмтылған шабыт пен көтеріңкі рухты сипаттау.",
            language = "KZ",
            tag = "#Atmosphere",
            sourceBook = "Қазақ классикалық әдебиеті"
        ),
        LexiconItem(
            id = "l-5",
            title = "Ғарыштық тыныс",
            dateAdded = System.currentTimeMillis() - 86400000L * 7,
            masteryLevel = 2,
            phrase = "Жұлдыздар арасындағы тылсым үнсіздік — мәңгілік пен болмыстың тынысы",
            meaningOrContext = "Шексіз ғарыш кеңістігіндегі тыныштық пен адам баласының ғаламдағы орны туралы философиялық ой толғау.",
            language = "KZ",
            tag = "#Sci-Fi",
            sourceBook = "Ғарыштық хикаялар жинағы"
        ),
        LexiconItem(
            id = "l-6",
            title = "Вечный круговорот",
            dateAdded = System.currentTimeMillis() - 86400000L * 12,
            masteryLevel = 1,
            phrase = "Ничто не ново под луной, но каждое мгновение неповторимо",
            meaningOrContext = "Осознание вечности универсальных истин сквозь призму личного сиюминутного опыта.",
            language = "RU",
            tag = "#Idiom",
            sourceBook = "Философские размышления"
        )
    )
}
