package com.ultra.megamod.feature.combat.archers.config;

import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import com.ultra.megamod.lib.spellengine.api.config.ArmorSetConfig;
import com.ultra.megamod.lib.spellengine.api.config.WeaponConfig;

import java.util.LinkedHashMap;

public class ArchersItemConfig {
    public ArchersItemConfig() {}
    public LinkedHashMap<String, RangedConfig> ranged_weapons = new LinkedHashMap<>();
    public LinkedHashMap<String, WeaponConfig> melee_weapons = new LinkedHashMap<>();
    public LinkedHashMap<String, ArmorSetConfig> armor_sets = new LinkedHashMap<>();
}
