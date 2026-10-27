package com.example.engram.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engram.model.ConstantCategory
import com.example.engram.model.ConstantItem
import com.example.engram.model.LexiconItem
import com.example.engram.model.MemoryNode
import com.example.engram.ui.theme.AccentAddress
import com.example.engram.ui.theme.AccentContact
import com.example.engram.ui.theme.AccentDate
import com.example.engram.ui.theme.AccentIdNumber
import com.example.engram.ui.theme.LangEnColor
import com.example.engram.ui.theme.LangKzColor
import com.example.engram.ui.theme.LangRuColor
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Screen 4: Challenge & Interactive Active Recall Screen.
 *
 * Implements:
 * 1. Feature 1: Timed Mode with a 15-second Coroutine Countdown (`LaunchedEffect` + `delay(100L)`).
 * 2. Progressive Obfuscation for Constants recall (`+7 (701) 492 - ____`).
 * 3. Feature 2: Wordsmith Sentence Practice for Lexicon items, recording sentences into `practiceHistory`.
 * 4. Automatic grading, mastery level updating, and session review summary.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(
    isTimedMode: Boolean,
    constants: List<ConstantItem>,
    lexicon: List<LexiconItem>,
    onAnswerConstant: (id: String, isCorrect: Boolean) -> Unit,
    onRecordLexiconSentence: (id: String, sentence: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Generate a balanced drill deck containing both Constants and Lexicon nodes
    val drillDeck: List<MemoryNode> = remember(constants, lexicon) {
        val combined = mutableListOf<MemoryNode>()
        val maxLen = maxOf(constants.size, lexicon.size)
        for (i in 0 until maxLen) {
            if (i < constants.size) combined.add(constants[i])
            if (i < lexicon.size) combined.add(lexicon[i])
        }
        if (combined.isEmpty()) emptyList() else combined
    }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    var sentencesWrittenCount by rememberSaveable { mutableIntStateOf(0) }

    val isFinished = drillDeck.isEmpty() || currentIndex >= drillDeck.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isTimedMode) "⚡ Timed Recall Challenge" else "Active Recall Drill",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!isFinished && drillDeck.isNotEmpty()) {
                            Text(
                                text = "Card ${currentIndex + 1} of ${drillDeck.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return to Dashboard"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (drillDeck.isEmpty()) {
                EmptyDrillNotice(
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (isFinished) {
                DrillFinishedSummary(
                    totalCards = drillDeck.size,
                    correctCount = correctCount,
                    sentencesWritten = sentencesWrittenCount,
                    isTimedMode = isTimedMode,
                    onRestart = {
                        currentIndex = 0
                        correctCount = 0
                        sentencesWrittenCount = 0
                    },
                    onNavigateBack = onNavigateBack,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                val currentCard = drillDeck[currentIndex]
                val cardProgress = (currentIndex.toFloat() / drillDeck.size.toFloat()).coerceIn(0f, 1f)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Deck Progress Indicator
                    LinearProgressIndicator(
                        progress = { cardProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    when (currentCard) {
                        is ConstantItem -> {
                            ConstantChallengeCard(
                                item = currentCard,
                                isTimedMode = isTimedMode,
                                onGradeAnswer = { isCorrect ->
                                    if (isCorrect) correctCount++
                                    onAnswerConstant(currentCard.id, isCorrect)
                                },
                                onAdvance = { currentIndex++ }
                            )
                        }

                        is LexiconItem -> {
                            LexiconSentencePracticeCard(
                                item = currentCard,
                                isTimedMode = isTimedMode,
                                onSaveSentence = { sentence ->
                                    sentencesWrittenCount++
                                    correctCount++
                                    onRecordLexiconSentence(currentCard.id, sentence)
                                },
                                onAdvance = { currentIndex++ }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Feature 1: Progressive Obfuscation card for Vital Constants with 15s Coroutine Countdown timer.
 */
@Composable
private fun ConstantChallengeCard(
    item: ConstantItem,
    isTimedMode: Boolean,
    onGradeAnswer: (isCorrect: Boolean) -> Unit,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Progressive Obfuscation: split value into visible prefix and missing target
    val (visiblePrefix, missingTarget, maskedPrompt) = remember(item.id) {
        computeProgressiveObfuscation(item.secretValue)
    }

    var userInput by rememberSaveable(item.id) { mutableStateOf("") }
    var isSubmitted by rememberSaveable(item.id) { mutableStateOf(false) }
    var isCorrectResult by rememberSaveable(item.id) { mutableStateOf<Boolean?>(null) }
    var isShowHint by rememberSaveable(item.id) { mutableStateOf(false) }
    var isTimedOut by rememberSaveable(item.id) { mutableStateOf(false) }

    // Coroutine Timer: 15s countdown with delay(100L)
    var timeLeftMillis by remember(item.id) { mutableLongStateOf(15000L) }

    LaunchedEffect(item.id, isSubmitted) {
        if (!isTimedMode || isSubmitted) return@LaunchedEffect
        timeLeftMillis = 15000L
        val startTime = System.currentTimeMillis()
        val totalDuration = 15000L

        while (timeLeftMillis > 0 && !isSubmitted) {
            delay(100L)
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = (totalDuration - elapsed).coerceAtLeast(0L)
            timeLeftMillis = remaining
        }

        if (timeLeftMillis <= 0L && !isSubmitted) {
            isTimedOut = true
            isSubmitted = true
            isCorrectResult = false
            keyboardController?.hide()
            onGradeAnswer(false)
        }
    }

    val (categoryColor, categoryName) = when (item.category) {
        ConstantCategory.CONTACT -> Pair(AccentContact, "Contact")
        ConstantCategory.ADDRESS -> Pair(AccentAddress, "Address")
        ConstantCategory.DATE -> Pair(AccentDate, "Date")
        ConstantCategory.ID_NUMBER -> Pair(AccentIdNumber, "ID Number")
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Category Tag & Timer or Mastery Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = categoryName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (isTimedMode) {
                    CountdownTimerBadge(
                        timeLeftMillis = timeLeftMillis,
                        isSubmitted = isSubmitted,
                        isTimedOut = isTimedOut
                    )
                } else {
                    Text(
                        text = "Mastery: ${item.masteryLevel}/5",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Node Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Progressive Obfuscation Prompt Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROGRESSIVE OBFUSCATION CUE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = maskedPrompt,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Type the missing portion ($missingTarget) to verify recall:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Memory Cue / Hint Toggle
            if (item.hint.isNotEmpty()) {
                OutlinedButton(
                    onClick = { isShowHint = !isShowHint },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isShowHint) "Hide Memory Cue" else "Show Memory Cue / Hint")
                }

                if (isShowHint) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 ${item.hint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            // Answer Input Field (enabled only before submit/timeout)
            OutlinedTextField(
                value = userInput,
                onValueChange = { if (!isSubmitted) userInput = it },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitted,
                label = { Text("Your answer") },
                placeholder = { Text("e.g. $missingTarget or full value") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (userInput.isNotBlank() && !isSubmitted) {
                        keyboardController?.hide()
                        val correct = checkAnswer(userInput, missingTarget, item.secretValue)
                        isCorrectResult = correct
                        isSubmitted = true
                        onGradeAnswer(correct)
                    }
                })
            )

            // Result Feedback Banner after Submission or Timeout
            if (isSubmitted) {
                if (isCorrectResult == true) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Spot-on Recall! +1 Mastery",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Verified: ${item.secretValue}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isTimedOut) Icons.Default.TimerOff else Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isTimedOut) "Time's Up! (15s expired)" else "Keep Practicing!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Full secret value: ${item.secretValue}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Action Button: Submit Recall or Next Node
            if (!isSubmitted) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        val correct = checkAnswer(userInput, missingTarget, item.secretValue)
                        isCorrectResult = correct
                        isSubmitted = true
                        onGradeAnswer(correct)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = userInput.isNotBlank()
                ) {
                    Text("Check Recall")
                }
            } else {
                Button(
                    onClick = onAdvance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Next Memory Node →")
                }
            }
        }
    }
}

/**
 * Feature 2: Wordsmith Sentence Practice for Lexicon Items.
 * Prompts: "Write an original sentence using this phrase:"
 * Saves into LexiconItem.practiceHistory and reinforces mastery.
 */
@Composable
private fun LexiconSentencePracticeCard(
    item: LexiconItem,
    isTimedMode: Boolean,
    onSaveSentence: (sentence: String) -> Unit,
    onAdvance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var userSentence by rememberSaveable(item.id) { mutableStateOf("") }
    var isSubmitted by rememberSaveable(item.id) { mutableStateOf(false) }
    var isTimedOut by rememberSaveable(item.id) { mutableStateOf(false) }

    // 15-second timer for timed mode
    var timeLeftMillis by remember(item.id) { mutableLongStateOf(15000L) }

    LaunchedEffect(item.id, isSubmitted) {
        if (!isTimedMode || isSubmitted) return@LaunchedEffect
        timeLeftMillis = 15000L
        val startTime = System.currentTimeMillis()
        val totalDuration = 15000L

        while (timeLeftMillis > 0 && !isSubmitted) {
            delay(100L)
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = (totalDuration - elapsed).coerceAtLeast(0L)
            timeLeftMillis = remaining
        }

        if (timeLeftMillis <= 0L && !isSubmitted) {
            isTimedOut = true
            isSubmitted = true
            keyboardController?.hide()
            // If timed out, save whatever the user started writing or prompt them
            if (userSentence.isNotBlank()) {
                onSaveSentence(userSentence.trim())
            }
        }
    }

    val langBadgeColor = when (item.language.uppercase()) {
        "EN" -> LangEnColor
        "RU" -> LangRuColor
        "KZ" -> LangKzColor
        else -> MaterialTheme.colorScheme.primary
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Language badge & Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(langBadgeColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.language,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.tag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (isTimedMode) {
                    CountdownTimerBadge(
                        timeLeftMillis = timeLeftMillis,
                        isSubmitted = isSubmitted,
                        isTimedOut = isTimedOut
                    )
                }
            }

            // Target Phrase Display
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                border = androidx.compose.foundation.BorderStroke(1.dp, langBadgeColor.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WORDSMITH TARGET PHRASE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = langBadgeColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "“${item.phrase}”",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.meaningOrContext,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Prompt Requirement
            Column {
                Text(
                    text = "✍️ Write an original sentence using this phrase:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Active usage builds lasting synaptic memory paths.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sentence Input Field
            OutlinedTextField(
                value = userSentence,
                onValueChange = { if (!isSubmitted) userSentence = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                enabled = !isSubmitted,
                placeholder = { Text("Compose your original sentence here...") },
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (userSentence.isNotBlank() && !isSubmitted) {
                        keyboardController?.hide()
                        isSubmitted = true
                        onSaveSentence(userSentence.trim())
                    }
                })
            )

            // Previous Practice Sentences from practiceHistory
            if (item.practiceHistory.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Previous Sentences (${item.practiceHistory.size}):",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "“${item.practiceHistory.first()}”",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Feedback Banner after submission
            if (isSubmitted) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sentence Preserved! +1 Mastery",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Saved into your palace practice history.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Action Buttons
            if (!isSubmitted) {
                Button(
                    onClick = {
                        keyboardController?.hide()
                        isSubmitted = true
                        onSaveSentence(userSentence.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = userSentence.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Practice Sentence")
                }
            } else {
                Button(
                    onClick = onAdvance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Next Memory Node →")
                }
            }
        }
    }
}

/**
 * 15-second Coroutine Countdown badge with animated color shift.
 */
@Composable
private fun CountdownTimerBadge(
    timeLeftMillis: Long,
    isSubmitted: Boolean,
    isTimedOut: Boolean,
    modifier: Modifier = Modifier
) {
    val secondsLeft = timeLeftMillis / 1000f

    val badgeColor by animateColorAsState(
        targetValue = when {
            isTimedOut -> MaterialTheme.colorScheme.error
            isSubmitted -> MaterialTheme.colorScheme.primary
            secondsLeft <= 4f -> MaterialTheme.colorScheme.error
            secondsLeft <= 8f -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        },
        label = "timer_badge_color"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isTimedOut) Icons.Default.TimerOff else Icons.Default.Timer,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isTimedOut) "0.0s" else String.format(Locale.US, "%.1fs", secondsLeft),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = badgeColor
            )
        }
    }
}

/**
 * Completion summary screen with session stats and action buttons.
 */
@Composable
private fun DrillFinishedSummary(
    totalCards: Int,
    correctCount: Int,
    sentencesWritten: Int,
    isTimedMode: Boolean,
    onRestart: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isTimedMode) "⚡ Timed Challenge Complete!" else "Recall Session Complete!",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Synaptic pathways reinforced in your Presque Vu mental palace.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$correctCount / $totalCards",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+$correctCount",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Mastery Points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$sentencesWritten",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Sentences",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Practice Again")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Return to Dashboard")
        }
    }
}

@Composable
private fun EmptyDrillNotice(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Palace is Empty",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add some constants or lexicon phrases before starting a challenge.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onNavigateBack) {
            Text("Back to Dashboard")
        }
    }
}

/**
 * Computes progressive obfuscation masking:
 * Returns Triple(visiblePrefix, missingTarget, maskedPromptString).
 */
private fun computeProgressiveObfuscation(secretValue: String): Triple<String, String, String> {
    if (secretValue.length <= 3) {
        return Triple("", secretValue, "___")
    }

    // Try splitting on last dash/space/separator if present
    val lastDashIdx = secretValue.lastIndexOfAny(charArrayOf('-', ' ', '*', '#'))
    if (lastDashIdx in 3 until secretValue.length - 1) {
        val prefix = secretValue.substring(0, lastDashIdx + 1)
        val missing = secretValue.substring(lastDashIdx + 1).trim()
        val masked = prefix + "_".repeat(missing.length.coerceAtLeast(3))
        return Triple(prefix, missing, masked)
    }

    // Default numeric/character split: show 65% prefix, obfuscate last 35%
    val visibleLength = (secretValue.length * 0.65).toInt().coerceAtLeast(1)
    val prefix = secretValue.take(visibleLength)
    val missing = secretValue.substring(visibleLength)
    val masked = prefix + "_".repeat(missing.length.coerceAtLeast(3))
    return Triple(prefix, missing, masked)
}

/**
 * Validates user recall answer.
 * Accepts either the missing portion or the complete secret value.
 */
private fun checkAnswer(userAnswer: String, missingPart: String, fullSecretValue: String): Boolean {
    val clean = { str: String -> str.filter { it.isLetterOrDigit() }.lowercase() }
    val cleanUser = clean(userAnswer)
    val cleanMissing = clean(missingPart)
    val cleanFull = clean(fullSecretValue)

    return cleanUser == cleanMissing || cleanUser == cleanFull ||
            userAnswer.trim().equals(missingPart.trim(), ignoreCase = true) ||
            userAnswer.trim().equals(fullSecretValue.trim(), ignoreCase = true)
}
