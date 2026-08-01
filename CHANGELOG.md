# Filion Changelog

> **Project:** Filion
> **Version:** 0.0.3
> **Last Updated:** 2026-08-01

---

## [0.0.3] - 2026-08-01

- **Documentation Website**: Added a responsive GitHub Pages guide covering setup, controls, folder scanning, privacy, technology, downloads, and FAQs.
- **Accessible Navigation**: Added keyboard-friendly mobile navigation and FAQ controls, reduced-motion support, and graceful GitHub API fallbacks.

## [0.0.2] - 2026-08-01

- **Settings & About**: Added persistent theme controls, scan-folder management, app information, privacy, support, and offline open-source license details.
- **Vector Branding**: Rebuilt the Filion mark as an editable SVG with full-color and dedicated monochrome Android launcher vectors.
- **System Typography**: Removed the bundled font and switched the app to Android system typography.
- **Project Policies**: Added the MIT license, privacy policy, and third-party notices.

## [0.0.1] - 2026-07-12

- **Initial Release**: Ported the GLB 3D viewer plugin codebase from Arcile into a standalone 3D model viewer application.
- **Material 3 Expressive**: Integrated dynamic variable typography using the Google Sans Flex font, supporting custom display presets and premium UI colors.
- **Robust Model Loading**: Reconfigured the Sceneview loader to parse content URI stream bytes directly into a `ByteBuffer` to avoid URI-resolution errors.
- **Custom Scanner Folders**: Added the ability to choose custom directories on storage, persist read permissions, and automatically discover GLB model files recursively.
- **Intent Association**: Configured intent filters inside the manifest to let Filion launch directly when viewing `.glb` files from other file managers and browsers.
