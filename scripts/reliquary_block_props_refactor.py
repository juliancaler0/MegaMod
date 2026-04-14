#!/usr/bin/env python3
"""Refactor Reliquary block ctors to take Properties (for 1.21.11 setId() path)."""
import re
from pathlib import Path

BLOCKS_DIR = Path("src/main/java/com/ultra/megamod/reliquary/block")

# Regex: match `public <ClassName>() { super(Properties.of()...); ...` -> accept properties
# We replace the no-arg ctor body with a props-taking variant while preserving the rest.
patches = [
    # (class_name, old_regex, new_body)
    ("ApothecaryCauldronBlock",
     r"public ApothecaryCauldronBlock\(\)\s*\{\s*super\(Properties\.of\(\)\.mapColor\(MapColor\.METAL\)\.strength\(1\.5F, 5\.0F\)\.noOcclusion\(\)\);\s*registerDefaultState",
     "public ApothecaryCauldronBlock(Properties properties) {\n\t\tsuper(properties);\n\t\tregisterDefaultState"),
    ("ApothecaryMortarBlock",
     r"public ApothecaryMortarBlock\(\)\s*\{\s*super\(Properties\.of\(\)\.mapColor\(MapColor\.STONE\)\.strength\(1\.5F, 2\.0F\)\);\s*registerDefaultState",
     "public ApothecaryMortarBlock(Properties properties) {\n\t\tsuper(properties);\n\t\tregisterDefaultState"),
    ("WraithNodeBlock",
     r"public WraithNodeBlock\(\)\s*\{\s*super\(Properties\.of\(\)\.mapColor\(MapColor\.STONE\)\.strength\(1\.5F, 5\.0F\)\.noOcclusion\(\)\);",
     "public WraithNodeBlock(Properties properties) {\n\t\tsuper(properties);"),
    ("PassivePedestalBlock",
     r"public PassivePedestalBlock\(\)\s*\{\s*super\(Properties\.of\(\)\.mapColor\(MapColor\.STONE\)\.strength\(1\.5F, 2\.0F\)\.forceSolidOn\(\)\);\s*registerDefaultState",
     "public PassivePedestalBlock(Properties properties) {\n\t\tsuper(properties);\n\t\tregisterDefaultState"),
    ("FertileLilyPadBlock",
     r"public FertileLilyPadBlock\(\)\s*\{\s*super\(Properties\.of\(\)\.mapColor\(MapColor\.PLANT\)\);\s*\}",
     "public FertileLilyPadBlock(Properties properties) {\n\t\tsuper(properties);\n\t}"),
    ("InterdictionTorchBlock",
     r"public InterdictionTorchBlock\(\)\s*\{\s*super\(ParticleTypes\.FLAME, Properties\.of\(\)\.strength\(0\)\.lightLevel\(value -> 15\)\.randomTicks\(\)\.sound\(SoundType\.WOOD\)\.noCollision\(\)\);\s*\}",
     "public InterdictionTorchBlock(Properties properties) {\n\t\tsuper(ParticleTypes.FLAME, properties);\n\t}"),
    ("WallInterdictionTorchBlock",
     r"public WallInterdictionTorchBlock\(\)\s*\{\s*super\(\);\s*registerDefaultState",
     "public WallInterdictionTorchBlock(Properties properties) {\n\t\tsuper(properties);\n\t\tregisterDefaultState"),
    ("PedestalBlock",
     r"public PedestalBlock\(\)\s*\{\s*super\(\);\s*registerDefaultState",
     "public PedestalBlock(Properties properties) {\n\t\tsuper(properties);\n\t\tregisterDefaultState"),
]

total_changed = 0
for name, pat, rep in patches:
    f = BLOCKS_DIR / (name + ".java")
    if not f.exists():
        print(f"skip {name}: not found")
        continue
    src = f.read_text(encoding="utf-8")
    new = re.sub(pat, rep, src, flags=re.DOTALL)
    if new != src:
        f.write_text(new, encoding="utf-8")
        total_changed += 1
        print(f"ok   {name}")
    else:
        print(f"MISS {name} (pattern didn't match)")

print(f"total changed: {total_changed}")
