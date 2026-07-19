package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Club
import com.example.model.League
import com.example.ui.theme.*

@Composable
fun ClubAndStandingsScreen(
    club: Club?,
    league: League?,
    newsLog: List<String>
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: League Standings Table (Level 1 detailed or Level 2 quick)
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
            border = BorderStroke(1.dp, DarkSteel),
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = league?.name ?: "LIGA SIN CONFIGURAR",
                    color = GrassEmerald,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSteel)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Club", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                    Text("PJ", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                    Text("PG", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                    Text("PE", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                    Text("PP", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                    Text("GF:GC", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                    Text("Pts", color = NeonAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.End)
                }

                val sortedClubs = remember(league?.clubs, league?.currentRound) {
                    league?.clubs?.sortedWith(compareByDescending<Club> { it.points }.thenByDescending { it.goalDifference }) ?: emptyList()
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(sortedClubs, key = { it.id }) { item ->
                        val isUserClub = item.id == club?.id
                        val rowBg = if (isUserClub) DarkSteel.copy(alpha = 0.5f) else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.name,
                                color = if (isUserClub) GrassEmerald else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isUserClub) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(2f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(item.played.toString(), color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                            Text(item.wins.toString(), color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                            Text(item.draws.toString(), color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                            Text(item.losses.toString(), color = TextPrimary, fontSize = 11.sp, modifier = Modifier.weight(0.4f), textAlign = TextAlign.Center)
                            Text("${item.goalsFor}:${item.goalsAgainst}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(0.7f), textAlign = TextAlign.Center)
                            Text(
                                item.points.toString(),
                                color = NeonAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }

        // Right Column: Club Stats and Live Logging
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Infrastructure Card
            if (club != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ESTRUCTURA DEPORTIVA", color = GrassEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Estadio:", color = TextSecondary, fontSize = 12.sp)
                            Text("${club.stadiumCapacity} locales (Ticket: $${club.ticketPrice})", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Entrenamiento:", color = TextSecondary, fontSize = 12.sp)
                            Text("★".repeat(club.trainingFacilities) + "☆".repeat(5 - club.trainingFacilities), color = NeonAmber, fontSize = 12.sp)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Cantera (Academia):", color = TextSecondary, fontSize = 12.sp)
                            Text("★".repeat(club.youthAcademy) + "☆".repeat(5 - club.youthAcademy), color = NeonAmber, fontSize = 12.sp)
                        }
                    }
                }
            }

            // News Ticker
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                border = BorderStroke(1.dp, DarkSteel),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("BITÁCORA DEL UNIVERSO", color = NeonAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(newsLog, key = { it.hashCode() }) { news ->
                            Text(
                                text = "• $news",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 4.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
