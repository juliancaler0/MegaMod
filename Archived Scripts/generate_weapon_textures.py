"""Generate all 10 RPG weapon textures for MegaMod - 16x16 RGBA PNGs."""
import struct, zlib, os

OUT_DIR = "src/main/resources/assets/megamod/textures/item"

def write_png(path, pixels):
    """Write a 16x16 RGBA PNG from a 16x16 list of (r,g,b,a) tuples."""
    w, h = 16, 16
    raw = b''
    for y in range(h):
        raw += b'\x00'  # filter byte
        for x in range(w):
            r, g, b, a = pixels[y][x]
            raw += struct.pack('BBBB', r, g, b, a)

    def chunk(ctype, data):
        c = ctype + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xFFFFFFFF)

    ihdr = struct.pack('>IIBBBBB', w, h, 8, 6, 0, 0, 0)
    idat = zlib.compress(raw)

    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        f.write(chunk(b'IHDR', ihdr))
        f.write(chunk(b'IDAT', idat))
        f.write(chunk(b'IEND', b''))
    print(f"  Written: {path}")

def lerp(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(len(c1)))

def make_blank():
    return [[(0,0,0,0) for _ in range(16)] for _ in range(16)]

def set_px(img, x, y, c):
    if 0 <= x < 16 and 0 <= y < 16:
        img[y][x] = c

def fill_rect(img, x1, y1, x2, y2, c):
    for yy in range(y1, y2):
        for xx in range(x1, x2):
            set_px(img, xx, yy, c)

def bevel_rect(img, x1, y1, x2, y2, base, light, dark):
    fill_rect(img, x1, y1, x2, y2, base)
    for xx in range(x1, x2):
        set_px(img, xx, y1, light)
    for yy in range(y1, y2):
        set_px(img, x1, yy, light)
    for xx in range(x1, x2):
        set_px(img, xx, y2-1, dark)
    for yy in range(y1, y2):
        set_px(img, x2-1, yy, dark)

# ============================================================
# 1. LUNAR CROWN - Silver/blue crescent moon crown
# ============================================================
def gen_lunar_crown():
    img = make_blank()
    silver = (160, 168, 184, 255)
    silver_light = (192, 200, 216, 255)
    silver_dark = (112, 120, 136, 255)
    moon_bright = (192, 208, 232, 255)
    moon_mid = (160, 184, 216, 255)
    moon_dark = (136, 152, 176, 255)
    moonstone = (208, 224, 255, 255)
    moonstone_glow = (180, 200, 248, 255)
    deep = (72, 80, 96, 255)

    # Crescent horns (rows 0-3, cols 1-2 left, 9-10 right)
    for y in range(4):
        t = y / 3.0
        c = lerp(moon_bright, moon_mid, t)
        set_px(img, 1, y, c)
        set_px(img, 2, y, lerp(moon_mid, moon_dark, t))
        set_px(img, 9, y, c)
        set_px(img, 10, y, lerp(moon_mid, moon_dark, t))

    # Crescent base (rows 4-5)
    for x in range(3, 9):
        t = (x - 3) / 5.0
        set_px(img, x, 4, lerp(moon_bright, moon_mid, t))
        set_px(img, x, 5, lerp(moon_mid, moon_dark, t))

    # Crown points (rows 6-9)
    for y in range(6, 10):
        t = (y - 6) / 3.0
        # Left point
        set_px(img, 0, y, lerp(silver_light, silver, t))
        set_px(img, 1, y, lerp(silver, silver_dark, t))
        # Center point (taller)
        for x in range(5, 8):
            set_px(img, x, y, lerp(silver_light, silver, t + (x-5)*0.1))
        # Right point
        set_px(img, 10, y, lerp(silver_light, silver, t))
        set_px(img, 11, y, lerp(silver, silver_dark, t))

    # Crown band (rows 10-12)
    bevel_rect(img, 0, 10, 12, 13, silver, silver_light, silver_dark)
    # Band pattern
    for x in range(1, 11):
        if (x + 0) % 3 == 0:
            set_px(img, x, 11, deep)

    # Moonstones (row 13-14)
    for dx in range(2):
        set_px(img, 6 + dx, 13, moonstone)
        set_px(img, 6 + dx, 14, moonstone_glow)
    # Side gems
    set_px(img, 13, 13, moonstone)
    set_px(img, 13, 14, moonstone_glow)
    set_px(img, 14, 13, moonstone_glow)
    set_px(img, 14, 14, moonstone)

    return img

# ============================================================
# 2. SOLAR CROWN - Gold sun-ray crown
# ============================================================
def gen_solar_crown():
    img = make_blank()
    gold = (255, 215, 0, 255)
    gold_light = (255, 238, 136, 255)
    gold_mid = (218, 165, 32, 255)
    gold_dark = (200, 168, 76, 255)
    gold_shadow = (139, 105, 20, 255)
    orange = (255, 165, 0, 255)
    sunstone = (255, 238, 136, 255)
    red_accent = (204, 80, 0, 255)

    # Sun disc (rows 0-3, cols 4-7)
    bevel_rect(img, 4, 0, 8, 4, gold_light, (255,250,220,255), gold)
    set_px(img, 5, 1, (255,255,200,255))
    set_px(img, 6, 2, (255,255,180,255))

    # Sun ray tips (rows 2-9)
    # Center tall ray
    for y in range(2, 10):
        t = (y - 2) / 7.0
        for x in range(4, 7):
            c = lerp(gold_light, gold_mid, t + (x-4)*0.05)
            set_px(img, x, y, c)

    # Left ray
    for y in range(5, 10):
        t = (y - 5) / 4.0
        set_px(img, 1, y, lerp(gold, gold_mid, t))
        set_px(img, 2, y, lerp(gold_light, gold_dark, t))
        set_px(img, 3, y, lerp(gold, gold_shadow, t))

    # Right ray
    for y in range(5, 10):
        t = (y - 5) / 4.0
        set_px(img, 8, y, lerp(gold, gold_mid, t))
        set_px(img, 9, y, lerp(gold_light, gold_dark, t))
        set_px(img, 10, y, lerp(gold, gold_shadow, t))

    # Side rays (shorter)
    for y in range(7, 10):
        t = (y - 7) / 2.0
        set_px(img, 0, y, lerp(gold, gold_shadow, t))
        set_px(img, 1, y, lerp(gold_mid, gold_shadow, t))
        set_px(img, 11, y, lerp(gold, gold_shadow, t))
        set_px(img, 12, y, lerp(gold_mid, gold_shadow, t))

    # Back ridge (row 8-9)
    for x in range(3, 9):
        set_px(img, x, 8, gold_dark)
        set_px(img, x, 9, gold_shadow)

    # Crown band (rows 10-12)
    bevel_rect(img, 0, 10, 12, 13, gold_mid, gold, gold_shadow)
    for x in range(1, 11):
        if x % 2 == 0:
            set_px(img, x, 11, orange)

    # Sunstone gems (rows 14-15)
    set_px(img, 6, 14, sunstone)
    set_px(img, 7, 14, (255,255,220,255))
    set_px(img, 6, 15, (255,220,100,255))
    set_px(img, 7, 15, sunstone)

    return img

# ============================================================
# 3. VAMPIRIC TOME - Dark crimson leather grimoire
# ============================================================
def gen_vampiric_tome():
    img = make_blank()
    cover_dark = (58, 8, 8, 255)
    cover = (80, 16, 16, 255)
    cover_light = (106, 24, 24, 255)
    page_light = (240, 224, 192, 255)
    page = (232, 208, 168, 255)
    page_edge = (208, 184, 136, 255)
    clasp_metal = (128, 128, 128, 255)
    clasp_dark = (96, 96, 96, 255)
    blood_red = (204, 0, 0, 255)
    blood_glow = (255, 32, 32, 255)
    spine_dark = (40, 4, 4, 255)
    gold_accent = (180, 140, 40, 255)
    ribbon = (180, 0, 0, 255)

    # Cover background (cols 1-9, rows 1-14)
    for y in range(1, 15):
        for x in range(1, 9):
            t = ((x * 7 + y * 3) % 5) / 8.0
            c = lerp(cover, cover_dark, t)
            set_px(img, x, y, c)
    # Top/bottom edges
    for x in range(1, 9):
        set_px(img, x, 1, cover_light)
        set_px(img, x, 14, cover_dark)

    # Spine (col 0)
    for y in range(1, 15):
        set_px(img, 0, y, spine_dark)

    # Pages visible (cols 9-11, rows 2-13)
    for y in range(2, 14):
        t = (y - 2) / 11.0
        set_px(img, 9, y, page_edge)
        set_px(img, 10, y, lerp(page, page_light, abs(t-0.5)*2))
        set_px(img, 11, y, page_light)

    # Corner reinforcements (gold)
    set_px(img, 0, 0, gold_accent)
    set_px(img, 1, 0, gold_accent)
    set_px(img, 0, 15, gold_accent)
    set_px(img, 1, 15, gold_accent)

    # Clasp (cols 10-12, rows 5-9)
    for y in range(5, 10):
        set_px(img, 10, y, clasp_metal)
        set_px(img, 11, y, clasp_dark)
        set_px(img, 12, y, clasp_metal)
    # Fang details
    set_px(img, 11, 3, clasp_metal)
    set_px(img, 11, 4, clasp_dark)
    set_px(img, 11, 10, clasp_dark)
    set_px(img, 11, 11, clasp_metal)

    # Blood eye emblem (cols 12-15, rows 6-9)
    set_px(img, 12, 6, cover_light)
    set_px(img, 13, 6, blood_red)
    set_px(img, 14, 6, blood_red)
    set_px(img, 15, 6, cover_light)
    set_px(img, 12, 7, blood_red)
    set_px(img, 13, 7, blood_glow)
    set_px(img, 14, 7, blood_glow)
    set_px(img, 15, 7, blood_red)
    set_px(img, 12, 8, blood_red)
    set_px(img, 13, 8, blood_glow)
    set_px(img, 14, 8, blood_glow)
    set_px(img, 15, 8, blood_red)
    set_px(img, 12, 9, cover_light)
    set_px(img, 13, 9, blood_red)
    set_px(img, 14, 9, blood_red)
    set_px(img, 15, 9, cover_light)

    # Eye pupil
    set_px(img, 14, 14, (20, 0, 0, 255))
    set_px(img, 14, 15, blood_glow)

    # Bookmark ribbon
    set_px(img, 14, 0, ribbon)
    set_px(img, 14, 1, (150, 0, 0, 255))

    return img

# ============================================================
# 4. STATIC SEEKER - Electric tesla-coil wand
# ============================================================
def gen_static_seeker():
    img = make_blank()
    copper = (184, 115, 51, 255)
    copper_light = (210, 150, 80, 255)
    copper_dark = (139, 90, 43, 255)
    wire_gold = (218, 165, 32, 255)
    crystal_bright = (128, 240, 255, 255)
    crystal = (0, 204, 255, 255)
    crystal_dark = (0, 140, 200, 255)
    spark = (255, 255, 128, 255)
    spark_white = (255, 255, 255, 255)
    grip_dark = (58, 26, 10, 255)
    grip = (90, 58, 42, 255)
    coil_metal = (160, 140, 100, 255)

    # Lightning crystal & sparks (rows 0-3)
    # Prong areas (cols 0-1 left, 9-10 right)
    for y in range(6):
        set_px(img, 0, y, copper_dark)
        set_px(img, 1, y, copper)
        set_px(img, 9, y, copper)
        set_px(img, 10, y, copper_dark)
    # Crystal (cols 5-6)
    for y in range(4):
        t = y / 3.0
        set_px(img, 5, y, lerp(crystal_bright, crystal, t))
        set_px(img, 6, y, lerp(crystal, crystal_dark, t))
    # Spark tips
    set_px(img, 12, 0, spark)
    set_px(img, 13, 0, spark_white)
    set_px(img, 12, 1, spark_white)
    set_px(img, 13, 1, spark)
    set_px(img, 12, 2, spark)

    # Coil ring (rows 2-3)
    for x in range(2, 7):
        set_px(img, x, 2, coil_metal)
        set_px(img, x, 3, wire_gold)

    # Coil base / capacitor (rows 4-7)
    for y in range(4, 8):
        for x in range(3, 7):
            t = ((x + y) % 2) / 3.0
            set_px(img, x, y, lerp(copper, wire_gold, t))
    # Capacitor glow
    set_px(img, 4, 5, crystal)
    set_px(img, 5, 5, crystal_bright)
    set_px(img, 4, 6, crystal_dark)
    set_px(img, 5, 6, crystal)

    # Shaft (rows 6-10, cols 7-8)
    for y in range(6, 11):
        t = (y - 6) / 4.0
        set_px(img, 7, y, lerp(copper_light, copper, t))
        set_px(img, 8, y, lerp(copper, copper_dark, t))

    # Shaft lower (rows 11-15)
    for y in range(11, 16):
        t = (y - 11) / 4.0
        set_px(img, 7, y, lerp(copper, copper_dark, t))
        set_px(img, 8, y, lerp(copper_dark, grip_dark, t))

    # Grip wrap (rows 12-15, cols 4-6)
    for y in range(12, 16):
        for x in range(4, 7):
            stripe = ((x + y) % 2 == 0)
            set_px(img, x, y, grip if stripe else grip_dark)

    return img

# ============================================================
# 5. BATTLEDANCER - Elegant curved sword with red silk grip
# ============================================================
def gen_battledancer():
    img = make_blank()
    blade_bright = (232, 240, 248, 255)
    blade = (208, 216, 224, 255)
    blade_mid = (176, 184, 200, 255)
    blade_dark = (144, 152, 168, 255)
    edge = (248, 252, 255, 255)
    guard_gold = (200, 168, 76, 255)
    guard_bright = (218, 185, 100, 255)
    guard_dark = (139, 105, 20, 255)
    grip_red = (204, 34, 34, 255)
    grip_dark = (170, 24, 24, 255)
    pommel = (176, 176, 176, 255)
    fuller_dark = (100, 108, 120, 255)
    tassel_red = (180, 20, 20, 255)

    # Blade tip (rows 0-2)
    for y in range(3):
        t = y / 2.0
        set_px(img, 7, y, edge)
        set_px(img, 8, y, lerp(blade_bright, blade, t))
        set_px(img, 9, y, lerp(blade, blade_mid, t))

    # Blade upper curve (rows 2-5)
    for y in range(2, 6):
        t = (y - 2) / 3.0
        for x in range(3, 7):
            tx = (x - 3) / 3.0
            set_px(img, x, y, lerp(edge, blade_mid, tx * 0.5 + t * 0.3))

    # Edge sweep
    for y in range(1, 5):
        set_px(img, 0, y, edge)
        set_px(img, 1, y, blade_bright)

    # Blade mid (rows 2-5)
    for y in range(2, 6):
        t = (y - 2) / 3.0
        for x in range(3, 7):
            set_px(img, x, y, lerp(blade_bright, blade_mid, t))

    # Blade lower (rows 5-9)
    for y in range(5, 9):
        t = (y - 5) / 3.0
        set_px(img, 4, y, lerp(blade_bright, blade, t))
        set_px(img, 5, y, lerp(blade, blade_mid, t))
        set_px(img, 6, y, lerp(blade_mid, blade_dark, t))

    # Fuller groove (col 11, rows 3-8)
    for y in range(3, 9):
        set_px(img, 11, y, fuller_dark)

    # Guard curl (rows 7-9)
    for y in range(7, 10):
        t = (y - 7) / 2.0
        set_px(img, 0, y, lerp(guard_bright, guard_dark, t))
        set_px(img, 1, y, lerp(guard_gold, guard_dark, t))
        set_px(img, 9, y, lerp(guard_bright, guard_dark, t))
        set_px(img, 10, y, lerp(guard_gold, guard_dark, t))

    # Guard center (row 9, cols 1-7)
    for x in range(1, 8):
        t = x / 7.0
        set_px(img, x, 9, lerp(guard_bright, guard_gold, t))
    set_px(img, 8, 9, guard_dark)

    # Grip (rows 10-14)
    for y in range(10, 15):
        stripe = (y % 2 == 0)
        set_px(img, 5, y, grip_red if stripe else grip_dark)
        set_px(img, 6, y, grip_dark if stripe else grip_red)

    # Pommel (rows 14-15)
    for x in range(4, 7):
        set_px(img, x, 14, pommel)
        set_px(img, x, 15, (140, 140, 140, 255))

    # Tassel (cols 13, rows 12-13)
    set_px(img, 13, 12, tassel_red)
    set_px(img, 13, 13, (150, 10, 10, 255))

    return img

# ============================================================
# 6. EBONCHILL - Dark ice crystal staff
# ============================================================
def gen_ebonchill():
    img = make_blank()
    obsidian = (26, 26, 46, 255)
    obsidian_light = (45, 45, 68, 255)
    obsidian_dark = (16, 16, 30, 255)
    ice_bright = (204, 255, 255, 255)
    ice = (136, 221, 255, 255)
    ice_mid = (68, 187, 238, 255)
    ice_dark = (0, 170, 221, 255)
    frost = (102, 170, 204, 255)
    frost_light = (170, 220, 238, 255)
    grip_blue = (15, 52, 96, 255)

    # Crystal cluster (rows 0-4)
    # Main crystal
    for y in range(5):
        t = y / 4.0
        for x in range(5):
            tx = x / 4.0
            c = lerp(ice_bright, ice, t * 0.6 + tx * 0.4)
            set_px(img, x, y, c)
    # Crystal center glow
    set_px(img, 2, 1, ice_bright)
    set_px(img, 2, 2, (230, 255, 255, 255))
    set_px(img, 3, 2, ice_bright)

    # Crystal spikes (cols 10-11)
    for y in range(1, 4):
        t = (y - 1) / 2.0
        set_px(img, 10, y, lerp(ice_bright, ice, t))
        set_px(img, 11, y, lerp(ice, ice_mid, t))

    # Frost ring (row 5)
    for x in range(3, 7):
        set_px(img, x, 5, frost_light)

    # Icicle drips
    set_px(img, 12, 5, frost)
    set_px(img, 12, 6, frost_light)
    set_px(img, 14, 5, frost)
    set_px(img, 14, 6, frost_light)

    # Shaft upper (rows 5-10, cols 7-8)
    for y in range(5, 11):
        t = (y - 5) / 5.0
        set_px(img, 7, y, lerp(obsidian_light, obsidian, t))
        set_px(img, 8, y, lerp(obsidian, obsidian_dark, t))
        # Frost accent
        if (y + 7) % 3 == 0:
            set_px(img, 7, y, frost)

    # Shaft lower (rows 11-15)
    for y in range(11, 16):
        t = (y - 11) / 4.0
        set_px(img, 7, y, lerp(obsidian, obsidian_dark, t))
        set_px(img, 8, y, lerp(obsidian_dark, (10,10,20,255), t))

    # Grip band (rows 14-15, cols 4-6)
    for y in range(14, 16):
        for x in range(4, 7):
            stripe = (x + y) % 2 == 0
            set_px(img, x, y, grip_blue if stripe else obsidian_light)

    return img

# ============================================================
# 7. LIGHTBINDER - Holy golden scepter with sun disc
# ============================================================
def gen_lightbinder():
    img = make_blank()
    gold = (218, 165, 32, 255)
    gold_light = (255, 215, 0, 255)
    gold_bright = (255, 238, 68, 255)
    gold_dark = (200, 168, 76, 255)
    gold_shadow = (139, 105, 20, 255)
    sun_white = (255, 255, 240, 255)
    sun_cream = (255, 248, 220, 255)
    halo = (255, 228, 181, 255)
    gem_white = (255, 255, 255, 255)

    # Sun disc (rows 0-4, cols 0-4)
    for y in range(5):
        for x in range(5):
            dist = abs(x - 2) + abs(y - 2)
            if dist <= 2:
                t = dist / 2.0
                set_px(img, x, y, lerp(sun_white, gold_bright, t))
            elif dist == 3:
                set_px(img, x, y, gold_light)
    # Sun center
    set_px(img, 2, 2, (255, 255, 255, 255))

    # Rays (cols 10-11, rows 1-2)
    set_px(img, 10, 1, gold_light)
    set_px(img, 11, 1, gold_bright)
    set_px(img, 10, 2, gold_bright)
    set_px(img, 11, 2, gold_light)

    # Bottom ray
    set_px(img, 10, 4, gold)
    set_px(img, 11, 4, gold_light)
    set_px(img, 10, 5, gold_dark)
    set_px(img, 11, 5, gold)

    # Halo bar (row 6)
    for x in range(8):
        t = x / 7.0
        set_px(img, x, 6, lerp(halo, gold, t))

    # Shaft upper (rows 4-10, cols 7-8)
    for y in range(4, 11):
        t = (y - 4) / 6.0
        set_px(img, 7, y, lerp(gold_light, gold, t))
        set_px(img, 8, y, lerp(gold, gold_dark, t))

    # Shaft ornament band (row 9, cols 4-6)
    for x in range(4, 7):
        set_px(img, x, 9, gold_bright)
        set_px(img, x, 10, gold_shadow)

    # Shaft lower (rows 11-15)
    for y in range(11, 16):
        t = (y - 11) / 4.0
        set_px(img, 7, y, lerp(gold, gold_dark, t))
        set_px(img, 8, y, lerp(gold_dark, gold_shadow, t))

    # Grip wrap (rows 13-15, cols 4-6)
    for y in range(13, 16):
        for x in range(4, 7):
            stripe = (x + y) % 2 == 0
            set_px(img, x, y, sun_cream if stripe else gold)

    # Core gem (rows 13-14, cols 13-14)
    set_px(img, 13, 13, gem_white)
    set_px(img, 14, 13, sun_white)
    set_px(img, 13, 14, sun_white)
    set_px(img, 14, 14, gold_bright)

    return img

# ============================================================
# 8. CRESCENT BLADE - Moon-curved polearm scythe
# ============================================================
def gen_crescent_blade():
    img = make_blank()
    blade_body = (80, 88, 104, 255)
    blade_light = (96, 104, 120, 255)
    blade_dark = (64, 72, 80, 255)
    blade_edge = (192, 208, 224, 255)
    blade_edge_bright = (216, 224, 232, 255)
    handle_dark = (58, 26, 10, 255)
    handle = (90, 58, 42, 255)
    handle_light = (139, 90, 43, 255)
    guard_metal = (112, 120, 128, 255)
    guard_dark = (80, 88, 96, 255)
    rune_teal = (0, 204, 160, 255)
    rune_bright = (0, 255, 200, 255)
    gem_teal = (0, 170, 128, 255)

    # Crescent blade tips (rows 0-4)
    # Left tip
    for y in range(5):
        t = y / 4.0
        set_px(img, 0, y, lerp(blade_edge_bright, blade_edge, t))
        set_px(img, 1, y, lerp(blade_edge, blade_body, t))

    # Blade body inner (rows 1-5, cols 0-3)
    for y in range(1, 6):
        for x in range(4):
            t = ((x + y) % 3) / 4.0
            set_px(img, x, y, lerp(blade_edge, blade_body, t + x * 0.15))

    # Blade body outer (rows 1-5, cols 8-11)
    for y in range(1, 6):
        for x in range(8, 12):
            t = ((x + y) % 3) / 4.0
            set_px(img, x, y, lerp(blade_body, blade_dark, t))

    # Right tip (rows 0-4, cols 12-13)
    for y in range(5):
        t = y / 4.0
        set_px(img, 12, y, lerp(blade_edge_bright, blade_edge, t))
        set_px(img, 13, y, lerp(blade_edge, blade_body, t))

    # Blade spine (rows 3-7, cols 5-6)
    for y in range(3, 8):
        t = (y - 3) / 4.0
        set_px(img, 5, y, lerp(blade_light, blade_body, t))
        set_px(img, 6, y, lerp(blade_body, blade_dark, t))

    # Shaft (rows 6-11, cols 7-8)
    for y in range(6, 12):
        t = (y - 6) / 5.0
        set_px(img, 7, y, lerp(handle_light, handle, t))
        set_px(img, 8, y, lerp(handle, handle_dark, t))

    # Guard (row 11, cols 3-8)
    for x in range(3, 9):
        set_px(img, x, 11, guard_metal)
    set_px(img, 1, 11, guard_dark)
    set_px(img, 9, 11, guard_dark)

    # Handle wrap (rows 12-15, cols 4-6 and 7-8)
    for y in range(12, 16):
        t = (y - 12) / 3.0
        set_px(img, 7, y, lerp(handle_light, handle, t))
        set_px(img, 8, y, lerp(handle, handle_dark, t))
        # Wrap pattern
        for x in range(4, 7):
            stripe = (x + y) % 2 == 0
            set_px(img, x, y, handle_light if stripe else handle_dark)

    # Rune gems
    set_px(img, 13, 9, rune_teal)
    set_px(img, 14, 9, rune_bright)
    set_px(img, 13, 10, rune_bright)
    set_px(img, 14, 10, rune_teal)

    # Pommel gem
    set_px(img, 13, 14, gem_teal)
    set_px(img, 14, 14, rune_bright)
    set_px(img, 13, 15, rune_bright)
    set_px(img, 14, 15, gem_teal)

    return img

# ============================================================
# 9. GHOST FANG - Spectral ethereal dagger
# ============================================================
def gen_ghost_fang():
    img = make_blank()
    bone = (232, 220, 200, 255)
    bone_light = (240, 232, 216, 255)
    bone_dark = (208, 192, 168, 255)
    bone_shadow = (192, 176, 152, 255)
    blade_ghost = (136, 204, 187, 255)
    blade_light = (170, 221, 204, 255)
    blade_bright = (204, 255, 238, 255)
    blade_dark = (102, 153, 136, 255)
    wisp_bright = (136, 255, 238, 255)
    wisp = (102, 238, 221, 255)
    wisp_faint = (68, 204, 187, 200)
    spirit_pink = (255, 68, 136, 255)
    spirit_dark = (204, 34, 102, 255)

    # Fang blade tip (rows 0-3)
    for y in range(4):
        t = y / 3.0
        set_px(img, 2, y, lerp(blade_bright, blade_light, t))
        set_px(img, 3, y, lerp(blade_light, blade_ghost, t))
        set_px(img, 4, y, lerp(blade_ghost, blade_dark, t))
    set_px(img, 3, 0, blade_bright)

    # Fang blade wide (rows 2-5)
    for y in range(2, 6):
        t = (y - 2) / 3.0
        for x in range(1, 6):
            tx = (x - 1) / 4.0
            c = lerp(blade_bright, blade_dark, tx * 0.5 + t * 0.3)
            set_px(img, x, y, c)
    # Edge highlight
    for y in range(2, 6):
        set_px(img, 0, y, blade_bright)
    set_px(img, 6, 2, blade_dark)
    set_px(img, 6, 3, blade_dark)

    # Blade base (rows 5-9)
    for y in range(5, 10):
        t = (y - 5) / 4.0
        set_px(img, 4, y, lerp(blade_light, blade_ghost, t))
        set_px(img, 5, y, lerp(blade_ghost, blade_dark, t))
        set_px(img, 6, y, lerp(blade_dark, bone_shadow, t))

    # Guard fangs (rows 9-10)
    set_px(img, 3, 9, bone_light)
    set_px(img, 4, 9, bone)
    set_px(img, 9, 9, bone)
    set_px(img, 10, 9, bone_light)
    set_px(img, 3, 10, bone)
    set_px(img, 4, 10, bone_dark)
    set_px(img, 9, 10, bone_dark)
    set_px(img, 10, 10, bone)

    # Bone handle (rows 10-15)
    for y in range(10, 16):
        t = (y - 10) / 5.0
        set_px(img, 7, y, lerp(bone_light, bone, t))
        set_px(img, 8, y, lerp(bone, bone_dark, t))
    # Knuckle
    for x in range(4, 7):
        set_px(img, x, 13, bone_light)
        set_px(img, x, 14, bone_dark)

    # Wisps (ghostly ethereal effects)
    # Left wisp (cols 11-12, rows 4-6)
    set_px(img, 11, 4, wisp)
    set_px(img, 12, 4, wisp_bright)
    set_px(img, 11, 5, wisp_bright)
    set_px(img, 12, 5, wisp_faint)
    set_px(img, 11, 6, wisp_faint)

    # Right wisp (cols 13-14, rows 4-6)
    set_px(img, 13, 4, wisp_faint)
    set_px(img, 14, 4, wisp)
    set_px(img, 13, 5, wisp)
    set_px(img, 14, 5, wisp_bright)
    set_px(img, 13, 6, wisp_bright)
    set_px(img, 14, 6, wisp_faint)

    # Top wisp
    set_px(img, 12, 1, wisp_faint)
    set_px(img, 13, 1, wisp)
    set_px(img, 12, 2, wisp)
    set_px(img, 13, 2, wisp_bright)

    # Spirit eye pommel
    set_px(img, 14, 14, spirit_pink)
    set_px(img, 15, 14, spirit_dark)
    set_px(img, 14, 15, spirit_dark)
    set_px(img, 15, 15, spirit_pink)

    return img

# ============================================================
# 10. TERRA WARHAMMER - Earth/nature massive hammer
# ============================================================
def gen_terra_warhammer():
    img = make_blank()
    stone = (128, 128, 112, 255)
    stone_light = (152, 152, 136, 255)
    stone_dark = (104, 104, 88, 255)
    stone_shadow = (80, 80, 64, 255)
    glow_amber = (221, 170, 68, 255)
    glow_bright = (255, 204, 68, 255)
    glow_dark = (204, 136, 0, 255)
    wood = (107, 66, 38, 255)
    wood_light = (139, 90, 43, 255)
    wood_dark = (74, 42, 26, 255)
    vine_green = (34, 139, 34, 255)
    vine_dark = (27, 94, 32, 255)
    vine_light = (50, 160, 50, 255)
    leaf = (46, 125, 50, 255)
    crystal_amber = (221, 170, 68, 255)
    root_dark = (58, 26, 10, 255)

    # Hammer head (rows 0-4, cols 0-7)
    for y in range(5):
        for x in range(8):
            t = ((x * 3 + y * 7) % 5) / 6.0
            c = lerp(stone_light, stone_dark, t)
            set_px(img, x, y, c)
    # Beveled edges
    for x in range(8):
        set_px(img, x, 0, stone_light)
    for y in range(5):
        set_px(img, 0, y, stone_light)
        set_px(img, 7, y, stone_shadow)
    for x in range(8):
        set_px(img, x, 4, stone_shadow)

    # Hammer face detail (cols 9-12, rows 0-3)
    for y in range(4):
        for x in range(9, 13):
            t = ((x + y) % 3) / 4.0
            set_px(img, x, y, lerp(stone, stone_dark, t))
    # Face front
    set_px(img, 10, 0, stone_light)
    set_px(img, 11, 1, stone_light)

    # Earth crystal embedded (cols 12-13, rows 6-7)
    set_px(img, 12, 6, glow_bright)
    set_px(img, 13, 6, glow_amber)
    set_px(img, 12, 7, glow_amber)
    set_px(img, 13, 7, glow_dark)

    # Shaft collar (rows 5-6)
    for x in range(3, 6):
        set_px(img, x, 5, stone)
        set_px(img, x, 6, stone_dark)
        set_px(img, x, 7, stone_shadow)

    # Crack glow (rows 8-9, cols 10-15)
    for x in range(10, 16):
        set_px(img, x, 8, glow_amber)
        set_px(img, x, 9, glow_bright)

    # Shaft (rows 5-12, cols 7-8)
    for y in range(5, 13):
        t = (y - 5) / 7.0
        set_px(img, 7, y, lerp(wood_light, wood, t))
        set_px(img, 8, y, lerp(wood, wood_dark, t))

    # Leaf details (rows 8-10, col 0-1)
    set_px(img, 0, 8, leaf)
    set_px(img, 0, 9, vine_green)
    set_px(img, 0, 10, vine_dark)
    set_px(img, 1, 8, vine_green)
    set_px(img, 1, 9, leaf)
    set_px(img, 1, 10, vine_green)

    # Vine wrap (rows 11-15, cols 4-6)
    for y in range(11, 16):
        for x in range(4, 7):
            if (x + y) % 3 == 0:
                set_px(img, x, y, vine_green)
            elif (x + y) % 3 == 1:
                set_px(img, x, y, wood)
            else:
                set_px(img, x, y, vine_dark)

    # Shaft lower (rows 10-15)
    for y in range(10, 16):
        t = (y - 10) / 5.0
        set_px(img, 7, y, lerp(wood, wood_dark, t))
        set_px(img, 8, y, lerp(wood_dark, root_dark, t))

    # Root pommel (rows 14-15, cols 2-5)
    for x in range(2, 6):
        t = (x - 2) / 3.0
        set_px(img, x, 14, lerp(root_dark, wood_dark, t))
        set_px(img, x, 15, lerp(wood_dark, root_dark, t))

    return img


# ============================================================
# Generate all textures
# ============================================================
if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)

    weapons = {
        "lunar_crown": gen_lunar_crown,
        "solar_crown": gen_solar_crown,
        "vampiric_tome": gen_vampiric_tome,
        "static_seeker": gen_static_seeker,
        "battledancer": gen_battledancer,
        "ebonchill": gen_ebonchill,
        "lightbinder": gen_lightbinder,
        "crescent_blade": gen_crescent_blade,
        "ghost_fang": gen_ghost_fang,
        "terra_warhammer": gen_terra_warhammer,
    }

    print(f"Generating {len(weapons)} weapon textures...")
    for name, gen_func in weapons.items():
        pixels = gen_func()
        write_png(os.path.join(OUT_DIR, f"{name}.png"), pixels)

    print("Done! All weapon textures generated.")
