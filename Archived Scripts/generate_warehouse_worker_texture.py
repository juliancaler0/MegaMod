"""
Generates warehouse_worker.png — a UPS-inspired brown uniform skin
for the Warehouse Worker citizen using standard 64x64 player skin UV layout.

Player skin UV layout (64x64):
HEAD (8x8x8):
  Top: (8,0)-(16,8), Bottom: (16,0)-(24,8)
  Right: (0,8)-(8,16), Front/Face: (8,8)-(16,16), Left: (16,8)-(24,16), Back: (24,8)-(32,16)
BODY (8x12x4):
  Top: (20,16)-(28,20), Bottom: (28,16)-(36,20)
  Right: (16,20)-(20,32), Front: (20,20)-(28,32), Left: (28,20)-(32,32), Back: (32,20)-(40,32)
RIGHT ARM (4x12x4):
  Top: (44,16)-(48,20), Right: (40,20)-(44,32), Front: (44,20)-(48,32), Left: (48,20)-(52,32), Back: (52,20)-(56,32)
LEFT ARM (4x12x4):
  Top: (36,48)-(40,52), Right: (32,52)-(36,64), Front: (36,52)-(40,64), Left: (40,52)-(44,64), Back: (44,52)-(48,64)
RIGHT LEG (4x12x4):
  Top: (4,16)-(8,20), Right: (0,20)-(4,32), Front: (4,20)-(8,32), Left: (8,20)-(12,32), Back: (12,20)-(16,32)
LEFT LEG (4x12x4):
  Top: (20,48)-(24,52), Right: (16,52)-(20,64), Front: (20,52)-(24,64), Left: (24,52)-(28,64), Back: (28,52)-(32,64)

HAT overlay: same as head but offset by (32,0) in U
JACKET overlay: body at (16,32)
RIGHT ARM overlay: (40,32)
LEFT ARM overlay: (48,48)  -- actually (52,48) for some versions
RIGHT LEG overlay: (0,32)
LEFT LEG overlay: (0,48)
"""

from PIL import Image
import os

OUTPUT = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "src", "main", "resources", "assets", "megamod", "textures", "entity", "citizen",
    "warehouse_worker.png"
)

# === UPS-inspired color palette ===
UPS_BROWN       = (100, 65, 35, 255)     # Main brown uniform
UPS_BROWN_DARK  = (70, 45, 25, 255)      # Darker shade/seams
UPS_BROWN_LIGHT = (120, 80, 45, 255)     # Lighter shade for highlights
UPS_GOLD        = (218, 175, 50, 255)    # Gold/yellow accent (like UPS shield)
UPS_GOLD_DARK   = (180, 140, 30, 255)    # Darker gold
BELT_COLOR      = (50, 35, 20, 255)      # Dark belt
BOOT_BROWN      = (45, 30, 18, 255)      # Very dark brown boots
SKIN            = (210, 170, 135, 255)   # Medium skin tone
SKIN_SHADE      = (190, 150, 115, 255)   # Skin shadow
HAIR_BROWN      = (60, 40, 25, 255)      # Short brown hair
EYE_COLOR       = (50, 30, 15, 255)      # Dark brown eyes
WHITE           = (255, 255, 255, 255)
CAP_BROWN       = (85, 55, 30, 255)      # Cap color (slightly different from shirt)
CAP_BRIM        = (65, 42, 22, 255)      # Cap brim


def fill(img, x1, y1, x2, y2, color):
    """Fill rectangle [x1,y1) to [x2,y2) inclusive of x1,y1."""
    for y in range(y1, y2):
        for x in range(x1, x2):
            if 0 <= x < 64 and 0 <= y < 64:
                img.putpixel((x, y), color)


def draw_head(img):
    """Draw the head — short brown hair, face, no hat (hat goes in overlay layer)."""
    # Top of head (8,0)-(16,8) — hair
    fill(img, 8, 0, 16, 8, HAIR_BROWN)
    # Bottom of head (16,0)-(24,8) — chin/neck skin
    fill(img, 16, 0, 24, 8, SKIN)

    # Right side (0,8)-(8,16)
    fill(img, 0, 8, 8, 16, SKIN)
    fill(img, 0, 8, 8, 10, HAIR_BROWN)  # hair on top portion
    # Ear
    img.putpixel((6, 11), SKIN_SHADE)
    img.putpixel((6, 12), SKIN_SHADE)

    # Front / face (8,8)-(16,16)
    fill(img, 8, 8, 16, 16, SKIN)
    # Hair fringe across top
    fill(img, 8, 8, 16, 10, HAIR_BROWN)
    # Eyes at y=11-12
    img.putpixel((10, 11), WHITE)
    img.putpixel((11, 11), WHITE)
    img.putpixel((13, 11), WHITE)
    img.putpixel((14, 11), WHITE)
    img.putpixel((10, 12), EYE_COLOR)
    img.putpixel((11, 12), EYE_COLOR)
    img.putpixel((13, 12), EYE_COLOR)
    img.putpixel((14, 12), EYE_COLOR)
    # Eyebrows
    brow = (45, 30, 18, 255)
    img.putpixel((10, 10), brow)
    img.putpixel((11, 10), brow)
    img.putpixel((13, 10), brow)
    img.putpixel((14, 10), brow)
    # Mouth
    mouth = (185, 135, 110, 255)
    img.putpixel((11, 14), mouth)
    img.putpixel((12, 14), mouth)
    img.putpixel((13, 14), mouth)

    # Left side (16,8)-(24,16)
    fill(img, 16, 8, 24, 16, SKIN)
    fill(img, 16, 8, 24, 10, HAIR_BROWN)
    img.putpixel((17, 11), SKIN_SHADE)
    img.putpixel((17, 12), SKIN_SHADE)

    # Back (24,8)-(32,16)
    fill(img, 24, 8, 32, 16, HAIR_BROWN)


def draw_hat_overlay(img):
    """Draw a UPS-style cap in the hat overlay layer (offset +32 in U from head)."""
    # Hat overlay uses same layout as head but at (32+x, y)
    # Top: (40,0)-(48,8)
    fill(img, 40, 0, 48, 8, CAP_BROWN)
    # Bottom: (48,0)-(56,8) — brim underside
    fill(img, 48, 0, 56, 8, CAP_BRIM)

    # Right side: (32,8)-(40,16) — cap side
    fill(img, 32, 8, 40, 12, CAP_BROWN)
    # Just the upper portion for the cap, rest transparent

    # Front: (40,8)-(48,16) — cap front with brim
    fill(img, 40, 8, 48, 10, CAP_BROWN)
    # Brim extending forward — slightly darker row
    fill(img, 40, 10, 48, 11, CAP_BRIM)
    # Gold accent stripe on cap front
    fill(img, 41, 9, 47, 10, UPS_GOLD_DARK)

    # Left side: (48,8)-(56,16)
    fill(img, 48, 8, 56, 12, CAP_BROWN)

    # Back: (56,8)-(64,16)
    fill(img, 56, 8, 64, 12, CAP_BROWN)
    # Adjustable strap detail on back
    fill(img, 58, 11, 62, 12, CAP_BRIM)


def draw_body(img):
    """Draw the torso — brown UPS-style collared button-up shirt."""
    # Top: (20,16)-(28,20)
    fill(img, 20, 16, 28, 20, UPS_BROWN)
    # Bottom: (28,16)-(36,20)
    fill(img, 28, 16, 36, 20, UPS_BROWN_DARK)

    # Right side: (16,20)-(20,32) — 4x12
    fill(img, 16, 20, 20, 32, UPS_BROWN_DARK)

    # Front: (20,20)-(28,32) — 8x12 — the main shirt front
    fill(img, 20, 20, 28, 32, UPS_BROWN)
    # Collar — lighter brown with gold trim
    fill(img, 22, 20, 26, 21, UPS_BROWN_LIGHT)  # collar
    img.putpixel((21, 20), UPS_GOLD)  # collar point left
    img.putpixel((26, 20), UPS_GOLD)  # collar point right
    # Button line down center
    for y in range(22, 30):
        img.putpixel((24, y), UPS_BROWN_DARK)
    # Buttons (gold)
    img.putpixel((24, 23), UPS_GOLD_DARK)
    img.putpixel((24, 25), UPS_GOLD_DARK)
    img.putpixel((24, 27), UPS_GOLD_DARK)
    # Breast pocket on right side
    fill(img, 21, 23, 23, 26, UPS_BROWN_DARK)
    img.putpixel((22, 24), UPS_GOLD)  # small gold logo/badge
    # Belt area
    fill(img, 20, 30, 28, 32, BELT_COLOR)
    img.putpixel((23, 31), UPS_GOLD_DARK)  # belt buckle
    img.putpixel((24, 31), UPS_GOLD_DARK)

    # Left side: (28,20)-(32,32) — 4x12
    fill(img, 28, 20, 32, 32, UPS_BROWN_DARK)

    # Back: (32,20)-(40,32) — 8x12
    fill(img, 32, 20, 40, 32, UPS_BROWN)
    # Collar on back
    fill(img, 34, 20, 38, 21, UPS_BROWN_LIGHT)
    # Belt on back
    fill(img, 32, 30, 40, 32, BELT_COLOR)


def draw_right_arm(img):
    """Right arm — short sleeve brown shirt, then skin below."""
    # Top: (44,16)-(48,20)
    fill(img, 44, 16, 48, 20, UPS_BROWN)

    # Right: (40,20)-(44,32)
    fill(img, 40, 20, 44, 26, UPS_BROWN_DARK)   # sleeve
    fill(img, 40, 26, 44, 32, SKIN)              # bare arm
    fill(img, 40, 26, 44, 27, UPS_BROWN_DARK)    # sleeve cuff

    # Front: (44,20)-(48,32)
    fill(img, 44, 20, 48, 26, UPS_BROWN)         # sleeve
    fill(img, 44, 26, 48, 32, SKIN)              # bare arm
    fill(img, 44, 26, 48, 27, UPS_BROWN_DARK)    # sleeve cuff

    # Left: (48,20)-(52,32)
    fill(img, 48, 20, 52, 26, UPS_BROWN_DARK)    # sleeve
    fill(img, 48, 26, 52, 32, SKIN)
    fill(img, 48, 26, 52, 27, UPS_BROWN_DARK)

    # Back: (52,20)-(56,32)
    fill(img, 52, 20, 56, 26, UPS_BROWN)         # sleeve
    fill(img, 52, 26, 56, 32, SKIN)
    fill(img, 52, 26, 56, 27, UPS_BROWN_DARK)


def draw_left_arm(img):
    """Left arm — same as right arm, mirrored. UV at second layer area."""
    # Top: (36,48)-(40,52)
    fill(img, 36, 48, 40, 52, UPS_BROWN)

    # Right: (32,52)-(36,64)
    fill(img, 32, 52, 36, 58, UPS_BROWN_DARK)
    fill(img, 32, 58, 36, 64, SKIN)
    fill(img, 32, 58, 36, 59, UPS_BROWN_DARK)

    # Front: (36,52)-(40,64)
    fill(img, 36, 52, 40, 58, UPS_BROWN)
    fill(img, 36, 58, 40, 64, SKIN)
    fill(img, 36, 58, 40, 59, UPS_BROWN_DARK)

    # Left: (40,52)-(44,64)
    fill(img, 40, 52, 44, 58, UPS_BROWN_DARK)
    fill(img, 40, 58, 44, 64, SKIN)
    fill(img, 40, 58, 44, 59, UPS_BROWN_DARK)

    # Back: (44,52)-(48,64)
    fill(img, 44, 52, 48, 58, UPS_BROWN)
    fill(img, 44, 58, 48, 64, SKIN)
    fill(img, 44, 58, 48, 59, UPS_BROWN_DARK)


def draw_right_leg(img):
    """Right leg — brown pants with dark brown boots at bottom."""
    # Top: (4,16)-(8,20)
    fill(img, 4, 16, 8, 20, UPS_BROWN)

    # Right: (0,20)-(4,32)
    fill(img, 0, 20, 4, 28, UPS_BROWN_DARK)     # pants
    fill(img, 0, 28, 4, 32, BOOT_BROWN)          # boots

    # Front: (4,20)-(8,32)
    fill(img, 4, 20, 8, 28, UPS_BROWN)           # pants
    fill(img, 4, 28, 8, 32, BOOT_BROWN)          # boots

    # Left: (8,20)-(12,32)
    fill(img, 8, 20, 12, 28, UPS_BROWN_DARK)
    fill(img, 8, 28, 12, 32, BOOT_BROWN)

    # Back: (12,20)-(16,32)
    fill(img, 12, 20, 16, 28, UPS_BROWN)
    fill(img, 12, 28, 16, 32, BOOT_BROWN)


def draw_left_leg(img):
    """Left leg — brown pants with dark brown boots."""
    # Top: (20,48)-(24,52)
    fill(img, 20, 48, 24, 52, UPS_BROWN)

    # Right: (16,52)-(20,64)
    fill(img, 16, 52, 20, 60, UPS_BROWN_DARK)
    fill(img, 16, 60, 20, 64, BOOT_BROWN)

    # Front: (20,52)-(24,64)
    fill(img, 20, 52, 24, 60, UPS_BROWN)
    fill(img, 20, 60, 24, 64, BOOT_BROWN)

    # Left: (24,52)-(28,64)
    fill(img, 24, 52, 28, 60, UPS_BROWN_DARK)
    fill(img, 24, 60, 28, 64, BOOT_BROWN)

    # Back: (28,52)-(32,64)
    fill(img, 28, 52, 32, 60, UPS_BROWN)
    fill(img, 28, 60, 32, 64, BOOT_BROWN)


def main():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))

    draw_head(img)
    draw_hat_overlay(img)
    draw_body(img)
    draw_right_arm(img)
    draw_left_arm(img)
    draw_right_leg(img)
    draw_left_leg(img)

    os.makedirs(os.path.dirname(OUTPUT), exist_ok=True)
    img.save(OUTPUT)
    print(f"Generated: {OUTPUT}")


if __name__ == "__main__":
    main()
