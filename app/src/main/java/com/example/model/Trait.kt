package com.example.model

enum class Trait(
    val displayName: String,
    val description: String,
    val speedMultiplier: Float = 1.0f,
    val mentalMultiplier: Float = 1.0f,
    val pressureMultiplier: Float = 1.0f,
    val chemistryModifier: Float = 0.0f,
    val staminaModifier: Float = 1.0f,
    val injuryProbabilityMultiplier: Float = 1.0f
) {
    VELOCISTA_NATO(
        displayName = "Velocista Nato",
        description = "Aumenta la velocidad y aceleración base en 15%.",
        speedMultiplier = 1.15f
    ),
    MENTE_DE_HIERRO(
        displayName = "Mente de Hierro",
        description = "Excelente resistencia a la presión, mejora el rendimiento bajo tensión en 20%.",
        pressureMultiplier = 1.20f,
        mentalMultiplier = 1.10f
    ),
    CUERPO_DE_CRISTAL(
        displayName = "Cuerpo de Cristal",
        description = "Tres veces más propenso a sufrir lesiones graves en colisiones físicas.",
        injuryProbabilityMultiplier = 3.0f,
        staminaModifier = 0.85f
    ),
    EGO_DE_SUPERESTRELLA(
        displayName = "Ego de Superestrella",
        description = "Aumenta un 15% el remate, pero penaliza la química del vestuario en 10% si es suplente.",
        chemistryModifier = -0.10f
    ),
    LIDER_NATO(
        displayName = "Líder Nato",
        description = "Inyecta +15% de química y moral al equipo cuando está en el campo.",
        chemistryModifier = 0.15f,
        mentalMultiplier = 1.10f
    ),
    HEROE_BAJO_PALOS(
        displayName = "Héroe Bajo Palos",
        description = "Mejora los reflejos del portero en un 20% en situaciones de alto peligro.",
        pressureMultiplier = 1.20f
    ),
    PULMON_INFINITO(
        displayName = "Pulmón Infinito",
        description = "Stamina un 25% superior. Se fatiga mucho más lento.",
        staminaModifier = 1.25f
    ),
    PECHO_FRIO(
        displayName = "Pecho Frío",
        description = "Penalización de -20% bajo presión en partidos decisivos.",
        pressureMultiplier = 0.80f
    )
}
