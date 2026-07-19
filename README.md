# FEDEBOL MANAGER - SIMULADOR DE GESTIÓN DE FÚTBOL PROCEDURAL

FEDEBOL Manager (antes FAFI) es un simulador táctico y político de gestión futbolística para dispositivos Android. A través de un universo procedural realista y blindado legalmente mediante nombres ficticios, el juego sitúa al usuario en el rol de mánager y director deportivo para liderar clubes o fundar nuevas instituciones. El simulador integra un sistema de gobierno global (FEDEBOL), confederaciones de fantasía (SUDAMBOL, EUROBOL, NORAMBOL), algoritmos de partidos minuto a minuto, microblogging en red social y una oficina de convocatoria a selección nacional que se pulirá y profundizará en futuras actualizaciones.

---

## 🛠️ REQUISITOS PREVIOS Y VERSIONES

*   **Sistema Operativo:** Android 12.0+ (API Level 31+) recomendado (Soporta API Level 26+).
*   **Java Development Kit (JDK):** JDK 17 o superior.
*   **Gradle Build System:** Gradle 8.2+ con Kotlin DSL (`build.gradle.kts`).
*   **Android Gradle Plugin (AGP):** 8.2.x o superior.
*   **Kotlin Compiler:** 1.9.x o superior.
*   **Jetpack Compose:** Última versión estable bajo Material Design 3 (M3).

---

## 🚀 INSTALACIÓN Y CONFIGURACIÓN PASO A PASO

Sigue estos sencillos pasos para compilar, ensamblar y ejecutar el proyecto de forma local o en tu entorno de desarrollo:

1. **Clonar el Repositorio:**
   ```bash
   git clone https://github.com/tu-usuario/fedebol-manager.git
   cd fedebol-manager
   ```

2. **Verificar Configuración del Entorno:**
   Asegúrate de tener configurada la variable `ANDROID_SDK_ROOT` o `ANDROID_HOME` apuntando al SDK de Android de tu sistema.

3. **Compilar el Proyecto con Gradle:**
   Utiliza Gradle para compilar todas las dependencias y validar la sintaxis:
   ```bash
   gradle compileDebugSources
   ```

4. **Generar el Archivo APK de Depuración:**
   ```bash
   gradle assembleDebug
   ```
   El APK resultante estará disponible en la ruta:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 📅 SISTEMA DE CALENDARIO Y FECHA REAL

El juego cuenta con un planificador temporal dinámico con las siguientes características:
*   **Inicio Realista:** Comienza de forma predeterminada el **1 de enero de 2025**.
*   **Avance Semanal:** Cada jornada deportiva avanzada incrementa la fecha actual en exactamente 7 días.
*   **Pantalla de Fixture Detallada:** Permite auditar el calendario de partidos del propio club mánager, el de rivales locales e inclusive el de clubes de ligas internacionales de otros países.
*   **Persistencia Completa:** La fecha actual y el perfil del mánager se guardan de forma segura de manera asíncrona, de modo que las partidas continúen exactamente donde se dejaron.

---

## 🎨 DISEÑO ESTÉTICO: AZUL GLACIAR Y ZAFIRO PROFUNDO

Para optimizar la experiencia durante jornadas de juego largas, se ha diseñado una interfaz de usuario cómoda para la vista basada en:
*   **Zafiro Profundo:** Fondo y áreas de lectura oscuras para reducir la fatiga visual.
*   **Azul Glaciar:** Color de acento moderno que resalta elementos interactivos clave, botones principales y la fecha actual.
*   **Métricas Clarificadas:** Diseño limpio de Material Design 3 con bordes suaves de acero y espaciado de cuadrícula estricto.

---

## 🗺️ ESTRUCTURA DEL PROYECTO

La organización de carpetas y módulos principales del código fuente de FEDEBOL Manager se divide de la siguiente manera:

```text
/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/          # Código fuente principal en Kotlin
│   │   │   │   ├── engine/                # Motor de juego, simulación y estados globales
│   │   │   │   │   ├── GameEngine.kt      # Núcleo del simulador y avance de jornadas
│   │   │   │   │   └── GameStorage.kt     # Serialización y persistencia local asíncrona
│   │   │   │   ├── model/                 # Modelos de dominio y generadores procedurales
│   │   │   │   │   ├── Player.kt          # Futbolistas, rasgos, cálculos de calificación
│   │   │   │   │   ├── Club.kt            # Entidades de clubes deportivos y ligas
│   │   │   │   │   └── Manager.kt         # Perfil del mánager, finanzas y licencias
│   │   │   │   ├── storage/               # Utilidades de guardado y cifrado local
│   │   │   │   ├── ui/                    # Componentes visuales bajo Jetpack Compose
│   │   │   │   │   ├── theme/             # Colores (Glacier Blue/Sapphire), tipografía y formas
│   │   │   │   │   └── MainDashboard.kt   # Interfaz principal, pantallas de tabs (incluyendo Calendario)
│   │   │   │   └── MainActivity.kt        # Actividad raíz y flujo de Onboarding
│   │   │   └── res/                       # Recursos estáticos (strings, drawables, XML)
│   │   └── test/                          # Unit Testing y Robolectric local JVM
│   └── build.gradle.kts                   # Configuración del módulo de la aplicación
├── gradle/                                # Archivos de envoltura de Gradle
├── build.gradle.kts                       # Script de construcción a nivel de proyecto
├── settings.gradle.kts                    # Definición de submódulos y dependencias
├── ROADMAP.md                             # Planificación y fases del proyecto
├── STRUCTURE.md                           # Detalle minucioso de la arquitectura técnica
└── AI_CONTEXT.md                          # Manual para asistentes de desarrollo de IA
```

---

## 🧪 CÓMO EJECUTAR LOS TESTS

El proyecto incluye soporte para pruebas unitarias rápidas y pruebas integrales basadas en Robolectric en la Máquina Virtual de Java (JVM):

*   **Ejecutar todas las pruebas locales:**
    ```bash
    gradle :app:testDebugUnitTest
    ```

*   **Grabar imágenes de referencia de captura (Screenshot Testing con Roborazzi):**
    ```bash
    gradle :app:recordRoborazziDebug
    ```

*   **Validar la fidelidad del diseño visual mediante comparativas de pantalla:**
    ```bash
    gradle :app:verifyRoborazziDebug
    ```

---

## 🤖 INTEGRACIÓN CONTINUA (GITHUB ACTIONS)

El proyecto cuenta con pipelines de integración continua (CI) configurados mediante **GitHub Actions** para automatizar la verificación del código, seguridad y compilación. En el futuro, se podrán incorporar workflows adicionales de pruebas instrumentadas o despliegue automatizado:

1. **Build Debug APK (`build-debug-apk.yml`):**
   - Compila el proyecto de forma automática.
   - **Optimización de Disparador (Path Filtering):** Solo se activa cuando se detectan modificaciones reales dentro del subdirectorio `/app` (código fuente y recursos directos de la aplicación), evitando compilaciones redundantes por cambios en documentación o scripts de soporte.
   - Genera y emite un artefacto descargable con el archivo APK depurado (`app-debug.apk`) con retención de 7 días.

2. **Code Quality & Security Scan (`code-analysis.yml`):**
   - Ejecuta un análisis estático profundo del repositorio en busca de malas prácticas y vulnerabilidades.
   - **Análisis de Tamaño de Archivo:** Alerta y reporta sobre cualquier archivo de código (`.kt`, `.kts`, `.java`, `.gradle`) que supere las **300 líneas de código** para garantizar modularidad y limpieza arquitectónica.
   - **Escaneo de Seguridad:** Rastrea asignaciones de llaves API, secretos y tokens hardcodeados en el código.
   - **Tareas Pendientes (TODO/FIXME):** Centraliza todos los recordatorios técnicos de refactorización pendientes.
   - **Malas Prácticas:** Detecta la depuración cruda en archivos principales (como `System.out.println` o `printStackTrace()`) promoviendo el estándar de logs en Android.
   - **Notificaciones Seguras y Privadas a Discord:** Permite enviar el reporte automáticamente y de manera 100% privada a tu canal de Discord en lugar de depender de artefactos públicos. Para activarlo, añade la URL de tu webhook de Discord en los secretos de tu repositorio de GitHub como `DISCORD_WEBHOOK_URL`.
   - Genera un archivo descargable de reporte independiente en formato plano (`code-analysis-report.txt`) con retención de 14 días.

---

## 🔒 VARIABLES DE ENTORNO Y SECRETOS

Para cualquier integración con servicios de red externos o inteligencia artificial (como la API de Gemini), configure sus claves en el panel de **Secrets** de su entorno de compilación, las cuales se mapearán automáticamente en `BuildConfig`.

| Variable | Tipo | Descripción | Obligatoria | Ejemplo |
| :--- | :--- | :--- | :---: | :--- |
| `GEMINI_API_KEY` | String | Clave de acceso para la generación de resúmenes de prensa mediante IA | No (Opcional) | `AIzaSyB4x...` |

---

## 🤝 CÓMO CONTRIBUIR

1. Crea una rama para tu característica: `git checkout -b feature/nueva-tactica`
2. Asegúrate de compilar y correr las pruebas antes de enviar: `gradle :app:testDebugUnitTest`
3. Haz un commit de tus cambios: `git commit -m "Añadir rasgo de jugador: LÍDER_NATO"`
4. Realiza un Push a tu rama: `git push origin feature/nueva-tactica`
5. Abre un Pull Request describiendo detalladamente tu implementación.
