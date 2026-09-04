package com.example.swat.model

data class BulletTracer(
    val id: Long,
    val start: Vector2D,
    val end: Vector2D,
    val isSwat: Boolean,
    val createdAtMs: Long,
    val durationMs: Long = 180L
)

data class ImpactEffect(
    val id: Long,
    val position: Vector2D,
    val isBlood: Boolean,
    val createdAtMs: Long,
    val durationMs: Long = 400L
)

data class RadioMessage(
    val id: Long,
    val sender: String,
    val message: String,
    val createdAtMs: Long,
    val durationMs: Long = 3500L
)
