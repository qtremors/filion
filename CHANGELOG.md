# Filion Changelog

> **Project:** Filion
> **Version:** 0.0.8
> **Last Updated:** 2026-08-30

---

## [0.0.8] - 2026-08-30

- Added smooth screensaver auto-rotation turntable mode with custom speed presets (Slow, Smooth, Fast), immersive fullscreen hiding of system bars, and active toolbar indicators.
- Added full multi-touch 360-degree free viewer controls with 1-finger unconstrained pitch/yaw orbit, 2-finger pan translation, 2-finger pinch zoom, 2-finger roll rotation, and double-tap orientation reset.
- Redesigned Home Screen with collapsible `LargeTopAppBar`, eliminating static header clipping and providing smooth edge-to-edge list scrolling.
- Added swipe-to-refresh (`PullToRefreshBox`) to instantly rescan configured directories for newly added GLB models.
- Upgraded discovered model cards to Material 3 Expressive `SegmentedListItem` groups with tonal 3D icon badges, formatted file sizes, and touch feedback.
- Integrated live model search filtering with instant query matching, clear action, and clean search results empty states.
- Created reusable layered `EmptyState` component with responsive action buttons for opening models and configuring scan folders.

## [0.0.7] - 2026-08-30

- Added AndroidX splash screen with smooth startup preload and edge-to-edge system bar integration.
- Upgraded Settings with Material 3 Expressive layout, interactive 4-mode theme selector (System, Light, Dark, OLED Pure Black), dynamic color toggle, and segmented scan folder cards.
- Redesigned About page with collapsible top bar, tap-to-copy version and device specifications, and external project links.
- Updated Open Source Licenses screen with complete runtime library notices and expandable Apache License 2.0 viewer.
- Added spring motion curves, bounce click interactions, and system reduced-motion support across in-app screens.

## [0.0.6] - 2026-08-30

- Added CPU architecture APK splits, reducing ARM64 download and install size by 60% down to ~12.6 MB alongside a universal fallback build.
- Enabled release R8 minification and resource shrinking with dedicated ProGuard rules for Filament native C++ bindings and SceneView.
- Upgraded Android build toolchain to AGP 9.3.1, Kotlin 2.4.10, and Jetpack Compose BOM 2026.08.00.
- Added composite build convention verification for production UI string localization, version catalog integrity, and release metadata.
- Redesigned documentation website with bespoke 3D spatial styling, live GitHub repository and download statistics, fullscreen mobile navigation, and open-source acknowledgements.
- Aligned project documentation across README, architecture guides, privacy policy, and third-party notices.

## [0.0.5] - 2026-08-01

- Linked the README title to the website and made APK downloads clear with consistent wording, icons, and direct release links.
- Named debug and release APKs with the Filion name and their version.
- Refocused the website and documentation on opening, organizing, and inspecting GLB models.
- Separated debug and release installs with distinct package names and app labels.
- Recreated the Filion SVG as a smooth layered vector that preserves the PNG's shape, depth, gradients, and highlights.
- Updated the full-color and themed Android icons to match the refined Filion mark.
- Prepared signed release builds, removed obsolete storage access, and blocked inherited network access.

## [0.0.4] - 2026-08-01

- Simplified the documentation and website so the main features, setup, and privacy details are easier to find.

## [0.0.3] - 2026-08-01

- **Documentation Website**: Added a responsive GitHub Pages guide covering setup, controls, folder scanning, privacy, technology, downloads, and FAQs.
- **Accessible Navigation**: Added keyboard-friendly mobile navigation and FAQ controls, reduced-motion support, and graceful GitHub API fallbacks.

## [0.0.2] - 2026-08-01

- **Settings & About**: Added persistent theme controls, scan-folder management, app information, privacy, support, and offline open-source license details.
- **Vector Branding**: Rebuilt the Filion mark as an editable SVG with full-color and dedicated monochrome Android launcher vectors.
- **System Typography**: Removed the bundled font and switched the app to Android system typography.
- **Project Policies**: Added the MIT license, privacy policy, and third-party notices.

## [0.0.1] - 2026-07-12

- **Initial Release**: Initial release of Filion as a standalone 3D model viewer application.
- **Material 3 Expressive**: Integrated dynamic variable typography using the Google Sans Flex font, supporting custom display presets and premium UI colors.
- **Robust Model Loading**: Reconfigured the Sceneview loader to parse content URI stream bytes directly into a `ByteBuffer` to avoid URI-resolution errors.
- **Custom Scanner Folders**: Added the ability to choose custom directories on storage, persist read permissions, and automatically discover GLB model files recursively.
- **Intent Association**: Configured intent filters inside the manifest to let Filion launch directly when viewing `.glb` files from other file managers and browsers.
