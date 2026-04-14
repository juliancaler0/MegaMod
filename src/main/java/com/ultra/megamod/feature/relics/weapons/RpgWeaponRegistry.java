/*
 * Decompiled with CFR 0.152.
 */
package com.ultra.megamod.feature.relics.weapons;

import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpgWeaponRegistry {
    private static final Map<String, List<RpgWeaponItem.WeaponSkill>> WEAPON_SKILLS = new HashMap<String, List<RpgWeaponItem.WeaponSkill>>();
    private static final Map<String, RpgWeaponItem.WeaponSkill> ALL_SKILLS_BY_NAME = new HashMap<>();

    public static List<RpgWeaponItem.WeaponSkill> getSkillsForWeapon(String registryName) {
        return WEAPON_SKILLS.getOrDefault(registryName, List.of());
    }

    public static boolean isRpgWeapon(String registryName) {
        return WEAPON_SKILLS.containsKey(registryName);
    }

    public static Iterable<String> getAllWeaponNames() {
        return WEAPON_SKILLS.keySet();
    }

    /** Lookup any weapon skill by name (e.g. "Chain Lightning") */
    public static RpgWeaponItem.WeaponSkill getSkillByName(String name) {
        return ALL_SKILLS_BY_NAME.get(name);
    }

    /** Returns all unique skill names across all weapons */
    public static java.util.Collection<String> getAllSkillNames() {
        return ALL_SKILLS_BY_NAME.keySet();
    }

    /** Returns all unique skills */
    public static java.util.Collection<RpgWeaponItem.WeaponSkill> getAllSkills() {
        return ALL_SKILLS_BY_NAME.values();
    }

    static {
        // Original RPG Weapons
        WEAPON_SKILLS.put("megamod:vampiric_tome", List.of(new RpgWeaponItem.WeaponSkill("Drain Life", "Siphon life from your target, dealing damage and healing yourself", 80), new RpgWeaponItem.WeaponSkill("Blood Pact", "Sacrifice 4 HP to empower your next attack with +50% bonus damage for 5 seconds", 160)));
        WEAPON_SKILLS.put("megamod:static_seeker", List.of(new RpgWeaponItem.WeaponSkill("Chain Lightning", "Unleash a bolt of lightning that bounces between up to 3 targets", 100), new RpgWeaponItem.WeaponSkill("Overcharge", "Channel static energy so your next melee hit deals 3x damage", 200)));
        WEAPON_SKILLS.put("megamod:battledancer", List.of(new RpgWeaponItem.WeaponSkill("Whirlwind", "Spin with your blade, dealing damage to all enemies in a 4-block radius", 80), new RpgWeaponItem.WeaponSkill("Riposte", "Enter a parry stance for 3 seconds; the next attack against you is reflected", 240)));
        WEAPON_SKILLS.put("megamod:ebonchill", List.of(new RpgWeaponItem.WeaponSkill("Frost Nova", "Flash-freeze all enemies in a 5-block radius, slowing and damaging them", 120), new RpgWeaponItem.WeaponSkill("Icicle Lance", "Fire a piercing ice shard that damages all enemies in a line", 100)));
        WEAPON_SKILLS.put("megamod:lightbinder", List.of(new RpgWeaponItem.WeaponSkill("Holy Smite", "Call down holy light on your target, dealing magic damage and healing you", 80), new RpgWeaponItem.WeaponSkill("Sacred Shield", "Conjure a barrier that absorbs the next incoming hit for 8 seconds", 200)));
        WEAPON_SKILLS.put("megamod:crescent_blade", List.of(new RpgWeaponItem.WeaponSkill("Crescent Slash", "Execute a wide arc slash that damages all enemies in front of you", 60), new RpgWeaponItem.WeaponSkill("Shadow Dash", "Dash 8 blocks forward through enemies, damaging each one you pass through", 160)));
        WEAPON_SKILLS.put("megamod:ghost_fang", List.of(new RpgWeaponItem.WeaponSkill("Spectral Bite", "Strike with ghostly fangs that bypass armor, dealing true damage", 80), new RpgWeaponItem.WeaponSkill("Phase", "Become briefly intangible for 2 seconds, avoiding all damage", 300)));
        WEAPON_SKILLS.put("megamod:terra_warhammer", List.of(new RpgWeaponItem.WeaponSkill("Earthquake", "Slam the ground, dealing AOE damage and knocking back all nearby enemies", 100), new RpgWeaponItem.WeaponSkill("Fortify", "Harden your body with earth magic, gaining bonus armor for 10 seconds", 200)));
        WEAPON_SKILLS.put("megamod:soka_singing_blade", List.of(new RpgWeaponItem.WeaponSkill("Annihilating Slash", "Unleash a devastating dimensional rift slash that deals 5x weapon damage to all enemies in a 6-block cone, ignoring armor and shields", 120), new RpgWeaponItem.WeaponSkill("Grand Arcanum", "Channel ancient arcane power for 3 seconds, then release a massive energy nova dealing 8x damage in a 10-block radius while granting yourself Resistance III, Regeneration II, and Speed III for 8 seconds", 400), new RpgWeaponItem.WeaponSkill("Archon's Judgment", "Mark your target with divine judgment for 5 seconds — the mark amplifies all damage they receive by 50% and when it expires, it detonates for 10x weapon damage split among all enemies within 8 blocks", 600)));

        // Arsenal unique_* weapons are wired via SpellEngine SpellContainer (see ArsenalWeapons.java).
        // Their passive/active spells are attached as data components — do NOT register them here.

        // New Core RPG Weapons
        WEAPON_SKILLS.put("megamod:voidreaver", List.of(new RpgWeaponItem.WeaponSkill("Nether Rend", "Shadow arc that bypasses 50% of target's armor", 100), new RpgWeaponItem.WeaponSkill("Dimensional Collapse", "Create a void singularity that pulls and damages all nearby enemies", 240)));
        WEAPON_SKILLS.put("megamod:solaris", List.of(new RpgWeaponItem.WeaponSkill("Consecrate", "Holy fire circle that damages enemies and heals allies", 120), new RpgWeaponItem.WeaponSkill("Judgment", "Mark a target, then detonate for massive holy damage", 200)));
        WEAPON_SKILLS.put("megamod:stormfury", List.of(new RpgWeaponItem.WeaponSkill("Lightning Dash", "Dash through enemies dealing lightning damage to each", 80), new RpgWeaponItem.WeaponSkill("Thunder God's Descent", "Leap into the sky and slam down with devastating AOE", 200)));
        WEAPON_SKILLS.put("megamod:briarthorn", List.of(new RpgWeaponItem.WeaponSkill("Entangling Roots", "Root and poison all enemies in an area", 120), new RpgWeaponItem.WeaponSkill("Nature's Wrath", "Rain thorns from above, dealing heavy poison damage", 180)));
        WEAPON_SKILLS.put("megamod:abyssal_trident", List.of(new RpgWeaponItem.WeaponSkill("Tidal Surge", "Send a water wave that knocks back all enemies", 80), new RpgWeaponItem.WeaponSkill("Maelstrom", "Create a whirlpool that pulls and damages enemies", 240)));
        WEAPON_SKILLS.put("megamod:pyroclast", List.of(new RpgWeaponItem.WeaponSkill("Molten Strike", "Create a lava fissure line that burns enemies", 100), new RpgWeaponItem.WeaponSkill("Eruption", "Volcanic explosion launching enemies and raining fire", 260)));
        WEAPON_SKILLS.put("megamod:whisperwind", List.of(new RpgWeaponItem.WeaponSkill("Piercing Gale", "Wind projectile that pierces through all enemies", 60), new RpgWeaponItem.WeaponSkill("Cyclone Volley", "Launch 8 homing wind arrows at nearby enemies", 200)));
        WEAPON_SKILLS.put("megamod:soulchain", List.of(new RpgWeaponItem.WeaponSkill("Soul Lash", "Chain that pulls the first enemy hit toward you", 80), new RpgWeaponItem.WeaponSkill("Reaping Harvest", "Spin attack that damages all nearby and heals per hit", 160)));

        // Other Arsenal unique_* (whips/wands/katanas/greatshields/throwing axes/rapiers/_3 tier variants)
        // are handled by SpellEngine when wired — no hardcoded skills here.

        // Build global skill-by-name lookup
        for (List<RpgWeaponItem.WeaponSkill> skills : WEAPON_SKILLS.values()) {
            for (RpgWeaponItem.WeaponSkill skill : skills) {
                ALL_SKILLS_BY_NAME.put(skill.name(), skill);
            }
        }
    }
}

