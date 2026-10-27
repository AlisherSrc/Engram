package com.example.engram.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engram.model.ConstantCategory

/**
 * Modal dialog for creating a new memory node (either Constant or Lexicon).
 *
 * CRITICAL STATE REQUIREMENT:
 * All form input states use [rememberSaveable] so that during screen rotation
 * (Activity recreation), user-typed draft values are strictly preserved!
 */
@Composable
fun AddMemoryDialog(
    initialTab: Int = 0,
    onDismissRequest: () -> Unit,
    onAddConstant: (title: String, secretValue: String, category: ConstantCategory, hint: String, initialMastery: Int) -> Unit,
    onAddLexicon: (title: String, phrase: String, meaning: String, language: String, tag: String, sourceBook: String, initialMastery: Int) -> Unit
) {
    // Survives screen rotation via rememberSaveable
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    // Form fields for ConstantItem (all rememberSaveable)
    var constantTitle by rememberSaveable { mutableStateOf("") }
    var constantSecretValue by rememberSaveable { mutableStateOf("") }
    var constantCategoryName by rememberSaveable { mutableStateOf(ConstantCategory.CONTACT.name) }
    var constantHint by rememberSaveable { mutableStateOf("") }
    var constantInitialMastery by rememberSaveable { mutableIntStateOf(1) }
    var constantNeedsDailyPractice by rememberSaveable { mutableStateOf(true) }

    // Form fields for LexiconItem (all rememberSaveable)
    var lexiconTitle by rememberSaveable { mutableStateOf("") }
    var lexiconPhrase by rememberSaveable { mutableStateOf("") }
    var lexiconMeaning by rememberSaveable { mutableStateOf("") }
    var lexiconLanguage by rememberSaveable { mutableStateOf("EN") }
    var lexiconTag by rememberSaveable { mutableStateOf("#Sci-Fi") }
    var lexiconSourceBook by rememberSaveable { mutableStateOf("") }
    var lexiconInitialMastery by rememberSaveable { mutableIntStateOf(1) }
    var lexiconAddToWritingGoals by rememberSaveable { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "New Memory Node",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Preserve knowledge in your mental palace",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Segmented Tab Selector (Constant vs. Lexicon)
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Constant (Vital)") },
                        icon = { Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Lexicon (Wordsmith)") },
                        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Constant Form Inputs
                    OutlinedTextField(
                        value = constantTitle,
                        onValueChange = { constantTitle = it },
                        label = { Text("Node Title *") },
                        placeholder = { Text("e.g. Grandma's Landline, Front Gate Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = constantSecretValue,
                        onValueChange = { constantSecretValue = it },
                        label = { Text("Secret Value to Remember *") },
                        placeholder = { Text("e.g. +7 (701) 555-0142 or #9482") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ConstantCategory.values().forEach { category ->
                            FilterChip(
                                selected = constantCategoryName == category.name,
                                onClick = { constantCategoryName = category.name },
                                label = { Text(category.displayName, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = constantHint,
                        onValueChange = { constantHint = it },
                        label = { Text("Memory Cue / Hint") },
                        placeholder = { Text("e.g. Kcell prefix, rhyme with door number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Initial Mastery Picker
                    Text(
                        text = "Initial Mastery Level ($constantInitialMastery / 5):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MasteryIndicator(
                        masteryLevel = constantInitialMastery,
                        onIncrement = {
                            constantInitialMastery = if (constantInitialMastery >= 5) 0 else constantInitialMastery + 1
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checkbox requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = constantNeedsDailyPractice,
                            onCheckedChange = { constantNeedsDailyPractice = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark for daily active recall practice",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                } else {
                    // Lexicon Form Inputs
                    OutlinedTextField(
                        value = lexiconTitle,
                        onValueChange = { lexiconTitle = it },
                        label = { Text("Phrase Concept / Title *") },
                        placeholder = { Text("e.g. Cosmic Melancholy, Cyber Horizon") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lexiconPhrase,
                        onValueChange = { lexiconPhrase = it },
                        label = { Text("Expressive Phrase / Quote *") },
                        placeholder = { Text("e.g. C-beams glitter in the dark...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lexiconMeaning,
                        onValueChange = { lexiconMeaning = it },
                        label = { Text("Meaning or Context *") },
                        placeholder = { Text("Literary nuance, feeling, or usage note") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Language & Tag",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Language Chips (EN, RU, KZ)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("EN", "RU", "KZ").forEach { lang ->
                            FilterChip(
                                selected = lexiconLanguage == lang,
                                onClick = { lexiconLanguage = lang },
                                label = { Text(lang) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tag Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("#Sci-Fi", "#Atmosphere", "#Idiom", "#Poetic").forEach { tag ->
                            FilterChip(
                                selected = lexiconTag == tag,
                                onClick = { lexiconTag = tag },
                                label = { Text(tag) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = lexiconSourceBook,
                        onValueChange = { lexiconSourceBook = it },
                        label = { Text("Source Book / Author") },
                        placeholder = { Text("e.g. Neuromancer, Blade Runner, Abai") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Initial Mastery Picker
                    Text(
                        text = "Initial Mastery Level ($lexiconInitialMastery / 5):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    MasteryIndicator(
                        masteryLevel = lexiconInitialMastery,
                        onIncrement = {
                            lexiconInitialMastery = if (lexiconInitialMastery >= 5) 0 else lexiconInitialMastery + 1
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Checkbox requirement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = lexiconAddToWritingGoals,
                            onCheckedChange = { lexiconAddToWritingGoals = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add to active writing vocabulary goals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dialog Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val isFormValid = if (selectedTab == 0) {
                        constantTitle.isNotBlank() && constantSecretValue.isNotBlank()
                    } else {
                        lexiconTitle.isNotBlank() && lexiconPhrase.isNotBlank() && lexiconMeaning.isNotBlank()
                    }

                    Button(
                        onClick = {
                            if (selectedTab == 0) {
                                val category = try {
                                    ConstantCategory.valueOf(constantCategoryName)
                                } catch (e: Exception) {
                                    ConstantCategory.CONTACT
                                }
                                onAddConstant(
                                    constantTitle,
                                    constantSecretValue,
                                    category,
                                    constantHint,
                                    constantInitialMastery
                                )
                            } else {
                                onAddLexicon(
                                    lexiconTitle,
                                    lexiconPhrase,
                                    lexiconMeaning,
                                    lexiconLanguage,
                                    lexiconTag,
                                    lexiconSourceBook,
                                    lexiconInitialMastery
                                )
                            }
                            onDismissRequest()
                        },
                        enabled = isFormValid,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Node")
                    }
                }
            }
        }
    }
}
