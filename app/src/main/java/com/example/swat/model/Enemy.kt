package com.example.swat.model

data class Enemy(
    val id: String,
    val position: Vector2D,
    val angleDegrees: Float = 180f,
    val health: Float = 75f,
    val maxHealth: Float = 75f,
    val isAlerted: Boolean = false,
    val weaponName: String = "AK-47 Hostil",
    val damage: Float = 16f,
    val range: Float = 270f,
    val fireCooldownMs: Long = 450L,
    val lastShotTimeMs: Long = 0L,
    val isFiring: Boolean = false,
    val patrolPath: List<Vector2D> = emptyList(),
    val currentPatrolIndex: Int = 0
) {
    val isAlive: Boolean get() = health > 0f
}
