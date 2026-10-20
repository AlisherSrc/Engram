package com.example.engram.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable visual indicator for a MemoryNode's mastery level (0 to 5 dots).
 * Clicking the row or individual dots invokes [onIncrement] to practice and reinforce memory.
 */
@Composable
fun MasteryIndicator(
    masteryLevel: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onIncrement)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..5) {
            val isFilled = i <= masteryLevel
            val dotColor by animateColorAsState(
                targetValue = if (isFilled) activeColor else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(durationMillis = 200),
                label = "mastery_dot_$i"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(
                        width = 1.dp,
                        color = if (isFilled) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }

        Text(
            text = "$masteryLevel/5",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
