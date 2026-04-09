package de.cadentem.pufferfish_unofficial_additions.events;

import de.cadentem.pufferfish_unofficial_additions.experience.FishingExperienceSource;
import de.cadentem.pufferfish_unofficial_additions.rewards.EffectReward;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.puffish.skillsmod.api.SkillsAPI;

@EventBusSubscriber
public class GameEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void grantFishingExperience(final ItemFishedEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (event.getDrops().isEmpty()) {
               SkillsAPI.updateExperienceSources(serverPlayer, FishingExperienceSource.class, source -> source.getValue(serverPlayer, serverPlayer.getMainHandItem(), ItemStack.EMPTY));
            } else {
                event.getDrops().forEach(drop -> SkillsAPI.updateExperienceSources(serverPlayer, FishingExperienceSource.class, source -> source.getValue(serverPlayer, serverPlayer.getMainHandItem(), drop)));
            }
        }
    }

    @SubscribeEvent
    public static void immuneEffects(final MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Holder<MobEffect> effect = event.getEffectInstance().getEffect();
            int amplifier = event.getEffectInstance().getAmplifier();

            if (EffectReward.isImmune(player.getUUID(), effect, amplifier)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    /** Avoid loss of effects on clear command */
    @SubscribeEvent
    public static void retainEffects(final MobEffectEvent.Remove event) {
        MobEffectInstance instance = event.getEffectInstance();

        if (event.getEntity() instanceof ServerPlayer player && instance != null && instance.isInfiniteDuration()) {
            Holder<MobEffect> effect = instance.getEffect();
            int amplifier = instance.getAmplifier();

            if (EffectReward.shouldRemove(player.getUUID(), effect, amplifier)) {
                event.setCanceled(true);
            }
        }
    }

    /** Avoid loss of effects on death / when changing dimensions */
    @SubscribeEvent
    public static void retainEffects(final EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EffectReward.applyEffects(player);
        }
    }

    @SubscribeEvent
    public static void clearData(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EffectReward.clearData(player.getUUID());
        }
    }
}
