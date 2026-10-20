package com.example.engram.model

/**
 * Base interface representing a retrievable node of memory in the Presque Vu palace.
 */
interface MemoryNode {
    val id: String
    val title: String
    val dateAdded: Long
    val masteryLevel: Int // 0 (Brand new / Forgotten) to 5 (Mastered by heart)
}

/**
 * Categories for vital personal and factual constants that must be remembered by heart.
 */
enum class ConstantCategory(val displayName: String) {
    CONTACT("Contact"),
    ADDRESS("Address"),
    DATE("Date"),
    ID_NUMBER("ID Number")
}

/**
 * A Constant memory node: high-stakes vital info like phone numbers, codes, or ID numbers.
 * Includes masked value peeking and memory cues.
 */
data class ConstantItem(
    override val id: String,
    override val title: String,
    override val dateAdded: Long,
    override val masteryLevel: Int,
    val secretValue: String,
    val category: ConstantCategory,
    val hint: String
) : MemoryNode

/**
 * A Lexicon memory node: expressive literary phrases, sci-fi idioms, and multilingual vocabulary
 * designed to overcome the tip-of-the-tongue phenomenon during writing and speaking.
 */
data class LexiconItem(
    override val id: String,
    override val title: String,
    override val dateAdded: Long,
    override val masteryLevel: Int,
    val phrase: String,
    val meaningOrContext: String,
    val language: String, // e.g. "EN", "RU", "KZ"
    val tag: String,      // e.g. "#Sci-Fi", "#Atmosphere", "#Idiom"
    val sourceBook: String
) : MemoryNode
