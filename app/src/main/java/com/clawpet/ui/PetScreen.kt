package com.clawpet.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.clawpet.domain.PetMood
import com.clawpet.domain.PetState
import kotlin.math.cos
import kotlin.math.PI
import kotlin.math.sin

/** Personality messages — rotates per widget update / mood change */
val MOOD_MESSAGES = mapOf(
    PetMood.HAPPY to listOf("Clawby is vibing ✨", "World domination later, nap now", "You're my favorite human"),
    PetMood.HUNGRY to listOf("Feed me or I eat your homework", "Is that a snack in your pocket?", "Rumbling intensifies"),
    PetMood.SLEEPY to listOf("5 more minutes... zZz", "Dreaming of electric sheep", "Currently unavailable. Try later."),
    PetMood.SAD to listOf("Did you forget about me? 😢", "Just raincloud things", "Hug needed desperately"),
    PetMood.EXCITED to listOf("OH BOY OH BOY", "Something cool happening?!", "I'm literally shaking"),
    PetMood.DEAD to listOf("Clawby has left the chat", "Press F to pay respects", "Respawn timer: feed me"),
)

data class MoodColors(val bodyTop: Color, val bodyBottom: Color, val eye: Color, val mouth: Color)

val MOOD_PALETTE = mapOf(
    PetMood.HAPPY to MoodColors(Color(0xFF8B5CF6), Color(0xFF6D28D9), Color.White, Color(0xFF4CAF50)),
    PetMood.HUNGRY to MoodColors(Color(0xFFFF9800), Color(0xFFE65100), Color.White, Color(0xFFFF5722)),
    PetMood.SLEEPY to MoodColors(Color(0xFF78909C), Color(0xFF546E7A), Color(0xFFB0BEC5), Color(0xFF90A4AE)),
    PetMood.SAD to MoodColors(Color(0xFF9E9E9E), Color(0xFF616161), Color(0xFFE0E0E0), Color(0xFF757575)),
    PetMood.EXCITED to MoodColors(Color(0xFFE91E63), Color(0xFFAD1457), Color(0xFFFFEB3B), Color(0xFFFF9800)),
    PetMood.DEAD to MoodColors(Color(0xFF424242), Color(0xFF212121), Color(0xFF616161), Color(0xFF424242)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(viewModel: PetViewModel = hiltViewModel()) {
    val state by viewModel.petState.collectAsState()
    var tapOffset by remember { mutableStateOf(Offset.Zero) }
    var showParticles by remember { mutableStateOf<List<Particle>>(emptyList()) }

    // Pick a message based on mood
    val messages = MOOD_MESSAGES[state.mood] ?: listOf("...")
    val messageIndex = remember(state.mood) { (0..100).random() }
    val moodMessage = messages[messageIndex % messages.size]

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.name) }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pet canvas with tap interaction
            ClawPetCanvas(
                mood = state.mood,
                isPressed = false,
                tapOffset = tapOffset,
                particles = showParticles,
                modifier = Modifier.size(220.dp).pointerInput(Unit) {
                    detectTapGestures { offset -> tapOffset = offset }
                }
            )

            // Mood message
            Text(
                text = moodMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                ActionButton("Feed 🍖") { viewModel.feed(); showParticles = heartParticles() }
                ActionButton("Pet 🤗") { viewModel.pet(); showParticles = heartParticles() }
                ActionButton("Play 🎮") { viewModel.play(); showParticles = starParticles() }
            }

            if (!state.isAwake) {
                ActionButton("Wake Up! ⏰") { viewModel.wake() }
            }

            // Mood badge
            Text("${state.mood.emoji} ${state.mood.label}", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

data class Particle(val x: Float, val y: Float, val vx: Float, val vy: Float, val life: Float, val type: ParticleType)
enum class ParticleType { STAR, HEART, Z }

fun heartParticles(): List<Particle> = (1..6).map {
    Particle(0.5f, 0.5f, (Math.random().toFloat() - 0.5f) * 0.02f, -0.01f - Math.random().toFloat() * 0.01f, 1f, ParticleType.HEART)
}
fun starParticles(): List<Particle> = (1..8).map {
    val angle = (it.toFloat() / 8) * (2 * PI.toFloat())
    Particle(0.5f, 0.5f, cos(angle) * 0.015f, sin(angle) * 0.015f, 1f, ParticleType.STAR)
}

@Composable
private fun StatBar(label: String, value: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
    FilledTonalButton(onClick = onClick) { Text(text) }
}

/** Draws the claw mascot with gradient body, rounded claws, pupil tracking, and particle effects */
@Composable
fun ClawPetCanvas(
    mood: PetMood,
    isPressed: Boolean,
    tapOffset: Offset,
    particles: List<Particle>,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pet_anim")

    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.97f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "breath"
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "bounce"
    )

    val clawAngle1 by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "claw1"
    )
    val clawAngle2 by infiniteTransition.animateFloat(
        initialValue = 2f, targetValue = -2f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "claw2"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )

    val colors = MOOD_PALETTE[mood] ?: MOOD_PALETTE[PetMood.HAPPY]!!

    Canvas(modifier = modifier) {
        val cx = size.width / 2
        val cy = size.height / 2 + bounce
        val scale = breathScale * pressScale
        val bodyRadius = size.minDimension * 0.30f * scale

        // Shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.12f),
            topLeft = Offset(cx - bodyRadius * 0.8f, cy + bodyRadius * 0.9f),
            size = Size(bodyRadius * 1.6f, bodyRadius * 0.3f)
        )

        // Body gradient
        drawCircle(
            brush = Brush.verticalGradient(colors = listOf(colors.bodyTop, colors.bodyBottom)),
            radius = bodyRadius,
            center = Offset(cx, cy)
        )

        // Shine highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = bodyRadius * 0.35f,
            center = Offset(cx - bodyRadius * 0.25f, cy - bodyRadius * 0.3f)
        )

        // 3 Claws with rounded tips
        val clawOffsets = listOf(-1, 0, 1)
        val clawAngles = listOf(clawAngle1, clawAngle2, clawAngle1)
        clawOffsets.forEachIndexed { i, offset ->
            val clawX = cx + offset * size.minDimension * 0.09f
            val clawY = cy - bodyRadius * 0.95f
            val clawSize = size.minDimension * 0.06f
            val path = Path().apply {
                moveTo(clawX, clawY - clawSize * 2)
                quadraticBezierTo(clawX - clawSize * 1.2f, clawY - clawSize * 0.5f, clawX, clawY + clawSize * 0.5f)
                quadraticBezierTo(clawX + clawSize * 1.2f, clawY - clawSize * 0.5f, clawX, clawY - clawSize * 2)
                close()
            }
            drawPath(path, color = colors.bodyTop.copy(alpha = 0.95f), style = Fill)
            drawPath(path, color = Color.White, style = Stroke(width = 2f))
        }

        // Eyes
        val eyeSpacing = size.minDimension * 0.1f
        val eyeY = cy - size.minDimension * 0.06f
        val eyeRadius = size.minDimension * 0.045f
        val maxPupilOffset = eyeRadius * 0.4f

        // Pupil tracking
        val normalizedTapX = if (tapOffset != Offset.Zero) ((tapOffset.x - cx) / size.width).coerceIn(-1f, 1f) else 0f
        val normalizedTapY = if (tapOffset != Offset.Zero) ((tapOffset.y - cy) / size.height).coerceIn(-1f, 1f) else 0f
        val pupilOffX = normalizedTapX * maxPupilOffset
        val pupilOffY = normalizedTapY * maxPupilOffset

        when (mood) {
            PetMood.SLEEPY -> {
                // Closed eyes (lines)
                drawLine(colors.eye, Offset(cx - eyeSpacing - eyeRadius, eyeY), Offset(cx - eyeSpacing + eyeRadius, eyeY), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(colors.eye, Offset(cx + eyeSpacing - eyeRadius, eyeY), Offset(cx + eyeSpacing + eyeRadius, eyeY), strokeWidth = 3f, cap = StrokeCap.Round)
                // Zzz dots
                for (zi in 1..3) {
                    val zx = cx + size.minDimension * 0.22f + zi * 12f
                    val zy = cy - size.minDimension * 0.28f - zi * 16f
                    drawCircle(Color.White.copy(alpha = 0.4f / zi), radius = 5f - zi * 1f, center = Offset(zx, zy))
                }
            }
            PetMood.DEAD -> {
                // X eyes
                for (ex in listOf(cx - eyeSpacing, cx + eyeSpacing)) {
                    drawLine(Color.Red, Offset(ex - eyeRadius, eyeY - eyeRadius), Offset(ex + eyeRadius, eyeY + eyeRadius), strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(Color.Red, Offset(ex + eyeRadius, eyeY - eyeRadius), Offset(ex - eyeRadius, eyeY + eyeRadius), strokeWidth = 3f, cap = StrokeCap.Round)
                }
            }
            else -> {
                // Normal eyes with tracking pupils
                drawCircle(colors.eye, eyeRadius, Offset(cx - eyeSpacing, eyeY))
                drawCircle(colors.eye, eyeRadius, Offset(cx + eyeSpacing, eyeY))
                drawCircle(Color(0xFF212121), eyeRadius * 0.5f, Offset(cx - eyeSpacing + pupilOffX, eyeY + pupilOffY))
                drawCircle(Color(0xFF212121), eyeRadius * 0.5f, Offset(cx + eyeSpacing + pupilOffX, eyeY + pupilOffY))
                // Highlights
                drawCircle(Color.White, eyeRadius * 0.15f, Offset(cx - eyeSpacing + eyeRadius * 0.2f, eyeY - eyeRadius * 0.2f))
                drawCircle(Color.White, eyeRadius * 0.15f, Offset(cx + eyeSpacing + eyeRadius * 0.2f, eyeY - eyeRadius * 0.2f))
            }
        }

        // Mouth
        val mouthY = cy + size.minDimension * 0.1f
        when (mood) {
            PetMood.HAPPY, PetMood.EXCITED -> {
                drawArc(colors.mouth, 20f, 140f, useCenter = false,
                    topLeft = Offset(cx - size.minDimension * 0.08f, mouthY - size.minDimension * 0.04f),
                    size = Size(size.minDimension * 0.16f, size.minDimension * 0.08f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round))
                if (mood == PetMood.EXCITED) {
                    drawOval(Color(0xFFFF8A80), topLeft = Offset(cx - size.minDimension * 0.02f, mouthY + size.minDimension * 0.01f),
                        size = Size(size.minDimension * 0.04f, size.minDimension * 0.03f))
                }
            }
            PetMood.SAD -> {
                drawArc(colors.mouth, 200f, 140f, useCenter = false,
                    topLeft = Offset(cx - size.minDimension * 0.08f, mouthY),
                    size = Size(size.minDimension * 0.16f, size.minDimension * 0.08f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
            PetMood.HUNGRY -> {
                drawCircle(colors.mouth, size.minDimension * 0.05f, Offset(cx, mouthY), style = Stroke(width = 3f))
            }
            PetMood.SLEEPY -> {
                drawCircle(Color(0xFF90A4AE), size.minDimension * 0.03f, Offset(cx, mouthY))
            }
            PetMood.DEAD -> {
                drawLine(Color.Red, Offset(cx - size.minDimension * 0.06f, mouthY), Offset(cx + size.minDimension * 0.06f, mouthY), strokeWidth = 3f)
            }
        }

        // Blush cheeks (happy/excited)
        if (mood == PetMood.HAPPY || mood == PetMood.EXCITED) {
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.35f), size.minDimension * 0.04f,
                Offset(cx - size.minDimension * 0.18f, eyeY + size.minDimension * 0.06f))
            drawCircle(Color(0xFFFF8A80).copy(alpha = 0.35f), size.minDimension * 0.04f,
                Offset(cx + size.minDimension * 0.18f, eyeY + size.minDimension * 0.06f))
        }

        // Particle effects
        particles.forEach { p ->
            val alpha = p.life.coerceIn(0f, 1f)
            when (p.type) {
                ParticleType.HEART -> {
                    drawCircle(Color(0xFFF472B6).copy(alpha = alpha), 6f,
                        Offset(cx + p.x * size.width, cy + p.y * size.height))
                }
                ParticleType.STAR -> {
                    drawCircle(Color(0xFFFFD700).copy(alpha = alpha), 4f,
                        Offset(cx + p.x * size.width, cy + p.y * size.height))
                }
                ParticleType.Z -> {}
            }
        }
    }
}