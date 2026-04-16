package com.ultra.megamod.lib.skilltree;

import com.ultra.megamod.lib.skilltree.items.SkillItems;
import com.ultra.megamod.lib.skilltree.node.ConditionalAttributeReward;
import com.ultra.megamod.lib.skilltree.node.SpellContainerReward;
import com.ultra.megamod.lib.skilltree.effect.SkillEffects;
import com.ultra.megamod.lib.skilltree.skills.SkillSounds;
import com.ultra.megamod.lib.spellengine.api.config.ConfigFile;

public class SkillTreeMod {
    public static final String NAMESPACE = "skill_tree_rpgs";
    // Simplified: ConfigManager replaced with direct ConfigFile instance
    public static ConfigFile.Effects effectConfig = new ConfigFile.Effects();

    public static void init() {
        SpellContainerReward.register();
        ConditionalAttributeReward.register();
    }

    public static void registerSounds() {
        SkillSounds.register();
    }

    public static void registerItems() {
        SkillItems.register();
    }

    public static void registerEffects() {
        SkillEffects.register(effectConfig);
    }
}
