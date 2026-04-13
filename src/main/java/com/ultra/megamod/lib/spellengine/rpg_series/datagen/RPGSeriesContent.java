package com.ultra.megamod.lib.spellengine.rpg_series.datagen;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import com.ultra.megamod.lib.spellengine.api.datagen.NamespacedLangGenerator;
import com.ultra.megamod.lib.spellengine.api.datagen.SpellGenerator;
import com.ultra.megamod.lib.spellengine.rpg_series.item.Equipment;
import com.ultra.megamod.lib.spellengine.api.tags.SpellEngineItemTags;
import com.ultra.megamod.lib.spellengine.rpg_series.tags.RPGSeriesItemTags;
import com.ultra.megamod.lib.spellpower.api.SpellPowerTags;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO: 1.21.11 - Datagen classes need NeoForge datagen providers instead of Fabric
// These classes are stubs — actual datagen is not functional until ported
public class RPGSeriesContent {
    // EquipmentTagGen removed — IntrinsicHolderTagsProvider is Fabric-only
    // WeaponSkillGen and LangGenerator removed — PackOutput is Fabric-only
}
