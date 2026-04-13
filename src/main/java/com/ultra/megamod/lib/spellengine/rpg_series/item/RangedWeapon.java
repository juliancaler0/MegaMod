package com.ultra.megamod.lib.spellengine.rpg_series.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;
import net.minecraft.util.Util;
import com.ultra.megamod.lib.rangedweapon.api.RangedConfig;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellChoice;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RangedWeapon {

    public interface RangedFactory {
        Item create(Item.Properties settings, RangedConfig config, Supplier<Ingredient> repairIngredientSupplier);
    }

    public static final class Entry {
        private final Identifier id;
        private final RangedFactory factory;
        private final RangedConfig defaults;
        private final Supplier<Ingredient> repairIngredientSupplier;
        private Equipment.Tier tier;

        private String translatedName = "";
        public Rarity rarity = Rarity.COMMON;
        @Nullable private Item registeredItem;

        public String weaponAttributesPreset = "";
        @Nullable public SpellChoice spellChoice;
        @Nullable public SpellContainer spellContainer;

        public Equipment.WeaponType category = Equipment.WeaponType.LONG_BOW;
        public Equipment.LootProperties lootProperties = Equipment.LootProperties.EMPTY;

        public Entry(Identifier id, Equipment.Tier tier, RangedFactory factory, RangedConfig defaults, Supplier<Ingredient> repairIngredientSupplier, Equipment.WeaponType category) {
            this.id = id;
            this.tier = tier;
            this.lootProperties = Equipment.LootProperties.of(tier.getNumber());
            this.factory = factory;
            this.defaults = defaults;
            this.repairIngredientSupplier = repairIngredientSupplier;
            this.category = category;
        }

        @Nullable public Item item() {
            return registeredItem;
        }

        public Identifier id() {
            return id;
        }

        public RangedFactory factory() {
            return factory;
        }

        public RangedConfig defaults() {
            return defaults;
        }

        public Supplier<Ingredient> repairIngredientSupplier() {
            return repairIngredientSupplier;
        }

        public int durability() {
            switch (tier) {
                case WOODEN, GOLDEN -> { return 384; }
                case TIER_0, TIER_1 -> { return 465; }
                case TIER_2 -> { return 1561; } // Diamond-equivalent
                case TIER_3 -> { return 2031; } // Netherite-equivalent
                case TIER_4, TIER_5 -> { return 4062; } // 2x Netherite
                default -> { return 250; }
            }
        }

        public Item create(Item.Properties settings, RangedConfig config) {
            this.registeredItem = factory.create(
                    settings.durability(durability()),
                    config,
                    repairIngredientSupplier
            );
            return this.registeredItem;
        }

        public Entry translatedName(String translatedName) {
            this.translatedName = translatedName;
            return this;
        }

        public String translatedName() {
            return translatedName;
        }

        public String translationKey() {
            return Util.makeDescriptionId("item", id());
        }

        public Entry spellChoice(SpellChoice choice) {
            this.spellChoice = choice;
            return this;
        }

        public Entry spellContainer(SpellContainer container) {
            this.spellContainer = container;
            return this;
        }

        public Entry withSpellChoices(String pool) {
            this.spellContainer = this.spellContainer.withBindingPool(Identifier.parse(pool));
            this.spellChoice = SpellChoice.of(pool);
            return this;
        }

        public Entry lootTheme(String theme) {
            lootProperties = Equipment.LootProperties.of(lootProperties.tier(), theme);
            return this;
        }

        public Entry loot(int tier, String theme) {
            this.lootProperties = Equipment.LootProperties.of(tier, theme);
            return this;
        }
    }

    public static void register(Map<String, RangedConfig> rangedConfig, List<Entry> entries, ResourceKey<CreativeModeTab> itemGroupKey) {
        for (var entry: entries) {
            var config = rangedConfig.get(entry.id.toString());
            if (config == null) {
                config = entry.defaults;
                rangedConfig.put(entry.id.toString(), config);
            }
            final var finalConfig = config;
            final var finalEntry = entry;

            RPGItemRegistry.registerItem(entry.id.getPath(), (props) -> {
                if (finalEntry.tier.getNumber() >= Equipment.Tier.TIER_3.getNumber()) {
                    props.fireResistant();
                }
                if (finalEntry.rarity != Rarity.COMMON) {
                    props.rarity(finalEntry.rarity);
                }
                if (finalEntry.spellChoice != null) {
                    props.component(SpellDataComponents.SPELL_CHOICE, finalEntry.spellChoice);
                }
                if (finalEntry.spellContainer != null) {
                    props.component(SpellDataComponents.SPELL_CONTAINER, finalEntry.spellContainer);
                }
                return finalEntry.create(props, finalConfig);
            });
        }
        // TODO: 1.21.11 - ItemGroupEvents is Fabric-only; use NeoForge creative tab event instead
    }
}
