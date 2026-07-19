# DOCUMENTO DE ESTRUCTURA Y ARQUITECTURA TÉCNICA: FEDEBOL MANAGER

Este documento describe la arquitectura de software, los patrones de diseño, el modelo de datos y el flujo operativo del simulador de gestión de fútbol FEDEBOL Manager (antes FAFI). Está diseñado para proporcionar una comprensión clara y profunda de cómo interactúan los componentes en el sistema.

---

## 🎨 1. ARQUITECTURA Y DISEÑO GENERAL

FEDEBOL Manager sigue el patrón arquitectónico **MVVM (Model-View-ViewModel)** combinado con principios de **Clean Architecture** para asegurar la modularidad, facilidad de prueba y escalabilidad.

```text
┌────────────────────────────────────────────────────────┐
│                        VIEW                            │
│   (MainActivity, MainDashboard, Jetpack Compose UI)   │
│   - Delegación en pantallas modulares específicas      │
│     en com.example.ui.screens.* para cada pestaña.     │
└───────────────────────────┬────────────────────────────┘
                            │  Observa estados
                            ▼
┌────────────────────────────────────────────────────────┐
│                     VIEWMODEL                          │
│   (GameEngine expone StateFlows a nivel de UI,        │
│    delegando lógica pesada a MatchEngine)              │
└───────────────────────────┬────────────────────────────┘
                            │  Modifica / Coordina
                            ▼
┌────────────────────────────────────────────────────────┐
│                        MODEL                           │
│   (Player, Club, League, Manager, LocalStorage)        │
│   - Modelos desacoplados e independientes.             │
│   - CryptoHelper aisla la seguridad de guardado.      │
└────────────────────────────────────────────────────────┘
```

---

## 💻 2. STACK TECNOLÓGICO Y COMPONENTES CLAVE

1.  **UI & Presentación:** **Jetpack Compose** bajo el sistema de diseño **Material Design 3 (M3)**. Uso estricto de colores adaptativos relajantes (**Zafiro Profundo** y **Azul Glaciar**), tipografía limpia (`FontFamily.Monospace` para métricas técnicas) y paddings generosos basados en una grilla de 8dp. Las pantallas de la interfaz están completamente modularizadas en el paquete `com.example.ui.screens`.
2.  **Lógica del Simulador (Engine):** Kotlin Coroutines y `StateFlow`/`SharedFlow` para actualizaciones reactivas. El motor de juego se compone de:
    *   `GameEngine`: Orquestador principal y ViewModel del estado de la simulación.
    *   `MatchEngine`: Clase de utilidad pura dedicada exclusivamente a la simulación matemática y táctica por zonas de los partidos, resolución de lesiones y generación de narración minutada.
3.  **Persistencia Local:** Serialización asíncrona mediante **Moshi / Kotlinx Serialization** y lecturas asíncronas estructuradas:
    *   `GameStorage`: Gestor de lectura y escritura estructurada por lotes.
    *   `CryptoHelper`: Módulo seguro que encapsula las operaciones de cifrado/descifrado simétrico AES con relleno PKCS5.
4.  **Inyección de Dependencias:** Constructor Injection simple para mantener el codebase desacoplado y ligero.

---

## 📂 3. ESTRUCTURA DE PAQUETES (MODULARIZADA)

La estructura física de los archivos fuentes ha sido rediseñada para respetar el principio de responsabilidad única:

```text
/app/src/main/java/com/example/
│
├── engine/                       # --- LÓGICA DE SIMULACIÓN Y MOTOR ---
│   ├── GameEngine.kt             # Orquestador del ciclo de juego (ViewModel centralizado)
│   ├── CareerManager.kt          # Gestor modular de licencias, finanzas, campañas y convocatorias nacionales
│   ├── SocialFeedManager.kt      # Gestor modular de microblogging, decisiones de crisis y redes
│   ├── MatchEngine.kt            # Algoritmos tácticos de simulación de partidos
│   ├── GameStorage.kt            # Serializador asíncrono y gestor de datos
│   └── CryptoHelper.kt           # Encriptación AES de las partidas guardadas
│
├── model/                        # --- ENTIDADES DEL DOMINIO ---
│   ├── Player.kt                 # Datos individuales de futbolistas
│   ├── Position.kt               # Enum con posiciones tácticas (GK, DEF, MID, ATT)
│   ├── Trait.kt                  # Rasgos de conducta y físicos inmutables
│   ├── Club.kt                   # Datos financieros, plantel e infraestructura
│   ├── Country.kt                # Factores de cantera y multiplicadores macroeconómicos
│   ├── League.kt                 # Liga, Fixtures y tablas generales
│   ├── Manager.kt                # Avatar, finanzas personales y reputación
│   ├── FAFI.kt                   # Gabinete de IA Política, Elecciones de FEDEBOL y Confederaciones
│   ├── SocialFeed.kt             # Posts de microblogging reactivos
│   └── SocialFeedGenerator.kt    # Generador procedural de contenido de red social
│
└── ui/                           # --- COMPONENTES DE INTERFAZ DE USUARIO ---
    ├── MainActivity.kt           # Punto de entrada ultraligero del ciclo de vida
    ├── MainDashboard.kt          # Layout principal landscape dividido
    ├── DashboardTab.kt           # Definición de pestañas de navegación
    │
    ├── theme/                    # Paleta de colores, tipografías y MaterialTheme 3
    │   ├── Color.kt
    │   ├── Type.kt
    │   └── Theme.kt
    │
    ├── components/               # Componentes UI desacoplados y reutilizables
    │   ├── NavigationSidebar.kt  # Panel lateral izquierdo de navegación
    │   └── HeaderBar.kt          # Panel superior con estado y botón simular
    │
    └── screens/                  # Pestañas y pantallas de juego modularizadas
        ├── OnboardingScreen.kt        # Flujo de onboarding, fundar o elegir club
        ├── ClubAndStandingsScreen.kt  # Pizarra deportiva y bitácora del universo
        ├── SquadScreen.kt             # Plantilla y panel de ojeo de futbolistas
        ├── CalendarScreen.kt          # Calendario dinámico, filtros y fixtures locales/intls
        ├── LiveMatchTickerScreen.kt   # Simulador de partidos en vivo minuto a minuto
        ├── SocialFeedScreen.kt        # Microblogging y widgets de toma de decisiones de crisis
        ├── ManagerCareerScreen.kt     # Finanzas personales, licencias y contratación de agentes
        └── FafiFederationScreen.kt    # Gabinete de FEDEBOL, Convocatorias (Preview) y Confederaciones (SUDAMBOL, EUROBOL, NORAMBOL)
```

---

## 🗄️ 4. MODELO DE DATOS Y RELACIONES

```text
  ┌─────────────────┐             ┌─────────────────┐
  │     Country     │             │     League      │
  │─────────────────│             │─────────────────│
  │ name: String    │             │ name: String    │
  │ economyFactor   │◄───────────┼│ country: String │
  │ academyFactor   │             │ clubs: List     │
  └─────────────────┘             └────────┬────────┘
                                           │ Contiene 6
                                           ▼
  ┌─────────────────┐             ┌─────────────────┐
  │     Player      │             │      Club       │
  │─────────────────│             │─────────────────│
  │ id: String      │             │ id: String      │
  │ fullName: String│             │ name: String    │
  │ position: Enum  │◄────────────│ country: String │
  │ attributes: Obj │ Contiene 18 │ budget: Long    │
  │ traits: List    │             │ squad: List     │
  └─────────────────┘             └─────────────────┘
```

### Entidades Principales

*   **Country (País):** Define las constantes económicas y de cantera (`economyFactor`, `academyFactor`, `selectionPower`) que alteran proporcionalmente el valor de los futbolistas y los presupuestos de los clubes.
*   **Club (Club):** Representa una institución deportiva. Contiene presupuestos financieros (`budget`, `wageBudget`), tamaño de la afición, capacidad del estadio, precio del boleto, nivel de infraestructura y una plantilla activa de exactamente 18 jugadores.
*   **Player (Futbolista):** Entidad individual con nombre procedural localizado según país. Cuenta con edad, posición (`GK`, `DEF`, `MID`, `ATT`), atributos numéricos específicos, potencial de desarrollo y rasgos inmutables de comportamiento.
*   **League (Liga):** Agrupa los clubes de un país, controla el calendario de partidos ida/vuelta generados proceduralmente y almacena la tabla de posiciones dinámicas.
*   **Manager (Mánager):** Representa el avatar del usuario. Almacena su nombre, club actual, licencias, reputación y su riqueza personal acumulada (`personalWealth`).
*   **Calendar (Calendario/Planificador):** Representado dinámicamente mediante `java.time.LocalDate` en el motor del juego. Inicializado en el **1 de enero de 2025** y actualizado semanalmente en saltos de 7 días con cada jornada de juego.

---

## 🔄 4. DIAGRAMA DE FLUJO: EXPERIENCIA DEL USUARIO

```text
[Inicio App]
     │
     ▼
[Paso 1: Onboarding - Ingreso del Nombre del Mánager]
     │
     ▼
[Paso 2: Generación Procedural del Universo] (Se crean Países, Ligas, Clubes y Jugadores)
     │
     ▼
[Paso 3: Selección de Proyecto o Creación Directa (Fundar Club desde cero)]
     │
     ▼
[Paso 4: Inicialización del Mánager, Calendario en 2025-01-01 y Guardado de Datos Seguro]
     │
     ▼
┌───►[Paso 5: Pantalla Principal (Main Dashboard) / Navegación por Tabs]
│    ├── Tab Inicio (Resumen de plantilla, finanzas, red social interactiva)
│    ├── Tab Plantilla (Lista completa de futbolistas y rasgos)
│    ├── Tab Calendario (Auditoría del fixture completo de mi club, rivales o ligas internacionales)
│    ├── Tab Liga (Tabla general de posiciones actualizada tras simular fechas)
│    └── Tab Ajustes (Guardado, carga, estadísticas acumuladas y reinicio de partida)
│          │
│          ▼
└───[Ejecutar Simulación de Fecha / Avanzar Jornada] (Calcula partidos, suma +7 días al Calendario, guarda asíncronamente en disco)
```

---

## 🎯 5. DECISIONES DE DISEÑO CLAVE

*   **Paleta Confortable de Contraste Alto (Glacier Blue):** Descarte de la anterior paleta verde que causaba cansancio visual. Se adoptó el color **Zafiro Profundo** para el fondo (`SurfaceCarbon` y `PitchDarkBg`) y el **Azul Glaciar** como acento primario y marcador de fecha, garantizando sesiones de desarrollo y testeo descansadas.
*   **Avance Temporal Realista:** Vinculación de `LocalDate` con el simulador de ligas. Los partidos de ida y vuelta de cada jornada de liga ocurren exactamente a los `(currentRound) * 7` días transcurridos de la temporada que se inicia a principios del 2025.
*   **Gestión de Visibilidad de Liga:** Para mitigar el costo computacional, solo la liga activa donde compite el mánager se calcula bajo `LeagueVisibility.MAX_DETAIL` (genera narración minuto a minuto de partidos, posts sociales pormenorizados y lesiones). Las demás se resuelven de forma matemática rápida (`ZERO_DETAIL`).
*   **Nombre Procedural Localizado:** El algoritmo en `Player.Companion.generateProcedural` evalúa el país del club. Si es Brasil, selecciona nombres portugueses; si es Francia, nombres franceses; y para el resto de América Latina, utiliza nombres en español de alta fidelidad para maximizar la inmersión.

---

## ⚠️ 6. RIESGOS TÉCNICOS Y SU MITIGACIÓN

1.  **Riesgo: Caída de rendimiento por persistencia de datos (I/O bloqueante).**
    *   *Mitigación:* Se implementa persistencia asíncrona diferida estructurada delegando a `Dispatchers.IO` a través del sistema de serialización local estructurado. No se guarda toda la base de datos de una vez; se dividen en archivos de lote (`saveClubsBatch`, `savePlayersBatch`) manejados de manera reactiva por coroutines y archivos planos ligeros como `calendar.txt` para la fecha.
2.  **Riesgo: Consistencia del calendario tras fundar un club personalizado.**
    *   *Mitigación:* Al crear un club nuevo, el sistema reemplaza al último club de la liga del país correspondiente para mantener el número de clubes par (6 clubes). Posteriormente, invoca inmediatamente a `generateSchedule()` para reconstruir el fixture deportivo de partidos sin romper el motor de simulación.
