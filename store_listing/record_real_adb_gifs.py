#!/usr/bin/env python3
"""
Automates real device interactions via ADB on the active Android device (Pixel 10 Pro)
and generates 100% real, authentic screen recordings & animated GIFs:
1. real_quicknote_sticky_pinning.gif (Typing 'qn Zarif: Finish lab report', pressing Enter, pulling down notification shade to show sticky alert)
2. real_pomodoro_focus_sprint.gif (Typing 'pomo 50m', live home screen countdown widget ticking)
3. real_notification_hub_swipe.gif (Swiping into Hub, viewing live alerts and tapping export)
"""

import os
import time
import subprocess
from PIL import Image

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SHOWCASE_DIR = os.path.join(ROOT_DIR, "showcase")
GIFS_DIR = os.path.join(SHOWCASE_DIR, "gifs")
TMP_DIR = os.path.join(SHOWCASE_DIR, ".tmp_adb_record")

os.makedirs(GIFS_DIR, exist_ok=True)
os.makedirs(TMP_DIR, exist_ok=True)

def adb(cmd):
    full_cmd = f"adb {cmd}"
    res = subprocess.run(full_cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    return res.stdout.strip()

def screencap(save_path):
    subprocess.run(f"adb exec-out screencap -p > '{save_path}'", shell=True, check=True)

def compile_gif(frame_dir, out_gif, fps=6, scale_w=400):
    cmd = [
        "ffmpeg", "-y",
        "-framerate", str(fps),
        "-i", os.path.join(frame_dir, "f_%03d.png"),
        "-vf", f"fps={fps},scale={scale_w}:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=96:reserve_transparent=0[p];[s1][p]paletteuse=dither=bayer:bayer_scale=3",
        out_gif
    ]
    subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    print(f"Generated real GIF: {out_gif}")

# ==============================================================================
# REAL SCENARIO 1: Quick Note & Sticky Pinning via ADB
# ==============================================================================
def record_real_quicknote():
    print("\n--- Recording Real Scenario 1: Quick Note & Sticky Pinning ---")
    fdir = os.path.join(TMP_DIR, "qn")
    os.makedirs(fdir, exist_ok=True)
    frame_i = 0
    
    # 1. Ensure on home screen
    adb("shell input keyevent KEYCODE_HOME")
    time.sleep(0.5)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    # 2. Tap Command Bar
    adb("shell input tap 400 2240")
    time.sleep(0.4)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    # 3. Type "qn Zarif: Finish lab report & deploy v1.9.9"
    chunks = [
        "qn",
        "%sZarif:",
        "%sFinish",
        "%slab",
        "%sreport",
        "%s&",
        "%sdeploy",
        "%sv1.9.9"
    ]
    for c in chunks:
        adb(f"shell input text '{c}'")
        time.sleep(0.2)
        screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
        frame_i += 1
        
    time.sleep(0.4)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    # 4. Press Enter (KEYCODE_ENTER = 66)
    adb("shell input keyevent 66")
    time.sleep(0.5)
    for _ in range(3):
        screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
        frame_i += 1
        time.sleep(0.2)
        
    # 5. Pull down Android Notification Shade to show sticky pinned notification
    adb("shell cmd statusbar expand-notifications")
    time.sleep(0.6)
    for _ in range(5):
        screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
        frame_i += 1
        time.sleep(0.2)
        
    # 6. Collapse notification shade
    adb("shell cmd statusbar collapse")
    time.sleep(0.4)
    for _ in range(3):
        screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
        frame_i += 1
        time.sleep(0.2)
        
    out_gif = os.path.join(GIFS_DIR, "01_quicknote_sticky_pinning.gif")
    compile_gif(fdir, out_gif, fps=4, scale_w=380)

# ==============================================================================
# REAL SCENARIO 2: Pomodoro Focus Sprint (pomo 50m) via ADB
# ==============================================================================
def record_real_pomodoro():
    print("\n--- Recording Real Scenario 2: Pomodoro Focus Sprint ---")
    fdir = os.path.join(TMP_DIR, "pomo")
    os.makedirs(fdir, exist_ok=True)
    frame_i = 0
    
    adb("shell input keyevent KEYCODE_HOME")
    time.sleep(0.5)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    adb("shell input tap 400 2240")
    time.sleep(0.3)
    
    # Type pomo 50m
    adb("shell input text 'pomo'")
    time.sleep(0.2)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    adb("shell input text '%s50m'")
    time.sleep(0.3)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    # Press Enter
    adb("shell input keyevent 66")
    time.sleep(0.6)
    
    # Capture live countdown widget ticking
    for _ in range(8):
        screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
        frame_i += 1
        time.sleep(0.8)
        
    out_gif = os.path.join(GIFS_DIR, "02_pomodoro_focus_sprint.gif")
    compile_gif(fdir, out_gif, fps=3, scale_w=380)

# ==============================================================================
# REAL SCENARIO 3: Notification Hub & AI Export via ADB
# ==============================================================================
def record_real_hub():
    print("\n--- Recording Real Scenario 3: Notification Hub & AI Export ---")
    fdir = os.path.join(TMP_DIR, "hub")
    os.makedirs(fdir, exist_ok=True)
    frame_i = 0
    
    adb("shell input keyevent KEYCODE_HOME")
    time.sleep(0.5)
    
    # Tap "<- hub" button at bottom-left [120, 2100]
    adb("shell input tap 120 2110")
    time.sleep(0.8)
    
    # In Hub screen
    for _ in range(3):
        screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
        frame_i += 1
        time.sleep(0.3)
        
    # Tap SAVE: ON toggle at top right [960, 310]
    adb("shell input tap 960 310")
    time.sleep(0.4)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    adb("shell input tap 960 310")
    time.sleep(0.4)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    # Tap "download" button at bottom [150, 2330]
    adb("shell input tap 150 2330")
    time.sleep(0.5)
    screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
    frame_i += 1
    
    # Tap "ai export" button at bottom [60, 2330]
    adb("shell input tap 60 2330")
    time.sleep(0.7)
    
    # Export sheet is open
    for _ in range(4):
        screencap(os.path.join(fdir, f"f_{frame_i:03d}.png"))
        frame_i += 1
        time.sleep(0.3)
        
    out_gif = os.path.join(GIFS_DIR, "03_notification_hub_ai_and_download.gif")
    compile_gif(fdir, out_gif, fps=3, scale_w=380)

def main():
    record_real_quicknote()
    record_real_pomodoro()
    record_real_hub()
    print("\nAll 3 real ADB device recordings compiled into GIFs successfully!")

if __name__ == "__main__":
    main()

