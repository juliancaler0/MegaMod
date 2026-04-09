package com.ultra.megamod.feature.dungeons.entity;

import com.ultra.megamod.feature.dungeons.DungeonTheme;
import com.ultra.megamod.feature.dungeons.DungeonTier;
import com.ultra.megamod.feature.dungeons.loot.DungeonExclusiveItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DungeonSlimeEntity extends Slime {
    private float difficultyMultiplier = 1.0f;
    private int tierLevel = 1;
    private int themeColor = 0; // 0=green, 1=blue, 2=purple

    public DungeonSlimeEntity(EntityType<? extends Slime> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createDungeonSlimeAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 16.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    public int getThemeColor() {
        return this.themeColor;
    }

    public void setThemeColor(int color) {
        this.themeColor = color;
    }

    public void applyDungeonScaling(DungeonTier tier) {
        this.tierLevel = tier.getLevel();
        this.difficultyMultiplier = tier.getDifficultyMultiplier();
        // Size 1 for tight hitbox — all scaling via HP/damage, not physical size
        this.setSize(1, true);
        // Scale HP and damage with tier instead of size
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            double baseHP = this.getAttribute(Attributes.MAX_HEALTH).getBaseValue();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHP * this.difficultyMultiplier);
            this.setHealth((float) (baseHP * this.difficultyMultiplier));
        }
        if (this.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            double baseDmg = this.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
            float damageMult = 1.0f + (this.difficultyMultiplier - 1.0f) * 0.5f;
            this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * damageMult);
        }
        tier.applyMobEffects(this);
    }

    public void applyThemeColor(DungeonTheme theme) {
        switch (theme) {
            case ICE_CAVERN -> this.themeColor = 1;    // blue
            case VOID_CASTLE -> this.themeColor = 2;    // purple
            default -> this.themeColor = 0;              // green
        }
    }

    public static DungeonSlimeEntity create(ServerLevel level, DungeonTier tier, DungeonTheme theme, BlockPos pos) {
        DungeonSlimeEntity slime = new DungeonSlimeEntity(DungeonEntityRegistry.DUNGEON_SLIME.get(), (Level) level);
        slime.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        slime.applyDungeonScaling(tier);
        slime.applyThemeColor(theme);
        level.addFreshEntity((Entity) slime);
        return slime;
    }

    public static DungeonSlimeEntity create(ServerLevel level, DungeonTier tier, BlockPos pos) {
        DungeonSlimeEntity slime = new DungeonSlimeEntity(DungeonEntityRegistry.DUNGEON_SLIME.get(), (Level) level);
        slime.setPos((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5);
        slime.applyDungeonScaling(tier);
        level.addFreshEntity((Entity) slime);
        return slime;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.getSize() > 1) {
            // Only large slimes drop extra items
            this.spawnAtLocation(level, new ItemStack(Items.SLIME_BALL, 1 + this.getRandom().nextInt(2)));
            if (this.getRandom().nextFloat() < 0.15f) {
                this.spawnAtLocation(level, new ItemStack(DungeonExclusiveItems.VOID_SHARD.get()));
            }
        }
    }

    /**
     * Override vanilla slime push — vanilla slimes push players in a huge radius
     * based on size. We reduce this to only push on actual contact.
     */
    @Override
    public void playerTouch(net.minecraft.world.entity.player.Player player) {
        if (this.level().isClientSide()) return;
        if (this.isAlive()) {
            // Only damage/push if player is actually within the slime's bounding box (inflated slightly)
            if (this.getBoundingBox().inflate(0.2).intersects(player.getBoundingBox())) {
                if (this.doHurtTarget((ServerLevel) this.level(), player)) {
                    this.playSound(this.getHurtSound(this.damageSources().generic()), 1.0f, 1.0f);
                }
            }
        }
    }

    /**
     * Prevent vanilla slime from splitting into smaller slimes on death.
     * Dungeon slimes should just die and drop loot.
     */
    @Override
    public void remove(Entity.RemovalReason reason) {
        // Skip the vanilla Slime.remove() which spawns smaller slimes
        // Call Entity.remove() directly
        this.setRemoved(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("DifficultyMult", this.difficultyMultiplier);
        output.putInt("TierLevel", this.tierLevel);
        output.putInt("ThemeColor", this.themeColor);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.difficultyMultiplier = input.getFloatOr("DifficultyMult", 1.0f);
        this.tierLevel = input.getIntOr("TierLevel", 1);
        this.themeColor = input.getIntOr("ThemeColor", 0);
        DungeonTier.fromLevel(this.tierLevel).applyMobEffects(this);
    }
}
