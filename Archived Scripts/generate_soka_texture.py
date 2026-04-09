"""
Generate 3D model JSON + 16x16 texture for Soka Singing Blade.
Katana with sonic rings. texture_size [16,16] so UV coords = pixel coords.
"""
import json
from PIL import Image

BASE = r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod"
TEX_OUT = BASE + r"\textures\item\soka_singing_blade.png"
MODEL_OUT = BASE + r"\models\item\soka_singing_blade.json"

# ============ TEXTURE ============
img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
px = img.load()

# Material palette - each gets a 4px-wide x 2px-tall zone
zones = {
    # Row 0-1: Blade
    'blade_bright': ((210, 225, 250, 255), 0, 0),
    'blade_main':   ((170, 188, 220, 255), 4, 0),
    'blade_edge':   ((235, 242, 255, 255), 8, 0),
    'blade_dark':   ((130, 148, 185, 255), 12, 0),
    # Row 2-3: Blade tip (slightly brighter)
    'tip_bright': ((230, 240, 255, 255), 0, 2),
    'tip_main':   ((200, 215, 240, 255), 4, 2),
    'tip_edge':   ((245, 250, 255, 255), 8, 2),
    'tip_dark':   ((165, 180, 210, 255), 12, 2),
    # Row 4-5: Guard
    'guard_bright': ((245, 220, 110, 255), 0, 4),
    'guard_main':   ((220, 190, 75, 255),  4, 4),
    'guard_accent': ((255, 235, 140, 255), 8, 4),
    'guard_dark':   ((160, 135, 45, 255),  12, 4),
    # Row 6-7: Handle + Wrap
    'handle_light': ((70, 58, 100, 255),  0, 6),
    'handle_main':  ((45, 38, 70, 255),   4, 6),
    'handle_dark':  ((25, 20, 45, 255),   8, 6),
    'wrap':         ((95, 75, 125, 255),  12, 6),
    # Row 8-9: Pommel
    'pommel_bright': ((235, 210, 100, 255), 0, 8),
    'pommel_main':   ((200, 175, 65, 255),  4, 8),
    'pommel_dark':   ((140, 110, 30, 255),  8, 8),
    # Row 10-11: Sonic rings
    'sonic_bright': ((160, 255, 250, 255), 0, 10),
    'sonic_core':   ((80, 230, 235, 255),  4, 10),
    'sonic_glow':   ((50, 200, 210, 255),  8, 10),
    'sonic_dark':   ((30, 160, 165, 255),  12, 10),
}

for name, (color, cx, cy) in zones.items():
    for dy in range(2):
        for dx in range(4):
            px[cx + dx, cy + dy] = color

img.save(TEX_OUT)
print(f"Texture saved: {TEX_OUT}")

# ============ UV HELPERS ============
def uv(zone_name):
    """Return a 1x1 UV ref for a material zone."""
    _, cx, cy = zones[zone_name]
    return [cx, cy, cx + 1, cy + 1]

def faces(mat_prefix):
    """Standard directional shading faces for a material prefix."""
    return {
        "north": {"uv": uv(f"{mat_prefix}_main"), "texture": "#0"},
        "east":  {"uv": uv(f"{mat_prefix}_dark"), "texture": "#0"},
        "south": {"uv": uv(f"{mat_prefix}_main"), "texture": "#0"},
        "west":  {"uv": uv(f"{mat_prefix}_bright"), "texture": "#0"},
        "up":    {"uv": uv(f"{mat_prefix}_bright"), "texture": "#0"},
        "down":  {"uv": uv(f"{mat_prefix}_dark"), "texture": "#0"},
    }

def blade_faces(prefix="blade"):
    return {
        "north": {"uv": uv(f"{prefix}_main"), "texture": "#0"},
        "east":  {"uv": uv(f"{prefix}_dark"), "texture": "#0"},
        "south": {"uv": uv(f"{prefix}_main"), "texture": "#0"},
        "west":  {"uv": uv(f"{prefix}_bright"), "texture": "#0"},
        "up":    {"uv": uv(f"{prefix}_edge"), "texture": "#0"},
        "down":  {"uv": uv(f"{prefix}_dark"), "texture": "#0"},
    }

def guard_faces():
    return {
        "north": {"uv": uv("guard_main"), "texture": "#0"},
        "east":  {"uv": uv("guard_dark"), "texture": "#0"},
        "south": {"uv": uv("guard_main"), "texture": "#0"},
        "west":  {"uv": uv("guard_bright"), "texture": "#0"},
        "up":    {"uv": uv("guard_accent"), "texture": "#0"},
        "down":  {"uv": uv("guard_dark"), "texture": "#0"},
    }

def handle_faces():
    return {
        "north": {"uv": uv("handle_main"), "texture": "#0"},
        "east":  {"uv": uv("handle_dark"), "texture": "#0"},
        "south": {"uv": uv("handle_main"), "texture": "#0"},
        "west":  {"uv": uv("handle_light"), "texture": "#0"},
        "up":    {"uv": uv("handle_light"), "texture": "#0"},
        "down":  {"uv": uv("handle_dark"), "texture": "#0"},
    }

def wrap_faces():
    w = uv("wrap")
    return {
        "north": {"uv": w, "texture": "#0"},
        "east":  {"uv": w, "texture": "#0"},
        "south": {"uv": w, "texture": "#0"},
        "west":  {"uv": w, "texture": "#0"},
        "up":    {"uv": w, "texture": "#0"},
        "down":  {"uv": w, "texture": "#0"},
    }

def sonic_faces():
    return {
        "north": {"uv": uv("sonic_core"), "texture": "#0"},
        "east":  {"uv": uv("sonic_dark"), "texture": "#0"},
        "south": {"uv": uv("sonic_core"), "texture": "#0"},
        "west":  {"uv": uv("sonic_bright"), "texture": "#0"},
        "up":    {"uv": uv("sonic_bright"), "texture": "#0"},
        "down":  {"uv": uv("sonic_glow"), "texture": "#0"},
    }

ROT = {"angle": 45, "axis": "x", "origin": [8, 8, 8]}

# ============ MODEL ============
model = {
    "credit": "MegaMod - Soka Singing Blade",
    "texture_size": [16, 16],
    "textures": {
        "0": "megamod:item/soka_singing_blade",
        "particle": "megamod:item/soka_singing_blade"
    },
    "elements": [
        # 1. Pommel
        {
            "name": "pommel",
            "from": [6.5, 1, 6.5], "to": [9.5, 3, 9.5],
            "rotation": ROT,
            "faces": faces("pommel")
        },
        # 2. Handle
        {
            "name": "handle",
            "from": [7.2, 3, 7.2], "to": [8.8, 10, 8.8],
            "rotation": ROT,
            "faces": handle_faces()
        },
        # 3. Handle wrap lower
        {
            "name": "handle_wrap_lower",
            "from": [7, 5, 7], "to": [9, 6.4, 9],
            "rotation": ROT,
            "faces": wrap_faces()
        },
        # 4. Handle wrap upper
        {
            "name": "handle_wrap_upper",
            "from": [7, 7.6, 7], "to": [9, 9, 9],
            "rotation": ROT,
            "faces": wrap_faces()
        },
        # 5. Guard (tsuba)
        {
            "name": "guard",
            "from": [4, 10, 6.5], "to": [12, 11.5, 9.5],
            "rotation": ROT,
            "faces": guard_faces()
        },
        # 6. Blade base
        {
            "name": "blade_base",
            "from": [6.8, 11.5, 6.8], "to": [9.2, 18.5, 9.2],
            "rotation": ROT,
            "faces": blade_faces("blade")
        },
        # 7. Blade upper
        {
            "name": "blade_upper",
            "from": [7.2, 18.5, 7.2], "to": [8.8, 26.5, 8.8],
            "rotation": ROT,
            "faces": blade_faces("blade")
        },
        # 8. Blade tip
        {
            "name": "blade_tip",
            "from": [7.5, 26.5, 7.5], "to": [8.5, 32, 8.5],
            "rotation": ROT,
            "faces": blade_faces("tip")
        },
        # 9. Sonic ring lower (near blade base)
        {
            "name": "sonic_ring_lower",
            "from": [4, 15, 6], "to": [12, 15.4, 10],
            "rotation": {"angle": 45, "axis": "x", "origin": [8, 15.2, 8]},
            "faces": sonic_faces()
        },
        # 10. Sonic ring upper (near blade tip)
        {
            "name": "sonic_ring_upper",
            "from": [5, 24, 6.5], "to": [11, 24.4, 9.5],
            "rotation": {"angle": 45, "axis": "x", "origin": [8, 24.2, 8]},
            "faces": sonic_faces()
        },
    ],
    "gui_light": "front",
    "display": {
        "thirdperson_righthand": {
            "translation": [0, 3, 2]
        },
        "thirdperson_lefthand": {
            "translation": [0, 3, 2]
        },
        "firstperson_righthand": {
            "rotation": [0, -20, -20],
            "translation": [4, -1, 0],
            "scale": [1.1, 1.25, 1.25]
        },
        "firstperson_lefthand": {
            "rotation": [0, -20, -20],
            "translation": [4, -1, 0],
            "scale": [1.1, 1.25, 1.25]
        },
        "ground": {
            "rotation": [45, 0, 0],
            "translation": [0, 2, 0],
            "scale": [0.75, 0.75, 0.75]
        },
        "gui": {
            "rotation": [90, -135, 90],
            "translation": [-4, -4.5, 0],
            "scale": [0.94, 0.94, 0.94]
        },
        "fixed": {
            "rotation": [90, -45, 90],
            "translation": [5.5, -5.5, 0]
        }
    },
    "groups": [
        {"name": "handle", "origin": [8, 8, 8], "color": 0, "children": [0, 1, 2, 3]},
        {"name": "guard", "origin": [8, 11, 8], "color": 0, "children": [4]},
        {"name": "blade", "origin": [8, 22, 8], "color": 0, "children": [5, 6, 7]},
        {"name": "sonic_rings", "origin": [8, 20, 8], "color": 0, "children": [8, 9]}
    ]
}

with open(MODEL_OUT, 'w') as f:
    json.dump(model, f, indent='\t')

print(f"Model saved: {MODEL_OUT}")
print(f"Elements: {len(model['elements'])}, Texture: 16x16")
