"""
Generate textures for Mythic/Eternal dungeon keys and Mythic Netherite gear.
All textures are 16x16 RGBA PNGs.
"""
from PIL import Image, ImageDraw

OUTPUT = "src/main/resources/assets/megamod/textures/item"

def save(img, name):
    img.save(f"{OUTPUT}/{name}.png")
    print(f"  Created {name}.png")

# ============================================================
# Dungeon Keys — based on existing key silhouette pattern
# ============================================================

def draw_key(img, body_color, teeth_color, highlight_color):
    """Draw a key: circular head top-center, shaft down, teeth right."""
    d = ImageDraw.Draw(img)
    # Key head (circle, rows 1-6, cols 5-10)
    head = [
        (6,1),(7,1),(8,1),(9,1),
        (5,2),(6,2),(9,2),(10,2),
        (5,3),(6,3),(9,3),(10,3),
        (5,4),(6,4),(9,4),(10,4),
        (6,5),(7,5),(8,5),(9,5),
    ]
    head_inner = [(7,2),(8,2),(7,3),(8,3),(7,4),(8,4)]
    # Shaft (rows 6-12, col 7-8)
    shaft = []
    for y in range(6, 13):
        shaft.append((7, y))
        shaft.append((8, y))
    # Teeth (rows 10-12, cols 9-11)
    teeth = [
        (9,9),(10,9),
        (9,10),(10,10),(11,10),
        (9,12),(10,12),
    ]
    # Draw
    for x,y in head:
        img.putpixel((x,y), body_color)
    for x,y in head_inner:
        img.putpixel((x,y), highlight_color)
    for x,y in shaft:
        img.putpixel((x,y), body_color)
    for x,y in teeth:
        img.putpixel((x,y), teeth_color)
    # Highlight on shaft
    img.putpixel((7, 7), highlight_color)
    img.putpixel((7, 9), highlight_color)

# Mythic Key — light purple/magenta
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw_key(img,
    body_color=(180, 100, 220, 255),
    teeth_color=(220, 150, 255, 255),
    highlight_color=(240, 200, 255, 255))
save(img, "dungeon_key_mythic")

# Eternal Key — deep gold with amber glow
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
draw_key(img,
    body_color=(220, 170, 40, 255),
    teeth_color=(255, 210, 80, 255),
    highlight_color=(255, 240, 160, 255))
save(img, "dungeon_key_eternal")

# ============================================================
# Mythic Netherite Gear — dark purple-tinted netherite
# ============================================================

# Color palette: dark netherite base with purple mythic shimmer
BASE = (50, 30, 50, 255)       # dark purple-gray
MID = (80, 45, 85, 255)        # purple mid
HIGHLIGHT = (140, 80, 160, 255) # bright purple
ACCENT = (200, 130, 220, 255)  # mythic glow
DARK = (30, 18, 32, 255)       # shadow
HANDLE = (70, 50, 40, 255)     # wood handle
HANDLE_HI = (100, 75, 55, 255)

def draw_sword(img):
    """16x16 sword: blade top-left to bottom-right, handle bottom-right."""
    d = ImageDraw.Draw(img)
    # Blade
    blade = [
        (3,1),(4,1),
        (4,2),(5,2),
        (5,3),(6,3),
        (6,4),(7,4),
        (7,5),(8,5),
        (8,6),(9,6),
        (9,7),(10,7),
    ]
    blade_hi = [(3,1),(5,2),(7,4),(9,6)]
    # Guard
    guard = [(9,8),(10,8),(11,8),(12,8)]
    # Handle
    handle = [(11,9),(12,10),(13,11)]
    # Pommel
    pommel = [(13,12),(14,12),(14,13)]
    for x,y in blade:
        img.putpixel((x,y), MID)
    for x,y in blade_hi:
        img.putpixel((x,y), HIGHLIGHT)
    for x,y in guard:
        img.putpixel((x,y), ACCENT)
    for x,y in handle:
        img.putpixel((x,y), HANDLE)
    for x,y in pommel:
        img.putpixel((x,y), ACCENT)

def draw_axe(img):
    """16x16 axe: blade top, handle going down."""
    # Axe head
    head = [
        (4,1),(5,1),(6,1),
        (3,2),(4,2),(5,2),(6,2),(7,2),
        (3,3),(4,3),(5,3),(6,3),(7,3),
        (4,4),(5,4),(6,4),(7,4),
        (6,5),(7,5),
    ]
    head_hi = [(5,1),(4,2),(4,3),(5,4)]
    # Handle
    handle = [(7,6),(7,7),(8,8),(8,9),(8,10),(9,11),(9,12),(9,13)]
    for x,y in head:
        img.putpixel((x,y), MID)
    for x,y in head_hi:
        img.putpixel((x,y), HIGHLIGHT)
    for x,y in handle:
        img.putpixel((x,y), HANDLE)
    # Handle highlight
    img.putpixel((8,9), HANDLE_HI)

def draw_helmet(img):
    """16x16 helmet."""
    rows = {
        2: range(5, 11),
        3: range(4, 12),
        4: range(3, 13),
        5: range(3, 13),
        6: range(3, 13),
        7: range(3, 13),
        8: range(3, 13),
        9: range(4, 12),
        10: range(4, 12),
        11: range(5, 11),
        12: range(5, 11),
    }
    for y, xs in rows.items():
        for x in xs:
            img.putpixel((x, y), BASE)
    # Mid tones
    for y in [3,4,5]:
        for x in range(5, 11):
            img.putpixel((x, y), MID)
    # Highlight (top visor)
    for x in range(6, 10):
        img.putpixel((x, 3), HIGHLIGHT)
    # Visor slit
    for x in range(5, 11):
        img.putpixel((x, 8), DARK)
    # Accent trim
    for x in range(5, 11):
        img.putpixel((x, 11), ACCENT)

def draw_chestplate(img):
    """16x16 chestplate."""
    # Shoulders + body
    rows = {
        2: list(range(2,6)) + list(range(10,14)),
        3: list(range(2,6)) + list(range(10,14)),
        4: list(range(3,13)),
        5: list(range(4,12)),
        6: list(range(4,12)),
        7: list(range(4,12)),
        8: list(range(4,12)),
        9: list(range(4,12)),
        10: list(range(5,11)),
        11: list(range(5,11)),
        12: list(range(5,11)),
    }
    for y, xs in rows.items():
        for x in xs:
            img.putpixel((x, y), BASE)
    # Mid tones on chest
    for y in range(5, 10):
        for x in range(5, 11):
            img.putpixel((x, y), MID)
    # Highlight center
    for y in range(5, 9):
        img.putpixel((7, y), HIGHLIGHT)
        img.putpixel((8, y), HIGHLIGHT)
    # Accent collar
    for x in range(4, 12):
        img.putpixel((x, 4), ACCENT)

def draw_leggings(img):
    """16x16 leggings."""
    # Belt
    for x in range(4, 12):
        img.putpixel((x, 3), ACCENT)
        img.putpixel((x, 4), MID)
    # Body
    for y in range(5, 9):
        for x in range(4, 12):
            img.putpixel((x, y), BASE)
    # Left leg
    for y in range(9, 14):
        for x in range(4, 7):
            img.putpixel((x, y), BASE)
    # Right leg
    for y in range(9, 14):
        for x in range(9, 12):
            img.putpixel((x, y), BASE)
    # Highlights
    for y in range(5, 8):
        img.putpixel((5, y), MID)
        img.putpixel((10, y), MID)
    for y in range(10, 13):
        img.putpixel((5, y), MID)
        img.putpixel((10, y), MID)

def draw_boots(img):
    """16x16 boots."""
    # Left boot
    for y in range(6, 12):
        for x in range(2, 6):
            img.putpixel((x, y), BASE)
    for x in range(1, 6):
        img.putpixel((x, 12), MID)
    for x in range(1, 6):
        img.putpixel((x, 13), DARK)
    # Right boot
    for y in range(6, 12):
        for x in range(10, 14):
            img.putpixel((x, y), BASE)
    for x in range(10, 15):
        img.putpixel((x, 12), MID)
    for x in range(10, 15):
        img.putpixel((x, 13), DARK)
    # Highlights
    img.putpixel((3, 7), MID)
    img.putpixel((3, 8), MID)
    img.putpixel((11, 7), MID)
    img.putpixel((11, 8), MID)
    # Accent trim
    for x in range(2, 6):
        img.putpixel((x, 6), ACCENT)
    for x in range(10, 14):
        img.putpixel((x, 6), ACCENT)

# Generate all Mythic Netherite textures
for name, draw_fn in [
    ("mythic_netherite_sword", draw_sword),
    ("mythic_netherite_axe", draw_axe),
    ("mythic_netherite_helmet", draw_helmet),
    ("mythic_netherite_chestplate", draw_chestplate),
    ("mythic_netherite_leggings", draw_leggings),
    ("mythic_netherite_boots", draw_boots),
]:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    draw_fn(img)
    save(img, name)

print("\nAll dungeon textures generated!")
