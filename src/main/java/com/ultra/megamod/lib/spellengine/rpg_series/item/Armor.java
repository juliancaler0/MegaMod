package com.ultra.megamod.lib.spellengine.rpg_series.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.spellengine.api.config.ArmorSetConfig;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Armor {

    public static class CustomItem extends Item implements ConfigurableAttributes {

        public final ArmorMaterial customMaterial;
        private final EquipmentSlot slot;
        private ItemAttributeModifiers attributeModifiers = ItemAttributeModifiers.EMPTY;

        public CustomItem(ArmorMaterial material, EquipmentSlot slot, Item.Properties settings) {
            super(settings);
            this.customMaterial = material;
            this.slot = slot;
        }

        public EquipmentSlot getSlotType() {
            return slot;
        }

        @Override
        public void setAttributes(ItemAttributeModifiers attributeModifiers) {
            this.attributeModifiers = attributeModifiers;
        }

        public ItemAttributeModifiers getAttributeModifiers() {
            return this.attributeModifiers;
        }

        public Identifier getFirstLayerId() {
            return Identifier.fromNamespaceAndPath("megamod", "armor_layer");
        }
    }

    public static class Set<A extends Item> {
        public final String namespace;
        public final String name;
        public A head, chest, legs, feet;
        public String headTranslation, chestTranslation, legsTranslation, feetTranslation = "";
        // Factories for deferred creation — accept Item.Properties from the registry
        private Function<Item.Properties, A> headFactory, chestFactory, legsFactory, feetFactory;

        public Set(String namespace, String name, A head, A chest, A legs, A feet) {
            this.namespace = namespace;
            this.name = name;
            this.head = head;
            this.chest = chest;
            this.legs = legs;
            this.feet = feet;
        }

        /** Deferred construction using factories that accept registry-provided Item.Properties */
        public Set(String namespace, String name,
                   Function<Item.Properties, A> headFactory, Function<Item.Properties, A> chestFactory,
                   Function<Item.Properties, A> legsFactory, Function<Item.Properties, A> feetFactory) {
            this.namespace = namespace;
            this.name = name;
            this.headFactory = headFactory;
            this.chestFactory = chestFactory;
            this.legsFactory = legsFactory;
            this.feetFactory = feetFactory;
        }

        private void ensureCreated() {
            if (head == null && headFactory != null) head = headFactory.apply(new Item.Properties());
            if (chest == null && chestFactory != null) chest = chestFactory.apply(new Item.Properties());
            if (legs == null && legsFactory != null) legs = legsFactory.apply(new Item.Properties());
            if (feet == null && feetFactory != null) feet = feetFactory.apply(new Item.Properties());
        }

        public List<A> pieces() {
            ensureCreated();
            return Stream.of(head, chest, legs, feet).filter(Objects::nonNull).collect(Collectors.toList());
        }

        public Identifier idOf(Item piece) {
            var slotName = "unknown";
            if (piece instanceof CustomItem customItem) {
                slotName = customItem.getSlotType().getName();
            }
            var itemName = this.name + "_" + slotName;
            return Identifier.fromNamespaceAndPath(namespace, itemName);
        }

        /** Get piece IDs without creating items (uses slot names directly) */
        public List<String> slotNames() {
            return List.of("head", "chest", "legs", "feet");
        }

        public List<String> idStrings() {
            return pieces().stream().map(piece -> idOf(piece).toString()).toList();
        }
        public List<Identifier> pieceIds() {
            return pieces().stream().map(this::idOf).toList();
        }

        public Set<A> translate(String headName, String chestName, String legsName, String feetName) {
            this.headTranslation = headName;
            this.chestTranslation = chestName;
            this.legsTranslation = legsName;
            this.feetTranslation = feetName;
            return this;
        }

        @SuppressWarnings("unchecked")
        public void register(ResourceKey<CreativeModeTab> itemGroupKey) {
            // Register each piece independently via RPGItemRegistry.registerItem().
            // Each piece gets registry-provided Item.Properties with the ResourceKey set
            // (required in NeoForge 1.21.11).
            final var setRef = this;

            if (headFactory != null) {
                RPGItemRegistry.registerItem(this.name + "_head", (props) -> {
                    setRef.head = (A) setRef.headFactory.apply(props);
                    return setRef.head;
                });
            }
            if (chestFactory != null) {
                RPGItemRegistry.registerItem(this.name + "_chest", (props) -> {
                    setRef.chest = (A) setRef.chestFactory.apply(props);
                    return setRef.chest;
                });
            }
            if (legsFactory != null) {
                RPGItemRegistry.registerItem(this.name + "_legs", (props) -> {
                    setRef.legs = (A) setRef.legsFactory.apply(props);
                    return setRef.legs;
                });
            }
            if (feetFactory != null) {
                RPGItemRegistry.registerItem(this.name + "_feet", (props) -> {
                    setRef.feet = (A) setRef.feetFactory.apply(props);
                    return setRef.feet;
                });
            }
        }

        public interface ItemFactory<T extends Item> {
            T create(ArmorMaterial material, EquipmentSlot slot, Item.Properties settings);
        }
    }

    public record ItemSettingsTweaker(Consumer<Item.Properties> helmet,
                                      Consumer<Item.Properties> chestplate,
                                      Consumer<Item.Properties> leggings,
                                      Consumer<Item.Properties> boots) {
        public static ItemSettingsTweaker standard(Consumer<Item.Properties> consumer) {
            return new ItemSettingsTweaker(consumer, consumer, consumer, consumer);
        }
    }

    // Durability multipliers matching vanilla ArmorItem behavior
    private static final int[] DURABILITY_PER_SLOT = {13, 15, 16, 11}; // boots, leggings, chestplate, helmet

    private static int getMaxDamage(EquipmentSlot slot, int baseDurability) {
        return switch (slot) {
            case FEET -> baseDurability * DURABILITY_PER_SLOT[0];
            case LEGS -> baseDurability * DURABILITY_PER_SLOT[1];
            case CHEST -> baseDurability * DURABILITY_PER_SLOT[2];
            case HEAD -> baseDurability * DURABILITY_PER_SLOT[3];
            default -> baseDurability;
        };
    }

    public record Entry(ArmorMaterial material, Armor.Set armorSet, ArmorSetConfig defaults, Equipment.LootProperties lootProperties) {
        public static Entry create(ArmorMaterial material, Identifier id, int durability, Set.ItemFactory factory, ArmorSetConfig defaults) {
            return create(material, id, durability, factory, defaults, Equipment.LootProperties.EMPTY);
        }
        public static Entry create(ArmorMaterial material, Identifier id, int durability, Set.ItemFactory factory, ArmorSetConfig defaults, Equipment.LootProperties lootProperties) {
            return create(material, id, durability, factory, defaults, lootProperties, null);
        }
        public static Entry create(ArmorMaterial material, Identifier id, int durability, Set.ItemFactory factory, ArmorSetConfig defaults,
                                   Equipment.LootProperties lootProperties, @Nullable ItemSettingsTweaker settingsTweaker) {

            // Deferred item creation — factories accept registry-provided Item.Properties
            // (with ResourceKey already set, as required by NeoForge 1.21.11)
            Function<Item.Properties, Item> headFac = (props) -> {
                props.durability(getMaxDamage(EquipmentSlot.HEAD, durability));
                if (settingsTweaker != null) settingsTweaker.helmet.accept(props);
                if (lootProperties.tier() >= 3) props.fireResistant();
                return factory.create(material, EquipmentSlot.HEAD, props);
            };
            Function<Item.Properties, Item> chestFac = (props) -> {
                props.durability(getMaxDamage(EquipmentSlot.CHEST, durability));
                if (settingsTweaker != null) settingsTweaker.chestplate.accept(props);
                if (lootProperties.tier() >= 3) props.fireResistant();
                return factory.create(material, EquipmentSlot.CHEST, props);
            };
            Function<Item.Properties, Item> legsFac = (props) -> {
                props.durability(getMaxDamage(EquipmentSlot.LEGS, durability));
                if (settingsTweaker != null) settingsTweaker.leggings.accept(props);
                if (lootProperties.tier() >= 3) props.fireResistant();
                return factory.create(material, EquipmentSlot.LEGS, props);
            };
            Function<Item.Properties, Item> feetFac = (props) -> {
                props.durability(getMaxDamage(EquipmentSlot.FEET, durability));
                if (settingsTweaker != null) settingsTweaker.boots.accept(props);
                if (lootProperties.tier() >= 3) props.fireResistant();
                return factory.create(material, EquipmentSlot.FEET, props);
            };

            var set = new Armor.Set(id.getNamespace(), id.getPath(),
                    headFac, chestFac, legsFac, feetFac
            );
            return new Entry(material, set, defaults, lootProperties);
        }


        public Entry translatedName(String headName, String chestName, String legsName, String feetName) {
            armorSet.translate(headName, chestName, legsName, feetName);
            return this;
        }

        public String name() {
            return armorSet.name;
        }

        public <T extends Item> Entry bundle(Function<ArmorMaterial, Armor.Set<T>> factory) {
            var armorSet = factory.apply(material);
            return new Entry(material, armorSet, defaults, lootProperties);
        }

        public <T extends Item> Entry put(ArrayList<Entry> list) {
            list.add(this);
            return this;
        }
    }

    // MARK: Registration

    public static void register(Map<String, ArmorSetConfig> configs, List<Entry> entries, ResourceKey<CreativeModeTab> itemGroupKey) {
        for(var entry: entries) {
            var config = configs.get(entry.name());
            if (config == null) {
                config = entry.defaults();
                configs.put(entry.name(), config);
            }
            // Register with deferred system - items will be created during registry event
            entry.armorSet().register(itemGroupKey);
        }
    }

    private static ItemAttributeModifiers attributesFrom(ArmorSetConfig config, EquipmentSlot slot) {
        ArmorSetConfig.Piece piece = null;
        var modifierId = Identifier.withDefaultNamespace("armor." + slot.getName());
        switch (slot) {
            case FEET -> {
                piece = config.feet;
            }
            case LEGS -> {
                piece = config.legs;
            }
            case CHEST -> {
                piece = config.chest;
            }
            case HEAD -> {
                piece = config.head;
            }
        }

        EquipmentSlotGroup attributeModifierSlot = switch(slot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.MAINHAND;
        };

        var builder = ItemAttributeModifiers.builder();

        if (config.armor_toughness != 0) {
            builder.add(Attributes.ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            modifierId,
                            config.armor_toughness,
                            AttributeModifier.Operation.ADD_VALUE),
                    attributeModifierSlot);
        }
        if (config.knockback_resistance != 0) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            modifierId,
                            config.knockback_resistance,
                            AttributeModifier.Operation.ADD_VALUE),
                    attributeModifierSlot);
        }
        if (piece != null && piece.armor != 0) {
            builder.add(Attributes.ARMOR,
                    new AttributeModifier(
                            modifierId,
                            piece.armor,
                            AttributeModifier.Operation.ADD_VALUE),
                    attributeModifierSlot);
        }
        if (piece != null) {
            for (var attribute : piece.selectedAttributes()) {
                try {
                    var entityAttribute = BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attribute.attribute));
                    if (entityAttribute.isPresent()) {
                        builder.add(entityAttribute.get(),
                                new AttributeModifier(
                                        modifierId,
                                        attribute.value,
                                        attribute.operation),
                                attributeModifierSlot);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to add item attribute modifier: " + e.getMessage());
                }
            }
        }

        return builder.build();
    }
}
