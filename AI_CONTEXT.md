# Contexto Técnico para Modelos de IA (AI Context)

Este archivo sirve como referencia concisa y precisa para cualquier modelo de Inteligencia Artificial o asistente que colabore en este repositorio.

---

## 1. Naturaleza del Proyecto
- **Tipo de Aplicación**: Videojuego táctico de vista superior (*top-down tactical shooter*) en 2D para Android.
- **Formato**: Exclusivamente horizontal / apaisado (*landscape*).
- **Temática**: Operaciones de asalto táctico SWAT en combate cercano (CQB).

---

## 2. Stack Tecnológico

| Componente | Tecnología | Uso específico |
| :--- | :--- | :--- |
| **UI & Pantalla** | Kotlin 2.2 + Jetpack Compose (M3) | HUD, menú de inventario, controles táctiles y vista general. |
| **Renderizado 2D** | Compose `Canvas` + `DrawScope` | Dibujo acelerado de muros, puertas, operadores, trazadoras y niebla de visión. |
| **Audio** | Android `AudioTrack` procedural | Generación de disparos, recargas, apertura de puertas y radio sin archivos de audio pesados. |
| **Compilación Nativa** | Android NDK 25.2 + CMake 3.22.1 | Configurado en `app/build.gradle.kts` vía `externalNativeBuild`. |
| **C++** | C++17 | Biblioteca compartida `libswattactics_native.so` que actúa como orquestador y puente JNI. |
| **Rust** | Rust 2021 (`swat_rust_engine`) | Biblioteca estática compilada con Cargo NDK para cálculos pesados de FOV, física y distancias. |
| **Lua** | Lua 5.4.7 oficial en C puro | Intérprete nativo sin wrappers de terceros para ejecución de scripts y lógica de misiones. |
| **Testing** | Robolectric 4.16 + JUnit 4 | Pruebas unitarias de lógica de juego, FOV y puente JNI en la JVM local sin emulador. |
| **CI / CD (GitHub Actions)** | GitHub Workflows | `build_debug_apk.yml` (compilación manual de APK Debug) y `override_commit.yml` (sobrescritura de commits). |

---

## 3. Principios de Diseño y Restricciones
- **Orientación fija**: Mantener siempre la app en modo landscape (`sensorLandscape` o `landscape` en `AndroidManifest.xml`).
- **No wrappers de Lua**: Usar siempre las cabeceras estándar de C (`lua.h`, `lauxlib.h`, `lualib.h`) y enlaces directos con `extern "C"`.
- **Compatibilidad con arquitecturas Android**: Todo código en Rust debe compilarse para `arm64-v8a`, `armeabi-v7a` y `x86_64` antes de empaquetar mediante `./build_rust.sh`.
- **Compilación de APK Debug en CI**: El flujo de GitHub Actions para compilar el APK (`build_debug_apk.yml`) debe ser estrictamente manual (`workflow_dispatch`).
- **Mensajes de Commit (`commit_message.txt`)**: Siempre deben estar en español y mantenerse sincronizados con las novedades funcionales.
- **Cero dependencias innecesarias de servicios de terceros**: La app está diseñada para funcionar sin dependencias forzosas de Google Play Services o tiendas propietarias (distribuible en Uptodown o APK directo).
- **Rendimiento táctil**: El bucle de dibujo no debe generar asignaciones masivas de objetos en memoria durante la fase `onDraw` para evitar pausas del recolector de basura (GC).
