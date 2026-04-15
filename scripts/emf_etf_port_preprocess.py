#!/usr/bin/env python3
"""
EMF/ETF upstream preprocessor + package renamer.

Reads Fabric upstream source with Essential Gradle Toolkit preprocessor markers
(//#if, //#else, //#elseif, //#endif, //$$ commented branches), evaluates them
for our target environment (Minecraft 1.21.11 NeoForge), and writes plain Java
with the target package.

Target environment constants:
  MC = 12111 (1.21.11)
  NEOFORGE = True
  FABRIC = False
  FORGE = False
  IRIS = False
  SODIUM = False
  3DSKINLAYERS = False
  IMMEDIATELYFAST = False

Usage:
    python emf_etf_port_preprocess.py \
        --source <dir> --target <dir> \
        --source-pkg <pkg> --target-pkg <pkg>

Multiple (source, target, source-pkg, target-pkg) triples can be passed by
running the script multiple times.
"""

from __future__ import annotations

import argparse
import os
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

# -------------------- Condition evaluation --------------------

# Environment constants
MC_VERSION = 12111
FLAGS = {
    "NEOFORGE": True,
    "FORGE": False,
    "FABRIC": False,
    "IRIS": False,
    "SODIUM": False,
    "3DSKINLAYERS": False,
    "IMMEDIATELYFAST": False,
}

# Files to skip (relative to source root using dots for packages, slashes for paths)
SKIP_FILES = {
    # EMF entry + modmenu
    "traben/entity_model_features/EMFInit.java",
    "traben/entity_model_features/config/EMFModMenu.java",
    # ETF entry + modmenu
    "traben/entity_texture_features/ETFInit.java",
    "traben/entity_texture_features/config/ETFModMenu.java",
}


def parse_mc_version(token: str) -> int:
    """Parse an MC version token.

    Accepts either numeric (12111) or dotted (1.21.11, 1.21, 1.20.3).
    Dotted form: major.minor.patch => major*10000 + minor*100 + patch.
    Numeric form is assumed to already be in this form.
    Unknown / oddball versions (e.g. 26.1) are normalized by filling in zeros.
    """
    token = token.strip()
    if re.fullmatch(r"\d+", token):
        return int(token)
    # Dotted: 1.21.11, 1.21, 26.1
    parts = token.split(".")
    while len(parts) < 3:
        parts.append("0")
    major, minor, patch = parts[0], parts[1], parts[2]
    # For "1.21.2" -> 1*10000 + 21*100 + 2 = 12102. Matches upstream convention.
    try:
        return int(major) * 10000 + int(minor) * 100 + int(patch)
    except ValueError:
        # Totally unparseable: return a huge number so "MC < X" is false and "MC >= X" false too
        return -1


# Tokenize a condition expression into atoms and operators.
# Atoms: MC <op> <version>, flag name (maybe negated with !).
# Operators: || &&.
_TOKEN_RE = re.compile(
    r"""
    \s+ |                                         # whitespace
    (\|\|) |                                      # ||
    (&&) |                                        # &&
    (\() |                                        # (
    (\)) |                                        # )
    (!)(?=[A-Za-z_0-9]) |                         # ! before identifier
    (MC\s*(?:>=|<=|==|!=|>|<)\s*[\w.]+) |         # MC comparison
    ([A-Za-z_0-9][A-Za-z_0-9]*)                   # flag / identifier (allows leading digit, e.g. 3DSKINLAYERS)
    """,
    re.VERBOSE,
)


def _tokenize(expr: str) -> list[str]:
    tokens: list[str] = []
    pos = 0
    while pos < len(expr):
        m = _TOKEN_RE.match(expr, pos)
        if not m:
            raise ValueError(f"Cannot tokenize at {pos}: {expr!r}")
        pos = m.end()
        # Any non-whitespace group
        for i in range(1, m.lastindex + 1 if m.lastindex else 1):
            g = m.group(i)
            if g is not None:
                tokens.append(g)
                break
    return tokens


def _eval_atom(tok: str) -> bool:
    # MC comparison
    m = re.fullmatch(r"MC\s*(>=|<=|==|!=|>|<)\s*([\w.]+)", tok)
    if m:
        op, ver = m.group(1), m.group(2)
        rhs = parse_mc_version(ver)
        lhs = MC_VERSION
        if op == ">=":
            return lhs >= rhs
        if op == "<=":
            return lhs <= rhs
        if op == "==":
            return lhs == rhs
        if op == "!=":
            return lhs != rhs
        if op == ">":
            return lhs > rhs
        if op == "<":
            return lhs < rhs
    # Flag
    if tok in FLAGS:
        return FLAGS[tok]
    # Unknown identifier: treat as false (safe default; nothing in the wild
    # uses an unknown identifier that should evaluate true for us)
    return False


def evaluate_condition(expr: str) -> bool:
    """Evaluate an EGT preprocessor condition for the target environment."""
    tokens = _tokenize(expr)
    # Rewrite into a Python-evaluable expression using True/False for atoms
    # to take advantage of Python's operator precedence (&& before ||), but
    # since we've tokenized we'll hand-roll a simple recursive-descent parser
    # that handles parens + unary ! + && + ||.
    pos = 0

    def peek():
        nonlocal pos
        return tokens[pos] if pos < len(tokens) else None

    def consume(t=None):
        nonlocal pos
        tok = tokens[pos]
        if t is not None and tok != t:
            raise ValueError(f"Expected {t}, got {tok}")
        pos += 1
        return tok

    def parse_or() -> bool:
        left = parse_and()
        while peek() == "||":
            consume("||")
            right = parse_and()
            left = left or right
        return left

    def parse_and() -> bool:
        left = parse_unary()
        while peek() == "&&":
            consume("&&")
            right = parse_unary()
            left = left and right
        return left

    def parse_unary() -> bool:
        if peek() == "!":
            consume("!")
            return not parse_unary()
        return parse_primary()

    def parse_primary() -> bool:
        tok = peek()
        if tok == "(":
            consume("(")
            v = parse_or()
            consume(")")
            return v
        consume()
        return _eval_atom(tok)

    result = parse_or()
    if pos != len(tokens):
        raise ValueError(f"Trailing tokens: {tokens[pos:]}")
    return result


# -------------------- Preprocessor --------------------

_IF_RE = re.compile(r"^(\s*)//\s*#\s*if\s+(.+?)\s*$")
_ELSEIF_RE = re.compile(r"^(\s*)//\s*#\s*elseif\s+(.+?)\s*$")
_ELSE_RE = re.compile(r"^(\s*)//\s*#\s*else\s*$")
_ENDIF_RE = re.compile(r"^(\s*)//\s*#\s*endif\s*$")

# //$$ matches either "//$$" alone or "//$$ something"
# Preserve leading whitespace.
_DOLLAR_RE = re.compile(r"^(\s*)//\$\$ ?(.*)$")


@dataclass
class Frame:
    # True when we're currently in an active branch
    active: bool
    # True when any previous branch in this if/elseif/else chain was taken
    any_taken: bool
    # True when the parent frame is active (so we can take a branch at all)
    parent_active: bool
    # For diagnostics
    start_line: int = 0


def preprocess_source(text: str) -> str:
    lines = text.splitlines(keepends=False)
    out: list[str] = []
    stack: list[Frame] = []

    # Root frame: everything outside any #if is unconditionally active
    root = Frame(active=True, any_taken=True, parent_active=True, start_line=0)
    stack.append(root)

    for idx, line in enumerate(lines, start=1):
        top = stack[-1]

        # #if
        m = _IF_RE.match(line)
        if m:
            cond = m.group(2)
            # Strip any trailing comment that might follow the condition
            cond = re.sub(r"\s*//.*$", "", cond).strip()
            parent_active = top.active
            taken = parent_active and evaluate_condition(cond)
            stack.append(
                Frame(
                    active=taken,
                    any_taken=taken,
                    parent_active=parent_active,
                    start_line=idx,
                )
            )
            continue

        m = _ELSEIF_RE.match(line)
        if m:
            cond = m.group(2)
            cond = re.sub(r"\s*//.*$", "", cond).strip()
            if len(stack) <= 1:
                raise ValueError(f"Line {idx}: #elseif without #if")
            frame = stack[-1]
            if frame.any_taken:
                frame.active = False
            else:
                taken = frame.parent_active and evaluate_condition(cond)
                frame.active = taken
                if taken:
                    frame.any_taken = True
            continue

        m = _ELSE_RE.match(line)
        if m:
            if len(stack) <= 1:
                raise ValueError(f"Line {idx}: #else without #if")
            frame = stack[-1]
            if frame.any_taken:
                frame.active = False
            else:
                frame.active = frame.parent_active
                if frame.active:
                    frame.any_taken = True
            continue

        m = _ENDIF_RE.match(line)
        if m:
            if len(stack) <= 1:
                raise ValueError(f"Line {idx}: #endif without #if")
            stack.pop()
            continue

        # Non-marker line: emit only if ALL frames on the stack are active
        if all(f.active for f in stack):
            # In the active branch, //$$ lines get uncommented
            d = _DOLLAR_RE.match(line)
            if d:
                indent, body = d.group(1), d.group(2)
                out.append(indent + body)
            else:
                out.append(line)
        # else: drop the line

    if len(stack) != 1:
        raise ValueError(
            f"Unclosed #if (opened at line {stack[-1].start_line})"
        )

    # Preserve final newline if original had one
    result = "\n".join(out)
    if text.endswith("\n"):
        result += "\n"
    return result


# -------------------- Package rename --------------------


def rename_packages(text: str, src_pkg: str, tgt_pkg: str) -> str:
    """Replace all occurrences of src_pkg (dotted) with tgt_pkg in the file.

    We do a simple string replace for the dotted form (matches package
    statements, imports, fully-qualified refs in strings, annotations,
    mixin targets).
    """
    return text.replace(src_pkg, tgt_pkg)


# -------------------- Driver --------------------


@dataclass
class TreeSpec:
    source: Path
    target: Path
    src_pkg: str  # traben.entity_model_features
    tgt_pkg: str  # com.ultra.megamod.lib.emf


# ALL package renames applied to every file (since EMF refs ETF/tconfig, etc.)
ALL_RENAMES: list[tuple[str, str]] = [
    ("traben.entity_model_features", "com.ultra.megamod.lib.emf"),
    ("traben.entity_texture_features", "com.ultra.megamod.lib.etf"),
    ("traben.tconfig", "com.ultra.megamod.lib.tconfig"),
]


def relative_to_src_java(path: Path, source_root: Path) -> str:
    """Return the path relative to source_root, using forward slashes."""
    rel = path.relative_to(source_root).as_posix()
    return rel


def process_tree(spec: TreeSpec) -> tuple[int, int, int]:
    """Process a single tree. Returns (processed, skipped, errored)."""
    processed = 0
    skipped = 0
    errored = 0

    if not spec.source.exists():
        raise FileNotFoundError(f"Source tree not found: {spec.source}")
    spec.target.mkdir(parents=True, exist_ok=True)

    # Source root is the parent of the actual leaf (traben/) — we walk starting
    # from the leaf but check SKIP_FILES against a path built from the broader
    # root (the src/main/java).
    # Our source param is like .../src/main/java/traben/entity_model_features
    # We walk from source (the leaf package), but SKIP_FILES is relative to
    # src/main/java. Reconstruct that prefix.
    src_pkg_path = spec.src_pkg.replace(".", "/") + "/"

    for root, dirs, files in os.walk(spec.source):
        for fname in files:
            if not fname.endswith(".java"):
                continue
            src_file = Path(root) / fname
            # Path relative to the leaf package dir
            rel_from_leaf = src_file.relative_to(spec.source).as_posix()
            # Rebuild the full package-relative path for SKIP check
            skip_key = src_pkg_path + rel_from_leaf
            if skip_key in SKIP_FILES:
                skipped += 1
                print(f"  SKIP {skip_key}", file=sys.stderr)
                continue

            tgt_file = spec.target / rel_from_leaf
            tgt_file.parent.mkdir(parents=True, exist_ok=True)

            try:
                text = src_file.read_text(encoding="utf-8")
                # 1. Preprocess
                text = preprocess_source(text)
                # 2. Rename ALL known packages
                for a, b in ALL_RENAMES:
                    text = rename_packages(text, a, b)
                tgt_file.write_text(text, encoding="utf-8")
                processed += 1
            except Exception as e:  # noqa: BLE001
                errored += 1
                print(f"  ERROR {src_file}: {e}", file=sys.stderr)
                import traceback

                traceback.print_exc(file=sys.stderr)

    return processed, skipped, errored


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("--source", required=True, type=Path)
    ap.add_argument("--target", required=True, type=Path)
    ap.add_argument(
        "--source-pkg",
        required=True,
        help="Dotted source package (e.g. traben.entity_model_features)",
    )
    ap.add_argument(
        "--target-pkg",
        required=True,
        help="Dotted target package (e.g. com.ultra.megamod.lib.emf)",
    )
    args = ap.parse_args()

    spec = TreeSpec(
        source=args.source,
        target=args.target,
        src_pkg=args.source_pkg,
        tgt_pkg=args.target_pkg,
    )

    print(f"Processing {spec.source} -> {spec.target}")
    print(f"  rename {spec.src_pkg} -> {spec.tgt_pkg}")
    processed, skipped, errored = process_tree(spec)
    print(
        f"Done. processed={processed}, skipped={skipped}, errored={errored}"
    )
    return 0 if errored == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
