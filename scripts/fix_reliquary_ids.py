"""
Migrate data JSONs that reference reliquary bullet / magazine items by their
old flat ids (`reliquary:<name>_bullet`, `reliquary:<name>_magazine`) to the
current sub-folder ids (`reliquary:bullets/<name>_bullet`,
`reliquary:magazines/<name>_magazine`) that the ModItems registry uses.

Vanilla MC 1.21.x allows slashes in item ids and the registry uses the exact
path as stored at registration time, so bare `reliquary:empty_bullet` can no
longer resolve.
"""

import json
import os
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA = ROOT / "src" / "main" / "resources" / "data"

BULLETS = [
    "empty", "neutral", "exorcism", "blaze", "ender",
    "concussive", "buster", "seeker", "sand", "storm",
]
MAGAZINES = BULLETS  # same suffix list

MAPPINGS = {}
for name in BULLETS:
    MAPPINGS[f"reliquary:{name}_bullet"] = f"reliquary:bullets/{name}_bullet"
for name in MAGAZINES:
    MAPPINGS[f"reliquary:{name}_magazine"] = f"reliquary:magazines/{name}_magazine"


def rewrite_text(text: str) -> str:
    for old, new in MAPPINGS.items():
        # Match quoted occurrences to avoid touching comments / substrings.
        # Avoid double-rewriting when the path form already contains the short form.
        # e.g. "reliquary:bullets/empty_bullet" must not be clobbered.
        pattern = re.compile(r'"' + re.escape(old) + r'(?=")')
        text = pattern.sub('"' + new, text)
    return text


def main():
    total = 0
    updated = 0
    for path in DATA.rglob("*.json"):
        total += 1
        text = path.read_text(encoding="utf-8")
        new_text = rewrite_text(text)
        if new_text != text:
            path.write_text(new_text, encoding="utf-8")
            updated += 1
            print(f"[FIX ] {path.relative_to(DATA)}")
    print(f"Scanned {total}, updated {updated}.")


if __name__ == "__main__":
    sys.exit(main())
