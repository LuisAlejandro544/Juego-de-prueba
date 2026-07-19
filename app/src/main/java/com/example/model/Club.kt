package com.example.model

import com.squareup.moshi.JsonClass
import java.util.UUID
import kotlin.random.Random

@JsonClass(generateAdapter = true)
data class Club(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val country: String,
    val budget: Long,               // Transfer and structural budget
    val wageBudget: Long,           // Weekly wage allowance
    var fanBaseSize: Long,
    var stadiumCapacity: Int,
    var ticketPrice: Int,
    var trainingFacilities: Int = 1, // 1 - 5 stars
    var youthAcademy: Int = 1,       // 1 - 5 stars
    val squad: MutableList<Player> = mutableListOf(),
    
    // Standings / Stats
    var played: Int = 0,
    var wins: Int = 0,
    var draws: Int = 0,
    var losses: Int = 0,
    var goalsFor: Int = 0,
    var goalsAgainst: Int = 0,
    var points: Int = 0
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst

    fun resetStats() {
        played = 0
        wins = 0
        draws = 0
        losses = 0
        goalsFor = 0
        goalsAgainst = 0
        points = 0
    }

    // Calculates overall defensive, midfield, and offensive ratings from squad
    fun getTeamRatings(): Triple<Int, Int, Int> {
        if (squad.isEmpty()) return Triple(30, 30, 30)
        
        val defenseScore = squad.filter { it.position == Position.DEF || it.position == Position.GK }
            .map { it.getOverallRating() }.average().let { if (it.isNaN()) 30.0 else it }
            
        val midfieldScore = squad.filter { it.position == Position.MID }
            .map { it.getOverallRating() }.average().let { if (it.isNaN()) 30.0 else it }
            
        val attackScore = squad.filter { it.position == Position.ATT }
            .map { it.getOverallRating() }.average().let { if (it.isNaN()) 30.0 else it }

        return Triple(defenseScore.toInt(), midfieldScore.toInt(), attackScore.toInt())
    }

    fun getAverageRating(): Int {
        if (squad.isEmpty()) return 30
        return squad.map { it.getOverallRating() }.average().toInt()
    }

    companion object {
        private val countryClubTemplates = mapOf(
            "ARGENTINA" to listOf(
                "Boca de Buenos Aires", "River de Núñez", "Racing Club de Avellaneda", "Estudiantes de La Plata",
                "Independiente de Avellaneda", "San Lorenzo de Almagro", "Rosario Canalla", "Córdoba Talleres",
                "Lanús del Sur", "Vélez de Liniers", "Banfield de Peña", "Colón de Santa Fe"
            ),
            "BRASIL" to listOf(
                "Flamengo de Río", "Palmeiras de Sao Paulo", "Sao Paulo Tricolor", "Corinthians Alvinegro",
                "Santos Peixe", "Grêmio de Porto Alegre", "Cruzeiro de Belo Horizonte", "Atlético Mineiro",
                "Fluminense das Laranjeiras", "Botafogo da Estrela", "Vasco da Gama da Colina", "Internacional Colorado"
            ),
            "FRANCIA" to listOf(
                "Olympique de Paris", "Marseille Phocéen", "Lyon des Lions", "Monaco Princier",
                "Lille du Nord", "Rennes Rouge-Noir", "Lens Sang-et-Or", "Nice Azur",
                "Nantes Canari", "Strasbourg d'Alsace", "Reims Couronné", "Toulouse Violet"
            ),
            "MÉXICO" to listOf(
                "Club América de Coapa", "CD Guadalajara Chivas", "Cruz Azul de México", "Tigres de Nuevo León",
                "Rayados de Monterrey", "Pumas de la UNAM", "Atlas de Guadalajara", "León FC",
                "Pachuca de Hidalgo", "Toluca Escarlata", "Santos Laguna del Norte", "Necaxa del Rayo"
            ),
            "COLOMBIA" to listOf(
                "Millonarios de Bogotá", "Atlético Nacional Verde", "Junior de Barranquilla", "América de Cali",
                "Independiente Santa Fe", "Deportivo Cali Azucarero", "Independiente Medellín", "Once Caldas de Manizales",
                "Tolima Pijao", "La Equidad Seguros", "Atlético Bucaramanga", "Deportivo Pasto"
            ),
            "CHILE" to listOf(
                "Colo-Colo Albo", "Universidad de Chile Azul", "Universidad Católica Cruzada", "Cobreloa del Desierto",
                "Unión Española de Santiago", "Audax Italiano de La Florida", "Everton de Viña", "Santiago Wanderers"
            ),
            "URUGUAY" to listOf(
                "Peñarol de Montevideo", "Nacional Uruguayo", "Defensor de Montevideo", "Danubio de la Curva",
                "Montevideo City", "Liverpool de Belvedere", "River Plate Uruguayo", "Fénix del Capurro"
            ),
            "PARAGUAY" to listOf(
                "Olimpia Decano", "Cerro Porteño de Barrio Obrero", "Libertad Gumarelo", "Guaraní Aborigen",
                "Nacional Querido", "Sol de América de Villa Elisa", "Sportivo Luqueño", "Tacuary de Asunción"
            ),
            "ECUADOR" to listOf(
                "Barcelona de Guayaquil", "LDU de Quito", "Emelec Eléctrico", "Independiente del Valle",
                "El Nacional de Quito", "Delfín de Manta", "Aucas Oriental", "Deportivo Cuenca"
            ),
            "PERÚ" to listOf(
                "Alianza de Lima", "Universitario Crema", "Sporting Cristal Celeste", "Melgar de Arequipa",
                "Cienciano Imperial", "Cusco FC", "César Vallejo de Trujillo", "Sport Boys del Callao"
            ),
            "VENEZUELA" to listOf(
                "Caracas FC", "Deportivo Táchira Aurinegro", "Zamora del Llano", "Monagas Azulgrana",
                "Estudiantes de Mérida", "Metropolitanos de Caracas", "Deportivo La Guaira", "Academia Puerto Cabello"
            ),
            "BOLIVIA" to listOf(
                "Bolívar de La Paz", "The Strongest Atigrado", "Jorge Wilstermann Aviador", "Oriente Petrolero",
                "Blooming de Santa Cruz", "Always Ready de El Alto", "Royal Pari", "Real Tomayapo"
            ),
            "COSTA RICA" to listOf(
                "Deportivo Saprissa", "Alajuelense Manuda", "Herediano Florense", "Cartaginés de Cartago",
                "San Carlos del Norte", "Puntarenas Porteño", "Santos de Guápiles", "Liberia Aurinegra"
            ),
            "PANAMÁ" to listOf(
                "Tauro de Pedregal", "Plaza Amador", "Árabe Unido de Colón", "San Francisco de Chorrera",
                "Alianza de Panamá", "CAI de La Chorrera", "Sporting San Miguelito", "Herrera FC"
            ),
            "HONDURAS" to listOf(
                "Olimpia de Tegucigalpa", "Motagua de las Águilas", "Real España de San Pedro Sula", "Marathón de San Pedro",
                "Vida de La Ceiba", "Olancho FC", "Real Sociedad de Tocoa", "Victoria Ceibeño"
            ),
            "EL SALVADOR" to listOf(
                "Alianza de San Salvador", "FAS de Santa Ana", "Águila de San Miguel", "Luis Ángel Firpo",
                "Isidro Metapán", "Santa Tecla Colina", "Platense de Zacatecoluca", "Dragón Mitológico"
            ),
            "GUATEMALA" to listOf(
                "Comunicaciones Crema", "Municipal Escarlata", "Xelajú de Quetzaltenango", "Antigua de Sacatepéquez",
                "Cobán Imperial", "Guastatoya de El Progreso", "Malacateco de San Marcos", "Achuapa del Progreso"
            ),
            "NICARAGUA" to listOf(
                "Real Estelí del Tren", "Diriangén Cacique", "Managua FC", "Walter Ferretti",
                "Ocotal del Norte", "Matagalpa Indígena", "Jalapa de las Segovias", "UNAN Managua"
            )
        )

        fun generateProcedural(country: Country, index: Int, minStars: Int = 1, maxStars: Int = 5): Club {
            val random = Random
            
            // Extract core country key without flag emojis
            val countryKey = country.name.split(" ")[0].uppercase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")

            val templates = countryClubTemplates[countryKey] ?: listOf("Real Club", "Fútbol Club", "Atlético")
            val name = if (index < templates.size) {
                templates[index]
            } else {
                "${templates.random(random)} $index"
            }

            val starPower = random.nextInt(minStars, maxStars + 1).coerceIn(1, 5)
            
            // Economy factors scale financial metrics dynamically
            val baseBudget = (starPower * starPower * 1_500_000L * country.economyFactor).toLong()
            val baseWage = (starPower * 100_000L * country.economyFactor).toLong()
            val stadiumCap = starPower * starPower * 10_000 + random.nextInt(2000, 8000)
            val fanBase = (stadiumCap * random.nextDouble(1.5, 5.0)).toLong()
            val ticketPrice = starPower * 8 + random.nextInt(5, 15)

            val club = Club(
                name = name,
                country = country.name,
                budget = baseBudget,
                wageBudget = baseWage,
                stadiumCapacity = stadiumCap,
                fanBaseSize = fanBase,
                ticketPrice = ticketPrice,
                trainingFacilities = starPower,
                youthAcademy = starPower
            )

            // Generate squad of 18 players
            val squadSize = 18
            val minRating = 30 + starPower * 10
            val maxRating = 45 + starPower * 11

            // 2 GKs, 6 DEFs, 6 MIDs, 4 ATTs
            repeat(2) { club.squad.add(Player.generateProcedural(country.name, Position.GK, minRating, maxRating)) }
            repeat(6) { club.squad.add(Player.generateProcedural(country.name, Position.DEF, minRating, maxRating)) }
            repeat(6) { club.squad.add(Player.generateProcedural(country.name, Position.MID, minRating, maxRating)) }
            repeat(4) { club.squad.add(Player.generateProcedural(country.name, Position.ATT, minRating, maxRating)) }

            return club
        }
    }
}
