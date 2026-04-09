package com.ultra.megamod.feature.dungeons.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CeruleanArrowEntity extends AbstractArrow {
    public CeruleanArrowEntity(EntityType<? extends CeruleanArrowEntity> type, Level world) {
        super(type, world);
    }

    public CeruleanArrowEntity(Level world, LivingEntity shooter) {
        super(DungeonEntityRegistry.CERULEAN_ARROW.get(), shooter, world,
                new ItemStack(DungeonEntityRegistry.CERULEAN_ARROW_ITEM.get()), null);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(DungeonEntityRegistry.CERULEAN_ARROW_ITEM.get());
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        // Apply Slowness II for 60 ticks (3 seconds)
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1, false, true));
    }
}
