package com.huojieren.apppause.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

private val ALPHABET = listOf(
    "↑", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
    "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
)

private const val TAG = "AlphabetIndexBar"

@Composable
fun AlphabetIndexBar(
    modifier: Modifier = Modifier,
    onLetterSelected: (letter: String) -> Unit,
    displayLetter: String?,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            try {
                                val itemHeight = size.height.toFloat() / ALPHABET.size
                                val index =
                                    (offset.y / itemHeight).toInt().coerceIn(0, ALPHABET.size - 1)
                                val letter = ALPHABET[index]
                                logger(TAG, "Press letter: $letter")
                                onLetterSelected(letter)
                                awaitRelease()
                            } finally {
                                onLetterSelected("")
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            onLetterSelected("")
                        },
                        onDragCancel = {
                            onLetterSelected("")
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val itemHeight = size.height.toFloat() / ALPHABET.size
                            val index = (change.position.y / itemHeight).toInt()
                                .coerceIn(0, ALPHABET.size - 1)
                            val letter = ALPHABET[index]
                            logger(TAG, "Drag letter: $letter")
                            onLetterSelected(letter)
                        }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ALPHABET.forEach { letter ->
                Text(
                    text = letter,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (letter == displayLetter)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (displayLetter != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-24).dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayLetter,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun AlphabetIndexBarPreview() {
    AppTheme {
        AlphabetIndexBar(
            displayLetter = "A",
            onLetterSelected = { letter ->
                logger(TAG, "Selected letter: $letter")
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}
