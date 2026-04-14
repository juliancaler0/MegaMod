package com.ultra.megamod.feature.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Global Loot Modifier that injects rolled relics/weapons into vanilla structure chests.
 * Registered via JSON in data/megamod/loot_modifiers/ and data/neoforge/loot_modifiers/.
 * Chance and quality scale with player's Luck attribute.
 */
public class StructureChestLootModifier extends LootModifier {

    public static final MapCodec<StructureChestLootModifier> CODEC = RecordCodecBuilder.mapCodec(
            inst -> codecStart(inst).apply(inst, StructureChestLootModifier::new));

    public StructureChestLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        // Get the loot table ID to determine structure type
        String lootTableId = context.getQueriedLootTableId().toString();

        // Get luck from the player who triggered the loot generation
        double luck = 0.0;
        try {
            Entity entity = context.getParameter(LootContextParams.THIS_ENTITY);
            if (entity instanceof ServerPlayer player) {
                luck = WorldLootIntegration.getLuck(player);
            }
        } catch (Exception ignored) {
            // Some loot contexts may not have an entity parameter
        }

        RandomSource random = context.getRandom();
        ItemStack relic = WorldLootIntegration.tryGenerateStructureChestRelic(lootTableId, random, luck);
        if (relic != null) {
            generatedLoot.add(relic);
        }

        // Jewelry is a separate independent roll — a chest can drop both a relic and jewelry.
        ItemStack jewelry = WorldLootIntegration.tryGenerateStructureChestJewelry(lootTableId, random, luck);
        if (jewelry != null) {
            generatedLoot.add(jewelry);
        }

        // Runes roll independently too — they're spell reagents players need regularly
        ItemStack rune = WorldLootIntegration.tryGenerateStructureChestRune(lootTableId, random, luck);
        if (rune != null) {
            generatedLoot.add(rune);
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
