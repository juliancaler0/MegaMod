"""
Generate 16x16 pixel art textures for RPG accessory items.
Medieval fantasy style with shading (highlight top-left, shadow bottom-right).
"""
from PIL import Image
import os

output_dir = r"C:\Users\JulianCalero\OneDrive - Associated Rebar\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\item"

def create_texture(name, draw_func):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    pixels = img.load()
    draw_func(pixels)
    img.save(os.path.join(output_dir, name))
    print(f"Created {name}")

# Helper to shade a color (lighten or darken)
def lighten(color, amount=30):
    return tuple(min(255, c + amount) for c in color[:3]) + (color[3],)

def darken(color, amount=30):
    return tuple(max(0, c - amount) for c in color[:3]) + (color[3],)

# ============================================================
# 1. LEATHER BELT
# ============================================================
def draw_leather_belt(p):
    brown = (139, 90, 43, 255)
    brown_hi = lighten(brown, 35)
    brown_sh = darken(brown, 35)
    gold = (218, 165, 32, 255)
    gold_hi = lighten(gold, 40)
    gold_sh = darken(gold, 40)

    # Belt strap across middle (y=6 to y=9)
    for x in range(1, 15):
        p[x, 6] = brown_hi  # top highlight
        p[x, 7] = brown
        p[x, 8] = brown
        p[x, 9] = brown_sh  # bottom shadow

    # Stitch line detail
    for x in range(1, 15, 2):
        p[x, 7] = darken(brown, 15)

    # Belt holes (small dark dots)
    for x in [9, 11, 13]:
        p[x, 8] = darken(brown, 50)

    # Buckle (left side, square frame)
    for x in range(2, 6):
        for y in range(5, 11):
            p[x, y] = gold
    # Buckle frame outline
    for x in range(2, 6):
        p[x, 5] = gold_hi
        p[x, 10] = gold_sh
    for y in range(5, 11):
        p[2, y] = gold_hi
        p[5, y] = gold_sh
    # Buckle inner
    for x in range(3, 5):
        for y in range(6, 10):
            p[x, y] = darken(gold, 50)
    # Prong
    p[4, 7] = gold_hi
    p[5, 7] = gold_hi
    p[6, 7] = gold
    p[7, 7] = gold_sh

    # Belt tip on right
    p[14, 7] = brown_sh
    p[14, 8] = brown_sh

create_texture("leather_belt.png", draw_leather_belt)

# ============================================================
# 2. DROWNED BELT
# ============================================================
def draw_drowned_belt(p):
    teal = (42, 107, 90, 255)
    teal_hi = lighten(teal, 30)
    teal_sh = darken(teal, 30)
    rust = (139, 105, 20, 255)
    rust_sh = darken(rust, 30)
    barnacle = (160, 160, 150, 255)
    barnacle_dk = (120, 120, 110, 255)

    # Belt strap across middle
    for x in range(1, 15):
        p[x, 6] = teal_hi
        p[x, 7] = teal
        p[x, 8] = teal
        p[x, 9] = teal_sh

    # Waterlogged texture - darker patches
    for x in [3, 5, 8, 11, 13]:
        p[x, 7] = darken(teal, 15)
        p[x, 8] = darken(teal, 20)

    # Seaweed strands hanging
    p[7, 10] = (30, 80, 50, 200)
    p[7, 11] = (25, 70, 45, 180)
    p[10, 10] = (30, 80, 50, 180)
    p[10, 11] = (25, 70, 45, 150)
    p[10, 12] = (20, 60, 40, 120)

    # Barnacles
    p[6, 7] = barnacle
    p[6, 6] = barnacle_dk
    p[9, 8] = barnacle
    p[12, 7] = barnacle
    p[12, 6] = barnacle_dk

    # Rusty buckle
    for x in range(2, 5):
        for y in range(5, 11):
            p[x, y] = rust
    for x in range(2, 5):
        p[x, 5] = lighten(rust, 20)
        p[x, 10] = rust_sh
    # Buckle rust patches
    p[3, 7] = darken(rust, 40)
    p[2, 9] = darken(rust, 40)
    p[3, 6] = lighten(rust, 30)

    # Water drip below belt
    p[4, 11] = (60, 140, 160, 150)
    p[4, 12] = (80, 170, 190, 100)

create_texture("drowned_belt.png", draw_drowned_belt)

# ============================================================
# 3. HUNTER BELT
# ============================================================
def draw_hunter_belt(p):
    green = (58, 90, 42, 255)
    green_hi = lighten(green, 30)
    green_sh = darken(green, 30)
    pouch = (107, 66, 38, 255)
    pouch_hi = lighten(pouch, 30)
    pouch_sh = darken(pouch, 30)
    silver = (192, 192, 192, 255)
    silver_hi = (220, 220, 225, 255)
    silver_sh = (140, 140, 145, 255)

    # Belt strap
    for x in range(1, 15):
        p[x, 6] = green_hi
        p[x, 7] = green
        p[x, 8] = green_sh

    # Stitch detail
    for x in range(1, 15, 3):
        p[x, 7] = lighten(green, 15)

    # Silver buckle (left)
    for x in range(2, 5):
        for y in range(5, 10):
            p[x, y] = silver
    p[2, 5] = silver_hi
    p[3, 5] = silver_hi
    p[4, 9] = silver_sh
    p[3, 9] = silver_sh
    # Inner buckle
    p[3, 7] = silver_sh

    # Pouch 1 (hanging from belt)
    for x in range(6, 9):
        for y in range(8, 13):
            p[x, y] = pouch
    p[6, 8] = pouch_hi
    p[7, 8] = pouch_hi
    p[8, 12] = pouch_sh
    p[8, 11] = pouch_sh
    # Pouch flap
    for x in range(6, 9):
        p[x, 9] = pouch_hi
    # Pouch button
    p[7, 10] = silver

    # Pouch 2 (smaller)
    for x in range(10, 13):
        for y in range(8, 12):
            p[x, y] = darken(pouch, 10)
    p[10, 8] = pouch_hi
    p[12, 11] = pouch_sh
    # Pouch flap
    for x in range(10, 13):
        p[x, 9] = pouch
    p[11, 10] = silver

    # Small knife handle on belt
    p[14, 6] = (80, 60, 40, 255)
    p[14, 7] = (80, 60, 40, 255)
    p[14, 8] = silver
    p[14, 9] = silver_sh

create_texture("hunter_belt.png", draw_hunter_belt)

# ============================================================
# 4. MAGMA WALKER
# ============================================================
def draw_magma_walker(p):
    black = (26, 26, 26, 255)
    black_hi = (50, 50, 50, 255)
    black_sh = (10, 10, 10, 255)
    orange1 = (255, 102, 0, 255)
    orange2 = (255, 68, 0, 255)
    ember = (255, 200, 50, 255)
    glow = (200, 80, 0, 180)

    # Boot shape (side view, facing right)
    # Shaft (top part, y=2 to y=8)
    for x in range(5, 10):
        for y in range(2, 9):
            p[x, y] = black
    # Foot (bottom part, extending right)
    for x in range(4, 13):
        for y in range(9, 12):
            p[x, y] = black
    # Sole
    for x in range(4, 13):
        p[x, 12] = black_sh
    # Toe (front)
    for x in range(11, 14):
        for y in range(8, 12):
            p[x, y] = black
    p[13, 9] = black
    p[13, 10] = black

    # Top highlight
    for x in range(5, 10):
        p[x, 2] = black_hi
    p[5, 3] = black_hi
    p[5, 4] = black_hi

    # Glowing orange cracks
    p[6, 4] = orange1
    p[7, 5] = orange2
    p[7, 6] = orange1
    p[8, 7] = orange2
    p[6, 7] = orange1

    # Cracks on foot area
    p[5, 10] = orange1
    p[7, 10] = orange2
    p[9, 10] = orange1
    p[10, 9] = orange2
    p[11, 10] = orange1

    # Ember glow pixels
    p[6, 3] = ember
    p[8, 6] = ember
    p[9, 11] = ember

    # Glow around cracks
    p[6, 5] = glow
    p[8, 5] = glow
    p[6, 9] = glow
    p[8, 9] = glow

    # Sole glow
    for x in range(5, 12):
        p[x, 13] = (255, 80, 0, 100)

create_texture("magma_walker.png", draw_magma_walker)

# ============================================================
# 5. AQUA WALKER
# ============================================================
def draw_aqua_walker(p):
    blue = (68, 136, 204, 255)
    blue_hi = lighten(blue, 40)
    blue_sh = darken(blue, 40)
    crystal = (100, 180, 230, 255)
    white = (240, 245, 255, 255)
    drop = (80, 160, 220, 200)

    # Boot shaft
    for x in range(5, 10):
        for y in range(2, 9):
            p[x, y] = blue
    # Foot
    for x in range(4, 13):
        for y in range(9, 12):
            p[x, y] = blue
    # Toe extension
    for x in range(11, 14):
        for y in range(8, 12):
            p[x, y] = blue
    p[13, 9] = blue
    p[13, 10] = blue
    # Sole
    for x in range(4, 13):
        p[x, 12] = blue_sh

    # Crystal highlights
    p[5, 2] = blue_hi
    p[6, 2] = blue_hi
    p[5, 3] = blue_hi
    p[6, 3] = crystal
    p[7, 4] = crystal
    p[8, 3] = crystal

    # Crystal facet lines
    p[7, 5] = blue_hi
    p[6, 6] = blue_hi
    p[8, 7] = blue_hi
    p[9, 5] = blue_sh

    # Shading on right/bottom
    p[9, 6] = blue_sh
    p[9, 7] = blue_sh
    p[9, 8] = blue_sh
    for x in range(10, 14):
        p[x, 10] = blue_sh
        p[x, 11] = blue_sh

    # White highlight spot
    p[6, 4] = white
    p[5, 9] = white

    # Water droplet below
    p[8, 13] = drop
    p[8, 14] = (80, 160, 220, 150)
    p[7, 14] = (80, 160, 220, 100)
    p[9, 14] = (80, 160, 220, 100)

    # Frost/sparkle pixels
    p[12, 9] = (200, 230, 255, 180)
    p[5, 5] = (200, 230, 255, 150)

create_texture("aqua_walker.png", draw_aqua_walker)

# ============================================================
# 6. ICE SKATES
# ============================================================
def draw_ice_skates(p):
    white = (221, 221, 238, 255)
    white_hi = (240, 240, 250, 255)
    white_sh = (180, 180, 200, 255)
    grey = (150, 150, 160, 255)
    silver = (192, 192, 192, 255)
    silver_hi = (220, 220, 230, 255)
    ice_blue = (140, 200, 240, 255)

    # Boot shaft
    for x in range(5, 10):
        for y in range(2, 9):
            p[x, y] = white
    # Foot
    for x in range(4, 13):
        for y in range(9, 11):
            p[x, y] = white
    # Toe
    for x in range(11, 14):
        for y in range(8, 11):
            p[x, y] = white
    p[13, 9] = white

    # Boot top highlight
    for x in range(5, 10):
        p[x, 2] = white_hi
    p[5, 3] = white_hi
    p[5, 4] = white_hi

    # Boot shadow (right side, bottom)
    p[9, 5] = white_sh
    p[9, 6] = white_sh
    p[9, 7] = white_sh
    p[9, 8] = white_sh
    for x in range(10, 14):
        p[x, 10] = white_sh

    # Grey sole
    for x in range(4, 14):
        p[x, 11] = grey

    # Ice skate blade
    for x in range(3, 14):
        p[x, 12] = silver
    p[3, 12] = silver_hi
    p[14, 12] = silver  # blade extends
    # Blade edge highlight
    for x in range(3, 14):
        p[x, 13] = silver_hi
    # Blade tip (front curve up)
    p[14, 11] = silver
    p[14, 12] = silver_hi

    # Ice blue accents
    p[6, 4] = ice_blue
    p[7, 3] = ice_blue
    p[5, 7] = ice_blue
    p[8, 5] = ice_blue

    # Lace detail
    p[7, 5] = white_sh
    p[7, 7] = white_sh
    p[8, 6] = white_sh

    # Frost sparkle
    p[12, 13] = (200, 240, 255, 200)
    p[5, 13] = (200, 240, 255, 180)

create_texture("ice_skates.png", draw_ice_skates)

# ============================================================
# 7. ICE BREAKER
# ============================================================
def draw_ice_breaker(p):
    grey = (85, 85, 85, 255)
    grey_hi = lighten(grey, 35)
    grey_sh = darken(grey, 35)
    metal = (136, 136, 136, 255)
    metal_hi = lighten(metal, 40)
    spike = (170, 170, 180, 255)

    # Heavy boot shaft (wider)
    for x in range(4, 11):
        for y in range(2, 9):
            p[x, y] = grey
    # Foot (wider sole)
    for x in range(3, 14):
        for y in range(9, 12):
            p[x, y] = grey
    # Toe
    for x in range(12, 15):
        for y in range(8, 12):
            p[x, y] = grey

    # Thick sole
    for x in range(3, 15):
        p[x, 12] = grey_sh
        p[x, 13] = darken(grey, 50)

    # Top highlight
    for x in range(4, 11):
        p[x, 2] = grey_hi
    p[4, 3] = grey_hi
    p[4, 4] = grey_hi

    # Shadow on right
    p[10, 5] = grey_sh
    p[10, 6] = grey_sh
    p[10, 7] = grey_sh
    p[10, 8] = grey_sh
    for x in range(12, 15):
        p[x, 11] = grey_sh

    # Metal studs on toe
    p[13, 9] = metal_hi
    p[14, 9] = metal
    p[13, 10] = metal
    p[12, 9] = metal_hi

    # Spikes on toe (front)
    p[15, 9] = spike
    p[15, 10] = spike
    p[14, 8] = spike

    # Metal reinforcement strips
    for y in range(4, 8):
        p[6, y] = metal
        p[8, y] = metal

    # Ankle strap
    for x in range(4, 11):
        p[x, 4] = darken(grey, 15)

    # Rugged tread on sole
    p[4, 13] = grey
    p[6, 13] = grey
    p[8, 13] = grey
    p[10, 13] = grey
    p[12, 13] = grey

create_texture("ice_breaker.png", draw_ice_breaker)

# ============================================================
# 8. ROLLER SKATES
# ============================================================
def draw_roller_skates(p):
    brown = (139, 90, 43, 255)
    brown_hi = lighten(brown, 30)
    brown_sh = darken(brown, 30)
    wheel = (119, 119, 119, 255)
    wheel_hi = (160, 160, 165, 255)
    wheel_sh = (80, 80, 85, 255)
    axle = (100, 100, 100, 255)
    lace = (200, 190, 170, 255)

    # Boot shaft
    for x in range(5, 10):
        for y in range(2, 9):
            p[x, y] = brown
    # Foot
    for x in range(4, 13):
        for y in range(9, 11):
            p[x, y] = brown
    # Toe
    for x in range(11, 14):
        for y in range(8, 11):
            p[x, y] = brown
    p[13, 9] = brown

    # Top highlight
    for x in range(5, 10):
        p[x, 2] = brown_hi
    p[5, 3] = brown_hi
    p[5, 4] = brown_hi

    # Shadow
    p[9, 5] = brown_sh
    p[9, 6] = brown_sh
    p[9, 7] = brown_sh
    for x in range(11, 14):
        p[x, 10] = brown_sh

    # Sole plate
    for x in range(4, 14):
        p[x, 11] = (80, 80, 85, 255)

    # Axle bar
    for x in range(5, 13):
        p[x, 12] = axle

    # Wheels (3 circles beneath)
    # Wheel 1
    p[5, 13] = wheel_hi
    p[6, 13] = wheel
    p[5, 14] = wheel
    p[6, 14] = wheel_sh
    # Wheel center
    p[5, 13] = wheel_hi

    # Wheel 2
    p[8, 13] = wheel_hi
    p[9, 13] = wheel
    p[8, 14] = wheel
    p[9, 14] = wheel_sh

    # Wheel 3
    p[11, 13] = wheel_hi
    p[12, 13] = wheel
    p[11, 14] = wheel
    p[12, 14] = wheel_sh

    # Lace details
    p[7, 4] = lace
    p[7, 6] = lace
    p[7, 8] = lace

    # Tongue
    p[6, 3] = brown_hi
    p[7, 3] = brown_hi

create_texture("roller_skates.png", draw_roller_skates)

# ============================================================
# 9. AMPHIBIAN BOOT
# ============================================================
def draw_amphibian_boot(p):
    green = (90, 122, 58, 255)
    green_hi = lighten(green, 30)
    green_sh = darken(green, 30)
    web = (70, 120, 50, 180)
    dark_green = darken(green, 45)
    spot = (60, 100, 40, 255)

    # Boot shaft
    for x in range(5, 10):
        for y in range(2, 9):
            p[x, y] = green
    # Foot
    for x in range(4, 13):
        for y in range(9, 12):
            p[x, y] = green
    # Toe area (wider, flatter - frog foot)
    for x in range(11, 15):
        for y in range(9, 12):
            p[x, y] = green
    p[14, 10] = green

    # Top highlight
    for x in range(5, 10):
        p[x, 2] = green_hi
    p[5, 3] = green_hi
    p[5, 4] = green_hi

    # Shadow
    p[9, 5] = green_sh
    p[9, 6] = green_sh
    p[9, 7] = green_sh
    for x in range(11, 15):
        p[x, 11] = green_sh

    # Sole
    for x in range(4, 15):
        p[x, 12] = dark_green

    # Frog skin spots/bumps
    p[6, 4] = spot
    p[8, 6] = spot
    p[7, 8] = spot
    p[5, 10] = spot
    p[10, 10] = spot

    # Webbed toe lines (translucent webbing between toe tips)
    # Toe tips
    p[12, 12] = green_hi
    p[13, 12] = green_hi
    p[14, 12] = green_hi
    p[11, 13] = green
    p[13, 13] = green
    p[15, 11] = green

    # Web between toes
    p[12, 13] = web
    p[14, 12] = web
    p[14, 13] = (70, 120, 50, 140)

    # Moist/glossy highlight
    p[6, 3] = (130, 170, 100, 200)
    p[5, 9] = (130, 170, 100, 180)

    # Slight slime drip
    p[13, 12] = (100, 160, 70, 160)
    p[13, 13] = (100, 160, 70, 120)

create_texture("amphibian_boot.png", draw_amphibian_boot)

# ============================================================
# 10. ENDER HAND
# ============================================================
def draw_ender_hand(p):
    purple = (58, 26, 90, 255)
    purple_hi = lighten(purple, 35)
    purple_sh = darken(purple, 25)
    particle = (170, 68, 255, 255)
    particle2 = (140, 50, 220, 200)
    black = (15, 10, 25, 255)
    glow = (120, 40, 180, 150)

    # Glove body (palm area)
    for x in range(4, 12):
        for y in range(5, 13):
            p[x, y] = purple
    # Wrist
    for x in range(5, 11):
        for y in range(13, 15):
            p[x, y] = purple_sh

    # Fingers (4 fingers at top)
    # Index
    for y in range(2, 6):
        p[5, y] = purple
    p[5, 1] = black  # fingertip
    # Middle
    for y in range(1, 6):
        p[7, y] = purple
    p[7, 0] = black
    # Ring
    for y in range(2, 6):
        p[9, y] = purple
    p[9, 1] = black
    # Pinky
    for y in range(3, 6):
        p[11, y] = purple
    p[11, 2] = black

    # Thumb
    p[3, 8] = purple
    p[2, 9] = purple
    p[2, 10] = black

    # Highlight on left edge
    p[4, 5] = purple_hi
    p[4, 6] = purple_hi
    p[4, 7] = purple_hi
    p[4, 8] = purple_hi

    # Shadow on right
    p[11, 7] = purple_sh
    p[11, 8] = purple_sh
    p[11, 9] = purple_sh
    p[11, 10] = purple_sh
    p[11, 11] = purple_sh
    p[11, 12] = purple_sh

    # Ender particles (bright purple dots floating around)
    p[1, 3] = particle
    p[13, 2] = particle
    p[2, 6] = particle2
    p[13, 7] = particle
    p[0, 10] = particle2
    p[14, 11] = particle
    p[12, 4] = particle2
    p[3, 1] = particle

    # Glow around hand
    p[3, 5] = glow
    p[3, 12] = glow
    p[12, 6] = glow
    p[12, 12] = glow

    # Dark veins on glove
    p[6, 8] = purple_sh
    p[8, 7] = purple_sh
    p[7, 10] = purple_sh
    p[9, 9] = purple_sh

create_texture("ender_hand.png", draw_ender_hand)

# ============================================================
# 11. RAGE GLOVE
# ============================================================
def draw_rage_glove(p):
    dark_red = (139, 26, 26, 255)
    bright_red = (204, 34, 34, 255)
    red_hi = lighten(bright_red, 40)
    red_sh = darken(dark_red, 30)
    spike_gold = (218, 165, 32, 255)
    spike_hi = lighten(spike_gold, 30)
    black = (30, 10, 10, 255)

    # Gauntlet body
    for x in range(4, 12):
        for y in range(5, 13):
            p[x, y] = dark_red
    # Wrist (armored)
    for x in range(5, 11):
        for y in range(13, 15):
            p[x, y] = red_sh

    # Fingers
    for y in range(2, 6):
        p[5, y] = dark_red
    p[5, 1] = dark_red
    for y in range(1, 6):
        p[7, y] = dark_red
    p[7, 0] = dark_red
    for y in range(2, 6):
        p[9, y] = dark_red
    p[9, 1] = dark_red
    for y in range(3, 6):
        p[11, y] = dark_red
    p[11, 2] = dark_red

    # Thumb
    p[3, 8] = dark_red
    p[2, 9] = dark_red

    # Bright red knuckle highlights
    p[5, 5] = bright_red
    p[7, 5] = bright_red
    p[9, 5] = bright_red
    p[11, 5] = bright_red
    p[5, 4] = bright_red
    p[7, 4] = bright_red
    p[9, 4] = bright_red

    # Highlight on top-left
    p[4, 5] = red_hi
    p[4, 6] = lighten(dark_red, 25)
    p[4, 7] = lighten(dark_red, 20)

    # Shadow on bottom-right
    p[11, 9] = red_sh
    p[11, 10] = red_sh
    p[11, 11] = red_sh
    p[11, 12] = red_sh

    # Gold spike on top of hand
    p[7, 6] = spike_gold
    p[8, 6] = spike_gold
    p[7, 5] = spike_gold
    p[8, 5] = spike_hi
    p[8, 4] = spike_hi  # spike tip pointing up

    # Additional small spikes on knuckles
    p[5, 3] = spike_gold
    p[9, 3] = spike_gold

    # Metal plate detail
    for x in range(5, 11):
        p[x, 8] = darken(dark_red, 15)
    for x in range(5, 11):
        p[x, 11] = darken(dark_red, 20)

    # Wrist spikes
    p[5, 14] = spike_gold
    p[10, 14] = spike_gold

create_texture("rage_glove.png", draw_rage_glove)

# ============================================================
# 12. WOOL MITTEN
# ============================================================
def draw_wool_mitten(p):
    cream = (240, 224, 192, 255)
    cream_hi = lighten(cream, 15)
    cream_sh = darken(cream, 25)
    knit = darken(cream, 15)
    red = (180, 60, 60, 255)
    cuff = (200, 185, 160, 255)
    cuff_sh = darken(cuff, 25)

    # Mitten body (rounded, no individual fingers)
    for x in range(4, 12):
        for y in range(3, 12):
            p[x, y] = cream
    # Round top
    for x in range(5, 11):
        p[x, 2] = cream
    for x in range(6, 10):
        p[x, 1] = cream
    # Round bottom
    for x in range(5, 11):
        p[x, 12] = cream

    # Thumb (sticking out left)
    p[3, 7] = cream
    p[2, 7] = cream
    p[2, 8] = cream
    p[3, 8] = cream
    p[3, 9] = cream
    p[2, 9] = cream_sh

    # Highlight on top-left
    p[4, 3] = cream_hi
    p[4, 4] = cream_hi
    p[5, 2] = cream_hi
    p[6, 1] = cream_hi
    p[5, 3] = cream_hi

    # Shadow on bottom-right
    p[11, 8] = cream_sh
    p[11, 9] = cream_sh
    p[11, 10] = cream_sh
    p[11, 11] = cream_sh
    p[10, 12] = cream_sh

    # Knit texture (horizontal lines)
    for x in range(4, 12):
        if x % 2 == 0:
            p[x, 4] = knit
            p[x, 6] = knit
            p[x, 8] = knit
            p[x, 10] = knit
        else:
            p[x, 5] = knit
            p[x, 7] = knit
            p[x, 9] = knit

    # Ribbed cuff at bottom
    for x in range(5, 11):
        for y in range(12, 15):
            p[x, y] = cuff
    for x in range(5, 11):
        p[x, 14] = cuff_sh
    # Cuff ribs
    p[6, 13] = cuff_sh
    p[8, 13] = cuff_sh
    p[10, 13] = cuff_sh

    # Tiny red heart on mitten
    p[7, 5] = red
    p[9, 5] = red
    p[7, 6] = red
    p[8, 6] = red
    p[9, 6] = red
    p[8, 7] = red

create_texture("wool_mitten.png", draw_wool_mitten)

# ============================================================
# 13. REFLECTION NECKLACE
# ============================================================
def draw_reflection_necklace(p):
    chain = (153, 153, 153, 255)
    chain_hi = (180, 180, 185, 255)
    mirror = (204, 221, 238, 255)
    mirror_hi = (240, 245, 255, 255)
    mirror_sh = (150, 170, 190, 255)
    frame = (170, 170, 175, 255)

    # Chain in V shape
    # Left chain going down-right
    p[3, 1] = chain_hi
    p[4, 2] = chain
    p[5, 3] = chain
    p[6, 4] = chain
    p[7, 5] = chain

    # Right chain going down-left
    p[12, 1] = chain_hi
    p[11, 2] = chain
    p[10, 3] = chain
    p[9, 4] = chain
    p[8, 5] = chain

    # Chain link detail
    p[3, 1] = chain_hi
    p[12, 1] = chain_hi
    p[5, 3] = chain_hi
    p[10, 3] = chain_hi

    # Mirror pendant (oval/round)
    for x in range(6, 10):
        for y in range(6, 12):
            p[x, y] = mirror
    # Top/bottom rounding
    p[7, 5] = frame
    p[8, 5] = frame
    p[7, 12] = frame
    p[8, 12] = frame

    # Frame around mirror
    for y in range(6, 12):
        p[5, y] = frame
        p[10, y] = frame
    for x in range(6, 10):
        p[x, 6] = frame
        p[x, 11] = frame

    # Mirror surface - gradient for reflection effect
    p[7, 7] = mirror_hi
    p[8, 7] = mirror_hi
    p[7, 8] = mirror_hi
    p[6, 7] = mirror
    p[8, 8] = mirror
    p[9, 9] = mirror_sh
    p[9, 10] = mirror_sh
    p[8, 10] = mirror_sh

    # White highlight dot (reflection spot)
    p[7, 7] = (255, 255, 255, 255)

    # Slight gleam
    p[6, 8] = (220, 235, 245, 255)

    # Bail (connector to chain)
    p[7, 5] = chain
    p[8, 5] = chain

create_texture("reflection_necklace.png", draw_reflection_necklace)

# ============================================================
# 14. JELLYFISH NECKLACE
# ============================================================
def draw_jellyfish_necklace(p):
    gold = (218, 165, 32, 255)
    gold_hi = lighten(gold, 30)
    cyan = (0, 204, 204, 255)
    bright_cyan = (68, 255, 255, 255)
    glow = (0, 180, 180, 150)
    tentacle = (0, 160, 170, 200)
    tentacle2 = (0, 140, 150, 150)

    # Gold chain in V shape
    p[3, 1] = gold_hi
    p[4, 2] = gold
    p[5, 3] = gold
    p[6, 4] = gold
    p[7, 5] = gold

    p[12, 1] = gold_hi
    p[11, 2] = gold
    p[10, 3] = gold
    p[9, 4] = gold
    p[8, 5] = gold

    # Chain links
    p[4, 2] = gold_hi
    p[11, 2] = gold_hi

    # Jellyfish bell/dome (pendant)
    for x in range(6, 10):
        for y in range(5, 9):
            p[x, y] = cyan
    # Dome top rounding
    p[7, 4] = cyan
    p[8, 4] = cyan
    # Dome sides
    p[5, 6] = cyan
    p[5, 7] = cyan
    p[10, 6] = cyan
    p[10, 7] = cyan

    # Bright cyan highlights (bioluminescent)
    p[7, 5] = bright_cyan
    p[8, 5] = bright_cyan
    p[7, 6] = bright_cyan
    p[6, 6] = lighten(cyan, 20)

    # Darker shading on bottom/right of bell
    p[9, 7] = darken(cyan, 20)
    p[9, 8] = darken(cyan, 25)
    p[10, 7] = darken(cyan, 30)

    # Tentacles trailing down
    p[6, 9] = tentacle
    p[6, 10] = tentacle
    p[6, 11] = tentacle2
    p[6, 12] = (0, 120, 130, 100)

    p[8, 9] = tentacle
    p[8, 10] = tentacle
    p[8, 11] = tentacle2
    p[8, 12] = tentacle2
    p[8, 13] = (0, 120, 130, 80)

    p[7, 9] = tentacle
    p[7, 10] = tentacle2

    p[9, 9] = tentacle
    p[9, 10] = tentacle2
    p[9, 11] = (0, 120, 130, 100)

    # Glow aura
    p[5, 5] = glow
    p[5, 8] = glow
    p[10, 5] = glow
    p[10, 8] = glow
    p[4, 6] = (0, 150, 150, 80)
    p[11, 6] = (0, 150, 150, 80)

create_texture("jellyfish_necklace.png", draw_jellyfish_necklace)

# ============================================================
# 15. HOLY LOCKET
# ============================================================
def draw_holy_locket(p):
    gold = (218, 165, 32, 255)
    gold_hi = lighten(gold, 35)
    gold_sh = darken(gold, 35)
    white = (255, 255, 255, 255)
    divine = (255, 255, 200, 180)
    divine2 = (255, 255, 180, 100)

    # Gold chain in V shape
    p[3, 1] = gold_hi
    p[4, 2] = gold
    p[5, 3] = gold
    p[6, 4] = gold

    p[12, 1] = gold_hi
    p[11, 2] = gold
    p[10, 3] = gold
    p[9, 4] = gold

    # Link details
    p[3, 1] = gold_hi
    p[12, 1] = gold_hi

    # Locket body (oval shape)
    for x in range(5, 11):
        for y in range(5, 13):
            p[x, y] = gold
    # Oval rounding
    for x in range(6, 10):
        p[x, 4] = gold
        p[x, 13] = gold
    p[7, 3] = gold
    p[8, 3] = gold

    # Highlight on top-left
    p[5, 5] = gold_hi
    p[5, 6] = gold_hi
    p[6, 4] = gold_hi
    p[6, 5] = gold_hi
    p[7, 3] = gold_hi

    # Shadow on bottom-right
    p[10, 10] = gold_sh
    p[10, 11] = gold_sh
    p[10, 12] = gold_sh
    p[9, 13] = gold_sh

    # Locket edge/rim
    for y in range(5, 13):
        p[4, y] = gold_sh
        p[11, y] = gold_sh

    # Cross on locket face
    # Vertical bar
    for y in range(6, 12):
        p[8, y] = white
    # Horizontal bar
    for x in range(6, 11):
        p[x, 8] = white

    # Cross glow
    p[7, 7] = divine
    p[9, 7] = divine
    p[7, 9] = divine
    p[9, 9] = divine

    # Divine glow emanating
    p[3, 4] = divine2
    p[12, 4] = divine2
    p[3, 9] = divine2
    p[12, 9] = divine2
    p[7, 1] = divine2
    p[8, 1] = divine2
    p[7, 14] = divine2
    p[8, 14] = divine2

    # Hinge on right
    p[11, 8] = darken(gold, 50)
    p[11, 9] = darken(gold, 50)

    # Bail (top connector)
    p[7, 4] = gold
    p[8, 4] = gold

create_texture("holy_locket.png", draw_holy_locket)

# ============================================================
# 16. BASTION RING
# ============================================================
def draw_bastion_ring(p):
    dark_gold = (139, 105, 20, 255)
    gold_hi = lighten(dark_gold, 40)
    gold_sh = darken(dark_gold, 30)
    red_gem = (204, 34, 34, 255)
    gem_hi = (255, 100, 100, 255)
    gem_sh = (140, 20, 20, 255)
    black = (30, 25, 15, 255)

    # Ring band (circular, viewed at slight angle)
    # Bottom curve of ring
    for x in range(4, 12):
        p[x, 12] = dark_gold
        p[x, 13] = gold_sh
    for x in range(5, 11):
        p[x, 14] = gold_sh
    # Left side of ring
    for y in range(6, 13):
        p[3, y] = dark_gold
    for y in range(7, 12):
        p[2, y] = gold_sh
    # Right side of ring
    for y in range(6, 13):
        p[12, y] = dark_gold
    for y in range(7, 12):
        p[13, y] = gold_sh

    # Top of ring band
    for x in range(4, 12):
        p[x, 5] = gold_hi
        p[x, 6] = dark_gold

    # Inner hole (dark)
    for x in range(5, 11):
        for y in range(7, 12):
            p[x, y] = black

    # Angular/harsh piglin-style engravings on band
    p[4, 6] = gold_hi
    p[5, 5] = gold_hi
    p[4, 12] = gold_sh
    p[11, 12] = gold_sh
    p[11, 6] = gold_hi
    p[10, 5] = gold_hi

    # Engraving details
    p[3, 8] = gold_hi
    p[3, 10] = gold_hi
    p[12, 8] = gold_sh
    p[12, 10] = gold_sh

    # Red gem setting on top
    for x in range(6, 10):
        for y in range(2, 6):
            p[x, y] = red_gem
    # Gem highlight
    p[6, 2] = gem_hi
    p[7, 2] = gem_hi
    p[7, 3] = gem_hi
    # Gem shadow
    p[9, 5] = gem_sh
    p[9, 4] = gem_sh
    # Gem prongs (gold claws holding gem)
    p[5, 3] = dark_gold
    p[5, 4] = dark_gold
    p[10, 3] = dark_gold
    p[10, 4] = dark_gold

    # Angular design detail on band sides
    p[2, 9] = gold_hi
    p[13, 9] = gold_sh

create_texture("bastion_ring.png", draw_bastion_ring)

# ============================================================
# 17. CHORUS INHIBITOR
# ============================================================
def draw_chorus_inhibitor(p):
    purple = (119, 68, 170, 255)
    purple_hi = lighten(purple, 35)
    purple_sh = darken(purple, 30)
    pink = (204, 136, 221, 255)
    pink_hi = lighten(pink, 30)
    pink_sh = darken(pink, 30)
    end_particle = (180, 100, 220, 150)

    # Ring band
    for x in range(4, 12):
        p[x, 12] = purple
        p[x, 13] = purple_sh
    for x in range(5, 11):
        p[x, 14] = purple_sh
    for y in range(6, 13):
        p[3, y] = purple
    for y in range(7, 12):
        p[2, y] = purple_sh
    for y in range(6, 13):
        p[12, y] = purple
    for y in range(7, 12):
        p[13, y] = purple_sh

    # Top of ring
    for x in range(4, 12):
        p[x, 5] = purple_hi
        p[x, 6] = purple

    # Inner hole
    for x in range(5, 11):
        for y in range(7, 12):
            p[x, y] = (20, 10, 30, 255)

    # Chorus fruit sphere on top
    for x in range(6, 10):
        for y in range(1, 5):
            p[x, y] = pink
    p[7, 0] = pink
    p[8, 0] = pink
    p[7, 5] = pink
    p[8, 5] = pink

    # Sphere highlight
    p[6, 1] = pink_hi
    p[7, 1] = pink_hi
    p[7, 2] = pink_hi

    # Sphere shadow
    p[9, 4] = pink_sh
    p[9, 3] = pink_sh

    # Chorus fruit spot details
    p[7, 3] = lighten(pink, 15)
    p[8, 2] = darken(pink, 10)

    # End-dimension particles
    p[4, 2] = end_particle
    p[11, 3] = end_particle
    p[1, 8] = end_particle
    p[14, 7] = end_particle
    p[5, 0] = (180, 100, 220, 100)
    p[10, 1] = (180, 100, 220, 100)

    # Ring highlight (left side)
    p[3, 7] = purple_hi
    p[3, 8] = purple_hi
    p[2, 8] = purple_hi

create_texture("chorus_inhibitor.png", draw_chorus_inhibitor)

# ============================================================
# 18. ARROW QUIVER
# ============================================================
def draw_arrow_quiver(p):
    brown = (107, 66, 38, 255)
    brown_hi = lighten(brown, 30)
    brown_sh = darken(brown, 30)
    shaft = (139, 115, 85, 255)
    red_f = (200, 50, 50, 255)
    blue_f = (50, 100, 200, 255)
    green_f = (50, 170, 50, 255)
    strap = (90, 55, 30, 255)
    arrow_tip = (180, 180, 190, 255)

    # Quiver body (cylinder shape)
    for x in range(5, 11):
        for y in range(5, 15):
            p[x, y] = brown
    # Rounded left edge
    for y in range(6, 14):
        p[4, y] = brown_sh
    # Rounded right edge shadow
    for y in range(6, 14):
        p[11, y] = brown_sh

    # Highlight on left face
    for y in range(5, 15):
        p[5, y] = brown_hi
        p[6, y] = brown_hi

    # Shadow on right face
    for y in range(5, 15):
        p[10, y] = darken(brown, 15)

    # Leather rim at top
    for x in range(4, 12):
        p[x, 5] = darken(brown, 20)
        p[x, 4] = brown_sh

    # Bottom of quiver
    for x in range(5, 11):
        p[x, 15] = brown_sh

    # Strap going diagonally
    p[4, 6] = strap
    p[3, 5] = strap
    p[2, 4] = strap
    p[11, 10] = strap
    p[12, 11] = strap
    p[13, 12] = strap

    # Arrow 1 (left) with red fletching
    p[6, 4] = shaft
    p[6, 3] = shaft
    p[6, 2] = red_f
    p[6, 1] = red_f
    p[5, 1] = darken(red_f, 30)
    p[7, 1] = darken(red_f, 30)

    # Arrow 2 (middle) with blue fletching
    p[8, 4] = shaft
    p[8, 3] = shaft
    p[8, 2] = shaft
    p[8, 1] = blue_f
    p[8, 0] = blue_f
    p[7, 0] = darken(blue_f, 30)
    p[9, 0] = darken(blue_f, 30)

    # Arrow 3 (right) with green fletching
    p[10, 4] = shaft
    p[10, 3] = shaft
    p[10, 2] = green_f
    p[10, 1] = green_f
    p[9, 2] = darken(green_f, 30)
    p[11, 2] = darken(green_f, 30)

    # Decorative stitch on quiver
    for y in range(7, 14, 2):
        p[8, y] = darken(brown, 25)

create_texture("arrow_quiver.png", draw_arrow_quiver)

# ============================================================
# 19. ELYTRA BOOSTER
# ============================================================
def draw_elytra_booster(p):
    metal = (68, 68, 68, 255)
    metal_hi = lighten(metal, 40)
    metal_sh = darken(metal, 30)
    orange = (255, 136, 0, 255)
    orange_hi = lighten(orange, 40)
    flame = (255, 200, 50, 255)
    flame2 = (255, 100, 0, 200)
    dark = (40, 40, 45, 255)

    # Central body/backpack
    for x in range(5, 11):
        for y in range(3, 12):
            p[x, y] = metal
    # Backpack highlight
    for y in range(3, 12):
        p[5, y] = metal_hi
    for x in range(5, 11):
        p[x, 3] = metal_hi
    # Shadow
    for y in range(3, 12):
        p[10, y] = metal_sh
    for x in range(5, 11):
        p[x, 11] = metal_sh

    # Straps at top
    p[4, 3] = dark
    p[4, 4] = dark
    p[4, 5] = dark
    p[11, 3] = dark
    p[11, 4] = dark
    p[11, 5] = dark

    # Wing stubs (small mechanical wings)
    # Left wing
    p[3, 5] = metal
    p[2, 5] = metal
    p[1, 4] = metal_hi
    p[2, 4] = metal
    p[3, 6] = metal_sh

    # Right wing
    p[12, 5] = metal
    p[13, 5] = metal
    p[14, 4] = metal
    p[13, 4] = metal
    p[12, 6] = metal_sh

    # Rocket nozzles (bottom)
    # Left nozzle
    for y in range(10, 13):
        p[6, y] = orange
    p[6, 10] = orange_hi
    p[6, 12] = darken(orange, 20)

    # Right nozzle
    for y in range(10, 13):
        p[9, y] = orange
    p[9, 10] = orange_hi
    p[9, 12] = darken(orange, 20)

    # Nozzle rims
    p[5, 12] = metal_sh
    p[7, 12] = metal_sh
    p[8, 12] = metal_sh
    p[10, 12] = metal_sh

    # Flame particles from nozzles
    p[6, 13] = flame
    p[6, 14] = flame2
    p[6, 15] = (255, 80, 0, 120)
    p[9, 13] = flame
    p[9, 14] = flame2
    p[9, 15] = (255, 80, 0, 120)

    # Side flame wisps
    p[5, 14] = (255, 150, 0, 100)
    p[10, 14] = (255, 150, 0, 100)

    # Rivets/details on body
    p[7, 5] = metal_hi
    p[8, 5] = metal_hi
    p[7, 8] = metal_hi
    p[8, 8] = metal_hi

    # Panel line
    for y in range(4, 11):
        p[8, y] = darken(metal, 15)

    # Gauge/indicator light
    p[7, 6] = (0, 200, 0, 255)
    p[7, 7] = (0, 150, 0, 255)

create_texture("elytra_booster.png", draw_elytra_booster)

# ============================================================
# 20. MIDNIGHT ROBE
# ============================================================
def draw_midnight_robe(p):
    dark = (10, 10, 42, 255)
    dark_hi = lighten(dark, 25)
    dark_sh = darken(dark, 10)
    star = (170, 170, 204, 255)
    star_bright = (220, 220, 240, 255)
    moon = (136, 136, 170, 255)
    moon_hi = lighten(moon, 30)
    edge = (30, 30, 60, 255)

    # Robe body (flowing shape)
    for x in range(3, 13):
        for y in range(1, 15):
            p[x, y] = dark

    # Robe narrows at shoulders
    for x in range(5, 11):
        p[x, 0] = dark
    # Collar
    p[6, 0] = edge
    p[9, 0] = edge

    # Flowing bottom (wider, asymmetric)
    p[2, 13] = dark
    p[2, 14] = dark
    p[13, 13] = dark
    p[13, 14] = dark
    p[1, 14] = dark_sh
    p[14, 14] = dark_sh
    p[2, 15] = dark_sh
    p[13, 15] = dark_sh

    # Left edge highlight (moonlight)
    for y in range(1, 14):
        p[3, y] = dark_hi
    p[5, 0] = dark_hi

    # Right edge shadow
    for y in range(1, 14):
        p[12, y] = dark_sh

    # Fold lines
    p[7, 3] = edge
    p[7, 4] = edge
    p[7, 5] = edge
    p[8, 6] = edge
    p[8, 7] = edge
    p[7, 8] = edge
    p[7, 9] = edge
    p[7, 10] = edge
    p[6, 11] = edge
    p[6, 12] = edge
    p[6, 13] = edge

    # Star sparkle pixels scattered
    p[5, 3] = star
    p[9, 2] = star_bright
    p[4, 6] = star
    p[10, 5] = star
    p[6, 9] = star_bright
    p[11, 8] = star
    p[5, 12] = star
    p[9, 11] = star_bright
    p[11, 3] = star
    p[4, 10] = star
    p[10, 13] = star

    # Tiny star clusters
    p[8, 4] = (200, 200, 230, 200)
    p[10, 7] = (200, 200, 230, 180)

    # Crescent moon detail (upper area)
    p[9, 2] = moon_hi
    p[10, 2] = moon
    p[10, 3] = moon
    p[10, 4] = moon
    p[9, 4] = moon_hi
    # Crescent shape (inner dark = the "bite" out of the moon)
    p[9, 3] = dark  # This creates the crescent

    # Hood shadow at top
    for x in range(5, 11):
        p[x, 1] = dark_sh

    # Bottom hem glow
    for x in range(3, 13):
        p[x, 14] = (20, 20, 55, 255)

create_texture("midnight_robe.png", draw_midnight_robe)

print("\n=== All 20 textures generated! ===")
