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

You do not need to build anything. You need a computer and ADB access to the TV. Setup takes about 5 minutes.

**1. Get ADB working.** Follow the [ADB setup guide](docs/adb-setup.md). It covers developer mode, wireless debugging, installing ADB on Windows, macOS and Linux, and connecting. When `adb devices` lists your TV as `device`, go to step 2.

**2. Download the APK.** Get `bare-1.0.0.apk` from the [latest release](https://github.com/marquez5g/bare-launcher/releases/latest). Save it in the folder where you run ADB.

**3. Run these commands, one by one, in this order:**

```
adb install -r bare-1.0.0.apk
adb shell settings put secure enabled_accessibility_services app.bare.launcher/app.bare.launcher.KeyService
adb shell settings put secure accessibility_enabled 1
adb shell pm disable-user --user 0 com.google.android.apps.tv.launcherx
adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith
adb shell cmd role add-role-holder --user 0 android.app.role.HOME app.bare.launcher
adb shell input keyevent KEYCODE_HOME
```

Each command prints `Success` or a short message. The last command opens Bare. Press **Home** on the remote to check. You see a grid of your apps.

What the commands do:

- Lines 1 to 3 install Bare and turn on its key service (needed for the Settings button).
- Lines 4 and 5 disable the stock Google TV launcher and the Google setup wizard for your user. Many devices keep opening the stock launcher on Home until you do this. The setup wizard also registers as a Home app. Some features of the stock launcher stop working, for example the dashboard panel and the Assistant button panel.
- Line 6 makes Bare the Home app.

> Line 2 replaces the list of accessibility services you have turned on. If you use another accessibility service (for example a screen reader), use the script below instead. It keeps them.

**On Linux or macOS you can use the script instead.** It does the same steps and keeps your other accessibility services:

```
git clone https://github.com/marquez5g/bare-launcher
cd bare-launcher
# put bare-1.0.0.apk in build/bare.apk, or run ./build.sh
scripts/setup.sh 192.168.1.16:36259 --disable-stock
```

Use the IP address and port from your TV.

## Undo

Run these commands to go back to the stock launcher:

```
adb shell pm enable com.google.android.apps.tv.launcherx
adb shell pm enable com.google.android.tungsten.setupwraith
adb shell cmd role remove-role-holder --user 0 android.app.role.HOME app.bare.launcher
adb shell settings delete secure enabled_accessibility_services
adb shell pm uninstall app.bare.launcher
adb shell input keyevent KEYCODE_HOME
```

(`scripts/restore.sh <ip:port>` does the same and keeps your other accessibility services.)

If the screen is stuck or Home does nothing, the first two commands alone bring the stock launcher back.

## The Settings button

On the projector, the Settings button on the remote sends scan code 384 (`KEY_TAPE`). The stock launcher handled it. When you disable the stock launcher, the button does nothing. Bare includes an accessibility service (`KeyService`) that catches the key and opens Settings.

The service reads key events only. It does not read screen content. Android shows a warning when you enable any accessibility service. This is normal.

To use another remote, set `DEBUG = true` in `KeyService.java`, rebuild, press the button and run `adb logcat -s BareKey`. Then add the code to the key check. Turn `DEBUG` off again, because it logs every key press.

## Build from source

You only need this to change the code. To install, use the release (see above).

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
