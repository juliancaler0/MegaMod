package com.ultra.megamod.lib.spellpower;

import com.ultra.megamod.lib.spellpower.api.*;
import com.ultra.megamod.lib.spellpower.config.AttributesConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SpellPowerMod {
    public static final String ID = "megamod";

    // Deferred registers for NeoForge
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, ID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, ID);
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, ID);

    // Config - using static defaults instead of tiny_config
    private static final AttributesConfig config = AttributesConfig.defaults();

    public static AttributesConfig safeConfig() {
        return config;
    }

    /**
     * Initialize the SpellPower system. Call from MegaMod constructor.
     */
    public static void init(IEventBus modEventBus) {
        // Register all spell school attributes, mechanics attributes, resistance attributes,
        // status effects, and potions via deferred registration
        registerAttributes();
        registerStatusEffects();
        registerPotionsInternal();

        ATTRIBUTES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);

        // Register attribute modification event to add attributes to LivingEntity
        modEventBus.addListener(SpellPowerMod::onAttributeModification);

        // Wire attribute migration on player login — NeoForge game event bus (not mod bus)
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent.class,
                event -> {
                    if (event.getEntity() instanceof ServerPlayer sp) {
                        migrateAttributes(sp);
                    }
                });
    }

    /**
     * Register all attributes via DeferredRegister.
     */
    public static void registerAttributes() {
        // Mechanics attributes (haste, crit chance, crit damage)
        for (var entry : SpellPowerMechanics.all.entrySet()) {
            var mechanic = entry.getValue();
            mechanic.deferredHolder = ATTRIBUTES.register(
                    mechanic.name,
                    () -> mechanic.attribute
            );
        }

        // Resistance attributes
        for (var resistance : SpellResistance.Attributes.all) {
            resistance.deferredHolder = ATTRIBUTES.register(
                    resistance.id.getPath(),
                    () -> resistance.attribute
            );
        }

        // Spell school attributes
        for (var school : SpellSchools.all()) {
            if (school.ownsAttribute()) {
                school.deferredHolder = ATTRIBUTES.register(
                        school.id.getPath(),
                        () -> school.getOwnedAttribute()
                );
            }
        }
    }

    /**
     * Register all status effects via DeferredRegister.
     */
    public static void registerStatusEffects() {
        var modifierId = Identifier.fromNamespaceAndPath(ID, "potion_effect");
        var bonus_per_stack = 0.1F;

        // School boost effects
        for (var school : SpellSchools.all()) {
            if (school.ownedBoostEffect != null) {
                // The attribute modifier will be added after registration resolves
                final float schoolBonus = bonus_per_stack;
                final SpellSchool capturedSchool = school;
                school.effectHolder = MOB_EFFECTS.register(
                        school.id.getPath(),
                        () -> {
                            // Add attribute modifier to effect
                            if (capturedSchool.deferredHolder != null) {
                                capturedSchool.ownedBoostEffect.addAttributeModifier(
                                        capturedSchool.deferredHolder,
                                        modifierId,
                                        schoolBonus,
                                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                            }
                            return capturedSchool.ownedBoostEffect;
                        }
                );
            }
        }

        // Mechanic boost effects
        var safeConfig = safeConfig();
        for (var entry : SpellPowerMechanics.all.entrySet()) {
            var secondary = entry.getValue();

            var effectConfig = safeConfig.secondary_effects.get(secondary.name);
            float effectBonus = bonus_per_stack;
            if (effectConfig != null) {
                effectBonus = effectConfig.bonus_per_stack;
            }

            final float capturedBonus = effectBonus;
            final SpellPowerMechanics.Entry capturedSecondary = secondary;
            secondary.effectHolder = MOB_EFFECTS.register(
                    secondary.name,
                    () -> {
                        if (capturedSecondary.deferredHolder != null) {
                            capturedSecondary.boostEffect.addAttributeModifier(
                                    capturedSecondary.deferredHolder,
                                    modifierId,
                                    capturedBonus,
                                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                        }
                        return capturedSecondary.boostEffect;
                    }
            );
        }
    }

    public static void registerPotionsInternal() {
        if (safeConfig().register_potions) {
            registerPotions();
        }
    }

    private static boolean potionsRegistered = false;
    public static void registerPotions() {
        if (potionsRegistered) {
            return;
        }
        potionsRegistered = true;

        for (var school : SpellSchools.all()) {
            if (school.archetype == SpellSchool.Archetype.MAGIC
                    && !school.id.getPath().contains("generic")) {
                if (school.effectHolder != null) {
                    final SpellSchool capturedSchool = school;
                    final String potionName = potionIdFrom(school.id).getPath();
                    POTIONS.register(
                            potionName,
                            () -> new Potion(potionName, new MobEffectInstance(capturedSchool.effectHolder, 3600))
                    );
                }
            }
        }
        for (var secondary : SpellPowerMechanics.all.entrySet()) {
            var mechanic = secondary.getValue();
            if (mechanic.effectHolder != null) {
                final SpellPowerMechanics.Entry capturedMechanic = mechanic;
                final String mechanicPotionName = potionIdFrom(mechanic.id).getPath();
                POTIONS.register(
                        mechanicPotionName,
                        () -> new Potion(mechanicPotionName, new MobEffectInstance(capturedMechanic.effectHolder, 3600))
                );
            }
        }
    }

    public static Identifier potionIdFrom(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getNamespace() + "." + id.getPath());
    }

    /**
     * Add spell power attributes to all living entities via NeoForge event.
     */
    private static void onAttributeModification(EntityAttributeModificationEvent event) {
        // Add to all living entity types
        for (var entityType : event.getTypes()) {
            // Mechanics attributes
            for (var entry : SpellPowerMechanics.all.entrySet()) {
                var secondary = entry.getValue();
                if (secondary.deferredHolder != null && !event.has(entityType, secondary.deferredHolder)) {
                    event.add(entityType, secondary.deferredHolder);
                }
            }
            // School attributes
            for (var school : SpellSchools.all()) {
                if (school.ownsAttribute() && school.deferredHolder != null) {
                    if (!event.has(entityType, school.deferredHolder)) {
                        event.add(entityType, school.deferredHolder);
                    }
                }
            }
            // Resistance attributes
            for (var resistance : SpellResistance.Attributes.all) {
                if (resistance.deferredHolder != null && !event.has(entityType, resistance.deferredHolder)) {
                    event.add(entityType, resistance.deferredHolder);
                }
            }
        }
    }

    @Deprecated(forRemoval = true)
    public static AttributesConfig.AttributeScope attributeScopeOverride = null;
    @Deprecated(forRemoval = true)
    public static AttributesConfig.AttributeScope attributeScope() {
        return attributeScopeOverride;
    }

    public static void migrateAttributes(ServerPlayer player) {
        var attributes = SpellSchools.all().stream()
                .filter(school -> school.isMagicArchetype() && school.ownsAttribute())
                .map(school -> school.deferredHolder)
                .toList();
        for (var attribute : attributes) {
            if (attribute == null) {
                continue;
            }
            var instance = player.getAttributes().getInstance(attribute);
            if (instance == null) {
                continue;
            }
            var defaultValue = attribute.value().getDefaultValue();
            if (instance.getBaseValue() != defaultValue) {
                instance.setBaseValue(defaultValue);
            }
        }
    }
}
