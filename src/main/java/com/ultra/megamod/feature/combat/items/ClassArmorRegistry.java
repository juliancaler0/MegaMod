package com.ultra.megamod.feature.combat.items;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.relics.weapons.RpgArmorItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * Class armor sets for the RPG combat system.
 * Each set has 4 pieces (head, chest, legs, feet).
 * Uses the Equippable component for slot assignment and rendering.
 * Armor and bonus stats are rolled via ArmorStatRoller on first inventory tick.
 * Set bonuses come from EquipmentSetManager.
 *
 * <p><b>SpellEngine migration:</b> Wizard robes, paladin/priest sets, and rogue/warrior
 * sets are now registered through SpellEngine's {@code Armor} factory pipeline by
 * their respective class mods ({@code WizardsMod.registerItems},
 * {@code PaladinsMod.registerItems}, {@code RoguesMod.registerItems}) so they carry
 * MODIFIER spell containers (set-bonus attributes). Their entries have been removed
 * from this registry to avoid duplicate item registrations. Legacy
 * {@code ClassArmorRegistry.XYZ_FEET} (etc.) field references have been replaced with
 * {@link Lookup} shims below so downstream callers (client renderers, trades,
 * shop, loot generators) keep compiling without churn.</p>
 *
 * <p>Archer/Ranger armor sets remain on the legacy {@link RpgArmorItem} path until
 * a dedicated Archer SpellEngine armor port exists.</p>
 */
public class ClassArmorRegistry {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    // ─── Custom equipment asset keys (point to megamod equipment JSONs + textures) ───
    private static ResourceKey<EquipmentAsset> customAsset(String name) {
        return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(MegaMod.MODID, name));
    }

    // Archer Armor (still registered here)
    private static final ResourceKey<EquipmentAsset> ARCHER_ARMOR_ASSET = customAsset("archer_armor");
    private static final ResourceKey<EquipmentAsset> RANGER_ARMOR_ASSET = customAsset("ranger_armor");
    private static final ResourceKey<EquipmentAsset> NETHERITE_RANGER_ARMOR_ASSET = customAsset("netherite_ranger_armor");

    // ─── Armor weight classes ───
    // Armor values: [head, chest, legs, feet]
    // Medium — starter 11 total (chain), T2 iron+ 17, T3 diamond+ 20
    private static final int[] MEDIUM_T1 = {2, 4, 3, 2};   // 11 total (chain)
    private static final int[] MEDIUM_T2 = {3, 6, 5, 3};   // 17 total (iron+)
    private static final int[] MEDIUM_T3 = {3, 7, 6, 4};   // 20 total (diamond+)

    // T3 adds real netherite-tier toughness on top of the higher armor values
    private static final double T3_MEDIUM_TOUGHNESS = 1.5;

    // ─── Slot index constants ───
    private static final int HEAD = 0;
    private static final int CHEST = 1;
    private static final int LEGS = 2;
    private static final int FEET = 3;

    // ─── Helper to register RPG armor with rolled stats ───
    private static DeferredItem<RpgArmorItem> rpgArmor(String name, EquipmentSlot slot,
                                                        ResourceKey<EquipmentAsset> asset,
                                                        int[] armorValues, double toughness) {
        int slotIndex = switch (slot) {
            case HEAD -> HEAD;
            case CHEST -> CHEST;
            case LEGS -> LEGS;
            case FEET -> FEET;
            default -> 0;
        };
        double baseArmor = armorValues[slotIndex];
        EquipmentSlotGroup group = switch (slot) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
            default -> EquipmentSlotGroup.ARMOR;
        };
        Identifier armorModId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor." + name);
        Identifier toughModId = Identifier.fromNamespaceAndPath(MegaMod.MODID, "armor_toughness." + name);
        ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder()
            .add(Attributes.ARMOR,
                new AttributeModifier(armorModId, baseArmor, AttributeModifier.Operation.ADD_VALUE),
                group);
        if (toughness > 0) {
            attrBuilder.add(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(toughModId, toughness, AttributeModifier.Operation.ADD_VALUE),
                group);
        }
        ItemAttributeModifiers defaultAttrs = attrBuilder.build();
        return ITEMS.registerItem(name,
            props -> new RpgArmorItem(baseArmor, toughness, slot, (Item.Properties) props),
            () -> new Item.Properties().stacksTo(1)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(slot).setAsset(asset).build())
                .component(DataComponents.ATTRIBUTE_MODIFIERS, defaultAttrs));
    }

    // ═══════════════════════════════════════════════════════════════
    // ARCHER ARMOR (3 sets) — Light leather, ranged focused
    // ═══════════════════════════════════════════════════════════════

    // --- Archer Armor (starter, T1 medium) ---
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_HEAD = rpgArmor("archer_armor_head", EquipmentSlot.HEAD, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_CHEST = rpgArmor("archer_armor_chest", EquipmentSlot.CHEST, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_LEGS = rpgArmor("archer_armor_legs", EquipmentSlot.LEGS, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);
    public static final DeferredItem<RpgArmorItem> ARCHER_ARMOR_FEET = rpgArmor("archer_armor_feet", EquipmentSlot.FEET, ARCHER_ARMOR_ASSET, MEDIUM_T1, 0);

    // --- Ranger Armor (T2 medium) ---
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_HEAD = rpgArmor("ranger_armor_head", EquipmentSlot.HEAD, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_CHEST = rpgArmor("ranger_armor_chest", EquipmentSlot.CHEST, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_LEGS = rpgArmor("ranger_armor_legs", EquipmentSlot.LEGS, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);
    public static final DeferredItem<RpgArmorItem> RANGER_ARMOR_FEET = rpgArmor("ranger_armor_feet", EquipmentSlot.FEET, RANGER_ARMOR_ASSET, MEDIUM_T2, 0);

    // --- Netherite Ranger Armor (T3 medium) ---
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_HEAD = rpgArmor("netherite_ranger_armor_head", EquipmentSlot.HEAD, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_CHEST = rpgArmor("netherite_ranger_armor_chest", EquipmentSlot.CHEST, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_LEGS = rpgArmor("netherite_ranger_armor_legs", EquipmentSlot.LEGS, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);
    public static final DeferredItem<RpgArmorItem> NETHERITE_RANGER_ARMOR_FEET = rpgArmor("netherite_ranger_armor_feet", EquipmentSlot.FEET, NETHERITE_RANGER_ARMOR_ASSET, MEDIUM_T3, T3_MEDIUM_TOUGHNESS);

    // ═══════════════════════════════════════════════════════════════
    // SpellEngine-registered armor (legacy field-alias shims)
    //
    // Wizards / Paladins / Priests / Rogues / Warriors armor IDs are now
    // registered by their respective class mods via the SpellEngine Armor
    // factory pipeline (with MODIFIER spell containers providing set-bonus
    // attributes). Downstream callers still reference ClassArmorRegistry.XYZ
    // fields; those are now lazy lookups into BuiltInRegistries.ITEM, wrapped
    // in a small Lookup helper that mimics DeferredItem's .get() surface.
    // ═══════════════════════════════════════════════════════════════

    /** Wraps a {@link BuiltInRegistries#ITEM} lookup so existing {@code .get()} call sites keep working. */
    public static final class Lookup {
        private final String path;
        private Item cached;
        private Lookup(String path) { this.path = path; }
        public Item get() {
            if (cached == null) {
                cached = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(MegaMod.MODID, path));
            }
            return cached;
        }
        public String path() { return path; }
    }

    private static Lookup item(String path) { return new Lookup(path); }

    // ─── Wizard robes (WizardsMod.registerItems -> Armors.init) ───
    public static final Lookup WIZARD_ROBE_HEAD = item("wizard_robe_head");
    public static final Lookup WIZARD_ROBE_CHEST = item("wizard_robe_chest");
    public static final Lookup WIZARD_ROBE_LEGS = item("wizard_robe_legs");
    public static final Lookup WIZARD_ROBE_FEET = item("wizard_robe_feet");

    public static final Lookup ARCANE_ROBE_HEAD = item("arcane_robe_head");
    public static final Lookup ARCANE_ROBE_CHEST = item("arcane_robe_chest");
    public static final Lookup ARCANE_ROBE_LEGS = item("arcane_robe_legs");
    public static final Lookup ARCANE_ROBE_FEET = item("arcane_robe_feet");

    public static final Lookup FIRE_ROBE_HEAD = item("fire_robe_head");
    public static final Lookup FIRE_ROBE_CHEST = item("fire_robe_chest");
    public static final Lookup FIRE_ROBE_LEGS = item("fire_robe_legs");
    public static final Lookup FIRE_ROBE_FEET = item("fire_robe_feet");

    public static final Lookup FROST_ROBE_HEAD = item("frost_robe_head");
    public static final Lookup FROST_ROBE_CHEST = item("frost_robe_chest");
    public static final Lookup FROST_ROBE_LEGS = item("frost_robe_legs");
    public static final Lookup FROST_ROBE_FEET = item("frost_robe_feet");

    public static final Lookup NETHERITE_ARCANE_ROBE_HEAD = item("netherite_arcane_robe_head");
    public static final Lookup NETHERITE_ARCANE_ROBE_CHEST = item("netherite_arcane_robe_chest");
    public static final Lookup NETHERITE_ARCANE_ROBE_LEGS = item("netherite_arcane_robe_legs");
    public static final Lookup NETHERITE_ARCANE_ROBE_FEET = item("netherite_arcane_robe_feet");

    public static final Lookup NETHERITE_FIRE_ROBE_HEAD = item("netherite_fire_robe_head");
    public static final Lookup NETHERITE_FIRE_ROBE_CHEST = item("netherite_fire_robe_chest");
    public static final Lookup NETHERITE_FIRE_ROBE_LEGS = item("netherite_fire_robe_legs");
    public static final Lookup NETHERITE_FIRE_ROBE_FEET = item("netherite_fire_robe_feet");

    public static final Lookup NETHERITE_FROST_ROBE_HEAD = item("netherite_frost_robe_head");
    public static final Lookup NETHERITE_FROST_ROBE_CHEST = item("netherite_frost_robe_chest");
    public static final Lookup NETHERITE_FROST_ROBE_LEGS = item("netherite_frost_robe_legs");
    public static final Lookup NETHERITE_FROST_ROBE_FEET = item("netherite_frost_robe_feet");

    // ─── Paladin plate (PaladinsMod.registerItems -> Armors.init) ───
    public static final Lookup PALADIN_ARMOR_HEAD = item("paladin_armor_head");
    public static final Lookup PALADIN_ARMOR_CHEST = item("paladin_armor_chest");
    public static final Lookup PALADIN_ARMOR_LEGS = item("paladin_armor_legs");
    public static final Lookup PALADIN_ARMOR_FEET = item("paladin_armor_feet");

    public static final Lookup CRUSADER_ARMOR_HEAD = item("crusader_armor_head");
    public static final Lookup CRUSADER_ARMOR_CHEST = item("crusader_armor_chest");
    public static final Lookup CRUSADER_ARMOR_LEGS = item("crusader_armor_legs");
    public static final Lookup CRUSADER_ARMOR_FEET = item("crusader_armor_feet");

    public static final Lookup NETHERITE_CRUSADER_ARMOR_HEAD = item("netherite_crusader_armor_head");
    public static final Lookup NETHERITE_CRUSADER_ARMOR_CHEST = item("netherite_crusader_armor_chest");
    public static final Lookup NETHERITE_CRUSADER_ARMOR_LEGS = item("netherite_crusader_armor_legs");
    public static final Lookup NETHERITE_CRUSADER_ARMOR_FEET = item("netherite_crusader_armor_feet");

    // ─── Priest robes (PaladinsMod.registerItems -> Armors.init) ───
    public static final Lookup PRIEST_ROBE_HEAD = item("priest_robe_head");
    public static final Lookup PRIEST_ROBE_CHEST = item("priest_robe_chest");
    public static final Lookup PRIEST_ROBE_LEGS = item("priest_robe_legs");
    public static final Lookup PRIEST_ROBE_FEET = item("priest_robe_feet");

    public static final Lookup PRIOR_ROBE_HEAD = item("prior_robe_head");
    public static final Lookup PRIOR_ROBE_CHEST = item("prior_robe_chest");
    public static final Lookup PRIOR_ROBE_LEGS = item("prior_robe_legs");
    public static final Lookup PRIOR_ROBE_FEET = item("prior_robe_feet");

    public static final Lookup NETHERITE_PRIOR_ROBE_HEAD = item("netherite_prior_robe_head");
    public static final Lookup NETHERITE_PRIOR_ROBE_CHEST = item("netherite_prior_robe_chest");
    public static final Lookup NETHERITE_PRIOR_ROBE_LEGS = item("netherite_prior_robe_legs");
    public static final Lookup NETHERITE_PRIOR_ROBE_FEET = item("netherite_prior_robe_feet");

    // ─── Rogue medium (RoguesMod.registerItems -> Armors.init) ───
    public static final Lookup ROGUE_ARMOR_HEAD = item("rogue_armor_head");
    public static final Lookup ROGUE_ARMOR_CHEST = item("rogue_armor_chest");
    public static final Lookup ROGUE_ARMOR_LEGS = item("rogue_armor_legs");
    public static final Lookup ROGUE_ARMOR_FEET = item("rogue_armor_feet");

    public static final Lookup ASSASSIN_ARMOR_HEAD = item("assassin_armor_head");
    public static final Lookup ASSASSIN_ARMOR_CHEST = item("assassin_armor_chest");
    public static final Lookup ASSASSIN_ARMOR_LEGS = item("assassin_armor_legs");
    public static final Lookup ASSASSIN_ARMOR_FEET = item("assassin_armor_feet");

    public static final Lookup NETHERITE_ASSASSIN_ARMOR_HEAD = item("netherite_assassin_armor_head");
    public static final Lookup NETHERITE_ASSASSIN_ARMOR_CHEST = item("netherite_assassin_armor_chest");
    public static final Lookup NETHERITE_ASSASSIN_ARMOR_LEGS = item("netherite_assassin_armor_legs");
    public static final Lookup NETHERITE_ASSASSIN_ARMOR_FEET = item("netherite_assassin_armor_feet");

    // ─── Warrior plate (RoguesMod.registerItems -> Armors.init) ───
    public static final Lookup WARRIOR_ARMOR_HEAD = item("warrior_armor_head");
    public static final Lookup WARRIOR_ARMOR_CHEST = item("warrior_armor_chest");
    public static final Lookup WARRIOR_ARMOR_LEGS = item("warrior_armor_legs");
    public static final Lookup WARRIOR_ARMOR_FEET = item("warrior_armor_feet");

    public static final Lookup BERSERKER_ARMOR_HEAD = item("berserker_armor_head");
    public static final Lookup BERSERKER_ARMOR_CHEST = item("berserker_armor_chest");
    public static final Lookup BERSERKER_ARMOR_LEGS = item("berserker_armor_legs");
    public static final Lookup BERSERKER_ARMOR_FEET = item("berserker_armor_feet");

    public static final Lookup NETHERITE_BERSERKER_ARMOR_HEAD = item("netherite_berserker_armor_head");
    public static final Lookup NETHERITE_BERSERKER_ARMOR_CHEST = item("netherite_berserker_armor_chest");
    public static final Lookup NETHERITE_BERSERKER_ARMOR_LEGS = item("netherite_berserker_armor_legs");
    public static final Lookup NETHERITE_BERSERKER_ARMOR_FEET = item("netherite_berserker_armor_feet");

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
    }

    // ─── Loot tier helpers ───

    /** Null-tolerant builder so the list doesn't contain AIR entries if SpellEngine items haven't fully loaded yet. */
    private static void addItem(List<Item> out, Item item) {
        if (item != null && item != net.minecraft.world.item.Items.AIR) out.add(item);
    }

    public static java.util.List<net.minecraft.world.item.Item> getTier1Items() {
        List<Item> out = new ArrayList<>();
        addItem(out, WIZARD_ROBE_HEAD.get()); addItem(out, WIZARD_ROBE_CHEST.get()); addItem(out, WIZARD_ROBE_LEGS.get()); addItem(out, WIZARD_ROBE_FEET.get());
        addItem(out, PALADIN_ARMOR_HEAD.get()); addItem(out, PALADIN_ARMOR_CHEST.get()); addItem(out, PALADIN_ARMOR_LEGS.get()); addItem(out, PALADIN_ARMOR_FEET.get());
        addItem(out, PRIEST_ROBE_HEAD.get()); addItem(out, PRIEST_ROBE_CHEST.get()); addItem(out, PRIEST_ROBE_LEGS.get()); addItem(out, PRIEST_ROBE_FEET.get());
        addItem(out, ROGUE_ARMOR_HEAD.get()); addItem(out, ROGUE_ARMOR_CHEST.get()); addItem(out, ROGUE_ARMOR_LEGS.get()); addItem(out, ROGUE_ARMOR_FEET.get());
        addItem(out, WARRIOR_ARMOR_HEAD.get()); addItem(out, WARRIOR_ARMOR_CHEST.get()); addItem(out, WARRIOR_ARMOR_LEGS.get()); addItem(out, WARRIOR_ARMOR_FEET.get());
        out.add(ARCHER_ARMOR_HEAD.get()); out.add(ARCHER_ARMOR_CHEST.get()); out.add(ARCHER_ARMOR_LEGS.get()); out.add(ARCHER_ARMOR_FEET.get());
        return out;
    }

    public static java.util.List<net.minecraft.world.item.Item> getTier2Items() {
        List<Item> out = new ArrayList<>();
        addItem(out, ARCANE_ROBE_HEAD.get()); addItem(out, ARCANE_ROBE_CHEST.get()); addItem(out, ARCANE_ROBE_LEGS.get()); addItem(out, ARCANE_ROBE_FEET.get());
        addItem(out, FIRE_ROBE_HEAD.get()); addItem(out, FIRE_ROBE_CHEST.get()); addItem(out, FIRE_ROBE_LEGS.get()); addItem(out, FIRE_ROBE_FEET.get());
        addItem(out, FROST_ROBE_HEAD.get()); addItem(out, FROST_ROBE_CHEST.get()); addItem(out, FROST_ROBE_LEGS.get()); addItem(out, FROST_ROBE_FEET.get());
        addItem(out, CRUSADER_ARMOR_HEAD.get()); addItem(out, CRUSADER_ARMOR_CHEST.get()); addItem(out, CRUSADER_ARMOR_LEGS.get()); addItem(out, CRUSADER_ARMOR_FEET.get());
        addItem(out, PRIOR_ROBE_HEAD.get()); addItem(out, PRIOR_ROBE_CHEST.get()); addItem(out, PRIOR_ROBE_LEGS.get()); addItem(out, PRIOR_ROBE_FEET.get());
        addItem(out, ASSASSIN_ARMOR_HEAD.get()); addItem(out, ASSASSIN_ARMOR_CHEST.get()); addItem(out, ASSASSIN_ARMOR_LEGS.get()); addItem(out, ASSASSIN_ARMOR_FEET.get());
        addItem(out, BERSERKER_ARMOR_HEAD.get()); addItem(out, BERSERKER_ARMOR_CHEST.get()); addItem(out, BERSERKER_ARMOR_LEGS.get()); addItem(out, BERSERKER_ARMOR_FEET.get());
        out.add(RANGER_ARMOR_HEAD.get()); out.add(RANGER_ARMOR_CHEST.get()); out.add(RANGER_ARMOR_LEGS.get()); out.add(RANGER_ARMOR_FEET.get());
        return out;
    }

    public static java.util.List<net.minecraft.world.item.Item> getTier3Items() {
        List<Item> out = new ArrayList<>();
        addItem(out, NETHERITE_ARCANE_ROBE_HEAD.get()); addItem(out, NETHERITE_ARCANE_ROBE_CHEST.get()); addItem(out, NETHERITE_ARCANE_ROBE_LEGS.get()); addItem(out, NETHERITE_ARCANE_ROBE_FEET.get());
        addItem(out, NETHERITE_FIRE_ROBE_HEAD.get()); addItem(out, NETHERITE_FIRE_ROBE_CHEST.get()); addItem(out, NETHERITE_FIRE_ROBE_LEGS.get()); addItem(out, NETHERITE_FIRE_ROBE_FEET.get());
        addItem(out, NETHERITE_FROST_ROBE_HEAD.get()); addItem(out, NETHERITE_FROST_ROBE_CHEST.get()); addItem(out, NETHERITE_FROST_ROBE_LEGS.get()); addItem(out, NETHERITE_FROST_ROBE_FEET.get());
        addItem(out, NETHERITE_CRUSADER_ARMOR_HEAD.get()); addItem(out, NETHERITE_CRUSADER_ARMOR_CHEST.get()); addItem(out, NETHERITE_CRUSADER_ARMOR_LEGS.get()); addItem(out, NETHERITE_CRUSADER_ARMOR_FEET.get());
        addItem(out, NETHERITE_PRIOR_ROBE_HEAD.get()); addItem(out, NETHERITE_PRIOR_ROBE_CHEST.get()); addItem(out, NETHERITE_PRIOR_ROBE_LEGS.get()); addItem(out, NETHERITE_PRIOR_ROBE_FEET.get());
        addItem(out, NETHERITE_ASSASSIN_ARMOR_HEAD.get()); addItem(out, NETHERITE_ASSASSIN_ARMOR_CHEST.get()); addItem(out, NETHERITE_ASSASSIN_ARMOR_LEGS.get()); addItem(out, NETHERITE_ASSASSIN_ARMOR_FEET.get());
        addItem(out, NETHERITE_BERSERKER_ARMOR_HEAD.get()); addItem(out, NETHERITE_BERSERKER_ARMOR_CHEST.get()); addItem(out, NETHERITE_BERSERKER_ARMOR_LEGS.get()); addItem(out, NETHERITE_BERSERKER_ARMOR_FEET.get());
        out.add(NETHERITE_RANGER_ARMOR_HEAD.get()); out.add(NETHERITE_RANGER_ARMOR_CHEST.get()); out.add(NETHERITE_RANGER_ARMOR_LEGS.get()); out.add(NETHERITE_RANGER_ARMOR_FEET.get());
        return out;
    }
}
