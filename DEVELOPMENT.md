# Developing Filion

Filion is a single-module Android app written in Kotlin and Jetpack Compose. Filament and SceneView handle GLB rendering.

## Requirements

- Android Studio with JDK 11 or newer
- Android SDK 37
- An Android 10+ device or emulator

## Build and test

Run commands from `filion-app/`:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

On Windows, use `gradlew.bat`.

## Project layout

```text
filion-app/app/src/main/
├── java/dev/qtremors/filion/
│   ├── MainActivity.kt       App shell, file opening, sharing, and folder scans
│   ├── about/                About and license screens
│   ├── settings/             Settings UI and saved preferences
│   ├── theme/                Compose theme
│   ├── ui/                   Shared UI components
│   └── viewer/               Model loading, rendering, state, and controls
└── res/                      Strings, icons, themes, and bundled license text
```

The static website lives in `docs/`.

## How models are opened

Filion accepts a GLB through its file picker or an Android `ACTION_VIEW` intent. The app reads the selected content URI through `ContentResolver`, copies the bytes into a `ByteBuffer`, and passes the buffer to SceneView.

This keeps model loading compatible with Android document providers. Do not replace content URI handling with direct file paths.

## How folder scans work

1. The user chooses a folder with Android's system folder picker.
2. Filion saves the read grant and folder URI.
3. The app scans that tree for GLB files.
4. Removing a folder deletes the saved URI and releases the grant when Android permits it.

Keep scans limited to user-selected folders. The app must not request broad storage access.

## Viewer state

`ModelViewerState` holds the active control panel, zoom, light level, and background choice. `ModelViewerScreen` owns the SceneView integration, while `ModelViewerChrome` draws the controls and file actions.

## Privacy rule

The app intentionally has no `android.permission.INTERNET` entry. Do not add network features, analytics, advertising, telemetry, or remote model processing without a clear product decision and a privacy-policy update.

## Tests

Unit tests cover navigation, preferences, and viewer state under:

```text
filion-app/app/src/test/java/dev/qtremors/filion/
```

Before submitting a change, run the unit tests and the narrowest relevant Android build task.

## Website

The GitHub Pages site is plain HTML and CSS:

```text
docs/
├── assets/Filion.svg
├── index.html
└── styles.css
```

Keep the site copy short, keep the logo copy in sync with `assets/Filion.svg`, and test it at desktop and mobile widths before publishing.
