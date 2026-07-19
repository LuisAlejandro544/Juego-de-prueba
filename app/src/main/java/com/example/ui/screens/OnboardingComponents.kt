package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun EnterNameScreen(
    inputName: String,
    onValueChange: (String) -> Unit,
    isSimulating: Boolean,
    onInitializeUniverse: () -> Unit,
    onLoadGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDarkBg)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
            border = BorderStroke(1.dp, GrassEmerald),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsFootball,
                    contentDescription = null,
                    tint = GrassEmerald,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "FEDEBOL MANAGER",
                    color = GrassEmerald,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Simulador de gestión de fútbol latinoamericano y mundial procedural hiperrealista.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(color = DarkSteel)

                OutlinedTextField(
                    value = inputName,
                    onValueChange = onValueChange,
                    label = { Text("Nombre del Mánager", color = GrassEmerald) },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GrassEmerald,
                        unfocusedBorderColor = DarkSteel,
                        focusedLabelColor = GrassEmerald,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = GrassEmerald
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manager_name_input")
                )

                Button(
                    onClick = onInitializeUniverse,
                    enabled = inputName.trim().isNotEmpty() && !isSimulating,
                    colors = ButtonDefaults.buttonColors(containerColor = GrassEmerald, contentColor = PitchDarkBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("init_universe_btn")
                ) {
                    if (isSimulating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = PitchDarkBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CREANDO MATRICES...", fontWeight = FontWeight.Bold, color = PitchDarkBg)
                    } else {
                        Text("CREAR UNIVERSO PROCEDURAL", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }

                TextButton(
                    onClick = onLoadGame,
                    modifier = Modifier.testTag("load_game_btn")
                ) {
                    Text("Cargar partida guardada de mánager", color = NeonAmber, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun UniverseSelectionBrowser(
    manager: Manager,
    countries: List<Country>,
    clubs: List<Club>,
    selectedCountry: Country?,
    onSelectCountry: (Country) -> Unit,
    selectedClub: Club?,
    onSelectClub: (Club) -> Unit,
    isSimulating: Boolean,
    onFundClubClick: () -> Unit,
    onChooseClubAndStart: () -> Unit,
    onSelectPlayerForDetail: (Player) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PitchDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "SELECCIÓN DE PROYECTO DEPORTIVO",
                        color = GrassEmerald,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "Mánager: ${manager.name} • Elige una institución para dirigir o funda un club nuevo",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onFundClubClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = PitchDarkBg),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("FUNDAR CLUB DESDE CERO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onChooseClubAndStart,
                        enabled = selectedClub != null && !isSimulating,
                        colors = ButtonDefaults.buttonColors(containerColor = GrassEmerald, contentColor = PitchDarkBg),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        if (isSimulating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = PitchDarkBg)
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ELEGIR ESTE CLUB Y EMPEZAR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            HorizontalDivider(color = DarkSteel)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // COLUMN 1: Countries
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "1. PAÍSES DISPONIBLES",
                            color = GrassEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(countries) { country ->
                                val isSelected = selectedCountry?.name == country.name
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) DarkSteel else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) GrassEmerald else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onSelectCountry(country) }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        country.name,
                                        color = if (isSelected) GrassEmerald else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Prestigio: ${country.selectionPower}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // COLUMN 2: Clubs
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier
                        .weight(1.8f)
                        .fillMaxHeight()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "2. CLUBES EN LA LIGA",
                            color = GrassEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        selectedCountry?.let { country ->
                            val countryClubs = clubs.filter { it.country == country.name }
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(countryClubs) { club ->
                                    val isSelected = selectedClub?.id == club.id
                                    val ratings = club.getTeamRatings()
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) DarkSteel else Color.Transparent)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) NeonAmber else DarkSteel,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { onSelectClub(club) }
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                club.name,
                                                color = if (isSelected) NeonAmber else TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                "Presupuesto: $${club.budget / 1_000_000}M",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("ATA: ${ratings.third}", color = Color(0xFFFF4D4D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("MED: ${ratings.second}", color = Color(0xFF00FF88), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("DEF: ${ratings.first}", color = Color(0xFF3399FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text("🏟️ ${club.stadiumCapacity} cap", color = TextSecondary, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        } ?: Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selecciona un país para ver sus ligas", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                // COLUMN 3: Squad & Player Details
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
                    border = BorderStroke(1.dp, DarkSteel),
                    modifier = Modifier
                        .weight(2.0f)
                        .fillMaxHeight()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "3. PLANTILLA Y ESTADÍSTICAS",
                            color = GrassEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        selectedClub?.let { club ->
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(club.squad) { player ->
                                    val posColor = when (player.position) {
                                        Position.GK -> Color(0xFFFFD700)
                                        Position.DEF -> Color(0xFF3399FF)
                                        Position.MID -> Color(0xFF00FF88)
                                        Position.ATT -> Color(0xFFFF4D4D)
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF131D31))
                                            .clickable { onSelectPlayerForDetail(player) }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 36.dp, height = 20.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(posColor.copy(alpha = 0.15f))
                                                .border(1.dp, posColor, RoundedCornerShape(4.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                player.position.name,
                                                color = posColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            player.fullName,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            "EDAD: ${player.age}",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )

                                        Text(
                                            "VAL: ${player.getOverallRating()}",
                                            color = GrassEmerald,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        } ?: Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selecciona un club para auditar su plantel", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerDetailsDialog(
    player: Player,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
            border = BorderStroke(1.dp, NeonAmber),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(400.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        player.fullName,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "RAT: ${player.getOverallRating()}",
                        color = NeonAmber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    "Posición: ${player.position.name} • Edad: ${player.age} años",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                HorizontalDivider(color = DarkSteel)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ataque", color = TextSecondary, fontSize = 12.sp)
                        Text(player.attributes.attack.toString(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Defensa", color = TextSecondary, fontSize = 12.sp)
                        Text(player.attributes.defense.toString(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mediocampo", color = TextSecondary, fontSize = 12.sp)
                        Text(player.attributes.midfield.toString(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Físico", color = TextSecondary, fontSize = 12.sp)
                        Text(player.attributes.physical.toString(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Velocidad", color = TextSecondary, fontSize = 12.sp)
                        Text(player.attributes.speed.toString(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Arquero", color = TextSecondary, fontSize = 12.sp)
                        Text(player.attributes.goalkeeper.toString(), color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (player.traits.isNotEmpty()) {
                    HorizontalDivider(color = DarkSteel)
                    Text("Rasgos Especiales:", color = GrassEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    player.traits.forEach { trait ->
                        Text("✨ ${trait.displayName}: ${trait.description}", color = TextSecondary, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cerrar", color = GrassEmerald)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomClubCreationDialog(
    initialClubName: String,
    countryName: String?,
    onDismissRequest: () -> Unit,
    onFundClub: (name: String, stadiumCapacity: Int, budget: Long) -> Unit
) {
    var name by remember { mutableStateOf(initialClubName) }
    var capacity by remember { mutableFloatStateOf(25000f) }
    var budgetValue by remember { mutableFloatStateOf(10_000_000f) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
            border = BorderStroke(2.dp, GrassEmerald),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(420.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "FUNDAR NUEVO CLUB DEPORTIVO",
                    color = GrassEmerald,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la Institución", color = GrassEmerald) },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = GrassEmerald,
                        unfocusedBorderColor = DarkSteel,
                        focusedLabelColor = GrassEmerald,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = GrassEmerald
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "País afiliado: ${countryName ?: "Ninguno seleccionado"}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Column {
                    Text(
                        "Capacidad del Estadio: ${capacity.toInt()} espectadores",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = capacity,
                        onValueChange = { capacity = it },
                        valueRange = 10000f..80000f,
                        colors = SliderDefaults.colors(
                            thumbColor = GrassEmerald,
                            activeTrackColor = GrassEmerald,
                            inactiveTrackColor = DarkSteel
                        )
                    )
                }

                Column {
                    Text(
                        "Presupuesto de Arranque: $${(budgetValue / 1_000_000).toInt()}M USD",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Slider(
                        value = budgetValue,
                        onValueChange = { budgetValue = it },
                        valueRange = 5000000f..40000000f,
                        colors = SliderDefaults.colors(
                            thumbColor = GrassEmerald,
                            activeTrackColor = GrassEmerald,
                            inactiveTrackColor = DarkSteel
                        )
                    )
                }

                HorizontalDivider(color = DarkSteel)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onFundClub(name.trim(), capacity.toInt(), budgetValue.toLong())
                            }
                        },
                        enabled = name.trim().isNotEmpty() && countryName != null,
                        colors = ButtonDefaults.buttonColors(containerColor = GrassEmerald, contentColor = PitchDarkBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("FUNDAR CLUB", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
