# Instrucciones Operativas para Agentes (AGENTS.md)

Este documento contiene las normas estrictas y directivas de comportamiento para cualquier agente de codificación automatizado o asistente de desarrollo en este proyecto.

---

## 1. Directivas Obligatorias de Razonamiento y Ejecución
- **Razonamiento previo obligatorio**: Antes de ejecutar cualquier herramienta o aplicar cambios en el código fuente, el agente DEBE razonar paso a paso qué archivos va a modificar, qué impacto tendrán los cambios y por qué son necesarios.
- **Acción sobre conversación**: No generar explicaciones extensas entre llamadas de herramientas; ejecutar las operaciones de forma precisa y concisa.
- **No asumir contenidos**: Siempre usar `view_file` para leer el código real de un archivo antes de aplicar una modificación quirúrgica con `edit_file`.

---

## 2. Restricciones de Dominio y Plataforma
- **Orientación**: La aplicación es un juego en formato **horizontal (landscape)**. Bajo ninguna circunstancia modificar la orientación a vertical ni alterar la configuración de pantalla completa sin petición explícita.
- **Entorno del Usuario**: El usuario opera principalmente desde un dispositivo móvil / teléfono, no desde un ordenador de escritorio. Mantener los comandos sencillos, automatizados y reproducibles en scripts directos.
- **Distribución**: El APK está pensado para tiendas alternativas como Uptodown o distribución directa de APK, no únicamente para Google Play. No asumir disponibilidad de servicios de Google Play en el dispositivo final.
- **Game Boosters**: En caso de implementar utilidades de optimización o Game Booster, NUNCA utilizar propiedades de sistema restringidas como `persist.sys.*`.
- **Mensajes de Commit (`commit_message.txt`)**: Si existe o se crea un archivo `commit_message.txt`, la información debe redactarse en español y no debe ser alterado a menos que el usuario lo solicite expresamente.
- **Propiedad Intelectual y Nombres**: Evitar nombrar archivos o identificadores con marcas registradas protegidas por derechos de autor que puedan comprometer al usuario.

---

## 3. Reglas para C++, Rust, Lua y Gradle
- **Integración completa en Gradle**: Si el proyecto utiliza C++, Rust, Lua o cualquier lenguaje nativo, DEBEN estar plenamente integrados en `app/build.gradle.kts` o en los scripts de compilación correspondientes (`CMakeLists.txt`, `build_rust.sh`).
- **No reemplazar por fallbacks vacíos**: Si se solicita una funcionalidad nativa o un módulo en un framework/lenguaje específico, no sustituirlo por una función dummy de Kotlin que descarte el código nativo solicitado.
- **Lua Puro**: Se utiliza el Lua oficial en C puro (versión 5.4+). Prohibido sustituirlo por wrappers o bindings de alto nivel que limiten el acceso directo a la máquina virtual (`lua_State`).
- **Limpieza de Git**: Asegurarse de que el `.gitignore` mantenga fuera del control de versiones los artefactos de compilación temporal (`.cxx`, `CMakeFiles`, `target/`, `*.so`, `*.o`, `*.luac`, etc.).

---

## 4. Verificación y Calidad
- Cada cambio que afecte el código fuente Kotlin o nativo debe validarse ejecutando la compilación (`compile_applet`) y/o las pruebas unitarias correspondientes con Robolectric (`gradle :app:testDebugUnitTest`).
