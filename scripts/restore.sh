#!/usr/bin/env bash
# Removes Bare and brings the stock launcher back.
#
# Usage: scripts/restore.sh <ip:port>
set -euo pipefail

DEV="${1:?Usage: scripts/restore.sh <ip:port>}"
PKG="app.bare.launcher"
SVC="$PKG/$PKG.KeyService"

adb connect "$DEV" >/dev/null
a() { adb -s "$DEV" "$@"; }

a shell pm enable com.google.android.apps.tv.launcherx || true
a shell pm enable com.google.android.tungsten.setupwraith || true

# Remove only the Bare entry from the accessibility list.
cur="$(a shell settings get secure enabled_accessibility_services | tr -d '\r')"
new="$(printf '%s' "$cur" | tr ':' '\n' | grep -vxF "$SVC" | grep -vx 'null' | paste -sd: - || true)"
if [ -z "$new" ]; then
  a shell settings delete secure enabled_accessibility_services
else
  a shell settings put secure enabled_accessibility_services "$new"
fi

a shell cmd role remove-role-holder --user 0 android.app.role.HOME "$PKG" || true
a shell pm uninstall "$PKG" || true
a shell input keyevent KEYCODE_HOME
echo "Done. The stock launcher is back."
