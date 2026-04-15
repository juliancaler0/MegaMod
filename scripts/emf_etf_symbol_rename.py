"""
Rewrite MC 1.21.11 mob/model subpackage imports across the freshly-ported
EMF/ETF/tconfig source tree.

Upstream code was written against pre-1.21.11 vanilla packages; in 1.21.11 a
huge swath of mob and model classes were relocated into per-mob subpackages
(net.minecraft.world.entity.animal.wolf.Wolf, etc.). This script does literal
string replacements for both the `import ...;` line and any fully-qualified
references in the source body.

Run from the project root; idempotent.
"""

from __future__ import annotations

import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
TARGETS = [
    ROOT / "src/main/java/com/ultra/megamod/lib/emf",
    ROOT / "src/main/java/com/ultra/megamod/lib/etf",
    ROOT / "src/main/java/com/ultra/megamod/lib/tconfig",
]

# (old-fqn, new-fqn) pairs. ORDER MATTERS — apply more-specific renames first
# so we don't double-rewrite an inner-class reference.
RENAMES: list[tuple[str, str]] = [
    # --- model classes (1.21.11 subpackage split) ---
    ("net.minecraft.client.model.WolfModel", "net.minecraft.client.model.animal.wolf.WolfModel"),
    ("net.minecraft.client.model.PigModel", "net.minecraft.client.model.animal.pig.PigModel"),
    ("net.minecraft.client.model.ColdPigModel", "net.minecraft.client.model.animal.pig.ColdPigModel"),
    ("net.minecraft.client.model.ChickenModel", "net.minecraft.client.model.animal.chicken.ChickenModel"),
    ("net.minecraft.client.model.ColdChickenModel", "net.minecraft.client.model.animal.chicken.ColdChickenModel"),
    ("net.minecraft.client.model.CreeperModel", "net.minecraft.client.model.monster.creeper.CreeperModel"),
    ("net.minecraft.client.model.ParrotModel", "net.minecraft.client.model.animal.parrot.ParrotModel"),
    ("net.minecraft.client.model.PlayerModel", "net.minecraft.client.model.player.PlayerModel"),
    ("net.minecraft.client.model.VillagerModel", "net.minecraft.client.model.npc.VillagerModel"),
    ("net.minecraft.client.model.WardenModel", "net.minecraft.client.model.monster.warden.WardenModel"),
    ("net.minecraft.client.model.ArmadilloModel", "net.minecraft.client.model.animal.armadillo.ArmadilloModel"),
    ("net.minecraft.client.model.SkullModelBase", "net.minecraft.client.model.object.skull.SkullModelBase"),
    ("net.minecraft.client.model.ZombieVillagerModel", "net.minecraft.client.model.monster.zombie.ZombieVillagerModel"),

    # --- entity classes (1.21.11 subpackage split) ---
    ("net.minecraft.world.entity.animal.Wolf", "net.minecraft.world.entity.animal.wolf.Wolf"),
    ("net.minecraft.world.entity.animal.Pig", "net.minecraft.world.entity.animal.pig.Pig"),
    ("net.minecraft.world.entity.animal.Chicken", "net.minecraft.world.entity.animal.chicken.Chicken"),
    ("net.minecraft.world.entity.animal.Parrot", "net.minecraft.world.entity.animal.parrot.Parrot"),
    ("net.minecraft.world.entity.animal.Cat", "net.minecraft.world.entity.animal.feline.Cat"),
    ("net.minecraft.world.entity.animal.CatVariant", "net.minecraft.world.entity.animal.feline.CatVariant"),
    ("net.minecraft.world.entity.animal.Squid", "net.minecraft.world.entity.animal.squid.Squid"),
    ("net.minecraft.world.entity.animal.GlowSquid", "net.minecraft.world.entity.animal.squid.GlowSquid"),
    ("net.minecraft.world.entity.GlowSquid", "net.minecraft.world.entity.animal.squid.GlowSquid"),
    ("net.minecraft.world.entity.animal.Fox", "net.minecraft.world.entity.animal.fox.Fox"),
    ("net.minecraft.world.entity.animal.IronGolem", "net.minecraft.world.entity.animal.golem.IronGolem"),
    ("net.minecraft.world.entity.animal.MushroomCow", "net.minecraft.world.entity.animal.cow.MushroomCow"),
    ("net.minecraft.world.entity.animal.Panda", "net.minecraft.world.entity.animal.panda.Panda"),
    ("net.minecraft.world.entity.animal.TropicalFish", "net.minecraft.world.entity.animal.fish.TropicalFish"),
    ("net.minecraft.world.entity.animal.horse.AbstractHorse", "net.minecraft.world.entity.animal.equine.AbstractHorse"),
    ("net.minecraft.world.entity.animal.horse.Llama", "net.minecraft.world.entity.animal.equine.Llama"),
    # NB: there are several other horse subtypes (TraderLlama, Donkey, Mule,
    # Horse, ZombieHorse, SkeletonHorse, Camel) that may need similar moves
    # but they don't show up in the current error list.

    # --- decoration / vehicle / projectile / illager moves ---
    ("net.minecraft.world.entity.decoration.Painting", "net.minecraft.world.entity.decoration.painting.Painting"),
    ("net.minecraft.world.entity.decoration.PaintingVariant", "net.minecraft.world.entity.decoration.painting.PaintingVariant"),
    ("net.minecraft.world.entity.vehicle.AbstractMinecart", "net.minecraft.world.entity.vehicle.minecart.AbstractMinecart"),
    ("net.minecraft.world.entity.vehicle.AbstractBoat", "net.minecraft.world.entity.vehicle.boat.AbstractBoat"),
    ("net.minecraft.world.entity.projectile.Arrow", "net.minecraft.world.entity.projectile.arrow.Arrow"),
    ("net.minecraft.world.entity.monster.SpellcasterIllager", "net.minecraft.world.entity.monster.illager.SpellcasterIllager"),
    ("net.minecraft.world.entity.monster.Vindicator", "net.minecraft.world.entity.monster.illager.Vindicator"),

    # --- npc subpackage ---
    ("net.minecraft.world.entity.npc.VillagerType", "net.minecraft.world.entity.npc.villager.VillagerType"),
    ("net.minecraft.world.entity.npc.VillagerDataHolder", "net.minecraft.world.entity.npc.villager.VillagerDataHolder"),
    # Villager itself was already moved per CLAUDE.md, but include for safety.
    ("net.minecraft.world.entity.npc.Villager", "net.minecraft.world.entity.npc.villager.Villager"),

    # --- util / advancements ---
    ("net.minecraft.Util", "net.minecraft.util.Util"),
    ("net.minecraft.FileUtil", "net.minecraft.util.FileUtil"),
    ("net.minecraft.advancements.critereon.NbtPredicate", "net.minecraft.advancements.criterion.NbtPredicate"),

    # --- ResourceKey method rename ---
    # location() -> identifier() per Parchment 2025.12.20.
    # The replacements below cover the shapes seen in the EMF/ETF source —
    # they are intentionally narrow so we don't accidentally rewrite a
    # ResourceLocation.location() (which doesn't exist) call. If this list
    # ever grows, prefer adding more specific shapes over a blanket
    # `.location()` -> `.identifier()` rename.
    (".unwrapKey().get().location()", ".unwrapKey().get().identifier()"),
    (".unwrapKey().orElseThrow().location()", ".unwrapKey().orElseThrow().identifier()"),
    ("optional.get().location()", "optional.get().identifier()"),
    ("dimKey.get().location()", "dimKey.get().identifier()"),
    ("key -> key.location().getPath()", "key -> key.identifier().getPath()"),
]


def main() -> None:
    files: list[pathlib.Path] = []
    for root in TARGETS:
        if not root.exists():
            continue
        files.extend(root.rglob("*.java"))
    print(f"Scanning {len(files)} java files...")

    edits = 0
    per_rule: dict[str, int] = {old: 0 for old, _ in RENAMES}
    for path in files:
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        original = text
        for old, new in RENAMES:
            if old in text:
                count = text.count(old)
                text = text.replace(old, new)
                per_rule[old] += count
        if text != original:
            path.write_text(text, encoding="utf-8")
            edits += 1
    print(f"Modified {edits} files. Per-rule replacements:")
    for rule, count in per_rule.items():
        if count:
            print(f"  {count:4d}  {rule}")


if __name__ == "__main__":
    main()
