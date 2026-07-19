package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Club
import com.example.model.Player
import com.example.model.Position
import com.example.ui.theme.*

@Composable
fun SquadScreen(
    club: Club?,
    selectedPlayer: Player?,
    onPlayerClick: (Player) -> Unit
) {
    if (club == null) return

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left pane: Squad Roster
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
            border = BorderStroke(1.dp, DarkSteel),
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "PLANTILLA PROFESIONAL (SQUAD)",
                    color = GrassEmerald,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(club.squad, key = { it.id }) { player ->
                        val isSelected = selectedPlayer?.id == player.id
                        val cardBg = if (isSelected) DarkSteel else SurfaceCarbon.copy(alpha = 0.5f)
                        val borderCol = if (isSelected) GrassEmerald else DarkSteel

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .background(cardBg)
                                .border(1.dp, borderCol, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .clickable { onPlayerClick(player) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val badgeBg = when (player.position) {
                                        Position.GK -> PositionOrangeGK
                                        Position.DEF -> StatusBlue
                                        Position.MID -> StatusTeal
                                        Position.ATT -> StatusRed
                                    }
                                    Text(
                                        text = player.position.name,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = player.fullName,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Edad: ${player.age} | Energía: ${player.energy}% | Moral: ${player.moral}%",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            
                            // Visual display of overall rating
                            Text(
                                text = player.getOverallRating().toString(),
                                color = if (player.getOverallRating() >= 80) NeonAmber else GrassEmerald,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Right pane: Player Detailed Scouting Report & Attributes
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
            border = BorderStroke(1.dp, DarkSteel),
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
        ) {
            if (selectedPlayer != null) {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text(selectedPlayer.fullName.uppercase(), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Nacionalidad: ${selectedPlayer.country} | Age: ${selectedPlayer.age}", color = TextSecondary, fontSize = 12.sp)
                            }
                            Text(
                                text = "OVR: ${selectedPlayer.getOverallRating()}",
                                color = NeonAmber,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        HorizontalDivider(color = DarkSteel, modifier = Modifier.padding(vertical = 10.dp))

                        Text("REPORTE DE OJEO (Descubierto: ${selectedPlayer.scoutingLevel}%)", color = GrassEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Attributes matrix
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AttributeRow("Ataque / Remate", selectedPlayer.getScoutedAttributeString("attack", selectedPlayer.attributes.attack))
                            AttributeRow("Defensa / Marcaje", selectedPlayer.getScoutedAttributeString("defense", selectedPlayer.attributes.defense))
                            AttributeRow("Mediocampo / Pase", selectedPlayer.getScoutedAttributeString("midfield", selectedPlayer.attributes.midfield))
                            AttributeRow("Velocidad Base", selectedPlayer.getScoutedAttributeString("speed", selectedPlayer.attributes.speed))
                            AttributeRow("Fuerza Física", selectedPlayer.getScoutedAttributeString("physical", selectedPlayer.attributes.physical))
                            AttributeRow("Estilo Mental", selectedPlayer.getScoutedAttributeString("mental", selectedPlayer.attributes.mental))
                            AttributeRow("Habilidad Arquero", selectedPlayer.getScoutedAttributeString("goalkeeper", selectedPlayer.attributes.goalkeeper))
                        }
                        HorizontalDivider(color = DarkSteel, modifier = Modifier.padding(vertical = 10.dp))
                    }

                    // Inmutable traits
                    item {
                        Text("RASGOS PSICOLÓGICOS Y FÍSICOS", color = NeonAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (selectedPlayer.traits.isEmpty()) {
                            Text("Ningún rasgo especial detectado.", color = TextSecondary, fontSize = 12.sp)
                        } else {
                            selectedPlayer.traits.forEach { trait ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = PitchDarkBg),
                                    border = BorderStroke(1.dp, DarkSteel),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(trait.displayName, color = GrassEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(trait.description, color = TextPrimary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = DarkSteel, modifier = Modifier.padding(vertical = 10.dp))
                    }

                    // Contract
                    item {
                        Text("CONDICIONES CONTRACTUALES", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Valor de Mercado:", color = TextSecondary, fontSize = 12.sp)
                            Text("$${String.format("%,d", selectedPlayer.marketValue)}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Salario Semanal:", color = TextSecondary, fontSize = 12.sp)
                            Text("$${String.format("%,d", selectedPlayer.salary)}/sem", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SportsFootball, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Selecciona un futbolista para auditar su perfil técnico", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AttributeRow(label: String, valStr: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(
            text = valStr,
            color = if (valStr.contains("-") || valStr.contains("?")) TextSecondary else GrassEmerald,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
