# Neon Blocks

An original **offline Android falling-block puzzle** with neon graphics, levels, scoring, ghost pieces, hold, next queue, touch/swipe gestures, and on-screen controls.

## Install the APK

1. Open [GitHub Actions — Build Android APK](https://github.com/Yaushi420/Neon-Blocks/actions/workflows/android.yml).
2. Select the most recent **successful** run.
3. Under **Artifacts**, download **Neon-Blocks-APK**.
4. Extract the downloaded ZIP and install `app-debug.apk` on your Android phone. You may need to allow installation from your file manager.

The APK is a debug build, signed automatically by the Android debug keystore. A production release would require a separate signing key.

## Music Library

Open the **♫** menu to:
- Enable or disable individual songs without deleting them.
- Choose **Play through** or **Shuffle**.
- Skip to the next track or preview a selected sequence.
- Import multiple MP3, M4A, OGG, WAV, or other Android-supported audio files from your phone.
- Remove imported tracks entirely when desired.

Imported files are copied into private offline app storage (IndexedDB within Android WebView). Clearing the app's data or uninstalling it also removes this music library. Import depends on your Android WebView supporting this storage API.

Four original synthesized takes on the public-domain folk melody **Korobeiniki** ship with the game: Arcade Classic, Neon Synthwave, 8-Bit Remix, and Dance Pulse. These are newly synthesized interpretations, not recordings extracted from the official Tetris game.

The game does not need network connectivity and declares no Internet permission.

## Controls

Swipe left/right to move; swipe down to drop; swipe up or tap to rotate; double tap to hard drop. Alternatively use the six visible on-screen buttons. Hold is limited to once per placed piece. Settings include music, sound effects, vibration, and the ghost piece.

## Build

The workflow uses JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3 and Android SDK 35. Pushes to `main` trigger automatic builds. You can also launch a run with **Run workflow**.

This independent game is **not affiliated with Tetris Holding** or the official Tetris mobile app.
