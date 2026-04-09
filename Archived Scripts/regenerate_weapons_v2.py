"""
Regenerate all 41 new weapon textures with DENSE pixel art matching old weapon quality.
Target: 15-21% canvas coverage, 15-25 colors, 5-7px blade widths.
Studies from old weapons:
  - claymore_1: 216px filled (21%), 22 colors, 7px wide blade, 11px wide guard
  - staff_damage_1: 157px filled (15%), 3px shaft, 10px orb head
  - spear_1: 192px filled (18%), 3px shaft, 12px wide head section
Old weapons run diagonal from top-right to bottom-left corner, filling nearly every row.
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
    # Count filled pixels
    px = img.load()
    w, h = img.size
    filled = sum(1 for y in range(h) for x in range(w) if px[x, y][3] > 0)
    colors = len(set(px[x, y][:3] for y in range(h) for x in range(w) if px[x, y][3] > 0))
    pct = filled * 100 // (w * h)
    print(f"  {name}.png ({w}x{h}): {filled}px ({pct}%), {colors} colors")

def px(p, x, y, c, S=32):
    if 0 <= x < S and 0 <= y < S:
        if len(c) == 3:
            p[x, y] = c + (255,)
        else:
            p[x, y] = c

# ============================================================
# SWORD BUILDER — builds thick diagonal swords matching old style
# Blade: 5-7px wide band, diagonal top-right to bottom-left
# Guard: 10-12px wide band perpendicular to blade
# Handle: 3px wide with wrap detail
# ============================================================
def build_sword(p, S, blade_colors, guard_colors, handle_colors,
                blade_len=20, guard_row=None, handle_len=6,
                edge_glow=None, blade_detail=None, pommel_color=None,
                tip_glow=None, extra_fn=None):
    """
    blade_colors: dict with 'edge_hi', 'mid_hi', 'mid', 'mid_dk', 'edge_dk', 'outline'
    guard_colors: dict with 'hi', 'mid', 'dk', 'gem' (optional)
    handle_colors: dict with 'base', 'wrap', 'wrap_alt'
    """
    bc = blade_colors
    gc = guard_colors
    hc = handle_colors

    if guard_row is None:
        guard_row = blade_len + 2

    # Blade — runs from top-right toward bottom-left
    for i in range(blade_len):
        t = i / max(1, blade_len - 1)
        # Position: starts at top-right, moves to center-left
        cx = 28 - int(i * (28 - 10) / blade_len)
        cy = 1 + int(i * (guard_row - 1) / blade_len)

        # Taper: wider at base, narrower at tip
        half_w = max(1, int(3.5 - t * 2))

        for dy in range(-half_w, half_w + 1):
            for dx in range(-1, 2):
                xx = cx + dx
                yy = cy + dy

                # Color based on position across blade width
                norm_dy = (dy + half_w) / max(1, 2 * half_w)

                if norm_dy < 0.15:
                    c = bc.get('outline', darken(bc['edge_dk'], 30))
                elif norm_dy < 0.3:
                    c = bc['edge_hi']
                elif norm_dy < 0.5:
                    c = lerp(bc['mid_hi'], bc['mid'], t)
                elif norm_dy < 0.7:
                    c = lerp(bc['mid'], bc['mid_dk'], t)
                elif norm_dy < 0.85:
                    c = bc['edge_dk']
                else:
                    c = bc.get('outline', darken(bc['edge_dk'], 30))

                px(p, xx, yy, c, S)

        # Central highlight line
        px(p, cx, cy, bc['mid_hi'], S)

    # Blade tip glow
    tip_x = 28
    tip_y = 1
    if tip_glow:
        px(p, tip_x + 1, tip_y - 1, tip_glow, S)
        px(p, tip_x + 2, tip_y, tip_glow, S)
        px(p, tip_x, tip_y - 1, darken(tip_glow, 30), S)

    # Cross guard — perpendicular band
    guard_cx = 10
    guard_cy = guard_row
    guard_half = 5  # 10-11px total

    for dx in range(-guard_half, guard_half + 1):
        for dy in range(-1, 2):
            xx = guard_cx + dx
            yy = guard_cy + dy
            t_g = (dx + guard_half) / (2 * guard_half)
            if dy == -1:
                c = gc['hi']
            elif dy == 0:
                c = lerp(gc['hi'], gc['dk'], t_g)
            else:
                c = gc['dk']
            px(p, xx, yy, c, S)

    # Guard gem/center detail
    if 'gem' in gc:
        px(p, guard_cx, guard_cy, gc['gem'], S)
        px(p, guard_cx - 1, guard_cy, lighten(gc['gem'], 20), S)
        px(p, guard_cx + 1, guard_cy, darken(gc['gem'], 20), S)

    # Handle with wrapping
    for i in range(handle_len):
        t = i / max(1, handle_len - 1)
        hx = guard_cx - 2 - int(i * 1.0)
        hy = guard_cy + 2 + i

        for dx in range(-1, 2):
            if i % 2 == 0:
                c = hc['wrap'] if dx == 0 else hc['base']
            else:
                c = hc['wrap_alt'] if dx == 0 else darken(hc['base'], 15)
            px(p, hx + dx, hy, c, S)

    # Pommel
    pm_x = guard_cx - 2 - int(handle_len * 1.0)
    pm_y = guard_cy + 2 + handle_len
    pm_c = pommel_color or gc['hi']
    px(p, pm_x, pm_y, pm_c, S)
    px(p, pm_x - 1, pm_y, lighten(pm_c, 15), S)
    px(p, pm_x + 1, pm_y, darken(pm_c, 15), S)
    px(p, pm_x, pm_y + 1, darken(pm_c, 20), S)
    px(p, pm_x - 1, pm_y + 1, pm_c, S)

    # Edge glow along blade (optional)
    if edge_glow:
        for i in range(0, blade_len, 2):
            t = i / max(1, blade_len - 1)
            cx = 28 - int(i * (28 - 10) / blade_len)
            cy = 1 + int(i * (guard_row - 1) / blade_len)
            half_w = max(1, int(3.5 - t * 2))
            px(p, cx, cy - half_w - 1, edge_glow, S)

    # Blade detail callback
    if blade_detail:
        blade_detail(p, S, blade_len, guard_row)

    # Extra callback
    if extra_fn:
        extra_fn(p, S)


# ============================================================
# STAFF BUILDER — thick shaft with large ornate orb/crystal head
# ============================================================
def build_staff(p, S, shaft_colors, head_colors, head_radius=5,
                head_type='orb', extra_fn=None):
    """
    shaft_colors: dict with 'hi', 'mid', 'dk', 'wrap', 'wrap_alt'
    head_colors: dict with 'core', 'bright', 'mid', 'dk', 'glow'
    """
    sc = shaft_colors
    hc = head_colors

    # Shaft — diagonal, 3px wide, runs from center toward bottom-left
    shaft_start_x, shaft_start_y = 17, 12
    shaft_len = 18

    for i in range(shaft_len):
        t = i / max(1, shaft_len - 1)
        sx = shaft_start_x - int(i * 0.7)
        sy = shaft_start_y + i

        for dx in range(-1, 2):
            if dx == -1:
                c = sc['hi']
            elif dx == 0:
                c = lerp(sc['mid'], sc['dk'], t)
            else:
                c = sc['dk']
            px(p, sx + dx, sy, c, S)

        # Wrap detail
        if i % 3 == 0:
            px(p, sx - 1, sy, sc.get('wrap', lighten(sc['hi'], 10)), S)
            px(p, sx, sy, sc.get('wrap', lighten(sc['mid'], 10)), S)
            px(p, sx + 1, sy, sc.get('wrap_alt', sc['dk']), S)

    # End cap
    ex = shaft_start_x - int(shaft_len * 0.7)
    ey = shaft_start_y + shaft_len
    px(p, ex, ey, sc['dk'], S)
    px(p, ex - 1, ey, darken(sc['dk'], 10), S)

    # Head — large orb/crystal at top
    head_cx = shaft_start_x + 3
    head_cy = shaft_start_y - head_radius + 1

    if head_type == 'orb':
        for dy in range(-head_radius, head_radius + 1):
            for dx in range(-head_radius, head_radius + 1):
                dist = (dx * dx + dy * dy) ** 0.5
                if dist < head_radius + 0.5:
                    t = dist / head_radius
                    # Spherical shading
                    shade = (dx - dy) / (head_radius * 2)  # light from top-left
                    if t < 0.3:
                        c = hc['core']
                    elif t < 0.5:
                        c = hc['bright']
                    elif t < 0.75:
                        c = lerp(hc['mid'], hc['dk'], shade + 0.5)
                    else:
                        c = hc['dk']
                    px(p, head_cx + dx, head_cy + dy, c, S)

        # Core highlight
        px(p, head_cx - 1, head_cy - 1, lighten(hc['core'], 30), S)
        px(p, head_cx, head_cy - 1, hc['core'], S)

    elif head_type == 'crystal':
        # Diamond/crystal shape
        for dy in range(-head_radius, head_radius + 1):
            w = head_radius - abs(dy)
            for dx in range(-w, w + 1):
                t = abs(dy) / head_radius
                if dx < 0:
                    c = hc['bright']
                elif dx > 0:
                    c = hc['dk']
                else:
                    c = lerp(hc['core'], hc['mid'], t)
                px(p, head_cx + dx, head_cy + dy, c, S)

    # Prongs/frame holding the head
    px(p, head_cx - head_radius, head_cy + head_radius, sc['hi'], S)
    px(p, head_cx + head_radius, head_cy + head_radius, sc['dk'], S)
    px(p, head_cx, head_cy + head_radius + 1, sc['mid'], S)

    # Glow around head
    if 'glow' in hc:
        for angle in range(0, 360, 45):
            rad = angle * 3.14159 / 180
            gx = int(head_cx + (head_radius + 1.5) * math.cos(rad))
            gy = int(head_cy + (head_radius + 1.5) * math.sin(rad))
            px(p, gx, gy, hc['glow'], S)

    if extra_fn:
        extra_fn(p, S)


# ============================================================
# SPEAR BUILDER — long shaft with large ornate head
# ============================================================
def build_spear(p, S, shaft_colors, head_colors, head_shape='diamond',
                cross_piece=True, extra_fn=None):
    sc = shaft_colors
    hc = head_colors

    # Shaft — diagonal from center to bottom-left
    shaft_start_x, shaft_start_y = 18, 14
    shaft_len = 18

    for i in range(shaft_len):
        t = i / max(1, shaft_len - 1)
        sx = shaft_start_x - int(i * 0.85)
        sy = shaft_start_y + i

        for dx in range(-1, 2):
            c = sc['hi'] if dx == -1 else sc['dk'] if dx == 1 else lerp(sc['mid'], sc['dk'], t)
            px(p, sx + dx, sy, c, S)

        if i % 3 == 0:
            px(p, sx, sy, sc.get('wrap', lighten(sc['mid'], 15)), S)

    # Spearhead — large ornate shape
    head_cx = shaft_start_x + 4
    head_cy = shaft_start_y - 6
    head_h = 12

    for i in range(head_h):
        t = i / max(1, head_h - 1)
        hy = head_cy + i
        # Diamond shape: narrow at top/bottom, wide in middle
        if t < 0.5:
            half_w = int(t * 2 * 5) + 1
        else:
            half_w = int((1 - t) * 2 * 5) + 1

        hx = head_cx - int(i * 0.3)

        for dx in range(-half_w, half_w + 1):
            norm = (dx + half_w) / max(1, 2 * half_w)
            if norm < 0.2:
                c = hc.get('outline', darken(hc['dk'], 20))
            elif norm < 0.35:
                c = hc['hi']
            elif norm < 0.65:
                c = lerp(hc['mid'], hc['dk'], t)
            elif norm < 0.8:
                c = hc['dk']
            else:
                c = hc.get('outline', darken(hc['dk'], 20))
            px(p, hx + dx, hy, c, S)

        # Central line
        px(p, hx, hy, hc['hi'], S)

    # Tip glow
    if 'glow' in hc:
        px(p, head_cx, head_cy - 1, hc['glow'], S)
        px(p, head_cx + 1, head_cy - 2, hc.get('glow', hc['hi']), S)

    # Cross piece
    if cross_piece:
        cp_y = shaft_start_y
        for dx in range(-4, 5):
            c = sc['hi'] if dx < 0 else sc['dk']
            px(p, shaft_start_x + dx, cp_y, c, S)
            px(p, shaft_start_x + dx, cp_y + 1, darken(c, 15), S)

    # End cap
    ex = shaft_start_x - int(shaft_len * 0.85)
    ey = shaft_start_y + shaft_len
    px(p, ex, ey, sc['dk'], S)
    px(p, ex - 1, ey, darken(sc['dk'], 10), S)

    if extra_fn:
        extra_fn(p, S)


# ============================================================
# CORE RPG WEAPONS (8 items, 32x32)
# ============================================================

def draw_voidreaver():
    S = 32
    img, p = create(S)

    def void_detail(p, S, blade_len, guard_row):
        # Void energy wisps along blade
        for i in range(3, blade_len, 4):
            cx = 28 - int(i * (28 - 10) / blade_len)
            cy = 1 + int(i * (guard_row - 1) / blade_len)
            px(p, cx + 2, cy - 3, (180, 120, 255, 140), S)
            px(p, cx - 2, cy + 3, (140, 80, 220, 100), S)

    def void_extra(p, S):
        # Void particles
        px(p, 30, 0, (160, 100, 240, 120), S)
        px(p, 31, 2, (180, 130, 255, 80), S)
        px(p, 1, 28, (140, 80, 220, 100), S)

    build_sword(p, S,
        blade_colors={
            'edge_hi': (100, 60, 160, 255),
            'mid_hi': (80, 40, 140, 255),
            'mid': (55, 25, 100, 255),
            'mid_dk': (40, 15, 75, 255),
            'edge_dk': (25, 8, 50, 255),
            'outline': (15, 4, 30, 255),
        },
        guard_colors={
            'hi': (130, 100, 170, 255),
            'mid': (90, 65, 130, 255),
            'dk': (55, 35, 85, 255),
            'gem': (200, 140, 255, 255),
        },
        handle_colors={
            'base': (40, 30, 55, 255),
            'wrap': (70, 50, 100, 255),
            'wrap_alt': (50, 35, 75, 255),
        },
        edge_glow=(160, 100, 240, 150),
        tip_glow=(200, 150, 255, 180),
        blade_detail=void_detail,
        extra_fn=void_extra,
    )
    save(img, "voidreaver")

def draw_solaris():
    S = 32
    img, p = create(S)

    def sun_detail(p, S, blade_len, guard_row):
        # Radiant glow along blade center
        for i in range(2, blade_len, 3):
            cx = 28 - int(i * (28 - 10) / blade_len)
            cy = 1 + int(i * (guard_row - 1) / blade_len)
            px(p, cx, cy, (255, 255, 220, 255), S)

    def sun_extra(p, S):
        # Sun rays at tip
        for dx, dy in [(1,-1),(2,0),(1,1),(0,-2),(2,-2)]:
            px(p, 29+dx, 0+dy, (255, 240, 120, 150), S)

    build_sword(p, S,
        blade_colors={
            'edge_hi': (255, 250, 220, 255),
            'mid_hi': (255, 240, 180, 255),
            'mid': (255, 225, 140, 255),
            'mid_dk': (240, 200, 100, 255),
            'edge_dk': (220, 180, 60, 255),
            'outline': (180, 140, 30, 255),
        },
        guard_colors={
            'hi': (255, 220, 100, 255),
            'mid': (220, 185, 50, 255),
            'dk': (180, 145, 20, 255),
            'gem': (255, 255, 200, 255),
        },
        handle_colors={
            'base': (140, 100, 30, 255),
            'wrap': (180, 140, 50, 255),
            'wrap_alt': (120, 85, 25, 255),
        },
        pommel_color=(255, 215, 0, 255),
        tip_glow=(255, 255, 180, 200),
        blade_detail=sun_detail,
        extra_fn=sun_extra,
    )
    save(img, "solaris")

def draw_stormfury():
    S = 32
    img, p = create(S)

    def lightning_detail(p, S, blade_len, guard_row):
        # Lightning bolt zigzag down blade
        for i in range(1, blade_len, 2):
            cx = 28 - int(i * (28 - 10) / blade_len)
            cy = 1 + int(i * (guard_row - 1) / blade_len)
            px(p, cx, cy, (255, 255, 240, 255), S)
            if i + 1 < blade_len:
                px(p, cx - 1, cy + 1, (255, 255, 130, 255), S)

    def storm_extra(p, S):
        # Electric sparks at tip
        px(p, 30, 0, (255, 255, 200, 200), S)
        px(p, 31, 1, (200, 230, 255, 150), S)
        px(p, 29, 0, (180, 220, 255, 120), S)

    build_sword(p, S,
        blade_colors={
            'edge_hi': (210, 220, 245, 255),
            'mid_hi': (180, 195, 230, 255),
            'mid': (155, 170, 210, 255),
            'mid_dk': (130, 145, 185, 255),
            'edge_dk': (100, 115, 160, 255),
            'outline': (70, 80, 120, 255),
        },
        guard_colors={
            'hi': (190, 200, 225, 255),
            'mid': (140, 150, 180, 255),
            'dk': (95, 105, 140, 255),
            'gem': (255, 255, 130, 255),
        },
        handle_colors={
            'base': (55, 60, 80, 255),
            'wrap': (80, 85, 110, 255),
            'wrap_alt': (45, 50, 70, 255),
        },
        edge_glow=(200, 230, 255, 130),
        tip_glow=(255, 255, 200, 200),
        blade_detail=lightning_detail,
        extra_fn=storm_extra,
    )
    save(img, "stormfury")

def draw_briarthorn():
    S = 32
    img, p = create(S)
    # Thorny nature mace — build manually for unique shape

    wood = (95, 70, 35, 255)
    wood_hi = (130, 100, 55, 255)
    wood_dk = (60, 42, 18, 255)
    bark_dk = (45, 30, 12, 255)
    thorn = (150, 120, 55, 255)
    thorn_tip = (190, 160, 80, 255)
    vine = (45, 125, 35, 255)
    vine_hi = (70, 170, 55, 255)
    leaf = (55, 150, 40, 255)

    # Handle — thick diagonal shaft
    for i in range(16):
        t = i / 15.0
        hx = 12 - int(i * 0.6)
        hy = 16 + i
        for dx in range(-1, 2):
            c = wood_hi if dx == -1 else wood_dk if dx == 1 else lerp(wood_hi, wood_dk, t)
            px(p, hx + dx, hy, c, S)
        # Vine wrapping
        if i % 3 == 0:
            px(p, hx - 2, hy, vine, S)
            px(p, hx + 2, hy, vine, S)
            px(p, hx, hy, vine_hi, S)

    # Mace head — large thorny ball
    head_cx, head_cy = 17, 11
    head_r = 6
    for dy in range(-head_r, head_r + 1):
        for dx in range(-head_r, head_r + 1):
            dist = (dx*dx + dy*dy) ** 0.5
            if dist < head_r + 0.5:
                t = dist / head_r
                shade = (dx - dy) / (head_r * 2)
                if t < 0.4:
                    c = wood_hi
                elif t < 0.7:
                    c = lerp(wood, wood_dk, shade + 0.5)
                else:
                    c = wood_dk
                # Bark texture patches
                if (dx + dy) % 4 == 0:
                    c = bark_dk
                px(p, head_cx + dx, head_cy + dy, c, S)

    # Core highlight
    px(p, head_cx - 2, head_cy - 2, lighten(wood_hi, 20), S)

    # Thorns protruding outward (12 thorns)
    for angle in range(0, 360, 30):
        rad = angle * 3.14159 / 180
        # Base of thorn at edge of ball
        bx = int(head_cx + (head_r + 0.5) * math.cos(rad))
        by = int(head_cy + (head_r + 0.5) * math.sin(rad))
        # Tip further out
        tx = int(head_cx + (head_r + 2.5) * math.cos(rad))
        ty = int(head_cy + (head_r + 2.5) * math.sin(rad))
        px(p, bx, by, thorn, S)
        px(p, tx, ty, thorn_tip, S)
        # Middle
        mx = (bx + tx) // 2
        my = (by + ty) // 2
        px(p, mx, my, thorn, S)

    # Leaves near top of handle
    px(p, 10, 18, leaf, S)
    px(p, 9, 17, vine_hi, S)
    px(p, 15, 19, leaf, S)

    # Handle end cap
    px(p, 2, 30, wood_dk, S)
    px(p, 1, 31, bark_dk, S)
    px(p, 3, 31, wood, S)

    save(img, "briarthorn")

def draw_abyssal_trident():
    S = 32
    img, p = create(S)

    deep = (20, 45, 95, 255)
    deep_hi = (35, 70, 135, 255)
    deep_dk = (10, 25, 60, 255)
    aqua = (55, 175, 200, 255)
    aqua_hi = (100, 215, 240, 255)
    coral = (130, 95, 80, 255)
    pearl = (220, 230, 240, 255)
    shaft = (45, 65, 110, 255)
    shaft_hi = (65, 90, 140, 255)
    shaft_dk = (30, 45, 75, 255)
    barnacle = (100, 85, 70, 255)

    # Shaft — thick, diagonal
    for i in range(18):
        t = i / 17.0
        sx = 16 - int(i * 0.5)
        sy = 14 + i
        for dx in range(-1, 2):
            c = shaft_hi if dx == -1 else shaft_dk if dx == 1 else lerp(shaft, shaft_dk, t)
            px(p, sx + dx, sy, c, S)
        if i % 4 == 0:
            px(p, sx, sy, lighten(shaft, 15), S)

    # Three prongs — center tall, sides shorter
    prong_base_y = 14

    # Center prong
    for i in range(14):
        t = i / 13.0
        w = max(1, int(2.5 - t * 1.5))
        py = prong_base_y - i
        pcx = 16
        for dx in range(-w, w + 1):
            c = deep_hi if dx < 0 else deep_dk if dx > 0 else lerp(aqua_hi, deep, t)
            px(p, pcx + dx, py, c, S)
    # Tip
    px(p, 16, 0, aqua_hi, S)
    px(p, 16, -1 + 1, aqua, S)  # y=0 already set

    # Left prong
    for i in range(10):
        t = i / 9.0
        py = prong_base_y - i
        pcx = 11 - int(i * 0.3)
        w = max(1, int(2 - t * 1))
        for dx in range(-w, w + 1):
            c = lerp(deep_hi, deep_dk, t + dx * 0.1)
            px(p, pcx + dx, py, c, S)
    px(p, 8, 4, aqua, S)

    # Right prong
    for i in range(10):
        t = i / 9.0
        py = prong_base_y - i
        pcx = 21 + int(i * 0.3)
        w = max(1, int(2 - t * 1))
        for dx in range(-w, w + 1):
            c = lerp(deep_hi, deep_dk, t + dx * 0.1)
            px(p, pcx + dx, py, c, S)
    px(p, 24, 4, aqua, S)

    # Barbs on prongs
    px(p, 8, 6, deep_hi, S)
    px(p, 7, 7, aqua, S)
    px(p, 25, 6, deep_hi, S)
    px(p, 26, 7, aqua, S)
    px(p, 14, 2, deep_hi, S)
    px(p, 18, 2, deep_hi, S)

    # Cross piece connecting prongs
    for x in range(10, 23):
        px(p, x, prong_base_y, deep_hi, S)
        px(p, x, prong_base_y + 1, deep, S)

    # Coral growths
    px(p, 13, 16, coral, S)
    px(p, 12, 15, coral, S)
    px(p, 19, 16, coral, S)
    px(p, 14, 17, barnacle, S)

    # Pearl at center crossing
    px(p, 16, prong_base_y, pearl, S)
    px(p, 15, prong_base_y, lighten(pearl, 10), S)

    # End cap
    px(p, 7, 31, shaft_dk, S)
    px(p, 6, 31, darken(shaft_dk, 10), S)

    save(img, "abyssal_trident")

def draw_pyroclast():
    S = 32
    img, p = create(S)

    obsidian = (30, 25, 35, 255)
    obsidian_hi = (55, 48, 62, 255)
    obsidian_dk = (15, 12, 18, 255)
    magma = (255, 100, 0, 255)
    magma_hi = (255, 180, 40, 255)
    magma_dk = (200, 50, 0, 255)
    ember = (255, 210, 70, 200)
    handle = (85, 58, 35, 255)
    handle_hi = (115, 82, 48, 255)
    handle_dk = (55, 35, 18, 255)
    iron = (100, 90, 80, 255)

    # Handle — thick diagonal
    for i in range(14):
        t = i / 13.0
        hx = 12 - int(i * 0.5)
        hy = 18 + i
        for dx in range(-1, 2):
            c = handle_hi if dx == -1 else handle_dk if dx == 1 else lerp(handle, handle_dk, t)
            px(p, hx + dx, hy, c, S)
        if i % 2 == 0:
            px(p, hx, hy, lighten(handle, 15), S)

    # Axe head — large obsidian blade with magma cracks
    # Right blade
    for dy in range(-8, 9):
        blade_w = max(0, int(8 - abs(dy) * 0.8))
        for dx in range(0, blade_w):
            y = 12 + dy
            x = 15 + dx
            t = dx / max(1, blade_w)
            # Gradient from thick to edge
            if t < 0.3:
                c = obsidian_hi
            elif t < 0.7:
                c = obsidian
            else:
                c = obsidian_dk
            px(p, x, y, c, S)

    # Left blade (smaller, back side)
    for dy in range(-5, 6):
        blade_w = max(0, int(4 - abs(dy) * 0.6))
        for dx in range(0, blade_w):
            y = 12 + dy
            x = 12 - dx
            c = obsidian_hi if dx == 0 else obsidian
            px(p, x, y, c, S)

    # Magma cracks through obsidian
    crack_positions = [(16, 8), (18, 10), (20, 12), (19, 14), (17, 16),
                       (15, 10), (17, 12), (21, 11), (18, 15), (16, 13)]
    for cx, cy in crack_positions:
        px(p, cx, cy, magma, S)

    # Magma glow on edges
    for dy in range(-7, 8):
        blade_w = max(0, int(8 - abs(dy) * 0.8))
        if blade_w > 0:
            x = 15 + blade_w - 1
            y = 12 + dy
            px(p, x, y, magma_dk, S)
            px(p, x + 1, y, magma, S)

    # Bright magma highlights
    px(p, 17, 9, magma_hi, S)
    px(p, 19, 13, magma_hi, S)
    px(p, 16, 15, magma_hi, S)

    # Ember particles
    px(p, 24, 7, ember, S)
    px(p, 25, 10, (ember[0], ember[1], ember[2], 120), S)
    px(p, 23, 15, ember, S)
    px(p, 9, 9, (ember[0], ember[1], ember[2], 80), S)

    # Iron band at head/handle junction
    for dy in range(-1, 2):
        px(p, 13, 17 + dy, iron, S)
        px(p, 14, 17 + dy, iron, S)

    # Handle end
    px(p, 5, 31, handle_dk, S)
    px(p, 4, 31, darken(handle_dk, 10), S)

    save(img, "pyroclast")

def draw_whisperwind():
    S = 32
    img, p = create(S)

    build_staff(p, S,
        shaft_colors={
            'hi': (200, 210, 225, 255),
            'mid': (170, 180, 200, 255),
            'dk': (130, 140, 160, 255),
            'wrap': (210, 220, 235, 255),
            'wrap_alt': (150, 160, 180, 255),
        },
        head_colors={
            'core': (240, 250, 255, 255),
            'bright': (200, 225, 250, 255),
            'mid': (160, 195, 235, 255),
            'dk': (120, 160, 210, 255),
            'glow': (180, 215, 245, 140),
        },
        head_radius=5,
        head_type='crystal',
    )

    # Add wind swirl lines
    swirls = [(10, 4), (8, 6), (7, 3), (25, 5), (27, 7), (26, 9), (9, 8), (24, 3)]
    for sx, sy in swirls:
        px(p, sx, sy, (180, 210, 240, 130), S)

    save(img, "whisperwind")

def draw_soulchain():
    S = 32
    img, p = create(S)

    iron = (80, 75, 90, 255)
    iron_hi = (115, 108, 125, 255)
    iron_dk = (50, 45, 58, 255)
    chain_hi = (105, 98, 115, 255)
    chain_dk = (60, 55, 68, 255)
    soul = (130, 170, 220, 255)
    soul_hi = (180, 210, 245, 255)
    soul_core = (220, 240, 255, 255)
    ghost = (120, 155, 205, 140)
    handle = (60, 48, 42, 255)
    handle_hi = (88, 72, 62, 255)
    handle_dk = (38, 28, 22, 255)

    # Handle
    for i in range(8):
        t = i / 7.0
        hx = 4 + int(i * 0.3)
        hy = 24 + i
        for dx in range(-1, 2):
            c = handle_hi if dx == -1 else handle_dk if dx == 1 else lerp(handle, handle_dk, t)
            px(p, hx + dx, hy, c, S)
        if i % 2 == 0:
            px(p, hx, hy, lighten(handle, 12), S)

    # Chain links — diagonal path from handle to orb
    chain_path = [(7, 22), (9, 20), (11, 18), (13, 16), (15, 14), (17, 12), (19, 10)]
    for i, (cx, cy) in enumerate(chain_path):
        # Each link: 3x2 oval
        px(p, cx-1, cy, chain_hi, S)
        px(p, cx, cy, iron, S)
        px(p, cx+1, cy, chain_dk, S)
        px(p, cx-1, cy+1, iron, S)
        px(p, cx, cy+1, chain_dk, S)
        px(p, cx+1, cy+1, iron_dk, S)

    # Soul orb — large, glowing
    orb_cx, orb_cy = 23, 6
    orb_r = 5
    for dy in range(-orb_r, orb_r + 1):
        for dx in range(-orb_r, orb_r + 1):
            dist = (dx*dx + dy*dy) ** 0.5
            if dist < orb_r + 0.5:
                t = dist / orb_r
                shade = (dx - dy) / (orb_r * 2)
                if t < 0.25:
                    c = soul_core
                elif t < 0.5:
                    c = soul_hi
                elif t < 0.75:
                    c = lerp(soul, iron, shade + 0.3)
                else:
                    c = lerp(soul, iron_dk, 0.5)
                px(p, orb_cx + dx, orb_cy + dy, c, S)

    # Ghost face in orb
    px(p, 21, 5, (40, 35, 55, 200), S)
    px(p, 25, 5, (40, 35, 55, 200), S)
    px(p, 22, 7, (40, 35, 55, 180), S)
    px(p, 23, 8, (40, 35, 55, 180), S)
    px(p, 24, 7, (40, 35, 55, 180), S)

    # Soul wisps
    for sx, sy, sa in [(18, 3, 120), (28, 4, 100), (20, 11, 110), (27, 9, 90), (17, 7, 70)]:
        px(p, sx, sy, (ghost[0], ghost[1], ghost[2], sa), S)

    # Pommel
    px(p, 3, 31, iron_hi, S)
    px(p, 2, 31, iron, S)
    px(p, 4, 31, iron_dk, S)

    save(img, "soulchain")


# ============================================================
# ARSENAL WEAPONS
# ============================================================

# --- Whips (16x16) ---
def draw_unique_whip_1():  # Serpent's Lash
    S = 16
    img, p = create(S)
    green = (40, 130, 50, 255)
    green_hi = (65, 175, 75, 255)
    green_dk = (22, 80, 28, 255)
    scale = (50, 155, 62, 255)
    belly = (85, 170, 80, 255)
    eye = (255, 200, 0, 255)
    fang = (240, 235, 220, 255)
    handle = (95, 65, 35, 255)
    handle_hi = (125, 90, 50, 255)
    handle_dk = (65, 42, 20, 255)

    # Handle (thick, bottom-left)
    for y in range(10, 15):
        for dx in range(-1, 2):
            c = handle_hi if dx == -1 else handle_dk if dx == 1 else handle
            px(p, 2 + dx, y, c, S)
    px(p, 2, 15, darken(handle_dk, 10), S)

    # Whip body — sinuous thick curve
    path = [(3,9),(4,8),(5,7),(6,6),(7,6),(8,5),(9,5),(10,4),(11,4),(12,3)]
    for i, (x, y) in enumerate(path):
        t = i / 9.0
        c = lerp(green_hi, green_dk, t)
        px(p, x, y, c, S)
        px(p, x, y+1, lerp(belly, green_dk, t), S)  # belly side
        if i % 2 == 0:
            px(p, x, y, scale, S)  # scale highlights

    # Snake head
    px(p, 13, 2, green_hi, S)
    px(p, 14, 2, green, S)
    px(p, 13, 3, green, S)
    px(p, 14, 3, green_dk, S)
    px(p, 15, 1, green, S)
    px(p, 14, 1, green_hi, S)
    px(p, 15, 2, eye, S)  # eye
    px(p, 14, 4, fang, S)  # fang
    px(p, 15, 0, (200, 40, 30, 220), S)  # tongue
    px(p, 14, 0, (200, 40, 30, 180), S)

    save(img, "unique_whip_1")

def draw_unique_whip_2():  # Flamelash
    S = 16
    img, p = create(S)
    red = (200, 40, 20, 255)
    red_hi = (240, 85, 35, 255)
    orange = (255, 145, 30, 255)
    yellow = (255, 215, 65, 255)
    handle = (65, 42, 30, 255)
    handle_hi = (95, 68, 45, 255)
    ember = (255, 185, 45, 160)

    for y in range(10, 15):
        for dx in range(-1, 2):
            c = handle_hi if dx == -1 else darken(handle, 15) if dx == 1 else handle
            px(p, 2 + dx, y, c, S)

    path = [(3,9),(4,8),(5,7),(6,7),(7,6),(8,5),(9,5),(10,4),(11,3),(12,3)]
    for i, (x, y) in enumerate(path):
        t = i / 9.0
        c = lerp(red, yellow, t)
        px(p, x, y, c, S)
        px(p, x, y+1, lerp(red_hi, orange, t), S)
        if i % 2 == 0:
            px(p, x, y-1, orange, S)

    # Fire tip
    px(p, 13, 2, yellow, S)
    px(p, 14, 1, (255, 255, 190, 255), S)
    px(p, 13, 1, orange, S)
    px(p, 14, 2, orange, S)
    px(p, 15, 1, ember, S)
    px(p, 12, 1, ember, S)
    px(p, 5, 6, ember, S)
    px(p, 8, 4, ember, S)

    save(img, "unique_whip_2")

def draw_unique_whip_sw():  # Thornwhip of the Verdant Warden
    S = 16
    img, p = create(S)
    brown = (105, 72, 35, 255)
    brown_hi = (135, 98, 52, 255)
    brown_dk = (72, 48, 20, 255)
    thorn = (155, 125, 62, 255)
    vine = (48, 132, 38, 255)
    leaf = (62, 165, 48, 255)
    handle = (82, 58, 30, 255)

    for y in range(10, 15):
        for dx in range(-1, 2):
            c = lighten(handle, 15) if dx == -1 else darken(handle, 15) if dx == 1 else handle
            px(p, 2 + dx, y, c, S)

    path = [(3,9),(4,8),(5,8),(6,7),(7,6),(8,6),(9,5),(10,5),(11,4),(12,3)]
    for i, (x, y) in enumerate(path):
        t = i / 9.0
        px(p, x, y, lerp(brown_hi, brown_dk, t), S)
        px(p, x, y+1, brown_dk, S)

    # Thorns along whip
    for i in range(0, 10, 2):
        x, y = path[i]
        px(p, x, y-1, thorn, S)

    # Leafy tip
    px(p, 13, 2, leaf, S)
    px(p, 14, 2, vine, S)
    px(p, 13, 3, vine, S)
    px(p, 12, 2, leaf, S)

    # Vine on handle
    px(p, 1, 12, vine, S)
    px(p, 3, 11, vine, S)

    save(img, "unique_whip_sw")

# --- Wands (16x16) ---
def draw_unique_wand_1():  # Arcane Focus
    S = 16
    img, p = create(S)
    wood = (92, 62, 45, 255)
    wood_hi = (125, 88, 62, 255)
    wood_dk = (62, 38, 25, 255)
    crystal = (162, 82, 225, 255)
    crystal_hi = (205, 145, 255, 255)
    crystal_dk = (105, 42, 155, 255)
    glow = (182, 125, 255, 140)
    gold = (205, 175, 52, 255)
    gold_dk = (160, 128, 28, 255)

    # Shaft diagonal — 2px wide
    for i in range(9):
        x = 3 + int(i * 0.7)
        y = 14 - i
        px(p, x, y, wood, S)
        px(p, x+1, y, wood_hi, S)
        if i % 3 == 0:
            px(p, x, y, lighten(wood, 15), S)

    # Gold band
    px(p, 7, 8, gold, S)
    px(p, 8, 7, gold, S)
    px(p, 7, 7, gold_dk, S)

    # Crystal tip — larger, faceted
    px(p, 9, 5, crystal_hi, S)
    px(p, 10, 4, crystal, S)
    px(p, 10, 3, crystal_hi, S)
    px(p, 11, 3, crystal, S)
    px(p, 11, 2, crystal_dk, S)
    px(p, 10, 2, crystal_hi, S)
    px(p, 12, 2, crystal, S)
    px(p, 11, 1, crystal_hi, S)
    px(p, 9, 4, crystal_dk, S)

    # Glow aura
    px(p, 9, 2, glow, S)
    px(p, 12, 1, glow, S)
    px(p, 13, 2, glow, S)
    px(p, 8, 4, glow, S)

    # Pommel
    px(p, 2, 15, wood_dk, S)
    px(p, 3, 15, wood, S)

    save(img, "unique_wand_1")

def draw_unique_wand_2():  # Frostfinger
    S = 16
    img, p = create(S)
    wood = (102, 92, 112, 255)
    wood_hi = (132, 122, 142, 255)
    ice = (142, 202, 245, 255)
    ice_hi = (202, 232, 255, 255)
    ice_dk = (82, 142, 202, 255)
    frost = (222, 242, 255, 180)
    silver = (185, 188, 198, 255)

    for i in range(9):
        x = 3 + int(i * 0.7)
        y = 14 - i
        px(p, x, y, wood, S)
        px(p, x+1, y, wood_hi, S)
        if i % 3 == 0: px(p, x, y, lighten(wood, 12), S)

    px(p, 7, 8, silver, S)
    px(p, 8, 7, silver, S)

    px(p, 9, 5, ice, S)
    px(p, 10, 4, ice_hi, S)
    px(p, 10, 3, ice, S)
    px(p, 11, 3, ice_dk, S)
    px(p, 11, 2, ice_hi, S)
    px(p, 10, 2, (255, 255, 255, 255), S)
    px(p, 12, 2, ice, S)
    px(p, 11, 1, ice_hi, S)

    px(p, 9, 2, frost, S)
    px(p, 12, 1, frost, S)
    px(p, 8, 4, frost, S)
    px(p, 13, 3, frost, S)

    px(p, 2, 15, darken(wood, 15), S)
    save(img, "unique_wand_2")

def draw_unique_wand_sw():  # Star Conduit
    S = 16
    img, p = create(S)
    wood = (122, 102, 62, 255)
    wood_hi = (152, 132, 82, 255)
    gold = (205, 175, 52, 255)
    gold_hi = (255, 225, 102, 255)
    star = (255, 255, 205, 255)
    star_glow = (255, 242, 165, 180)

    for i in range(9):
        x = 3 + int(i * 0.7)
        y = 14 - i
        px(p, x, y, wood, S)
        px(p, x+1, y, wood_hi, S)
        if i % 3 == 0: px(p, x, y, lighten(wood, 12), S)

    px(p, 7, 8, gold, S)
    px(p, 8, 7, gold, S)

    # Star shape at tip
    px(p, 11, 1, star, S)  # center
    px(p, 11, 0, gold_hi, S)  # top
    px(p, 11, 2, gold_hi, S)  # bottom
    px(p, 10, 1, star, S)  # left
    px(p, 12, 1, star, S)  # right
    px(p, 10, 0, gold, S)
    px(p, 12, 0, gold, S)
    px(p, 10, 2, gold, S)
    px(p, 12, 2, gold, S)
    px(p, 9, 1, star_glow, S)
    px(p, 13, 1, star_glow, S)
    px(p, 11, 3, gold, S)

    px(p, 2, 15, darken(wood, 15), S)
    save(img, "unique_wand_sw")

# --- Katanas (32x32) ---
def draw_katana(name, blade_c, guard_c, handle_c, accent_fn=None):
    S = 32
    img, p = create(S)

    # Long curved blade — katana style, thinner than claymore but still substantial
    blade_len = 24
    for i in range(blade_len):
        t = i / (blade_len - 1)
        cx = 28 - int(i * 0.85)
        cy = 2 + int(i * 0.95) + (1 if 6 < i < 18 else 0)  # gentle curve

        half_w = max(1, int(2.5 - t * 1))

        for dy in range(-half_w, half_w + 1):
            norm = (dy + half_w) / max(1, 2 * half_w)
            if norm < 0.2:
                c = blade_c['outline']
            elif norm < 0.4:
                c = blade_c['hi']
            elif norm < 0.6:
                c = lerp(blade_c['mid'], blade_c['dk'], t * 0.5)
            elif norm < 0.8:
                c = blade_c['dk']
            else:
                c = blade_c['outline']
            px(p, cx, cy + dy, c, S)

    # Hamon line (temper line on edge)
    for i in range(2, blade_len - 2):
        t = i / (blade_len - 1)
        cx = 28 - int(i * 0.85)
        cy = 2 + int(i * 0.95) + (1 if 6 < i < 18 else 0)
        half_w = max(1, int(2.5 - t * 1))
        px(p, cx, cy - half_w + 1, blade_c.get('hamon', lighten(blade_c['hi'], 20)), S)

    # Tsuba (round guard)
    gx, gy = 8, 25
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            dist = abs(dx) + abs(dy)
            if dist <= 2:
                c = guard_c['hi'] if dist == 0 else guard_c['mid'] if dist == 1 else guard_c['dk']
                px(p, gx + dx, gy + dy, c, S)

    # Handle with wrapping
    for i in range(5):
        hx = 6 - int(i * 0.4)
        hy = 27 + i
        for dx in range(-1, 2):
            if i % 2 == 0:
                c = handle_c['wrap']
            else:
                c = handle_c['base']
            if dx == -1: c = lighten(c, 15)
            elif dx == 1: c = darken(c, 15)
            px(p, hx + dx, hy, c, S)

    # Pommel cap
    px(p, 4, 31, guard_c['mid'], S)
    px(p, 3, 31, guard_c['hi'], S)
    px(p, 5, 31, guard_c['dk'], S)

    if accent_fn:
        accent_fn(p, S)

    save(img, name)

def draw_unique_katana_1():
    def wind_accent(p, S):
        for sx, sy, sa in [(29, 1, 130), (30, 3, 100), (28, 0, 80)]:
            px(p, sx, sy, (180, 210, 240, sa), S)
    draw_katana("unique_katana_1",
        blade_c={'hi': (225, 230, 245, 255), 'mid': (195, 200, 218, 255), 'dk': (155, 160, 178, 255), 'outline': (105, 110, 130, 255), 'hamon': (245, 250, 255, 255)},
        guard_c={'hi': (145, 140, 155, 255), 'mid': (110, 105, 120, 255), 'dk': (75, 70, 85, 255)},
        handle_c={'base': (35, 30, 48, 255), 'wrap': (225, 218, 202, 255)},
        accent_fn=wind_accent)

def draw_unique_katana_2():
    def moon_accent(p, S):
        px(p, 18, 12, (202, 212, 242, 255), S)
        px(p, 19, 11, (202, 212, 242, 180), S)
    draw_katana("unique_katana_2",
        blade_c={'hi': (122, 92, 162, 255), 'mid': (82, 58, 125, 255), 'dk': (52, 32, 82, 255), 'outline': (28, 18, 48, 255)},
        guard_c={'hi': (92, 78, 112, 255), 'mid': (62, 48, 82, 255), 'dk': (38, 28, 55, 255)},
        handle_c={'base': (28, 18, 38, 255), 'wrap': (102, 82, 132, 255)},
        accent_fn=moon_accent)

def draw_unique_katana_sw():
    def ember_accent(p, S):
        for i in range(4, 20, 5):
            cx = 28 - int(i * 0.85)
            cy = 2 + int(i * 0.95) + (1 if 6 < i < 18 else 0)
            px(p, cx, cy, (255, 125, 42, 255), S)
    draw_katana("unique_katana_sw",
        blade_c={'hi': (182, 122, 108, 255), 'mid': (142, 88, 72, 255), 'dk': (95, 55, 42, 255), 'outline': (55, 30, 22, 255)},
        guard_c={'hi': (102, 75, 62, 255), 'mid': (72, 52, 42, 255), 'dk': (45, 30, 22, 255)},
        handle_c={'base': (42, 28, 22, 255), 'wrap': (122, 82, 62, 255)},
        accent_fn=ember_accent)

# --- Greatshields (32x32) — matching old shield style, dense fill ---
def draw_greatshield(name, face_colors, trim_color, emblem_fn=None):
    S = 32
    img, p = create(S)
    fc = face_colors

    # Shield body — large kite/heater shape filling most of canvas
    for y in range(2, 30):
        t = (y - 2) / 27.0
        if t < 0.7:
            half_w = int(12 - t * 3)
        else:
            half_w = int(12 - (t - 0.3) * 12)
        half_w = max(1, half_w)

        for x in range(16 - half_w, 16 + half_w):
            t_x = (x - (16 - half_w)) / max(1, 2 * half_w - 1)
            t_y = t

            # Multi-tone gradient
            if t_x < 0.15:
                c = fc['edge']
            elif t_x < 0.3:
                c = fc['hi']
            elif t_x < 0.7:
                c = lerp(fc['mid'], fc['dk'], t_y)
            elif t_x < 0.85:
                c = fc['dk']
            else:
                c = darken(fc['edge'], 20)
            px(p, x, y, c, S)

    # Trim border
    for y in range(2, 30):
        t = (y - 2) / 27.0
        if t < 0.7:
            half_w = int(12 - t * 3)
        else:
            half_w = int(12 - (t - 0.3) * 12)
        half_w = max(1, half_w)
        px(p, 16 - half_w, y, trim_color, S)
        px(p, 16 + half_w - 1, y, darken(trim_color, 20), S)

    # Top/bottom trim
    for x in range(5, 27):
        px(p, x, 2, lighten(trim_color, 15), S)

    # Rivets
    for ry in [6, 14, 22]:
        for rx_off in [-8, 0, 8]:
            rx = 16 + rx_off
            if 4 <= rx <= 27:
                px(p, rx, ry, darken(trim_color, 30), S)

    if emblem_fn:
        emblem_fn(p, S)

    save(img, name)

def draw_unique_greatshield_1():  # Ironwall
    def rivets(p, S):
        # Extra horizontal bands
        for x in range(6, 26):
            px(p, x, 10, (125, 125, 135, 255), S)
            px(p, x, 20, (125, 125, 135, 255), S)
        # Central boss dome
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                if abs(dx)+abs(dy) <= 2:
                    px(p, 16+dx, 15+dy, (205, 205, 215, 255), S)

    draw_greatshield("unique_greatshield_1",
        face_colors={'hi': (195, 195, 205, 255), 'mid': (155, 155, 165, 255), 'dk': (115, 115, 125, 255), 'edge': (85, 85, 95, 255)},
        trim_color=(135, 135, 145, 255),
        emblem_fn=rivets)

def draw_unique_greatshield_2():  # Aegis of Dawn
    def sun_emblem(p, S):
        # Sun disc center
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                dist = (dx*dx+dy*dy)**0.5
                if dist < 3.5:
                    px(p, 16+dx, 14+dy, (255, 242, 155, 255), S)
        px(p, 16, 14, (255, 255, 215, 255), S)
        # Rays
        for rx, ry in [(16,8),(16,20),(10,14),(22,14),(12,10),(20,10),(12,18),(20,18)]:
            px(p, rx, ry, (255, 235, 125, 200), S)

    draw_greatshield("unique_greatshield_2",
        face_colors={'hi': (255, 232, 105, 255), 'mid': (225, 195, 55, 255), 'dk': (185, 152, 22, 255), 'edge': (145, 115, 8, 255)},
        trim_color=(255, 218, 82, 255),
        emblem_fn=sun_emblem)

def draw_unique_greatshield_sw():  # Titan's Bulwark
    def cracks(p, S):
        for cx, cy in [(12,12),(14,14),(18,16),(20,18),(10,20),(22,10)]:
            px(p, cx, cy, (92, 88, 78, 255), S)
        # Stone boss
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                if abs(dx)+abs(dy) <= 2:
                    px(p, 16+dx, 15+dy, (148, 142, 132, 255), S)

    draw_greatshield("unique_greatshield_sw",
        face_colors={'hi': (205, 165, 88, 255), 'mid': (168, 128, 55, 255), 'dk': (128, 92, 32, 255), 'edge': (92, 62, 18, 255)},
        trim_color=(175, 138, 62, 255),
        emblem_fn=cracks)

# --- Throwing Axes (16x16) ---
def draw_throwing_axe(name, blade_c, handle_c, accent_fn=None):
    S = 16
    img, p = create(S)

    # Handle
    for y in range(8, 15):
        px(p, 5, y, handle_c[0], S)
        px(p, 6, y, handle_c[1], S)
        px(p, 7, y, handle_c[2], S)

    # Axe head — substantial, fills upper right
    for dy in range(-4, 5):
        w = max(0, int(5 - abs(dy) * 0.9))
        for dx in range(0, w):
            y = 6 + dy
            x = 8 + dx
            t = dx / max(1, w)
            if t < 0.3: c = blade_c[0]
            elif t < 0.7: c = blade_c[1]
            else: c = blade_c[2]
            px(p, x, y, c, S)

    # Edge highlight
    for dy in range(-3, 4):
        w = max(0, int(5 - abs(dy) * 0.9))
        if w > 0:
            px(p, 8 + w - 1, 6 + dy, lighten(blade_c[0], 20), S)

    if accent_fn:
        accent_fn(p, S)

    save(img, name)

def draw_unique_throwing_axe_1():
    def bolts(p, S):
        px(p, 10, 5, (255, 255, 135, 255), S)
        px(p, 11, 6, (255, 255, 240, 255), S)
        px(p, 10, 7, (255, 255, 135, 255), S)
    draw_throwing_axe("unique_throwing_axe_1",
        blade_c=[(205, 215, 245, 255), (168, 178, 208, 255), (128, 138, 168, 255)],
        handle_c=[(85, 75, 62, 255), (115, 102, 85, 255), (68, 58, 45, 255)],
        accent_fn=bolts)

def draw_unique_throwing_axe_2():
    def frost(p, S):
        px(p, 10, 4, (225, 242, 255, 185), S)
        px(p, 12, 6, (225, 242, 255, 185), S)
        px(p, 11, 8, (225, 242, 255, 185), S)
    draw_throwing_axe("unique_throwing_axe_2",
        blade_c=[(195, 228, 255, 255), (138, 195, 245, 255), (85, 145, 205, 255)],
        handle_c=[(85, 88, 102, 255), (115, 118, 132, 255), (62, 65, 78, 255)],
        accent_fn=frost)

def draw_unique_throwing_axe_sw():
    def wind(p, S):
        px(p, 13, 4, (185, 215, 242, 145), S)
        px(p, 14, 3, (185, 215, 242, 95), S)
    draw_throwing_axe("unique_throwing_axe_sw",
        blade_c=[(175, 178, 185, 255), (142, 145, 152, 255), (108, 112, 118, 255)],
        handle_c=[(105, 82, 58, 255), (135, 112, 82, 255), (78, 58, 38, 255)],
        accent_fn=wind)

# --- Rapiers (32x32) ---
def draw_rapier(name, blade_c, guard_c, handle_c, accent_fn=None):
    S = 32
    img, p = create(S)

    # Long thin blade — 26px long, 3-4px wide
    for i in range(26):
        t = i / 25.0
        bx = 7 + i
        by = 28 - int(i * 0.92)

        half_w = 2 if i < 20 else 1
        for dy in range(-half_w, half_w + 1):
            norm = (dy + half_w) / max(1, 2 * half_w)
            if norm < 0.25: c = blade_c['outline']
            elif norm < 0.5: c = blade_c['hi']
            elif norm < 0.75: c = lerp(blade_c['mid'], blade_c['dk'], t)
            else: c = blade_c['outline']
            px(p, bx, by + dy, c, S)
        px(p, bx, by, blade_c['hi'], S)

    # Ornate swept guard
    gx, gy = 7, 28
    # Main guard bar
    for dx in range(-5, 6):
        for dy in range(-1, 2):
            c = guard_c['hi'] if dy == -1 else guard_c['dk'] if dy == 1 else guard_c['mid']
            px(p, gx + dx, gy + dy, c, S)

    # Shell/cup guard
    for dy in range(0, 4):
        for dx in range(-3, 1):
            px(p, gx + dx, gy + dy, guard_c['mid'], S)
    px(p, gx - 3, gy, guard_c['hi'], S)
    px(p, gx, gy + 3, guard_c['dk'], S)

    # Handle
    for i in range(3):
        hx = 4 - int(i * 0.3)
        hy = 30 + i
        px(p, hx - 1, hy, lighten(handle_c, 15), S)
        px(p, hx, hy, handle_c, S)
        px(p, hx + 1, hy, darken(handle_c, 15), S)

    # Pommel
    px(p, 3, 31, guard_c['hi'], S)
    px(p, 2, 31, lighten(guard_c['hi'], 10), S)

    if accent_fn:
        accent_fn(p, S)

    save(img, name)

def draw_unique_rapier_1():
    draw_rapier("unique_rapier_1",
        blade_c={'hi': (235, 238, 248, 255), 'mid': (202, 205, 218, 255), 'dk': (165, 168, 178, 255), 'outline': (118, 122, 135, 255)},
        guard_c={'hi': (235, 215, 112, 255), 'mid': (205, 185, 62, 255), 'dk': (162, 142, 28, 255)},
        handle_c=(75, 42, 32, 255))

def draw_unique_rapier_2():
    def poison(p, S):
        for i in range(4, 22, 5):
            bx = 7 + i
            by = 28 - int(i * 0.92)
            px(p, bx, by + 3, (62, 185, 42, 185), S)
            px(p, bx, by + 4, (62, 185, 42, 105), S)
    draw_rapier("unique_rapier_2",
        blade_c={'hi': (175, 215, 165, 255), 'mid': (138, 178, 128, 255), 'dk': (102, 142, 92, 255), 'outline': (62, 95, 55, 255)},
        guard_c={'hi': (135, 125, 108, 255), 'mid': (102, 92, 78, 255), 'dk': (72, 62, 48, 255)},
        handle_c=(52, 42, 32, 255),
        accent_fn=poison)

def draw_unique_rapier_sw():
    def gem(p, S):
        px(p, 6, 29, (102, 155, 255, 255), S)
    draw_rapier("unique_rapier_sw",
        blade_c={'hi': (248, 250, 255, 255), 'mid': (222, 225, 238, 255), 'dk': (188, 192, 205, 255), 'outline': (142, 148, 165, 255)},
        guard_c={'hi': (242, 218, 92, 255), 'mid': (212, 188, 55, 255), 'dk': (172, 148, 28, 255)},
        handle_c=(82, 52, 42, 255),
        accent_fn=gem)

# --- Longswords (32x32) ---
def draw_unique_longsword_1():  # Dragonscale-Encrusted Longblade
    S = 32
    img, p = create(S)
    def dragon_detail(p, S, bl, gr):
        for i in range(2, 18, 3):
            cx = 28 - int(i * (28 - 10) / bl)
            cy = 1 + int(i * (gr - 1) / bl)
            px(p, cx + 1, cy + 1, (185, 105, 42, 255), S)
            px(p, cx - 1, cy - 1, (225, 145, 62, 255), S)
    def fire_tip(p, S):
        px(p, 30, 0, (255, 142, 32, 180), S)
        px(p, 29, 0, (255, 185, 55, 120), S)
    build_sword(p, S,
        blade_colors={'edge_hi': (248, 178, 82, 255), 'mid_hi': (225, 148, 55, 255), 'mid': (202, 122, 42, 255), 'mid_dk': (172, 95, 28, 255), 'edge_dk': (142, 72, 18, 255), 'outline': (95, 45, 8, 255)},
        guard_colors={'hi': (185, 118, 35, 255), 'mid': (148, 88, 22, 255), 'dk': (108, 62, 12, 255), 'gem': (255, 85, 25, 255)},
        handle_colors={'base': (82, 52, 25, 255), 'wrap': (115, 78, 42, 255), 'wrap_alt': (62, 38, 18, 255)},
        blade_detail=dragon_detail, extra_fn=fire_tip)
    save(img, "unique_longsword_1")

def draw_unique_longsword_2():  # Hearthguard Blade
    S = 32
    img, p = create(S)
    def warm_glow(p, S, bl, gr):
        for i in range(0, 20, 3):
            cx = 28 - int(i * (28 - 10) / bl)
            cy = 1 + int(i * (gr - 1) / bl)
            px(p, cx, cy, (205, 148, 65, 255), S)
    build_sword(p, S,
        blade_colors={'edge_hi': (228, 212, 178, 255), 'mid_hi': (202, 182, 148, 255), 'mid': (178, 158, 122, 255), 'mid_dk': (152, 132, 98, 255), 'edge_dk': (128, 108, 78, 255), 'outline': (88, 72, 48, 255)},
        guard_colors={'hi': (175, 142, 78, 255), 'mid': (142, 112, 52, 255), 'dk': (108, 82, 32, 255), 'gem': (255, 195, 82, 255)},
        handle_colors={'base': (92, 62, 35, 255), 'wrap': (125, 88, 52, 255), 'wrap_alt': (72, 48, 25, 255)},
        blade_detail=warm_glow)
    save(img, "unique_longsword_2")

# --- Fill-in variants ---
def draw_unique_claymore_3():  # Northwind Greatsword
    S = 32
    img, p = create(S)
    def frost_detail(p, S, bl, gr):
        for i in range(2, 20, 4):
            cx = 28 - int(i * (28 - 10) / bl)
            cy = 1 + int(i * (gr - 1) / bl)
            px(p, cx, cy - 3, (228, 245, 255, 185), S)
            px(p, cx + 2, cy - 2, (215, 235, 255, 140), S)
    build_sword(p, S,
        blade_colors={'edge_hi': (212, 235, 255, 255), 'mid_hi': (178, 212, 248, 255), 'mid': (148, 188, 235, 255), 'mid_dk': (118, 162, 218, 255), 'edge_dk': (85, 132, 195, 255), 'outline': (55, 95, 155, 255)},
        guard_colors={'hi': (165, 195, 228, 255), 'mid': (128, 158, 192, 255), 'dk': (88, 115, 152, 255), 'gem': (225, 242, 255, 255)},
        handle_colors={'base': (58, 68, 88, 255), 'wrap': (88, 98, 118, 255), 'wrap_alt': (42, 52, 72, 255)},
        blade_detail=frost_detail, tip_glow=(215, 238, 255, 180))
    save(img, "unique_claymore_3")

def draw_unique_dagger_3():  # Whispersting
    S = 16
    img, p = create(S)
    blade = [(235, 238, 248, 255), (202, 205, 218, 255), (168, 172, 185, 255), (128, 132, 148, 255)]
    guard = (158, 152, 145, 255)
    handle = (72, 55, 45, 255)
    handle_hi = (102, 82, 65, 255)

    # Blade diagonal — 3px wide
    for i in range(8):
        t = i / 7.0
        x = 7 + i
        y = 10 - i
        w = 2 if i < 6 else 1
        for dy in range(-w, w + 1):
            ci = min(3, int((dy + w) / max(1, 2*w) * 3))
            px(p, x, y + dy, blade[ci], S)
    px(p, 15, 2, lighten(blade[0], 15), S)

    # Guard
    for dx in range(-2, 3):
        px(p, 7 + dx, 11, guard, S)
        px(p, 7 + dx, 12, darken(guard, 15), S)

    # Handle
    for i in range(3):
        px(p, 5 - int(i*0.3), 13 + i, handle if i % 2 else handle_hi, S)
        px(p, 6 - int(i*0.3), 13 + i, handle_hi if i % 2 else handle, S)

    save(img, "unique_dagger_3")

def draw_unique_double_axe_3():  # Cindercleaver
    S = 32
    img, p = create(S)
    iron = (105, 82, 72, 255)
    iron_hi = (148, 118, 102, 255)
    iron_dk = (62, 48, 38, 255)
    fire = (255, 125, 22, 255)
    fire_hi = (255, 185, 62, 255)
    ember = (255, 205, 65, 165)
    handle = (82, 58, 35, 255)
    handle_hi = (112, 82, 52, 255)

    # Handle
    for y in range(14, 30):
        px(p, 15, y, handle, S)
        px(p, 16, y, handle_hi, S)
        px(p, 14, y, darken(handle, 12), S)
        if y % 3 == 0:
            px(p, 15, y, lighten(handle, 12), S)

    # Right blade
    for dy in range(-7, 8):
        w = max(0, int(8 - abs(dy) * 0.9))
        for dx in range(0, w):
            y = 10 + dy
            x = 17 + dx
            t = dx / max(1, w)
            c = iron_hi if t < 0.3 else iron if t < 0.7 else iron_dk
            px(p, x, y, c, S)

    # Left blade
    for dy in range(-7, 8):
        w = max(0, int(8 - abs(dy) * 0.9))
        for dx in range(0, w):
            y = 10 + dy
            x = 14 - dx
            t = dx / max(1, w)
            c = iron_hi if t < 0.3 else iron if t < 0.7 else iron_dk
            px(p, x, y, c, S)

    # Fire cracks in both blades
    for cx, cy in [(19,7),(21,9),(20,12),(18,14),(23,10),(10,7),(8,9),(9,12),(11,14),(6,10)]:
        px(p, cx, cy, fire, S)
    px(p, 20, 8, fire_hi, S)
    px(p, 9, 8, fire_hi, S)

    # Edge glow
    for dy in range(-6, 7):
        w = max(0, int(8 - abs(dy) * 0.9))
        if w > 0:
            px(p, 17 + w, 10 + dy, fire, S)
            px(p, 14 - w, 10 + dy, fire, S)

    # Embers
    for ex, ey, ea in [(26,5,165),(4,5,165),(27,12,105),(3,12,105)]:
        px(p, ex, ey, (ember[0],ember[1],ember[2],ea), S)

    # End cap
    px(p, 15, 30, darken(handle, 15), S)
    px(p, 16, 30, darken(handle, 15), S)

    save(img, "unique_double_axe_3")

def draw_unique_glaive_3():  # Moonreaver Glaive
    S = 32
    img, p = create(S)
    blade = (185, 195, 225, 255)
    blade_hi = (215, 225, 252, 255)
    blade_dk = (135, 145, 175, 255)
    moon = (205, 215, 245, 255)
    shaft = (105, 88, 72, 255)
    shaft_hi = (135, 115, 95, 255)
    shaft_dk = (75, 60, 45, 255)

    # Long shaft
    for y in range(14, 31):
        px(p, 14, y, shaft_hi, S)
        px(p, 15, y, shaft, S)
        px(p, 16, y, shaft_dk, S)
        if y % 3 == 0: px(p, 15, y, lighten(shaft, 12), S)

    # Blade — large crescent at top
    for y in range(1, 14):
        t = (y - 1) / 12.0
        # Crescent: wide in middle, narrow at ends
        w = int(2 + (1 - abs(t - 0.5) * 2) * 6)
        for dx in range(0, w):
            norm = dx / max(1, w)
            c = blade_hi if norm < 0.3 else blade if norm < 0.7 else blade_dk
            px(p, 17 + dx, y, c, S)
        # Inner curve (crescent cutout)
        if 4 < y < 11:
            inner_w = max(0, int(w * 0.4))
            for dx in range(0, inner_w):
                px(p, 17 + dx, y, (0, 0, 0, 0), S)

    # Blade back edge
    for y in range(1, 14):
        px(p, 17, y, darken(blade_dk, 15), S)

    # Edge highlight
    for y in range(1, 14):
        t = (y - 1) / 12.0
        w = int(2 + (1 - abs(t - 0.5) * 2) * 6)
        if w > 1:
            px(p, 17 + w - 1, y, lighten(blade_hi, 15), S)

    # Moon glow
    px(p, 22, 7, moon, S)
    px(p, 23, 8, (moon[0], moon[1], moon[2], 150), S)

    # Connection piece
    for dy in range(-1, 2):
        px(p, 15, 13 + dy, blade_dk, S)
        px(p, 16, 13 + dy, blade, S)

    save(img, "unique_glaive_3")

def draw_unique_hammer_3():  # Frostfall Maul
    S = 32
    img, p = create(S)
    ice = (145, 195, 242, 255)
    ice_hi = (195, 225, 255, 255)
    ice_dk = (95, 148, 205, 255)
    frost = (225, 242, 255, 185)
    handle = (82, 88, 105, 255)
    handle_hi = (112, 118, 135, 255)

    # Handle
    for y in range(16, 31):
        px(p, 15, y, handle, S)
        px(p, 16, y, handle_hi, S)
        px(p, 14, y, darken(handle, 12), S)
        if y % 3 == 0: px(p, 15, y, lighten(handle, 12), S)

    # Hammer head — large rectangular block
    for y in range(4, 16):
        for x in range(6, 26):
            ty = (y - 4) / 11.0
            tx = (x - 6) / 19.0
            c = lerp(ice_hi, ice_dk, ty * 0.6 + tx * 0.4)
            px(p, x, y, c, S)

    # Beveled edges
    for x in range(6, 26):
        px(p, x, 4, lighten(ice_hi, 18), S)
        px(p, x, 15, darken(ice_dk, 18), S)
    for y in range(4, 16):
        px(p, 6, y, lighten(ice_hi, 12), S)
        px(p, 25, y, darken(ice_dk, 12), S)

    # Frost crystal details
    for fx, fy in [(10,7),(14,10),(20,6),(18,12),(8,13),(22,9)]:
        px(p, fx, fy, frost, S)

    # Icicles hanging from bottom
    for ix, il in [(8,3),(12,2),(18,3),(22,2)]:
        for dy in range(il):
            px(p, ix, 16 + dy, (ice[0], ice[1], ice[2], 225 - dy * 45), S)

    save(img, "unique_hammer_3")

def draw_unique_mace_3():  # Radiant Morningstar
    S = 32
    img, p = create(S)
    gold = (225, 195, 55, 255)
    gold_hi = (255, 232, 105, 255)
    gold_dk = (175, 145, 22, 255)
    spike = (245, 215, 82, 255)
    glow = (255, 242, 155, 165)
    handle = (105, 72, 42, 255)
    handle_hi = (135, 98, 58, 255)

    # Handle
    for y in range(18, 31):
        px(p, 15, y, handle, S)
        px(p, 16, y, handle_hi, S)
        px(p, 14, y, darken(handle, 12), S)
        if y % 2 == 0: px(p, 15, y, lighten(handle, 12), S)

    # Morningstar head — spiked ball
    cx, cy = 16, 11
    head_r = 5
    for dy in range(-head_r, head_r + 1):
        for dx in range(-head_r, head_r + 1):
            dist = (dx*dx + dy*dy) ** 0.5
            if dist < head_r + 0.5:
                t = dist / head_r
                shade = (dx - dy) / (head_r * 2)
                if t < 0.35: c = gold_hi
                elif t < 0.65: c = lerp(gold, gold_dk, shade + 0.5)
                else: c = gold_dk
                px(p, cx + dx, cy + dy, c, S)

    # Spikes — 8 directions
    for angle in range(0, 360, 45):
        rad = angle * 3.14159 / 180
        for r in range(head_r, head_r + 4):
            sx = int(cx + r * math.cos(rad))
            sy = int(cy + r * math.sin(rad))
            t = (r - head_r) / 3.0
            c = lerp(spike, lighten(spike, 20), t)
            px(p, sx, sy, c, S)

    # Holy glow center
    px(p, cx - 1, cy - 1, glow, S)
    px(p, cx, cy, (255, 255, 225, 255), S)

    save(img, "unique_mace_3")

def draw_unique_sickle_3():  # Frostbite Reaper
    S = 16
    img, p = create(S)
    ice = (135, 195, 245, 255)
    ice_hi = (185, 225, 255, 255)
    ice_dk = (85, 145, 205, 255)
    frost = (225, 242, 255, 185)
    handle = (82, 88, 105, 255)
    handle_hi = (112, 118, 135, 255)

    # Handle
    for y in range(9, 15):
        px(p, 4, y, handle, S)
        px(p, 5, y, handle_hi, S)
        px(p, 6, y, darken(handle, 12), S)

    # Curved blade — 3px wide arc
    path = [(6,8),(7,7),(8,6),(9,5),(10,4),(11,3),(12,3),(13,4),(14,5),(14,6),(13,7)]
    for i, (x, y) in enumerate(path):
        t = i / 10.0
        px(p, x, y, lerp(ice_hi, ice_dk, t), S)
        px(p, x, y-1, ice_hi, S)
        px(p, x, y+1, ice_dk, S)

    # Frost sparkles
    for fx, fy in [(9,3),(11,2),(13,2),(15,5)]:
        px(p, fx, fy, frost, S)

    save(img, "unique_sickle_3")

def draw_unique_spear_3():  # Thunderlance
    S = 32
    img, p = create(S)

    def bolt_detail(p, S):
        # Lightning along spearhead
        px(p, 20, 6, (255, 255, 135, 255), S)
        px(p, 21, 5, (255, 255, 242, 255), S)
        px(p, 22, 4, (255, 255, 135, 255), S)
        # Sparks at tip
        px(p, 24, 2, (255, 255, 205, 185), S)
        px(p, 25, 1, (255, 255, 135, 125), S)

    build_spear(p, S,
        shaft_colors={'hi': (125, 118, 138, 255), 'mid': (95, 88, 108, 255), 'dk': (65, 58, 78, 255), 'wrap': (142, 135, 155, 255)},
        head_colors={'hi': (215, 222, 242, 255), 'mid': (178, 188, 218, 255), 'dk': (135, 148, 185, 255), 'outline': (88, 98, 128, 255), 'glow': (255, 255, 195, 255)},
        extra_fn=bolt_detail)
    save(img, "unique_spear_3")

def draw_unique_longbow_3():  # Galewind Bow
    S = 32
    img, p = create(S)
    wood = (145, 125, 82, 255)
    wood_hi = (178, 155, 108, 255)
    wood_dk = (105, 85, 55, 255)
    string = (205, 198, 188, 255)
    wind = (185, 215, 242, 140)
    feather = (195, 215, 235, 255)
    grip = (95, 72, 42, 255)

    # Bow limb — curved arc
    for y in range(1, 31):
        t = (y - 1) / 29.0
        curve = int(22 - 10 * abs(t - 0.5) * 2)
        px(p, curve - 1, y, wood_hi, S)
        px(p, curve, y, wood, S)
        px(p, curve + 1, y, wood_dk, S)

    # String
    for y in range(2, 30):
        px(p, 12, y, string, S)

    # Grip wrapping
    for y in range(13, 19):
        cx = int(22 - 10 * abs((y-1)/29.0 - 0.5) * 2)
        px(p, cx - 2, y, grip, S)
        px(p, cx - 1, y, lighten(grip, 15), S)
        px(p, cx, y, grip, S)
        px(p, cx + 1, y, darken(grip, 15), S)
        px(p, cx + 2, y, grip, S)

    # Feather decorations at tips
    px(p, 13, 1, feather, S)
    px(p, 14, 0, feather, S)
    px(p, 12, 0, lighten(feather, 10), S)
    px(p, 13, 30, feather, S)
    px(p, 14, 31, feather, S)
    px(p, 12, 31, lighten(feather, 10), S)

    # Wind swirls
    for sx, sy in [(24,6),(25,10),(23,22),(24,26),(8,8),(9,24)]:
        px(p, sx, sy, wind, S)

    save(img, "unique_longbow_3")

def draw_unique_heavy_crossbow_3():  # Arcane Arbalest
    S = 32
    img, p = create(S)
    wood = (82, 62, 92, 255)
    wood_hi = (112, 88, 125, 255)
    wood_dk = (52, 35, 62, 255)
    arcane = (165, 105, 232, 255)
    arcane_hi = (205, 145, 255, 255)
    string = (185, 175, 205, 255)
    metal = (145, 135, 165, 255)
    metal_dk = (108, 98, 128, 255)
    glow = (185, 125, 255, 140)

    # Stock (horizontal-ish)
    for x in range(2, 24):
        for dy in range(-2, 3):
            t = (x - 2) / 21.0
            c = lerp(wood_hi, wood_dk, t)
            if dy == -2: c = lighten(c, 12)
            elif dy == 2: c = darken(c, 12)
            px(p, x, 16 + dy, c, S)

    # Bow limbs — curved
    for y in range(6, 26):
        t = abs(y - 16) / 10.0
        curve_x = int(24 + t * 5)
        for dx in range(-1, 2):
            c = metal if dx == 0 else lighten(metal, 12) if dx == -1 else metal_dk
            px(p, curve_x + dx, y, c, S)

    # String
    for y in range(7, 25):
        px(p, 24, y, string, S)

    # Trigger
    px(p, 10, 19, metal, S)
    px(p, 10, 20, metal, S)
    px(p, 10, 21, metal_dk, S)
    px(p, 11, 20, metal_dk, S)

    # Arcane runes on stock
    for rx, ry in [(6,16),(10,16),(14,16),(18,16)]:
        px(p, rx, ry, arcane, S)
        px(p, rx, ry - 1, glow, S)

    # Arcane crystal at front
    px(p, 22, 15, arcane_hi, S)
    px(p, 22, 16, arcane, S)
    px(p, 23, 15, arcane, S)
    px(p, 23, 16, darken(arcane, 15), S)

    # Bolt channel
    for x in range(20, 30):
        px(p, x, 15, darken(wood, 15), S)
        px(p, x, 16, darken(wood, 20), S)

    save(img, "unique_heavy_crossbow_3")

def draw_unique_staff_damage_8():  # Stormcaller's Rod
    S = 32
    img, p = create(S)

    def lightning_burst(p, S):
        # Lightning bolts from orb
        bolt_c = (255, 255, 135, 255)
        bolt_w = (255, 255, 242, 255)
        px(p, 20, 0, bolt_w, S)
        px(p, 19, 1, bolt_c, S)
        px(p, 21, 1, bolt_c, S)
        px(p, 18, 0, (bolt_c[0], bolt_c[1], bolt_c[2], 140), S)
        px(p, 22, 0, (bolt_c[0], bolt_c[1], bolt_c[2], 140), S)
        px(p, 14, 4, bolt_c, S)
        px(p, 26, 5, bolt_c, S)

    build_staff(p, S,
        shaft_colors={'hi': (128, 118, 148, 255), 'mid': (98, 88, 118, 255), 'dk': (68, 58, 88, 255), 'wrap': (145, 135, 168, 255), 'wrap_alt': (82, 72, 102, 255)},
        head_colors={'core': (215, 235, 255, 255), 'bright': (165, 205, 248, 255), 'mid': (118, 175, 235, 255), 'dk': (75, 135, 205, 255), 'glow': (185, 225, 255, 135)},
        head_radius=5,
        head_type='orb',
        extra_fn=lightning_burst)
    save(img, "unique_staff_damage_8")

def draw_unique_staff_heal_3():  # Living Wood Staff
    S = 32
    img, p = create(S)

    def bloom(p, S):
        # Blossoms at top
        px(p, 17, 2, (255, 185, 205, 255), S)
        px(p, 23, 3, (255, 185, 205, 255), S)
        # Extra leaves
        px(p, 14, 5, (55, 155, 42, 255), S)
        px(p, 26, 6, (55, 155, 42, 255), S)
        px(p, 15, 3, (72, 175, 58, 255), S)
        px(p, 25, 4, (72, 175, 58, 255), S)
        # Roots at base
        px(p, 3, 30, (65, 48, 25, 255), S)
        px(p, 2, 31, (55, 38, 18, 255), S)
        px(p, 6, 31, (55, 38, 18, 255), S)

    build_staff(p, S,
        shaft_colors={'hi': (128, 102, 58, 255), 'mid': (98, 72, 38, 255), 'dk': (65, 48, 22, 255), 'wrap': (48, 125, 38, 255), 'wrap_alt': (72, 95, 42, 255)},
        head_colors={'core': (145, 255, 148, 255), 'bright': (105, 232, 108, 255), 'mid': (68, 195, 72, 255), 'dk': (42, 148, 45, 255), 'glow': (105, 235, 108, 130)},
        head_radius=5,
        head_type='orb',
        extra_fn=bloom)
    save(img, "unique_staff_heal_3")

def draw_unique_shield_3():  # Dreadnought Ward
    def skull_emblem(p, S):
        # Menacing red eye
        px(p, 15, 12, (205, 52, 42, 255), S)
        px(p, 17, 12, (205, 52, 42, 255), S)
        px(p, 16, 14, (205, 52, 42, 200), S)
        # V shape
        for i in range(4):
            px(p, 13-i, 10+i, (105, 95, 115, 255), S)
            px(p, 19+i, 10+i, (105, 95, 115, 255), S)
        # Spike at top
        px(p, 16, 0, (95, 82, 105, 255), S)
        px(p, 16, 1, (85, 72, 95, 255), S)
        # Side spikes
        px(p, 3, 10, (95, 82, 105, 255), S)
        px(p, 29, 10, (85, 72, 95, 255), S)

    draw_greatshield("unique_shield_3",
        face_colors={'hi': (72, 62, 88, 255), 'mid': (48, 42, 62, 255), 'dk': (28, 22, 42, 255), 'edge': (18, 12, 28, 255)},
        trim_color=(62, 52, 78, 255),
        emblem_fn=skull_emblem)


# ============================================================
# MAIN
# ============================================================
def main():
    print("=" * 60)
    print("Regenerating 41 weapon textures (v2 — dense pixel art)")
    print("=" * 60)

    count = 0

    print("\n=== Core RPG Weapons (8, 32x32) ===")
    for fn in [draw_voidreaver, draw_solaris, draw_stormfury, draw_briarthorn,
               draw_abyssal_trident, draw_pyroclast, draw_whisperwind, draw_soulchain]:
        fn(); count += 1

    print("\n=== Arsenal Whips (3, 16x16) ===")
    for fn in [draw_unique_whip_1, draw_unique_whip_2, draw_unique_whip_sw]:
        fn(); count += 1

    print("\n=== Arsenal Wands (3, 16x16) ===")
    for fn in [draw_unique_wand_1, draw_unique_wand_2, draw_unique_wand_sw]:
        fn(); count += 1

    print("\n=== Arsenal Katanas (3, 32x32) ===")
    for fn in [draw_unique_katana_1, draw_unique_katana_2, draw_unique_katana_sw]:
        fn(); count += 1

    print("\n=== Arsenal Greatshields (3, 32x32) ===")
    for fn in [draw_unique_greatshield_1, draw_unique_greatshield_2, draw_unique_greatshield_sw]:
        fn(); count += 1

    print("\n=== Arsenal Throwing Axes (3, 16x16) ===")
    for fn in [draw_unique_throwing_axe_1, draw_unique_throwing_axe_2, draw_unique_throwing_axe_sw]:
        fn(); count += 1

    print("\n=== Arsenal Rapiers (3, 32x32) ===")
    for fn in [draw_unique_rapier_1, draw_unique_rapier_2, draw_unique_rapier_sw]:
        fn(); count += 1

    print("\n=== Arsenal Longswords (2, 32x32) ===")
    for fn in [draw_unique_longsword_1, draw_unique_longsword_2]:
        fn(); count += 1

    print("\n=== Fill-in Variants (15, mixed sizes) ===")
    for fn in [draw_unique_claymore_3, draw_unique_dagger_3, draw_unique_double_axe_3,
               draw_unique_glaive_3, draw_unique_hammer_3, draw_unique_mace_3,
               draw_unique_sickle_3, draw_unique_spear_3, draw_unique_longbow_3,
               draw_unique_heavy_crossbow_3, draw_unique_staff_damage_8,
               draw_unique_staff_heal_3, draw_unique_shield_3]:
        fn(); count += 1

    print(f"\nDone! Regenerated {count} weapon textures")

if __name__ == "__main__":
    main()
