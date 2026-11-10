#!/usr/bin/env python3
"""
Generate refined variations of Concept 01 (The Monolith Terminal) based on user feedback:
"i liked this design. but write small at the botttom minimal. or perfectly blend minimal or m so brand identity stays. also keep this version this image"

Variations:
1. Original Concept 01 (Kept 100% intact as requested)
2. Variation A: "M I N I M A L" (Small, elegant wide-spaced uppercase at bottom)
3. Variation B: "minimal" (Clean, modern lowercase matching Minimal OS typography)
4. Variation C: "M I N I M A L  O S" (Precision technical monospace branding)
5. Variation D: "M_" / Blended Monogram with terminal chevron & glowing cursor
"""

import os
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "docs", "media", "brand")
os.makedirs(OUTPUT_DIR, exist_ok=True)

COLOR_RED_ACCENT = (255, 59, 48)       # Minimal Crimson
COLOR_TITANIUM_LIGHT = (245, 247, 250) # Brushed aluminum
COLOR_TITANIUM_MID = (160, 165, 180)   # Muted brand silver
COLOR_BG_DARK_TOP = (18, 20, 25)
COLOR_BG_DARK_BOT = (8, 9, 12)

def get_font(size, bold=True, mono=False):
    if mono:
        paths = ["/System/Library/Fonts/SFNSMono.ttf", "/System/Library/Fonts/Menlo.ttc"]
    elif bold:
        paths = ["/System/Library/Fonts/SFNS.ttf", "/System/Library/Fonts/SFProDisplay-Bold.otf"]
    else:
        paths = ["/System/Library/Fonts/SFNS.ttf", "/System/Library/Fonts/Helvetica.ttc"]
    for p in paths:
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size)
            except Exception:
                continue
    return ImageFont.load_default()

def create_apple_squircle_mask(size, scale=0.88, p=4.8):
    super_size = size * 2
    y, x = np.ogrid[:super_size, :super_size]
    cx = super_size / 2.0
    cy = super_size / 2.0
    half_w = (super_size * scale) / 2.0
    
    nx = np.abs((x - cx) / half_w)
    ny = np.abs((y - cy) / half_w)
    dist = (nx ** p) + (ny ** p)
    mask_arr = (dist <= 1.0).astype(np.uint8) * 255
    
    mask_img = Image.fromarray(mask_arr, mode='L')
    return mask_img.resize((size, size), Image.Resampling.LANCZOS)

def apply_specular_rim_light(img, squircle_mask):
    w, h = img.size
    edges = squircle_mask.filter(ImageFilter.FIND_EDGES)
    rim_grad = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(rim_grad)
    for y in range(h):
        alpha = int(120 * (1.0 - (y / float(h)) ** 0.8)) + 15
        d.line([(0, y), (w, y)], fill=(255, 255, 255, alpha))
    rim_img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    rim_img.paste(rim_grad, (0, 0), edges)
    return Image.alpha_composite(img, rim_img)

def render_monolith_base(size=2048, cy_offset=-0.03):
    s = size
    base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(base)
    
    # 1. Base Gradient
    for y in range(s):
        ratio = y / float(s)
        r = int(COLOR_BG_DARK_TOP[0] * (1 - ratio) + COLOR_BG_DARK_BOT[0] * ratio)
        g = int(COLOR_BG_DARK_TOP[1] * (1 - ratio) + COLOR_BG_DARK_BOT[1] * ratio)
        b = int(COLOR_BG_DARK_TOP[2] * (1 - ratio) + COLOR_BG_DARK_BOT[2] * ratio)
        b_draw.line([(0, y), (s, y)], fill=(r, g, b, 255))
        
    # Studio top light glow
    glow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow)
    g_draw.ellipse([s * 0.15, -s * 0.2, s * 0.85, s * 0.6], fill=(255, 255, 255, 18))
    glow = glow.filter(ImageFilter.GaussianBlur(s * 0.12))
    base = Image.alpha_composite(base, glow)
    
    # Subtle dot matrix
    grid_img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    grid_draw = ImageDraw.Draw(grid_img)
    step = int(s * 0.045)
    for gx in range(step, s, step):
        for gy in range(step, s, step):
            grid_draw.point((gx, gy), fill=(255, 255, 255, 12))
    base = Image.alpha_composite(base, grid_img)
    
    # 2. Chevron coordinates (optically balanced)
    cx = s * 0.39
    cy = s * (0.48 + cy_offset)
    cw = s * 0.18
    ch = s * 0.235
    thick = s * 0.052
    
    # Chevron shadow
    shadow_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    sh_draw = ImageDraw.Draw(shadow_layer)
    sh_offset = int(s * 0.015)
    p1_s = (cx - cw + sh_offset, cy - ch + sh_offset)
    p2_s = (cx + cw + sh_offset, cy + sh_offset)
    p3_s = (cx - cw + sh_offset, cy + ch + sh_offset)
    sh_draw.line([p1_s, p2_s, p3_s], fill=(0, 0, 0, 190), width=int(thick * 1.1), joint="miter")
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(s * 0.025))
    base = Image.alpha_composite(base, shadow_layer)
    
    # Chevron Body
    glyph_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glyph_layer)
    p1 = (cx - cw, cy - ch)
    p2 = (cx + cw, cy)
    p3 = (cx - cw, cy + ch)
    gd.line([p1, p2, p3], fill=COLOR_TITANIUM_LIGHT, width=int(thick), joint="miter")
    gd.line([p1, p2, p3], fill=(255, 255, 255, 255), width=int(thick * 0.3), joint="miter")
    
    # 3. Glowing Cursor
    cur_x = cx + cw + s * 0.075
    cur_w = s * 0.14
    cur_h = s * 0.045
    cur_y = cy + ch - (cur_h / 2.0)
    
    cursor_glow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    cgd = ImageDraw.Draw(cursor_glow)
    cgd.rounded_rectangle(
        [cur_x - 14, cur_y - 14, cur_x + cur_w + 14, cur_y + cur_h + 14],
        radius=int(cur_h * 0.6),
        fill=(COLOR_RED_ACCENT[0], COLOR_RED_ACCENT[1], COLOR_RED_ACCENT[2], 140)
    )
    cursor_glow = cursor_glow.filter(ImageFilter.GaussianBlur(s * 0.035))
    base = Image.alpha_composite(base, cursor_glow)
    
    gd.rounded_rectangle([cur_x, cur_y, cur_x + cur_w, cur_y + cur_h], radius=int(cur_h * 0.35), fill=COLOR_RED_ACCENT)
    gd.rounded_rectangle([cur_x + 2, cur_y + 2, cur_x + cur_w - 2, cur_y + (cur_h * 0.45)], radius=int(cur_h * 0.2), fill=(255, 180, 180, 200))
    
    base = Image.alpha_composite(base, glyph_layer)
    return base, s, cx, cy, ch

def finalize_icon(base_img, s, target_size=1024):
    mask = create_apple_squircle_mask(s, scale=0.88, p=4.8)
    final_icon = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    final_icon.paste(base_img, (0, 0), mask)
    final_icon = apply_specular_rim_light(final_icon, mask)
    return final_icon.resize((target_size, target_size), Image.Resampling.LANCZOS)

# ==============================================================================
# Variation 1: "M I N I M A L" (Uppercase, Wide Spacing, Apple/Steipete Style)
# ==============================================================================
def render_variation_minimal_caps(target_size=1024):
    base, s, cx, cy, ch = render_monolith_base(2048, cy_offset=-0.04)
    draw = ImageDraw.Draw(base)
    
    # Text: "M I N I M A L"
    font_size = int(s * 0.042)
    font = get_font(font_size, bold=True)
    text = "M  I  N  I  M  A  L"
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    
    tx = (s - tw) / 2.0
    ty = s * 0.82
    
    # Subtle text drop shadow
    draw.text((tx + 2, ty + 4), text, font=font, fill=(0, 0, 0, 180))
    # Crisp titanium silver text
    draw.text((tx, ty), text, font=font, fill=COLOR_TITANIUM_MID)
    
    return finalize_icon(base, s, target_size)

# ==============================================================================
# Variation 2: "minimal" (Lowercase, Pure Modernist Minimal OS Tone)
# ==============================================================================
def render_variation_minimal_lower(target_size=1024):
    base, s, cx, cy, ch = render_monolith_base(2048, cy_offset=-0.04)
    draw = ImageDraw.Draw(base)
    
    # Text: "m i n i m a l"
    font_size = int(s * 0.046)
    font = get_font(font_size, bold=False)
    text = "m  i  n  i  m  a  l"
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    
    tx = (s - tw) / 2.0
    ty = s * 0.82
    
    draw.text((tx + 2, ty + 4), text, font=font, fill=(0, 0, 0, 180))
    draw.text((tx, ty), text, font=font, fill=COLOR_TITANIUM_MID)
    
    return finalize_icon(base, s, target_size)

# ==============================================================================
# Variation 3: "MINIMAL OS" (Precision Monospace with red accent dot)
# ==============================================================================
def render_variation_minimal_os(target_size=1024):
    base, s, cx, cy, ch = render_monolith_base(2048, cy_offset=-0.045)
    draw = ImageDraw.Draw(base)
    
    font_size = int(s * 0.036)
    font = get_font(font_size, bold=True, mono=True)
    text = "MINIMAL OS"
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    
    tx = (s - tw) / 2.0 + s * 0.015
    ty = s * 0.82
    
    # Red accent bullet before text
    dot_r = int(s * 0.009)
    draw.ellipse([tx - int(s * 0.035), ty + int(font_size * 0.4) - dot_r, tx - int(s * 0.035) + dot_r * 2, ty + int(font_size * 0.4) + dot_r], fill=COLOR_RED_ACCENT)
    
    draw.text((tx + 2, ty + 3), text, font=font, fill=(0, 0, 0, 180))
    draw.text((tx, ty), text, font=font, fill=COLOR_TITANIUM_MID)
    
    return finalize_icon(base, s, target_size)

# ==============================================================================
# Variation 4: "M" Monogram Blend (Subtle geometric 'M' indicator)
# ==============================================================================
def render_variation_m_blend(target_size=1024):
    base, s, cx, cy, ch = render_monolith_base(2048, cy_offset=-0.045)
    draw = ImageDraw.Draw(base)
    
    # Geometric pill with 'M' at bottom center
    pw = int(s * 0.16)
    ph = int(s * 0.046)
    px = (s - pw) / 2.0
    py = s * 0.815
    
    draw.rounded_rectangle([px, py, px + pw, py + ph], radius=int(ph / 2), fill=(24, 27, 36, 220), outline=(50, 55, 70, 200), width=int(s * 0.003))
    
    font = get_font(int(s * 0.026), bold=True, mono=True)
    text = "// M"
    tb = font.getbbox(text)
    tw = tb[2] - tb[0]
    draw.text(((s - tw) / 2.0, py + int(ph * 0.18)), text, font=font, fill=COLOR_RED_ACCENT)
    
    return finalize_icon(base, s, target_size)

# ==============================================================================
# Master Comparison Graphic of Variations (1920x1080)
# ==============================================================================
def generate_variations_comparison_card(icons, output_path):
    w, h = 2048, 1100
    canvas = Image.new("RGBA", (w, h), (10, 11, 14, 255))
    draw = ImageDraw.Draw(canvas)
    
    # Dot grid
    for x in range(0, w, 40):
        for y in range(0, h, 40):
            draw.point((x, y), fill=(255, 255, 255, 10))
            
    # Red glow
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([w * 0.35, -180, w * 0.65, 280], fill=(255, 59, 48, 28))
    glow = glow.filter(ImageFilter.GaussianBlur(110))
    canvas.alpha_composite(glow)
    draw = ImageDraw.Draw(canvas)
    
    # Header
    font_badge = get_font(18, bold=True)
    draw.rounded_rectangle([w // 2 - 160, 45, w // 2 + 160, 88], radius=21, fill=(20, 22, 28, 255), outline=(45, 50, 62, 255), width=1)
    draw.ellipse([w // 2 - 140, 62, w // 2 - 130, 72], fill=COLOR_RED_ACCENT)
    draw.text((w // 2 - 118, 54), "MONOLITH TERMINAL VARIATIONS", font=font_badge, fill=COLOR_TITANIUM_LIGHT)
    
    font_title = get_font(44, bold=True)
    title = "MINIMAL OS // REFINED ICON CANDIDATES"
    tb = font_title.getbbox(title)
    draw.text(((w - (tb[2] - tb[0])) // 2, 105), title, font=font_title, fill=(255, 255, 255, 255))
    
    font_sub = get_font(19, bold=False)
    sub = "Requested refinements: Adding subtle bottom 'minimal' / 'M' brand identity while preserving original icon purity."
    sb = font_sub.getbbox(sub)
    draw.text(((w - (sb[2] - sb[0])) // 2, 165), sub, font=font_sub, fill=(150, 155, 170, 255))
    
    # 5 Columns (Original + 4 Variations)
    card_w = 350
    card_h = 740
    spacing = 34
    start_x = (w - (5 * card_w + 4 * spacing)) // 2
    start_y = 230
    
    meta = [
        {
            "tag": "ORIGINAL (KEPT)",
            "title": "Pure Monolith",
            "bottom_text": "None (Pure Symbol)",
            "desc": "Original prompt & cursor without text. 100% pure minimalist symbol."
        },
        {
            "tag": "VARIATION A",
            "title": "M I N I M A L",
            "bottom_text": "M I N I M A L",
            "desc": "Wide-spaced uppercase titanium typography. High-end Swiss luxury feel."
        },
        {
            "tag": "VARIATION B",
            "title": "m i n i m a l",
            "bottom_text": "m i n i m a l",
            "desc": "Clean lowercase Apple San Francisco. Matches launcher's calm UI tone."
        },
        {
            "tag": "VARIATION C",
            "title": "MINIMAL OS",
            "bottom_text": "● MINIMAL OS",
            "desc": "Technical monospace brand title with precision red hardware status dot."
        },
        {
            "tag": "VARIATION D",
            "title": "// M Monogram",
            "bottom_text": "[ // M ] Pill",
            "desc": "Modern hardware badge blending 'M' with terminal chevron architecture."
        }
    ]
    
    for i in range(5):
        cx = start_x + i * (card_w + spacing)
        cy = start_y
        
        # Container Card
        draw.rounded_rectangle([cx, cy, cx + card_w, cy + card_h], radius=24, fill=(16, 18, 23, 255), outline=(36, 40, 50, 255), width=1)
        
        # Tag Badge
        font_tag = get_font(12, bold=True)
        draw.rounded_rectangle([cx + 20, cy + 20, cx + 155, cy + 46], radius=6, fill=(24, 27, 36, 255), outline=(50, 55, 70, 255), width=1)
        draw.text((cx + 28, cy + 26), meta[i]["tag"], font=font_tag, fill=COLOR_RED_ACCENT if i > 0 else (130, 135, 150, 255))
        
        # Icon
        icon_size = 250
        resized = icons[i].resize((icon_size, icon_size), Image.Resampling.LANCZOS)
        ix = cx + (card_w - icon_size) // 2
        iy = cy + 75
        
        # Shadow
        sh = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        shd = ImageDraw.Draw(sh)
        shd.rounded_rectangle([ix + 10, iy + 20, ix + icon_size - 10, iy + icon_size + 20], radius=56, fill=(0, 0, 0, 190))
        sh = sh.filter(ImageFilter.GaussianBlur(26))
        canvas.alpha_composite(sh)
        draw = ImageDraw.Draw(canvas)
        
        canvas.paste(resized, (ix, iy), resized)
        
        # Title
        draw.text((cx + 20, cy + 360), meta[i]["title"], font=get_font(20, bold=True), fill=(255, 255, 255, 255))
        
        # Bottom text indicator pill
        draw.rounded_rectangle([cx + 20, cy + 395, cx + card_w - 20, cy + 430], radius=6, fill=(12, 13, 17, 255))
        draw.text((cx + 32, cy + 404), f"Bottom: {meta[i]['bottom_text']}", font=get_font(12, bold=True, mono=True), fill=COLOR_TITANIUM_MID)
        
        # Description
        words = meta[i]["desc"].split()
        lines = []
        cur_line = []
        for word in words:
            cur_line.append(word)
            tb = get_font(13, bold=False).getbbox(" ".join(cur_line))
            if (tb[2] - tb[0]) > (card_w - 40):
                cur_line.pop()
                lines.append(" ".join(cur_line))
                cur_line = [word]
        if cur_line:
            lines.append(" ".join(cur_line))
            
        dy = cy + 450
        for l in lines:
            draw.text((cx + 20, dy), l, font=get_font(13, bold=False), fill=(140, 145, 160, 255))
            dy += 22
            
        # Mini Dock Preview at 48px
        dock_y = cy + 600
        draw.rounded_rectangle([cx + 20, dock_y, cx + card_w - 20, dock_y + 90], radius=14, fill=(11, 12, 16, 255), outline=(26, 28, 36, 255), width=1)
        draw.text((cx + 32, dock_y + 12), "DOCK 48px TEST", font=get_font(10, bold=True), fill=(80, 85, 100, 255))
        
        mini = icons[i].resize((52, 52), Image.Resampling.LANCZOS)
        canvas.paste(mini, (cx + (card_w - 52) // 2, dock_y + 28), mini)
        
    canvas.convert("RGB").save(output_path, "PNG", optimize=True)
    print(f"Generated Variations Comparison Card: {output_path}")

def main():
    print("=== Generating Refined Variations of Concept 01 ===")
    
    # 1. Original (Kept 100% intact)
    orig_path = os.path.join(OUTPUT_DIR, "concept_1_monolith_terminal_1024.png")
    orig_icon = Image.open(orig_path).convert("RGBA")
    
    # 2. Variation A: M I N I M A L (Uppercase)
    var_a = render_variation_minimal_caps(1024)
    var_a_path = os.path.join(OUTPUT_DIR, "concept_1_variation_a_minimal_caps_1024.png")
    var_a.save(var_a_path, "PNG", optimize=True)
    var_a.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_1_variation_a_minimal_caps_512.png"), "PNG", optimize=True)
    print(f"Saved: {var_a_path}")
    
    # 3. Variation B: minimal (Lowercase)
    var_b = render_variation_minimal_lower(1024)
    var_b_path = os.path.join(OUTPUT_DIR, "concept_1_variation_b_minimal_lower_1024.png")
    var_b.save(var_b_path, "PNG", optimize=True)
    var_b.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_1_variation_b_minimal_lower_512.png"), "PNG", optimize=True)
    print(f"Saved: {var_b_path}")
    
    # 4. Variation C: MINIMAL OS (Monospace)
    var_c = render_variation_minimal_os(1024)
    var_c_path = os.path.join(OUTPUT_DIR, "concept_1_variation_c_minimal_os_1024.png")
    var_c.save(var_c_path, "PNG", optimize=True)
    var_c.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_1_variation_c_minimal_os_512.png"), "PNG", optimize=True)
    print(f"Saved: {var_c_path}")
    
    # 5. Variation D: // M Monogram
    var_d = render_variation_m_blend(1024)
    var_d_path = os.path.join(OUTPUT_DIR, "concept_1_variation_d_m_monogram_1024.png")
    var_d.save(var_d_path, "PNG", optimize=True)
    var_d.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_1_variation_d_m_monogram_512.png"), "PNG", optimize=True)
    print(f"Saved: {var_d_path}")
    
    # Master Variations Comparison Graphic
    out_comparison = os.path.join(OUTPUT_DIR, "monolith_variations_showcase.png")
    generate_variations_comparison_card([orig_icon, var_a, var_b, var_c, var_d], out_comparison)
    print(f"Saved Comparison: {out_comparison}")

if __name__ == "__main__":
    main()

