package com.example.ui.screens

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.League
import com.example.model.MatchEvent
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class SimPhase {
    NOT_STARTED,
    FIRST_HALF,
    HALF_TIME_PAUSE,
    SECOND_HALF,
    FINISHED
}

// Play a nice goal sound (whistle/beep melody) safely in the background
fun playGoalSound() {
    try {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        Thread {
            try {
                Thread.sleep(150)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                Thread.sleep(150)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Generate procedurally sound football commentaries to make the tick feel alive
fun getDynamicCommentary(minute: Int, homeTeam: String, awayTeam: String): String? {
    val random = java.util.Random(minute.toLong() * 31 + homeTeam.hashCode())
    val commentaries = listOf(
        "El mediocampo de $homeTeam intenta hilvanar juego paciente con pases cortos y precisos.",
        "Presión alta de $awayTeam complicando seriamente la salida defensiva local.",
        "Se calientan los ánimos tras una disputada barrida de balón en la línea de banda.",
        "Balón largo buscando la velocidad por los extremos, pero se pierde por banda.",
        "Falta táctica inteligente en mitad de cancha para frenar un contragolpe rival.",
        "La hinchada de $homeTeam canta con fervor en las tribunas para animar a los suyos.",
        "El director técnico de $awayTeam grita indicaciones enérgicas al borde de su área técnica.",
        "Pase filtrado peligroso en la frontal, interceptado con maestría por la saga defensiva.",
        "Balón dividido en tres cuartos de cancha, juego físico de contacto y mucha fricción.",
        "Excelente cruce defensivo en el área grande para despejar un balón sumamente venenoso.",
        "Disparo lejano con poca dirección que termina cómodamente en los guantes del arquero.",
        "El partido reduce la intensidad mientras ambas escuadras reordenan sus líneas tácticas."
    )
    return if (minute > 0 && minute % 6 == 0) {
        commentaries[random.nextInt(commentaries.size)]
    } else {
        null
    }
}

@Composable
fun LiveMatchTickerScreen(
    league: League?,
    managerClubId: String?
) {
    if (league == null || managerClubId == null) return

    val currentFixtureRound = league.fixtures.getOrNull((league.currentRound - 1).coerceAtLeast(0))
    val userMatch = currentFixtureRound?.matches?.firstOrNull { 
        it.homeClubId == managerClubId || it.awayClubId == managerClubId 
    }

    if (userMatch != null) {
        val context = LocalContext.current
        val playWhistleSound = {
            try {
                val whistleResIds = listOf(
                    com.example.R.raw.whistle0,
                    com.example.R.raw.whistle1,
                    com.example.R.raw.whistle2,
                    com.example.R.raw.whistle3,
                    com.example.R.raw.whistle4,
                    com.example.R.raw.whistle5,
                    com.example.R.raw.whistle6
                )
                val randomRes = whistleResIds.random()
                val mp = MediaPlayer.create(context, randomRes)
                mp?.setOnCompletionListener { mediaPlayer ->
                    mediaPlayer.release()
                }
                mp?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Simulation States
        var simPhase by rememberSaveable { mutableStateOf(SimPhase.NOT_STARTED) }
        var currentMin by rememberSaveable { mutableStateOf(0) }
        var playSpeedMultiplier by rememberSaveable { mutableStateOf(1) } // 1x, 2x, 5x, 10x
        var isPaused by rememberSaveable { mutableStateOf(false) }

        // Trigger referee whistle sound on every match phase transition
        LaunchedEffect(simPhase) {
            if (simPhase != SimPhase.NOT_STARTED) {
                playWhistleSound()
            }
        }

        // Automatically reset simulation when match changes
        LaunchedEffect(userMatch.id) {
            simPhase = SimPhase.NOT_STARTED
            currentMin = 0
            isPaused = false
        }

        // Timer ticking loop
        LaunchedEffect(simPhase, isPaused, playSpeedMultiplier) {
            if (isPaused) return@LaunchedEffect

            if (simPhase == SimPhase.FIRST_HALF) {
                while (currentMin < 45) {
                    val baseDelay = 500L // 500ms real time = 1 game minute by default
                    delay(baseDelay / playSpeedMultiplier)
                    currentMin++
                    if (currentMin == 45) {
                        simPhase = SimPhase.HALF_TIME_PAUSE
                        isPaused = true
                        break
                    }
                }
            } else if (simPhase == SimPhase.SECOND_HALF) {
                while (currentMin < 90) {
                    val baseDelay = 500L
                    delay(baseDelay / playSpeedMultiplier)
                    currentMin++
                    if (currentMin == 90) {
                        simPhase = SimPhase.FINISHED
                        break
                    }
                }
            }
        }

        // Live calculated stats based on current running minute
        val liveEvents = remember(userMatch.events, currentMin) {
            val eventsList = mutableListOf<MatchEvent>()
            
            // 1. Add official match events up to currentMin
            eventsList.addAll(userMatch.events.filter { it.minute <= currentMin })
            
            // 2. Insert procedural commentaries to fill blank minutes
            val officialMinutes = userMatch.events.map { it.minute }.toSet()
            for (m in 1..currentMin) {
                if (!officialMinutes.contains(m)) {
                    val comment = getDynamicCommentary(m, userMatch.homeClubName, userMatch.awayClubName)
                    if (comment != null) {
                        eventsList.add(MatchEvent(minute = m, description = comment, type = "INFO_GENERIC"))
                    }
                }
            }
            
            // Newest events at the top (sports app style)
            eventsList.sortedByDescending { it.minute }
        }

        val liveHomeGoals = remember(liveEvents) {
            liveEvents.count { it.type == "GOAL_HOME" }
        }

        val liveAwayGoals = remember(liveEvents) {
            liveEvents.count { it.type == "GOAL_AWAY" }
        }

        // Sound Notification when a Goal is scored live!
        LaunchedEffect(liveHomeGoals, liveAwayGoals) {
            if (currentMin > 0) {
                playGoalSound()
            }
        }

        val liveHomeShots = remember(userMatch.homeShots, currentMin) {
            if (currentMin == 0) 0 else ((currentMin.toFloat() / 90f) * userMatch.homeShots).toInt().coerceIn(0, userMatch.homeShots)
        }

        val liveAwayShots = remember(userMatch.awayShots, currentMin) {
            if (currentMin == 0) 0 else ((currentMin.toFloat() / 90f) * userMatch.awayShots).toInt().coerceIn(0, userMatch.awayShots)
        }

        val livePossessionHome = remember(userMatch.possessionHome, currentMin) {
            if (currentMin == 0) 50 else userMatch.possessionHome
        }

        val livePossessionAway = remember(userMatch.possessionAway, currentMin) {
            if (currentMin == 0) 50 else userMatch.possessionAway
        }

        val scoreboardCard = @Composable {
            Card(
                colors = CardDefaults.cardColors(containerColor = PitchDarkBg),
                border = BorderStroke(1.dp, GrassEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Match status & Running minute label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isSimulatingLive = (simPhase == SimPhase.FIRST_HALF || simPhase == SimPhase.SECOND_HALF) && !isPaused
                        if (isSimulatingLive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusGreen)
                            )
                        }
                        
                        val phaseLabel = when (simPhase) {
                            SimPhase.NOT_STARTED -> "PRE-PARTIDO"
                            SimPhase.FIRST_HALF -> "1ER TIEMPO"
                            SimPhase.HALF_TIME_PAUSE -> "ENTRETEMPO"
                            SimPhase.SECOND_HALF -> "2DO TIEMPO"
                            SimPhase.FINISHED -> "FIN DEL PARTIDO"
                        }
                        
                        Text(
                            text = "$phaseLabel - $currentMin'",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(userMatch.homeClubName.uppercase(), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("LOCAL", color = TextSecondary, fontSize = 9.sp)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(liveHomeGoals.toString(), color = GrassEmerald, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Text("-", color = TextSecondary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(liveAwayGoals.toString(), color = GrassEmerald, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        }

                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(userMatch.awayClubName.uppercase(), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("VISITANTE", color = TextSecondary, fontSize = 9.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Match Progression Bar
                    LinearProgressIndicator(
                        progress = { currentMin.toFloat() / 90f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GrassEmerald,
                        trackColor = DarkSteel
                    )
                }
            }
        }

        val controlsCard = @Composable {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                border = BorderStroke(1.dp, DarkSteel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (simPhase) {
                        SimPhase.NOT_STARTED -> {
                            Button(
                                onClick = {
                                    simPhase = SimPhase.FIRST_HALF
                                    isPaused = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrassEmerald),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("start_match_button").height(34.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PitchDarkBg, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Iniciar Partido", color = PitchDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        SimPhase.FIRST_HALF, SimPhase.SECOND_HALF -> {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Play / Pause
                                Button(
                                    onClick = { isPaused = !isPaused },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftSapphire),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("pause_play_button").height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = GrassEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isPaused) "Reanudar" else "Pausar", color = TextPrimary, fontSize = 11.sp)
                                }

                                // Speed Multiplier (1x, 2x, 5x, 10x)
                                Button(
                                    onClick = {
                                        playSpeedMultiplier = when (playSpeedMultiplier) {
                                            1 -> 2
                                            2 -> 5
                                            5 -> 10
                                            else -> 1
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftSapphire),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("speed_button").height(34.dp)
                                ) {
                                    Icon(Icons.Default.FastForward, contentDescription = null, tint = GrassEmerald, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Velocidad: ${playSpeedMultiplier}x", color = TextPrimary, fontSize = 11.sp)
                                }
                            }
                        }
                        SimPhase.HALF_TIME_PAUSE -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                                Text("¡Descanso! Jugadores al vestuario.", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(bottom = 4.dp))
                                Button(
                                    onClick = {
                                        simPhase = SimPhase.SECOND_HALF
                                        isPaused = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrassEmerald),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("resume_match_button").height(34.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PitchDarkBg, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Iniciar Segundo Tiempo", color = PitchDarkBg, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                        SimPhase.FINISHED -> {
                            Button(
                                onClick = {
                                    currentMin = 0
                                    simPhase = SimPhase.FIRST_HALF
                                    isPaused = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftSapphire),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("replay_match_button").height(34.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = GrassEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver Repetición", color = TextPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        val statsRow = @Composable {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Possession Bar Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("POSESIÓN BALÓN", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("${livePossessionHome}%", color = GrassEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${livePossessionAway}%", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Shots bar Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DISPAROS DE ZONA", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("$liveHomeShots r", color = GrassEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$liveAwayShots r", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        val chronicleCard = @Composable { modifier: Modifier ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                border = BorderStroke(1.dp, DarkSteel),
                modifier = modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CRÓNICA DE JUEGO MINUTO A MINUTO (ZONA TÁCTICA)", color = NeonAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    if (simPhase == SimPhase.NOT_STARTED) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.SportsFootball, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                                Text("Presiona 'Iniciar Partido' para comenzar la transmisión en vivo del cotejo.", color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(liveEvents, key = { "${it.minute}_${it.type}_${it.description.hashCode()}" }) { event ->
                                val color = when (event.type) {
                                    "GOAL_HOME", "GOAL_AWAY" -> CardGold
                                    "SHUTOUT_HERO" -> GrassEmerald
                                    "YELLOW", "RED" -> StatusInsecureRed
                                    "INFO_GENERIC" -> TextSecondary
                                    else -> TextPrimary
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "[Min ${event.minute}']",
                                        color = GrassEmerald,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.width(55.dp)
                                    )
                                    Text(
                                        text = event.description,
                                        color = color,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        scoreboardCard()
                        controlsCard()
                        statsRow()
                    }

                    chronicleCard(Modifier.weight(0.9f).fillMaxHeight())
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    scoreboardCard()
                    controlsCard()
                    statsRow()
                    chronicleCard(Modifier.weight(1f))
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Simula una jornada en el botón superior para ver crónicas de partidos vivos.", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

