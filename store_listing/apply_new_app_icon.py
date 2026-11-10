#!/usr/bin/env python3
"""
Applies the chosen Minimal OS icon (Variation C: The Monolith Terminal with '● MINIMAL OS')
across the entire Android application and documentation suite:
- docs/media/logo.png (1024x1024 Master)
- store_listing/graphics/icon_512x512.png (512x512 Store Master)
- app/src/main/res/mipmap-* (ic_launcher.png and ic_launcher_round.png across all densities)
- app/src/main/res/drawable-* (ic_launcher_foreground.png across all densities with safe-zone margin)
"""

import os
from PIL import Image, ImageDraw, ImageFilter

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SOURCE_ICON_1024 = os.path.join(ROOT_DIR, "docs", "media", "brand", "concept_1_variation_c_minimal_os_1024.png")
RES_DIR = os.path.join(ROOT_DIR, "app", "src", "main", "res")

def create_circular_icon(icon_img, size):
    """
    Creates a pixel-perfect circular icon for Android round icon launchers.
    """
    # Resize icon slightly to fit circle
    target_img = icon_img.resize((size, size), Image.Resampling.LANCZOS)
    
    # 2x supersampled circle mask
    mask_size = size * 2
    mask = Image.new("L", (mask_size, mask_size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse([2, 2, mask_size - 2, mask_size - 2], fill=255)
    mask = mask.resize((size, size), Image.Resampling.LANCZOS)
    
    circular = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    circular.paste(target_img, (0, 0), mask)
    
    # Subtle circular edge rim light
    edges = mask.filter(ImageFilter.FIND_EDGES)
    rim = Image.new("RGBA", (size, size), (255, 255, 255, 30))
    circular.paste(rim, (0, 0), edges)
    
    return circular

def create_adaptive_foreground(icon_img, size):
    """
    Creates adaptive icon foreground placed within the 72dp safe zone of a 108dp canvas (66.6% scale).
    """
    fg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    inner_size = int(size * 0.72)
    resized_inner = icon_img.resize((inner_size, inner_size), Image.Resampling.LANCZOS)
    
    offset = (size - inner_size) // 2
    fg.paste(resized_inner, (offset, offset), resized_inner)
    return fg

def main():
    if not os.path.exists(SOURCE_ICON_1024):
        print(f"Error: Source icon not found at {SOURCE_ICON_1024}")
        return
        
    master = Image.open(SOURCE_ICON_1024).convert("RGBA")
    print(f"Loaded Master Variation C Icon: {master.size}")
    
    # 1. docs/media/logo.png (1024x1024)
    logo_path = os.path.join(ROOT_DIR, "docs", "media", "logo.png")
    master.save(logo_path, "PNG", optimize=True)
    print(f"Updated: {logo_path}")
    
    # 2. store_listing/graphics/icon_512x512.png (512x512)
    store_icon_path = os.path.join(ROOT_DIR, "store_listing", "graphics", "icon_512x512.png")
    store_icon = master.resize((512, 512), Image.Resampling.LANCZOS)
    store_icon.save(store_icon_path, "PNG", optimize=True)
    print(f"Updated: {store_icon_path}")
    
    # 3. Legacy Launcher Icons (mipmap-*)
    mipmap_densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    
    for folder, size in mipmap_densities.items():
        folder_path = os.path.join(RES_DIR, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        # Square / squircle icon
        sq_path = os.path.join(folder_path, "ic_launcher.png")
        sq_img = master.resize((size, size), Image.Resampling.LANCZOS)
        sq_img.save(sq_path, "PNG", optimize=True)
        
        # Round icon
        rd_path = os.path.join(folder_path, "ic_launcher_round.png")
        rd_img = create_circular_icon(master, size)
        rd_img.save(rd_path, "PNG", optimize=True)
        print(f"Updated {folder}: {size}x{size} (square & round)")
        
    # 4. Adaptive Icon Foreground (drawable-*)
    drawable_densities = {
        "drawable": 432,
        "drawable-mdpi": 108,
        "drawable-hdpi": 162,
        "drawable-xhdpi": 216,
        "drawable-xxhdpi": 324,
        "drawable-xxxhdpi": 432,
    }
    
    for folder, size in drawable_densities.items():
        folder_path = os.path.join(RES_DIR, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        fg_path = os.path.join(folder_path, "ic_launcher_foreground.png")
        fg_img = create_adaptive_foreground(master, size)
        fg_img.save(fg_path, "PNG", optimize=True)
        print(f"Updated {folder}: ic_launcher_foreground.png ({size}x{size})")
        
    print("\nAll app icons successfully updated across Android resources and project branding!")

if __name__ == "__main__":
    main()
