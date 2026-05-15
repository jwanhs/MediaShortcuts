package dev.shortcuts.media

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.widget.Toast

/**
 * Reads its own component's meta-data to figure out which media key to send,
 * then dispatches it via AudioManager.dispatchMediaKeyEvent(). This is the
 * public API for sending media keys to the active session — same path
 * Bluetooth headphone buttons take. Works on Android 5+.
 */
class DispatcherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val keyCode = readKeyCode()
        if (keyCode == null) {
            Toast.makeText(this, "Shortcut misconfigured: no key_code", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        dispatchMediaKey(keyCode)
        finish()
    }

    private fun readKeyCode(): Int? {
        val flags = PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
        val info = try {
            packageManager.getActivityInfo(componentName, flags)
        } catch (e: PackageManager.NameNotFoundException) {
            return null
        }
        return mapKeyName(info.metaData?.getString("key_code"))
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

    private fun dispatchMediaKey(keyCode: Int) {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val now = SystemClock.uptimeMillis()
        audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
        audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
    }
}