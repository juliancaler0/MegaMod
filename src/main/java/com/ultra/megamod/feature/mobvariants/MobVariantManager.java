package com.ultra.megamod.feature.mobvariants;

import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Random;

/**
 * Promotes hostile mobs to Elite (5%) or Champion (1%) variants on spawn.
 * Skips pocket dimensions (dungeons/museum). Uses entity PersistentData for tagging.
 */
@EventBusSubscriber(modid = "megamod")
public class MobVariantManager {

    private static final String TAG_VARIANT = "megamod_variant";
    private static final String TAG_MODIFIERS = "megamod_variant_modifiers";

    public static final String VARIANT_NONE = "";
    public static final String VARIANT_ELITE = "elite";
    public static final String VARIANT_CHAMPION = "champion";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        if (!FeatureToggleManager.get(level).isEnabled("mob_variants")) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof Monster mob)) return;

        // Skip if already tagged
        if (!mob.getPersistentData().getStringOr(TAG_VARIANT, "").isEmpty()) return;

        // Skip pocket dimensions (dungeon, museum)
        String dimPath = level.dimension().identifier().getPath();
        if (dimPath.contains("pocket") || dimPath.contains("dungeon") || dimPath.contains("museum")) return;

        Random random = new Random();
        double roll = random.nextDouble();

        if (roll < 0.01) {
            // Champion: 1%
            promoteToChampion(mob, level);
        } else if (roll < 0.06) {
            // Elite: 5% (total, minus champion chance)
            promoteToElite(mob, level);
        }
    }

    private static void promoteToElite(Mob mob, ServerLevel level) {
        mob.getPersistentData().putString(TAG_VARIANT, VARIANT_ELITE);

        // 2x HP
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            double baseHP = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * 2.0);
            mob.setHealth((float) (baseHP * 2.0));
        }
        // +2 armor
        if (mob.getAttribute(Attributes.ARMOR) != null) {
            double baseArmor = mob.getAttribute(Attributes.ARMOR).getBaseValue();
            mob.getAttribute(Attributes.ARMOR).setBaseValue(baseArmor + 2.0);
        }

        // Apply 1 random modifier
        MobVariantModifiers.Modifier mod = MobVariantModifiers.getRandomModifier(level.getRandom());
        mod.apply(mob);
        mob.getPersistentData().putString(TAG_MODIFIERS, mod.name());

        // Custom name
        String mobName = mob.getType().getDescription().getString();
        mob.setCustomName(Component.literal("[Elite] " + mobName).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
    }

    private static void promoteToChampion(Mob mob, ServerLevel level) {
        mob.getPersistentData().putString(TAG_VARIANT, VARIANT_CHAMPION);

        // 3x HP
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            double baseHP = mob.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * 3.0);
            mob.setHealth((float) (baseHP * 3.0));
        }
        // +5 armor
        if (mob.getAttribute(Attributes.ARMOR) != null) {
            double baseArmor = mob.getAttribute(Attributes.ARMOR).getBaseValue();
            mob.getAttribute(Attributes.ARMOR).setBaseValue(baseArmor + 5.0);
        }

        // Apply 2 random modifiers
        MobVariantModifiers.Modifier mod1 = MobVariantModifiers.getRandomModifier(level.getRandom());
        MobVariantModifiers.Modifier mod2 = MobVariantModifiers.getRandomModifier(level.getRandom());
        while (mod2 == mod1) mod2 = MobVariantModifiers.getRandomModifier(level.getRandom());
        mod1.apply(mob);
        mod2.apply(mob);
        mob.getPersistentData().putString(TAG_MODIFIERS, mod1.name() + "," + mod2.name());

        // Custom name + glowing
        String mobName = mob.getType().getDescription().getString();
        mob.setCustomName(Component.literal("[Champion] " + mobName).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
        mob.setGlowingTag(true);
        mob.setPersistenceRequired();
    }

    public static String getVariant(Entity entity) {
        return entity.getPersistentData().getStringOr(TAG_VARIANT, "");
    }

    public static String getModifiers(Entity entity) {
        return entity.getPersistentData().getStringOr(TAG_MODIFIERS, "");
    }
}
