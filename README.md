# Neon Blocks

An offline Android WebView falling-block game with a 10×20 board, bright neon blocks, touch gestures, Hold, Next, ghost, haptic feedback, score progression, and line-clear animations.

## Android builds

The existing [Android GitHub Actions workflow](https://github.com/Yaushi420/Neon-Blocks/actions/workflows/android.yml) builds a debug APK on pushes to `main`. The APK uses the GitHub runner's debug signing certificate, **which may differ across build runs**. A newer APK must use the same certificate to update over an installed version.

## Gameplay and scores

Every tap rotates, including two quick taps. Swipe left/right moves. Swipe down soft-drops; only a fast downward flick triggers the animated hard drop. Swipe up or tap Hold to save the piece. The game has a short grace period after hard drops, a top-right menu, sound options, and seven random selectable tracks.

After each run you can enter a name for an **on-device leaderboard** (top 30). Scores are stored locally in the WebView and are not synchronized between devices.

## Soundtrack

Gapless looping uses a decoded Web Audio buffer on supported Android versions; HTML audio remains as a compatibility fallback. Seven built-in slots: Pixel Classic, Neon Drive, Cinematic Rise, Battle 99, Swing Shift, Hardbass Rush, and Neon Reactor.

Each built-in track uses **two files**:
- `app/src/main/assets/music/<track_id>_intro.m4a` (opening + first main passage, once)
- `app/src/main/assets/music/<track_id>_loop.m4a` (repeatable main passage)

The game also supports per-track enable/disable, an individual max speed, TEST MAX, random track selection per run, and importing songs locally.

**Repository limitation:** The seven uploaded commercial/remix recordings and derived 14 audio files are *not stored in this public GitHub repository*. When absent, `tools/generate_soundtrack.py` generates 14 placeholder files before GitHub's Android build. The locally packaged Original Soundtrack APK in the chat is different from a standard GitHub debug artifact until these replacements are provided.

The v2.1 extended loop points (approximately 63–179 seconds per repeat) and smooth crossfade settings are documented in `soundtrack/loop_points.json`. To recreate the original files with the required permission to use and redistribute the recordings:

1. Supply seven source .m4a files in `soundtrack-sources/`, with filename prefixes matching the `source` field in `soundtrack/loop_points.json`.
2. Install Python numpy and FFmpeg, then run `python3 tools/segment_original_music.py`.
3. Verify the 14 files are present in `app/src/main/assets/music/`. GitHub Actions preserves existing music files and only fills in missing slots with placeholders.
4. Build using the existing Android workflow. For update installation, use the same private signing key as the user's installed APK.

The original inputs remain unchanged. Check each recording's rights before publishing the audio or distributing an APK publicly.

## Implementation

Android SDK 35, Java 17, Gradle build system. The app uses local WebView storage and declares no Internet permission.

This independent project is not affiliated with Tetris Holding or the official Tetris mobile app.
