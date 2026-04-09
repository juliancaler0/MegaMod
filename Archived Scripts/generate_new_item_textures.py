"""
Generate 16x16 pixel art textures for 71 new items (30 relics + 41 weapons).
Each texture uses colored gradients, shapes, and patterns appropriate to the item's theme.
"""
from PIL import Image, ImageDraw
import os

OUTPUT_DIR = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\item"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# ===== Color Palettes by Element =====
PALETTES = {
    "fire":      {"primary": (255, 80, 20),  "secondary": (255, 160, 40),  "dark": (200, 50, 0)},
    "ice":       {"primary": (120, 200, 255), "secondary": (180, 220, 255), "dark": (60, 140, 200)},
    "lightning": {"primary": (255, 255, 100), "secondary": (200, 200, 255), "dark": (255, 230, 50)},
    "poison":    {"primary": (80, 200, 50),   "secondary": (50, 150, 30),   "dark": (120, 220, 80)},
    "holy":      {"primary": (255, 240, 180), "secondary": (255, 220, 120), "dark": (255, 255, 200)},
    "shadow":    {"primary": (60, 20, 80),    "secondary": (100, 40, 120),  "dark": (40, 10, 60)},
    "arcane":    {"primary": (180, 100, 255), "secondary": (140, 60, 220),  "dark": (200, 140, 255)},
    "physical":  {"primary": (180, 180, 180), "secondary": (140, 140, 140), "dark": (200, 200, 200)},
    "nature":    {"primary": (40, 160, 40),   "secondary": (80, 200, 60),   "dark": (30, 120, 30)},
    "water":     {"primary": (40, 80, 200),   "secondary": (60, 120, 220),  "dark": (80, 160, 240)},
}

# ===== Helper Functions =====
def lighten(color, amount=40):
    return tuple(min(255, c + amount) for c in color[:3])

def darken(color, amount=40):
    return tuple(max(0, c - amount) for c in color[:3])

def lerp_color(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))

def get_palette(element):
    """Get palette, supporting dual elements like 'ice/fire'."""
    if "/" in element:
        e1, e2 = element.split("/")
        p1, p2 = PALETTES[e1], PALETTES[e2]
        return {
            "primary": lerp_color(p1["primary"], p2["primary"], 0.5),
            "secondary": lerp_color(p1["secondary"], p2["secondary"], 0.5),
            "dark": lerp_color(p1["dark"], p2["dark"], 0.5),
        }
    return PALETTES[element]

def set_px(pixels, x, y, color, alpha=255):
    if 0 <= x < 16 and 0 <= y < 16:
        if len(color) == 3:
            pixels[x, y] = color + (alpha,)
        else:
            pixels[x, y] = color

def create_image():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    return img, img.load()

def save_texture(img, name):
    path = os.path.join(OUTPUT_DIR, f"{name}.png")
    img.save(path)
    print(f"  Created: {name}.png")

# ===== Shape Drawing Functions =====

def draw_visor(px, pal):
    """Face mask / visor shape - covers upper face area."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Main mask body (rows 3-10, cols 2-13)
    for y in range(3, 11):
        t = (y - 3) / 7.0
        for x in range(2, 14):
            c = lerp_color(lighten(p, 30), d, t)
            set_px(px, x, y, c)
    # Eye slits
    for x in range(4, 7):
        set_px(px, x, 6, (0, 0, 0), 200)
    for x in range(9, 12):
        set_px(px, x, 6, (0, 0, 0), 200)
    # Nose bridge
    set_px(px, 7, 6, lighten(s, 20))
    set_px(px, 8, 6, lighten(s, 20))
    # Top edge highlight
    for x in range(3, 13):
        set_px(px, x, 3, lighten(p, 50))
    # Strap sides
    for y in range(4, 9):
        set_px(px, 1, y, darken(d, 20))
        set_px(px, 14, y, darken(d, 20))
    # Accent details
    set_px(px, 7, 4, s)
    set_px(px, 8, 4, s)
    set_px(px, 7, 9, s)
    set_px(px, 8, 9, s)

def draw_mask(px, pal):
    """Full face mask shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Rounded mask shape
    for y in range(2, 14):
        width = 5 if y < 4 or y > 11 else 7
        start = 8 - width
        end = 8 + width
        t = (y - 2) / 11.0
        for x in range(start, end):
            c = lerp_color(lighten(p, 20), d, t)
            set_px(px, x, y, c)
    # Eye holes
    for x in range(4, 6):
        set_px(px, x, 6, (10, 10, 10), 230)
        set_px(px, x, 7, (10, 10, 10), 230)
    for x in range(10, 12):
        set_px(px, x, 6, (10, 10, 10), 230)
        set_px(px, x, 7, (10, 10, 10), 230)
    # Nose
    set_px(px, 7, 8, lighten(p, 30))
    set_px(px, 8, 8, lighten(p, 30))
    # Leaf/nature decorations on sides
    set_px(px, 3, 5, s)
    set_px(px, 12, 5, s)

def draw_veil(px, pal):
    """Draped veil shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Headband at top
    for x in range(3, 13):
        set_px(px, x, 2, lighten(s, 30))
        set_px(px, x, 3, s)
    # Flowing veil below
    for y in range(4, 15):
        t = (y - 4) / 10.0
        width = min(6 + (y - 4), 7)
        start = 8 - width
        end = 8 + width
        for x in range(start, end):
            wave = ((x + y) % 3) * 10
            c = lerp_color(lighten(p, wave), d, t)
            alpha = max(80, 255 - (y - 4) * 15)
            set_px(px, x, y, c, alpha)
    # Jewel on forehead
    set_px(px, 7, 3, lighten(p, 60))
    set_px(px, 8, 3, lighten(p, 60))

def draw_circlet(px, pal):
    """Thin circlet/headband with gem."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Circlet band - curved arc
    for x in range(2, 14):
        y_offset = 7 + int(((x - 7.5) ** 2) / 10)
        set_px(px, x, y_offset, lighten(p, 20))
        set_px(px, x, y_offset + 1, p)
        set_px(px, x, y_offset + 2, darken(p, 20))
    # Center gem
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            c = lighten(s, 50) if dx == 0 and dy == 0 else s
            set_px(px, 7 + dx, 6 + dy, c)
    # Side accent gems
    set_px(px, 3, 8, lighten(d, 30))
    set_px(px, 12, 8, lighten(d, 30))
    # Thin chains hanging
    for y in range(9, 12):
        set_px(px, 4, y, darken(p, 10), 180)
        set_px(px, 11, y, darken(p, 10), 180)

def draw_diadem(px, pal):
    """Crown-like diadem with central peak."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Central peak
    for y in range(2, 8):
        t = (y - 2) / 5.0
        w = max(1, 3 - (y - 5) if y < 5 else 1 + (y - 5))
        for dx in range(-w, w + 1):
            set_px(px, 7 + dx, y, lerp_color(lighten(s, 40), p, t))
    # Band
    for x in range(1, 15):
        t = abs(x - 7.5) / 7.5
        set_px(px, x, 8, lerp_color(lighten(p, 20), d, t))
        set_px(px, x, 9, lerp_color(p, darken(d, 20), t))
        set_px(px, x, 10, darken(d, 10))
    # Side peaks
    for y in range(5, 8):
        set_px(px, 3, y, lerp_color(s, d, (y - 5) / 2.0))
        set_px(px, 12, y, lerp_color(s, d, (y - 5) / 2.0))
    # Gems
    set_px(px, 7, 3, lighten(s, 60))
    set_px(px, 8, 3, lighten(s, 60))
    set_px(px, 3, 6, lighten(p, 40))
    set_px(px, 12, 6, lighten(p, 40))

def draw_crown(px, pal):
    """Royal crown shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Crown points
    points = [2, 5, 8, 11, 13]
    for px_x in points:
        for y in range(3, 7):
            t = (y - 3) / 3.0
            set_px(px, px_x, y, lerp_color(lighten(s, 30), p, t))
            if px_x + 1 < 16:
                set_px(px, px_x + 1, y, lerp_color(s, d, t))
    # Crown body
    for y in range(7, 12):
        t = (y - 7) / 4.0
        for x in range(1, 15):
            set_px(px, x, y, lerp_color(p, darken(d, 20), t))
    # Rim highlight
    for x in range(1, 15):
        set_px(px, x, 7, lighten(p, 40))
    # Jewels on points
    for px_x in points:
        set_px(px, px_x, 4, lighten(s, 60))
    # Bottom edge shadow
    for x in range(1, 15):
        set_px(px, x, 11, darken(d, 30))

def draw_gauntlet(px, pal):
    """Armored glove/gauntlet shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Wrist/cuff (bottom)
    for y in range(11, 15):
        t = (y - 11) / 3.0
        for x in range(4, 12):
            set_px(px, x, y, lerp_color(darken(p, 10), d, t))
    # Hand body
    for y in range(5, 11):
        t = (y - 5) / 5.0
        for x in range(3, 13):
            set_px(px, x, y, lerp_color(lighten(p, 20), darken(p, 10), t))
    # Fingers (top)
    fingers = [(4, 2), (6, 1), (8, 1), (10, 2), (12, 3)]
    for fx, fy in fingers:
        for y in range(fy, 5):
            set_px(px, fx, y, lerp_color(s, p, (y - fy) / max(1, 4 - fy)))
    # Knuckle plates
    for x in range(4, 12, 2):
        set_px(px, x, 5, lighten(s, 30))
        set_px(px, x, 6, s)
    # Gem on back of hand
    set_px(px, 7, 7, lighten(s, 50))
    set_px(px, 8, 7, lighten(s, 50))
    set_px(px, 7, 8, s)
    set_px(px, 8, 8, s)

def draw_fist(px, pal):
    """Clenched fist / brass knuckles shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Fist body
    for y in range(4, 12):
        t = (y - 4) / 7.0
        for x in range(3, 13):
            set_px(px, x, y, lerp_color(lighten(p, 15), darken(d, 10), t))
    # Knuckle bumps
    for x in range(4, 12, 2):
        set_px(px, x, 3, lighten(p, 30))
        set_px(px, x, 4, lighten(p, 20))
    # Knuckle plate highlight
    for x in range(3, 13):
        set_px(px, x, 5, lighten(s, 20))
    # Wrist band
    for x in range(4, 12):
        set_px(px, x, 12, s)
        set_px(px, x, 13, darken(s, 20))
    # Metal studs
    for x in [5, 7, 9, 11]:
        set_px(px, x, 5, lighten(p, 50))

def draw_grasp(px, pal):
    """Open claw/grasp glove shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Palm
    for y in range(6, 13):
        t = (y - 6) / 6.0
        for x in range(4, 12):
            set_px(px, x, y, lerp_color(p, d, t))
    # Clawed fingers
    claws = [(3, 1), (5, 0), (8, 0), (10, 1), (12, 2)]
    for cx, cy in claws:
        for y in range(cy, 6):
            t = (y - cy) / max(1, 5 - cy)
            set_px(px, cx, y, lerp_color(lighten(s, 40), p, t))
        # Claw tips
        set_px(px, cx, cy, lighten(s, 60))
    # Dripping effect
    set_px(px, 6, 13, darken(s, 10), 180)
    set_px(px, 6, 14, darken(s, 10), 120)
    set_px(px, 9, 13, darken(s, 10), 180)

def draw_bracer(px, pal):
    """Wrist bracer / arm guard shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Main bracer body (vertical rectangle, slightly angled)
    for y in range(3, 13):
        t = (y - 3) / 9.0
        for x in range(4, 12):
            set_px(px, x, y, lerp_color(lighten(p, 20), darken(d, 10), t))
    # Rim top
    for x in range(4, 12):
        set_px(px, x, 3, lighten(s, 40))
        set_px(px, x, 4, lighten(s, 20))
    # Rim bottom
    for x in range(4, 12):
        set_px(px, x, 12, darken(d, 20))
    # Decorative band in middle
    for x in range(4, 12):
        set_px(px, x, 7, s)
        set_px(px, x, 8, lighten(s, 10))
    # Gem
    set_px(px, 7, 7, lighten(s, 60))
    set_px(px, 8, 7, lighten(s, 60))

def draw_ring(px, pal):
    """Ring shape - small circle with gem."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Ring band - oval
    ring_pixels = [
        (6, 4), (7, 4), (8, 4), (9, 4),
        (5, 5), (10, 5),
        (4, 6), (11, 6),
        (4, 7), (11, 7),
        (4, 8), (11, 8),
        (4, 9), (11, 9),
        (5, 10), (10, 10),
        (6, 11), (7, 11), (8, 11), (9, 11),
    ]
    for rx, ry in ring_pixels:
        t = ry / 15.0
        set_px(px, rx, ry, lerp_color(lighten(p, 30), d, t))
    # Inner shine
    set_px(px, 6, 5, lighten(p, 50))
    set_px(px, 7, 5, lighten(p, 50))
    # Gem setting on top
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            if abs(dx) + abs(dy) <= 1:
                c = lighten(s, 60) if dx == 0 and dy == 0 else s
                set_px(px, 7 + dx, 3 + dy, c)

def draw_signet(px, pal):
    """Signet ring - ring with flat top/seal."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Ring band
    ring_pixels = [
        (6, 5), (7, 5), (8, 5), (9, 5),
        (5, 6), (10, 6),
        (4, 7), (11, 7),
        (4, 8), (11, 8),
        (4, 9), (11, 9),
        (5, 10), (10, 10),
        (6, 11), (7, 11), (8, 11), (9, 11),
    ]
    for rx, ry in ring_pixels:
        t = ry / 15.0
        set_px(px, rx, ry, lerp_color(lighten(p, 20), d, t))
    # Flat signet face on top
    for x in range(5, 11):
        for y in range(2, 5):
            t = (y - 2) / 2.0
            set_px(px, x, y, lerp_color(lighten(s, 30), s, t))
    # Engraved symbol
    set_px(px, 7, 3, lighten(s, 60))
    set_px(px, 8, 3, lighten(s, 60))
    set_px(px, 6, 3, darken(s, 20))
    set_px(px, 9, 3, darken(s, 20))

def draw_mantle(px, pal):
    """Shoulder mantle / cape draped over shoulders."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Shoulder pieces
    for y in range(2, 7):
        t = (y - 2) / 4.0
        for x in range(1, 7):
            set_px(px, x, y, lerp_color(lighten(p, 30), p, t))
        for x in range(9, 15):
            set_px(px, x, y, lerp_color(lighten(p, 30), p, t))
    # Clasp in center
    for y in range(3, 6):
        set_px(px, 7, y, lighten(s, 40))
        set_px(px, 8, y, lighten(s, 40))
    # Flowing cape below
    for y in range(7, 15):
        t = (y - 7) / 7.0
        width = 6 + int(t * 2)
        start = 8 - width
        end = 8 + width
        for x in range(max(0, start), min(16, end)):
            wave = ((x + y) % 4) * 8
            alpha = max(100, 255 - int(t * 80))
            set_px(px, x, y, lerp_color(p, darken(d, wave), t), alpha)

def draw_cloak(px, pal):
    """Full cloak / cape shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Collar
    for x in range(5, 11):
        set_px(px, x, 1, lighten(s, 30))
        set_px(px, x, 2, s)
    # Clasp
    set_px(px, 7, 2, lighten(p, 60))
    set_px(px, 8, 2, lighten(p, 60))
    # Cape body - widens as it goes down
    for y in range(3, 15):
        t = (y - 3) / 11.0
        width = 3 + int(t * 5)
        start = 8 - width
        end = 8 + width
        for x in range(max(0, start), min(16, end)):
            fold = abs(x - 7.5) / 8.0
            c = lerp_color(p, darken(d, 20), t * 0.7 + fold * 0.3)
            set_px(px, x, y, c)
    # Hood suggestion at top
    for x in range(4, 12):
        set_px(px, x, 0, darken(p, 20))

def draw_cape(px, pal):
    """Short cape shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Collar/neck
    for x in range(5, 11):
        set_px(px, x, 2, lighten(s, 20))
        set_px(px, x, 3, s)
    # Body - wide flowing
    for y in range(4, 14):
        t = (y - 4) / 9.0
        width = 4 + int(t * 4)
        start = 8 - width
        end = 8 + width
        for x in range(max(0, start), min(16, end)):
            wave = ((x * 3 + y * 2) % 5) * 6
            c = lerp_color(lighten(p, 10), darken(d, wave), t)
            alpha = max(120, 255 - int(t * 50))
            set_px(px, x, y, c, alpha)
    # Bottom edge tatter
    for x in range(1, 15):
        if x % 3 != 0:
            set_px(px, x, 14, darken(d, 30), 150)

def draw_sash(px, pal):
    """Diagonal sash / belt."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Diagonal band from top-right to bottom-left
    for i in range(16):
        x = 14 - i
        y = 1 + i
        for w in range(-1, 2):
            px_x = x + w
            t = i / 15.0
            c = lerp_color(lighten(p, 20), d, t)
            set_px(px, px_x, y, c)
    # Pouch on the sash
    for y in range(7, 11):
        for x in range(5, 9):
            set_px(px, x, y, darken(s, 10))
    set_px(px, 6, 7, lighten(s, 30))
    set_px(px, 7, 7, lighten(s, 30))
    # Buckle
    set_px(px, 10, 5, lighten(p, 50))
    set_px(px, 11, 5, lighten(p, 50))

def draw_girdle(px, pal):
    """Wide belt / girdle shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Wide belt (rows 4-11)
    for y in range(4, 12):
        t = (y - 4) / 7.0
        for x in range(1, 15):
            set_px(px, x, y, lerp_color(lighten(p, 15), darken(d, 15), t))
    # Top and bottom edges
    for x in range(1, 15):
        set_px(px, x, 4, lighten(s, 30))
        set_px(px, x, 11, darken(d, 30))
    # Central buckle plate
    for y in range(5, 11):
        for x in range(5, 11):
            t2 = abs(x - 7.5) / 3.0 + abs(y - 7.5) / 3.0
            set_px(px, x, y, lerp_color(lighten(s, 40), s, min(1, t2)))
    # Buckle border
    for x in range(5, 11):
        set_px(px, x, 5, lighten(s, 50))
        set_px(px, x, 10, darken(s, 20))
    for y in range(5, 11):
        set_px(px, 5, y, lighten(s, 50))
        set_px(px, 10, y, darken(s, 20))

def draw_belt(px, pal):
    """Standard belt shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Belt strap
    for x in range(1, 15):
        set_px(px, x, 6, lighten(p, 20))
        set_px(px, x, 7, p)
        set_px(px, x, 8, p)
        set_px(px, x, 9, darken(d, 10))
    # Scale/texture pattern
    for x in range(1, 15, 2):
        set_px(px, x, 7, darken(p, 15))
    # Buckle (left side)
    for x in range(2, 5):
        for y in range(5, 11):
            set_px(px, x, y, s)
    for x in range(2, 5):
        set_px(px, x, 5, lighten(s, 30))
        set_px(px, x, 10, darken(s, 30))
    # Buckle inner
    set_px(px, 3, 7, darken(s, 50))
    set_px(px, 3, 8, darken(s, 50))
    # Fang/tooth hanging
    set_px(px, 8, 10, lighten(p, 40))
    set_px(px, 8, 11, lighten(p, 50))
    set_px(px, 8, 12, lighten(p, 30))

def draw_pendant(px, pal):
    """Necklace pendant shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Chain
    for x in range(3, 13):
        y = 3 + int(((x - 7.5) ** 2) / 8)
        set_px(px, x, y, lighten(p, 30))
        if y + 1 < 16:
            set_px(px, x, y + 1, p)
    # Pendant body (diamond shape)
    pendant_px = [
        (7, 7), (8, 7),
        (6, 8), (7, 8), (8, 8), (9, 8),
        (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9),
        (6, 10), (7, 10), (8, 10), (9, 10),
        (7, 11), (8, 11),
    ]
    for ppx, ppy in pendant_px:
        t = (ppy - 7) / 4.0
        c = lerp_color(lighten(s, 40), d, t)
        set_px(px, ppx, ppy, c)
    # Gem center
    set_px(px, 7, 9, lighten(s, 60))
    set_px(px, 8, 9, lighten(s, 60))

def draw_amulet(px, pal):
    """Round amulet necklace."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Chain
    for x in range(4, 12):
        y = 2 + int(((x - 7.5) ** 2) / 10)
        set_px(px, x, y, lighten(p, 20))
    # Round amulet
    center_x, center_y = 7, 9
    for y in range(6, 13):
        for x in range(4, 12):
            dist = ((x - center_x) ** 2 + (y - center_y) ** 2) ** 0.5
            if dist <= 3.5:
                t = dist / 3.5
                c = lerp_color(lighten(s, 50), d, t)
                set_px(px, x, y, c)
    # Inner glow
    set_px(px, 7, 9, lighten(s, 70))
    set_px(px, 8, 9, lighten(s, 60))
    set_px(px, 7, 8, lighten(s, 50))
    # Edge highlight
    set_px(px, 7, 6, lighten(p, 40))
    set_px(px, 8, 6, lighten(p, 40))

def draw_choker(px, pal):
    """Tight necklace/choker."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Choker band - slight curve
    for x in range(2, 14):
        y = 7 + int(((x - 7.5) ** 2) / 20)
        for dy in range(3):
            t = dy / 2.0
            c = lerp_color(lighten(p, 20), d, t)
            set_px(px, x, y + dy, c)
    # Central gem
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            if abs(dx) + abs(dy) <= 1:
                c = lighten(s, 60) if dx == 0 and dy == 0 else s
                set_px(px, 7 + dx, 7 + dy, c)
    # Side studs
    set_px(px, 4, 7, lighten(s, 30))
    set_px(px, 11, 7, lighten(s, 30))
    # Dangling charm
    set_px(px, 7, 10, darken(s, 10))
    set_px(px, 7, 11, lighten(p, 40))
    set_px(px, 7, 12, lighten(p, 30))

def draw_glove(px, pal):
    """Glove shape (thinner than gauntlet)."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Hand
    for y in range(5, 12):
        t = (y - 5) / 6.0
        for x in range(4, 12):
            set_px(px, x, y, lerp_color(lighten(p, 15), d, t))
    # Fingers
    fingers = [(4, 2), (6, 1), (8, 1), (10, 2)]
    for fx, fy in fingers:
        for y in range(fy, 5):
            t = (y - fy) / max(1, 4 - fy)
            set_px(px, fx, y, lerp_color(lighten(s, 30), p, t))
            set_px(px, fx + 1, y, lerp_color(s, darken(p, 10), t))
    # Thumb
    set_px(px, 3, 6, p)
    set_px(px, 2, 7, p)
    set_px(px, 2, 8, darken(p, 10))
    # Wrist
    for x in range(4, 12):
        set_px(px, x, 12, s)
        set_px(px, x, 13, darken(s, 15))
    # Vine/thorn details
    set_px(px, 6, 6, lighten(s, 40))
    set_px(px, 8, 8, lighten(s, 40))

def draw_boots(px, pal):
    """Boot shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Boot shaft
    for y in range(2, 10):
        t = (y - 2) / 7.0
        for x in range(5, 11):
            set_px(px, x, y, lerp_color(lighten(p, 20), p, t))
    # Boot top rim
    for x in range(5, 11):
        set_px(px, x, 2, lighten(s, 30))
        set_px(px, x, 3, s)
    # Foot (wider)
    for y in range(10, 14):
        t = (y - 10) / 3.0
        for x in range(3, 13):
            set_px(px, x, y, lerp_color(p, darken(d, 15), t))
    # Sole
    for x in range(3, 13):
        set_px(px, x, 14, darken(d, 40))
    # Toe
    for x in range(10, 14):
        set_px(px, x, 11, lighten(p, 10))
    # Buckle/strap
    for x in range(5, 11):
        set_px(px, x, 6, s)
    set_px(px, 7, 6, lighten(s, 50))

def draw_treads(px, pal):
    """Sandal/tread boots."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Low boot shaft
    for y in range(5, 10):
        t = (y - 5) / 4.0
        for x in range(5, 11):
            set_px(px, x, y, lerp_color(lighten(p, 15), p, t))
    # Foot
    for y in range(10, 14):
        t = (y - 10) / 3.0
        for x in range(3, 13):
            set_px(px, x, y, lerp_color(p, darken(d, 10), t))
    # Treaded sole
    for x in range(3, 13):
        if x % 2 == 0:
            set_px(px, x, 14, darken(d, 50))
        else:
            set_px(px, x, 14, darken(d, 30))
    # Straps
    for x in range(4, 12):
        set_px(px, x, 7, s)
        set_px(px, x, 9, s)
    # Buckle
    set_px(px, 5, 7, lighten(s, 40))

def draw_band(px, pal):
    """Simple band / ring-like accessory."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Thick band
    ring_pixels = [
        (5, 4), (6, 4), (7, 4), (8, 4), (9, 4), (10, 4),
        (4, 5), (11, 5),
        (3, 6), (3, 7), (3, 8), (3, 9),
        (12, 6), (12, 7), (12, 8), (12, 9),
        (4, 10), (11, 10),
        (5, 11), (6, 11), (7, 11), (8, 11), (9, 11), (10, 11),
    ]
    for rx, ry in ring_pixels:
        t = ry / 15.0
        c = lerp_color(lighten(p, 30), d, t)
        set_px(px, rx, ry, c)
    # Inner band fill
    inner = [
        (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5),
        (4, 6), (4, 7), (4, 8), (4, 9),
        (11, 6), (11, 7), (11, 8), (11, 9),
        (5, 10), (6, 10), (7, 10), (8, 10), (9, 10), (10, 10),
    ]
    for rx, ry in inner:
        set_px(px, rx, ry, lerp_color(lighten(p, 10), darken(p, 10), ry / 15.0))
    # Glow/gem inset
    set_px(px, 7, 4, lighten(s, 60))
    set_px(px, 8, 4, lighten(s, 60))

def draw_lantern(px, pal):
    """Handheld lantern shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Handle (top)
    set_px(px, 7, 1, lighten(p, 20))
    set_px(px, 8, 1, lighten(p, 20))
    set_px(px, 6, 2, lighten(p, 20))
    set_px(px, 9, 2, lighten(p, 20))
    # Top cap
    for x in range(5, 11):
        set_px(px, x, 3, lighten(p, 30))
        set_px(px, x, 4, p)
    # Glass body
    for y in range(5, 12):
        t = (y - 5) / 6.0
        for x in range(5, 11):
            c = lerp_color(lighten(s, 60), s, abs(t - 0.5) * 2)
            alpha = 200 if x in [5, 10] else 180
            set_px(px, x, y, c, alpha)
    # Inner glow
    for y in range(6, 11):
        for x in range(6, 10):
            set_px(px, x, y, lighten(s, 70), 220)
    # Flame core
    set_px(px, 7, 7, lighten(s, 80))
    set_px(px, 8, 7, lighten(s, 80))
    set_px(px, 7, 8, lighten(s, 90))
    set_px(px, 8, 8, lighten(s, 90))
    # Bottom cap
    for x in range(5, 11):
        set_px(px, x, 12, p)
        set_px(px, x, 13, darken(d, 20))

def draw_horn(px, pal):
    """Horn / trumpet shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Mouthpiece (left, narrow)
    for y in range(6, 9):
        set_px(px, 1, y, lighten(p, 30))
        set_px(px, 2, y, p)
    # Body (widens left to right)
    for x in range(3, 13):
        t = (x - 3) / 9.0
        width = 1 + int(t * 3)
        center_y = 7
        for dy in range(-width, width + 1):
            y = center_y + dy
            ct = abs(dy) / max(1, width)
            c = lerp_color(lighten(p, 20), darken(d, 10), ct)
            set_px(px, x, y, c)
    # Bell (right end, flared)
    for y in range(3, 12):
        t = abs(y - 7) / 4.0
        set_px(px, 13, y, lerp_color(lighten(s, 20), d, t))
        set_px(px, 14, y, lerp_color(s, darken(d, 10), t))
    # Rim of bell
    for y in range(3, 12):
        set_px(px, 15, y, lighten(s, 30) if abs(y - 7) < 3 else darken(d, 20))
    # Decorative ring
    for y in range(5, 10):
        set_px(px, 6, y, lighten(s, 40))

def draw_chalice(px, pal):
    """Goblet / chalice shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Rim (top, wide)
    for x in range(3, 13):
        set_px(px, x, 2, lighten(s, 40))
        set_px(px, x, 3, lighten(s, 20))
    # Cup body (narrowing)
    for y in range(4, 8):
        t = (y - 4) / 3.0
        width = 5 - int(t * 2)
        start = 8 - width
        end = 8 + width
        for x in range(start, end):
            ct = abs(x - 7.5) / max(1, width)
            c = lerp_color(lighten(p, 20), darken(p, 10), ct)
            set_px(px, x, y, c)
    # Stem
    for y in range(8, 12):
        set_px(px, 7, y, p)
        set_px(px, 8, y, darken(p, 10))
    # Base
    for x in range(5, 11):
        set_px(px, x, 12, lighten(p, 10))
        set_px(px, x, 13, p)
        set_px(px, x, 14, darken(d, 20))
    # Liquid inside
    for x in range(4, 12):
        set_px(px, x, 4, lighten(s, 50))
    # Gem on cup
    set_px(px, 7, 5, lighten(s, 60))
    set_px(px, 8, 5, lighten(s, 60))


# ============================================================
# WEAPON SHAPE FUNCTIONS
# ============================================================

def draw_sword(px, pal):
    """Standard sword silhouette."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Blade (diagonal from top-right to center)
    for i in range(10):
        x = 12 - i
        y = 1 + i
        set_px(px, x, y, lighten(p, 30))
        set_px(px, x + 1, y, p)
        set_px(px, x - 1, y + 1, darken(p, 10))
    # Blade edge highlight
    for i in range(8):
        x = 12 - i
        y = 1 + i
        set_px(px, x + 1, y - 1, lighten(s, 40), 180)
    # Guard
    for x in range(2, 8):
        set_px(px, x, 11, s)
        set_px(px, x, 12, darken(s, 20))
    # Handle
    for y in range(12, 15):
        set_px(px, 3, y, darken(d, 20))
        set_px(px, 4, y, darken(d, 10))
    # Pommel
    set_px(px, 3, 15, lighten(s, 30))
    set_px(px, 4, 15, lighten(s, 30))

def draw_trident(px, pal):
    """Trident shape - three prongs."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Three prongs
    for y in range(1, 7):
        t = y / 6.0
        # Left prong
        set_px(px, 4, y, lerp_color(lighten(p, 30), p, t))
        # Center prong (taller)
        set_px(px, 7, y - 1 if y > 0 else 0, lerp_color(lighten(s, 30), p, t))
        set_px(px, 8, y - 1 if y > 0 else 0, lerp_color(s, darken(p, 10), t))
        # Right prong
        set_px(px, 11, y, lerp_color(lighten(p, 30), p, t))
    # Prong tips
    set_px(px, 4, 0, lighten(s, 50))
    set_px(px, 7, 0, lighten(s, 50))
    set_px(px, 11, 0, lighten(s, 50))
    # Connecting bar
    for x in range(4, 12):
        set_px(px, x, 7, p)
    # Shaft
    for y in range(8, 15):
        t = (y - 8) / 6.0
        set_px(px, 7, y, lerp_color(darken(p, 20), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(p, 10), darken(d, 20), t))
    # Bottom
    set_px(px, 7, 15, darken(d, 40))
    set_px(px, 8, 15, darken(d, 40))

def draw_axe_weapon(px, pal):
    """Battle axe shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Axe head (right side)
    for y in range(2, 9):
        t = abs(y - 5) / 3.0
        width = max(1, 4 - int(t * 3))
        for x in range(8, 8 + width + 2):
            ct = (x - 8) / max(1, width + 1)
            set_px(px, x, y, lerp_color(lighten(p, 20), d, ct))
    # Axe edge
    for y in range(2, 9):
        edge_x = 8 + max(1, 4 - int(abs(y - 5) / 3.0 * 3)) + 1
        set_px(px, min(15, edge_x), y, lighten(s, 40))
    # Handle (vertical)
    for y in range(1, 15):
        set_px(px, 6, y, darken(d, 20))
        set_px(px, 7, y, darken(d, 10))
    # Handle wrapping
    for y in range(10, 14, 2):
        set_px(px, 6, y, s)
        set_px(px, 7, y, darken(s, 10))

def draw_whip(px, pal):
    """Whip curve shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Handle (bottom-left)
    for y in range(11, 15):
        set_px(px, 2, y, darken(d, 20))
        set_px(px, 3, y, darken(d, 10))
    # Handle grip
    set_px(px, 2, 14, lighten(s, 30))
    set_px(px, 3, 14, lighten(s, 30))
    # Whip body (curving upward and to the right)
    whip_path = [(3, 10), (4, 9), (5, 8), (6, 7), (7, 6), (8, 5),
                 (9, 4), (10, 3), (11, 3), (12, 2), (13, 2), (14, 1)]
    for i, (wx, wy) in enumerate(whip_path):
        t = i / len(whip_path)
        c = lerp_color(p, lighten(s, 20), t)
        set_px(px, wx, wy, c)
        # Thickness
        set_px(px, wx, wy + 1, darken(c, 20))
    # Whip crack tip
    set_px(px, 14, 0, lighten(s, 60))
    set_px(px, 15, 0, lighten(s, 50), 180)
    # Handle crossguard
    for x in range(1, 5):
        set_px(px, x, 11, s)

def draw_wand(px, pal):
    """Magic wand shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Wand shaft (diagonal)
    for i in range(12):
        x = 4 + int(i * 0.5)
        y = 14 - i
        t = i / 11.0
        set_px(px, x, y, lerp_color(darken(d, 10), lighten(p, 10), t))
        set_px(px, x + 1, y, lerp_color(darken(d, 20), p, t))
    # Crystal/orb at tip
    cx, cy = 9, 2
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            dist = abs(dx) + abs(dy)
            if dist <= 1:
                c = lighten(s, 60) if dist == 0 else lighten(s, 30)
                set_px(px, cx + dx, cy + dy, c)
    # Sparkle
    set_px(px, 11, 1, lighten(s, 70), 150)
    set_px(px, 8, 0, lighten(s, 70), 150)
    # Handle wrap
    for y in range(12, 15):
        set_px(px, 4, y, s)
        set_px(px, 5, y, darken(s, 15))
    # Pommel
    set_px(px, 4, 15, lighten(s, 30))

def draw_katana(px, pal):
    """Katana - slightly curved single-edge blade."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Blade (diagonal, long and thin)
    for i in range(11):
        x = 11 - i
        y = 1 + i
        # Slight curve
        x_off = 1 if i > 5 else 0
        set_px(px, x + x_off, y, lighten(p, 40))
        set_px(px, x + x_off + 1, y, lighten(p, 20))
    # Blade edge shine
    for i in range(9):
        x = 12 - i
        y = 1 + i
        set_px(px, x + 1, y, lighten(s, 50), 160)
    # Tsuba (guard) - small square
    for x in range(1, 5):
        set_px(px, x, 12, s)
        set_px(px, x, 13, darken(s, 20))
    # Handle (tsuka)
    set_px(px, 1, 14, darken(d, 30))
    set_px(px, 2, 14, darken(d, 20))
    set_px(px, 1, 15, darken(d, 30))
    set_px(px, 2, 15, darken(d, 20))
    # Wrapping pattern
    set_px(px, 1, 14, lighten(s, 10))

def draw_shield(px, pal):
    """Shield shape (kite shield)."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Shield body (wide at top, pointed at bottom)
    for y in range(1, 14):
        t = (y - 1) / 12.0
        width = max(1, int(7 - t * 5))
        start = 8 - width
        end = 8 + width
        for x in range(start, end):
            ct = abs(x - 7.5) / max(1, width)
            c = lerp_color(lighten(p, 15), darken(p, 15), ct)
            set_px(px, x, y, c)
    # Rim
    for y in range(1, 14):
        t = (y - 1) / 12.0
        width = max(1, int(7 - t * 5))
        set_px(px, 8 - width, y, lighten(s, 30))
        set_px(px, 7 + width, y, darken(d, 20))
    # Cross emblem
    for y in range(4, 10):
        set_px(px, 7, y, s)
        set_px(px, 8, y, s)
    for x in range(4, 12):
        set_px(px, x, 6, s)
        set_px(px, x, 7, s)
    # Boss (center circle)
    set_px(px, 7, 7, lighten(s, 50))
    set_px(px, 8, 7, lighten(s, 40))

def draw_greatshield(px, pal):
    """Large tower shield shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Large rectangular body with rounded top
    for y in range(1, 15):
        t = (y - 1) / 13.0
        width = 6 if y < 3 else 7
        if y > 12:
            width = max(3, width - (y - 12) * 2)
        start = 8 - width
        end = 8 + width
        for x in range(start, min(16, end)):
            ct = abs(x - 7.5) / max(1, width)
            c = lerp_color(p, darken(p, 20), ct * 0.5 + t * 0.5)
            set_px(px, x, y, c)
    # Horizontal bands
    for x in range(2, 14):
        set_px(px, x, 4, lighten(s, 20))
        set_px(px, x, 8, lighten(s, 20))
        set_px(px, x, 12, lighten(s, 20))
    # Central emblem
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            if abs(dx) + abs(dy) <= 1:
                set_px(px, 7 + dx, 6 + dy, lighten(s, 40))
    # Rim highlight
    for y in range(1, 14):
        set_px(px, 1, y, lighten(p, 30))

def draw_throwing_axe(px, pal):
    """Small throwing axe."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Axe head (top right)
    for y in range(1, 6):
        t = abs(y - 3) / 2.0
        width = max(1, 3 - int(t * 2))
        for x in range(8, 8 + width + 1):
            set_px(px, x, y, lerp_color(lighten(p, 20), p, (x - 8) / max(1, width)))
    # Edge
    for y in range(1, 6):
        edge_x = 8 + max(1, 3 - int(abs(y - 3) / 2.0 * 2))
        set_px(px, min(15, edge_x), y, lighten(s, 40))
    # Short handle (diagonal)
    for i in range(8):
        x = 7 - int(i * 0.3)
        y = 6 + i
        set_px(px, x, y, darken(d, 15))
        set_px(px, x - 1, y, darken(d, 25))
    # Wrapping
    set_px(px, 6, 8, s)
    set_px(px, 5, 10, s)
    set_px(px, 5, 12, s)

def draw_rapier(px, pal):
    """Thin rapier blade."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Thin blade (diagonal)
    for i in range(11):
        x = 11 - i
        y = 1 + i
        set_px(px, x, y, lighten(p, 35))
        # Thin shine line
        if i % 2 == 0:
            set_px(px, x + 1, y, lighten(s, 50), 140)
    # Tip
    set_px(px, 12, 0, lighten(s, 60))
    # Cup guard (curved)
    guard = [(2, 11), (3, 11), (4, 11), (5, 12), (1, 12), (0, 13)]
    for gx, gy in guard:
        set_px(px, gx, gy, s)
    # Handle
    set_px(px, 1, 13, darken(d, 10))
    set_px(px, 1, 14, darken(d, 20))
    set_px(px, 0, 14, darken(d, 10))
    set_px(px, 0, 15, lighten(s, 30))

def draw_longsword(px, pal):
    """Longsword - wider blade than katana."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Wide blade (diagonal)
    for i in range(10):
        x = 11 - i
        y = 1 + i
        set_px(px, x, y, lighten(p, 30))
        set_px(px, x + 1, y, p)
        set_px(px, x - 1, y + 1, darken(p, 10))
        set_px(px, x + 2, y, darken(p, 20), 180)
    # Blade tip
    set_px(px, 12, 0, lighten(s, 50))
    # Cross guard
    for x in range(0, 7):
        set_px(px, x, 11, s)
        set_px(px, x, 12, darken(s, 20))
    # Handle
    for y in range(13, 16):
        set_px(px, 1, y, darken(d, 15))
        set_px(px, 2, y, darken(d, 25))
    # Pommel
    set_px(px, 1, 15, lighten(s, 20))
    set_px(px, 0, 15, lighten(s, 20))

def draw_claymore(px, pal):
    """Large claymore - wide two-handed sword."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Wide blade
    for i in range(9):
        x = 11 - i
        y = 1 + i
        for w in range(-1, 2):
            t = abs(w) / 1.5
            set_px(px, x + w, y, lerp_color(lighten(p, 30), darken(p, 10), t))
        set_px(px, x + 2, y, lighten(s, 30), 140)
    # Blade tip
    set_px(px, 12, 0, lighten(s, 50))
    set_px(px, 13, 0, lighten(s, 40), 120)
    # Wide cross guard
    for x in range(0, 8):
        set_px(px, x, 10, lighten(s, 20))
        set_px(px, x, 11, s)
    # Long handle
    for y in range(12, 16):
        set_px(px, 2, y, darken(d, 10))
        set_px(px, 3, y, darken(d, 20))
    # Handle wrapping
    set_px(px, 2, 13, s)
    set_px(px, 3, 13, darken(s, 10))
    # Pommel
    set_px(px, 1, 15, lighten(s, 30))
    set_px(px, 2, 15, lighten(s, 30))

def draw_dagger(px, pal):
    """Short dagger."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Short blade
    for i in range(6):
        x = 10 - i
        y = 3 + i
        set_px(px, x, y, lighten(p, 30))
        set_px(px, x + 1, y, lighten(p, 10))
    # Blade tip
    set_px(px, 11, 2, lighten(s, 50))
    # Guard
    for x in range(3, 7):
        set_px(px, x, 9, s)
        set_px(px, x, 10, darken(s, 15))
    # Handle
    for y in range(10, 14):
        set_px(px, 4, y, darken(d, 15))
        set_px(px, 5, y, darken(d, 25))
    # Pommel
    set_px(px, 4, 14, lighten(s, 25))
    set_px(px, 5, 14, lighten(s, 25))

def draw_double_axe(px, pal):
    """Double-headed battle axe."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Left axe head
    for y in range(2, 7):
        t = abs(y - 4) / 2.0
        width = max(1, 3 - int(t * 2))
        for x in range(4 - width, 5):
            set_px(px, x, y, lerp_color(lighten(p, 20), p, (4 - x) / max(1, width)))
    # Right axe head
    for y in range(2, 7):
        t = abs(y - 4) / 2.0
        width = max(1, 3 - int(t * 2))
        for x in range(11, 11 + width + 1):
            set_px(px, x, y, lerp_color(lighten(p, 20), p, (x - 11) / max(1, width)))
    # Edges
    for y in range(2, 7):
        set_px(px, max(0, 4 - max(1, 3 - int(abs(y - 4) / 2.0 * 2)) - 1), y, lighten(s, 40))
        set_px(px, min(15, 11 + max(1, 3 - int(abs(y - 4) / 2.0 * 2))), y, lighten(s, 40))
    # Handle connecting heads
    for x in range(5, 11):
        set_px(px, x, 4, darken(d, 15))
    # Handle shaft
    for y in range(7, 15):
        set_px(px, 7, y, darken(d, 15))
        set_px(px, 8, y, darken(d, 25))
    # Wrapping
    set_px(px, 7, 9, s)
    set_px(px, 7, 11, s)
    set_px(px, 7, 13, s)

def draw_glaive(px, pal):
    """Polearm glaive - blade on a pole."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Blade (top, curved)
    for y in range(1, 6):
        t = (y - 1) / 4.0
        width = 2 + int((1 - t) * 2)
        for x in range(7, 7 + width):
            ct = (x - 7) / max(1, width - 1)
            set_px(px, x, y, lerp_color(lighten(p, 25), darken(p, 10), ct))
    # Blade edge
    for y in range(1, 6):
        t = (y - 1) / 4.0
        edge_x = 7 + 2 + int((1 - t) * 2)
        set_px(px, min(15, edge_x), y, lighten(s, 45))
    # Blade tip
    set_px(px, 8, 0, lighten(s, 50))
    # Shaft
    for y in range(6, 16):
        t = (y - 6) / 9.0
        set_px(px, 7, y, lerp_color(darken(d, 10), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(d, 5), darken(d, 25), t))
    # Decorative ring
    set_px(px, 6, 6, s)
    set_px(px, 7, 6, lighten(s, 20))
    set_px(px, 8, 6, lighten(s, 20))
    set_px(px, 9, 6, s)

def draw_hammer(px, pal):
    """War hammer shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Hammer head (top, wide rectangle)
    for y in range(1, 6):
        t = (y - 1) / 4.0
        for x in range(3, 13):
            ct = abs(x - 7.5) / 5.0
            c = lerp_color(lighten(p, 15), darken(p, 15), ct * 0.5 + t * 0.5)
            set_px(px, x, y, c)
    # Head top/bottom bevels
    for x in range(3, 13):
        set_px(px, x, 1, lighten(s, 25))
        set_px(px, x, 5, darken(d, 25))
    # Handle
    for y in range(6, 15):
        t = (y - 6) / 8.0
        set_px(px, 7, y, lerp_color(darken(d, 10), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(d, 5), darken(d, 25), t))
    # Handle wrap
    set_px(px, 7, 8, s)
    set_px(px, 7, 10, s)
    set_px(px, 7, 12, s)
    # Pommel
    set_px(px, 7, 15, lighten(s, 20))
    set_px(px, 8, 15, lighten(s, 20))

def draw_mace(px, pal):
    """Flanged mace shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Mace head (spiky ball)
    center_x, center_y = 7, 4
    for y in range(1, 8):
        for x in range(4, 12):
            dist = ((x - center_x) ** 2 + (y - center_y) ** 2) ** 0.5
            if dist <= 3.5:
                t = dist / 3.5
                c = lerp_color(lighten(p, 20), darken(p, 10), t)
                set_px(px, x, y, c)
    # Flanges (spikes)
    flanges = [(4, 2), (10, 2), (3, 4), (11, 4), (4, 6), (10, 6), (7, 0), (7, 7)]
    for fx, fy in flanges:
        set_px(px, fx, fy, lighten(s, 40))
    # Handle
    for y in range(8, 15):
        t = (y - 8) / 6.0
        set_px(px, 7, y, lerp_color(darken(d, 10), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(d, 5), darken(d, 25), t))
    # Handle wraps
    set_px(px, 7, 10, s)
    set_px(px, 7, 12, s)
    # Pommel
    set_px(px, 7, 15, lighten(s, 25))

def draw_sickle(px, pal):
    """Curved sickle blade."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Curved blade (arc from top to right)
    blade_path = [(5, 1), (6, 1), (7, 2), (8, 2), (9, 3), (10, 4),
                  (11, 5), (11, 6), (10, 7), (9, 8)]
    for i, (bx, by) in enumerate(blade_path):
        t = i / len(blade_path)
        c = lerp_color(lighten(p, 30), darken(p, 10), t)
        set_px(px, bx, by, c)
        # Inner edge
        set_px(px, bx - 1, by + 1, darken(c, 15))
    # Blade edge highlight
    set_px(px, 5, 0, lighten(s, 50))
    set_px(px, 6, 0, lighten(s, 40))
    # Handle
    for y in range(9, 15):
        t = (y - 9) / 5.0
        set_px(px, 7, y, lerp_color(darken(d, 10), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(d, 5), darken(d, 25), t))
    # Wrapping
    set_px(px, 7, 11, s)
    set_px(px, 7, 13, s)

def draw_spear(px, pal):
    """Spear - long shaft with pointed tip."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Spear tip (diamond shape at top)
    tip_pixels = [(7, 0), (6, 1), (7, 1), (8, 1),
                  (5, 2), (6, 2), (7, 2), (8, 2), (9, 2),
                  (6, 3), (7, 3), (8, 3),
                  (7, 4)]
    for tx, ty in tip_pixels:
        t = ty / 4.0
        ct = abs(tx - 7) / 2.0
        c = lerp_color(lighten(p, 30), p, t * 0.5 + ct * 0.5)
        set_px(px, tx, ty, c)
    # Tip highlight
    set_px(px, 7, 0, lighten(s, 60))
    # Shaft
    for y in range(5, 16):
        t = (y - 5) / 10.0
        set_px(px, 7, y, lerp_color(darken(d, 5), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(d, 10), darken(d, 35), t))
    # Wrapping near head
    set_px(px, 7, 5, s)
    set_px(px, 8, 5, darken(s, 10))
    set_px(px, 7, 6, darken(s, 5))

def draw_longbow(px, pal):
    """Longbow shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Bow limb (curved arc)
    bow_path = [(3, 1), (2, 2), (2, 3), (1, 4), (1, 5), (1, 6), (1, 7),
                (1, 8), (1, 9), (1, 10), (2, 11), (2, 12), (3, 13)]
    for i, (bx, by) in enumerate(bow_path):
        t = i / len(bow_path)
        c = lerp_color(darken(d, 10), darken(d, 30), abs(t - 0.5) * 2)
        set_px(px, bx, by, c)
        set_px(px, bx + 1, by, lerp_color(p, darken(p, 15), abs(t - 0.5) * 2))
    # Grip (center)
    for y in range(6, 9):
        set_px(px, 2, y, s)
        set_px(px, 3, y, darken(s, 10))
    # String
    for y in range(1, 14):
        set_px(px, 6, y, lighten(p, 40), 200)
    # Arrow resting on bow
    for y in range(2, 14):
        set_px(px, 9, y, darken(d, 20), 180)
    # Arrowhead
    set_px(px, 9, 1, lighten(s, 30))
    set_px(px, 8, 2, lighten(s, 20))
    set_px(px, 10, 2, lighten(s, 20))
    # Fletching
    set_px(px, 8, 12, lighten(p, 20))
    set_px(px, 10, 12, lighten(p, 20))
    set_px(px, 8, 13, lighten(p, 10))
    set_px(px, 10, 13, lighten(p, 10))

def draw_crossbow(px, pal):
    """Heavy crossbow shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Stock (horizontal body)
    for x in range(3, 14):
        t = (x - 3) / 10.0
        set_px(px, x, 7, lerp_color(darken(d, 5), darken(d, 25), t))
        set_px(px, x, 8, lerp_color(darken(d, 10), darken(d, 30), t))
    # Bow limbs (angled from center)
    for i in range(5):
        # Top limb
        set_px(px, 5 - i, 6 - i, lerp_color(p, darken(p, 20), i / 4.0))
        set_px(px, 6 - i, 6 - i, lerp_color(darken(p, 5), darken(p, 25), i / 4.0))
        # Bottom limb
        set_px(px, 5 - i, 9 + i, lerp_color(p, darken(p, 20), i / 4.0))
        set_px(px, 6 - i, 9 + i, lerp_color(darken(p, 5), darken(p, 25), i / 4.0))
    # String
    for y in range(2, 13):
        if y != 7 and y != 8:
            set_px(px, 3, y, lighten(p, 30), 180)
    # Trigger mechanism
    set_px(px, 10, 9, s)
    set_px(px, 10, 10, darken(s, 10))
    # Bolt
    for x in range(5, 14):
        set_px(px, x, 6, darken(d, 10), 160)
    set_px(px, 14, 6, lighten(s, 40))

def draw_staff(px, pal):
    """Magic staff shape."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Staff shaft (vertical, slightly offset)
    for y in range(4, 16):
        t = (y - 4) / 11.0
        set_px(px, 7, y, lerp_color(darken(d, 5), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(d, 10), darken(d, 35), t))
    # Crystal/orb at top
    cx, cy = 7, 2
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            dist = (dx ** 2 + dy ** 2) ** 0.5
            if dist <= 2.5:
                t = dist / 2.5
                c = lerp_color(lighten(s, 60), p, t)
                alpha = max(150, 255 - int(t * 80))
                set_px(px, cx + dx, cy + dy, c, alpha)
    # Crystal core glow
    set_px(px, 7, 2, lighten(s, 80))
    set_px(px, 8, 2, lighten(s, 70))
    # Prong holders around crystal
    set_px(px, 5, 3, darken(d, 15))
    set_px(px, 10, 3, darken(d, 15))
    set_px(px, 6, 4, darken(d, 10))
    set_px(px, 9, 4, darken(d, 10))
    # Handle wraps
    set_px(px, 7, 8, s)
    set_px(px, 7, 10, s)
    set_px(px, 7, 12, s)

def draw_heal_staff(px, pal):
    """Healing staff - staff with cross/leaf motif."""
    p, s, d = pal["primary"], pal["secondary"], pal["dark"]
    # Staff shaft
    for y in range(6, 16):
        t = (y - 6) / 9.0
        set_px(px, 7, y, lerp_color(darken(d, 5), darken(d, 30), t))
        set_px(px, 8, y, lerp_color(darken(d, 10), darken(d, 35), t))
    # Leaf/cross at top
    # Vertical
    for y in range(0, 6):
        t = abs(y - 2.5) / 2.5
        set_px(px, 7, y, lerp_color(lighten(s, 40), p, t))
        set_px(px, 8, y, lerp_color(lighten(s, 30), darken(p, 10), t))
    # Horizontal
    for x in range(4, 12):
        t = abs(x - 7.5) / 3.5
        set_px(px, x, 2, lerp_color(lighten(s, 35), p, t))
        set_px(px, x, 3, lerp_color(lighten(s, 25), darken(p, 10), t))
    # Healing glow particles
    set_px(px, 5, 1, lighten(s, 60), 150)
    set_px(px, 10, 1, lighten(s, 60), 150)
    set_px(px, 6, 4, lighten(s, 50), 120)
    set_px(px, 9, 4, lighten(s, 50), 120)
    # Wraps
    set_px(px, 7, 8, s)
    set_px(px, 7, 10, s)


# ============================================================
# ITEM DEFINITIONS
# ============================================================

RELICS = [
    # (name, element, shape_function)
    ("wardens_visor", "shadow", draw_visor),
    ("verdant_mask", "nature", draw_mask),
    ("frostweave_veil", "ice", draw_veil),
    ("stormcaller_circlet", "lightning", draw_circlet),
    ("ashen_diadem", "fire/shadow", draw_diadem),
    ("wraith_crown", "shadow", draw_crown),
    ("arcane_gauntlet", "arcane", draw_gauntlet),
    ("iron_fist", "physical", draw_fist),
    ("plague_grasp", "poison", draw_grasp),
    ("sunforged_bracer", "holy", draw_bracer),
    ("stormband", "lightning", draw_band),
    ("gravestone_ring", "shadow", draw_ring),
    ("verdant_signet", "nature", draw_signet),
    ("phoenix_mantle", "fire", draw_mantle),
    ("windrunner_cloak", "physical", draw_cloak),
    ("abyssal_cape", "shadow", draw_cape),
    ("alchemists_sash", "arcane", draw_sash),
    ("guardians_girdle", "physical", draw_girdle),
    ("serpent_belt", "poison", draw_belt),
    ("frostfire_pendant", "ice/fire", draw_pendant),
    ("tidekeeper_amulet", "water", draw_amulet),
    ("bloodstone_choker", "shadow", draw_choker),
    ("thornweave_glove", "nature", draw_glove),
    ("chrono_glove", "arcane", draw_glove),
    ("stormstrider_boots", "lightning", draw_boots),
    ("sandwalker_treads", "physical", draw_treads),
    ("emberstone_band", "fire", draw_band),
    ("void_lantern", "shadow", draw_lantern),
    ("thunderhorn", "lightning", draw_horn),
    ("mending_chalice", "holy", draw_chalice),
]

WEAPONS = [
    # (name, element, shape_function)
    ("voidreaver", "shadow", draw_sword),
    ("solaris", "holy", draw_longsword),
    ("stormfury", "lightning", draw_sword),
    ("briarthorn", "nature", draw_sickle),
    ("abyssal_trident", "water", draw_trident),
    ("pyroclast", "fire", draw_axe_weapon),
    ("whisperwind", "arcane", draw_wand),
    ("soulchain", "shadow", draw_whip),
    ("unique_whip_1", "poison", draw_whip),
    ("unique_whip_2", "fire", draw_whip),
    ("unique_whip_sw", "nature", draw_whip),
    ("unique_wand_1", "arcane", draw_wand),
    ("unique_wand_2", "ice", draw_wand),
    ("unique_wand_sw", "holy", draw_wand),
    ("unique_katana_1", "physical", draw_katana),
    ("unique_katana_2", "shadow", draw_katana),
    ("unique_katana_sw", "fire", draw_katana),
    ("unique_greatshield_1", "physical", draw_greatshield),
    ("unique_greatshield_2", "holy", draw_greatshield),
    ("unique_greatshield_sw", "physical", draw_greatshield),
    ("unique_throwing_axe_1", "lightning", draw_throwing_axe),
    ("unique_throwing_axe_2", "ice", draw_throwing_axe),
    ("unique_throwing_axe_sw", "physical", draw_throwing_axe),
    ("unique_rapier_1", "physical", draw_rapier),
    ("unique_rapier_2", "poison", draw_rapier),
    ("unique_rapier_sw", "holy", draw_rapier),
    ("unique_longsword_1", "fire", draw_longsword),
    ("unique_longsword_2", "shadow", draw_longsword),
    ("unique_claymore_3", "ice", draw_claymore),
    ("unique_dagger_3", "physical", draw_dagger),
    ("unique_double_axe_3", "fire", draw_double_axe),
    ("unique_glaive_3", "arcane", draw_glaive),
    ("unique_hammer_3", "ice", draw_hammer),
    ("unique_mace_3", "holy", draw_mace),
    ("unique_sickle_3", "ice", draw_sickle),
    ("unique_spear_3", "lightning", draw_spear),
    ("unique_longbow_3", "physical", draw_longbow),
    ("unique_heavy_crossbow_3", "arcane", draw_crossbow),
    ("unique_staff_damage_8", "lightning", draw_staff),
    ("unique_staff_heal_3", "nature", draw_heal_staff),
    ("unique_shield_3", "shadow", draw_shield),
]


# ============================================================
# MAIN GENERATION
# ============================================================

def main():
    print(f"Output directory: {OUTPUT_DIR}")
    print(f"Generating {len(RELICS)} relic textures and {len(WEAPONS)} weapon textures ({len(RELICS) + len(WEAPONS)} total)...\n")

    count = 0

    print("=== RELICS ===")
    for name, element, draw_fn in RELICS:
        pal = get_palette(element)
        img, px = create_image()
        draw_fn(px, pal)
        save_texture(img, name)
        count += 1

    print("\n=== WEAPONS ===")
    for name, element, draw_fn in WEAPONS:
        pal = get_palette(element)
        img, px = create_image()
        draw_fn(px, pal)
        save_texture(img, name)
        count += 1

    print(f"\nDone! Generated {count} textures in {OUTPUT_DIR}")

if __name__ == "__main__":
    main()
