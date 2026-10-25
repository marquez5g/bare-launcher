#!/usr/bin/env bash
# Installs Bare on a TV or projector over ADB and makes it the Home app.
#
# Usage: scripts/setup.sh <ip:port> [--disable-stock]
#
# --disable-stock also disables the stock Google TV launcher for your user. Many devices
# keep opening the stock launcher on Home until it is disabled. Undo it with scripts/restore.sh.
set -euo pipefail
cd "$(dirname "$0")/.."

DEV="${1:?Usage: scripts/setup.sh <ip:port> [--disable-stock]}"
APK="build/bare.apk"
PKG="app.bare.launcher"
SVC="$PKG/$PKG.KeyService"
[ -f "$APK" ] || { echo "Build first: ./build.sh" >&2; exit 1; }

adb connect "$DEV" >/dev/null
a() { adb -s "$DEV" "$@"; }

a install -r "$APK"

# Older versions used an accessibility service for the remote Settings button.
# It is gone now. Remove it if it is still enabled, because it stops key repeat.
cur="$(a shell settings get secure enabled_accessibility_services | tr -d '\r')"
case ":$cur:" in
  *":$SVC:"*)
    new="$(printf '%s' "$cur" | tr ':' '\n' | grep -vxF "$SVC" | grep -vx 'null' | paste -sd: - || true)"
    if [ -z "$new" ]; then
      a shell settings delete secure enabled_accessibility_services || true
    else
      a shell settings put secure enabled_accessibility_services "$new"
    fi ;;
esac

if [ "${2:-}" = "--disable-stock" ]; then
  a shell pm disable-user --user 0 com.google.android.apps.tv.launcherx || true
  # The Google TV setup wizard also registers as a Home app.
  a shell pm disable-user --user 0 com.google.android.tungsten.setupwraith || true
fi

a shell cmd role add-role-holder --user 0 android.app.role.HOME "$PKG" || true
a shell input keyevent KEYCODE_HOME
echo "Done. Press Home on the remote."
