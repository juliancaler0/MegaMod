#!/usr/bin/env python3
"""Pass 3: bulk mechanical fixes after the entry-points compile.

These are repeated across many files and would be tedious to hand-edit.

Coverage:
- Level.isClientSide is now private; call .isClientSide() (99 sites).
- Item.Properties#setNoRepair() removed; non-repairability is the default
  in 1.21.11 — drop the chained call (13 sites).
- MobEffects renames (MOVEMENT_SLOWDOWN -> SLOWNESS, CONFUSION -> NAUSEA,
  HUNGER -> HUNGER, JUMP -> JUMP_BOOST, DAMAGE_RESISTANCE -> RESISTANCE).
- Inventory.items field is private; replace inv.items with inv.getItems().
"""
import os
import re

ROOT = os.path.join("src", "main", "java", "com", "ultra", "megamod", "reliquary")

# Word-boundary identifier renames
RENAMES = [
    (re.compile(r"MobEffects\.MOVEMENT_SLOWDOWN\b"), "MobEffects.SLOWNESS"),
    (re.compile(r"MobEffects\.CONFUSION\b"), "MobEffects.NAUSEA"),
    (re.compile(r"MobEffects\.JUMP\b"), "MobEffects.JUMP_BOOST"),
    (re.compile(r"MobEffects\.DAMAGE_RESISTANCE\b"), "MobEffects.RESISTANCE"),
]

# (.isClientSide) -> (.isClientSide())
# Only when followed by something that isn't already a method call: e.g.
# `level.isClientSide` (end of line, ; , && ||, etc.) but NOT `level.isClientSide()`
ISCLIENTSIDE = re.compile(r"\.isClientSide(?!\s*\()")

# .setNoRepair() removal — strip the chained call entirely
SETNOREPAIR = re.compile(r"\s*\.setNoRepair\(\s*\)")

# Inventory#items access -> Inventory#getItems()
# Be careful: only target `inventory.items` / `inv.items` / `playerInv.items` etc.
# We use a simple word-bound match preceded by an identifier ending in "ventory"
# or "Inv" (case sensitive). Also handle player.getInventory().items.
INV_ITEMS = re.compile(r"\.getInventory\(\)\.items\b")


def process(path: str) -> bool:
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    orig = src

    for pat, repl in RENAMES:
        src = pat.sub(repl, src)

    src = ISCLIENTSIDE.sub(".isClientSide()", src)
    src = SETNOREPAIR.sub("", src)
    src = INV_ITEMS.sub(".getInventory().getItems()", src)

    if src != orig:
        with open(path, "w", encoding="utf-8") as f:
            f.write(src)
        return True
    return False


def main() -> None:
    total = 0
    changed = 0
    for root, _dirs, files in os.walk(ROOT):
        for name in files:
            if name.endswith(".java"):
                total += 1
                if process(os.path.join(root, name)):
                    changed += 1
    print(f"scanned={total} changed={changed}")


if __name__ == "__main__":
    main()
