package com.example.darkgo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.darkgo.core.BingoEngine
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSuccess
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTextDisabled
import com.example.ui.theme.DarkGoTextPrimary

/**
 * Modern 5x5 Bingo Card Grid for Dark Go.
 *
 * Highlights:
 * - Completed straight lines (horizontal rows, vertical columns, diagonals)
 *   get illuminated with emerald/gold styling.
 * - Called numbers show visual mark and neon indicator.
 * - Supports sequential card-building mode (empty / numbered).
 */
@Composable
fun BingoCardGrid(
    cells: List<Int?>, // 25 elements
    calledNumbers: Set<Int> = emptySet(),
    completedRowIndices: List<Int> = emptyList(),
    completedCellIndices: Set<Int> = emptySet(),
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    highlightCurrentNumber: Int? = null,
    onCellClick: (index: Int) -> Unit = {}
) {
    val bingoEngine = remember { BingoEngine() }

    // Resolve which cells are part of a completed line (horizontal, vertical, or diagonal)
    val activeCompletedCells = remember(cells, calledNumbers, completedCellIndices, completedRowIndices) {
        if (completedCellIndices.isNotEmpty()) {
            completedCellIndices
        } else {
            val nonNull = cells.filterNotNull()
            if (nonNull.size == BingoEngine.TOTAL_CELLS) {
                bingoEngine.getCompletedCellIndices(nonNull, calledNumbers)
            } else {
                completedRowIndices.flatMap { r -> (0 until BingoEngine.GRID_SIZE).map { c -> r * BingoEngine.GRID_SIZE + c } }.toSet()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (row in 0 until BingoEngine.GRID_SIZE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (col in 0 until BingoEngine.GRID_SIZE) {
                    val index = row * BingoEngine.GRID_SIZE + col
                    val cellValue = cells.getOrNull(index)
                    val isCalled = cellValue != null && calledNumbers.contains(cellValue)
                    val isCurrentCalled = cellValue != null && cellValue == highlightCurrentNumber
                    val isLineComplete = activeCompletedCells.contains(index)

                    BingoCell(
                        value = cellValue,
                        isCalled = isCalled,
                        isCurrentCalled = isCurrentCalled,
                        isLineComplete = isLineComplete,
                        isInteractive = isInteractive,
                        testTag = "cell_${row}_${col}",
                        modifier = Modifier.weight(1f),
                        onClick = { onCellClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun BingoCell(
    value: Int?,
    isCalled: Boolean,
    isCurrentCalled: Boolean,
    isLineComplete: Boolean,
    isInteractive: Boolean,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val targetBgColor = when {
        isCurrentCalled -> DarkGoPrimary.copy(alpha = 0.35f)
        isLineComplete && isCalled -> DarkGoSuccess.copy(alpha = 0.28f)
        isCalled -> DarkGoSurfaceElevated
        value != null -> DarkGoSurfaceVariant
        else -> DarkGoSurfaceVariant.copy(alpha = 0.4f)
    }

    val targetBorderColor = when {
        isCurrentCalled -> DarkGoPrimary
        isLineComplete && isCalled -> DarkGoSuccess
        isCalled -> DarkGoPrimary.copy(alpha = 0.6f)
        value != null -> DarkGoBorder
        else -> DarkGoBorder.copy(alpha = 0.5f)
    }

    val animatedBg by animateColorAsState(targetValue = targetBgColor, animationSpec = tween(300), label = "cellBg")
    val animatedBorder by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(300), label = "cellBorder")

    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(shape)
            .background(animatedBg)
            .border(
                width = if (isLineComplete || isCurrentCalled) 2.dp else 1.dp,
                color = animatedBorder,
                shape = shape
            )
            .clickable(enabled = isInteractive) { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (value != null) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isCalled || isCurrentCalled) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 17.sp
                ),
                color = when {
                    isCurrentCalled -> DarkGoPrimary
                    isLineComplete && isCalled -> DarkGoSuccess
                    isCalled -> DarkGoTextPrimary
                    else -> DarkGoTextPrimary
                },
                textAlign = TextAlign.Center
            )

            if (isCalled) {
                // Checked icon badge in upper corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Called",
                        tint = if (isLineComplete) DarkGoSuccess else DarkGoPrimary,
                        modifier = Modifier.padding(1.dp)
                    )
                }
            }
        } else {
            Text(
                text = "•",
                color = DarkGoTextDisabled,
                fontSize = 18.sp
            )
        }
    }
}
