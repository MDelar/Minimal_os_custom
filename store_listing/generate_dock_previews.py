#!/usr/bin/env python3
"""
Generate contextual phone dock and home screen previews for Minimal OS brand icons.
Shows icons on both pure AMOLED dark mode and modern frosted glass docks.
"""

import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

BRAND_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "docs", "media", "brand")

def get_font(size, bold=True):
    paths = [
        "/System/Library/Fonts/SFNS.ttf",
        "/System/Library/Fonts/SFProDisplay-Bold.otf",
        "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
        "/System/Library/Fonts/Menlo.ttc"
    ]
    for p in paths:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size)
            except Exception:
                continue
    return ImageFont.load_default()

def generate_dock_showcase(output_path):
    w, h = 1920, 960
    canvas = Image.new("RGBA", (w, h), (10, 11, 14, 255))
    draw = ImageDraw.Draw(canvas)
    
    # Subtle gradient background
    for y in range(h):
        r = int(12 * (1 - y/h) + 6 * (y/h))
        draw.line([(0, y), (w, y)], fill=(r, r+1, r+3, 255))
        
    font_head = get_font(36, bold=True)
    title = "MINIMAL OS // CONTEXTUAL HOME SCREEN & DOCK TEST"
    tb = font_head.getbbox(title)
    draw.text(((w - (tb[2] - tb[0])) // 2, 60), title, font=font_head, fill=(255, 255, 255, 255))
    
    font_sub = get_font(18, bold=False)
    sub = "Simulated on real phone home docks (160px app grid) with frosted glass refraction and typography labels."
    sb = font_sub.getbbox(sub)
    draw.text(((w - (sb[2] - sb[0])) // 2, 115), sub, font=font_sub, fill=(140, 145, 160, 255))
    
    # 4 Phone Dock Bars
    concepts = [
        ("CONCEPT 01: MONOLITH TERMINAL", "concept_1_monolith_terminal_1024.png", "Apple Pro / Steipete"),
        ("CONCEPT 02: GEOMETRIC M-APERTURE", "concept_2_geometric_m_1024.png", "Google / Modernist"),
        ("CONCEPT 03: TACTILE GLASS FOCUS", "concept_3_tactile_glass_1024.png", "VisionOS / Frosted Glass"),
        ("CONCEPT 04: SWISS CYBER MINIMAL", "concept_4_swiss_minimal_1024.png", "Dieter Rams / TE OP-1")
    ]
    
    bar_w = 420
    bar_h = 660
    spacing = 40
    start_x = (w - (4 * bar_w + 3 * spacing)) // 2
    start_y = 190
    
    for i, (c_name, icon_file, style_tag) in enumerate(concepts):
        bx = start_x + i * (bar_w + spacing)
        by = start_y
        
        # Phone Screen Crop Box (Simulated phone bottom)
        draw.rounded_rectangle([bx, by, bx + bar_w, by + bar_h], radius=32, fill=(18, 20, 26, 255), outline=(38, 42, 54, 255), width=2)
        
        # Header Badge
        font_tag = get_font(13, bold=True)
        draw.text((bx + 24, by + 24), c_name, font=font_tag, fill=(255, 59, 48, 255))
        font_subtag = get_font(12, bold=False)
        draw.text((bx + 24, by + 46), style_tag, font=font_subtag, fill=(120, 125, 140, 255))
        
        # Home Screen Page Dots
        dot_y = by + bar_h - 180
        for di in range(3):
            dx = bx + bar_w // 2 - 20 + di * 20
            draw.ellipse([dx - 3, dot_y - 3, dx + 3, dot_y + 3], fill=(255, 255, 255, 180 if di == 1 else 60))
            
        # Frosted Glass Dock Container (Simulated iOS/Pixel Dock)
        dock_y = by + bar_h - 150
        dock_h = 115
        dock_margin = 18
        dock_box = [bx + dock_margin, dock_y, bx + bar_w - dock_margin, dock_y + dock_h]
        
        # Dock shadow
        d_shadow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        dsd = ImageDraw.Draw(d_shadow)
        dsd.rounded_rectangle([dock_box[0], dock_box[1] + 6, dock_box[2], dock_box[3] + 6], radius=28, fill=(0, 0, 0, 120))
        d_shadow = d_shadow.filter(ImageFilter.GaussianBlur(16))
        canvas.alpha_composite(d_shadow)
        draw = ImageDraw.Draw(canvas)
        
        # Frosted glass dock
        draw.rounded_rectangle(dock_box, radius=28, fill=(35, 38, 48, 190), outline=(255, 255, 255, 30), width=1)
        
        # Load and resize Concept Icon
        icon_path = os.path.join(BRAND_DIR, icon_file)
        raw_icon = Image.open(icon_path).convert("RGBA")
        
        # Main Hero Icon in center of screen
        hero_size = 180
        hero_icon = raw_icon.resize((hero_size, hero_size), Image.Resampling.LANCZOS)
        hx = bx + (bar_w - hero_size) // 2
        hy = by + 120
        
        # Hero Icon shadow
        h_shadow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        hsd = ImageDraw.Draw(h_shadow)
        hsd.rounded_rectangle([hx + 10, hy + 20, hx + hero_size - 10, hy + hero_size + 20], radius=42, fill=(0, 0, 0, 190))
        h_shadow = h_shadow.filter(ImageFilter.GaussianBlur(24))
        canvas.alpha_composite(h_shadow)
        draw = ImageDraw.Draw(canvas)
        
        canvas.paste(hero_icon, (hx, hy), hero_icon)
        
        # Hero App Label
        font_label = get_font(15, bold=True)
        lbl = "Minimal OS"
        lb = font_label.getbbox(lbl)
        lw = lb[2] - lb[0]
        draw.text((bx + (bar_w - lw) // 2, hy + hero_size + 14), lbl, font=font_label, fill=(255, 255, 255, 255))
        
        # Dock Icons (4 icons across dock: Phone, Minimal OS, Messages, Camera)
        dock_icon_size = 64
        dock_y_pos = dock_y + (dock_h - dock_icon_size) // 2
        
        # Slot 1: Phone (Dark minimalist)
        s1_x = dock_box[0] + 18
        draw.rounded_rectangle([s1_x, dock_y_pos, s1_x + dock_icon_size, dock_y_pos + dock_icon_size], radius=16, fill=(28, 30, 36, 255), outline=(50, 54, 65, 255), width=1)
        draw.text((s1_x + 12, dock_y_pos + 22), "CALL", font=get_font(11, bold=True), fill=(140, 145, 160, 255))
        
        # Slot 2: Minimal OS ICON in Dock!
        s2_x = s1_x + dock_icon_size + 24
        dock_min_icon = raw_icon.resize((dock_icon_size, dock_icon_size), Image.Resampling.LANCZOS)
        canvas.paste(dock_min_icon, (s2_x, dock_y_pos), dock_min_icon)
        
        # Slot 3: Messages
        s3_x = s2_x + dock_icon_size + 24
        draw.rounded_rectangle([s3_x, dock_y_pos, s3_x + dock_icon_size, dock_y_pos + dock_icon_size], radius=16, fill=(28, 30, 36, 255), outline=(50, 54, 65, 255), width=1)
        draw.text((s3_x + 10, dock_y_pos + 22), "TEXT", font=get_font(11, bold=True), fill=(140, 145, 160, 255))
        
        # Slot 4: Notes
        s4_x = s3_x + dock_icon_size + 24
        draw.rounded_rectangle([s4_x, dock_y_pos, s4_x + dock_icon_size, dock_y_pos + dock_icon_size], radius=16, fill=(28, 30, 36, 255), outline=(50, 54, 65, 255), width=1)
        draw.text((s4_x + 10, dock_y_pos + 22), "NOTE", font=get_font(11, bold=True), fill=(140, 145, 160, 255))
        
        # High-res Badge / Rating chip
        font_rating = get_font(12, bold=True)
        draw.rounded_rectangle([bx + 30, by + bar_h - 45, bx + bar_w - 30, by + bar_h - 18], radius=6, fill=(24, 27, 34, 255))
        r_text = "SCALE TEST: 1024px -> 180px -> 64px (PASS)"
        rb = font_rating.getbbox(r_text)
        rw = rb[2] - rb[0]
        draw.text((bx + (bar_w - rw) // 2, by + bar_h - 38), r_text, font=font_rating, fill=(90, 200, 120, 255))
        
    canvas.convert("RGB").save(output_path, "PNG", optimize=True)
    print(f"Generated Dock Showcase: {output_path}")

if __name__ == "__main__":
    out_path = os.path.join(BRAND_DIR, "icon_context_dock_mockups.png")
    generate_dock_showcase(out_path)
