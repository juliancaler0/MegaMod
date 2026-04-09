"""
Generate 16x16 RGBA textures for colony blueprint blocks.
Uses PIL (Pillow) for image generation.
Outputs to src/main/resources/assets/megamod/textures/block/
"""

from PIL import Image
import os

OUT_DIR = os.path.join(os.path.dirname(__file__),
    "src", "main", "resources", "assets", "megamod", "textures", "block")

os.makedirs(OUT_DIR, exist_ok=True)


def save(img, name):
    path = os.path.join(OUT_DIR, name + ".png")
    img.save(path)
    print(f"  Saved {path}")


def colony_hut():
    """Dark wood frame with lighter center - building marker look."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()

    dark_frame = (74, 42, 26, 255)       # dark wood
    mid_wood   = (107, 66, 38, 255)      # mid wood
    light_wood = (139, 90, 43, 255)      # light center
    accent     = (200, 168, 76, 255)     # gold accent

    for y in range(16):
        for x in range(16):
            # Outer frame (2px border)
            if x < 2 or x > 13 or y < 2 or y > 13:
                px[x, y] = dark_frame
            # Inner frame (1px)
            elif x < 3 or x > 12 or y < 3 or y > 12:
                px[x, y] = mid_wood
            # Center fill with subtle grain
            else:
                grain = ((x * 7 + y * 3) % 5)
                if grain == 0:
                    px[x, y] = mid_wood
                else:
                    px[x, y] = light_wood

    # Corner accents (gold dots)
    for cx, cy in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        px[cx, cy] = accent

    # Center cross marker
    for i in range(6, 10):
        px[i, 8] = accent
        px[8, i] = accent

    save(img, "colony_hut")


def colony_rack():
    """Horizontal shelf lines on wood background."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()

    wood_bg   = (139, 90, 43, 255)    # light wood
    shelf     = (90, 58, 30, 255)     # darker shelf line
    dark_edge = (74, 42, 26, 255)     # frame edge
    side_rail = (60, 35, 18, 255)     # vertical side

    for y in range(16):
        for x in range(16):
            # Left and right edges (vertical rails)
            if x == 0 or x == 15:
                px[x, y] = side_rail
            # Horizontal shelf lines at y=3, y=7, y=11, y=15
            elif y == 3 or y == 7 or y == 11 or y == 15:
                px[x, y] = shelf
            # Bottom edge of shelves (shadow)
            elif y == 4 or y == 8 or y == 12:
                px[x, y] = dark_edge
            # Wood background with grain
            else:
                grain = ((x * 5 + y * 2) % 4)
                if grain == 0:
                    px[x, y] = (130, 82, 38, 255)
                else:
                    px[x, y] = wood_bg

    # Top edge
    for x in range(16):
        px[x, 0] = side_rail

    save(img, "colony_rack")


def colony_barrel():
    """Circular top/bottom with vertical planks - barrel look."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()

    plank_1 = (139, 90, 43, 255)
    plank_2 = (120, 76, 35, 255)
    band    = (80, 80, 90, 255)      # iron band
    gap     = (60, 35, 18, 255)      # gap between planks

    for y in range(16):
        for x in range(16):
            # Iron bands at rows 2, 13
            if y == 2 or y == 13:
                px[x, y] = band
            # Plank gaps at columns 3, 7, 11
            elif x == 3 or x == 7 or x == 11:
                px[x, y] = gap
            # Alternating plank colors
            else:
                section = x // 4
                if section % 2 == 0:
                    grain = ((x * 3 + y * 7) % 5)
                    if grain == 0:
                        px[x, y] = plank_2
                    else:
                        px[x, y] = plank_1
                else:
                    grain = ((x * 3 + y * 7) % 5)
                    if grain == 0:
                        px[x, y] = plank_1
                    else:
                        px[x, y] = plank_2

    # Top/bottom beveled edges
    for x in range(16):
        px[x, 0] = (100, 65, 30, 255)
        px[x, 15] = (100, 65, 30, 255)

    save(img, "colony_barrel")


def colony_waypoint():
    """Blue diamond/gem on gray stone background."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()

    stone_light = (140, 140, 140, 255)
    stone_dark  = (110, 110, 110, 255)
    blue_bright = (60, 120, 220, 255)
    blue_mid    = (40, 90, 180, 255)
    blue_dark   = (30, 60, 130, 255)
    white_shine = (180, 200, 255, 255)

    # Stone background with noise
    for y in range(16):
        for x in range(16):
            noise = ((x * 7 + y * 13) % 5)
            if noise < 2:
                px[x, y] = stone_dark
            else:
                px[x, y] = stone_light

    # Diamond shape centered at (8, 8), radius 5
    cx, cy = 8, 8
    for y in range(16):
        for x in range(16):
            # Manhattan distance for diamond shape
            dist = abs(x - cx) + abs(y - cy)
            if dist <= 4:
                if dist <= 2:
                    px[x, y] = blue_bright
                elif dist <= 3:
                    px[x, y] = blue_mid
                else:
                    px[x, y] = blue_dark

    # Shine highlight
    px[7, 6] = white_shine
    px[8, 5] = white_shine

    save(img, "colony_waypoint")


def colony_scarecrow():
    """Straw/hay colored with cross pattern."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()

    hay_light  = (218, 185, 110, 255)
    hay_mid    = (190, 155, 80, 255)
    hay_dark   = (160, 130, 60, 255)
    wood_post  = (90, 58, 30, 255)
    wood_dark  = (74, 42, 26, 255)
    hat_brown  = (120, 76, 35, 255)
    hat_dark   = (90, 55, 25, 255)

    # Fill with hay texture
    for y in range(16):
        for x in range(16):
            noise = ((x * 11 + y * 7) % 7)
            if noise < 2:
                px[x, y] = hay_dark
            elif noise < 4:
                px[x, y] = hay_mid
            else:
                px[x, y] = hay_light

    # Vertical post (center column, x=7-8)
    for y in range(16):
        px[7, y] = wood_post
        px[8, y] = wood_dark

    # Horizontal crossbar (y=5-6, across full width)
    for x in range(16):
        px[x, 5] = wood_post
        px[x, 6] = wood_dark

    # Hat shape (top 3 rows, wider)
    for x in range(4, 12):
        px[x, 0] = hat_brown
        px[x, 1] = hat_dark
    for x in range(5, 11):
        px[x, 2] = hat_brown

    save(img, "colony_scarecrow")


def colony_construction_tape():
    """Yellow and black diagonal stripes."""
    img = Image.new("RGBA", (16, 16))
    px = img.load()

    yellow = (255, 215, 0, 255)
    black  = (30, 30, 30, 255)

    for y in range(16):
        for x in range(16):
            # Diagonal stripe pattern: 4px wide stripes
            stripe = ((x + y) % 8)
            if stripe < 4:
                px[x, y] = yellow
            else:
                px[x, y] = black

    # Subtle highlight on yellow pixels at top row
    for x in range(16):
        r, g, b, a = px[x, 0]
        if (r, g, b) == (255, 215, 0):
            px[x, 0] = (255, 230, 80, 255)

    # Subtle shadow on bottom row
    for x in range(16):
        r, g, b, a = px[x, 15]
        if (r, g, b) == (255, 215, 0):
            px[x, 15] = (220, 185, 0, 255)
        elif (r, g, b) == (30, 30, 30):
            px[x, 15] = (20, 20, 20, 255)

    save(img, "colony_construction_tape")


if __name__ == "__main__":
    print("Generating colony blueprint block textures...")
    colony_hut()
    colony_rack()
    colony_barrel()
    colony_waypoint()
    colony_scarecrow()
    colony_construction_tape()
    print("Done! Generated 6 textures.")
