package com.ultra.megamod.lib.skilltree.items;

import com.ultra.megamod.lib.skilltree.utils.SkillHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.ultra.megamod.lib.spellengine.api.spell.fx.ParticleBatch;
import com.ultra.megamod.lib.spellengine.client.util.Color;
import com.ultra.megamod.lib.spellengine.fx.ParticleHelper;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineParticles;

public class RespecItem extends Item {
    public RespecItem(Properties settings) {
        super(settings);
    }

    public SoundEvent getBreakSound() {
        return SoundEvents.AMETHYST_CLUSTER_BREAK;
    }

    public static final ParticleBatch[] RESET_PARTICLES = new ParticleBatch[] {
        new ParticleBatch(
                SpellEngineParticles.MagicParticles.get(
                        SpellEngineParticles.MagicParticles.Shape.SPARK,
                        SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                30, 0.2F, 0.25F)
                .color(Color.from(0x8000ff).toRGBA()),
            new ParticleBatch(
                    SpellEngineParticles.MagicParticles.get(
                            SpellEngineParticles.MagicParticles.Shape.SPARK,
                            SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                    ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.CENTER,
                    30, 0.2F, 0.25F)
                    .color(Color.from(0x8000ff).toRGBA())
                    .invert()
    };

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if ((user instanceof ServerPlayer serverUser)) {
            if (SkillHelper.respec(serverUser)) {
                user.awardStat(Stats.ITEM_USED.get(this));
                var equipmentSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                itemStack.hurtAndBreak(1, serverUser, equipmentSlot);
                ParticleHelper.sendBatches(user, RESET_PARTICLES);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
