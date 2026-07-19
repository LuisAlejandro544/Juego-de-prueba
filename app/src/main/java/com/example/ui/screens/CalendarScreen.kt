package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Club
import com.example.model.League
import com.example.model.Manager
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun CalendarScreen(
    currentDate: LocalDate,
    manager: Manager,
    ligas: List<League>,
    clubs: List<Club>
) {
    var selectedViewType by remember { mutableStateOf("mine") } // "mine", "league", "intl"
    
    // Find manager's active league
    val userClubId = manager.currentClubId
    val userLeague = ligas.firstOrNull { it.clubs.any { c -> c.id == userClubId } }
    
    // Selected club state for inspection
    var selectedClubForCalendar by remember(userClubId) { mutableStateOf(clubs.firstOrNull { it.id == userClubId }) }
    var selectedLeagueForCalendar by remember(userLeague) { mutableStateOf(userLeague) }
    
    // Filter/populate selected club lists depending on type
    val displayedMatches = remember(selectedClubForCalendar, selectedLeagueForCalendar) {
        if (selectedClubForCalendar == null || selectedLeagueForCalendar == null) emptyList()
        else {
            selectedLeagueForCalendar!!.fixtures.flatMap { it.matches }.filter {
                it.homeClubId == selectedClubForCalendar!!.id || it.awayClubId == selectedClubForCalendar!!.id
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Card with Dynamic Date Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                border = BorderStroke(1.dp, DarkSteel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "PLANIFICADOR TEMPORAL",
                            color = GlacierBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatLocalDateToSpanish(currentDate).uppercase(),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Temporada 2025 activa | Partidos de liga semanales",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(GlacierBlue.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, GlacierBlue, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "AÑO 2025",
                            color = GlacierBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Dropdown or Tab Row for Selection Category
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        selectedViewType = "mine"
                        selectedClubForCalendar = clubs.firstOrNull { it.id == userClubId }
                        selectedLeagueForCalendar = userLeague
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedViewType == "mine") GlacierBlue else DarkSteel,
                        contentColor = if (selectedViewType == "mine") PitchDarkBg else TextPrimary
                    ),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Text("Mi Calendario", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        selectedViewType = "league"
                        // Default to first other club in league
                        val otherClub = userLeague?.clubs?.firstOrNull { it.id != userClubId }
                        selectedClubForCalendar = otherClub
                        selectedLeagueForCalendar = userLeague
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedViewType == "league") GlacierBlue else DarkSteel,
                        contentColor = if (selectedViewType == "league") PitchDarkBg else TextPrimary
                    ),
                    modifier = Modifier.weight(1.5f).height(40.dp)
                ) {
                    Text("Otras Rivalidades", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        selectedViewType = "intl"
                        // Find first club outside user league
                        val intlLeague = ligas.firstOrNull { it.id != userLeague?.id }
                        val intlClub = intlLeague?.clubs?.firstOrNull()
                        selectedClubForCalendar = intlClub
                        selectedLeagueForCalendar = intlLeague
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedViewType == "intl") GlacierBlue else DarkSteel,
                        contentColor = if (selectedViewType == "intl") PitchDarkBg else TextPrimary
                    ),
                    modifier = Modifier.weight(1.5f).height(40.dp)
                ) {
                    Text("Internacionales", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Sub-selector menus for fine-grained club selection
        if (selectedViewType == "league" && userLeague != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Selecciona un rival de tu liga nacional para auditar su fixture:", color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(userLeague.clubs) { cl ->
                                val isSel = selectedClubForCalendar?.id == cl.id
                                Box(
                                    modifier = Modifier
                                        .background(if (isSel) GlacierBlue.copy(alpha = 0.2f) else PitchDarkBg, RoundedCornerShape(6.dp))
                                        .border(1.dp, if (isSel) GlacierBlue else DarkSteel, RoundedCornerShape(6.dp))
                                        .clickable { selectedClubForCalendar = cl }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cl.name,
                                        color = if (isSel) GlacierBlue else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedViewType == "intl") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Filtrar por Liga y Club Internacional:", color = TextSecondary, fontSize = 11.sp)
                        
                        // League Row Selector
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ligas) { lg ->
                                val isSel = selectedLeagueForCalendar?.id == lg.id
                                Box(
                                    modifier = Modifier
                                        .background(if (isSel) GlacierBlue.copy(alpha = 0.2f) else PitchDarkBg, RoundedCornerShape(6.dp))
                                        .border(1.dp, if (isSel) GlacierBlue else DarkSteel, RoundedCornerShape(6.dp))
                                        .clickable {
                                            selectedLeagueForCalendar = lg
                                            selectedClubForCalendar = lg.clubs.firstOrNull()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = lg.name.replace("Liga Profesional de ", ""),
                                        color = if (isSel) GlacierBlue else TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Club Row Selector under that league
                        selectedLeagueForCalendar?.let { lg ->
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(lg.clubs) { cl ->
                                    val isSel = selectedClubForCalendar?.id == cl.id
                                    Box(
                                        modifier = Modifier
                                            .background(if (isSel) GlacierBlue.copy(alpha = 0.2f) else PitchDarkBg, RoundedCornerShape(6.dp))
                                            .border(1.dp, if (isSel) GlacierBlue else DarkSteel, RoundedCornerShape(6.dp))
                                            .clickable { selectedClubForCalendar = cl }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = cl.name,
                                            color = if (isSel) GlacierBlue else TextPrimary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Timeline Schedule Output
        item {
            Text(
                text = "FIXTURE DE COMPETICIÓN: ${selectedClubForCalendar?.name?.uppercase() ?: "SIN SELECCIÓN"}",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (displayedMatches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay partidos programados para este club.", color = TextSecondary)
                }
            }
        } else {
            val userLgCurrentRound = selectedLeagueForCalendar?.currentRound ?: 0
            
            items(displayedMatches.size) { index ->
                val match = displayedMatches[index]
                val roundNumber = index + 1
                val matchDate = getDateForRound(roundNumber)
                val isNextMatch = roundNumber == userLgCurrentRound + 1
                
                CalendarMatchRow(
                    match = match,
                    roundNumber = roundNumber,
                    matchDate = matchDate,
                    isNextMatch = isNextMatch,
                    selectedClubForCalendar = selectedClubForCalendar
                )
            }
        }
    }
}
