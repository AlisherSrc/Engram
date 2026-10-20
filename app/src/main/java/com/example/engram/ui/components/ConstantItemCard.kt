package com.example.engram.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlusOne
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engram.model.ConstantCategory
import com.example.engram.model.ConstantItem
import com.example.engram.ui.theme.AccentAddress
import com.example.engram.ui.theme.AccentContact
import com.example.engram.ui.theme.AccentDate
import com.example.engram.ui.theme.AccentIdNumber

/**
 * Reusable card composable for displaying a ConstantItem.
 * Employs Card, Row, Column, and Box.
 *
 * Uses `remember` for ephemeral visual-only state:
 * - [isRevealed]: Whether the blurred/masked secret value is temporarily unmasked (peeked).
 * - [isExpanded]: Whether card metadata and memory hints are expanded.
 * Both deliberately reset upon device rotation per memory-practice rules.
 */
@Composable
fun ConstantItemCard(
    item: ConstantItem,
    onIncrementMastery: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ephemeral visual states (reset on Activity recreation / screen rotation)
    var isRevealed by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val (categoryColor, categoryIcon) = when (item.category) {
        ConstantCategory.CONTACT -> Pair(AccentContact, Icons.Default.Contacts)
        ConstantCategory.ADDRESS -> Pair(AccentAddress, Icons.Default.Home)
        ConstantCategory.DATE -> Pair(AccentDate, Icons.Default.CalendarToday)
        ConstantCategory.ID_NUMBER -> Pair(AccentIdNumber, Icons.Default.Badge)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Category Icon Badge, Title, and Mastery Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = item.category.displayName,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor
                        )
                    }
                }

                // Mastery Dots Indicator
                MasteryIndicator(
                    masteryLevel = item.masteryLevel,
                    onIncrement = onIncrementMastery,
                    activeColor = categoryColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Secret Value Mask Box with Peek Spoiler Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (isRevealed) {
                    categoryColor.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isRevealed) "REVEALED VALUE" else "TAP EYE TO TEST RECALL",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            color = if (isRevealed) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isRevealed) item.secretValue else maskSecretValue(item.secretValue),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isRevealed) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isRevealed) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }

                    // Eye / Peek Icon Button
                    IconButton(
                        onClick = { isRevealed = !isRevealed },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isRevealed) categoryColor.copy(alpha = 0.2f) else Color.Transparent,
                            contentColor = if (isRevealed) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isRevealed) "Hide Secret Value" else "Peek Secret Value"
                        )
                    }
                }
            }

            // Expandable Memory Hint / Metadata Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Hint",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Memory Cue / Hint:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.hint.ifEmpty { "No specific hint provided for this node." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer Actions: +1 Mastery Button, Expand Hint Toggle, and Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // +1 Mastery Action Button
                    AssistChip(
                        onClick = onIncrementMastery,
                        label = { Text("+1 Mastery") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlusOne,
                                contentDescription = "+1 Mastery",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Expand / Collapse Hint Button
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Hide Hint" else "Show Hint",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Memory Node",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Helper to produce realistic obscured masking for phone numbers, dates, IDs, and codes.
 */
private fun maskSecretValue(value: String): String {
    if (value.length <= 4) return "••••"
    // Keep first 3 chars and last 2 chars visible, mask everything between
    val prefix = value.take(4)
    val suffix = value.takeLast(2)
    val maskedLength = (value.length - 6).coerceAtLeast(4)
    val bullets = "•".repeat(maskedLength)
    return "$prefix $bullets $suffix"
}
