#!/bin/bash
set -e

NDK_PATH=/opt/android/sdk/ndk/25.2.9519653
TOOLCHAIN=$NDK_PATH/toolchains/llvm/prebuilt/linux-x86_64/bin
export PATH=$HOME/.cargo/bin:$TOOLCHAIN:$PATH

TARGET_API=24

mkdir -p app/src/main/jniLibs/arm64-v8a
mkdir -p app/src/main/jniLibs/armeabi-v7a
mkdir -p app/src/main/jniLibs/x86_64

echo "Compiling for aarch64-linux-android..."
$HOME/.cargo/bin/cargo build \
  --manifest-path app/src/main/rust/Cargo.toml \
  --config "target.aarch64-linux-android.linker=\"$TOOLCHAIN/aarch64-linux-android$TARGET_API-clang\"" \
  --target aarch64-linux-android \
  --release
cp app/src/main/rust/target/aarch64-linux-android/release/libswat_rust_engine.a app/src/main/jniLibs/arm64-v8a/

echo "Compiling for armv7-linux-androideabi..."
$HOME/.cargo/bin/cargo build \
  --manifest-path app/src/main/rust/Cargo.toml \
  --config "target.armv7-linux-androideabi.linker=\"$TOOLCHAIN/armv7a-linux-androideabi$TARGET_API-clang\"" \
  --target armv7-linux-androideabi \
  --release
cp app/src/main/rust/target/armv7-linux-androideabi/release/libswat_rust_engine.a app/src/main/jniLibs/armeabi-v7a/

echo "Compiling for x86_64-linux-android..."
$HOME/.cargo/bin/cargo build \
  --manifest-path app/src/main/rust/Cargo.toml \
  --config "target.x86_64-linux-android.linker=\"$TOOLCHAIN/x86_64-linux-android$TARGET_API-clang\"" \
  --target x86_64-linux-android \
  --release
cp app/src/main/rust/target/x86_64-linux-android/release/libswat_rust_engine.a app/src/main/jniLibs/x86_64/

echo "All Rust static binaries built successfully for Android!"
ls -la app/src/main/jniLibs/*/libswat_rust_engine.a
