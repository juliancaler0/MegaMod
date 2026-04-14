"""Add English lang keys for relic/accessory custom spells.

Reads RelicSpellAssignments.java for authoritative SPELLS (relic_key -> spell_id list)
and META (spell_id -> ability display name) maps, then appends .name and .description
keys to assets/megamod/lang/en_us.json for every spell ID found.
"""
import json
import re
from collections import OrderedDict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/ultra/megamod/feature/relics/RelicSpellAssignments.java"
LANG = ROOT / "src/main/resources/assets/megamod/lang/en_us.json"
SPELL_IDS_FILE = ROOT / "build" / "spell_ids.txt"

text = JAVA.read_text(encoding="utf-8")

# Parse SPELLS map: spells.put("megamod:<relic>", List.of("megamod:relic_<...>", ...))
spells_map = {}  # relic_key -> [spell_id, ...]   relic_key like "megamod:abyssal_cape"
for m in re.finditer(r'spells\.put\("(megamod:[a-z_]+)",\s*List\.of\(([^)]+)\)\)', text):
    relic_key = m.group(1)
    ids = re.findall(r'"(megamod:relic_[a-z_]+)"', m.group(2))
    spells_map[relic_key] = ids

# Build spell_id -> relic_display_name (e.g. "Abyssal Cape")
spell_to_relic = {}
for relic_key, ids in spells_map.items():
    relic_short = relic_key.split(":", 1)[1]  # abyssal_cape
    relic_display = " ".join(w.capitalize() for w in relic_short.split("_"))
    for sid in ids:
        spell_to_relic[sid] = relic_display

# Hardcoded possessive forms for relic display names (snake form has no apostrophe).
POSSESSIVE_RELICS = {
    "Alchemists Sash": "Alchemist's Sash",
    "Guardians Girdle": "Guardian's Girdle",
    "Wardens Visor": "Warden's Visor",
}
for sid, name in list(spell_to_relic.items()):
    if name in POSSESSIVE_RELICS:
        spell_to_relic[sid] = POSSESSIVE_RELICS[name]

# Parse META map: meta.put("megamod:relic_...", new SpellMeta("Display Name", N));
meta_map = {}  # spell_id -> ability display name
for m in re.finditer(
    r'meta\.put\("(megamod:relic_[a-z_]+)",\s*new SpellMeta\("([^"]+)",\s*\d+\)\)',
    text,
):
    meta_map[m.group(1)] = m.group(2)

# Load all 164 spell IDs
spell_ids = [line.strip() for line in SPELL_IDS_FILE.read_text().splitlines() if line.strip()]
assert len(spell_ids) == 164, f"Expected 164 spell IDs, got {len(spell_ids)}"

# Helper: derive ability name from spell id when META is missing
def derive_ability_name(spell_id_short, relic_key_short):
    """spell_id_short = 'relic_abyssal_cape_void_walker'; relic_key_short = 'abyssal_cape'."""
    prefix = f"relic_{relic_key_short}_"
    assert spell_id_short.startswith(prefix), (spell_id_short, prefix)
    ability_snake = spell_id_short[len(prefix):]
    words = ability_snake.split("_")
    return " ".join(w.capitalize() for w in words)

# Build new keys
new_keys = OrderedDict()
unmapped = []
derived = []  # spell ids where META missing and we derived
for full_id in spell_ids:
    spell_id_with_ns = f"megamod:{full_id}"  # full_id starts with relic_
    # Find longest matching relic prefix
    best_relic_key = None
    for relic_key in spells_map:
        relic_short = relic_key.split(":", 1)[1]
        prefix = f"relic_{relic_short}_"
        if full_id.startswith(prefix):
            if best_relic_key is None or len(relic_key) > len(best_relic_key):
                best_relic_key = relic_key
    if best_relic_key is None:
        unmapped.append(full_id)
        relic_display = "Relic"
        ability_display = full_id  # fallback
    else:
        relic_display = spell_to_relic[spell_id_with_ns] if spell_id_with_ns in spell_to_relic \
            else " ".join(w.capitalize() for w in best_relic_key.split(":")[1].split("_"))
        if spell_id_with_ns in meta_map:
            ability_display = meta_map[spell_id_with_ns]
        else:
            ability_display = derive_ability_name(full_id, best_relic_key.split(":")[1])
            derived.append(full_id)

    name_key = f"spell.megamod.{full_id}.name"
    desc_key = f"spell.megamod.{full_id}.description"
    new_keys[name_key] = ability_display
    new_keys[desc_key] = f"{relic_display} ability."

# Load lang file as ordered dict
lang_text = LANG.read_text(encoding="utf-8")
lang = json.loads(lang_text, object_pairs_hook=OrderedDict)

# Add new keys (skip any that already exist; preserve existing values)
added = 0
skipped = 0
for k, v in new_keys.items():
    if k in lang:
        skipped += 1
        continue
    lang[k] = v
    added += 1

# Write back with 4-space indent (matches existing file)
out = json.dumps(lang, indent=4, ensure_ascii=False)
LANG.write_text(out + "\n", encoding="utf-8")

print(f"Added: {added}")
print(f"Skipped (already present): {skipped}")
print(f"Unmapped relic prefix (fallback used): {len(unmapped)}")
for u in unmapped:
    print(f"  - {u}")
print(f"Derived ability name (META missing): {len(derived)}")
for d in derived:
    print(f"  - {d}")
