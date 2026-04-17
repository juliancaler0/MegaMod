"""
Fix block hut and pillar models that inherit from cube_all but don't define
the `#all` texture. Add an `all` entry aliasing an existing texture so the
model loader can resolve all parent references.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets\megamod\models")

TARGETS: list[tuple[Path, str]] = []

# Hut-style blocks + items and pillar files we saw in the log.
for p in ROOT.rglob("blockhut*.json"):
    TARGETS.append((p, "block"))
for p in (ROOT / "item").rglob("hut_*.json"):
    TARGETS.append((p, "item"))
for p in (ROOT / "block" / "pillar").glob("*.json"):
    TARGETS.append((p, "pillar"))


def patch(path: Path) -> bool:
    raw = path.read_text(encoding="utf-8")
    try:
        data = json.loads(raw)
    except json.JSONDecodeError as e:
        print(f"SKIP parse: {path.name}: {e}", file=sys.stderr)
        return False

    parent = data.get("parent", "")
    if "cube_all" not in parent:
        return False  # only cube_all variants need #all

    textures = data.get("textures", {})
    if "all" in textures:
        return False

    # Pick an alias target: prefer #0 if present, then particle, then first key.
    if "0" in textures:
        alias_val = "#0"
    elif "particle" in textures:
        alias_val = "#particle"
    elif textures:
        alias_val = "#" + next(iter(textures))
    else:
        return False

    # Insert into textures object in raw text.
    m = re.search(r'"textures"\s*:\s*\{', raw)
    if not m:
        return False
    start = m.end()
    depth = 1
    i = start
    in_string = False
    escape = False
    while i < len(raw) and depth > 0:
        c = raw[i]
        if in_string:
            if escape:
                escape = False
            elif c == "\\":
                escape = True
            elif c == '"':
                in_string = False
        else:
            if c == '"':
                in_string = True
            elif c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
                if depth == 0:
                    break
        i += 1
    if depth != 0:
        return False
    close_brace = i

    body = raw[start:close_brace]
    entry_indent = ""
    for line in body.split("\n"):
        if line.strip():
            entry_indent = line[: len(line) - len(line.lstrip())]
            break

    new_entry = f'"all": "{alias_val}"'
    insertion = f',\n{entry_indent}{new_entry}'
    j = close_brace - 1
    while j >= start and raw[j] in " \t\r\n":
        j -= 1
    insert_at = j + 1
    new_raw = raw[:insert_at] + insertion + raw[insert_at:]

    try:
        json.loads(new_raw)
    except json.JSONDecodeError as e:
        print(f"SKIP invalid after patch: {path.name}: {e}", file=sys.stderr)
        return False

    path.write_text(new_raw, encoding="utf-8")
    return True


def main() -> int:
    patched = 0
    scanned = 0
    for p, kind in TARGETS:
        scanned += 1
        if patch(p):
            patched += 1
            print(f"patched ({kind}): {p.relative_to(ROOT)}")
    print(f"\nscanned={scanned} patched={patched}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
