"""
Regenerate all 71 new item textures with high-quality hand-crafted pixel art.
30 relics (16x16) + 41 weapons (32x32 or 16x16 matching old weapon sizes).
Each item has a unique draw function — no generic templates.
"""
from PIL import Image
import os

OUTPUT_DIR = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\item"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# ===== Helpers =====
def lighten(c, amt=30):
    return tuple(min(255, v + amt) for v in c[:3]) + ((c[3],) if len(c) == 4 else (255,))

def darken(c, amt=30):
    return tuple(max(0, v - amt) for v in c[:3]) + ((c[3],) if len(c) == 4 else (255,))

def lerp(c1, c2, t):
    t = max(0.0, min(1.0, t))
    r = int(c1[0] + (c2[0] - c1[0]) * t)
    g = int(c1[1] + (c2[1] - c1[1]) * t)
    b = int(c1[2] + (c2[2] - c1[2]) * t)
    a1 = c1[3] if len(c1) > 3 else 255
    a2 = c2[3] if len(c2) > 3 else 255
    a = int(a1 + (a2 - a1) * t)
    return (r, g, b, a)

def create(size=16):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    return img, img.load()

def save(img, name):
    path = os.path.join(OUTPUT_DIR, f"{name}.png")
    img.save(path)
    print(f"  Created: {name}.png ({img.size[0]}x{img.size[1]})")

def px(p, x, y, c, size=16):
    if 0 <= x < size and 0 <= y < size:
        if len(c) == 3:
            p[x, y] = c + (255,)
        else:
            p[x, y] = c

def fill(p, x1, y1, x2, y2, c, size=16):
    for yy in range(y1, y2):
        for xx in range(x1, x2):
            px(p, xx, yy, c, size)

def line_diag(p, x1, y1, x2, y2, c, size=16):
    """Draw a line from (x1,y1) to (x2,y2) using Bresenham's."""
    dx = abs(x2 - x1)
    dy = abs(y2 - y1)
    sx = 1 if x1 < x2 else -1
    sy = 1 if y1 < y2 else -1
    err = dx - dy
    while True:
        px(p, x1, y1, c, size)
        if x1 == x2 and y1 == y2:
            break
        e2 = 2 * err
        if e2 > -dy:
            err -= dy
            x1 += sx
        if e2 < dx:
            err += dx
            y1 += sy

# ============================================================
# RELICS (30 items, all 16x16)
# ============================================================

# --- 1. WARDEN'S VISOR (dark sculpted face mask) ---
def draw_wardens_visor():
    img, p = create(16)
    dk = (40, 30, 50, 255)
    mid = (65, 50, 75, 255)
    hi = (90, 75, 100, 255)
    eye_glow = (120, 200, 180, 255)
    bone = (80, 70, 85, 255)
    # Mask shape (wide at eyes, narrow at chin)
    for x in range(3, 13):
        px(p, x, 3, hi)  # top edge highlight
    for y in range(4, 11):
        t = (y - 4) / 6.0
        w = int(5 - t * 1.5)  # narrows toward chin
        cx_l = 8 - w
        cx_r = 8 + w
        for x in range(cx_l, cx_r):
            c = lerp(hi, dk, t)
            px(p, x, y, c)
    # Left edge highlight
    for y in range(4, 9):
        px(p, 3, y, lighten(hi, 15))
    # Right edge shadow
    for y in range(4, 9):
        px(p, 12, y, darken(dk, 15))
    # Eye slits — glowing teal
    for x in range(4, 7):
        px(p, x, 6, (10, 10, 15, 255))
        px(p, x, 7, eye_glow)
    for x in range(9, 12):
        px(p, x, 6, (10, 10, 15, 255))
        px(p, x, 7, eye_glow)
    # Nose bridge
    px(p, 7, 6, bone)
    px(p, 8, 6, bone)
    px(p, 7, 7, mid)
    px(p, 8, 7, mid)
    # Brow ridge highlights
    for x in range(4, 7):
        px(p, x, 5, lighten(mid, 20))
    for x in range(9, 12):
        px(p, x, 5, lighten(mid, 20))
    # Sculpted cheek lines
    px(p, 5, 8, darken(mid, 20))
    px(p, 10, 8, darken(mid, 20))
    # Chin detail
    px(p, 7, 10, darken(dk, 10))
    px(p, 8, 10, darken(dk, 10))
    # Strap sides
    for y in range(5, 9):
        px(p, 2, y, darken(dk, 25))
        px(p, 13, y, darken(dk, 25))
    # Forehead rune (small V)
    px(p, 7, 4, eye_glow)
    px(p, 8, 4, eye_glow)
    px(p, 6, 3, (80, 160, 140, 200))
    px(p, 9, 3, (80, 160, 140, 200))
    save(img, "wardens_visor")

# --- 2. VERDANT MASK (living leaf mask) ---
def draw_verdant_mask():
    img, p = create(16)
    leaf_dk = (25, 90, 20, 255)
    leaf = (45, 140, 35, 255)
    leaf_hi = (70, 180, 55, 255)
    vine = (55, 100, 30, 255)
    eye = (180, 220, 80, 255)
    wood = (80, 55, 30, 255)
    # Mask shape — leafy, organic
    for y in range(2, 12):
        t = (y - 2) / 9.0
        w = 5 if y < 8 else int(5 - (y - 8) * 1.2)
        for x in range(8 - w, 8 + w):
            vein = ((x + y) % 3 == 0)
            if vein:
                c = vine
            else:
                c = lerp(leaf_hi, leaf_dk, t)
            px(p, x, y, c)
    # Leaf points at top
    px(p, 5, 1, leaf_hi)
    px(p, 6, 1, leaf)
    px(p, 9, 1, leaf)
    px(p, 10, 1, leaf_hi)
    px(p, 7, 1, leaf_hi)
    px(p, 8, 1, leaf_hi)
    # Top edge highlight
    for x in range(4, 12):
        px(p, x, 2, lighten(leaf_hi, 15))
    # Eye holes — glowing yellow-green
    for x in range(4, 7):
        px(p, x, 6, (15, 10, 5, 255))
        px(p, x, 7, eye)
    for x in range(9, 12):
        px(p, x, 6, (15, 10, 5, 255))
        px(p, x, 7, eye)
    # Nose
    px(p, 7, 7, wood)
    px(p, 8, 7, wood)
    # Vine details along edges
    px(p, 3, 5, vine)
    px(p, 3, 7, vine)
    px(p, 12, 5, vine)
    px(p, 12, 7, vine)
    # Small leaf sprouts
    px(p, 3, 4, leaf_hi)
    px(p, 12, 4, leaf_hi)
    px(p, 4, 11, leaf)
    px(p, 11, 11, leaf)
    # Leaf vein down center
    for y in range(3, 10):
        if y not in [6, 7]:
            px(p, 7, y, vine)
    save(img, "verdant_mask")

# --- 3. FROSTWEAVE VEIL (translucent ice veil) ---
def draw_frostweave_veil():
    img, p = create(16)
    ice_hi = (210, 235, 255, 255)
    ice = (160, 200, 240, 220)
    ice_mid = (120, 170, 220, 180)
    ice_dk = (80, 130, 190, 150)
    crystal = (200, 230, 255, 255)
    frost = (230, 245, 255, 200)
    # Headband
    for x in range(2, 14):
        px(p, x, 3, crystal)
        px(p, x, 4, lighten(crystal, 10))
    # Central gem on headband
    px(p, 7, 3, (180, 220, 255, 255))
    px(p, 8, 3, (180, 220, 255, 255))
    px(p, 7, 2, frost)
    px(p, 8, 2, frost)
    # Veil draping down — translucent, flowing
    for y in range(5, 14):
        t = (y - 5) / 8.0
        for x in range(2, 14):
            # Wave pattern for flowing fabric
            wave = ((x + y) % 4 == 0)
            if wave:
                c = lerp(ice_hi, ice_dk, t)
            else:
                c = lerp(ice, ice_dk, t)
            # Taper at bottom
            if y > 10 and (x < 4 or x > 11):
                continue
            if y > 12 and (x < 5 or x > 10):
                continue
            px(p, x, y, c)
    # Frost sparkle details
    px(p, 5, 6, frost)
    px(p, 10, 7, frost)
    px(p, 6, 9, frost)
    px(p, 9, 11, frost)
    px(p, 7, 8, crystal)
    # Edge frost fringe
    for x in range(4, 12):
        if x % 2 == 0:
            px(p, x, 13, frost)
    save(img, "frostweave_veil")

# --- 4. STORMCALLER CIRCLET (silver circlet, lightning gem) ---
def draw_stormcaller_circlet():
    img, p = create(16)
    silver = (170, 175, 185, 255)
    silver_hi = (210, 215, 225, 255)
    silver_dk = (120, 125, 135, 255)
    bolt_yellow = (255, 255, 120, 255)
    bolt_white = (255, 255, 220, 255)
    gem_blue = (100, 160, 255, 255)
    gem_hi = (160, 210, 255, 255)
    # Circlet band (curved, rows 7-9)
    for x in range(1, 15):
        t = abs(x - 7.5) / 7.5
        y_off = int(t * 2)  # curves up at sides
        for dy in range(3):
            yy = 7 + dy - y_off
            c = [silver_hi, silver, silver_dk][dy]
            px(p, x, yy, c)
    # Central gem mount
    fill(p, 6, 4, 10, 7, silver)
    px(p, 6, 4, silver_hi)
    px(p, 7, 4, silver_hi)
    px(p, 8, 4, silver_hi)
    px(p, 9, 4, silver_hi)
    px(p, 9, 6, silver_dk)
    # Gem (blue with lightning inside)
    px(p, 7, 5, gem_blue)
    px(p, 8, 5, gem_hi)
    px(p, 7, 4, gem_hi)
    px(p, 8, 4, gem_blue)
    # Lightning bolt above gem
    px(p, 7, 2, bolt_yellow)
    px(p, 8, 1, bolt_white)
    px(p, 8, 2, bolt_yellow)
    px(p, 7, 3, bolt_white)
    px(p, 9, 3, bolt_yellow)
    # Side prongs
    px(p, 3, 5, silver_hi)
    px(p, 3, 4, silver)
    px(p, 12, 5, silver)
    px(p, 12, 4, silver_dk)
    # Electric glow
    px(p, 6, 3, (180, 200, 255, 150))
    px(p, 9, 2, (180, 200, 255, 150))
    # Band pattern
    for x in range(2, 14, 2):
        px(p, x, 8, darken(silver, 15))
    save(img, "stormcaller_circlet")

# --- 5. ASHEN DIADEM (charred crown with ember cracks) ---
def draw_ashen_diadem():
    img, p = create(16)
    ash = (70, 65, 60, 255)
    ash_hi = (100, 92, 85, 255)
    ash_dk = (40, 38, 35, 255)
    ember = (255, 120, 20, 255)
    ember_hi = (255, 180, 60, 255)
    ember_dk = (200, 60, 0, 255)
    coal = (30, 25, 22, 255)
    # Crown points (3 points)
    # Center tall point
    for y in range(1, 6):
        t = (y - 1) / 4.0
        px(p, 7, y, lerp(ash_hi, ash, t))
        px(p, 8, y, lerp(ash, ash_dk, t))
    # Left point
    for y in range(3, 6):
        t = (y - 3) / 2.0
        px(p, 4, y, lerp(ash_hi, ash, t))
        px(p, 5, y, lerp(ash, ash_dk, t))
    # Right point
    for y in range(3, 6):
        t = (y - 3) / 2.0
        px(p, 10, y, lerp(ash_hi, ash, t))
        px(p, 11, y, lerp(ash, ash_dk, t))
    # Crown band (rows 6-9)
    for x in range(2, 14):
        t = (x - 2) / 11.0
        px(p, x, 6, ash_hi)
        px(p, x, 7, ash)
        px(p, x, 8, ash)
        px(p, x, 9, ash_dk)
    # Ember cracks (glowing orange lines through the ash)
    px(p, 3, 7, ember)
    px(p, 4, 8, ember_hi)
    px(p, 5, 7, ember)
    px(p, 6, 8, ember_dk)
    px(p, 8, 7, ember)
    px(p, 9, 8, ember_hi)
    px(p, 10, 7, ember)
    px(p, 11, 8, ember)
    px(p, 12, 7, ember_dk)
    # Ember glow on crown points
    px(p, 7, 2, ember)
    px(p, 8, 3, ember_dk)
    px(p, 4, 4, ember)
    px(p, 11, 4, ember)
    # Charred edges
    px(p, 2, 7, coal)
    px(p, 13, 7, coal)
    px(p, 2, 8, coal)
    px(p, 13, 8, coal)
    # Hot coal base glow
    for x in range(3, 13):
        px(p, x, 10, (ember_dk[0], ember_dk[1], ember_dk[2], 80))
    save(img, "ashen_diadem")

# --- 6. WRAITH CROWN (ghostly translucent crown) ---
def draw_wraith_crown():
    img, p = create(16)
    ghost = (140, 150, 180, 200)
    ghost_hi = (190, 200, 230, 230)
    ghost_dk = (80, 85, 120, 160)
    wisp = (170, 180, 220, 140)
    core = (200, 210, 255, 255)
    void = (40, 35, 70, 200)
    # Crown shape — ethereal, jagged
    # Three spires
    for y in range(1, 5):
        t = (y - 1) / 3.0
        px(p, 7, y, lerp(core, ghost, t))
        px(p, 8, y, lerp(ghost_hi, ghost_dk, t))
    for y in range(3, 5):
        px(p, 4, y, lerp(ghost_hi, ghost, (y-3)/1.0))
        px(p, 5, y, lerp(ghost, ghost_dk, (y-3)/1.0))
    for y in range(3, 5):
        px(p, 10, y, lerp(ghost_hi, ghost, (y-3)/1.0))
        px(p, 11, y, lerp(ghost, ghost_dk, (y-3)/1.0))
    # Crown band
    for x in range(2, 14):
        px(p, x, 5, ghost_hi)
        px(p, x, 6, ghost)
        px(p, x, 7, ghost_dk)
    # Ghostly wisps trailing down
    px(p, 3, 8, wisp)
    px(p, 4, 9, (wisp[0], wisp[1], wisp[2], 100))
    px(p, 3, 10, (wisp[0], wisp[1], wisp[2], 60))
    px(p, 12, 8, wisp)
    px(p, 11, 9, (wisp[0], wisp[1], wisp[2], 100))
    px(p, 12, 10, (wisp[0], wisp[1], wisp[2], 60))
    px(p, 7, 8, wisp)
    px(p, 8, 9, (wisp[0], wisp[1], wisp[2], 100))
    # Soul gem in center
    px(p, 7, 5, core)
    px(p, 8, 5, (180, 190, 255, 255))
    # Void eye sockets
    px(p, 5, 6, void)
    px(p, 6, 6, void)
    px(p, 9, 6, void)
    px(p, 10, 6, void)
    # Spectral glow around crown
    px(p, 1, 5, (wisp[0], wisp[1], wisp[2], 60))
    px(p, 14, 5, (wisp[0], wisp[1], wisp[2], 60))
    px(p, 6, 1, (core[0], core[1], core[2], 80))
    px(p, 9, 1, (core[0], core[1], core[2], 80))
    save(img, "wraith_crown")

# --- 7. ARCANE GAUNTLET (purple magic gauntlet with runes) ---
def draw_arcane_gauntlet():
    img, p = create(16)
    purple = (100, 50, 140, 255)
    purple_hi = (140, 80, 180, 255)
    purple_dk = (60, 30, 90, 255)
    rune = (200, 160, 255, 255)
    rune_glow = (180, 140, 255, 180)
    metal = (130, 120, 150, 255)
    metal_hi = (170, 160, 190, 255)
    # Gauntlet shape — armored glove, side view
    # Cuff (bottom)
    for x in range(3, 11):
        for y in range(10, 14):
            t = (y - 10) / 3.0
            px(p, x, y, lerp(metal_hi, metal, t))
    px(p, 3, 10, metal_hi)
    px(p, 10, 13, darken(metal, 20))
    # Hand body
    for x in range(3, 12):
        for y in range(5, 10):
            t = (y - 5) / 4.0
            px(p, x, y, lerp(purple_hi, purple_dk, t))
    # Fingers (top)
    for x in range(4, 8):
        px(p, x, 4, purple_hi)
        px(p, x, 3, purple)
    # Thumb
    px(p, 11, 6, purple)
    px(p, 12, 5, purple_hi)
    px(p, 12, 6, purple)
    # Knuckle plates
    for x in range(4, 8):
        px(p, x, 5, metal_hi)
    for x in range(4, 8):
        px(p, x, 6, metal)
    # Glowing runes on back of hand
    px(p, 6, 7, rune)
    px(p, 7, 8, rune)
    px(p, 8, 7, rune)
    px(p, 7, 7, rune_glow)
    # Rune glow aura
    px(p, 5, 7, rune_glow)
    px(p, 9, 7, rune_glow)
    px(p, 7, 6, (rune_glow[0], rune_glow[1], rune_glow[2], 100))
    # Cuff detail
    for x in range(4, 10):
        px(p, x, 10, lighten(metal_hi, 10))
    # Finger tips
    px(p, 4, 3, lighten(purple, 20))
    px(p, 7, 2, purple_hi)
    save(img, "arcane_gauntlet")

# --- 8. IRON FIST (heavy metal fist) ---
def draw_iron_fist():
    img, p = create(16)
    iron = (150, 150, 155, 255)
    iron_hi = (190, 190, 195, 255)
    iron_dk = (100, 100, 108, 255)
    bolt = (80, 80, 85, 255)
    rust = (140, 110, 80, 255)
    dark = (60, 60, 65, 255)
    # Heavy gauntlet — clenched fist facing forward
    # Cuff
    for x in range(3, 12):
        for y in range(10, 14):
            t = (y - 10) / 3.0
            px(p, x, y, lerp(iron_hi, iron_dk, t))
    # Main hand
    for x in range(3, 13):
        for y in range(4, 10):
            t = (y - 4) / 5.0 + (x - 3) * 0.02
            px(p, x, y, lerp(iron_hi, iron_dk, t))
    # Finger knuckles — raised bumps
    for x in [4, 6, 8, 10]:
        px(p, x, 4, lighten(iron_hi, 20))
        px(p, x, 5, iron_hi)
        px(p, x + 1, 4, iron)
        px(p, x + 1, 5, iron_dk)
    # Bolts/rivets
    px(p, 4, 7, bolt)
    px(p, 7, 7, bolt)
    px(p, 10, 7, bolt)
    px(p, 5, 9, bolt)
    px(p, 9, 9, bolt)
    # Rust patches
    px(p, 6, 8, rust)
    px(p, 11, 6, rust)
    px(p, 3, 11, rust)
    # Edge highlights/shadows
    for y in range(4, 10):
        px(p, 3, y, lighten(iron_hi, 10))
        px(p, 12, y, dark)
    # Top highlight
    for x in range(4, 12):
        px(p, x, 4, lighten(iron_hi, 15))
    # Bottom shadow
    for x in range(3, 12):
        px(p, x, 13, dark)
    # Cuff bolts
    px(p, 4, 11, bolt)
    px(p, 10, 11, bolt)
    save(img, "iron_fist")

# --- 9. PLAGUE GRASP (sickly green poisoned glove) ---
def draw_plague_grasp():
    img, p = create(16)
    green = (70, 140, 50, 255)
    green_hi = (100, 180, 70, 255)
    green_dk = (40, 90, 30, 255)
    ooze = (120, 200, 40, 255)
    ooze_hi = (160, 230, 80, 255)
    cloth = (60, 55, 45, 255)
    cloth_hi = (85, 78, 65, 255)
    drip = (90, 170, 30, 200)
    # Wrapped cloth base
    for x in range(4, 12):
        for y in range(4, 13):
            t = (y - 4) / 8.0
            if (x + y) % 3 == 0:
                px(p, x, y, cloth)
            else:
                px(p, x, y, lerp(green_hi, green_dk, t))
    # Fingers — gnarled
    for i, xx in enumerate([4, 6, 8, 10]):
        h = 3 if i in [1, 2] else 4
        px(p, xx, h, green_hi)
        px(p, xx, h + 1, green)
    # Toxic ooze dripping from fingers
    px(p, 5, 3, ooze_hi)
    px(p, 5, 2, ooze)
    px(p, 7, 2, ooze_hi)
    px(p, 7, 1, (ooze[0], ooze[1], ooze[2], 180))
    px(p, 9, 3, ooze)
    # Ooze patches on glove
    px(p, 6, 7, ooze)
    px(p, 7, 8, ooze_hi)
    px(p, 8, 6, ooze)
    px(p, 9, 9, ooze)
    # Drip effects
    px(p, 4, 13, drip)
    px(p, 4, 14, (drip[0], drip[1], drip[2], 120))
    px(p, 8, 13, drip)
    px(p, 8, 14, (drip[0], drip[1], drip[2], 100))
    # Cloth wrapping detail
    for x in range(4, 12):
        px(p, x, 10, cloth_hi)
    for x in range(4, 12):
        px(p, x, 12, darken(cloth, 15))
    # Thumb
    px(p, 12, 6, green)
    px(p, 12, 7, green_dk)
    save(img, "plague_grasp")

# --- 10. SUNFORGED BRACER (golden wrist guard with sun) ---
def draw_sunforged_bracer():
    img, p = create(16)
    gold = (220, 180, 50, 255)
    gold_hi = (255, 220, 100, 255)
    gold_dk = (170, 130, 20, 255)
    sun = (255, 240, 150, 255)
    sun_center = (255, 255, 200, 255)
    ray = (255, 200, 80, 255)
    leather = (100, 70, 40, 255)
    # Bracer body (wrist armor, horizontal)
    for x in range(2, 14):
        for y in range(5, 11):
            t = (y - 5) / 5.0
            px(p, x, y, lerp(gold_hi, gold_dk, t))
    # Top/bottom borders
    for x in range(2, 14):
        px(p, x, 5, lighten(gold_hi, 20))
        px(p, x, 10, darken(gold_dk, 20))
    # Side edges
    for y in range(5, 11):
        px(p, 2, y, lighten(gold_hi, 10))
        px(p, 13, y, darken(gold_dk, 15))
    # Sun emblem in center
    px(p, 7, 7, sun_center)
    px(p, 8, 7, sun_center)
    px(p, 7, 8, sun)
    px(p, 8, 8, sun)
    # Sun rays
    px(p, 6, 7, ray)
    px(p, 9, 7, ray)
    px(p, 7, 6, ray)
    px(p, 8, 6, ray)
    px(p, 7, 9, ray)
    px(p, 8, 9, ray)
    # Diagonal rays
    px(p, 6, 6, (ray[0], ray[1], ray[2], 200))
    px(p, 9, 6, (ray[0], ray[1], ray[2], 200))
    px(p, 6, 9, (ray[0], ray[1], ray[2], 200))
    px(p, 9, 9, (ray[0], ray[1], ray[2], 200))
    # Filigree pattern
    for x in [3, 5, 10, 12]:
        px(p, x, 7, darken(gold, 20))
        px(p, x, 8, darken(gold, 15))
    # Leather backing visible at edges
    px(p, 1, 7, leather)
    px(p, 1, 8, leather)
    px(p, 14, 7, leather)
    px(p, 14, 8, leather)
    # Buckle straps
    px(p, 2, 4, leather)
    px(p, 3, 4, leather)
    px(p, 11, 4, leather)
    px(p, 12, 4, leather)
    save(img, "sunforged_bracer")

# --- 11. STORMBAND (electric ring) ---
def draw_stormband():
    img, p = create(16)
    silver = (180, 185, 200, 255)
    silver_hi = (220, 225, 240, 255)
    silver_dk = (130, 135, 150, 255)
    spark = (255, 255, 140, 255)
    spark_w = (255, 255, 230, 255)
    elec = (120, 180, 255, 255)
    # Ring band (oval viewed at angle)
    # Top arc
    for x in range(5, 11):
        px(p, x, 5, silver_hi)
        px(p, x, 6, silver)
    # Sides
    for y in range(6, 11):
        t = (y - 6) / 4.0
        px(p, 4, y, lerp(silver_hi, silver, t))
        px(p, 5, y, lerp(silver_hi, silver_dk, t))
        px(p, 10, y, lerp(silver, silver_dk, t))
        px(p, 11, y, lerp(silver_dk, darken(silver_dk, 15), t))
    # Bottom arc
    for x in range(5, 11):
        px(p, x, 10, silver_dk)
        px(p, x, 11, darken(silver_dk, 15))
    # Inner hole
    for x in range(6, 10):
        for y in range(7, 10):
            px(p, x, y, (0, 0, 0, 0))  # transparent
    # Lightning sparks on the band
    px(p, 7, 5, spark_w)
    px(p, 8, 5, spark)
    px(p, 4, 8, elec)
    px(p, 11, 8, spark)
    # Tiny lightning bolt from gem
    px(p, 7, 4, spark)
    px(p, 8, 3, spark_w)
    px(p, 6, 4, (spark[0], spark[1], spark[2], 150))
    # Electric arc around ring
    px(p, 3, 7, (elec[0], elec[1], elec[2], 120))
    px(p, 12, 9, (elec[0], elec[1], elec[2], 120))
    # Engraving lines
    for x in range(5, 11):
        if x % 2 == 0:
            px(p, x, 6, darken(silver, 15))
    save(img, "stormband")

# --- 12. GRAVESTONE RING (dark stone ring with cross) ---
def draw_gravestone_ring():
    img, p = create(16)
    stone = (90, 85, 80, 255)
    stone_hi = (120, 115, 110, 255)
    stone_dk = (55, 52, 48, 255)
    cross = (160, 155, 148, 255)
    moss = (60, 90, 40, 255)
    crack = (40, 38, 35, 255)
    # Ring band
    for x in range(5, 11):
        px(p, x, 5, stone_hi)
        px(p, x, 6, stone)
    for y in range(6, 11):
        px(p, 4, y, lerp(stone_hi, stone, (y-6)/4.0))
        px(p, 5, y, lerp(stone_hi, stone_dk, (y-6)/4.0))
        px(p, 10, y, lerp(stone, stone_dk, (y-6)/4.0))
        px(p, 11, y, lerp(stone_dk, crack, (y-6)/4.0))
    for x in range(5, 11):
        px(p, x, 10, stone_dk)
        px(p, x, 11, crack)
    # Inner hole
    for x in range(6, 10):
        for y in range(7, 10):
            px(p, x, y, (0, 0, 0, 0))
    # Gravestone bezel (flat top on ring)
    fill(p, 6, 3, 10, 6, stone)
    px(p, 6, 3, stone_hi)
    px(p, 7, 3, stone_hi)
    px(p, 8, 3, stone_hi)
    px(p, 9, 3, stone_hi)
    px(p, 9, 5, stone_dk)
    # Cross engraved
    px(p, 7, 3, cross)
    px(p, 8, 3, cross)
    px(p, 7, 4, cross)
    px(p, 8, 4, cross)
    px(p, 6, 4, cross)
    px(p, 9, 4, cross)
    px(p, 7, 5, cross)
    px(p, 8, 5, cross)
    # Moss patches
    px(p, 5, 9, moss)
    px(p, 10, 7, moss)
    # Crack detail
    px(p, 4, 8, crack)
    save(img, "gravestone_ring")

# --- 13. VERDANT SIGNET (green nature ring) ---
def draw_verdant_signet():
    img, p = create(16)
    wood = (95, 70, 40, 255)
    wood_hi = (125, 95, 55, 255)
    wood_dk = (65, 48, 28, 255)
    leaf = (60, 160, 45, 255)
    leaf_hi = (90, 200, 70, 255)
    vine = (40, 100, 30, 255)
    # Ring band (wood)
    for x in range(5, 11):
        px(p, x, 5, wood_hi)
        px(p, x, 6, wood)
    for y in range(6, 11):
        px(p, 4, y, lerp(wood_hi, wood, (y-6)/4.0))
        px(p, 5, y, lerp(wood_hi, wood_dk, (y-6)/4.0))
        px(p, 10, y, lerp(wood, wood_dk, (y-6)/4.0))
        px(p, 11, y, lerp(wood_dk, darken(wood_dk, 10), (y-6)/4.0))
    for x in range(5, 11):
        px(p, x, 10, wood_dk)
        px(p, x, 11, darken(wood_dk, 10))
    # Inner hole
    for x in range(6, 10):
        for y in range(7, 10):
            px(p, x, y, (0, 0, 0, 0))
    # Leaf emblem on top
    px(p, 7, 3, leaf_hi)
    px(p, 8, 3, leaf)
    px(p, 6, 4, leaf_hi)
    px(p, 7, 4, leaf)
    px(p, 8, 4, leaf)
    px(p, 9, 4, vine)
    px(p, 7, 5, vine)
    px(p, 8, 5, leaf)
    # Leaf vein
    px(p, 7, 4, lighten(leaf_hi, 20))
    # Small vine wrapping
    px(p, 4, 7, vine)
    px(p, 5, 8, vine)
    px(p, 10, 9, vine)
    px(p, 11, 8, vine)
    # Bark texture on ring
    px(p, 6, 6, darken(wood, 12))
    px(p, 9, 6, darken(wood, 12))
    save(img, "verdant_signet")

# --- 14. PHOENIX MANTLE (fiery feathered cape) ---
def draw_phoenix_mantle():
    img, p = create(16)
    red = (200, 40, 20, 255)
    red_hi = (240, 80, 40, 255)
    orange = (255, 140, 30, 255)
    yellow = (255, 210, 60, 255)
    gold_tip = (255, 240, 120, 255)
    dark_red = (120, 20, 10, 255)
    ember = (255, 100, 20, 200)
    # Cape/mantle shape — spread like wings
    # Shoulder bar
    for x in range(3, 13):
        px(p, x, 3, red_hi)
        px(p, x, 4, red)
    # Clasp center
    px(p, 7, 3, yellow)
    px(p, 8, 3, yellow)
    px(p, 7, 2, gold_tip)
    px(p, 8, 2, gold_tip)
    # Feathered cape body — gradient red->orange->yellow at tips
    for y in range(5, 14):
        t = (y - 5) / 8.0
        w = int(3 + t * 4)  # flares outward
        for x in range(8 - w, 8 + w):
            if x < 0 or x > 15:
                continue
            ft = t + abs(x - 7.5) * 0.03  # edges more fiery
            ft = min(1.0, ft)
            c = lerp(red, orange, ft)
            if ft > 0.7:
                c = lerp(orange, yellow, (ft - 0.7) / 0.3)
            px(p, x, y, c)
    # Feather edge pattern (zigzag at bottom)
    for x in range(1, 15):
        if x % 2 == 0:
            px(p, x, 13, gold_tip)
        else:
            px(p, x, 13, orange)
    # Individual feather veins
    for y in range(6, 13, 2):
        px(p, 7, y, dark_red)
        px(p, 8, y, dark_red)
    # Left feather highlights
    for y in range(5, 12):
        xx = max(1, 8 - int((y - 5) / 8.0 * 4) - 3)
        px(p, xx, y, lighten(red_hi, 15))
    # Ember particles
    px(p, 2, 12, ember)
    px(p, 13, 11, ember)
    px(p, 5, 14, (ember[0], ember[1], ember[2], 120))
    px(p, 10, 14, (ember[0], ember[1], ember[2], 120))
    save(img, "phoenix_mantle")

# --- 15. WINDRUNNER CLOAK (flowing silver-white cape) ---
def draw_windrunner_cloak():
    img, p = create(16)
    white = (220, 225, 235, 255)
    silver = (180, 185, 200, 255)
    silver_dk = (140, 145, 160, 255)
    pale_blue = (190, 210, 235, 255)
    wind = (160, 190, 225, 200)
    clasp = (170, 160, 140, 255)
    # Shoulder bar
    for x in range(3, 13):
        px(p, x, 3, white)
        px(p, x, 4, silver)
    # Clasp
    px(p, 7, 3, clasp)
    px(p, 8, 3, clasp)
    px(p, 7, 2, lighten(clasp, 20))
    px(p, 8, 2, lighten(clasp, 20))
    # Cape body — flows and curves with wind
    for y in range(5, 14):
        t = (y - 5) / 8.0
        w = int(3 + t * 3)
        offset = int(1.5 * (0.5 - abs(t - 0.5)))  # wind curve
        for x in range(8 - w + offset, 8 + w + offset):
            if x < 0 or x > 15:
                continue
            c = lerp(white, silver_dk, t)
            # Wind swirl pattern
            if (x + y * 2) % 5 == 0:
                c = pale_blue
            px(p, x, y, c)
    # Wind swirl lines
    px(p, 5, 7, wind)
    px(p, 6, 8, wind)
    px(p, 7, 9, wind)
    px(p, 9, 7, wind)
    px(p, 10, 8, wind)
    px(p, 11, 9, wind)
    # Flowing edge
    for x in range(3, 13):
        if (x % 3) == 0:
            px(p, x, 13, pale_blue)
    # Highlight on left fold
    for y in range(5, 12):
        px(p, 4, y, lighten(white, 10))
    save(img, "windrunner_cloak")

# --- 16. ABYSSAL CAPE (dark deep-sea cape) ---
def draw_abyssal_cape():
    img, p = create(16)
    deep = (15, 12, 40, 255)
    dark_blue = (25, 20, 60, 255)
    mid = (40, 35, 80, 255)
    tentacle = (50, 40, 90, 255)
    eye = (100, 200, 180, 255)
    biolum = (80, 180, 160, 180)
    clasp = (60, 80, 100, 255)
    # Shoulder bar
    for x in range(3, 13):
        px(p, x, 3, mid)
        px(p, x, 4, dark_blue)
    # Clasp
    px(p, 7, 3, clasp)
    px(p, 8, 3, clasp)
    px(p, 7, 2, lighten(clasp, 20))
    px(p, 8, 2, lighten(clasp, 20))
    # Cape body — dark, ominous
    for y in range(5, 14):
        t = (y - 5) / 8.0
        w = int(3 + t * 3)
        for x in range(8 - w, 8 + w):
            if x < 0 or x > 15:
                continue
            c = lerp(mid, deep, t)
            px(p, x, y, c)
    # Tentacle-like edges
    px(p, 3, 12, tentacle)
    px(p, 2, 13, tentacle)
    px(p, 2, 14, dark_blue)
    px(p, 12, 12, tentacle)
    px(p, 13, 13, tentacle)
    px(p, 13, 14, dark_blue)
    px(p, 7, 14, tentacle)
    px(p, 8, 14, tentacle)
    # Bioluminescent spots
    px(p, 6, 8, biolum)
    px(p, 9, 10, biolum)
    px(p, 5, 11, (biolum[0], biolum[1], biolum[2], 120))
    px(p, 10, 7, (biolum[0], biolum[1], biolum[2], 120))
    # Abyssal eye detail
    px(p, 7, 9, eye)
    px(p, 8, 9, (eye[0], eye[1], eye[2], 200))
    # Dark fold lines
    px(p, 7, 7, deep)
    px(p, 8, 8, deep)
    px(p, 7, 11, deep)
    save(img, "abyssal_cape")

# --- 17. ALCHEMIST'S SASH (purple sash with potion vials) ---
def draw_alchemists_sash():
    img, p = create(16)
    purple = (110, 50, 130, 255)
    purple_hi = (145, 75, 165, 255)
    purple_dk = (75, 30, 95, 255)
    glass = (180, 200, 210, 200)
    potion_red = (200, 40, 30, 255)
    potion_green = (50, 180, 60, 255)
    potion_blue = (50, 100, 200, 255)
    cork = (140, 110, 70, 255)
    gold = (200, 170, 50, 255)
    # Sash diagonal (top-left to bottom-right)
    for i in range(14):
        x = 1 + i
        y = 3 + int(i * 0.6)
        for dy in range(-1, 2):
            yy = y + dy
            if dy == -1:
                c = purple_hi
            elif dy == 0:
                c = purple
            else:
                c = purple_dk
            px(p, x, yy, c)
    # Stitch detail along sash
    for i in range(0, 14, 3):
        x = 2 + i
        y = 3 + int(i * 0.6)
        px(p, x, y, lighten(purple, 15))
    # Potion vial 1 (red, hanging at pos ~4,8)
    px(p, 4, 7, cork)
    px(p, 4, 8, glass)
    px(p, 4, 9, potion_red)
    px(p, 4, 10, potion_red)
    # Potion vial 2 (green, at ~7,9)
    px(p, 7, 8, cork)
    px(p, 7, 9, glass)
    px(p, 7, 10, potion_green)
    px(p, 7, 11, potion_green)
    # Potion vial 3 (blue, at ~10,10)
    px(p, 10, 9, cork)
    px(p, 10, 10, glass)
    px(p, 10, 11, potion_blue)
    px(p, 10, 12, potion_blue)
    # Gold buckle at top
    px(p, 2, 3, gold)
    px(p, 3, 3, gold)
    px(p, 2, 4, darken(gold, 20))
    px(p, 3, 4, darken(gold, 20))
    save(img, "alchemists_sash")

# --- 18. GUARDIAN'S GIRDLE (heavy armored belt) ---
def draw_guardians_girdle():
    img, p = create(16)
    steel = (160, 165, 175, 255)
    steel_hi = (200, 205, 215, 255)
    steel_dk = (110, 115, 125, 255)
    blue = (60, 80, 140, 255)
    blue_hi = (80, 100, 170, 255)
    rivet = (90, 90, 95, 255)
    gold = (200, 170, 50, 255)
    gold_dk = (160, 130, 30, 255)
    # Belt strap (armored plates)
    for x in range(1, 15):
        px(p, x, 5, steel_hi)
        px(p, x, 6, steel)
        px(p, x, 7, steel)
        px(p, x, 8, steel)
        px(p, x, 9, steel_dk)
    # Plate divisions
    for x in [4, 7, 10]:
        for y in range(5, 10):
            px(p, x, y, darken(steel, 20))
    # Shield buckle (center)
    fill(p, 5, 4, 10, 11, blue)
    px(p, 5, 4, blue_hi)
    px(p, 6, 4, blue_hi)
    px(p, 7, 4, blue_hi)
    px(p, 8, 4, blue_hi)
    px(p, 9, 4, blue_hi)
    px(p, 9, 10, darken(blue, 20))
    px(p, 8, 10, darken(blue, 20))
    # Shield emblem (cross)
    for y in range(5, 10):
        px(p, 7, y, gold)
    for x in range(5, 10):
        px(p, x, 7, gold)
    px(p, 7, 7, lighten(gold, 20))
    # Shield border
    for x in range(5, 10):
        px(p, x, 4, gold_dk)
    for y in range(4, 11):
        px(p, 5, y, gold_dk)
    # Rivets
    px(p, 2, 6, rivet)
    px(p, 2, 8, rivet)
    px(p, 13, 6, rivet)
    px(p, 13, 8, rivet)
    # Edge highlights
    for x in range(1, 15):
        px(p, x, 5, lighten(steel_hi, 10))
    save(img, "guardians_girdle")

# --- 19. SERPENT BELT (snake-themed belt) ---
def draw_serpent_belt():
    img, p = create(16)
    scale = (40, 120, 50, 255)
    scale_hi = (60, 160, 70, 255)
    scale_dk = (25, 80, 30, 255)
    belly = (100, 160, 80, 255)
    eye = (255, 200, 0, 255)
    fang = (240, 235, 220, 255)
    tongue = (200, 40, 30, 255)
    # Snake body as belt (curved S-shape)
    snake_path = [(2,6),(3,6),(4,7),(5,7),(6,7),(7,8),(8,8),(9,8),(10,7),(11,7),(12,6),(13,6)]
    for sx, sy in snake_path:
        px(p, sx, sy-1, scale_hi)
        px(p, sx, sy, scale)
        px(p, sx, sy+1, scale_dk)
    # Scale pattern
    for i, (sx, sy) in enumerate(snake_path):
        if i % 2 == 0:
            px(p, sx, sy, darken(scale, 15))
    # Belly scales (lighter stripe)
    for sx, sy in snake_path:
        px(p, sx, sy, belly)
    # Snake head (left side)
    fill(p, 0, 4, 3, 9, scale)
    px(p, 0, 4, scale_hi)
    px(p, 1, 4, scale_hi)
    px(p, 2, 4, scale_hi)
    px(p, 0, 8, scale_dk)
    px(p, 1, 8, scale_dk)
    # Eye
    px(p, 1, 5, eye)
    # Fangs
    px(p, 0, 8, fang)
    px(p, 0, 9, fang)
    # Tongue
    px(p, 0, 10, tongue)
    px(p, 0, 11, (tongue[0], tongue[1], tongue[2], 200))
    # Tail (right, tapering)
    px(p, 14, 6, scale)
    px(p, 14, 5, scale_hi)
    px(p, 15, 5, scale_dk)
    # Scale texture highlights
    for i in range(2, 13, 3):
        if i < len(snake_path):
            sx, sy = snake_path[min(i, len(snake_path)-1)]
            px(p, sx, sy-1, lighten(scale_hi, 15))
    save(img, "serpent_belt")

# --- 20. FROSTFIRE PENDANT (dual ice/fire pendant) ---
def draw_frostfire_pendant():
    img, p = create(16)
    chain = (180, 175, 165, 255)
    chain_dk = (130, 125, 115, 255)
    ice = (100, 180, 255, 255)
    ice_hi = (180, 220, 255, 255)
    ice_dk = (50, 120, 200, 255)
    fire = (255, 100, 20, 255)
    fire_hi = (255, 180, 60, 255)
    fire_dk = (200, 50, 0, 255)
    frame = (140, 135, 125, 255)
    # Chain
    for x in range(5, 11):
        px(p, x, 2, chain)
    px(p, 5, 3, chain_dk)
    px(p, 10, 3, chain_dk)
    px(p, 6, 3, chain)
    px(p, 9, 3, chain)
    # Chain V-shape down to pendant
    px(p, 6, 4, chain)
    px(p, 9, 4, chain)
    px(p, 7, 5, chain_dk)
    px(p, 8, 5, chain_dk)
    # Pendant frame
    for x in range(5, 11):
        px(p, x, 6, frame)
        px(p, x, 12, darken(frame, 20))
    for y in range(6, 13):
        px(p, 5, y, frame)
        px(p, 10, y, darken(frame, 15))
    # Left half — ICE
    for y in range(7, 12):
        for x in range(6, 8):
            t = (y - 7) / 4.0
            px(p, x, y, lerp(ice_hi, ice_dk, t))
    # Right half — FIRE
    for y in range(7, 12):
        for x in range(8, 10):
            t = (y - 7) / 4.0
            px(p, x, y, lerp(fire_hi, fire_dk, t))
    # Dividing line
    for y in range(7, 12):
        px(p, 8, y, (200, 180, 160, 255))
    # Ice crystal detail
    px(p, 6, 8, ice_hi)
    px(p, 7, 9, (220, 240, 255, 255))
    # Fire flicker detail
    px(p, 9, 8, fire_hi)
    px(p, 8, 10, (255, 200, 80, 255))
    # Glow
    px(p, 4, 9, (ice[0], ice[1], ice[2], 80))
    px(p, 11, 9, (fire[0], fire[1], fire[2], 80))
    save(img, "frostfire_pendant")

# --- 21. TIDEKEEPER AMULET (ocean blue amulet) ---
def draw_tidekeeper_amulet():
    img, p = create(16)
    chain = (160, 170, 180, 255)
    chain_dk = (120, 130, 140, 255)
    blue = (30, 80, 180, 255)
    blue_hi = (60, 120, 220, 255)
    blue_dk = (15, 50, 130, 255)
    aqua = (80, 200, 220, 255)
    pearl = (230, 235, 240, 255)
    frame = (100, 130, 150, 255)
    wave = (50, 150, 200, 255)
    # Chain
    for x in range(5, 11):
        px(p, x, 2, chain)
    px(p, 6, 3, chain)
    px(p, 9, 3, chain)
    px(p, 7, 4, chain_dk)
    px(p, 8, 4, chain_dk)
    # Amulet frame (circular)
    # Top arc
    for x in range(5, 11):
        px(p, x, 5, frame)
    for y in range(5, 13):
        px(p, 4, y, frame)
        px(p, 11, y, darken(frame, 15))
    for x in range(5, 11):
        px(p, x, 12, darken(frame, 20))
    # Interior — ocean gradient
    for y in range(6, 12):
        for x in range(5, 11):
            t = (y - 6) / 5.0
            c = lerp(blue_hi, blue_dk, t)
            px(p, x, y, c)
    # Wave pattern
    px(p, 5, 8, wave)
    px(p, 6, 7, wave)
    px(p, 7, 8, wave)
    px(p, 8, 7, wave)
    px(p, 9, 8, wave)
    px(p, 10, 7, wave)
    # Central pearl
    px(p, 7, 9, pearl)
    px(p, 8, 9, pearl)
    px(p, 7, 10, (pearl[0]-20, pearl[1]-15, pearl[2]-10, 255))
    px(p, 8, 10, (pearl[0]-20, pearl[1]-15, pearl[2]-10, 255))
    # Pearl highlight
    px(p, 7, 9, (255, 255, 255, 255))
    # Aqua sparkle
    px(p, 6, 6, aqua)
    px(p, 9, 11, aqua)
    save(img, "tidekeeper_amulet")

# --- 22. BLOODSTONE CHOKER (dark red choker with crimson gem) ---
def draw_bloodstone_choker():
    img, p = create(16)
    leather = (35, 15, 15, 255)
    leather_hi = (55, 25, 22, 255)
    leather_dk = (20, 8, 8, 255)
    blood = (160, 15, 10, 255)
    blood_hi = (210, 40, 30, 255)
    blood_dk = (100, 5, 5, 255)
    gem_bright = (230, 50, 40, 255)
    silver = (160, 155, 150, 255)
    # Choker band (wraps around neck, shown flat)
    for x in range(1, 15):
        px(p, x, 6, leather_hi)
        px(p, x, 7, leather)
        px(p, x, 8, leather)
        px(p, x, 9, leather_dk)
    # Stitch details
    for x in range(1, 15, 2):
        px(p, x, 7, darken(leather, 10))
    # Central bloodstone gem
    fill(p, 5, 4, 11, 12, blood)
    # Gem facets
    px(p, 5, 4, blood_hi)
    px(p, 6, 4, blood_hi)
    px(p, 7, 4, blood_hi)
    px(p, 8, 4, blood_hi)
    px(p, 9, 4, blood_hi)
    px(p, 10, 4, blood)
    for y in range(5, 12):
        px(p, 5, y, blood_hi)
        px(p, 10, y, blood_dk)
    for x in range(5, 11):
        px(p, x, 11, blood_dk)
    # Gem center highlight
    px(p, 7, 6, gem_bright)
    px(p, 7, 7, gem_bright)
    px(p, 8, 6, (255, 80, 60, 255))
    # Blood drip detail inside
    px(p, 6, 8, blood_dk)
    px(p, 8, 9, blood_dk)
    px(p, 9, 7, blood_dk)
    # Silver setting
    px(p, 5, 5, silver)
    px(p, 10, 5, silver)
    px(p, 5, 10, silver)
    px(p, 10, 10, silver)
    # Chain attachments
    px(p, 3, 7, silver)
    px(p, 2, 7, silver)
    px(p, 12, 7, silver)
    px(p, 13, 7, silver)
    save(img, "bloodstone_choker")

# --- 23. THORNWEAVE GLOVE (green thorny glove) ---
def draw_thornweave_glove():
    img, p = create(16)
    cloth = (60, 80, 45, 255)
    cloth_hi = (80, 105, 60, 255)
    cloth_dk = (40, 55, 30, 255)
    thorn = (120, 90, 50, 255)
    thorn_tip = (160, 120, 70, 255)
    vine = (45, 110, 35, 255)
    leaf = (60, 150, 40, 255)
    # Glove body
    for x in range(4, 12):
        for y in range(5, 13):
            t = (y - 5) / 7.0
            px(p, x, y, lerp(cloth_hi, cloth_dk, t))
    # Fingers
    for i, xx in enumerate([4, 6, 8, 10]):
        h = 3 if i in [1, 2] else 4
        px(p, xx, h, cloth_hi)
        px(p, xx, h + 1, cloth)
        px(p, xx + 1, h, cloth)
        px(p, xx + 1, h + 1, cloth_dk)
    # Thumb
    px(p, 12, 7, cloth)
    px(p, 12, 8, cloth_dk)
    # Thorns protruding from knuckles
    px(p, 5, 3, thorn_tip)
    px(p, 5, 4, thorn)
    px(p, 7, 2, thorn_tip)
    px(p, 7, 3, thorn)
    px(p, 9, 3, thorn_tip)
    px(p, 9, 4, thorn)
    px(p, 11, 4, thorn_tip)
    # Vine wrapping around hand
    px(p, 5, 7, vine)
    px(p, 6, 8, vine)
    px(p, 7, 9, vine)
    px(p, 8, 8, vine)
    px(p, 9, 7, vine)
    px(p, 10, 8, vine)
    # Small leaves
    px(p, 4, 7, leaf)
    px(p, 11, 9, leaf)
    # Wrist cuff
    for x in range(4, 12):
        px(p, x, 12, darken(cloth_dk, 10))
    px(p, 4, 11, vine)
    px(p, 11, 11, vine)
    save(img, "thornweave_glove")

# --- 24. CHRONO GLOVE (time-themed purple glove with gears) ---
def draw_chrono_glove():
    img, p = create(16)
    purple = (90, 60, 130, 255)
    purple_hi = (120, 85, 165, 255)
    purple_dk = (55, 35, 85, 255)
    gold = (200, 170, 50, 255)
    gold_dk = (150, 120, 30, 255)
    clock = (220, 215, 200, 255)
    hand = (40, 35, 30, 255)
    glow = (180, 160, 255, 180)
    # Glove body
    for x in range(4, 12):
        for y in range(5, 13):
            t = (y - 5) / 7.0
            px(p, x, y, lerp(purple_hi, purple_dk, t))
    # Fingers
    for i, xx in enumerate([4, 6, 8, 10]):
        h = 3 if i in [1, 2] else 4
        px(p, xx, h, purple_hi)
        px(p, xx, h + 1, purple)
        px(p, xx + 1, h, purple)
    # Thumb
    px(p, 12, 7, purple)
    px(p, 12, 8, purple_dk)
    # Clock face on back of hand
    fill(p, 5, 6, 10, 11, clock)
    # Clock border (gold)
    for x in range(5, 10):
        px(p, x, 6, gold)
        px(p, x, 10, gold_dk)
    for y in range(6, 11):
        px(p, 5, y, gold)
        px(p, 9, y, gold_dk)
    # Clock center
    px(p, 7, 8, hand)
    # Hour hand
    px(p, 7, 7, hand)
    px(p, 7, 6, gold)
    # Minute hand
    px(p, 8, 8, hand)
    px(p, 9, 8, gold)
    # Hour markers
    px(p, 7, 6, gold)
    px(p, 9, 8, gold)
    px(p, 7, 10, gold)
    px(p, 5, 8, gold)
    # Gear teeth on edges
    px(p, 4, 7, gold)
    px(p, 4, 9, gold)
    px(p, 10, 7, gold_dk)
    px(p, 10, 9, gold_dk)
    # Time glow
    px(p, 6, 7, glow)
    px(p, 8, 9, glow)
    # Cuff with gear pattern
    for x in range(4, 12):
        px(p, x, 12, darken(purple_dk, 10))
    px(p, 5, 12, gold_dk)
    px(p, 8, 12, gold_dk)
    px(p, 10, 12, gold_dk)
    save(img, "chrono_glove")

# --- 25. STORMSTRIDER BOOTS (electric boots) ---
def draw_stormstrider_boots():
    img, p = create(16)
    blue = (60, 80, 140, 255)
    blue_hi = (90, 110, 175, 255)
    blue_dk = (35, 50, 100, 255)
    silver = (180, 185, 200, 255)
    bolt = (255, 255, 130, 255)
    bolt_w = (255, 255, 230, 255)
    sole = (30, 30, 40, 255)
    spark = (180, 220, 255, 200)
    # Boot shape (side view, facing right)
    # Shaft
    for x in range(5, 10):
        for y in range(2, 9):
            t = (y - 2) / 6.0
            px(p, x, y, lerp(blue_hi, blue_dk, t))
    # Foot
    for x in range(4, 13):
        for y in range(9, 12):
            px(p, x, y, blue)
    # Toe
    for x in range(11, 14):
        for y in range(8, 12):
            px(p, x, y, blue)
    px(p, 13, 9, blue)
    px(p, 13, 10, blue)
    # Sole
    for x in range(4, 14):
        px(p, x, 12, sole)
    # Top highlight
    for x in range(5, 10):
        px(p, x, 2, blue_hi)
    px(p, 5, 3, blue_hi)
    # Silver trim band
    for x in range(4, 14):
        px(p, x, 9, silver)
    # Lightning bolt on shaft
    px(p, 7, 3, bolt_w)
    px(p, 6, 4, bolt)
    px(p, 7, 5, bolt_w)
    px(p, 8, 6, bolt)
    px(p, 7, 7, bolt_w)
    # Electric sparks at sole
    px(p, 5, 13, spark)
    px(p, 8, 13, spark)
    px(p, 11, 13, spark)
    px(p, 6, 14, (spark[0], spark[1], spark[2], 100))
    px(p, 10, 14, (spark[0], spark[1], spark[2], 100))
    # Buckle
    px(p, 5, 9, lighten(silver, 15))
    px(p, 6, 9, lighten(silver, 15))
    save(img, "stormstrider_boots")

# --- 26. SANDWALKER TREADS (desert boots with wrappings) ---
def draw_sandwalker_treads():
    img, p = create(16)
    sand = (190, 165, 120, 255)
    sand_hi = (220, 195, 150, 255)
    sand_dk = (150, 125, 85, 255)
    wrap = (170, 150, 110, 255)
    wrap_hi = (200, 180, 140, 255)
    wrap_dk = (130, 110, 75, 255)
    leather = (120, 85, 50, 255)
    sole = (90, 65, 40, 255)
    # Boot shape
    # Shaft
    for x in range(5, 10):
        for y in range(2, 9):
            t = (y - 2) / 6.0
            px(p, x, y, lerp(sand_hi, sand_dk, t))
    # Foot
    for x in range(4, 13):
        for y in range(9, 12):
            px(p, x, y, sand)
    # Toe
    for x in range(11, 14):
        for y in range(8, 12):
            px(p, x, y, sand)
    px(p, 13, 9, sand)
    # Sole
    for x in range(4, 14):
        px(p, x, 12, sole)
    # Cloth wrapping bands
    for x in range(5, 10):
        px(p, x, 3, wrap_hi)
        px(p, x, 4, wrap_dk)
    for x in range(5, 10):
        px(p, x, 6, wrap_hi)
        px(p, x, 7, wrap_dk)
    # Ankle wrapping
    for x in range(4, 13):
        px(p, x, 9, wrap)
    for x in range(4, 13):
        if x % 2 == 0:
            px(p, x, 9, wrap_hi)
    # Leather reinforcements
    px(p, 5, 10, leather)
    px(p, 6, 10, leather)
    px(p, 11, 10, leather)
    px(p, 12, 10, leather)
    # Toe cap
    px(p, 12, 9, leather)
    px(p, 13, 9, leather)
    # Sand particles
    px(p, 4, 13, (sand[0], sand[1], sand[2], 150))
    px(p, 8, 13, (sand[0], sand[1], sand[2], 100))
    px(p, 12, 13, (sand[0], sand[1], sand[2], 150))
    # Top highlight
    for x in range(5, 10):
        px(p, x, 2, sand_hi)
    save(img, "sandwalker_treads")

# --- 27. EMBERSTONE BAND (fire ring) ---
def draw_emberstone_band():
    img, p = create(16)
    stone = (120, 70, 30, 255)
    stone_hi = (160, 100, 50, 255)
    stone_dk = (80, 45, 15, 255)
    ember = (255, 120, 20, 255)
    ember_hi = (255, 180, 60, 255)
    ember_dk = (200, 60, 0, 255)
    glow = (255, 160, 40, 150)
    # Ring band
    for x in range(5, 11):
        px(p, x, 5, stone_hi)
        px(p, x, 6, stone)
    for y in range(6, 11):
        px(p, 4, y, lerp(stone_hi, stone, (y-6)/4.0))
        px(p, 5, y, lerp(stone_hi, stone_dk, (y-6)/4.0))
        px(p, 10, y, lerp(stone, stone_dk, (y-6)/4.0))
        px(p, 11, y, lerp(stone_dk, darken(stone_dk, 15), (y-6)/4.0))
    for x in range(5, 11):
        px(p, x, 10, stone_dk)
        px(p, x, 11, darken(stone_dk, 15))
    # Inner hole
    for x in range(6, 10):
        for y in range(7, 10):
            px(p, x, y, (0, 0, 0, 0))
    # Ember cracks in the stone
    px(p, 5, 7, ember)
    px(p, 6, 6, ember_hi)
    px(p, 8, 5, ember)
    px(p, 10, 8, ember)
    px(p, 9, 10, ember_dk)
    px(p, 4, 9, ember)
    # Central ember gem on top
    px(p, 7, 4, ember_hi)
    px(p, 8, 4, ember)
    px(p, 7, 3, (255, 200, 80, 255))
    px(p, 8, 3, ember_hi)
    # Ember glow
    px(p, 3, 8, glow)
    px(p, 12, 7, glow)
    px(p, 6, 3, (glow[0], glow[1], glow[2], 80))
    save(img, "emberstone_band")

# --- 28. VOID LANTERN (dark iron lantern with purple flame) ---
def draw_void_lantern():
    img, p = create(16)
    iron = (60, 55, 65, 255)
    iron_hi = (85, 80, 90, 255)
    iron_dk = (35, 30, 40, 255)
    void_purple = (120, 40, 180, 255)
    void_hi = (170, 80, 220, 255)
    void_dk = (70, 20, 110, 255)
    void_core = (200, 140, 255, 255)
    chain = (100, 95, 105, 255)
    # Handle/hook at top
    px(p, 7, 0, chain)
    px(p, 8, 0, chain)
    px(p, 6, 1, chain)
    px(p, 9, 1, chain)
    px(p, 7, 1, iron_hi)
    px(p, 8, 1, iron)
    # Lantern top cap
    for x in range(5, 11):
        px(p, x, 2, iron_hi)
        px(p, x, 3, iron)
    # Lantern body frame
    for y in range(4, 12):
        px(p, 4, y, iron_hi)
        px(p, 5, y, iron)
        px(p, 10, y, iron)
        px(p, 11, y, iron_dk)
    # Cross bars
    for x in range(4, 12):
        px(p, x, 4, iron_hi)
        px(p, x, 7, iron)
        px(p, x, 11, iron_dk)
    # Void flame inside (rows 5-10, cols 6-9)
    for y in range(5, 11):
        for x in range(6, 10):
            t = (y - 5) / 5.0
            c = lerp(void_core, void_dk, t)
            px(p, x, y, c)
    # Flame shape — brighter at center
    px(p, 7, 5, void_core)
    px(p, 8, 5, void_hi)
    px(p, 7, 6, void_hi)
    px(p, 8, 6, void_core)
    px(p, 7, 4, (void_core[0], void_core[1], void_core[2], 200))
    # Flame flicker
    px(p, 6, 6, void_hi)
    px(p, 9, 7, void_hi)
    # Base
    for x in range(5, 11):
        px(p, x, 12, iron_dk)
        px(p, x, 13, darken(iron_dk, 10))
    # Feet
    px(p, 5, 13, iron)
    px(p, 10, 13, iron)
    # Glow around lantern
    px(p, 3, 7, (void_purple[0], void_purple[1], void_purple[2], 60))
    px(p, 12, 7, (void_purple[0], void_purple[1], void_purple[2], 60))
    save(img, "void_lantern")

# --- 29. THUNDERHORN (war horn with lightning) ---
def draw_thunderhorn():
    img, p = create(16)
    bronze = (180, 140, 60, 255)
    bronze_hi = (220, 180, 90, 255)
    bronze_dk = (130, 95, 35, 255)
    gold = (240, 210, 80, 255)
    leather_wrap = (90, 60, 35, 255)
    bolt = (255, 255, 130, 255)
    bolt_w = (255, 255, 230, 255)
    mouth = (50, 35, 20, 255)
    # Horn shape — curved, bell facing right
    # Bell opening (right side)
    for y in range(3, 11):
        t = (y - 3) / 7.0
        px(p, 13, y, bronze_hi)
        px(p, 14, y, bronze)
        if 4 <= y <= 9:
            px(p, 15, y, bronze_dk)
    # Horn body — tapers left
    for x in range(3, 14):
        t = (x - 3) / 10.0
        w = int(1 + t * 3)
        cy = 7
        for dy in range(-w, w + 1):
            yy = cy + dy
            if dy <= -w + 1:
                c = bronze_hi
            elif dy >= w - 1:
                c = bronze_dk
            else:
                c = bronze
            px(p, x, yy, c)
    # Bell interior (dark)
    for y in range(5, 9):
        px(p, 14, y, mouth)
        px(p, 15, y, mouth)
    px(p, 13, 6, mouth)
    px(p, 13, 7, mouth)
    px(p, 13, 8, mouth)
    # Mouthpiece (left tip)
    px(p, 2, 7, bronze_hi)
    px(p, 1, 7, bronze)
    px(p, 1, 6, bronze_hi)
    # Leather wrapping
    for x in range(5, 9):
        px(p, x, 6, leather_wrap)
        px(p, x, 8, leather_wrap)
    # Gold band at bell
    for y in range(3, 11):
        px(p, 12, y, gold)
    # Lightning bolt engravings
    px(p, 8, 5, bolt)
    px(p, 9, 6, bolt_w)
    px(p, 10, 5, bolt)
    px(p, 9, 7, bolt)
    # Rim highlights
    px(p, 13, 3, lighten(bronze_hi, 15))
    px(p, 14, 3, lighten(bronze_hi, 10))
    save(img, "thunderhorn")

# --- 30. MENDING CHALICE (golden goblet with green glow) ---
def draw_mending_chalice():
    img, p = create(16)
    gold = (210, 175, 50, 255)
    gold_hi = (250, 220, 100, 255)
    gold_dk = (160, 130, 25, 255)
    green = (80, 220, 80, 255)
    green_hi = (140, 255, 140, 255)
    green_glow = (60, 180, 60, 150)
    gem = (100, 255, 120, 255)
    stem = (170, 140, 40, 255)
    # Cup bowl (top, wide)
    for x in range(3, 13):
        px(p, x, 2, gold_hi)
        px(p, x, 3, gold)
    for y in range(4, 8):
        w = 5 - int((y - 4) * 0.8)
        for x in range(8 - w, 8 + w):
            t = (y - 4) / 3.0
            px(p, x, y, lerp(gold_hi, gold_dk, t))
    # Cup rim highlight
    for x in range(3, 13):
        px(p, x, 2, lighten(gold_hi, 20))
    # Left edge highlight
    for y in range(3, 7):
        px(p, 3 + int((y-3)*0.8), y, lighten(gold_hi, 10))
    # Green healing liquid inside
    for x in range(4, 12):
        px(p, x, 3, green)
    for x in range(5, 11):
        px(p, x, 4, green)
    px(p, 7, 3, green_hi)
    px(p, 8, 3, green_hi)
    # Healing sparkle
    px(p, 6, 3, gem)
    px(p, 9, 4, (gem[0], gem[1], gem[2], 200))
    # Glow above cup
    px(p, 7, 1, green_glow)
    px(p, 8, 1, green_glow)
    px(p, 6, 1, (green_glow[0], green_glow[1], green_glow[2], 80))
    # Stem
    for y in range(8, 12):
        px(p, 7, y, stem)
        px(p, 8, y, gold_dk)
    # Base (wide)
    for x in range(4, 12):
        px(p, x, 12, gold)
        px(p, x, 13, gold_dk)
    px(p, 4, 12, gold_hi)
    px(p, 11, 13, darken(gold_dk, 15))
    # Gem on cup
    px(p, 5, 5, gem)
    px(p, 10, 5, (gem[0], gem[1], gem[2], 200))
    save(img, "mending_chalice")


# ============================================================
# WEAPONS — Core RPG (8 items, 32x32)
# ============================================================

# --- W1. VOIDREAVER (dark void greatsword) ---
def draw_voidreaver():
    S = 32
    img, p = create(S)
    void = (40, 15, 70, 255)
    void_hi = (70, 30, 110, 255)
    void_dk = (20, 5, 40, 255)
    edge = (120, 60, 180, 255)
    edge_hi = (160, 100, 220, 255)
    core = (180, 120, 255, 255)
    handle = (50, 40, 55, 255)
    handle_dk = (30, 25, 35, 255)
    guard = (100, 80, 130, 255)
    guard_hi = (140, 110, 170, 255)
    pommel = (80, 50, 120, 255)
    # Blade (diagonal, bottom-left to top-right)
    for i in range(20):
        x = 8 + i
        y = 26 - int(i * 1.15)
        t = i / 19.0
        # Blade width tapers
        w = max(1, int(3 - t * 2))
        for dy in range(-w, w + 1):
            if dy <= -w + 1:
                c = edge_hi if t < 0.5 else edge
            elif dy >= w - 1:
                c = void_dk
            else:
                c = lerp(void_hi, void, t)
            px(p, x, y + dy, c, S)
    # Blade edge glow
    for i in range(0, 20, 2):
        x = 8 + i
        y = 26 - int(i * 1.15)
        px(p, x - 1, y - 2, (core[0], core[1], core[2], 100), S)
    # Void energy wisps
    px(p, 15, 16, core, S)
    px(p, 18, 12, (core[0], core[1], core[2], 180), S)
    px(p, 22, 8, core, S)
    px(p, 12, 20, (core[0], core[1], core[2], 120), S)
    # Cross guard
    for x in range(4, 12):
        for dy in range(3):
            t = dy / 2.0
            px(p, x, 26 + dy, lerp(guard_hi, guard, t), S)
    # Handle
    for i in range(5):
        x = 5 - i
        y = 28 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_dk, S)
        if i % 2 == 0:
            px(p, x, y, lighten(handle, 15), S)
    # Pommel
    px(p, 1, 31, pommel, S)
    px(p, 0, 31, lighten(pommel, 15), S)
    px(p, 2, 31, darken(pommel, 15), S)
    px(p, 1, 30, pommel, S)
    save(img, "voidreaver")

# --- W2. SOLARIS (radiant golden longsword) ---
def draw_solaris():
    S = 32
    img, p = create(S)
    gold = (255, 215, 0, 255)
    gold_hi = (255, 240, 120, 255)
    gold_dk = (200, 160, 0, 255)
    blade = (255, 250, 200, 255)
    blade_hi = (255, 255, 240, 255)
    blade_dk = (230, 210, 150, 255)
    ray = (255, 235, 100, 200)
    handle = (140, 100, 30, 255)
    handle_hi = (170, 130, 50, 255)
    sun = (255, 220, 80, 255)
    # Blade (diagonal)
    for i in range(18):
        x = 9 + i
        y = 24 - int(i * 1.1)
        t = i / 17.0
        w = max(1, int(2.5 - t * 1.5))
        for dy in range(-w, w + 1):
            if dy <= -w + 1:
                c = blade_hi
            elif dy >= w - 1:
                c = blade_dk
            else:
                c = lerp(blade_hi, blade_dk, 0.5)
            px(p, x, y + dy, c, S)
    # Blade edge highlight
    for i in range(18):
        x = 9 + i
        y = 24 - int(i * 1.1)
        px(p, x, y - 2, gold_hi, S)
    # Sun rays emanating from blade tip
    px(p, 27, 3, ray, S)
    px(p, 28, 2, ray, S)
    px(p, 29, 4, ray, S)
    px(p, 26, 2, (ray[0], ray[1], ray[2], 120), S)
    # Cross guard (ornate, golden)
    for x in range(4, 14):
        px(p, x, 25, gold_hi, S)
        px(p, x, 26, gold, S)
        px(p, x, 27, gold_dk, S)
    # Sun disc on guard center
    px(p, 8, 25, sun, S)
    px(p, 9, 25, sun, S)
    px(p, 8, 26, lighten(sun, 30), S)
    px(p, 9, 26, sun, S)
    # Handle
    for i in range(5):
        x = 6 - i
        y = 27 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_hi, S)
        if i % 2 == 0:
            px(p, x, y, lighten(handle, 20), S)
    # Pommel (golden sun)
    px(p, 1, 31, gold, S)
    px(p, 2, 31, gold_dk, S)
    px(p, 0, 31, gold_hi, S)
    px(p, 1, 30, gold_hi, S)
    save(img, "solaris")

# --- W3. STORMFURY (electric lightning blade) ---
def draw_stormfury():
    S = 32
    img, p = create(S)
    steel = (170, 180, 200, 255)
    steel_hi = (210, 220, 240, 255)
    steel_dk = (120, 130, 150, 255)
    bolt = (255, 255, 130, 255)
    bolt_w = (255, 255, 240, 255)
    elec = (100, 180, 255, 255)
    handle = (60, 65, 80, 255)
    handle_hi = (90, 95, 110, 255)
    guard = (130, 140, 165, 255)
    # Blade (diagonal with jagged lightning edge)
    for i in range(18):
        x = 9 + i
        y = 24 - int(i * 1.1)
        t = i / 17.0
        w = max(1, int(2.5 - t * 1.5))
        for dy in range(-w, w + 1):
            if dy <= -w + 1:
                c = steel_hi
            elif dy >= w - 1:
                c = steel_dk
            else:
                c = steel
            px(p, x, y + dy, c, S)
    # Lightning bolt running through blade
    for i in range(0, 18, 2):
        x = 9 + i
        y = 24 - int(i * 1.1)
        px(p, x, y, bolt_w, S)
        if i + 1 < 18:
            px(p, x + 1, y - 1, bolt, S)
    # Electric sparks
    px(p, 27, 4, bolt_w, S)
    px(p, 28, 3, elec, S)
    px(p, 26, 2, bolt, S)
    px(p, 14, 18, (elec[0], elec[1], elec[2], 120), S)
    px(p, 20, 11, (elec[0], elec[1], elec[2], 120), S)
    # Cross guard
    for x in range(4, 14):
        px(p, x, 25, guard, S)
        px(p, x, 26, darken(guard, 15), S)
    # Guard bolt accents
    px(p, 4, 25, bolt, S)
    px(p, 13, 25, bolt, S)
    # Handle
    for i in range(5):
        x = 6 - i
        y = 27 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_hi, S)
        if i % 2 == 0:
            px(p, x, y, lighten(handle, 10), S)
    # Pommel
    px(p, 1, 31, guard, S)
    px(p, 0, 31, lighten(guard, 15), S)
    px(p, 2, 31, darken(guard, 15), S)
    save(img, "stormfury")

# --- W4. BRIARTHORN (living wood thorny mace) ---
def draw_briarthorn():
    S = 32
    img, p = create(S)
    wood = (95, 70, 35, 255)
    wood_hi = (125, 95, 50, 255)
    wood_dk = (65, 45, 20, 255)
    bark = (80, 55, 25, 255)
    thorn = (140, 110, 50, 255)
    thorn_tip = (180, 150, 80, 255)
    vine = (50, 130, 40, 255)
    vine_hi = (70, 170, 55, 255)
    leaf = (60, 160, 45, 255)
    # Handle (diagonal, thick wood)
    for i in range(14):
        x = 6 + int(i * 0.5)
        y = 18 + i
        for dx in range(-1, 2):
            for dy in range(-1, 1):
                t = i / 13.0
                c = lerp(wood_hi, wood_dk, t)
                if dx == -1:
                    c = wood_hi
                elif dx == 1:
                    c = wood_dk
                px(p, x + dx, y + dy, c, S)
    # Head (thorny ball at top)
    cx, cy = 10, 12
    for dy in range(-4, 5):
        for dx in range(-4, 5):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < 4.5:
                t = dist / 4.5
                c = lerp(wood_hi, bark, t)
                px(p, cx + dx, cy + dy, c, S)
    # Thorns protruding from head
    thorn_positions = [(-5, -2), (-4, -4), (-2, -5), (1, -5), (3, -4), (5, -2), (5, 1), (4, 3), (2, 5), (-1, 5), (-3, 4), (-5, 1)]
    for tx, ty in thorn_positions:
        px(p, cx + tx, cy + ty, thorn, S)
        # Thorn tips further out
        tx2 = tx + (1 if tx > 0 else -1 if tx < 0 else 0)
        ty2 = ty + (1 if ty > 0 else -1 if ty < 0 else 0)
        px(p, cx + tx2, cy + ty2, thorn_tip, S)
    # Vine wrapping handle
    for i in range(0, 14, 3):
        x = 6 + int(i * 0.5)
        y = 18 + i
        px(p, x - 2, y, vine, S)
        px(p, x + 2, y + 1, vine, S)
    # Leaves
    px(p, 4, 20, leaf, S)
    px(p, 3, 19, vine_hi, S)
    px(p, 15, 22, leaf, S)
    # Bark texture on head
    px(p, 9, 11, bark, S)
    px(p, 11, 13, bark, S)
    save(img, "briarthorn")

# --- W5. ABYSSAL TRIDENT (deep ocean trident) ---
def draw_abyssal_trident():
    S = 32
    img, p = create(S)
    deep = (25, 50, 100, 255)
    deep_hi = (40, 75, 140, 255)
    deep_dk = (15, 30, 65, 255)
    coral = (120, 90, 80, 255)
    aqua = (60, 180, 200, 255)
    aqua_hi = (100, 220, 240, 255)
    pearl = (220, 230, 240, 255)
    shaft = (50, 70, 110, 255)
    shaft_hi = (70, 95, 140, 255)
    # Shaft (vertical-ish diagonal)
    for i in range(18):
        x = 13 + int(i * 0.2)
        y = 14 + i
        for dx in range(-1, 2):
            c = shaft_hi if dx == -1 else shaft if dx == 0 else deep_dk
            px(p, x + dx, y, c, S)
    # Three prongs
    # Center prong (tallest)
    for y in range(2, 14):
        t = y / 12.0
        px(p, 14, y, lerp(aqua_hi, deep_hi, t), S)
        px(p, 15, y, lerp(deep_hi, deep_dk, t), S)
    # Prong tip
    px(p, 14, 1, aqua_hi, S)
    px(p, 14, 0, aqua, S)
    # Left prong
    for y in range(4, 14):
        t = y / 12.0
        px(p, 10, y, lerp(aqua_hi, deep, t), S)
        px(p, 11, y, lerp(deep_hi, deep_dk, t), S)
    px(p, 10, 3, aqua, S)
    # Right prong
    for y in range(4, 14):
        t = y / 12.0
        px(p, 18, y, lerp(aqua_hi, deep, t), S)
        px(p, 19, y, lerp(deep_hi, deep_dk, t), S)
    px(p, 18, 3, aqua, S)
    # Prong barbs
    px(p, 9, 5, deep_hi, S)
    px(p, 20, 5, deep_hi, S)
    px(p, 13, 3, deep_hi, S)
    px(p, 16, 3, deep_hi, S)
    # Cross piece connecting prongs
    for x in range(10, 20):
        px(p, x, 14, deep_hi, S)
        px(p, x, 15, deep, S)
    # Coral growth
    px(p, 12, 16, coral, S)
    px(p, 11, 17, coral, S)
    px(p, 17, 15, coral, S)
    # Pearl decoration
    px(p, 14, 14, pearl, S)
    px(p, 15, 14, (pearl[0]-20, pearl[1]-15, pearl[2]-10, 255), S)
    # Barnacle
    px(p, 13, 20, coral, S)
    # Bubbles
    px(p, 8, 8, (aqua[0], aqua[1], aqua[2], 100), S)
    px(p, 21, 6, (aqua[0], aqua[1], aqua[2], 80), S)
    save(img, "abyssal_trident")

# --- W6. PYROCLAST (volcanic battle axe) ---
def draw_pyroclast():
    S = 32
    img, p = create(S)
    obsidian = (30, 25, 35, 255)
    obsidian_hi = (55, 45, 60, 255)
    obsidian_dk = (15, 12, 18, 255)
    magma = (255, 100, 0, 255)
    magma_hi = (255, 180, 40, 255)
    magma_dk = (200, 50, 0, 255)
    ember = (255, 200, 60, 200)
    handle = (80, 55, 35, 255)
    handle_hi = (110, 80, 50, 255)
    # Handle (diagonal)
    for i in range(14):
        x = 6 + int(i * 0.4)
        y = 18 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_hi, S)
        if i % 3 == 0:
            px(p, x, y, lighten(handle, 15), S)
    # Axe head (large, top-right)
    # Main blade shape
    for dy in range(-6, 7):
        for dx in range(0, 8 - abs(dy)):
            x = 12 + dx
            y = 14 + dy
            t = dx / 7.0
            if dy < 0:
                c = obsidian_hi
            elif dy > 3:
                c = obsidian_dk
            else:
                c = obsidian
            px(p, x, y, c, S)
    # Magma cracks through the obsidian
    px(p, 13, 12, magma, S)
    px(p, 14, 13, magma_hi, S)
    px(p, 15, 14, magma, S)
    px(p, 16, 15, magma_dk, S)
    px(p, 14, 16, magma, S)
    px(p, 13, 15, magma_hi, S)
    px(p, 15, 11, magma, S)
    px(p, 17, 13, magma, S)
    px(p, 16, 17, magma, S)
    # Blade edge glow
    for dy in range(-5, 6):
        x = 12 + 7 - abs(dy)
        y = 14 + dy
        px(p, x, y, magma_dk, S)
        px(p, x + 1, y, magma, S)
    # Ember particles
    px(p, 20, 9, ember, S)
    px(p, 22, 12, (ember[0], ember[1], ember[2], 120), S)
    px(p, 18, 7, ember, S)
    px(p, 21, 18, (ember[0], ember[1], ember[2], 80), S)
    # Handle binding at head
    for dy in range(-1, 2):
        px(p, 11, 14 + dy, (100, 90, 70, 255), S)
    save(img, "pyroclast")

# --- W7. WHISPERWIND (ethereal wind staff) ---
def draw_whisperwind():
    S = 32
    img, p = create(S)
    silver = (190, 200, 215, 255)
    silver_hi = (220, 230, 245, 255)
    silver_dk = (140, 150, 165, 255)
    wind = (180, 210, 240, 200)
    wind_hi = (210, 235, 255, 220)
    crystal = (200, 230, 255, 255)
    crystal_core = (240, 250, 255, 255)
    wood = (160, 150, 140, 255)
    wood_hi = (190, 180, 170, 255)
    # Staff shaft (mostly vertical)
    for y in range(8, 30):
        t = (y - 8) / 21.0
        px(p, 15, y, lerp(silver_hi, wood, t), S)
        px(p, 16, y, lerp(silver, darken(wood, 15), t), S)
    # Crystal at top (wind-swirl orb)
    cx, cy = 15, 5
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < 3.5:
                t = dist / 3.5
                c = lerp(crystal_core, crystal, t)
                px(p, cx + dx, cy + dy, c, S)
    # Crystal bright center
    px(p, 14, 4, crystal_core, S)
    px(p, 15, 5, crystal_core, S)
    # Wind swirl lines around crystal
    swirl = [(11, 3), (12, 2), (13, 2), (18, 3), (19, 4), (19, 5), (18, 7), (17, 8), (12, 7), (11, 6)]
    for sx, sy in swirl:
        px(p, sx, sy, wind, S)
    # Extended wind trails
    px(p, 10, 2, wind_hi, S)
    px(p, 20, 3, wind_hi, S)
    px(p, 9, 4, (wind[0], wind[1], wind[2], 100), S)
    px(p, 21, 5, (wind[0], wind[1], wind[2], 100), S)
    # Prong frame holding crystal
    px(p, 13, 7, silver_hi, S)
    px(p, 14, 8, silver, S)
    px(p, 17, 7, silver, S)
    px(p, 16, 8, silver_dk, S)
    # Staff wrapping
    for y in range(14, 28, 3):
        px(p, 14, y, lighten(wood, 15), S)
        px(p, 17, y + 1, darken(wood, 10), S)
    # Foot cap
    px(p, 15, 30, silver_dk, S)
    px(p, 16, 30, silver_dk, S)
    px(p, 15, 31, darken(silver_dk, 10), S)
    px(p, 16, 31, darken(silver_dk, 10), S)
    save(img, "whisperwind")

# --- W8. SOULCHAIN (dark chain flail with ghost orb) ---
def draw_soulchain():
    S = 32
    img, p = create(S)
    iron = (80, 75, 90, 255)
    iron_hi = (110, 105, 120, 255)
    iron_dk = (50, 45, 60, 255)
    soul = (140, 180, 220, 255)
    soul_hi = (180, 210, 240, 255)
    soul_core = (220, 240, 255, 255)
    ghost = (120, 150, 200, 180)
    handle = (55, 45, 40, 255)
    handle_hi = (80, 70, 60, 255)
    chain = (100, 95, 110, 255)
    # Handle (bottom-left)
    for i in range(6):
        x = 3 + i
        y = 26 + int(i * 0.5)
        px(p, x, y, handle, S)
        px(p, x, y + 1, handle_hi, S)
        if i % 2 == 0:
            px(p, x, y, lighten(handle, 12), S)
    # Chain links (diagonal from handle to orb)
    chain_path = [(9, 24), (11, 22), (13, 20), (15, 18), (17, 16), (19, 14)]
    for i, (cx, cy) in enumerate(chain_path):
        # Each link is a small oval
        px(p, cx, cy, chain, S)
        px(p, cx + 1, cy, iron_hi, S)
        px(p, cx, cy + 1, iron_dk, S)
        px(p, cx + 1, cy + 1, chain, S)
    # Soul orb at end
    cx, cy = 22, 10
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist < 3.5:
                t = dist / 3.5
                c = lerp(soul_core, soul, t)
                px(p, cx + dx, cy + dy, c, S)
    # Ghost face in orb
    px(p, 21, 9, iron_dk, S)
    px(p, 23, 9, iron_dk, S)
    px(p, 22, 11, iron_dk, S)
    # Soul wisps
    px(p, 19, 7, ghost, S)
    px(p, 25, 8, ghost, S)
    px(p, 20, 13, ghost, S)
    px(p, 24, 12, ghost, S)
    px(p, 18, 6, (ghost[0], ghost[1], ghost[2], 100), S)
    px(p, 26, 7, (ghost[0], ghost[1], ghost[2], 80), S)
    # Handle pommel
    px(p, 2, 27, iron_hi, S)
    px(p, 2, 28, iron, S)
    save(img, "soulchain")


# ============================================================
# WEAPONS — New Arsenal (33 items)
# ============================================================

# --- Whips (16x16) ---
def draw_unique_whip_1():  # Serpent's Lash — green snake whip
    img, p = create(16)
    green = (40, 130, 50, 255)
    green_hi = (60, 170, 70, 255)
    green_dk = (25, 85, 30, 255)
    scale = (50, 150, 60, 255)
    eye = (255, 200, 0, 255)
    fang = (240, 235, 220, 255)
    handle = (90, 65, 35, 255)
    handle_hi = (120, 90, 50, 255)
    # Handle
    for y in range(10, 15):
        px(p, 2, y, handle)
        px(p, 3, y, handle_hi)
    px(p, 2, 10, handle_hi)
    # Whip body — sinuous curve
    path = [(3,9),(4,8),(5,7),(6,7),(7,6),(8,5),(9,5),(10,4),(11,4),(12,3),(13,3)]
    for i, (x, y) in enumerate(path):
        t = i / 10.0
        c = lerp(green_hi, green_dk, t)
        px(p, x, y, c)
        # Scale pattern
        if i % 2 == 0:
            px(p, x, y - 1, scale)
    # Snake head at tip
    px(p, 14, 2, green_hi)
    px(p, 14, 3, green)
    px(p, 15, 2, green)
    px(p, 15, 1, eye)
    px(p, 15, 3, fang)
    # Tongue
    px(p, 15, 0, (200, 40, 30, 200))
    save(img, "unique_whip_1")

def draw_unique_whip_2():  # Flamelash — fiery whip
    img, p = create(16)
    red = (200, 40, 20, 255)
    red_hi = (240, 80, 30, 255)
    orange = (255, 140, 30, 255)
    yellow = (255, 210, 60, 255)
    handle = (60, 40, 30, 255)
    handle_hi = (90, 65, 45, 255)
    ember = (255, 180, 40, 180)
    for y in range(10, 15):
        px(p, 2, y, handle)
        px(p, 3, y, handle_hi)
    px(p, 2, 10, handle_hi)
    path = [(3,9),(4,8),(5,7),(6,7),(7,6),(8,5),(9,5),(10,4),(11,3),(12,3),(13,2)]
    for i, (x, y) in enumerate(path):
        t = i / 10.0
        c = lerp(red, yellow, t)
        px(p, x, y, c)
        if i % 2 == 0:
            px(p, x, y - 1, orange)
    px(p, 14, 1, yellow)
    px(p, 14, 2, orange)
    px(p, 15, 1, (255, 255, 180, 255))
    # Fire flicker
    px(p, 13, 1, ember)
    px(p, 15, 0, (ember[0], ember[1], ember[2], 100))
    px(p, 5, 6, ember)
    px(p, 8, 4, ember)
    save(img, "unique_whip_2")

def draw_unique_whip_sw():  # Thornwhip of the Verdant Warden
    img, p = create(16)
    brown = (100, 70, 35, 255)
    brown_hi = (130, 95, 50, 255)
    brown_dk = (70, 48, 22, 255)
    thorn = (150, 120, 60, 255)
    vine = (50, 130, 40, 255)
    leaf = (60, 160, 45, 255)
    handle = (80, 55, 30, 255)
    for y in range(10, 15):
        px(p, 2, y, handle)
        px(p, 3, y, lighten(handle, 15))
    path = [(3,9),(4,8),(5,8),(6,7),(7,6),(8,6),(9,5),(10,5),(11,4),(12,3),(13,3)]
    for i, (x, y) in enumerate(path):
        t = i / 10.0
        px(p, x, y, lerp(brown_hi, brown_dk, t))
    # Thorns
    for i in range(0, 11, 2):
        x, y = path[i]
        px(p, x, y - 1, thorn)
    # Leaf at tip
    px(p, 14, 2, leaf)
    px(p, 14, 3, vine)
    px(p, 13, 2, vine)
    # Vine wrapping handle
    px(p, 2, 12, vine)
    px(p, 3, 11, vine)
    save(img, "unique_whip_sw")

# --- Wands (16x16) ---
def draw_unique_wand_1():  # Arcane Focus — purple crystal wand
    img, p = create(16)
    wood = (90, 60, 45, 255)
    wood_hi = (120, 85, 60, 255)
    crystal = (160, 80, 220, 255)
    crystal_hi = (200, 140, 255, 255)
    crystal_dk = (100, 40, 150, 255)
    glow = (180, 120, 255, 150)
    gold = (200, 170, 50, 255)
    # Shaft (diagonal)
    for i in range(10):
        x = 4 + int(i * 0.6)
        y = 13 - i
        px(p, x, y, wood)
        px(p, x + 1, y, wood_hi)
    # Gold band
    px(p, 7, 7, gold)
    px(p, 8, 6, gold)
    # Crystal tip
    px(p, 9, 4, crystal_hi)
    px(p, 10, 3, crystal)
    px(p, 10, 2, crystal_hi)
    px(p, 11, 2, crystal)
    px(p, 11, 1, crystal_dk)
    px(p, 10, 1, crystal_hi)
    # Glow
    px(p, 9, 2, glow)
    px(p, 12, 1, glow)
    px(p, 11, 0, (glow[0], glow[1], glow[2], 80))
    # Pommel
    px(p, 3, 14, darken(wood, 15))
    px(p, 4, 14, wood)
    save(img, "unique_wand_1")

def draw_unique_wand_2():  # Frostfinger — ice wand
    img, p = create(16)
    ice = (140, 200, 240, 255)
    ice_hi = (200, 230, 255, 255)
    ice_dk = (80, 140, 200, 255)
    frost = (220, 240, 255, 200)
    wood = (100, 90, 110, 255)
    wood_hi = (130, 120, 140, 255)
    silver = (180, 185, 195, 255)
    for i in range(10):
        x = 4 + int(i * 0.6)
        y = 13 - i
        px(p, x, y, wood)
        px(p, x + 1, y, wood_hi)
    px(p, 7, 7, silver)
    px(p, 8, 6, silver)
    # Ice crystal tip
    px(p, 9, 4, ice_hi)
    px(p, 10, 3, ice)
    px(p, 10, 2, ice_hi)
    px(p, 11, 2, ice_dk)
    px(p, 11, 1, ice_hi)
    px(p, 10, 1, (255, 255, 255, 255))
    # Frost sparkles
    px(p, 9, 2, frost)
    px(p, 12, 1, frost)
    px(p, 8, 3, (frost[0], frost[1], frost[2], 100))
    px(p, 3, 14, darken(wood, 15))
    save(img, "unique_wand_2")

def draw_unique_wand_sw():  # Star Conduit — golden star wand
    img, p = create(16)
    gold = (200, 170, 50, 255)
    gold_hi = (255, 220, 100, 255)
    star = (255, 255, 200, 255)
    star_glow = (255, 240, 160, 200)
    wood = (120, 100, 60, 255)
    wood_hi = (150, 130, 80, 255)
    for i in range(10):
        x = 4 + int(i * 0.6)
        y = 13 - i
        px(p, x, y, wood)
        px(p, x + 1, y, wood_hi)
    px(p, 7, 7, gold)
    px(p, 8, 6, gold)
    # Star at tip
    px(p, 10, 1, star)
    px(p, 10, 2, gold_hi)
    px(p, 10, 3, star)
    px(p, 9, 2, star)
    px(p, 11, 2, star)
    px(p, 10, 0, star_glow)
    px(p, 12, 2, star_glow)
    px(p, 8, 2, star_glow)
    px(p, 9, 1, gold_hi)
    px(p, 11, 1, gold)
    px(p, 3, 14, darken(wood, 15))
    save(img, "unique_wand_sw")

# --- Katanas (32x32) ---
def draw_unique_katana_1():  # Windcutter — silver/gray katana
    S = 32
    img, p = create(S)
    blade = (200, 205, 215, 255)
    blade_hi = (235, 240, 250, 255)
    blade_dk = (155, 160, 170, 255)
    edge = (240, 245, 255, 255)
    handle = (40, 35, 50, 255)
    handle_hi = (65, 58, 75, 255)
    wrap = (220, 215, 200, 255)
    guard = (120, 115, 130, 255)
    wind = (180, 210, 240, 150)
    # Long curved blade
    for i in range(22):
        x = 8 + i
        y = 26 - int(i * 1.05) - (1 if 8 < i < 18 else 0)  # slight curve
        t = i / 21.0
        w = 2 if i < 18 else 1
        for dy in range(-w, w + 1):
            if dy == -w:
                c = blade_hi
            elif dy == w:
                c = blade_dk
            else:
                c = blade
            px(p, x, y + dy, c, S)
    # Blade edge highlight (hamon line)
    for i in range(20):
        x = 9 + i
        y = 26 - int(i * 1.05) - (1 if 8 < i < 18 else 0)
        px(p, x, y - 1, edge, S)
    # Tsuba (guard — small round)
    for dx in range(-2, 3):
        for dy in range(-1, 2):
            px(p, 8 + dx, 27 + dy, guard, S)
    # Handle wrapping
    for i in range(5):
        x = 5 - int(i * 0.4)
        y = 28 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_hi, S)
        if i % 2 == 0:
            px(p, x, y, wrap, S)
    # Wind trail
    px(p, 25, 5, wind, S)
    px(p, 27, 3, wind, S)
    px(p, 28, 2, (wind[0], wind[1], wind[2], 80), S)
    save(img, "unique_katana_1")

def draw_unique_katana_2():  # Shadowmoon Blade — dark purple katana
    S = 32
    img, p = create(S)
    blade = (80, 60, 120, 255)
    blade_hi = (120, 90, 160, 255)
    blade_dk = (45, 30, 70, 255)
    moon = (200, 210, 240, 255)
    handle = (30, 20, 40, 255)
    handle_hi = (50, 38, 60, 255)
    wrap = (100, 80, 130, 255)
    guard = (70, 55, 90, 255)
    for i in range(22):
        x = 8 + i
        y = 26 - int(i * 1.05) - (1 if 8 < i < 18 else 0)
        w = 2 if i < 18 else 1
        for dy in range(-w, w + 1):
            if dy == -w: c = blade_hi
            elif dy == w: c = blade_dk
            else: c = blade
            px(p, x, y + dy, c, S)
    # Moon reflection on blade
    px(p, 18, 15, moon, S)
    px(p, 19, 14, (moon[0], moon[1], moon[2], 200), S)
    px(p, 17, 16, (moon[0], moon[1], moon[2], 150), S)
    for dx in range(-2, 3):
        for dy in range(-1, 2):
            px(p, 8 + dx, 27 + dy, guard, S)
    for i in range(5):
        x = 5 - int(i * 0.4)
        y = 28 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_hi, S)
        if i % 2 == 0:
            px(p, x, y, wrap, S)
    save(img, "unique_katana_2")

def draw_unique_katana_sw():  # Ashenblade of the Eternal Dusk — dark red/gray
    S = 32
    img, p = create(S)
    blade = (140, 90, 80, 255)
    blade_hi = (180, 120, 105, 255)
    blade_dk = (90, 55, 48, 255)
    ember = (255, 120, 40, 255)
    ash = (100, 90, 85, 255)
    handle = (45, 30, 25, 255)
    handle_hi = (70, 50, 40, 255)
    wrap = (120, 80, 60, 255)
    guard = (80, 60, 55, 255)
    for i in range(22):
        x = 8 + i
        y = 26 - int(i * 1.05) - (1 if 8 < i < 18 else 0)
        w = 2 if i < 18 else 1
        for dy in range(-w, w + 1):
            if dy == -w: c = blade_hi
            elif dy == w: c = blade_dk
            else: c = blade
            px(p, x, y + dy, c, S)
    # Ember cracks
    for i in range(2, 20, 4):
        x = 9 + i
        y = 26 - int(i * 1.05) - (1 if 8 < i < 18 else 0)
        px(p, x, y, ember, S)
    # Ash particles
    px(p, 26, 5, (ash[0], ash[1], ash[2], 120), S)
    px(p, 28, 3, (ash[0], ash[1], ash[2], 80), S)
    for dx in range(-2, 3):
        for dy in range(-1, 2):
            px(p, 8 + dx, 27 + dy, guard, S)
    for i in range(5):
        x = 5 - int(i * 0.4)
        y = 28 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_hi, S)
        if i % 2 == 0: px(p, x, y, wrap, S)
    save(img, "unique_katana_sw")

# --- Greatshields (32x32) ---
def draw_unique_greatshield_1():  # Ironwall — massive iron shield
    S = 32
    img, p = create(S)
    iron = (150, 150, 160, 255)
    iron_hi = (190, 190, 200, 255)
    iron_dk = (100, 100, 110, 255)
    rivet = (80, 80, 88, 255)
    band = (120, 120, 130, 255)
    dark = (60, 60, 68, 255)
    # Shield shape (large rectangle with beveled edges)
    for y in range(2, 30):
        for x in range(4, 28):
            t_y = (y - 2) / 27.0
            t_x = (x - 4) / 23.0
            c = lerp(iron_hi, iron_dk, t_y * 0.7 + t_x * 0.3)
            px(p, x, y, c, S)
    # Beveled edges
    for x in range(4, 28):
        px(p, x, 2, lighten(iron_hi, 15), S)
        px(p, x, 29, dark, S)
    for y in range(2, 30):
        px(p, 4, y, lighten(iron_hi, 10), S)
        px(p, 27, y, darken(iron_dk, 15), S)
    # Horizontal bands
    for x in range(5, 27):
        px(p, x, 10, band, S)
        px(p, x, 20, band, S)
    # Vertical center band
    for y in range(3, 29):
        px(p, 15, y, band, S)
        px(p, 16, y, band, S)
    # Rivets
    for ry in [5, 15, 25]:
        for rx in [8, 16, 23]:
            px(p, rx, ry, rivet, S)
    # Boss (center dome)
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            dist = abs(dx) + abs(dy)
            if dist <= 2:
                c = lighten(iron_hi, 20 - dist * 8)
                px(p, 16 + dx, 15 + dy, c, S)
    save(img, "unique_greatshield_1")

def draw_unique_greatshield_2():  # Aegis of Dawn — golden radiant shield
    S = 32
    img, p = create(S)
    gold = (220, 190, 50, 255)
    gold_hi = (255, 230, 100, 255)
    gold_dk = (170, 140, 20, 255)
    white = (250, 245, 235, 255)
    sun = (255, 240, 150, 255)
    ray = (255, 220, 80, 200)
    for y in range(2, 30):
        for x in range(4, 28):
            t = (y - 2) / 27.0
            px(p, x, y, lerp(gold_hi, gold_dk, t), S)
    for x in range(4, 28):
        px(p, x, 2, lighten(gold_hi, 20), S)
        px(p, x, 29, darken(gold_dk, 20), S)
    for y in range(2, 30):
        px(p, 4, y, lighten(gold_hi, 15), S)
        px(p, 27, y, darken(gold_dk, 15), S)
    # Sun emblem center
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            dist = (dx*dx + dy*dy) ** 0.5
            if dist < 3.5:
                px(p, 16 + dx, 15 + dy, sun, S)
    px(p, 16, 15, white, S)
    px(p, 15, 14, white, S)
    # Rays
    for angle_px in [(16,9),(16,21),(9,15),(23,15),(10,10),(22,10),(10,20),(22,20)]:
        px(p, angle_px[0], angle_px[1], ray, S)
    # Border filigree
    for x in range(6, 26, 3):
        px(p, x, 4, darken(gold, 15), S)
        px(p, x, 27, darken(gold, 15), S)
    save(img, "unique_greatshield_2")

def draw_unique_greatshield_sw():  # Titan's Bulwark — bronze/stone ancient shield
    S = 32
    img, p = create(S)
    bronze = (160, 120, 60, 255)
    bronze_hi = (200, 160, 85, 255)
    bronze_dk = (110, 80, 35, 255)
    stone = (140, 135, 125, 255)
    crack = (90, 85, 75, 255)
    rust = (140, 90, 50, 255)
    for y in range(2, 30):
        for x in range(4, 28):
            t = (y - 2) / 27.0
            px(p, x, y, lerp(bronze_hi, bronze_dk, t), S)
    for x in range(4, 28):
        px(p, x, 2, lighten(bronze_hi, 15), S)
        px(p, x, 29, darken(bronze_dk, 15), S)
    for y in range(2, 30):
        px(p, 4, y, lighten(bronze_hi, 10), S)
        px(p, 27, y, darken(bronze_dk, 10), S)
    # Stone boss
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            if abs(dx) + abs(dy) <= 4:
                px(p, 16 + dx, 15 + dy, stone, S)
    # Cracks
    px(p, 14, 14, crack, S)
    px(p, 13, 13, crack, S)
    px(p, 18, 16, crack, S)
    px(p, 19, 17, crack, S)
    px(p, 10, 20, crack, S)
    # Rust patches
    px(p, 8, 8, rust, S)
    px(p, 22, 24, rust, S)
    px(p, 20, 7, rust, S)
    save(img, "unique_greatshield_sw")

# --- Throwing Axes (16x16) ---
def draw_unique_throwing_axe_1():  # Stormhatchet — electric blue-silver
    img, p = create(16)
    steel = (160, 170, 200, 255)
    steel_hi = (200, 210, 240, 255)
    steel_dk = (110, 120, 150, 255)
    bolt = (255, 255, 130, 255)
    handle = (80, 70, 60, 255)
    handle_hi = (110, 100, 85, 255)
    # Handle
    for y in range(8, 14):
        px(p, 6, y, handle)
        px(p, 7, y, handle_hi)
    # Axe head (right side)
    for dy in range(-3, 4):
        for dx in range(0, 4 - abs(dy)):
            y = 6 + dy
            x = 8 + dx
            c = steel_hi if dy < 0 else steel_dk if dy > 1 else steel
            px(p, x, y, c)
    # Edge highlight
    for dy in range(-2, 3):
        x = 8 + 3 - abs(dy)
        px(p, x, 6 + dy, lighten(steel_hi, 15))
    # Lightning bolt engraving
    px(p, 9, 5, bolt)
    px(p, 10, 6, bolt)
    px(p, 9, 7, bolt)
    # Handle wrapping
    px(p, 6, 9, handle_hi)
    px(p, 6, 11, handle_hi)
    save(img, "unique_throwing_axe_1")

def draw_unique_throwing_axe_2():  # Frostbite Hatchet — icy
    img, p = create(16)
    ice = (130, 190, 240, 255)
    ice_hi = (190, 225, 255, 255)
    ice_dk = (80, 140, 200, 255)
    frost = (220, 240, 255, 200)
    handle = (80, 85, 100, 255)
    for y in range(8, 14):
        px(p, 6, y, handle)
        px(p, 7, y, lighten(handle, 15))
    for dy in range(-3, 4):
        for dx in range(0, 4 - abs(dy)):
            c = ice_hi if dy < 0 else ice_dk if dy > 1 else ice
            px(p, 8 + dx, 6 + dy, c)
    for dy in range(-2, 3):
        x = 8 + 3 - abs(dy)
        px(p, x, 6 + dy, lighten(ice_hi, 10))
    px(p, 9, 5, frost)
    px(p, 11, 6, frost)
    px(p, 10, 8, frost)
    save(img, "unique_throwing_axe_2")

def draw_unique_throwing_axe_sw():  # Galeforce Tomahawk — wind-themed gray
    img, p = create(16)
    steel = (170, 175, 180, 255)
    steel_hi = (200, 205, 210, 255)
    steel_dk = (130, 135, 140, 255)
    wind = (180, 210, 240, 150)
    handle = (100, 80, 55, 255)
    for y in range(8, 14):
        px(p, 6, y, handle)
        px(p, 7, y, lighten(handle, 15))
    for dy in range(-3, 4):
        for dx in range(0, 4 - abs(dy)):
            c = steel_hi if dy < 0 else steel_dk if dy > 1 else steel
            px(p, 8 + dx, 6 + dy, c)
    for dy in range(-2, 3):
        px(p, 8 + 3 - abs(dy), 6 + dy, lighten(steel_hi, 10))
    # Wind trail
    px(p, 12, 4, wind)
    px(p, 13, 3, (wind[0], wind[1], wind[2], 80))
    px(p, 11, 8, wind)
    save(img, "unique_throwing_axe_sw")

# --- Rapiers (32x32) ---
def draw_unique_rapier_1():  # Duelist's Sting — elegant silver rapier
    S = 32
    img, p = create(S)
    blade = (210, 215, 225, 255)
    blade_hi = (240, 245, 255, 255)
    blade_dk = (160, 165, 175, 255)
    guard = (200, 180, 80, 255)
    guard_hi = (230, 210, 110, 255)
    guard_dk = (150, 130, 50, 255)
    handle = (70, 40, 30, 255)
    handle_hi = (100, 65, 45, 255)
    # Long thin blade (diagonal)
    for i in range(24):
        x = 7 + i
        y = 27 - int(i * 1.0)
        px(p, x, y, blade, S)
        px(p, x, y - 1, blade_hi, S)
    # Blade tip
    px(p, 30, 4, blade_hi, S)
    # Ornate swept guard (curved)
    for x in range(3, 12):
        curve_y = 28 - int(abs(x - 7) * 0.3)
        px(p, x, curve_y, guard, S)
        px(p, x, curve_y - 1, guard_hi, S)
        px(p, x, curve_y + 1, guard_dk, S)
    # Shell guard
    for dy in range(0, 3):
        for dx in range(0, 4):
            px(p, 4 + dx, 27 + dy, guard, S)
    px(p, 4, 27, guard_hi, S)
    px(p, 7, 29, guard_dk, S)
    # Handle
    for i in range(4):
        x = 4 - int(i * 0.3)
        y = 29 + i
        px(p, x, y, handle, S)
        px(p, x + 1, y, handle_hi, S)
    # Pommel
    px(p, 3, 31, guard, S)
    px(p, 2, 31, guard_hi, S)
    save(img, "unique_rapier_1")

def draw_unique_rapier_2():  # Venomfang Foil — green poisoned rapier
    S = 32
    img, p = create(S)
    blade = (140, 180, 130, 255)
    blade_hi = (170, 210, 160, 255)
    blade_dk = (100, 140, 90, 255)
    poison = (80, 200, 50, 255)
    drip = (60, 180, 40, 200)
    guard = (100, 90, 80, 255)
    guard_hi = (130, 120, 105, 255)
    handle = (50, 40, 30, 255)
    for i in range(24):
        x = 7 + i
        y = 27 - int(i * 1.0)
        px(p, x, y, blade, S)
        px(p, x, y - 1, blade_hi, S)
    # Poison drips from blade
    for i in range(4, 20, 5):
        x = 7 + i
        y = 27 - int(i * 1.0) + 2
        px(p, x, y, drip, S)
        px(p, x, y + 1, (drip[0], drip[1], drip[2], 120), S)
    # Green tinge on blade
    for i in range(0, 24, 3):
        x = 7 + i
        y = 27 - int(i * 1.0)
        px(p, x, y, poison, S)
    for x in range(3, 12):
        px(p, x, 28, guard, S)
        px(p, x, 27, guard_hi, S)
    for i in range(4):
        px(p, 4 - int(i*0.3), 29 + i, handle, S)
    px(p, 3, 31, guard, S)
    save(img, "unique_rapier_2")

def draw_unique_rapier_sw():  # Silvered Estoc of the Court — platinum elegant
    S = 32
    img, p = create(S)
    plat = (220, 225, 235, 255)
    plat_hi = (245, 250, 255, 255)
    plat_dk = (180, 185, 195, 255)
    gold = (210, 185, 60, 255)
    gold_hi = (240, 215, 90, 255)
    handle = (80, 50, 40, 255)
    gem = (100, 150, 255, 255)
    for i in range(24):
        x = 7 + i
        y = 27 - int(i * 1.0)
        px(p, x, y, plat, S)
        px(p, x, y - 1, plat_hi, S)
    px(p, 30, 4, plat_hi, S)
    for x in range(3, 12):
        curve_y = 28 - int(abs(x - 7) * 0.3)
        px(p, x, curve_y, gold, S)
        px(p, x, curve_y - 1, gold_hi, S)
    for dy in range(0, 3):
        for dx in range(0, 4):
            px(p, 4 + dx, 27 + dy, gold, S)
    # Gem in guard
    px(p, 6, 28, gem, S)
    for i in range(4):
        px(p, 4 - int(i*0.3), 29 + i, handle, S)
    px(p, 3, 31, gold, S)
    px(p, 2, 31, gold_hi, S)
    save(img, "unique_rapier_sw")

# --- Longswords (32x32) ---
def draw_unique_longsword_1():  # Dragonscale-Encrusted Longblade
    S = 32
    img, p = create(S)
    blade = (200, 120, 50, 255)
    blade_hi = (240, 160, 80, 255)
    blade_dk = (150, 80, 30, 255)
    scale = (180, 100, 40, 255)
    scale_hi = (220, 140, 60, 255)
    guard = (160, 100, 30, 255)
    handle = (80, 50, 25, 255)
    handle_hi = (110, 75, 40, 255)
    fire = (255, 140, 30, 200)
    for i in range(20):
        x = 8 + i
        y = 25 - int(i * 1.05)
        w = 2 if i < 16 else 1
        for dy in range(-w, w + 1):
            if dy == -w: c = blade_hi
            elif dy == w: c = blade_dk
            else: c = blade
            px(p, x, y + dy, c, S)
    # Dragon scale pattern on blade
    for i in range(2, 18, 3):
        x = 8 + i
        y = 25 - int(i * 1.05)
        px(p, x, y, scale_hi, S)
        px(p, x + 1, y + 1, scale, S)
    # Fire glow at edge
    px(p, 27, 5, fire, S)
    px(p, 26, 4, (fire[0], fire[1], fire[2], 120), S)
    for x in range(4, 14):
        px(p, x, 26, guard, S)
        px(p, x, 27, darken(guard, 15), S)
    for i in range(5):
        x = 5 - int(i*0.4)
        y = 28 + i
        px(p, x, y, handle, S)
        if i % 2 == 0: px(p, x, y, handle_hi, S)
    px(p, 1, 31, darken(guard, 10), S)
    save(img, "unique_longsword_1")

def draw_unique_longsword_2():  # Hearthguard Blade
    S = 32
    img, p = create(S)
    blade = (190, 170, 140, 255)
    blade_hi = (220, 200, 170, 255)
    blade_dk = (140, 120, 95, 255)
    warm = (200, 140, 60, 255)
    guard = (150, 120, 70, 255)
    handle = (90, 60, 35, 255)
    handle_hi = (120, 85, 50, 255)
    for i in range(20):
        x = 8 + i
        y = 25 - int(i * 1.05)
        w = 2 if i < 16 else 1
        for dy in range(-w, w + 1):
            if dy == -w: c = blade_hi
            elif dy == w: c = blade_dk
            else: c = blade
            px(p, x, y + dy, c, S)
    # Warm glow line
    for i in range(0, 20, 3):
        x = 8 + i
        y = 25 - int(i * 1.05)
        px(p, x, y, warm, S)
    for x in range(4, 14):
        px(p, x, 26, guard, S)
        px(p, x, 27, darken(guard, 15), S)
    # Hearth emblem on guard
    px(p, 8, 26, lighten(warm, 20), S)
    px(p, 9, 26, warm, S)
    for i in range(5):
        x = 5 - int(i*0.4)
        y = 28 + i
        px(p, x, y, handle, S)
        if i % 2 == 0: px(p, x, y, handle_hi, S)
    save(img, "unique_longsword_2")

# --- Fill-in Variants ---
def draw_unique_claymore_3():  # Northwind Greatsword — icy blue
    S = 32
    img, p = create(S)
    blade = (160, 200, 240, 255)
    blade_hi = (200, 230, 255, 255)
    blade_dk = (100, 150, 200, 255)
    frost = (230, 245, 255, 200)
    guard = (130, 160, 190, 255)
    handle = (60, 70, 90, 255)
    ice_crystal = (180, 220, 255, 255)
    for i in range(22):
        x = 7 + i
        y = 26 - int(i * 1.1)
        w = max(1, int(3 - i * 0.1))
        for dy in range(-w, w + 1):
            if dy == -w: c = blade_hi
            elif dy == w: c = blade_dk
            else: c = blade
            px(p, x, y + dy, c, S)
    # Frost particles
    for i in range(2, 20, 4):
        x = 8 + i
        y = 26 - int(i * 1.1)
        px(p, x, y - 2, frost, S)
    # Ice crystals on blade
    px(p, 18, 14, ice_crystal, S)
    px(p, 22, 9, ice_crystal, S)
    for x in range(3, 13):
        px(p, x, 27, guard, S)
        px(p, x, 28, darken(guard, 15), S)
    for i in range(5):
        px(p, 4 - int(i*0.4), 29 + i, handle, S)
    save(img, "unique_claymore_3")

def draw_unique_dagger_3():  # Whispersting — gray silver dagger
    img, p = create(16)
    blade = (200, 205, 215, 255)
    blade_hi = (230, 235, 245, 255)
    blade_dk = (155, 160, 170, 255)
    guard = (150, 145, 140, 255)
    handle = (70, 55, 45, 255)
    handle_hi = (100, 82, 65, 255)
    # Short blade diagonal
    for i in range(8):
        x = 7 + i
        y = 10 - i
        px(p, x, y, blade)
        px(p, x, y - 1, blade_hi)
    px(p, 14, 3, blade_hi)
    # Guard
    for x in range(5, 9):
        px(p, x, 11, guard)
    # Handle
    for i in range(4):
        x = 5 - int(i*0.3)
        y = 12 + i
        px(p, x, y, handle)
        px(p, x + 1, y, handle_hi)
    px(p, 3, 15, guard)
    save(img, "unique_dagger_3")

def draw_unique_double_axe_3():  # Cindercleaver — fiery double axe
    S = 32
    img, p = create(S)
    iron = (100, 80, 70, 255)
    iron_hi = (140, 115, 100, 255)
    iron_dk = (60, 45, 38, 255)
    fire = (255, 120, 20, 255)
    fire_hi = (255, 180, 60, 255)
    ember = (255, 200, 60, 180)
    handle = (80, 55, 35, 255)
    # Handle
    for y in range(10, 28):
        px(p, 15, y, handle, S)
        px(p, 16, y, lighten(handle, 15), S)
    # Top blade
    for dy in range(-5, 1):
        for dx in range(0, 6 - abs(dy)):
            c = iron_hi if dy < -2 else iron if dy < 0 else iron_dk
            px(p, 17 + dx, 8 + dy, c, S)
            px(p, 14 - dx, 8 + dy, c, S)
    # Fire cracks
    px(p, 18, 5, fire, S)
    px(p, 19, 6, fire_hi, S)
    px(p, 12, 5, fire, S)
    px(p, 11, 6, fire_hi, S)
    px(p, 16, 4, fire, S)
    # Ember particles
    px(p, 22, 3, ember, S)
    px(p, 8, 4, ember, S)
    px(p, 20, 7, (ember[0], ember[1], ember[2], 100), S)
    # Blade edge glow
    for dy in range(-4, 1):
        px(p, 22 - abs(dy), 8 + dy, fire, S)
        px(p, 9 + abs(dy), 8 + dy, fire, S)
    save(img, "unique_double_axe_3")

def draw_unique_glaive_3():  # Moonreaver Glaive — silver/blue
    S = 32
    img, p = create(S)
    blade = (180, 190, 220, 255)
    blade_hi = (210, 220, 250, 255)
    blade_dk = (130, 140, 170, 255)
    moon = (200, 210, 240, 255)
    shaft = (100, 85, 70, 255)
    shaft_hi = (130, 110, 90, 255)
    # Long shaft
    for y in range(12, 30):
        px(p, 14, y, shaft, S)
        px(p, 15, y, shaft_hi, S)
    # Blade at top (crescent moon shape)
    for y in range(2, 12):
        t = (y - 2) / 9.0
        w = int(3 + (1 - abs(t - 0.5) * 2) * 3)
        for dx in range(0, w):
            c = lerp(blade_hi, blade_dk, dx / max(1, w))
            px(p, 16 + dx, y, c, S)
    # Crescent curve
    for y in range(4, 10):
        px(p, 16 + int(2 + abs(y - 7) * 0.5), y, blade_hi, S)
    # Moon glow
    px(p, 20, 6, moon, S)
    px(p, 21, 7, (moon[0], moon[1], moon[2], 150), S)
    # Blade back
    for y in range(2, 12):
        px(p, 16, y, darken(blade, 15), S)
    # Connecting piece
    px(p, 14, 11, blade_dk, S)
    px(p, 15, 11, blade, S)
    save(img, "unique_glaive_3")

def draw_unique_hammer_3():  # Frostfall Maul — icy hammer
    S = 32
    img, p = create(S)
    ice = (140, 190, 240, 255)
    ice_hi = (190, 220, 255, 255)
    ice_dk = (90, 140, 200, 255)
    frost = (220, 240, 255, 200)
    handle = (80, 85, 100, 255)
    handle_hi = (110, 115, 130, 255)
    # Handle
    for y in range(14, 30):
        px(p, 15, y, handle, S)
        px(p, 16, y, handle_hi, S)
    # Hammer head (rectangular)
    for y in range(6, 14):
        for x in range(8, 24):
            t_y = (y - 6) / 7.0
            t_x = (x - 8) / 15.0
            c = lerp(ice_hi, ice_dk, t_y * 0.6 + t_x * 0.4)
            px(p, x, y, c, S)
    # Edges
    for x in range(8, 24):
        px(p, x, 6, lighten(ice_hi, 15), S)
        px(p, x, 13, darken(ice_dk, 15), S)
    for y in range(6, 14):
        px(p, 8, y, lighten(ice_hi, 10), S)
        px(p, 23, y, darken(ice_dk, 10), S)
    # Frost crystals
    px(p, 10, 8, frost, S)
    px(p, 14, 10, frost, S)
    px(p, 20, 7, frost, S)
    px(p, 18, 12, frost, S)
    # Icicles hanging
    px(p, 10, 14, (ice[0], ice[1], ice[2], 200), S)
    px(p, 10, 15, (ice[0], ice[1], ice[2], 120), S)
    px(p, 20, 14, (ice[0], ice[1], ice[2], 200), S)
    px(p, 20, 15, (ice[0], ice[1], ice[2], 120), S)
    save(img, "unique_hammer_3")

def draw_unique_mace_3():  # Radiant Morningstar — golden holy
    S = 32
    img, p = create(S)
    gold = (220, 190, 50, 255)
    gold_hi = (255, 230, 100, 255)
    gold_dk = (170, 140, 20, 255)
    spike = (240, 210, 80, 255)
    glow = (255, 240, 150, 180)
    handle = (100, 70, 40, 255)
    handle_hi = (130, 95, 55, 255)
    # Handle
    for y in range(16, 30):
        px(p, 15, y, handle, S)
        px(p, 16, y, handle_hi, S)
    # Morningstar head (spiked ball)
    cx, cy = 16, 10
    for dy in range(-4, 5):
        for dx in range(-4, 5):
            dist = (dx*dx + dy*dy) ** 0.5
            if dist < 4.5:
                t = dist / 4.5
                c = lerp(gold_hi, gold_dk, t)
                px(p, cx + dx, cy + dy, c, S)
    # Spikes
    spike_positions = [(0, -6), (0, 6), (-6, 0), (6, 0), (-4, -4), (4, -4), (-4, 4), (4, 4)]
    for sx, sy in spike_positions:
        px(p, cx + sx, cy + sy, spike, S)
        # Second spike pixel
        sx2 = sx + (1 if sx > 0 else -1 if sx < 0 else 0)
        sy2 = sy + (1 if sy > 0 else -1 if sy < 0 else 0)
        px(p, cx + sx2, cy + sy2, lighten(spike, 15), S)
    # Holy glow
    px(p, cx, cy, glow, S)
    px(p, cx - 1, cy - 1, (glow[0], glow[1], glow[2], 100), S)
    save(img, "unique_mace_3")

def draw_unique_sickle_3():  # Frostbite Reaper — icy sickle
    img, p = create(16)
    ice = (130, 190, 240, 255)
    ice_hi = (180, 220, 255, 255)
    ice_dk = (80, 140, 200, 255)
    frost = (220, 240, 255, 200)
    handle = (80, 85, 100, 255)
    # Handle
    for y in range(9, 15):
        px(p, 5, y, handle)
        px(p, 6, y, lighten(handle, 15))
    # Curved blade
    blade_path = [(6,8),(7,7),(8,6),(9,5),(10,4),(11,4),(12,5),(13,6),(13,7)]
    for i, (x, y) in enumerate(blade_path):
        t = i / 8.0
        px(p, x, y, lerp(ice_hi, ice_dk, t))
        px(p, x, y - 1, ice_hi)
    # Frost tip
    px(p, 13, 8, frost)
    px(p, 14, 7, frost)
    # Ice crystals
    px(p, 9, 4, frost)
    px(p, 11, 3, frost)
    save(img, "unique_sickle_3")

def draw_unique_spear_3():  # Thunderlance — electric lance
    S = 32
    img, p = create(S)
    steel = (170, 180, 200, 255)
    steel_hi = (210, 220, 240, 255)
    steel_dk = (120, 130, 150, 255)
    bolt = (255, 255, 130, 255)
    bolt_w = (255, 255, 240, 255)
    shaft = (90, 80, 70, 255)
    shaft_hi = (120, 108, 92, 255)
    # Shaft (diagonal)
    for i in range(20):
        x = 6 + int(i * 0.5)
        y = 10 + i
        px(p, x, y, shaft, S)
        px(p, x + 1, y, shaft_hi, S)
    # Spearhead (elongated diamond)
    for i in range(10):
        x = 10 + i
        y = 6 - int(i * 0.4)
        w = max(0, 2 - abs(i - 5) // 2)
        for dy in range(-w, w + 1):
            if dy == -w: c = steel_hi
            elif dy == w: c = steel_dk
            else: c = steel
            px(p, x, y + dy, c, S)
    # Lightning engravings
    px(p, 12, 5, bolt, S)
    px(p, 14, 4, bolt_w, S)
    px(p, 16, 3, bolt, S)
    # Electric sparks at tip
    px(p, 20, 2, bolt_w, S)
    px(p, 21, 1, bolt, S)
    px(p, 19, 1, (bolt[0], bolt[1], bolt[2], 150), S)
    # Cross piece
    for dx in range(-3, 4):
        px(p, 10 + dx, 9, steel, S)
    save(img, "unique_spear_3")

def draw_unique_longbow_3():  # Galewind Bow — wind-themed
    S = 32
    img, p = create(S)
    wood = (140, 120, 80, 255)
    wood_hi = (170, 150, 105, 255)
    wood_dk = (100, 82, 55, 255)
    string = (200, 195, 185, 255)
    wind = (180, 210, 240, 150)
    feather = (190, 210, 230, 255)
    # Bow limb (curved)
    for y in range(2, 30):
        t = (y - 2) / 27.0
        curve_x = int(20 - 8 * abs(t - 0.5) * 2)
        px(p, curve_x, y, wood, S)
        px(p, curve_x - 1, y, wood_hi, S)
        px(p, curve_x + 1, y, wood_dk, S)
    # String (straight)
    for y in range(3, 29):
        px(p, 12, y, string, S)
    # Wind swirls
    px(p, 22, 8, wind, S)
    px(p, 23, 10, wind, S)
    px(p, 21, 22, wind, S)
    px(p, 22, 24, (wind[0], wind[1], wind[2], 80), S)
    # Feather decorations at tips
    px(p, 13, 2, feather, S)
    px(p, 14, 1, feather, S)
    px(p, 13, 29, feather, S)
    px(p, 14, 30, feather, S)
    # Grip wrapping
    for y in range(14, 18):
        px(p, 11, y, darken(wood, 15), S)
    save(img, "unique_longbow_3")

def draw_unique_heavy_crossbow_3():  # Arcane Arbalest — purple arcane crossbow
    S = 32
    img, p = create(S)
    wood = (80, 60, 90, 255)
    wood_hi = (110, 85, 120, 255)
    wood_dk = (50, 35, 60, 255)
    arcane = (160, 100, 230, 255)
    arcane_hi = (200, 140, 255, 255)
    string = (180, 170, 200, 255)
    metal = (140, 130, 160, 255)
    glow = (180, 120, 255, 150)
    # Stock (horizontal-ish)
    for x in range(4, 22):
        for dy in range(-1, 2):
            t = (x - 4) / 17.0
            c = lerp(wood_hi, wood_dk, t)
            px(p, x, 16 + dy, c, S)
    # Bow limbs
    for y in range(8, 24):
        t = abs(y - 16) / 8.0
        curve_x = int(22 + t * 4)
        px(p, curve_x, y, metal, S)
        px(p, curve_x + 1, y, darken(metal, 15), S)
    # String
    for y in range(9, 23):
        px(p, 22, y, string, S)
    # Trigger mechanism
    px(p, 10, 18, metal, S)
    px(p, 10, 19, metal, S)
    px(p, 10, 20, darken(metal, 15), S)
    # Arcane runes on stock
    px(p, 8, 16, arcane, S)
    px(p, 12, 16, arcane_hi, S)
    px(p, 16, 16, arcane, S)
    # Arcane glow
    px(p, 7, 15, glow, S)
    px(p, 13, 15, glow, S)
    px(p, 17, 17, glow, S)
    # Bolt channel
    for x in range(18, 28):
        px(p, x, 15, darken(wood, 15), S)
    save(img, "unique_heavy_crossbow_3")

def draw_unique_staff_damage_8():  # Stormcaller's Rod — lightning staff
    S = 32
    img, p = create(S)
    shaft = (100, 90, 120, 255)
    shaft_hi = (130, 120, 150, 255)
    shaft_dk = (70, 60, 85, 255)
    orb = (120, 180, 255, 255)
    orb_core = (200, 230, 255, 255)
    bolt = (255, 255, 130, 255)
    bolt_w = (255, 255, 240, 255)
    metal = (150, 145, 165, 255)
    # Shaft
    for y in range(10, 30):
        t = (y - 10) / 19.0
        px(p, 15, y, lerp(shaft_hi, shaft_dk, t), S)
        px(p, 16, y, lerp(shaft, darken(shaft_dk, 10), t), S)
    # Orb at top
    cx, cy = 15, 6
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            dist = (dx*dx + dy*dy) ** 0.5
            if dist < 3.5:
                t = dist / 3.5
                c = lerp(orb_core, orb, t)
                px(p, cx + dx, cy + dy, c, S)
    # Lightning from orb
    px(p, 15, 1, bolt_w, S)
    px(p, 14, 2, bolt, S)
    px(p, 16, 2, bolt, S)
    px(p, 13, 1, (bolt[0], bolt[1], bolt[2], 150), S)
    px(p, 17, 1, (bolt[0], bolt[1], bolt[2], 150), S)
    # Orb frame
    px(p, 13, 9, metal, S)
    px(p, 14, 10, metal, S)
    px(p, 17, 9, metal, S)
    px(p, 16, 10, metal, S)
    # Wrapping on shaft
    for y in range(14, 26, 3):
        px(p, 14, y, lighten(shaft, 15), S)
    # Foot cap
    px(p, 15, 30, metal, S)
    px(p, 16, 30, metal, S)
    px(p, 15, 31, darken(metal, 15), S)
    save(img, "unique_staff_damage_8")

def draw_unique_staff_heal_3():  # Living Wood Staff — nature green
    S = 32
    img, p = create(S)
    wood = (90, 70, 40, 255)
    wood_hi = (120, 95, 55, 255)
    wood_dk = (60, 45, 25, 255)
    bark = (75, 55, 30, 255)
    leaf = (60, 160, 45, 255)
    leaf_hi = (90, 200, 70, 255)
    vine = (45, 120, 35, 255)
    glow = (100, 230, 100, 180)
    blossom = (255, 180, 200, 255)
    # Gnarled wood shaft
    for y in range(8, 30):
        t = (y - 8) / 21.0
        # Slight irregular shape
        dx_off = 1 if y % 5 == 0 else 0
        px(p, 15 + dx_off, y, lerp(wood_hi, wood_dk, t), S)
        px(p, 16 + dx_off, y, lerp(wood, darken(wood_dk, 10), t), S)
    # Branches at top
    px(p, 13, 6, wood, S)
    px(p, 12, 5, wood_hi, S)
    px(p, 17, 6, wood, S)
    px(p, 18, 5, wood_dk, S)
    px(p, 14, 7, wood, S)
    px(p, 16, 7, wood, S)
    # Leaves on branches
    px(p, 11, 4, leaf_hi, S)
    px(p, 12, 4, leaf, S)
    px(p, 10, 5, leaf, S)
    px(p, 19, 4, leaf, S)
    px(p, 18, 4, leaf_hi, S)
    px(p, 20, 5, leaf, S)
    # Central bloom/crystal
    px(p, 15, 4, glow, S)
    px(p, 15, 3, (glow[0], glow[1], glow[2], 120), S)
    px(p, 14, 5, glow, S)
    px(p, 16, 5, glow, S)
    # Blossoms
    px(p, 13, 3, blossom, S)
    px(p, 17, 3, blossom, S)
    # Vine wrapping
    for y in range(12, 26, 4):
        px(p, 14, y, vine, S)
        px(p, 17, y + 2, vine, S)
    # Roots at base
    px(p, 14, 30, wood_dk, S)
    px(p, 13, 31, bark, S)
    px(p, 17, 30, wood_dk, S)
    px(p, 18, 31, bark, S)
    save(img, "unique_staff_heal_3")

def draw_unique_shield_3():  # Dreadnought Ward — dark menacing shield
    S = 32
    img, p = create(S)
    dark = (40, 35, 50, 255)
    dark_hi = (70, 60, 85, 255)
    dark_dk = (20, 15, 30, 255)
    spike = (90, 80, 100, 255)
    spike_hi = (120, 110, 135, 255)
    eye = (200, 50, 40, 255)
    eye_glow = (255, 80, 60, 200)
    metal = (100, 95, 110, 255)
    # Shield body (kite-shaped)
    for y in range(3, 28):
        t = (y - 3) / 24.0
        w = int(12 - t * 6) if t < 0.7 else int(12 - t * 12)
        w = max(1, w)
        for x in range(16 - w, 16 + w):
            c = lerp(dark_hi, dark_dk, t)
            px(p, x, y, c, S)
    # Edge highlight
    for y in range(3, 28):
        t = (y - 3) / 24.0
        w = int(12 - t * 6) if t < 0.7 else int(12 - t * 12)
        w = max(1, w)
        px(p, 16 - w, y, lighten(dark_hi, 10), S)
        px(p, 16 + w - 1, y, darken(dark_dk, 10), S)
    # Top edge
    for x in range(5, 27):
        px(p, x, 3, lighten(dark_hi, 15), S)
    # Central skull/eye emblem
    px(p, 15, 12, eye, S)
    px(p, 16, 12, eye, S)
    px(p, 15, 13, eye_glow, S)
    px(p, 16, 13, eye, S)
    # Menacing V shape around eye
    for i in range(3):
        px(p, 13 - i, 11 + i, metal, S)
        px(p, 18 + i, 11 + i, metal, S)
    # Spike at top
    px(p, 15, 1, spike_hi, S)
    px(p, 16, 1, spike, S)
    px(p, 15, 2, spike, S)
    px(p, 16, 2, spike, S)
    # Spikes at sides
    px(p, 3, 10, spike_hi, S)
    px(p, 4, 10, spike, S)
    px(p, 27, 10, spike, S)
    px(p, 28, 10, spike_hi, S)
    # Rivets
    px(p, 10, 8, metal, S)
    px(p, 21, 8, metal, S)
    px(p, 10, 18, metal, S)
    px(p, 21, 18, metal, S)
    save(img, "unique_shield_3")


# ============================================================
# MAIN
# ============================================================
def main():
    print("=" * 60)
    print("Regenerating 71 item textures with hand-crafted pixel art")
    print("=" * 60)

    count = 0

    print("\n=== RELICS (30 items, 16x16) ===")
    relic_funcs = [
        draw_wardens_visor, draw_verdant_mask, draw_frostweave_veil,
        draw_stormcaller_circlet, draw_ashen_diadem, draw_wraith_crown,
        draw_arcane_gauntlet, draw_iron_fist, draw_plague_grasp, draw_sunforged_bracer,
        draw_stormband, draw_gravestone_ring, draw_verdant_signet,
        draw_phoenix_mantle, draw_windrunner_cloak, draw_abyssal_cape,
        draw_alchemists_sash, draw_guardians_girdle, draw_serpent_belt,
        draw_frostfire_pendant, draw_tidekeeper_amulet, draw_bloodstone_choker,
        draw_thornweave_glove, draw_chrono_glove,
        draw_stormstrider_boots, draw_sandwalker_treads,
        draw_emberstone_band,
        draw_void_lantern, draw_thunderhorn, draw_mending_chalice,
    ]
    for fn in relic_funcs:
        fn()
        count += 1

    print("\n=== WEAPONS — Core RPG (8 items, 32x32) ===")
    weapon_core_funcs = [
        draw_voidreaver, draw_solaris, draw_stormfury, draw_briarthorn,
        draw_abyssal_trident, draw_pyroclast, draw_whisperwind, draw_soulchain,
    ]
    for fn in weapon_core_funcs:
        fn()
        count += 1

    print("\n=== WEAPONS — Arsenal (33 items, mixed sizes) ===")
    arsenal_funcs = [
        draw_unique_whip_1, draw_unique_whip_2, draw_unique_whip_sw,
        draw_unique_wand_1, draw_unique_wand_2, draw_unique_wand_sw,
        draw_unique_katana_1, draw_unique_katana_2, draw_unique_katana_sw,
        draw_unique_greatshield_1, draw_unique_greatshield_2, draw_unique_greatshield_sw,
        draw_unique_throwing_axe_1, draw_unique_throwing_axe_2, draw_unique_throwing_axe_sw,
        draw_unique_rapier_1, draw_unique_rapier_2, draw_unique_rapier_sw,
        draw_unique_longsword_1, draw_unique_longsword_2,
        draw_unique_claymore_3, draw_unique_dagger_3, draw_unique_double_axe_3,
        draw_unique_glaive_3, draw_unique_hammer_3, draw_unique_mace_3,
        draw_unique_sickle_3, draw_unique_spear_3, draw_unique_longbow_3,
        draw_unique_heavy_crossbow_3, draw_unique_staff_damage_8,
        draw_unique_staff_heal_3, draw_unique_shield_3,
    ]
    for fn in arsenal_funcs:
        fn()
        count += 1

    print(f"\nDone! Regenerated {count} textures in {OUTPUT_DIR}")

if __name__ == "__main__":
    main()
