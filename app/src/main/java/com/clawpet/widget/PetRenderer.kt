package com.clawpet.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.hypot

/** Renders the pet to a Bitmap using android.graphics.Canvas (no Compose needed). */
class PetRenderer {
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyeWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blushPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clawPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clawStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val zPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Mood color palettes: bodyTop, bodyBottom, eyeWhite, mouthColor
    private val palettes = mapOf(
        "😊" to intArrayOf(0xFF8B5CF6.toInt(), 0xFF6D28D9.toInt(), 0xFFFFFFFF.toInt(), 0xFF4CAF50.toInt()),  // HAPPY
        "😋" to intArrayOf(0xFFFF9800.toInt(), 0xFFE65100.toInt(), 0xFFFFFFFF.toInt(), 0xFFFF5722.toInt()),  // HUNGRY
        "😴" to intArrayOf(0xFF78909C.toInt(), 0xFF546E7A.toInt(), 0xFFB0BEC5.toInt(), 0xFF90A4AE.toInt()),  // SLEEPY
        "😢" to intArrayOf(0xFF9E9E9E.toInt(), 0xFF616161.toInt(), 0xFFE0E0E0.toInt(), 0xFF757575.toInt()),  // SAD
        "🤩" to intArrayOf(0xFFE91E63.toInt(), 0xFFAD1457.toInt(), 0xFFFFEB3B.toInt(), 0xFFFF9800.toInt()),  // EXCITED
        "💀" to intArrayOf(0xFF424242.toInt(), 0xFF212121.toInt(), 0xFF616161.toInt(), 0xFF424242.toInt()),  // DEAD
    )

    fun render(state: PetFrameState, w: Int, h: Int): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val colors = palettes[state.moodEmoji] ?: palettes["😊"]!!
        val bodyTop = colors[0]
        val bodyBottom = colors[1]
        val eyeWhite = colors[2]
        val mouthColor = colors[3]

        val cx = state.x
        val cy = state.y
        val bodyRadius = w * 0.18f

        // Shadow (scales inverse to squash)
        shadowPaint.color = Color.argb(30, 0, 0, 0)
        val shadowScale = 1f / state.scaleX.coerceIn(0.5f, 1.5f)
        canvas.drawOval(
            RectF(
                cx - bodyRadius * 0.85f * shadowScale,
                cy + bodyRadius * 0.85f,
                cx + bodyRadius * 0.85f * shadowScale,
                cy + bodyRadius * 1.15f
            ),
            shadowPaint
        )

        // Body with gradient
        canvas.save()
        canvas.translate(cx, cy)
        canvas.scale(state.scaleX, state.scaleY)
        bodyPaint.shader = LinearGradient(
            0f, -bodyRadius, 0f, bodyRadius,
            bodyTop, bodyBottom, Shader.TileMode.CLAMP
        )
        canvas.drawCircle(0f, 0f, bodyRadius, bodyPaint)

        // Shine highlight
        highlightPaint.color = Color.argb(46, 255, 255, 255) // 18% white
        canvas.drawCircle(-bodyRadius * 0.25f, -bodyRadius * 0.3f, bodyRadius * 0.35f, highlightPaint)

        // 3 Claws (rounded triangles)
        val clawOffsets = floatArrayOf(-0.12f, 0f, 0.12f)
        val clawW = w * 0.04f
        clawPaint.color = bodyTop
        clawStrokePaint.color = Color.WHITE
        clawStrokePaint.style = Paint.Style.STROKE
        clawStrokePaint.strokeWidth = 2f

        for (offset in clawOffsets) {
            val clawX = offset * w
            val clawTop = -bodyRadius * 1.3f
            val clawBot = -bodyRadius * 0.7f
            val path = Path()
            path.moveTo(clawX, clawTop)
            path.quadTo(clawX - clawW * 1.5f, (clawTop + clawBot) / 2, clawX, clawBot)
            path.quadTo(clawX + clawW * 1.5f, (clawTop + clawBot) / 2, clawX, clawTop)
            path.close()
            canvas.drawPath(path, clawPaint)
            canvas.drawPath(path, clawStrokePaint)
        }

        // Eyes
        val eyeSpacing = w * 0.075f
        val eyeY = -bodyRadius * 0.15f
        val eyeRadius = w * 0.04f

        eyeWhitePaint.color = eyeWhite
        pupilPaint.color = 0xFF212121.toInt()
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeWidth = 3f

        when (state.moodEmoji) {
            "😴" -> {
                // Closed eyes (horizontal lines)
                linePaint.color = eyeWhite
                canvas.drawLine(cx - eyeSpacing - eyeRadius, eyeY, cx - eyeSpacing + eyeRadius, eyeY, linePaint)
                canvas.drawLine(cx + eyeSpacing - eyeRadius, eyeY, cx + eyeSpacing + eyeRadius, eyeY, linePaint)
                // Zzz
                zPaint.color = Color.WHITE
                zPaint.alpha = 80
                zPaint.textSize = 14f
                for (zi in 1..3) {
                    canvas.drawText("z", cx + bodyRadius * 0.6f + zi * 12f, eyeY - zi * 18f, zPaint)
                }
            }
            "💀" -> {
                // X eyes
                linePaint.color = Color.RED
                linePaint.strokeWidth = 3f
                for (ex in floatArrayOf(cx - eyeSpacing, cx + eyeSpacing)) {
                    canvas.drawLine(ex - eyeRadius, eyeY - eyeRadius, ex + eyeRadius, eyeY + eyeRadius, linePaint)
                    canvas.drawLine(ex + eyeRadius, eyeY - eyeRadius, ex - eyeRadius, eyeY + eyeRadius, linePaint)
                }
            }
            else -> {
                // Open eyes with pupils
                canvas.drawCircle(cx - eyeSpacing, eyeY, eyeRadius, eyeWhitePaint)
                canvas.drawCircle(cx + eyeSpacing, eyeY, eyeRadius, eyeWhitePaint)

                // Pupil tracking (look toward movement target)
                val maxPupilOff = eyeRadius * 0.35f
                val lookX = if (hypot((state.targetX - cx).toDouble(), (state.targetY - cy).toDouble()) > 1f) {
                    ((state.targetX - cx) / w).coerceIn(-1f, 1f) * maxPupilOff
                } else 0f
                val lookY = if (hypot((state.targetX - cx).toDouble(), (state.targetY - cy).toDouble()) > 1f) {
                    ((state.targetY - cy) / h).coerceIn(-1f, 1f) * maxPupilOff
                } else 0f

                // Blink: draw line instead of pupils when closing
                if (state.blinkPhase > 0.4f) {
                    linePaint.color = 0xFF212121.toInt()
                    linePaint.strokeWidth = 2f
                    canvas.drawLine(cx - eyeSpacing - eyeRadius * 0.6f, eyeY, cx - eyeSpacing + eyeRadius * 0.6f, eyeY, linePaint)
                    canvas.drawLine(cx + eyeSpacing - eyeRadius * 0.6f, eyeY, cx + eyeSpacing + eyeRadius * 0.6f, eyeY, linePaint)
                } else {
                    canvas.drawCircle(cx - eyeSpacing + lookX, eyeY + lookY, eyeRadius * 0.45f, pupilPaint)
                    canvas.drawCircle(cx + eyeSpacing + lookX, eyeY + lookY, eyeRadius * 0.45f, pupilPaint)
                    // Tiny highlight
                    highlightPaint.color = Color.WHITE
                    canvas.drawCircle(cx - eyeSpacing + eyeRadius * 0.2f, eyeY - eyeRadius * 0.25f, eyeRadius * 0.15f, highlightPaint)
                    canvas.drawCircle(cx + eyeSpacing + eyeRadius * 0.2f, eyeY - eyeRadius * 0.25f, eyeRadius * 0.15f, highlightPaint)
                }
            }
        }

        // Mouth
        val mouthY = bodyRadius * 0.35f
        mouthPaint.style = Paint.Style.STROKE
        mouthPaint.strokeCap = Paint.Cap.ROUND
        mouthPaint.strokeWidth = 3f

        when (state.moodEmoji) {
            "😊", "🤩" -> {
                // Smile arc
                mouthPaint.color = mouthColor
                val rect = RectF(-bodyRadius * 0.35f, mouthY - bodyRadius * 0.1f, bodyRadius * 0.35f, mouthY + bodyRadius * 0.2f)
                canvas.drawArc(rect, 20f, 140f, false, mouthPaint)
                // Tongue when excited
                if (state.moodEmoji == "🤩") {
                    mouthPaint.style = Paint.Style.FILL
                    mouthPaint.color = 0xFFFF8A80.toInt()
                    canvas.drawOval(RectF(-bodyRadius * 0.08f, mouthY + bodyRadius * 0.03f, bodyRadius * 0.08f, mouthY + bodyRadius * 0.12f), mouthPaint)
                }
            }
            "😋" -> {
                // Open mouth (O shape)
                mouthPaint.color = mouthColor
                mouthPaint.style = Paint.Style.STROKE
                canvas.drawCircle(0f, mouthY, bodyRadius * 0.15f, mouthPaint)
            }
            "😢" -> {
                // Frown arc
                mouthPaint.color = mouthColor
                val rect = RectF(-bodyRadius * 0.35f, mouthY, bodyRadius * 0.35f, mouthY + bodyRadius * 0.25f)
                canvas.drawArc(rect, 200f, 140f, false, mouthPaint)
            }
            "😴" -> {
                mouthPaint.style = Paint.Style.FILL
                mouthPaint.color = 0xFF90A4AE.toInt()
                canvas.drawCircle(0f, mouthY, bodyRadius * 0.06f, mouthPaint)
            }
            "💀" -> {
                linePaint.color = Color.RED
                linePaint.strokeWidth = 3f
                canvas.drawLine(-bodyRadius * 0.2f, mouthY, bodyRadius * 0.2f, mouthY, linePaint)
            }
        }

        // Blush cheeks (happy/excited)
        if (state.moodEmoji == "😊" || state.moodEmoji == "🤩") {
            blushPaint.color = Color.argb(89, 244, 114, 182) // ~35% alpha pink
            canvas.drawCircle(-bodyRadius * 0.55f, eyeY + bodyRadius * 0.2f, bodyRadius * 0.12f, blushPaint)
            canvas.drawCircle(bodyRadius * 0.55f, eyeY + bodyRadius * 0.2f, bodyRadius * 0.12f, blushPaint)
        }

        canvas.restore()

        return bmp
    }
}