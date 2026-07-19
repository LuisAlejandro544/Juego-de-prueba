# MANUAL DE CONTEXTO PARA DESARROLLO CON INTELIGENCIA ARTIFICIAL: FEDEBOL MANAGER

Este documento sirve como manual arquitectónico e instructivo de desarrollo para cualquier Agente de IA que trabaje en el código fuente de FEDEBOL Manager (antes FAFI). Contiene directrices inmutables para mantener la consistencia del sistema, evitar regresiones y respetar el diseño original del simulador.

---

## 🧭 1. PRINCIPIOS DE DESARROLLO INDISPENSABLES

1.  **Preservar la Proceduralidad (Sin Licencias Reales y Nombres Ficticios):**
    *   No hardcodear nombres de clubes o futbolistas reales de ligas profesionales europeas o sudamericanas en el código principal.
    *   Toda confederación y competición utiliza nombres ficticios (FEDEBOL, SUDAMBOL, EUROBOL, NORAMBOL, Copa de los Conquistadores, Copa Continental de las Alturas, etc.) para blindar legalmente el proyecto.
    *   *Mecánica de Selección Nacional (Preview):* El mánager puede recibir propuestas de selecciones ficticias regionales (e.g. Argen-Pampa, Samba-FC, Galia-FC) al acumular reputación o realizar campañas de relaciones públicas. Este sistema inicial se pulirá y expandirá con calendarios dedicados en el futuro.
    *   Toda generación de datos debe basarse en plantillas de nombres, factores económicos de país (`economyFactor`) y coeficientes de potencial de cantera (`academyFactor`).
2.  **No Modificar Formatos de Serialización a la Ligera:**
    *   Los modelos de datos (`Player`, `Club`, `League`, `Manager`) están anotados con `@JsonClass(generateAdapter = true)` para su persistencia asíncrona local estructurada. Cualquier adición de campo debe contemplar un valor por defecto o inicializador seguro para no corromper partidas guardadas en disco por versiones anteriores.
3.  **UI 100% Jetpack Compose (Material Design 3):**
    *   No usar layouts XML tradicionales para la interfaz de usuario.
    *   Uso de temas e inyecciones de color unificados (**GlacierBlue** / Azul Glaciar, **PitchDarkBg** / Zafiro Profundo, **SurfaceCarbon** / Carbón de Interfaz). No definir colores hexadecimales directos en los archivos composables independientes.
    *   Respetar la adaptabilidad: el diseño es responsivo y soporta tanto visualizaciones de móviles en vertical como pantallas anchas de tabletas o emuladores en modo apaisado utilizando estructuras de columnas fluidas o barras de navegación en riel.

---

## 🧠 2. FLUJO DE ESTADO Y SUS ESPECIFICACIONES

El `GameEngine` actúa como la única fuente de verdad (*Single Source of Truth*). Expone flujos de lectura `StateFlow<T>` y manipula mutabilidades a través de hilos seguros en Coroutines con el contexto `Dispatchers.Default` o `Dispatchers.IO`.

### Estados Críticos que debes Vigilar:

*   `isOnboardingFinished` (Boolean): Controla si el mánager ya completó su registro o selección de club y se encuentra en el menú principal del Dashboard.
*   `isSimulating` (Boolean): Bandera para indicar que hay un procesamiento de fondo activo. Debe deshabilitar clics e interacciones de usuario en pantalla para evitar estados de carrera.
*   `manager` (StateFlow<Manager>): Perfil activo de la carrera del mánager (dinero, reputación, licencias).
*   `currentDate` (StateFlow<LocalDate>): Mantiene la fecha real de la simulación del juego. Inicia en el **1 de enero de 2025** de forma predeterminada.
*   `clubs` (StateFlow<List<Club>>) y `ligas` (StateFlow<List<League>>): Colecciones que se mutan en conjunto tras simular fechas.

### Almacenamiento Local Específico:
*   `manager.json`: Serialización JSON del objeto `Manager` para persistir compras de licencias o contratación de agentes.
*   `calendar.txt`: Archivo plano que almacena la fecha real en formato ISO-8601 (`YYYY-MM-DD`). Se actualiza sumando 7 días de forma asíncrona al simular cada fecha.

---

## 🛠️ 3. REGLAS PARA IMPLEMENTAR NUEVAS FUNCIONALIDADES

### A. Para Añadir un Rasgo de Jugador (`Trait`)
1.  Modifica el enumerador de rasgos en `Player.kt`.
2.  Define una descripción descriptiva clara y un modificador aplicable.
3.  Inserta la lógica del modificador en el motor de partidos de `GameEngine.kt` (por ejemplo, dentro del generador de eventos clave del partido o resolución de disparos/ataques).

### B. Para Ampliar Países o Ligas
1.  Edita `Country.Companion.generateUniverse()` en `Club.kt` añadiendo la bandera emoji, factores de escala financiera y prestigio.
2.  Agrega plantillas de nombres de clubes ficticios representativos en el mapa `countryClubTemplates` dentro de `Club.Companion`.
3.  Asegúrate de que la cantidad de clubes generada por defecto se mantenga en números pares para no romper el algoritmo del fixture cíclico de partidos.

### C. Para Modificar Lógicas Temporales en el Calendario
1.  La fecha de la partida avanza llamando a `engine.advanceRound()`.
2.  La fórmula para derivar la fecha histórica o futura de cualquier fecha de jornada se calcula multiplicando el índice de la jornada por 7 días a partir del 1 de enero de 2025.

---

## 🧪 4. ESTÁNDARES DE CALIDAD Y PRUEBAS

*   **Identificación de Componentes en Tests (`testTag`):**
    *   Asigna siempre tags únicos de prueba usando `Modifier.testTag("tag_name")` en todos los botones y campos clave de entrada (por ejemplo, `manager_name_input`, `simulate_button`, `calendar_match_row_<X>`).
*   **Pruebas Locales (Robolectric):**
    *   Cualquier refactorización de lógica en `GameEngine` o cálculo de atributos en `Player` requiere ejecutar las pruebas automatizadas locales para garantizar estabilidad:
        ```bash
        gradle :app:testDebugUnitTest
        ```
*   **Validaciones Visuales (Roborazzi):**
    *   Si realizas cambios estéticos en la UI, asegúrate de actualizar las capturas de referencia usando:
        ```bash
        gradle :app:recordRoborazziDebug
        ```

---

## 🔒 5. PREVENCIÓN DE ERRORES FRECUENTES (ANTI-PATTERNS)

*   **❌ NO** realices llamadas bloqueantes `Thread.sleep()` en Compose o el hilo principal. Utiliza `delay()` o maneja eventos concurrentes con Coroutines.
*   **❌ NO** agregues dependencias externas a `libs.versions.toml` sin revisar primero su compatibilidad con la versión activa de Kotlin del proyecto.
*   **❌ NO** uses variables mutables globales que no estén vinculadas al hilo seguro del motor de persistencia estructurado.
*   **❌ NO** asumas que el almacenamiento local siempre tiene datos válidos. Implementa bloques `try-catch` con valores de respaldo al deserializar archivos JSON o planos de disco.
