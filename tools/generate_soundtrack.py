#!/usr/bin/env python3
"""Generate 14 self-contained fallback music segments when original tracks are absent.

This project does not store the original soundtrack. These independently composed
synthesized placeholders allow fresh GitHub builds to have working audio. To
restore original songs place matching *_intro.m4a and *_loop.m4a into assets/music.
Existing files are NEVER overwritten.
"""
from __future__ import annotations
import array
import math
from pathlib import Path
import shutil
import subprocess
import tempfile
import wave

OUT = Path("app/src/main/assets/music")
OUT.mkdir(parents=True, exist_ok=True)
FFMPEG = shutil.which("ffmpeg")
if not FFMPEG:
    raise SystemExit("ffmpeg is required to encode bundled AAC music")
RATE = 22050
TWOPI = 2 * math.pi
TRACKS = [
  ("pixel_classic", 126, [0, 2, 4, 7, 9, 7, 4, 2], [0, 5, 7, 3], "square"),
  ("neon_drive", 138, [0, 7, 12, 7, 5, 9, 7, 4], [0, 8, 5, 10], "saw"),
  ("cinematic_rise", 108, [0, 4, 7, 12, 9, 7, 5, 4], [0, 5, 9, 7], "sine"),
  ("battle_99", 152, [0, 3, 7, 10, 12, 10, 7, 3], [0, 10, 8, 7], "saw"),
  ("swing_shift", 120, [0, 4, 7, 10, 9, 7, 4, 2], [0, 5, 7, 10], "sine"),
  ("hardbass_rush", 166, [0, 0, 12, 7, 0, 10, 7, 3], [0, 8, 10, 7], "square"),
  ("neon_reactor", 146, [0, 7, 5, 12, 9, 5, 3, 7], [0, 5, 3, 8], "saw"),
]

def note(n, octave=0):
    return 110.0 * (2 ** ((n + 12 * octave) / 12))

def osc(kind, phase):
    p = phase % 1.0
    if kind == "square": return 0.7 * (1 if p < .5 else -1)
    if kind == "saw": return 0.85 * (2 * p - 1)
    return math.sin(TWOPI * p)

def render(bpm, melody, chords, voice, intro):
    beats = 8 if intro else 16  # two-bar first pass, then independent four-bar loop
    period = 60.0 / bpm
    length = round(beats * period * RATE)
    result = array.array("h")
    seed = 0
    for i in range(length):
        t = i / RATE
        pos = t / period
        beat = int(pos)
        beat_fraction = pos - beat
        step = int(pos * 2)
        step_fraction = pos * 2 - step
        idx = step % len(melody)
        root = chords[(beat // 4) % len(chords)]
        if intro:
            idx = (idx + 2) % len(melody)
        # melodic upper voice and syncopated pluck
        pitch = note(melody[idx] + root, 1)
        lead_env = max(0.0, 1.0 - step_fraction * 1.20)
        lead = osc(voice, t * pitch) * lead_env * .17
        if intro:
            lead *= min(1.0, t / .6) * .82
        # harmonized arpeggiator adds bright audibility on small phone speakers
        arp_note = root + [0, 7, 12, 7][int(pos * 4) % 4]
        arp = math.sin(TWOPI * t * note(arp_note, 2)) * .095 * (1 - (pos * 4 % 1))
        bass = (math.sin(TWOPI * t * note(root, -1)) +
                .30 * math.sin(TWOPI * t * note(root, 0))) * .23 * (1 - beat_fraction * .5)
        # deterministic bass drum on beats 1 and 3, soft snare on 2 and 4
        kick_age = beat_fraction
        kick = math.sin(TWOPI * (57 * kick_age + 38 * kick_age * kick_age)) * .25 * math.exp(-kick_age * 15)
        snare_age = beat_fraction
        seed = (1664525 * seed + 1013904223) & 0xffffffff
        white = (seed / 0xffffffff) * 2 - 1
        snare = white * .09 * math.exp(-snare_age * 21) if beat % 2 else 0.0
        value = lead + arp + bass + kick + snare
        edge = min(1., i / (RATE * .018), (length - i - 1) / (RATE * .018))
        value = max(-.95, min(.95, value * .84 * max(0,edge)))
        result.append(round(value * 32767))
    return result

def encode(dest, samples):
    with tempfile.TemporaryDirectory() as d:
        raw = Path(d) / "audio.wav"
        with wave.open(str(raw), "wb") as out:
            out.setnchannels(1)
            out.setsampwidth(2)
            out.setframerate(RATE)
            out.writeframes(samples.tobytes())
        subprocess.run([FFMPEG, "-hide_banner", "-loglevel", "error", "-y",
                        "-i", str(raw), "-vn", "-c:a", "aac", "-b:a", "96k",
                        "-movflags", "+faststart", str(dest)], check=True)

def main():
    for ident, bpm, melody, chords, voice in TRACKS:
        for part in ("intro", "loop"):
            dest = OUT / f"{ident}_{part}.m4a"
            if dest.is_file() and dest.stat().st_size > 1000:
                print(f"Preserving original {dest}")
                continue
            print(f"Generating fallback {dest}", flush=True)
            encode(dest, render(bpm, melody, chords, voice, part == "intro"))
    files = sorted(OUT.glob("*.m4a"))
    if len(files) != 14:
        raise SystemExit(f"Expected 14 audio assets, found {len(files)}")

if __name__ == "__main__":
    main()
