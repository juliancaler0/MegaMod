#!/usr/bin/env python3
"""Pass 1 of 1.21.1 -> 1.21.11 API drift fixes for the Reliquary port.

Handles purely mechanical import/rename transformations. Semantic rewrites
(e.g. InteractionResultHolder return statements, DirectionProperty.create)
are handled separately in pass2 or by hand.
"""
import os
import re

ROOT = os.path.join("src", "main", "java", "com", "ultra", "megamod", "reliquary")

# --- Import replacements (whole-line) ---
IMPORT_SUBS = {
    "import net.minecraft.resources.ResourceLocation;":
        "import net.minecraft.resources.Identifier;",
    "import net.minecraft.world.ItemInteractionResult;":
        "import net.minecraft.world.InteractionResult;",
    "import net.minecraft.world.InteractionResultHolder;":
        "",  # delete; code already imports InteractionResult where needed
    "import net.minecraft.world.item.UseAnim;":
        "import net.minecraft.world.item.ItemUseAnimation;",
    "import net.minecraft.util.FastColor;":
        "import net.minecraft.util.ARGB;",
    "import net.minecraft.world.level.portal.DimensionTransition;":
        "import net.minecraft.world.level.portal.TeleportTransition;",
    "import net.neoforged.neoforge.common.util.TriState;":
        "import net.minecraft.util.TriState;",
    "import net.minecraft.world.entity.vehicle.Boat;":
        "import net.minecraft.world.entity.vehicle.AbstractBoat;",
    "import net.minecraft.world.entity.projectile.AbstractArrow;":
        "import net.minecraft.world.entity.projectile.arrow.AbstractArrow;",
    "import net.minecraft.world.entity.projectile.LargeFireball;":
        "import net.minecraft.world.entity.projectile.fireball.LargeFireball;",
    "import net.minecraft.world.entity.projectile.SmallFireball;":
        "import net.minecraft.world.entity.projectile.fireball.SmallFireball;",
    "import net.neoforged.neoforge.event.AddReloadListenerEvent;":
        "import net.neoforged.neoforge.event.AddServerReloadListenersEvent;",
    "import net.minecraft.world.item.component.Unbreakable;":
        "",
    "import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;":
        "import net.minecraft.world.item.crafting.RecipeSerializer;",
    "import net.minecraft.world.level.block.state.properties.DirectionProperty;":
        "import net.minecraft.world.level.block.state.properties.EnumProperty;\nimport net.minecraft.core.Direction;",
}

# --- Identifier-level substitutions (word-boundary regex, applied line-by-line) ---
IDENT_RENAMES = [
    (re.compile(r"\bResourceLocation\b"), "Identifier"),
    (re.compile(r"\bItemInteractionResult\b"), "InteractionResult"),
    (re.compile(r"\bUseAnim\b"), "ItemUseAnimation"),
    (re.compile(r"\bFastColor\b"), "ARGB"),
    (re.compile(r"\bDimensionTransition\b"), "TeleportTransition"),
    (re.compile(r"\bAddReloadListenerEvent\b"), "AddServerReloadListenersEvent"),
    (re.compile(r"\bAbstractArrow\b"), "AbstractArrow"),  # no-op, kept for reference
    (re.compile(r"\bLargeFireball\b"), "LargeFireball"),  # no-op
    (re.compile(r"\bSmallFireball\b"), "SmallFireball"),  # no-op
]

# --- InteractionResult enum value renames ---
# Old ItemInteractionResult values that don't exist on InteractionResult
INTERACTION_VALUE_RENAMES = [
    (re.compile(r"\bInteractionResult\.PASS_TO_DEFAULT_BLOCK_INTERACTION\b"),
     "InteractionResult.TRY_WITH_EMPTY_HAND"),
    (re.compile(r"\bInteractionResult\.SUCCESS_NO_ITEM_USED\b"),
     "InteractionResult.SUCCESS"),
    (re.compile(r"\bInteractionResult\.sidedSuccess\s*\([^)]*\)"),
     "InteractionResult.SUCCESS"),
]

# --- InteractionResultHolder<X> -> InteractionResult in types; and the
#     `new InteractionResultHolder<>(InteractionResult.X, stack)` expression
#     -> `InteractionResult.X` (stack arg dropped). ---
# We apply these carefully to avoid mangling comments.
IRH_TYPE = re.compile(r"InteractionResultHolder\s*<[^>]*>")
IRH_NEW = re.compile(
    r"new\s+InteractionResultHolder\s*<\s*>\s*\(\s*(InteractionResult\.\w+)\s*,\s*[^;]+?\)"
)

# --- DirectionProperty.create(...) => EnumProperty.create(..., Direction.class, ...)
#     We handle the two specific Reliquary call sites:
#       `DirectionProperty.create("facing", Direction.Plane.HORIZONTAL)`
#     becomes
#       `EnumProperty.create("facing", Direction.class,
#           Direction.Plane.HORIZONTAL.stream().toList().toArray(Direction[]::new))`
#     Simpler: use BlockStateProperties.HORIZONTAL_FACING at the call site.
DIRPROP_FIELD = re.compile(r"\bDirectionProperty\b\s+(\w+)\s*=\s*DirectionProperty\.create\(\s*\"facing\"\s*,\s*Direction\.Plane\.HORIZONTAL\s*\)")
DIRPROP_TYPE = re.compile(r"\bDirectionProperty\b")


def process_imports(src: str) -> str:
    lines = src.split("\n")
    out = []
    for line in lines:
        key = line.strip()
        # Exact-match swap (preserve leading whitespace)
        match = None
        for old, new in IMPORT_SUBS.items():
            if key == old:
                match = (old, new)
                break
        if match:
            if match[1]:
                leading = line[: len(line) - len(line.lstrip())]
                for sub in match[1].split("\n"):
                    out.append(leading + sub if sub else sub)
            # else: drop the line
        else:
            out.append(line)
    return "\n".join(out)


def process_src(path: str) -> bool:
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    orig = src

    src = process_imports(src)

    # Identifier-level renames
    for pat, repl in IDENT_RENAMES:
        src = pat.sub(repl, src)

    # Enum value renames on InteractionResult
    for pat, repl in INTERACTION_VALUE_RENAMES:
        src = pat.sub(repl, src)

    # InteractionResultHolder expression rewrite (do this before the type rewrite
    # so the regex for `new InteractionResultHolder<>` still matches)
    src = IRH_NEW.sub(r"\1", src)
    # Type-level: InteractionResultHolder<X> -> InteractionResult
    src = IRH_TYPE.sub("InteractionResult", src)

    # DirectionProperty HORIZONTAL facing field: replace with
    # BlockStateProperties.HORIZONTAL_FACING reference at the field level
    def dirprop_field_sub(match: re.Match) -> str:
        name = match.group(1)
        return f"EnumProperty<Direction> {name} = net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING"

    src = DIRPROP_FIELD.sub(dirprop_field_sub, src)
    # Remaining DirectionProperty references -> EnumProperty<Direction>
    src = DIRPROP_TYPE.sub("EnumProperty<Direction>", src)

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
                if process_src(os.path.join(root, name)):
                    changed += 1
    print(f"scanned={total} changed={changed}")


if __name__ == "__main__":
    main()
