package com.example.swat.model

data class Operator(
    val id: String,
    val callsign: String,
    val role: String,
    val position: Vector2D,
    val targetPosition: Vector2D? = null,
    val angleDegrees: Float = 0f,
    val health: Float = 100f,
    val maxHealth: Float = 100f,
    val armor: Float = 50f,
    val inventory: List<Weapon> = listOf(Weapon.createRifle(), Weapon.createGlock()),
    val equippedWeaponIndex: Int = 0,
    val isReloading: Boolean = false,
    val reloadEndTimeMs: Long = 0L,
    val lastShotTimeMs: Long = 0L,
    val isFiring: Boolean = false,
    val kills: Int = 0,
    val fovAngleDegrees: Float = 100f,
    val fovRange: Float = 340f,
    val peripheralRange: Float = 55f
) {
    val isAlive: Boolean get() = health > 0f
    val equippedWeapon: Weapon get() = inventory.getOrElse(equippedWeaponIndex) { inventory.first() }

    fun switchWeapon(newIndex: Int): Operator {
        if (newIndex in inventory.indices && newIndex != equippedWeaponIndex) {
            return copy(
                equippedWeaponIndex = newIndex,
                isReloading = false
            )
        }
        return this
    }

    fun updateEquippedWeapon(newWeapon: Weapon): Operator {
        val updatedInventory = inventory.toMutableList()
        updatedInventory[equippedWeaponIndex] = newWeapon
        return copy(inventory = updatedInventory)
    }
}
