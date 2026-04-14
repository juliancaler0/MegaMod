#!/usr/bin/env python3
"""Replaces the props supplier for each RelicItem entry in RelicRegistry
with a call to RelicSpellPropsHelper that attaches the SpellContainer."""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REG = ROOT / "src/main/java/com/ultra/megamod/feature/relics/RelicRegistry.java"

text = REG.read_text(encoding="utf-8")

# Pattern: ITEMS.registerItem("item_id", props -> new RelicItem(..., (Item.Properties)props), () -> new Item.Properties().stacksTo(1));
# Replacement: ITEMS.registerItem("item_id", props -> new RelicItem(..., (Item.Properties)props), () -> RelicSpellPropsHelper.relicProps("megamod:item_id"));
pattern = re.compile(
    r'(ITEMS\.registerItem\("([^"]+)",\s*props\s*->\s*new\s+RelicItem\([^;]*?\),\s*)\(\)\s*->\s*new\s+Item\.Properties\(\)\.stacksTo\(1\)\);',
    re.DOTALL,
)

def replace(match: re.Match) -> str:
    prefix = match.group(1)
    item_id = match.group(2)
    return f'{prefix}() -> RelicSpellPropsHelper.relicProps("megamod:{item_id}"));'

new_text, count = pattern.subn(replace, text)
print(f"patched {count} relic registrations")

# Add import if missing
if "import com.ultra.megamod.feature.relics.RelicSpellPropsHelper;" not in new_text:
    # The file imports are weird (explicit classes like com.ultra.megamod.feature.relics.RelicItem)
    # Since RelicSpellPropsHelper is in the same package, no import is needed.
    pass

REG.write_text(new_text, encoding="utf-8")
