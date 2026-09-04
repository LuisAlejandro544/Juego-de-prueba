package com.example.swat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swat.viewmodel.TacticalGameViewModel
import com.example.swat.viewmodel.TacticalStance

@Composable
fun TacticalGameScreen(
    viewModel: TacticalGameViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B13))
            .systemBarsPadding()
    ) {
        // Main Tactical Canvas (World, FOV, Operators, Enemies, Doors, Walls)
        TacticalGameCanvas(
            state = state,
            onMapTap = { x, y -> viewModel.handleMapTap(x, y) }
        )

        // Overlay HUD (Team selector, Weapon bar, Ammo, Reload, Door action, Radio feed)
        TacticalHud(
            state = state,
            onSelectOperator = { viewModel.selectOperator(it) },
            onToggleWeapon = { viewModel.toggleEquippedWeapon() },
            onReload = { viewModel.reloadWeapon() },
            onOpenInventory = { viewModel.setShowInventory(true) },
            onToggleDoor = { viewModel.toggleDoor(it) },
            onRestartMission = { viewModel.restartMission() },
            onToggleStance = {
                val nextStance = when (state.stance) {
                    TacticalStance.ASSAULT -> TacticalStance.STEALTH
                    TacticalStance.STEALTH -> TacticalStance.HOLD_FIRE
                    TacticalStance.HOLD_FIRE -> TacticalStance.ASSAULT
                }
                viewModel.setTacticalStance(nextStance)
            }
        )

        // Detailed Mini-Inventory Dialog Modal
        if (state.showInventoryDialog && state.selectedOperator != null) {
            MiniInventoryDialog(
                operator = state.selectedOperator!!,
                onDismiss = { viewModel.setShowInventory(false) },
                onSelectWeapon = {
                    viewModel.switchWeapon(it)
                    viewModel.setShowInventory(false)
                },
                onReload = { viewModel.reloadWeapon() }
            )
        }
    }
}
