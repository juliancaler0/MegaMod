package com.ultra.megamod.lib.spellengine.api.spell;

import com.ultra.megamod.lib.spellengine.api.spell.container.SpellChoice;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.UnaryOperator;

public class SpellDataComponents {
    @SuppressWarnings("unchecked")
    public static final DataComponentType<SpellContainer> SPELL_CONTAINER = register(
            Identifier.fromNamespaceAndPath("megamod", "spell_container"),
            builder -> builder.persistent(SpellContainer.CODEC)
    );
    @SuppressWarnings("unchecked")
    public static final DataComponentType<SpellChoice> SPELL_CHOICE = register(
            Identifier.fromNamespaceAndPath("megamod", "spell_choice"),
            builder -> builder.persistent(SpellChoice.CODEC)
    );
    @SuppressWarnings("unchecked")
    public static final DataComponentType<Identifier> EQUIPMENT_SET = register(
            Identifier.fromNamespaceAndPath("megamod", "equipment_set"),
            builder -> builder.persistent(Identifier.CODEC)
    );
    @SuppressWarnings("unchecked")
    public static final DataComponentType<Identifier> ITEM_MODEL = register(
            Identifier.fromNamespaceAndPath("megamod", "item_model"),
            builder -> builder.persistent(Identifier.CODEC)
    );

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> DataComponentType<T> register(Identifier id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return (DataComponentType<T>) Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE, id,
                builderOperator.apply(DataComponentType.builder()).build()
        );
    }

    public static void init() {
        // Force static initialization
    }
}
