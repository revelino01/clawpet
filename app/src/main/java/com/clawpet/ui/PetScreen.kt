package com.clawpet.ui

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clawpet.domain.PetMood
import com.clawpet.domain.PetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(viewModel: PetViewModel = hiltViewModel()) {
    val state by viewModel.petState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.name) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pet canvas
            ClawPetCanvas(
                mood = state.mood,
                modifier = Modifier.size(200.dp)
            )

            // Level badge
            Text("Lv.${state.level} • ${state.xp}/${state.xpToNext} XP", style = MaterialTheme.typography.labelLarge)

            // Stats
            StatBar("🍖 Hunger", state.hunger, Color(0xFFFF7043))
            StatBar("😊 Happy", state.happiness, Color(0xFF66BB6A))
            StatBar("⚡ Energy", state.energy, Color(0xFF42A5F5))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton("Feed 🍖", onClick = { viewModel.feed() })
                ActionButton("Pet 🤗", onClick = { viewModel.pet() })
                ActionButton("Play 🎮", onClick = { viewModel.play() })
            }

            if (!state.isAwake) {
                ActionButton("Wake Up! ⏰", onClick = { viewModel.wake() })
            }

            // Mood text
            Text(
                "${state.mood.emoji} ${state.mood.label}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
private fun StatBar(label: String, value: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("$value%", style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
        )
    }
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick) {
        Text(text)
    }
}

/** Draws the claw mascot on a Canvas with idle bounce animation. */
@Composable
fun ClawPetCanvas(mood: PetMood, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet_anim")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "bounce"
    )

    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ), label = "breath"
    )

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2
        val bounceOffset = (bounce - 0.5f) * 8f // ±4px bounce
        val scale = breathScale

        // Body colors based on mood
        val (bodyColor, eyeColor, mouthColor) = when (mood) {
            PetMood.HAPPY -> Triple(Color(0xFF6C63FF), Color.White, Color(0xFF4CAF50))
            PetMood.HUNGRY -> Triple(Color(0xFFFF9800), Color.White, Color(0xFFFF5722))
            PetMood.SLEEPY -> Triple(Color(0xFF78909C), Color(0xFFB0BEC5), Color(0xFF90A4AE))
            PetMood.SAD -> Triple(Color(0xFF9E9E9E), Color(0xFFE0E0E0), Color(0xFF757575))
            PetMood.EXCITED -> Triple(Color(0xFFE91E63), Color(0xFFFFEB3B), Color(0xFFFF9800))
            PetMood.DEAD -> Triple(Color(0xFF424242), Color(0xFF616161), Color(0xFF212121))
        }

        val scaledCx = cx
        val scaledCy = cy + bounceOffset

        // Body (circle)
        drawCircle(
            color = bodyColor,
            radius = size.minDimension * 0.32f * scale,
            center = Offset(scaledCx, scaledCy)
        )

        // Claws (3 triangular claws on top)
        val clawSize = size.minDimension * 0.08f
        for (i in -1..1) {
            val clawX = scaledCx + i * size.minDimension * 0.1f
            val clawY = scaledCy - size.minDimension * 0.32f * scale
            val path = Path().apply {
                moveTo(clawX, clawY)
                lineTo(clawX - clawSize, clawY + clawSize * 2)
                lineTo(clawX + clawSize, clawY + clawSize * 2)
                close()
            }
            drawPath(path, color = bodyColor.copy(alpha = 0.9f), style = Fill)
            drawPath(path, color = Color.White, style = Stroke(width = 2f))
        }

        // Eyes
        val eyeSpacing = size.minDimension * 0.1f
        val eyeY = scaledCy - size.minDimension * 0.06f
        val eyeRadius = size.minDimension * 0.04f

        if (mood == PetMood.SLEEPY) {
            // Closed eyes (lines)
            drawLine(eyeColor, Offset(scaledCx - eyeSpacing - eyeRadius, eyeY), Offset(scaledCx - eyeSpacing + eyeRadius, eyeY), strokeWidth = 3f, cap = StrokeCap.Round)
            drawLine(eyeColor, Offset(scaledCx + eyeSpacing - eyeRadius, eyeY), Offset(scaledCx + eyeSpacing + eyeRadius, eyeY), strokeWidth = 3f, cap = StrokeCap.Round)
        } else if (mood == PetMood.DEAD) {
            // X eyes
            for (ex in listOf(scaledCx - eyeSpacing, scaledCx + eyeSpacing)) {
                drawLine(Color.Red, Offset(ex - eyeRadius, eyeY - eyeRadius), Offset(ex + eyeRadius, eyeY + eyeRadius), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(Color.Red, Offset(ex + eyeRadius, eyeY - eyeRadius), Offset(ex - eyeRadius, eyeY + eyeRadius), strokeWidth = 3f, cap = StrokeCap.Round)
            }
        } else {
            // Normal eyes
            drawCircle(eyeColor, eyeRadius, Offset(scaledCx - eyeSpacing, eyeY))
            drawCircle(eyeColor, eyeRadius, Offset(scaledCx + eyeSpacing, eyeY))
            // Pupils
            drawCircle(Color(0xFF212121), eyeRadius * 0.5f, Offset(scaledCx - eyeSpacing, eyeY))
            drawCircle(Color(0xFF212121), eyeRadius * 0.5f, Offset(scaledCx + eyeSpacing, eyeY))
        }

        // Mouth
        val mouthY = scaledCy + size.minDimension * 0.1f
        when (mood) {
            PetMood.HAPPY, PetMood.EXCITED -> {
                // Smile arc
                drawArc(
                    color = mouthColor,
                    startAngle = 20f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(scaledCx - size.minDimension * 0.08f, mouthY - size.minDimension * 0.04f),
                    size = Size(size.minDimension * 0.16f, size.minDimension * 0.08f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }
            PetMood.SAD -> {
                // Frown
                drawArc(
                    color = mouthColor,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(scaledCx - size.minDimension * 0.08f, mouthY),
                    size = Size(size.minDimension * 0.16f, size.minDimension * 0.08f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
            }
            PetMood.HUNGRY -> {
                // Open mouth circle
                drawCircle(mouthColor, size.minDimension * 0.05f, Offset(scaledCx, mouthY), style = Stroke(width = 3f))
            }
            PetMood.SLEEPY -> {
                // Small O mouth (snoring)
                drawCircle(Color(0xFF90A4AE), size.minDimension * 0.03f, Offset(scaledCx, mouthY))
            }
            PetMood.DEAD -> {
                // Flat line
                drawLine(Color.Red, Offset(scaledCx - size.minDimension * 0.06f, mouthY), Offset(scaledCx + size.minDimension * 0.06f, mouthY), strokeWidth = 3f)
            }
        }

        // Blush cheeks (when happy/excited)
        if (mood == PetMood.HAPPY || mood == PetMood.EXCITED) {
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.4f), size.minDimension * 0.04f, Offset(scaledCx - size.minDimension * 0.18f, eyeY + size.minDimension * 0.06f))
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.4f), size.minDimension * 0.04f, Offset(scaledCx + size.minDimension * 0.18f, eyeY + size.minDimension * 0.06f))
        }
    }
}