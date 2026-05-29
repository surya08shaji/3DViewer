# AETHER 3D Model Space - Android App

A production-ready, high-performance, single-activity Android application built in Kotlin and Jetpack Compose to render multiple independent 3D models simultaneously.

---

## 3D Engine Selection: SceneView (Filament)

We selected **SceneView 4.15.1** (built on Google's high-performance **Filament** C++ engine) for the following reasons:
1. **Low-Level Native Performance**: Filament is a mobile-first, hardware-accelerated physically based rendering (PBR) engine written in modern C++. It leverages Vulkan and OpenGL ES 3.0+ directly, delivering console-grade visual fidelity and rock-solid 60 FPS performance on lower-end GPUs.
2. **Compose-Native Lifecycle Integration**: SceneView wraps Filament's complex rendering pipelines into declarative, Compose-friendly components (`Scene`, `ModelNode`). This eliminates historical Android-3D boilerplate (like managing SurfaceViews, OpenGL thread states, and manual render loops) and aligns perfectly with modern MVVM Jetpack Compose architectures.
3. **Shared Context Optimization**: SceneView exposes `rememberEngine()` and `rememberModelLoader()`. This allows us to instantiate a single shared Filament context globally and pass it down to multiple independent canvas containers, preventing resource duplication and extreme RAM overhead.

---

## Performance Optimizations

To target smooth 60 FPS rendering on budget devices, we implemented the following strategies:

### 1. Unified Filament Engine & Model Cache
Instead of creating isolated, costly rendering contexts per container, we instantiate a single shared `Engine` and a single `ModelLoader` at the Compose root (`MainActivity`).
- All active `ModelContainer` instances load their assets through this shared loader.
- Under the hood, Filament shares the same GPU memory buffer for textures and mesh geometry.
- If a user spawns multiple instances of the same model (e.g., three Ducks), Filament caches the source geometry and only creates lightweight instances.

### 2. Smooth, Separate Gestures (No Layout Drifts)
Gestures are intercepted at the Jetpack Compose layer using `Modifier.pointerInput` and custom delta mappings rather than letting native views fight for touches:
- **Normal Mode**: Single-finger drag pans the container; multi-finger pinch resizes the container bounds. During pinch gestures, panning is ignored to prevent annoying layout drift.
- **Interaction Mode**: Single-finger drag rotates the 3D model itself; pinch zooms the model internally. The container size and position remain completely fixed.
- **Immediate Selection**: We use raw touch `Press` events via Compose pointer events to instantly bring clicked containers to the front, eliminating the standard ~100ms click delay.

### 3. Recomposition Pruning via `derivedStateOf`
Sorting containers by Z-index can trigger heavy layouts. We wrapped our sorting logic inside a Compose `derivedStateOf` block in the main screen:
- Re-sorting only runs when an item's `zIndex` actually changes or items are added/removed.
- We utilize Compose `key(item.id)` wrappers on containers to ensure that rearrangements do not force Filament to recreate the underlying 3D views.

### 4. Zero-Overdraw CSS Glassmorphism
Real-time render-node blur filters (`Modifier.blur()`) are notorious GPU killers on budget mobile devices. Instead, we simulated high-end glassmorphism using semi-transparent multi-layered gradient brushes (`Brush.verticalGradient`) with glowing neon borders. This delivers a premium cybernetic UI aesthetic with **zero** performance penalty.

---

## Memory Management

- **Asynchronous Compilation**: Models compile in background threads via SceneView's coroutine-backed asset loader. The UI remains fully responsive, displaying a custom neon loading HUD until Filament completes GPU upload.
- **Explicit Disposals**: When a container is closed, it leaves the Compose composition. Because SceneView binds node lifetimes directly to their composable scope, leaving composition triggers immediate native destructors for those nodes, purging them from GPU memory.
- **JVM Metaspace Safety**: Configured JVM properties (`org.gradle.jvmargs`) in `gradle.properties` to strictly clamp memory sizes and prevent build-time daemon leaks.

---

## Limitations

1. **OpenGL ES 3.0 Requirement**: The app explicitly requires OpenGL ES 3.0 or higher (declared in `AndroidManifest.xml`) because Filament relies heavily on GLES3 features for PBR rendering. It will not run on extremely archaic devices lacking GLES3 hardware.
2. **Strict Single-Threaded Rendering**: Filament commands must be executed on the UI/main thread. SceneView handles this automatically, but custom multi-threaded additions must be avoided to prevent native crashes.

---

## How to Run the Project

### Prerequisites
- **JDK**: Java 17 or higher (bundled with modern Android Studio).
- **Android Studio**: Android Studio Hedgehog (2023.1.1) or newer recommended.
- **Device**: Android device or Emulator running Android 7.0+ (Min SDK 24) with OpenGL ES 3.0 support.

### Building via Command Line
Using the bundled Java 21 wrapper in your terminal:
```bash
# Build the debug APK
./gradlew assembleDebug
```
The compiled APK will be available under `app/build/outputs/apk/debug/app-debug.apk`.

### Running in Android Studio
1. Open Android Studio.
2. Select **Open** and choose the directory `c:\Users\wac\AndroidStudioProjects\3DModelViewer`.
3. Wait for Gradle sync to complete successfully.
4. Click **Run** (`Shift + F10`) to deploy to your connected device/emulator.
