#!/usr/bin/env python3
"""Pass 2 of 1.21.1 -> 1.21.11 API drift fixes for the Reliquary port.

- Corrects the wrong fireball / throwable subpackage paths from pass 1.
- Removes references to the deleted reliquary.data datagen package.
- Migrates entities into the new vanilla subpackages (Cow, Villager,
  ZombieVillager, ThrownEnderpearl).
"""
import os
import re

ROOT = os.path.join("src", "main", "java", "com", "ultra", "megamod", "reliquary")

IMPORT_SUBS = {
    # Wrong "fireball" subpackage from pass 1 -> the real "hurtingprojectile" one
    "import net.minecraft.world.entity.projectile.fireball.LargeFireball;":
        "import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;",
    "import net.minecraft.world.entity.projectile.fireball.SmallFireball;":
        "import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;",
    # Other entity moves
    "import net.minecraft.world.entity.projectile.ThrowableItemProjectile;":
        "import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;",
    "import net.minecraft.world.entity.projectile.ThrownEnderpearl;":
        "import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;",
    "import net.minecraft.world.entity.animal.Cow;":
        "import net.minecraft.world.entity.animal.cow.Cow;",
    "import net.minecraft.world.entity.npc.Villager;":
        "import net.minecraft.world.entity.npc.villager.Villager;",
    "import net.minecraft.world.entity.monster.Zombie;":
        "import net.minecraft.world.entity.monster.zombie.Zombie;",
    "import net.minecraft.world.entity.monster.ZombieVillager;":
        "import net.minecraft.world.entity.monster.zombie.ZombieVillager;",
}


def process_imports(src: str) -> str:
    lines = src.split("\n")
    out = []
    for line in lines:
        key = line.strip()
        new = IMPORT_SUBS.get(key)
        if new is not None:
            leading = line[: len(line) - len(line.lstrip())]
            out.append(leading + new if new else "")
        else:
            out.append(line)
    return "\n".join(out)


def process_src(path: str) -> bool:
    with open(path, "r", encoding="utf-8") as f:
        src = f.read()
    orig = src
    src = process_imports(src)
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
