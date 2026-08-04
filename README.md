# Dim It

A lightweight, open-source screen dimmer for Android.

## Why?

Most screen dimmer apps on the Play Store require accessibility or screen reading permissions — basically giving them access to everything on your screen. I wasn't comfortable with that, so I built this instead.

## What it does

Dims your screen beyond the system minimum using a simple overlay. No tricks.

## Features

- Quick Settings tile — swipe down and tap to toggle
- Adjustable dim level (0–100%)
- Remembers your last setting
- Minimal footprint (~5MB)
- No internet, no tracking, no data collection

## Permissions

- `SYSTEM_ALERT_WINDOW` — draws the dim overlay (that's it)
- `FOREGROUND_SERVICE` — keeps the overlay alive when active
- `POST_NOTIFICATIONS` — shows status notification on Android 13+

No screen reading. No contacts. No nonsense.

## Install

### From source

```bash
git clone https://github.com/fitiha/dim-it.git
cd dim-it
export ANDROID_HOME=~/Android/Sdk
gradle assembleDebug --no-daemon
```

APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Usage

1. Open the app and grant overlay permission
2. Set your preferred dim level with the slider
3. Swipe down notification shade → tap the **Dimmer** tile to toggle

## Limitations

- **Notification shade is not dimmed** — Android's notification/Quick Settings panel is a system window that sits above app overlays. This is a platform limitation; only accessibility or root permissions could override it (which defeats the privacy purpose).
- **Status bar and navigation bar are excluded** — the overlay only dims the app content area, not system UI elements.

## License

MIT
