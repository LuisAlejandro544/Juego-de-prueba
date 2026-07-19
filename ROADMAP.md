# ROADMAP DEL SIMULADOR DE GESTIÓN DE FÚTBOL: FEDEBOL MANAGER

Este documento detalla la planificación estratégica, el estado actual de desarrollo y las fases del proyecto diseñadas para expandir la profundidad algorítmica, táctica, temporal, federativa e inmersiva de FEDEBOL Manager (antes FAFI).

---

## 📌 FASE 1: ARQUITECTURA BASE, FACTOR HUMANO Y SEGUIMIENTO TEMPORAL (COMPLETADO ✅)

*   **Paleta de Colores Confortable (Glacier Blue y Deep Sapphire):** Migración exitosa de la paleta visual inicial hacia una combinación de azul glaciar y zafiro profundo que evita el cansancio ocular durante partidas prolongadas.
*   **Sistema de Calendario Dinámico con Fechas Reales:** Implementación del planificador temporal con fecha real que inicia por defecto el **1 de enero de 2025**. El calendario avanza semanalmente (+7 días) con cada jornada simulada, manejando año, mes y día de forma realista.
*   **Persistencia Segura de Perfil y Calendario:** Integración en la capa de almacenamiento AES cifrada de soporte para guardar y cargar de forma consistente el perfil de mánager (monedas, reputación, licencias) y la fecha exacta de la partida.
*   **Generación de Universo Procedural:** Creación de economías, prestigios y canteras aleatorias por país (América Latina y Francia) para garantizar rejugabilidad infinita y eludir problemas de licencias.
*   **Creación y Gestión de Clubes (Onboarding):** Flujo de bienvenida interactivo que permite al mánager seleccionar un club existente o fundar una institución desde cero con nombre, estadio y presupuesto personalizados.
*   **Estructura del Jugador & Rasgos:** Atributos futbolísticos y rasgos inmutables (`VELOCISTA_NATO`, `CUERPO_DE_CRISTAL`, `HEROE_BAJO_PALOS`, `EGO_DE_SUPERESTRELLA`) que aplican modificadores matemáticos precisos durante el simulacro de juego.
*   **Motor de Partidos Táctico Realista:** Simulación minuto a minuto mediante comparación de matrices de zonas (Defensa, Mediocampo, Ataque) y resolución probabilística de jugadas clave ("Saving Throws" del arquero, oportunidades de gol).
*   **Red Social de Microblogging:** Feed social reactivo que responde en tiempo real a las decisiones del mánager, el rendimiento deportivo de la plantilla y los egos individuales de los jugadores.

---

## 📈 FASE 2: MERCADO DE FICHAJES Y NEGOCIACIONES COMPLEJAS (EN DESARROLLO ⏳)

*   **Algoritmo de Ofertas de la IA:** Los clubes de las ligas secundarias y principales calcularán dinámicamente sus necesidades de plantilla por posición y emitirán ofertas automatizadas por tus futbolistas estrella.
*   **Mecánica de Puja de Contrato:**
    *   Negociación directa de prima por firma, sueldo semanal, cláusula de rescisión, bonos por valla invicta o gol, y duración del contrato.
    *   *Rasgos de Representante:* Los agentes tendrán personalidades (Codicioso, Conciliador, Agresivo) que alterarán la paciencia del jugador durante la puja.
*   **Contratos de Prueba (Trial Contracts):** Opción de fichar agentes libres por un periodo corto de prueba para revelar sus estadísticas físicas y técnicas ocultas mediante el entrenamiento diario antes de firmar un contrato definitivo.

---

## 🛡️ FASE 3: PIZARRA TÁCTICA AVANZADA E INSTRUCCIONES DE CAMPO (PLANIFICADO)

*   **Sistemas de Juego Seleccionables:** Implementar esquemas tácticos tradicionales (4-4-2, 4-3-3, 3-5-2, 5-3-2) donde la alineación modifique directamente la fuerza de las matrices de zona.
    *   *Ejemplo:* Un 3-5-2 aumentará la posesión y el mediocampo un 15%, pero debilitará la defensa por bandas en un 10%.
*   **Instrucciones de Enfrentamiento:**
    *   *Estilo:* Presión Alta (mayor fatiga física, más robos), Contraataque (ideal para jugadores con el rasgo `VELOCISTA_NATO`), Juego de Posesión.
    *   *Marcaje:* Marcaje zonal o personal sobre la estrella del club rival para desactivar sus coeficientes individuales.
*   **Cambios en Vivo y Fatiga:** Menú de sustituciones durante el minuto a minuto del partido en el simulador vivo.

---

## 🗳️ FASE 4: IA POLÍTICA DE FEDEBOL, CONFEDERACIONES Y CONVOCATORIAS (FASE INICIAL INTEGRADA ⏳)

*   **Estructura Federativa Procedural (SUDAMBOL, EUROBOL, NORAMBOL):**
    *   Integración de nombres ficticios para competiciones y asociaciones regionales para evitar disputas legales, organizando clubes y naciones según su procedencia de simulación.
*   **Mecánica de Convocatoria a Selección Nacional (Preview):**
    *   Soporte inicial interactivo en el Gabinete FEDEBOL para recibir propuestas de selecciones regionales ficticias (como Argen-Pampa, Samba-FC o Galia-FC) según tu reputación y recibir un sueldo extra semanal de +$2,500.
    *   *Nota de Desarrollo:* **Estas competiciones y el sistema de convocatoria se pulirán y perfeccionarán con calendarios dedicados, fixtures de eliminatorias y partidos independientes en el futuro.**
*   **Reformas de Torneo Cada 4 Años:**
    *   *Mundial de 64 Equipos:* Ejecutar lógica de expansión y congreso si asume un presidente con rasgo `EXPANSIVO`.
    *   *Bancarrota / Crisis macroeconómica:* Reducción general de presupuestos en países con inflación para forzar transferencias de bajo coste.

---

## 🧠 FASE 5: DINÁMICA DE VESTUARIO Y CLIQUES DE JUGADORES (PLANIFICADO)

*   **Facciones en el Vestuario:** Los futbolistas se agruparán en "cliques" según su nacionalidad, edad o rasgo. Un conflicto con el líder de una facción (por ejemplo, multar a un jugador con el rasgo `EGO_DE_SUPERESTRELLA`) causará un desplome en cadena de la moral de sus aliados en la red social.
*   **Conferencias de Prensa:** Menús interactivos antes y después de partidos de alta presión que alteran la moral del plantel o causan debates políticos con la prensa y directiva.

---

## 🔌 FASE 6: SISTEMA DE MODS Y SCRIPTING LUA (PLANIFICADO)

*   **Directorio `/Mods` Activo:**
    *   Carga de archivos de texto JSON prioritarios para reemplazar los nombres ficticios generados proceduralmente por bases de datos de clubes y futbolistas reales creadas por la comunidad.
    *   *Scripting de Reglas (Lua):* Habilitación de scripts Lua ligeros para permitir a los modders definir formatos personalizados de copas e impuestos de fichajes sin alterar el código base de Kotlin.
