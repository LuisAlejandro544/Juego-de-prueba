package com.example.swat.model

import com.example.R

enum class WeaponType {
    GLOCK,
    RIFLE
}

data class Weapon(
    val type: WeaponType,
    val name: String,
    val caliber: String,
    val currentAmmo: Int,
    val maxMagazine: Int,
    val reserveAmmo: Int,
    val damage: Float,
    val range: Float,
    val fireRateCooldownMs: Long,
    val reloadDurationMs: Long,
    val drawableResId: Int
) {
    val isMagazineEmpty: Boolean get() = currentAmmo <= 0
    val canReload: Boolean get() = currentAmmo < maxMagazine && reserveAmmo > 0

    companion object {
        fun createGlock(): Weapon = Weapon(
            type = WeaponType.GLOCK,
            name = "Glock 17",
            caliber = "9x19mm Parabellum",
            currentAmmo = 17,
            maxMagazine = 17,
            reserveAmmo = 51,
            damage = 25f,
            range = 240f,
            fireRateCooldownMs = 300L,
            reloadDurationMs = 1400L,
            drawableResId = R.drawable.img_weapon_glock
        )

        fun createRifle(): Weapon = Weapon(
            type = WeaponType.RIFLE,
            name = "Fusil M4 CQB",
            caliber = "5.56x45mm NATO",
            currentAmmo = 30,
            maxMagazine = 30,
            reserveAmmo = 90,
            damage = 42f,
            range = 380f,
            fireRateCooldownMs = 140L,
            reloadDurationMs = 2100L,
            drawableResId = R.drawable.img_weapon_rifle
        )
    }
}
