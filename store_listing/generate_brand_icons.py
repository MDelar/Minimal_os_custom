#!/usr/bin/env python3
"""
Generate ultra-premium, professional App Icons and Logo concepts for Minimal OS.
Inspired by Apple Pro craftsmanship, Google geometric clarity, and Steipete (Peter Steinberger) indie polish.
Generates 4 distinct concepts at 1024x1024, 512x512, plus a comprehensive comparison showcase.
"""

import os
import math
import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "docs", "media", "brand")
os.makedirs(OUTPUT_DIR, exist_ok=True)

# Colors
COLOR_RED_ACCENT = (255, 59, 48)       # iOS System Red / Minimal Crimson
COLOR_RED_GLOW = (255, 45, 85, 90)     # Ambient neon bloom
COLOR_TITANIUM_LIGHT = (245, 247, 250) # Brushed aluminum
COLOR_TITANIUM_MID = (180, 185, 195)   # Mid tone metal
COLOR_TITANIUM_DARK = (45, 48, 56)     # Deep metal
COLOR_BG_DARK_TOP = (18, 20, 25)       # Obsidian top
COLOR_BG_DARK_BOT = (8, 9, 12)         # Pitch bottom

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

def create_apple_squircle_mask(size, scale=0.90, p=5.0):
    """
    Renders an Apple-grade continuous curvature superellipse mask at 2x supersampling.
    Equation: |x|^p + |y|^p <= 1
    """
    super_size = size * 2
    y, x = np.ogrid[:super_size, :super_size]
    # Center and normalize coordinates to [-1, 1]
    cx = super_size / 2.0
    cy = super_size / 2.0
    half_w = (super_size * scale) / 2.0
    
    nx = np.abs((x - cx) / half_w)
    ny = np.abs((y - cy) / half_w)
    
    # Superellipse distance
    dist = (nx ** p) + (ny ** p)
    mask_arr = (dist <= 1.0).astype(np.uint8) * 255
    
    mask_img = Image.fromarray(mask_arr, mode='L')
    mask_img = mask_img.resize((size, size), Image.Resampling.LANCZOS)
    return mask_img

def apply_specular_rim_light(img, squircle_mask, width=4):
    """
    Adds Apple-grade specular edge bevel highlight (soft white light catching the top edge).
    """
    w, h = img.size
    edges = squircle_mask.filter(ImageFilter.FIND_EDGES)
    
    # Create top-biased gradient for the rim
    rim_grad = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(rim_grad)
    for y in range(h):
        # Bright at top (alpha 120), fading to subtle (alpha 20) at bottom
        alpha = int(120 * (1.0 - (y / float(h)) ** 0.8)) + 15
        d.line([(0, y), (w, y)], fill=(255, 255, 255, alpha))
        
    rim_img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    rim_img.paste(rim_grad, (0, 0), edges)
    
    # Composite over image
    return Image.alpha_composite(img, rim_img)

# ==============================================================================
# CONCEPT 1: "The Monolith Terminal" (Apple Pro / Steipete Style)
# Dark titanium obsidian base with brushed chevron '>' and incandescent red cursor '_'
# ==============================================================================
def render_concept_1(size=1024):
    scale = 2  # 2x supersampling for razor-sharp antialiasing
    s = size * scale
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 1. Base Gradient (Deep Titanium / Obsidian glass)
    base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(base)
    for y in range(s):
        ratio = y / float(s)
        r = int(COLOR_BG_DARK_TOP[0] * (1 - ratio) + COLOR_BG_DARK_BOT[0] * ratio)
        g = int(COLOR_BG_DARK_TOP[1] * (1 - ratio) + COLOR_BG_DARK_BOT[1] * ratio)
        b = int(COLOR_BG_DARK_TOP[2] * (1 - ratio) + COLOR_BG_DARK_BOT[2] * ratio)
        b_draw.line([(0, y), (s, y)], fill=(r, g, b, 255))
        
    # Subtle radial lighting in top-center (Apple studio lamp effect)
    glow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow)
    g_draw.ellipse([s * 0.15, -s * 0.2, s * 0.85, s * 0.6], fill=(255, 255, 255, 18))
    glow = glow.filter(ImageFilter.GaussianBlur(s * 0.12))
    base = Image.alpha_composite(base, glow)
    
    # Subtle dot matrix texture (Steipete mechanical precision)
    grid_img = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    grid_draw = ImageDraw.Draw(grid_img)
    step = int(s * 0.045)
    for gx in range(step, s, step):
        for gy in range(step, s, step):
            grid_draw.point((gx, gy), fill=(255, 255, 255, 12))
    base = Image.alpha_composite(base, grid_img)
    
    # 2. Main Glyph: Precision Command Chevron '>'
    glyph_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glyph_layer)
    
    # Chevron coordinates (Centered horizontally, optically weighted)
    cx = s * 0.40
    cy = s * 0.50
    cw = s * 0.18
    ch = s * 0.24
    thick = s * 0.052
    
    # Drop shadow for chevron
    shadow_layer = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    sh_draw = ImageDraw.Draw(shadow_layer)
    sh_offset = int(s * 0.015)
    p1_s = (cx - cw + sh_offset, cy - ch + sh_offset)
    p2_s = (cx + cw + sh_offset, cy + sh_offset)
    p3_s = (cx - cw + sh_offset, cy + ch + sh_offset)
    sh_draw.line([p1_s, p2_s, p3_s], fill=(0, 0, 0, 180), width=int(thick * 1.1), joint="miter")
    shadow_layer = shadow_layer.filter(ImageFilter.GaussianBlur(s * 0.025))
    base = Image.alpha_composite(base, shadow_layer)
    
    # Draw Chevron in Brushed Titanium
    p1 = (cx - cw, cy - ch)
    p2 = (cx + cw, cy)
    p3 = (cx - cw, cy + ch)
    g_draw.line([p1, p2, p3], fill=COLOR_TITANIUM_LIGHT, width=int(thick), joint="miter")
    
    # Subtle inner bevel on chevron
    g_draw.line([p1, p2, p3], fill=(255, 255, 255, 255), width=int(thick * 0.3), joint="miter")
    
    # 3. Glowing Terminal Block / Underscore Cursor '_'
    cur_x = cx + cw + s * 0.075
    cur_w = s * 0.14
    cur_h = s * 0.045
    cur_y = cy + ch - (cur_h / 2.0)
    
    # Neon Bloom Glow for Cursor
    cursor_glow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    cg_draw = ImageDraw.Draw(cursor_glow)
    cg_draw.rounded_rectangle(
        [cur_x - 14, cur_y - 14, cur_x + cur_w + 14, cur_y + cur_h + 14],
        radius=int(cur_h * 0.6),
        fill=(COLOR_RED_ACCENT[0], COLOR_RED_ACCENT[1], COLOR_RED_ACCENT[2], 140)
    )
    cursor_glow = cursor_glow.filter(ImageFilter.GaussianBlur(s * 0.035))
    base = Image.alpha_composite(base, cursor_glow)
    
    # Sharp Red Cursor
    g_draw.rounded_rectangle(
        [cur_x, cur_y, cur_x + cur_w, cur_y + cur_h],
        radius=int(cur_h * 0.35),
        fill=COLOR_RED_ACCENT
    )
    # Cursor specular highlight
    g_draw.rounded_rectangle(
        [cur_x + 2, cur_y + 2, cur_x + cur_w - 2, cur_y + (cur_h * 0.45)],
        radius=int(cur_h * 0.2),
        fill=(255, 180, 180, 200)
    )
    
    base = Image.alpha_composite(base, glyph_layer)
    
    # 4. Mask to Apple Squircle
    mask = create_apple_squircle_mask(s, scale=0.88, p=4.8)
    final_icon = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    final_icon.paste(base, (0, 0), mask)
    
    # Specular rim light on squircle edge
    final_icon = apply_specular_rim_light(final_icon, mask)
    
    # Resize down with Lanczos
    return final_icon.resize((size, size), Image.Resampling.LANCZOS)

# ==============================================================================
# CONCEPT 2: "Geometric M-Aperture" (Google & Braun / Teenage Engineering Style)
# Architectural geometric 'M' with center chevron aperture & glowing crimson focal point
# ==============================================================================
def render_concept_2(size=1024):
    scale = 2
    s = size * scale
    
    base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(base)
    
    # Deep Matte Graphite Gradient
    for y in range(s):
        ratio = y / float(s)
        val = int(22 * (1 - ratio) + 10 * ratio)
        b_draw.line([(0, y), (s, y)], fill=(val, val + 2, val + 5, 255))
        
    # Subtle technical crosshairs / circle guidelines (Braun/Leica aesthetic)
    grid = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(grid)
    cen = s // 2
    g_draw.ellipse([cen - s * 0.34, cen - s * 0.34, cen + s * 0.34, cen + s * 0.34], outline=(255, 255, 255, 10), width=2)
    base = Image.alpha_composite(base, grid)
    
    # Construct Modern Geometric 'M'
    glyph = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glyph)
    
    mw = s * 0.54
    mh = s * 0.46
    x0 = (s - mw) / 2.0
    y0 = (s - mh) / 2.0 + s * 0.02
    bar_w = s * 0.075
    
    # Left Vertical Pillar
    gd.rounded_rectangle([x0, y0, x0 + bar_w, y0 + mh], radius=int(bar_w * 0.4), fill=COLOR_TITANIUM_LIGHT)
    # Right Vertical Pillar
    gd.rounded_rectangle([x0 + mw - bar_w, y0, x0 + mw, y0 + mh], radius=int(bar_w * 0.4), fill=COLOR_TITANIUM_LIGHT)
    
    # Angled Central Chevrons forming 'M' center & downward terminal cursor
    mid_x = s / 2.0
    mid_y = y0 + mh * 0.68
    
    # Center chevron shadow
    sh = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    shd = ImageDraw.Draw(sh)
    sh_pts = [
        (x0 + bar_w * 0.7, y0 + 10),
        (mid_x, mid_y + 12),
        (x0 + mw - bar_w * 0.7, y0 + 10)
    ]
    shd.line(sh_pts, fill=(0, 0, 0, 160), width=int(bar_w * 0.95), joint="miter")
    sh = sh.filter(ImageFilter.GaussianBlur(s * 0.02))
    base = Image.alpha_composite(base, sh)
    
    # Center chevron in Titanium Mid
    cen_pts = [
        (x0 + bar_w * 0.7, y0),
        (mid_x, mid_y),
        (x0 + mw - bar_w * 0.7, y0)
    ]
    gd.line(cen_pts, fill=COLOR_TITANIUM_MID, width=int(bar_w * 0.88), joint="miter")
    
    # Radiant Focal Dot (Aperture Focus / Status light)
    dot_r = s * 0.045
    dot_y = mid_y + s * 0.12
    
    # Red Glow
    dot_glow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    dgd = ImageDraw.Draw(dot_glow)
    dgd.ellipse([mid_x - dot_r * 2.2, dot_y - dot_r * 2.2, mid_x + dot_r * 2.2, dot_y + dot_r * 2.2], fill=(255, 59, 48, 120))
    dot_glow = dot_glow.filter(ImageFilter.GaussianBlur(s * 0.035))
    base = Image.alpha_composite(base, dot_glow)
    
    # Sharp Red Dot
    gd.ellipse([mid_x - dot_r, dot_y - dot_r, mid_x + dot_r, dot_y + dot_r], fill=COLOR_RED_ACCENT)
    gd.ellipse([mid_x - dot_r * 0.6, dot_y - dot_r * 0.6, mid_x + dot_r * 0.2, dot_y + dot_r * 0.2], fill=(255, 190, 190, 210))
    
    base = Image.alpha_composite(base, glyph)
    
    mask = create_apple_squircle_mask(s, scale=0.88, p=4.8)
    final_icon = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    final_icon.paste(base, (0, 0), mask)
    final_icon = apply_specular_rim_light(final_icon, mask)
    
    return final_icon.resize((size, size), Image.Resampling.LANCZOS)

# ==============================================================================
# CONCEPT 3: "Tactile Glass Focus" (Apple VisionOS / Steipete Depth)
# Floating frosted acrylic card, refraction rim, and floating crimson focus prism
# ==============================================================================
def render_concept_3(size=1024):
    scale = 2
    s = size * scale
    
    base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(base)
    
    # Pure Deep Obsidian
    for y in range(s):
        ratio = y / float(s)
        val = int(14 * (1 - ratio) + 6 * ratio)
        b_draw.line([(0, y), (s, y)], fill=(val, val + 1, val + 3, 255))
        
    # Floating Frosted Card in Center
    card_margin = s * 0.20
    card_box = [card_margin, card_margin, s - card_margin, s - card_margin]
    card_rad = int(s * 0.08)
    
    # Ambient Card Drop Shadow
    card_shadow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    csd = ImageDraw.Draw(card_shadow)
    csd.rounded_rectangle([card_box[0], card_box[1] + s * 0.03, card_box[2], card_box[3] + s * 0.03], radius=card_rad, fill=(0, 0, 0, 220))
    card_shadow = card_shadow.filter(ImageFilter.GaussianBlur(s * 0.045))
    base = Image.alpha_composite(base, card_shadow)
    
    # Frosted Card Body with Glass Tint
    glass_card = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    gcd = ImageDraw.Draw(glass_card)
    gcd.rounded_rectangle(card_box, radius=card_rad, fill=(28, 32, 42, 230), outline=(255, 255, 255, 45), width=int(s * 0.005))
    
    # Inner glass top highlight
    gcd.rounded_rectangle(
        [card_box[0] + 4, card_box[1] + 4, card_box[2] - 4, card_box[1] + int((card_box[3] - card_box[1]) * 0.45)],
        radius=card_rad - 2,
        fill=(255, 255, 255, 12)
    )
    base = Image.alpha_composite(base, glass_card)
    
    # Center Minimal Prompt: "> _"
    prompt = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    pd = ImageDraw.Draw(prompt)
    
    cen_x = s * 0.44
    cen_y = s * 0.50
    ch_w = s * 0.09
    ch_h = s * 0.11
    
    # Chevron
    pd.line(
        [(cen_x - ch_w, cen_y - ch_h), (cen_x, cen_y), (cen_x - ch_w, cen_y + ch_h)],
        fill=(255, 255, 255, 255),
        width=int(s * 0.026),
        joint="miter"
    )
    
    # Underscore Cursor
    cur_x = cen_x + s * 0.045
    cur_w = s * 0.11
    cur_h = s * 0.024
    cur_y = cen_y + ch_h - cur_h
    
    pd.rounded_rectangle([cur_x, cur_y, cur_x + cur_w, cur_y + cur_h], radius=int(cur_h * 0.3), fill=COLOR_TITANIUM_MID)
    base = Image.alpha_composite(base, prompt)
    
    # Floating Radiant Crimson Gem / Status Aperture in top right of card
    gem_cx = card_box[2] - s * 0.065
    gem_cy = card_box[1] + s * 0.065
    gem_r = s * 0.026
    
    gem_glow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    ggd = ImageDraw.Draw(gem_glow)
    ggd.ellipse([gem_cx - gem_r * 3, gem_cy - gem_r * 3, gem_cx + gem_r * 3, gem_cy + gem_r * 3], fill=(255, 59, 48, 140))
    gem_glow = gem_glow.filter(ImageFilter.GaussianBlur(s * 0.03))
    base = Image.alpha_composite(base, gem_glow)
    
    gem_top = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    gtd = ImageDraw.Draw(gem_top)
    gtd.ellipse([gem_cx - gem_r, gem_cy - gem_r, gem_cx + gem_r, gem_cy + gem_r], fill=COLOR_RED_ACCENT)
    gtd.ellipse([gem_cx - gem_r * 0.5, gem_cy - gem_r * 0.5, gem_cx + gem_r * 0.2, gem_cy + gem_r * 0.2], fill=(255, 200, 200, 220))
    base = Image.alpha_composite(base, gem_top)
    
    mask = create_apple_squircle_mask(s, scale=0.88, p=4.8)
    final_icon = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    final_icon.paste(base, (0, 0), mask)
    final_icon = apply_specular_rim_light(final_icon, mask)
    
    return final_icon.resize((size, size), Image.Resampling.LANCZOS)

# ==============================================================================
# CONCEPT 4: "Swiss Cyber Minimal" (Dieter Rams / Teenage Engineering OP-1)
# Pure industrial minimalism: ultra-bold typography, precision tactile markings
# ==============================================================================
def render_concept_4(size=1024):
    scale = 2
    s = size * scale
    
    base = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    b_draw = ImageDraw.Draw(base)
    
    # Ultra-clean Matte Charcoal `#0C0D10`
    b_draw.rectangle([0, 0, s, s], fill=(12, 13, 16, 255))
    
    # Hardware chamfered border
    edge_m = int(s * 0.08)
    b_draw.rounded_rectangle([edge_m, edge_m, s - edge_m, s - edge_m], radius=int(s * 0.14), outline=(32, 35, 42, 255), width=int(s * 0.008))
    
    # Mechanical corner dots (Industrial hardware markings)
    dot_color = (255, 255, 255, 25)
    d_step = int(s * 0.02)
    for i in range(3):
        for j in range(3):
            b_draw.point((edge_m + int(s * 0.04) + i * d_step, edge_m + int(s * 0.04) + j * d_step), fill=dot_color)
            
    # Red Hardware Indicator Pill (Top Right)
    pill_w = int(s * 0.07)
    pill_h = int(s * 0.028)
    pill_x = s - edge_m - int(s * 0.04) - pill_w
    pill_y = edge_m + int(s * 0.04)
    b_draw.rounded_rectangle([pill_x, pill_y, pill_x + pill_w, pill_y + pill_h], radius=int(pill_h / 2), fill=COLOR_RED_ACCENT)
    
    # Bold Central Typographic Monogram "M_"
    center_y = s * 0.44
    font_large = get_font(int(s * 0.38), bold=True)
    text = "M"
    bbox = font_large.getbbox(text)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    
    tx = (s - tw) / 2.0 - s * 0.05
    ty = (s - th) / 2.0 - s * 0.02
    
    # Text shadow
    b_draw.text((tx + 6, ty + 10), text, font=font_large, fill=(0, 0, 0, 180))
    # Crisp White 'M'
    b_draw.text((tx, ty), text, font=font_large, fill=(255, 255, 255, 255))
    
    # Precision Red Underscore beneath / next to M
    cur_x = tx + tw + s * 0.03
    cur_y = ty + th - int(s * 0.04)
    cur_w = s * 0.12
    cur_h = s * 0.04
    
    # Red Glow
    glow = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.rounded_rectangle([cur_x - 12, cur_y - 12, cur_x + cur_w + 12, cur_y + cur_h + 12], radius=int(cur_h * 0.5), fill=(255, 59, 48, 130))
    glow = glow.filter(ImageFilter.GaussianBlur(s * 0.03))
    base = Image.alpha_composite(base, glow)
    
    bd = ImageDraw.Draw(base)
    bd.rounded_rectangle([cur_x, cur_y, cur_x + cur_w, cur_y + cur_h], radius=int(cur_h * 0.25), fill=COLOR_RED_ACCENT)
    
    # Subtitle Micro-label at bottom center
    font_mono = get_font(int(s * 0.035), bold=False)
    sub = "MINIMAL OS // 01"
    sb = font_mono.getbbox(sub)
    sw = sb[2] - sb[0]
    bd.text(((s - sw) / 2.0, s - edge_m - int(s * 0.065)), sub, font=font_mono, fill=(130, 135, 150, 255))
    
    mask = create_apple_squircle_mask(s, scale=0.88, p=4.8)
    final_icon = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    final_icon.paste(base, (0, 0), mask)
    final_icon = apply_specular_rim_light(final_icon, mask)
    
    return final_icon.resize((size, size), Image.Resampling.LANCZOS)

# ==============================================================================
# Master Showcase Graphic (2048x1080 Brand Presentation Card)
# Shows all 4 concepts in real device-style shadow boxes with brand analysis
# ==============================================================================
def generate_master_brand_showcase(icons, output_path):
    w, h = 2048, 1150
    canvas = Image.new("RGBA", (w, h), (10, 11, 14, 255))
    draw = ImageDraw.Draw(canvas)
    
    # Studio Dot-matrix grid background
    step = 40
    for x in range(0, w, step):
        for y in range(0, h, step):
            draw.point((x, y), fill=(255, 255, 255, 10))
            
    # Red studio accent glow at top
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse([w * 0.35, -200, w * 0.65, 300], fill=(255, 59, 48, 30))
    glow = glow.filter(ImageFilter.GaussianBlur(120))
    canvas.alpha_composite(glow)
    draw = ImageDraw.Draw(canvas)
    
    # Header Branding
    font_badge = get_font(18, bold=True)
    draw.rounded_rectangle([w // 2 - 160, 50, w // 2 + 160, 92], radius=21, fill=(20, 22, 28, 255), outline=(45, 50, 62, 255), width=1)
    draw.ellipse([w // 2 - 140, 66, w // 2 - 130, 76], fill=COLOR_RED_ACCENT)
    draw.text((w // 2 - 118, 59), "BRAND IDENTITY & APP ICON SUITE", font=font_badge, fill=(225, 228, 240, 255))
    
    font_title = get_font(44, bold=True)
    title = "MINIMAL OS // LOGO & ICON CANDIDATES"
    tb = font_title.getbbox(title)
    draw.text(((w - (tb[2] - tb[0])) // 2, 115), title, font=font_title, fill=(255, 255, 255, 255))
    
    font_sub = get_font(20, bold=False)
    sub = "Crafted to the exacting standards of Apple Pro tools, Google geometric purity, and Steipete indie craftsmanship."
    sb = font_sub.getbbox(sub)
    draw.text(((w - (sb[2] - sb[0])) // 2, 175), sub, font=font_sub, fill=(150, 155, 170, 255))
    
    # Render 4 Card Slots
    card_w = 420
    card_h = 760
    spacing = 48
    start_x = (w - (4 * card_w + 3 * spacing)) // 2
    start_y = 250
    
    metadata = [
        {
            "tag": "CONCEPT 01",
            "name": "The Monolith Terminal",
            "style": "Apple Pro / Steipete",
            "desc": "Brushed titanium chevron prompt with active glowing crimson block cursor on obsidian superellipse."
        },
        {
            "tag": "CONCEPT 02",
            "name": "Geometric M-Aperture",
            "style": "Google / Braun Modernism",
            "desc": "Architectural monogram merging the letter 'M' with terminal command chevron & focal status lens."
        },
        {
            "tag": "CONCEPT 03",
            "name": "Tactile Glass Focus",
            "style": "Apple VisionOS / Depth",
            "desc": "Multi-layer spatial frosted acrylic card with refraction highlight and radiant focus status gem."
        },
        {
            "tag": "CONCEPT 04",
            "name": "Swiss Cyber Minimal",
            "style": "Dieter Rams / Teenage Eng",
            "desc": "Pure Swiss typographic boldness, precision hardware chamfer, and mechanical status indicators."
        }
    ]
    
    for i in range(4):
        cx = start_x + i * (card_w + spacing)
        cy = start_y
        
        # Card Background Container
        draw.rounded_rectangle([cx, cy, cx + card_w, cy + card_h], radius=28, fill=(16, 18, 23, 255), outline=(36, 40, 50, 255), width=1)
        
        # Tag Badge
        font_tag = get_font(13, bold=True)
        draw.rounded_rectangle([cx + 24, cy + 24, cx + 130, cy + 52], radius=8, fill=(26, 29, 38, 255), outline=(50, 55, 70, 255), width=1)
        draw.text((cx + 34, cy + 30), metadata[i]["tag"], font=font_tag, fill=COLOR_RED_ACCENT)
        
        # Style Pill
        font_style = get_font(12, bold=False)
        draw.text((cx + 144, cy + 31), metadata[i]["style"], font=font_style, fill=(130, 135, 150, 255))
        
        # Render Icon with Realistic Drop Shadow
        icon_size = 300
        resized_icon = icons[i].resize((icon_size, icon_size), Image.Resampling.LANCZOS)
        
        # Shadow underneath icon
        ix = cx + (card_w - icon_size) // 2
        iy = cy + 85
        
        icon_shadow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        isd = ImageDraw.Draw(icon_shadow)
        isd.rounded_rectangle([ix + 12, iy + 24, ix + icon_size - 12, iy + icon_size + 24], radius=68, fill=(0, 0, 0, 200))
        icon_shadow = icon_shadow.filter(ImageFilter.GaussianBlur(32))
        canvas.alpha_composite(icon_shadow)
        draw = ImageDraw.Draw(canvas)
        
        # Paste Icon
        canvas.paste(resized_icon, (ix, iy), resized_icon)
        
        # Title
        font_cname = get_font(21, bold=True)
        draw.text((cx + 24, cy + 425), metadata[i]["name"], font=font_cname, fill=(255, 255, 255, 255))
        
        # Description
        font_cdesc = get_font(14, bold=False)
        # Word wrap description
        words = metadata[i]["desc"].split()
        lines = []
        cur_line = []
        for word in words:
            cur_line.append(word)
            test_line = " ".join(cur_line)
            tb = font_cdesc.getbbox(test_line)
            if (tb[2] - tb[0]) > (card_w - 48):
                cur_line.pop()
                lines.append(" ".join(cur_line))
                cur_line = [word]
        if cur_line:
            lines.append(" ".join(cur_line))
            
        desc_y = cy + 468
        for line in lines:
            draw.text((cx + 24, desc_y), line, font=font_cdesc, fill=(150, 155, 170, 255))
            desc_y += 24
            
        # Mini Phone Dock Preview (How it sits on a phone home bar)
        dock_y = cy + 625
        draw.rounded_rectangle([cx + 24, dock_y, cx + card_w - 24, dock_y + 90], radius=16, fill=(11, 12, 16, 255), outline=(28, 30, 38, 255), width=1)
        font_dock = get_font(11, bold=True)
        draw.text((cx + 40, dock_y + 14), "DOCK DOCK PREVIEW (48px)", font=font_dock, fill=(90, 95, 110, 255))
        
        mini_icon = icons[i].resize((52, 52), Image.Resampling.LANCZOS)
        canvas.paste(mini_icon, (cx + 40, dock_y + 30), mini_icon)
        
        # Neighbor dummy minimalist apps for context
        for d_idx, d_name in enumerate(["PHONE", "MESSAGES", "BROWSER"]):
            d_x = cx + 115 + d_idx * 88
            draw.rounded_rectangle([d_x, dock_y + 30, d_x + 52, dock_y + 82], radius=12, fill=(22, 24, 30, 255))
            draw.text((d_x + 6, dock_y + 48), d_name[:3], font=get_font(10, bold=True), fill=(80, 85, 100, 255))
            
    # Bottom Brand Stamp
    font_foot = get_font(14, bold=False)
    foot = "MINIMAL OS BRAND LAB // DESIGNED FOR THE HIGHEST CALIBER OF TACTILE HARDWARE & DIGITAL CLARITY"
    fb = font_foot.getbbox(foot)
    draw.text(((w - (fb[2] - fb[0])) // 2, h - 45), foot, font=font_foot, fill=(80, 85, 100, 255))
    
    canvas.convert("RGB").save(output_path, "PNG", optimize=True)
    print(f"Generated Master Brand Showcase: {output_path}")

def main():
    print("=== Generating 4 World-Class Minimal OS App Icons (1024x1024) ===")
    
    c1 = render_concept_1(1024)
    c1_path = os.path.join(OUTPUT_DIR, "concept_1_monolith_terminal_1024.png")
    c1.save(c1_path, "PNG", optimize=True)
    c1.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_1_monolith_terminal_512.png"), "PNG", optimize=True)
    print(f"Saved: {c1_path}")
    
    c2 = render_concept_2(1024)
    c2_path = os.path.join(OUTPUT_DIR, "concept_2_geometric_m_1024.png")
    c2.save(c2_path, "PNG", optimize=True)
    c2.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_2_geometric_m_512.png"), "PNG", optimize=True)
    print(f"Saved: {c2_path}")
    
    c3 = render_concept_3(1024)
    c3_path = os.path.join(OUTPUT_DIR, "concept_3_tactile_glass_1024.png")
    c3.save(c3_path, "PNG", optimize=True)
    c3.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_3_tactile_glass_512.png"), "PNG", optimize=True)
    print(f"Saved: {c3_path}")
    
    c4 = render_concept_4(1024)
    c4_path = os.path.join(OUTPUT_DIR, "concept_4_swiss_minimal_1024.png")
    c4.save(c4_path, "PNG", optimize=True)
    c4.resize((512, 512), Image.Resampling.LANCZOS).save(os.path.join(OUTPUT_DIR, "concept_4_swiss_minimal_512.png"), "PNG", optimize=True)
    print(f"Saved: {c4_path}")
    
    print("\n=== Generating Master Showcase Comparison Graphic (2048x1150) ===")
    showcase_path = os.path.join(OUTPUT_DIR, "brand_showcase_comparison.png")
    generate_master_brand_showcase([c1, c2, c3, c4], showcase_path)
    
    print("\nDone! All icons and master showcase ready in docs/media/brand/")

if __name__ == "__main__":
    main()

