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

        // Arsenal Claymores
        WEAPON_SKILLS.put("megamod:unique_claymore_1", List.of(new RpgWeaponItem.WeaponSkill("Explosive Strike", "Trigger a fiery explosion at target, dealing AOE damage", 100), new RpgWeaponItem.WeaponSkill("Flame Cleave", "Wide fire-infused slash hitting all enemies in front", 200)));
        WEAPON_SKILLS.put("megamod:unique_claymore_2", List.of(new RpgWeaponItem.WeaponSkill("Radiant Strike", "Strike target with holy damage and heal nearby allies", 80), new RpgWeaponItem.WeaponSkill("Holy Cleave", "Wide holy arc that damages enemies and heals allies", 160)));
        WEAPON_SKILLS.put("megamod:unique_claymore_sw", List.of(new RpgWeaponItem.WeaponSkill("Soul Rampage", "Enter a rage, gaining increased damage and speed", 100), new RpgWeaponItem.WeaponSkill("Annihilating Slash", "Devastating single-target strike with soul fire", 240)));

        // Arsenal Daggers
        WEAPON_SKILLS.put("megamod:unique_dagger_1", List.of(new RpgWeaponItem.WeaponSkill("Frostbite", "Freeze target, slowing movement and attack speed", 60), new RpgWeaponItem.WeaponSkill("Frozen Barrage", "Rapid ice strikes that freeze all nearby enemies", 120)));
        WEAPON_SKILLS.put("megamod:unique_dagger_2", List.of(new RpgWeaponItem.WeaponSkill("Life Leech", "Drain life from target, dealing damage and healing you", 60), new RpgWeaponItem.WeaponSkill("Shadow Strike", "Teleport behind target and deliver a devastating backstab", 120)));
        WEAPON_SKILLS.put("megamod:unique_dagger_sw", List.of(new RpgWeaponItem.WeaponSkill("Armor Sunder", "Shatter target's defenses, reducing their armor", 60), new RpgWeaponItem.WeaponSkill("Apocalypse Mark", "Mark target for death, making them glow and weakening them", 200)));

        // Arsenal Double Axes
        WEAPON_SKILLS.put("megamod:unique_double_axe_1", List.of(new RpgWeaponItem.WeaponSkill("Butcher's Leech", "Drain life with a brutal axe strike", 80), new RpgWeaponItem.WeaponSkill("Cleaving Frenzy", "Dual spin attack hitting all nearby enemies twice", 160)));
        WEAPON_SKILLS.put("megamod:unique_double_axe_2", List.of(new RpgWeaponItem.WeaponSkill("Wither Strike", "Inflict powerful Wither effect on target", 80), new RpgWeaponItem.WeaponSkill("Reaping Swing", "Wide wither-infused swing damaging and decaying all nearby", 160)));
        WEAPON_SKILLS.put("megamod:unique_double_axe_sw", List.of(new RpgWeaponItem.WeaponSkill("War Rampage", "Enter a battle rage, stacking damage and speed", 100), new RpgWeaponItem.WeaponSkill("Battle Cry", "Rally yourself and nearby allies with strength and resistance", 200)));

        // Arsenal Glaives
        WEAPON_SKILLS.put("megamod:unique_glaive_1", List.of(new RpgWeaponItem.WeaponSkill("Flame Strike", "Create a cloud of fire at target's location", 80), new RpgWeaponItem.WeaponSkill("Infernal Thrust", "Long-range piercing fire thrust through enemies", 140)));
        WEAPON_SKILLS.put("megamod:unique_glaive_2", List.of(new RpgWeaponItem.WeaponSkill("Shockwave", "Send a piercing shockwave forward through enemies", 80), new RpgWeaponItem.WeaponSkill("Crystal Sweep", "Wide sweep attack with massive knockback", 140)));
        WEAPON_SKILLS.put("megamod:unique_glaive_sw", List.of(new RpgWeaponItem.WeaponSkill("Fel Swirl", "Spin attack with fel-fire damaging all nearby", 80), new RpgWeaponItem.WeaponSkill("Spine Thrust", "Extended range piercing thrust through enemies", 160)));

        // Arsenal Hammers
        WEAPON_SKILLS.put("megamod:unique_hammer_1", List.of(new RpgWeaponItem.WeaponSkill("Shockwave Slam", "Slam the ground creating a shockwave", 100), new RpgWeaponItem.WeaponSkill("Destined Impact", "Massive single-target hit that stuns", 200)));
        WEAPON_SKILLS.put("megamod:unique_hammer_2", List.of(new RpgWeaponItem.WeaponSkill("Explosive Impact", "Trigger a fiery explosion on target", 100), new RpgWeaponItem.WeaponSkill("Scorched Earth", "Create a massive fire explosion around you", 200)));
        WEAPON_SKILLS.put("megamod:unique_hammer_sw", List.of(new RpgWeaponItem.WeaponSkill("Holy Radiance", "Heal allies and deal extra damage to undead", 80), new RpgWeaponItem.WeaponSkill("Sanctify", "Purify a large area, healing allies and smiting undead", 200)));

        // Arsenal Maces
        WEAPON_SKILLS.put("megamod:unique_mace_1", List.of(new RpgWeaponItem.WeaponSkill("Armor Sunder", "Crush target's armor, reducing their defenses", 80), new RpgWeaponItem.WeaponSkill("Bone Crush", "Devastating hit that weakens and slows the target", 160)));
        WEAPON_SKILLS.put("megamod:unique_mace_2", List.of(new RpgWeaponItem.WeaponSkill("Stun Strike", "Strike that stuns the target briefly", 80), new RpgWeaponItem.WeaponSkill("Storm Smash", "Lightning AOE damaging all nearby enemies", 160)));
        WEAPON_SKILLS.put("megamod:unique_mace_sw", List.of(new RpgWeaponItem.WeaponSkill("Guarding Strike", "Strike target and gain a defensive buff", 80), new RpgWeaponItem.WeaponSkill("Archon's Judgment", "Magic damage strike with absorption and resistance", 200)));

        // Arsenal Sickles
        WEAPON_SKILLS.put("megamod:unique_sickle_1", List.of(new RpgWeaponItem.WeaponSkill("Poison Cloud", "Create a toxic cloud that poisons nearby enemies", 80), new RpgWeaponItem.WeaponSkill("Toxic Slash", "Slash enemies in front, poisoning all hit", 120)));
        WEAPON_SKILLS.put("megamod:unique_sickle_2", List.of(new RpgWeaponItem.WeaponSkill("Infernal Explosion", "Trigger a fiery explosion on target", 80), new RpgWeaponItem.WeaponSkill("Soul Harvest", "Reap life from all nearby enemies", 120)));
        WEAPON_SKILLS.put("megamod:unique_sickle_sw", List.of(new RpgWeaponItem.WeaponSkill("Swirling Reap", "Spin attack damaging all nearby enemies", 80), new RpgWeaponItem.WeaponSkill("Elven Harvest", "Multi-hit combo that heals you per hit", 160)));

        // Arsenal Spears
        WEAPON_SKILLS.put("megamod:unique_spear_1", List.of(new RpgWeaponItem.WeaponSkill("Frostbite Thrust", "Thrust that slows and freezes the target", 80), new RpgWeaponItem.WeaponSkill("Sonic Impale", "Long-range piercing thrust through enemies", 140)));
        WEAPON_SKILLS.put("megamod:unique_spear_2", List.of(new RpgWeaponItem.WeaponSkill("Stun Thrust", "Thrust that stuns the target", 80), new RpgWeaponItem.WeaponSkill("Divine Impale", "Holy piercing thrust through enemies", 140)));
        WEAPON_SKILLS.put("megamod:unique_spear_sw", List.of(new RpgWeaponItem.WeaponSkill("Vengeance Leech", "Drain life from target with a vengeful thrust", 80), new RpgWeaponItem.WeaponSkill("Mounting Strike", "Triple-hit combo with escalating damage", 160)));

        // Arsenal Longsword
        WEAPON_SKILLS.put("megamod:unique_longsword_sw", List.of(new RpgWeaponItem.WeaponSkill("Armor Sunder", "Shatter target's defenses with a powerful strike", 80), new RpgWeaponItem.WeaponSkill("Dragon Strike", "Massive fire-trail slash hitting all enemies in front", 160)));

        // Arsenal Longbows
        WEAPON_SKILLS.put("megamod:unique_longbow_1", List.of(new RpgWeaponItem.WeaponSkill("Radiant Arrow", "Fire a radiant arrow that heals you on hit", 60), new RpgWeaponItem.WeaponSkill("Solar Volley", "Fire multiple radiant arrows at nearby enemies", 160)));
        WEAPON_SKILLS.put("megamod:unique_longbow_2", List.of(new RpgWeaponItem.WeaponSkill("Withering Shot", "Fire an arrow that inflicts strong Wither", 60), new RpgWeaponItem.WeaponSkill("Dark Barrage", "Rapid fire wither arrows at multiple targets", 160)));
        WEAPON_SKILLS.put("megamod:unique_longbow_sw", List.of(new RpgWeaponItem.WeaponSkill("Focusing Shot", "Focus your aim, gaining a damage buff", 60), new RpgWeaponItem.WeaponSkill("Arrow Rain", "Unleash a massive volley of arrows on enemies", 200)));

        // Arsenal Heavy Crossbows
        WEAPON_SKILLS.put("megamod:unique_heavy_crossbow_1", List.of(new RpgWeaponItem.WeaponSkill("Flame Bolt", "Fire a bolt that creates a fire cloud on impact", 80), new RpgWeaponItem.WeaponSkill("Phoenix Shot", "Massive fire bolt that explodes on impact", 200)));
        WEAPON_SKILLS.put("megamod:unique_heavy_crossbow_2", List.of(new RpgWeaponItem.WeaponSkill("Toxic Bolt", "Fire a bolt that creates a poison cloud on impact", 80), new RpgWeaponItem.WeaponSkill("Death Bolt", "Massive bolt spreading wither to all nearby", 200)));
        WEAPON_SKILLS.put("megamod:unique_heavy_crossbow_sw", List.of(new RpgWeaponItem.WeaponSkill("Multi-Shot", "Fire multiple bolts at nearby enemies", 60), new RpgWeaponItem.WeaponSkill("Relentless Barrage", "Rapid fire bolts at all enemies in range", 200)));

        // Arsenal Damage Staves
        WEAPON_SKILLS.put("megamod:unique_staff_damage_1", List.of(new RpgWeaponItem.WeaponSkill("Frost Bolt", "Launch a frost bolt that slows the target", 60), new RpgWeaponItem.WeaponSkill("Cooldown Reset", "Reset all your ability cooldowns", 200)));
        WEAPON_SKILLS.put("megamod:unique_staff_damage_2", List.of(new RpgWeaponItem.WeaponSkill("Chain Reaction", "Launch a chain missile that bounces between 3 targets", 100), new RpgWeaponItem.WeaponSkill("Arcane Blast", "Concentrated arcane damage on a single target", 60)));
        WEAPON_SKILLS.put("megamod:unique_staff_damage_3", List.of(new RpgWeaponItem.WeaponSkill("Flame Cloud", "Create a fire zone at target's location", 80), new RpgWeaponItem.WeaponSkill("Dragon Breath", "Cone of fire damage in front of you", 140)));
        WEAPON_SKILLS.put("megamod:unique_staff_damage_4", List.of(new RpgWeaponItem.WeaponSkill("Soul Leech", "Drain life from target, dealing magic damage and healing", 80), new RpgWeaponItem.WeaponSkill("Gargoyle's Curse", "Curse target with wither and slowness", 160)));
        WEAPON_SKILLS.put("megamod:unique_staff_damage_5", List.of(new RpgWeaponItem.WeaponSkill("Shockwave Burst", "Send shockwaves in 4 directions", 100), new RpgWeaponItem.WeaponSkill("Arcane Barrage", "Rapid fire arcane bolts at multiple targets", 60)));
        WEAPON_SKILLS.put("megamod:unique_staff_damage_6", List.of(new RpgWeaponItem.WeaponSkill("Frosty Puddle", "Create a freezing zone that slows enemies", 80), new RpgWeaponItem.WeaponSkill("Blizzard", "Massive frost AOE freezing and damaging all nearby", 200)));
        WEAPON_SKILLS.put("megamod:unique_staff_damage_sw", List.of(new RpgWeaponItem.WeaponSkill("Surging Power", "Gain stacking spell power and haste buff", 100), new RpgWeaponItem.WeaponSkill("Grand Arcanum", "Massive arcane explosion damaging all nearby", 240)));

        // Arsenal Healing Staves
        WEAPON_SKILLS.put("megamod:unique_staff_heal_1", List.of(new RpgWeaponItem.WeaponSkill("Radiance", "Heal yourself and nearby allies", 60), new RpgWeaponItem.WeaponSkill("Crystal Shield", "Grant absorption shields to yourself and allies", 120)));
        WEAPON_SKILLS.put("megamod:unique_staff_heal_2", List.of(new RpgWeaponItem.WeaponSkill("Guardian Remedy", "Heal yourself and gain absorption shield", 80), new RpgWeaponItem.WeaponSkill("Purify", "Remove all negative effects from you and nearby allies", 120)));
        WEAPON_SKILLS.put("megamod:unique_staff_heal_sw", List.of(new RpgWeaponItem.WeaponSkill("Cooldown Touch", "Reset all cooldowns when below 50% health", 100), new RpgWeaponItem.WeaponSkill("Sin'dorei Blessing", "Massive heal with regeneration, absorption, and resistance", 200)));

        // Arsenal Shields
        WEAPON_SKILLS.put("megamod:unique_shield_1", List.of(new RpgWeaponItem.WeaponSkill("Spiked Block", "Gain resistance and reflect damage to attackers", 60), new RpgWeaponItem.WeaponSkill("Shield Bash", "Bash nearby enemies, stunning and knocking them back", 120)));
        WEAPON_SKILLS.put("megamod:unique_shield_2", List.of(new RpgWeaponItem.WeaponSkill("Guarding Aura", "Grant damage resistance to yourself and nearby allies", 80), new RpgWeaponItem.WeaponSkill("Light Barrier", "Create a powerful absorption and resistance barrier", 160)));
        WEAPON_SKILLS.put("megamod:unique_shield_sw", List.of(new RpgWeaponItem.WeaponSkill("Unyielding", "Gain powerful damage resistance", 60), new RpgWeaponItem.WeaponSkill("Bulwark Slam", "Slam the ground, massively knocking back all enemies", 160)));

        // New Core RPG Weapons
        WEAPON_SKILLS.put("megamod:voidreaver", List.of(new RpgWeaponItem.WeaponSkill("Nether Rend", "Shadow arc that bypasses 50% of target's armor", 100), new RpgWeaponItem.WeaponSkill("Dimensional Collapse", "Create a void singularity that pulls and damages all nearby enemies", 240)));
        WEAPON_SKILLS.put("megamod:solaris", List.of(new RpgWeaponItem.WeaponSkill("Consecrate", "Holy fire circle that damages enemies and heals allies", 120), new RpgWeaponItem.WeaponSkill("Judgment", "Mark a target, then detonate for massive holy damage", 200)));
        WEAPON_SKILLS.put("megamod:stormfury", List.of(new RpgWeaponItem.WeaponSkill("Lightning Dash", "Dash through enemies dealing lightning damage to each", 80), new RpgWeaponItem.WeaponSkill("Thunder God's Descent", "Leap into the sky and slam down with devastating AOE", 200)));
        WEAPON_SKILLS.put("megamod:briarthorn", List.of(new RpgWeaponItem.WeaponSkill("Entangling Roots", "Root and poison all enemies in an area", 120), new RpgWeaponItem.WeaponSkill("Nature's Wrath", "Rain thorns from above, dealing heavy poison damage", 180)));
        WEAPON_SKILLS.put("megamod:abyssal_trident", List.of(new RpgWeaponItem.WeaponSkill("Tidal Surge", "Send a water wave that knocks back all enemies", 80), new RpgWeaponItem.WeaponSkill("Maelstrom", "Create a whirlpool that pulls and damages enemies", 240)));
        WEAPON_SKILLS.put("megamod:pyroclast", List.of(new RpgWeaponItem.WeaponSkill("Molten Strike", "Create a lava fissure line that burns enemies", 100), new RpgWeaponItem.WeaponSkill("Eruption", "Volcanic explosion launching enemies and raining fire", 260)));
        WEAPON_SKILLS.put("megamod:whisperwind", List.of(new RpgWeaponItem.WeaponSkill("Piercing Gale", "Wind projectile that pierces through all enemies", 60), new RpgWeaponItem.WeaponSkill("Cyclone Volley", "Launch 8 homing wind arrows at nearby enemies", 200)));
        WEAPON_SKILLS.put("megamod:soulchain", List.of(new RpgWeaponItem.WeaponSkill("Soul Lash", "Chain that pulls the first enemy hit toward you", 80), new RpgWeaponItem.WeaponSkill("Reaping Harvest", "Spin attack that damages all nearby and heals per hit", 160)));

        // New Arsenal Whips
        WEAPON_SKILLS.put("megamod:unique_whip_1", List.of(new RpgWeaponItem.WeaponSkill("Venom Crack", "Crack whip at target dealing damage and poisoning", 80), new RpgWeaponItem.WeaponSkill("Constrict", "Pull and trap enemies in a constricting zone", 140)));
        WEAPON_SKILLS.put("megamod:unique_whip_2", List.of(new RpgWeaponItem.WeaponSkill("Fire Crack", "Crack a flaming whip at target setting them ablaze", 80), new RpgWeaponItem.WeaponSkill("Infernal Coil", "Wrap fire around nearby enemies trapping and burning them", 160)));
        WEAPON_SKILLS.put("megamod:unique_whip_sw", List.of(new RpgWeaponItem.WeaponSkill("Thorn Snap", "Lash with thorns dealing damage and slowing", 60), new RpgWeaponItem.WeaponSkill("Binding Vines", "Entangle all nearby enemies in vines rooting them", 180)));

        // New Arsenal Wands
        WEAPON_SKILLS.put("megamod:unique_wand_1", List.of(new RpgWeaponItem.WeaponSkill("Arcane Missile", "Fire three arcane bolts at the target", 60), new RpgWeaponItem.WeaponSkill("Mana Burst", "Release an arcane shockwave damaging all nearby enemies", 120)));
        WEAPON_SKILLS.put("megamod:unique_wand_2", List.of(new RpgWeaponItem.WeaponSkill("Ice Shard", "Launch an ice shard that slows the target", 60), new RpgWeaponItem.WeaponSkill("Flash Freeze", "Freeze all nearby enemies in a frost shockwave", 160)));
        WEAPON_SKILLS.put("megamod:unique_wand_sw", List.of(new RpgWeaponItem.WeaponSkill("Starfall", "Call down 5 star strikes on random enemies", 100), new RpgWeaponItem.WeaponSkill("Celestial Beam", "Fire a continuous beam of celestial energy", 200)));

        // New Arsenal Katanas
        WEAPON_SKILLS.put("megamod:unique_katana_1", List.of(new RpgWeaponItem.WeaponSkill("Quick Draw", "Lightning-fast draw slash with critical damage", 60), new RpgWeaponItem.WeaponSkill("Blade Flurry", "Three rapid slashes hitting all enemies in front", 120)));
        WEAPON_SKILLS.put("megamod:unique_katana_2", List.of(new RpgWeaponItem.WeaponSkill("Shadow Step", "Teleport behind target and deliver a backstab", 80), new RpgWeaponItem.WeaponSkill("Moonlit Slash", "Wide crescent slash infused with shadow energy", 140)));
        WEAPON_SKILLS.put("megamod:unique_katana_sw", List.of(new RpgWeaponItem.WeaponSkill("Twilight Slash", "Phase through target dealing shadow damage", 80), new RpgWeaponItem.WeaponSkill("Dusk Storm", "Five rapid expanding arcs of shadow fire", 200)));

        // New Arsenal Greatshields
        WEAPON_SKILLS.put("megamod:unique_greatshield_1", List.of(new RpgWeaponItem.WeaponSkill("Fortress", "Raise an impenetrable shield gaining massive resistance", 80), new RpgWeaponItem.WeaponSkill("Battering Ram", "Charge forward bashing all enemies in your path", 160)));
        WEAPON_SKILLS.put("megamod:unique_greatshield_2", List.of(new RpgWeaponItem.WeaponSkill("Holy Barrier", "Grant a holy shield to yourself and nearby allies", 100), new RpgWeaponItem.WeaponSkill("Radiant Repulse", "Blast all nearby enemies away with holy energy", 180)));
        WEAPON_SKILLS.put("megamod:unique_greatshield_sw", List.of(new RpgWeaponItem.WeaponSkill("Unbreakable", "Gain an indestructible golden dome shield", 120), new RpgWeaponItem.WeaponSkill("Titan Slam", "Slam shield into ground creating a massive shockwave", 240)));

        // New Arsenal Throwing Axes
        WEAPON_SKILLS.put("megamod:unique_throwing_axe_1", List.of(new RpgWeaponItem.WeaponSkill("Thunder Throw", "Throw a lightning-charged axe that chains to 2 enemies", 60), new RpgWeaponItem.WeaponSkill("Boomerang Arc", "Throw axe in a wide arc hitting all enemies in its path", 140)));
        WEAPON_SKILLS.put("megamod:unique_throwing_axe_2", List.of(new RpgWeaponItem.WeaponSkill("Frozen Hurl", "Throw a frost axe that freezes on impact", 80), new RpgWeaponItem.WeaponSkill("Shatter Throw", "Throw axe that shatters on impact creating frost shockwave", 160)));
        WEAPON_SKILLS.put("megamod:unique_throwing_axe_sw", List.of(new RpgWeaponItem.WeaponSkill("Windborne Hatchet", "Throw a wind-powered axe that pierces through all", 60), new RpgWeaponItem.WeaponSkill("Whirlwind Toss", "Throw 4 axes in cardinal directions", 180)));

        // New Arsenal Rapiers
        WEAPON_SKILLS.put("megamod:unique_rapier_1", List.of(new RpgWeaponItem.WeaponSkill("Riposte", "Enter a parry stance, countering the next attack", 80), new RpgWeaponItem.WeaponSkill("Flurry of Blows", "4 rapid precise strikes on the target", 120)));
        WEAPON_SKILLS.put("megamod:unique_rapier_2", List.of(new RpgWeaponItem.WeaponSkill("Envenom", "Coat blade in poison for enhanced attacks", 80), new RpgWeaponItem.WeaponSkill("Lethal Lunge", "Dash forward with a devastating poisoned thrust", 140)));
        WEAPON_SKILLS.put("megamod:unique_rapier_sw", List.of(new RpgWeaponItem.WeaponSkill("Perfect Parry", "Perfect defensive stance that reflects on counter", 100), new RpgWeaponItem.WeaponSkill("En Passant", "Phase through target with a deadly dash strike", 160)));

        // Fill-in _3 Variants + New Longswords
        WEAPON_SKILLS.put("megamod:unique_longsword_1", List.of(new RpgWeaponItem.WeaponSkill("Sweeping Cut", "Wide fire sweep hitting and igniting all enemies in front", 60), new RpgWeaponItem.WeaponSkill("Valiant Charge", "Charge forward dealing fire damage to all in path", 120)));
        WEAPON_SKILLS.put("megamod:unique_longsword_2", List.of(new RpgWeaponItem.WeaponSkill("Shadow Slash", "Shadow-infused slash that weakens the target", 80), new RpgWeaponItem.WeaponSkill("Dark Rend", "Devastating shadow strike that bypasses armor", 160)));
        WEAPON_SKILLS.put("megamod:unique_claymore_3", List.of(new RpgWeaponItem.WeaponSkill("Frostwind Slash", "Wide frost-infused slash that slows all hit", 100), new RpgWeaponItem.WeaponSkill("Avalanche", "Massive frost slam creating an icy shockwave", 200)));
        WEAPON_SKILLS.put("megamod:unique_dagger_3", List.of(new RpgWeaponItem.WeaponSkill("Nerve Strike", "Precise strike that stuns the target", 60), new RpgWeaponItem.WeaponSkill("Fan of Knives", "Throw poisoned knives in a cone, hitting enemies in front", 120)));
        WEAPON_SKILLS.put("megamod:unique_double_axe_3", List.of(new RpgWeaponItem.WeaponSkill("Flame Rend", "Dual fire slash hitting nearby enemies", 80), new RpgWeaponItem.WeaponSkill("Pyroclastic Frenzy", "Rapid fire axe swings creating explosions", 160)));
        WEAPON_SKILLS.put("megamod:unique_glaive_3", List.of(new RpgWeaponItem.WeaponSkill("Crescent Arc", "Sweep a wide moonlit arc hitting all enemies in front", 80), new RpgWeaponItem.WeaponSkill("Lunar Pierce", "Thrust forward with a piercing moonlit lance through enemies", 140)));
        WEAPON_SKILLS.put("megamod:unique_hammer_3", List.of(new RpgWeaponItem.WeaponSkill("Permafrost Slam", "Slam ground creating a frost shockwave", 100), new RpgWeaponItem.WeaponSkill("Glacier Crush", "Devastating ice impact that freezes all nearby", 200)));
        WEAPON_SKILLS.put("megamod:unique_mace_3", List.of(new RpgWeaponItem.WeaponSkill("Holy Impact", "Strike with holy force dealing bonus damage to undead", 80), new RpgWeaponItem.WeaponSkill("Cleansing Smite", "Powerful holy smite that purges and damages", 160)));
        WEAPON_SKILLS.put("megamod:unique_sickle_3", List.of(new RpgWeaponItem.WeaponSkill("Frozen Reap", "Frost-infused reap that slows all hit", 80), new RpgWeaponItem.WeaponSkill("Winter's Harvest", "Reap all nearby enemies with a freezing spin", 120)));
        WEAPON_SKILLS.put("megamod:unique_spear_3", List.of(new RpgWeaponItem.WeaponSkill("Charged Thrust", "Lightning-charged thrust through enemies", 80), new RpgWeaponItem.WeaponSkill("Chain Impale", "Pierce through multiple enemies with chain lightning", 140)));
        WEAPON_SKILLS.put("megamod:unique_longbow_3", List.of(new RpgWeaponItem.WeaponSkill("Wind Arrow", "Fire a wind arrow that knocks back the target", 60), new RpgWeaponItem.WeaponSkill("Tornado Shot", "Fire a volley of wind arrows that blow enemies away", 160)));
        WEAPON_SKILLS.put("megamod:unique_heavy_crossbow_3", List.of(new RpgWeaponItem.WeaponSkill("Mana Bolt", "Fire an arcane bolt that deals magic damage", 80), new RpgWeaponItem.WeaponSkill("Arcane Barrage", "Rapid fire arcane bolts at all nearby targets", 200)));
        WEAPON_SKILLS.put("megamod:unique_staff_damage_8", List.of(new RpgWeaponItem.WeaponSkill("Ball Lightning", "Launch a concentrated ball of lightning at your target", 80), new RpgWeaponItem.WeaponSkill("Tempest", "Create a massive lightning storm damaging all nearby", 200)));
        WEAPON_SKILLS.put("megamod:unique_staff_heal_3", List.of(new RpgWeaponItem.WeaponSkill("Nature's Embrace", "Heal yourself and nearby allies with nature magic", 60), new RpgWeaponItem.WeaponSkill("Verdant Bloom", "Create a healing garden that restores health over time", 180)));
        WEAPON_SKILLS.put("megamod:unique_shield_3", List.of(new RpgWeaponItem.WeaponSkill("Fear Aura", "Gain fire resistance and reflect flame damage to attackers", 80), new RpgWeaponItem.WeaponSkill("Dread Slam", "Slam shield to ignite and knock back all nearby enemies", 160)));

        // Fill-in _3 variants for remaining weapon types (Nightmare tier)
        WEAPON_SKILLS.put("megamod:unique_longsword_3", List.of(new RpgWeaponItem.WeaponSkill("Crescent Strike", "Arcing slash that damages all enemies in a wide frontal cone", 70), new RpgWeaponItem.WeaponSkill("Blade Storm", "Rapid consecutive slashes creating a storm of blades", 150)));
        WEAPON_SKILLS.put("megamod:unique_whip_3", List.of(new RpgWeaponItem.WeaponSkill("Lightning Crack", "Crack whip charged with lightning, chaining to 2 nearby enemies", 80), new RpgWeaponItem.WeaponSkill("Tempest Coil", "Whirl the whip creating a vortex that pulls and damages enemies", 160)));
        WEAPON_SKILLS.put("megamod:unique_wand_3", List.of(new RpgWeaponItem.WeaponSkill("Shadow Bolt", "Fire a bolt of shadow energy that pierces through enemies", 60), new RpgWeaponItem.WeaponSkill("Void Rift", "Open a void rift that damages and weakens all enemies in area", 160)));
        WEAPON_SKILLS.put("megamod:unique_katana_3", List.of(new RpgWeaponItem.WeaponSkill("Crescent Cut", "Swift crescent-shaped slash with increased critical damage", 70), new RpgWeaponItem.WeaponSkill("Phantom Dance", "Dash through enemies 3 times dealing shadow damage", 150)));
        WEAPON_SKILLS.put("megamod:unique_greatshield_3", List.of(new RpgWeaponItem.WeaponSkill("Earthen Bulwark", "Plant shield creating a stone barrier that absorbs damage", 100), new RpgWeaponItem.WeaponSkill("Tremor Bash", "Slam shield creating an earthquake that staggers all nearby", 200)));
        WEAPON_SKILLS.put("megamod:unique_throwing_axe_3", List.of(new RpgWeaponItem.WeaponSkill("Ricochet Throw", "Throw axe that ricochets between 3 enemies", 70), new RpgWeaponItem.WeaponSkill("Axe Storm", "Throw multiple axes in a spiral pattern hitting all nearby", 160)));
        WEAPON_SKILLS.put("megamod:unique_rapier_3", List.of(new RpgWeaponItem.WeaponSkill("Feint Strike", "Deceptive strike that bypasses target's armor", 70), new RpgWeaponItem.WeaponSkill("Blade Dance", "Elegant 5-hit combo dealing escalating damage", 140)));

        // Build global skill-by-name lookup
        for (List<RpgWeaponItem.WeaponSkill> skills : WEAPON_SKILLS.values()) {
            for (RpgWeaponItem.WeaponSkill skill : skills) {
                ALL_SKILLS_BY_NAME.put(skill.name(), skill);
            }
        }
    }
}

