package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class PlayerAttributes(
    val attack: Int,      // Technical offensive
    val defense: Int,     // Technical defensive
    val midfield: Int,    // Tactical / passing
    val speed: Int,       // Speed
    val stamina: Int,     // Stamina
    val goalkeeper: Int,  // GK skill
    val mental: Int,      // Mental / pressure resistance
    val physical: Int     // Strength / physicality
)

@JsonClass(generateAdapter = true)
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val lastName: String,
    val age: Int,
    val country: String,
    val position: Position,
    val traits: List<Trait>,
    val attributes: PlayerAttributes,
    val marketValue: Long,
    val salary: Long,
    val contractYears: Int,
    var scoutingLevel: Int = 0, // 0 = Unknown, 100 = Fully Scouted
    var energy: Int = 100,      // 0 - 100%
    var moral: Int = 80,        // 0 - 100%
    var matchPerformanceLast: Float = 6.0f,
    var isInjured: Boolean = false,
    var injuryDurationWeeks: Int = 0
) {
    val fullName: String get() = "$firstName $lastName"

    // Calculates overall rating based on position, applying traits mathematically
    fun getOverallRating(): Int {
        val raw = when (position) {
            Position.GK -> (attributes.goalkeeper * 0.6 + attributes.defense * 0.2 + attributes.mental * 0.2)
            Position.DEF -> (attributes.defense * 0.6 + attributes.physical * 0.2 + attributes.speed * 0.2)
            Position.MID -> (attributes.midfield * 0.5 + attributes.attack * 0.2 + attributes.defense * 0.1 + attributes.stamina * 0.2)
            Position.ATT -> (attributes.attack * 0.6 + attributes.speed * 0.2 + attributes.mental * 0.2)
        }

        // Apply Trait Multipliers
        var multiplier = 1.0f
        traits.forEach { trait ->
            if (position == Position.ATT && trait == Trait.VELOCISTA_NATO) multiplier *= trait.speedMultiplier
            if (position == Position.GK && trait == Trait.HEROE_BAJO_PALOS) multiplier *= trait.pressureMultiplier
            if (trait == Trait.PULMON_INFINITO) multiplier *= 1.05f
            if (trait == Trait.PECHO_FRIO) multiplier *= 0.95f
        }

        return (raw * multiplier).coerceIn(10.0, 99.0).toInt()
    }

    // Returns a representation of an attribute. If scouting is low, returns a fuzzy range or "?"
    fun getScoutedAttributeString(attributeName: String, value: Int): String {
        if (scoutingLevel >= 80) return value.toString()
        if (scoutingLevel >= 40) {
            val delta = (100 - scoutingLevel) / 5
            val min = (value - delta).coerceIn(10, 99)
            val max = (value + delta).coerceIn(10, 99)
            return "$min-$max"
        }
        return "?? - ??"
    }

    companion object {
        private val frenchFirstNames = listOf(
            "Hugo", "Lucas", "Antoine", "Julien", "Nicolas", "Mathieu", "Pierre", "Jean", "Arthur", "Bastien",
            "Mael", "Raphael", "Enzo", "Louis", "Alexandre", "Thomas", "Paul", "Maxime", "Clement", "Adrien"
        )
        private val frenchLastNames = listOf(
            "Dubois", "Dupont", "Martin", "Lefebvre", "Moreau", "Laurent", "Simon", "Michel", "Leroy", "Roux",
            "David", "Bertrand", "Garnier", "Faure", "Lambert", "Aubry", "Gautier", "Morin", "Girard", "Fournier"
        )

        private val brazilianFirstNames = listOf(
            "Gabriel", "Lucas", "Matheus", "Pedro", "Thiago", "Vinicius", "Arthur", "Felipe", "Gustavo", "Rodrigo",
            "Douglas", "Ronaldo", "Adriano", "Neymar", "Bruno", "Marcelo", "Diego", "Alisson", "Richarlison", "Igor"
        )
        private val brazilianLastNames = listOf(
            "Silva", "Santos", "Souza", "Oliveira", "Pereira", "Lima", "Carvalho", "Ferreira", "Ribeiro", "Almeida",
            "Costa", "Gomes", "Alves", "Rocha", "Cardoso", "Rodrigues", "Martins", "Teixeira", "Barbosa", "Moreira"
        )

        private val spanishFirstNames = listOf(
            "Santiago", "Mateo", "Juan", "Lucas", "Matías", "Nicolás", "Alejandro", "Diego", "Felipe", "Carlos",
            "Emiliano", "Gabriel", "Lautaro", "Enzo", "Federico", "Luis", "Jorge", "Francisco", "Andrés", "Sebastián",
            "Tomás", "Facundo", "Ignacio", "Ezequiel", "Gonzalo", "Álvaro", "Rodrigo", "Manuel", "Agustín", "Joaquín"
        )
        private val spanishLastNames = listOf(
            "González", "Rodríguez", "Gómez", "Fernández", "López", "Díaz", "Martínez", "Pérez", "Romero", "Sánchez",
            "Álvarez", "Cardozo", "Silva", "Medina", "Torres", "Suárez", "Gutiérrez", "Vidal", "Mendoza", "Castillo",
            "Ortiz", "Paz", "Rojas", "Herrera", "Castro", "Cáceres", "Bustos", "Vargas", "Benítez", "Morales"
        )

        fun generateProcedural(
            country: String,
            pos: Position? = null,
            minRating: Int = 50,
            maxRating: Int = 85
        ): Player {
            val random = Random
            
            // Localized name resolution
            val countryUpper = country.uppercase()
            val (fName, lName) = when {
                countryUpper.contains("FRANCIA") || countryUpper.contains("FRANCE") -> {
                    frenchFirstNames.random(random) to frenchLastNames.random(random)
                }
                countryUpper.contains("BRASIL") || countryUpper.contains("BRAZIL") -> {
                    brazilianFirstNames.random(random) to brazilianLastNames.random(random)
                }
                else -> {
                    spanishFirstNames.random(random) to spanishLastNames.random(random)
                }
            }

            val age = random.nextInt(16, 38)
            val position = pos ?: Position.values().random(random)

            // Random traits assignment
            val traits = mutableListOf<Trait>()
            if (random.nextFloat() < 0.25f) {
                traits.add(Trait.values().random(random))
            }
            if (random.nextFloat() < 0.05f && traits.isNotEmpty()) { // Rare second trait
                val second = Trait.values().random(random)
                if (second != traits[0]) traits.add(second)
            }

            // Generate stats based on target ratings
            val targetBase = random.nextInt(minRating, maxRating).coerceIn(20, 95)
            val attack = if (position == Position.ATT) targetBase + random.nextInt(5, 15) else targetBase - random.nextInt(10, 25)
            val defense = if (position == Position.DEF) targetBase + random.nextInt(5, 15) else targetBase - random.nextInt(10, 25)
            val midfield = if (position == Position.MID) targetBase + random.nextInt(5, 15) else targetBase - random.nextInt(10, 20)
            val goalkeeper = if (position == Position.GK) targetBase + random.nextInt(10, 25) else random.nextInt(5, 15)
            
            val speed = targetBase + random.nextInt(-10, 15)
            val stamina = targetBase + random.nextInt(-10, 15)
            val mental = targetBase + random.nextInt(-15, 15)
            val physical = targetBase + random.nextInt(-10, 15)

            val attrs = PlayerAttributes(
                attack = attack.coerceIn(10, 99),
                defense = defense.coerceIn(10, 99),
                midfield = midfield.coerceIn(10, 99),
                speed = speed.coerceIn(10, 99),
                stamina = stamina.coerceIn(10, 99),
                goalkeeper = goalkeeper.coerceIn(10, 99),
                mental = mental.coerceIn(10, 99),
                physical = physical.coerceIn(10, 99)
            )

            // Value & Salary calculation
            val overall = (targetBase + 5).coerceIn(30, 99)
            val valueFactor = Math.pow(overall.toDouble() / 50.0, 4.0)
            val marketValue = (valueFactor * 500_000 * random.nextDouble(0.8, 1.2)).toLong().coerceAtLeast(10_000L)
            val salary = (marketValue / 52 * random.nextDouble(0.9, 1.1)).toLong().coerceAtLeast(500L) // Weekly salary
            val contractYears = random.nextInt(1, 5)

            return Player(
                firstName = fName,
                lastName = lName,
                age = age,
                country = country,
                position = position,
                traits = traits,
                attributes = attrs,
                marketValue = (marketValue / 1000) * 1000, // Round to nearest thousand
                salary = (salary / 10) * 10,
                contractYears = contractYears,
                scoutingLevel = random.nextInt(0, 101) // Fully random scouting for initial gen
            )
        }
    }
}
