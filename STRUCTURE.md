# Estructura del Proyecto (Architecture & Directory Layout)

Este documento describe la organización de carpetas, responsabilidades de cada módulo y el flujo de datos entre las capas de Kotlin, C++, Rust y Lua, así como la infraestructura de integración continua (CI/CD).

---

## Árbol de Directorios del Repositorio

```
/
├── .github/
│   └── workflows/
│       ├── build_debug_apk.yml       # Action manual (workflow_dispatch) para compilar APK Debug
│       └── override_commit.yml       # Action para reescribir mensajes de commit desde commit_message.txt
├── app/
│   ├── build.gradle.kts              # Configuración del módulo Android, NDK, CMake y dependencias
│   ├── proguard-rules.pro            # Reglas de ofuscación y mantenimiento de clases JNI
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # Manifiesto de Android (modo horizontal configurado)
│       │   ├── cpp/                  # Capa nativa C / C++ y Lua Oficial
│       │   │   ├── CMakeLists.txt    # Script de compilación CMake 3.22
│       │   │   ├── swat_native_bridge.cpp # Puente JNI entre Kotlin, Rust y Lua
│       │   │   └── lua/              # Fuentes oficiales originales de Lua 5.4.7 (en C)
│       │   │       ├── lua.h, luaconf.h, lualib.h, lauxlib.h
│       │   │       ├── lapi.c, lvm.c, ltable.c, ldo.c, lgc.c, ...
│       │   ├── rust/                 # Módulo de cálculo y motor en Rust
│       │   │   ├── Cargo.toml        # Definición del crate `swat_rust_engine` (cdylib, staticlib)
│       │   │   └── src/
│       │   │       └── lib.rs        # Funciones de física, FOV y distancias en Rust (C-ABI)
│       │   ├── jniLibs/              # Bibliotecas estáticas precompiladas de Rust
│       │   │   ├── arm64-v8a/libswat_rust_engine.a
│       │   │   ├── armeabi-v7a/libswat_rust_engine.a
│       │   │   └── x86_64/libswat_rust_engine.a
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt   # Punto de entrada de la aplicación en Compose
│       │   │   ├── swat/
│       │   │   │   ├── engine/       # Lógica del motor y puente nativo
│       │   │   │   │   ├── FieldOfView.kt        # Algoritmo de visión y oclusión
│       │   │   │   │   ├── NativeEngineBridge.kt # Interfaz JNI con C++, Rust y Lua
│       │   │   │   │   └── TacticalSoundEngine.kt# Síntesis procedural de efectos de audio
│       │   │   │   ├── model/        # Modelos de datos inmutables
│       │   │   │   │   ├── Operator.kt           # Operadores SWAT y armamento
│       │   │   │   │   ├── Weapon.kt             # Estadísticas balísticas de Glock y M4
│       │   │   │   │   ├── Enemy.kt              # Hostiles armados y estados
│       │   │   │   │   ├── Wall.kt               # Geometría física de muros
│       │   │   │   │   ├── Door.kt               # Puertas interactivas
│       │   │   │   │   └── Vector2D.kt           # Vectores matemáticos en 2D
│       │   │   │   └── ui/           # Capa de presentación y renderizado (Jetpack Compose)
│       │   │   │       ├── TacticalGameScreen.kt # Orquestador de pantalla y loop de juego
│       │   │   │       ├── TacticalGameCanvas.kt # Dibujado en tiempo real en Canvas
│       │   │   │       ├── TacticalHud.kt        # Interfaz de usuario táctica (HUD)
│       │   │   │       └── MiniInventoryDialog.kt# Diálogo modal de cambio de armas
│       │   │   └── ui/theme/         # Tema visual, tipografía y paleta táctica
│       │   └── res/
│       │       ├── values/strings.xml
│       │       └── values/themes.xml
│       └── test/                     # Pruebas unitarias con JVM y Robolectric
│           └── java/com/example/
│               └── ExampleRobolectricTest.kt # Validación de FOV, armas y JNI Bridge
├── gradle/
│   └── libs.versions.toml            # Catálogo centralizado de versiones y dependencias
├── build_rust.sh                     # Script para compilar el código Rust para los ABIs de Android
├── commit_message.txt                # Mensaje en español para sobrescritura controlada de commits
├── .gitignore                        # Reglas completas para no subir archivos de C++, Rust, CMake, Kotlin y Lua
├── README.md                         # Descripción general e instrucciones del proyecto
├── ROADMAP.md                        # Planificación de fases y desarrollo futuro
├── STRUCTURE.md                      # Este documento de arquitectura técnica
├── AI_CONTEXT.md                     # Contexto técnico para asistentes de IA
└── AGENTS.md                         # Reglas e instrucciones operativas para agentes
```

---

## Flujo de Datos Arquitectónico

1. **Entrada de Usuario (Touch / Gestos)**: El usuario toca la pantalla en `TacticalGameCanvas` o `TacticalHud`.
2. **Controlador / Loop (Kotlin)**: El bucle de simulación procesa las órdenes tácticas del escuadrón, verifica el arma seleccionada y gestiona las recargas o aperturas de puertas.
3. **Capa Nativa (C++ / Rust / Lua)**:
   - A través de `NativeEngineBridge`, Kotlin puede invocar métodos nativos en C++.
   - C++ enlaza las funciones de Rust (`swat_rust_engine`) para cálculos de alto rendimiento y física vectorial.
   - C++ aloja la máquina virtual de Lua 5.4 para ejecutar scripts y eventos en tiempo real.
4. **Renderizado (Jetpack Compose Canvas)**: El estado reactivo actualizado se dibuja en pantalla con aceleración por hardware en formato apaisado.
5. **Canal CI/CD de Automatización**:
   - `build_debug_apk.yml`: Pipeline reproducible que orquesta la compilación en contenedores Ubuntu con Android NDK, Rust y Gradle, generando el APK Debug bajo demanda.
   - `override_commit.yml`: Guardián de la bitácora que alinea el mensaje del último commit con `commit_message.txt`.
