# Filion - Releases

> **Project:** Filion
> **Version:** 0.1.0
> **Last Updated:** 2026-08-30

| Version | Release Date | Key Focus |
| :--- | :--- | :--- |
| [v0.1.0](#v010) | 2026-08-30 | Horizontal folder navigation, recently opened shelf, turntable auto-rotation, multi-touch 360 viewer, splash preloading, and CPU split optimization |
| [v0.0.5](#v005) | 2026-08-01 | Initial public release with vector branding, per-ABI APK splits, debug/release separation, settings, and documentation website |

---

# v0.1.0

**Release Date:** August 30, 2026

**Previous public release:** v0.0.5

**Development range included:** v0.0.6 through v0.1.0

Filion v0.1.0 introduces a comprehensive overhaul of the Home Screen browsing experience with horizontal folder navigation, real-time model counts, a recently opened models shelf, smooth screensaver turntable auto-rotation, 360-degree multi-touch gestures, startup preloading, and major performance optimizations.

## Highlights

- **Horizontal Folder Navigation**: Browse added storage folders in a horizontal scrolling row with live model count badges, one-tap filtering, and direct folder creation.
- **Recently Opened Shelf**: Instantly access and reopen recently inspected 3D models from a dedicated horizontal shelf located below folders.
- **Turntable Auto-Rotation Mode**: Inspect models hands-free with smooth 360-degree turntable rotation, custom speed presets (Slow, Smooth, Fast), and immersive fullscreen viewing.
- **Multi-Touch 360 Orbit Controls**: Full 1-finger orbit, 2-finger pan translation, 2-finger pinch zoom, 2-finger roll rotation, and double-tap orientation reset.
- **Startup Preload & Fast Scrollbar**: Asynchronous model and folder preloading during the splash screen, combined with a spring-physics fast scrollbar with alphabet index scrubbing.
- **Optimized APK Splits**: Dedicated CPU architecture builds (ARM64, ARMv7, x86, x86_64) reducing download and install size by 60% down to ~12.6 MB.

## What's New Since v0.0.5

### Home Screen & Library Navigation

- **Horizontal Folders Row**: Added interactive filter chips for "All" and each configured scan folder with real-time model count badges and instant "+ Add Folder" creation.
- **One-Tap Folder Filtering**: Selecting a folder filters the model collection below, updating the header to `Models in <Folder>` with a quick "Show all" reset button and folder-specific empty states.
- **Recently Opened Shelf**: Added a horizontal shelf beneath folders for immediate access to recently inspected models, with persistent history saved across sessions.
- **Empty State Display Fix**: Fixed the Home Screen empty state by suppressing the "Models Discovered" header when no models are scanned.
- **Collapsible Top App Bar**: Integrated a collapsible `LargeTopAppBar` with search, refresh, and settings actions that smoothly transitions on list scroll.
- **Pull-to-Refresh**: Added swipe-to-refresh to rescan configured storage roots and refresh discovered models on demand.
- **Fast Scroll Scrubbing**: Integrated `FilionFastScrollbar` with tactile spring stretch, thumb feedback, and dynamic alphabet index tooltips for large collections.

### 3D Viewer & Gesture Controls

- **Turntable Screensaver Mode**: Added hands-free continuous turntable auto-rotation with speed presets (Slow, Smooth, Fast) and active toolbar indicators.
- **Multi-Touch Orbit & Pan**: Added 1-finger unconstrained pitch/yaw orbit, 2-finger pan translation, 2-finger pinch zoom, 2-finger roll rotation, and double-tap orientation reset.
- **Screen Wake Lock**: Integrated automatic display wake locks during 3D model inspection and turntable rotation to prevent screen timeouts.
- **Cross-Screen Motion**: Added smooth fade and scale transitions when entering and leaving the 3D model viewer while preserving list scroll position.

### Performance, Storage & Build Engineering

- **Splash Screen Preloading**: Preloaded themes, scan folders, and local GLB models during the AndroidX splash screen to eliminate startup empty-state flicker.
- **Folder Hierarchy Pruning**: Smart folder management automatically prunes redundant child folders when parent folders are added, avoiding duplicate scans.
- **Path Deduplication**: Canonical document and file path deduplication ensures models never duplicate across overlapping storage roots.
- **CPU Architecture Splits**: Configured split APKs for ARM64-v8a, ARMeabi-v7a, x86, and x86_64 alongside a universal fallback build.
- **R8 & ProGuard Optimization**: Enabled release R8 minification and resource shrinking with dedicated keep rules for Google Filament native C++ bindings and SceneView.
- **Updated Toolchain**: Upgraded Android build toolchain to AGP 9.3.1, Kotlin 2.4.10, and Jetpack Compose BOM 2026.08.00.

---

# v0.0.5

**Release Date:** August 1, 2026

**Initial Public Release**

**Development range included:** v0.0.1 through v0.0.5

Filion v0.0.5 is the initial public release of Filion as a standalone, offline-first 3D GLB model viewer for Android, featuring polished vector branding, custom folder scanning, Storage Access Framework compatibility, package separation, and complete application settings.

## Highlights

- **Google Filament 3D Rendering**: Render 3D GLB models with realistic physically based rendering (PBR) powered by Google Filament and SceneView.
- **Content URI Byte Streaming**: Stream model bytes directly through Android ContentResolver into a direct ByteBuffer for full Storage Access Framework compatibility.
- **Custom Scan Folders**: Select custom directories on device storage to scan and index GLB models recursively with persistent read permissions.
- **Vector Branding & Identity**: Rebuilt the Filion mark as an editable SVG with full-color and themed monochrome Android launcher icons.
- **Settings & Theming**: 4-mode theme selector (System, Light, Dark, OLED Pure Black) with Material 3 dynamic color toggle.
- **Documentation & Privacy**: Dedicated GitHub Pages guide, MIT license, and offline-first processing with zero network permissions.
