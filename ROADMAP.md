# Hoja de Ruta del Proyecto (Roadmap)

Este documento detalla los hitos completados y los siguientes pasos en la evolución del juego táctico SWAT y su mini motor nativo.

---

## Fase 1: Fundamentos y Prototipo Jugable [COMPLETADO]
- [x] Orientación horizontal fija (*landscape*) con soporte de pantalla completa.
- [x] Motor de renderizado en Jetpack Compose Canvas a 60 FPS.
- [x] Control de escuadrón: selección de operadores (Bravo-1, Bravo-2, Bravo-3) y órdenes de desplazamiento táctico.
- [x] Escenario CQB con salas funcionales (Recepción, Servidores, Armería, Mando).
- [x] Sistema de muros y puertas interactivas con bloqueo dinámico de paso y de visión.
- [x] Sistema de campo de visión (FOV) direccional con niebla de guerra (*Fog of War*).
- [x] Detección de hostiles y combate bidireccional (balas, trazadoras, salud de agentes y enemigos).
- [x] Mini-inventario con conmutación entre Glock 17 y Fusil de asalto M4 CQB.
- [x] Audio procedural táctico (disparos, recargas, apertura de puertas y radio).

---

## Fase 2: Infraestructura Nativa Multi-Lenguaje [COMPLETADO]
- [x] Integración de **Android NDK** y **CMake 3.22** en `app/build.gradle.kts`.
- [x] Inclusión de las fuentes originales y puras de **Lua 5.4.7** en C (`lapi.c`, `lvm.c`, etc.) compiladas directamente sin wrappers.
- [x] Creación del crate de **Rust** (`swat_rust_engine`) con funciones exportadas en C-ABI para distancia, matemáticas y FOV.
- [x] Compilación multiplataforma de Rust para `arm64-v8a`, `armeabi-v7a` y `x86_64`.
- [x] Puente JNI en C++ (`swat_native_bridge.cpp`) conectando Kotlin con Lua y Rust.
- [x] Clase puente `NativeEngineBridge.kt` y pruebas unitarias automáticas con Robolectric.

---

## Fase 3: Delegación de Física e IA al Motor Nativo [PRÓXIMO]
- [ ] Migrar el algoritmo de raycasting continuo del cono de visión (FOV) a Rust / C++ con instrucciones SIMD (ARM NEON).
- [ ] Implementar el cálculo de rutas A* (Pathfinding) en Rust para navegación autónoma alrededor de obstáculos y puertas.
- [ ] Ejecutar la máquina de estados de los enemigos en un hilo nativo independiente en segundo plano para evitar caídas de FPS.
- [ ] Parser de niveles desde archivos de texto o mapas binarios compactos cargados directamente por el motor nativo.

---

## Fase 4: Scripting de Misiones con Lua Puro [FUTURO]
- [ ] Carga de scripts `.lua` desde la carpeta de assets de Android para definir objetivos de misión (ej. rescate de rehenes, desactivación de bombas).
- [ ] Exponer funciones nativas de C++ a la máquina virtual Lua (`swat_spawn_enemy()`, `swat_trigger_alarm()`, `swat_lock_door()`).
- [ ] Diálogos y eventos de radio dirigidos dinámicamente por scripts de Lua sin necesidad de recompilar la aplicación.

---

## Fase 5: Equipamiento Táctico y Mejoras Visuales [FUTURO]
- [ ] Granadas aturdidoras (*flashbangs*) y fumígenas con físicas de rebote en muros.
- [ ] Cámara de fibra óptica para inspeccionar por debajo de las puertas antes de abrir.
- [ ] Modo cooperativo local o misiones con progresión de rango y desbloqueo de accesorios balísticos (silenciadores, miras holográficas).
- [ ] Soporte para gamepads Bluetooth externos.
