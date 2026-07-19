package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Club
import com.example.model.Manager
import com.example.ui.screens.formatLocalDateToSpanish
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun HeaderBar(
    manager: Manager,
    club: Club?,
    isSimulating: Boolean,
    currentDate: LocalDate,
    onSimulateClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCarbon),
        border = BorderStroke(1.dp, DarkSteel),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "MÁNAGER: ${manager.name.uppercase()}",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Licencia: ${manager.license} | Reputación: ${manager.reputation}/100",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            if (club != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = club.name.uppercase(),
                        color = GlacierBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Balance: $${String.format("%,d", club.budget)}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "FECHA ACTUAL",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatLocalDateToSpanish(currentDate).uppercase(),
                        color = GlacierBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = onSimulateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GlacierBlue, contentColor = PitchDarkBg),
                    enabled = !isSimulating,
                    modifier = Modifier
                        .testTag("simulate_button")
                        .height(44.dp)
                ) {
                    if (isSimulating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PitchDarkBg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculando...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Siguiente Jornada")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("SIMULAR JORNADA", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
