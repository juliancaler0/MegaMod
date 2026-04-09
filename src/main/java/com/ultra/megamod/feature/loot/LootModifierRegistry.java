package com.ultra.megamod.feature.loot;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.MegaMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class LootModifierRegistry {

    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MegaMod.MODID);

    public static final Supplier<MapCodec<StructureChestLootModifier>> STRUCTURE_CHEST =
            GLM_SERIALIZERS.register("structure_chest", () -> StructureChestLootModifier.CODEC);

    public static void init(IEventBus modEventBus) {
        GLM_SERIALIZERS.register(modEventBus);
    }
}
