#!/usr/bin/env python3
"""
Generate ultra-premium, top-brand Google Play Store graphics for Minimal OS.
Outputs:
1. store_listing/graphics/screenshots/ (8 x 1080x2400 Phone Showcase Slides)
2. store_listing/graphics/feature_graphic_1024x500.png (1024x500 Feature Graphic)
3. store_listing/graphics/tablet_7inch/ (4 x 1200x1920 Tablet Screenshots)
4. store_listing/graphics/tablet_10inch/ (4 x 1600x2560 Tablet Screenshots)
"""

import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

# Base paths
ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SCREENSHOTS_REAL_DIR = os.path.join(ROOT_DIR, "store_listing", "screenshots_real")
GRAPHICS_DIR = os.path.join(ROOT_DIR, "store_listing", "graphics")
SCREENSHOTS_OUT_DIR = os.path.join(GRAPHICS_DIR, "screenshots")
TABLET_7_DIR = os.path.join(GRAPHICS_DIR, "tablet_7inch")
TABLET_10_DIR = os.path.join(GRAPHICS_DIR, "tablet_10inch")

os.makedirs(SCREENSHOTS_OUT_DIR, exist_ok=True)
os.makedirs(TABLET_7_DIR, exist_ok=True)
os.makedirs(TABLET_10_DIR, exist_ok=True)

# Fonts
FONT_BOLD = "/System/Library/Fonts/SFNS.ttf"
FONT_INTER = os.path.join(ROOT_DIR, "app/src/main/res/font/inter_regular.ttf")
FONT_MONO = "/System/Library/Fonts/Menlo.ttc"

def get_font(path, size):
    try:
        return ImageFont.truetype(path, size)
    except Exception:
        return ImageFont.load_default()

def draw_rounded_rect(draw, box, radius, fill=None, outline=None, width=1):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)

def create_device_mockup(screen_img, target_w, target_h, corner_radius=44):
    """
    Renders a realistic dark titanium phone mockup containing screen_img.
    """
    # Device frame dimensions
    bezel = 18
    dev_w = target_w
    dev_h = target_h
    
    dev_img = Image.new("RGBA", (dev_w, dev_h), (0, 0, 0, 0))
    d = ImageDraw.Draw(dev_img)
    
    # Outer phone body
    d.rounded_rectangle(
        [0, 0, dev_w - 1, dev_h - 1],
        radius=corner_radius,
        fill=(22, 24, 28, 255),
        outline=(52, 56, 66, 255),
        width=3
    )
    
    # Inner screen area
    inner_w = dev_w - (bezel * 2)
    inner_h = dev_h - (bezel * 2)
    
    resized_screen = screen_img.resize((inner_w, inner_h), Image.Resampling.LANCZOS).convert("RGBA")
    
    # Screen mask with rounded corners
    mask = Image.new("L", (inner_w, inner_h), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle([0, 0, inner_w, inner_h], radius=corner_radius - 8, fill=255)
    
    dev_img.paste(resized_screen, (bezel, bezel), mask)
    
    # Draw subtle inner screen border
    d.rounded_rectangle(
        [bezel, bezel, bezel + inner_w - 1, bezel + inner_h - 1],
        radius=corner_radius - 8,
        fill=None,
        outline=(35, 38, 45, 200),
        width=1
    )
    
    # Speaker slit and camera cutout at top center
    cam_x = dev_w // 2
    cam_y = bezel + 16
    d.ellipse([cam_x - 7, cam_y - 7, cam_x + 7, cam_y + 7], fill=(10, 10, 12, 255), outline=(28, 30, 36, 255), width=1)
    
    return dev_img

def add_drop_shadow(base_img, dev_img, pos_x, pos_y, blur_radius=35):
    """
    Draws a soft, realistic drop shadow underneath the phone mockup.
    """
    shadow = Image.new("RGBA", base_img.size, (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow)
    
    dev_w, dev_h = dev_img.size
    shadow_box = [
        pos_x + 8,
        pos_y + 16,
        pos_x + dev_w - 8,
        pos_y + dev_h + 12
    ]
    s_draw.rounded_rectangle(shadow_box, radius=48, fill=(0, 0, 0, 180))
    shadow = shadow.filter(ImageFilter.GaussianBlur(blur_radius))
    
    # Composite shadow then device
    base_img.alpha_composite(shadow)
    base_img.alpha_composite(dev_img, dest=(pos_x, pos_y))

def draw_dot_grid(draw, w, h, step=48, dot_color=(255, 255, 255, 12)):
    """Draws a subtle high-tech dot matrix grid in the background."""
    for x in range(24, w, step):
        for y in range(24, h, step):
            draw.ellipse([x - 1, y - 1, x + 1, y + 1], fill=dot_color)

def generate_phone_showcase_slide(
    bg_w, bg_h,
    badge_text,
    title_line1,
    title_line2,
    subtitle,
    source_screen_path,
    output_path
):
    canvas = Image.new("RGBA", (bg_w, bg_h), (9, 10, 14, 255))
    draw = ImageDraw.Draw(canvas)
    
    # 1. Background subtle dot grid
    draw_dot_grid(draw, bg_w, 650, step=44, dot_color=(255, 255, 255, 10))
    
    # Ambient red glow at top center
    glow = Image.new("RGBA", (bg_w, bg_h), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow)
    g_draw.ellipse([bg_w // 2 - 350, -100, bg_w // 2 + 350, 400], fill=(255, 51, 51, 14))
    glow = glow.filter(ImageFilter.GaussianBlur(80))
    canvas.alpha_composite(glow)
    draw = ImageDraw.Draw(canvas)
    
    # 2. Typography Header
    # Badge Pill
    font_badge = get_font(FONT_MONO, 26)
    badge_bbox = font_badge.getbbox(badge_text)
    badge_tw = badge_bbox[2] - badge_bbox[0]
    badge_th = badge_bbox[3] - badge_bbox[1]
    
    badge_pad_x = 24
    badge_pad_y = 10
    pill_w = badge_tw + (badge_pad_x * 2) + 24
    pill_h = 44
    pill_x = (bg_w - pill_w) // 2
    pill_y = 100
    
    # Pill background
    draw.rounded_rectangle(
        [pill_x, pill_y, pill_x + pill_w, pill_y + pill_h],
        radius=22,
        fill=(20, 22, 28, 255),
        outline=(42, 46, 56, 255),
        width=1
    )
    # Red accent dot inside pill
    draw.ellipse([pill_x + 18, pill_y + 16, pill_x + 28, pill_y + 26], fill=(255, 51, 51, 255))
    draw.text((pill_x + 38, pill_y + 8), badge_text, font=font_badge, fill=(225, 228, 235, 255))
    
    # Main Title
    font_title = get_font(FONT_INTER, 66)
    curr_y = 175
    
    if title_line1:
        t1_bbox = font_title.getbbox(title_line1)
        t1_w = t1_bbox[2] - t1_bbox[0]
        draw.text(((bg_w - t1_w) // 2, curr_y), title_line1, font=font_title, fill=(255, 255, 255, 255))
        curr_y += 76
        
    if title_line2:
        t2_bbox = font_title.getbbox(title_line2)
        t2_w = t2_bbox[2] - t2_bbox[0]
        draw.text(((bg_w - t2_w) // 2, curr_y), title_line2, font=font_title, fill=(255, 255, 255, 255))
        curr_y += 76
        
    # Subtitle
    font_sub = get_font(FONT_INTER, 30)
    curr_y += 8
    sub_bbox = font_sub.getbbox(subtitle)
    sub_w = sub_bbox[2] - sub_bbox[0]
    draw.text(((bg_w - sub_w) // 2, curr_y), subtitle, font=font_sub, fill=(155, 160, 175, 255))
    
    # 3. Phone Mockup
    screen_raw = Image.open(source_screen_path).convert("RGBA")
    dev_w = 900
    dev_h = 1860
    phone = create_device_mockup(screen_raw, dev_w, dev_h, corner_radius=52)
    
    pos_x = (bg_w - dev_w) // 2
    pos_y = 480
    
    add_drop_shadow(canvas, phone, pos_x, pos_y, blur_radius=42)
    
    # Save optimized
    canvas.convert("RGB").save(output_path, "PNG", optimize=True)
    print(f"Generated phone showcase: {output_path}")

def generate_feature_graphic(output_path):
    w, h = 1024, 500
    canvas = Image.new("RGBA", (w, h), (9, 10, 14, 255))
    draw = ImageDraw.Draw(canvas)
    
    # Dot matrix background
    draw_dot_grid(draw, w, h, step=36, dot_color=(255, 255, 255, 8))
    
    # Subtle radial glow
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow)
    g_draw.ellipse([600, -50, 1050, 450], fill=(255, 51, 51, 20))
    glow = glow.filter(ImageFilter.GaussianBlur(70))
    canvas.alpha_composite(glow)
    draw = ImageDraw.Draw(canvas)
    
    # Left Side: Branding and Typography
    # Red accent bar
    draw.rectangle([64, 90, 70, 126], fill=(255, 51, 51, 255))
    
    font_cat = get_font(FONT_MONO, 16)
    draw.text((82, 98), "MINIMAL OS // INTENTIONAL LAUNCHER", font=font_cat, fill=(255, 51, 51, 255))
    
    font_hero = get_font(FONT_INTER, 46)
    draw.text((64, 145), "Type Less.", font=font_hero, fill=(255, 255, 255, 255))
    draw.text((64, 200), "Do More.", font=font_hero, fill=(255, 255, 255, 255))
    
    font_desc = get_font(FONT_INTER, 18)
    draw.text((64, 275), "Text-first terminal launcher engineered for mental clarity.", font=font_desc, fill=(160, 165, 180, 255))
    draw.text((64, 305), "Reclaim your attention with zero algorithmic feeds.", font=font_desc, fill=(160, 165, 180, 255))
    
    # Feature Pills
    pills = ["COMMAND BAR", "FOCUS TIMER", "TASKS", "100% PRIVATE"]
    px = 64
    py = 365
    font_pill = get_font(FONT_MONO, 13)
    for p in pills:
        pb = font_pill.getbbox(p)
        pw = pb[2] - pb[0] + 20
        draw.rounded_rectangle([px, py, px + pw, py + 30], radius=15, fill=(22, 25, 32, 255), outline=(45, 50, 62, 255), width=1)
        draw.text((px + 10, py + 7), p, font=font_pill, fill=(220, 225, 235, 255))
        px += pw + 12
        
    # Right Side: Sleek Phone Mockup showing Home Screen
    screen_path = os.path.join(SCREENSHOTS_REAL_DIR, "01_live_home_screen_active.png")
    if os.path.exists(screen_path):
        screen_raw = Image.open(screen_path).convert("RGBA")
        phone_w = 320
        phone_h = 660
        phone = create_device_mockup(screen_raw, phone_w, phone_h, corner_radius=32)
        add_drop_shadow(canvas, phone, 640, 40, blur_radius=28)
        
    canvas.convert("RGB").save(output_path, "PNG", optimize=True)
    print(f"Generated feature graphic: {output_path}")

def generate_tablet_showcase(w, h, badge_text, title, subtitle, screen_path, output_path):
    canvas = Image.new("RGBA", (w, h), (9, 10, 14, 255))
    draw = ImageDraw.Draw(canvas)
    draw_dot_grid(draw, w, h // 2, step=48, dot_color=(255, 255, 255, 10))
    
    # Header
    font_badge = get_font(FONT_MONO, int(w * 0.022))
    b_bbox = font_badge.getbbox(badge_text)
    bw = b_bbox[2] - b_bbox[0] + 48
    bh = int(w * 0.038)
    bx = (w - bw) // 2
    by = int(h * 0.04)
    draw.rounded_rectangle([bx, by, bx + bw, by + bh], radius=bh // 2, fill=(20, 22, 28, 255), outline=(42, 46, 56, 255), width=1)
    draw.text((bx + 24, by + 8), badge_text, font=font_badge, fill=(255, 51, 51, 255))
    
    font_title = get_font(FONT_INTER, int(w * 0.048))
    t_bbox = font_title.getbbox(title)
    tw = t_bbox[2] - t_bbox[0]
    draw.text(((w - tw) // 2, by + bh + int(h * 0.02)), title, font=font_title, fill=(255, 255, 255, 255))
    
    font_sub = get_font(FONT_INTER, int(w * 0.024))
    s_bbox = font_sub.getbbox(subtitle)
    sw = s_bbox[2] - s_bbox[0]
    draw.text(((w - sw) // 2, by + bh + int(h * 0.07)), subtitle, font=font_sub, fill=(155, 160, 175, 255))
    
    # Phone frame in tablet center
    if os.path.exists(screen_path):
        screen_raw = Image.open(screen_path).convert("RGBA")
        pw = int(w * 0.62)
        ph = int(pw * (2400 / 1080))
        phone = create_device_mockup(screen_raw, pw, ph, corner_radius=int(pw * 0.055))
        px = (w - pw) // 2
        py = int(h * 0.18)
        add_drop_shadow(canvas, phone, px, py, blur_radius=int(w * 0.03))
        
    canvas.convert("RGB").save(output_path, "PNG", optimize=True)
    print(f"Generated tablet showcase: {output_path}")

def main():
    slides = [
        {
            "badge": "01 // PURE FOCUS LAUNCHER",
            "t1": "TYPE LESS. DO MORE.",
            "t2": "",
            "sub": "Minimal typographic home screen. Zero algorithmic feeds.",
            "src": "01_live_home_screen_active.png",
            "out": "01_live_home_screen.png"
        },
        {
            "badge": "02 // TERMINAL COMMAND BAR",
            "t1": "INTERACTIVE CHAT BUFFER",
            "t2": "",
            "sub": "Numbered execution cards ready to edit, re-run, and cycle.",
            "src": "02_command_terminal_buffer.png",
            "out": "02_command_terminal_buffer.png"
        },
        {
            "badge": "03 // DEEP FOCUS SESSIONS",
            "t1": "LIVE COUNTDOWN TIMER",
            "t2": "",
            "sub": "Pomodoro sprints and ambient soundscapes on your home screen.",
            "src": "08_live_focus_timer_home.png",
            "out": "03_live_focus_timer.png"
        },
        {
            "badge": "04 // FIRST-CLASS EXPERIENCE",
            "t1": "APPLE-GRADE ONBOARDING",
            "t2": "",
            "sub": "Interactive live command typing and personalized alias setup.",
            "src": "01_onboarding_welcome_apple.png",
            "out": "04_apple_grade_onboarding.png"
        },
        {
            "badge": "05 // DISTRACTION-FREE APPS",
            "t1": "ALPHABETIC TEXT DRAWER",
            "t2": "",
            "sub": "Zero icon noise. Instant fuzzy search launches in milliseconds.",
            "src": "04_real_app_drawer.png",
            "out": "05_alphabetical_app_drawer.png"
        },
        {
            "badge": "06 // UNIFIED DASHBOARD",
            "t1": "ALL-IN-ONE WIDGETS DECK",
            "t2": "",
            "sub": "Essential tasks, calendar events, battery health, and Android widgets.",
            "src": "11_real_widgets_deck.png",
            "out": "06_widgets_deck.png"
        },
        {
            "badge": "07 // INSTANT CAPTURE",
            "t1": "SLIDE-UP SCRATCHPAD",
            "t2": "",
            "sub": "Quick tasks and markdown notes accessible from any screen.",
            "src": "06_real_memo_sheet.png",
            "out": "07_quick_memo_tasks.png"
        },
        {
            "badge": "08 // PRIVACY BY ARCHITECTURE",
            "t1": "100% PRIVATE & OFFLINE",
            "t2": "",
            "sub": "Fully open source. No trackers, no ads, and no cloud dependencies.",
            "src": "09_real_settings_screen.png",
            "out": "08_zero_telemetry_privacy.png"
        },
        {
            "badge": "09 // UNIFIED NOTIFICATION HUB",
            "t1": "LIVE ALERTS & AI EXPORT",
            "t2": "",
            "sub": "Live music player, local archive, direct .txt download & AI summary prompts.",
            "src": "12_real_notification_hub.png",
            "out": "09_real_notification_hub.png"
        }
    ]
    
    print("=== Generating Phone Showcase Graphics (1080x2400) ===")
    for s in slides:
        src_path = os.path.join(SCREENSHOTS_REAL_DIR, s["src"])
        out_path = os.path.join(SCREENSHOTS_OUT_DIR, s["out"])
        generate_phone_showcase_slide(
            bg_w=1080, bg_h=2400,
            badge_text=s["badge"],
            title_line1=s["t1"],
            title_line2=s["t2"],
            subtitle=s["sub"],
            source_screen_path=src_path,
            output_path=out_path
        )
        
    print("\n=== Generating Feature Graphic (1024x500) ===")
    feat_path = os.path.join(GRAPHICS_DIR, "feature_graphic_1024x500.png")
    generate_feature_graphic(feat_path)
    
    print("\n=== Generating 7-Inch Tablet Screenshots (1200x1920) ===")
    tablet_slides = [slides[0], slides[1], slides[2], slides[5]]
    for idx, s in enumerate(tablet_slides, 1):
        src_path = os.path.join(SCREENSHOTS_REAL_DIR, s["src"])
        out_path = os.path.join(TABLET_7_DIR, f"0{idx}_tablet_7inch.png")
        generate_tablet_showcase(1200, 1920, s["badge"], s["t1"], s["sub"], src_path, out_path)
        
    print("\n=== Generating 10-Inch Tablet Screenshots (1600x2560) ===")
    for idx, s in enumerate(tablet_slides, 1):
        src_path = os.path.join(SCREENSHOTS_REAL_DIR, s["src"])
        out_path = os.path.join(TABLET_10_DIR, f"0{idx}_tablet_10inch.png")
        generate_tablet_showcase(1600, 2560, s["badge"], s["t1"], s["sub"], src_path, out_path)
        
    print("\nAll top-brand store graphics generated successfully!")

if __name__ == "__main__":
    main()
