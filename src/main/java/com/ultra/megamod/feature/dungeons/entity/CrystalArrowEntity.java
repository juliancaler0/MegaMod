package com.ultra.megamod.feature.dungeons.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrystalArrowEntity extends AbstractArrow {
    public CrystalArrowEntity(EntityType<? extends CrystalArrowEntity> type, Level world) {
        super(type, world);
        this.setBaseDamage(6.0); // base 2.0 + 4.0 bonus
    }

    public CrystalArrowEntity(Level world, LivingEntity shooter) {
        super(DungeonEntityRegistry.CRYSTAL_ARROW.get(), shooter, world,
                new ItemStack(DungeonEntityRegistry.CRYSTAL_ARROW_ITEM.get()), null);
        this.setBaseDamage(6.0);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(DungeonEntityRegistry.CRYSTAL_ARROW_ITEM.get());
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        // Apply Glowing for 100 ticks (5 seconds)
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, true));
    }
}
