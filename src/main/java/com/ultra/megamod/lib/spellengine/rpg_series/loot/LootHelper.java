package com.ultra.megamod.lib.spellengine.rpg_series.loot;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.rpg_series.RPGSeriesCore;
import com.ultra.megamod.lib.spellengine.spellbinding.SpellBindRandomlyLootFunction;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class LootHelper {

    public static SpellEngineClient.ConfigHolder<TagCache> TAG_CACHE =
            new SpellEngineClient.ConfigHolder<>("tag_cache", new TagCache(), TagCache.class);

    public static class TagCache {
        public HashMap<String, List<String>> cache = new HashMap<>();
    }

    public static void updateTagCache(LootConfig lootConfig) {
        var updatedTags = new HashSet<String>();
        for (var entry: lootConfig.injectors.entrySet()) {
            var tableId = entry.getKey();
            var pool = entry.getValue();
            for (var itemInjectorEntry: pool.entries) {
                var tagsToCache = new ArrayList<String>();
                if (itemInjectorEntry.id != null) {
                    tagsToCache.add(itemInjectorEntry.id);
                }
                if (itemInjectorEntry.filters != null) {
                    tagsToCache.addAll(itemInjectorEntry.filters);
                }
                for (var id: tagsToCache) {
                    if (id.startsWith("#")) {
                        var tagString = id.substring(1);
                        if (updatedTags.contains(tagString)) {
                            continue;
                        }
                        var tagId = Identifier.parse(tagString);
                        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                        var itemList = new ArrayList<String>();
                        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach((itemHolder) -> {
                            var itemId = itemHolder.getRegisteredName();
                            itemList.add(itemId);
                        });
                        LootHelper.TAG_CACHE.value.cache.put(tagString, itemList);
                        updatedTags.add(tagString);
                    }
                }
            }
        }
        LootHelper.TAG_CACHE.save();
    }

    public static void configureV2(HolderLookup.Provider registries, Identifier lootTableId, LootTable.Builder tableBuilder, LootConfig config, HashMap<String, Item> entries) {
        boolean isEntityLootTable = lootTableId.getPath().startsWith("entities");
        var tableId = lootTableId.toString();
        var pool = config.injectors.get(tableId);
        if (pool == null) {
            for (var regex: config.regex_injectors.keySet()) {
                if (tableId.matches(regex)) {
                    pool = config.regex_injectors.get(regex);
                    break;
                }
            }
        }
        if (pool == null) { return; }

        var rolls = pool.rolls > 0 ? pool.rolls : 1F;

        boolean skipConditions = pool.skip_conditions != null ? pool.skip_conditions : false;
        var lootPoolBuilder = LootPool.lootPool();
        if (isEntityLootTable && !skipConditions) {
            lootPoolBuilder.when(LootItemKilledByPlayerCondition.killedByPlayer());
        }

        var attempts = Math.ceil(rolls);
        var chance = pool.rolls / attempts;
        lootPoolBuilder.setRolls(BinomialDistributionGenerator.binomial((int) attempts, (float) chance));
        lootPoolBuilder.setBonusRolls(ConstantValue.exactly(pool.bonus_rolls));
        for (var entry: pool.entries) {
            var entryId = entry.id;
            var weight = entry.weight;
            var enchant = entry.enchant;
            var spellBind = entry.spell_bind;
            if (entryId == null || entryId.isEmpty()) { continue; }

            // Tag cache is used, because this event handler is called before the game loads the item tags
            List<String> itemList = entryId.startsWith("#")
                            ? TAG_CACHE.value.cache.get(entryId.substring(1))
                            : List.of(entryId);
            List<String> filters = entry.filters != null ? entry.filters : List.of();

            if (itemList == null) {
                System.err.println("RPG Series loot config: failed to resolve itemList for: " + entryId
                + " (Probably just needs a game restart)");
                continue;
            }

            for (var itemId: itemList) {
                var itemOpt = BuiltInRegistries.ITEM.get(Identifier.parse(itemId));
                if (itemOpt.isEmpty()) { continue; }
                var item = itemOpt.get().value();
                var lootEntry = LootItem.lootTableItem(item)
                        .setWeight(weight);
                var filtersMatch = entry.filters_lenient ? filters.isEmpty() : true;
                for (var filter: filters) {
                    if (!filter.startsWith("#")) { continue; }
                    var tag = TAG_CACHE.value.cache.get(filter.substring(1));
                    if (tag == null) { continue; }
                    var contains = tag.contains(itemId);
                    if (entry.filters_lenient) {
                        filtersMatch = filtersMatch || contains;
                    } else {
                        filtersMatch = filtersMatch && contains;
                    }
                }
                if (!filtersMatch) { continue; }

                if (enchant != null && enchant.isValid()) {
                    var enchantFunction = EnchantWithLevelsFunction.enchantWithLevels(registries, numberProvider(enchant.min_power, enchant.max_power));
                    lootEntry.apply(enchantFunction);
                }
                if (spellBind != null && spellBind.isValid()) {
                    var function = SpellBindRandomlyLootFunction.builder(
                            spellBind.pool,
                            numberProvider(spellBind.tier_min, spellBind.tier_max),
                            numberProvider(spellBind.count_min, spellBind.count_max));
                    lootEntry.apply(function);
                }
                lootPoolBuilder.add(lootEntry);
            }
        }
        tableBuilder.withPool(lootPoolBuilder);
    }

    private static NumberProvider numberProvider(float min, float max) {
        if (max <= min) {
            return ConstantValue.exactly(min);
        } else {
            return UniformGenerator.between(min, max);
        }
    }

    private static List<TagKey<Item>> tagKeys(List<String> tags) {
        return tags.stream().map(LootHelper::tagKey).toList();
    }

    private static TagKey<Item> tagKey(String tag) {
        return TagKey.create(Registries.ITEM, Identifier.parse(tag));
    }
}
