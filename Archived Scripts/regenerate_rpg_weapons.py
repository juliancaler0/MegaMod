"""
Regenerate 7 RPG weapon textures at 32x32 to match arsenal weapon style.
Target: 15-21% canvas coverage, 15-25 colors, diagonal orientation, 5-6 shade gradients.
Light from top-left. Handle bottom-left, tip/head top-right.

Weapons:
  1. Vampiric Tome     - Dark leather spellbook with blood-red eye emblem and clasps
  2. Static Seeker     - Electrified wand with copper coils and crackling energy
  3. Battledancer      - Elegant dual-edged curved sword, nimble and light
  4. Ebonchill         - Frost-infused dark greatsword with ice crystal accents
  5. Lightbinder       - Holy golden scepter/mace radiating divine light
  6. Crescent Blade    - Curved moon-shaped blade on a pole, silver with purple glow
  7. Ghost Fang        - Ethereal spectral dagger/short sword, ghostly teal translucent
"""
from PIL import Image
import os, math

OUTPUT_DIR = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\item"

def lighten(c, amt=30):
    return tuple(min(255, v + amt) for v in c[:3]) + ((c[3],) if len(c) > 3 else (255,))

def darken(c, amt=30):
    return tuple(max(0, v - amt) for v in c[:3]) + ((c[3],) if len(c) > 3 else (255,))

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    r = int(c1[0] + (c2[0] - c1[0]) * t)
    g = int(c1[1] + (c2[1] - c1[1]) * t)
    b = int(c1[2] + (c2[2] - c1[2]) * t)
    a1 = c1[3] if len(c1) > 3 else 255
    a2 = c2[3] if len(c2) > 3 else 255
    a = int(a1 + (a2 - a1) * t)
    return (r, g, b, a)

def create(size=32):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    return img, img.load()

def save(img, name):
    path = os.path.join(OUTPUT_DIR, f"{name}.png")
    img.save(path)
    px = img.load()
    w, h = img.size
    filled = sum(1 for y in range(h) for x in range(w) if px[x, y][3] > 0)
    colors = len(set(px[x, y][:3] for y in range(h) for x in range(w) if px[x, y][3] > 0))
    pct = filled * 100 // (w * h)
    print(f"  {name}.png ({w}x{h}): {filled}px ({pct}%), {colors} colors")

def px(p, x, y, c, S=32):
    if 0 <= x < S and 0 <= y < S:
        p[x, y] = c if len(c) == 4 else c + (255,)


# ============================================================
# 1. VAMPIRIC TOME — Dark spellbook with blood-red accents
#    Diagonal book shape, open slightly, with eye symbol and clasps
# ============================================================
def gen_vampiric_tome():
    img, p = create()
    S = 32

    # Color palette — dark leather with blood red accents
    cover_hi = (80, 25, 25, 255)
    cover_mid = (60, 18, 18, 255)
    cover_dk = (40, 10, 10, 255)
    cover_outline = (25, 5, 5, 255)
    page_hi = (230, 220, 200, 255)
    page_mid = (200, 190, 170, 255)
    page_dk = (170, 160, 140, 255)
    spine_hi = (50, 15, 15, 255)
    spine_dk = (30, 8, 8, 255)
    clasp_gold = (200, 170, 50, 255)
    clasp_dk = (150, 120, 30, 255)
    eye_red = (200, 30, 30, 255)
    eye_bright = (255, 60, 60, 255)
    eye_pupil = (20, 0, 0, 255)
    blood_glow = (180, 20, 20, 100)

    # Book body — angled rectangle, top-right to bottom-left
    # Front cover
    for i in range(18):
        t = i / 17.0
        bx = 24 - i
        by = 4 + int(i * 0.8)
        w = 10 - int(t * 3)
        for dx in range(-w // 2, w // 2 + 1):
            norm = (dx + w // 2) / max(1, w)
            if norm < 0.1 or norm > 0.9:
                c = cover_outline
            elif norm < 0.3:
                c = cover_hi
            elif norm < 0.6:
                c = lerp(cover_mid, cover_dk, t)
            else:
                c = cover_dk
            px(p, bx + dx, by, c, S)

    # Pages visible on the side (slightly lighter strip)
    for i in range(14):
        t = i / 13.0
        bx = 22 - i
        by = 6 + int(i * 0.8)
        c = lerp(page_hi, page_dk, t)
        px(p, bx + 5, by, c, S)
        px(p, bx + 6, by, page_mid, S)

    # Spine — left edge of book
    for i in range(16):
        t = i / 15.0
        sx = 24 - i - 5
        sy = 4 + int(i * 0.8)
        c = lerp(spine_hi, spine_dk, t)
        px(p, sx, sy, c, S)
        px(p, sx - 1, sy, spine_dk, S)

    # Eye emblem in center of cover
    eye_cx = 17
    eye_cy = 11
    # Eye shape — horizontal diamond
    for dy in range(-2, 3):
        w = 3 - abs(dy)
        for dx in range(-w, w + 1):
            dist = abs(dx) + abs(dy)
            if dist <= 1:
                c = eye_pupil
            elif abs(dx) <= 1 and abs(dy) <= 1:
                c = eye_red
            else:
                c = eye_bright
            px(p, eye_cx + dx, eye_cy + dy, c, S)
    # Eye glow
    for angle in range(0, 360, 60):
        rad = angle * 3.14159 / 180
        gx = int(eye_cx + 3.5 * math.cos(rad))
        gy = int(eye_cy + 3.5 * math.sin(rad))
        px(p, gx, gy, blood_glow, S)

    # Gold clasps — top and bottom of book
    for off in [(22, 6), (10, 17)]:
        cx, cy = off
        px(p, cx, cy, clasp_gold, S)
        px(p, cx + 1, cy, clasp_dk, S)
        px(p, cx, cy + 1, clasp_dk, S)

    # Corner decorations
    px(p, 25, 4, clasp_gold, S)
    px(p, 8, 18, clasp_gold, S)

    save(img, "vampiric_tome")


# ============================================================
# 2. STATIC SEEKER — Electrified wand/rod with copper coils
#    Staff-style diagonal, copper shaft, electric orb at tip
# ============================================================
def gen_static_seeker():
    img, p = create()
    S = 32

    # Copper/electric palette
    copper_hi = (220, 150, 80, 255)
    copper_mid = (180, 110, 55, 255)
    copper_dk = (130, 75, 35, 255)
    copper_outline = (80, 45, 20, 255)
    coil_hi = (240, 180, 100, 255)
    coil_dk = (160, 100, 50, 255)
    electric_core = (180, 220, 255, 255)
    electric_bright = (220, 240, 255, 255)
    electric_mid = (100, 170, 240, 255)
    electric_dk = (60, 120, 200, 255)
    spark = (255, 255, 200, 200)
    handle_wrap = (100, 60, 30, 255)
    handle_alt = (80, 50, 25, 255)

    # Shaft — diagonal from bottom-left to top-right
    shaft_len = 20
    for i in range(shaft_len):
        t = i / max(1, shaft_len - 1)
        sx = 6 + int(i * 1.05)
        sy = 28 - i
        for dx in range(-1, 2):
            if dx == -1:
                c = copper_hi
            elif dx == 0:
                c = lerp(copper_mid, copper_dk, t)
            else:
                c = copper_dk
            px(p, sx + dx, sy, c, S)
        # Coil wrapping every 3 pixels
        if i % 3 == 0 and i < shaft_len - 4:
            px(p, sx - 2, sy, coil_hi, S)
            px(p, sx - 1, sy, coil_hi, S)
            px(p, sx + 2, sy, coil_dk, S)

    # Handle wrap at bottom
    for i in range(5):
        hx = 6 + int(i * 1.05)
        hy = 28 - i
        c = handle_wrap if i % 2 == 0 else handle_alt
        px(p, hx, hy, c, S)

    # Electric orb at top
    orb_cx = 26
    orb_cy = 7
    orb_r = 4
    for dy in range(-orb_r, orb_r + 1):
        for dx in range(-orb_r, orb_r + 1):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < orb_r + 0.5:
                t = dist / orb_r
                shade = (dx - dy) / (orb_r * 2)
                if t < 0.3:
                    c = electric_bright
                elif t < 0.55:
                    c = electric_core
                elif t < 0.8:
                    c = lerp(electric_mid, electric_dk, shade + 0.5)
                else:
                    c = electric_dk
                px(p, orb_cx + dx, orb_cy + dy, c, S)
    # Core highlight
    px(p, orb_cx - 1, orb_cy - 1, (255, 255, 255, 255), S)
    px(p, orb_cx, orb_cy - 1, electric_bright, S)

    # Lightning sparks radiating from orb
    spark_positions = [(orb_cx + 5, orb_cy - 2), (orb_cx + 3, orb_cy - 5),
                       (orb_cx - 3, orb_cy - 3), (orb_cx + 4, orb_cy + 3),
                       (orb_cx - 2, orb_cy + 4)]
    for sx, sy in spark_positions:
        px(p, sx, sy, spark, S)
    # Connecting arcs
    px(p, orb_cx + 4, orb_cy - 1, electric_mid, S)
    px(p, orb_cx + 3, orb_cy - 3, electric_core, S)
    px(p, orb_cx - 1, orb_cy - 4, electric_mid, S)
    px(p, orb_cx + 2, orb_cy + 3, electric_core, S)

    # Prongs holding the orb
    px(p, orb_cx - orb_r, orb_cy + orb_r, copper_hi, S)
    px(p, orb_cx + orb_r, orb_cy + orb_r, copper_dk, S)
    px(p, orb_cx, orb_cy + orb_r + 1, copper_mid, S)

    save(img, "static_seeker")


# ============================================================
# 3. BATTLEDANCER — Elegant curved sword, light and nimble
#    Thin curved blade with ornate guard, rapier-like
# ============================================================
def gen_battledancer():
    img, p = create()
    S = 32

    # Elegant silver/rose-gold palette
    blade_hi = (220, 225, 235, 255)
    blade_mid = (180, 185, 200, 255)
    blade_dk = (130, 135, 155, 255)
    blade_outline = (80, 85, 100, 255)
    guard_hi = (210, 180, 140, 255)
    guard_mid = (180, 145, 105, 255)
    guard_dk = (140, 110, 75, 255)
    guard_gem = (220, 60, 120, 255)
    handle_base = (120, 50, 60, 255)
    handle_wrap = (160, 70, 80, 255)
    handle_alt = (100, 40, 50, 255)
    edge_glow = (240, 240, 255, 180)

    # Curved blade — slight sinusoidal curve
    blade_len = 22
    guard_row = 24
    for i in range(blade_len):
        t = i / max(1, blade_len - 1)
        # Curve: sinusoidal offset for elegance
        curve = int(1.5 * math.sin(t * 3.14159))
        cx = 28 - int(i * (28 - 8) / blade_len) + curve
        cy = 2 + int(i * (guard_row - 2) / blade_len)

        # Taper: narrow tip, slightly wider at base
        half_w = max(1, int(1.0 + t * 1.5))

        for dy in range(-half_w, half_w + 1):
            for dx in range(0, 2):
                norm_dy = (dy + half_w) / max(1, 2 * half_w)
                if norm_dy < 0.15 or norm_dy > 0.85:
                    c = blade_outline
                elif norm_dy < 0.35:
                    c = blade_hi
                elif norm_dy < 0.6:
                    c = lerp(blade_mid, blade_dk, t * 0.5)
                else:
                    c = blade_dk
                px(p, cx + dx, cy + dy, c, S)

        # Sharp edge highlight
        px(p, cx, cy - half_w, edge_glow, S)

    # Tip glow
    px(p, 29, 1, blade_hi, S)
    px(p, 30, 0, edge_glow, S)

    # Ornate curved guard
    guard_cx = 8
    guard_cy = guard_row
    for dx in range(-4, 5):
        curve_g = int(1.2 * math.sin((dx + 4) / 8.0 * 3.14159))
        for dy in range(-1, 2):
            t_g = (dx + 4) / 8.0
            if dy == -1:
                c = guard_hi
            elif dy == 0:
                c = lerp(guard_hi, guard_dk, t_g)
            else:
                c = guard_dk
            px(p, guard_cx + dx, guard_cy + dy + curve_g, c, S)
    # Guard gem
    px(p, guard_cx, guard_cy, guard_gem, S)
    px(p, guard_cx - 1, guard_cy, lighten(guard_gem, 30), S)

    # Handle
    for i in range(6):
        hx = guard_cx - 1 - int(i * 0.8)
        hy = guard_cy + 2 + i
        c = handle_wrap if i % 2 == 0 else handle_alt
        for dx in range(-1, 2):
            cc = c if dx == 0 else handle_base
            px(p, hx + dx, hy, cc, S)
    # Pommel
    px(p, guard_cx - 6, guard_cy + 8, guard_hi, S)
    px(p, guard_cx - 7, guard_cy + 8, guard_mid, S)
    px(p, guard_cx - 6, guard_cy + 9, guard_dk, S)

    save(img, "battledancer")


# ============================================================
# 4. EBONCHILL — Frost-infused dark greatsword
#    Thick dark blade with ice crystal accents and frost glow
# ============================================================
def gen_ebonchill():
    img, p = create()
    S = 32

    # Dark steel + ice palette
    blade_hi = (100, 110, 130, 255)
    blade_mid = (65, 70, 85, 255)
    blade_dk = (35, 38, 50, 255)
    blade_outline = (15, 18, 25, 255)
    ice_bright = (180, 230, 255, 255)
    ice_mid = (120, 190, 230, 255)
    ice_dk = (70, 140, 190, 255)
    ice_glow = (140, 210, 255, 120)
    guard_hi = (90, 95, 110, 255)
    guard_dk = (45, 48, 60, 255)
    guard_gem = (100, 200, 255, 255)
    handle_base = (40, 35, 30, 255)
    handle_wrap = (60, 55, 50, 255)
    handle_alt = (35, 30, 25, 255)

    # Wide blade — greatsword style (wider than normal)
    blade_len = 22
    guard_row = 24
    for i in range(blade_len):
        t = i / max(1, blade_len - 1)
        cx = 28 - int(i * (28 - 9) / blade_len)
        cy = 1 + int(i * (guard_row - 1) / blade_len)
        half_w = max(2, int(3.5 - t * 1.5))

        for dy in range(-half_w, half_w + 1):
            for dx in range(-1, 2):
                norm_dy = (dy + half_w) / max(1, 2 * half_w)
                if norm_dy < 0.1 or norm_dy > 0.9:
                    c = blade_outline
                elif norm_dy < 0.3:
                    c = blade_hi
                elif norm_dy < 0.55:
                    c = lerp(blade_mid, blade_dk, t * 0.6)
                else:
                    c = blade_dk
                px(p, cx + dx, cy + dy, c, S)

        # Central ice vein — runs through the blade
        px(p, cx, cy, lerp(ice_mid, ice_dk, t), S)

    # Ice crystal clusters along blade edge (every 4 pixels)
    for i in range(0, blade_len, 4):
        t = i / max(1, blade_len - 1)
        cx = 28 - int(i * (28 - 9) / blade_len)
        cy = 1 + int(i * (guard_row - 1) / blade_len)
        half_w = max(2, int(3.5 - t * 1.5))
        # Crystal protrusion on one edge
        px(p, cx, cy - half_w - 1, ice_bright, S)
        px(p, cx + 1, cy - half_w - 1, ice_mid, S)
        px(p, cx - 1, cy - half_w - 2, ice_glow, S)

    # Frost glow at tip
    px(p, 29, 0, ice_glow, S)
    px(p, 30, 1, ice_bright, S)
    px(p, 28, 0, ice_mid, S)

    # Guard
    guard_cx = 9
    guard_cy = guard_row
    for dx in range(-5, 6):
        for dy in range(-1, 2):
            t_g = (dx + 5) / 10.0
            c = lerp(guard_hi, guard_dk, t_g) if dy == 0 else (guard_hi if dy < 0 else guard_dk)
            px(p, guard_cx + dx, guard_cy + dy, c, S)
    px(p, guard_cx, guard_cy, guard_gem, S)
    px(p, guard_cx - 1, guard_cy, lighten(guard_gem, 20), S)
    px(p, guard_cx + 1, guard_cy, darken(guard_gem, 20), S)

    # Handle
    for i in range(6):
        hx = guard_cx - 2 - int(i * 0.9)
        hy = guard_cy + 2 + i
        for dx in range(-1, 2):
            c = handle_wrap if (i % 2 == 0 and dx == 0) else handle_base if dx == 0 else handle_alt
            px(p, hx + dx, hy, c, S)
    # Pommel
    pm_x = guard_cx - 7
    pm_y = guard_cy + 8
    px(p, pm_x, pm_y, guard_hi, S)
    px(p, pm_x - 1, pm_y, guard_dk, S)
    px(p, pm_x, pm_y + 1, guard_dk, S)

    save(img, "ebonchill")


# ============================================================
# 5. LIGHTBINDER — Holy golden scepter/mace radiating light
#    Staff with radiant sun-shaped head
# ============================================================
def gen_lightbinder():
    img, p = create()
    S = 32

    # Holy gold/white palette
    shaft_hi = (220, 200, 140, 255)
    shaft_mid = (190, 170, 110, 255)
    shaft_dk = (150, 130, 80, 255)
    shaft_wrap = (255, 240, 180, 255)
    shaft_wrap_alt = (200, 180, 120, 255)
    head_core = (255, 255, 220, 255)
    head_bright = (255, 240, 180, 255)
    head_mid = (240, 210, 130, 255)
    head_dk = (200, 170, 90, 255)
    head_glow = (255, 255, 200, 100)
    white_glow = (255, 255, 255, 150)

    # Shaft — diagonal
    shaft_start_x, shaft_start_y = 16, 14
    shaft_len = 17
    for i in range(shaft_len):
        t = i / max(1, shaft_len - 1)
        sx = shaft_start_x - int(i * 0.75)
        sy = shaft_start_y + i
        for dx in range(-1, 2):
            if dx == -1:
                c = shaft_hi
            elif dx == 0:
                c = lerp(shaft_mid, shaft_dk, t)
            else:
                c = shaft_dk
            px(p, sx + dx, sy, c, S)
        if i % 3 == 0:
            px(p, sx, sy, shaft_wrap if i % 6 == 0 else shaft_wrap_alt, S)

    # End cap
    ex = shaft_start_x - int(shaft_len * 0.75)
    ey = shaft_start_y + shaft_len
    px(p, ex, ey, shaft_dk, S)
    px(p, ex - 1, ey, darken(shaft_dk, 15), S)

    # Radiant sun head — circle with rays
    head_cx = shaft_start_x + 5
    head_cy = shaft_start_y - 5
    orb_r = 4

    # Orb
    for dy in range(-orb_r, orb_r + 1):
        for dx in range(-orb_r, orb_r + 1):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < orb_r + 0.5:
                t = dist / orb_r
                if t < 0.25:
                    c = head_core
                elif t < 0.5:
                    c = head_bright
                elif t < 0.75:
                    c = head_mid
                else:
                    c = head_dk
                px(p, head_cx + dx, head_cy + dy, c, S)

    # Core white highlight
    px(p, head_cx - 1, head_cy - 1, (255, 255, 255, 255), S)
    px(p, head_cx, head_cy - 1, head_core, S)

    # Sun rays — 8 directions
    for angle in range(0, 360, 45):
        rad = angle * 3.14159 / 180
        for dist in range(orb_r + 1, orb_r + 4):
            rx = int(head_cx + dist * math.cos(rad))
            ry = int(head_cy + dist * math.sin(rad))
            t = (dist - orb_r) / 3.0
            c = lerp(head_bright, head_glow, t)
            px(p, rx, ry, c, S)

    # Glow halo
    for angle in range(0, 360, 30):
        rad = angle * 3.14159 / 180
        rx = int(head_cx + (orb_r + 4) * math.cos(rad))
        ry = int(head_cy + (orb_r + 4) * math.sin(rad))
        px(p, rx, ry, white_glow, S)

    # Prongs connecting shaft to head
    px(p, head_cx - orb_r, head_cy + orb_r, shaft_hi, S)
    px(p, head_cx + orb_r, head_cy + orb_r, shaft_dk, S)
    px(p, head_cx, head_cy + orb_r + 1, shaft_mid, S)

    save(img, "lightbinder")


# ============================================================
# 6. CRESCENT BLADE — Curved moon-blade on a pole
#    Polearm with crescent-shaped silver blade, purple accents
# ============================================================
def gen_crescent_blade():
    img, p = create()
    S = 32

    # Silver blade + dark purple accent
    blade_hi = (210, 215, 225, 255)
    blade_mid = (170, 175, 190, 255)
    blade_dk = (120, 125, 145, 255)
    blade_outline = (70, 75, 90, 255)
    purple_hi = (160, 100, 200, 255)
    purple_mid = (120, 60, 170, 255)
    purple_dk = (80, 35, 130, 255)
    purple_glow = (140, 80, 200, 100)
    shaft_hi = (140, 120, 100, 255)
    shaft_mid = (110, 90, 70, 255)
    shaft_dk = (80, 60, 45, 255)
    shaft_wrap = (130, 110, 90, 255)

    # Shaft — long pole, diagonal
    shaft_start_x, shaft_start_y = 14, 16
    shaft_len = 16
    for i in range(shaft_len):
        t = i / max(1, shaft_len - 1)
        sx = shaft_start_x - int(i * 0.75)
        sy = shaft_start_y + i
        for dx in range(-1, 2):
            c = shaft_hi if dx == -1 else shaft_dk if dx == 1 else lerp(shaft_mid, shaft_dk, t)
            px(p, sx + dx, sy, c, S)
        if i % 3 == 0:
            px(p, sx, sy, shaft_wrap, S)

    # Crescent blade — curved arc at top
    # The crescent faces top-right, opening to the left
    blade_cx = 22
    blade_cy = 10
    outer_r = 8
    inner_r = 5

    for dy in range(-outer_r, outer_r + 1):
        for dx in range(-outer_r, outer_r + 1):
            outer_dist = (dx * dx + dy * dy) ** 0.5
            # Offset the inner circle to create crescent shape
            inner_dx = dx + 3
            inner_dy = dy + 1
            inner_dist = (inner_dx * inner_dx + inner_dy * inner_dy) ** 0.5

            if outer_dist < outer_r + 0.5 and inner_dist > inner_r - 0.5:
                t = outer_dist / outer_r
                # Color based on angle for shading
                angle = math.atan2(dy, dx)
                shade = (math.cos(angle - 0.7) + 1) / 2  # light from top-left

                if shade > 0.7:
                    c = blade_hi
                elif shade > 0.4:
                    c = blade_mid
                else:
                    c = blade_dk

                # Outline on outer edge
                if outer_dist > outer_r - 0.8:
                    c = blade_outline

                px(p, blade_cx + dx, blade_cy + dy, c, S)

    # Purple energy along inner edge of crescent
    for angle_deg in range(-60, 120, 15):
        rad = angle_deg * 3.14159 / 180
        ix = int(blade_cx - 3 + inner_r * math.cos(rad))
        iy = int(blade_cy - 1 + inner_r * math.sin(rad))
        px(p, ix, iy, purple_hi, S)
        # Glow
        px(p, ix + 1, iy, purple_glow, S)

    # Purple gem at blade-shaft junction
    jx = shaft_start_x + 2
    jy = shaft_start_y - 1
    px(p, jx, jy, purple_hi, S)
    px(p, jx - 1, jy, purple_mid, S)
    px(p, jx + 1, jy, purple_dk, S)
    px(p, jx, jy - 1, lighten(purple_hi, 20), S)

    save(img, "crescent_blade")


# ============================================================
# 7. GHOST FANG — Ethereal spectral dagger/short sword
#    Translucent ghostly teal blade with wisps
# ============================================================
def gen_ghost_fang():
    img, p = create()
    S = 32

    # Ghostly teal/cyan palette — semi-transparent
    blade_hi = (150, 240, 220, 200)
    blade_mid = (100, 200, 190, 170)
    blade_dk = (60, 160, 155, 140)
    blade_outline = (40, 120, 115, 160)
    blade_core = (200, 255, 245, 220)
    wisp = (120, 220, 210, 100)
    wisp_bright = (180, 255, 240, 130)
    guard_hi = (80, 90, 100, 255)
    guard_dk = (50, 55, 65, 255)
    guard_gem = (100, 255, 230, 255)
    handle_base = (60, 55, 65, 255)
    handle_wrap = (90, 85, 100, 255)
    handle_alt = (50, 45, 55, 255)

    # Blade — short sword, slightly curved, semi-transparent
    blade_len = 18
    guard_row = 20
    for i in range(blade_len):
        t = i / max(1, blade_len - 1)
        curve = int(0.8 * math.sin(t * 2.5))
        cx = 28 - int(i * (28 - 12) / blade_len) + curve
        cy = 2 + int(i * (guard_row - 2) / blade_len)
        half_w = max(1, int(2.5 - t * 1.0))

        for dy in range(-half_w, half_w + 1):
            for dx in range(0, 2):
                norm_dy = (dy + half_w) / max(1, 2 * half_w)
                if norm_dy < 0.15 or norm_dy > 0.85:
                    c = blade_outline
                elif norm_dy < 0.3:
                    c = blade_hi
                elif norm_dy < 0.55:
                    c = lerp(blade_mid, blade_dk, t * 0.5)
                else:
                    c = blade_dk
                px(p, cx + dx, cy + dy, c, S)

        # Ethereal core glow line
        px(p, cx, cy, lerp(blade_core, blade_mid, t), S)

    # Ghostly wisps floating off the blade
    wisp_offsets = [
        (26, 3), (24, 1), (22, 4), (20, 2), (18, 6),
        (27, 6), (23, 8), (16, 9), (25, 9), (19, 5)
    ]
    for wx, wy in wisp_offsets:
        px(p, wx, wy, wisp, S)
    # Brighter wisps near tip
    px(p, 29, 1, wisp_bright, S)
    px(p, 30, 2, wisp, S)
    px(p, 28, 0, wisp_bright, S)
    px(p, 27, 1, blade_core, S)

    # Guard — dark metal, contrasting with ethereal blade
    guard_cx = 12
    guard_cy = guard_row
    for dx in range(-4, 5):
        for dy in range(-1, 2):
            t_g = (dx + 4) / 8.0
            c = lerp(guard_hi, guard_dk, t_g) if dy == 0 else (guard_hi if dy < 0 else guard_dk)
            px(p, guard_cx + dx, guard_cy + dy, c, S)
    px(p, guard_cx, guard_cy, guard_gem, S)
    px(p, guard_cx - 1, guard_cy, lighten(guard_gem, 20), S)

    # Handle
    for i in range(5):
        hx = guard_cx - 1 - int(i * 0.8)
        hy = guard_cy + 2 + i
        for dx in range(-1, 2):
            c = handle_wrap if (i % 2 == 0 and dx == 0) else handle_base if dx == 0 else handle_alt
            px(p, hx + dx, hy, c, S)
    # Pommel
    px(p, guard_cx - 5, guard_cy + 7, guard_hi, S)
    px(p, guard_cx - 6, guard_cy + 7, guard_dk, S)

    save(img, "ghost_fang")


# ============================================================
# MAIN
# ============================================================
if __name__ == '__main__':
    print("Regenerating 7 RPG weapons at 32x32 (arsenal style)...")
    gen_vampiric_tome()
    gen_static_seeker()
    gen_battledancer()
    gen_ebonchill()
    gen_lightbinder()
    gen_crescent_blade()
    gen_ghost_fang()
    print("Done! All 7 weapons regenerated.")
