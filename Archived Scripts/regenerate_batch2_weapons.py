"""
Regenerate Batch 2 Arsenal weapon textures to match Batch 1 quality.

Style Guide (from Batch 1 analysis):
  - 5-6 shade gradient per material (edge_hi -> mid_hi -> mid -> mid_dk -> edge_dk -> outline)
  - Each shade ~25-30 value steps apart
  - Diagonal orientation: handle bottom-left, business end top-right
  - 15-21% canvas coverage, 15-25 distinct colors
  - Blade/shaft width: 3-7px
  - Material differentiation: metal, wood, leather/wrap, gem each distinct
  - 1px anti-alias edge transitions
  - Accent details: glowing gems, wrapping patterns, guard filigree, tip glow
  - Light from top-left, shadow bottom-right

Weapons to regenerate (18 textures):
  Katanas (32x32):       unique_katana_1, _2, _sw
  Greatshields (32x32):  unique_greatshield_1, _2, _sw
  Rapiers (32x32):       unique_rapier_1, _2, _sw
  Whips (16x16):         unique_whip_1, _2, _sw
  Wands (16x16):         unique_wand_1, _2, _sw
  Throwing Axes (16x16): unique_throwing_axe_1, _2, _sw
"""
from PIL import Image
import os, math

OUTPUT_DIR = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\item"


# ============================================================
# UTILITY FUNCTIONS (matching regenerate_weapons_v2.py)
# ============================================================

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
        if len(c) == 3:
            p[x, y] = c + (255,)
        else:
            p[x, y] = c


# ============================================================
# KATANA BUILDER — curved blade, wrapped handle, tsuba guard
# 32x32, diagonal bottom-left to top-right, gentle curve
# ============================================================

def build_katana(p, S, blade_colors, guard_colors, handle_colors,
                 blade_len=22, curve_amt=2.5, extra_fn=None):
    bc = blade_colors
    gc = guard_colors
    hc = handle_colors

    guard_row = blade_len + 1

    # Blade — curved diagonal from top-right to center
    for i in range(blade_len):
        t = i / max(1, blade_len - 1)
        # Straight diagonal with gentle curve offset
        base_x = 29 - int(i * (29 - 10) / blade_len)
        base_y = 1 + int(i * (guard_row - 1) / blade_len)
        # Curve: sinusoidal offset perpendicular to blade axis
        curve_offset = math.sin(t * math.pi) * curve_amt
        cx = base_x + int(curve_offset * 0.5)
        cy = base_y - int(curve_offset * 0.7)

        # Taper: narrow at tip, wider at base (wider than before for visual weight)
        half_w = 1 if t < 0.1 else (2 if t < 0.4 else 3)

        for dy in range(-half_w, half_w + 1):
            for dx in range(0, 3 if half_w >= 2 else 2):
                xx = cx + dx
                yy = cy + dy
                norm_dy = (dy + half_w) / max(1, 2 * half_w)

                if norm_dy < 0.12:
                    c = bc.get('outline', darken(bc['edge_dk'], 30))
                elif norm_dy < 0.25:
                    c = bc['edge_hi']
                elif norm_dy < 0.5:
                    c = lerp(bc['mid_hi'], bc['mid'], t)
                elif norm_dy < 0.75:
                    c = lerp(bc['mid'], bc['mid_dk'], t)
                elif norm_dy < 0.88:
                    c = bc['edge_dk']
                else:
                    c = bc.get('outline', darken(bc['edge_dk'], 30))
                px(p, xx, yy, c, S)

        # Highlight along spine (back edge of katana)
        px(p, cx, cy - half_w, lighten(bc['edge_hi'], 15), S)
        # Hamon line (temper line) — characteristic katana feature
        if half_w >= 2 and i > 2:
            hamon_dy = half_w - 1 + (1 if (i % 3 == 0) else 0)
            px(p, cx, cy + hamon_dy, lighten(bc['mid_hi'], 20), S)

    # Tip — kissaki shape
    tip_x, tip_y = 29, 1
    px(p, tip_x + 1, tip_y - 1, bc['edge_hi'], S)
    px(p, tip_x + 1, tip_y, bc['mid_hi'], S)
    if 'tip_glow' in bc:
        px(p, tip_x + 2, tip_y - 1, bc['tip_glow'], S)

    # Tsuba (guard) — circular/oval
    guard_cx = 10
    guard_cy = guard_row + 1
    tsuba_r = 3
    for dy in range(-tsuba_r, tsuba_r + 1):
        for dx in range(-tsuba_r, tsuba_r + 1):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < tsuba_r + 0.5:
                t_g = dist / tsuba_r
                shade = (dx - dy) / (tsuba_r * 2)
                if t_g < 0.4:
                    c = gc.get('gem', gc['hi'])
                elif t_g < 0.7:
                    c = lerp(gc['hi'], gc['dk'], shade + 0.5)
                else:
                    c = gc['dk']
                px(p, guard_cx + dx, guard_cy + dy, c, S)
    # Tsuba center hole
    px(p, guard_cx, guard_cy, darken(gc['dk'], 20), S)

    # Handle (tsuka) — wrapped with diamond pattern
    handle_len = 7
    for i in range(handle_len):
        t = i / max(1, handle_len - 1)
        hx = guard_cx - 2 - int(i * 0.8)
        hy = guard_cy + tsuba_r + 1 + i

        for dx in range(-1, 2):
            # Diamond wrap pattern (ito)
            wrap_phase = (i + dx) % 3
            if wrap_phase == 0:
                c = hc['wrap']
            elif wrap_phase == 1:
                c = hc['base']
            else:
                c = hc['wrap_alt']
            px(p, hx + dx, hy, c, S)

    # Kashira (pommel cap)
    pm_x = guard_cx - 2 - int(handle_len * 0.8)
    pm_y = guard_cy + tsuba_r + 1 + handle_len
    pm_c = gc['hi']
    px(p, pm_x, pm_y, pm_c, S)
    px(p, pm_x - 1, pm_y, lighten(pm_c, 15), S)
    px(p, pm_x + 1, pm_y, darken(pm_c, 15), S)
    px(p, pm_x, pm_y + 1, darken(pm_c, 25), S)

    if extra_fn:
        extra_fn(p, S)


# ============================================================
# GREATSHIELD BUILDER — large tower shield, boss, rivets
# 32x32, front-facing with slight perspective tilt
# ============================================================

def build_greatshield(p, S, face_colors, trim_colors, boss_colors,
                      emblem_fn=None, extra_fn=None):
    fc = face_colors
    tc = trim_colors
    boc = boss_colors

    # Shield body — tall rectangle with rounded top
    shield_x, shield_y = 6, 2
    shield_w, shield_h = 20, 26

    # Draw shield face
    for y in range(shield_h):
        fy = shield_y + y
        t_y = y / max(1, shield_h - 1)

        # Width narrows at top (rounded top)
        if y < 4:
            inset = 4 - y
        elif y > shield_h - 3:
            inset = y - (shield_h - 3)
        else:
            inset = 0

        row_x1 = shield_x + inset
        row_x2 = shield_x + shield_w - inset

        for x in range(row_x1, row_x2):
            t_x = (x - row_x1) / max(1, row_x2 - row_x1 - 1)

            # Convex shading — lighter in center, darker at edges
            center_dist = abs(t_x - 0.4)  # light slightly left of center
            shade = center_dist * 1.5 + t_y * 0.3

            if shade < 0.2:
                c = fc['hi']
            elif shade < 0.4:
                c = lerp(fc['hi'], fc['mid'], (shade - 0.2) / 0.2)
            elif shade < 0.6:
                c = fc['mid']
            elif shade < 0.8:
                c = lerp(fc['mid'], fc['dk'], (shade - 0.6) / 0.2)
            else:
                c = fc['dk']
            px(p, x, fy, c, S)

    # Trim/border — 1px bright edge on top-left, dark on bottom-right
    for y in range(shield_h):
        fy = shield_y + y
        if y < 4:
            inset = 4 - y
        elif y > shield_h - 3:
            inset = y - (shield_h - 3)
        else:
            inset = 0
        row_x1 = shield_x + inset
        row_x2 = shield_x + shield_w - inset - 1

        # Left and top edges
        px(p, row_x1, fy, tc['hi'], S)
        if y == max(0, inset):
            for x in range(row_x1, row_x2 + 1):
                px(p, x, fy, tc['hi'], S)
        # Right and bottom edges
        px(p, row_x2, fy, tc['dk'], S)
        if y == shield_h - 1:
            for x in range(row_x1, row_x2 + 1):
                px(p, x, fy, tc['dk'], S)

    # Second border line inset
    for y in range(2, shield_h - 2):
        fy = shield_y + y
        if y < 4:
            inset = 4 - y + 1
        elif y > shield_h - 3:
            inset = y - (shield_h - 3) + 1
        else:
            inset = 1
        px(p, shield_x + inset, fy, tc['mid'], S)
        px(p, shield_x + shield_w - inset - 1, fy, darken(tc['mid'], 15), S)

    # Central boss — circular raised metal piece
    boss_cx = shield_x + shield_w // 2
    boss_cy = shield_y + shield_h // 2 - 1
    boss_r = 4
    for dy in range(-boss_r, boss_r + 1):
        for dx in range(-boss_r, boss_r + 1):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < boss_r + 0.5:
                t = dist / boss_r
                shade = (dx - dy) / (boss_r * 2)
                if t < 0.3:
                    c = lighten(boc['hi'], 20)
                elif t < 0.5:
                    c = boc['hi']
                elif t < 0.75:
                    c = lerp(boc['mid'], boc['dk'], shade + 0.5)
                else:
                    c = boc['dk']
                px(p, boss_cx + dx, boss_cy + dy, c, S)
    # Boss center stud
    px(p, boss_cx, boss_cy, lighten(boc['hi'], 40), S)
    px(p, boss_cx - 1, boss_cy - 1, lighten(boc['hi'], 25), S)

    # Rivets — 4 corners of the boss area
    rivet_offsets = [(-6, -7), (6, -7), (-6, 7), (6, 7)]
    for rx, ry in rivet_offsets:
        rvx = boss_cx + rx
        rvy = boss_cy + ry
        if 0 <= rvx < S and 0 <= rvy < S:
            px(p, rvx, rvy, tc['hi'], S)
            px(p, rvx + 1, rvy + 1, tc['dk'], S)

    # Vertical reinforcement bands
    band_x1 = shield_x + 5
    band_x2 = shield_x + shield_w - 6
    for y in range(4, shield_h - 3):
        fy = shield_y + y
        px(p, band_x1, fy, tc['mid'], S)
        px(p, band_x2, fy, tc['mid'], S)

    if emblem_fn:
        emblem_fn(p, S, boss_cx, boss_cy)

    if extra_fn:
        extra_fn(p, S)


# ============================================================
# RAPIER BUILDER — thin blade, ornate basket guard
# 32x32, diagonal
# ============================================================

def build_rapier(p, S, blade_colors, guard_colors, handle_colors,
                 blade_len=20, extra_fn=None):
    bc = blade_colors
    gc = guard_colors
    hc = handle_colors

    guard_row = blade_len + 2

    # Blade — thin (2-3px), long, straight diagonal
    for i in range(blade_len):
        t = i / max(1, blade_len - 1)
        cx = 29 - int(i * (29 - 12) / blade_len)
        cy = 2 + int(i * (guard_row - 2) / blade_len)

        # Rapier blade: thin at tip, 2-3px wide in body
        half_w = 0 if t < 0.1 else (1 if t < 0.5 else 2)

        for dy in range(-half_w, half_w + 1):
            norm_dy = (dy + half_w) / max(1, 2 * half_w) if half_w > 0 else 0.5

            if norm_dy < 0.15:
                c = bc.get('outline', darken(bc['edge_dk'], 25))
            elif norm_dy < 0.3:
                c = bc['edge_hi']
            elif norm_dy < 0.55:
                c = lerp(bc['mid_hi'], bc['mid'], t)
            elif norm_dy < 0.75:
                c = bc['mid_dk']
            elif norm_dy < 0.9:
                c = bc['edge_dk']
            else:
                c = bc.get('outline', darken(bc['edge_dk'], 25))

            px(p, cx, cy + dy, c, S)
            px(p, cx + 1, cy + dy, lerp(c, bc['mid'], 0.3), S)
            # Third pixel of width for mid-section
            if half_w >= 2:
                px(p, cx - 1, cy + dy, lerp(c, bc['edge_dk'], 0.5), S)

        # Fuller (groove line down center)
        if half_w > 0 and i > 2:
            px(p, cx, cy, lighten(bc['mid_hi'], 10), S)

    # Tip
    px(p, 30, 1, bc['edge_hi'], S)
    px(p, 31, 0, lighten(bc['edge_hi'], 20), S)

    # Basket guard — ornate curved guard
    guard_cx = 12
    guard_cy = guard_row + 1

    # Main guard ring
    ring_r = 4
    for angle in range(0, 360, 8):
        rad = angle * math.pi / 180
        gx = int(guard_cx + ring_r * math.cos(rad))
        gy = int(guard_cy + ring_r * math.sin(rad))
        t_a = angle / 360
        c = lerp(gc['hi'], gc['dk'], t_a)
        px(p, gx, gy, c, S)

    # Cross bars of basket
    for dx in range(-ring_r, ring_r + 1):
        t_g = (dx + ring_r) / (2 * ring_r)
        c = lerp(gc['hi'], gc['dk'], t_g)
        px(p, guard_cx + dx, guard_cy, c, S)
    for dy in range(-ring_r + 1, ring_r):
        t_g = (dy + ring_r) / (2 * ring_r)
        c = lerp(gc['hi'], gc['dk'], t_g)
        px(p, guard_cx, guard_cy + dy, c, S)

    # Guard gem at center
    if 'gem' in gc:
        px(p, guard_cx, guard_cy, gc['gem'], S)
        px(p, guard_cx - 1, guard_cy, lighten(gc['gem'], 15), S)
        px(p, guard_cx + 1, guard_cy, darken(gc['gem'], 15), S)

    # Knuckle bow (curved bar connecting guard to pommel)
    for i in range(4):
        t = i / 3
        kx = guard_cx - ring_r - i
        ky = guard_cy + i + 1
        c = lerp(gc['hi'], gc['dk'], t)
        px(p, kx, ky, c, S)
        px(p, kx + 1, ky, darken(c, 10), S)

    # Handle
    handle_len = 5
    for i in range(handle_len):
        t = i / max(1, handle_len - 1)
        hx = guard_cx - ring_r - 4 - int(i * 0.7)
        hy = guard_cy + 5 + i

        for dx in range(-1, 2):
            if i % 2 == 0:
                c = hc['wrap'] if dx == 0 else hc['base']
            else:
                c = hc['wrap_alt'] if dx == 0 else darken(hc['base'], 15)
            px(p, hx + dx, hy, c, S)

    # Pommel
    pm_x = guard_cx - ring_r - 4 - int(handle_len * 0.7)
    pm_y = guard_cy + 5 + handle_len
    pm_c = gc['hi']
    px(p, pm_x, pm_y, pm_c, S)
    px(p, pm_x - 1, pm_y, lighten(pm_c, 15), S)
    px(p, pm_x + 1, pm_y, darken(pm_c, 15), S)
    px(p, pm_x, pm_y + 1, darken(pm_c, 25), S)

    if extra_fn:
        extra_fn(p, S)


# ============================================================
# 16x16 BUILDERS — whip, wand, throwing axe
# ============================================================

def px16(p, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        if len(c) == 3:
            p[x, y] = c + (255,)
        else:
            p[x, y] = c


def build_whip(p, lash_colors, handle_colors, curve_style=0, extra_fn=None):
    lc = lash_colors
    hc = handle_colors
    S = 16

    # Handle — bottom-left, thick with wrapping
    for i in range(5):
        t = i / 4
        hx = 2 + i
        hy = 14 - i
        # 3px wide handle for more presence
        for dy in range(-1, 2):
            if i % 2 == 0:
                c = hc['wrap'] if dy == 0 else (hc['base'] if dy == -1 else hc['wrap_alt'])
            else:
                c = hc['base'] if dy == 0 else (hc['wrap_alt'] if dy == -1 else darken(hc['base'], 20))
            px16(p, hx, hy + dy, c)
    # Pommel knob
    px16(p, 1, 15, hc['pommel'])
    px16(p, 2, 15, darken(hc['pommel'], 20))
    px16(p, 1, 14, lighten(hc['pommel'], 15))

    # Lash — different curve shapes per variant
    lash_len = 10
    for i in range(lash_len):
        t = i / max(1, lash_len - 1)

        if curve_style == 0:
            # S-curve — sweeps up then curls down at tip
            lx = 7 + int(i * 0.8)
            ly = 9 - int(i * 0.5) + int(math.sin(t * math.pi * 1.5) * 2.5)
        elif curve_style == 1:
            # Upward arc — sweeps up and right
            lx = 7 + int(i * 0.85)
            ly = 9 - int(i * 0.8) + int(math.sin(t * math.pi * 0.8) * 1.5)
        else:
            # Downward crack — goes right and dips down then snaps up
            lx = 7 + int(i * 0.8)
            ly = 8 + int(math.sin(t * math.pi * 2.0) * 3.0) - int(i * 0.3)

        # Taper from thick to thin with stronger contrast
        if t < 0.25:
            # Thick end near handle — 3px
            px16(p, lx, ly, lc['mid'])
            px16(p, lx, ly - 1, lc['hi'])
            px16(p, lx, ly + 1, lc['dk'])
            px16(p, lx + 1, ly, darken(lc['mid'], 15))
        elif t < 0.55:
            # Mid section — 2px
            px16(p, lx, ly, lc['mid'])
            px16(p, lx, ly - 1, lc['hi'])
            px16(p, lx + 1, ly, lc['dk'])
        elif t < 0.8:
            # Thin section
            px16(p, lx, ly, lerp(lc['mid'], lc['tip'], (t - 0.55) / 0.25))
            px16(p, lx, ly - 1, lc['hi'])
        else:
            # Tip
            c = lerp(lc['mid'], lc['tip'], (t - 0.7) / 0.3)
            px16(p, lx, ly, c)

    # Cracker/tip accent
    tip_x = 7 + int(lash_len * 0.8)
    tip_y = 9 - int(lash_len * 0.5) + int(math.sin(math.pi * 1.5) * 2.5)
    if curve_style == 1:
        tip_y = 9 - int(lash_len * 0.8) + int(math.sin(lash_len / 9 * math.pi * 0.8) * 1.5)
    elif curve_style == 2:
        tip_y = 8 + int(math.sin(math.pi * 2.0) * 3.0) - int(lash_len * 0.3)
    if 'glow' in lc:
        px16(p, min(15, tip_x + 1), max(0, tip_y - 1), lc['glow'])
        px16(p, min(15, tip_x), max(0, tip_y - 1), darken(lc['glow'], 30))

    if extra_fn:
        extra_fn(p)


def build_wand(p, shaft_colors, tip_colors, extra_fn=None):
    sc = shaft_colors
    tc = tip_colors

    # Shaft — diagonal from bottom-left to top-right, thin
    shaft_len = 10
    for i in range(shaft_len):
        t = i / max(1, shaft_len - 1)
        sx = 3 + int(i * 0.9)
        sy = 13 - int(i * 0.9)

        # 1-2px wide shaft with shading
        c_main = lerp(sc['hi'], sc['dk'], t)
        px16(p, sx, sy, c_main)
        px16(p, sx + 1, sy, darken(c_main, 20))

        # Wrap detail
        if i % 3 == 0 and i < shaft_len - 2:
            px16(p, sx, sy, sc.get('wrap', lighten(sc['hi'], 10)))

    # Tip — crystal/orb at top-right
    tip_cx = 3 + int(shaft_len * 0.9) + 1
    tip_cy = 13 - int(shaft_len * 0.9) - 1
    tip_r = 2

    for dy in range(-tip_r, tip_r + 1):
        for dx in range(-tip_r, tip_r + 1):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < tip_r + 0.5:
                t = dist / tip_r
                if t < 0.3:
                    c = tc['core']
                elif t < 0.6:
                    c = tc['bright']
                elif t < 0.85:
                    c = tc['mid']
                else:
                    c = tc['dk']
                px16(p, tip_cx + dx, tip_cy + dy, c)

    # Core highlight
    px16(p, tip_cx - 1, tip_cy - 1, lighten(tc['core'], 30))

    # Glow
    if 'glow' in tc:
        for angle in range(0, 360, 60):
            rad = angle * math.pi / 180
            gx = int(tip_cx + (tip_r + 1) * math.cos(rad))
            gy = int(tip_cy + (tip_r + 1) * math.sin(rad))
            px16(p, gx, gy, tc['glow'])

    # Grip end cap
    px16(p, 2, 14, sc['dk'])
    px16(p, 3, 14, darken(sc['dk'], 15))

    if extra_fn:
        extra_fn(p)


def build_throwing_axe(p, head_colors, handle_colors, extra_fn=None):
    hc_ax = head_colors
    hndl = handle_colors

    # Handle — short diagonal shaft
    for i in range(6):
        t = i / 5
        hx = 4 + int(i * 0.6)
        hy = 12 - i

        c = lerp(hndl['hi'], hndl['dk'], t)
        px16(p, hx, hy, c)
        px16(p, hx + 1, hy, darken(c, 20))

        # Wrap
        if i == 1 or i == 3:
            px16(p, hx, hy, hndl.get('wrap', lighten(hndl['hi'], 10)))

    # Axe head — larger crescent/fan shape at top-right
    head_cx = 9
    head_cy = 4

    # Blade fan — bigger, more angular
    for dy in range(-4, 5):
        t_y = (dy + 4) / 8
        width = 5 - abs(dy)  # Wider fan shape
        if width < 1:
            width = 1
        for dx in range(0, width + 1):
            t_x = dx / max(1, width)
            norm = t_x * 0.6 + t_y * 0.4

            if norm < 0.12:
                c = hc_ax.get('outline', darken(hc_ax['dk'], 25))
            elif norm < 0.28:
                c = hc_ax['hi']
            elif norm < 0.5:
                c = lerp(hc_ax['hi'], hc_ax['mid'], (norm - 0.28) / 0.22)
            elif norm < 0.72:
                c = lerp(hc_ax['mid'], hc_ax['dk'], (norm - 0.5) / 0.22)
            elif norm < 0.88:
                c = hc_ax['dk']
            else:
                c = hc_ax.get('outline', darken(hc_ax['dk'], 25))

            px16(p, head_cx + dx, head_cy + dy, c)

    # Cutting edge highlight — bright leading edge
    for dy in range(-3, 4):
        edge_x = head_cx + (5 - abs(dy))
        if edge_x < 16:
            px16(p, edge_x, head_cy + dy, lighten(hc_ax['hi'], 20))

    # Back of head (poll) — thicker spine
    for dy in range(-2, 3):
        px16(p, head_cx - 1, head_cy + dy, hc_ax['dk'])
        px16(p, head_cx, head_cy + dy, darken(hc_ax['mid'], 10))

    # Pommel end
    px16(p, 3, 13, hndl['dk'])
    px16(p, 4, 13, darken(hndl['dk'], 15))

    if extra_fn:
        extra_fn(p)


# ============================================================
# KATANA VARIANTS
# ============================================================

def draw_katana_1():
    """Windcutter — pale steel/silver katana with wind motif"""
    S = 32
    img, p = create(S)

    def wind_wisps(p, S):
        # Small wind streak accents
        px(p, 25, 4, (200, 230, 255, 120), S)
        px(p, 26, 3, (180, 220, 250, 90), S)
        px(p, 22, 7, (190, 225, 255, 100), S)

    build_katana(p, S,
        blade_colors={
            'edge_hi': (220, 228, 240, 255),
            'mid_hi': (195, 205, 220, 255),
            'mid': (170, 180, 195, 255),
            'mid_dk': (140, 150, 168, 255),
            'edge_dk': (110, 120, 140, 255),
            'outline': (75, 82, 98, 255),
            'tip_glow': (230, 240, 255, 180),
        },
        guard_colors={
            'hi': (160, 165, 175, 255),
            'mid': (120, 125, 138, 255),
            'dk': (80, 85, 100, 255),
        },
        handle_colors={
            'base': (50, 55, 70, 255),
            'wrap': (90, 95, 115, 255),
            'wrap_alt': (65, 70, 85, 255),
        },
        extra_fn=wind_wisps,
    )
    save(img, "unique_katana_1")

def draw_katana_2():
    """Shadowmoon Blade — dark purple/midnight katana"""
    S = 32
    img, p = create(S)

    def shadow_fx(p, S):
        px(p, 27, 3, (120, 80, 180, 110), S)
        px(p, 24, 6, (100, 60, 160, 90), S)
        px(p, 20, 10, (130, 90, 190, 80), S)

    build_katana(p, S,
        blade_colors={
            'edge_hi': (150, 130, 185, 255),
            'mid_hi': (120, 100, 160, 255),
            'mid': (90, 70, 130, 255),
            'mid_dk': (65, 45, 100, 255),
            'edge_dk': (45, 28, 72, 255),
            'outline': (25, 15, 45, 255),
            'tip_glow': (180, 150, 220, 160),
        },
        guard_colors={
            'hi': (130, 110, 160, 255),
            'mid': (90, 72, 120, 255),
            'dk': (55, 38, 78, 255),
            'gem': (200, 160, 255, 255),
        },
        handle_colors={
            'base': (35, 25, 50, 255),
            'wrap': (75, 55, 105, 255),
            'wrap_alt': (50, 35, 72, 255),
        },
        extra_fn=shadow_fx,
    )
    save(img, "unique_katana_2")

def draw_katana_sw():
    """Ashenblade of the Eternal Dusk — red/ember legendary katana"""
    S = 32
    img, p = create(S)

    def ember_fx(p, S):
        # Ember glow along blade
        px(p, 28, 2, (255, 180, 60, 150), S)
        px(p, 25, 5, (255, 160, 40, 120), S)
        px(p, 22, 8, (255, 140, 30, 100), S)
        px(p, 19, 12, (255, 120, 20, 80), S)
        # Ash particles
        px(p, 30, 0, (200, 200, 190, 100), S)
        px(p, 26, 4, (180, 175, 165, 80), S)

    build_katana(p, S,
        blade_colors={
            'edge_hi': (220, 180, 140, 255),
            'mid_hi': (200, 140, 90, 255),
            'mid': (170, 100, 55, 255),
            'mid_dk': (140, 70, 30, 255),
            'edge_dk': (100, 45, 15, 255),
            'outline': (60, 25, 8, 255),
            'tip_glow': (255, 200, 100, 200),
        },
        guard_colors={
            'hi': (180, 150, 100, 255),
            'mid': (140, 110, 65, 255),
            'dk': (90, 65, 30, 255),
            'gem': (255, 200, 80, 255),
        },
        handle_colors={
            'base': (55, 30, 15, 255),
            'wrap': (120, 80, 40, 255),
            'wrap_alt': (85, 55, 25, 255),
        },
        curve_amt=3.0,
        extra_fn=ember_fx,
    )
    save(img, "unique_katana_sw")


# ============================================================
# GREATSHIELD VARIANTS
# ============================================================

def draw_greatshield_1():
    """Ironwall — steel/iron tower shield"""
    S = 32
    img, p = create(S)
    build_greatshield(p, S,
        face_colors={
            'hi': (180, 185, 195, 255),
            'mid': (140, 145, 158, 255),
            'dk': (95, 100, 115, 255),
        },
        trim_colors={
            'hi': (200, 205, 215, 255),
            'mid': (160, 165, 178, 255),
            'dk': (80, 85, 100, 255),
        },
        boss_colors={
            'hi': (210, 215, 225, 255),
            'mid': (165, 170, 185, 255),
            'dk': (110, 115, 130, 255),
        },
    )
    save(img, "unique_greatshield_1")

def draw_greatshield_2():
    """Aegis of Dawn — golden/white radiant shield"""
    S = 32
    img, p = create(S)

    def sun_emblem(p, S, cx, cy):
        # Small sun rays around boss
        sun = (255, 230, 140, 200)
        for angle in range(0, 360, 45):
            rad = angle * math.pi / 180
            for r in range(5, 7):
                gx = int(cx + r * math.cos(rad))
                gy = int(cy + r * math.sin(rad))
                px(p, gx, gy, sun, S)

    build_greatshield(p, S,
        face_colors={
            'hi': (255, 245, 220, 255),
            'mid': (235, 215, 170, 255),
            'dk': (195, 170, 120, 255),
        },
        trim_colors={
            'hi': (255, 230, 140, 255),
            'mid': (230, 200, 100, 255),
            'dk': (180, 150, 60, 255),
        },
        boss_colors={
            'hi': (255, 240, 180, 255),
            'mid': (240, 210, 120, 255),
            'dk': (200, 170, 70, 255),
        },
        emblem_fn=sun_emblem,
    )
    save(img, "unique_greatshield_2")

def draw_greatshield_sw():
    """Titan's Bulwark — dark bronze/obsidian legendary"""
    S = 32
    img, p = create(S)

    def titan_emblem(p, S, cx, cy):
        # Rune marks
        rune = (180, 140, 80, 220)
        for dy in range(-3, 4):
            px(p, cx - 7, cy + dy, rune, S)
            px(p, cx + 7, cy + dy, rune, S)
        for dx in range(-3, 4):
            px(p, cx + dx, cy - 8, rune, S)

    build_greatshield(p, S,
        face_colors={
            'hi': (130, 110, 80, 255),
            'mid': (95, 78, 52, 255),
            'dk': (60, 48, 30, 255),
        },
        trim_colors={
            'hi': (175, 145, 90, 255),
            'mid': (140, 115, 65, 255),
            'dk': (85, 65, 35, 255),
        },
        boss_colors={
            'hi': (200, 170, 110, 255),
            'mid': (160, 130, 75, 255),
            'dk': (110, 85, 45, 255),
        },
        emblem_fn=titan_emblem,
    )
    save(img, "unique_greatshield_sw")


# ============================================================
# RAPIER VARIANTS
# ============================================================

def draw_rapier_1():
    """Duelist's Sting — polished silver rapier"""
    S = 32
    img, p = create(S)
    build_rapier(p, S,
        blade_colors={
            'edge_hi': (225, 230, 240, 255),
            'mid_hi': (200, 208, 222, 255),
            'mid': (175, 182, 200, 255),
            'mid_dk': (145, 152, 172, 255),
            'edge_dk': (115, 122, 145, 255),
            'outline': (80, 85, 105, 255),
        },
        guard_colors={
            'hi': (210, 200, 180, 255),
            'mid': (175, 165, 142, 255),
            'dk': (130, 120, 100, 255),
            'gem': (180, 200, 255, 255),
        },
        handle_colors={
            'base': (70, 55, 35, 255),
            'wrap': (110, 90, 60, 255),
            'wrap_alt': (85, 68, 42, 255),
        },
    )
    save(img, "unique_rapier_1")

def draw_rapier_2():
    """Venomfang Foil — green/toxic rapier"""
    S = 32
    img, p = create(S)

    def venom_drip(p, S):
        px(p, 28, 3, (80, 200, 60, 140), S)
        px(p, 27, 5, (60, 180, 45, 110), S)
        px(p, 29, 2, (100, 220, 80, 100), S)

    build_rapier(p, S,
        blade_colors={
            'edge_hi': (180, 220, 170, 255),
            'mid_hi': (150, 195, 140, 255),
            'mid': (120, 170, 110, 255),
            'mid_dk': (90, 140, 80, 255),
            'edge_dk': (60, 108, 52, 255),
            'outline': (35, 70, 30, 255),
        },
        guard_colors={
            'hi': (120, 150, 100, 255),
            'mid': (85, 115, 68, 255),
            'dk': (55, 78, 42, 255),
            'gem': (140, 255, 100, 255),
        },
        handle_colors={
            'base': (40, 50, 30, 255),
            'wrap': (70, 90, 50, 255),
            'wrap_alt': (50, 65, 35, 255),
        },
        extra_fn=venom_drip,
    )
    save(img, "unique_rapier_2")

def draw_rapier_sw():
    """Silvered Estoc of the Court — ornate gold/silver legendary"""
    S = 32
    img, p = create(S)

    def royal_fx(p, S):
        # Sparkle on blade
        px(p, 26, 5, (255, 255, 230, 160), S)
        px(p, 22, 9, (255, 255, 220, 120), S)

    build_rapier(p, S,
        blade_colors={
            'edge_hi': (240, 238, 230, 255),
            'mid_hi': (220, 215, 205, 255),
            'mid': (195, 190, 180, 255),
            'mid_dk': (168, 162, 152, 255),
            'edge_dk': (138, 132, 122, 255),
            'outline': (95, 90, 80, 255),
        },
        guard_colors={
            'hi': (255, 225, 120, 255),
            'mid': (220, 190, 80, 255),
            'dk': (175, 145, 40, 255),
            'gem': (255, 100, 100, 255),
        },
        handle_colors={
            'base': (100, 70, 30, 255),
            'wrap': (160, 130, 60, 255),
            'wrap_alt': (130, 100, 45, 255),
        },
        extra_fn=royal_fx,
    )
    save(img, "unique_rapier_sw")


# ============================================================
# WHIP VARIANTS (16x16)
# ============================================================

def draw_whip_1():
    """Serpent's Lash — green/emerald whip"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()
    build_whip(p,
        lash_colors={
            'hi': (100, 180, 80, 255),
            'mid': (65, 140, 50, 255),
            'dk': (40, 95, 30, 255),
            'tip': (130, 210, 100, 255),
            'glow': (140, 230, 110, 150),
        },
        handle_colors={
            'base': (80, 60, 35, 255),
            'wrap': (110, 85, 50, 255),
            'wrap_alt': (65, 48, 28, 255),
            'pommel': (130, 180, 80, 255),
        },
        curve_style=0,
    )
    save(img, "unique_whip_1")

def draw_whip_2():
    """Flamelash — orange/fire whip"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()

    def fire_sparks(p):
        px16(p, 14, 2, (255, 200, 80, 130))
        px16(p, 13, 1, (255, 180, 60, 100))

    build_whip(p,
        lash_colors={
            'hi': (255, 200, 100, 255),
            'mid': (220, 140, 50, 255),
            'dk': (180, 90, 25, 255),
            'tip': (255, 230, 140, 255),
            'glow': (255, 180, 60, 150),
        },
        handle_colors={
            'base': (90, 50, 25, 255),
            'wrap': (130, 75, 35, 255),
            'wrap_alt': (75, 42, 20, 255),
            'pommel': (200, 120, 40, 255),
        },
        curve_style=1,
        extra_fn=fire_sparks,
    )
    save(img, "unique_whip_2")

def draw_whip_sw():
    """Thornwhip of the Verdant Warden — legendary nature whip"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()

    def thorns(p):
        # Thorn spikes along lash
        thorn_c = (160, 140, 70, 255)
        px16(p, 9, 7, thorn_c)
        px16(p, 11, 5, thorn_c)
        px16(p, 13, 4, thorn_c)
        # Leaf accent
        px16(p, 10, 8, (80, 170, 55, 200))

    build_whip(p,
        lash_colors={
            'hi': (90, 150, 60, 255),
            'mid': (60, 120, 40, 255),
            'dk': (35, 85, 22, 255),
            'tip': (120, 190, 80, 255),
            'glow': (100, 200, 70, 140),
        },
        handle_colors={
            'base': (95, 70, 35, 255),
            'wrap': (130, 100, 55, 255),
            'wrap_alt': (70, 52, 28, 255),
            'pommel': (160, 140, 70, 255),
        },
        curve_style=2,
        extra_fn=thorns,
    )
    save(img, "unique_whip_sw")


# ============================================================
# WAND VARIANTS (16x16)
# ============================================================

def draw_wand_1():
    """Arcane Focus — purple/arcane wand"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()
    build_wand(p,
        shaft_colors={
            'hi': (130, 100, 160, 255),
            'mid': (100, 72, 130, 255),
            'dk': (65, 45, 90, 255),
            'wrap': (150, 120, 180, 255),
        },
        tip_colors={
            'core': (230, 200, 255, 255),
            'bright': (200, 160, 240, 255),
            'mid': (160, 120, 200, 255),
            'dk': (110, 75, 155, 255),
            'glow': (190, 150, 240, 120),
        },
    )
    save(img, "unique_wand_1")

def draw_wand_2():
    """Frostfinger — ice/blue wand"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()
    build_wand(p,
        shaft_colors={
            'hi': (140, 170, 200, 255),
            'mid': (100, 135, 175, 255),
            'dk': (60, 90, 135, 255),
            'wrap': (160, 190, 220, 255),
        },
        tip_colors={
            'core': (230, 245, 255, 255),
            'bright': (180, 220, 250, 255),
            'mid': (130, 180, 230, 255),
            'dk': (80, 130, 190, 255),
            'glow': (160, 210, 255, 120),
        },
    )
    save(img, "unique_wand_2")

def draw_wand_sw():
    """Star Conduit — legendary gold/celestial wand"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()

    def star_burst(p):
        px16(p, 14, 0, (255, 255, 200, 140))
        px16(p, 12, 0, (255, 250, 180, 100))
        px16(p, 15, 1, (255, 240, 160, 80))

    build_wand(p,
        shaft_colors={
            'hi': (200, 180, 120, 255),
            'mid': (165, 140, 80, 255),
            'dk': (120, 100, 50, 255),
            'wrap': (220, 200, 140, 255),
        },
        tip_colors={
            'core': (255, 255, 230, 255),
            'bright': (255, 240, 180, 255),
            'mid': (255, 220, 130, 255),
            'dk': (220, 185, 80, 255),
            'glow': (255, 240, 150, 140),
        },
        extra_fn=star_burst,
    )
    save(img, "unique_wand_sw")


# ============================================================
# THROWING AXE VARIANTS (16x16)
# ============================================================

def draw_throwing_axe_1():
    """Stormhatchet — steel/blue electric axe"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()

    def spark(p):
        px16(p, 14, 2, (200, 220, 255, 140))
        px16(p, 13, 1, (180, 210, 255, 100))

    build_throwing_axe(p,
        head_colors={
            'hi': (190, 200, 220, 255),
            'mid': (150, 162, 185, 255),
            'dk': (105, 115, 140, 255),
            'outline': (70, 78, 100, 255),
        },
        handle_colors={
            'hi': (140, 120, 85, 255),
            'mid': (110, 92, 60, 255),
            'dk': (75, 60, 38, 255),
            'wrap': (170, 150, 110, 255),
        },
        extra_fn=spark,
    )
    save(img, "unique_throwing_axe_1")

def draw_throwing_axe_2():
    """Frostbite Hatchet — ice/frost axe"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()

    def frost(p):
        px16(p, 14, 3, (200, 230, 255, 120))
        px16(p, 12, 1, (180, 220, 250, 90))

    build_throwing_axe(p,
        head_colors={
            'hi': (190, 215, 235, 255),
            'mid': (145, 175, 205, 255),
            'dk': (95, 125, 165, 255),
            'outline': (55, 80, 120, 255),
        },
        handle_colors={
            'hi': (130, 115, 100, 255),
            'mid': (100, 85, 68, 255),
            'dk': (68, 55, 40, 255),
            'wrap': (160, 145, 125, 255),
        },
        extra_fn=frost,
    )
    save(img, "unique_throwing_axe_2")

def draw_throwing_axe_sw():
    """Galeforce Tomahawk — legendary wind/green axe"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()

    def wind_trail(p):
        px16(p, 14, 2, (180, 230, 180, 130))
        px16(p, 15, 3, (160, 220, 160, 90))
        px16(p, 13, 1, (200, 240, 200, 100))

    build_throwing_axe(p,
        head_colors={
            'hi': (180, 210, 180, 255),
            'mid': (140, 175, 140, 255),
            'dk': (95, 130, 95, 255),
            'outline': (55, 82, 55, 255),
        },
        handle_colors={
            'hi': (150, 130, 90, 255),
            'mid': (120, 100, 62, 255),
            'dk': (80, 65, 35, 255),
            'wrap': (175, 155, 110, 255),
        },
        extra_fn=wind_trail,
    )
    save(img, "unique_throwing_axe_sw")


# ============================================================
# MAIN
# ============================================================

if __name__ == '__main__':
    print("=== Regenerating Batch 2 Arsenal Weapons ===\n")

    print("--- Katanas (32x32) ---")
    draw_katana_1()
    draw_katana_2()
    draw_katana_sw()

    print("\n--- Greatshields (32x32) ---")
    draw_greatshield_1()
    draw_greatshield_2()
    draw_greatshield_sw()

    print("\n--- Rapiers (32x32) ---")
    draw_rapier_1()
    draw_rapier_2()
    draw_rapier_sw()

    print("\n--- Whips (16x16) ---")
    draw_whip_1()
    draw_whip_2()
    draw_whip_sw()

    print("\n--- Wands (16x16) ---")
    draw_wand_1()
    draw_wand_2()
    draw_wand_sw()

    print("\n--- Throwing Axes (16x16) ---")
    draw_throwing_axe_1()
    draw_throwing_axe_2()
    draw_throwing_axe_sw()

    print("\n=== Done! 18 textures regenerated. ===")
