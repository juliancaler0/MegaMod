"""
Generate placeholder 16x16 PNG textures for MegaMod assets.

Uses PIL/Pillow to create solid-color placeholders with a single letter overlay.
Run with: python scripts/generate_placeholder_textures.py

Requires: pip install Pillow
"""

import os
import sys

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("ERROR: Pillow is required. Install with: pip install Pillow")
    sys.exit(1)


BASE_DIR = os.path.join(
    "C:", os.sep, "Users", "JulianCalero", "Desktop", "Projects", "MegaMod",
    "src", "main", "resources", "assets", "megamod", "textures"
)

BLOCK_DIR = os.path.join(BASE_DIR, "block")
ITEM_DIR = os.path.join(BASE_DIR, "item")
CITIZEN_DIR = os.path.join(BASE_DIR, "entity", "citizen")
RAIDER_DIR = os.path.join(BASE_DIR, "entity", "raider")

created_count = 0


def ensure_dir(path):
    os.makedirs(path, exist_ok=True)


def make_placeholder(path, bg_color, letter, letter_color=(255, 255, 255, 255), size=16):
    """Create a 16x16 solid-color PNG with a centered letter."""
    global created_count

    # Skip if file already exists
    if os.path.exists(path):
        return False

    img = Image.new("RGBA", (size, size), bg_color)
    draw = ImageDraw.Draw(img)

    # Use default bitmap font (always available, no external font file needed)
    try:
        font = ImageFont.load_default()
    except Exception:
        font = None

    # Center the letter
    if font:
        bbox = draw.textbbox((0, 0), letter, font=font)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        tx = (size - tw) // 2
        ty = (size - th) // 2
        draw.text((tx, ty), letter, fill=letter_color, font=font)
    else:
        # Fallback: just draw a small rectangle in the center
        cx, cy = size // 2, size // 2
        draw.rectangle([cx - 2, cy - 2, cx + 2, cy + 2], fill=letter_color)

    img.save(path, "PNG")
    created_count += 1
    return True


def generate_hut_blocks():
    """50 hut blocks - brown with white H."""
    ensure_dir(BLOCK_DIR)

    hut_names = [
        "hut_archery", "hut_baker", "hut_barracks", "hut_barracks_tower",
        "hut_beekeeper", "hut_blacksmith", "hut_builder", "hut_chicken_farmer",
        "hut_citizen", "hut_combatacademy", "hut_composter", "hut_concretemixer",
        "hut_cook", "hut_cowboy", "hut_crusher", "hut_deliveryman",
        "hut_dyer", "hut_enchanter", "hut_farmer", "hut_fisherman",
        "hut_fletcher", "hut_florist", "hut_glassblower", "hut_graveyard",
        "hut_guardtower", "hut_hospital", "hut_library", "hut_lumberjack",
        "hut_mechanic", "hut_miner", "hut_mystical_site", "hut_netherworker",
        "hut_plantation", "hut_rabbit_farmer", "hut_residence", "hut_sawmill",
        "hut_school", "hut_shepherd", "hut_sifter", "hut_smeltery",
        "hut_stonemason", "hut_stone_smeltery", "hut_swineherd", "hut_tavern",
        "hut_townhall", "hut_university", "hut_warehouse",
        "hut_cattle_farmer", "hut_goat_farmer", "hut_merchant",
    ]

    brown = (139, 90, 43, 255)
    count = 0
    for name in hut_names:
        path = os.path.join(BLOCK_DIR, name + ".png")
        if make_placeholder(path, brown, "H"):
            count += 1
    print(f"  Hut blocks: {count} created ({len(hut_names)} total)")


def generate_raider_entities():
    """18 raider entities - red with culture initial."""
    ensure_dir(RAIDER_DIR)

    raiders = {
        # Barbarians (B)
        "raider_barbarian": "B", "raider_archer_barbarian": "B", "raider_chief_barbarian": "B",
        # Pirates (P)
        "raider_pirate": "P", "raider_archer_pirate": "P", "raider_captain_pirate": "P",
        # Egyptians (E)
        "raider_mummy": "E", "raider_archer_mummy": "E", "raider_pharao": "E",
        # Norsemen (N)
        "raider_shieldmaiden": "N", "raider_norsemen_archer": "N", "raider_norsemen_chief": "N",
        # Amazons (A)
        "raider_amazon_spearman": "A", "raider_archer_amazon": "A", "raider_amazon_chief": "A",
        # Drowned Pirates (D)
        "raider_drowned_pirate": "D", "raider_drowned_archer_pirate": "D",
        "raider_drowned_captain_pirate": "D",
    }

    red = (180, 40, 40, 255)
    count = 0
    for name, initial in raiders.items():
        path = os.path.join(RAIDER_DIR, name + ".png")
        if make_placeholder(path, red, initial):
            count += 1
    print(f"  Raider entities: {count} created ({len(raiders)} total)")


def generate_blueprint_tools():
    """4 blueprint tools - blue with tool initial."""
    ensure_dir(ITEM_DIR)

    tools = {
        "build_tool": "B",
        "scan_tool": "S",
        "tag_tool": "T",
        "shape_tool": "P",
    }

    blue = (50, 80, 180, 255)
    count = 0
    for name, initial in tools.items():
        path = os.path.join(ITEM_DIR, name + ".png")
        if make_placeholder(path, blue, initial):
            count += 1
    print(f"  Blueprint tools: {count} created ({len(tools)} total)")


def generate_crops():
    """15 crop textures - green with crop initial."""
    ensure_dir(BLOCK_DIR)

    crops = {
        "crop_barley": "B", "crop_bell_pepper": "P", "crop_cabbage": "C",
        "crop_chickpea": "K", "crop_corn": "N", "crop_durum": "D",
        "crop_eggplant": "E", "crop_garlic": "G", "crop_oat": "O",
        "crop_onion": "I", "crop_peas": "A", "crop_rice": "R",
        "crop_soybean": "S", "crop_tomato": "T", "crop_turnip": "U",
    }

    green = (50, 140, 50, 255)
    count = 0
    for name, initial in crops.items():
        path = os.path.join(BLOCK_DIR, name + ".png")
        if make_placeholder(path, green, initial):
            count += 1
    print(f"  Crop blocks: {count} created ({len(crops)} total)")


def generate_ornament_blocks():
    """20 ornament blocks - purple with O."""
    ensure_dir(BLOCK_DIR)

    ornaments = [
        "ornament_barrel", "ornament_bookshelf", "ornament_crate",
        "ornament_clay_pot", "ornament_flower_pot_large", "ornament_hay_bale",
        "ornament_lantern_post", "ornament_market_stall", "ornament_rain_catcher",
        "ornament_scarecrow", "ornament_sack", "ornament_trough",
        "ornament_water_well", "ornament_wheel", "ornament_windmill_blade",
        "ornament_wine_barrel", "ornament_wood_pile", "ornament_fence_gate_colony",
        "ornament_colony_lamp", "ornament_banner_stand",
    ]

    purple = (130, 50, 160, 255)
    count = 0
    for name in ornaments:
        path = os.path.join(BLOCK_DIR, name + ".png")
        if make_placeholder(path, purple, "O"):
            count += 1
    print(f"  Ornament blocks: {count} created ({len(ornaments)} total)")


def generate_multi_piston():
    """Multi-piston block - gray with P."""
    ensure_dir(BLOCK_DIR)

    gray = (120, 120, 120, 255)
    count = 0
    for name in ["multi_piston", "multi_piston_head", "multi_piston_side"]:
        path = os.path.join(BLOCK_DIR, name + ".png")
        if make_placeholder(path, gray, "P"):
            count += 1
    print(f"  Multi-piston: {count} created")


def generate_utility_blocks():
    """Utility blocks - yellow."""
    ensure_dir(BLOCK_DIR)

    utility = [
        "town_chest", "colony_flag", "colony_waypoint", "colony_postbox",
        "colony_stash", "colony_warehouse_shelf", "trading_terminal",
        "atm_block", "computer_block", "questboard_block",
    ]

    yellow = (200, 180, 50, 255)
    count = 0
    for name in utility:
        path = os.path.join(BLOCK_DIR, name + ".png")
        if make_placeholder(path, yellow, "U"):
            count += 1
    print(f"  Utility blocks: {count} created ({len(utility)} total)")


def generate_citizen_job_textures():
    """Citizen job textures that may be missing (including visitor)."""
    ensure_dir(CITIZEN_DIR)

    jobs = [
        "visitor", "baker", "cook", "deliveryman", "enchanter",
        "fletcher", "florist", "glassblower", "composter",
        "concretemixer", "crusher", "dyer", "mechanic",
        "netherworker", "plantation_worker", "sawmill_worker",
        "school_teacher", "sifter", "smelter", "stonemason",
    ]

    tan = (180, 150, 100, 255)
    count = 0
    for name in jobs:
        path = os.path.join(CITIZEN_DIR, name + ".png")
        if make_placeholder(path, tan, name[0].upper()):
            count += 1
    print(f"  Citizen jobs: {count} created ({len(jobs)} total)")


def main():
    global created_count

    print("=" * 60)
    print("MegaMod Placeholder Texture Generator")
    print("=" * 60)
    print(f"Output base: {BASE_DIR}")
    print()

    generate_hut_blocks()
    generate_raider_entities()
    generate_blueprint_tools()
    generate_crops()
    generate_ornament_blocks()
    generate_multi_piston()
    generate_utility_blocks()
    generate_citizen_job_textures()

    print()
    print("-" * 60)
    print(f"TOTAL: {created_count} new placeholder textures created.")
    print("(Existing files were skipped.)")
    print("=" * 60)


if __name__ == "__main__":
    main()
