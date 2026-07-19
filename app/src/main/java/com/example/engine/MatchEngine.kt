package com.example.engine

import com.example.model.*
import kotlin.random.Random

object MatchEngine {

    // Real Match Engine algorithm based on zone tactical comparison
    suspend fun simulateMatch(
        match: Match,
        home: Club,
        away: Club,
        visibility: LeagueVisibility
    ) {
        val random = Random
        
        // Calculate dynamic tactical rating vectors (Attack, Midfield, Defense)
        val (homeDef, homeMid, homeAtt) = home.getTeamRatings()
        val (awayDef, awayMid, awayAtt) = away.getTeamRatings()

        // Home advantage modifier (+5% general efficiency)
        val homeAdvantage = 1.05f

        if (visibility == LeagueVisibility.MAX_DETAIL) {
            // LEVEL 1: Detailed minute-by-minute tactical zone simulation
            val eventsList = mutableListOf<MatchEvent>()
            var homeGoals = 0
            var awayGoals = 0
            var homeShots = 0
            var awayShots = 0

            // Estimate possession based on midfield density
            val totalMid = (homeMid * homeAdvantage + awayMid).coerceAtLeast(1f)
            val homePossessionPercent = ((homeMid * homeAdvantage / totalMid) * 100).toInt().coerceIn(25, 75)
            val awayPossessionPercent = 100 - homePossessionPercent

            eventsList.add(MatchEvent(1, "¡Comienza el encuentro en el estadio de ${home.name}! Capacidad: ${home.stadiumCapacity} aficionados.", "INFO"))

            // Simulate match blocks (representing key tactical minutes)
            val criticalMinutes = listOf(15, 30, 45, 60, 75, 90)
            criticalMinutes.forEach { min ->
                // Decide which team dominates midfield control to initiate attack
                val attackRoll = random.nextFloat() * 100
                if (attackRoll < homePossessionPercent) {
                    // Home Attack vs Away Defense
                    homeShots++
                    val attackVal = homeAtt * homeAdvantage * random.nextDouble(0.7, 1.3)
                    val defenseVal = awayDef * random.nextDouble(0.7, 1.3)

                    if (attackVal > defenseVal) {
                        // Check if GK triggers "Hero" saving throws
                        val awayGK = away.squad.firstOrNull { it.position == Position.GK }
                        val gkPower = (awayGK?.attributes?.goalkeeper ?: 45) * random.nextDouble(0.8, 1.2)
                        
                        // Trait trigger check!
                        val hasHeroGK = awayGK?.traits?.contains(Trait.HEROE_BAJO_PALOS) == true
                        val thresholdMultiplier = if (hasHeroGK) 1.25f else 1.0f

                        if (gkPower * thresholdMultiplier > attackVal) {
                            if (hasHeroGK) {
                                eventsList.add(MatchEvent(min, "¡PARADÓN EXTRAORDINARIO de ${awayGK?.fullName}! El guardameta activa 'Héroe Bajo Palos' y bloquea el misil.", "SHUTOUT_HERO"))
                            } else {
                                eventsList.add(MatchEvent(min, "Disparo potente de ${home.squad.filter { it.position == Position.ATT }.randomOrNull()?.lastName ?: "delantero"}, pero el guardameta ataja seguro.", "INFO"))
                            }
                        } else {
                            homeGoals++
                            eventsList.add(MatchEvent(min, "⚽ ¡GOOOOOL DE ${home.name}! Magnífico remate ajustado al poste de ${home.squad.filter { it.position == Position.ATT }.randomOrNull()?.fullName ?: "delantero"}.", "GOAL_HOME"))
                        }
                    } else {
                        eventsList.add(MatchEvent(min, "Contraataque peligroso del ${home.name} cortado magníficamente por la defensa rival.", "INFO"))
                    }
                } else {
                    // Away Attack vs Home Defense
                    awayShots++
                    val attackVal = awayAtt * random.nextDouble(0.7, 1.3)
                    val defenseVal = homeDef * homeAdvantage * random.nextDouble(0.7, 1.3)

                    if (attackVal > defenseVal) {
                        val homeGK = home.squad.firstOrNull { it.position == Position.GK }
                        val gkPower = (homeGK?.attributes?.goalkeeper ?: 45) * random.nextDouble(0.8, 1.2)
                        val hasHeroGK = homeGK?.traits?.contains(Trait.HEROE_BAJO_PALOS) == true
                        val thresholdMultiplier = if (hasHeroGK) 1.25f else 1.0f

                        if (gkPower * thresholdMultiplier > attackVal) {
                            if (hasHeroGK) {
                                eventsList.add(MatchEvent(min, "¡SALVADA MONUMENTAL! El arquero local ${homeGK?.fullName} vuela bloqueando un disparo cantado gracias a su rasgo 'Héroe Bajo Palos'.", "SHUTOUT_HERO"))
                            } else {
                                eventsList.add(MatchEvent(min, "Remate de cabeza peligroso del ${away.name}, pero el arquero local desvía a córner.", "INFO"))
                            }
                        } else {
                            awayGoals++
                            eventsList.add(MatchEvent(min, "⚽ ¡GOOOOOL DE ${away.name}! Excelente conducción táctica finiquitada por ${away.squad.filter { it.position == Position.ATT }.randomOrNull()?.fullName ?: "delantero"}.", "GOAL_AWAY"))
                        }
                    } else {
                        eventsList.add(MatchEvent(min, "Balón largo del ${away.name} que se pierde por la línea de fondo por falta de coordinación.", "INFO"))
                    }
                }

                // Random yellow card check
                if (random.nextFloat() < 0.15f) {
                    val bookingTeam = if (random.nextBoolean()) home else away
                    val bookedPlayer = bookingTeam.squad.randomOrNull()?.fullName ?: "Defensor"
                    eventsList.add(MatchEvent(min, "🟨 Tarjeta amarilla para $bookedPlayer del $bookingTeam de juego fuerte.", "YELLOW"))
                }
            }

            eventsList.add(MatchEvent(90, "¡Pitido final! El colegiado decreta el término del cotejo. Marcador final: ${home.name} $homeGoals - $awayGoals ${away.name}.", "INFO"))

            // Apply results to Match Object
            match.played = true
            match.homeGoals = homeGoals
            match.awayGoals = awayGoals
            match.possessionHome = homePossessionPercent
            match.possessionAway = awayPossessionPercent
            match.homeShots = homeShots
            match.awayShots = awayShots
            match.events = eventsList

            // Apply to Club Standings
            applyMatchResultsToClubs(home, away, homeGoals, awayGoals)

        } else {
            // LEVEL 2 & 3: Fast Probability Statistical simulation (No minute-by-minute calculations)
            val homeStrength = (homeDef + homeMid + homeAtt) * homeAdvantage
            val awayStrength = (awayDef + awayMid + awayAtt).toFloat()

            val homeExpected = (homeStrength / (homeStrength + awayStrength)) * 3.0f + random.nextFloat() * 1.5f
            val awayExpected = (awayStrength / (homeStrength + awayStrength)) * 3.0f + random.nextFloat() * 1.5f

            val homeGoals = homeExpected.toInt().coerceAtLeast(0)
            val awayGoals = awayExpected.toInt().coerceAtLeast(0)

            match.played = true
            match.homeGoals = homeGoals
            match.awayGoals = awayGoals
            match.possessionHome = if (homeStrength > awayStrength) 55 else 45
            match.possessionAway = 100 - match.possessionHome

            applyMatchResultsToClubs(home, away, homeGoals, awayGoals)
        }
    }

    private fun applyMatchResultsToClubs(home: Club, away: Club, homeGoals: Int, awayGoals: Int) {
        home.played++
        away.played++
        home.goalsFor += homeGoals
        home.goalsAgainst += awayGoals
        away.goalsFor += awayGoals
        away.goalsAgainst += homeGoals

        when {
            homeGoals > awayGoals -> {
                home.wins++
                home.points += 3
                away.losses++
            }
            homeGoals < awayGoals -> {
                away.wins++
                away.points += 3
                home.losses++
            }
            else -> {
                home.draws++
                home.points += 1
                away.draws++
                away.points += 1
            }
        }

        // Simulating stamina fatigue and small injury chance on players
        home.squad.forEach { player ->
            player.energy = (player.energy - Random.nextInt(5, 12)).coerceAtLeast(15)
            // Crystals are 3x more prone to injuries
            val injuryProb = if (player.traits.contains(Trait.CUERPO_DE_CRISTAL)) 0.06f else 0.02f
            if (Random.nextFloat() < injuryProb) {
                player.isInjured = true
                player.injuryDurationWeeks = Random.nextInt(1, 4)
            }
        }
        away.squad.forEach { player ->
            player.energy = (player.energy - Random.nextInt(5, 12)).coerceAtLeast(15)
            val injuryProb = if (player.traits.contains(Trait.CUERPO_DE_CRISTAL)) 0.06f else 0.02f
            if (Random.nextFloat() < injuryProb) {
                player.isInjured = true
                player.injuryDurationWeeks = Random.nextInt(1, 4)
            }
        }
    }
}
