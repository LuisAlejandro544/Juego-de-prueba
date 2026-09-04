package com.example.swat.model

import kotlin.math.max
import kotlin.math.min

data class Wall(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val colorHex: Long = 0xFF1E293B
) {
    val segments: List<Segment2D> by lazy {
        listOf(
            Segment2D(Vector2D(left, top), Vector2D(right, top)),
            Segment2D(Vector2D(right, top), Vector2D(right, bottom)),
            Segment2D(Vector2D(right, bottom), Vector2D(left, bottom)),
            Segment2D(Vector2D(left, bottom), Vector2D(left, top))
        )
    }

    fun containsPoint(p: Vector2D): Boolean {
        return p.x in left..right && p.y in top..bottom
    }

    fun collidesWithCircle(center: Vector2D, radius: Float): Boolean {
        val closestX = max(left, min(center.x, right))
        val closestY = max(top, min(center.y, bottom))
        val distanceX = center.x - closestX
        val distanceY = center.y - closestY
        return (distanceX * distanceX + distanceY * distanceY) < (radius * radius)
    }
}

enum class DoorOrientation {
    HORIZONTAL,
    VERTICAL
}

data class Door(
    val id: String,
    val name: String,
    val center: Vector2D,
    val length: Float = 60f,
    val orientation: DoorOrientation = DoorOrientation.HORIZONTAL,
    val isOpen: Boolean = false,
    val openProgress: Float = 0f
) {
    val closedSegment: Segment2D
        get() {
            return if (orientation == DoorOrientation.HORIZONTAL) {
                Segment2D(
                    Vector2D(center.x - length / 2f, center.y),
                    Vector2D(center.x + length / 2f, center.y)
                )
            } else {
                Segment2D(
                    Vector2D(center.x, center.y - length / 2f),
                    Vector2D(center.x, center.y + length / 2f)
                )
            }
        }

    fun collidesWithCircle(circleCenter: Vector2D, radius: Float): Boolean {
        if (isOpen) return false
        val seg = closedSegment
        return distancePointToSegment(circleCenter, seg.p1, seg.p2) < radius
    }

    val interactionRadius: Float = 70f

    fun canInteract(pos: Vector2D): Boolean {
        return pos.distanceTo(center) <= interactionRadius
    }
}

fun distancePointToSegment(p: Vector2D, a: Vector2D, b: Vector2D): Float {
    val l2 = (b - a).let { it.x * it.x + it.y * it.y }
    if (l2 == 0f) return p.distanceTo(a)
    val t = max(0f, min(1f, ((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2))
    val projection = a + (b - a) * t
    return p.distanceTo(projection)
}
