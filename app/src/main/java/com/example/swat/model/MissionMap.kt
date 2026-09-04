package com.example.swat.model

data class MissionMap(
    val width: Float = 960f,
    val height: Float = 540f,
    val walls: List<Wall>,
    val doors: List<Door>,
    val defaultOperators: List<Operator>,
    val defaultEnemies: List<Enemy>
) {
    companion object {
        fun createDefaultScenario(): MissionMap {
            val walls = listOf(
                // Outer perimeter borders
                Wall("border_top", 10f, 10f, 950f, 22f),
                Wall("border_bottom", 10f, 518f, 950f, 530f),
                Wall("border_left", 10f, 10f, 22f, 530f),
                Wall("border_right", 938f, 10f, 950f, 530f),

                // Infiltration zone divider (x = 190) with gap for Door 1 (y: 230..290)
                Wall("wall_entry_top", 190f, 22f, 202f, 230f),
                Wall("wall_entry_bottom", 190f, 290f, 202f, 518f),

                // North room horizontal divider (y = 180) with gap for Door 2 (x: 330..390)
                Wall("wall_north_left", 202f, 175f, 330f, 187f),
                Wall("wall_north_right", 390f, 175f, 530f, 187f),

                // South room horizontal divider (y = 340) with gap for Door 3 (x: 330..390)
                Wall("wall_south_left", 202f, 335f, 330f, 347f),
                Wall("wall_south_right", 390f, 335f, 530f, 347f),

                // Middle/East room divider (x = 530) with gap for Door 4 (y: 230..290)
                Wall("wall_east_top", 525f, 22f, 537f, 230f),
                Wall("wall_east_bottom", 525f, 290f, 537f, 518f),

                // Internal cover obstacles in Executive Command Room
                Wall("cover_exec_1", 660f, 120f, 720f, 140f),
                Wall("cover_exec_2", 660f, 380f, 720f, 400f),
                Wall("cover_exec_pillar", 740f, 240f, 770f, 280f),

                // Server rack obstacles in North Room
                Wall("cover_server_1", 260f, 70f, 310f, 110f),
                Wall("cover_server_2", 420f, 70f, 470f, 110f),

                // Crates obstacle in South Armory
                Wall("cover_armory_1", 270f, 410f, 330f, 450f),
                Wall("cover_armory_2", 410f, 410f, 460f, 450f)
            )

            val doors = listOf(
                Door(
                    id = "door_entry",
                    name = "Puerta Principal",
                    center = Vector2D(196f, 260f),
                    length = 60f,
                    orientation = DoorOrientation.VERTICAL,
                    isOpen = false
                ),
                Door(
                    id = "door_server",
                    name = "Puerta Servidores",
                    center = Vector2D(360f, 181f),
                    length = 60f,
                    orientation = DoorOrientation.HORIZONTAL,
                    isOpen = false
                ),
                Door(
                    id = "door_armory",
                    name = "Puerta Armería",
                    center = Vector2D(360f, 341f),
                    length = 60f,
                    orientation = DoorOrientation.HORIZONTAL,
                    isOpen = false
                ),
                Door(
                    id = "door_command",
                    name = "Puerta Comando",
                    center = Vector2D(531f, 260f),
                    length = 60f,
                    orientation = DoorOrientation.VERTICAL,
                    isOpen = false
                )
            )

            val operators = listOf(
                Operator(
                    id = "op_bravo1",
                    callsign = "Bravo-1",
                    role = "Líder de Asalto",
                    position = Vector2D(80f, 240f),
                    angleDegrees = 0f,
                    inventory = listOf(Weapon.createRifle(), Weapon.createGlock()),
                    equippedWeaponIndex = 0
                ),
                Operator(
                    id = "op_bravo2",
                    callsign = "Bravo-2",
                    role = "Breacher CQB",
                    position = Vector2D(80f, 280f),
                    angleDegrees = 0f,
                    inventory = listOf(Weapon.createGlock(), Weapon.createRifle()),
                    equippedWeaponIndex = 0
                ),
                Operator(
                    id = "op_bravo3",
                    callsign = "Bravo-3",
                    role = "Tirador Táctico",
                    position = Vector2D(50f, 260f),
                    angleDegrees = 0f,
                    inventory = listOf(Weapon.createRifle(), Weapon.createGlock()),
                    equippedWeaponIndex = 0
                )
            )

            val enemies = listOf(
                Enemy(
                    id = "enemy_corridor",
                    position = Vector2D(360f, 260f),
                    angleDegrees = 180f,
                    health = 60f,
                    maxHealth = 60f,
                    weaponName = "Subfusil 9mm",
                    damage = 14f,
                    range = 240f
                ),
                Enemy(
                    id = "enemy_server",
                    position = Vector2D(380f, 90f),
                    angleDegrees = 200f,
                    health = 75f,
                    maxHealth = 75f,
                    weaponName = "Fusil AKM",
                    damage = 20f,
                    range = 280f
                ),
                Enemy(
                    id = "enemy_armory",
                    position = Vector2D(370f, 440f),
                    angleDegrees = 160f,
                    health = 70f,
                    maxHealth = 70f,
                    weaponName = "Escopeta Táctica",
                    damage = 25f,
                    range = 200f
                ),
                Enemy(
                    id = "enemy_exec_guard",
                    position = Vector2D(650f, 240f),
                    angleDegrees = 180f,
                    health = 80f,
                    maxHealth = 80f,
                    weaponName = "Carabina M4",
                    damage = 22f,
                    range = 300f
                ),
                Enemy(
                    id = "enemy_exec_leader",
                    position = Vector2D(840f, 260f),
                    angleDegrees = 180f,
                    health = 110f,
                    maxHealth = 110f,
                    weaponName = "Fusil Pesado",
                    damage = 26f,
                    range = 340f
                )
            )

            return MissionMap(
                walls = walls,
                doors = doors,
                defaultOperators = operators,
                defaultEnemies = enemies
            )
        }
    }
}
