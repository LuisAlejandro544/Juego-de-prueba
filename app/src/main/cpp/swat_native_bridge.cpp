#include <jni.h>
#include <string>
#include <sstream>

extern "C" {
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"

// Enlace con funciones de Rust
const char* swat_rust_get_version();
float swat_rust_calc_distance(float x1, float y1, float x2, float y2);
bool swat_rust_is_in_fov(float target_x, float target_y, float eye_x, float eye_y, float facing_deg, float fov_deg, float max_range);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_swat_engine_NativeEngineBridge_getNativeEngineInfo(
        JNIEnv* env,
        jobject /* this */) {
    
    // 1. Probar Lua puro 5.4 oficial
    lua_State *L = luaL_newstate();
    std::string luaStatus = "Inactivo";
    if (L != nullptr) {
        luaL_openlibs(L);
        const char *luaTestScript = "return 'Lua ' .. _VERSION .. ' Oficial Inicializado'";
        if (luaL_dostring(L, luaTestScript) == LUA_OK) {
            if (lua_isstring(L, -1)) {
                luaStatus = lua_tostring(L, -1);
            }
        }
        lua_close(L);
    }

    // 2. Probar Rust Engine
    const char* rustVer = swat_rust_get_version();
    float testDist = swat_rust_calc_distance(0.0f, 0.0f, 30.0f, 40.0f);

    std::ostringstream oss;
    oss << "[C++ NDK Activo] | [" << rustVer << " | Test Dist(30,40)=" << testDist << "] | [" << luaStatus << "]";
    return env->NewStringUTF(oss.str().c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_swat_engine_NativeEngineBridge_runLuaScript(
        JNIEnv* env,
        jobject /* this */,
        jstring script) {
    const char *nativeScript = env->GetStringUTFChars(script, nullptr);
    lua_State *L = luaL_newstate();
    std::string resultStr;
    if (L == nullptr) {
        env->ReleaseStringUTFChars(script, nativeScript);
        return env->NewStringUTF("Error: no se pudo instanciar lua_State");
    }

    luaL_openlibs(L);
    if (luaL_dostring(L, nativeScript) != LUA_OK) {
        resultStr = std::string("Error Lua: ") + lua_tostring(L, -1);
    } else {
        if (lua_isstring(L, -1)) {
            resultStr = lua_tostring(L, -1);
        } else {
            resultStr = "Lua ejecutado con éxito (sin retorno de string)";
        }
    }
    lua_close(L);
    env->ReleaseStringUTFChars(script, nativeScript);
    return env->NewStringUTF(resultStr.c_str());
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_example_swat_engine_NativeEngineBridge_rustCalculateDistance(
        JNIEnv* /* env */,
        jobject /* this */,
        jfloat x1, jfloat y1, jfloat x2, jfloat y2) {
    return swat_rust_calc_distance(x1, y1, x2, y2);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_swat_engine_NativeEngineBridge_rustIsInFov(
        JNIEnv* /* env */,
        jobject /* this */,
        jfloat targetX, jfloat targetY,
        jfloat eyeX, jfloat eyeY,
        jfloat facingDeg, jfloat fovDeg, jfloat maxRange) {
    return swat_rust_is_in_fov(targetX, targetY, eyeX, eyeY, facingDeg, fovDeg, maxRange);
}
