# Bare

A truly minimal Android TV launcher. It shows your apps and starts them. Nothing else.

Made for the **Xiaomi Smart Projector L1 Pro** (Formovie C045RGN, Android 14, 1.8 GB RAM), where the stock Google TV launcher is slow and uses a lot of memory.

> **Untested on other hardware.** It works on the projector above. It may work on other Android 14 TVs. It has not been tried there. Use it at your own risk and keep ADB access so you can undo it.

## Why

| | Bare | Stock Google TV launcher (same device) |
|---|---|---|
| APK size | 21 KB | about 23 MB |
| Idle memory (PSS) | about 41 MB | about 125 MB (two processes) |
| Processes | 1 | 2 |
| Background services | none | many |
| Network permission | none | yes |
| Ads, recommendations, analytics | none | yes |

Memory numbers come from `dumpsys meminfo` on the projector. Your device can differ.

How it stays small:

- Plain Java and plain Android views. No Kotlin, no Flutter, no Compose, no AndroidX, no libraries.
- One Activity and one tiny accessibility service. No background services or boot receivers.
- It listens for app install and remove events only while the launcher is on screen.
- No network permission. It cannot send anything anywhere.

## What it does

- Shows a grid of your apps with their TV banners (rounded corners, 16:9). Apps with no banner show their icon.
- D-pad focus with a two-tone border that shows on light and dark tiles.
- Orders apps by how often you open them, then by name.
- Shows a clock.
- Opens Settings from the remote Settings button.

It does not do wallpapers, folders, recommendations, search, widgets or themes. That is the point.

## Install

You need ADB access to the device (Wireless debugging on Android 11 and newer).

```
./build.sh                           # needs JDK 17+ and the Android SDK (platform 34)
scripts/setup.sh 192.168.1.16:PORT --disable-stock
```

`setup.sh` installs the APK, sets Bare as the Home app, and turns on the key service. Without `--disable-stock`, many devices keep opening the stock launcher on Home. With it, the script disables the stock Google TV launcher (`com.google.android.apps.tv.launcherx`) and the setup wizard (`com.google.android.tungsten.setupwraith`) for your user. That is a normal, reversible `pm disable-user`. Some features that need the stock launcher stop working, such as the Google TV dashboard and the Assistant button panel.

## Undo

```
scripts/restore.sh 192.168.1.16:PORT
```

This removes Bare and brings back the stock launcher. If the screen is stuck, run these two commands:

```
adb shell pm enable com.google.android.apps.tv.launcherx
adb shell pm enable com.google.android.tungsten.setupwraith
```

## The Settings button

On the projector, the Settings button on the remote sends scan code 384 (`KEY_TAPE`). The stock launcher handled it. When you disable the stock launcher, the button does nothing. Bare includes an accessibility service (`KeyService`) that catches the key and opens Settings.

The service reads key events only. It does not read screen content. Android shows a warning when you enable any accessibility service. This is normal.

To use another remote, set `DEBUG = true` in `KeyService.java`, rebuild, press the button and run `adb logcat -s BareKey`. Then add the code to the key check. Turn `DEBUG` off again, because it logs every key press.

## Build

```
./build.sh
```

The script uses `aapt2`, `javac`, `d8`, `zipalign` and `apksigner` from the Android SDK. It looks for the SDK in `$ANDROID_HOME` or `~/Android/Sdk`. It signs with `~/.android/debug.keystore`, and creates that file if it is missing. Set `KEYSTORE`, `KS_PASS` and `KS_ALIAS` to use your own key. The output is `build/bare.apk`.

## Limits

- Android 14 only (minSdk 34).
- No way to hide, reorder or remove apps from the grid.
- Apps without a launcher activity do not show.
- Tested on one device.

## License

MIT
