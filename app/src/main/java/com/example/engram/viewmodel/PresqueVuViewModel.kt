package com.example.engram.viewmodel

import androidx.lifecycle.ViewModel
import com.example.engram.model.ConstantCategory
import com.example.engram.model.ConstantItem
import com.example.engram.model.LexiconItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * ViewModel for the Presque Vu application.
 * Exposes a single [uiState] StateFlow adhering to unidirectional data flow (UDF).
 * Manages memory nodes (Constants and Lexicon), active filters, search queries,
 * active recall quiz evaluations, and wordsmith sentence practice history.
 */
class PresqueVuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        PresqueVuUiState(
            constants = initialConstants(),
            lexicon = initialLexicon(),
            spotlightLexiconId = "l-1"
        )
    )
    val uiState: StateFlow<PresqueVuUiState> = _uiState.asStateFlow()

    // --- Search & Filter Mutators ---

    fun setConstantsSearchQuery(query: String) {
        _uiState.update { it.copy(constantsSearchQuery = query) }
    }

    fun setLexiconSearchQuery(query: String) {
        _uiState.update { it.copy(lexiconSearchQuery = query) }
    }

    fun setConstantCategoryFilter(category: ConstantCategory?) {
        _uiState.update { it.copy(selectedConstantCategory = category) }
    }

    fun setLexiconFilter(filter: String?) {
        _uiState.update { it.copy(selectedLexiconFilter = filter) }
    }

    fun refreshSpotlightPhrase() {
        val currentLexicon = _uiState.value.lexicon
        if (currentLexicon.isNotEmpty()) {
            val remaining = currentLexicon.filter { it.id != _uiState.value.spotlightLexiconId }
            val next = if (remaining.isNotEmpty()) remaining.random() else currentLexicon.random()
            _uiState.update { it.copy(spotlightLexiconId = next.id) }
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
        _uiState.update { it.copy(constants = listOf(newItem) + it.constants) }
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
            practiceHistory = emptyList()
        )
        _uiState.update { it.copy(lexicon = listOf(newItem) + it.lexicon) }
    }

    fun incrementConstantMastery(id: String) {
        _uiState.update { state ->
            state.copy(
                constants = state.constants.map { item ->
                    if (item.id == id) {
                        val next = if (item.masteryLevel >= 5) 0 else item.masteryLevel + 1
                        item.copy(masteryLevel = next)
                    } else item
                }
            )
        }
    }

    fun incrementLexiconMastery(id: String) {
        _uiState.update { state ->
            state.copy(
                lexicon = state.lexicon.map { item ->
                    if (item.id == id) {
                        val next = if (item.masteryLevel >= 5) 0 else item.masteryLevel + 1
                        item.copy(masteryLevel = next)
                    } else item
                }
            )
        }
    }

    /**
     * Grades the user's recall of a constant.
     * Increases mastery if correct (up to 5), or decreases (down to 0) if failed.
     */
    fun recordConstantRecallResult(id: String, isCorrect: Boolean) {
        _uiState.update { state ->
            state.copy(
                constants = state.constants.map { item ->
                    if (item.id == id) {
                        val updated = if (isCorrect) {
                            (item.masteryLevel + 1).coerceAtMost(5)
                        } else {
                            (item.masteryLevel - 1).coerceAtLeast(0)
                        }
                        item.copy(masteryLevel = updated)
                    } else item
                }
            )
        }
    }

    /**
     * Feature 2: Saves the user's original practice sentence into [LexiconItem.practiceHistory]
     * and reinforces the mastery level by +1.
     */
    fun recordLexiconPracticeSentence(id: String, practiceSentence: String) {
        val trimmed = practiceSentence.trim()
        if (trimmed.isEmpty()) return

        _uiState.update { state ->
            state.copy(
                lexicon = state.lexicon.map { item ->
                    if (item.id == id) {
                        val updatedHistory = listOf(trimmed) + item.practiceHistory
                        val updatedMastery = (item.masteryLevel + 1).coerceAtMost(5)
                        item.copy(
                            practiceHistory = updatedHistory,
                            masteryLevel = updatedMastery
                        )
                    } else item
                }
            )
        }
    }

    fun deleteConstant(id: String) {
        _uiState.update { state ->
            state.copy(constants = state.constants.filterNot { it.id == id })
        }
    }

    fun deleteLexicon(id: String) {
        _uiState.update { state ->
            state.copy(lexicon = state.lexicon.filterNot { it.id == id })
        }
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
            sourceBook = "Do Androids Dream of Electric Sheep? (Blade Runner)",
            practiceHistory = listOf(
                "As the midnight train crossed the viaduct, lights flickered like C-beams near the Tannhäuser Gate."
            )
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
            sourceBook = "Neuromancer (William Gibson)",
            practiceHistory = listOf(
                "Stepping onto the industrial rooftop, the static sky felt tuned to a dead channel."
            )
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
            sourceBook = "Пикник на обочине (А. и Б. Стругацкие)",
            practiceHistory = emptyList()
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
            sourceBook = "Қазақ классикалық әдебиеті",
            practiceHistory = emptyList()
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
            sourceBook = "Ғарыштық хикаялар жинағы",
            practiceHistory = emptyList()
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
            sourceBook = "Философские размышления",
            practiceHistory = emptyList()
        )
    )
}
