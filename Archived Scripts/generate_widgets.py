"""
Generate a detailed medieval fantasy GUI sprite sheet (widgets.png) for nine-slice rendering.
256x256 sprite atlas with ornate gold-framed, leather-interior RPG-style GUI elements.
"""
from PIL import Image, ImageDraw
import random

random.seed(42)  # Reproducible texture

img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
pixels = img.load()

# ── Color palette ──────────────────────────────────────────────
BRIGHT_GOLD   = (255, 215, 0)
GOLD          = (218, 165, 32)
MID_GOLD      = (200, 168, 76)
DARK_GOLD     = (139, 105, 20)
VERY_DARK_GOLD= (90, 58, 10)
LEATHER       = (74, 42, 26)
LEATHER_LIGHT = (90, 58, 42)
LEATHER_DARK  = (58, 26, 10)
LEATHER_VDARK = (42, 26, 16)
DARK_BROWN    = (26, 10, 4)
VERY_DARK     = (10, 6, 4)
BORDER_DARK   = (26, 14, 8)   # muted dark border for inactive elements
INACTIVE_INT  = (26, 14, 8)


def set_px(x, y, r, g, b, a=255):
    if 0 <= x < 256 and 0 <= y < 256:
        pixels[x, y] = (r, g, b, a)

def get_px(x, y):
    if 0 <= x < 256 and 0 <= y < 256:
        return pixels[x, y]
    return (0, 0, 0, 0)

def set_px_t(x, y, color):
    """Set pixel from a tuple (r,g,b) or (r,g,b,a)."""
    if len(color) == 3:
        set_px(x, y, color[0], color[1], color[2])
    else:
        set_px(x, y, color[0], color[1], color[2], color[3])

def fill_rect(x1, y1, x2, y2, r, g, b, a=255):
    for yy in range(y1, y2):
        for xx in range(x1, x2):
            set_px(xx, yy, r, g, b, a)

def fill_rect_t(x1, y1, x2, y2, color):
    if len(color) == 3:
        fill_rect(x1, y1, x2, y2, color[0], color[1], color[2])
    else:
        fill_rect(x1, y1, x2, y2, color[0], color[1], color[2], color[3])

def leather_grain(x1, y1, x2, y2, base_r=74, base_g=42, base_b=26, intensity=12):
    """Fill area with leather grain texture."""
    for yy in range(y1, y2):
        for xx in range(x1, x2):
            v = random.randint(-intensity, intensity)
            # occasional darker 'pores'
            if random.random() < 0.06:
                v -= 15
            # occasional lighter highlight
            if random.random() < 0.04:
                v += 12
            set_px(xx, yy,
                   max(0, min(255, base_r + v)),
                   max(0, min(255, base_g + v)),
                   max(0, min(255, base_b + v)))

def draw_gold_border_top(x1, y1, width, thickness=3):
    """Draw a horizontal gold border strip (top style: bright top, dark bottom)."""
    for xx in range(x1, x1 + width):
        if thickness >= 1:
            set_px_t(xx, y1, MID_GOLD)
        if thickness >= 2:
            set_px_t(xx, y1 + 1, GOLD)
        if thickness >= 3:
            set_px_t(xx, y1 + 2, DARK_GOLD)

def draw_gold_border_bottom(x1, y1, width, thickness=3):
    """Draw a horizontal gold border strip (bottom style: dark top, bright bottom)."""
    for xx in range(x1, x1 + width):
        if thickness >= 1:
            set_px_t(xx, y1, DARK_GOLD)
        if thickness >= 2:
            set_px_t(xx, y1 + 1, GOLD)
        if thickness >= 3:
            set_px_t(xx, y1 + 2, MID_GOLD)

def draw_gold_border_left(x1, y1, height, thickness=3):
    """Draw a vertical gold border strip (left style: bright left, dark right)."""
    for yy in range(y1, y1 + height):
        if thickness >= 1:
            set_px_t(x1, yy, MID_GOLD)
        if thickness >= 2:
            set_px_t(x1 + 1, yy, GOLD)
        if thickness >= 3:
            set_px_t(x1 + 2, yy, DARK_GOLD)

def draw_gold_border_right(x1, y1, height, thickness=3):
    """Draw a vertical gold border strip (right style: dark left, bright right)."""
    for yy in range(y1, y1 + height):
        if thickness >= 1:
            set_px_t(x1, yy, DARK_GOLD)
        if thickness >= 2:
            set_px_t(x1 + 1, yy, GOLD)
        if thickness >= 3:
            set_px_t(x1 + 2, yy, MID_GOLD)

def gold_gradient_h(x1, y1, x2, y2, left_col, right_col):
    """Horizontal gradient fill."""
    w = x2 - x1
    if w <= 0:
        return
    for xx in range(x1, x2):
        t = (xx - x1) / max(1, w - 1)
        r = int(left_col[0] + (right_col[0] - left_col[0]) * t)
        g = int(left_col[1] + (right_col[1] - left_col[1]) * t)
        b = int(left_col[2] + (right_col[2] - left_col[2]) * t)
        for yy in range(y1, y2):
            set_px(xx, yy, r, g, b)


# ═══════════════════════════════════════════════════════════════
# 1. PANEL FRAME (nine-slice) at (0,0) — 48×48
# ═══════════════════════════════════════════════════════════════

# ── Center tile (32×32) at (8,8) ──
leather_grain(8, 8, 40, 40, 74, 42, 26, 10)
# Add subtle diagonal scratch lines
for i in range(6):
    sx = random.randint(8, 36)
    sy = random.randint(8, 36)
    length = random.randint(4, 10)
    for step in range(length):
        px = sx + step
        py = sy + step
        if 8 <= px < 40 and 8 <= py < 40:
            cur = get_px(px, py)
            set_px(px, py, min(255, cur[0] + 8), min(255, cur[1] + 6), min(255, cur[2] + 4))

# ── Top edge (32×8) at (8,0) ──
# 3px gold border at top
for xx in range(8, 40):
    set_px_t(xx, 0, MID_GOLD)
    set_px_t(xx, 1, GOLD)
    set_px_t(xx, 2, DARK_GOLD)
# 1px dark inset shadow
for xx in range(8, 40):
    set_px_t(xx, 3, DARK_BROWN)
# Leather fill below
leather_grain(8, 4, 40, 8, 74, 42, 26, 8)
# Dot pattern every 8px
for xx in range(12, 40, 8):
    set_px_t(xx, 1, BRIGHT_GOLD)

# ── Bottom edge (32×8) at (8,40) ──
leather_grain(8, 40, 40, 45, 74, 42, 26, 8)
# 1px dark inset shadow at top of bottom edge
for xx in range(8, 40):
    set_px_t(xx, 44, DARK_BROWN)
# 3px gold border at bottom
for xx in range(8, 40):
    set_px_t(xx, 45, DARK_GOLD)
    set_px_t(xx, 46, GOLD)
    set_px_t(xx, 47, MID_GOLD)
# Dot pattern
for xx in range(12, 40, 8):
    set_px_t(xx, 46, BRIGHT_GOLD)

# ── Left edge (8×32) at (0,8) ──
# 3px gold border at left
for yy in range(8, 40):
    set_px_t(0, yy, MID_GOLD)
    set_px_t(1, yy, GOLD)
    set_px_t(2, yy, DARK_GOLD)
# 1px dark inset
for yy in range(8, 40):
    set_px_t(3, yy, DARK_BROWN)
# Leather fill
leather_grain(4, 8, 8, 40, 74, 42, 26, 8)
# Dot pattern every 8px
for yy in range(12, 40, 8):
    set_px_t(1, yy, BRIGHT_GOLD)

# ── Right edge (8×32) at (40,8) ──
leather_grain(40, 8, 44, 40, 74, 42, 26, 8)
# 1px dark inset
for yy in range(8, 40):
    set_px_t(44, yy, DARK_BROWN)
# 3px gold border at right
for yy in range(8, 40):
    set_px_t(45, yy, DARK_GOLD)
    set_px_t(46, yy, GOLD)
    set_px_t(47, yy, MID_GOLD)
# Dot pattern
for yy in range(12, 40, 8):
    set_px_t(46, yy, BRIGHT_GOLD)

# ── Corners ──

def draw_corner_tl(ox, oy):
    """Top-left corner 8×8 at (ox, oy). L-shaped gold frame opening bottom-right."""
    # Clear
    fill_rect(ox, oy, ox+8, oy+8, 0, 0, 0, 0)
    # Outer L: top row and left col, 3px thick
    # Top 3 rows full width
    for yy in range(3):
        for xx in range(8):
            shade = [MID_GOLD, GOLD, DARK_GOLD][yy]
            set_px_t(ox+xx, oy+yy, shade)
    # Left 3 cols full height
    for xx in range(3):
        for yy in range(3, 8):
            shade = [MID_GOLD, GOLD, DARK_GOLD][xx]
            set_px_t(ox+xx, oy+yy, shade)
    # Bright rivet at very corner
    set_px_t(ox, oy, BRIGHT_GOLD)
    set_px_t(ox+1, oy, BRIGHT_GOLD)
    set_px_t(ox, oy+1, BRIGHT_GOLD)
    # Decorative flourish: bright gold pixels suggesting a scroll
    set_px_t(ox+4, oy+2, BRIGHT_GOLD)
    set_px_t(ox+2, oy+4, BRIGHT_GOLD)
    set_px_t(ox+3, oy+3, (180, 140, 40))
    # Dark shadow inside the L
    for yy in range(3, 8):
        set_px_t(ox+3, oy+yy, DARK_BROWN)
    for xx in range(4, 8):
        set_px_t(ox+xx, oy+3, DARK_BROWN)
    # Leather fill in the interior
    leather_grain(ox+4, oy+4, ox+8, oy+8, 74, 42, 26, 8)

def draw_corner_tr(ox, oy):
    """Top-right corner 8×8."""
    fill_rect(ox, oy, ox+8, oy+8, 0, 0, 0, 0)
    # Top 3 rows
    for yy in range(3):
        for xx in range(8):
            shade = [MID_GOLD, GOLD, DARK_GOLD][yy]
            set_px_t(ox+xx, oy+yy, shade)
    # Right 3 cols
    for xx in range(5, 8):
        for yy in range(3, 8):
            shade = [DARK_GOLD, GOLD, MID_GOLD][xx-5]
            set_px_t(ox+xx, oy+yy, shade)
    # Rivet
    set_px_t(ox+7, oy, BRIGHT_GOLD)
    set_px_t(ox+6, oy, BRIGHT_GOLD)
    set_px_t(ox+7, oy+1, BRIGHT_GOLD)
    # Flourish
    set_px_t(ox+3, oy+2, BRIGHT_GOLD)
    set_px_t(ox+5, oy+4, BRIGHT_GOLD)
    set_px_t(ox+4, oy+3, (180, 140, 40))
    # Shadow
    for yy in range(3, 8):
        set_px_t(ox+4, oy+yy, DARK_BROWN)
    for xx in range(0, 4):
        set_px_t(ox+xx, oy+3, DARK_BROWN)
    # Leather
    leather_grain(ox, oy+4, ox+4, oy+8, 74, 42, 26, 8)

def draw_corner_bl(ox, oy):
    """Bottom-left corner 8×8."""
    fill_rect(ox, oy, ox+8, oy+8, 0, 0, 0, 0)
    # Left 3 cols
    for xx in range(3):
        for yy in range(5):
            shade = [MID_GOLD, GOLD, DARK_GOLD][xx]
            set_px_t(ox+xx, oy+yy, shade)
    # Bottom 3 rows
    for yy in range(5, 8):
        for xx in range(8):
            shade = [DARK_GOLD, GOLD, MID_GOLD][yy-5]
            set_px_t(ox+xx, oy+yy, shade)
    # Rivet
    set_px_t(ox, oy+7, BRIGHT_GOLD)
    set_px_t(ox+1, oy+7, BRIGHT_GOLD)
    set_px_t(ox, oy+6, BRIGHT_GOLD)
    # Flourish
    set_px_t(ox+2, oy+3, BRIGHT_GOLD)
    set_px_t(ox+4, oy+5, BRIGHT_GOLD)
    set_px_t(ox+3, oy+4, (180, 140, 40))
    # Shadow
    for yy in range(0, 5):
        set_px_t(ox+3, oy+yy, DARK_BROWN)
    for xx in range(4, 8):
        set_px_t(ox+xx, oy+4, DARK_BROWN)
    # Leather
    leather_grain(ox+4, oy, ox+8, oy+4, 74, 42, 26, 8)

def draw_corner_br(ox, oy):
    """Bottom-right corner 8×8."""
    fill_rect(ox, oy, ox+8, oy+8, 0, 0, 0, 0)
    # Right 3 cols
    for xx in range(5, 8):
        for yy in range(5):
            shade = [DARK_GOLD, GOLD, MID_GOLD][xx-5]
            set_px_t(ox+xx, oy+yy, shade)
    # Bottom 3 rows
    for yy in range(5, 8):
        for xx in range(8):
            shade = [DARK_GOLD, GOLD, MID_GOLD][yy-5]
            set_px_t(ox+xx, oy+yy, shade)
    # Rivet
    set_px_t(ox+7, oy+7, BRIGHT_GOLD)
    set_px_t(ox+6, oy+7, BRIGHT_GOLD)
    set_px_t(ox+7, oy+6, BRIGHT_GOLD)
    # Flourish
    set_px_t(ox+5, oy+3, BRIGHT_GOLD)
    set_px_t(ox+3, oy+5, BRIGHT_GOLD)
    set_px_t(ox+4, oy+4, (180, 140, 40))
    # Shadow
    for yy in range(0, 5):
        set_px_t(ox+4, oy+yy, DARK_BROWN)
    for xx in range(0, 4):
        set_px_t(ox+xx, oy+4, DARK_BROWN)
    # Leather
    leather_grain(ox, oy, ox+4, oy+4, 74, 42, 26, 8)

draw_corner_tl(0, 0)
draw_corner_tr(40, 0)
draw_corner_bl(0, 40)
draw_corner_br(40, 40)


# ═══════════════════════════════════════════════════════════════
# 2. BUTTON NORMAL at (0,48) — 48×20
# ═══════════════════════════════════════════════════════════════
bx, by = 0, 48

# Fill interior with leather grain
leather_grain(bx+2, by+2, bx+46, by+18, 74, 42, 26, 8)

# 2px gold border — brighter on top for 3D bevel
# Top border
for xx in range(bx, bx+48):
    set_px_t(xx, by, GOLD)
    set_px_t(xx, by+1, MID_GOLD)
# Left border
for yy in range(by, by+20):
    set_px_t(bx, yy, GOLD)
    set_px_t(bx+1, yy, MID_GOLD)
# Bottom border — darker for shadow
for xx in range(bx, bx+48):
    set_px_t(xx, by+19, DARK_GOLD)
    set_px_t(xx, by+18, (107, 80, 16))
# Right border
for yy in range(by, by+20):
    set_px_t(bx+47, yy, DARK_GOLD)
    set_px_t(bx+46, yy, (107, 80, 16))

# Corner bright pixel
set_px_t(bx, by, BRIGHT_GOLD)
set_px_t(bx+1, by, BRIGHT_GOLD)
set_px_t(bx, by+1, BRIGHT_GOLD)
# Opposite corner subtle
set_px_t(bx+47, by+19, VERY_DARK_GOLD)

# Inner highlight line at top
for xx in range(bx+2, bx+46):
    cur = get_px(xx, by+2)
    set_px(xx, by+2, min(255, cur[0]+12), min(255, cur[1]+8), min(255, cur[2]+5))


# ═══════════════════════════════════════════════════════════════
# 3. BUTTON HOVER at (0,68) — 48×20
# ═══════════════════════════════════════════════════════════════
bx, by = 0, 68

# Brighter leather interior
leather_grain(bx+2, by+2, bx+46, by+18, 90, 58, 42, 10)

# Brighter gold border
for xx in range(bx, bx+48):
    set_px_t(xx, by, BRIGHT_GOLD)
    set_px_t(xx, by+1, GOLD)
for xx in range(bx, bx+48):
    set_px_t(xx, by+19, MID_GOLD)
    set_px_t(xx, by+18, DARK_GOLD)
for yy in range(by, by+20):
    set_px_t(bx, yy, BRIGHT_GOLD)
    set_px_t(bx+1, yy, GOLD)
for yy in range(by, by+20):
    set_px_t(bx+47, yy, MID_GOLD)
    set_px_t(bx+46, yy, DARK_GOLD)

# Glow pixels at corners
for c in [(bx, by), (bx+1, by), (bx, by+1), (bx+47, by), (bx+46, by), (bx+47, by+1),
          (bx, by+19), (bx+1, by+19), (bx, by+18), (bx+47, by+19), (bx+46, by+19), (bx+47, by+18)]:
    set_px_t(c[0], c[1], BRIGHT_GOLD)

# 1px golden glow inside border edges
for xx in range(bx+2, bx+46):
    set_px(xx, by+2, 107, 74, 26)
    set_px(xx, by+17, 107, 74, 26)
for yy in range(by+2, by+18):
    set_px(bx+2, yy, 107, 74, 26)
    set_px(bx+45, yy, 107, 74, 26)


# ═══════════════════════════════════════════════════════════════
# 4. TAB ACTIVE at (0,88) — 48×24
# ═══════════════════════════════════════════════════════════════
tx, ty = 0, 88

# Interior: leather grain matching panel center
leather_grain(tx+3, ty+3, tx+45, ty+24, 74, 42, 26, 10)

# Gold border top (3px)
for xx in range(tx, tx+48):
    set_px_t(xx, ty, MID_GOLD)
    set_px_t(xx, ty+1, GOLD)
    set_px_t(xx, ty+2, DARK_GOLD)

# Gold border left (3px)
for yy in range(ty, ty+24):
    set_px_t(tx, yy, MID_GOLD)
    set_px_t(tx+1, yy, GOLD)
    set_px_t(tx+2, yy, DARK_GOLD)

# Gold border right (3px)
for yy in range(ty, ty+24):
    set_px_t(tx+45, yy, DARK_GOLD)
    set_px_t(tx+46, yy, GOLD)
    set_px_t(tx+47, yy, MID_GOLD)

# NO bottom border — tab connects to panel
# Corner rivets
set_px_t(tx, ty, BRIGHT_GOLD)
set_px_t(tx+1, ty, BRIGHT_GOLD)
set_px_t(tx, ty+1, BRIGHT_GOLD)
set_px_t(tx+47, ty, BRIGHT_GOLD)
set_px_t(tx+46, ty, BRIGHT_GOLD)
set_px_t(tx+47, ty+1, BRIGHT_GOLD)

# Small gold diamond (3×3) centered at top edge
cx = tx + 24
set_px_t(cx, ty+1, BRIGHT_GOLD)
set_px_t(cx-1, ty+2, BRIGHT_GOLD)
set_px_t(cx, ty+2, BRIGHT_GOLD)
set_px_t(cx+1, ty+2, BRIGHT_GOLD)
set_px_t(cx, ty+3, GOLD)


# ═══════════════════════════════════════════════════════════════
# 5. TAB INACTIVE at (48,88) — 48×24
# ═══════════════════════════════════════════════════════════════
tx, ty = 48, 88

# Dark interior
fill_rect(tx+1, ty+1, tx+47, ty+23, 26, 14, 8)

# Add very subtle grain to inactive interior
for yy in range(ty+1, ty+23):
    for xx in range(tx+1, tx+47):
        if random.random() < 0.15:
            v = random.randint(-4, 4)
            cur = get_px(xx, yy)
            set_px(xx, yy, max(0, cur[0]+v), max(0, cur[1]+v), max(0, cur[2]+v))

# 1px dark border all around
for xx in range(tx, tx+48):
    set_px_t(xx, ty, (42, 26, 16))
    set_px_t(xx, ty+23, (42, 26, 16))
for yy in range(ty, ty+24):
    set_px_t(tx, yy, (42, 26, 16))
    set_px_t(tx+47, yy, (42, 26, 16))

# Slightly brighter top line for minimal highlight
for xx in range(tx+1, tx+47):
    set_px(xx, ty+1, 36, 22, 14)


# ═══════════════════════════════════════════════════════════════
# 6. PROGRESS BAR BACKGROUND at (0,112) — 48×10
# ═══════════════════════════════════════════════════════════════
px_, py_ = 0, 112

# Very dark interior
fill_rect(px_+2, py_+2, px_+46, py_+8, 10, 6, 4)

# 2px ornate gold frame (darker gold)
for xx in range(px_, px_+48):
    set_px_t(xx, py_, DARK_GOLD)
    set_px_t(xx, py_+1, VERY_DARK_GOLD)
    set_px_t(xx, py_+8, VERY_DARK_GOLD)
    set_px_t(xx, py_+9, DARK_GOLD)
for yy in range(py_, py_+10):
    set_px_t(px_, yy, DARK_GOLD)
    set_px_t(px_+1, yy, VERY_DARK_GOLD)
    set_px_t(px_+46, yy, VERY_DARK_GOLD)
    set_px_t(px_+47, yy, DARK_GOLD)

# Gold rivet dots at ends
set_px_t(px_+2, py_+2, GOLD)
set_px_t(px_+3, py_+2, MID_GOLD)
set_px_t(px_+2, py_+3, MID_GOLD)
set_px_t(px_+45, py_+2, GOLD)
set_px_t(px_+44, py_+2, MID_GOLD)
set_px_t(px_+45, py_+3, MID_GOLD)
set_px_t(px_+2, py_+7, MID_GOLD)
set_px_t(px_+45, py_+7, MID_GOLD)


# ═══════════════════════════════════════════════════════════════
# 7. PROGRESS BAR FILL at (0,122) — 48×10
# ═══════════════════════════════════════════════════════════════
px_, py_ = 0, 122

# Warm gradient: amber -> gold -> bright gold
for xx in range(px_, px_+48):
    t = (xx - px_) / 47.0
    if t < 0.5:
        # amber to gold
        t2 = t / 0.5
        r = int(DARK_GOLD[0] + (GOLD[0] - DARK_GOLD[0]) * t2)
        g = int(DARK_GOLD[1] + (GOLD[1] - DARK_GOLD[1]) * t2)
        b = int(DARK_GOLD[2] + (GOLD[2] - DARK_GOLD[2]) * t2)
    else:
        # gold to bright gold
        t2 = (t - 0.5) / 0.5
        r = int(GOLD[0] + (BRIGHT_GOLD[0] - GOLD[0]) * t2)
        g = int(GOLD[1] + (BRIGHT_GOLD[1] - GOLD[1]) * t2)
        b = int(GOLD[2] + (BRIGHT_GOLD[2] - GOLD[2]) * t2)
    for yy in range(py_, py_+10):
        set_px(xx, yy, r, g, b)

# 1px brighter highlight on top row
for xx in range(px_, px_+48):
    cur = get_px(xx, py_)
    set_px(xx, py_, min(255, cur[0]+30), min(255, cur[1]+25), min(255, cur[2]+15))

# 1px darker shadow on bottom row
for xx in range(px_, px_+48):
    cur = get_px(xx, py_+9)
    set_px(xx, py_+9, max(0, cur[0]-40), max(0, cur[1]-35), max(0, cur[2]-20))

# Diagonal striping for texture
for xx in range(px_, px_+48):
    for yy in range(py_+1, py_+9):
        if (xx + yy) % 4 == 0:
            cur = get_px(xx, yy)
            set_px(xx, yy, min(255, cur[0]+10), min(255, cur[1]+8), min(255, cur[2]+5))
        elif (xx + yy) % 4 == 2:
            cur = get_px(xx, yy)
            set_px(xx, yy, max(0, cur[0]-8), max(0, cur[1]-6), max(0, cur[2]-4))


# ═══════════════════════════════════════════════════════════════
# 8. INVENTORY SLOT at (0,132) — 18×18
# ═══════════════════════════════════════════════════════════════
sx, sy = 0, 132

# Interior: dark brown
fill_rect(sx+2, sy+2, sx+16, sy+16, 42, 26, 16)

# Subtle grain inside
for yy in range(sy+2, sy+16):
    for xx in range(sx+2, sx+16):
        if random.random() < 0.2:
            v = random.randint(-6, 6)
            cur = get_px(xx, yy)
            set_px(xx, yy, max(0, cur[0]+v), max(0, cur[1]+v), max(0, cur[2]+v))

# 2px inset border: top-left darker, bottom-right lighter
# Top edge (dark)
for xx in range(sx, sx+18):
    set_px(xx, sy, 10, 4, 2)
    set_px(xx, sy+1, 18, 8, 4)
# Left edge (dark)
for yy in range(sy, sy+18):
    set_px(sx, yy, 10, 4, 2)
    set_px(sx+1, yy, 18, 8, 4)
# Bottom edge (lighter)
for xx in range(sx, sx+18):
    set_px(xx, sy+17, 90, 58, 42)
    set_px(xx, sy+16, 74, 48, 34)
# Right edge (lighter)
for yy in range(sy, sy+18):
    set_px(sx+17, yy, 90, 58, 42)
    set_px(sx+16, yy, 74, 48, 34)

# Gold corner pixels
set_px_t(sx, sy, DARK_GOLD)
set_px_t(sx+17, sy, DARK_GOLD)
set_px_t(sx, sy+17, DARK_GOLD)
set_px_t(sx+17, sy+17, DARK_GOLD)
# Brighter inner corner accents
set_px_t(sx+1, sy+1, (107, 80, 16))
set_px_t(sx+16, sy+1, (107, 80, 16))
set_px_t(sx+1, sy+16, (107, 80, 16))
set_px_t(sx+16, sy+16, (107, 80, 16))


# ═══════════════════════════════════════════════════════════════
# 9. HUD OVERLAY PANEL at (0,150) — 64×32
# ═══════════════════════════════════════════════════════════════
hx, hy = 0, 150

# Semi-transparent dark brown background
fill_rect(hx+1, hy+1, hx+63, hy+31, 42, 26, 16, 180)

# Rounded corners: make corner pixels transparent
set_px(hx+1, hy+1, 0, 0, 0, 0)
set_px(hx+62, hy+1, 0, 0, 0, 0)
set_px(hx+1, hy+30, 0, 0, 0, 0)
set_px(hx+62, hy+30, 0, 0, 0, 0)

# 1px semi-transparent gold border
for xx in range(hx+2, hx+62):
    set_px(xx, hy, 200, 168, 76, 120)
    set_px(xx, hy+31, 200, 168, 76, 120)
for yy in range(hy+2, hy+30):
    set_px(hx, yy, 200, 168, 76, 120)
    set_px(hx+63, yy, 200, 168, 76, 120)
# Connect border near corners
set_px(hx+1, hy, 200, 168, 76, 80)
set_px(hx+62, hy, 200, 168, 76, 80)
set_px(hx, hy+1, 200, 168, 76, 80)
set_px(hx+63, hy+1, 200, 168, 76, 80)
set_px(hx+1, hy+31, 200, 168, 76, 80)
set_px(hx+62, hy+31, 200, 168, 76, 80)
set_px(hx, hy+30, 200, 168, 76, 80)
set_px(hx+63, hy+30, 200, 168, 76, 80)

# Gold corner dots (brighter, more opaque)
for (cx, cy) in [(hx+2, hy+1), (hx+61, hy+1), (hx+2, hy+30), (hx+61, hy+30)]:
    set_px(cx, cy, 218, 165, 32, 200)
for (cx, cy) in [(hx+1, hy+2), (hx+62, hy+2), (hx+1, hy+29), (hx+62, hy+29)]:
    set_px(cx, cy, 218, 165, 32, 200)


# ═══════════════════════════════════════════════════════════════
# 10. SCROLL BAR TRACK at (0,182) — 8×48
# ═══════════════════════════════════════════════════════════════
stx, sty = 0, 182

# Dark brown interior
fill_rect(stx+1, sty+1, stx+7, sty+47, 26, 10, 4)

# 1px gold border on left and right
for yy in range(sty, sty+48):
    set_px_t(stx, yy, DARK_GOLD)
    set_px_t(stx+7, yy, DARK_GOLD)

# Top and bottom caps
for xx in range(stx, stx+8):
    set_px_t(xx, sty, DARK_GOLD)
    set_px_t(xx, sty+47, DARK_GOLD)

# Gold dots at top and bottom center
set_px_t(stx+3, sty+1, GOLD)
set_px_t(stx+4, sty+1, GOLD)
set_px_t(stx+3, sty+46, GOLD)
set_px_t(stx+4, sty+46, GOLD)

# Subtle vertical groove lines in center
for yy in range(sty+3, sty+45):
    if yy % 3 == 0:
        set_px(stx+3, yy, 18, 6, 2)
        set_px(stx+4, yy, 18, 6, 2)


# ═══════════════════════════════════════════════════════════════
# 11. SCROLL BAR THUMB at (8,182) — 8×16
# ═══════════════════════════════════════════════════════════════
sbx, sby = 8, 182

# Gold fill with vertical gradient: brighter top, darker bottom
for yy in range(sby, sby+16):
    t = (yy - sby) / 15.0
    if t < 0.3:
        col = GOLD
    elif t < 0.7:
        col = MID_GOLD
    else:
        col = DARK_GOLD
    for xx in range(sbx+1, sbx+7):
        set_px_t(xx, yy, col)

# Brighter top highlight
for xx in range(sbx+1, sbx+7):
    set_px_t(xx, sby, BRIGHT_GOLD)
    set_px_t(xx, sby+1, GOLD)

# Darker bottom shadow
for xx in range(sbx+1, sbx+7):
    set_px_t(xx, sby+14, DARK_GOLD)
    set_px_t(xx, sby+15, VERY_DARK_GOLD)

# 1px dark border
for yy in range(sby, sby+16):
    set_px(sbx, yy, 90, 58, 10)
    set_px(sbx+7, yy, 90, 58, 10)
for xx in range(sbx, sbx+8):
    set_px(xx, sby, 90, 58, 10)
    set_px(xx, sby+15, 90, 58, 10)

# Grip lines in center
for i in range(-2, 3):
    yy = sby + 8 + i
    set_px_t(sbx+2, yy, BRIGHT_GOLD if i % 2 == 0 else DARK_GOLD)
    set_px_t(sbx+3, yy, BRIGHT_GOLD if i % 2 == 0 else DARK_GOLD)
    set_px_t(sbx+4, yy, BRIGHT_GOLD if i % 2 == 0 else DARK_GOLD)
    set_px_t(sbx+5, yy, BRIGHT_GOLD if i % 2 == 0 else DARK_GOLD)


# ═══════════════════════════════════════════════════════════════
# 12. HEALTH BAR FRAME at (0,230) — 128×12
# ═══════════════════════════════════════════════════════════════
hbx, hby = 0, 230

# Very dark interior
fill_rect(hbx+2, hby+2, hbx+126, hby+10, 10, 6, 4)

# 2px ornate gold frame
# Top
for xx in range(hbx, hbx+128):
    set_px_t(xx, hby, MID_GOLD)
    set_px_t(xx, hby+1, GOLD)
# Bottom
for xx in range(hbx, hbx+128):
    set_px_t(xx, hby+10, GOLD)
    set_px_t(xx, hby+11, MID_GOLD)
# Left
for yy in range(hby, hby+12):
    set_px_t(hbx, yy, MID_GOLD)
    set_px_t(hbx+1, yy, GOLD)
# Right
for yy in range(hby, hby+12):
    set_px_t(hbx+126, yy, GOLD)
    set_px_t(hbx+127, yy, MID_GOLD)

# Corner rivets
for (cx, cy) in [(hbx, hby), (hbx+1, hby), (hbx, hby+1),
                  (hbx+127, hby), (hbx+126, hby), (hbx+127, hby+1),
                  (hbx, hby+11), (hbx+1, hby+11), (hbx, hby+10),
                  (hbx+127, hby+11), (hbx+126, hby+11), (hbx+127, hby+10)]:
    set_px_t(cx, cy, BRIGHT_GOLD)

# Gold rivet at left end
set_px_t(hbx+3, hby+4, BRIGHT_GOLD)
set_px_t(hbx+4, hby+4, GOLD)
set_px_t(hbx+3, hby+5, GOLD)
set_px_t(hbx+4, hby+5, MID_GOLD)
set_px_t(hbx+3, hby+6, GOLD)
set_px_t(hbx+4, hby+6, GOLD)
set_px_t(hbx+3, hby+7, BRIGHT_GOLD)
set_px_t(hbx+4, hby+7, GOLD)

# Heart-shaped gold detail at right end
# A tiny 5x5 heart
hrt_x = hbx + 121
hrt_y = hby + 3
# Row 0:  .X.X.
set_px_t(hrt_x+1, hrt_y, BRIGHT_GOLD)
set_px_t(hrt_x+3, hrt_y, BRIGHT_GOLD)
# Row 1: XXXXX
for i in range(5):
    set_px_t(hrt_x+i, hrt_y+1, GOLD)
# Row 2: XXXXX
for i in range(5):
    set_px_t(hrt_x+i, hrt_y+2, MID_GOLD)
# Row 3: .XXX.
set_px_t(hrt_x+1, hrt_y+3, DARK_GOLD)
set_px_t(hrt_x+2, hrt_y+3, GOLD)
set_px_t(hrt_x+3, hrt_y+3, DARK_GOLD)
# Row 4: ..X..
set_px_t(hrt_x+2, hrt_y+4, DARK_GOLD)

# Decorative dot pattern along the frame
for xx in range(hbx+8, hbx+120, 8):
    set_px_t(xx, hby+1, BRIGHT_GOLD)
    set_px_t(xx, hby+10, BRIGHT_GOLD)


# ═══════════════════════════════════════════════════════════════
# 13. TEMPERATURE/THIRST BAR FRAME at (128,230) — 64×12
# ═══════════════════════════════════════════════════════════════
tbx, tby = 128, 230

# Very dark interior
fill_rect(tbx+2, tby+2, tbx+62, tby+10, 10, 6, 4)

# 2px gold frame
for xx in range(tbx, tbx+64):
    set_px_t(xx, tby, MID_GOLD)
    set_px_t(xx, tby+1, GOLD)
    set_px_t(xx, tby+10, GOLD)
    set_px_t(xx, tby+11, MID_GOLD)
for yy in range(tby, tby+12):
    set_px_t(tbx, yy, MID_GOLD)
    set_px_t(tbx+1, yy, GOLD)
    set_px_t(tbx+62, yy, GOLD)
    set_px_t(tbx+63, yy, MID_GOLD)

# Corner rivets
for (cx, cy) in [(tbx, tby), (tbx+1, tby), (tbx, tby+1),
                  (tbx+63, tby), (tbx+62, tby), (tbx+63, tby+1),
                  (tbx, tby+11), (tbx+1, tby+11), (tbx, tby+10),
                  (tbx+63, tby+11), (tbx+62, tby+11), (tbx+63, tby+10)]:
    set_px_t(cx, cy, BRIGHT_GOLD)

# Flame icon at left end (for temperature) — 5x7 flame
fl_x = tbx + 3
fl_y = tby + 2
# Tiny flame shape
set_px_t(fl_x+2, fl_y, BRIGHT_GOLD)       # tip
set_px_t(fl_x+1, fl_y+1, GOLD)
set_px_t(fl_x+2, fl_y+1, BRIGHT_GOLD)
set_px_t(fl_x+3, fl_y+1, GOLD)
set_px_t(fl_x+1, fl_y+2, GOLD)
set_px_t(fl_x+2, fl_y+2, (255, 180, 0))   # orange center
set_px_t(fl_x+3, fl_y+2, GOLD)
set_px_t(fl_x, fl_y+3, DARK_GOLD)
set_px_t(fl_x+1, fl_y+3, GOLD)
set_px_t(fl_x+2, fl_y+3, (255, 180, 0))
set_px_t(fl_x+3, fl_y+3, GOLD)
set_px_t(fl_x+4, fl_y+3, DARK_GOLD)
set_px_t(fl_x+1, fl_y+4, DARK_GOLD)
set_px_t(fl_x+2, fl_y+4, GOLD)
set_px_t(fl_x+3, fl_y+4, DARK_GOLD)
set_px_t(fl_x+2, fl_y+5, DARK_GOLD)

# Water drop icon at right end (for thirst) — 5x7 drop
dr_x = tbx + 57
dr_y = tby + 2
set_px_t(dr_x+2, dr_y, (100, 160, 220))      # tip
set_px_t(dr_x+1, dr_y+1, (80, 140, 200))
set_px_t(dr_x+2, dr_y+1, (120, 180, 240))
set_px_t(dr_x+3, dr_y+1, (80, 140, 200))
set_px_t(dr_x, dr_y+2, (60, 120, 180))
set_px_t(dr_x+1, dr_y+2, (100, 160, 220))
set_px_t(dr_x+2, dr_y+2, (140, 200, 255))     # bright center
set_px_t(dr_x+3, dr_y+2, (100, 160, 220))
set_px_t(dr_x+4, dr_y+2, (60, 120, 180))
set_px_t(dr_x, dr_y+3, (60, 120, 180))
set_px_t(dr_x+1, dr_y+3, (100, 160, 220))
set_px_t(dr_x+2, dr_y+3, (120, 180, 240))
set_px_t(dr_x+3, dr_y+3, (100, 160, 220))
set_px_t(dr_x+4, dr_y+3, (60, 120, 180))
set_px_t(dr_x+1, dr_y+4, (80, 140, 200))
set_px_t(dr_x+2, dr_y+4, (100, 160, 220))
set_px_t(dr_x+3, dr_y+4, (80, 140, 200))
set_px_t(dr_x+2, dr_y+5, (60, 120, 180))

# Decorative dots along frame
for xx in range(tbx+10, tbx+54, 8):
    set_px_t(xx, tby+1, BRIGHT_GOLD)
    set_px_t(xx, tby+10, BRIGHT_GOLD)


# ═══════════════════════════════════════════════════════════════
# SAVE
# ═══════════════════════════════════════════════════════════════
import os
output_path = r"C:\Users\JulianCalero\OneDrive - Associated Rebar\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\gui\widgets.png"
os.makedirs(os.path.dirname(output_path), exist_ok=True)
img.save(output_path)
print(f"Saved widgets.png to {output_path}")
print(f"Image size: {img.size}")
print(f"Image mode: {img.mode}")
