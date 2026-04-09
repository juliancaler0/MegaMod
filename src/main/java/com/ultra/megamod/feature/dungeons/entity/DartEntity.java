package com.ultra.megamod.feature.dungeons.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class DartEntity extends AbstractArrow {
    public static final ItemStack PROJECTILE_ITEM = new ItemStack(Blocks.AIR);

    public DartEntity(EntityType<? extends DartEntity> type, Level world) {
        super(type, world);
    }

    public DartEntity(EntityType<? extends DartEntity> type, double x, double y, double z, Level world) {
        super(type, x, y, z, world, PROJECTILE_ITEM.copy(), null);
    }

    public DartEntity(EntityType<? extends DartEntity> type, LivingEntity entity, Level world) {
        super(type, entity, world, PROJECTILE_ITEM.copy(), null);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return PROJECTILE_ITEM;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity entity) {
        super.doPostHurtEffects(entity);
        entity.setArrowCount(entity.getArrowCount() - 1);

        // Apply a random potion effect
        RandomSource random = entity.getRandom();
        int roll = random.nextInt(5);
        switch (roll) {
            case 0 -> entity.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, false, true));
            case 1 -> entity.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 60, 0, false, true));
            case 2 -> entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
            case 3 -> entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
            case 4 -> entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0, false, true));
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isInGround()) {
            this.discard();
        }
    }

    public static DartEntity shoot(Level world, LivingEntity entity, RandomSource source) {
        return shoot(world, entity, source, 1f, 1, 1);
    }

    public static DartEntity shoot(Level world, LivingEntity entity, RandomSource source, float pullingPower) {
        return shoot(world, entity, source, pullingPower * 1f, 1, 1);
    }

    public static DartEntity shoot(Level world, LivingEntity entity, RandomSource random, float power, double damage, int knockback) {
        DartEntity dart = new DartEntity(DungeonEntityRegistry.DART.get(), entity, world);
        dart.shoot(entity.getViewVector(1).x, entity.getViewVector(1).y, entity.getViewVector(1).z, power * 2, 0);
        dart.setSilent(true);
        dart.setCritArrow(false);
        dart.setBaseDamage(damage);
        world.addFreshEntity(dart);
        world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                BuiltInRegistries.SOUND_EVENT.getValue(Identifier.withDefaultNamespace("entity.arrow.shoot")), SoundSource.PLAYERS, 1,
                1f / (random.nextFloat() * 0.5f + 1) + (power / 2));
        return dart;
    }

    public static DartEntity shoot(LivingEntity entity, LivingEntity target) {
        DartEntity dart = new DartEntity(DungeonEntityRegistry.DART.get(), entity, entity.level());
        double dx = target.getX() - entity.getX();
        double dy = target.getY() + target.getEyeHeight() - 1.1;
        double dz = target.getZ() - entity.getZ();
        dart.shoot(dx, dy - dart.getY() + Math.hypot(dx, dz) * 0.2F, dz, 1f * 2, 12.0F);
        dart.setSilent(true);
        dart.setBaseDamage(1);
        dart.setCritArrow(false);
        entity.level().addFreshEntity(dart);
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                BuiltInRegistries.SOUND_EVENT.getValue(Identifier.withDefaultNamespace("entity.arrow.shoot")), SoundSource.PLAYERS, 1,
                1f / (RandomSource.create().nextFloat() * 0.5f + 1));
        return dart;
    }
}
