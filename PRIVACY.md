# Privacy Policy for Filion

**Last Updated:** August 30, 2026

Filion is a personal project built with a privacy-first mindset. This policy explains how the application handles your information.

## 1. No Data Collection

Filion **does not collect, store, or transmit** any personal data, usage statistics, or telemetry from your device.

## 2. Offline by Design

Filion is designed to operate entirely offline. The application does not declare the `android.permission.INTERNET` permission, so Filion cannot upload 3D models, telemetry, or activity data over the network.

## 3. No Advertisements or Trackers

The application contains **zero advertisements** and **zero third-party tracking SDKs**.

## 4. Local File & Folder Access

Filion reads only 3D models and folders you explicitly open or choose through Android's system pickers, plus files opened from another app. It reads model bytes locally to render 3D scenes in Filament/SceneView and extracts basic metadata (display name, byte size, and document reference).

When you add a scan folder, Android grants Filion persistable read access to that folder. Filion stores the folder URI reference in its private preferences to discover GLB files locally. Removing a folder in Settings removes the reference and asks Android to release the permission grant. Files leave Filion only through an action you explicitly initiate, such as sharing or choosing Open With.

## 5. Settings and Android Backup

Filion stores your appearance preferences (theme mode, dynamic color) and selected scan folder references in private app preferences. Depending on your device settings, Android may include these preferences in system backups. 3D model files are never stored in app settings.

You can remove local preferences by clearing app data or uninstalling Filion.

## 6. Source Availability

Filion's source code is publicly available for inspection and audit on [GitHub](https://github.com/qtremors/filion).

## 7. Changes to This Policy

This policy may be updated as new features are introduced. However, the core principles (privacy, offline-only operations, and zero data collection) will remain unchanged.

---
[Back to Home](https://qtremors.github.io/filion/)
