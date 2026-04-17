"""
Replace the obsolete `minecraft:item/template_spawn_egg` parent with a working
one for MegaMod's 20 custom spawn-egg item models. In 1.21.11 vanilla's
template_spawn_egg is no longer a discoverable JSON parent. Swap to
`minecraft:item/generated` with a `layer0` texture pointing at the mod's
spawn-egg icon PNG for that egg.

Also handles dungeonnowloading's spawn_egg_* files using the same pattern.

If a matching PNG exists at `textures/item/<name>.png` (same namespace as the
model's folder), layer0 points there. Otherwise falls back to the citizen
generic texture.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

ROOT = Path(r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets")

NAMESPACES = ["megamod", "dungeonnowloading"]


def find_texture(namespace: str, name: str) -> str:
    """Pick an existing texture path for this spawn egg, falling back gracefully."""
    base = ROOT / namespace / "textures" / "item"
    candidates = [
        f"{name}",
        f"spawn_egg_{name}",
        f"{name}_spawn_egg",
    ]
    for c in candidates:
        if (base / f"{c}.png").exists():
            return f"{namespace}:item/{c}"
    # Fallback: use the vanilla egg texture (always exists).
    return "minecraft:item/egg"


def patch(path: Path, namespace: str) -> bool:
    raw = path.read_text(encoding="utf-8")
    try:
        data = json.loads(raw)
    except json.JSONDecodeError as e:
        print(f"SKIP parse: {path.name}: {e}", file=sys.stderr)
        return False

    parent = data.get("parent", "")
    if parent not in ("minecraft:item/template_spawn_egg", "item/template_spawn_egg"):
        return False

    name = path.stem  # e.g. "mc_citizen_spawn_egg"
    tex = find_texture(namespace, name)

    new_data = {
        "parent": "minecraft:item/generated",
        "textures": {
            "layer0": tex,
            "particle": tex,
        },
    }
    path.write_text(json.dumps(new_data, indent=2), encoding="utf-8")
    return True


def main() -> int:
    patched = 0
    scanned = 0
    for ns in NAMESPACES:
        item_dir = ROOT / ns / "models" / "item"
        if not item_dir.is_dir():
            continue
        for p in item_dir.rglob("*.json"):
            scanned += 1
            if patch(p, ns):
                patched += 1
                print(f"patched: {p.relative_to(ROOT)}")
    print(f"\nscanned={scanned} patched={patched}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
