#!/usr/bin/env python3
"""
One-shot generator for Phase G.1: convert every relic ability into a SpellEngine
spell JSON under `src/main/resources/data/megamod/spell/relic_<...>.json`
and emit `RelicSpellAssignments.java` that lists which relic registry ID
holds which spell IDs (so RelicRegistry can attach a SpellContainer).

Source of truth: the decompiled ability Java files under
`src/main/java/com/ultra/megamod/feature/relics/ability/**/*.java`.
"""

from __future__ import annotations

import json
import os
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ABILITY_DIR = ROOT / "src/main/java/com/ultra/megamod/feature/relics/ability"
OUT_SPELL_DIR = ROOT / "src/main/resources/data/megamod/spell"
OUT_JAVA = ROOT / "src/main/java/com/ultra/megamod/feature/relics/RelicSpellAssignments.java"

# ----------------------------------------------------------------------------
# Relic display-name -> item registry id (item is always lowercase_snake).
# This mirrors RelicRegistry entries — keep in sync with that file.
# ----------------------------------------------------------------------------
RELIC_TO_ITEM_ID: dict[str, str] = {
    "Arrow Quiver": "arrow_quiver",
    "Elytra Booster": "elytra_booster",
    "Midnight Robe": "midnight_robe",
    "Leather Belt": "leather_belt",
    "Drowned Belt": "drowned_belt",
    "Hunter Belt": "hunter_belt",
    "Ender Hand": "ender_hand",
    "Rage Glove": "rage_glove",
    "Wool Mitten": "wool_mitten",
    "Magma Walker": "magma_walker",
    "Aqua Walker": "aqua_walker",
    "Ice Skates": "ice_skates",
    "Ice Breaker": "ice_breaker",
    "Roller Skates": "roller_skates",
    "Amphibian Boot": "amphibian_boot",
    "Reflection Necklace": "reflection_necklace",
    "Jellyfish Necklace": "jellyfish_necklace",
    "Holy Locket": "holy_locket",
    "Bastion Ring": "bastion_ring",
    "Chorus Inhibitor": "chorus_inhibitor",
    "Shadow Glaive": "shadow_glaive",
    "Infinity Ham": "infinity_ham",
    "Space Dissector": "space_dissector",
    "Magic Mirror": "magic_mirror",
    "Horse Flute": "horse_flute",
    "Spore Sack": "spore_sack",
    "Blazing Flask": "blazing_flask",
    "Lunar Crown": "lunar_crown",
    "Solar Crown": "solar_crown",
    "Warden's Visor": "wardens_visor",
    "Verdant Mask": "verdant_mask",
    "Frostweave Veil": "frostweave_veil",
    "Stormcaller Circlet": "stormcaller_circlet",
    "Ashen Diadem": "ashen_diadem",
    "Wraith Crown": "wraith_crown",
    "Arcane Gauntlet": "arcane_gauntlet",
    "Iron Fist": "iron_fist",
    "Plague Grasp": "plague_grasp",
    "Sunforged Bracer": "sunforged_bracer",
    "Stormband": "stormband",
    "Gravestone Ring": "gravestone_ring",
    "Verdant Signet": "verdant_signet",
    "Phoenix Mantle": "phoenix_mantle",
    "Windrunner Cloak": "windrunner_cloak",
    "Abyssal Cape": "abyssal_cape",
    "Alchemist's Sash": "alchemists_sash",
    "Guardian's Girdle": "guardians_girdle",
    "Serpent Belt": "serpent_belt",
    "Lodestone Magnet": "lodestone_magnet",
    "Frostfire Pendant": "frostfire_pendant",
    "Tidekeeper Amulet": "tidekeeper_amulet",
    "Bloodstone Choker": "bloodstone_choker",
    "Thornweave Glove": "thornweave_glove",
    "Chrono Glove": "chrono_glove",
    "Stormstrider Boots": "stormstrider_boots",
    "Sandwalker Treads": "sandwalker_treads",
    "Emberstone Band": "emberstone_band",
    "Void Lantern": "void_lantern",
    "Thunderhorn": "thunderhorn",
    "Mending Chalice": "mending_chalice",
}


# ----------------------------------------------------------------------------
# Parse ability files: extract (relic_name, ability_name, required_level,
# cast_type) for each `new RelicAbility(...)` call and combine with the
# matching `registerAbility(...)` calls to map relic names to abilities.
# ----------------------------------------------------------------------------

def snake(name: str) -> str:
    s = re.sub(r"[^A-Za-z0-9]+", "_", name).strip("_").lower()
    s = re.sub(r"_+", "_", s)
    return s


RELIC_ABILITY_RE = re.compile(
    r'new\s+RelicAbility\(\s*"(?P<name>[^"]+)"\s*,\s*"(?P<desc>[^"]*)"\s*,\s*(?P<level>\d+)\s*,\s*(?:RelicAbility\.CastType\.(?P<cast>PASSIVE|INSTANTANEOUS|TOGGLE)\s*,)?',
    re.MULTILINE | re.DOTALL,
)

REGISTER_RE = re.compile(
    r'AbilityCastHandler\.registerAbility\(\s*"(?P<relic>[^"]+)"\s*,\s*"(?P<ability>[^"]+)"',
)


def parse_ability_file(path: Path) -> list[dict]:
    """Returns list of {relic, ability, level, cast_type, desc}."""
    text = path.read_text(encoding="utf-8", errors="ignore")

    # Establish relic name from registerAbility calls (all within one relic per file).
    register_calls = [(m["relic"], m["ability"]) for m in REGISTER_RE.finditer(text)]
    if not register_calls:
        return []
    relic_name = register_calls[0][0]

    abilities: list[dict] = []
    for m in RELIC_ABILITY_RE.finditer(text):
        ability_name = m["name"]
        level = int(m["level"])
        cast_type = m["cast"] or "INSTANTANEOUS"
        desc = m["desc"]
        abilities.append({
            "relic": relic_name,
            "ability": ability_name,
            "level": level,
            "cast_type": cast_type,
            "desc": desc,
        })

    # Some files may have register calls for abilities not in order;
    # filter in case some RelicAbility entries aren't registered.
    registered = {a for _, a in register_calls}
    return [a for a in abilities if a["ability"] in registered]


def collect_all() -> list[dict]:
    out: list[dict] = []
    for f in sorted(ABILITY_DIR.rglob("*.java")):
        if f.name in ("AbilityCastHandler.java", "AbilitySystem.java", "AbilityCooldownManager.java"):
            continue
        out.extend(parse_ability_file(f))
    return out


# ----------------------------------------------------------------------------
# Heuristics: pick school, deliver type, cooldown, impacts.
# ----------------------------------------------------------------------------

SCHOOL_KEYWORDS = [
    # (keywords, school)
    (["fire", "flame", "burn", "ignite", "blaze", "phoenix", "pyroclasm", "fireball", "combustion", "ember", "inferno", "solar", "sunfire", "sunforged", "magma"], "megamod:fire"),
    (["frost", "ice", "freeze", "frozen", "chill", "cold", "snow", "blizzard", "frostbite"], "megamod:frost"),
    (["heal", "mend", "regen", "cure", "restore", "purif", "sanctif", "cleanse", "blessed", "radiance"], "megamod:healing"),
    (["lightning", "storm", "thunder", "shock", "electric", "arc", "static", "galvanic", "tempest"], "megamod:lightning"),
    (["soul", "void", "shadow", "wraith", "phantom", "gravestone", "death", "undead", "wither"], "megamod:soul"),
    (["arcane", "magic", "mana", "chrono", "teleport", "warp", "recall", "blink", "dash", "spellweave", "mark"], "megamod:arcane"),
    (["arrow", "quiver", "rain", "throw", "bolt", "sonic", "shot", "barrage"], "megamod:physical_ranged"),
    (["poison", "venom", "toxic", "spore", "plague", "bloom"], "megamod:soul"),
]

# Abilities whose effect shape is clearly AREA/AoE.
AREA_KEYWORDS = ["aoe", "radius", "blast", "shock", "pulse", "ripple", "stomp", "eruption", "shockwave", "quake", "nova", "storm", "wave", "wings", "ground pound", "sonic", "pyroclasm", "aura", "blizzard", "sandstorm"]
PROJECTILE_KEYWORDS = ["bolt", "shot", "fireball", "projectile", "throw", "arrow", "bolt", "snap", "spore", "glaive", "bolt"]
SELF_BUFF_KEYWORDS = ["transmute", "brace", "stalwart", "spellweave", "berserk", "predator", "vanish", "eclipse", "phantom", "war cry", "galvanic", "undying", "tailwind", "absorb", "regen", "rebirth", "fury", "anchor", "store", "release", "recall", "feast", "mark", "warp", "growth", "bloom shield", "equilibrium", "infernal command", "ember sight", "lunar glow", "solar warmth", "tremor sense", "ethereal sight"]
TELEPORT_KEYWORDS = ["teleport", "warp", "recall", "blink", "dash", "shadow step", "teleport swap", "displace"]


def detect_school(ability_name: str, relic_name: str, desc: str) -> str:
    text = f"{ability_name} {relic_name} {desc}".lower()
    for keywords, school in SCHOOL_KEYWORDS:
        if any(k in text for k in keywords):
            return school
    return "megamod:arcane"


def detect_deliver_type(ability_name: str, desc: str) -> str:
    text = f"{ability_name} {desc}".lower()
    if any(k in text for k in TELEPORT_KEYWORDS):
        return "DIRECT"
    if "cone" in text or "breath" in text or "beam" in text:
        return "DIRECT"  # best fit w/o proper cone support
    if any(k in text for k in AREA_KEYWORDS):
        return "AREA"
    if any(k in text for k in PROJECTILE_KEYWORDS):
        return "PROJECTILE"
    if any(k in text for k in SELF_BUFF_KEYWORDS):
        return "DIRECT"
    return "DIRECT"


def detect_cooldown_seconds(ability_name: str, cast_type: str, required_level: int) -> float:
    if cast_type == "PASSIVE":
        return 0.0
    text = ability_name.lower()
    base = 8.0
    if any(k in text for k in ["nova", "pyroclasm", "eruption", "stasis", "stalwart", "wings", "whirlpool", "vacuum", "sandstorm", "elemental storm", "sanguine feast", "void rift"]):
        base = 40.0
    elif any(k in text for k in ["recall", "eclipse", "brace", "transmute", "sanctify", "radiance", "divine", "undying", "purif", "galvanic", "warp", "growth", "bloom", "rain", "dark beacon", "dimensional"]):
        base = 25.0
    elif any(k in text for k in ["bolt", "snap", "throw", "shot", "spit", "shock", "paralysis", "blink", "dash", "leap", "jump", "jetpack", "gust", "vine lash", "uppercut", "smite", "moon", "thunder", "store", "release", "feast", "mark"]):
        base = 6.0
    elif cast_type == "TOGGLE":
        base = 2.0
    return base * (1.0 + 0.05 * max(0, required_level - 1))


def _school_matches(school: str, candidates: list[str]) -> bool:
    return any(c in school for c in candidates)


def build_impacts(ability: dict, school: str, deliver_type: str) -> list[dict]:
    name = ability["ability"]
    desc = ability["desc"].lower()
    cast_type = ability["cast_type"]
    lname = name.lower()

    impacts: list[dict] = []

    # Healing abilities -> HEAL
    if _school_matches(school, ["healing"]) or any(k in lname for k in ["heal", "regen", "mend"]) or "heal" in desc:
        impacts.append({
            "action": {
                "type": "HEAL",
                "heal": {"spell_power_coefficient": 1.0},
            },
            "sound": {"id": "minecraft:entity.experience_orb.pickup", "volume": 0.7},
        })
        if "regen" in desc or "regen" in lname:
            impacts.append({
                "action": {
                    "type": "STATUS_EFFECT",
                    "status_effect": {
                        "effect_id": "minecraft:regeneration",
                        "duration": 6.0,
                        "amplifier": 0,
                    },
                },
            })
        return impacts

    # Pure buff/self-effect abilities
    if cast_type == "TOGGLE" or any(k in lname for k in ["vanish", "eclipse", "phantom", "berserk", "brace", "stalwart", "predator", "aura", "anchor", "spellweave", "galvanic", "undying", "warmth", "glide", "momentum", "equilibrium", "infernal command", "ember sight", "lunar glow", "solar warmth", "tremor sense", "ethereal sight", "agility", "tailwind", "fury", "temporal flux", "heavy blows", "charged steps", "lightning step", "constrictor", "thorned grasp", "backstab", "fuel efficiency"]):
        # map to strength / speed / resistance
        effect = "minecraft:strength"
        if any(k in lname for k in ["glide", "aura", "tailwind", "agility", "momentum", "lightning step", "charged steps", "tempest"]):
            effect = "minecraft:speed"
        elif any(k in lname for k in ["brace", "stalwart", "warmth", "anchor", "undying", "fortitude", "equilibrium"]):
            effect = "minecraft:resistance"
        elif any(k in lname for k in ["vanish", "eclipse", "phantom", "shroud"]):
            effect = "minecraft:invisibility"
        elif any(k in lname for k in ["ember sight", "lunar glow", "ethereal sight", "tremor sense"]):
            effect = "minecraft:night_vision"
        elif any(k in lname for k in ["predator", "tracker"]):
            effect = "minecraft:glowing"
        duration = 20.0
        if cast_type == "TOGGLE":
            duration = 12.0
        impacts.append({
            "action": {
                "type": "STATUS_EFFECT",
                "apply_to_caster": True,
                "status_effect": {
                    "effect_id": effect,
                    "duration": duration,
                    "amplifier": 1,
                },
            },
        })
        return impacts

    # Teleport abilities
    if any(k in lname for k in TELEPORT_KEYWORDS):
        impacts.append({
            "action": {
                "type": "TELEPORT",
                "apply_to_caster": True,
                "teleport": {
                    "mode": "FORWARD",
                    "forward": {"distance": 8.0},
                },
            },
            "sound": {"id": "minecraft:entity.enderman.teleport"},
        })
        return impacts

    # Default: damage ability
    base_damage = 1.0
    if any(k in lname for k in ["pyroclasm", "devast", "stalwart", "undying", "inferno", "stasis", "sanguine", "sonic boom", "divine smite", "ground pound", "thunder", "elemental storm"]):
        base_damage = 1.4
    elif any(k in lname for k in ["bolt", "snap", "spit", "blast", "shot", "shock"]):
        base_damage = 0.7
    dmg_impact = {
        "action": {
            "type": "DAMAGE",
            "damage": {"spell_power_coefficient": base_damage, "knockback": 0.6},
        },
    }
    impacts.append(dmg_impact)

    # Fire on-hit for fire school
    if _school_matches(school, ["fire"]):
        impacts.append({"action": {"type": "FIRE", "fire": {"duration": 4.0}}})

    # Wither / soul / poison status
    if _school_matches(school, ["soul"]) or "wither" in lname or "wither" in desc:
        impacts.append({
            "action": {
                "type": "STATUS_EFFECT",
                "status_effect": {
                    "effect_id": "minecraft:wither",
                    "duration": 4.0,
                    "amplifier": 0,
                },
            },
        })
    if "poison" in lname or "venom" in lname or "plague" in lname or "spore" in lname or "verdant" in lname:
        impacts.append({
            "action": {
                "type": "STATUS_EFFECT",
                "status_effect": {
                    "effect_id": "minecraft:poison",
                    "duration": 6.0,
                    "amplifier": 0,
                },
            },
        })
    # Frost slow
    if _school_matches(school, ["frost"]):
        impacts.append({
            "action": {
                "type": "STATUS_EFFECT",
                "status_effect": {
                    "effect_id": "minecraft:slowness",
                    "duration": 4.0,
                    "amplifier": 1,
                },
            },
        })
    # Lightning stun
    if _school_matches(school, ["lightning"]) and any(k in lname for k in ["shock", "stun", "paralysis", "arc", "thunder"]):
        impacts.append({
            "action": {
                "type": "STATUS_EFFECT",
                "status_effect": {
                    "effect_id": "minecraft:slowness",
                    "duration": 3.0,
                    "amplifier": 3,
                },
            },
        })
    return impacts


def build_spell(ability: dict) -> dict:
    name = ability["ability"]
    desc = ability["desc"]
    cast_type = ability["cast_type"]
    required_level = ability["level"]
    relic_name = ability["relic"]
    school = detect_school(name, relic_name, desc)
    deliver_type = detect_deliver_type(name, desc)
    lname = name.lower()

    if cast_type == "PASSIVE":
        # Passive-tick: deliver DIRECT self impact, triggered on player effect tick.
        impacts = build_impacts(ability, school, "DIRECT")
        # Self-buff-style passives: mark them MODIFIER-ish via PASSIVE with EFFECT_TICK.
        return {
            "school": school,
            "range": 8.0,
            "tier": max(1, required_level // 2),
            "type": "PASSIVE",
            "passive": {
                "triggers": [
                    {"type": "DAMAGE_TAKEN", "stage": "PRE", "chance": 0.25},
                ]
            },
            "target": {"type": "CASTER"},
            "deliver": {"type": "DIRECT"},
            "impacts": impacts,
        }

    spell: dict = {
        "school": school,
        "range": 12.0 if deliver_type in ("PROJECTILE", "DIRECT") else 6.0,
        "group": "relic",
        "tier": max(1, required_level // 2),
        "active": {
            "cast": {"duration": 0.2 if cast_type == "INSTANTANEOUS" else 0.0},
        },
        "release": {},
        "target": {
            "type": {
                "PROJECTILE": "AIM",
                "DIRECT": "CASTER",
                "AREA": "AREA",
                "BEAM": "BEAM",
                "METEOR": "AIM",
            }.get(deliver_type, "CASTER"),
        },
    }

    if deliver_type == "AREA":
        spell["target"] = {"type": "AREA", "area": {"horizontal_range_multiplier": 1.0, "vertical_range_multiplier": 0.6}}
        spell["range"] = 6.0
        spell["deliver"] = {"type": "DIRECT"}
    elif deliver_type == "PROJECTILE":
        spell["target"] = {"type": "AIM", "aim": {}}
        spell["deliver"] = {
            "type": "PROJECTILE",
            "projectile": {
                "launch_properties": {"velocity": 1.5},
                "projectile": {
                    "client_data": {"light_level": 6},
                },
            },
        }
    elif deliver_type == "METEOR":
        spell["target"] = {"type": "AIM", "aim": {}}
        spell["deliver"] = {
            "type": "METEOR",
            "meteor": {"launch_height": 10.0, "launch_radius": 2.0},
        }
    elif deliver_type == "BEAM":
        spell["target"] = {"type": "BEAM", "beam": {}}
        spell["deliver"] = {"type": "DIRECT"}
    else:
        spell["deliver"] = {"type": "DIRECT"}
        # For self-buff-style, target caster
        if any(k in lname for k in SELF_BUFF_KEYWORDS) or cast_type == "TOGGLE":
            spell["target"] = {"type": "CASTER"}

    spell["impacts"] = build_impacts(ability, school, deliver_type)

    cd = detect_cooldown_seconds(name, cast_type, required_level)
    spell["cost"] = {
        "exhaust": 0.15,
        "cooldown": {"duration": cd},
    }

    # Release sound per school
    release_sound = {
        "megamod:fire": "minecraft:entity.blaze.shoot",
        "megamod:frost": "minecraft:block.glass.break",
        "megamod:healing": "minecraft:entity.experience_orb.pickup",
        "megamod:lightning": "minecraft:entity.lightning_bolt.thunder",
        "megamod:arcane": "minecraft:entity.evoker.cast_spell",
        "megamod:soul": "minecraft:entity.wither.shoot",
        "megamod:physical_melee": "minecraft:entity.player.attack.sweep",
        "megamod:physical_ranged": "minecraft:entity.arrow.shoot",
    }.get(school, "minecraft:entity.evoker.cast_spell")
    spell["release"]["sound"] = {"id": release_sound, "volume": 0.7}

    return spell


# ----------------------------------------------------------------------------
# Main generation
# ----------------------------------------------------------------------------

def ability_spell_id(ability: dict) -> str:
    relic_snake = snake(ability["relic"].replace("'s", "s").replace("'", ""))
    ability_snake = snake(ability["ability"])
    return f"relic_{relic_snake}_{ability_snake}"


def main():
    abilities = collect_all()
    print(f"Found {len(abilities)} ability entries")

    OUT_SPELL_DIR.mkdir(parents=True, exist_ok=True)

    written: list[tuple[str, list[str]]] = []  # (relic_name, [spell_ids])
    per_relic: dict[str, list[str]] = {}
    for a in abilities:
        spell_id = ability_spell_id(a)
        spell = build_spell(a)
        out_path = OUT_SPELL_DIR / f"{spell_id}.json"
        out_path.write_text(json.dumps(spell, indent=2) + "\n", encoding="utf-8")
        per_relic.setdefault(a["relic"], []).append(f"megamod:{spell_id}")

    # Collect level + ability metadata per spell id
    spell_meta: list[tuple[str, str, int]] = []  # (spell_id, ability_name, required_level)
    for a in abilities:
        sid = f"megamod:{ability_spell_id(a)}"
        spell_meta.append((sid, a["ability"], a["level"]))

    # Emit Java mapping file
    lines = [
        "package com.ultra.megamod.feature.relics;",
        "",
        "import java.util.HashMap;",
        "import java.util.List;",
        "import java.util.Map;",
        "",
        "/**",
        " * Auto-generated by scripts/generate_relic_spells.py - Phase G.1.",
        " * Maps each relic item registry ID to the list of SpellEngine spell IDs for its abilities.",
        " * RelicRegistry uses this to attach a SpellContainer data component.",
        " * Also exposes per-spell metadata (ability display name, required relic level)",
        " * used by the level-gate cast predicate.",
        " */",
        "public final class RelicSpellAssignments {",
        "    private RelicSpellAssignments() {}",
        "",
        "    public static final Map<String, List<String>> SPELLS;",
        "    /** spell id -> (ability display name, required level) */",
        "    public static final Map<String, SpellMeta> META;",
        "",
        "    public record SpellMeta(String abilityName, int requiredLevel) {}",
        "",
        "    static {",
        "        Map<String, List<String>> spells = new HashMap<>();",
    ]
    for relic_name, spell_ids in sorted(per_relic.items()):
        item_id = RELIC_TO_ITEM_ID.get(relic_name)
        if item_id is None:
            print(f"WARN: no item-id mapping for relic '{relic_name}'; skipped")
            continue
        joined = ", ".join(f'"{sid}"' for sid in spell_ids)
        lines.append(f'        spells.put("megamod:{item_id}", List.of({joined}));')
    lines.append("        SPELLS = Map.copyOf(spells);")
    lines.append("")
    lines.append("        Map<String, SpellMeta> meta = new HashMap<>();")
    for sid, ability_name, required_level in spell_meta:
        escaped = ability_name.replace('"', '\\"')
        lines.append(f'        meta.put("{sid}", new SpellMeta("{escaped}", {required_level}));')
    lines.append("        META = Map.copyOf(meta);")
    lines.append("    }")
    lines.append("")
    lines.append("    public static List<String> forItem(String itemId) {")
    lines.append("        return SPELLS.getOrDefault(itemId, List.of());")
    lines.append("    }")
    lines.append("")
    lines.append("    public static SpellMeta metaFor(String spellId) {")
    lines.append("        return META.get(spellId);")
    lines.append("    }")
    lines.append("}")
    lines.append("")
    OUT_JAVA.write_text("\n".join(lines), encoding="utf-8")

    print(f"Wrote {len(abilities)} spell JSONs + {OUT_JAVA.name}")
    # Count by PASSIVE vs active
    active = sum(1 for a in abilities if a["cast_type"] != "PASSIVE")
    passive = sum(1 for a in abilities if a["cast_type"] == "PASSIVE")
    print(f"  active: {active}, passive: {passive}")


if __name__ == "__main__":
    main()
