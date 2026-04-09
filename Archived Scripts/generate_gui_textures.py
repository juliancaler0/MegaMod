"""
Generate medieval fantasy RPG-style GUI textures for MegaMod.
Ornate gold frames, rich brown leather/parchment interiors, decorative elements.
"""

from PIL import Image, ImageDraw
import random
import os

random.seed(42)  # Reproducible grain

BASE_DIR = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "src", "main", "resources", "assets", "megamod", "textures", "gui"
)

os.makedirs(BASE_DIR, exist_ok=True)


def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))


def rgba(r, g, b, a=255):
    return (r, g, b, a)


def apply_grain(img, base_color, density=0.15, variation=8, region=None):
    """Apply leather grain texture to an image region."""
    pixels = img.load()
    if region is None:
        x0, y0, x1, y1 = 0, 0, img.width, img.height
    else:
        x0, y0, x1, y1 = region
    for x in range(x0, x1):
        for y in range(y0, y1):
            current = pixels[x, y]
            if random.random() < density:
                v = random.randint(-variation, variation)
                r = max(0, min(255, current[0] + v))
                g = max(0, min(255, current[1] + v))
                b = max(0, min(255, current[2] + v))
                a = current[3] if len(current) > 3 else 255
                pixels[x, y] = (r, g, b, a)


def lerp_color(c1, c2, t):
    """Linear interpolate between two color tuples."""
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(len(c1)))


def save(img, name):
    path = os.path.join(BASE_DIR, name)
    img.save(path)
    print(f"  Created: {path} ({img.size[0]}x{img.size[1]})")


# =============================================================================
# 1. screen_bg.png (256x256)
# Medieval dark background with leather grain
# =============================================================================
def gen_screen_bg():
    w, h = 256, 256
    base = hex_to_rgb('#1A0E08')
    img = Image.new('RGBA', (w, h), (*base, 255))
    apply_grain(img, (*base, 255), density=0.15, variation=8)
    save(img, 'screen_bg.png')


# =============================================================================
# 2. panel_bg.png (128x128)
# Ornate panel with gold border and corner pieces
# =============================================================================
def gen_panel_bg():
    w, h = 128, 128
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    pixels = img.load()

    outer_gold = hex_to_rgb('#C8A84C')
    mid_gold = hex_to_rgb('#DAA520')
    inner_shadow = hex_to_rgb('#8B6914')
    inset = hex_to_rgb('#2A1A10')
    interior = hex_to_rgb('#3A2218')
    bright_center = hex_to_rgb('#FFD700')

    # Outer border (px 0)
    draw.rectangle([0, 0, w-1, h-1], outline=(*outer_gold, 255))
    # Middle border (px 1)
    draw.rectangle([1, 1, w-2, h-2], outline=(*mid_gold, 255))
    # Inner shadow border (px 2)
    draw.rectangle([2, 2, w-3, h-3], outline=(*inner_shadow, 255))
    # Inset edge (px 3)
    draw.rectangle([3, 3, w-4, h-4], outline=(*inset, 255))
    # Interior fill
    draw.rectangle([4, 4, w-5, h-5], fill=(*interior, 255))

    # Corner pieces: 5x5 gold squares at each corner
    corners = [(0, 0), (w-5, 0), (0, h-5), (w-5, h-5)]
    for cx, cy in corners:
        for dx in range(5):
            for dy in range(5):
                pixels[cx+dx, cy+dy] = (*mid_gold, 255)
        # Brighter center pixel
        pixels[cx+2, cy+2] = (*bright_center, 255)

    # Grain on interior
    apply_grain(img, (*interior, 255), density=0.12, variation=6,
                region=(4, 4, w-4, h-4))

    save(img, 'panel_bg.png')


# =============================================================================
# 3. title_bar.png (256x32)
# Ornate title bar with gold borders and accent dots
# =============================================================================
def gen_title_bar():
    w, h = 256, 32
    img = Image.new('RGBA', (w, h), (0, 0, 0, 255))
    pixels = img.load()

    gold = hex_to_rgb('#C8A84C')
    edge_brown = hex_to_rgb('#2A1810')
    center_brown = hex_to_rgb('#3A2418')
    bright_gold = hex_to_rgb('#FFD700')

    # Fill interior with vertical gradient (lighter in center)
    for y in range(2, h-2):
        t = 1.0 - abs((y - h/2) / (h/2))  # 0 at edges, 1 at center
        c = lerp_color(edge_brown, center_brown, t)
        for x in range(0, w):
            pixels[x, y] = (*c, 255)

    # Top border (2px)
    for y in range(2):
        for x in range(w):
            pixels[x, y] = (*gold, 255)
    # Bottom border (2px)
    for y in range(h-2, h):
        for x in range(w):
            pixels[x, y] = (*gold, 255)

    # Gold accent dots along bottom border every 16px
    for x in range(0, w, 16):
        if 0 <= x < w:
            pixels[x, h-1] = (*bright_gold, 255)
            pixels[x, h-2] = (*bright_gold, 255)

    # Decorative gold corner pieces (3x3) at left and right ends
    for cx, cy in [(0, 0), (w-3, 0), (0, h-3), (w-3, h-3)]:
        for dx in range(3):
            for dy in range(3):
                px, py = cx+dx, cy+dy
                if 0 <= px < w and 0 <= py < h:
                    pixels[px, py] = (*bright_gold, 255)

    save(img, 'title_bar.png')


# =============================================================================
# 4. button_normal.png (64x20)
# Medieval button with gold frame
# =============================================================================
def gen_button_normal():
    w, h = 64, 20
    img = Image.new('RGBA', (w, h), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)
    pixels = img.load()

    gold = hex_to_rgb('#C8A84C')
    interior = hex_to_rgb('#4A2A1A')
    highlight = hex_to_rgb('#5A3A2A')
    shadow = hex_to_rgb('#3A1A0A')
    dark_corner = hex_to_rgb('#6B4A1A')

    # Gold border (2px)
    draw.rectangle([0, 0, w-1, h-1], outline=(*gold, 255))
    draw.rectangle([1, 1, w-2, h-2], outline=(*gold, 255))

    # Interior fill
    draw.rectangle([2, 2, w-3, h-3], fill=(*interior, 255))

    # Top highlight (1px inside border)
    for x in range(2, w-2):
        pixels[x, 2] = (*highlight, 255)

    # Bottom shadow (1px inside border)
    for x in range(2, w-2):
        pixels[x, h-3] = (*shadow, 255)

    # Corner pixels darker to simulate rounded gold frame
    corner_offsets = [(0, 0), (w-1, 0), (0, h-1), (w-1, h-1)]
    for cx, cy in corner_offsets:
        pixels[cx, cy] = (*dark_corner, 255)

    save(img, 'button_normal.png')


# =============================================================================
# 5. button_hover.png (64x20)
# Hovered medieval button with brighter gold and glow
# =============================================================================
def gen_button_hover():
    w, h = 64, 20
    img = Image.new('RGBA', (w, h), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)
    pixels = img.load()

    gold = hex_to_rgb('#DAA520')
    interior = hex_to_rgb('#5A3A2A')
    highlight = hex_to_rgb('#6A4A3A')
    shadow = hex_to_rgb('#4A2A1A')
    glow_tint = hex_to_rgb('#8B7330')
    dark_corner = hex_to_rgb('#8B6914')

    # Bright gold border (2px)
    draw.rectangle([0, 0, w-1, h-1], outline=(*gold, 255))
    draw.rectangle([1, 1, w-2, h-2], outline=(*gold, 255))

    # Interior fill
    draw.rectangle([2, 2, w-3, h-3], fill=(*interior, 255))

    # Top highlight
    for x in range(2, w-2):
        pixels[x, 2] = (*highlight, 255)

    # Bottom shadow
    for x in range(2, w-2):
        pixels[x, h-3] = (*shadow, 255)

    # Golden glow: border-adjacent interior pixels get gold tint
    for x in range(2, w-2):
        pixels[x, 3] = (*glow_tint, 255)
        pixels[x, h-4] = (*glow_tint, 255)
    for y in range(2, h-2):
        pixels[2, y] = (*glow_tint, 255)
        pixels[w-3, y] = (*glow_tint, 255)

    # Corner pixels darker
    for cx, cy in [(0, 0), (w-1, 0), (0, h-1), (w-1, h-1)]:
        pixels[cx, cy] = (*dark_corner, 255)

    save(img, 'button_hover.png')


# =============================================================================
# 6. progress_bar_bg.png (128x8)
# Progress bar track with dark gold border
# =============================================================================
def gen_progress_bar_bg():
    w, h = 128, 8
    img = Image.new('RGBA', (w, h), (0, 0, 0, 255))
    draw = ImageDraw.Draw(img)
    pixels = img.load()

    dark_gold = hex_to_rgb('#8B6914')
    interior = hex_to_rgb('#1A0E08')
    shadow = hex_to_rgb('#0E0804')

    # 1px dark gold border
    draw.rectangle([0, 0, w-1, h-1], outline=(*dark_gold, 255))

    # Interior: very dark brown
    draw.rectangle([1, 1, w-2, h-2], fill=(*interior, 255))

    # Subtle inner shadow on top edge
    for x in range(1, w-1):
        pixels[x, 1] = (*shadow, 255)

    save(img, 'progress_bar_bg.png')


# =============================================================================
# 7. progress_bar_fill.png (128x8)
# Warm amber to gold gradient fill
# =============================================================================
def gen_progress_bar_fill():
    w, h = 128, 8
    img = Image.new('RGBA', (w, h), (0, 0, 0, 255))
    pixels = img.load()

    left = hex_to_rgb('#8B6914')
    mid = hex_to_rgb('#DAA520')
    right = hex_to_rgb('#F0D878')

    # Horizontal gradient fill
    for x in range(w):
        t = x / (w - 1)
        if t < 0.5:
            c = lerp_color(left, mid, t * 2)
        else:
            c = lerp_color(mid, right, (t - 0.5) * 2)
        for y in range(h):
            pixels[x, y] = (*c, 255)

    # Top row highlight: brighter
    for x in range(w):
        r, g, b, a = pixels[x, 0]
        pixels[x, 0] = (min(255, r + 30), min(255, g + 30), min(255, b + 20), 255)

    # Bottom row slightly darker for depth
    for x in range(w):
        r, g, b, a = pixels[x, h-1]
        pixels[x, h-1] = (max(0, r - 15), max(0, g - 15), max(0, b - 10), 255)

    save(img, 'progress_bar_fill.png')


# =============================================================================
# 8. tab_active.png (64x24)
# Active tab with gold border on top/sides, diamond decoration
# =============================================================================
def gen_tab_active():
    w, h = 64, 24
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    pixels = img.load()

    gold = hex_to_rgb('#DAA520')
    interior = hex_to_rgb('#3A2218')
    bright_gold = hex_to_rgb('#FFD700')

    # Fill interior
    draw.rectangle([0, 0, w-1, h-1], fill=(*interior, 255))

    # 2px gold border: top
    for y in range(2):
        for x in range(w):
            pixels[x, y] = (*gold, 255)
    # 2px gold border: left side
    for x in range(2):
        for y in range(h):
            pixels[x, y] = (*gold, 255)
    # 2px gold border: right side
    for x in range(w-2, w):
        for y in range(h):
            pixels[x, y] = (*gold, 255)

    # NO bottom border (blends into content)

    # Top decorative: small gold diamond (3px) centered at top edge
    cx = w // 2
    pixels[cx, 0] = (*bright_gold, 255)
    pixels[cx-1, 1] = (*bright_gold, 255)
    pixels[cx, 1] = (*bright_gold, 255)
    pixels[cx+1, 1] = (*bright_gold, 255)
    pixels[cx, 2] = (*bright_gold, 255)

    # Grain on interior
    apply_grain(img, (*interior, 255), density=0.10, variation=5,
                region=(2, 2, w-2, h))

    save(img, 'tab_active.png')


# =============================================================================
# 9. tab_inactive.png (64x24)
# Inactive tab, muted dark appearance
# =============================================================================
def gen_tab_inactive():
    w, h = 64, 24
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    dark_border = hex_to_rgb('#2A1A10')
    interior = hex_to_rgb('#1A0E08')

    # 1px dark border all around
    draw.rectangle([0, 0, w-1, h-1], outline=(*dark_border, 255))
    # Interior
    draw.rectangle([1, 1, w-2, h-2], fill=(*interior, 255))

    save(img, 'tab_inactive.png')


# =============================================================================
# 10. hud_overlay.png (64x64)
# Semi-transparent HUD panel with rounded corners
# =============================================================================
def gen_hud_overlay():
    w, h = 64, 64
    img = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    pixels = img.load()

    bg = (26, 14, 8, 180)
    border = (200, 168, 76, 80)
    transparent = (0, 0, 0, 0)

    # Fill background
    draw.rectangle([0, 0, w-1, h-1], fill=bg)

    # 1px semi-transparent gold border
    draw.rectangle([0, 0, w-1, h-1], outline=border)

    # Corner pixels fully transparent for slight rounding
    for cx, cy in [(0, 0), (w-1, 0), (0, h-1), (w-1, h-1)]:
        pixels[cx, cy] = transparent

    save(img, 'hud_overlay.png')


# =============================================================================
# Run all generators
# =============================================================================
if __name__ == '__main__':
    print("Generating medieval RPG GUI textures...")
    print(f"Output directory: {BASE_DIR}\n")

    gen_screen_bg()
    gen_panel_bg()
    gen_title_bar()
    gen_button_normal()
    gen_button_hover()
    gen_progress_bar_bg()
    gen_progress_bar_fill()
    gen_tab_active()
    gen_tab_inactive()
    gen_hud_overlay()

    print("\nDone! All 10 textures generated.")
