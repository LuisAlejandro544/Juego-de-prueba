package com.example.swat.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.DoorBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swat.model.Door
import com.example.swat.model.Operator
import com.example.swat.model.WeaponType
import com.example.swat.viewmodel.TacticalGameState
import com.example.swat.viewmodel.TacticalStance

@Composable
fun TacticalHud(
    state: TacticalGameState,
    onSelectOperator: (String) -> Unit,
    onToggleWeapon: () -> Unit,
    onReload: () -> Unit,
    onOpenInventory: () -> Unit,
    onToggleDoor: (String) -> Unit,
    onRestartMission: () -> Unit,
    onToggleStance: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // TOP BAR
        TopMissionBar(
            state = state,
            onRestart = onRestartMission,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        // RADIO COMMS TICKER (Top Center, below top bar)
        val latestRadio = state.radioMessages.lastOrNull()
        if (latestRadio != null) {
            RadioTicker(
                sender = latestRadio.sender,
                message = latestRadio.message,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp)
            )
        }

        // BOTTOM LEFT: TEAM OPERATOR SELECTOR
        TeamSelectorBar(
            operators = state.operators,
            selectedOperatorId = state.selectedOperatorId,
            onSelect = onSelectOperator,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 10.dp)
        )

        // BOTTOM RIGHT: SELECTED OPERATOR MINI-INVENTORY & TACTICAL ACTIONS
        val selectedOp = state.selectedOperator
        if (selectedOp != null) {
            // Find if any door is within interaction distance of this operator
            val nearbyDoor = state.doors.find { it.canInteract(selectedOp.position) }

            SelectedOperatorControlPanel(
                operator = selectedOp,
                nearbyDoor = nearbyDoor,
                stance = state.stance,
                onToggleWeapon = onToggleWeapon,
                onReload = onReload,
                onOpenInventory = onOpenInventory,
                onToggleDoor = { if (nearbyDoor != null) onToggleDoor(nearbyDoor.id) },
                onToggleStance = onToggleStance,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 10.dp)
            )
        }

        // MISSION OUTCOME OVERLAYS
        if (state.isMissionCompleted) {
            MissionOutcomeOverlay(
                isVictory = true,
                onRestart = onRestartMission,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (state.isMissionFailed) {
            MissionOutcomeOverlay(
                isVictory = false,
                onRestart = onRestartMission,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun TopMissionBar(
    state: TacticalGameState,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCC0B1120))
            .border(BorderStroke(1.dp, Color(0x3338BDF8)), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mission Title & Status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(if (state.aliveEnemiesCount > 0) Color(0xFFEF4444) else Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "OP. CQB: DESPEJE DE EDIFICIO",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "SWAT táctico | C++ • Rust • Lua 5.4",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Color(0xFF38BDF8)
                )
            }
        }

        // Objective Counters
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hostiles alive
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "HOSTILES: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                Text(
                    text = "${state.aliveEnemiesCount} / ${state.totalEnemiesCount}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (state.aliveEnemiesCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace
                )
            }

            // Operators alive
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SWAT: ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp
                )
                Text(
                    text = "${state.aliveOperatorsCount} / ${state.operators.size}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace
                )
            }

            // Restart button
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .height(28.dp)
                    .testTag("restart_mission_top_button"),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0x6638BDF8)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF38BDF8)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "REINICIAR",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color(0xFF38BDF8)
                )
            }
        }
    }
}

@Composable
private fun RadioTicker(
    sender: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC090D16))
            .border(BorderStroke(1.dp, Color(0x3300E5FF)), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[$sender]: ",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00E5FF),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFE2E8F0),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun TeamSelectorBar(
    operators: List<Operator>,
    selectedOperatorId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        operators.forEach { op ->
            val isSelected = op.id == selectedOperatorId
            val borderColor = when {
                !op.isAlive -> Color(0xFF7F1D1D)
                isSelected -> Color(0xFF00E5FF)
                else -> Color(0xFF334155)
            }
            val bgColor = when {
                !op.isAlive -> Color(0xCC1A0F15)
                isSelected -> Color(0xEE0B192C)
                else -> Color(0xCC0F172A)
            }

            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor), RoundedCornerShape(8.dp))
                    .clickable(enabled = op.isAlive) { onSelect(op.id) }
                    .testTag("operator_card_${op.id}"),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = op.callsign,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (op.isAlive) Color.White else Color(0xFFEF4444),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // HP Bar
                    LinearProgressIndicator(
                        progress = { (op.health / op.maxHealth).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(54.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = if (op.health > 40f) Color(0xFF10B981) else Color(0xFFEF4444),
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Weapon tag
                    Text(
                        text = if (op.equippedWeapon.type == WeaponType.GLOCK) "GLOCK" else "M4 CQB",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 9.sp,
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedOperatorControlPanel(
    operator: Operator,
    nearbyDoor: Door?,
    stance: TacticalStance,
    onToggleWeapon: () -> Unit,
    onReload: () -> Unit,
    onOpenInventory: () -> Unit,
    onToggleDoor: () -> Unit,
    onToggleStance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weapon = operator.equippedWeapon

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xEE0B1120))
            .border(BorderStroke(1.2.dp, Color(0x6600E5FF)), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Door interaction shortcut button (Contextual!)
        if (nearbyDoor != null) {
            Button(
                onClick = onToggleDoor,
                modifier = Modifier
                    .height(46.dp)
                    .testTag("door_action_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (nearbyDoor.isOpen) Color(0xFF10B981) else Color(0xFFD97706)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = if (nearbyDoor.isOpen) Icons.Default.MeetingRoom else Icons.Default.DoorBack,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = if (nearbyDoor.isOpen) "CERRAR" else "ABRIR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "PUERTA",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 8.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Weapon Thumbnail & Ammo
        Box(
            modifier = Modifier
                .width(76.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF090D16))
                .clickable { onOpenInventory() }
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = weapon.drawableResId),
                contentDescription = weapon.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        // Ammo Display
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.clickable { onOpenInventory() }
        ) {
            Text(
                text = weapon.name.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 10.sp
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${weapon.currentAmmo}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (weapon.currentAmmo == 0) Color(0xFFEF4444) else Color(0xFF00E5FF),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
                Text(
                    text = " / ${weapon.reserveAmmo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (operator.isReloading) {
                Text(
                    text = "RECARGANDO...",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Quick Weapon Switch Button (Glock <-> Fusil)
        Button(
            onClick = onToggleWeapon,
            modifier = Modifier
                .height(46.dp)
                .testTag("quick_switch_weapon_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Cambiar Arma",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "CAMBIAR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 10.sp
                )
                Text(
                    text = if (weapon.type == WeaponType.GLOCK) "A FUSIL" else "A GLOCK",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 8.sp,
                    color = Color(0xFFE0F2FE)
                )
            }
        }

        // Reload Button
        OutlinedButton(
            onClick = onReload,
            enabled = weapon.canReload && !operator.isReloading,
            modifier = Modifier
                .height(46.dp)
                .testTag("quick_reload_button"),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Recargar",
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "RECARGAR",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8),
                fontSize = 10.sp
            )
        }

        // Mini Inventory Modal Button
        IconButton(
            onClick = onOpenInventory,
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B))
                .testTag("open_inventory_modal_button")
        ) {
            Icon(
                imageVector = Icons.Default.Backpack,
                contentDescription = "Abrir mini inventario",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MissionOutcomeOverlay(
    isVictory: Boolean,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isVictory) Color(0xFF10B981) else Color(0xFFEF4444)
    val titleText = if (isVictory) "¡MISIÓN CUMPLIDA!" else "¡OPERACIÓN FALLIDA!"
    val subText = if (isVictory) {
        "El edificio ha sido asegurado. Todos los hostiles fueron neutralizados por el equipo SWAT."
    } else {
        "Todo el equipo SWAT ha sido abatido en combate. La operación de asalto ha fracasado."
    }

    Surface(
        modifier = modifier
            .width(400.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp))
            .testTag(if (isVictory) "victory_overlay" else "defeat_overlay"),
        color = Color(0xF2090E17)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = borderColor,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("restart_mission_outcome_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVictory) Color(0xFF059669) else Color(0xFFDC2626)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VOLVER A JUGAR",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
