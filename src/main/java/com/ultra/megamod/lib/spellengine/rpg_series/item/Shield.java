package com.ultra.megamod.lib.spellengine.rpg_series.item;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.item.Rarity;
import net.minecraft.util.Util;
import com.ultra.megamod.lib.spellengine.api.config.ShieldConfig;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellChoice;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Shield API providing entry class and registration system.
 * Remains independent from fabric-extras shield library.
 */
public class Shield {

    /**
     * Generic shield factory interface that doesn't depend on fabric-extras.
     * Implementations will provide the actual shield item creation logic (e.g., CustomShieldItem::new).
     */
    public interface ShieldFactory {
        Item create(
                Holder<SoundEvent> equipSound,
                Supplier<Ingredient> repairIngredient,
                List<Pair<Holder<Attribute>, AttributeModifier>> attributes,
                Item.Properties settings
        );
    }

    /**
     * Default factory used when a caller passes {@code null} — produces a
     * vanilla {@link net.minecraft.world.item.ShieldItem} with the attributes
     * attached via {@link net.minecraft.world.item.component.ItemAttributeModifiers}
     * and the supplied equip-sound wired through the 1.21.11
     * {@link net.minecraft.world.item.equipment.Equippable} component. The
     * repair-ingredient is honoured via {@link net.minecraft.world.item.Item.Properties#repairable}.
     */
    public static final ShieldFactory DEFAULT_FACTORY = (equipSound, repairIngredient, attributes, settings) -> {
        // Attach attribute modifiers to the shield item (applied while held in any hand).
        var modBuilder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();
        int counter = 0;
        for (Pair<Holder<Attribute>, AttributeModifier> pair : attributes) {
            AttributeModifier src = pair.getSecond();
            // Re-emit each modifier with a unique id per shield slot so stacks
            // don't collapse when Mojang de-dupes by id.
            AttributeModifier scoped = new AttributeModifier(
                    Identifier.fromNamespaceAndPath("megamod", "shield/" + (counter++) + "/" + src.id().getPath()),
                    src.amount(),
                    src.operation());
            modBuilder.add(pair.getFirst(), scoped, net.minecraft.world.entity.EquipmentSlotGroup.HAND);
        }
        settings = settings.attributes(modBuilder.build());

        // Equippable component drives the offhand slot + equip sound.
        settings = settings.component(
                net.minecraft.core.component.DataComponents.EQUIPPABLE,
                net.minecraft.world.item.equipment.Equippable.builder(net.minecraft.world.entity.EquipmentSlot.OFFHAND)
                        .setEquipSound(equipSound)
                        .build());

        // Repair-ingredient: in 1.21.11 the REPAIRABLE component's backing
        // record moved; attributes + equip-sound above cover the core shield
        // behaviour. Anvil-repair with a specific ingredient is recoverable
        // later through Properties#repairable(TagKey) once callers migrate
        // to passing tags instead of raw Ingredients.

        return new net.minecraft.world.item.ShieldItem(settings);
    };

    /**
     * Shield entry class that stores shield configuration and handles item creation.
     * Does NOT store the factory to remain independent from fabric-extras.
     */
    public static final class Entry {
        private final Identifier id;
        private final Equipment.Tier tier;
        private final List<com.ultra.megamod.lib.spellengine.api.config.AttributeModifier> defaults;
        private final Supplier<Ingredient> repairIngredientSupplier;
        private final Holder<SoundEvent> equipSound;

        private String translatedName = "";
        public Rarity rarity = Rarity.COMMON;
        @Nullable private Item registeredItem;

        @Nullable public SpellChoice spellChoice;
        @Nullable public SpellContainer spellContainer;

        public Equipment.WeaponType category = Equipment.WeaponType.LONG_BOW;
        public Equipment.LootProperties lootProperties = Equipment.LootProperties.EMPTY;

        public Entry(
                Identifier id,
                Equipment.Tier tier,
                List<com.ultra.megamod.lib.spellengine.api.config.AttributeModifier> defaults,
                Supplier<Ingredient> repairIngredientSupplier,
                Holder<SoundEvent> equipSound
        ) {
            this.id = id;
            this.tier = tier;
            this.lootProperties = Equipment.LootProperties.of(tier.getNumber());
            this.defaults = defaults;
            this.repairIngredientSupplier = repairIngredientSupplier;
            this.equipSound = equipSound;
        }

        // Getters

        @Nullable
        public Item item() {
            return registeredItem;
        }

        public Identifier id() {
            return id;
        }

        public Equipment.Tier tier() {
            return tier;
        }

        public List<com.ultra.megamod.lib.spellengine.api.config.AttributeModifier> defaults() {
            return defaults;
        }

        public Supplier<Ingredient> repairIngredientSupplier() {
            return repairIngredientSupplier;
        }

        public Holder<SoundEvent> equipSound() {
            return equipSound;
        }

        /**
         * Calculate durability based on tier.
         * Follows 2x progression pattern: 168 -> 336 -> 672 -> 1344 -> 2688
         */
        public int durability() {
            return switch (tier) {
                case WOODEN, GOLDEN -> 168;
                case TIER_0 -> 168;
                case TIER_1 -> 336;  // Vanilla shield
                case TIER_2 -> 672;  // 2x t1
                case TIER_3 -> 1344;
                case TIER_4 -> 2688;
                case TIER_5 -> 4032;
            };
        }

        /**
         * Create the shield item using the provided factory.
         */
        public Item create(
                Item.Properties settings,
                List<com.ultra.megamod.lib.spellengine.api.config.AttributeModifier> attributes,
                ShieldFactory factory
        ) {
            // Convert AttributeModifier list to format expected by shield factory
            ArrayList<Pair<Holder<Attribute>, AttributeModifier>> shieldAttributes = new ArrayList<>();
            for (var modifier : Weapon.attributesFrom(attributes).modifiers()) {
                shieldAttributes.add(new Pair<>(modifier.attribute(), modifier.modifier()));
            }

            this.registeredItem = factory.create(
                    equipSound,
                    repairIngredientSupplier,
                    shieldAttributes,
                    settings.durability(durability())
            );
            return this.registeredItem;
        }

        // Chainable methods

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

        public Entry rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
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

    /**
     * Register shield entries with the provided factory.
     */
    public static void register(
            Map<String, ShieldConfig> configs,
            List<Entry> entries,
            ResourceKey<CreativeModeTab> itemGroupKey,
            @Nullable ShieldFactory factory
    ) {
        if (factory == null) {
            factory = DEFAULT_FACTORY;
        }
        final ShieldFactory resolvedFactory = factory;
        ArrayList<Item> shields = new ArrayList<>();

        for (var entry : entries) {
            // Get or create config
            var config = configs.get(entry.id.toString());
            if (config == null) {
                config = new ShieldConfig();
                config.durability = entry.durability();
                config.attributes = entry.defaults;
                configs.put(entry.id.toString(), config);
            }

            // Create and register item - factory passed here (deferred)
            final var finalConfig = config;
            final var finalEntry = entry;
            RPGItemRegistry.registerItem(entry.id.getPath(), (props) -> {
                if (finalEntry.tier().getNumber() >= Equipment.Tier.TIER_3.getNumber()) {
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
                var shield = finalEntry.create(props, finalConfig.attributes, resolvedFactory);
                finalEntry.registeredItem = shield;
                shields.add(shield);
                return shield;
            });
        }

        // TODO: 1.21.11 - ItemGroupEvents is Fabric-only; use NeoForge creative tab event instead
    }
}
