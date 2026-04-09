package com.ultra.megamod.feature.citizen.raid;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Base class for all colony raider entities.
 * Provides culture, raidId tracking, NBT persistence, common AI setup, and culture-specific loot.
 */
public abstract class AbstractRaiderEntity extends Monster {

    private RaiderCulture culture = RaiderCulture.BARBARIAN;
    private int raidId = -1;
    private BlockPos targetPos = BlockPos.ZERO;

    protected AbstractRaiderEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    // --- Culture Setup ---

    public RaiderCulture getCulture() {
        return culture;
    }

    public void setCulture(RaiderCulture culture) {
        this.culture = culture;
    }

    public int getRaidId() {
        return raidId;
    }

    public void setRaidId(int raidId) {
        this.raidId = raidId;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public void setTargetPos(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    // --- Attributes ---

    public static AttributeSupplier.Builder createRaiderAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30);
    }

    // --- AI Goals ---

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RaiderNavigateAI(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, MCEntityCitizen.class, true));

        registerCombatGoals();
    }

    /**
     * Subclasses override this to add melee or ranged combat goals.
     */
    protected abstract void registerCombatGoals();

    // --- Equipment ---

    /**
     * Subclasses call this to equip weapons/armor in their constructor.
     */
    protected void equipItem(EquipmentSlot slot, ItemStack stack) {
        this.setItemSlot(slot, stack);
        this.setDropChance(slot, 0.05f);
    }

    // --- Loot ---

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        // Culture-specific loot drops
        if (this.getRandom().nextFloat() < 0.3f) {
            ItemStack loot = getCultureLoot();
            if (!loot.isEmpty()) {
                this.spawnAtLocation(level, loot);
            }
        }
    }

    private ItemStack getCultureLoot() {
        return switch (culture) {
            case BARBARIAN -> new ItemStack(Items.IRON_NUGGET, 2 + this.getRandom().nextInt(4));
            case PIRATE -> new ItemStack(Items.GOLD_NUGGET, 2 + this.getRandom().nextInt(5));
            case EGYPTIAN -> new ItemStack(Items.GOLD_INGOT, 1);
            case NORSEMEN -> new ItemStack(Items.IRON_INGOT, 1 + this.getRandom().nextInt(2));
            case AMAZON -> new ItemStack(Items.ARROW, 4 + this.getRandom().nextInt(8));
            case DROWNED_PIRATE -> new ItemStack(Items.PRISMARINE_SHARD, 1 + this.getRandom().nextInt(3));
        };
    }

    // --- NBT ---

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("RaiderCulture", culture.ordinal());
        output.putInt("RaidId", raidId);
        output.putInt("TargetX", targetPos.getX());
        output.putInt("TargetY", targetPos.getY());
        output.putInt("TargetZ", targetPos.getZ());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.culture = RaiderCulture.fromOrdinal(input.getIntOr("RaiderCulture", 0));
        this.raidId = input.getIntOr("RaidId", -1);
        int tx = input.getIntOr("TargetX", 0);
        int ty = input.getIntOr("TargetY", 0);
        int tz = input.getIntOr("TargetZ", 0);
        this.targetPos = new BlockPos(tx, ty, tz);
    }

    // --- Misc ---

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false; // Raiders persist during raids
    }

    @Override
    public void tick() {
        super.tick();
        // Tag for raid manager tracking
        if (!this.getTags().contains("colony_raider")) {
            this.addTag("colony_raider");
        }
    }

    /**
     * Apply the culture's difficulty multiplier to this raider's stats.
     */
    public void applyCultureScaling() {
        double mult = culture.getDifficultyMultiplier();
        var hp = this.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null) {
            hp.setBaseValue(hp.getBaseValue() * mult);
            this.setHealth(this.getMaxHealth());
        }
        var dmg = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmg != null) {
            dmg.setBaseValue(dmg.getBaseValue() * mult);
        }
    }

    /**
     * Whether this raider can breathe underwater (Drowned Pirates).
     */
    public boolean canBreatheUnderwater() {
        return culture == RaiderCulture.DROWNED_PIRATE;
    }

    @Override
    public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        if (culture == RaiderCulture.DROWNED_PIRATE) {
            return false;
        }
        return super.canDrownInFluidType(type);
    }
}
