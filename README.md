# MediaShortcuts

A tiny Android app that exposes generic **media-control shortcuts** as separate launcher entries:

- **Next** — skips to the next track in whatever app currently holds the media session
- **Previous** — skips to the previous track in whatever app currently holds the media session

Works with SoundCloud, YouTube Music, Spotify, Poweramp, VLC, podcast apps, anything with a MediaSession. Same way Bluetooth headphone buttons work — Android routes the key event to whoever is playing or paused most recently.

## Why generic instead of per-app

Earlier versions of this app declared one shortcut per app (SC Next, YTM Next, etc.). That's unnecessary: when you broadcast a media key without targeting a package, Android's MediaSession framework dispatches it to the active session automatically. Two shortcuts cover every media app you'll ever install, with no manifest edits.

If you specifically want a per-app version anyway (e.g. you keep two sessions paused and want to skip in a specific one), see the "Per-app variants" section at the bottom.

## How it works

`AndroidManifest.xml` declares one real activity (`DispatcherActivity`) and two `<activity-alias>` entries. Each alias has its own icon, label, and a `<meta-data>` tag telling the dispatcher which key to send. Android treats each alias as an independent launchable activity, so your launcher lists them separately.

The dispatcher builds a `KeyEvent`, wraps it in `Intent.ACTION_MEDIA_BUTTON`, and broadcasts it. Both KEY_DOWN and KEY_UP are sent. Activity is `Theme.NoDisplay` — no UI flashes, no permissions required.

## Caveats

- Some media app needs to currently hold (or have recently held) the media session. If you've fully killed every player since reboot, the shortcuts have nothing to dispatch to. Start playback in any app once and the shortcuts work from then on, including from the lock screen.
- "Most recent session wins" — if you paused SoundCloud and then played a YouTube video, Next/Previous will go to the YouTube video. This is Android's behavior, not a bug.
- A handful of apps only implement modern MediaSession callbacks and ignore the legacy broadcast. SoundCloud, YouTube Music, Spotify, Poweramp, and VLC all work as of 2026.

## Build

Requires JDK 17.

```bash
./gradlew assembleDebug
```

APK at `app/build/outputs/apk/debug/app-debug.apk`. Or push to GitHub — the included Action builds it for you and uploads as an artifact.

## Adding more shortcuts

Want Play/Pause, Stop, or Fast Forward too? Open `AndroidManifest.xml`, copy any `<activity-alias>` block, and change:

- `android:name` (must be unique)
- `android:label` (what shows in the launcher)
- `android:icon` (drawable resource)
- `<meta-data android:name="key_code" android:value="..."/>`

Valid `key_code` values:
`next`, `previous`, `play_pause`, `play`, `pause`, `stop`, `rewind`, `fast_forward`

## Per-app variants

To target a specific app instead of routing to the active session, add a `target_package` meta-data entry alongside `key_code`:

```xml
<meta-data android:name="key_code" android:value="next" />
<meta-data android:name="target_package" android:value="com.soundcloud.android" />
```

The dispatcher will call `setPackage()` on the intent. Useful only if you regularly keep multiple paused sessions and need to disambiguate. For most people, the generic version is more reliable.

Common package names:
- SoundCloud: `com.soundcloud.android`
- YouTube Music: `com.google.android.apps.youtube.music`
- Spotify: `com.spotify.music`
- Poweramp: `com.maxmpz.audioplayer`
- VLC: `org.videolan.vlc`

## License

MIT.
