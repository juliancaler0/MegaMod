package com.ultra.megamod.feature.combat.spell.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultra.megamod.feature.backpacks.client.BackpackRenderContext;
import com.ultra.megamod.feature.combat.spell.SpellEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nonnull;

/**
 * Render layer that spawns visual particle effects around entities with active spell effects.
 * <p>
 * Instead of model overlays (which require complex render type wrangling in 1.21.11),
 * this spawns thematic particles around affected entities every few frames for clear
 * visual feedback of active buffs/debuffs.
 */
public class SpellEffectLayerRenderer<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
        extends RenderLayer<S, M> {

    public SpellEffectLayerRenderer(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @Override
    public void submit(@Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector nodeCollector,
                       int packedLight, @Nonnull S renderState, float vertRot, float horizRot) {

        int entityId = BackpackRenderContext.getEntityId();
        if (entityId < 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        net.minecraft.world.entity.Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity living)) return;

        // Only spawn particles every ~5 frames to avoid spam
        if (living.tickCount % 5 != 0) return;

        double x = living.getX(), y = living.getY(), z = living.getZ();

        // Frozen — ice crystals around entity
        if (living.hasEffect(SpellEffects.FROZEN)) {
            for (int i = 0; i < 3; i++) {
                double angle = living.getRandom().nextDouble() * Math.PI * 2;
                mc.level.addParticle(ParticleTypes.SNOWFLAKE,
                        x + Math.cos(angle) * 0.5, y + 0.5 + living.getRandom().nextDouble() * 1.5,
                        z + Math.sin(angle) * 0.5, 0, -0.02, 0);
            }
        }

        // Frost Shield — swirling cyan snowflakes
        if (living.hasEffect(SpellEffects.FROST_SHIELD)) {
            double angle = (living.tickCount * 0.15) % (Math.PI * 2);
            mc.level.addParticle(ParticleTypes.SNOWFLAKE,
                    x + Math.cos(angle) * 0.7, y + 1.2,
                    z + Math.sin(angle) * 0.7, 0, 0.01, 0);
        }

        // Divine Protection — golden sparks orbiting
        if (living.hasEffect(SpellEffects.DIVINE_PROTECTION)) {
            double angle = (living.tickCount * 0.1) % (Math.PI * 2);
            mc.level.addParticle(ParticleTypes.END_ROD,
                    x + Math.cos(angle) * 0.6, y + 1.0,
                    z + Math.sin(angle) * 0.6, 0, 0.02, 0);
        }

        // Arcane Charge — purple enchant particles intensifying with stacks
        if (living.hasEffect(SpellEffects.ARCANE_CHARGE)) {
            int count = 1;
            var eff = living.getEffect(SpellEffects.ARCANE_CHARGE);
            if (eff != null) count = eff.getAmplifier() + 1;
            for (int i = 0; i < count; i++) {
                mc.level.addParticle(ParticleTypes.ENCHANT,
                        x + (living.getRandom().nextDouble() - 0.5) * 0.8, y + 1.0 + living.getRandom().nextDouble(),
                        z + (living.getRandom().nextDouble() - 0.5) * 0.8,
                        0, 0.1 + living.getRandom().nextDouble() * 0.2, 0);
            }
        }

        // Hunter's Mark — red glowing crit particles
        if (living.hasEffect(SpellEffects.HUNTERS_MARK)) {
            mc.level.addParticle(ParticleTypes.CRIT,
                    x + (living.getRandom().nextDouble() - 0.5) * 0.6, y + 1.8,
                    z + (living.getRandom().nextDouble() - 0.5) * 0.6, 0, -0.05, 0);
        }

        // Entangling Roots — green particles at feet
        if (living.hasEffect(SpellEffects.ENTANGLING_ROOTS)) {
            for (int i = 0; i < 2; i++) {
                mc.level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                        x + (living.getRandom().nextDouble() - 0.5) * 0.6, y + 0.1 + living.getRandom().nextDouble() * 0.3,
                        z + (living.getRandom().nextDouble() - 0.5) * 0.6, 0, 0.02, 0);
            }
        }

        // Battle Banner — subtle upward golden particles
        if (living.hasEffect(SpellEffects.BATTLE_BANNER)) {
            if (living.tickCount % 10 == 0) {
                mc.level.addParticle(ParticleTypes.WAX_ON,
                        x + (living.getRandom().nextDouble() - 0.5) * 0.4, y + 0.5,
                        z + (living.getRandom().nextDouble() - 0.5) * 0.4, 0, 0.05, 0);
            }
        }

        // Slice and Dice — sweep attack particles around hands
        if (living.hasEffect(SpellEffects.SLICE_AND_DICE)) {
            mc.level.addParticle(ParticleTypes.SWEEP_ATTACK,
                    x + (living.getRandom().nextDouble() - 0.5), y + 1.0,
                    z + (living.getRandom().nextDouble() - 0.5), 0, 0, 0);
        }
    }

    /**
     * Register this layer on all player renderers.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onAddLayers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers event) {
        for (net.minecraft.world.entity.player.PlayerModelType skin : event.getSkins()) {
            var renderer = event.getPlayerRenderer(skin);
            if (renderer != null) {
                renderer.addLayer(new SpellEffectLayerRenderer(renderer));
            }
        }
    }
}
