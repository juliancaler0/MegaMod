package com.ultra.megamod.feature.combat.relics.item;

import com.ultra.megamod.MegaMod;
import com.ultra.megamod.feature.combat.relics.config.ItemConfig;
import com.ultra.megamod.feature.combat.relics.spell.RelicSpells;
import com.ultra.megamod.feature.combat.relics.util.RelicDefaults;
import com.ultra.megamod.lib.rangedweapon.api.EntityAttributes_RangedWeapon;
import com.ultra.megamod.lib.spellengine.api.config.AttributeModifier;
import com.ultra.megamod.lib.spellengine.api.config.ConfigUtil;
import com.ultra.megamod.lib.spellengine.api.entity.SpellEngineAttributes;
import com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainer;
import com.ultra.megamod.lib.spellengine.api.spell.container.SpellContainers;
import com.ultra.megamod.lib.spellpower.api.SpellSchools;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Ported 1:1 from Relics-1.21.1's RelicItems.
 *
 * Registers all 48 relic items with their attribute modifiers and SpellContainer components
 * so ref-mod's RelicSpells bindings activate. Uses NeoForge DeferredRegister instead of
 * Fabric's direct Registry.register. Default config values ship via {@link RelicDefaults}
 * (replacing tiny_config's JSON loader which isn't ported).
 */
public class RelicItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MegaMod.MODID);

    public static final List<Entry> entries = new ArrayList<>();

    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final class Entry {
        private final int tier;
        public String lootTheme;
        private final String name;
        private final String translatedName;
        private ItemConfig.Entry config;
        public ItemConfig.Entry defaults;
        private final DeferredHolder<Item, Item> deferred;
        private final Supplier<Item> item;
        private SpellContainer spellContainer;

        public Entry(int tier, String name, String translatedName) {
            this(tier, name, translatedName, ItemConfig.Entry.EMPTY);
            this.lootTheme = "generic";
        }

        public Entry(int tier, String name, String translatedName, ItemConfig.Entry config) {
            this.tier = tier;
            this.name = name;
            this.translatedName = translatedName;
            this.config = config;
            this.defaults = config;

            // Use DeferredRegister.registerItem(...) which calls properties.setId(key)
            // before the factory runs — required by 1.21.11's Item constructor.
            this.deferred = ITEMS.registerItem(name, settings -> {
                settings = settings.stacksTo(1);
                var selectedAttributes = config().selectedAttributes();
                var attributes = (selectedAttributes != null && !selectedAttributes.isEmpty())
                        ? ConfigUtil.attributesComponent(Identifier.fromNamespaceAndPath(MegaMod.MODID, name), selectedAttributes).build()
                        : null;
                var container = spellContainer();
                if (container != null) {
                    settings = settings.component(SpellDataComponents.SPELL_CONTAINER, container);
                }
                if (config().durability > 0) {
                    settings = settings.durability(config().durability);
                }
                var rarity = rarityFrom(tier);
                if (rarity != Rarity.COMMON) {
                    settings = settings.rarity(rarity);
                }
                return RelicFactory.getFactory().apply(new RelicFactory.ItemArgs(settings, attributes));
            });
            this.item = this.deferred;
        }

        private static Rarity rarityFrom(int tier) {
            return switch (tier) {
                case 0, 1 -> Rarity.COMMON;
                case 2 -> Rarity.UNCOMMON;
                case 3 -> Rarity.RARE;
                default -> Rarity.EPIC;
            };
        }

        public int tier() { return tier; }
        public Identifier id() { return Identifier.fromNamespaceAndPath(MegaMod.MODID, name); }
        public String name() { return name; }
        public String translatedName() { return translatedName; }
        public ItemConfig.Entry config() { return config; }
        public Supplier<Item> item() { return item; }
        public DeferredHolder<Item, Item> deferred() { return deferred; }
        @Nullable public SpellContainer spellContainer() { return spellContainer; }

        public Entry config(ItemConfig.Entry config) { this.config = config; return this; }
        public Entry spell(SpellContainer container) { this.spellContainer = container; return this; }
        public Entry lootTheme(String theme) { this.lootTheme = theme; return this; }

        public boolean isEnabled() { return true; }
    }

    public static final String COMBAT_ROLL_MODID = "combat_roll";
    public static final String COMBAT_ROLL_COUNT = COMBAT_ROLL_MODID + ":count";
    public static final String CRITICAL_STRIKE_MODID = "critical_strike";
    public static final String CRITICAL_STRIKE_CHANCE = CRITICAL_STRIKE_MODID + ":chance";
    public static final String CRITICAL_STRIKE_DAMAGE = CRITICAL_STRIKE_MODID + ":damage";

    private static final float tier_0_multiplier = 0.05F;

    // ═══════════════════════════════════════════════════════════════
    // TIER 1 — Figurines
    // ═══════════════════════════════════════════════════════════════
    public static final Entry JEWEL_FIGURINE_RUBY = add(new Entry(1, "jewel_figurine_ruby", "Ruby Serpent Figurine"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(Attributes.ATTACK_DAMAGE.getRegisteredName(), tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE)
            )));
    public static final Entry JEWEL_FIGURINE_TOPAZ = add(new Entry(1, "jewel_figurine_topaz", "Topaz Fox Figurine"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(SpellSchools.ARCANE.id, tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(SpellSchools.FIRE.id, tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE)
            )));
    public static final Entry JEWEL_FIGURINE_CITRINE = add(new Entry(1, "jewel_figurine_citrine", "Citrine Cat Figurine"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(SpellSchools.HEALING.id, tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(SpellSchools.LIGHTNING.id, tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE)
            )));
    public static final Entry JEWEL_FIGURINE_JADE = add(new Entry(1, "jewel_figurine_jade", "Jade Hawk Figurine"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(EntityAttributes_RangedWeapon.DAMAGE.id, tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE)
            )));
    public static final Entry JEWEL_FIGURINE_SAPPHIRE = add(new Entry(1, "jewel_figurine_sapphire", "Sapphire Turtle Figurine"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(Attributes.MAX_HEALTH.getRegisteredName(), 2, Operation.ADD_VALUE)
            )));
    public static final Entry JEWEL_FIGURINE_TANZANITE = add(new Entry(1, "jewel_figurine_tanzanite", "Tanzanite Bat Figurine"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(SpellSchools.FROST.id, tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE),
                    new AttributeModifier(SpellSchools.SOUL.id, tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE)
            )));

    // ═══════════════════════════════════════════════════════════════
    // TIER 1 — Lesser passive trinkets
    // ═══════════════════════════════════════════════════════════════
    public static final Entry LESSER_ROLL = add(new Entry(1, "lesser_roll", "Feather Talisman"))
            .config(new ItemConfig.Entry()
                    .withAttributes(List.of(
                            new AttributeModifier(Attributes.MOVEMENT_SPEED.getRegisteredName(), 0.1F, Operation.ADD_MULTIPLIED_BASE)
                    ))
                    .withConditionalAttributes(COMBAT_ROLL_MODID, List.of(
                            new AttributeModifier(COMBAT_ROLL_COUNT, 1, Operation.ADD_VALUE)
                    )));
    public static final Entry LESSER_EVASION = add(new Entry(1, "lesser_evasion", "Lucky Coin"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(SpellEngineAttributes.EVASION_CHANCE.id.toString(), 0.03F, Operation.ADD_MULTIPLIED_BASE)
            )));
    public static final Entry LESSER_MELEE_CRIT_CHANCE = add(new Entry(1, "lesser_melee_crit_chance", "Dice of Fate"))
            .config(new ItemConfig.Entry()
                    .withAttributes(List.of(
                            new AttributeModifier(Attributes.ATTACK_DAMAGE.getRegisteredName(), tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE)
                    ))
                    .withConditionalAttributes(CRITICAL_STRIKE_MODID, List.of(
                            new AttributeModifier(CRITICAL_STRIKE_CHANCE, 0.05F, Operation.ADD_MULTIPLIED_BASE)
                    )));
    public static final Entry LESSER_MELEE_CRIT_DAMAGE = add(new Entry(1, "lesser_melee_crit_damage", "Serrated Fang"))
            .config(new ItemConfig.Entry()
                    .withAttributes(List.of(
                            new AttributeModifier(Attributes.ATTACK_DAMAGE.getRegisteredName(), tier_0_multiplier, Operation.ADD_MULTIPLIED_BASE)
                    ))
                    .withConditionalAttributes(CRITICAL_STRIKE_MODID, List.of(
                            new AttributeModifier(CRITICAL_STRIKE_DAMAGE, 0.1F, Operation.ADD_MULTIPLIED_BASE)
                    )));

    // ═══════════════════════════════════════════════════════════════
    // TIER 1 — Use/Proc spells
    // ═══════════════════════════════════════════════════════════════
    public static final Entry LESSER_USE_DAMAGE = add(new Entry(1, "lesser_use_damage", "Meteorite Whetstone"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_use_damage.id()));
    public static final Entry LESSER_USE_DEX = add(new Entry(1, "lesser_use_dex", "Medal of Valor"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_use_dex.id()));
    public static final Entry LESSER_USE_RANGED = add(new Entry(1, "lesser_use_ranged", "Eagle Eye"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_use_ranged.id()));
    public static final Entry LESSER_USE_HEALTH = add(new Entry(1, "lesser_use_health", "Everflowing Vial"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_use_health.id()));
    public static final Entry LESSER_USE_SPELL_POWER = add(new Entry(1, "lesser_use_spell_power", "Silver Crescent"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_use_spell_power.id()));
    public static final Entry LESSER_USE_SPELL_HASTE = add(new Entry(1, "lesser_use_spell_haste", "Sorcerer's Chronograph"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_use_spell_haste.id()));
    public static final Entry LESSER_PROC_SPELL_CRIT = add(new Entry(1, "lesser_proc_spell_crit", "Scarab of Infinite Mysteries"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_proc_spell_crit.id()));
    public static final Entry LESSER_PROC_CRIT_DAMAGE = add(new Entry(1, "lesser_proc_crit_damage", "Splintered Focus Crystal"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_proc_crit_damage.id()));
    public static final Entry LESSER_PROC_ARCANE_FIRE = add(new Entry(1, "lesser_proc_arcane_fire", "Spellfire Stone"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_proc_arcane_fire.id()));
    public static final Entry LESSER_PROC_FROST_HEALING = add(new Entry(1, "lesser_proc_frost_healing", "Frozen Lotus"))
            .spell(SpellContainers.forRelic(RelicSpells.lesser_proc_frost_healing.id()));

    // ═══════════════════════════════════════════════════════════════
    // TIER 2 — Medium procs/uses
    // ═══════════════════════════════════════════════════════════════
    public static final Entry MEDIUM_PROC_ATTACK_DAMAGE = add(new Entry(2, "medium_proc_attack_damage", "Badge of Tenacity"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_proc_attack_damage.id()));
    public static final Entry MEDIUM_PROC_ATTACK_SPEED = add(new Entry(2, "medium_proc_attack_speed", "Dragon Skull Trophy"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_proc_attack_speed.id()));
    public static final Entry MEDIUM_PROC_RANGED_DAMAGE = add(new Entry(2, "medium_proc_ranged_damage", "Golden Bowstring"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_proc_ranged_damage.id()));
    public static final Entry MEDIUM_PROC_DEFENSE = add(new Entry(2, "medium_proc_defense", "Titanium Nautilus Shell"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_proc_defense.id()));
    public static final Entry MEDIUM_PROC_EVASION = add(new Entry(2, "medium_proc_evasion", "Monkey Talisman"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_proc_evasion.id()));
    public static final Entry MEDIUM_PROC_SPELL_POWER = add(new Entry(2, "medium_proc_spell_power", "Crystal Skull"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_proc_spell_power.id()));
    public static final Entry MEDIUM_PROC_SPELL_HASTE = add(new Entry(2, "medium_proc_spell_haste", "Hourglass of the Unraveller"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_proc_spell_haste.id()));
    public static final Entry MEDIUM_USE_ARCANE_POWER = add(new Entry(2, "medium_proc_arcane_power", "Arcane Orb"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_use_arcane_power.id()));
    public static final Entry MEDIUM_USE_FIRE_POWER = add(new Entry(2, "medium_proc_fire_power", "Fire Orb"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_use_fire_power.id()));
    public static final Entry MEDIUM_USE_FROST_POWER = add(new Entry(2, "medium_proc_frost_power", "Frost Orb"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_use_frost_power.id()));
    public static final Entry MEDIUM_USE_HEALING_POWER = add(new Entry(2, "medium_proc_healing_power", "Holy Orb"))
            .spell(SpellContainers.forRelic(RelicSpells.medium_use_healing_power.id()));

    // ═══════════════════════════════════════════════════════════════
    // TIER 3 — Greater perks/procs (RARE)
    // ═══════════════════════════════════════════════════════════════
    public static final Entry GREATER_PERK_ROLL_DAMAGE = add(new Entry(3, "greater_perk_roll_damage", "Thunderbird Feather"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_roll_damage.id()));
    public static final Entry GREATER_HEALING_TAKEN = add(new Entry(3, "greater_healing_taken", "Ankh"))
            .config(new ItemConfig.Entry().withAttributes(List.of(
                    new AttributeModifier(SpellEngineAttributes.HEALING_TAKEN.id.toString(), 0.1F, Operation.ADD_MULTIPLIED_BASE)
            )));
    public static final Entry GREATER_PERK_MELEE_STUN = add(new Entry(3, "greater_perk_melee_stun", "Blackjack"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_melee_stun.id()));
    public static final Entry GREATER_PERK_RANGED_LEVITATE = add(new Entry(3, "greater_perk_ranged_levitate", "Updraft Arrow"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_ranged_levitate.id()));
    public static final Entry GREATER_PERK_SPELL_STUN = add(new Entry(3, "greater_perk_spell_stun", "Malevolent Gaze"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_spell_stun.id()));
    public static final Entry GREATER_PERK_DEFENSE_BLOCK = add(new Entry(3, "greater_perk_defense_block", "Sacred Wardstone"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_defense_block.id()));
    public static final Entry GREATER_PERK_EVASION_ATTACK = add(new Entry(3, "greater_perk_evasion_attack", "Captain's Hook"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_evasion_attack.id()));
    public static final Entry GREATER_PERK_SHIELD_RESET = add(new Entry(3, "greater_perk_shield_reset", "Etienne's Enigma"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_shield_reset.id()));
    public static final Entry GREATER_PERK_HEAL_CLEANSE = add(new Entry(3, "greater_perk_heal_cleanse", "Holy Water"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_heal_cleanse.id()));
    public static final Entry GREATER_PROC_PHYSICAL_TRANCE = add(new Entry(3, "greater_proc_physical_trance", "Sharpened Dragon Scale"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_proc_physical_trance.id()))
            .lootTheme("dragon");
    public static final Entry GREATER_PROC_SPELL_TRANCE = add(new Entry(3, "greater_proc_spell_trance", "Twisted Dragon Scale"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_proc_spell_trance.id()))
            .lootTheme("dragon");
    public static final Entry GREATER_PERK_HEAL_DANGER = add(new Entry(3, "greater_perk_heal_danger", "Verdant Dragon Scale"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_perk_heal_danger.id()))
            .lootTheme("dragon");
    public static final Entry GREATER_PROC_DEFENSE_DANGER = add(new Entry(3, "greater_proc_defense_danger", "Petrified Dragon Scale"))
            .spell(SpellContainers.forRelic(RelicSpells.greater_proc_defense_danger.id()))
            .lootTheme("dragon");

    // ═══════════════════════════════════════════════════════════════
    // TIER 4 — Superior (EPIC)
    // ═══════════════════════════════════════════════════════════════
    public static final Entry SUPERIOR_USE_AREA_ATTACK_DAMAGE = add(new Entry(4, "superior_use_area_attack_damage", "Horn of the White City"))
            .spell(SpellContainers.forRelic(RelicSpells.superior_use_area_attack_damage.id()));
    public static final Entry SUPERIOR_USE_AREA_DEFENSE_HEALTH = add(new Entry(4, "superior_use_area_defense_health", "Heart of the Beast"))
            .spell(SpellContainers.forRelic(RelicSpells.superior_use_area_defense_health.id()));
    public static final Entry SUPERIOR_USE_ZONE_SPELL_POWER = add(new Entry(4, "superior_use_zone_spell_power", "Black Orb"))
            .spell(SpellContainers.forRelic(RelicSpells.superior_use_zone_spell_power.id()));
    public static final Entry SUPERIOR_USE_ZONE_HEALING_TAKEN = add(new Entry(4, "superior_use_zone_healing_taken", "Glimmering Crystal Sliver"))
            .spell(SpellContainers.forRelic(RelicSpells.superior_use_zone_healing_taken.id()));

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }

    /**
     * Finalize config-entry wiring. Called after the mod-load config step when
     * the caller has a prepared config Map. With no tiny_config, we seed from
     * {@link RelicDefaults} so attribute values end up on the items even without
     * a user-side config file.
     */
    public static void register(Map<String, ItemConfig.Entry> config) {
        for (var entry : entries) {
            var key = entry.id().toString();
            var configEntry = config.get(key);
            if (configEntry != null) {
                entry.config(configEntry);
            } else {
                config.put(key, entry.config());
            }
        }
    }
}
