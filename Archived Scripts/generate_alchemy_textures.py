"""
Generate improved 16x16 potion textures for all 20 MegaMod alchemy potions.
Each potion gets a detailed flask shape with glass shading, cork stopper,
gradient liquid fill, and per-potion unique accent details.
"""
from PIL import Image
import os

OUT = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\textures\item"
TRANSPARENT = (0, 0, 0, 0)


def new_img():
    return Image.new("RGBA", (16, 16), TRANSPARENT)


def px(img, x, y, color):
    if 0 <= x < 16 and 0 <= y < 16:
        if len(color) == 3:
            color = color + (255,)
        img.putpixel((x, y), color)


def save(img, name):
    path = os.path.join(OUT, name)
    img.save(path)
    print(f"  Saved {name}")


def blend(c1, c2, t):
    """Blend two RGB colors. t=0 gives c1, t=1 gives c2."""
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))


# ============================================================
# Glass & Cork palette
# ============================================================
GLASS_DARK = (120, 145, 160)
GLASS_MID = (165, 190, 205)
GLASS_LIGHT = (200, 220, 235)
GLASS_HIGHLIGHT = (230, 242, 250)

CORK_DARK = (110, 78, 42)
CORK_MID = (145, 105, 58)
CORK_LIGHT = (175, 135, 80)
CORK_TOP = (160, 120, 68)

# ============================================================
# Bottle shape definition
# ============================================================
# Cork
CORK_PIXELS = {
    0: [(7, CORK_TOP), (8, CORK_LIGHT)],
    1: [(7, CORK_DARK), (8, CORK_MID)],
}

# Glass neck
NECK_PIXELS = {
    2: [(7, GLASS_DARK), (8, GLASS_LIGHT)],
    3: [(7, GLASS_DARK), (8, GLASS_LIGHT)],
}

# Shoulder (glass transition - widens)
SHOULDER_PIXELS = {
    4: [(6, GLASS_DARK), (9, GLASS_LIGHT)],  # glass outline; 7,8 are liquid
}

# Body rows: (left_glass_x, right_glass_x, liquid_x_start, liquid_x_end_inclusive)
BODY_ROWS = {
    5: (5, 10, 6, 9),
    6: (4, 11, 5, 10),
    7: (4, 11, 5, 10),
    8: (4, 11, 5, 10),
    9: (4, 11, 5, 10),
    10: (4, 11, 5, 10),
    11: (5, 10, 6, 9),
}

# Base (all glass)
BASE_PIXELS = {
    12: [5, 6, 7, 8, 9, 10],
    13: [6, 7, 8, 9],
}


def draw_bottle(img, liquid_dark, liquid_mid, liquid_light, liquid_highlight, detail_func=None):
    """Draw the full bottle with colored liquid and optional per-potion details."""

    # Cork
    for row, pixels in CORK_PIXELS.items():
        for x, color in pixels:
            px(img, x, row, color)

    # Neck
    for row, pixels in NECK_PIXELS.items():
        for x, color in pixels:
            px(img, x, row, color)

    # Shoulder glass + liquid inside
    for x_glass_l, _ in SHOULDER_PIXELS[4]:
        pass
    px(img, 6, 4, GLASS_DARK)
    px(img, 9, 4, GLASS_LIGHT)
    # Shoulder liquid
    for x in [7, 8]:
        px(img, x, 4, liquid_highlight)

    # Body
    for row, (gl, gr, ls, le) in BODY_ROWS.items():
        # Glass edges
        px(img, gl, row, GLASS_DARK)
        px(img, gr, row, GLASS_LIGHT)
        # Liquid fill with gradient: top rows lighter, bottom darker
        # Row 5-6: highlight/light zone, 7-8: mid zone, 9-11: dark zone
        for x in range(ls, le + 1):
            # Horizontal shading: left = darker, center = base, right = lighter
            col_ratio = (x - ls) / max(le - ls, 1)  # 0.0 (left) to 1.0 (right)

            if row <= 6:
                # Top: light zone with highlight
                base = liquid_light
                if x == ls:
                    base = liquid_mid
                elif x == le:
                    base = blend(liquid_light, liquid_highlight, 0.3)
            elif row <= 8:
                # Middle zone
                if col_ratio < 0.2:
                    base = blend(liquid_dark, liquid_mid, 0.5)
                elif col_ratio > 0.8:
                    base = liquid_light
                else:
                    base = liquid_mid
            else:
                # Bottom: darker
                if col_ratio < 0.2:
                    base = liquid_dark
                elif col_ratio > 0.8:
                    base = blend(liquid_mid, liquid_light, 0.3)
                else:
                    base = blend(liquid_dark, liquid_mid, 0.6)

            px(img, x, row, base)

    # Liquid surface highlight (meniscus)
    px(img, 7, 5, liquid_highlight)
    px(img, 8, 5, blend(liquid_highlight, liquid_light, 0.5))

    # Glass highlight reflection on left side
    px(img, 5, 7, GLASS_HIGHLIGHT)
    px(img, 5, 8, blend(GLASS_LIGHT, (255, 255, 255), 0.2))

    # Base glass
    for row, x_list in BASE_PIXELS.items():
        for x in x_list:
            if x <= 6:
                px(img, x, row, GLASS_DARK)
            elif x >= 9:
                px(img, x, row, GLASS_LIGHT)
            else:
                px(img, x, row, GLASS_MID)

    # Per-potion unique accents
    if detail_func:
        detail_func(img)


# ============================================================
# Per-potion accent functions
# ============================================================

def accent_inferno(img):
    """Fire ember particles rising from liquid."""
    px(img, 7, 3, (255, 120, 20, 180))   # ember in neck
    px(img, 6, 5, (255, 180, 50, 160))    # spark
    px(img, 9, 7, (255, 100, 10, 140))    # deep ember
    px(img, 7, 9, (255, 160, 30, 120))    # glow


def accent_glacier(img):
    """Ice crystal highlights on glass."""
    px(img, 6, 6, (200, 240, 255, 200))   # frost
    px(img, 10, 8, (180, 230, 255, 180))  # frost
    px(img, 8, 10, (220, 245, 255, 160))  # frost crystal
    px(img, 5, 9, (200, 240, 255, 140))   # edge frost


def accent_shadow_step(img):
    """Dark wisps/smoke tendrils."""
    px(img, 6, 5, (40, 10, 60, 180))     # wisp
    px(img, 9, 6, (30, 5, 50, 160))      # wisp
    px(img, 7, 8, (50, 15, 70, 140))     # deep shadow
    px(img, 8, 3, (60, 20, 80, 120))     # escaping wisp


def accent_vitality(img):
    """Healing sparkle / heart-like glow."""
    px(img, 7, 7, (255, 255, 200, 200))  # bright center
    px(img, 8, 7, (255, 255, 200, 200))  # bright center
    px(img, 6, 8, (180, 255, 180, 160))  # green glow
    px(img, 9, 8, (180, 255, 180, 160))  # green glow


def accent_void_walk(img):
    """Void particles / enderman-like specks."""
    px(img, 6, 6, (20, 0, 40, 200))     # void dark
    px(img, 9, 9, (100, 0, 160, 180))   # purple spark
    px(img, 7, 10, (60, 0, 100, 160))   # void
    px(img, 8, 5, (140, 40, 200, 120))  # ender spark


def accent_tempest(img):
    """Lightning bolt accent."""
    px(img, 7, 5, (255, 255, 200))       # bolt top
    px(img, 8, 6, (255, 255, 180))       # bolt
    px(img, 7, 7, (255, 255, 220))       # bolt mid
    px(img, 6, 8, (255, 255, 160))       # bolt
    px(img, 7, 9, (255, 240, 140))       # bolt bottom


def accent_berserker(img):
    """Blood drip on side of bottle."""
    px(img, 10, 5, (180, 10, 10))        # blood drip top
    px(img, 10, 6, (160, 5, 5))          # drip
    px(img, 10, 7, (140, 0, 0, 200))     # drip fading
    px(img, 7, 8, (200, 40, 40, 180))    # inner glow


def accent_starlight(img):
    """Star sparkle pixels."""
    px(img, 6, 6, (255, 255, 200))       # star
    px(img, 9, 7, (255, 255, 180))       # star
    px(img, 7, 9, (255, 255, 220))       # star
    px(img, 8, 5, (255, 255, 240))       # bright star
    px(img, 8, 3, (255, 240, 160, 140))  # glow from neck


def accent_stone_skin(img):
    """Rocky/cracked texture in liquid."""
    px(img, 6, 7, (90, 85, 75))         # dark crack
    px(img, 8, 8, (110, 105, 95))       # crack
    px(img, 7, 10, (80, 75, 65))        # crack
    px(img, 9, 6, (100, 95, 85))        # crack


def accent_arcane_surge(img):
    """Magical swirl / rune glow."""
    px(img, 6, 6, (200, 140, 255, 200))  # swirl
    px(img, 8, 7, (220, 160, 255, 180))  # swirl center
    px(img, 9, 8, (180, 120, 255, 160))  # swirl
    px(img, 7, 9, (200, 140, 255, 140))  # swirl tail
    px(img, 7, 3, (180, 120, 240, 120))  # arcane glow in neck


def accent_swiftbrew(img):
    """Speed lines / motion blur effect."""
    px(img, 5, 6, (150, 220, 255, 180))  # streak
    px(img, 5, 8, (130, 200, 255, 160))  # streak
    px(img, 5, 10, (110, 180, 240, 140)) # streak
    px(img, 9, 7, (180, 240, 255, 120))  # motion


def accent_iron_gut(img):
    """Metallic sheen / iron flecks."""
    px(img, 7, 6, (200, 200, 190))       # metallic
    px(img, 8, 8, (180, 180, 170))       # iron fleck
    px(img, 6, 9, (210, 210, 200))       # sheen
    px(img, 9, 7, (190, 190, 180))       # fleck


def accent_midas_touch(img):
    """Gold sparkles / coins."""
    px(img, 6, 6, (255, 230, 100))       # gold spark
    px(img, 9, 8, (255, 220, 80))        # gold spark
    px(img, 7, 10, (255, 240, 120))      # gold gleam
    px(img, 8, 5, (255, 255, 180))       # bright gold
    px(img, 8, 3, (255, 215, 0, 140))    # gold glow neck


def accent_eagle_eye(img):
    """Eye-like detail / crosshair."""
    px(img, 7, 7, (255, 255, 220))       # eye center bright
    px(img, 8, 7, (255, 255, 220))       # eye center
    px(img, 6, 7, (0, 100, 110, 200))    # iris dark
    px(img, 9, 7, (0, 100, 110, 200))    # iris dark
    px(img, 7, 6, (0, 120, 130, 160))    # above
    px(img, 8, 8, (0, 120, 130, 160))    # below


def accent_undying(img):
    """Totem glow / golden aura."""
    px(img, 6, 5, (255, 220, 80, 180))   # aura
    px(img, 9, 5, (255, 220, 80, 180))   # aura
    px(img, 7, 7, (255, 255, 200))       # core glow
    px(img, 8, 7, (255, 255, 200))       # core glow
    px(img, 7, 3, (255, 200, 50, 140))   # neck glow
    px(img, 8, 3, (255, 200, 50, 140))   # neck glow


def accent_phantom(img):
    """Ghostly wisps / spectral particles."""
    px(img, 6, 5, (220, 230, 255, 160))  # ghost wisp
    px(img, 9, 7, (200, 215, 255, 140))  # wisp
    px(img, 7, 9, (230, 240, 255, 120))  # fading wisp
    px(img, 8, 3, (210, 220, 245, 100))  # spectral neck glow
    px(img, 8, 6, (255, 255, 255, 100))  # bright center


def accent_titan(img):
    """Strong / powerful cracks / earth energy."""
    px(img, 6, 7, (200, 100, 40))        # energy
    px(img, 9, 8, (220, 120, 50))        # energy
    px(img, 7, 6, (240, 160, 80))        # bright crack
    px(img, 8, 9, (180, 90, 30))         # deep crack
    px(img, 7, 10, (200, 110, 40))       # bottom energy


def accent_tidal_wave(img):
    """Ocean wave / water bubbles."""
    px(img, 6, 7, (100, 200, 255, 200))  # bubble
    px(img, 8, 9, (80, 180, 240, 180))   # bubble
    px(img, 9, 6, (120, 220, 255, 160))  # bubble
    px(img, 7, 8, (255, 255, 255, 140))  # foam
    px(img, 7, 5, (140, 230, 255, 180))  # wave crest


def accent_chronos(img):
    """Clock/time motif - hourglass shape."""
    px(img, 7, 6, (200, 180, 255))       # time glow top
    px(img, 8, 6, (200, 180, 255))       # time glow
    px(img, 7, 8, (180, 160, 240))       # center narrow
    px(img, 8, 8, (180, 160, 240))       # center
    px(img, 7, 10, (160, 140, 220))      # bottom wide
    px(img, 8, 10, (160, 140, 220))      # bottom
    px(img, 6, 6, (220, 200, 255, 140))  # outer glow
    px(img, 9, 10, (220, 200, 255, 140)) # outer glow


def accent_blood_rage(img):
    """Blood splatter / aggressive red accents."""
    px(img, 6, 6, (200, 0, 0))          # splat
    px(img, 9, 7, (180, 0, 0))          # splat
    px(img, 7, 9, (220, 20, 20))        # splat
    px(img, 10, 6, (160, 0, 0, 200))    # drip on glass
    px(img, 10, 7, (140, 0, 0, 160))    # drip fading
    px(img, 8, 5, (240, 40, 40, 180))   # angry glow


# ============================================================
# Potion definitions: (filename, dark, mid, light, highlight, accent_func)
# ============================================================
POTIONS = [
    # === Existing 15 ===
    ("potion_inferno",      (180, 60, 0),    (220, 100, 10),   (255, 150, 40),   (255, 200, 80),   accent_inferno),
    ("potion_glacier",      (40, 100, 160),  (70, 150, 210),   (110, 190, 240),  (180, 225, 255),  accent_glacier),
    ("potion_shadow_step",  (20, 5, 35),     (50, 15, 70),     (80, 30, 110),    (120, 50, 160),   accent_shadow_step),
    ("potion_vitality",     (20, 120, 40),   (50, 180, 70),    (90, 220, 110),   (150, 255, 170),  accent_vitality),
    ("potion_void_walk",    (30, 0, 50),     (60, 10, 100),    (90, 30, 150),    (130, 60, 200),   accent_void_walk),
    ("potion_tempest",      (160, 140, 20),  (200, 180, 40),   (240, 220, 70),   (255, 245, 140),  accent_tempest),
    ("potion_berserker",    (120, 10, 10),   (170, 20, 20),    (210, 50, 40),    (240, 90, 70),    accent_berserker),
    ("potion_starlight",    (160, 130, 20),  (200, 170, 40),   (240, 210, 80),   (255, 240, 150),  accent_starlight),
    ("potion_stone_skin",   (80, 78, 70),    (120, 118, 108),  (155, 150, 140),  (185, 180, 170),  accent_stone_skin),
    ("potion_arcane_surge", (60, 20, 120),   (100, 50, 180),   (140, 80, 220),   (180, 130, 255),  accent_arcane_surge),
    ("potion_swiftbrew",    (30, 100, 180),  (60, 150, 220),   (100, 190, 245),  (160, 225, 255),  accent_swiftbrew),
    ("potion_iron_gut",     (120, 95, 50),   (155, 125, 70),   (185, 155, 95),   (215, 190, 130),  accent_iron_gut),
    ("potion_midas_touch",  (160, 120, 0),   (200, 160, 10),   (240, 200, 30),   (255, 230, 80),   accent_midas_touch),
    ("potion_eagle_eye",    (0, 80, 100),    (0, 130, 150),    (20, 180, 200),   (80, 220, 240),   accent_eagle_eye),
    ("potion_undying",      (180, 140, 20),  (220, 180, 40),   (250, 220, 80),   (255, 245, 160),  accent_undying),
    # === New 5 ===
    ("potion_phantom",      (140, 150, 180), (170, 180, 210),  (200, 210, 235),  (235, 240, 255),  accent_phantom),
    ("potion_titan",        (130, 70, 20),   (170, 100, 35),   (210, 140, 55),   (240, 180, 90),   accent_titan),
    ("potion_tidal_wave",   (10, 60, 120),   (20, 110, 170),   (50, 160, 220),   (100, 210, 255),  accent_tidal_wave),
    ("potion_chronos",      (40, 20, 100),   (70, 50, 150),    (110, 80, 200),   (160, 130, 240),  accent_chronos),
    ("potion_blood_rage",   (100, 0, 10),    (150, 10, 20),    (190, 30, 40),    (230, 60, 60),    accent_blood_rage),
]


def main():
    os.makedirs(OUT, exist_ok=True)
    print("Generating 20 alchemy potion textures...")
    for name, dark, mid, light, highlight, accent in POTIONS:
        img = new_img()
        draw_bottle(img, dark, mid, light, highlight, accent)
        save(img, f"{name}.png")
    print(f"\nDone! Generated {len(POTIONS)} potion textures in {OUT}")


if __name__ == "__main__":
    main()
