"""
Generate 16x16 pixel art textures for special/unique RPG items.
Medieval fantasy style. Pillow required.
"""
from PIL import Image
import os

PROJECT = os.path.dirname(os.path.abspath(__file__))
ITEM_DIR = os.path.join(PROJECT, "src", "main", "resources", "assets", "megamod", "textures", "item")
BLOCK_DIR = os.path.join(PROJECT, "src", "main", "resources", "assets", "megamod", "textures", "block")


def create(output_dir, name, pixels_func):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()
    pixels_func(p)
    path = os.path.join(output_dir, name)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)
    print(f"  Created: {path}")


# ─── Color helpers ───
def shade(base, factor):
    """Darken or lighten a color. factor < 1 darkens, > 1 lightens."""
    r, g, b = base[:3]
    a = base[3] if len(base) == 4 else 255
    return (
        max(0, min(255, int(r * factor))),
        max(0, min(255, int(g * factor))),
        max(0, min(255, int(b * factor))),
        a,
    )


# ═══════════════════════════════════════════════════════════════════
# 1. MAGIC MIRROR
# ═══════════════════════════════════════════════════════════════════
def magic_mirror(p):
    T = (0, 0, 0, 0)
    # Gold colors
    gold_hi  = (255, 215, 0, 255)
    gold     = (218, 165, 32, 255)
    gold_dk  = (160, 120, 20, 255)
    gold_vdk = (120, 90, 15, 255)
    # Mirror surface
    mirror_bg = (180, 200, 220, 255)
    mirror_md = (200, 215, 230, 255)
    mirror_lt = (220, 235, 245, 255)
    mirror_hi = (255, 255, 255, 255)
    sparkle   = (230, 240, 255, 200)

    # Handle (columns 7-8, rows 12-15)
    for y in range(12, 16):
        p[7, y] = gold_dk
        p[8, y] = gold
    # Handle highlight
    p[8, 12] = gold_hi
    p[7, 15] = gold_vdk

    # Handle knob at bottom
    p[6, 15] = gold_dk
    p[9, 15] = gold_dk
    p[7, 14] = gold
    p[8, 14] = gold_hi

    # Frame oval: rows 1-11, cols 4-11
    # Outer gold frame
    frame_pixels = [
        # top edge
        (6, 1), (7, 1), (8, 1), (9, 1),
        # upper sides
        (5, 2), (10, 2),
        (4, 3), (11, 3),
        # middle sides
        (4, 4), (11, 4),
        (4, 5), (11, 5),
        (4, 6), (11, 6),
        (4, 7), (11, 7),
        (4, 8), (11, 8),
        (4, 9), (11, 9),
        # lower sides
        (5, 10), (10, 10),
        (6, 11), (7, 11), (8, 11), (9, 11),
    ]
    for (x, y) in frame_pixels:
        p[x, y] = gold

    # Frame shading: left/top brighter, right/bottom darker
    for (x, y) in [(6, 1), (7, 1), (5, 2), (4, 3), (4, 4), (4, 5), (4, 6)]:
        p[x, y] = gold_hi
    for (x, y) in [(10, 10), (9, 11), (8, 11), (11, 8), (11, 9)]:
        p[x, y] = gold_dk

    # Inner mirror surface
    mirror_fills = [
        (6, 2), (7, 2), (8, 2), (9, 2),
        (5, 3), (6, 3), (7, 3), (8, 3), (9, 3), (10, 3),
        (5, 4), (6, 4), (7, 4), (8, 4), (9, 4), (10, 4),
        (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5),
        (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6),
        (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
        (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8),
        (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9),
        (6, 10), (7, 10), (8, 10), (9, 10),
    ]
    for (x, y) in mirror_fills:
        p[x, y] = mirror_bg

    # Mirror gradient: lighter top-left
    for (x, y) in [(6, 2), (7, 2), (5, 3), (6, 3), (7, 3), (5, 4), (6, 4)]:
        p[x, y] = mirror_lt
    for (x, y) in [(8, 2), (9, 2), (8, 3), (5, 5)]:
        p[x, y] = mirror_md

    # Darker bottom-right
    for (x, y) in [(9, 9), (10, 8), (10, 9), (8, 10), (9, 10)]:
        p[x, y] = shade(mirror_bg, 0.85)

    # Highlight sparkle
    p[6, 3] = mirror_hi
    p[7, 4] = sparkle
    p[5, 4] = mirror_hi

    # Mystical sparkle pixels
    p[9, 5] = (200, 220, 255, 180)
    p[7, 7] = (210, 225, 255, 160)
    p[8, 9] = (195, 215, 240, 140)

    # Ornate frame details - small decorative dots
    p[7, 0] = gold_dk
    p[8, 0] = gold_dk
    p[3, 6] = gold_dk
    p[12, 6] = gold_dk


# ═══════════════════════════════════════════════════════════════════
# 2. HORSE FLUTE
# ═══════════════════════════════════════════════════════════════════
def horse_flute(p):
    wood_lt  = (184, 149, 106, 255)
    wood     = (160, 130, 90, 255)
    wood_dk  = (120, 95, 65, 255)
    hole     = (90, 58, 26, 255)
    gold     = (218, 165, 32, 255)
    gold_hi  = (255, 215, 0, 255)
    gold_dk  = (160, 120, 20, 255)

    # Main tube body: rows 6-9, cols 2-13 (horizontal flute)
    for y in range(7, 10):
        for x in range(2, 14):
            p[x, y] = wood

    # Top edge lighter (top-left lighting)
    for x in range(2, 14):
        p[x, 6] = wood_lt
    # Bottom edge darker
    for x in range(2, 14):
        p[x, 10] = wood_dk

    # Body shading
    for x in range(2, 14):
        p[x, 7] = wood_lt
    for x in range(2, 14):
        p[x, 9] = wood_dk

    # Mouthpiece on left end (tapered)
    p[1, 7] = wood
    p[1, 8] = wood
    p[1, 9] = wood_dk
    p[0, 8] = wood_dk

    # Bell (wider right end)
    p[14, 6] = wood
    p[14, 7] = wood_lt
    p[14, 8] = wood
    p[14, 9] = wood_dk
    p[14, 10] = wood_dk
    p[15, 7] = wood
    p[15, 8] = wood
    p[15, 9] = wood_dk

    # Finger holes
    for x in [4, 6, 8, 10, 12]:
        p[x, 8] = hole
        p[x, 7] = shade(wood, 0.9)

    # Gold trim bands
    for y in range(6, 11):
        p[3, y] = gold
        p[13, y] = gold
    # Trim highlights
    p[3, 6] = gold_hi
    p[13, 6] = gold_hi
    p[3, 10] = gold_dk
    p[13, 10] = gold_dk

    # Gold mouthpiece ring
    p[1, 7] = gold
    p[1, 9] = gold_dk


# ═══════════════════════════════════════════════════════════════════
# 3. INFINITY HAM
# ═══════════════════════════════════════════════════════════════════
def infinity_ham(p):
    meat_lt   = (220, 120, 120, 255)
    meat      = (204, 102, 102, 255)
    meat_dk   = (170, 75, 75, 255)
    meat_vdk  = (140, 55, 55, 255)
    skin      = (180, 130, 80, 255)
    skin_dk   = (140, 100, 60, 255)
    bone_lt   = (250, 235, 210, 255)
    bone      = (240, 224, 192, 255)
    bone_dk   = (210, 195, 165, 255)
    sparkle   = (255, 215, 0, 255)
    sparkle2  = (255, 240, 100, 200)
    sparkle3  = (255, 200, 50, 160)
    fat       = (240, 200, 180, 255)

    # Ham body: rounded leg shape (angled, fat end top-left, bone end bottom-right)
    # Main meat mass
    meat_pixels = [
        # Row 3
        (4, 3), (5, 3), (6, 3), (7, 3), (8, 3),
        # Row 4
        (3, 4), (4, 4), (5, 4), (6, 4), (7, 4), (8, 4), (9, 4),
        # Row 5
        (3, 5), (4, 5), (5, 5), (6, 5), (7, 5), (8, 5), (9, 5), (10, 5),
        # Row 6
        (3, 6), (4, 6), (5, 6), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6),
        # Row 7
        (4, 7), (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7), (11, 7),
        # Row 8
        (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (11, 8),
        # Row 9
        (6, 9), (7, 9), (8, 9), (9, 9), (10, 9), (11, 9),
        # Row 10
        (7, 10), (8, 10), (9, 10), (10, 10), (11, 10),
        # Row 11
        (8, 11), (9, 11), (10, 11),
    ]
    for (x, y) in meat_pixels:
        p[x, y] = meat

    # Top-left lighting on meat
    for (x, y) in [(4, 3), (5, 3), (6, 3), (3, 4), (4, 4), (5, 4), (3, 5), (4, 5)]:
        p[x, y] = meat_lt

    # Bottom-right shadow
    for (x, y) in [(10, 7), (11, 7), (10, 8), (11, 8), (11, 9), (10, 10), (11, 10), (10, 11)]:
        p[x, y] = meat_dk
    for (x, y) in [(9, 11), (11, 9)]:
        p[x, y] = meat_vdk

    # Skin/crust edge on top
    for (x, y) in [(4, 2), (5, 2), (6, 2), (7, 2), (8, 2)]:
        p[x, y] = skin
    for (x, y) in [(3, 3), (2, 4), (2, 5), (2, 6), (3, 7)]:
        p[x, y] = skin
    p[4, 2] = shade(skin, 1.1)
    p[2, 6] = skin_dk
    p[3, 7] = skin_dk

    # Fat layer
    for (x, y) in [(5, 3), (4, 4), (3, 5)]:
        p[x, y] = fat

    # Bone sticking out bottom-right
    p[11, 11] = bone
    p[12, 11] = bone_lt
    p[12, 12] = bone
    p[13, 12] = bone_lt
    p[13, 13] = bone
    p[14, 13] = bone_dk
    p[14, 14] = bone_dk
    # Bone knob
    p[13, 14] = bone
    p[15, 13] = bone
    p[15, 14] = bone_dk
    p[14, 15] = bone_dk

    # Cross-cut face on the open end (left side visible meat grain)
    p[4, 5] = (230, 140, 140, 255)
    p[5, 6] = (230, 140, 140, 255)

    # Golden sparkle pixels for infinite enchantment
    p[1, 1] = sparkle
    p[7, 1] = sparkle2
    p[12, 3] = sparkle
    p[0, 6] = sparkle3
    p[13, 7] = sparkle2
    p[2, 9] = sparkle
    p[5, 12] = sparkle3
    p[14, 1] = sparkle3
    p[10, 13] = sparkle2


# ═══════════════════════════════════════════════════════════════════
# 4. SPACE DISSECTOR
# ═══════════════════════════════════════════════════════════════════
def space_dissector(p):
    purple_dk  = (60, 25, 90, 255)
    purple     = (90, 42, 138, 255)
    purple_lt  = (130, 70, 180, 255)
    purple_hi  = (170, 100, 220, 255)
    silver_dk  = (130, 130, 130, 255)
    silver     = (170, 170, 170, 255)
    silver_lt  = (200, 200, 200, 255)
    silver_hi  = (230, 230, 230, 255)
    ender1     = (170, 68, 255, 255)
    ender2     = (204, 102, 255, 200)
    ender3     = (150, 50, 220, 160)
    void_black = (20, 10, 30, 255)

    # Blade: diagonal from top-right to center, crystal-like
    # Blade occupies upper portion, pointing up-right
    blade_pixels = [
        # Central blade column going up
        (8, 1), (8, 2), (8, 3), (8, 4), (8, 5), (8, 6), (8, 7),
        # Blade width
        (7, 2), (9, 2), (7, 3), (9, 3), (7, 4), (9, 4),
        (7, 5), (9, 5), (7, 6), (9, 6),
        # Blade tip
        (8, 0),
        # Blade base wider
        (6, 7), (7, 7), (9, 7), (10, 7),
    ]
    for (x, y) in blade_pixels:
        p[x, y] = purple

    # Crystal blade shading
    # Left edge lighter (top-left light)
    for (x, y) in [(7, 2), (7, 3), (7, 4), (7, 5), (7, 6), (8, 0), (8, 1)]:
        p[x, y] = purple_lt
    # Right edge darker
    for (x, y) in [(9, 3), (9, 4), (9, 5), (9, 6), (10, 7)]:
        p[x, y] = purple_dk
    # Highlight streak
    p[8, 2] = purple_hi
    p[8, 3] = purple_lt
    # Dark core for crystal depth
    p[8, 5] = void_black
    p[8, 6] = shade(purple_dk, 0.7)

    # Guard / crosspiece
    for x in range(5, 12):
        p[x, 8] = silver
    p[5, 8] = silver_dk
    p[6, 8] = silver_lt
    p[11, 8] = silver_dk
    p[7, 8] = silver_hi
    p[8, 8] = silver_lt

    # Handle
    for y in range(9, 14):
        p[7, y] = silver_dk
        p[8, y] = silver
    # Handle shading
    p[8, 9] = silver_lt
    p[7, 13] = shade(silver_dk, 0.85)

    # Pommel
    p[6, 14] = silver_dk
    p[7, 14] = silver
    p[8, 14] = silver_lt
    p[9, 14] = silver_dk
    p[7, 15] = silver_dk
    p[8, 15] = silver

    # Ender particles swirling around the blade
    p[5, 1] = ender2
    p[11, 3] = ender1
    p[4, 4] = ender3
    p[12, 5] = ender2
    p[3, 6] = ender1
    p[11, 1] = ender3
    p[13, 7] = ender2
    p[5, 5] = ender3
    p[10, 2] = ender1
    # A few particles near handle
    p[5, 10] = ender3
    p[11, 9] = ender2


# ═══════════════════════════════════════════════════════════════════
# 5. BLAZING FLASK
# ═══════════════════════════════════════════════════════════════════
def blazing_flask(p):
    glass_lt  = (190, 210, 230, 180)
    glass     = (170, 187, 204, 160)
    glass_dk  = (140, 160, 180, 140)
    cork      = (139, 105, 20, 255)
    cork_dk   = (110, 80, 15, 255)
    fire_hi   = (255, 220, 50, 255)
    fire_lt   = (255, 150, 30, 255)
    fire      = (255, 102, 0, 255)
    fire_dk   = (255, 51, 0, 255)
    fire_vdk  = (200, 30, 0, 255)
    flame_tip = (255, 240, 100, 200)
    flame_glow= (255, 180, 50, 140)

    # Cork (top, rows 2-3, cols 7-8)
    p[7, 2] = cork
    p[8, 2] = cork
    p[7, 3] = cork_dk
    p[8, 3] = cork

    # Flask neck (rows 4-5, cols 6-9)
    for y in [4, 5]:
        p[6, y] = glass
        p[9, y] = glass_dk
    p[7, 4] = glass_lt
    p[8, 4] = glass
    p[7, 5] = glass_lt
    p[8, 5] = glass

    # Flask body (rows 6-13, cols 4-11)
    # Outline
    body_outline = [
        (5, 6), (10, 6),
        (4, 7), (11, 7),
        (4, 8), (11, 8),
        (4, 9), (11, 9),
        (4, 10), (11, 10),
        (4, 11), (11, 11),
        (4, 12), (11, 12),
        (5, 13), (10, 13),
        (6, 14), (7, 14), (8, 14), (9, 14),
    ]
    for (x, y) in body_outline:
        p[x, y] = glass

    # Left glass brighter
    for (x, y) in [(5, 6), (4, 7), (4, 8), (4, 9)]:
        p[x, y] = glass_lt
    # Right glass darker
    for (x, y) in [(11, 9), (11, 10), (11, 11), (11, 12), (10, 13)]:
        p[x, y] = glass_dk

    # Fire liquid inside
    fire_inner = [
        (6, 6), (7, 6), (8, 6), (9, 6),
        (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
        (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8),
        (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9),
        (5, 10), (6, 10), (7, 10), (8, 10), (9, 10), (10, 10),
        (5, 11), (6, 11), (7, 11), (8, 11), (9, 11), (10, 11),
        (5, 12), (6, 12), (7, 12), (8, 12), (9, 12), (10, 12),
        (6, 13), (7, 13), (8, 13), (9, 13),
    ]
    for (x, y) in fire_inner:
        p[x, y] = fire

    # Fire gradient: lighter at top, darker at bottom
    for (x, y) in [(6, 6), (7, 6), (8, 6), (5, 7), (6, 7), (7, 7)]:
        p[x, y] = fire_lt
    for (x, y) in [(7, 6), (6, 7)]:
        p[x, y] = fire_hi

    for (x, y) in [(8, 12), (9, 12), (10, 12), (7, 13), (8, 13), (9, 13)]:
        p[x, y] = fire_dk
    for (x, y) in [(9, 13), (10, 12)]:
        p[x, y] = fire_vdk

    # Flame tongues rising above cork
    p[7, 1] = flame_tip
    p[8, 1] = flame_glow
    p[7, 0] = flame_glow
    p[6, 0] = (255, 200, 60, 100)
    p[9, 1] = (255, 160, 40, 120)
    p[9, 0] = (255, 140, 30, 80)

    # Glass highlight streak
    p[5, 8] = (220, 230, 240, 200)
    p[5, 9] = (210, 225, 235, 180)


# ═══════════════════════════════════════════════════════════════════
# 6. SPORE SACK
# ═══════════════════════════════════════════════════════════════════
def spore_sack(p):
    cloth_lt  = (130, 100, 65, 255)
    cloth     = (107, 74, 42, 255)
    cloth_dk  = (80, 55, 30, 255)
    cloth_vdk = (60, 40, 20, 255)
    tie       = (90, 65, 35, 255)
    tie_dk    = (65, 45, 25, 255)
    spore_lt  = (102, 204, 102, 220)
    spore     = (68, 170, 68, 200)
    spore_dk  = (50, 140, 50, 180)
    spore_glow= (120, 230, 120, 150)

    # Sack body (rounded, rows 6-14, cols 4-11)
    sack_pixels = [
        (6, 6), (7, 6), (8, 6), (9, 6),
        (5, 7), (6, 7), (7, 7), (8, 7), (9, 7), (10, 7),
        (4, 8), (5, 8), (6, 8), (7, 8), (8, 8), (9, 8), (10, 8), (11, 8),
        (4, 9), (5, 9), (6, 9), (7, 9), (8, 9), (9, 9), (10, 9), (11, 9),
        (4, 10), (5, 10), (6, 10), (7, 10), (8, 10), (9, 10), (10, 10), (11, 10),
        (4, 11), (5, 11), (6, 11), (7, 11), (8, 11), (9, 11), (10, 11), (11, 11),
        (4, 12), (5, 12), (6, 12), (7, 12), (8, 12), (9, 12), (10, 12), (11, 12),
        (5, 13), (6, 13), (7, 13), (8, 13), (9, 13), (10, 13),
        (6, 14), (7, 14), (8, 14), (9, 14),
    ]
    for (x, y) in sack_pixels:
        p[x, y] = cloth

    # Lighting: top-left bright
    for (x, y) in [(6, 6), (7, 6), (5, 7), (6, 7), (7, 7), (4, 8), (5, 8), (6, 8), (4, 9), (5, 9)]:
        p[x, y] = cloth_lt
    # Shadow: bottom-right
    for (x, y) in [(10, 11), (11, 11), (10, 12), (11, 12), (9, 13), (10, 13), (8, 14), (9, 14)]:
        p[x, y] = cloth_dk
    for (x, y) in [(11, 12), (10, 13), (9, 14)]:
        p[x, y] = cloth_vdk

    # Tie at top (gathered cinch)
    # Gathered neck
    p[6, 5] = tie
    p[7, 5] = tie
    p[8, 5] = tie
    p[9, 5] = tie
    p[7, 4] = tie_dk
    p[8, 4] = tie

    # Tie knot / string
    p[6, 4] = tie
    p[9, 4] = tie_dk
    p[7, 3] = cloth
    p[8, 3] = cloth_dk

    # Tie ears sticking up
    p[5, 3] = cloth_lt
    p[5, 2] = cloth
    p[10, 3] = cloth_dk
    p[10, 2] = cloth

    # Wrinkle/fold lines on sack
    p[7, 9] = cloth_dk
    p[6, 11] = cloth_dk
    p[8, 10] = shade(cloth, 0.9)

    # Green spores floating around
    # Leaking from top
    p[6, 2] = spore
    p[8, 1] = spore_lt
    p[4, 3] = spore_dk
    p[11, 4] = spore
    p[3, 5] = spore_glow
    p[12, 6] = spore_lt
    # Floating around sides
    p[2, 7] = spore
    p[13, 9] = spore_dk
    p[1, 10] = spore_glow
    p[13, 11] = spore
    p[3, 13] = spore_lt
    p[12, 13] = spore_dk
    # A few near bottom
    p[5, 15] = spore_glow
    p[10, 15] = spore
    p[7, 0] = spore_dk
    p[14, 7] = spore_glow


# ═══════════════════════════════════════════════════════════════════
# 7. RESEARCHING TABLE TOP
# ═══════════════════════════════════════════════════════════════════
def researching_table_top(p):
    wood      = (58, 42, 26, 255)
    wood_lt   = (74, 58, 42, 255)
    wood_dk   = (45, 32, 20, 255)
    wood_grain= (50, 36, 22, 255)
    page_lt   = (225, 210, 185, 255)
    page      = (210, 180, 140, 255)
    page_dk   = (190, 165, 125, 255)
    binding   = (107, 66, 38, 255)
    binding_dk= (80, 50, 28, 255)
    ink       = (26, 26, 26, 255)
    ink_lt    = (40, 40, 50, 255)
    quill_shaft = (30, 25, 20, 255)
    feather   = (240, 240, 240, 255)
    feather_dk= (200, 200, 200, 255)
    text_line = (60, 50, 40, 200)
    scroll    = (220, 200, 170, 255)

    # Wood surface base
    for y in range(16):
        for x in range(16):
            p[x, y] = wood

    # Wood grain lines (subtle horizontal variation)
    for x in range(16):
        p[x, 3] = wood_grain
        p[x, 7] = wood_grain
        p[x, 11] = wood_grain
        p[x, 14] = wood_grain
    # Some lighter grain
    for x in range(16):
        p[x, 1] = wood_lt
        p[x, 5] = wood_lt
        p[x, 9] = wood_lt
        p[x, 13] = wood_lt

    # Open book in center (rows 4-11, cols 3-12)
    # Left page
    for y in range(4, 12):
        for x in range(3, 8):
            p[x, y] = page
    # Right page
    for y in range(4, 12):
        for x in range(8, 13):
            p[x, y] = page

    # Spine / binding down center
    for y in range(4, 12):
        p[7, y] = binding
        p[8, y] = binding_dk

    # Page shading
    for y in range(4, 12):
        p[3, y] = page_dk
        p[12, y] = page_dk
    for x in range(3, 8):
        p[x, 11] = page_dk
    for x in range(8, 13):
        p[x, 11] = page_dk

    # Lighter top of pages
    for x in range(3, 7):
        p[x, 4] = page_lt
    for x in range(9, 13):
        p[x, 4] = page_lt

    # Text lines on pages (tiny dark dots)
    for x in [4, 5, 6]:
        p[x, 6] = text_line
        p[x, 8] = text_line
        p[x, 10] = text_line
    for x in [9, 10, 11]:
        p[x, 6] = text_line
        p[x, 8] = text_line
        p[x, 10] = text_line

    # Quill pen (top-right area, diagonal)
    p[13, 3] = quill_shaft
    p[12, 4] = quill_shaft
    p[11, 5] = quill_shaft
    p[14, 2] = feather
    p[15, 1] = feather
    p[15, 2] = feather_dk
    p[14, 1] = feather_dk
    p[13, 2] = quill_shaft

    # Ink pot (top-left corner)
    p[1, 1] = ink
    p[2, 1] = ink
    p[1, 2] = ink
    p[2, 2] = ink
    p[1, 0] = ink_lt
    p[2, 0] = ink_lt
    p[0, 1] = ink_lt
    p[3, 1] = ink_lt

    # Scattered scroll/paper edge (bottom-left)
    p[0, 13] = scroll
    p[1, 13] = scroll
    p[2, 13] = scroll
    p[0, 14] = page_dk
    p[1, 14] = page
    p[2, 14] = scroll

    # Paper edge bottom-right
    p[14, 13] = scroll
    p[15, 13] = page
    p[14, 14] = page_dk


# ═══════════════════════════════════════════════════════════════════
# 8. RESEARCHING TABLE SIDE
# ═══════════════════════════════════════════════════════════════════
def researching_table_side(p):
    wood_lt   = (74, 58, 42, 255)
    wood      = (58, 42, 26, 255)
    wood_dk   = (45, 32, 20, 255)
    wood_vdk  = (35, 24, 14, 255)
    plank_line= (40, 28, 18, 255)
    # Book spine colors
    book_red  = (160, 40, 40, 255)
    book_red_dk=(120, 30, 30, 255)
    book_blue = (50, 70, 150, 255)
    book_blue_dk=(35, 50, 110, 255)
    book_green= (40, 110, 50, 255)
    book_green_dk=(30, 80, 35, 255)
    book_brown= (100, 70, 40, 255)
    book_brown_dk=(75, 50, 28, 255)
    book_gold = (180, 150, 50, 255)
    # Drawer
    drawer    = (65, 48, 30, 255)
    drawer_dk = (50, 36, 22, 255)
    gold_knob = (218, 165, 32, 255)
    gold_knob_dk = (160, 120, 20, 255)

    # Top section: dark wood planks (rows 0-7)
    for y in range(8):
        for x in range(16):
            p[x, y] = wood

    # Plank variation
    for x in range(16):
        p[x, 0] = wood_lt  # Top edge highlight (table surface edge)
    for x in range(16):
        p[x, 3] = plank_line
        p[x, 7] = plank_line

    # Vertical plank lines
    for y in range(0, 8):
        p[5, y] = wood_dk
        p[11, y] = wood_dk

    # Lighter planks for variation
    for y in range(1, 3):
        for x in range(6, 11):
            p[x, y] = wood_lt
    for y in range(4, 7):
        for x in range(0, 5):
            p[x, y] = wood_lt

    # Lower section: bookshelf (rows 8-12)
    # Background
    for y in range(8, 13):
        for x in range(16):
            p[x, y] = wood_vdk

    # Book spines (vertical rectangles side by side)
    # Red book
    for y in range(8, 13):
        p[1, y] = book_red
        p[2, y] = book_red
    p[1, 8] = shade(book_red, 1.15)
    p[2, 12] = book_red_dk

    # Blue book
    for y in range(8, 13):
        p[3, y] = book_blue
        p[4, y] = book_blue
    p[3, 8] = shade(book_blue, 1.15)
    p[4, 12] = book_blue_dk

    # Green book (shorter)
    for y in range(9, 13):
        p[5, y] = book_green
        p[6, y] = book_green
    p[5, 9] = shade(book_green, 1.15)
    p[6, 12] = book_green_dk

    # Brown book
    for y in range(8, 13):
        p[7, y] = book_brown
        p[8, y] = book_brown
    p[7, 8] = shade(book_brown, 1.15)
    p[8, 12] = book_brown_dk

    # Gold-trimmed book
    for y in range(8, 13):
        p[9, y] = book_red_dk
        p[10, y] = book_red_dk
    p[9, 8] = book_gold
    p[10, 8] = book_gold
    p[9, 12] = book_gold

    # Another blue book
    for y in range(9, 13):
        p[11, y] = book_blue_dk
        p[12, y] = book_blue
    p[12, 9] = shade(book_blue, 1.15)

    # Small brown book
    for y in range(8, 13):
        p[13, y] = book_brown_dk
        p[14, y] = book_brown
    p[14, 8] = shade(book_brown, 1.15)

    # Shelf borders
    for x in range(16):
        p[x, 8] = wood_dk  # shelf top
    p[0, 8] = wood
    p[15, 8] = wood
    for y in range(8, 13):
        p[0, y] = wood_dk
        p[15, y] = wood_dk

    # Drawer section (rows 13-15)
    for y in range(13, 16):
        for x in range(16):
            p[x, y] = drawer

    # Drawer lines
    for x in range(16):
        p[x, 13] = drawer_dk  # top line
    for y in range(13, 16):
        p[0, y] = wood_dk
        p[8, y] = drawer_dk
        p[15, y] = wood_dk

    # Gold knobs on drawers
    p[4, 14] = gold_knob
    p[4, 15] = gold_knob_dk
    p[12, 14] = gold_knob
    p[12, 15] = gold_knob_dk

    # Bottom edge
    for x in range(16):
        p[x, 15] = wood_vdk


# ═══════════════════════════════════════════════════════════════════
# MAIN - Generate all textures
# ═══════════════════════════════════════════════════════════════════
if __name__ == "__main__":
    print("Generating special item textures...")

    # Item textures
    create(ITEM_DIR, "magic_mirror.png", magic_mirror)
    create(ITEM_DIR, "horse_flute.png", horse_flute)
    create(ITEM_DIR, "infinity_ham.png", infinity_ham)
    create(ITEM_DIR, "space_dissector.png", space_dissector)
    create(ITEM_DIR, "blazing_flask.png", blazing_flask)
    create(ITEM_DIR, "spore_sack.png", spore_sack)

    # Block textures
    create(BLOCK_DIR, "researching_table_top.png", researching_table_top)
    create(BLOCK_DIR, "researching_table_side.png", researching_table_side)

    print("\nAll textures generated successfully!")
