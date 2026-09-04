package com.example.swat.engine

import android.util.Log

/**
 * Puente JNI entre Kotlin y el motor nativo en C++, Rust y el intérprete puro de Lua 5.4.
 */
object NativeEngineBridge {

    private const val TAG = "NativeEngineBridge"
    var isLoaded: Boolean = false
        private set

    init {
        try {
            System.loadLibrary("swattactics_native")
            isLoaded = true
            Log.i(TAG, "swattactics_native library loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library swattactics_native not found or failed to load: ${e.message}")
            isLoaded = false
        }
    }

    /**
     * Retorna una cadena informativa que valida la inicialización de C++, Rust y Lua 5.4 oficial.
     */
    external fun getNativeEngineInfo(): String

    /**
     * Ejecuta un script en Lua 5.4 puro de manera directa y devuelve el string resultante.
     */
    external fun runLuaScript(script: String): String

    /**
     * Calcula la distancia entre dos puntos (x1, y1) y (x2, y2) directamente en Rust.
     */
    external fun rustCalculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float

    /**
     * Comprueba en Rust si una posición objetivo entra en el cono de visión FOV.
     */
    external fun rustIsInFov(
        targetX: Float,
        targetY: Float,
        eyeX: Float,
        eyeY: Float,
        facingDeg: Float,
        fovDeg: Float,
        maxRange: Float
    ): Boolean

    /**
     * Método seguro con fallback si la librería nativa aún no se ha cargado.
     */
    fun safeGetInfo(): String {
        return if (isLoaded) {
            try {
                getNativeEngineInfo()
            } catch (e: Throwable) {
                "C++ / Rust / Lua listo (JNI link pendiente: ${e.message})"
            }
        } else {
            "Motor C++ / Rust / Lua integrado en Gradle"
        }
    }
}
