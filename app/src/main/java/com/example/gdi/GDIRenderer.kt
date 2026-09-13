package com.example.gdi

import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.GDIPayload
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Legacy Windows Mobile GDI (Graphics Device Interface) renderer.
 * Simulates low-level raster operations (BitBlt, InvertRect, PatBlt, Ellipse, TextOut)
 * across the 4 requested payloads.
 */
class GDIRenderer {

    // Bouncing circle simulation state for Payload 3
    data class BounceCircle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val radius: Float,
        val color: Color,
        val isFilled: Boolean
    )

    data class CircleTrail(
        val x: Float,
        val y: Float,
        val radius: Float,
        val color: Color,
        val isFilled: Boolean,
        var life: Float = 1.0f
    )

    // Bouncing Triangles (Mocq epic.exe / Coore32.exe simulation)
    data class BounceTriangle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var rotation: Float,
        var rotationSpeed: Float,
        val color: Color,
        val isFilled: Boolean,
        val mode: Int // 0: solid, 1: wireframe GDI pen, 2: inverted difference, 3: hatched
    )

    data class TriangleTrail(
        val p1: Offset,
        val p2: Offset,
        val p3: Offset,
        val color: Color,
        val isFilled: Boolean,
        var life: Float = 1.0f
    )

    private val bounceCircles = mutableListOf<BounceCircle>()
    private val circleTrails = mutableListOf<CircleTrail>()
    private val maxTrails = 120

    private val bounceTriangles = mutableListOf<BounceTriangle>()
    private val triangleTrails = mutableListOf<TriangleTrail>()
    private val maxTriangleTrails = 90

    // Native paint for GDI TextOut
    private val textPaint = AndroidPaint().apply {
        isAntiAlias = false // Authentic pixelated GDI legacy font look
        typeface = Typeface.MONOSPACE
        textSize = 42f
        isFakeBoldText = true
    }

    private val subTextPaint = AndroidPaint().apply {
        isAntiAlias = false
        typeface = Typeface.MONOSPACE
        textSize = 24f
        isFakeBoldText = true
    }

    private val random = Random(42)

    init {
        resetBounceCircles()
        resetBounceTriangles()
    }

    fun resetBounceTriangles() {
        bounceTriangles.clear()
        triangleTrails.clear()
        val colors = listOf(
            Color(0xFFFF0055), // Vibrant Red-Pink (Coore32 style)
            Color(0xFF00FFCC), // Aqua Teal
            Color(0xFFFFFF00), // Pure Yellow
            Color(0xFF00E5FF), // Electric Cyan
            Color(0xFFFF8800), // Amber Orange
            Color(0xFF76FF03), // Lime Green
            Color(0xFFD500F9), // Neon Magenta
            Color(0xFFFFFFFF), // Pure White
            Color(0xFF2979FF)  // Deep Sky Blue
        )

        for (i in 0 until 9) {
            val sz = 45f + (i * 8f)
            bounceTriangles.add(
                BounceTriangle(
                    x = 80f + i * 45f,
                    y = 120f + i * 50f,
                    vx = (if (i % 2 == 0) 6f else -6f) * (1.1f + (i % 3) * 0.4f),
                    vy = (if (i % 3 == 0) 7f else -7f) * (1.1f + (i % 2) * 0.3f),
                    size = sz,
                    rotation = i * 40f,
                    rotationSpeed = (if (i % 2 == 0) 3.5f else -3.5f) * (1f + (i % 3) * 0.5f),
                    color = colors[i % colors.size],
                    isFilled = (i % 2 == 0),
                    mode = i % 4
                )
            )
        }
    }

    fun resetBounceCircles() {
        bounceCircles.clear()
        circleTrails.clear()
        val colors = listOf(
            Color(0xFFFF0000), // Pure Red
            Color(0xFF00FF00), // Pure Green
            Color(0xFF0000FF), // Pure Blue
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF00FF), // Magenta
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFFFFFF), // White
            Color(0xFFFF8800)  // Orange
        )

        for (i in 0 until 8) {
            val r = 40f + (i * 12f)
            bounceCircles.add(
                BounceCircle(
                    x = 100f + i * 50f,
                    y = 100f + i * 40f,
                    vx = (if (i % 2 == 0) 5f else -5f) * (1f + (i % 3) * 0.5f),
                    vy = (if (i % 3 == 0) 6f else -6f) * (1f + (i % 2) * 0.4f),
                    radius = r,
                    color = colors[i % colors.size],
                    isFilled = (i % 2 == 0)
                )
            )
        }
    }

    fun render(
        drawScope: DrawScope,
        payload: GDIPayload,
        elapsedTimeMs: Long,
        audioWaveform: FloatArray,
        showScanlines: Boolean
    ) {
        val width = drawScope.size.width
        val height = drawScope.size.height
        if (width <= 0 || height <= 0) return

        when (payload) {
            GDIPayload.PAYLOAD_1 -> renderPayload1GlitchDrone(drawScope, width, height, elapsedTimeMs, audioWaveform)
            GDIPayload.PAYLOAD_2 -> renderPayload2Swirl(drawScope, width, height, elapsedTimeMs, audioWaveform)
            GDIPayload.PAYLOAD_3 -> renderPayload3BounceCircles(drawScope, width, height, elapsedTimeMs)
            GDIPayload.PAYLOAD_4 -> renderPayload4TextOut(drawScope, width, height, elapsedTimeMs, audioWaveform)
            GDIPayload.PAYLOAD_5 -> renderPayload5RandomRectangles(drawScope, width, height, elapsedTimeMs, audioWaveform)
            GDIPayload.PAYLOAD_6 -> renderPayload6BouncingTriangles(drawScope, width, height, elapsedTimeMs, audioWaveform)
            GDIPayload.PAYLOAD_7 -> renderPayload7CurvyLines(drawScope, width, height, elapsedTimeMs, audioWaveform)
        }

        // Overlay legacy GDI CRT/LCD scanline effect if enabled
        if (showScanlines) {
            renderScanlines(drawScope, width, height)
        }
    }

    /**
     * Payload 1: Chaotic, glitch-heavy drone composed of randomized high-frequency chirps.
     * Features BitBlt slice displacement, InvertRect flashing blocks, horizontal jitter,
     * chromatic strobe bars, and noise patterns.
     */
    private fun renderPayload1GlitchDrone(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        time: Long,
        audioWaveform: FloatArray
    ) {
        // Base dark cyber/GDI canvas
        drawScope.drawRect(
            color = Color(0xFF080810),
            size = Size(width, height)
        )

        val frameSeed = (time / 45).toInt()
        val rng = Random(frameSeed)

        // 1. Chaotic horizontal BitBlt displacement strips
        val stripCount = 28
        val stripHeight = height / stripCount
        for (i in 0 until stripCount) {
            val y = i * stripHeight
            val glitchChance = rng.nextFloat()
            if (glitchChance > 0.45f) {
                val shiftX = (rng.nextFloat() - 0.5f) * width * 0.5f
                val stripColor = when (rng.nextInt(6)) {
                    0 -> Color(0xFFFF0055)
                    1 -> Color(0xFF00FFCC)
                    2 -> Color(0xFFFFFF00)
                    3 -> Color(0xFF110033)
                    4 -> Color(0xFFFFFFFF)
                    else -> Color(0xFF330066)
                }

                drawScope.drawRect(
                    color = stripColor.copy(alpha = 0.85f),
                    topLeft = Offset(shiftX, y),
                    size = Size(width * 1.2f, stripHeight * 0.9f)
                )

                // High-frequency chirp barcode pattern on active strips
                val barCount = 12
                for (b in 0 until barCount) {
                    if (rng.nextBoolean()) {
                        val bx = (width / barCount) * b + shiftX
                        drawScope.drawRect(
                            color = Color.White,
                            topLeft = Offset(bx, y),
                            size = Size(width / (barCount * 2f), stripHeight)
                        )
                    }
                }
            }
        }

        // 2. InvertRect flashing blocks (Legacy GDI DSTINVERT simulation)
        val blockCount = rng.nextInt(3, 9)
        for (b in 0 until blockCount) {
            val bw = rng.nextFloat() * (width * 0.4f) + 40f
            val bh = rng.nextFloat() * (height * 0.25f) + 20f
            val bx = rng.nextFloat() * (width - bw)
            val by = rng.nextFloat() * (height - bh)

            val blockColor = when (b % 4) {
                0 -> Color(0xFF00FFFF)
                1 -> Color(0xFFFF00FF)
                2 -> Color(0xFFFFFF00)
                else -> Color.White
            }

            drawScope.drawRect(
                color = blockColor.copy(alpha = 0.75f),
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                style = if (b % 2 == 0) Fill else Stroke(width = 4f)
            )
        }

        // 3. Audio waveform high-frequency chirp overlay
        if (audioWaveform.isNotEmpty()) {
            val step = width / audioWaveform.size
            val centerY = height * 0.5f
            for (i in 0 until audioWaveform.size - 1) {
                val x1 = i * step
                val y1 = centerY + (audioWaveform[i] * height * 0.35f)
                val x2 = (i + 1) * step
                val y2 = centerY + (audioWaveform[i + 1] * height * 0.35f)

                drawScope.drawLine(
                    color = Color(0xFF00FF44),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 3f
                )

                // High-frequency echo
                drawScope.drawLine(
                    color = Color(0xFFFF0055),
                    start = Offset(x1, y1 + 8f),
                    end = Offset(x2, y2 + 8f),
                    strokeWidth = 1.5f
                )
            }
        }

        // 4. Glitch drone telemetry text
        val canvas = drawScope.drawContext.canvas.nativeCanvas
        textPaint.color = AndroidColor.WHITE
        textPaint.textSize = 28f
        canvas.drawText("TERA_BONUS.EXE [GDI PAYLOAD 1: GLITCH DRONE]", 24f, 60f, textPaint)
        subTextPaint.color = AndroidColor.YELLOW
        canvas.drawText("CHIRP_FREQ: 8000Hz | AUDIO: t*(t>>8)*t>>16&63|t>>4", 24f, 95f, subTextPaint)
    }

    /**
     * Payload 2: Swirl A Movings And swirls.
     * Features rotating Archimedean/logarithmic vortices, radiating twisting rays,
     * undulating concentric rings, and cycling legacy GDI colors.
     */
    private fun renderPayload2Swirl(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        time: Long,
        audioWaveform: FloatArray
    ) {
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        val maxRadius = sqrt(centerX * centerX + centerY * centerY)
        val angleOffset = (time * 0.003f) % (2f * Math.PI.toFloat())

        // Deep blue/black GDI background
        drawScope.drawRect(
            color = Color(0xFF000818),
            size = Size(width, height)
        )

        val swirlArms = 8
        val pointsPerArm = 35
        val gdiPalette = listOf(
            Color(0xFFFF0000), // Red
            Color(0xFF00FF00), // Green
            Color(0xFF0000FF), // Blue
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF00FF), // Magenta
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFFFFFF), // White
            Color(0xFFFF8800)  // Orange
        )

        // 1. Concentric undulating swirl rings
        val ringCount = 14
        for (r in ringCount downTo 1) {
            val ringRadius = (maxRadius / ringCount) * r
            val waveMod = sin((time * 0.005f) + r * 0.5f) * 16f
            val ringColor = gdiPalette[(r + (time / 300).toInt()) % gdiPalette.size]

            drawScope.drawCircle(
                color = ringColor.copy(alpha = 0.5f),
                radius = (ringRadius + waveMod).coerceAtLeast(4f),
                center = Offset(centerX, centerY),
                style = Stroke(width = 3f + (r % 3) * 2f)
            )
        }

        // 2. Swirling spiral arms (Archimedean / Logarithmic vortex)
        for (arm in 0 until swirlArms) {
            val baseAngle = (arm * (2f * Math.PI.toFloat() / swirlArms)) + angleOffset
            val armPath = Path()
            var first = true

            val armColor = gdiPalette[arm % gdiPalette.size]

            for (p in 0..pointsPerArm) {
                val progress = p.toFloat() / pointsPerArm
                val dist = progress * maxRadius
                // Swirl formula: angle increases non-linearly with distance
                val twistAngle = baseAngle + (progress * 4.5f) + sin((time * 0.004f) + progress * 3f) * 0.4f
                val px = centerX + cos(twistAngle) * dist
                val py = centerY + sin(twistAngle) * dist

                if (first) {
                    armPath.moveTo(px, py)
                    first = false
                } else {
                    armPath.lineTo(px, py)
                }

                // Periodic GDI diamond / polygon nodes along the swirl arm
                if (p % 7 == 0 && p > 0) {
                    val nodeSize = 10f + progress * 16f
                    drawScope.drawRect(
                        color = armColor,
                        topLeft = Offset(px - nodeSize / 2f, py - nodeSize / 2f),
                        size = Size(nodeSize, nodeSize),
                        style = if (p % 14 == 0) Fill else Stroke(2f)
                    )
                }
            }

            drawScope.drawPath(
                path = armPath,
                color = armColor,
                style = Stroke(width = 4f)
            )
        }

        // 3. Center pulsating vortex core
        val corePulse = (sin(time * 0.008f) * 0.5f + 0.5f)
        val coreRadius = 25f + corePulse * 30f
        drawScope.drawCircle(
            color = Color(0xFFFFFF00),
            radius = coreRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 6f)
        )
        drawScope.drawCircle(
            color = Color(0xFFFF0055),
            radius = coreRadius * 0.5f,
            center = Offset(centerX, centerY),
            style = Fill
        )

        // Overlay text
        val canvas = drawScope.drawContext.canvas.nativeCanvas
        textPaint.color = AndroidColor.WHITE
        textPaint.textSize = 28f
        canvas.drawText("TERA_BONUS.EXE [GDI PAYLOAD 2: SWIRL & MOVINGS]", 24f, 60f, textPaint)
        subTextPaint.color = AndroidColor.CYAN
        canvas.drawText("VORTEX: ACTIVE | AUDIO: t&t>>7|t&t>>8", 24f, 95f, subTextPaint)
    }

    /**
     * Payload 3: Big bounce circles to draw the this.
     * Large bouncing GDI circles with collision physics that draw persistent trails,
     * spiderweb GDI chords, and geometric Spirograph patterns.
     */
    private fun renderPayload3BounceCircles(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        time: Long
    ) {
        // Dark retro background
        drawScope.drawRect(
            color = Color(0xFF0A0014),
            size = Size(width, height)
        )

        // 1. Update physics for bouncing circles
        for (circle in bounceCircles) {
            circle.x += circle.vx
            circle.y += circle.vy

            // Left / Right bounds collision
            if (circle.x - circle.radius <= 0) {
                circle.x = circle.radius
                circle.vx = -circle.vx
            } else if (circle.x + circle.radius >= width) {
                circle.x = width - circle.radius
                circle.vx = -circle.vx
            }

            // Top / Bottom bounds collision
            if (circle.y - circle.radius <= 0) {
                circle.y = circle.radius
                circle.vy = -circle.vy
            } else if (circle.y + circle.radius >= height) {
                circle.y = height - circle.radius
                circle.vy = -circle.vy
            }

            // Every few ticks, stamp a trail to "draw the this"
            if (time % 2L == 0L) {
                circleTrails.add(
                    CircleTrail(
                        x = circle.x,
                        y = circle.y,
                        radius = circle.radius * 0.75f,
                        color = circle.color,
                        isFilled = circle.isFilled,
                        life = 1.0f
                    )
                )
                if (circleTrails.size > maxTrails) {
                    circleTrails.removeAt(0)
                }
            }
        }

        // 2. Draw persistent trails ("drawing the this")
        val it = circleTrails.iterator()
        while (it.hasNext()) {
            val trail = it.next()
            trail.life -= 0.007f
            if (trail.life <= 0f) {
                it.remove()
                continue
            }

            drawScope.drawCircle(
                color = trail.color.copy(alpha = (trail.life * 0.5f).coerceIn(0f, 1f)),
                radius = trail.radius * (0.8f + (1f - trail.life) * 0.4f),
                center = Offset(trail.x, trail.y),
                style = if (trail.isFilled) Fill else Stroke(width = 2f)
            )
        }

        // 3. Draw spiderweb connective chords between bouncing circles
        for (i in 0 until bounceCircles.size) {
            for (j in i + 1 until bounceCircles.size) {
                val c1 = bounceCircles[i]
                val c2 = bounceCircles[j]
                val dx = c1.x - c2.x
                val dy = c1.y - c2.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 320f) {
                    val alpha = (1f - dist / 320f) * 0.7f
                    drawScope.drawLine(
                        color = c1.color.copy(alpha = alpha),
                        start = Offset(c1.x, c1.y),
                        end = Offset(c2.x, c2.y),
                        strokeWidth = 2f
                    )
                }
            }
        }

        // 4. Draw active Big Bouncing Circles (GDI Ellipse / CreateSolidBrush)
        for (circle in bounceCircles) {
            // Shadow / Inverted ring
            drawScope.drawCircle(
                color = Color.Black,
                radius = circle.radius + 4f,
                center = Offset(circle.x, circle.y),
                style = Stroke(width = 4f)
            )

            if (circle.isFilled) {
                drawScope.drawCircle(
                    color = circle.color.copy(alpha = 0.85f),
                    radius = circle.radius,
                    center = Offset(circle.x, circle.y),
                    style = Fill
                )
                // Center accent dot
                drawScope.drawCircle(
                    color = Color.White,
                    radius = circle.radius * 0.25f,
                    center = Offset(circle.x, circle.y),
                    style = Fill
                )
            } else {
                drawScope.drawCircle(
                    color = circle.color,
                    radius = circle.radius,
                    center = Offset(circle.x, circle.y),
                    style = Stroke(width = 6f)
                )
            }
        }

        // HUD overlay
        val canvas = drawScope.drawContext.canvas.nativeCanvas
        textPaint.color = AndroidColor.WHITE
        textPaint.textSize = 28f
        canvas.drawText("TERA_BONUS.EXE [GDI PAYLOAD 3: BIG BOUNCE CIRCLES]", 24f, 60f, textPaint)
        subTextPaint.color = AndroidColor.GREEN
        canvas.drawText("TRAILS: ACTIVE (${circleTrails.size}) | AUDIO: t*(t&t>>12)/256", 24f, 95f, subTextPaint)
    }

    /**
     * Payload 4: Textout says: Tera Bonus.apk.
     * Simulates Win32 GDI TextOut API stamping "Tera Bonus.apk" across the canvas
     * with matrix cascades, giant 3D embossed logo, and colored text bounding boxes.
     */
    private fun renderPayload4TextOut(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        time: Long,
        audioWaveform: FloatArray
    ) {
        val canvas = drawScope.drawContext.canvas.nativeCanvas

        // Classic retro dark/navy background
        drawScope.drawRect(
            color = Color(0xFF001020),
            size = Size(width, height)
        )

        // 1. Matrix waterfall cascade of "Tera Bonus.apk" in background
        val colCount = 10
        val colWidth = width / colCount
        for (c in 0 until colCount) {
            val speed = 3f + (c % 5) * 1.5f
            val baseOffset = (time * 0.12f * speed) % height
            val x = c * colWidth + 8f

            val rowCount = (height / 35f).toInt() + 2
            for (r in 0 until rowCount) {
                val y = (baseOffset + r * 35f) % height
                val isLeader = (r == 0)

                subTextPaint.textSize = 18f
                subTextPaint.color = if (isLeader) {
                    AndroidColor.WHITE
                } else {
                    AndroidColor.rgb(0, 180 - (r * 12).coerceIn(0, 140), 255)
                }

                canvas.drawText("Tera Bonus.apk", x, y, subTextPaint)
            }
        }

        // 2. Randomized stamped GDI TextOut boxes (simulating SetBkMode OPAQUE + SetTextColor)
        val stampSeed = (time / 350).toInt()
        val rng = Random(stampSeed)
        val stampCount = 8

        for (s in 0 until stampCount) {
            val sx = rng.nextFloat() * (width - 220f)
            val sy = rng.nextFloat() * (height - 80f) + 40f
            val boxW = 210f
            val boxH = 42f

            val (bgColor, fgColor) = when (s % 5) {
                0 -> Pair(Color(0xFFFF0000), AndroidColor.YELLOW)
                1 -> Pair(Color(0xFFFFFF00), AndroidColor.BLACK)
                2 -> Pair(Color(0xFF0000AA), AndroidColor.WHITE)
                3 -> Pair(Color(0xFF00AA00), AndroidColor.WHITE)
                else -> Pair(Color(0xFFAA00AA), AndroidColor.CYAN)
            }

            // GDI Opaque background rectangle
            drawScope.drawRect(
                color = bgColor,
                topLeft = Offset(sx, sy),
                size = Size(boxW, boxH)
            )
            // 3D beveled border
            drawScope.drawRect(
                color = Color.White,
                topLeft = Offset(sx, sy),
                size = Size(boxW, boxH),
                style = Stroke(2f)
            )

            textPaint.textSize = 22f
            textPaint.color = fgColor
            canvas.drawText("Tera Bonus.apk", sx + 10f, sy + 28f, textPaint)
        }

        // 3. Giant Centered 3D Embossed GDI Banner: "Tera Bonus.apk"
        val bannerY = height * 0.48f
        val pulse = sin(time * 0.006f) * 8f

        // Dark banner backplate
        drawScope.drawRect(
            color = Color(0xFF000080),
            topLeft = Offset(16f, bannerY - 60f),
            size = Size(width - 32f, 120f)
        )
        // 3D Windows Mobile border bevel
        drawScope.drawRect(
            color = Color.White,
            topLeft = Offset(16f, bannerY - 60f),
            size = Size(width - 32f, 120f),
            style = Stroke(3f)
        )

        textPaint.textSize = 48f

        // Drop shadow (GDI raster shadow)
        textPaint.color = AndroidColor.rgb(0, 0, 0)
        canvas.drawText("Tera Bonus.apk", (width * 0.5f) - 170f + 6f, bannerY + 12f + pulse + 6f, textPaint)

        // Cyan / Red chromatic 3D offset
        textPaint.color = AndroidColor.rgb(255, 0, 80)
        canvas.drawText("Tera Bonus.apk", (width * 0.5f) - 170f - 3f, bannerY + 12f + pulse, textPaint)

        textPaint.color = AndroidColor.rgb(0, 255, 255)
        canvas.drawText("Tera Bonus.apk", (width * 0.5f) - 170f + 3f, bannerY + 12f + pulse, textPaint)

        // Main text
        textPaint.color = AndroidColor.WHITE
        canvas.drawText("Tera Bonus.apk", (width * 0.5f) - 170f, bannerY + 12f + pulse, textPaint)

        // 4. Moving diagonal floating banners
        val bannerOffset = (time * 0.15f) % (width + 300f) - 150f
        subTextPaint.textSize = 26f
        subTextPaint.color = AndroidColor.YELLOW
        canvas.drawText(">>> TextOut(hdc, x, y, \"Tera Bonus.apk\", 14) <<<", bannerOffset, height * 0.85f, subTextPaint)

        // HUD Header
        textPaint.color = AndroidColor.WHITE
        textPaint.textSize = 28f
        canvas.drawText("TERA_BONUS.EXE [GDI PAYLOAD 4: TEXTOUT]", 24f, 60f, textPaint)
        subTextPaint.color = AndroidColor.MAGENTA
        canvas.drawText("API: TextOutA() | AUDIO: t&t>>8+t*(t)", 24f, 95f, subTextPaint)
    }

    /**
     * Payload 5: Random rectangles to Size and PX.
     * Simulates the infamous Windows GDI malware / demoscene payload with:
     * - Random rectangles generated with varying pixel dimensions and positions (Size and PX).
     * - Win32 GDI raster ops (PATINVERT via BlendMode.Difference, solid fills, and hatched lines).
     * - Coordinate and dimension tags (e.g., "[x, y] w x h PX").
     * - Calibration pixel ruler ticks along the perimeter.
     * - Real-time modulation by Bytebeat formula: t%((t&-16|t>>11)&42)<<2|t>>4.
     */
    private fun renderPayload5RandomRectangles(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        time: Long,
        audioWaveform: FloatArray
    ) {
        val canvas = drawScope.drawContext.canvas.nativeCanvas

        // 1. Dark navy blueprint/terminal background
        drawScope.drawRect(
            color = Color(0xFF070B19),
            size = Size(width, height)
        )

        // Pixel grid (PX coordinate lines every 60px)
        val gridStep = 60f
        var gx = 0f
        while (gx < width) {
            drawScope.drawLine(
                color = Color(0x22336699),
                start = Offset(gx, 0f),
                end = Offset(gx, height),
                strokeWidth = 1f
            )
            gx += gridStep
        }
        var gy = 0f
        while (gy < height) {
            drawScope.drawLine(
                color = Color(0x22336699),
                start = Offset(0f, gy),
                end = Offset(width, gy),
                strokeWidth = 1f
            )
            gy += gridStep
        }

        // Ruler PX labels on top and left axes
        subTextPaint.textSize = 14f
        subTextPaint.color = AndroidColor.rgb(80, 160, 255)
        var rx = 60f
        while (rx < width - 40f) {
            canvas.drawText("${rx.toInt()}px", rx + 2f, 18f, subTextPaint)
            rx += 120f
        }
        var ry = 60f
        while (ry < height - 40f) {
            canvas.drawText("${ry.toInt()}px", 4f, ry - 4f, subTextPaint)
            ry += 120f
        }

        // 2. Audio energy factor
        var audioSum = 0f
        for (i in 0 until minOf(32, audioWaveform.size)) {
            audioSum += kotlin.math.abs(audioWaveform[i])
        }
        val audioEnergy = (audioSum / 32f).coerceIn(0.1f, 1.0f)
        val pulseMultiplier = 0.8f + (audioEnergy * 0.7f)

        // 3. Procedural pseudo-random rectangles (bursts every 90ms)
        val frameId = (time / 90L).toInt()
        val rng = Random(frameId * 31337)

        val palette = listOf(
            Color(0xFFFF0000), // Red
            Color(0xFF00FF00), // Green
            Color(0xFF0066FF), // Blue
            Color(0xFFFFFF00), // Yellow
            Color(0xFFFF00FF), // Magenta
            Color(0xFF00FFFF), // Cyan
            Color(0xFFFFFFFF), // White
            Color(0xFFFF8800), // Orange
            Color(0xFF76FF03), // Lime
            Color(0xFFD500F9), // Purple
            Color(0xFF00E5FF), // Aqua
            Color(0xFFFF1744)  // Bright Pink
        )

        val rectCount = 28
        for (i in 0 until rectCount) {
            val baseW = rng.nextFloat() * (width * 0.45f) + 30f
            val baseH = rng.nextFloat() * (height * 0.30f) + 20f
            val rectW = (baseW * pulseMultiplier).coerceAtMost(width)
            val rectH = (baseH * pulseMultiplier).coerceAtMost(height)

            val rectX = rng.nextFloat() * (width - rectW * 0.5f) - (rectW * 0.2f)
            val rectY = rng.nextFloat() * (height - rectH * 0.5f) + 30f

            val color = palette[rng.nextInt(palette.size)]
            val styleMode = rng.nextInt(5)

            when (styleMode) {
                0 -> {
                    // Solid fill with alpha & sharp white GDI pen border
                    drawScope.drawRect(
                        color = color.copy(alpha = 0.75f),
                        topLeft = Offset(rectX, rectY),
                        size = Size(rectW, rectH)
                    )
                    drawScope.drawRect(
                        color = Color.White,
                        topLeft = Offset(rectX, rectY),
                        size = Size(rectW, rectH),
                        style = Stroke(width = 2.5f)
                    )
                }
                1 -> {
                    // Outlined wireframe rectangle (GDI Hollow Brush)
                    drawScope.drawRect(
                        color = color,
                        topLeft = Offset(rectX, rectY),
                        size = Size(rectW, rectH),
                        style = Stroke(width = rng.nextFloat() * 4f + 2f)
                    )
                }
                2 -> {
                    // InvertRect simulation (Difference blend mode / PATINVERT)
                    drawScope.drawRect(
                        color = Color.White,
                        topLeft = Offset(rectX, rectY),
                        size = Size(rectW, rectH),
                        blendMode = BlendMode.Difference
                    )
                }
                3 -> {
                    // Hatched / Diagonal Stripe Pattern Brush
                    drawScope.drawRect(
                        color = Color(0x33000000),
                        topLeft = Offset(rectX, rectY),
                        size = Size(rectW, rectH)
                    )
                    drawScope.drawRect(
                        color = color,
                        topLeft = Offset(rectX, rectY),
                        size = Size(rectW, rectH),
                        style = Stroke(width = 2f)
                    )
                    val stripeStep = 12f
                    var sx = rectX
                    while (sx < rectX + rectW + rectH) {
                        drawScope.drawLine(
                            color = color.copy(alpha = 0.65f),
                            start = Offset(sx.coerceIn(rectX, rectX + rectW), rectY),
                            end = Offset(rectX, (rectY + (sx - rectX)).coerceIn(rectY, rectY + rectH)),
                            strokeWidth = 1.5f
                        )
                        sx += stripeStep
                    }
                }
                else -> {
                    // Concentric nested GDI rectangles
                    val layers = 3
                    for (l in 0 until layers) {
                        val inset = l * 8f
                        if (rectW > inset * 2 && rectH > inset * 2) {
                            drawScope.drawRect(
                                color = color.copy(alpha = 1f - (l * 0.25f)),
                                topLeft = Offset(rectX + inset, rectY + inset),
                                size = Size(rectW - inset * 2, rectH - inset * 2),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
            }

            // Stamp PX and Size coordinates on select prominent rectangles
            if (i % 3 == 0 && rectW > 80f && rectH > 35f) {
                val labelText = "${rectW.toInt()}x${rectH.toInt()} PX"
                val coordText = "@(${rectX.toInt()}, ${rectY.toInt()})"

                drawScope.drawRect(
                    color = Color(0xDD000000),
                    topLeft = Offset(rectX + 4f, rectY + 4f),
                    size = Size(140f, 32f)
                )

                subTextPaint.textSize = 15f
                subTextPaint.color = AndroidColor.GREEN
                canvas.drawText(labelText, rectX + 8f, rectY + 18f, subTextPaint)
                subTextPaint.textSize = 12f
                subTextPaint.color = AndroidColor.YELLOW
                canvas.drawText(coordText, rectX + 8f, rectY + 31f, subTextPaint)
            }
        }

        // 4. GDI Telemetry Header
        textPaint.textSize = 28f
        textPaint.color = AndroidColor.WHITE
        canvas.drawText("TERA_BONUS.EXE [GDI PAYLOAD 5: RECTANGLES]", 24f, 55f, textPaint)

        subTextPaint.textSize = 20f
        subTextPaint.color = AndroidColor.CYAN
        canvas.drawText("API: Rectangle() | PatBlt(PATINVERT) | SIZE & PX", 24f, 85f, subTextPaint)

        subTextPaint.textSize = 18f
        subTextPaint.color = AndroidColor.YELLOW
        canvas.drawText("BYTEBEAT: t%((t&-16|t>>11)&42)<<2|t>>4", 24f, 112f, subTextPaint)

        // Bottom stats bar
        val activeX = ((time * 0.2f) % width).toInt()
        val activeY = ((time * 0.15f) % height).toInt()
        drawScope.drawRect(
            color = Color(0xCC001122),
            topLeft = Offset(16f, height - 52f),
            size = Size(width - 32f, 36f)
        )
        drawScope.drawRect(
            color = Color(0xFF00AAFF),
            topLeft = Offset(16f, height - 52f),
            size = Size(width - 32f, 36f),
            style = Stroke(width = 1.5f)
        )
        subTextPaint.textSize = 16f
        subTextPaint.color = AndroidColor.WHITE
        canvas.drawText("GDI POOL: $rectCount RECTS | CURSOR: ${activeX}x${activeY} PX | ROP: SRCINVERT", 28f, height - 28f, subTextPaint)
    }

    /**
     * Payload 6: Bouncing Triangles to Move PX (Like Mocq epic.exe and Coore32.exe).
     * Simulates the iconic GDI polygon/triangle kinematics:
     * - Multi-colored bouncing triangles colliding with viewport boundaries.
     * - Real-time MoveToEx / LineTo / Polygon GDI kinematics with spin rotation.
     * - Move PX telemetry HUD displaying current delta velocities (e.g. "ΔPX (+8, -6)").
     * - Persistent polygonal ghost trails fading across the canvas.
     * - Connective vertex triangulation chords connecting active triangle centroids.
     */
    private fun renderPayload6BouncingTriangles(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        time: Long,
        audioWaveform: FloatArray
    ) {
        val canvas = drawScope.drawContext.canvas.nativeCanvas

        // 1. Deep retro GDI canvas background
        drawScope.drawRect(
            color = Color(0xFF04060E),
            size = Size(width, height)
        )

        // Pixel grid lines (Mocq epic / Coore32 coordinate raster)
        val gridStep = 50f
        var gx = 0f
        while (gx < width) {
            drawScope.drawLine(
                color = Color(0x1800E5FF),
                start = Offset(gx, 0f),
                end = Offset(gx, height),
                strokeWidth = 1f
            )
            gx += gridStep
        }
        var gy = 0f
        while (gy < height) {
            drawScope.drawLine(
                color = Color(0x1800E5FF),
                start = Offset(0f, gy),
                end = Offset(width, gy),
                strokeWidth = 1f
            )
            gy += gridStep
        }

        // 2. Audio reactivity factor
        var audioEnergy = 0.2f
        if (audioWaveform.isNotEmpty()) {
            var sum = 0f
            for (i in 0 until minOf(24, audioWaveform.size)) {
                sum += kotlin.math.abs(audioWaveform[i])
            }
            audioEnergy = (sum / 24f).coerceIn(0.1f, 1.0f)
        }
        val audioScale = 1.0f + (audioEnergy * 0.4f)

        // 3. Update physics for bouncing triangles
        for (tri in bounceTriangles) {
            tri.x += tri.vx
            tri.y += tri.vy
            tri.rotation += tri.rotationSpeed

            val boundRadius = tri.size * 0.8f

            // Bounce X boundary
            if (tri.x - boundRadius <= 0) {
                tri.x = boundRadius
                tri.vx = -tri.vx
                tri.rotationSpeed = -tri.rotationSpeed
            } else if (tri.x + boundRadius >= width) {
                tri.x = width - boundRadius
                tri.vx = -tri.vx
                tri.rotationSpeed = -tri.rotationSpeed
            }

            // Bounce Y boundary
            if (tri.y - boundRadius <= 0) {
                tri.y = boundRadius
                tri.vy = -tri.vy
            } else if (tri.y + boundRadius >= height) {
                tri.y = height - boundRadius
                tri.vy = -tri.vy
            }

            // Calculate 3 vertices of the triangle
            val rad = Math.toRadians(tri.rotation.toDouble())
            val effectiveSize = tri.size * audioScale

            val p1 = Offset(
                (tri.x + effectiveSize * kotlin.math.cos(rad)).toFloat(),
                (tri.y + effectiveSize * kotlin.math.sin(rad)).toFloat()
            )
            val p2 = Offset(
                (tri.x + effectiveSize * kotlin.math.cos(rad + 2.094395)).toFloat(), // + 120 deg
                (tri.y + effectiveSize * kotlin.math.sin(rad + 2.094395)).toFloat()
            )
            val p3 = Offset(
                (tri.x + effectiveSize * kotlin.math.cos(rad + 4.18879)).toFloat(), // + 240 deg
                (tri.y + effectiveSize * kotlin.math.sin(rad + 4.18879)).toFloat()
            )

            // Spawn trails
            if (time % 2L == 0L) {
                triangleTrails.add(
                    TriangleTrail(
                        p1 = p1,
                        p2 = p2,
                        p3 = p3,
                        color = tri.color,
                        isFilled = tri.isFilled,
                        life = 1.0f
                    )
                )
                if (triangleTrails.size > maxTriangleTrails) {
                    triangleTrails.removeAt(0)
                }
            }
        }

        // 4. Render persistent triangle trails
        val it = triangleTrails.iterator()
        while (it.hasNext()) {
            val trail = it.next()
            trail.life -= 0.012f
            if (trail.life <= 0f) {
                it.remove()
                continue
            }

            val path = Path().apply {
                moveTo(trail.p1.x, trail.p1.y)
                lineTo(trail.p2.x, trail.p2.y)
                lineTo(trail.p3.x, trail.p3.y)
                close()
            }

            val trailAlpha = (trail.life * 0.45f).coerceIn(0f, 1f)
            if (trail.isFilled) {
                drawScope.drawPath(
                    path = path,
                    color = trail.color.copy(alpha = trailAlpha),
                    style = Fill
                )
            } else {
                drawScope.drawPath(
                    path = path,
                    color = trail.color.copy(alpha = trailAlpha),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // 5. Triangulation chords between triangle centroids (Coore32.exe style)
        for (i in 0 until bounceTriangles.size) {
            for (j in i + 1 until bounceTriangles.size) {
                val t1 = bounceTriangles[i]
                val t2 = bounceTriangles[j]
                val dx = t1.x - t2.x
                val dy = t1.y - t2.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 260f) {
                    val chordAlpha = (1f - dist / 260f) * 0.6f
                    drawScope.drawLine(
                        color = t1.color.copy(alpha = chordAlpha),
                        start = Offset(t1.x, t1.y),
                        end = Offset(t2.x, t2.y),
                        strokeWidth = 1.5f
                    )
                }
            }
        }

        // 6. Draw active bouncing triangles
        for ((idx, tri) in bounceTriangles.withIndex()) {
            val rad = Math.toRadians(tri.rotation.toDouble())
            val effectiveSize = tri.size * audioScale

            val p1 = Offset(
                (tri.x + effectiveSize * kotlin.math.cos(rad)).toFloat(),
                (tri.y + effectiveSize * kotlin.math.sin(rad)).toFloat()
            )
            val p2 = Offset(
                (tri.x + effectiveSize * kotlin.math.cos(rad + 2.094395)).toFloat(),
                (tri.y + effectiveSize * kotlin.math.sin(rad + 2.094395)).toFloat()
            )
            val p3 = Offset(
                (tri.x + effectiveSize * kotlin.math.cos(rad + 4.18879)).toFloat(),
                (tri.y + effectiveSize * kotlin.math.sin(rad + 4.18879)).toFloat()
            )

            val path = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }

            when (tri.mode) {
                0 -> {
                    // Solid fill with white GDI pen outline
                    drawScope.drawPath(path = path, color = tri.color.copy(alpha = 0.85f), style = Fill)
                    drawScope.drawPath(path = path, color = Color.White, style = Stroke(width = 2.5f))
                }
                1 -> {
                    // Hollow GDI wireframe pen
                    drawScope.drawPath(path = path, color = tri.color, style = Stroke(width = 3.5f))
                }
                2 -> {
                    // Difference raster inverted (PATINVERT simulation)
                    drawScope.drawPath(path = path, color = Color.White, blendMode = BlendMode.Difference)
                }
                else -> {
                    // Concentric nested triangles
                    drawScope.drawPath(path = path, color = tri.color, style = Stroke(width = 2f))
                    val innerPath = Path().apply {
                        val innerSz = effectiveSize * 0.55f
                        moveTo(
                            (tri.x + innerSz * kotlin.math.cos(rad)).toFloat(),
                            (tri.y + innerSz * kotlin.math.sin(rad)).toFloat()
                        )
                        lineTo(
                            (tri.x + innerSz * kotlin.math.cos(rad + 2.094395)).toFloat(),
                            (tri.y + innerSz * kotlin.math.sin(rad + 2.094395)).toFloat()
                        )
                        lineTo(
                            (tri.x + innerSz * kotlin.math.cos(rad + 4.18879)).toFloat(),
                            (tri.y + innerSz * kotlin.math.sin(rad + 4.18879)).toFloat()
                        )
                        close()
                    }
                    drawScope.drawPath(path = innerPath, color = Color.White, style = Fill)
                }
            }

            // Move PX telemetry label on select triangles
            if (idx % 2 == 0) {
                val vxSign = if (tri.vx >= 0) "+${tri.vx.toInt()}" else "${tri.vx.toInt()}"
                val vySign = if (tri.vy >= 0) "+${tri.vy.toInt()}" else "${tri.vy.toInt()}"
                val pxTag = "MOVE PX: [$vxSign, $vySign] @${tri.x.toInt()},${tri.y.toInt()}"

                drawScope.drawRect(
                    color = Color(0xEE001122),
                    topLeft = Offset(tri.x - 70f, tri.y + tri.size * 0.7f),
                    size = Size(170f, 22f)
                )
                drawScope.drawRect(
                    color = tri.color,
                    topLeft = Offset(tri.x - 70f, tri.y + tri.size * 0.7f),
                    size = Size(170f, 22f),
                    style = Stroke(width = 1f)
                )

                subTextPaint.textSize = 12f
                subTextPaint.color = AndroidColor.GREEN
                canvas.drawText(pxTag, tri.x - 66f, tri.y + tri.size * 0.7f + 15f, subTextPaint)
            }
        }

        // 7. Header HUD
        textPaint.textSize = 26f
        textPaint.color = AndroidColor.WHITE
        canvas.drawText("TERA_BONUS.EXE [GDI PAYLOAD 6: BOUNCING TRIANGLES]", 24f, 55f, textPaint)

        subTextPaint.textSize = 19f
        subTextPaint.color = AndroidColor.CYAN
        canvas.drawText("STYLE: Mocq epic.exe & Coore32.exe | MoveToEx / LineTo / Polygon()", 24f, 82f, subTextPaint)

        subTextPaint.textSize = 17f
        subTextPaint.color = AndroidColor.YELLOW
        canvas.drawText("KINEMATICS: MOVE PX DELTA | TRAILS: ${triangleTrails.size} | AUDIO: t*(t>>8)>>(t>>6)", 24f, 108f, subTextPaint)

        // Bottom stats bar
        drawScope.drawRect(
            color = Color(0xDD0A1526),
            topLeft = Offset(16f, height - 50f),
            size = Size(width - 32f, 34f)
        )
        drawScope.drawRect(
            color = Color(0xFF00FFCC),
            topLeft = Offset(16f, height - 50f),
            size = Size(width - 32f, 34f),
            style = Stroke(width = 1.5f)
        )
        subTextPaint.textSize = 15f
        subTextPaint.color = AndroidColor.WHITE
        canvas.drawText("TRIANGLES: ${bounceTriangles.size} POLYGONS | RASTEROPS: PATINVERT / SRCCOPY | ENGINE: 8000Hz", 26f, height - 28f, subTextPaint)
    }

    /**
     * Payload 7: Lines Curvy Effect to Random Lines.
     * Simulates the demoscene GDI random curvy lines raster effects:
     * - Multi-segmented oscillating Sine/Cosine wavy lines and cubic Bézier splines.
     * - Dynamic GDI Pen creation with randomized bright palette colors.
     * - Harmonic ribbon wave mesh that warps and undulates across the viewport.
     * - Audio-reactive frequency modulation and chromatic RGB split line echoes.
     */
    private fun renderPayload7CurvyLines(
        drawScope: DrawScope,
        width: Float,
        height: Float,
        time: Long,
        audioWaveform: FloatArray
    ) {
        val canvas = drawScope.drawContext.canvas.nativeCanvas

        // 1. Dark canvas background
        drawScope.drawRect(
            color = Color(0xFF05030A),
            size = Size(width, height)
        )

        // 2. Audio energy factor
        var audioEnergy = 0.25f
        if (audioWaveform.isNotEmpty()) {
            var sum = 0f
            for (i in 0 until minOf(32, audioWaveform.size)) {
                sum += kotlin.math.abs(audioWaveform[i])
            }
            audioEnergy = (sum / 32f).coerceIn(0.1f, 1.0f)
        }
        val audioWaveBoost = 1.0f + (audioEnergy * 1.2f)

        val palette = listOf(
            Color(0xFFFF0055),
            Color(0xFF00FFCC),
            Color(0xFFFFFF00),
            Color(0xFF00E5FF),
            Color(0xFFFF00FF),
            Color(0xFF76FF03),
            Color(0xFFFF9100),
            Color(0xFFFFFFFF),
            Color(0xFF2979FF),
            Color(0xFFFF1744)
        )

        // 3. Undulating Curvy Sine/Cosine Ribbons across vertical screen
        val ribbonCount = 14
        val timeSec = time * 0.002
        for (r in 0 until ribbonCount) {
            val color = palette[r % palette.size]
            val path = Path()
            val yBase = (height / (ribbonCount + 1)) * (r + 1)
            val freq = 0.008 + (r * 0.002)
            val phase = timeSec * (1.2 + (r * 0.15)) + (r * 0.7)
            val amp = (35f + (r * 6f)) * audioWaveBoost

            var first = true
            var x = 0f
            val xStep = 10f
            while (x <= width) {
                val waveY = yBase + (kotlin.math.sin(x * freq + phase) * amp).toFloat() +
                        (kotlin.math.cos(x * freq * 0.5 + phase * 1.5) * amp * 0.35f).toFloat()

                if (first) {
                    path.moveTo(x, waveY)
                    first = false
                } else {
                    path.lineTo(x, waveY)
                }
                x += xStep
            }

            // Draw main curvy line
            val strokeW = if (r % 3 == 0) 3.5f else 2.0f
            drawScope.drawPath(
                path = path,
                color = color.copy(alpha = 0.85f),
                style = Stroke(width = strokeW)
            )

            // Chromatic RGB line ghost echo
            val echoPath = Path()
            var ex = 0f
            var eFirst = true
            while (ex <= width) {
                val echoY = yBase + (kotlin.math.sin(ex * freq + phase) * amp).toFloat() + 6f
                if (eFirst) {
                    echoPath.moveTo(ex, echoY)
                    eFirst = false
                } else {
                    echoPath.lineTo(ex, echoY)
                }
                ex += xStep
            }
            drawScope.drawPath(
                path = echoPath,
                color = Color.White.copy(alpha = 0.35f),
                style = Stroke(width = 1f)
            )
        }

        // 4. Random Curvy Bézier Splines (pseudo-random bursts seeded by clock)
        val seed = (time / 140L).toInt()
        val rng = Random(seed * 777)
        val splineCount = 12

        for (s in 0 until splineCount) {
            val startX = rng.nextFloat() * width
            val startY = rng.nextFloat() * height
            val endX = rng.nextFloat() * width
            val endY = rng.nextFloat() * height

            val ctrl1X = rng.nextFloat() * width
            val ctrl1Y = rng.nextFloat() * height
            val ctrl2X = rng.nextFloat() * width
            val ctrl2Y = rng.nextFloat() * height

            val splineColor = palette[rng.nextInt(palette.size)]
            val splinePath = Path().apply {
                moveTo(startX, startY)
                cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, endX, endY)
            }

            val strokeWidth = rng.nextFloat() * 3.5f + 1.5f
            val isDotted = rng.nextBoolean()

            drawScope.drawPath(
                path = splinePath,
                color = splineColor.copy(alpha = 0.75f),
                style = Stroke(width = strokeWidth)
            )

            // Control point handles for authentic GDI spline curve debug aesthetic
            drawScope.drawCircle(
                color = Color.White,
                radius = 3.5f,
                center = Offset(ctrl1X, ctrl1Y),
                style = Fill
            )
            drawScope.drawCircle(
                color = splineColor,
                radius = 3.5f,
                center = Offset(ctrl2X, ctrl2Y),
                style = Fill
            )
            drawScope.drawLine(
                color = Color(0x33FFFFFF),
                start = Offset(startX, startY),
                end = Offset(ctrl1X, ctrl1Y),
                strokeWidth = 1f
            )
            drawScope.drawLine(
                color = Color(0x33FFFFFF),
                start = Offset(endX, endY),
                end = Offset(ctrl2X, ctrl2Y),
                strokeWidth = 1f
            )
        }

        // 5. Vertical Sweeping Curvy Interference Lines
        val vLineCount = 8
        for (v in 0 until vLineCount) {
            val xBase = (width / (vLineCount + 1)) * (v + 1)
            val vFreq = 0.01 + (v * 0.003)
            val vPhase = timeSec * 1.8 + (v * 0.8)
            val vAmp = 25f * audioWaveBoost

            val vPath = Path()
            var vFirst = true
            var y = 0f
            while (y <= height) {
                val curX = xBase + (kotlin.math.sin(y * vFreq + vPhase) * vAmp).toFloat()
                if (vFirst) {
                    vPath.moveTo(curX, y)
                    vFirst = false
                } else {
                    vPath.lineTo(curX, y)
                }
                y += 12f
            }

            drawScope.drawPath(
                path = vPath,
                color = palette[(v + 3) % palette.size].copy(alpha = 0.5f),
                style = Stroke(width = 1.8f)
            )
        }

        // 6. HUD Overlay
        textPaint.textSize = 26f
        textPaint.color = AndroidColor.WHITE
        canvas.drawText("TERA_BONUS.EXE [GDI PAYLOAD 7: CURVY LINES]", 24f, 55f, textPaint)

        subTextPaint.textSize = 19f
        subTextPaint.color = AndroidColor.CYAN
        canvas.drawText("EFFECT: Lines Curvy Effect to Random Lines | PolyBezier()", 24f, 82f, subTextPaint)

        subTextPaint.textSize = 17f
        subTextPaint.color = AndroidColor.YELLOW
        canvas.drawText("RASTER: SINE/COSINE SPLINE MESH | BÉZIER CONTROLS | AUDIO: CHIRP DRONE", 24f, 108f, subTextPaint)

        // Bottom stats bar
        drawScope.drawRect(
            color = Color(0xDD0D0818),
            topLeft = Offset(16f, height - 50f),
            size = Size(width - 32f, 34f)
        )
        drawScope.drawRect(
            color = Color(0xFFFF00CC),
            topLeft = Offset(16f, height - 50f),
            size = Size(width - 32f, 34f),
            style = Stroke(width = 1.5f)
        )
        subTextPaint.textSize = 15f
        subTextPaint.color = AndroidColor.WHITE
        canvas.drawText("CURVY RIBS: $ribbonCount | BÉZIER BURSTS: $splineCount | GDI PEN: RGB SPECTRUM", 26f, height - 28f, subTextPaint)
    }

    /**
     * Overlay subtle CRT / Pocket PC LCD scanline grid
     */
    private fun renderScanlines(drawScope: DrawScope, width: Float, height: Float) {
        val scanlineSpacing = 4f
        var y = 0f
        val scanlineColor = Color(0x33000000)
        while (y < height) {
            drawScope.drawLine(
                color = scanlineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.2f
            )
            y += scanlineSpacing
        }
    }
}
