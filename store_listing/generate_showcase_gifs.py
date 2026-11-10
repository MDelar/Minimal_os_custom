#!/usr/bin/env python3
"""
Generate ultra-high-quality animated GIFs and visual graphics for Minimal OS features.
Real examples featuring Zarif (MD. Zarif Azfar) doing tasks:
1. Quick Note Sticky Notification Pinning (qn Zarif: Finish lab report & deploy v1.9.8)
2. Live Pomodoro Deep Work Focus Sprint (pomo 50m)
3. Notification Hub: Live Music, Day-Wise Log, Download to Device & AI Assistant Export
4. Unified Command Bar Fuzzy Search & App Uninstallation
"""

import os
import shutil
import subprocess
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SHOWCASE_DIR = os.path.join(ROOT_DIR, "showcase")
GIFS_DIR = os.path.join(SHOWCASE_DIR, "gifs")
GRAPHICS_DIR = os.path.join(SHOWCASE_DIR, "graphics")
TMP_FRAMES_DIR = os.path.join(SHOWCASE_DIR, ".tmp_frames")

os.makedirs(GIFS_DIR, exist_ok=True)
os.makedirs(GRAPHICS_DIR, exist_ok=True)

# Color Palette
COLOR_BLACK = (10, 11, 14, 255)
COLOR_CARD_BG = (22, 24, 30, 255)
COLOR_DARK_CARD = (16, 17, 22, 255)
COLOR_RED = (255, 59, 48, 255)
COLOR_WHITE = (255, 255, 255, 255)
COLOR_GRAY = (150, 155, 170, 255)
COLOR_DARK_GRAY = (75, 80, 95, 255)

def get_font(size, mono=False, bold=False):
    if mono:
        paths = ["/System/Library/Fonts/SFNSMono.ttf", "/System/Library/Fonts/Menlo.ttc"]
    elif bold:
        paths = ["/System/Library/Fonts/SFNS.ttf", "/System/Library/Fonts/SFProDisplay-Bold.otf"]
    else:
        paths = [
            os.path.join(ROOT_DIR, "app/src/main/res/font/inter_regular.ttf"),
            "/System/Library/Fonts/SFNS.ttf"
        ]
    for p in paths:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size)
            except Exception:
                continue
    return ImageFont.load_default()

def draw_status_bar(draw, w, time_str="10:42 AM", battery_str="98%"):
    font = get_font(18, bold=True)
    draw.text((36, 18), time_str, font=font, fill=COLOR_WHITE)
    
    font_bat = get_font(16, bold=False)
    bb = font_bat.getbbox(battery_str)
    bw = bb[2] - bb[0]
    draw.text((w - 36 - bw, 18), battery_str, font=font_bat, fill=COLOR_WHITE)
    # battery icon
    bx = w - 28
    draw.rounded_rectangle([bx, 20, bx + 16, 32], radius=3, outline=COLOR_WHITE, width=1)
    draw.rectangle([bx + 2, 22, bx + 14, 30], fill=COLOR_WHITE)

def draw_command_bar(draw, w, y, text="", trigger=""):
    h = 68
    draw.rounded_rectangle([24, y, w - 24, y + h], radius=16, fill=COLOR_CARD_BG, outline=COLOR_DARK_GRAY, width=1)
    
    font = get_font(20, bold=True)
    draw.text((42, y + 22), ">", font=font, fill=COLOR_RED)
    
    cx = 64
    if trigger:
        draw.text((cx, y + 22), trigger, font=font, fill=COLOR_RED)
        tb = font.getbbox(trigger)
        cx += (tb[2] - tb[0])
        
    if text:
        draw.text((cx, y + 22), text, font=font, fill=COLOR_WHITE)
        tb = font.getbbox(text)
        cx += (tb[2] - tb[0])
        
    # Blinking cursor
    draw.rectangle([cx + 3, y + 22, cx + 5, y + 46], fill=COLOR_RED)

def compile_gif_with_ffmpeg(frame_dir, output_path, fps=12, width=480):
    cmd = [
        "ffmpeg", "-y",
        "-framerate", str(fps),
        "-i", os.path.join(frame_dir, "frame_%04d.png"),
        "-vf", f"fps={fps},scale={width}:-1:flags=lanczos,split[s0][s1];[s0]palettegen=max_colors=128:reserve_transparent=0[p];[s1][p]paletteuse=dither=bayer:bayer_scale=3",
        output_path
    ]
    subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    print(f"Generated: {output_path}")

# ==============================================================================
# GIF 1: Quick Note & Sticky Notification Bar Pinning
# ==============================================================================
def generate_quicknote_gif():
    w, h = 540, 1170
    frames_dir = os.path.join(TMP_FRAMES_DIR, "quicknote")
    shutil.rmtree(frames_dir, ignore_errors=True)
    os.makedirs(frames_dir, exist_ok=True)
    
    frame_idx = 0
    full_prompt = "Zarif: Finish lab report & deploy v1.9.8"
    
    # Phase 1: Typing in Command Bar (Frames 0 - 22)
    for i in range(23):
        img = Image.new("RGBA", (w, h), COLOR_BLACK)
        draw = ImageDraw.Draw(img)
        draw_status_bar(draw, w)
        
        # Home Screen Clock & Date
        draw.text((36, 120), "10:42", font=get_font(72, bold=True), fill=COLOR_WHITE)
        draw.text((36, 205), "SUN, NOV 10 · 24°C DHAKA", font=get_font(18, bold=False), fill=COLOR_GRAY)
        
        # Home Shortcuts
        shortcuts = ["PHONE", "MESSAGES", "CHROME", "GITHUB", "STUDIO"]
        sy = 300
        for s in shortcuts:
            draw.text((36, sy), s, font=get_font(26, bold=True), fill=COLOR_WHITE)
            sy += 50
            
        # Typing progression
        cmd_y = 660
        if i == 0:
            draw_command_bar(draw, w, cmd_y, text="", trigger="")
        elif i == 1:
            draw_command_bar(draw, w, cmd_y, text="", trigger="q")
        elif i == 2:
            draw_command_bar(draw, w, cmd_y, text=" ", trigger="qn")
        else:
            char_count = int((i - 2) * (len(full_prompt) / 18.0))
            sub_text = " " + full_prompt[:char_count]
            draw_command_bar(draw, w, cmd_y, text=sub_text, trigger="qn")
            
            # Action preview badge
            draw.rounded_rectangle([24, cmd_y - 48, w - 24, cmd_y - 12], radius=10, fill=COLOR_DARK_CARD, outline=COLOR_RED, width=1)
            draw.text((38, cmd_y - 40), "📌 PIN TO NOTIFICATION BAR", font=get_font(14, bold=True), fill=COLOR_RED)
            draw.text((250, cmd_y - 39), "Ongoing sticky alert", font=get_font(13, bold=False), fill=COLOR_GRAY)
            
        img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
        frame_idx += 1
        
    # Phase 2: Enter key pressed -> Terminal Buffer card slides up (Frames 23 - 28)
    for i in range(6):
        img = Image.new("RGBA", (w, h), COLOR_BLACK)
        draw = ImageDraw.Draw(img)
        draw_status_bar(draw, w)
        draw.text((36, 120), "10:42", font=get_font(72, bold=True), fill=COLOR_WHITE)
        draw.text((36, 205), "SUN, NOV 10 · 24°C DHAKA", font=get_font(18, bold=False), fill=COLOR_GRAY)
        
        # Buffer card
        card_y = 540
        draw.rounded_rectangle([24, card_y, w - 24, card_y + 80], radius=12, fill=COLOR_CARD_BG, outline=COLOR_DARK_GRAY, width=1)
        draw.rounded_rectangle([36, card_y + 14, 85, card_y + 36], radius=4, fill=COLOR_RED)
        draw.text((44, card_y + 17), "#1", font=get_font(12, bold=True), fill=COLOR_WHITE)
        draw.text((95, card_y + 16), "PINNED TO SYSTEM BAR", font=get_font(12, bold=True), fill=COLOR_RED)
        draw.text((36, card_y + 44), f"qn {full_prompt}", font=get_font(15, bold=False), fill=COLOR_WHITE)
        
        # Command bar cleared
        draw_command_bar(draw, w, 660, text="", trigger="")
        
        img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
        frame_idx += 1
        
    # Phase 3: Android Notification Shade Slides Down (Frames 29 - 42)
    for i in range(14):
        img = Image.new("RGBA", (w, h), COLOR_BLACK)
        draw = ImageDraw.Draw(img)
        draw_status_bar(draw, w)
        
        # Shade slide down offset
        slide_pct = min(1.0, (i + 1) / 8.0)
        shade_y = -h * (1.0 - slide_pct)
        
        # Shade overlay
        overlay = Image.new("RGBA", (w, h), (14, 15, 20, 245))
        od = ImageDraw.Draw(overlay)
        
        # Shade clock
        od.text((36, 50), "10:42", font=get_font(42, bold=True), fill=COLOR_WHITE)
        od.text((36, 100), "Sunday, November 10", font=get_font(16, bold=False), fill=COLOR_GRAY)
        
        # Sticky Notification Card
        notif_y = 160
        od.rounded_rectangle([20, notif_y, w - 20, notif_y + 130], radius=18, fill=COLOR_CARD_BG, outline=COLOR_RED, width=1)
        # Red Pin Icon
        od.ellipse([38, notif_y + 20, 56, notif_y + 38], fill=COLOR_RED)
        od.text((68, notif_y + 20), "MINIMAL OS // QUICK NOTE", font=get_font(14, bold=True), fill=COLOR_RED)
        od.text((w - 75, notif_y + 20), "STAY", font=get_font(12, bold=True), fill=COLOR_DARK_GRAY)
        
        # Note content
        od.text((38, notif_y + 52), f"\"{full_prompt}\"", font=get_font(16, bold=True), fill=COLOR_WHITE)
        
        # Action buttons
        od.rounded_rectangle([38, notif_y + 88, 120, notif_y + 116], radius=6, fill=COLOR_DARK_CARD, outline=COLOR_DARK_GRAY, width=1)
        od.text((56, notif_y + 94), "CLEAR", font=get_font(12, bold=True), fill=COLOR_RED)
        
        od.rounded_rectangle([135, notif_y + 88, 230, notif_y + 116], radius=6, fill=COLOR_DARK_CARD, outline=COLOR_DARK_GRAY, width=1)
        od.text((150, notif_y + 94), "OPEN KEEP", font=get_font(12, bold=True), fill=COLOR_WHITE)
        
        img.paste(overlay, (0, int(shade_y)), overlay)
        img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
        frame_idx += 1
        
    out_gif = os.path.join(GIFS_DIR, "01_quicknote_sticky_pinning.gif")
    compile_gif_with_ffmpeg(frames_dir, out_gif, fps=10, width=460)

# ==============================================================================
# GIF 2: Pomodoro Focus Sprint (pomo 50m)
# ==============================================================================
def generate_pomodoro_gif():
    w, h = 540, 1170
    frames_dir = os.path.join(TMP_FRAMES_DIR, "pomodoro")
    shutil.rmtree(frames_dir, ignore_errors=True)
    os.makedirs(frames_dir, exist_ok=True)
    
    frame_idx = 0
    
    # Phase 1: Typing "pomo 50m" (Frames 0 - 15)
    typed = "pomo 50m"
    for i in range(16):
        img = Image.new("RGBA", (w, h), COLOR_BLACK)
        draw = ImageDraw.Draw(img)
        draw_status_bar(draw, w)
        draw.text((36, 120), "10:45", font=get_font(72, bold=True), fill=COLOR_WHITE)
        draw.text((36, 205), "DEEP WORK // ZARIF AZFAR", font=get_font(18, bold=True), fill=COLOR_RED)
        
        # Typing
        cmd_y = 580
        if i < 5:
            draw_command_bar(draw, w, cmd_y, text="", trigger="pomo"[:i])
        else:
            arg = typed[4:i]
            draw_command_bar(draw, w, cmd_y, text=arg, trigger="pomo")
            # Suggestion badge
            draw.rounded_rectangle([24, cmd_y - 45, w - 24, cmd_y - 12], radius=8, fill=COLOR_DARK_CARD, outline=COLOR_RED, width=1)
            draw.text((38, cmd_y - 38), "⚡ START POMODORO // 50 MINUTE SPRINT", font=get_font(13, bold=True), fill=COLOR_RED)
            
        img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
        frame_idx += 1
        
    # Phase 2: Live Countdown Timer on Home Screen (Frames 16 - 38)
    times = [
        ("50:00", 0.0), ("49:59", 0.05), ("49:58", 0.10), ("49:57", 0.15),
        ("49:56", 0.20), ("49:55", 0.25), ("49:54", 0.30), ("49:53", 0.35),
        ("49:52", 0.40), ("49:51", 0.45), ("49:50", 0.50)
    ]
    for time_str, progress in times:
        for repeat in range(2):
            img = Image.new("RGBA", (w, h), COLOR_BLACK)
            draw = ImageDraw.Draw(img)
            draw_status_bar(draw, w)
            draw.text((36, 120), "10:45", font=get_font(72, bold=True), fill=COLOR_WHITE)
            draw.text((36, 205), "DEEP WORK // ZARIF AZFAR", font=get_font(18, bold=True), fill=COLOR_RED)
            
            # Live Pomodoro Card
            p_y = 300
            p_h = 190
            draw.rounded_rectangle([24, p_y, w - 24, p_y + p_h], radius=20, fill=COLOR_CARD_BG, outline=COLOR_DARK_GRAY, width=1)
            
            # Header
            draw.text((42, p_y + 22), "POMODORO FOCUS", font=get_font(14, bold=True), fill=COLOR_RED)
            draw.text((w - 145, p_y + 22), "SPRINT 1/4", font=get_font(13, bold=True), fill=COLOR_GRAY)
            
            # Large Time
            draw.text((42, p_y + 54), time_str, font=get_font(52, bold=True), fill=COLOR_WHITE)
            
            # Animated Play/Pause Button
            bx = w - 90
            by = p_y + 60
            draw.ellipse([bx, by, bx + 48, by + 48], fill=COLOR_DARK_CARD, outline=COLOR_RED, width=1)
            # Pause bars
            draw.rectangle([bx + 18, by + 16, bx + 22, by + 32], fill=COLOR_RED)
            draw.rectangle([bx + 26, by + 16, bx + 30, by + 32], fill=COLOR_RED)
            
            # Glowing Progress Bar
            bar_y = p_y + 140
            bar_w = w - 84
            draw.rounded_rectangle([42, bar_y, 42 + bar_w, bar_y + 10], radius=5, fill=COLOR_DARK_CARD)
            fill_w = max(14, int(bar_w * (0.15 + progress * 0.85)))
            draw.rounded_rectangle([42, bar_y, 42 + fill_w, bar_y + 10], radius=5, fill=COLOR_RED)
            
            # Micro subtitle
            draw.text((42, p_y + 160), "Minimal OS Live Engine · Screen Stays Dark", font=get_font(12, bold=False), fill=COLOR_DARK_GRAY)
            
            # Clean Command Bar beneath
            draw_command_bar(draw, w, 540, text="", trigger="")
            
            img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
            frame_idx += 1
            
    out_gif = os.path.join(GIFS_DIR, "02_pomodoro_focus_sprint.gif")
    compile_gif_with_ffmpeg(frames_dir, out_gif, fps=10, width=460)

# ==============================================================================
# GIF 3: Notification Hub, Day-Wise Log, Download to Device & AI Export
# ==============================================================================
def generate_hub_ai_gif():
    w, h = 540, 1170
    frames_dir = os.path.join(TMP_FRAMES_DIR, "hub_ai")
    shutil.rmtree(frames_dir, ignore_errors=True)
    os.makedirs(frames_dir, exist_ok=True)
    
    frame_idx = 0
    
    # Phase 1: In Hub Screen with Now Playing and Alerts (Frames 0 - 15)
    for i in range(16):
        img = Image.new("RGBA", (w, h), COLOR_BLACK)
        draw = ImageDraw.Draw(img)
        draw_status_bar(draw, w)
        
        # Hub Header
        draw.text((36, 70), "← hub", font=get_font(32, bold=True), fill=COLOR_WHITE)
        draw.text((36, 115), "live alerts · music playback · local archive", font=get_font(13, bold=False), fill=COLOR_GRAY)
        
        # Now Playing Music Card (Audio Wave Animation)
        my = 150
        mh = 120
        draw.rounded_rectangle([24, my, w - 24, my + mh], radius=16, fill=COLOR_CARD_BG, outline=COLOR_DARK_GRAY, width=1)
        # Music Note
        draw.ellipse([42, my + 24, 78, my + 60], fill=COLOR_DARK_CARD, outline=COLOR_RED, width=1)
        draw.text((54, my + 32), "♫", font=get_font(18, bold=True), fill=COLOR_RED)
        draw.text((92, my + 24), "Lofi Coding Beats", font=get_font(17, bold=True), fill=COLOR_WHITE)
        draw.text((92, my + 48), "Zarif Azfar // Deep Focus Session", font=get_font(13, bold=False), fill=COLOR_GRAY)
        
        # Animated Waveform bars
        wave_heights = [12, 22, 34, 18, 28, 14, 26, 32, 16, 20]
        shift = i % len(wave_heights)
        curr_waves = wave_heights[shift:] + wave_heights[:shift]
        wx = 42
        for wh_val in curr_waves:
            draw.rounded_rectangle([wx, my + 82, wx + 6, my + 82 + wh_val // 2], radius=3, fill=COLOR_RED)
            wx += 12
            
        # Play/Pause Icon
        draw.text((w - 70, my + 44), "❚❚", font=get_font(18, bold=True), fill=COLOR_WHITE)
        
        # Live vs Archive Tabs & SAVE: ON Toggle
        ty = 295
        draw.text((28, ty), "LIVE (3)", font=get_font(14, bold=True), fill=COLOR_WHITE)
        draw.text((105, ty), "ARCHIVE (128)", font=get_font(14, bold=True), fill=COLOR_DARK_GRAY)
        
        # SAVE: ON Toggle Badge
        draw.rounded_rectangle([w - 130, ty - 4, w - 24, ty + 22], radius=6, fill=COLOR_DARK_CARD, outline=COLOR_RED, width=1)
        draw.ellipse([w - 120, ty + 5, w - 112, ty + 13], fill=COLOR_RED)
        draw.text((w - 104, ty), "SAVE: ON", font=get_font(12, bold=True), fill=COLOR_WHITE)
        
        # Notification Items
        alerts = [
            ("GITHUB", "v1.9.8 deployed successfully to main", "2m ago"),
            ("WHATSAPP", "Alex: Congrats on the Minimal OS release!", "14m ago"),
            ("CALENDAR", "AIUB DataComm Lab Evaluation at 2:00 PM", "1h ago")
        ]
        ay = 340
        for app, msg, time_s in alerts:
            draw.rounded_rectangle([24, ay, w - 24, ay + 75], radius=12, fill=COLOR_CARD_BG, outline=COLOR_DARK_GRAY, width=1)
            draw.text((40, ay + 14), app, font=get_font(12, bold=True), fill=COLOR_RED)
            draw.text((w - 95, ay + 14), time_s, font=get_font(11, bold=False), fill=COLOR_DARK_GRAY)
            draw.text((40, ay + 38), msg, font=get_font(14, bold=False), fill=COLOR_WHITE)
            ay += 88
            
        # Bottom Actions Bar
        by = h - 65
        draw.text((28, by), "ai export", font=get_font(15, bold=True), fill=COLOR_RED)
        draw.text((120, by), "download", font=get_font(15, bold=True), fill=COLOR_WHITE)
        draw.text((w - 180, by), "← SWIPE FOR HOME", font=get_font(11, bold=True), fill=COLOR_DARK_GRAY)
        
        img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
        frame_idx += 1
        
    # Phase 2: Tap "download" -> Toast slides up (Frames 16 - 24)
    for i in range(9):
        img = Image.new("RGBA", (w, h), COLOR_BLACK)
        draw = ImageDraw.Draw(img)
        draw_status_bar(draw, w)
        draw.text((36, 70), "← hub", font=get_font(32, bold=True), fill=COLOR_WHITE)
        draw.text((36, 115), "live alerts · music playback · local archive", font=get_font(13, bold=False), fill=COLOR_GRAY)
        
        # Toast Popup at bottom
        toast_y = h - 140
        draw.rounded_rectangle([40, toast_y, w - 40, toast_y + 54], radius=27, fill=(28, 30, 38, 245), outline=COLOR_RED, width=1)
        draw.text((58, toast_y + 16), "💾 Saved to Downloads: minimal_notifications.txt", font=get_font(13, bold=True), fill=COLOR_WHITE)
        
        # Bottom Actions
        by = h - 65
        draw.text((28, by), "ai export", font=get_font(15, bold=True), fill=COLOR_RED)
        draw.text((120, by), "download", font=get_font(15, bold=True), fill=COLOR_RED)
        
        img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
        frame_idx += 1
        
    # Phase 3: AI Assistant & Export Dialog Pops Up (Frames 25 - 40)
    for i in range(16):
        img = Image.new("RGBA", (w, h), COLOR_BLACK)
        draw = ImageDraw.Draw(img)
        draw_status_bar(draw, w)
        
        # Dim background
        dim = Image.new("RGBA", (w, h), (0, 0, 0, 180))
        img.paste(dim, (0, 0), dim)
        draw = ImageDraw.Draw(img)
        
        # Modal Dialog Box
        dy = 400
        dh = 440
        draw.rounded_rectangle([24, dy, w - 24, dy + dh], radius=24, fill=COLOR_CARD_BG, outline=COLOR_DARK_GRAY, width=1)
        
        # Dialog Header
        draw.text((44, dy + 28), "✦ HUB INTELLIGENCE & EXPORT", font=get_font(16, bold=True), fill=COLOR_RED)
        draw.text((44, dy + 62), "Send day-wise alerts to AI assistant or save to disk:", font=get_font(13, bold=False), fill=COLOR_GRAY)
        
        # Option 1: Send to AI Assistant
        o1_y = dy + 105
        draw.rounded_rectangle([40, o1_y, w - 40, o1_y + 80], radius=12, fill=COLOR_DARK_CARD, outline=COLOR_RED, width=1)
        draw.text((56, o1_y + 18), "send to ai assistant", font=get_font(15, bold=True), fill=COLOR_WHITE)
        draw.text((56, o1_y + 42), "Auto-creates prompt with day-wise log to summarize", font=get_font(12, bold=False), fill=COLOR_GRAY)
        
        # Option 2: Download .txt to Device
        o2_y = dy + 200
        draw.rounded_rectangle([40, o2_y, w - 40, o2_y + 80], radius=12, fill=COLOR_DARK_CARD, outline=COLOR_DARK_GRAY, width=1)
        draw.text((56, o2_y + 18), "download .txt to device", font=get_font(15, bold=True), fill=COLOR_WHITE)
        draw.text((56, o2_y + 42), "Saves archive directly into public Downloads folder", font=get_font(12, bold=False), fill=COLOR_GRAY)
        
        # Option 3: Copy Prompt
        o3_y = dy + 295
        draw.rounded_rectangle([40, o3_y, w - 40, o3_y + 80], radius=12, fill=COLOR_DARK_CARD, outline=COLOR_DARK_GRAY, width=1)
        draw.text((56, o3_y + 18), "copy ai prompt & log", font=get_font(15, bold=True), fill=COLOR_WHITE)
        draw.text((56, o3_y + 42), "Copies prompt with timestamped log to clipboard", font=get_font(12, bold=False), fill=COLOR_GRAY)
        
        # Close button
        draw.text((w - 95, dy + dh - 40), "CLOSE", font=get_font(13, bold=True), fill=COLOR_WHITE)
        
        img.save(os.path.join(frames_dir, f"frame_{frame_idx:04d}.png"))
        frame_idx += 1
        
    out_gif = os.path.join(GIFS_DIR, "03_notification_hub_ai_and_download.gif")
    compile_gif_with_ffmpeg(frames_dir, out_gif, fps=10, width=460)

# ==============================================================================
# Master Showcase Graphic Card (Static PNG with side-by-side workflow breakdown)
# ==============================================================================
def generate_showcase_workflow_graphic(output_path):
    w, h = 1920, 1080
    canvas = Image.new("RGBA", (w, h), (10, 11, 14, 255))
    draw = ImageDraw.Draw(canvas)
    
    # Dot grid
    for x in range(0, w, 40):
        for y in range(0, h, 40):
            draw.point((x, y), fill=(255, 255, 255, 10))
            
    # Red accent studio glow
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([w * 0.4, -150, w * 0.6, 250], fill=(255, 59, 48, 25))
    glow = glow.filter(ImageFilter.GaussianBlur(100))
    canvas.alpha_composite(glow)
    draw = ImageDraw.Draw(canvas)
    
    # Header
    font_badge = get_font(18, bold=True)
    draw.rounded_rectangle([w // 2 - 180, 50, w // 2 + 180, 92], radius=21, fill=(20, 22, 28, 255), outline=(45, 50, 62, 255), width=1)
    draw.ellipse([w // 2 - 158, 66, w // 2 - 148, 76], fill=COLOR_RED)
    draw.text((w // 2 - 136, 59), "DAILY DRIVER WORKFLOW DEMONSTRATION", font=font_badge, fill=COLOR_WHITE)
    
    font_title = get_font(44, bold=True)
    title = "MINIMAL OS // REAL TASKS IN ACTION"
    tb = font_title.getbbox(title)
    draw.text(((w - (tb[2] - tb[0])) // 2, 115), title, font=font_title, fill=COLOR_WHITE)
    
    font_sub = get_font(20, bold=False)
    sub = "Step-by-step visual demonstration of high-speed command actions, sticky thoughts, focus sprints, and AI notification archives."
    sb = font_sub.getbbox(sub)
    draw.text(((w - (sb[2] - sb[0])) // 2, 175), sub, font=font_sub, fill=COLOR_GRAY)
    
    # 3 Workflow Columns
    col_w = 560
    col_h = 750
    spacing = 40
    start_x = (w - (3 * col_w + 2 * spacing)) // 2
    start_y = 240
    
    workflows = [
        {
            "num": "WORKFLOW 01",
            "title": "Quick Note & Sticky Pin",
            "cmd": "qn Zarif: Finish lab report & deploy v1.9.8",
            "points": [
                "1. Type 'qn <thought>' in command bar",
                "2. 'qn' lights up in bold glowing red accent",
                "3. Instant execution card created in buffer",
                "4. Sticky alert pinned to Android notification bar",
                "5. Survives reboots via goAsync() receiver"
            ],
            "benefit": "Keeps your core goal visible on every phone unlock."
        },
        {
            "num": "WORKFLOW 02",
            "title": "Live Pomodoro Sprints",
            "cmd": "pomo 50m (or pomo 25m)",
            "points": [
                "1. Type 'pomo 50m' from home screen",
                "2. Live focus widget expands into home deck",
                "3. Real-time countdown timer starts ticking",
                "4. Ambient work/break cycle tracker (1/4)",
                "5. Tactile Play / Pause controls built-in"
            ],
            "benefit": "Zero app switching. Total flow state."
        },
        {
            "num": "WORKFLOW 03",
            "title": "Hub & AI Notification Export",
            "cmd": "Swipe Right -> Hub -> AI Export",
            "points": [
                "1. Unified alerts stream with Now Playing music",
                "2. Local SQLite database archive (SAVE ON/OFF)",
                "3. Direct Download to device (.txt) via MediaStore",
                "4. 1-Tap AI Summary prompt generation",
                "5. Instant Gemini / Claude / ChatGPT executive overview"
            ],
            "benefit": "Turn notification chaos into calm AI intelligence."
        }
    ]
    
    for idx, wf in enumerate(workflows):
        cx = start_x + idx * (col_w + spacing)
        cy = start_y
        
        # Column card
        draw.rounded_rectangle([cx, cy, cx + col_w, cy + col_h], radius=24, fill=COLOR_CARD_BG, outline=COLOR_DARK_GRAY, width=1)
        
        # Step Number Badge
        draw.rounded_rectangle([cx + 30, cy + 30, cx + 160, cy + 62], radius=8, fill=COLOR_DARK_CARD, outline=COLOR_RED, width=1)
        draw.text((cx + 42, cy + 38), wf["num"], font=get_font(13, bold=True), fill=COLOR_RED)
        
        # Title
        draw.text((cx + 30, cy + 85), wf["title"], font=get_font(26, bold=True), fill=COLOR_WHITE)
        
        # Command Bar Code Box
        draw.rounded_rectangle([cx + 30, cy + 135, cx + col_w - 30, cy + 195], radius=12, fill=COLOR_BLACK, outline=COLOR_RED, width=1)
        draw.text((cx + 45, cy + 152), ">", font=get_font(18, bold=True), fill=COLOR_RED)
        draw.text((cx + 68, cy + 152), wf["cmd"], font=get_font(15, mono=True, bold=True), fill=COLOR_WHITE)
        
        # Bullet Points
        py = cy + 225
        for pt in wf["points"]:
            draw.text((cx + 30, py), pt, font=get_font(17, bold=False), fill=COLOR_WHITE)
            py += 42
            
        # Benefit Banner
        by = cy + col_h - 110
        draw.rounded_rectangle([cx + 30, by, cx + col_w - 30, by + 75], radius=14, fill=COLOR_DARK_CARD, outline=COLOR_DARK_GRAY, width=1)
        draw.text((cx + 46, by + 16), "KEY BENEFIT:", font=get_font(12, bold=True), fill=COLOR_RED)
        draw.text((cx + 46, by + 38), wf["benefit"], font=get_font(15, bold=True), fill=COLOR_WHITE)
        
    # Footer
    draw.text(((w - 600) // 2, h - 45), "MINIMAL OS // REAL TASKS · BUILT FOR ZARIF AZFAR & POWER USERS", font=get_font(14, bold=False), fill=COLOR_DARK_GRAY)
    
    canvas.convert("RGB").save(output_path, "PNG", optimize=True)
    print(f"Generated Workflow Graphic: {output_path}")

def main():
    print("=== Generating Showcase GIFs & Graphics ===")
    generate_quicknote_gif()
    generate_pomodoro_gif()
    generate_hub_ai_gif()
    
    out_graphic = os.path.join(GRAPHICS_DIR, "real_tasks_workflow_showcase.png")
    generate_showcase_workflow_graphic(out_graphic)
    
    shutil.rmtree(TMP_FRAMES_DIR, ignore_errors=True)
    print("=== All Showcase GIFs and Graphics Complete! ===")

if __name__ == "__main__":
    main()

