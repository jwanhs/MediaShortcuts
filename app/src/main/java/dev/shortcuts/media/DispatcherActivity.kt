package dev.shortcuts.media

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.widget.Toast

/**
 * Reads its own component's meta-data to figure out which media key to send,
 * and optionally which package to target. Then exits immediately.
 *
 * If no `target_package` is set, the intent is broadcast without setPackage(),
 * which lets Android's MediaSession framework route it to the active session.
 * That's the default behavior — it works the same way Bluetooth headphone
 * buttons do.
 */
class DispatcherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (keyCode, targetPackage) = readMetadata()
        if (keyCode == null) {
            Toast.makeText(this, "Shortcut misconfigured: no key_code", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dispatchMediaKey(keyCode, targetPackage)
        finish()
    }

    private fun readMetadata(): Pair<Int?, String?> {
        val flags = PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
        val info = try {
            // componentName here is the activity-alias the user tapped
            packageManager.getActivityInfo(componentName, flags)
        } catch (e: PackageManager.NameNotFoundException) {
            return null to null
        }
        val keyName = info.metaData?.getString("key_code")
        val pkg = info.metaData?.getString("target_package")
        return mapKeyName(keyName) to pkg
    }

    private fun mapKeyName(name: String?): Int? = when (name?.lowercase()) {
        "next" -> KeyEvent.KEYCODE_MEDIA_NEXT
        "previous", "prev" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
        "play_pause", "playpause" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        "play" -> KeyEvent.KEYCODE_MEDIA_PLAY
        "pause" -> KeyEvent.KEYCODE_MEDIA_PAUSE
        "stop" -> KeyEvent.KEYCODE_MEDIA_STOP
        "rewind" -> KeyEvent.KEYCODE_MEDIA_REWIND
        "fast_forward", "ff" -> KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
        else -> null
    }

    private fun dispatchMediaKey(keyCode: Int, targetPackage: String?) {
        val eventTime = SystemClock.uptimeMillis()
        sendKey(keyCode, KeyEvent.ACTION_DOWN, eventTime, targetPackage)
        sendKey(keyCode, KeyEvent.ACTION_UP, eventTime, targetPackage)
    }

    private fun sendKey(keyCode: Int, action: Int, eventTime: Long, targetPackage: String?) {
        val key = KeyEvent(eventTime, eventTime, action, keyCode, 0)
        val intent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, key)
            // Only target a specific package if explicitly configured.
            // Without setPackage(), Android dispatches to the active MediaSession.
            if (!targetPackage.isNullOrBlank()) {
                setPackage(targetPackage)
            }
        }
        sendBroadcast(intent)
    }
}
