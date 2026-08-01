# Filion - Developer Documentation

> Architecture, implementation notes, conventions, and verification guidance for Filion development.

**Version:** 0.0.3 | **Last Updated:** 2026-08-01
**Scope:** Internal development, app navigation, settings, 3D rendering, SAF directory scanning, documentation, and testing.

---

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Project Structure](#project-structure)
- [Scan Folders Pipeline](#scan-folders-pipeline)
- [3D Rendering Engine](#3d-rendering-engine)
- [Theming & M3 Expressive](#theming--m3-expressive)
- [Build & Run](#build--run)
- [Documentation Website](#documentation-website)
- [Testing](#testing)

---

## Architecture Overview

Filion is structured as a single-module, lightweight Jetpack Compose application designed to load 3D GLB models.

```mermaid
graph TD
    A["MainActivity<br/>Folder Scans + Main Shell"] -->|renders| B["HomeScreen<br/>Discovered Models"]
    A -->|routes| F["Settings / About / Licenses"]
    A -->|renders| C["ModelViewerScreen<br/>Sceneview Integration"]
    C --> D["ModelViewerLoader<br/>Content Resolver bytes -> ByteBuffer"]
    C --> E["ModelViewerChrome<br/>Top title banner + Bottom controls panel"]
```

### Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| **SAF DocumentTree Scanning** | Bypasses restrictive Scoped Storage limitations. Users choose custom directories (like Downloads/3D), and Filion queries the child documents tree using `DocumentsContract` and `ContentResolver` recursively. |
| **Stream-to-Buffer Loader** | Content URIs shared by other applications are converted to a raw byte array via `openInputStream` and wrapped in a `ByteBuffer`. This eliminates sceneview URI-resolution errors. |
| **System typography** | Uses the platform font through Material 3 so no font binary or separate font license is bundled. |
| **Lightweight navigation** | A small destination stack handles Settings, About, and Licenses without introducing a navigation framework. |
| **No-Internet Policy** | The app request manifest does not include `android.permission.INTERNET` to ensure absolute privacy for local CAD/3D designs. |

---

## Project Structure

```text
filion/
├── filion-app/
│   ├── app/
│   │   ├── src/main/java/dev/qtremors/filion/
│   │   │   ├── MainActivity.kt                  # Entry point, SAF scanner, and destination host
│   │   │   ├── about/                           # About and offline license presentation
│   │   │   ├── settings/                        # Settings UI and SharedPreferences wrapper
│   │   │   ├── theme/                           # Material colors and system typography
│   │   │   ├── ui/                              # Split buttons, custom menu items, cards, and dialogs
│   │   │   └── viewer/                          # Sceneview integrations, overlays, state types, and loaders
│   │   ├── src/main/res/                        # Vector icons, strings, policies, and license text
│   │   └── src/test/java/                       # State unit tests
```

---

## Scan Folders Pipeline

Filion stores directory permissions persistently:
1. User selects a folder tree using `OpenDocumentTree()`.
2. Persistable read permissions are acquired:
   ```kotlin
   contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
   ```
3. Folder URI references are saved in `SharedPreferences`.
4. Child items are scanned using `DocumentsContract.buildChildDocumentsUriUsingTree` with depth limits:
   - Queries `COLUMN_DOCUMENT_ID`, `COLUMN_DISPLAY_NAME`, `COLUMN_MIME_TYPE`, and `COLUMN_SIZE`.
   - Filters for subfolders and `.glb` matches.
5. Removing a folder deletes the stored URI and releases its persisted read grant when Android allows it.

---

## 3D Rendering Engine

Filion renders models using Google's Filament:
- **Renderer**: `SceneView` Composable node.
- **Lighting**: Direct main lighting (10,000 intensity) and fill light (3,000 intensity) multiplied by the user's brightness slider state.
- **Node Scaling**: Modulates scale matrices on gesture listeners Confirmed taps hide overlay UI chrome.

---

## Theming & M3 Expressive

Expressive styling is defined under `dev.qtremors.filion.theme`:
- Typography uses `FontFamily.Default` and Material 3 type sizes.
- Theme mode persists as system, light, or dark.
- Dynamic color is available on Android 12 and newer, with the static Filion palette as fallback.

---

## Build & Run

### Debug Build
```bash
cd filion-app
./gradlew assembleDebug
```

### Run Tests
```bash
./gradlew testDebugUnitTest
```

---

## Documentation Website

The GitHub Pages site is a dependency-free static application under `docs/`. It uses the system font stack and the deployable `docs/assets/Filion.svg` copy of the canonical mark. Keep the site version synchronized with the Android app and repository badges.

Before publishing, check `scripts.js` with Node, confirm every fragment link has a matching element ID, and verify the page contains no external font or raster-logo dependency. GitHub statistics enhance the page when the public API is available; static labels and release links are the intentional fallback.

---

## Testing

Unit tests live under `src/test/java/dev/qtremors/filion/`:
- **ModelViewerStateTest**: Asserts initial viewer state settings and active control drawer toggle logic.
- **FilionPreferencesTest**: Covers appearance defaults, persistence, fallback behavior, and folder storage.
- **AppNavigationTest**: Covers destination pushes, duplicate suppression, and back-stack pops.
