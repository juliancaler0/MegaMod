"""
Generate 19 dungeon item textures for MegaMod.
All textures are 16x16 RGBA PNGs using PIL/Pillow.
"""
from PIL import Image, ImageDraw
import os

OUT = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\item"

# Color palettes
IRON_DARK = (74, 74, 74)
IRON_MID = (106, 106, 106)
IRON_LIGHT = (138, 138, 138)
IRON_HIGHLIGHT = (170, 170, 170)

WOOD_DARK = (107, 66, 38)
WOOD_MID = (139, 90, 43)
WOOD_LIGHT = (160, 112, 60)

STONE_DARK = (107, 107, 94)
STONE_MID = (139, 139, 122)
STONE_LIGHT = (170, 160, 144)
STONE_VDARK = (80, 80, 70)

GOLD_BRIGHT = (255, 215, 0)
GOLD_MID = (218, 165, 32)
GOLD_DARK = (180, 130, 20)

CRYSTAL_LIGHT = (126, 204, 221)
CRYSTAL_MID = (74, 168, 184)
CRYSTAL_DARK = (50, 130, 150)

TRANSPARENT = (0, 0, 0, 0)


def new_img():
    return Image.new("RGBA", (16, 16), TRANSPARENT)


def px(img, x, y, color):
    """Set a pixel, with bounds checking and auto alpha."""
    if 0 <= x < 16 and 0 <= y < 16:
        if len(color) == 3:
            color = color + (255,)
        img.putpixel((x, y), color)


def save(img, name):
    img.save(os.path.join(OUT, name))
    print(f"  Saved {name}")


# ============================================================
# 1. spear.png - Wooden spear, diagonal, stone tip
# ============================================================
def gen_spear():
    img = new_img()
    # Shaft: diagonal from bottom-left to top-right
    # Shaft runs roughly from (2,13) to (10,5)
    shaft_pixels = [
        (2, 14), (3, 13), (3, 14),
        (3, 12), (4, 12), (4, 11),
        (5, 11), (5, 10), (6, 10),
        (6, 9), (7, 9), (7, 8),
        (8, 8), (8, 7), (9, 7),
        (9, 6), (10, 6), (10, 5),
    ]
    for (x, y) in shaft_pixels:
        noise = ((x * 7 + y * 3) % 3)
        if noise == 0:
            px(img, x, y, WOOD_DARK)
        elif noise == 1:
            px(img, x, y, WOOD_MID)
        else:
            px(img, x, y, WOOD_LIGHT)

    # Spear tip (iron/stone) at top-right
    tip_pixels = [
        (11, 4, IRON_MID), (11, 5, IRON_DARK),
        (12, 3, IRON_LIGHT), (12, 4, IRON_MID),
        (13, 2, IRON_LIGHT), (13, 3, IRON_MID),
        (14, 1, IRON_HIGHLIGHT), (14, 2, IRON_LIGHT),
        (15, 0, IRON_HIGHLIGHT),
    ]
    for (x, y, c) in tip_pixels:
        px(img, x, y, c)

    # Small binding wrap where tip meets shaft
    px(img, 10, 5, (160, 140, 90))
    px(img, 11, 5, (140, 120, 70))

    save(img, "spear.png")


# ============================================================
# 2. wrought_axe.png - Heavy iron battle axe
# ============================================================
def gen_wrought_axe():
    img = new_img()
    # Handle: diagonal from bottom-left to center
    handle_coords = [
        (2, 14), (3, 13), (4, 12), (5, 11), (6, 10), (7, 9), (8, 8),
    ]
    for (x, y) in handle_coords:
        noise = ((x * 5 + y * 3) % 3)
        c = [WOOD_DARK, WOOD_MID, WOOD_LIGHT][noise]
        px(img, x, y, c)
        px(img, x, y + 1, WOOD_DARK)  # thicker handle

    # Axe head - large brutal iron head at top-right
    # Right side blade (large, curved)
    head = [
        # Core of axe head
        (9, 7, IRON_MID), (10, 6, IRON_MID), (10, 7, IRON_DARK),
        (11, 5, IRON_LIGHT), (11, 6, IRON_MID), (11, 7, IRON_DARK),
        (12, 4, IRON_LIGHT), (12, 5, IRON_MID), (12, 6, IRON_MID), (12, 7, IRON_DARK),
        (13, 3, IRON_HIGHLIGHT), (13, 4, IRON_LIGHT), (13, 5, IRON_MID), (13, 6, IRON_DARK),
        (14, 3, IRON_HIGHLIGHT), (14, 4, IRON_LIGHT), (14, 5, IRON_MID),
        (13, 7, IRON_DARK),
        # Top edge (sharp)
        (10, 5, IRON_HIGHLIGHT), (11, 4, IRON_HIGHLIGHT),
        # Bottom blade extension
        (9, 8, IRON_DARK), (10, 8, IRON_DARK), (11, 8, IRON_DARK),
        (12, 8, IRON_DARK), (13, 8, IRON_DARK),
        # Back spike
        (8, 7, IRON_MID), (9, 6, IRON_MID),
    ]
    for (x, y, c) in head:
        px(img, x, y, c)

    # Edge highlight (sharp edge)
    for y in range(3, 9):
        if y < 5:
            px(img, 14, y, IRON_HIGHLIGHT)
        elif y < 7:
            px(img, 13, y, IRON_LIGHT)

    save(img, "wrought_axe.png")


# ============================================================
# 3. wrought_helm.png - Dark iron medieval helmet
# ============================================================
def gen_wrought_helm():
    img = new_img()

    # Helmet dome (top portion)
    # Row by row from top
    for x in range(5, 11):
        px(img, x, 2, IRON_MID)
    for x in range(4, 12):
        px(img, x, 3, IRON_MID if x in (4, 11) else IRON_LIGHT if x in (5, 6) else IRON_MID)
    for x in range(3, 13):
        px(img, x, 4, IRON_DARK if x in (3, 12) else IRON_LIGHT if x in (4, 5) else IRON_MID)
    for x in range(3, 13):
        px(img, x, 5, IRON_DARK if x in (3, 12) else IRON_LIGHT if x in (4, 5) else IRON_MID)

    # Top highlight
    for x in range(6, 10):
        px(img, x, 2, IRON_HIGHLIGHT)

    # Visor area
    for x in range(3, 13):
        px(img, x, 6, IRON_DARK)  # visor band dark line

    # Visor slit (eyes)
    for x in range(4, 12):
        if x in (5, 6, 9, 10):
            px(img, x, 7, (20, 20, 25))  # dark eye slits
        else:
            px(img, x, 7, IRON_DARK)
    px(img, 3, 7, IRON_DARK)
    px(img, 12, 7, IRON_DARK)

    # Nose guard
    px(img, 7, 7, IRON_MID)
    px(img, 8, 7, IRON_MID)

    # Lower face guard
    for y in range(8, 12):
        for x in range(3, 13):
            if x in (3, 12):
                px(img, x, y, IRON_DARK)
            elif x in (4, 11):
                px(img, x, y, IRON_MID)
            else:
                # Ventilation lines
                if y == 9 and x % 2 == 0:
                    px(img, x, y, (40, 40, 45))
                else:
                    px(img, x, y, IRON_MID)

    # Chin guard / bottom edge
    for x in range(4, 12):
        px(img, x, 12, IRON_DARK)
    for x in range(5, 11):
        px(img, x, 11, IRON_DARK)

    # Neck guard
    for x in range(4, 12):
        px(img, x, 13, IRON_DARK)

    # Rivets
    px(img, 4, 5, IRON_HIGHLIGHT)
    px(img, 11, 5, IRON_HIGHLIGHT)
    px(img, 4, 9, IRON_HIGHLIGHT)
    px(img, 11, 9, IRON_HIGHLIGHT)

    save(img, "wrought_helm.png")


# ============================================================
# 4. earthrend_gauntlet.png - Stone/earth gauntlet
# ============================================================
def gen_earthrend_gauntlet():
    img = new_img()

    # Wrist/cuff area (bottom)
    for y in range(11, 15):
        for x in range(4, 12):
            noise = (x * 3 + y * 7) % 4
            c = [STONE_DARK, STONE_MID, STONE_LIGHT, STONE_VDARK][noise]
            px(img, x, y, c)

    # Palm area
    for y in range(7, 11):
        for x in range(3, 13):
            noise = (x * 5 + y * 3) % 4
            c = [STONE_DARK, STONE_MID, STONE_MID, STONE_VDARK][noise]
            px(img, x, y, c)

    # Fingers (top)
    # Index finger
    for y in range(3, 7):
        px(img, 4, y, STONE_MID)
        px(img, 5, y, STONE_DARK)
    # Middle finger (tallest)
    for y in range(1, 7):
        px(img, 6, y, STONE_MID)
        px(img, 7, y, STONE_DARK)
    # Ring finger
    for y in range(2, 7):
        px(img, 8, y, STONE_MID)
        px(img, 9, y, STONE_DARK)
    # Pinky
    for y in range(4, 7):
        px(img, 10, y, STONE_MID)
        px(img, 11, y, STONE_DARK)

    # Thumb
    px(img, 3, 9, STONE_MID)
    px(img, 2, 10, STONE_MID)
    px(img, 2, 9, STONE_DARK)

    # Stone crack details
    px(img, 6, 9, (60, 55, 50))
    px(img, 7, 10, (60, 55, 50))
    px(img, 8, 9, (60, 55, 50))
    px(img, 9, 10, (60, 55, 50))

    # Earth/crystal accent on knuckles
    px(img, 5, 7, CRYSTAL_MID)
    px(img, 7, 7, CRYSTAL_LIGHT)
    px(img, 9, 7, CRYSTAL_MID)

    # Highlight edges
    px(img, 6, 1, STONE_LIGHT)
    px(img, 7, 1, STONE_LIGHT)

    save(img, "earthrend_gauntlet.png")


# ============================================================
# 5-10. Tribal masks
# ============================================================
def gen_mask(filename, base_color, accent_color, dark_color, highlight_color, expression_fn):
    """Generate a tribal mask with given colors and expression."""
    img = new_img()

    # Mask outline/shape (oval-ish, 10x12)
    rows = {
        2:  (5, 11),
        3:  (4, 12),
        4:  (3, 13),
        5:  (3, 13),
        6:  (3, 13),
        7:  (3, 13),
        8:  (3, 13),
        9:  (3, 13),
        10: (3, 13),
        11: (4, 12),
        12: (4, 12),
        13: (5, 11),
    }

    # Fill base mask shape
    for y, (x_start, x_end) in rows.items():
        for x in range(x_start, x_end):
            # Edge darkening
            if x == x_start or x == x_end - 1:
                px(img, x, y, dark_color)
            elif y == 2 or y == 13:
                px(img, x, y, dark_color)
            else:
                noise = (x * 3 + y * 5) % 3
                if noise == 0:
                    px(img, x, y, base_color)
                else:
                    px(img, x, y, base_color)

    # Brow ridge
    for x in range(4, 12):
        px(img, x, 4, dark_color)

    # Top highlight
    for x in range(6, 10):
        px(img, x, 2, highlight_color)

    # Call expression function to draw eyes, mouth, markings
    expression_fn(img, base_color, accent_color, dark_color, highlight_color)

    save(img, filename)


def fear_expression(img, base, accent, dark, highlight):
    # Eye holes - angular, threatening
    # Left eye (angled down-right)
    for dx in range(3):
        px(img, 4 + dx, 6 - dx // 2, (15, 15, 20))
        px(img, 4 + dx, 7 - dx // 2, (15, 15, 20))
    # Right eye (angled down-left)
    for dx in range(3):
        px(img, 11 - dx, 6 - dx // 2, (15, 15, 20))
        px(img, 11 - dx, 7 - dx // 2, (15, 15, 20))

    # Red accents around eyes
    px(img, 4, 5, accent)
    px(img, 7, 5, accent)
    px(img, 9, 5, accent)
    px(img, 12, 5, accent)

    # Grimacing mouth
    for x in range(5, 11):
        px(img, x, 11, (15, 15, 20))
    # Teeth
    for x in range(6, 10):
        if x % 2 == 0:
            px(img, x, 10, (200, 200, 190))

    # Vertical red streaks
    px(img, 5, 8, accent)
    px(img, 5, 9, accent)
    px(img, 10, 8, accent)
    px(img, 10, 9, accent)


def fury_expression(img, base, accent, dark, highlight):
    # Angry angled eyes
    # Left eye - angled inward
    px(img, 4, 5, (15, 15, 20))
    px(img, 5, 6, (15, 15, 20))
    px(img, 6, 6, (15, 15, 20))
    px(img, 4, 6, (15, 15, 20))
    px(img, 5, 5, dark)  # angry brow

    # Right eye
    px(img, 11, 5, (15, 15, 20))
    px(img, 10, 6, (15, 15, 20))
    px(img, 9, 6, (15, 15, 20))
    px(img, 11, 6, (15, 15, 20))
    px(img, 10, 5, dark)

    # Flame-like accents on forehead
    px(img, 7, 3, accent)
    px(img, 8, 2, accent)
    px(img, 6, 3, (255, 200, 50))
    px(img, 9, 3, (255, 200, 50))

    # Snarling mouth - wide open
    for x in range(5, 11):
        px(img, x, 10, (15, 15, 20))
        px(img, x, 11, (30, 10, 10))
    # Fangs
    px(img, 5, 11, (200, 200, 190))
    px(img, 10, 11, (200, 200, 190))

    # Sharp angle marks on cheeks
    px(img, 4, 8, accent)
    px(img, 4, 9, accent)
    px(img, 11, 8, accent)
    px(img, 11, 9, accent)


def faith_expression(img, base, accent, dark, highlight):
    # Serene, calm eyes - simple ovals
    # Left eye
    px(img, 5, 6, (15, 15, 20))
    px(img, 6, 6, (15, 15, 20))
    px(img, 5, 7, (15, 15, 20))
    px(img, 6, 7, (15, 15, 20))

    # Right eye
    px(img, 9, 6, (15, 15, 20))
    px(img, 10, 6, (15, 15, 20))
    px(img, 9, 7, (15, 15, 20))
    px(img, 10, 7, (15, 15, 20))

    # Gold circular patterns on forehead
    px(img, 7, 3, GOLD_BRIGHT)
    px(img, 8, 3, GOLD_BRIGHT)
    px(img, 6, 3, GOLD_MID)
    px(img, 9, 3, GOLD_MID)
    px(img, 7, 2, GOLD_MID)
    px(img, 8, 2, GOLD_MID)

    # Gentle smile
    px(img, 6, 10, dark)
    for x in range(7, 9):
        px(img, x, 11, dark)
    px(img, 9, 10, dark)

    # Circular cheek markings
    px(img, 4, 8, accent)
    px(img, 4, 9, accent)
    px(img, 11, 8, accent)
    px(img, 11, 9, accent)

    # Third eye dot
    px(img, 7, 5, GOLD_BRIGHT)
    px(img, 8, 5, GOLD_BRIGHT)


def rage_expression(img, base, accent, dark, highlight):
    # Aggressive, jagged eyes
    # Left eye - X shaped
    px(img, 4, 5, (15, 15, 20))
    px(img, 6, 5, (15, 15, 20))
    px(img, 5, 6, (15, 15, 20))
    px(img, 4, 7, (15, 15, 20))
    px(img, 6, 7, (15, 15, 20))

    # Right eye - X shaped
    px(img, 9, 5, (15, 15, 20))
    px(img, 11, 5, (15, 15, 20))
    px(img, 10, 6, (15, 15, 20))
    px(img, 9, 7, (15, 15, 20))
    px(img, 11, 7, (15, 15, 20))

    # Blood drip marks
    px(img, 5, 8, accent)
    px(img, 5, 9, accent)
    px(img, 5, 10, accent)
    px(img, 10, 8, accent)
    px(img, 10, 9, accent)
    px(img, 10, 10, accent)

    # Jagged mouth
    for x in range(5, 11):
        px(img, x, 11, (15, 15, 20))
        if x % 2 == 1:
            px(img, x, 10, (15, 15, 20))  # jagged top

    # Forehead marking
    px(img, 7, 3, accent)
    px(img, 8, 3, accent)


def misery_expression(img, base, accent, dark, highlight):
    # Sad downturned eyes
    # Left eye
    px(img, 5, 6, (15, 15, 20))
    px(img, 6, 6, (15, 15, 20))
    px(img, 4, 7, (15, 15, 20))  # drooping outer corner
    px(img, 5, 7, (15, 15, 20))

    # Right eye
    px(img, 9, 6, (15, 15, 20))
    px(img, 10, 6, (15, 15, 20))
    px(img, 11, 7, (15, 15, 20))  # drooping outer corner
    px(img, 10, 7, (15, 15, 20))

    # Tear drops (light blue)
    tear = (100, 150, 220)
    px(img, 5, 8, tear)
    px(img, 5, 9, tear)
    px(img, 10, 8, tear)
    px(img, 10, 9, tear)
    px(img, 6, 9, tear)
    px(img, 9, 9, tear)

    # Sad mouth - downturned
    px(img, 6, 11, dark)
    for x in range(7, 9):
        px(img, x, 12, dark)
    px(img, 9, 11, dark)

    # Forehead marking
    px(img, 7, 3, accent)
    px(img, 8, 3, accent)
    px(img, 7, 4, accent)
    px(img, 8, 4, accent)


def bliss_expression(img, base, accent, dark, highlight):
    # Happy closed eyes (curved lines)
    # Left eye - upward curve (happy squint)
    px(img, 4, 7, dark)
    px(img, 5, 6, dark)
    px(img, 6, 6, dark)
    px(img, 7, 7, dark)

    # Right eye
    px(img, 8, 7, dark)
    px(img, 9, 6, dark)
    px(img, 10, 6, dark)
    px(img, 11, 7, dark)

    # Sun ray accents from top
    px(img, 7, 1, GOLD_BRIGHT)
    px(img, 8, 1, GOLD_BRIGHT)
    px(img, 5, 2, GOLD_MID)
    px(img, 10, 2, GOLD_MID)
    px(img, 4, 3, (255, 200, 50))
    px(img, 11, 3, (255, 200, 50))

    # Wide smile
    px(img, 5, 10, dark)
    for x in range(6, 10):
        px(img, x, 11, dark)
    px(img, 10, 10, dark)

    # Rosy cheeks
    px(img, 4, 9, (255, 180, 100))
    px(img, 5, 9, (255, 180, 100))
    px(img, 10, 9, (255, 180, 100))
    px(img, 11, 9, (255, 180, 100))


# ============================================================
# 11. great_experience_bottle.png
# ============================================================
def gen_great_experience_bottle():
    img = new_img()

    # Bottle body (large, bulbous) - bright green
    green_bright = (80, 230, 80)
    green_mid = (50, 190, 50)
    green_dark = (30, 140, 30)
    green_glow = (140, 255, 140)

    # Bottle shape
    # Neck
    for y in range(3, 6):
        px(img, 7, y, green_mid)
        px(img, 8, y, green_dark)

    # Stopper/cork (gold)
    px(img, 7, 2, GOLD_BRIGHT)
    px(img, 8, 2, GOLD_MID)
    px(img, 6, 2, GOLD_MID)
    px(img, 9, 2, GOLD_MID)
    px(img, 7, 1, GOLD_DARK)
    px(img, 8, 1, GOLD_DARK)

    # Neck ring
    for x in range(6, 10):
        px(img, x, 5, green_dark)

    # Wide body
    for y in range(6, 13):
        width = 5 if y in (6, 12) else 6
        x_start = 8 - width // 2 - (1 if width == 6 else 0)
        x_end = 8 + width // 2 + (1 if width == 6 else 0)
        for x in range(x_start, x_end):
            if x == x_start:
                px(img, x, y, green_dark)
            elif x == x_end - 1:
                px(img, x, y, green_dark)
            elif y == 6 or y == 12:
                px(img, x, y, green_dark)
            else:
                px(img, x, y, green_mid)

    # Highlight/glow on body
    px(img, 5, 8, green_glow)
    px(img, 5, 9, green_glow)
    px(img, 6, 7, green_bright)
    px(img, 6, 8, green_bright)
    px(img, 6, 9, green_bright)

    # Sparkle effects
    sparkle = (200, 255, 200)
    px(img, 6, 7, sparkle)
    px(img, 9, 9, sparkle)
    px(img, 7, 10, sparkle)
    px(img, 10, 7, sparkle)

    # Bottom
    for x in range(5, 11):
        px(img, x, 13, green_dark)

    save(img, "great_experience_bottle.png")


# ============================================================
# 12. geomancer_helm.png
# ============================================================
def gen_geomancer_helm():
    img = new_img()

    # Helmet dome - earthy stone
    for y in range(4, 13):
        if y == 4:
            rng = range(5, 11)
        elif y == 5:
            rng = range(4, 12)
        elif y in (11, 12):
            rng = range(4, 12)
        else:
            rng = range(3, 13)
        for x in rng:
            noise = (x * 7 + y * 3) % 4
            c = [STONE_DARK, STONE_MID, STONE_LIGHT, STONE_MID][noise]
            if x == min(rng) or x == max(rng):
                c = STONE_VDARK
            px(img, x, y, c)

    # Visor slit
    for x in range(4, 12):
        px(img, x, 7, (30, 30, 28))
    px(img, 7, 7, STONE_MID)  # nose bridge
    px(img, 8, 7, STONE_MID)

    # Crystal accents on top
    px(img, 6, 2, CRYSTAL_LIGHT)
    px(img, 7, 1, CRYSTAL_LIGHT)
    px(img, 8, 1, CRYSTAL_MID)
    px(img, 9, 2, CRYSTAL_MID)
    px(img, 7, 2, CRYSTAL_MID)
    px(img, 8, 2, CRYSTAL_DARK)
    px(img, 7, 3, CRYSTAL_DARK)
    px(img, 8, 3, CRYSTAL_MID)

    # Stone texture cracks
    px(img, 5, 9, (70, 65, 60))
    px(img, 6, 10, (70, 65, 60))
    px(img, 10, 8, (70, 65, 60))

    save(img, "geomancer_helm.png")


# ============================================================
# 13. geomancer_chest.png
# ============================================================
def gen_geomancer_chest():
    img = new_img()
    brown_dark = (90, 70, 50)
    brown_mid = (120, 95, 65)
    brown_light = (150, 120, 85)

    # Chestplate shape
    # Shoulders
    for x in range(2, 6):
        px(img, x, 2, brown_dark)
        px(img, x, 3, brown_mid)
    for x in range(10, 14):
        px(img, x, 2, brown_dark)
        px(img, x, 3, brown_mid)

    # Neck opening
    for x in range(6, 10):
        px(img, x, 2, brown_dark)

    # Main body
    for y in range(4, 14):
        width = 12 if y < 12 else 10 if y < 13 else 8
        x_start = 8 - width // 2
        x_end = 8 + width // 2
        for x in range(x_start, x_end):
            noise = (x * 5 + y * 7) % 4
            if x == x_start or x == x_end - 1:
                px(img, x, y, STONE_VDARK)
            elif noise == 0:
                px(img, x, y, brown_dark)
            elif noise == 1:
                px(img, x, y, brown_mid)
            else:
                px(img, x, y, brown_light)

    # Stone plate overlays
    for y in range(5, 10):
        for x in range(4, 12):
            noise = (x * 3 + y * 11) % 5
            if noise < 2:
                px(img, x, y, STONE_MID)

    # Central crystal
    px(img, 7, 7, CRYSTAL_LIGHT)
    px(img, 8, 7, CRYSTAL_MID)
    px(img, 7, 8, CRYSTAL_MID)
    px(img, 8, 8, CRYSTAL_DARK)

    # Crystal glow
    px(img, 6, 7, (100, 180, 200, 180))
    px(img, 9, 7, (100, 180, 200, 180))
    px(img, 7, 6, (100, 180, 200, 180))
    px(img, 8, 9, (100, 180, 200, 180))

    save(img, "geomancer_chest.png")


# ============================================================
# 14. geomancer_legs.png
# ============================================================
def gen_geomancer_legs():
    img = new_img()
    brown_dark = (90, 70, 50)
    brown_mid = (120, 95, 65)

    # Waistband
    for x in range(3, 13):
        px(img, x, 1, STONE_DARK)
        px(img, x, 2, STONE_MID)

    # Left leg
    for y in range(3, 15):
        for x in range(3, 8):
            noise = (x * 5 + y * 3) % 3
            c = [brown_dark, brown_mid, STONE_MID][noise]
            if x == 3 or x == 7:
                c = STONE_VDARK
            px(img, x, y, c)

    # Right leg
    for y in range(3, 15):
        for x in range(8, 13):
            noise = (x * 5 + y * 3) % 3
            c = [brown_dark, brown_mid, STONE_MID][noise]
            if x == 8 or x == 12:
                c = STONE_VDARK
            px(img, x, y, c)

    # Stone plate at knees
    for x in range(4, 7):
        px(img, x, 8, STONE_LIGHT)
        px(img, x, 9, STONE_MID)
    for x in range(9, 12):
        px(img, x, 8, STONE_LIGHT)
        px(img, x, 9, STONE_MID)

    # Crystal accents at knees
    px(img, 5, 8, CRYSTAL_LIGHT)
    px(img, 10, 8, CRYSTAL_LIGHT)

    # Gap between legs
    for y in range(3, 15):
        # Already handled by separate leg columns (7 and 8 are edges)
        pass

    save(img, "geomancer_legs.png")


# ============================================================
# 15. geomancer_boots.png
# ============================================================
def gen_geomancer_boots():
    img = new_img()

    # Left boot
    for y in range(4, 14):
        for x in range(2, 7):
            noise = (x * 7 + y * 3) % 4
            c = [STONE_DARK, STONE_MID, STONE_MID, STONE_VDARK][noise]
            if x == 2 or x == 6:
                c = STONE_VDARK
            if y == 4 or y == 13:
                c = STONE_VDARK
            px(img, x, y, c)
    # Left toe extension
    for x in range(1, 4):
        px(img, x, 14, STONE_DARK)
        px(img, x, 13, STONE_MID)

    # Right boot
    for y in range(4, 14):
        for x in range(9, 14):
            noise = (x * 7 + y * 3) % 4
            c = [STONE_DARK, STONE_MID, STONE_MID, STONE_VDARK][noise]
            if x == 9 or x == 13:
                c = STONE_VDARK
            if y == 4 or y == 13:
                c = STONE_VDARK
            px(img, x, y, c)
    # Right toe extension
    for x in range(8, 11):
        px(img, x, 14, STONE_DARK)
        px(img, x, 13, STONE_MID)

    # Sole (darker)
    for x in range(1, 7):
        px(img, x, 15, (50, 50, 45))
    for x in range(8, 14):
        px(img, x, 15, (50, 50, 45))

    # Crystal on toe
    px(img, 3, 12, CRYSTAL_LIGHT)
    px(img, 4, 12, CRYSTAL_MID)
    px(img, 10, 12, CRYSTAL_LIGHT)
    px(img, 11, 12, CRYSTAL_MID)

    # Top cuff
    for x in range(2, 7):
        px(img, x, 4, STONE_LIGHT)
    for x in range(9, 14):
        px(img, x, 4, STONE_LIGHT)

    save(img, "geomancer_boots.png")


# ============================================================
# 16. blowgun.png - Bamboo blowgun
# ============================================================
def gen_blowgun():
    img = new_img()
    bamboo_light = (180, 200, 100)
    bamboo_mid = (140, 165, 70)
    bamboo_dark = (100, 130, 50)
    bamboo_node = (90, 115, 40)

    # Long thin tube diagonal from bottom-left to top-right
    tube_pixels = [
        (1, 14), (2, 13), (2, 14),
        (3, 12), (3, 13),
        (4, 11), (4, 12),
        (5, 10), (5, 11),
        (6, 9), (6, 10),
        (7, 8), (7, 9),
        (8, 7), (8, 8),
        (9, 6), (9, 7),
        (10, 5), (10, 6),
        (11, 4), (11, 5),
        (12, 3), (12, 4),
        (13, 2), (13, 3),
        (14, 1), (14, 2),
    ]

    for (x, y) in tube_pixels:
        # Alternate light/dark for bamboo texture
        if (x + y) % 3 == 0:
            px(img, x, y, bamboo_light)
        elif (x + y) % 3 == 1:
            px(img, x, y, bamboo_mid)
        else:
            px(img, x, y, bamboo_dark)

    # Bamboo nodes (rings/joints)
    node_positions = [(4, 11), (4, 12), (8, 7), (8, 8), (12, 3), (12, 4)]
    for (x, y) in node_positions:
        px(img, x, y, bamboo_node)

    # Dark hole at tip (top-right end)
    px(img, 14, 1, (30, 30, 25))
    px(img, 15, 0, (30, 30, 25))

    # Mouthpiece (bottom-left) - slightly wider
    px(img, 0, 15, bamboo_dark)
    px(img, 1, 15, bamboo_dark)
    px(img, 1, 14, bamboo_mid)
    px(img, 0, 14, bamboo_mid)

    # Light highlight along top edge
    highlight_px = [(3, 12), (5, 10), (7, 8), (9, 6), (11, 4), (13, 2)]
    for (x, y) in highlight_px:
        px(img, x, y, bamboo_light)

    save(img, "blowgun.png")


# ============================================================
# 17. dart_ammo.png - Small dart with feathers
# ============================================================
def gen_dart_ammo():
    img = new_img()

    # Dart shaft - thin diagonal from bottom-left to top-right
    shaft_color = (60, 55, 50)
    shaft_light = (80, 75, 70)

    shaft = [
        (5, 12), (6, 11), (7, 10), (8, 9), (9, 8), (10, 7), (11, 6),
    ]
    for (x, y) in shaft:
        px(img, x, y, shaft_color)

    # Sharp pointed tip (top-right)
    px(img, 12, 5, IRON_LIGHT)
    px(img, 13, 4, IRON_HIGHLIGHT)
    px(img, 14, 3, (200, 200, 200))

    # Feather flights at back (bottom-left) - red and green
    red = (200, 50, 40)
    red_dark = (150, 30, 25)
    green = (50, 180, 60)
    green_dark = (30, 130, 40)

    # Red feather (upper)
    px(img, 3, 12, red)
    px(img, 4, 11, red)
    px(img, 3, 11, red_dark)
    px(img, 2, 11, red_dark)

    # Green feather (lower)
    px(img, 4, 14, green)
    px(img, 4, 13, green)
    px(img, 3, 14, green_dark)
    px(img, 3, 13, green_dark)

    # Feather detail
    px(img, 2, 12, red_dark)
    px(img, 5, 13, green_dark)

    save(img, "dart_ammo.png")


# ============================================================
# 18. captured_grottol.png - Stone creature in glass jar
# ============================================================
def gen_captured_grottol():
    img = new_img()

    # Glass jar
    glass = (180, 210, 230, 160)
    glass_edge = (140, 170, 190, 200)
    glass_highlight = (220, 240, 250, 140)

    # Jar body
    for y in range(4, 14):
        if y == 4 or y == 13:
            rng = range(4, 12)
        else:
            rng = range(3, 13)
        for x in rng:
            if x == min(rng) or x == max(rng):
                px(img, x, y, glass_edge)
            elif y == 4 or y == 13:
                px(img, x, y, glass_edge)
            else:
                px(img, x, y, glass)

    # Jar lid (brown/iron)
    for x in range(4, 12):
        px(img, x, 3, (120, 90, 50))
        px(img, x, 2, (140, 110, 60))

    # Jar bottom
    for x in range(4, 12):
        px(img, x, 14, glass_edge)

    # Glass highlight
    for y in range(5, 12):
        px(img, 4, y, glass_highlight)

    # Grottol creature inside (small stone golem)
    grottol_body = (120, 120, 110)
    grottol_dark = (90, 90, 80)
    grottol_eye = (200, 180, 50)

    # Body
    for y in range(8, 13):
        for x in range(6, 10):
            noise = (x * 3 + y * 5) % 3
            c = [grottol_body, grottol_dark, grottol_body][noise]
            px(img, x, y, c)

    # Head
    for x in range(6, 10):
        px(img, x, 6, grottol_dark)
        px(img, x, 7, grottol_body)

    # Eyes (glowing)
    px(img, 7, 7, grottol_eye)
    px(img, 8, 7, grottol_eye)

    # Little legs
    px(img, 6, 13, grottol_dark)
    px(img, 9, 13, grottol_dark)

    save(img, "captured_grottol.png")


# ============================================================
# 19. bluff_rod.png - Purple/magenta glowing rod
# ============================================================
def gen_bluff_rod():
    img = new_img()
    purple_dark = (100, 30, 120)
    purple_mid = (140, 50, 170)
    purple_light = (180, 80, 210)
    purple_glow = (220, 130, 255)

    # Rod shaft diagonal from bottom-left to top-right
    rod_pixels = [
        (3, 14), (4, 13), (5, 12), (6, 11), (7, 10),
        (8, 9), (9, 8), (10, 7), (11, 6), (12, 5),
    ]

    for (x, y) in rod_pixels:
        px(img, x, y, purple_mid)
        # Thicken slightly
        px(img, x + 1, y, purple_dark)
        px(img, x, y + 1, purple_dark)

    # Glow highlights along rod
    glow_positions = [(4, 13), (7, 10), (10, 7), (12, 5)]
    for (x, y) in glow_positions:
        px(img, x, y, purple_light)

    # Tips
    px(img, 13, 4, purple_glow)
    px(img, 13, 5, purple_light)
    px(img, 2, 15, purple_light)
    px(img, 3, 15, purple_dark)

    # Glow particles around rod
    px(img, 5, 10, (200, 120, 240, 120))
    px(img, 9, 6, (200, 120, 240, 120))
    px(img, 11, 4, (200, 120, 240, 100))
    px(img, 6, 13, (200, 120, 240, 100))

    # Top spark
    px(img, 14, 3, (255, 200, 255, 150))

    save(img, "bluff_rod.png")


# ============================================================
# MAIN - Generate all textures
# ============================================================
if __name__ == "__main__":
    print("Generating MegaMod dungeon item textures...")
    print(f"Output: {OUT}\n")

    # 1. Spear
    print("[1/19] Spear")
    gen_spear()

    # 2. Wrought Axe
    print("[2/19] Wrought Axe")
    gen_wrought_axe()

    # 3. Wrought Helm
    print("[3/19] Wrought Helm")
    gen_wrought_helm()

    # 4. Earthrend Gauntlet
    print("[4/19] Earthrend Gauntlet")
    gen_earthrend_gauntlet()

    # 5. Mask of Fear
    print("[5/19] Mask of Fear")
    gen_mask("mask_of_fear.png",
             base_color=(50, 50, 55),        # dark grey
             accent_color=(180, 30, 30),      # red
             dark_color=(25, 25, 30),         # very dark
             highlight_color=(80, 80, 85),    # light grey
             expression_fn=fear_expression)

    # 6. Mask of Fury
    print("[6/19] Mask of Fury")
    gen_mask("mask_of_fury.png",
             base_color=(190, 70, 30),        # red-orange
             accent_color=(255, 140, 20),     # orange/fire
             dark_color=(120, 40, 15),        # dark red
             highlight_color=(240, 120, 50),  # bright orange
             expression_fn=fury_expression)

    # 7. Mask of Faith
    print("[7/19] Mask of Faith")
    gen_mask("mask_of_faith.png",
             base_color=(230, 225, 210),      # white/cream
             accent_color=GOLD_BRIGHT,        # gold
             dark_color=(180, 175, 160),      # light grey
             highlight_color=(250, 245, 235), # near white
             expression_fn=faith_expression)

    # 8. Mask of Rage
    print("[8/19] Mask of Rage")
    gen_mask("mask_of_rage.png",
             base_color=(140, 25, 25),        # crimson
             accent_color=(200, 40, 40),      # bright red
             dark_color=(80, 15, 15),         # dark red
             highlight_color=(180, 50, 50),   # lighter crimson
             expression_fn=rage_expression)

    # 9. Mask of Misery
    print("[9/19] Mask of Misery")
    gen_mask("mask_of_misery.png",
             base_color=(50, 50, 110),        # dark blue
             accent_color=(100, 60, 160),     # purple
             dark_color=(30, 30, 70),         # very dark blue
             highlight_color=(80, 80, 150),   # lighter blue
             expression_fn=misery_expression)

    # 10. Mask of Bliss
    print("[10/19] Mask of Bliss")
    gen_mask("mask_of_bliss.png",
             base_color=(240, 210, 60),       # bright yellow
             accent_color=GOLD_BRIGHT,        # gold
             dark_color=(180, 150, 30),       # dark gold
             highlight_color=(255, 240, 120), # light yellow
             expression_fn=bliss_expression)

    # 11. Great Experience Bottle
    print("[11/19] Great Experience Bottle")
    gen_great_experience_bottle()

    # 12. Geomancer Helm
    print("[12/19] Geomancer Helm")
    gen_geomancer_helm()

    # 13. Geomancer Chest
    print("[13/19] Geomancer Chest")
    gen_geomancer_chest()

    # 14. Geomancer Legs
    print("[14/19] Geomancer Legs")
    gen_geomancer_legs()

    # 15. Geomancer Boots
    print("[15/19] Geomancer Boots")
    gen_geomancer_boots()

    # 16. Blowgun
    print("[16/19] Blowgun")
    gen_blowgun()

    # 17. Dart Ammo
    print("[17/19] Dart Ammo")
    gen_dart_ammo()

    # 18. Captured Grottol
    print("[18/19] Captured Grottol")
    gen_captured_grottol()

    # 19. Bluff Rod
    print("[19/19] Bluff Rod")
    gen_bluff_rod()

    print("\nAll 19 textures generated successfully!")
