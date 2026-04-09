package com.ultra.megamod.feature.backpacks;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Registry of variant-specific passive abilities that activate when a backpack is worn.
 * Each variant maps to a BiConsumer that applies effects/modifiers to the wearing player.
 * Called every 40 ticks to avoid performance issues.
 */
public class BackpackAbilities {

    private static final Map<String, BiConsumer<ServerPlayer, ServerLevel>> ABILITIES = new HashMap<>();

    // Unique modifier IDs for attribute-based abilities
    private static final Identifier IRON_ARMOR_ID = Identifier.parse("megamod:backpack_iron_armor");
    private static final Identifier DIAMOND_ARMOR_ID = Identifier.parse("megamod:backpack_diamond_armor");
    private static final Identifier NETHERITE_ARMOR_ID = Identifier.parse("megamod:backpack_netherite_armor");

    // Standard effect duration (reapplied every 40 ticks, so 100 ticks gives comfortable overlap)
    private static final int DURATION = 100;

    static {
        // =====================
        // Material variants
        // =====================

        // STANDARD: no ability
        ABILITIES.put("STANDARD", (player, level) -> {});

        // IRON: +2 armor (attribute modifier)
        ABILITIES.put("IRON", (player, level) -> {
            applyArmorModifier(player, IRON_ARMOR_ID, 2.0);
        });

        // GOLD: Luck
        ABILITIES.put("GOLD", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, DURATION, 0, true, false));
        });

        // DIAMOND: +4 armor (attribute modifier)
        ABILITIES.put("DIAMOND", (player, level) -> {
            applyArmorModifier(player, DIAMOND_ARMOR_ID, 4.0);
        });

        // NETHERITE: +4 armor + fire resistance
        ABILITIES.put("NETHERITE", (player, level) -> {
            applyArmorModifier(player, NETHERITE_ARMOR_ID, 4.0);
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, true, false));
        });

        // EMERALD: Luck (amplifier 1)
        ABILITIES.put("EMERALD", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, DURATION, 1, true, false));
        });

        // LAPIS: +50% XP (Hero of the Village as proxy)
        ABILITIES.put("LAPIS", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, DURATION, 0, true, false));
        });

        // REDSTONE: Haste
        ABILITIES.put("REDSTONE", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, DURATION, 0, true, false));
        });

        // COAL: Night Vision in caves (Y < 50 only)
        ABILITIES.put("COAL", (player, level) -> {
            if (player.getY() < 50) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, true, false));
            }
        });

        // QUARTZ: Strength (amplifier 0)
        ABILITIES.put("QUARTZ", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, DURATION, 0, true, false));
        });

        // BOOKSHELF: no ability (enchanting bonus handled elsewhere)
        ABILITIES.put("BOOKSHELF", (player, level) -> {});

        // SANDSTONE: Haste in desert biome
        ABILITIES.put("SANDSTONE", (player, level) -> {
            if (isInDesertBiome(level, player)) {
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, DURATION, 0, true, false));
            }
        });

        // SNOW: Slow Falling in cold biomes
        ABILITIES.put("SNOW", (player, level) -> {
            if (isInColdBiome(level, player)) {
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, DURATION, 0, true, false));
            }
        });

        // SPONGE: Water Breathing
        ABILITIES.put("SPONGE", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, DURATION, 0, true, false));
        });

        // CAKE: Saturation every 200 ticks
        ABILITIES.put("CAKE", (player, level) -> {
            if (player.tickCount % 200 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false));
            }
        });

        // CACTUS: no ability defined in spec, register as empty
        ABILITIES.put("CACTUS", (player, level) -> {});

        // HAY: no ability defined in spec, register as empty
        ABILITIES.put("HAY", (player, level) -> {});

        // MELON: no ability defined in spec, register as empty
        ABILITIES.put("MELON", (player, level) -> {});

        // PUMPKIN: no ability defined in spec, register as empty
        ABILITIES.put("PUMPKIN", (player, level) -> {});

        // =====================
        // Mob variants
        // =====================

        // CREEPER: no passive (explosion on death handled by event)
        ABILITIES.put("CREEPER", (player, level) -> {});

        // DRAGON: Fire Resistance
        ABILITIES.put("DRAGON", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, true, false));
        });

        // ENDERMAN: Night Vision
        ABILITIES.put("ENDERMAN", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, true, false));
        });

        // BLAZE: Fire Resistance
        ABILITIES.put("BLAZE", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, true, false));
        });

        // GHAST: Slow Falling
        ABILITIES.put("GHAST", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, DURATION, 0, true, false));
        });

        // MAGMA_CUBE: Fire Resistance (in Nether only)
        ABILITIES.put("MAGMA_CUBE", (player, level) -> {
            if (level.dimension() == Level.NETHER) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION, 0, true, false));
            }
        });

        // SKELETON: no passive
        ABILITIES.put("SKELETON", (player, level) -> {});

        // SPIDER: Slow Falling (no fall damage proxy)
        ABILITIES.put("SPIDER", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, DURATION, 0, true, false));
        });

        // WITHER: Resistance (amplifier 0) as wither immunity proxy
        ABILITIES.put("WITHER", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURATION, 0, true, false));
        });

        // WARDEN: Strength + Night Vision
        ABILITIES.put("WARDEN", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, DURATION, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, true, false));
        });

        // BAT: Slow Falling + Night Vision
        ABILITIES.put("BAT", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, DURATION, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION, 0, true, false));
        });

        // BEE: Saturation near flowers (apply every 200 ticks)
        ABILITIES.put("BEE", (player, level) -> {
            if (player.tickCount % 200 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false));
            }
        });

        // WOLF: Strength
        ABILITIES.put("WOLF", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, DURATION, 0, true, false));
        });

        // FOX: Speed at night (check daylight)
        ABILITIES.put("FOX", (player, level) -> {
            if (isNightTime(level)) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, DURATION, 0, true, false));
            }
        });

        // OCELOT: Speed
        ABILITIES.put("OCELOT", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, DURATION, 0, true, false));
        });

        // HORSE: Speed (amplifier 1)
        ABILITIES.put("HORSE", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, DURATION, 1, true, false));
        });

        // COW: Saturation every 400 ticks
        ABILITIES.put("COW", (player, level) -> {
            if (player.tickCount % 400 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false));
            }
        });

        // PIG: Saturation every 400 ticks
        ABILITIES.put("PIG", (player, level) -> {
            if (player.tickCount % 400 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, true, false));
            }
        });

        // SHEEP: Regeneration (amplifier 0)
        ABILITIES.put("SHEEP", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, DURATION, 0, true, false));
        });

        // CHICKEN: Slow Falling
        ABILITIES.put("CHICKEN", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, DURATION, 0, true, false));
        });

        // SQUID: Water Breathing + Dolphin's Grace
        ABILITIES.put("SQUID", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, DURATION, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, DURATION, 0, true, false));
        });

        // VILLAGER: Luck (amplifier 1) + Hero of the Village
        ABILITIES.put("VILLAGER", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, DURATION, 1, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, DURATION, 0, true, false));
        });

        // IRON_GOLEM: Resistance + Strength
        ABILITIES.put("IRON_GOLEM", (player, level) -> {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, DURATION, 0, true, false));
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, DURATION, 0, true, false));
        });
    }

    /**
     * Tick the passive ability for the given backpack variant.
     * Should be called from the server-side backpack tick handler.
     * Internally gates on tickCount % 40 to avoid running every tick.
     *
     * @param player      the player wearing the backpack
     * @param variantName the BackpackVariant name (e.g. "IRON", "DRAGON")
     * @param level       the server level
     */
    public static void tickAbility(ServerPlayer player, String variantName, ServerLevel level) {
        if (player.tickCount % 40 != 0) return;

        BiConsumer<ServerPlayer, ServerLevel> ability = ABILITIES.get(variantName.toUpperCase());
        if (ability != null) {
            ability.accept(player, level);
        }
    }

    /**
     * Remove attribute-based modifiers when a backpack is unequipped.
     * Call this when the backpack is removed to clean up transient modifiers.
     *
     * @param player the player who unequipped the backpack
     */
    public static void removeAttributeModifiers(ServerPlayer player) {
        removeArmorModifier(player, IRON_ARMOR_ID);
        removeArmorModifier(player, DIAMOND_ARMOR_ID);
        removeArmorModifier(player, NETHERITE_ARMOR_ID);
    }

    // --- Internal helpers ---

    private static void applyArmorModifier(ServerPlayer player, Identifier modifierId, double amount) {
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        // Only add if not already present
        if (armorAttr.getModifier(modifierId) == null) {
            armorAttr.addTransientModifier(
                    new AttributeModifier(modifierId, amount, AttributeModifier.Operation.ADD_VALUE)
            );
        }
    }

    private static void removeArmorModifier(ServerPlayer player, Identifier modifierId) {
        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        if (armorAttr.getModifier(modifierId) != null) {
            armorAttr.removeModifier(modifierId);
        }
    }

    /**
     * Check if the player is in a desert-like biome by examining the biome name.
     */
    private static boolean isInDesertBiome(ServerLevel level, ServerPlayer player) {
        Holder<Biome> biomeHolder = level.getBiome(player.blockPosition());
        String biomeName = biomeHolder.unwrapKey()
                .map(key -> key.identifier().getPath().toLowerCase())
                .orElse("");
        return biomeName.contains("desert") || biomeName.contains("badlands") || biomeName.contains("mesa");
    }

    /**
     * Check if the player is in a cold biome by examining the biome temperature.
     */
    private static boolean isInColdBiome(ServerLevel level, ServerPlayer player) {
        Holder<Biome> biomeHolder = level.getBiome(player.blockPosition());
        float temperature = biomeHolder.value().getBaseTemperature();
        return temperature < 0.2f;
    }

    /**
     * Check if it is currently nighttime (between dusk and dawn).
     * Day ticks: 0-12000 is daytime, 12000-24000 is nighttime.
     */
    private static boolean isNightTime(ServerLevel level) {
        long dayTime = level.getDayTime() % 24000L;
        return dayTime >= 13000 && dayTime <= 23000;
    }
}
