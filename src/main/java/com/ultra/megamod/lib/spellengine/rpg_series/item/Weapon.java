package com.ultra.megamod.lib.spellengine.rpg_series.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Rarity;
import com.ultra.megamod.lib.spellengine.api.config.WeaponConfig;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellChoice;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Weapon {

    public interface Factory {
        Item create(CustomMaterial material, Item.Properties settings);
    }

    public static final class Entry {
        private final String namespace;
        private final String name;
        private final CustomMaterial material;
        private final Factory factory;
        @Nullable private Item registeredItem;
        private final WeaponConfig defaults;
        private @Nullable String requiredMod;
        public Rarity rarity = Rarity.COMMON;
        private String translatedName = ""; // Used for data gen

        public String weaponAttributesPreset = ""; // Used for data gen
        @Nullable public SpellChoice spellChoice;
        @Nullable public SpellContainer spellContainer;

        // Loot related classification
        public Equipment.WeaponType category = Equipment.WeaponType.SWORD;
        public Equipment.LootProperties lootProperties = Equipment.LootProperties.EMPTY;

        public Entry(String namespace, String name, CustomMaterial material, Factory factory, WeaponConfig defaults, Equipment.WeaponType category) {
            this.namespace = namespace;
            this.name = name;
            this.material = material;
            this.factory = factory;
            this.defaults = defaults;
            this.category = category;
        }

        public Identifier id() {
            return Identifier.fromNamespaceAndPath(namespace, name);
        }

        public Entry attribute(com.ultra.megamod.lib.spellengine.api.config.AttributeModifier attribute) {
            defaults.add(attribute);
            return this;
        }

        public Entry requires(String modName) {
            this.requiredMod = modName;
            return this;
        }

        public boolean isRequiredModInstalled() {
            if (requiredMod == null || requiredMod.isEmpty()) {
                return true;
            }
            return net.neoforged.fml.ModList.get().isLoaded(requiredMod);
        }

        public String name() {
            return name;
        }

        public CustomMaterial material() {
            return material;
        }

        public Item create(CustomMaterial material, Item.Properties settings) {
            var item = factory.create(material, settings);
            registeredItem = item;
            return item;
        }

        @Nullable public Item item() {
            return registeredItem;
        }

        public WeaponConfig defaults() {
            return defaults;
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

        public Entry withAdditionalSpell(String spellId) {
            var container = this.spellContainer;
            if (container != null) {
                this.spellContainer = container.withAdditionalSpell(List.of(spellId));
            }
            return this;
        }

        public Entry translatedName(String name) {
            this.translatedName = name;
            return this;
        }

        public String translatedName() {
            return translatedName;
        }

        public Equipment.WeaponType category() {
            return category;
        }

        public Entry loot(Equipment.LootProperties properties) {
            lootProperties = properties;
            return this;
        }

        public Entry lootTheme(String theme) {
            lootProperties = Equipment.LootProperties.of(lootProperties.tier(), theme);
            return this;
        }

        public Equipment.LootProperties lootProperties() {
            return lootProperties;
        }
    }

    // MARK: Material
    // In 1.21.11, ToolMaterial is removed. This is now a simple data holder.

    public static class CustomMaterial {
        public static CustomMaterial matching(int durability, float miningSpeed, int enchantability, Supplier<Ingredient> repairIngredient) {
            var material = new CustomMaterial();
            material.durability = durability;
            material.miningSpeed = miningSpeed;
            material.enchantability = enchantability;
            material.ingredient = repairIngredient;
            return material;
        }

        private int durability = 0;
        private float miningSpeed = 0;
        private int enchantability = 0;
        private Supplier<Ingredient> ingredient = null;

        public int getDurability() {
            return durability;
        }

        public float getMiningSpeedMultiplier() {
            return miningSpeed;
        }

        public float getAttackDamage() {
            return 0;
        }

        public int getEnchantability() {
            return enchantability;
        }

        public Ingredient getRepairIngredient() {
            return this.ingredient != null ? this.ingredient.get() : Ingredient.of();
        }
    }

    // MARK: Registration

    public static void register(Map<String, WeaponConfig> configs, List<Entry> entries, ResourceKey<CreativeModeTab> itemGroupKey) {
        for(var entry: entries) {
            var config = configs.get(entry.name);
            if (config == null) {
                config = entry.defaults;
                configs.put(entry.name(), config);
            }
            if (!entry.isRequiredModInstalled()) { continue; }

            final var finalConfig = config;
            final var finalMaterial = entry.material;
            final var finalEntry = entry;

            RPGItemRegistry.registerItem(entry.id().getPath(), (props) -> {
                props.attributes(attributesFrom(finalConfig));
                if (finalEntry.rarity != Rarity.COMMON) {
                    props.rarity(finalEntry.rarity);
                }

                if (finalEntry.spellChoice != null) {
                    props.component(SpellDataComponents.SPELL_CHOICE, finalEntry.spellChoice);
                }
                if (finalEntry.spellContainer != null) {
                    props.component(SpellDataComponents.SPELL_CONTAINER, finalEntry.spellContainer);
                }

                var tier = finalEntry.lootProperties().tier();
                if (tier >= 3) {
                    props.fireResistant();
                }
                return finalEntry.create(finalMaterial, props);
            });
        }
        // TODO: 1.21.11 - ItemGroupEvents is Fabric-only; use NeoForge creative tab event instead
    }

    public static ItemAttributeModifiers attributesFrom(WeaponConfig config) {
        var builder = ItemAttributeModifiers.builder();

        builder.add(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                        Item.BASE_ATTACK_DAMAGE_ID,
                        config.attack_damage,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        builder.add(Attributes.ATTACK_SPEED,
                new AttributeModifier(
                        Item.BASE_ATTACK_SPEED_ID,
                        config.attack_speed,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        for(var attribute: config.selectedAttributes()) {
            try {
                var attributeId = Identifier.parse(attribute.attribute);
                var entityAttribute = BuiltInRegistries.ATTRIBUTE.get(attributeId);
                if (entityAttribute.isPresent()) {
                    builder.add(entityAttribute.get(),
                            new AttributeModifier(
                                    equipmentBonusId,
                                    attribute.value,
                                    attribute.operation),
                            EquipmentSlotGroup.MAINHAND);
                }
            } catch (Exception e) {
                System.err.println("Failed to add item attribute modifier: " + e.getMessage());
            }
        }

        return builder.build();
    }

    public static ItemAttributeModifiers attributesFrom(List<com.ultra.megamod.lib.spellengine.api.config.AttributeModifier> attributes) {
        var builder = ItemAttributeModifiers.builder();

        for(var attribute: attributes) {
            try {
                var attributeId = Identifier.parse(attribute.attribute);
                var entityAttribute = BuiltInRegistries.ATTRIBUTE.get(attributeId);
                if (entityAttribute.isPresent()) {
                    builder.add(entityAttribute.get(),
                            new AttributeModifier(
                                    equipmentBonusId,
                                    attribute.value,
                                    attribute.operation),
                            EquipmentSlotGroup.MAINHAND);
                }
            } catch (Exception e) {
                System.err.println("Failed to add item attribute modifier: " + e.getMessage());
            }
        }

        return builder.build();
    }

    private static final Identifier equipmentBonusId = Identifier.fromNamespaceAndPath("megamod", "equipment_bonus");
}
