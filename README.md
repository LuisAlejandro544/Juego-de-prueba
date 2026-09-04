# Tactical SWAT Mobile

Juego táctico de vista cenital (*top-down tactical shooter*) en formato horizontal para dispositivos Android. Controla un escuadrón SWAT en operaciones de combate en espacios cerrados (CQB), gestionando movimiento táctico, cono dinámico de visión (Field of View), apertura y cierre de puertas, y combate con armamento variado contra hostiles armados.

El proyecto incorpora una arquitectura híbrida con **Kotlin + Jetpack Compose** para la interfaz móvil y un **mini motor nativo de alto rendimiento en C++, Rust y Lua 5.4 oficial puro**.

---

## Características Principales

- **Orientación Horizontal (Landscape)**: Diseñado y optimizado para pantallas móviles táctiles en modo apaisado.
- **Control de Escuadrón**: Selección rápida de operadores (Bravo-1, Bravo-2, Bravo-3) y órdenes de movimiento directas mediante toques en pantalla.
- **Simulación CQB**:
  - Muros que bloquean proyectiles y líneas de visión.
  - Puertas interactivas que pueden abrirse o cerrarse para despejar esquinas y planificar entradas.
  - Cono dinámico de visión (Field of View) con niebla de guerra táctica que revela amenazas en tiempo real.
- **Mini-Inventario y Armamento**:
  - **Glock 17**: Pistola de 9mm (17 rondas) con recarga rápida y maniobrabilidad.
  - **Fusil de Asalto M4**: Carabina de 5.56mm (30 rondas) con alta cadencia y alcance superior.
  - Estadísticas balísticas, recarga táctica y alternancia en combate.
- **Mini Motor Nativo Multi-Lenguaje**:
  - **C++ (NDK + CMake 3.22)**: Puente JNI de alto rendimiento (`libswattactics_native.so`).
  - **Rust (Cargo + NDK Toolchain)**: Módulo de física y cálculos matemáticos vectoriales compilado para `arm64-v8a`, `armeabi-v7a` y `x86_64`.
  - **Lua 5.4 Puro Oficial**: Intérprete C original (sin wrappers intermedios) integrado para soporte de scripting y comportamiento de IA.

---

## Requisitos Previos

- **Android SDK**: API 24 (Android 7.0) o superior (Target SDK: 36).
- **Gradle**: 9.0+.
- **Android NDK**: 25.2.9519653+ (r25c).
- **CMake**: 3.22.1+.
- **Rust Toolchain (Opcional para recompilar Rust)**: Rustc 1.70+ con los targets de Android (`aarch64-linux-android`, `armv7-linux-androideabi`, `x86_64-linux-android`).

---

## Compilación y Ejecución

### 1. Compilación Manual en GitHub Actions (APK Debug)
El repositorio incluye el flujo de trabajo automatizado `.github/workflows/build_debug_apk.yml` que:
- Se activa de forma **100% manual** desde la pestaña **Actions** -> **Build Debug APK** -> **Run workflow**.
- Descarga el código y configura JDK 17, Android NDK r25c y CMake 3.22.1.
- Configura el compilador de Rust con los 3 targets de Android y compila las librerías nativas con `./build_rust.sh`.
- Ejecuta `gradle assembleDebug` para empaquetar el APK Debug con C++, Rust, Lua y Kotlin.
- Sube el archivo `.apk` resultante como artefacto descargable directo (`tactical-swat-debug-apk`).

### 2. Compilar el APK de Forma Local
```bash
gradle assembleDebug
```
El archivo generado se ubicará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

### 3. Ejecutar Pruebas Unitarias
```bash
gradle :app:testDebugUnitTest
```

### 4. Recompilar el Módulo Rust (Si se realizan cambios en Rust)
```bash
./build_rust.sh
```

---

## Automatización de Mensajes de Commit
El proyecto incluye el workflow `.github/workflows/override_commit.yml` que, ante cada push a las ramas `main` o `master`, lee el contenido de `commit_message.txt` (siempre redactado en español) y reescribe automáticamente el último commit para mantener una bitácora impecable.

---

## Distribución

Este APK está preparado para distribución libre mediante plataformas de terceros como **Uptodown** o descarga directa sin ataduras a servicios propietarios.
