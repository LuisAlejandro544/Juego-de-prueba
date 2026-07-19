package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.engine.GameEngine
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PitchDarkBg
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        gameEngine = GameEngine(applicationContext)

        // Try to load state on startup
        lifecycleScope.launch {
            gameEngine.tryLoadGame()
        }

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PitchDarkBg)
                ) { innerPadding ->
                    OnboardingScreen(
                        engine = gameEngine,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
