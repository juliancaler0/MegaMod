"""
Patch block and item model JSONs that are missing a "particle" texture key.

Strategy: if a model's `textures` block has keys but no "particle", alias
"particle" to the first numeric-keyed texture (#0..#9) that is defined, or the
first key otherwise. This matches the convention used elsewhere in the mod
(e.g. park_bench uses "particle": "#3").

Preserves JSON formatting (tabs vs spaces) by using a light-touch regex-based
insertion rather than a full round-trip through json.dump.
"""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path

ROOT = Path(r"C:\Users\JulianCalero\Desktop\Projects\MegaMod\src\main\resources\assets")

MODEL_DIRS = [
    ROOT / "megamod" / "models" / "block",
    ROOT / "megamod" / "models" / "item",
]


def pick_particle_key(textures: dict) -> str | None:
    if not textures:
        return None
    if "particle" in textures:
        return None  # already has one
    # Prefer numeric keys in ascending order.
    numeric = sorted((k for k in textures if k.isdigit()), key=int)
    if numeric:
        return numeric[0]
    # Otherwise first non-particle key.
    for k in textures:
        if k != "particle":
            return k
    return None


def patch_file(path: Path) -> bool:
    raw = path.read_text(encoding="utf-8")
    try:
        data = json.loads(raw)
    except json.JSONDecodeError as e:
        print(f"SKIP (parse error): {path.name}: {e}", file=sys.stderr)
        return False

    textures = data.get("textures")
    if not isinstance(textures, dict):
        return False
    if "particle" in textures:
        return False

    key = pick_particle_key(textures)
    if key is None:
        return False

    # Find the last key:value pair inside the textures object in the raw text.
    # We do this by locating the "textures" object start, walking to its closing
    # brace, and inserting a new line before the brace.
    #
    # Use a regex to find `"textures"\s*:\s*{` then walk braces.
    m = re.search(r'"textures"\s*:\s*\{', raw)
    if not m:
        return False
    start = m.end()  # char after the opening {
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
    close_brace = i  # position of the closing }

    # Determine indentation and separator style.
    # Look at the line containing close_brace to find its indent,
    # and look for the last comma/last entry to determine inner indent.
    body = raw[start:close_brace]
    # Split body into lines; the first non-empty line's leading whitespace is our entry indent.
    entry_indent = ""
    for line in body.split("\n"):
        if line.strip():
            entry_indent = line[: len(line) - len(line.lstrip())]
            break

    # Build the new entry line.
    new_entry = f'"particle": "#{key}"' if key.isdigit() else f'"particle": "{textures[key]}"'
    insertion = f',\n{entry_indent}{new_entry}'

    # Insert before the closing brace. We need to place it after the last
    # non-whitespace character of the body (which will be the last value or
    # comma). Find last non-whitespace position in body:
    trailing_ws_start = close_brace
    j = close_brace - 1
    while j >= start and raw[j] in " \t\r\n":
        j -= 1
    # j is the index of the last non-whitespace char in the body
    insert_at = j + 1
    new_raw = raw[:insert_at] + insertion + raw[insert_at:]

    # Validate result parses.
    try:
        new_data = json.loads(new_raw)
    except json.JSONDecodeError as e:
        print(f"SKIP (would break json): {path.name}: {e}", file=sys.stderr)
        return False
    # Sanity: new textures has particle.
    if "particle" not in new_data.get("textures", {}):
        print(f"SKIP (particle missing after patch): {path.name}", file=sys.stderr)
        return False

    path.write_text(new_raw, encoding="utf-8")
    return True


def main() -> int:
    patched = 0
    scanned = 0
    for d in MODEL_DIRS:
        if not d.is_dir():
            continue
        for p in d.rglob("*.json"):
            scanned += 1
            if patch_file(p):
                patched += 1
                print(f"patched: {p.relative_to(ROOT)}")
    print(f"\nscanned={scanned} patched={patched}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
