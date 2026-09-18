package com.example.darkgo.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkGoBorder
import com.example.ui.theme.DarkGoPrimary
import com.example.ui.theme.DarkGoSurfaceElevated
import com.example.ui.theme.DarkGoSurfaceVariant
import com.example.ui.theme.DarkGoTextDisabled
import com.example.ui.theme.DarkGoTextPrimary

/**
 * 1-25 Number selection keypad for the active player's turn.
 */
@Composable
fun NumberSelector(
    calledNumbers: Set<Int>,
    isMyTurn: Boolean,
    onSelectNumber: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (r in 0 until 5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (c in 0 until 5) {
                    val num = r * 5 + c + 1
                    val isCalled = calledNumbers.contains(num)
                    val isClickable = isMyTurn && !isCalled

                    val shape = RoundedCornerShape(6.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.1f)
                            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                            .clip(shape)
                            .background(
                                when {
                                    isCalled -> DarkGoSurfaceVariant.copy(alpha = 0.3f)
                                    isClickable -> DarkGoSurfaceElevated
                                    else -> DarkGoSurfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                            .border(
                                width = if (isClickable) 1.5.dp else 1.dp,
                                color = if (isClickable) DarkGoPrimary.copy(alpha = 0.8f) else DarkGoBorder.copy(alpha = 0.4f),
                                shape = shape
                            )
                            .clickable(enabled = isClickable) {
                                onSelectNumber(num)
                            }
                            .testTag("num_key_$num"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCalled) "✕" else num.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isClickable) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 15.sp,
                                textDecoration = if (isCalled) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            color = when {
                                isCalled -> DarkGoTextDisabled
                                isClickable -> DarkGoPrimary
                                else -> DarkGoTextPrimary.copy(alpha = 0.5f)
                            }
                        )
                    }
                }
            }
        }
    }
}
