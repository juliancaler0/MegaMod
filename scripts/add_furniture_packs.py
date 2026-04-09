"""
Automated furniture pack importer for MegaMod.
Scans reference asset packs, copies models+textures, creates blockstates/items,
and generates Java registration code for FurnitureRegistry.
"""
import json, os, shutil, re
from pathlib import Path

PROJECT = Path(r"C:\Users\JulianCalero\Desktop\Projects\MegaMod")
SRC_ASSETS = PROJECT / "src/main/resources/assets/megamod"
REF_BASE = PROJECT / "ref assets/Furniture"

# Pack definitions: (directory, prefix, display_category)
PACKS = [
    ("elitecreatures-livingroom_furniture_v1", "lr", "Livingroom"),
    ("magic_store_medieval_full_2/magic_store_mcmodel_medieval_full_2", "ms2", "Magic Store 2"),
    ("magic_store_medieval_full_4/magic_store_mcmodel_medieval_full_4", "ms4", "Magic Store 4"),
    ("MarketFurnitureSet-Updated", "mkt", "Market"),
    ("Park_plus_ItemsAdder", "park", "Park"),
    ("W6 - Crafting Station Bundle", "craft", "Crafting Station"),
]

# Output dirs
MODELS_DIR = SRC_ASSETS / "models/block"
TEXTURES_DIR = SRC_ASSETS / "textures/block"
BLOCKSTATES_DIR = SRC_ASSETS / "blockstates"
ITEMS_DIR = SRC_ASSETS / "items"

for d in [MODELS_DIR, TEXTURES_DIR, BLOCKSTATES_DIR, ITEMS_DIR]:
    d.mkdir(parents=True, exist_ok=True)

all_items = []  # (registry_name, CONSTANT_NAME, category)
texture_map = {}  # original_ref -> new_name (for dedup)

def clean_name(name):
    """Convert a model filename to a clean registry name."""
    name = name.lower().replace(" ", "_").replace("-", "_")
    name = re.sub(r'[^a-z0-9_]', '', name)
    name = re.sub(r'_+', '_', name).strip('_')
    return name

def find_models(pack_dir):
    """Find all model JSON files in a pack directory."""
    models = []
    for root, dirs, files in os.walk(pack_dir):
        for f in files:
            if f.endswith('.json') and 'models' in root.replace('\\', '/'):
                models.append(Path(root) / f)
    return models

def find_textures_dir(pack_dir):
    """Find the textures directory in a pack."""
    for root, dirs, files in os.walk(pack_dir):
        if 'textures' in os.path.basename(root).lower():
            return Path(root)
        for d in dirs:
            if d.lower() == 'textures':
                return Path(root) / d
    return None

def resolve_texture(tex_ref, textures_base, pack_dir):
    """Find the actual PNG file for a texture reference like 'namespace:path/to/texture'."""
    # Strip namespace if present
    if ':' in tex_ref:
        tex_ref = tex_ref.split(':', 1)[1]

    # Search for the PNG
    candidates = [
        textures_base / (tex_ref + ".png") if textures_base else None,
    ]

    # Also search recursively in the pack dir
    tex_filename = os.path.basename(tex_ref) + ".png"
    for root, dirs, files in os.walk(pack_dir):
        for f in files:
            if f == tex_filename:
                candidates.append(Path(root) / f)

    for c in candidates:
        if c and c.exists():
            return c
    return None

def process_model(model_path, prefix, pack_dir):
    """Process a single model file. Returns (registry_name, success)."""
    stem = model_path.stem
    registry_name = f"{prefix}_{clean_name(stem)}"

    # Skip if already exists
    if (MODELS_DIR / f"{registry_name}.json").exists():
        return registry_name, False

    try:
        with open(model_path, 'r', encoding='utf-8') as f:
            model_data = json.load(f)
    except (json.JSONDecodeError, UnicodeDecodeError):
        print(f"  SKIP (bad JSON): {model_path.name}")
        return registry_name, False

    # Skip if no elements (might be a parent-reference model, not standalone)
    if 'elements' not in model_data:
        # Check if it has a parent reference - skip those
        if 'parent' in model_data:
            print(f"  SKIP (parent ref): {model_path.name}")
            return registry_name, False

    textures_base = find_textures_dir(pack_dir)

    # Process textures
    new_textures = {}
    if 'textures' in model_data:
        for key, tex_ref in model_data['textures'].items():
            if tex_ref.startswith('#'):
                new_textures[key] = tex_ref
                continue

            # Find and copy the texture file
            tex_file = resolve_texture(tex_ref, textures_base, pack_dir)
            if tex_file and tex_file.exists():
                # Create a clean texture name
                tex_clean = f"{prefix}_{clean_name(tex_file.stem)}"
                dest = TEXTURES_DIR / f"{tex_clean}.png"
                if not dest.exists():
                    shutil.copy2(tex_file, dest)
                new_textures[key] = f"megamod:block/{tex_clean}"
            else:
                # Texture not found, use a fallback
                print(f"  WARN: texture not found for '{tex_ref}' in {model_path.name}")
                new_textures[key] = tex_ref  # Keep original (will show missing texture)

        model_data['textures'] = new_textures

    # Add display transforms if missing
    if 'display' not in model_data:
        model_data['display'] = {
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
            "ground": {"translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
            "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]}
        }

    # Write model
    with open(MODELS_DIR / f"{registry_name}.json", 'w', encoding='utf-8') as f:
        json.dump(model_data, f, indent='\t')

    # Write blockstate
    blockstate = {
        "variants": {
            "facing=north": {"model": f"megamod:block/{registry_name}"},
            "facing=south": {"model": f"megamod:block/{registry_name}", "y": 180},
            "facing=east": {"model": f"megamod:block/{registry_name}", "y": 90},
            "facing=west": {"model": f"megamod:block/{registry_name}", "y": 270}
        }
    }
    with open(BLOCKSTATES_DIR / f"{registry_name}.json", 'w', encoding='utf-8') as f:
        json.dump(blockstate, f, indent=2)

    # Write item definition
    item_def = {"model": {"type": "minecraft:model", "model": f"megamod:block/{registry_name}"}}
    with open(ITEMS_DIR / f"{registry_name}.json", 'w', encoding='utf-8') as f:
        json.dump(item_def, f)

    return registry_name, True

# Main processing
java_lines = []
creative_tab_lines = []
total_added = 0

for pack_dir_rel, prefix, category in PACKS:
    pack_dir = REF_BASE / pack_dir_rel
    if not pack_dir.exists():
        print(f"SKIP: {pack_dir} not found")
        continue

    print(f"\n=== Processing {category} ({prefix}) ===")
    models = find_models(pack_dir)
    print(f"  Found {len(models)} model files")

    pack_items = []
    for model_path in sorted(models):
        registry_name, success = process_model(model_path, prefix, pack_dir)
        if success:
            const_name = registry_name.upper()
            java_lines.append(f'    public static final DeferredBlock<FurnitureBlock> {const_name} = BLOCKS.registerBlock("{registry_name}", FurnitureBlock::new, () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5f).noOcclusion());')
            java_lines.append(f'    public static final DeferredItem<BlockItem> {const_name}_ITEM = ITEMS.registerSimpleBlockItem("{registry_name}", {const_name});')
            java_lines.append('')
            creative_tab_lines.append(f'        output.accept((ItemLike){const_name}.get());')
            pack_items.append(registry_name)
            total_added += 1

    print(f"  Added {len(pack_items)} items")

# Write Java registration code to a file
with open(PROJECT / "generated_furniture_registration.java", 'w') as f:
    f.write(f"// === GENERATED FURNITURE REGISTRATION ({total_added} items) ===\n")
    f.write("// Add these to FurnitureRegistry.java\n\n")
    f.write("// --- Block + Item registrations ---\n")
    for line in java_lines:
        f.write(line + '\n')
    f.write("\n// --- Creative tab entries (add to acceptItems method) ---\n")
    for line in creative_tab_lines:
        f.write(line + '\n')

print(f"\n=== DONE: {total_added} furniture items added ===")
print(f"Generated registration code: generated_furniture_registration.java")
print(f"Models: {MODELS_DIR}")
print(f"Textures: {TEXTURES_DIR}")
print(f"Blockstates: {BLOCKSTATES_DIR}")
print(f"Items: {ITEMS_DIR}")
