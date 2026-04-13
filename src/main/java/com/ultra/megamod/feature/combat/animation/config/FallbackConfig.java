package com.ultra.megamod.feature.combat.animation.config;

/**
 * Fallback configuration for assigning weapon attributes to items by regex matching.
 * Ported 1:1 from BetterCombat (net.bettercombat.config.FallbackConfig).
 */
public class FallbackConfig {
    public int schema_version;
    public String blacklist_item_id_regex;
    public CompatibilitySpecifier[] fallback_compatibility;
    public CompatibilitySpecifier[] ranged_weapons;

    public static class CompatibilitySpecifier {
        public String item_id_regex;
        public String weapon_attributes;

        public CompatibilitySpecifier() {}

        public CompatibilitySpecifier(String item_id_regex, String weapon_attributes) {
            this.item_id_regex = item_id_regex;
            this.weapon_attributes = weapon_attributes;
        }
    }

    public static FallbackConfig createDefault() {
        var object = new FallbackConfig();
        object.schema_version = 1;
        object.blacklist_item_id_regex = "pickaxe";
        object.fallback_compatibility = new CompatibilitySpecifier[]{
                new CompatibilitySpecifier(
                        "claymore|great_sword|greatsword",
                        "megamod:claymore"),
                new CompatibilitySpecifier(
                        "great_hammer|greathammer|war_hammer|warhammer|maul",
                        "megamod:hammer"),
                new CompatibilitySpecifier(
                        "double_axe|doubleaxe|war_axe|waraxe|great_axe|greataxe",
                        "megamod:double_axe"),
                new CompatibilitySpecifier(
                        "scythe",
                        "megamod:scythe"),
                new CompatibilitySpecifier(
                        "halberd",
                        "megamod:halberd"),
                new CompatibilitySpecifier(
                        "glaive",
                        "megamod:glaive"),
                new CompatibilitySpecifier(
                        "spear",
                        "megamod:spear"),
                new CompatibilitySpecifier(
                        "lance",
                        "megamod:lance"),
                new CompatibilitySpecifier(
                        "anchor",
                        "megamod:anchor"),
                new CompatibilitySpecifier(
                        "battlestaff|battle_staff",
                        "megamod:battlestaff"),
                new CompatibilitySpecifier(
                        "claw",
                        "megamod:claw"),
                new CompatibilitySpecifier(
                        "fist|gauntlet",
                        "megamod:fist"),
                new CompatibilitySpecifier(
                        "trident|javelin|impaled",
                        "megamod:trident"),
                new CompatibilitySpecifier(
                        "katana",
                        "megamod:katana"),
                new CompatibilitySpecifier(
                        "rapier",
                        "megamod:rapier"),
                new CompatibilitySpecifier(
                        "sickle",
                        "megamod:sickle"),
                new CompatibilitySpecifier(
                        "soul_knife",
                        "megamod:soul_knife"),
                new CompatibilitySpecifier(
                        "dagger|knife",
                        "megamod:dagger"),
                new CompatibilitySpecifier(
                        "staff|wand|sceptre|stave|rod",
                        "megamod:wand"),
                new CompatibilitySpecifier(
                        "mace|hammer|flail",
                        "megamod:mace"),
                new CompatibilitySpecifier(
                        "axe",
                        "megamod:axe"),
                new CompatibilitySpecifier(
                        "coral_blade",
                        "megamod:coral_blade"),
                new CompatibilitySpecifier(
                        "twin_blade|twinblade",
                        "megamod:twin_blade"),
                new CompatibilitySpecifier(
                        "cutlass|scimitar|machete",
                        "megamod:cutlass"),
                new CompatibilitySpecifier(
                        "sword|blade",
                        "megamod:sword")
        };
        object.ranged_weapons = new CompatibilitySpecifier[]{
                new CompatibilitySpecifier(
                        "two_handed_crossbow",
                        "megamod:crossbow_two_handed_heavy"),
                new CompatibilitySpecifier(
                        "two_handed_bow",
                        "megamod:bow_two_handed_heavy")
        };
        return object;
    }

    public static FallbackConfig migrate(FallbackConfig oldConfig, FallbackConfig newConfig) {
        newConfig.fallback_compatibility = oldConfig.fallback_compatibility;
        return newConfig;
    }
}
