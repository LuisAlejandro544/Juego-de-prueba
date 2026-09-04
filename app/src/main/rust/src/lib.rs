use std::os::raw::c_char;

/// Función de prueba y base para el motor en Rust
#[no_mangle]
pub extern "C" fn swat_rust_get_version() -> *const c_char {
    static VERSION: &str = "SWAT Tactical Engine (Rust v0.1.0)\0";
    VERSION.as_ptr() as *const c_char
}

/// Cálculo de física / distancia rápida sin asignación dinámica de memoria
#[no_mangle]
pub extern "C" fn swat_rust_calc_distance(x1: f32, y1: f32, x2: f32, y2: f32) -> f32 {
    let dx = x2 - x1;
    let dy = y2 - y1;
    (dx * dx + dy * dy).sqrt()
}

/// Comprobación de campo de visión (FOV) / ángulo en Rust
#[no_mangle]
pub extern "C" fn swat_rust_is_in_fov(
    target_x: f32,
    target_y: f32,
    eye_x: f32,
    eye_y: f32,
    facing_angle_deg: f32,
    fov_deg: f32,
    max_range: f32,
) -> bool {
    let dx = target_x - eye_x;
    let dy = target_y - eye_y;
    let dist = (dx * dx + dy * dy).sqrt();
    if dist > max_range {
        return false;
    }
    let angle_to_target = dy.atan2(dx).to_degrees();
    let mut diff = (angle_to_target - facing_angle_deg).abs() % 360.0;
    if diff > 180.0 {
        diff = 360.0 - diff;
    }
    diff <= (fov_deg / 2.0)
}
