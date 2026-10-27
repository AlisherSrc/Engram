package com.example.engram.viewmodel

import com.example.engram.model.ConstantCategory
import com.example.engram.model.ConstantItem
import com.example.engram.model.LexiconItem

/**
 * Immutable UI state for the Presque Vu application.
 * Exposes all state required by the Dashboard, Constants Vault, Lexicon, and Challenge screens.
 */
data class PresqueVuUiState(
    val constants: List<ConstantItem> = emptyList(),
    val lexicon: List<LexiconItem> = emptyList(),
    val selectedConstantCategory: ConstantCategory? = null,
    val selectedLexiconFilter: String? = null,
    val constantsSearchQuery: String = "",
    val lexiconSearchQuery: String = "",
    val spotlightLexiconId: String? = null
) {
    /**
     * Total memory nodes stored across the entire palace.
     */
    val totalItems: Int
        get() = constants.size + lexicon.size

    /**
     * Total items with mastery level 5.
     */
    val masteredCount: Int
        get() = constants.count { it.masteryLevel >= 5 } + lexicon.count { it.masteryLevel >= 5 }

    /**
     * Items that need active review (mastery level below 3).
     */
    val reviewNeededCount: Int
        get() = constants.count { it.masteryLevel < 3 } + lexicon.count { it.masteryLevel < 3 }

    /**
     * Average overall mastery percentage [0f, 1f].
     */
    val masteryPercentage: Float
        get() = if (totalItems > 0) {
            val totalScore = constants.sumOf { it.masteryLevel } + lexicon.sumOf { it.masteryLevel }
            (totalScore.toFloat() / (totalItems * 5f)).coerceIn(0f, 1f)
        } else 0f

    /**
     * Active spotlight "Phrase of the Day" for the Dashboard.
     */
    val spotlightPhrase: LexiconItem?
        get() = lexicon.find { it.id == spotlightLexiconId } ?: lexicon.firstOrNull()

    /**
     * Filtered constants matching active category chip and live search query.
     */
    val filteredConstants: List<ConstantItem>
        get() {
            val trimmed = constantsSearchQuery.trim().lowercase()
            return constants.filter { item ->
                val matchesCategory = selectedConstantCategory == null || item.category == selectedConstantCategory
                val matchesQuery = trimmed.isEmpty() ||
                        item.title.lowercase().contains(trimmed) ||
                        item.hint.lowercase().contains(trimmed) ||
                        item.category.displayName.lowercase().contains(trimmed) ||
                        item.secretValue.lowercase().contains(trimmed)
                matchesCategory && matchesQuery
            }
        }

    /**
     * Filtered lexicon items matching active tag/language chip and live search query.
     */
    val filteredLexicon: List<LexiconItem>
        get() {
            val trimmed = lexiconSearchQuery.trim().lowercase()
            return lexicon.filter { item ->
                val matchesFilter = when {
                    selectedLexiconFilter == null -> true
                    selectedLexiconFilter.startsWith("#") -> item.tag.equals(selectedLexiconFilter, ignoreCase = true)
                    else -> item.language.equals(selectedLexiconFilter, ignoreCase = true)
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
}
