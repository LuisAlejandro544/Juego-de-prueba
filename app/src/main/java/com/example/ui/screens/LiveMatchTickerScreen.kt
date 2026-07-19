package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.League
import com.example.model.MatchEvent
import com.example.ui.theme.*

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live stats scoreboard
            Card(
                colors = CardDefaults.cardColors(containerColor = PitchDarkBg),
                border = BorderStroke(1.dp, GrassEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(userMatch.homeClubName.uppercase(), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                        Text("LOCAL", color = TextSecondary, fontSize = 10.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(userMatch.homeGoals.toString(), color = GrassEmerald, fontSize = 36.sp, fontWeight = FontWeight.Black)
                        Text("-", color = TextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(userMatch.awayGoals.toString(), color = GrassEmerald, fontSize = 36.sp, fontWeight = FontWeight.Black)
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(userMatch.awayClubName.uppercase(), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                        Text("VISITANTE", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }

            // Tactic Matrix statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Possession Bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("POSESIÓN BALÓN", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("${userMatch.possessionHome}%", color = GrassEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${userMatch.possessionAway}%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Shots bar
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DISPAROS DE ZONA", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("${userMatch.homeShots} remates", color = GrassEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${userMatch.awayShots} remates", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Live event logs / Live Ticker
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                border = BorderStroke(1.dp, DarkSteel),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("CRÓNICA DE JUEGO MINUTO A MINUTO (ZONA TÁCTICA)", color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(userMatch.events) { event ->
                            val color = when (event.type) {
                                "GOAL_HOME", "GOAL_AWAY" -> NeonAmber
                                "SHUTOUT_HERO" -> GrassEmerald
                                "YELLOW", "RED" -> Color.Red
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
                                    modifier = Modifier.width(60.dp)
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
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Simula una jornada en el botón superior para ver crónicas de partidos vivos.", color = TextSecondary, fontSize = 13.sp)
        }
    }
}
