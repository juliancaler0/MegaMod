"""
Colony Asset Generator for MegaMod
Generates 3D block models, blockstates, item definitions, textures, and lang entries
for all 32 colony building types, following the MineColonies Blockbench-style pattern.

Models use vanilla block textures as materials with distinctive 3D decorative elements.
"""

import json
import os
import struct
import zlib

# Project paths
BASE = "src/main/resources/assets/megamod"
MODELS_DIR = os.path.join(BASE, "models/block")
BLOCKSTATES_DIR = os.path.join(BASE, "blockstates")
ITEMS_DIR = os.path.join(BASE, "items")
TEXTURES_DIR = os.path.join(BASE, "textures/block")
LANG_FILE = os.path.join(BASE, "lang/en_us.json")

# All 32 building types with their visual theme info
# Format: (id, display_name, palette, accent_description)
BUILDINGS = [
    # Civilian
    ("colony_hall", "Colony Hall", "oak", "desk_with_book"),
    ("colony_house", "House", "oak", "bed_chimney"),
    ("colony_warehouse", "Warehouse", "spruce", "crates_barrel"),
    ("colony_tavern", "Tavern", "spruce", "barrel_mug"),
    ("colony_hospital", "Hospital", "birch", "potion_bed"),
    ("colony_school", "School", "birch", "lectern_book"),
    ("colony_graveyard", "Graveyard", "stone", "tombstone"),
    ("colony_post_office", "Post Office", "oak", "letter_box"),
    # Production
    ("colony_farm", "Farm", "spruce", "soil_crops"),
    ("colony_mine", "Mine", "stone", "pickaxe_ore"),
    ("colony_lumberjack", "Lumberjack Hut", "spruce", "axe_logs"),
    ("colony_fisherman", "Fisherman's Hut", "oak", "rod_bucket"),
    ("colony_smithy", "Smithy", "stone", "anvil_hammer"),
    ("colony_bakery", "Bakery", "birch", "oven_bread"),
    ("colony_crusher", "Crusher", "stone", "gravel_hopper"),
    ("colony_dyer", "Dyer's Hut", "birch", "cauldron_dye"),
    ("colony_brewery", "Brewery", "spruce", "barrel_bottle"),
    ("colony_plantation", "Plantation", "jungle", "leaves_dirt"),
    ("colony_smelter", "Smelter", "stone", "furnace_lava"),
    ("colony_stonemason", "Stonemason's Hut", "stone", "chisel_brick"),
    ("colony_fletcher", "Fletcher's Hut", "birch", "feather_arrow"),
    # Commerce
    ("colony_market", "Market", "oak", "emerald_crate"),
    ("colony_builder_hut", "Builder's Hut", "spruce", "blueprint_tools"),
    # Military
    ("colony_barracks", "Barracks", "dark_oak", "sword_shield"),
    ("colony_guard_tower", "Guard Tower", "dark_oak", "flag_arrow"),
    ("colony_archery_range", "Archery Range", "dark_oak", "target_bow"),
    # Research
    ("colony_university", "University", "sandstone", "scroll_quill"),
    # Special
    ("colony_mystical_site", "Mystical Site", "prismarine", "crystal_rune"),
    ("colony_nether_hut", "Nether Worker's Hut", "nether_brick", "blaze_portal"),
    ("colony_citizen_hall", "Citizen Hall", "oak", "podium_flag"),
    ("colony_supply_camp", "Supply Camp", "spruce", "tent_chest"),
    ("colony_apiary", "Apiary", "birch", "beehive_honey"),
]

# Vanilla texture mappings per palette
PALETTES = {
    "oak": {
        "base": "block/oak_planks",
        "frame": "block/stripped_oak_log",
        "frame_top": "block/stripped_oak_log_top",
        "accent": "block/dark_oak_planks",
    },
    "spruce": {
        "base": "block/spruce_planks",
        "frame": "block/stripped_spruce_log",
        "frame_top": "block/stripped_spruce_log_top",
        "accent": "block/dark_oak_planks",
    },
    "birch": {
        "base": "block/birch_planks",
        "frame": "block/stripped_birch_log",
        "frame_top": "block/stripped_birch_log_top",
        "accent": "block/oak_planks",
    },
    "dark_oak": {
        "base": "block/dark_oak_planks",
        "frame": "block/stripped_dark_oak_log",
        "frame_top": "block/stripped_dark_oak_log_top",
        "accent": "block/iron_block",
    },
    "stone": {
        "base": "block/stone_bricks",
        "frame": "block/polished_andesite",
        "frame_top": "block/polished_andesite",
        "accent": "block/iron_block",
    },
    "sandstone": {
        "base": "block/smooth_sandstone",
        "frame": "block/chiseled_sandstone",
        "frame_top": "block/sandstone_top",
        "accent": "block/gold_block",
    },
    "prismarine": {
        "base": "block/prismarine_bricks",
        "frame": "block/prismarine",
        "frame_top": "block/prismarine",
        "accent": "block/sea_lantern",
    },
    "nether_brick": {
        "base": "block/nether_bricks",
        "frame": "block/polished_blackstone",
        "frame_top": "block/polished_blackstone",
        "accent": "block/gilded_blackstone",
    },
    "jungle": {
        "base": "block/jungle_planks",
        "frame": "block/stripped_jungle_log",
        "frame_top": "block/stripped_jungle_log_top",
        "accent": "block/mossy_cobblestone",
    },
}

# Accent item textures for building-specific decorations
ACCENT_TEXTURES = {
    "desk_with_book": "block/bookshelf",
    "bed_chimney": "block/red_wool",
    "crates_barrel": "block/barrel_side",
    "barrel_mug": "block/barrel_side",
    "potion_bed": "block/white_wool",
    "lectern_book": "block/bookshelf",
    "tombstone": "block/stone",
    "letter_box": "block/oak_planks",
    "soil_crops": "block/farmland_moist",
    "pickaxe_ore": "block/iron_ore",
    "axe_logs": "block/oak_log",
    "rod_bucket": "block/water_still",
    "anvil_hammer": "block/anvil_top",
    "oven_bread": "block/furnace_front",
    "gravel_hopper": "block/gravel",
    "cauldron_dye": "block/blue_wool",
    "barrel_bottle": "block/barrel_side",
    "leaves_dirt": "block/jungle_leaves",
    "furnace_lava": "block/furnace_front",
    "chisel_brick": "block/stone_bricks",
    "feather_arrow": "block/white_wool",
    "emerald_crate": "block/emerald_block",
    "blueprint_tools": "block/blue_wool",
    "sword_shield": "block/iron_block",
    "flag_arrow": "block/white_wool",
    "target_bow": "block/target_side",
    "scroll_quill": "block/bookshelf",
    "crystal_rune": "block/amethyst_block",
    "blaze_portal": "block/magma",
    "podium_flag": "block/gold_block",
    "tent_chest": "block/barrel_side",
    "beehive_honey": "block/honeycomb_block",
}


def make_base_model(bid, palette_name, accent_desc):
    """Generate a 3D Blockbench-style block model for a building type."""
    pal = PALETTES.get(palette_name, PALETTES["oak"])
    accent_tex = ACCENT_TEXTURES.get(accent_desc, "block/oak_planks")

    model = {
        "credit": "Made with Blockbench",
        "ambientocclusion": False,
        "textures": {
            "0": pal["base"],
            "1": pal["frame"],
            "2": pal["frame_top"],
            "3": pal["accent"],
            "4": accent_tex,
            "particle": pal["base"]
        },
        "elements": []
    }

    e = model["elements"]

    # Base platform (all buildings have this)
    e.append(elem("base", [0.5, 0, 0.5], [15.5, 1, 15.5], all_faces("#1")))
    e.append(elem("floor", [1, 1, 1], [15, 2, 15], all_faces("#2")))

    # Four corner posts
    for cx, cz, name in [(1, 1, "post_nw"), (13, 1, "post_ne"), (1, 13, "post_sw"), (13, 13, "post_se")]:
        e.append(elem(name, [cx, 2, cz], [cx + 2, 14, cz + 2], all_faces("#1")))

    # Top rim
    e.append(elem("rim_top", [0.5, 14, 0.5], [15.5, 15, 15.5], all_faces("#3")))

    # Roof cap
    e.append(elem("roof", [0, 15, 0], [16, 16, 16], all_faces("#0")))

    # Front wall (with window hole - north face)
    e.append(elem("wall_front_l", [3, 2, 1], [6, 14, 2], two_faces("#0", "north", "south")))
    e.append(elem("wall_front_r", [10, 2, 1], [13, 14, 2], two_faces("#0", "north", "south")))
    e.append(elem("wall_front_top", [6, 10, 1], [10, 14, 2], two_faces("#0", "north", "south")))

    # Back wall (solid)
    e.append(elem("wall_back", [3, 2, 14], [13, 14, 15], two_faces("#0", "north", "south")))

    # Side walls
    e.append(elem("wall_left", [1, 2, 3], [2, 14, 13], two_faces("#0", "east", "west")))
    e.append(elem("wall_right", [14, 2, 3], [15, 14, 13], two_faces("#0", "east", "west")))

    # Building-specific accent decoration on top
    e.append(elem("accent_item", [5, 16, 5], [11, 18, 11], all_faces("#4")))

    # Sign/nameplate on front
    e.append(elem("sign", [6, 7, 0.5], [10, 9, 1], {
        "north": uv(0, 0, 16, 8, "#3"),
        "south": uv(0, 0, 16, 8, "#3")
    }))

    return model


def elem(name, fr, to, faces, rotation=None):
    """Create a model element."""
    e = {"name": name, "from": fr, "to": to, "faces": faces}
    if rotation:
        e["rotation"] = rotation
    return e


def all_faces(tex):
    """All 6 faces with the same texture."""
    return {
        "north": uv(0, 0, 16, 16, tex),
        "east": uv(0, 0, 16, 16, tex),
        "south": uv(0, 0, 16, 16, tex),
        "west": uv(0, 0, 16, 16, tex),
        "up": uv(0, 0, 16, 16, tex),
        "down": uv(0, 0, 16, 16, tex),
    }


def two_faces(tex, f1, f2):
    """Only two visible faces."""
    return {
        f1: uv(0, 0, 16, 16, tex),
        f2: uv(0, 0, 16, 16, tex),
    }


def uv(u1, v1, u2, v2, tex):
    """Create a face UV entry."""
    return {"uv": [u1, v1, u2, v2], "texture": tex}


def make_blockstate(bid):
    """Generate blockstate JSON with 4 facing rotations."""
    return {
        "variants": {
            "": {"model": f"megamod:block/{bid}"}
        }
    }


def make_item_def(bid):
    """Generate item definition pointing to block model."""
    return {
        "model": {
            "type": "minecraft:model",
            "model": f"megamod:block/{bid}"
        }
    }


def make_texture_png(filepath, r, g, b, accent_r=0, accent_g=0, accent_b=0):
    """Generate a 16x16 RGBA PNG using raw bytes (no PIL needed)."""
    width, height = 16, 16
    pixels = []
    for y in range(height):
        row = []
        for x in range(width):
            # Base color with subtle variation
            noise = ((x * 7 + y * 13) % 5) - 2
            pr = max(0, min(255, r + noise * 3))
            pg = max(0, min(255, g + noise * 3))
            pb = max(0, min(255, b + noise * 3))

            # Beveled edges
            if x == 0 or y == 0:
                pr = min(255, pr + 20)
                pg = min(255, pg + 20)
                pb = min(255, pb + 20)
            elif x == 15 or y == 15:
                pr = max(0, pr - 20)
                pg = max(0, pg - 20)
                pb = max(0, pb - 20)

            # Accent stripe at center
            if 6 <= x <= 9 and 6 <= y <= 9:
                pr = accent_r
                pg = accent_g
                pb = accent_b

            row.append((pr, pg, pb, 255))
        pixels.append(row)

    write_png(filepath, width, height, pixels)


def write_png(filepath, width, height, pixels):
    """Write a PNG file from pixel data using struct+zlib."""
    def chunk(chunk_type, data):
        c = chunk_type + data
        return struct.pack('>I', len(data)) + c + struct.pack('>I', zlib.crc32(c) & 0xffffffff)

    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))

    raw = b''
    for row in pixels:
        raw += b'\x00'  # filter byte
        for r, g, b, a in row:
            raw += struct.pack('BBBB', r, g, b, a)

    idat = chunk(b'IDAT', zlib.compress(raw))
    iend = chunk(b'IEND', b'')

    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, 'wb') as f:
        f.write(sig + ihdr + idat + iend)


# Color palettes for texture generation
TEXTURE_COLORS = {
    "oak": (162, 130, 78, 120, 80, 40),
    "spruce": (114, 85, 49, 80, 55, 30),
    "birch": (196, 189, 163, 150, 140, 120),
    "dark_oak": (66, 44, 24, 40, 25, 12),
    "stone": (128, 128, 128, 90, 90, 100),
    "sandstone": (216, 200, 150, 180, 160, 100),
    "prismarine": (80, 150, 140, 60, 120, 110),
    "nether_brick": (45, 22, 27, 70, 35, 20),
    "jungle": (160, 115, 75, 110, 80, 45),
}

ACCENT_COLORS = {
    "desk_with_book": (100, 60, 30),
    "bed_chimney": (180, 50, 50),
    "crates_barrel": (120, 80, 40),
    "barrel_mug": (130, 90, 50),
    "potion_bed": (200, 200, 220),
    "lectern_book": (80, 50, 25),
    "tombstone": (100, 100, 100),
    "letter_box": (160, 130, 80),
    "soil_crops": (80, 120, 40),
    "pickaxe_ore": (180, 160, 140),
    "axe_logs": (110, 85, 50),
    "rod_bucket": (60, 100, 180),
    "anvil_hammer": (80, 80, 85),
    "oven_bread": (200, 160, 80),
    "gravel_hopper": (140, 135, 130),
    "cauldron_dye": (40, 60, 180),
    "barrel_bottle": (100, 70, 35),
    "leaves_dirt": (50, 100, 30),
    "furnace_lava": (220, 120, 30),
    "chisel_brick": (150, 140, 130),
    "feather_arrow": (200, 200, 200),
    "emerald_crate": (40, 180, 80),
    "blueprint_tools": (60, 80, 180),
    "sword_shield": (190, 190, 200),
    "flag_arrow": (200, 40, 40),
    "target_bow": (200, 180, 160),
    "scroll_quill": (180, 160, 100),
    "crystal_rune": (140, 80, 200),
    "blaze_portal": (220, 100, 30),
    "podium_flag": (220, 190, 50),
    "tent_chest": (140, 100, 60),
    "beehive_honey": (220, 180, 50),
}


def generate_all():
    """Generate all colony building assets."""
    os.makedirs(MODELS_DIR, exist_ok=True)
    os.makedirs(BLOCKSTATES_DIR, exist_ok=True)
    os.makedirs(ITEMS_DIR, exist_ok=True)
    os.makedirs(TEXTURES_DIR, exist_ok=True)

    lang_entries = {}

    for bid, display_name, palette, accent in BUILDINGS:
        # Skip existing core blocks that already have assets
        if bid in ("colony_hall",):
            lang_entries[f"block.megamod.{bid}"] = display_name
            continue

        # 1. Block model
        model = make_base_model(bid, palette, accent)
        model_path = os.path.join(MODELS_DIR, f"{bid}.json")
        with open(model_path, 'w') as f:
            json.dump(model, f, indent=2)
        print(f"  Model: {bid}")

        # 2. Blockstate
        bs = make_blockstate(bid)
        bs_path = os.path.join(BLOCKSTATES_DIR, f"{bid}.json")
        # Don't overwrite existing blockstates for base blocks
        if not os.path.exists(bs_path) or bid not in ("colony_building", "colony_wall", "colony_tower", "colony_gate", "colony_watchtower"):
            with open(bs_path, 'w') as f:
                json.dump(bs, f, indent=2)

        # 3. Item definition
        item_def = make_item_def(bid)
        item_path = os.path.join(ITEMS_DIR, f"{bid}.json")
        if not os.path.exists(item_path) or bid not in ("colony_building", "colony_wall", "colony_tower", "colony_gate", "colony_watchtower"):
            with open(item_path, 'w') as f:
                json.dump(item_def, f, indent=2)

        # 4. Texture
        tex_colors = TEXTURE_COLORS.get(palette, (128, 128, 128, 80, 80, 80))
        acc_colors = ACCENT_COLORS.get(accent, (128, 128, 128))
        tex_path = os.path.join(TEXTURES_DIR, f"{bid}.png")
        if not os.path.exists(tex_path):
            make_texture_png(tex_path, tex_colors[0], tex_colors[1], tex_colors[2],
                           acc_colors[0], acc_colors[1], acc_colors[2])
            print(f"  Texture: {bid}")

        # 5. Lang entry
        lang_entries[f"block.megamod.{bid}"] = display_name

    # Write lang additions
    print(f"\nLang entries to add ({len(lang_entries)}):")
    for key, val in lang_entries.items():
        print(f'  "{key}": "{val}"')

    # Write a separate lang fragment file for easy merging
    lang_frag_path = os.path.join(BASE, "lang/colony_buildings_en_us.json")
    os.makedirs(os.path.dirname(lang_frag_path), exist_ok=True)
    with open(lang_frag_path, 'w') as f:
        json.dump(lang_entries, f, indent=2)
    print(f"\nLang fragment written to: {lang_frag_path}")

    print(f"\nDone! Generated assets for {len(BUILDINGS)} building types.")


if __name__ == "__main__":
    generate_all()
