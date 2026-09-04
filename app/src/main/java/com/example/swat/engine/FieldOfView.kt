package com.example.swat.engine

import com.example.swat.model.Door
import com.example.swat.model.Operator
import com.example.swat.model.Segment2D
import com.example.swat.model.Vector2D
import com.example.swat.model.Wall
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object FieldOfView {

    /**
     * Checks if a point is in line of sight from origin, given blocking walls and closed doors.
     */
    fun hasLineOfSight(
        origin: Vector2D,
        target: Vector2D,
        walls: List<Wall>,
        doors: List<Door>
    ): Boolean {
        val sightLine = Segment2D(origin, target)

        // Check walls
        for (wall in walls) {
            for (segment in wall.segments) {
                if (sightLine.intersects(segment)) {
                    val pt = sightLine.intersectionPoint(segment)
                    if (pt != null) {
                        // Check if intersection is strictly between origin and target (with epsilon)
                        val distOriginToPt = origin.distanceTo(pt)
                        val distOriginToTarget = origin.distanceTo(target)
                        if (distOriginToPt < distOriginToTarget - 1f && distOriginToPt > 1f) {
                            return false
                        }
                    }
                }
            }
        }

        // Check closed doors
        for (door in doors) {
            if (!door.isOpen) {
                val doorSeg = door.closedSegment
                if (sightLine.intersects(doorSeg)) {
                    val pt = sightLine.intersectionPoint(doorSeg)
                    if (pt != null) {
                        val distOriginToPt = origin.distanceTo(pt)
                        val distOriginToTarget = origin.distanceTo(target)
                        if (distOriginToPt < distOriginToTarget - 1f && distOriginToPt > 1f) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    /**
     * Checks whether an operator can see a given target position considering distance, angle, and line of sight.
     */
    fun canOperatorSeePoint(
        operator: Operator,
        target: Vector2D,
        walls: List<Wall>,
        doors: List<Door>
    ): Boolean {
        val distance = operator.position.distanceTo(target)

        // Close 360-degree peripheral check
        if (distance <= operator.peripheralRange) {
            return hasLineOfSight(operator.position, target, walls, doors)
        }

        // Range check
        if (distance > operator.fovRange) {
            return false
        }

        // Angle check
        val delta = target - operator.position
        val angleToTarget = Math.toDegrees(atan2(delta.y.toDouble(), delta.x.toDouble())).toFloat()

        var angleDiff = abs(angleToTarget - operator.angleDegrees) % 360f
        if (angleDiff > 180f) {
            angleDiff = 360f - angleDiff
        }

        val halfFov = operator.fovAngleDegrees / 2f
        if (angleDiff > halfFov) {
            return false
        }

        return hasLineOfSight(operator.position, target, walls, doors)
    }

    /**
     * Computes the vision polygon vertices for an operator to render vision cone on canvas.
     */
    fun computeVisionPolygon(
        operator: Operator,
        walls: List<Wall>,
        doors: List<Door>,
        rayCount: Int = 36
    ): List<Vector2D> {
        val origin = operator.position
        val halfFov = operator.fovAngleDegrees / 2f
        val startAngle = operator.angleDegrees - halfFov
        val endAngle = operator.angleDegrees + halfFov
        val step = (endAngle - startAngle) / rayCount

        val polygonPoints = mutableListOf<Vector2D>()
        polygonPoints.add(origin)

        // Cast rays in FOV cone
        for (i in 0..rayCount) {
            val angle = startAngle + i * step
            val rad = Math.toRadians(angle.toDouble())
            val dir = Vector2D(cos(rad).toFloat(), sin(rad).toFloat())
            val maxPoint = origin + (dir * operator.fovRange)

            val hitPoint = castRay(origin, maxPoint, walls, doors)
            polygonPoints.add(hitPoint)
        }

        return polygonPoints
    }

    private fun castRay(
        origin: Vector2D,
        target: Vector2D,
        walls: List<Wall>,
        doors: List<Door>
    ): Vector2D {
        val ray = Segment2D(origin, target)
        var closestHit: Vector2D = target
        var minDistance = origin.distanceTo(target)

        for (wall in walls) {
            for (segment in wall.segments) {
                val hit = ray.intersectionPoint(segment)
                if (hit != null) {
                    val dist = origin.distanceTo(hit)
                    if (dist < minDistance && dist > 0.5f) {
                        minDistance = dist
                        closestHit = hit
                    }
                }
            }
        }

        for (door in doors) {
            if (!door.isOpen) {
                val hit = ray.intersectionPoint(door.closedSegment)
                if (hit != null) {
                    val dist = origin.distanceTo(hit)
                    if (dist < minDistance && dist > 0.5f) {
                        minDistance = dist
                        closestHit = hit
                    }
                }
            }
        }

        return closestHit
    }
}
