"""
Migrate recipe JSONs from MC 1.21.1 ingredient format to 1.21.2+ format.

Old: {"item": "minecraft:diamond"}  -> "minecraft:diamond"
Old: {"tag":  "minecraft:wool"}     -> "#minecraft:wool"

Only ingredient positions are touched. Result fields (result.id / result.count)
are never modified. NeoForge custom ingredients (objects carrying
`neoforge:ingredient_type`) are preserved verbatim.

Shaped:    key.<L>           (single obj, or list of objs)
Shapeless: ingredients[i]
Furnace/Blasting/Smoking/Campfire/Stonecutting: ingredient
Smithing (transform/trim): template, base, addition
"""

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src" / "main" / "resources" / "data"
NAMESPACES = ["megamod", "reliquary", "skill_tree_rpgs"]

INGREDIENT_FIELDS_SINGLE = ("ingredient", "template", "base", "addition", "input")
INGREDIENT_FIELDS_LIST = ("ingredients",)


def convert_one(val):
    """Convert one ingredient value. Returns the new value (possibly unchanged)."""
    # Already migrated: bare string
    if isinstance(val, str):
        return val

    # Array of ingredients
    if isinstance(val, list):
        return [convert_one(v) for v in val]

    if isinstance(val, dict):
        # NeoForge custom ingredient - leave alone
        if "neoforge:ingredient_type" in val or "type" in val:
            return val

        # Legacy single-item
        if set(val.keys()) == {"item"}:
            return val["item"]

        # Legacy single-tag
        if set(val.keys()) == {"tag"}:
            return "#" + val["tag"]

        # Unknown structure - leave alone (avoid breaking edge cases)
        return val

    return val


def convert_recipe(recipe):
    """Mutate recipe in place. Returns True if anything changed."""
    changed = False

    # Shaped: key is {letter: ingredient}
    if isinstance(recipe.get("key"), dict):
        for letter, val in list(recipe["key"].items()):
            new_val = convert_one(val)
            if new_val != val:
                recipe["key"][letter] = new_val
                changed = True

    # Single ingredient slots
    for field in INGREDIENT_FIELDS_SINGLE:
        if field in recipe:
            new_val = convert_one(recipe[field])
            if new_val != recipe[field]:
                recipe[field] = new_val
                changed = True

    # List ingredient slots
    for field in INGREDIENT_FIELDS_LIST:
        if isinstance(recipe.get(field), list):
            new_list = [convert_one(v) for v in recipe[field]]
            if new_list != recipe[field]:
                recipe[field] = new_list
                changed = True

    return changed


def main():
    total = 0
    changed = 0
    errors = 0

    for ns in NAMESPACES:
        root = DATA / ns / "recipe"
        if not root.exists():
            continue
        for path in root.rglob("*.json"):
            total += 1
            try:
                text = path.read_text(encoding="utf-8")
                recipe = json.loads(text)
            except Exception as e:
                print(f"[ERR ] {path}: {e}", file=sys.stderr)
                errors += 1
                continue

            if convert_recipe(recipe):
                path.write_text(
                    json.dumps(recipe, indent=2, ensure_ascii=False) + "\n",
                    encoding="utf-8",
                )
                changed += 1

    print(f"Scanned {total} recipes. Updated {changed}. Parse errors: {errors}.")
    return 0 if errors == 0 else 2


if __name__ == "__main__":
    sys.exit(main())
