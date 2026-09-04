package com.example.swat.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2D(val x: Float, val y: Float) {
    operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
    operator fun minus(other: Vector2D) = Vector2D(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2D(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vector2D(x / scalar, y / scalar)

    fun length(): Float = sqrt(x * x + y * y)

    fun normalized(): Vector2D {
        val len = length()
        return if (len > 0.0001f) Vector2D(x / len, y / len) else Vector2D(0f, 0f)
    }

    fun distanceTo(other: Vector2D): Float = (this - other).length()

    fun angleTo(other: Vector2D): Float {
        val delta = other - this
        return Math.toDegrees(atan2(delta.y.toDouble(), delta.x.toDouble())).toFloat()
    }

    companion object {
        val ZERO = Vector2D(0f, 0f)

        fun fromAngle(angleDegrees: Float, length: Float = 1f): Vector2D {
            val rad = Math.toRadians(angleDegrees.toDouble())
            return Vector2D((cos(rad) * length).toFloat(), (sin(rad) * length).toFloat())
        }
    }
}

data class Segment2D(val p1: Vector2D, val p2: Vector2D) {
    fun intersects(other: Segment2D): Boolean {
        return lineIntersection(p1, p2, other.p1, other.p2) != null
    }

    fun intersectionPoint(other: Segment2D): Vector2D? {
        return lineIntersection(p1, p2, other.p1, other.p2)
    }
}

/**
 * Returns point of intersection between segment AB and segment CD, or null if no intersection.
 */
fun lineIntersection(a: Vector2D, b: Vector2D, c: Vector2D, d: Vector2D): Vector2D? {
    val r = b - a
    val s = d - c
    val rxs = r.x * s.y - r.y * s.x
    val qpxr = (c.x - a.x) * r.y - (c.y - a.y) * r.x

    if (kotlin.math.abs(rxs) < 1e-6f) {
        return null // Parallel
    }

    val t = ((c.x - a.x) * s.y - (c.y - a.y) * s.x) / rxs
    val u = qpxr / rxs

    if (t in 0.0f..1.0f && u in 0.0f..1.0f) {
        return Vector2D(a.x + t * r.x, a.y + t * r.y)
    }
    return null
}
