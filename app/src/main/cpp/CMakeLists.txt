cmake_minimum_required(VERSION 3.22.1)

project("swattactics_native" LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Incluir cabeceras de Lua oficial
include_directories(lua)

# Fuentes oficiales de Lua 5.4.7
file(GLOB LUA_SOURCES "lua/*.c")

# Biblioteca estática de Lua puro oficial
add_library(lua_static STATIC
    ${LUA_SOURCES}
)
target_compile_definitions(lua_static PRIVATE LUA_COMPAT_5_3)

# Biblioteca Rust precompilada según la arquitectura ABI de Android
set(RUST_LIB_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}")
add_library(swat_rust_engine STATIC IMPORTED)
set_target_properties(swat_rust_engine PROPERTIES
    IMPORTED_LOCATION "${RUST_LIB_DIR}/libswat_rust_engine.a"
)

# Biblioteca compartida principal que se enlaza con Java/Kotlin
add_library(swattactics_native SHARED
    swat_native_bridge.cpp
)

target_link_libraries(swattactics_native
    PRIVATE
    lua_static
    swat_rust_engine
    log
    m
)
