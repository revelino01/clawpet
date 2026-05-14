package com.clawpet.widget

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/** Wander state for the animated pet on the widget. */
data class PetFrameState(
    val x: Float,
    val y: Float,
    val targetX: Float,
    val targetY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val blinkPhase: Float,      // 0=open, 1=closed
    val wanderTimer: Int,
    val phase: Float,            // breathing cycle
    val mouthOpen: Float,        // 0=closed, 1=open (for hungry)
    val moodEmoji: String,
)

class PetAnimator(
    private val width: Float,
    private val height: Float,
) {
    private val rng = java.util.Random()
    private val padding = 40f

    var state = PetFrameState(
        x = width / 2, y = height / 2,
        targetX = width / 2, targetY = height / 2,
        scaleX = 1f, scaleY = 1f,
        blinkPhase = 0f, wanderTimer = 0, phase = 0f,
        mouthOpen = 0f, moodEmoji = "😊",
    )
        private set

    fun advance(moodEmoji: String = "😊") {
        val s = state
        var phase = s.phase + 0.12f
        val breath = 1f + sin(phase) * 0.02f

        var timer = s.wanderTimer - 1
        var targetX = s.targetX
        var targetY = s.targetY

        // Pick a new random target when timer expires
        if (timer <= 0) {
            targetX = padding + rng.nextFloat() * (width - padding * 2)
            targetY = padding + rng.nextFloat() * (height - padding * 2)
            timer = 40 + rng.nextInt(80) // ~40-120 frames between targets
        }

        val dx = targetX - s.x
        val dy = targetY - s.y
        val dist = hypot(dx, dy)

        var x = s.x
        var y = s.y
        var scaleX = breath
        var scaleY = breath

        // Squash-and-stretch: anticipation before moving, stretch in direction
        if (dist > 3f) {
            val speed = 2.5f
            x += (dx / dist) * speed
            y += (dy / dist) * speed
            val angle = atan2(dy, dx)
            scaleX = 1f + cos(angle) * 0.06f
            scaleY = 1f + sin(angle) * 0.04f
        }

        // Blinking
        var blink = s.blinkPhase
        if (rng.nextFloat() < 0.03f && blink == 0f) blink = 1f
        if (blink > 0f) {
            blink -= 0.3f
            if (blink < 0f) blink = 0f
        }

        state = PetFrameState(
            x = x, y = y,
            targetX = targetX, targetY = targetY,
            scaleX = scaleX.coerceIn(0.88f, 1.12f),
            scaleY = scaleY.coerceIn(0.88f, 1.12f),
            blinkPhase = blink,
            wanderTimer = timer,
            phase = phase,
            mouthOpen = if (moodEmoji in listOf("😋", "🤩")) 0.7f else 0f,
            moodEmoji = moodEmoji,
        )
    }
}