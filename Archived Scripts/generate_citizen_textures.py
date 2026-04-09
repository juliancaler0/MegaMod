"""
Generates 8 citizen textures (4 male, 4 female) for MegaMod colony system.
Uses the standard 64x64 Minecraft villager skin layout.

Villager texture layout (64x64):
- Head: face at (8,8)-(15,15), top at (8,0)-(15,7), etc. Head box starts at U=0,V=0
- Body/Torso: front at (20,20)-(27,31), etc. Body box starts at U=16,V=20
- Right arm: U=44,V=22 area (arms crossed in front)
- Left arm: U=4,V=22 area
- Right leg: U=0,V=20 area (but actually villager uses robe that covers legs)
- Nose: small protrusion on face

The villager model is different from player model:
- Head is 8x10x8 (taller than player), UV starts at (0,0)
- Body is 8x12x8, UV starts at (16,20)
- Arms are 4x12x4, right arm UV at (44,22), left arm UV at (4,22) (mirrored for crossed arms)
- Legs are hidden by robe

Actually, the vanilla villager.png layout:
- The texture is mapped with the head taking the top portion
- Head (8x10x8): U0,V0 for top, with face at U8,V10 to U16,V20
- Hat/overlay layer in the second row
- Body (8x12x8): U16,V20 for body
- Arms: Right at U44,V22, Left mirrored

Let me use the actual vanilla villager UV mapping carefully.
"""

from PIL import Image, ImageDraw
import os

OUTPUT_DIR = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "src", "main", "resources", "assets", "megamod", "textures", "entity", "citizen"
)

os.makedirs(OUTPUT_DIR, exist_ok=True)

# ============================================================
# Villager UV layout reference (64x64 texture)
# ============================================================
# The vanilla villager model has these UV regions:
#
# HEAD (8w x 10h x 8d):
#   Top:    (8,0)  -> (16,8)    8x8
#   Bottom: (16,0) -> (24,8)    8x8
#   Front:  (8,8)  -> (16,18)   8x10  (the face)
#   Back:   (24,8) -> (32,18)   8x10
#   Right:  (0,8)  -> (8,18)    8x10
#   Left:   (16,8) -> (24,18)   8x10
#
# HAT / Head overlay (same dims, offset):
#   Top:    (40,0) -> (48,8)
#   Front:  (40,8) -> (48,18)
#   etc.    shifted by +32 in U
#
# BODY (8w x 12h x 8d):  UV origin at (16,20)
#   Top:    (24,20) -> (32,24)   -- wait, let me recalculate
#   Actually body UV origin = (16,20) means:
#   Right:  (16,24) -> (24,36)   8x12
#   Front:  (24,24) -> (32,36)   8x12  (the torso front)
#   Left:   (32,24) -> (40,36)   8x12
#   Back:   (40,24) -> (48,36)   8x12
#   Top:    (24,20) -> (32,24)   8x4...
#
# Actually the standard box UV mapping for a box with dims (W, H, D):
# Starting at origin (U, V):
#   Top:    (U+D,    V)       -> (U+D+W,   V+D)
#   Bottom: (U+D+W,  V)       -> (U+D+W+W, V+D)
#   Right:  (U,      V+D)     -> (U+D,     V+D+H)
#   Front:  (U+D,    V+D)     -> (U+D+W,   V+D+H)
#   Left:   (U+D+W,  V+D)     -> (U+D+W+D, V+D+H)
#   Back:   (U+D+W+D,V+D)     -> (U+D+W+D+W, V+D+H)
#
# HEAD (W=8, H=10, D=8) at origin (0,0):
#   Top:    (8,0)   -> (16,8)
#   Bottom: (16,0)  -> (24,8)
#   Right:  (0,8)   -> (8,18)
#   Front:  (8,8)   -> (16,18)    <-- THE FACE
#   Left:   (16,8)  -> (24,18)
#   Back:   (24,8)  -> (32,18)
#
# BODY (W=8, H=12, D=8) at origin (16,20):
#   Top:    (24,20) -> (32,24)    -- wait D=8? No...
#   Hmm, Villager body is actually not standard. Let me check.
#   In VillagerModel, body is addOrReplaceChild("body",
#     CubeListBuilder.create().texOffs(16, 20).addBox(-4, -12, -3, 8, 12, 6), ...)
#   So body is W=8, H=12, D=6
#
# BODY (W=8, H=12, D=6) at origin (16,20):
#   Top:    (22,20) -> (30,26)
#   Bottom: (30,20) -> (38,26)
#   Right:  (16,26) -> (22,38)    6x12
#   Front:  (22,26) -> (30,38)    8x12  <-- TORSO FRONT
#   Left:   (30,26) -> (36,38)    6x12
#   Back:   (36,26) -> (44,38)    8x12
#
# ARMS (W=4, H=12, D=4) - the villager has crossed arms
#   Right arm at texOffs(44, 22):
#     Top:    (48,22) -> (52,26)
#     Right:  (44,26) -> (48,38)   4x12
#     Front:  (48,26) -> (52,38)   4x12
#     Left:   (52,26) -> (56,38)   4x12
#     Back:   (56,26) -> (60,38)   4x12
#   Left arm is mirrored from right arm in vanilla
#
# ROBE / lower body jacket overlay at texOffs(0, 38):
#   (W=8, H=12, D=6 or similar - the "jacket" layer)
#   This covers the lower body like a robe
#
# NOSE: texOffs(24, 0), box size 2x4x2 approximately
#   At (24,0): Top: (26,0)->(28,2), Front: (26,2)->(28,6), etc.

def fill_rect(img, x1, y1, x2, y2, color):
    """Fill rectangle from (x1,y1) to (x2-1,y2-1) with color."""
    for y in range(y1, y2):
        for x in range(x1, x2):
            if 0 <= x < 64 and 0 <= y < 64:
                img.putpixel((x, y), color)

def draw_face(img, skin_tone, eye_color, lip_color=None):
    """Draw the face on the head front region (8,8) to (16,18). That's 8 wide x 10 tall."""
    # Fill entire face area with skin tone
    fill_rect(img, 8, 8, 16, 18, skin_tone)

    # Eyes at row 12-13 (relative to face top at y=8, so y=12,13 is row 4-5)
    # Left eye at x=10,11 and right eye at x=13,14
    img.putpixel((10, 12), eye_color)
    img.putpixel((11, 12), eye_color)
    img.putpixel((13, 12), eye_color)
    img.putpixel((14, 12), eye_color)

    # White of eyes (above pupil)
    white = (255, 255, 255, 255)
    img.putpixel((10, 11), white)
    img.putpixel((11, 11), white)
    img.putpixel((13, 11), white)
    img.putpixel((14, 11), white)

    # Eyebrows - slightly darker than skin
    brow = (max(0, skin_tone[0]-40), max(0, skin_tone[1]-40), max(0, skin_tone[2]-40), 255)
    img.putpixel((10, 10), brow)
    img.putpixel((11, 10), brow)
    img.putpixel((13, 10), brow)
    img.putpixel((14, 10), brow)

    # Mouth at row 15 (y=15)
    mouth_color = lip_color if lip_color else (max(0, skin_tone[0]-20), max(0, skin_tone[1]-30), max(0, skin_tone[2]-30), 255)
    img.putpixel((11, 15), mouth_color)
    img.putpixel((12, 15), mouth_color)
    img.putpixel((13, 15), mouth_color)

def draw_head_sides(img, skin_tone, hair_color):
    """Draw head sides (right, left, back, top, bottom)."""
    # Right side of head (0,8) -> (8,18)
    fill_rect(img, 0, 8, 8, 18, skin_tone)
    # Hair on top portion of side
    fill_rect(img, 0, 8, 8, 11, hair_color)
    # Ear
    ear_color = (min(255, skin_tone[0]+10), min(255, skin_tone[1]+5), skin_tone[2], 255)
    img.putpixel((6, 12), ear_color)
    img.putpixel((6, 13), ear_color)

    # Left side of head (16,8) -> (24,18)
    fill_rect(img, 16, 8, 24, 18, skin_tone)
    fill_rect(img, 16, 8, 24, 11, hair_color)
    img.putpixel((17, 12), ear_color)
    img.putpixel((17, 13), ear_color)

    # Back of head (24,8) -> (32,18)
    fill_rect(img, 24, 8, 32, 18, hair_color)

    # Top of head (8,0) -> (16,8)
    fill_rect(img, 8, 0, 16, 8, hair_color)

    # Bottom of head (16,0) -> (24,8) - chin/neck area
    fill_rect(img, 16, 0, 24, 8, skin_tone)

def draw_hair_front(img, hair_color):
    """Add hair fringe on front of head face area."""
    # Hair across top of forehead (row 8-9 of face)
    fill_rect(img, 8, 8, 16, 10, hair_color)

def draw_long_hair(img, hair_color):
    """For female variants: extend hair lower on sides and back."""
    # Extend hair lower on right side
    fill_rect(img, 0, 11, 4, 16, hair_color)
    # Extend hair lower on left side
    fill_rect(img, 20, 11, 24, 16, hair_color)
    # Hair hangs lower on back
    fill_rect(img, 24, 8, 32, 18, hair_color)
    # Extra fringe - hair visible at edges of face
    img.putpixel((8, 10), hair_color)
    img.putpixel((15, 10), hair_color)

def draw_nose(img, skin_tone):
    """Draw the villager nose at UV origin (24,0). Nose is 2w x 4h x 2d."""
    # Nose box (W=2, H=4, D=2) at origin (24,0):
    #   Top:    (26,0) -> (28,2)
    #   Right:  (24,2) -> (26,6)
    #   Front:  (26,2) -> (28,6)
    #   Left:   (28,2) -> (30,6)
    #   Back:   (30,2) -> (32,6)
    nose_color = (max(0, skin_tone[0]-10), max(0, skin_tone[1]-15), max(0, skin_tone[2]-15), 255)
    nose_shade = (max(0, skin_tone[0]-25), max(0, skin_tone[1]-30), max(0, skin_tone[2]-30), 255)

    fill_rect(img, 26, 0, 28, 2, nose_color)   # top
    fill_rect(img, 24, 2, 26, 6, nose_shade)   # right
    fill_rect(img, 26, 2, 28, 6, nose_color)   # front
    fill_rect(img, 28, 2, 30, 6, nose_shade)   # left
    fill_rect(img, 30, 2, 32, 6, nose_color)   # back

def draw_body_front(img, tunic_color, tunic_shade):
    """Draw the body/torso front at (22,26) -> (30,38), 8x12."""
    fill_rect(img, 22, 26, 30, 38, tunic_color)
    # Collar / neckline - slightly lighter at top
    collar = (min(255, tunic_color[0]+20), min(255, tunic_color[1]+20), min(255, tunic_color[2]+20), 255)
    fill_rect(img, 24, 26, 28, 28, collar)
    # Belt line
    fill_rect(img, 22, 34, 30, 35, tunic_shade)
    # Button or center line detail
    for y in range(28, 34):
        img.putpixel((26, y), tunic_shade)

def draw_body_sides(img, tunic_color, tunic_shade):
    """Draw body sides, top, bottom, back."""
    # Right side (16,26) -> (22,38) - 6x12
    fill_rect(img, 16, 26, 22, 38, tunic_shade)
    # Left side (30,26) -> (36,38) - 6x12
    fill_rect(img, 30, 26, 36, 38, tunic_shade)
    # Back (36,26) -> (44,38) - 8x12
    fill_rect(img, 36, 26, 44, 38, tunic_color)
    # Belt on back too
    fill_rect(img, 36, 34, 44, 35, tunic_shade)
    # Top (22,20) -> (30,26) - 8x6
    fill_rect(img, 22, 20, 30, 26, tunic_color)
    # Bottom (30,20) -> (38,26) - 8x6
    fill_rect(img, 30, 20, 38, 26, tunic_shade)

def draw_arms(img, tunic_color, tunic_shade, skin_tone):
    """Draw arms (crossed villager style)."""
    # Right arm at texOffs(44,22), box W=4, H=12, D=4:
    #   Top:    (48,22) -> (52,26)
    #   Right:  (44,26) -> (48,38)
    #   Front:  (48,26) -> (52,38)
    #   Left:   (52,26) -> (56,38)
    #   Back:   (56,26) -> (60,38)

    # Sleeve (upper portion) + skin (lower portion where hands show)
    # Top
    fill_rect(img, 48, 22, 52, 26, tunic_color)
    # Right side
    fill_rect(img, 44, 26, 48, 35, tunic_shade)
    fill_rect(img, 44, 35, 48, 38, skin_tone)  # hand
    # Front
    fill_rect(img, 48, 26, 52, 35, tunic_color)
    fill_rect(img, 48, 35, 52, 38, skin_tone)  # hand
    # Left side
    fill_rect(img, 52, 26, 56, 35, tunic_shade)
    fill_rect(img, 52, 35, 56, 38, skin_tone)  # hand
    # Back
    fill_rect(img, 56, 26, 60, 35, tunic_color)
    fill_rect(img, 56, 35, 60, 38, skin_tone)  # hand

def draw_robe(img, tunic_color, tunic_shade):
    """Draw the robe/jacket overlay at texOffs(0,38). This is the lower body covering."""
    # Jacket overlay for body, same box dimensions as body but offset
    # (W=8, H=12, D=6) at origin (0,38):
    #   Top:    (6,38) -> (14,44)
    #   Right:  (0,44) -> (6,56)    6x12
    #   Front:  (6,44) -> (14,56)   8x12
    #   Left:   (14,44) -> (20,56)  6x12
    #   Back:   (20,44) -> (28,56)  8x12

    robe_dark = (max(0, tunic_shade[0]-15), max(0, tunic_shade[1]-15), max(0, tunic_shade[2]-15), 255)

    fill_rect(img, 6, 38, 14, 44, tunic_color)    # top
    fill_rect(img, 0, 44, 6, 56, robe_dark)        # right
    fill_rect(img, 6, 44, 14, 56, tunic_shade)     # front
    fill_rect(img, 14, 44, 20, 56, robe_dark)      # left
    fill_rect(img, 20, 44, 28, 56, tunic_shade)    # back

    # Add some detail to robe front - hem line
    fill_rect(img, 6, 54, 14, 56, robe_dark)
    # Center line
    for y in range(44, 54):
        img.putpixel((10, y), robe_dark)

def add_tunic_detail_female(img, accent_color, tunic_color):
    """Add feminine details - apron or decorative trim on body front."""
    # Slight waist taper effect - lighter pixels at waist
    waist = (min(255, tunic_color[0]+15), min(255, tunic_color[1]+15), min(255, tunic_color[2]+15), 255)
    fill_rect(img, 23, 32, 29, 34, waist)

    # Trim along neckline
    img.putpixel((23, 26), accent_color)
    img.putpixel((28, 26), accent_color)
    img.putpixel((24, 27), accent_color)
    img.putpixel((27, 27), accent_color)

def add_tunic_detail_male(img, accent_color, tunic_shade):
    """Add masculine details - collar points, thicker belt."""
    # Thicker belt
    fill_rect(img, 22, 33, 30, 36, tunic_shade)
    # Belt buckle
    img.putpixel((26, 34), accent_color)
    img.putpixel((25, 34), accent_color)

def generate_citizen_texture(filename, skin_tone, hair_color, eye_color,
                              tunic_color, tunic_shade, is_female,
                              accent_color=None, lip_color=None):
    """Generate a single citizen texture and save it."""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))

    # Draw all parts
    draw_head_sides(img, skin_tone, hair_color)
    draw_face(img, skin_tone, eye_color, lip_color)
    draw_hair_front(img, hair_color)
    draw_nose(img, skin_tone)

    if is_female:
        draw_long_hair(img, hair_color)

    draw_body_front(img, tunic_color, tunic_shade)
    draw_body_sides(img, tunic_color, tunic_shade)
    draw_arms(img, tunic_color, tunic_shade, skin_tone)
    draw_robe(img, tunic_color, tunic_shade)

    if accent_color is None:
        accent_color = (200, 180, 80, 255)  # default gold-ish accent

    if is_female:
        add_tunic_detail_female(img, accent_color, tunic_color)
    else:
        add_tunic_detail_male(img, accent_color, tunic_shade)

    filepath = os.path.join(OUTPUT_DIR, filename)
    img.save(filepath)
    print(f"  Generated: {filepath}")

# ============================================================
# Skin tones
# ============================================================
SKIN_LIGHT    = (228, 190, 160, 255)   # fair/light
SKIN_MEDIUM   = (210, 170, 135, 255)   # medium
SKIN_TAN      = (190, 150, 110, 255)   # tan
SKIN_DARK     = (160, 120, 85, 255)    # darker

# ============================================================
# Hair colors
# ============================================================
HAIR_BROWN    = (80, 50, 30, 255)
HAIR_BLACK    = (30, 25, 20, 255)
HAIR_RED      = (140, 55, 30, 255)
HAIR_BLONDE   = (195, 165, 90, 255)

# ============================================================
# Eye colors
# ============================================================
EYE_BROWN     = (60, 35, 15, 255)
EYE_BLUE      = (40, 70, 140, 255)
EYE_GREEN     = (35, 90, 45, 255)
EYE_HAZEL     = (90, 65, 25, 255)

# ============================================================
# Male variants
# ============================================================
print("Generating male citizen textures...")

# Male 1: Brown hair, light skin, brown tunic (farmer/peasant look)
generate_citizen_texture(
    "citizen_male_1.png",
    skin_tone=SKIN_LIGHT,
    hair_color=HAIR_BROWN,
    eye_color=EYE_BROWN,
    tunic_color=(120, 85, 50, 255),    # warm brown
    tunic_shade=(90, 60, 35, 255),
    is_female=False,
    accent_color=(170, 140, 60, 255)   # brass buckle
)

# Male 2: Black hair, tan skin, green tunic (woodsman look)
generate_citizen_texture(
    "citizen_male_2.png",
    skin_tone=SKIN_TAN,
    hair_color=HAIR_BLACK,
    eye_color=EYE_BROWN,
    tunic_color=(60, 90, 50, 255),     # forest green
    tunic_shade=(40, 65, 35, 255),
    is_female=False,
    accent_color=(140, 120, 50, 255)   # tarnished gold
)

# Male 3: Red hair, medium skin, beige tunic (merchant look)
generate_citizen_texture(
    "citizen_male_3.png",
    skin_tone=SKIN_MEDIUM,
    hair_color=HAIR_RED,
    eye_color=EYE_GREEN,
    tunic_color=(180, 160, 120, 255),  # beige/linen
    tunic_shade=(140, 125, 90, 255),
    is_female=False,
    accent_color=(160, 100, 40, 255)   # copper buckle
)

# Male 4: Blonde hair, dark skin, grey tunic (laborer/smith look)
generate_citizen_texture(
    "citizen_male_4.png",
    skin_tone=SKIN_DARK,
    hair_color=HAIR_BLONDE,
    eye_color=EYE_HAZEL,
    tunic_color=(100, 95, 90, 255),    # stone grey
    tunic_shade=(70, 65, 60, 255),
    is_female=False,
    accent_color=(130, 130, 130, 255)  # iron buckle
)

# ============================================================
# Female variants
# ============================================================
print("Generating female citizen textures...")

# Female 1: Brown hair, medium skin, blue dress
generate_citizen_texture(
    "citizen_female_1.png",
    skin_tone=SKIN_MEDIUM,
    hair_color=HAIR_BROWN,
    eye_color=EYE_BLUE,
    tunic_color=(60, 75, 130, 255),    # medieval blue
    tunic_shade=(40, 55, 100, 255),
    is_female=True,
    accent_color=(180, 160, 100, 255), # gold trim
    lip_color=(190, 120, 120, 255)
)

# Female 2: Black hair, light skin, red dress
generate_citizen_texture(
    "citizen_female_2.png",
    skin_tone=SKIN_LIGHT,
    hair_color=HAIR_BLACK,
    eye_color=EYE_GREEN,
    tunic_color=(140, 45, 40, 255),    # deep red
    tunic_shade=(105, 30, 25, 255),
    is_female=True,
    accent_color=(200, 170, 70, 255),  # gold trim
    lip_color=(180, 110, 110, 255)
)

# Female 3: Red hair, light skin, cream dress
generate_citizen_texture(
    "citizen_female_3.png",
    skin_tone=SKIN_LIGHT,
    hair_color=HAIR_RED,
    eye_color=EYE_BLUE,
    tunic_color=(200, 185, 155, 255),  # cream/off-white
    tunic_shade=(165, 150, 125, 255),
    is_female=True,
    accent_color=(140, 90, 35, 255),   # copper trim
    lip_color=(200, 130, 130, 255)
)

# Female 4: Blonde hair, dark skin, purple dress
generate_citizen_texture(
    "citizen_female_4.png",
    skin_tone=SKIN_DARK,
    hair_color=HAIR_BLONDE,
    eye_color=EYE_HAZEL,
    tunic_color=(90, 55, 110, 255),    # muted purple
    tunic_shade=(65, 40, 85, 255),
    is_female=True,
    accent_color=(170, 150, 80, 255),  # gold trim
    lip_color=(170, 110, 100, 255)
)

print(f"\nDone! Generated 8 citizen textures in:\n  {OUTPUT_DIR}")
