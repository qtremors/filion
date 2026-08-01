# Filion Privacy Policy

**Effective date:** August 1, 2026

Filion is an offline Android 3D model viewer. The app does not declare the
Android `INTERNET` permission and cannot directly send model files, usage data,
or diagnostics over the network.

## Information Filion accesses

Filion accesses only files and folders that you explicitly choose through the
Android system picker or open from another app. It reads GLB model content and
basic metadata such as the display name, size, MIME type, and local document
reference so it can render and describe the model on your device.

When you add a scan folder, Android grants Filion persistent read access to
that folder. Filion stores the folder's document URI in private app preferences
and scans it locally for GLB files. Removing the folder in Settings removes the
saved reference and asks Android to release the persisted permission.

## Data collection and sharing

Filion contains no advertising, analytics, telemetry, tracking SDKs, accounts,
or remote services. Filion does not collect, sell, or share personal data.

The Share and Open With actions transfer a model only after you request the
action and choose another installed app. Links in About open your chosen web
browser; the browser and destination website operate under their own privacy
policies.

## Settings and Android backup

Theme preferences and selected folder references are stored locally in Android
SharedPreferences. Depending on your device and Android backup settings, the
operating system may include these preferences in encrypted cloud backup or
device-to-device transfer. Filion does not initiate or control that transfer,
and model file contents are not copied into Filion's preferences.

You can clear Filion's local settings by clearing app data or uninstalling the
app. Folder access can also be removed individually from Filion Settings.

## Documentation website

The Filion website is a static GitHub Pages site. It has no advertising,
analytics, cookies, accounts, or custom tracking. It may request public release
statistics from GitHub's API, so visiting the site can send ordinary network
information such as your IP address and browser details to GitHub under
GitHub's privacy terms.

## Changes and contact

Material changes will be recorded in the repository. Questions and privacy
concerns can be submitted at <https://github.com/qtremors/filion/issues>.
