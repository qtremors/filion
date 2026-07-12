# Filion Changelog

> **Project:** Filion
> **Version:** 0.0.1
> **Last Updated:** 2026-07-12

---

## [0.0.1] - 2026-07-12

- **Initial Release**: Ported the GLB 3D viewer plugin codebase from Arcile into a standalone 3D model viewer application.
- **Material 3 Expressive**: Integrated dynamic variable typography using the Google Sans Flex font, supporting custom display presets and premium UI colors.
- **Robust Model Loading**: Reconfigured the Sceneview loader to parse content URI stream bytes directly into a `ByteBuffer` to avoid URI-resolution errors.
- **Custom Scanner Folders**: Added the ability to choose custom directories on storage, persist read permissions, and automatically discover GLB model files recursively.
- **Intent Association**: Configured intent filters inside the manifest to let Filion launch directly when viewing `.glb` files from other file managers and browsers.
