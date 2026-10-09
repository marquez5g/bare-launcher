package app.bare.launcher;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/**
 * Opens Settings when the remote Settings key is pressed.
 *
 * Some TV firmware sends the Settings key to the stock launcher, which this app replaces.
 * This service sees the key first and handles it. It reads key events only. It does not
 * read screen content.
 *
 * To find the key code of your own remote, set DEBUG to true, rebuild, press the key and
 * run: adb logcat -s BareKey
 */
public class KeyService extends AccessibilityService {
    private static final boolean DEBUG = false;
    private static final String TAG = "BareKey";

    // Scan code 384 (KEY_TAPE) is the Settings button on the Xiaomi Smart Projector L1 Pro remote.
    private static final int SETTINGS_SCAN_CODE = 384;

    @Override
    protected boolean onKeyEvent(KeyEvent e) {
        if (DEBUG) {
            Log.i(TAG, "key " + KeyEvent.keyCodeToString(e.getKeyCode())
                    + " scan=" + e.getScanCode() + " action=" + e.getAction());
        }
        int k = e.getKeyCode();
        if (k == KeyEvent.KEYCODE_SETTINGS || k == KeyEvent.KEYCODE_NOTIFICATION
                || e.getScanCode() == SETTINGS_SCAN_CODE) {
            if (e.getAction() == KeyEvent.ACTION_UP) {
                startActivity(new Intent(Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
