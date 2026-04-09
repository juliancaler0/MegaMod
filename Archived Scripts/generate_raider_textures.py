"""
Generate 64x64 humanoid entity textures for 14 new colony raider types.
Uses standard Minecraft player skin UV layout:
  Head:      (0,0)   8x8 top, (8,0) 8x8 bottom, (0,8) 8x8 right, (8,8) 8x8 front, (16,8) 8x8 left, (24,8) 8x8 back
  Body:      (16,16) front 8x12, (20,16) top 8x4, (28,16) right 4x12, (32,16) back 8x12, (36,16) left 4x12
  Right arm: (40,16) front 4x12, (44,16) top 4x4, (48,16) outer 4x12, (52,16) back 4x12
  Left arm:  (32,48) front 4x12, (36,48) top 4x4, (40,48) outer 4x12, (44,48) back 4x12
  Right leg: (0,16)  front 4x12, (4,16) top 4x4, (8,16) outer 4x12, (12,16) back 4x12
  Left leg:  (16,48) front 4x12, (20,48) top 4x4, (24,48) outer 4x12, (28,48) back 4x12
"""

from PIL import Image, ImageDraw
import os
import random

OUTPUT_DIR = os.path.join(os.path.dirname(__file__),
    "src", "main", "resources", "assets", "megamod", "textures", "entity", "raider")
os.makedirs(OUTPUT_DIR, exist_ok=True)

def new_tex():
    """Create a new 64x64 RGBA image."""
    return Image.new("RGBA", (64, 64), (0, 0, 0, 0))

def fill_rect(img, x, y, w, h, color):
    """Fill a rectangle on the image."""
    for px in range(x, x + w):
        for py in range(y, y + h):
            if 0 <= px < 64 and 0 <= py < 64:
                img.putpixel((px, py), color)

def noise_rect(img, x, y, w, h, base_color, variation=15):
    """Fill with slight color noise."""
    r, g, b = base_color[:3]
    a = base_color[3] if len(base_color) > 3 else 255
    rng = random.Random(r * 1000 + g * 100 + b + x * 7 + y * 13)
    for px in range(x, x + w):
        for py in range(y, y + h):
            if 0 <= px < 64 and 0 <= py < 64:
                dr = rng.randint(-variation, variation)
                dg = rng.randint(-variation, variation)
                db = rng.randint(-variation, variation)
                img.putpixel((px, py), (
                    max(0, min(255, r + dr)),
                    max(0, min(255, g + dg)),
                    max(0, min(255, b + db)),
                    a
                ))

def paint_head(img, skin, hair, helmet=None):
    """Paint head area: top(0,0), bottom(8,0), right(0,8), front(8,8), left(16,8), back(24,8)."""
    # Head top
    noise_rect(img, 8, 0, 8, 8, hair)
    # Head bottom (chin)
    noise_rect(img, 16, 0, 8, 8, skin)
    # Head right
    noise_rect(img, 0, 8, 8, 8, skin)
    # Head front (face)
    noise_rect(img, 8, 8, 8, 8, skin)
    # Eyes
    fill_rect(img, 10, 12, 2, 1, (40, 40, 40, 255))
    fill_rect(img, 14, 12, 2, 1, (40, 40, 40, 255))
    # Mouth
    fill_rect(img, 11, 14, 4, 1, (100, 50, 50, 255))
    # Head left
    noise_rect(img, 16, 8, 8, 8, skin)
    # Head back
    noise_rect(img, 24, 8, 8, 8, hair)

    if helmet:
        # Overlay helmet on top of head
        noise_rect(img, 8, 0, 8, 3, helmet, 10)  # top part
        # Helmet band on front
        fill_rect(img, 8, 8, 8, 2, helmet)
        # Helmet sides
        fill_rect(img, 0, 8, 8, 2, helmet)
        fill_rect(img, 16, 8, 8, 2, helmet)

def paint_body(img, color, belt_color=None):
    """Paint body (torso): offset at (16,16) area."""
    # Body top (shoulders) at (20,16) 8x4
    noise_rect(img, 20, 16, 8, 4, color)
    # Body bottom at (28,16) 8x4
    noise_rect(img, 28, 16, 8, 4, color)
    # Body front at (20,20) 8x12
    noise_rect(img, 20, 20, 8, 12, color)
    # Body right at (16,20) 4x12
    noise_rect(img, 16, 20, 4, 12, color)
    # Body back at (32,20) 8x12
    noise_rect(img, 32, 20, 8, 12, color)
    # Body left at (28, 20) - wait, let me recalculate
    # Actually for 1.21 the UV is:
    # Body: starts at (16,16), width=8, height=12, depth=4
    # Top: (20,16) 8x4
    # Bottom: (28,16) 8x4
    # Right: (16,20) 4x12
    # Front: (20,20) 8x12
    # Left: (28,20) 4x12
    # Back: (32,20) 8x12
    noise_rect(img, 28, 20, 4, 12, color)

    if belt_color:
        # Belt across the waist (bottom rows of body front)
        fill_rect(img, 20, 28, 8, 2, belt_color)
        fill_rect(img, 16, 28, 4, 2, belt_color)
        fill_rect(img, 28, 28, 4, 2, belt_color)
        fill_rect(img, 32, 28, 8, 2, belt_color)

def paint_right_arm(img, color, hand_color=None):
    """Right arm at (40,16)."""
    # Top: (44,16) 4x4
    noise_rect(img, 44, 16, 4, 4, color)
    # Bottom: (48,16) 4x4
    noise_rect(img, 48, 16, 4, 4, color)
    # Right: (40,20) 4x12
    noise_rect(img, 40, 20, 4, 12, color)
    # Front: (44,20) 4x12
    noise_rect(img, 44, 20, 4, 12, color)
    # Left: (48,20) 4x12
    noise_rect(img, 48, 20, 4, 12, color)
    # Back: (52,20) 4x12
    noise_rect(img, 52, 20, 4, 12, color)

    if hand_color:
        # Hands (bottom 3 rows)
        fill_rect(img, 40, 29, 4, 3, hand_color)
        fill_rect(img, 44, 29, 4, 3, hand_color)
        fill_rect(img, 48, 29, 4, 3, hand_color)
        fill_rect(img, 52, 29, 4, 3, hand_color)

def paint_left_arm(img, color, hand_color=None):
    """Left arm at (32,48)."""
    # Top: (36,48) 4x4
    noise_rect(img, 36, 48, 4, 4, color)
    # Bottom: (40,48) 4x4
    noise_rect(img, 40, 48, 4, 4, color)
    # Right: (32,52) 4x12
    noise_rect(img, 32, 52, 4, 12, color)
    # Front: (36,52) 4x12
    noise_rect(img, 36, 52, 4, 12, color)
    # Left: (40,52) 4x12
    noise_rect(img, 40, 52, 4, 12, color)
    # Back: (44,52) 4x12
    noise_rect(img, 44, 52, 4, 12, color)

    if hand_color:
        fill_rect(img, 32, 61, 4, 3, hand_color)
        fill_rect(img, 36, 61, 4, 3, hand_color)
        fill_rect(img, 40, 61, 4, 3, hand_color)
        fill_rect(img, 44, 61, 4, 3, hand_color)

def paint_right_leg(img, color, boot_color=None):
    """Right leg at (0,16)."""
    # Top: (4,16) 4x4
    noise_rect(img, 4, 16, 4, 4, color)
    # Bottom: (8,16) 4x4
    noise_rect(img, 8, 16, 4, 4, color)
    # Right: (0,20) 4x12
    noise_rect(img, 0, 20, 4, 12, color)
    # Front: (4,20) 4x12
    noise_rect(img, 4, 20, 4, 12, color)
    # Left: (8,20) 4x12
    noise_rect(img, 8, 20, 4, 12, color)
    # Back: (12,20) 4x12
    noise_rect(img, 12, 20, 4, 12, color)

    if boot_color:
        fill_rect(img, 0, 28, 4, 4, boot_color)
        fill_rect(img, 4, 28, 4, 4, boot_color)
        fill_rect(img, 8, 28, 4, 4, boot_color)
        fill_rect(img, 12, 28, 4, 4, boot_color)

def paint_left_leg(img, color, boot_color=None):
    """Left leg at (16,48)."""
    # Top: (20,48) 4x4
    noise_rect(img, 20, 48, 4, 4, color)
    # Bottom: (24,48) 4x4
    noise_rect(img, 24, 48, 4, 4, color)
    # Right: (16,52) 4x12
    noise_rect(img, 16, 52, 4, 12, color)
    # Front: (20,52) 4x12
    noise_rect(img, 20, 52, 4, 12, color)
    # Left: (24,52) 4x12
    noise_rect(img, 24, 52, 4, 12, color)
    # Back: (28,52) 4x12
    noise_rect(img, 28, 52, 4, 12, color)

    if boot_color:
        fill_rect(img, 16, 60, 4, 4, boot_color)
        fill_rect(img, 20, 60, 4, 4, boot_color)
        fill_rect(img, 24, 60, 4, 4, boot_color)
        fill_rect(img, 28, 60, 4, 4, boot_color)


# ============================================================
# TEXTURE DEFINITIONS
# ============================================================

def gen_barbarian_axeman():
    """Barbarian Axeman: fur armor, horned helmet, rugged."""
    img = new_tex()
    skin = (180, 140, 110, 255)
    fur = (100, 70, 40, 255)
    dark_fur = (70, 50, 30, 255)
    helmet = (80, 80, 80, 255)

    paint_head(img, skin, (80, 55, 30, 255), helmet)
    # Horns on helmet
    fill_rect(img, 8, 0, 2, 2, (200, 190, 160, 255))  # left horn
    fill_rect(img, 14, 0, 2, 2, (200, 190, 160, 255))  # right horn

    paint_body(img, fur, dark_fur)
    # Fur chest pattern
    fill_rect(img, 22, 22, 4, 2, dark_fur)

    paint_right_arm(img, fur, skin)
    paint_left_arm(img, fur, skin)
    paint_right_leg(img, dark_fur, (50, 35, 20, 255))
    paint_left_leg(img, dark_fur, (50, 35, 20, 255))

    return img

def gen_pirate_captain():
    """Pirate Captain: tricorn hat, fancy blue coat, gold trim."""
    img = new_tex()
    skin = (190, 150, 120, 255)
    coat = (30, 50, 100, 255)
    gold = (200, 170, 50, 255)
    hat = (25, 25, 30, 255)

    paint_head(img, skin, (50, 30, 15, 255))
    # Tricorn hat
    noise_rect(img, 8, 0, 8, 4, hat, 5)
    fill_rect(img, 8, 8, 8, 2, hat)
    fill_rect(img, 0, 8, 8, 3, hat)
    fill_rect(img, 16, 8, 8, 3, hat)
    # Gold hat band
    fill_rect(img, 8, 10, 8, 1, gold)
    # Beard
    fill_rect(img, 10, 14, 6, 2, (40, 25, 10, 255))

    paint_body(img, coat, (20, 20, 20, 255))
    # Gold buttons
    fill_rect(img, 23, 22, 1, 1, gold)
    fill_rect(img, 23, 24, 1, 1, gold)
    fill_rect(img, 23, 26, 1, 1, gold)
    # Gold epaulettes
    fill_rect(img, 20, 20, 2, 1, gold)
    fill_rect(img, 26, 20, 2, 1, gold)

    paint_right_arm(img, coat, skin)
    paint_left_arm(img, coat, skin)
    paint_right_leg(img, (40, 40, 50, 255), (20, 20, 25, 255))
    paint_left_leg(img, (40, 40, 50, 255), (20, 20, 25, 255))

    return img

def gen_pirate_gunner():
    """Pirate Gunner: bandana, vest, loose shirt."""
    img = new_tex()
    skin = (200, 160, 130, 255)
    bandana = (160, 30, 30, 255)
    vest = (60, 40, 25, 255)
    shirt = (200, 195, 180, 255)

    paint_head(img, skin, (60, 40, 20, 255))
    # Red bandana
    noise_rect(img, 8, 0, 8, 3, bandana, 10)
    fill_rect(img, 8, 8, 8, 2, bandana)
    fill_rect(img, 0, 8, 8, 2, bandana)
    fill_rect(img, 16, 8, 8, 2, bandana)
    # Eyepatch
    fill_rect(img, 14, 12, 2, 2, (20, 20, 20, 255))

    paint_body(img, shirt, vest)
    # Vest overlay on front
    fill_rect(img, 20, 20, 2, 8, vest)
    fill_rect(img, 26, 20, 2, 8, vest)

    paint_right_arm(img, shirt, skin)
    paint_left_arm(img, shirt, skin)
    paint_right_leg(img, (80, 65, 50, 255), (40, 30, 20, 255))
    paint_left_leg(img, (80, 65, 50, 255), (40, 30, 20, 255))

    return img

def gen_amazon_archer():
    """Amazon Archer: tribal war paint, light leather armor, green accents."""
    img = new_tex()
    skin = (165, 120, 80, 255)
    leather = (90, 60, 35, 255)
    green = (40, 100, 40, 255)
    warpaint = (180, 40, 30, 255)

    paint_head(img, skin, (30, 20, 10, 255))
    # War paint stripes on face
    fill_rect(img, 9, 11, 1, 3, warpaint)
    fill_rect(img, 15, 11, 1, 3, warpaint)
    # Feather in hair
    fill_rect(img, 12, 0, 1, 3, green)
    fill_rect(img, 13, 0, 1, 2, (60, 140, 60, 255))

    paint_body(img, leather, green)
    # Tribal pattern on chest
    fill_rect(img, 22, 22, 1, 1, green)
    fill_rect(img, 24, 22, 1, 1, green)
    fill_rect(img, 23, 23, 1, 1, green)

    paint_right_arm(img, leather, skin)
    paint_left_arm(img, leather, skin)
    paint_right_leg(img, leather, (60, 40, 20, 255))
    paint_left_leg(img, leather, (60, 40, 20, 255))

    return img

def gen_amazon_chief():
    """Amazon Chief: feathered headdress, golden ornaments, vibrant."""
    img = new_tex()
    skin = (165, 120, 80, 255)
    gold = (200, 170, 50, 255)
    green = (30, 110, 30, 255)
    red = (170, 40, 30, 255)

    paint_head(img, skin, (25, 15, 8, 255))
    # Feathered headdress
    fill_rect(img, 8, 0, 8, 2, gold)  # Gold band
    fill_rect(img, 9, 0, 1, 4, green)  # Green feather
    fill_rect(img, 11, 0, 1, 5, red)   # Red feather
    fill_rect(img, 13, 0, 1, 4, green)  # Green feather
    fill_rect(img, 15, 0, 1, 3, (255, 200, 50, 255))  # Yellow feather
    # Gold face jewelry
    fill_rect(img, 10, 13, 1, 1, gold)
    fill_rect(img, 15, 13, 1, 1, gold)
    # Face paint
    fill_rect(img, 8, 12, 1, 2, red)

    paint_body(img, (100, 65, 35, 255), gold)
    # Gold chest plate
    fill_rect(img, 22, 21, 4, 3, gold)
    # Necklace
    fill_rect(img, 21, 20, 6, 1, gold)

    paint_right_arm(img, (100, 65, 35, 255), skin)
    paint_left_arm(img, (100, 65, 35, 255), skin)
    # Gold armbands
    fill_rect(img, 40, 22, 4, 1, gold)
    fill_rect(img, 32, 54, 4, 1, gold)

    paint_right_leg(img, (80, 55, 30, 255), (50, 30, 15, 255))
    paint_left_leg(img, (80, 55, 30, 255), (50, 30, 15, 255))

    return img

def gen_mummy():
    """Mummy: wrapped bandages, exposed skin patches."""
    img = new_tex()
    bandage = (200, 190, 160, 255)
    dark_bandage = (170, 160, 130, 255)
    skin = (100, 85, 60, 255)  # Decayed skin

    paint_head(img, bandage, bandage)
    # Visible wrapping pattern on face
    fill_rect(img, 8, 9, 8, 1, dark_bandage)
    fill_rect(img, 8, 11, 8, 1, dark_bandage)
    fill_rect(img, 8, 13, 8, 1, dark_bandage)
    # Dark eye sockets
    fill_rect(img, 10, 12, 2, 1, (30, 30, 20, 255))
    fill_rect(img, 14, 12, 2, 1, (30, 30, 20, 255))
    # Exposed skin patch
    fill_rect(img, 1, 10, 2, 3, skin)

    paint_body(img, bandage)
    # Bandage wrapping pattern
    for y in range(20, 32, 2):
        fill_rect(img, 20, y, 8, 1, dark_bandage)
        fill_rect(img, 32, y, 8, 1, dark_bandage)
    # Exposed skin patches on body
    fill_rect(img, 24, 23, 2, 2, skin)
    fill_rect(img, 34, 25, 2, 3, skin)

    paint_right_arm(img, bandage)
    paint_left_arm(img, bandage)
    # Arm wrapping
    for y in range(20, 32, 2):
        fill_rect(img, 44, y, 4, 1, dark_bandage)
        fill_rect(img, 36, y + 32, 4, 1, dark_bandage)

    paint_right_leg(img, bandage)
    paint_left_leg(img, bandage)
    for y in range(20, 32, 2):
        fill_rect(img, 4, y, 4, 1, dark_bandage)
        fill_rect(img, 20, y + 32, 4, 1, dark_bandage)

    return img

def gen_archer_mummy():
    """Archer Mummy: bandages with slightly darker tint, quiver hint on back."""
    img = new_tex()
    bandage = (185, 175, 145, 255)
    dark_bandage = (155, 145, 120, 255)
    skin = (90, 75, 50, 255)
    quiver = (80, 50, 30, 255)

    paint_head(img, bandage, bandage)
    fill_rect(img, 8, 9, 8, 1, dark_bandage)
    fill_rect(img, 8, 11, 8, 1, dark_bandage)
    fill_rect(img, 8, 13, 8, 1, dark_bandage)
    fill_rect(img, 10, 12, 2, 1, (30, 30, 20, 255))
    fill_rect(img, 14, 12, 2, 1, (30, 30, 20, 255))
    # Glowing green eye
    fill_rect(img, 10, 12, 1, 1, (60, 180, 60, 255))

    paint_body(img, bandage)
    for y in range(20, 32, 2):
        fill_rect(img, 20, y, 8, 1, dark_bandage)
        fill_rect(img, 32, y, 8, 1, dark_bandage)
    # Quiver strap across chest
    fill_rect(img, 20, 21, 1, 8, quiver)
    fill_rect(img, 21, 22, 1, 7, quiver)
    # Quiver on back
    fill_rect(img, 34, 20, 3, 10, quiver)
    fill_rect(img, 35, 20, 1, 2, (120, 100, 70, 255))  # Arrow tips

    paint_right_arm(img, bandage)
    paint_left_arm(img, bandage)
    for y in range(20, 32, 2):
        fill_rect(img, 44, y, 4, 1, dark_bandage)

    paint_right_leg(img, bandage)
    paint_left_leg(img, bandage)
    for y in range(20, 32, 2):
        fill_rect(img, 4, y, 4, 1, dark_bandage)

    return img

def gen_pharaoh():
    """Pharaoh: gold mask, blue/gold striped headdress (nemes), royal robes."""
    img = new_tex()
    gold = (220, 185, 50, 255)
    blue = (30, 60, 160, 255)
    dark_gold = (170, 140, 30, 255)
    white = (240, 235, 220, 255)

    # Head: gold mask
    paint_head(img, gold, gold)
    # Blue/gold striped nemes headdress
    for x in range(8, 16):
        for y in range(0, 8):
            if (x + y) % 3 == 0:
                img.putpixel((x, y), blue)
            else:
                img.putpixel((x, y), gold)
    # Face mask details
    fill_rect(img, 10, 12, 2, 1, (20, 20, 20, 255))  # Eye holes - dark
    fill_rect(img, 14, 12, 2, 1, (20, 20, 20, 255))
    fill_rect(img, 10, 12, 1, 1, (100, 200, 255, 255))  # Glowing blue eyes
    fill_rect(img, 15, 12, 1, 1, (100, 200, 255, 255))
    # Nemes stripes on sides
    for y in range(8, 16):
        if y % 2 == 0:
            fill_rect(img, 0, y, 8, 1, blue)
            fill_rect(img, 16, y, 8, 1, blue)
        else:
            fill_rect(img, 0, y, 8, 1, gold)
            fill_rect(img, 16, y, 8, 1, gold)
    # Nemes back
    for y in range(8, 16):
        if y % 2 == 0:
            fill_rect(img, 24, y, 8, 1, blue)
        else:
            fill_rect(img, 24, y, 8, 1, gold)

    # Body: white robes with gold trim
    paint_body(img, white, gold)
    # Gold chest plate
    fill_rect(img, 21, 20, 6, 4, gold)
    fill_rect(img, 22, 21, 4, 2, blue)  # Blue scarab center
    # Gold trim lines
    fill_rect(img, 20, 24, 8, 1, gold)

    paint_right_arm(img, white, gold)
    paint_left_arm(img, white, gold)
    # Gold bracers
    fill_rect(img, 44, 26, 4, 2, gold)
    fill_rect(img, 36, 58, 4, 2, gold)

    paint_right_leg(img, white, gold)
    paint_left_leg(img, white, gold)

    return img

def gen_viking():
    """Viking: chainmail, round shield implied, leather with metal."""
    img = new_tex()
    skin = (200, 165, 140, 255)
    chainmail = (140, 140, 150, 255)
    leather = (80, 55, 35, 255)
    metal = (170, 170, 180, 255)

    paint_head(img, skin, (180, 140, 60, 255))  # Blonde hair
    # Simple iron helmet
    noise_rect(img, 8, 0, 8, 4, metal, 8)
    fill_rect(img, 8, 8, 8, 2, metal)
    fill_rect(img, 0, 8, 8, 3, metal)
    fill_rect(img, 16, 8, 8, 3, metal)
    # Nose guard
    fill_rect(img, 12, 10, 1, 3, metal)
    # Beard
    fill_rect(img, 10, 14, 6, 2, (180, 140, 60, 255))

    # Body: chainmail
    paint_body(img, chainmail, leather)
    # Chainmail pattern (checkerboard)
    for x in range(20, 28):
        for y in range(20, 30):
            if (x + y) % 2 == 0:
                img.putpixel((x, y), (130, 130, 140, 255))

    paint_right_arm(img, chainmail, skin)
    paint_left_arm(img, chainmail, skin)

    paint_right_leg(img, leather, (50, 35, 20, 255))
    paint_left_leg(img, leather, (50, 35, 20, 255))

    return img

def gen_viking_archer():
    """Viking Archer: leather armor, fur cape hint, no helmet."""
    img = new_tex()
    skin = (195, 160, 135, 255)
    leather = (90, 60, 35, 255)
    fur = (110, 85, 55, 255)

    paint_head(img, skin, (160, 120, 50, 255))  # Sandy blonde
    # Leather cap
    noise_rect(img, 8, 0, 8, 3, leather, 8)
    fill_rect(img, 8, 8, 8, 1, leather)

    paint_body(img, leather, (60, 40, 25, 255))
    # Fur collar/cape
    fill_rect(img, 20, 20, 8, 2, fur)
    fill_rect(img, 32, 20, 8, 4, fur)  # Cape on back
    # Quiver strap
    fill_rect(img, 21, 22, 1, 8, (50, 35, 20, 255))

    paint_right_arm(img, leather, skin)
    paint_left_arm(img, leather, skin)

    paint_right_leg(img, (70, 50, 30, 255), (45, 30, 18, 255))
    paint_left_leg(img, (70, 50, 30, 255), (45, 30, 18, 255))

    return img

def gen_viking_chief():
    """Viking Chief: winged helmet, heavy armor, imposing."""
    img = new_tex()
    skin = (200, 165, 140, 255)
    heavy_metal = (160, 160, 175, 255)
    gold = (200, 170, 50, 255)
    dark_metal = (100, 100, 110, 255)

    paint_head(img, skin, (180, 130, 50, 255))
    # Winged helmet
    noise_rect(img, 8, 0, 8, 5, heavy_metal, 8)
    fill_rect(img, 8, 8, 8, 3, heavy_metal)
    fill_rect(img, 0, 8, 8, 4, heavy_metal)
    fill_rect(img, 16, 8, 8, 4, heavy_metal)
    # Gold crown band
    fill_rect(img, 8, 10, 8, 1, gold)
    # Wings on sides
    fill_rect(img, 0, 8, 2, 1, (230, 230, 240, 255))
    fill_rect(img, 1, 9, 1, 1, (230, 230, 240, 255))
    fill_rect(img, 22, 8, 2, 1, (230, 230, 240, 255))
    fill_rect(img, 22, 9, 1, 1, (230, 230, 240, 255))
    # Big beard
    fill_rect(img, 9, 13, 7, 3, (180, 130, 50, 255))
    fill_rect(img, 10, 15, 5, 1, (160, 115, 40, 255))

    # Heavy armor body
    paint_body(img, heavy_metal, gold)
    # Chest emblem
    fill_rect(img, 22, 22, 4, 3, gold)
    fill_rect(img, 23, 23, 2, 1, dark_metal)
    # Shoulder plates
    fill_rect(img, 20, 20, 2, 2, gold)
    fill_rect(img, 26, 20, 2, 2, gold)

    paint_right_arm(img, heavy_metal, skin)
    paint_left_arm(img, heavy_metal, skin)
    # Gold bracers
    fill_rect(img, 44, 24, 4, 2, gold)
    fill_rect(img, 36, 56, 4, 2, gold)

    paint_right_leg(img, dark_metal, (60, 60, 70, 255))
    paint_left_leg(img, dark_metal, (60, 60, 70, 255))

    return img

def gen_mercenary():
    """Mercenary: iron armor, neutral gray/brown colors, professional."""
    img = new_tex()
    skin = (190, 155, 125, 255)
    iron = (130, 130, 140, 255)
    leather = (75, 55, 35, 255)
    dark_iron = (90, 90, 100, 255)

    paint_head(img, skin, (60, 45, 30, 255))  # Dark hair
    # Iron open-face helmet
    noise_rect(img, 8, 0, 8, 4, iron, 8)
    fill_rect(img, 8, 8, 8, 1, iron)
    fill_rect(img, 0, 8, 8, 2, iron)
    fill_rect(img, 16, 8, 8, 2, iron)

    paint_body(img, iron, leather)
    # Front laces/buckle
    fill_rect(img, 23, 22, 1, 6, (40, 30, 20, 255))
    fill_rect(img, 24, 22, 1, 1, (180, 170, 120, 255))  # Buckle

    paint_right_arm(img, iron, leather)
    paint_left_arm(img, iron, leather)

    paint_right_leg(img, leather, dark_iron)
    paint_left_leg(img, leather, dark_iron)

    return img

def gen_mercenary_crossbow():
    """Mercenary Crossbow: leather armor with crossbow bolts."""
    img = new_tex()
    skin = (185, 150, 120, 255)
    leather = (85, 60, 38, 255)
    dark_leather = (55, 38, 22, 255)
    metal = (150, 150, 160, 255)

    paint_head(img, skin, (70, 50, 30, 255))
    # Leather hood
    noise_rect(img, 8, 0, 8, 5, dark_leather, 8)
    fill_rect(img, 8, 8, 8, 2, dark_leather)
    fill_rect(img, 24, 8, 8, 5, dark_leather)  # Hood on back

    paint_body(img, leather, dark_leather)
    # Bolt quiver across chest
    fill_rect(img, 26, 20, 1, 8, dark_leather)
    fill_rect(img, 27, 20, 1, 8, dark_leather)
    # Bolt tips
    fill_rect(img, 26, 20, 1, 1, metal)
    fill_rect(img, 27, 20, 1, 1, metal)
    # Quiver on back
    fill_rect(img, 34, 20, 3, 10, dark_leather)

    paint_right_arm(img, leather, skin)
    paint_left_arm(img, leather, skin)
    # Leather bracers
    fill_rect(img, 44, 26, 4, 2, dark_leather)

    paint_right_leg(img, dark_leather, (40, 28, 16, 255))
    paint_left_leg(img, dark_leather, (40, 28, 16, 255))

    return img

def gen_mercenary_leader():
    """Mercenary Leader: decorated iron plate, red cape, commanding."""
    img = new_tex()
    skin = (195, 160, 130, 255)
    plate = (150, 150, 165, 255)
    dark_plate = (110, 110, 125, 255)
    red = (150, 30, 30, 255)
    gold = (200, 170, 50, 255)

    paint_head(img, skin, (50, 35, 20, 255))
    # Full iron helm
    noise_rect(img, 8, 0, 8, 5, plate, 8)
    fill_rect(img, 8, 8, 8, 3, plate)
    fill_rect(img, 0, 8, 8, 4, plate)
    fill_rect(img, 16, 8, 8, 4, plate)
    # Gold trim on helm
    fill_rect(img, 8, 10, 8, 1, gold)
    # Visor slit
    fill_rect(img, 10, 12, 6, 1, (20, 20, 20, 255))
    fill_rect(img, 11, 12, 1, 1, (180, 50, 50, 255))  # Red eye glow
    fill_rect(img, 14, 12, 1, 1, (180, 50, 50, 255))

    # Decorated plate armor
    paint_body(img, plate, dark_plate)
    # Gold chest emblem
    fill_rect(img, 22, 21, 4, 3, gold)
    fill_rect(img, 23, 22, 2, 1, red)
    # Red cape on back
    fill_rect(img, 32, 20, 8, 12, red)
    noise_rect(img, 32, 20, 8, 12, red, 10)
    # Shoulder plates
    fill_rect(img, 20, 20, 2, 1, gold)
    fill_rect(img, 26, 20, 2, 1, gold)

    paint_right_arm(img, plate, dark_plate)
    paint_left_arm(img, plate, dark_plate)
    # Gold bracers
    fill_rect(img, 44, 24, 4, 2, gold)
    fill_rect(img, 36, 56, 4, 2, gold)

    paint_right_leg(img, dark_plate, (80, 80, 90, 255))
    paint_left_leg(img, dark_plate, (80, 80, 90, 255))

    return img


# ============================================================
# GENERATE ALL TEXTURES
# ============================================================

textures = {
    "barbarian_axeman": gen_barbarian_axeman,
    "pirate_captain": gen_pirate_captain,
    "pirate_gunner": gen_pirate_gunner,
    "amazon_archer": gen_amazon_archer,
    "amazon_chief": gen_amazon_chief,
    "mummy": gen_mummy,
    "archer_mummy": gen_archer_mummy,
    "pharaoh": gen_pharaoh,
    "viking": gen_viking,
    "viking_archer": gen_viking_archer,
    "viking_chief": gen_viking_chief,
    "mercenary": gen_mercenary,
    "mercenary_crossbow": gen_mercenary_crossbow,
    "mercenary_leader": gen_mercenary_leader,
}

for name, gen_func in textures.items():
    img = gen_func()
    path = os.path.join(OUTPUT_DIR, f"{name}.png")
    img.save(path)
    print(f"Generated: {path}")

print(f"\nDone! Generated {len(textures)} raider textures.")
