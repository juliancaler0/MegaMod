"""
Regenerate all backpack block and item models to use the 3D backpack_base geometry.

Block models: parent -> megamod:block/backpack_base, texture -> megamod:block/backpack/<variant>
Item models: parent -> megamod:block/<variant>_backpack (inherits 3D shape from block model)
Also updates backpack_block.json (the single registered block) to use the base model.
"""

import json
import os

VARIANTS = [
    "bat", "bee", "blaze", "bookshelf", "cactus", "cake", "chicken", "coal",
    "cow", "creeper", "diamond", "dragon", "emerald", "enderman", "fox",
    "ghast", "gold", "hay", "horse", "iron", "iron_golem", "lapis",
    "magma_cube", "melon", "netherite", "ocelot", "pig", "pumpkin", "quartz",
    "redstone", "sandstone", "sheep", "skeleton", "snow", "spider", "sponge",
    "squid", "standard", "villager", "warden", "wither", "wolf"
]

BASE_DIR = os.path.join(
    "C:", os.sep, "Users", "JulianCalero", "Desktop", "Projects", "MegaMod",
    "src", "main", "resources", "assets", "megamod"
)

BLOCK_MODEL_DIR = os.path.join(BASE_DIR, "models", "block")
ITEM_MODEL_DIR = os.path.join(BASE_DIR, "models", "item")
ITEMS_DIR = os.path.join(BASE_DIR, "items")


def write_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main():
    count_block = 0
    count_item = 0
    count_items_def = 0

    # Generate variant block models
    for variant in VARIANTS:
        block_model = {
            "parent": "megamod:block/backpack_base",
            "textures": {
                "0": f"megamod:block/backpack/{variant}",
                "particle": f"megamod:block/backpack/{variant}"
            }
        }
        path = os.path.join(BLOCK_MODEL_DIR, f"{variant}_backpack.json")
        write_json(path, block_model)
        count_block += 1

    # Update backpack_block.json (the single registered block)
    backpack_block = {
        "parent": "megamod:block/backpack_base",
        "textures": {
            "0": "megamod:block/backpack/standard",
            "particle": "megamod:block/backpack/standard"
        }
    }
    write_json(os.path.join(BLOCK_MODEL_DIR, "backpack_block.json"), backpack_block)
    print(f"Updated backpack_block.json")

    # Generate variant item models (reference block model for 3D shape)
    for variant in VARIANTS:
        item_model = {
            "parent": f"megamod:block/{variant}_backpack"
        }
        path = os.path.join(ITEM_MODEL_DIR, f"{variant}_backpack.json")
        write_json(path, item_model)
        count_item += 1

    # Update items definition files to reference the new item models
    for variant in VARIANTS:
        items_def = {
            "model": {
                "type": "minecraft:model",
                "model": f"megamod:item/{variant}_backpack"
            }
        }
        path = os.path.join(ITEMS_DIR, f"{variant}_backpack.json")
        write_json(path, items_def)
        count_items_def += 1

    print(f"Generated {count_block} block models in {BLOCK_MODEL_DIR}")
    print(f"Generated {count_item} item models in {ITEM_MODEL_DIR}")
    print(f"Updated {count_items_def} items definitions in {ITEMS_DIR}")
    print(f"Total variants: {len(VARIANTS)}")


if __name__ == "__main__":
    main()
