package com.ultra.megamod.feature.combat.runes;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registrations for the Rune Crafting system: custom recipe type, serializer, and sound event.
 * Ported 1:1 from the Runes mod's RuneCrafting class.
 */
public class RuneCrafting {
    public static final String NAME = "crafting";
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MegaMod.MODID, NAME);
    public static final int SOUND_DELAY = 20;

    // ── Deferred Registers ──

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MegaMod.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MegaMod.MODID);

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MegaMod.MODID);

    // ── Recipe Type ──

    public static final Supplier<RecipeType<RuneCraftingRecipe>> RECIPE_TYPE =
            RECIPE_TYPES.register(NAME, () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return NAME;
                }
            });

    // ── Recipe Serializer ──

    public static final Supplier<RecipeSerializer<RuneCraftingRecipe>> RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register(NAME, RuneCraftingRecipe.Serializer::new);

    // ── Sound Event ──

    public static final Supplier<SoundEvent> SOUND = SOUND_EVENTS.register("combat.rune_crafting",
            () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MegaMod.MODID, "combat.rune_crafting")));

    // ── Init ──

    public static void init(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        SOUND_EVENTS.register(modBus);
    }
}
