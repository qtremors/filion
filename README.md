<p align="center">
  <img src="assets/Filion.svg" alt="Filion" width="128">
</p>

<h1 align="center">Filion</h1>

<p align="center">A private, offline GLB viewer for Android.</p>

Filion opens and inspects 3D models without an account, ads, analytics, or internet permission. Files stay on your device unless you choose to share one with another app.

## What it does

- Opens `.glb` files from Filion, a file manager, or another Android app
- Rotates, pans, and zooms models with touch controls
- Adjusts scene brightness and switches between theme, black, and white backgrounds
- Scans only the folders you choose
- Shows the model name, size, file type, and local reference
- Supports system, light, dark, and Android dynamic color themes

Filion requires Android 10 or newer.

## Install

Download the latest APK from [GitHub Releases](https://github.com/qtremors/filion/releases).

## Build

Run these commands from `filion-app/`:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Privacy

Filion does not request Android's `INTERNET` permission. Read the full [privacy policy](PRIVACY.md).

## Project guide

- [Website and user guide](https://qtremors.github.io/filion/)
- [Development guide](DEVELOPMENT.md)
- [Changelog](CHANGELOG.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)

## License

Filion is available under the [MIT License](LICENSE.md).
