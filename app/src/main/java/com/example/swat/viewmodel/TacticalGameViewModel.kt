package com.example.swat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swat.engine.FieldOfView
import com.example.swat.engine.TacticalAudio
import com.example.swat.model.BulletTracer
import com.example.swat.model.Door
import com.example.swat.model.Enemy
import com.example.swat.model.ImpactEffect
import com.example.swat.model.MissionMap
import com.example.swat.model.Operator
import com.example.swat.model.RadioMessage
import com.example.swat.model.Vector2D
import com.example.swat.model.Wall
import com.example.swat.model.WeaponType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.min

enum class TacticalStance {
    ASSAULT, // Fuego a discreción al entrar en rango
    STEALTH, // Apuntar y aproximarse silencioso
    HOLD_FIRE // Alto el fuego
}

data class TacticalGameState(
    val operators: List<Operator>,
    val selectedOperatorId: String? = null,
    val enemies: List<Enemy>,
    val walls: List<Wall>,
    val doors: List<Door>,
    val tracers: List<BulletTracer> = emptyList(),
    val impacts: List<ImpactEffect> = emptyList(),
    val radioMessages: List<RadioMessage> = emptyList(),
    val visionPolygons: Map<String, List<Vector2D>> = emptyMap(),
    val visibleEnemyIds: Set<String> = emptySet(),
    val stance: TacticalStance = TacticalStance.ASSAULT,
    val isMissionCompleted: Boolean = false,
    val isMissionFailed: Boolean = false,
    val isPaused: Boolean = false,
    val showInventoryDialog: Boolean = false
) {
    val selectedOperator: Operator?
        get() = operators.find { it.id == selectedOperatorId }

    val aliveOperatorsCount: Int get() = operators.count { it.isAlive }
    val aliveEnemiesCount: Int get() = enemies.count { it.isAlive }
    val totalEnemiesCount: Int get() = enemies.size
}

class TacticalGameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(createInitialState())
    val gameState: StateFlow<TacticalGameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var lastTickTimeMs: Long = System.currentTimeMillis()
    private var nextEffectId: Long = 1L

    init {
        startGameLoop()
        postRadioMessage("COMANDO", "Equipo SWAT en posición. Objetivo: despejar el edificio.")
    }

    private fun createInitialState(): TacticalGameState {
        val scenario = MissionMap.createDefaultScenario()
        val initialSelected = scenario.defaultOperators.firstOrNull()?.id
        return TacticalGameState(
            operators = scenario.defaultOperators,
            selectedOperatorId = initialSelected,
            enemies = scenario.defaultEnemies,
            walls = scenario.walls,
            doors = scenario.doors
        )
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastTickTimeMs = System.currentTimeMillis()
        gameLoopJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val deltaSec = ((now - lastTickTimeMs).coerceAtMost(50L)) / 1000f
                lastTickTimeMs = now

                updateGame(deltaSec, now)
                delay(16L) // Target ~60 FPS
            }
        }
    }

    private fun updateGame(deltaSec: Float, now: Long) {
        val currentState = _gameState.value
        if (currentState.isPaused || currentState.isMissionCompleted || currentState.isMissionFailed) {
            return
        }

        // 1. Update Operators (Movement, Collisions, Reloading)
        val updatedOperators = currentState.operators.map { op ->
            var currentOp = op
            if (!currentOp.isAlive) return@map currentOp

            // Check reload completion
            if (currentOp.isReloading && now >= currentOp.reloadEndTimeMs) {
                val weapon = currentOp.equippedWeapon
                val needed = weapon.maxMagazine - weapon.currentAmmo
                val reloadAmount = min(needed, weapon.reserveAmmo)
                val updatedWeapon = weapon.copy(
                    currentAmmo = weapon.currentAmmo + reloadAmount,
                    reserveAmmo = weapon.reserveAmmo - reloadAmount
                )
                currentOp = currentOp.updateEquippedWeapon(updatedWeapon).copy(isReloading = false)
            }

            // Movement logic
            val target = currentOp.targetPosition
            if (target != null) {
                val distance = currentOp.position.distanceTo(target)
                if (distance < 4f) {
                    // Arrived at destination
                    currentOp = currentOp.copy(targetPosition = null)
                } else {
                    val moveSpeed = if (currentOp.equippedWeapon.type == WeaponType.GLOCK) 120f else 100f
                    val step = moveSpeed * deltaSec
                    val dir = (target - currentOp.position).normalized()
                    val nextPos = currentOp.position + (dir * step)

                    // Collision checking against walls and closed doors
                    val opRadius = 16f
                    var blocked = false

                    for (wall in currentState.walls) {
                        if (wall.collidesWithCircle(nextPos, opRadius)) {
                            blocked = true
                            break
                        }
                    }

                    if (!blocked) {
                        for (door in currentState.doors) {
                            if (door.collidesWithCircle(nextPos, opRadius)) {
                                blocked = true
                                break
                            }
                        }
                    }

                    if (!blocked) {
                        // Smoothly face movement direction
                        val targetAngle = Math.toDegrees(atan2(dir.y.toDouble(), dir.x.toDouble())).toFloat()
                        val smoothAngle = interpolateAngle(currentOp.angleDegrees, targetAngle, 0.25f)
                        currentOp = currentOp.copy(position = nextPos, angleDegrees = smoothAngle)
                    } else {
                        // Attempt wall sliding or stop
                        currentOp = currentOp.copy(targetPosition = null)
                    }
                }
            }

            currentOp
        }

        // 2. Compute Vision and Field of View
        val visionMap = mutableMapOf<String, List<Vector2D>>()
        val visibleEnemies = mutableSetOf<String>()

        for (op in updatedOperators) {
            if (op.isAlive) {
                val polygon = FieldOfView.computeVisionPolygon(
                    operator = op,
                    walls = currentState.walls,
                    doors = currentState.doors,
                    rayCount = 40
                )
                visionMap[op.id] = polygon

                // Check which enemies are seen by this operator
                for (enemy in currentState.enemies) {
                    if (enemy.isAlive && !visibleEnemies.contains(enemy.id)) {
                        if (FieldOfView.canOperatorSeePoint(op, enemy.position, currentState.walls, currentState.doors)) {
                            visibleEnemies.add(enemy.id)
                        }
                    }
                }
            }
        }

        // 3. Combat Simulation: Operators Engage Enemies
        val newTracers = mutableListOf<BulletTracer>()
        val newImpacts = mutableListOf<ImpactEffect>()
        val enemyHealthMap = currentState.enemies.associate { it.id to it.health }.toMutableMap()
        var killsAdded = 0

        val finalOperators = updatedOperators.map { op ->
            var currentOp = op
            if (!currentOp.isAlive || currentOp.isReloading || currentState.stance == TacticalStance.HOLD_FIRE) {
                return@map currentOp.copy(isFiring = false)
            }

            // Find closest visible enemy in line of sight and range
            val targetEnemy = currentState.enemies
                .filter { it.isAlive && (enemyHealthMap[it.id] ?: 0f) > 0f }
                .filter { visibleEnemies.contains(it.id) }
                .filter { currentOp.position.distanceTo(it.position) <= currentOp.equippedWeapon.range }
                .minByOrNull { currentOp.position.distanceTo(it.position) }

            if (targetEnemy != null) {
                // Rotate operator towards target
                val aimAngle = currentOp.position.angleTo(targetEnemy.position)
                currentOp = currentOp.copy(angleDegrees = aimAngle)

                val weapon = currentOp.equippedWeapon
                if (now - currentOp.lastShotTimeMs >= weapon.fireRateCooldownMs) {
                    if (weapon.currentAmmo > 0) {
                        // FIRE!
                        val updatedWeapon = weapon.copy(currentAmmo = weapon.currentAmmo - 1)
                        currentOp = currentOp.updateEquippedWeapon(updatedWeapon).copy(
                            lastShotTimeMs = now,
                            isFiring = true
                        )

                        // Sound
                        if (weapon.type == WeaponType.GLOCK) {
                            TacticalAudio.playGlockShot()
                        } else {
                            TacticalAudio.playRifleShot()
                        }

                        // Tracer
                        newTracers.add(
                            BulletTracer(
                                id = nextEffectId++,
                                start = currentOp.position,
                                end = targetEnemy.position,
                                isSwat = true,
                                createdAtMs = now
                            )
                        )

                        // Damage & Impact
                        val currentHp = enemyHealthMap[targetEnemy.id] ?: 0f
                        val newHp = (currentHp - weapon.damage).coerceAtLeast(0f)
                        enemyHealthMap[targetEnemy.id] = newHp

                        newImpacts.add(
                            ImpactEffect(
                                id = nextEffectId++,
                                position = targetEnemy.position,
                                isBlood = true,
                                createdAtMs = now
                            )
                        )

                        if (newHp <= 0f && currentHp > 0f) {
                            killsAdded++
                            currentOp = currentOp.copy(kills = currentOp.kills + 1)
                            postRadioMessage(currentOp.callsign, "¡Hostil neutralizado!")
                        }
                    } else if (weapon.canReload) {
                        // Auto reload when empty
                        TacticalAudio.playReloadSound()
                        currentOp = currentOp.copy(
                            isReloading = true,
                            reloadEndTimeMs = now + weapon.reloadDurationMs,
                            isFiring = false
                        )
                    }
                } else {
                    currentOp = currentOp.copy(isFiring = false)
                }
            } else {
                currentOp = currentOp.copy(isFiring = false)
            }

            currentOp
        }

        // 4. Combat Simulation: Enemies Engage Operators
        val opHealthMap = finalOperators.associate { it.id to it.health }.toMutableMap()
        val opArmorMap = finalOperators.associate { it.id to it.armor }.toMutableMap()

        val updatedEnemies = currentState.enemies.map { enemy ->
            var currentEnemy = enemy
            val currentHp = enemyHealthMap[enemy.id] ?: 0f
            if (currentHp <= 0f) {
                return@map currentEnemy.copy(health = 0f, isFiring = false)
            }
            currentEnemy = currentEnemy.copy(health = currentHp)

            // Find closest visible operator in line of sight
            val targetOp = finalOperators
                .filter { it.isAlive && (opHealthMap[it.id] ?: 0f) > 0f }
                .filter { currentEnemy.position.distanceTo(it.position) <= currentEnemy.range }
                .filter { FieldOfView.hasLineOfSight(currentEnemy.position, it.position, currentState.walls, currentState.doors) }
                .minByOrNull { currentEnemy.position.distanceTo(it.position) }

            if (targetOp != null) {
                val aimAngle = currentEnemy.position.angleTo(targetOp.position)
                currentEnemy = currentEnemy.copy(angleDegrees = aimAngle, isAlerted = true)

                if (now - currentEnemy.lastShotTimeMs >= currentEnemy.fireCooldownMs) {
                    // Enemy fires!
                    TacticalAudio.playEnemyShot()
                    currentEnemy = currentEnemy.copy(lastShotTimeMs = now, isFiring = true)

                    newTracers.add(
                        BulletTracer(
                            id = nextEffectId++,
                            start = currentEnemy.position,
                            end = targetOp.position,
                            isSwat = false,
                            createdAtMs = now
                        )
                    )

                    // Damage calculation (Armor absorbs 60%)
                    val rawDmg = currentEnemy.damage
                    val currentArmor = opArmorMap[targetOp.id] ?: 0f
                    val currentOpHp = opHealthMap[targetOp.id] ?: 0f

                    val armorDmg = min(currentArmor, rawDmg * 0.6f)
                    val hpDmg = rawDmg - armorDmg

                    val newArmor = (currentArmor - armorDmg).coerceAtLeast(0f)
                    val newOpHp = (currentOpHp - hpDmg).coerceAtLeast(0f)

                    opArmorMap[targetOp.id] = newArmor
                    opHealthMap[targetOp.id] = newOpHp

                    newImpacts.add(
                        ImpactEffect(
                            id = nextEffectId++,
                            position = targetOp.position,
                            isBlood = false,
                            createdAtMs = now
                        )
                    )

                    if (newOpHp <= 0f && currentOpHp > 0f) {
                        postRadioMessage("COMANDO", "¡${targetOp.callsign} herido crítico! ¡Operador caído!")
                    }
                } else {
                    currentEnemy = currentEnemy.copy(isFiring = false)
                }
            } else {
                currentEnemy = currentEnemy.copy(isFiring = false)
            }

            currentEnemy
        }

        // Apply health & armor to operators
        val operatorsWithDamage = finalOperators.map { op ->
            val updatedHp = opHealthMap[op.id] ?: op.health
            val updatedArmor = opArmorMap[op.id] ?: op.armor
            op.copy(health = updatedHp, armor = updatedArmor)
        }

        // 5. Clean up expired visual effects & radio messages
        val activeTracers = (currentState.tracers + newTracers).filter { now - it.createdAtMs < it.durationMs }
        val activeImpacts = (currentState.impacts + newImpacts).filter { now - it.createdAtMs < it.durationMs }
        val activeRadio = currentState.radioMessages.filter { now - it.createdAtMs < it.durationMs }

        // 6. Check Win/Loss conditions
        val allEnemiesDead = updatedEnemies.none { it.isAlive }
        val allOperatorsDead = operatorsWithDamage.none { it.isAlive }

        if (allEnemiesDead && !currentState.isMissionCompleted) {
            postRadioMessage("COMANDO", "¡Sector despejado! Todos los hostiles han sido neutralizados. Misión cumplida.")
            TacticalAudio.playRadioPing()
        }

        _gameState.update {
            it.copy(
                operators = operatorsWithDamage,
                enemies = updatedEnemies,
                tracers = activeTracers,
                impacts = activeImpacts,
                radioMessages = activeRadio,
                visionPolygons = visionMap,
                visibleEnemyIds = visibleEnemies,
                isMissionCompleted = allEnemiesDead,
                isMissionFailed = allOperatorsDead
            )
        }
    }

    fun selectOperator(operatorId: String) {
        val op = _gameState.value.operators.find { it.id == operatorId }
        if (op != null && op.isAlive) {
            TacticalAudio.playRadioPing()
            _gameState.update { it.copy(selectedOperatorId = operatorId) }
        }
    }

    fun handleMapTap(tapX: Float, tapY: Float) {
        val state = _gameState.value
        val tapPos = Vector2D(tapX, tapY)

        // First, check if clicked directly on a living operator to select them
        val clickedOp = state.operators.find { it.isAlive && it.position.distanceTo(tapPos) <= 30f }
        if (clickedOp != null) {
            selectOperator(clickedOp.id)
            return
        }

        val selectedOpId = state.selectedOperatorId ?: return
        val selectedOp = state.operators.find { it.id == selectedOpId } ?: return
        if (!selectedOp.isAlive) return

        // Second, check if clicked on a door within reasonable distance to interact with it
        val clickedDoor = state.doors.find { it.center.distanceTo(tapPos) <= 45f }
        if (clickedDoor != null) {
            if (clickedDoor.canInteract(selectedOp.position)) {
                toggleDoor(clickedDoor.id)
                return
            } else {
                // Move towards the door to interact with it!
                orderMoveSelectedOperator(clickedDoor.center)
                return
            }
        }

        // Third, normal move order
        orderMoveSelectedOperator(tapPos)
    }

    private fun orderMoveSelectedOperator(destination: Vector2D) {
        val selectedOpId = _gameState.value.selectedOperatorId ?: return
        _gameState.update { state ->
            state.copy(
                operators = state.operators.map { op ->
                    if (op.id == selectedOpId) {
                        op.copy(targetPosition = destination)
                    } else {
                        op
                    }
                }
            )
        }
    }

    fun toggleDoor(doorId: String) {
        val state = _gameState.value
        val door = state.doors.find { it.id == doorId } ?: return
        val selectedOp = state.selectedOperator

        // If door is close to any operator or selected operator
        val canToggle = state.operators.any { it.isAlive && door.canInteract(it.position) }
        if (!canToggle) {
            postRadioMessage("SWAT", "Demasiado lejos para operar la puerta.")
            return
        }

        TacticalAudio.playDoorSound()
        val willOpen = !door.isOpen
        val actionText = if (willOpen) "abierta" else "cerrada"
        postRadioMessage(selectedOp?.callsign ?: "EQUIPO", "${door.name} $actionText.")

        _gameState.update { s ->
            s.copy(
                doors = s.doors.map { d ->
                    if (d.id == doorId) d.copy(isOpen = willOpen) else d
                }
            )
        }
    }

    fun switchWeapon(weaponType: WeaponType) {
        val selectedOpId = _gameState.value.selectedOperatorId ?: return
        TacticalAudio.playReloadSound()

        _gameState.update { state ->
            state.copy(
                operators = state.operators.map { op ->
                    if (op.id == selectedOpId) {
                        val newIndex = op.inventory.indexOfFirst { it.type == weaponType }
                        if (newIndex != -1) op.switchWeapon(newIndex) else op
                    } else {
                        op
                    }
                }
            )
        }
    }

    fun toggleEquippedWeapon() {
        val selectedOp = _gameState.value.selectedOperator ?: return
        val currentType = selectedOp.equippedWeapon.type
        val nextType = if (currentType == WeaponType.GLOCK) WeaponType.RIFLE else WeaponType.GLOCK
        switchWeapon(nextType)
    }

    fun reloadWeapon() {
        val state = _gameState.value
        val selectedOp = state.selectedOperator ?: return
        if (!selectedOp.isAlive || selectedOp.isReloading) return
        if (!selectedOp.equippedWeapon.canReload) return

        TacticalAudio.playReloadSound()
        val now = System.currentTimeMillis()
        val reloadTime = selectedOp.equippedWeapon.reloadDurationMs

        _gameState.update { s ->
            s.copy(
                operators = s.operators.map { op ->
                    if (op.id == selectedOp.id) {
                        op.copy(
                            isReloading = true,
                            reloadEndTimeMs = now + reloadTime
                        )
                    } else {
                        op
                    }
                }
            )
        }
    }

    fun setTacticalStance(stance: TacticalStance) {
        TacticalAudio.playRadioPing()
        val desc = when (stance) {
            TacticalStance.ASSAULT -> "Asalto táctico: Fuego a discreción."
            TacticalStance.STEALTH -> "Modo sigilo: Movimiento cauto."
            TacticalStance.HOLD_FIRE -> "Alto el fuego: Mantener posición."
        }
        postRadioMessage("COMANDO", desc)
        _gameState.update { it.copy(stance = stance) }
    }

    fun setShowInventory(show: Boolean) {
        _gameState.update { it.copy(showInventoryDialog = show) }
    }

    fun restartMission() {
        TacticalAudio.playRadioPing()
        _gameState.value = createInitialState()
        postRadioMessage("COMANDO", "Misión reiniciada. Equipos listos para el asalto.")
    }

    private fun postRadioMessage(sender: String, message: String) {
        val newMsg = RadioMessage(
            id = nextEffectId++,
            sender = sender,
            message = message,
            createdAtMs = System.currentTimeMillis()
        )
        _gameState.update { it.copy(radioMessages = it.radioMessages + newMsg) }
    }

    private fun interpolateAngle(current: Float, target: Float, factor: Float): Float {
        var diff = (target - current) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return (current + diff * factor) % 360f
    }
}
