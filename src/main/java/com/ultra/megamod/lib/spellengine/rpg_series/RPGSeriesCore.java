package com.ultra.megamod.lib.spellengine.rpg_series;

import net.minecraft.world.item.enchantment.Enchantments;
import com.ultra.megamod.lib.spellengine.api.item.weapon.StaffItem;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.rpg_series.loot.LootConfig;
import com.ultra.megamod.lib.spellengine.rpg_series.loot.LootHelper;
import com.ultra.megamod.lib.spellengine.rpg_series.config.Defaults;

import java.util.HashMap;
import java.util.Set;

public class RPGSeriesCore {
    public static final String NAMESPACE = "rpg_series";

    public static SpellEngineClient.ConfigHolder<LootConfig> lootEquipmentConfig =
            new SpellEngineClient.ConfigHolder<>("loot_equipment_v2", Defaults.itemLootConfig, LootConfig.class);

    public static SpellEngineClient.ConfigHolder<LootConfig> lootScrollsConfig =
            new SpellEngineClient.ConfigHolder<>("loot_scrolls_v2", Defaults.scrollLootConfig, LootConfig.class);

    public static void init() {
        lootEquipmentConfig.refresh();
        lootScrollsConfig.refresh();
        LootHelper.TAG_CACHE.refresh();
        // LootTableEvents and ServerLifecycleEvents were Fabric events.
        // In NeoForge, loot table modification is done via LootTableLoadEvent
        // or GlobalLootModifierProvider. These are registered elsewhere.
        // For now, we provide a method that can be called from event handlers.
    }

    /**
     * Called from a NeoForge loot table modification event to inject loot.
     * Hook this up via @EventBusSubscriber or modEventBus.addListener.
     */
    public static void onLootTableModify(net.minecraft.core.HolderLookup.Provider registries,
                                          net.minecraft.resources.ResourceKey<?> key,
                                          net.minecraft.world.level.storage.loot.LootTable.Builder tableBuilder) {
        LootHelper.configureV2(registries, key.identifier(), tableBuilder, lootEquipmentConfig.value, new HashMap<>());
        LootHelper.configureV2(registries, key.identifier(), tableBuilder, lootScrollsConfig.value, new HashMap<>());
    }

    /**
     * Called when the server starts or data packs reload to update tag caches.
     */
    public static void onServerStartedOrReload() {
        LootHelper.updateTagCache(lootEquipmentConfig.value);
    }
}
