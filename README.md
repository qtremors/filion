<p align="center">
  <img src="assets/Filion.svg" alt="Filion" width="128">
</p>

<h1 align="center"><a href="https://qtremors.github.io/filion/">Filion</a></h1>

<p align="center">A focused GLB model viewer for Android.</p>

Filion opens GLB files, lets you inspect them from every angle, and keeps models from selected folders easy to find.

## What it does

- Opens `.glb` files from Filion, a file manager, or another Android app
- Rotates, pans, and zooms models with touch controls
- Adjusts scene brightness and switches between theme, black, and white backgrounds
- Scans selected folders for nearby models
- Shows the model name, size, file type, and document reference
- Shares a model or opens it in another compatible app
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

## Project guide

- [Website and user guide](https://qtremors.github.io/filion/)
- [Development guide](DEVELOPMENT.md)
- [Changelog](CHANGELOG.md)
- [Privacy policy](PRIVACY.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)

## License

Filion is available under the [MIT License](LICENSE.md).
