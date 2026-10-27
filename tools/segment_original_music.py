#!/usr/bin/env python3
"""Cut original uploaded music to Neon Blocks' 14 intro/loop asset filenames.

Requires ffmpeg, numpy and seven appropriately named M4A sources in
soundtrack-sources/. This script does not fetch or redistribute recordings.
Original sources are not included in this public repository.
"""
import json, subprocess
from pathlib import Path
import numpy as np

PROJECT=Path(__file__).resolve().parent.parent
CFG=json.loads((PROJECT/"soundtrack/loop_points.json").read_text())
SOURCES=PROJECT/CFG["source_dir"]
OUT=PROJECT/CFG["target_dir"]
RATE=44100

def decode(file,end):
    return np.frombuffer(subprocess.check_output([
        "ffmpeg","-hide_banner","-loglevel","error","-i",str(file),
        "-t",str(end+0.2),"-ar",str(RATE),"-ac","2","-f","f32le","-"]),
        dtype="<f4").copy().reshape((-1,2))

def encode(path,data):
    import tempfile
    with tempfile.TemporaryDirectory() as tmp:
        raw=Path(tmp)/"source.pcm"
        raw.write_bytes(np.asarray(data,dtype="<f4").tobytes())
        subprocess.run(["ffmpeg","-hide_banner","-loglevel","error","-y",
          "-f","f32le","-ar",str(RATE),"-ac","2","-i",str(raw),
          "-c:a","aac","-b:a","192k","-movflags","+faststart",str(path)],check=True)

def main():
    OUT.mkdir(parents=True,exist_ok=True)
    for name,config in CFG["tracks"].items():
        candidates=list(SOURCES.glob(config["source"]+"*.m4a"))
        if len(candidates)!=1:
            raise RuntimeError("Expected one original source for "+name+" matching "+config["source"]+", found "+str(len(candidates)))
        start,end=config["start"],config["end"]
        audio=decode(candidates[0],end)
        first,last=round(start*RATE),round(end*RATE)
        fade=round(CFG["crossfade_seconds"]*RATE)
        if last>len(audio) or first+fade>=last-fade:raise RuntimeError("Invalid cut "+name)
        alpha=(.5-.5*np.cos(np.linspace(0,np.pi,fade,dtype=np.float32)))[:,None]
        joint=audio[last-fade:last]*(1-alpha)+audio[first:first+fade]*alpha
        intro=audio[:last-fade].copy()
        loop=np.concatenate([joint,audio[first+fade:last-fade]],axis=0)
        edge=round(CFG["edge_fade_seconds"]*RATE)
        ramp=(.5-.5*np.cos(np.linspace(0,np.pi,edge,dtype=np.float32)))[:,None]
        for piece in [intro,loop]:
            piece[:edge]*=ramp
            piece[-edge:]*=ramp[::-1]
        encode(OUT/(name+"_intro.m4a"),np.clip(intro,-.985,.985))
        encode(OUT/(name+"_loop.m4a"),np.clip(loop,-.985,.985))
        print(name,round(len(intro)/RATE,2),round(len(loop)/RATE,2),flush=True)

if __name__=="__main__":main()
