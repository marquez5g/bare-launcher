# ADB setup guide

Bare is installed from a computer with ADB (Android Debug Bridge). This guide shows how to turn on developer mode, get ADB, and connect to your TV or projector. You do this once.

Menu names change between devices and Android versions. The steps below match Android TV 11 and newer. If a menu has a different name on your device, look for the closest one.

You need:

- A computer (Windows, macOS or Linux) on the same Wi-Fi network as the TV.
- The TV remote.

## 1. Turn on developer options on the TV

1. On the TV, open **Settings**.
2. Go to **System** > **About** (on some devices: **Device Preferences** > **About**).
3. Find **Build** (or **Android TV OS build**). Select it **7 times** with the remote. A message says "You are now a developer".
4. Go back. A new menu **Developer options** now shows in **System** (or **Device Preferences**).

## 2. Turn on wireless debugging

1. Open **Developer options**.
2. Turn on **USB debugging**.
3. Turn on **Wireless debugging**. Accept the warning.
4. Select the **Wireless debugging** text (not only the switch) to open its screen. Keep this screen open. It shows:
   - **IP address & Port**, for example `192.168.1.16:36259`. This is the connect address.
   - **Pair device with pairing code**. This gives a different address and a 6-digit code.

> Some older devices have **ADB debugging** and **ADB over network** instead. In that case the port is usually `5555`, and you can skip the pairing step.

## 3. Get ADB on your computer

Download **SDK Platform-Tools** from Google: https://developer.android.com/tools/releases/platform-tools

Unzip it. The folder has the `adb` program.

- **Windows:** Open the unzipped folder. Click the address bar, type `powershell` and press Enter. Run commands as `.\adb ...`
- **macOS:** Open Terminal. Go to the folder with `cd`. Run commands as `./adb ...`
- **Linux:** Many distributions have a package. For example `sudo apt install adb` (Debian, Ubuntu) or `sudo pacman -S android-tools` (Arch). Run commands as `adb ...`

Check that it works:

```
adb version
```

The examples in the README use `adb`. Use `.\adb` or `./adb` if you run it from the folder.

## 4. Pair and connect

On the TV, open **Pair device with pairing code**. It shows an address like `192.168.1.16:41007` and a code like `123456`.

On the computer:

```
adb pair 192.168.1.16:41007
```

Enter the 6-digit code when asked. You see `Successfully paired`.

Then connect with the **IP address & Port** from the main Wireless debugging screen. This port is not the same as the pairing port:

```
adb connect 192.168.1.16:36259
```

You see `connected to 192.168.1.16:36259`. Look at the TV. If it asks "Allow USB debugging?", select **Always allow** and **Allow**.

Check the connection:

```
adb devices
```

Your device must show as `device` (not `offline` or `unauthorized`).

## Problems

- **`failed to connect` or `No route to host`:** The port changed. The wireless debugging port changes each time you turn the switch off and on, or reboot the TV. Read the **IP address & Port** line again. Also check that the computer and TV use the same Wi-Fi network.
- **`unauthorized`:** Look at the TV screen and accept the "Allow USB debugging?" message. If it does not show, turn Wireless debugging off and on, and connect again.
- **`offline`:** Run `adb disconnect` and then `adb connect` again.
- **Pairing fails:** The pairing code and port last a short time. Open **Pair device with pairing code** again and use the new values.
- **The connection drops after some time:** The TV may sleep. Wake it with the remote and run `adb connect` again.
- **More than one device:** Add `-s 192.168.1.16:36259` after `adb` in each command.

## Official documentation

- Android Debug Bridge: https://developer.android.com/tools/adb
- Developer options: https://developer.android.com/studio/debug/dev-options
