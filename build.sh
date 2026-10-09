#!/usr/bin/env bash
# Builds build/bare.apk with the Android SDK command line tools. No Gradle, no Kotlin.
#
# Needs: JDK 17 or newer (JAVA_HOME or on PATH), Android SDK with platform android-34
# and build-tools. Set ANDROID_HOME if the SDK is not in ~/Android/Sdk.
# The APK is signed with the standard debug key (~/.android/debug.keystore, created if missing).
# Set KEYSTORE, KS_PASS and KS_ALIAS to use your own key.
set -euo pipefail
cd "$(dirname "$0")"

SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}}"
BT="$(ls -d "$SDK"/build-tools/*/ 2>/dev/null | sort -V | tail -1)"
AJ="$SDK/platforms/android-34/android.jar"
[ -n "$BT" ] || { echo "No build-tools found in $SDK" >&2; exit 1; }
[ -f "$AJ" ] || { echo "Missing $AJ (install platform android-34)" >&2; exit 1; }
[ -n "${JAVA_HOME:-}" ] && export PATH="$JAVA_HOME/bin:$PATH"

KEYSTORE="${KEYSTORE:-$HOME/.android/debug.keystore}"
KS_PASS="${KS_PASS:-android}"
KS_ALIAS="${KS_ALIAS:-androiddebugkey}"
if [ ! -f "$KEYSTORE" ]; then
  mkdir -p "$(dirname "$KEYSTORE")"
  keytool -genkeypair -keystore "$KEYSTORE" -storepass "$KS_PASS" -keypass "$KS_PASS" \
    -alias "$KS_ALIAS" -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US" >/dev/null
fi

rm -rf build && mkdir -p build/gen build/classes
"$BT/aapt2" compile --dir res -o build/res.zip
"$BT/aapt2" link -o build/base.apk -I "$AJ" --manifest AndroidManifest.xml --java build/gen \
  --min-sdk-version 34 --target-sdk-version 34 build/res.zip
javac --release 8 -Xlint:-options -cp "$AJ" -d build/classes $(find src build/gen -name '*.java')
"$BT/d8" --release --min-api 34 --lib "$AJ" --output build $(find build/classes -name '*.class')
cp build/base.apk build/unsigned.apk
(cd build && zip -q unsigned.apk classes.dex)
"$BT/zipalign" -f 4 build/unsigned.apk build/aligned.apk
"$BT/apksigner" sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --ks-key-alias "$KS_ALIAS" \
  --out build/bare.apk build/aligned.apk
rm -f build/bare.apk.idsig
ls -l build/bare.apk
