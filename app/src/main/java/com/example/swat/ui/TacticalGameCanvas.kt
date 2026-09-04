package com.example.swat.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.R
import com.example.swat.model.BulletTracer
import com.example.swat.model.Door
import com.example.swat.model.DoorOrientation
import com.example.swat.model.Enemy
import com.example.swat.model.ImpactEffect
import com.example.swat.model.Operator
import com.example.swat.model.Vector2D
import com.example.swat.model.Wall
import com.example.swat.viewmodel.TacticalGameState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TacticalGameCanvas(
    state: TacticalGameState,
    onMapTap: (worldX: Float, worldY: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val swatBitmap = ImageBitmap.imageResource(id = R.drawable.img_swat_operator)
    val enemyBitmap = ImageBitmap.imageResource(id = R.drawable.img_enemy_suspect)

    // Map world dimensions are 960 x 540
    val worldWidth = 960f
    val worldHeight = 540f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("tactical_game_canvas")
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    val scaleX = size.width / worldWidth
                    val scaleY = size.height / worldHeight
                    val scale = minOf(scaleX, scaleY)
                    val offsetX = (size.width - worldWidth * scale) / 2f
                    val offsetY = (size.height - worldHeight * scale) / 2f

                    val worldX = (tapOffset.x - offsetX) / scale
                    val worldY = (tapOffset.y - offsetY) / scale

                    if (worldX in 0f..worldWidth && worldY in 0f..worldHeight) {
                        onMapTap(worldX, worldY)
                    }
                }
            }
    ) {
        val scaleX = size.width / worldWidth
        val scaleY = size.height / worldHeight
        val scale = minOf(scaleX, scaleY)
        val offsetX = (size.width - worldWidth * scale) / 2f
        val offsetY = (size.height - worldHeight * scale) / 2f

        fun toCanvas(v: Vector2D): Offset = Offset(offsetX + v.x * scale, offsetY + v.y * scale)
        fun toCanvas(x: Float, y: Float): Offset = Offset(offsetX + x * scale, offsetY + y * scale)
        fun toCanvasLen(len: Float): Float = len * scale

        // 1. Draw Tactical Floor / Background
        drawRect(
            color = Color(0xFF0B1120),
            topLeft = Offset(offsetX, offsetY),
            size = Size(worldWidth * scale, worldHeight * scale)
        )

        // Draw tactical blueprint grid
        val gridSize = 40f
        var gx = 0f
        while (gx <= worldWidth) {
            drawLine(
                color = Color(0x1538BDF8),
                start = toCanvas(gx, 0f),
                end = toCanvas(gx, worldHeight),
                strokeWidth = 1f
            )
            gx += gridSize
        }
        var gy = 0f
        while (gy <= worldHeight) {
            drawLine(
                color = Color(0x1538BDF8),
                start = toCanvas(0f, gy),
                end = toCanvas(worldWidth, gy),
                strokeWidth = 1f
            )
            gy += gridSize
        }

        // Room Labels on floor
        drawRoomFloorDecorations(offsetX, offsetY, scale)

        // 2. Draw Vision Cones (Field of View) for living operators
        for (op in state.operators) {
            if (op.isAlive) {
                val poly = state.visionPolygons[op.id]
                if (poly != null && poly.size >= 3) {
                    val path = Path()
                    val first = toCanvas(poly[0])
                    path.moveTo(first.x, first.y)
                    for (i in 1 until poly.size) {
                        val pt = toCanvas(poly[i])
                        path.lineTo(pt.x, pt.y)
                    }
                    path.close()

                    // Glow beam
                    val opCanvasPos = toCanvas(op.position)
                    drawPath(
                        path = path,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x3500D2FF),
                                Color(0x200284C7),
                                Color(0x0600D2FF),
                                Color(0x00000000)
                            ),
                            center = opCanvasPos,
                            radius = toCanvasLen(op.fovRange)
                        )
                    )

                    // Outline of FOV beam
                    drawPath(
                        path = path,
                        color = Color(0x2238BDF8),
                        style = Stroke(width = 1.5f)
                    )
                }

                // 360-degree close awareness circle
                drawCircle(
                    color = Color(0x1800D2FF),
                    radius = toCanvasLen(op.peripheralRange),
                    center = toCanvas(op.position)
                )
            }
        }

        // 3. Draw Doors
        for (door in state.doors) {
            drawTacticalDoor(door, { toCanvasLen(it) }, { toCanvas(it) })
        }

        // 4. Draw Walls
        for (wall in state.walls) {
            val topL = toCanvas(wall.left, wall.top)
            val wWidth = toCanvasLen(wall.right - wall.left)
            val wHeight = toCanvasLen(wall.bottom - wall.top)

            // Wall Shadow / Depth
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(topL.x + 3f, topL.y + 3f),
                size = Size(wWidth, wHeight)
            )

            // Wall Body
            drawRect(
                color = Color(0xFF1E293B),
                topLeft = topL,
                size = Size(wWidth, wHeight)
            )

            // Tactical wall border
            drawRect(
                color = Color(0xFF475569),
                topLeft = topL,
                size = Size(wWidth, wHeight),
                style = Stroke(width = 1.5f)
            )
        }

        // 5. Draw Enemies
        for (enemy in state.enemies) {
            val isVisible = state.visibleEnemyIds.contains(enemy.id)
            if (isVisible || !enemy.isAlive) {
                drawEnemy(
                    enemy = enemy,
                    enemyBitmap = enemyBitmap,
                    isVisible = isVisible,
                    toCanvas = { toCanvas(it) },
                    toCanvasLen = { toCanvasLen(it) }
                )
            }
        }

        // 6. Draw SWAT Operators
        for (op in state.operators) {
            drawSwatOperator(
                operator = op,
                isSelected = op.id == state.selectedOperatorId,
                swatBitmap = swatBitmap,
                toCanvas = { toCanvas(it) },
                toCanvasLen = { toCanvasLen(it) }
            )
        }

        // 7. Draw Bullet Tracers
        for (tracer in state.tracers) {
            val start = toCanvas(tracer.start)
            val end = toCanvas(tracer.end)
            val tracerColor = if (tracer.isSwat) Color(0xFF38BDF8) else Color(0xFFEF4444)
            val coreColor = Color.White

            drawLine(
                color = tracerColor,
                start = start,
                end = end,
                strokeWidth = 3f
            )
            drawLine(
                color = coreColor,
                start = start,
                end = end,
                strokeWidth = 1.2f
            )
        }

        // 8. Draw Impacts
        for (impact in state.impacts) {
            val pos = toCanvas(impact.position)
            val color = if (impact.isBlood) Color(0xCCDC2626) else Color(0xCCFBBF24)
            drawCircle(
                color = color,
                radius = 6f * scale,
                center = pos
            )
        }

        // 9. Selected Operator Destination Marker
        val selectedOp = state.selectedOperator
        if (selectedOp != null && selectedOp.targetPosition != null && selectedOp.isAlive) {
            val dest = toCanvas(selectedOp.targetPosition!!)
            val opPos = toCanvas(selectedOp.position)

            // Dotted waypoint line
            drawLine(
                color = Color(0x6600E5FF),
                start = opPos,
                end = dest,
                strokeWidth = 2f
            )

            // Target ring
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 8f * scale,
                center = dest,
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 2.5f * scale,
                center = dest
            )
        }
    }
}

private fun DrawScope.drawRoomFloorDecorations(offsetX: Float, offsetY: Float, scale: Float) {
    // Subtle floor markings for tactical zones
    val borderCol = Color(0x1A64748B)

    // Entry Zone (Infiltration)
    drawRect(
        color = Color(0x0A0284C7),
        topLeft = Offset(offsetX + 22f * scale, offsetY + 22f * scale),
        size = Size(168f * scale, 496f * scale)
    )

    // Server Room (North)
    drawRect(
        color = Color(0x0810B981),
        topLeft = Offset(offsetX + 202f * scale, offsetY + 22f * scale),
        size = Size(323f * scale, 153f * scale)
    )

    // Armory (South)
    drawRect(
        color = Color(0x08F59E0B),
        topLeft = Offset(offsetX + 202f * scale, offsetY + 347f * scale),
        size = Size(323f * scale, 171f * scale)
    )

    // Command Suite (East)
    drawRect(
        color = Color(0x08EF4444),
        topLeft = Offset(offsetX + 537f * scale, offsetY + 22f * scale),
        size = Size(401f * scale, 496f * scale)
    )
}

private fun DrawScope.drawTacticalDoor(
    door: Door,
    toCanvasLen: (Float) -> Float,
    toCanvas: (Vector2D) -> Offset
) {
    val center = toCanvas(door.center)
    val len = toCanvasLen(door.length)
    val isVert = door.orientation == DoorOrientation.VERTICAL

    if (door.isOpen) {
        // Draw open swing outline
        val swingColor = Color(0xFF10B981)
        if (isVert) {
            drawLine(
                color = swingColor,
                start = Offset(center.x, center.y - len / 2f),
                end = Offset(center.x + len * 0.8f, center.y - len / 2f),
                strokeWidth = 4f
            )
        } else {
            drawLine(
                color = swingColor,
                start = Offset(center.x - len / 2f, center.y),
                end = Offset(center.x - len / 2f, center.y - len * 0.8f),
                strokeWidth = 4f
            )
        }

        // Green clear indicator light
        drawCircle(
            color = Color(0xFF10B981),
            radius = 4f,
            center = center
        )
    } else {
        // Draw closed barricade door
        val doorColor = Color(0xFFD97706)
        val strokeW = 7f

        if (isVert) {
            drawLine(
                color = doorColor,
                start = Offset(center.x, center.y - len / 2f),
                end = Offset(center.x, center.y + len / 2f),
                strokeWidth = strokeW
            )
        } else {
            drawLine(
                color = doorColor,
                start = Offset(center.x - len / 2f, center.y),
                end = Offset(center.x + len / 2f, center.y),
                strokeWidth = strokeW
            )
        }

        // Door handle & status dot
        drawCircle(
            color = Color(0xFFEF4444),
            radius = 4f,
            center = center
        )
    }
}

private fun DrawScope.drawSwatOperator(
    operator: Operator,
    isSelected: Boolean,
    swatBitmap: ImageBitmap,
    toCanvas: (Vector2D) -> Offset,
    toCanvasLen: (Float) -> Float
) {
    val pos = toCanvas(operator.position)
    val radius = toCanvasLen(18f)

    if (!operator.isAlive) {
        // Operator Down icon
        drawCircle(
            color = Color(0x66475569),
            radius = radius,
            center = pos
        )
        drawCircle(
            color = Color(0xFFEF4444),
            radius = radius,
            center = pos,
            style = Stroke(width = 2f)
        )
        drawLine(
            color = Color(0xFFEF4444),
            start = Offset(pos.x - 8f, pos.y - 8f),
            end = Offset(pos.x + 8f, pos.y + 8f),
            strokeWidth = 2.5f
        )
        drawLine(
            color = Color(0xFFEF4444),
            start = Offset(pos.x + 8f, pos.y - 8f),
            end = Offset(pos.x - 8f, pos.y + 8f),
            strokeWidth = 2.5f
        )
        return
    }

    // Selected Glowing Ring
    if (isSelected) {
        drawCircle(
            color = Color(0x3300D2FF),
            radius = radius * 1.55f,
            center = pos
        )
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = radius * 1.4f,
            center = pos,
            style = Stroke(width = 2.5f)
        )

        // Crosshair ticks
        val tickLen = 6f
        val rOuter = radius * 1.4f
        drawLine(Color(0xFF00E5FF), Offset(pos.x - rOuter - tickLen, pos.y), Offset(pos.x - rOuter, pos.y), 2f)
        drawLine(Color(0xFF00E5FF), Offset(pos.x + rOuter, pos.y), Offset(pos.x + rOuter + tickLen, pos.y), 2f)
        drawLine(Color(0xFF00E5FF), Offset(pos.x, pos.y - rOuter - tickLen), Offset(pos.x, pos.y - rOuter), 2f)
        drawLine(Color(0xFF00E5FF), Offset(pos.x, pos.y + rOuter), Offset(pos.x, pos.y + rOuter + tickLen), 2f)
    }

    // Facing Direction / Flashlight pointer
    val rad = Math.toRadians(operator.angleDegrees.toDouble())
    val pointerEnd = Offset(
        pos.x + (cos(rad) * radius * 1.4f).toFloat(),
        pos.y + (sin(rad) * radius * 1.4f).toFloat()
    )
    drawLine(
        color = Color(0x8800E5FF),
        start = pos,
        end = pointerEnd,
        strokeWidth = 2f
    )

    // Draw Operator Sprite Rotated
    rotate(degrees = operator.angleDegrees, pivot = pos) {
        drawImage(
            image = swatBitmap,
            dstOffset = IntOffset((pos.x - radius).toInt(), (pos.y - radius).toInt()),
            dstSize = IntSize((radius * 2).toInt(), (radius * 2).toInt())
        )
    }

    // Muzzle Flash
    if (operator.isFiring) {
        drawCircle(
            color = Color(0xFFFFF176),
            radius = radius * 0.7f,
            center = pointerEnd
        )
        drawCircle(
            color = Color.White,
            radius = radius * 0.35f,
            center = pointerEnd
        )
    }

    // Health Bar above operator
    val barWidth = radius * 2.2f
    val barHeight = 4f
    val barY = pos.y - radius - 10f
    val hpPercent = (operator.health / operator.maxHealth).coerceIn(0f, 1f)

    // Background bar
    drawRect(
        color = Color(0x991E293B),
        topLeft = Offset(pos.x - barWidth / 2f, barY),
        size = Size(barWidth, barHeight)
    )
    // Health fill
    val hpColor = when {
        hpPercent > 0.5f -> Color(0xFF10B981)
        hpPercent > 0.25f -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    drawRect(
        color = hpColor,
        topLeft = Offset(pos.x - barWidth / 2f, barY),
        size = Size(barWidth * hpPercent, barHeight)
    )

    // Reloading text/bar indicator
    if (operator.isReloading) {
        drawRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(pos.x - barWidth / 2f, barY - 4f),
            size = Size(barWidth * 0.8f, 2f)
        )
    }
}

private fun DrawScope.drawEnemy(
    enemy: Enemy,
    enemyBitmap: ImageBitmap,
    isVisible: Boolean,
    toCanvas: (Vector2D) -> Offset,
    toCanvasLen: (Float) -> Float
) {
    val pos = toCanvas(enemy.position)
    val radius = toCanvasLen(17f)

    if (!enemy.isAlive) {
        // Corpse marker
        drawCircle(
            color = Color(0x44B91C1C),
            radius = radius * 0.9f,
            center = pos
        )
        drawLine(
            color = Color(0x88DC2626),
            start = Offset(pos.x - 7f, pos.y - 7f),
            end = Offset(pos.x + 7f, pos.y + 7f),
            strokeWidth = 2f
        )
        drawLine(
            color = Color(0x88DC2626),
            start = Offset(pos.x + 7f, pos.y - 7f),
            end = Offset(pos.x - 7f, pos.y + 7f),
            strokeWidth = 2f
        )
        return
    }

    // Hostile red threat ring
    drawCircle(
        color = Color(0x33EF4444),
        radius = radius * 1.3f,
        center = pos
    )
    drawCircle(
        color = Color(0x99EF4444),
        radius = radius * 1.15f,
        center = pos,
        style = Stroke(width = 1.5f)
    )

    // Rotated Enemy Sprite
    rotate(degrees = enemy.angleDegrees, pivot = pos) {
        drawImage(
            image = enemyBitmap,
            dstOffset = IntOffset((pos.x - radius).toInt(), (pos.y - radius).toInt()),
            dstSize = IntSize((radius * 2).toInt(), (radius * 2).toInt())
        )
    }

    // Muzzle Flash
    if (enemy.isFiring) {
        val rad = Math.toRadians(enemy.angleDegrees.toDouble())
        val barrelPos = Offset(
            pos.x + (cos(rad) * radius * 1.3f).toFloat(),
            pos.y + (sin(rad) * radius * 1.3f).toFloat()
        )
        drawCircle(
            color = Color(0xFFFF9800),
            radius = radius * 0.6f,
            center = barrelPos
        )
    }

    // Enemy Health Bar
    val barWidth = radius * 2f
    val barHeight = 4f
    val barY = pos.y - radius - 8f
    val hpPercent = (enemy.health / enemy.maxHealth).coerceIn(0f, 1f)

    drawRect(
        color = Color(0x991E293B),
        topLeft = Offset(pos.x - barWidth / 2f, barY),
        size = Size(barWidth, barHeight)
    )
    drawRect(
        color = Color(0xFFEF4444),
        topLeft = Offset(pos.x - barWidth / 2f, barY),
        size = Size(barWidth * hpPercent, barHeight)
    )
}
