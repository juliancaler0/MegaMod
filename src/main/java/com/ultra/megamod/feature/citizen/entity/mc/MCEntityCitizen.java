package com.ultra.megamod.feature.citizen.entity.mc;

import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.entity.mc.handlers.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * MineColonies-ported citizen entity for MegaMod.
 * <p>
 * Uses handler composition pattern: instead of inheritance per job type,
 * a single entity class delegates to pluggable handlers for colony, job,
 * experience, inventory, sleep, food, disease, happiness, and mourning.
 * <p>
 * This runs alongside the old MegaMod citizen entities; references will
 * be switched over after testing.
 */
public class MCEntityCitizen extends PathfinderMob implements Npc {

    // ---- Entity AI Tick Rate ----
    public static final int ENTITY_AI_TICKRATE = 5;

    // ---- Synched Data ----
    public static final EntityDataAccessor<Integer> DATA_LEVEL =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_TEXTURE =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_IS_FEMALE =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_COLONY_ID =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> DATA_CITIZEN_ID =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> DATA_RENDER_METADATA =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> DATA_IS_ASLEEP =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DATA_IS_CHILD =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<BlockPos> DATA_BED_POS =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.BLOCK_POS);
    public static final EntityDataAccessor<String> DATA_STYLE =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> DATA_TEXTURE_SUFFIX =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> DATA_JOB =
            SynchedEntityData.defineId(MCEntityCitizen.class, EntityDataSerializers.STRING);

    // ---- Constants ----
    private static final double BASE_MAX_HEALTH = 20.0D;
    private static final double BASE_MOVEMENT_SPEED = 0.3D;
    private static final double BASE_PATHFINDING_RANGE = 64.0D;
    private static final double CITIZEN_SWIM_BONUS = 2.0;
    private static final double MAX_SPEED_FACTOR = 0.5;
    private static final int COLL_THRESHOLD = 50;
    private static final int TICKS_20 = 20;
    private static final int TICKS_SECOND = 20;
    private static final float GUARD_BLOCK_DAMAGE = 0.5f;
    private static final int CALL_HELP_CD = 100;
    private static final int CITIZEN_INVENTORY_SIZE = 27;

    // ---- NBT Tags ----
    private static final String TAG_COLONY_ID = "ColonyId";
    private static final String TAG_CITIZEN_ID = "CitizenId";
    private static final String TAG_CITIZEN_NAME = "CitizenName";
    private static final String TAG_IS_FEMALE = "IsFemale";
    private static final String TAG_TEXTURE_ID = "TextureId";
    private static final String TAG_JOB = "Job";
    private static final String TAG_JOB_AI_DATA = "JobAIData";
    private static final String TAG_IS_CHILD = "IsChild";
    private static final String TAG_XP = "TotalXp";
    private static final String TAG_FOOD = "FoodData";
    private static final String TAG_DISEASE = "DiseaseData";
    private static final String TAG_HAPPINESS = "HappinessData";
    private static final String TAG_MOURN = "MournData";
    private static final String TAG_OWNER_UUID = "OwnerUUID";
    private static final String TAG_HUNGER = "Hunger";
    private static final String TAG_START_POS = "StartPos";
    private static final String TAG_UPKEEP_CHEST_POS = "UpkeepChestPos";
    private static final String TAG_BUILD_ORDER_ID = "BuildOrderId";
    private static final String TAG_BUILD_PROGRESS = "BuildProgress";
    private static final String TAG_BUILD_STATE = "BuildState";

    // ---- Instance Fields ----
    private int citizenId = 0;
    private String citizenName = "Citizen";
    private boolean female = false;
    private boolean child = false;
    private int textureId = 0;
    private String renderMetadata = "";
    private int collisionCounter = 0;
    private boolean isEquipmentDirty = true;
    private boolean textureDirty = true;
    private long nextPlayerCollisionTime = 0;
    private int callForHelpCooldown = 0;
    private int interactionCooldown = 0;
    private double totalXp = 0;
    private int lastHurtByPlayerTick = 0;

    // ---- Owner/Colony bridge fields ----
    @Nullable
    private java.util.UUID ownerUUID;
    private int hunger = 20;
    private BlockPos startPos = BlockPos.ZERO;
    private BlockPos upkeepChestPos = BlockPos.ZERO;
    private String buildOrderId = "";
    private int buildProgress = 0;
    private String buildState = "IDLE";

    /**
     * Citizen inventory, independent of any armor/hand slots.
     */
    private final SimpleContainer citizenInventory = new SimpleContainer(CITIZEN_INVENTORY_SIZE);

    // ---- Handlers (MineColonies composition pattern) ----
    private ICitizenColonyHandler citizenColonyHandler;
    private ICitizenExperienceHandler citizenExperienceHandler;
    private ICitizenInventoryHandler citizenInventoryHandler;
    private ICitizenJobHandler citizenJobHandler;
    private ICitizenSleepHandler citizenSleepHandler;
    private ICitizenFoodHandler citizenFoodHandler;
    private ICitizenDiseaseHandler citizenDiseaseHandler;
    private ICitizenHappinessHandler citizenHappinessHandler;
    private ICitizenMournHandler citizenMournHandler;

    // ---- Texture (client-side only) ----
    @Nullable
    private Identifier texture;

    // ==================== Constructor ====================

    public MCEntityCitizen(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        this.citizenColonyHandler = new CitizenColonyHandler(this);
        this.citizenExperienceHandler = new CitizenExperienceHandler(this);
        this.citizenInventoryHandler = new CitizenInventoryHandler(this);
        this.citizenJobHandler = new CitizenJobHandler(this);
        this.citizenSleepHandler = new CitizenSleepHandler(this);
        this.citizenFoodHandler = new CitizenFoodHandler();
        this.citizenDiseaseHandler = new CitizenDiseaseHandler();
        this.citizenHappinessHandler = new CitizenHappinessHandler();
        this.citizenMournHandler = new CitizenMournHandler();

        this.setPersistenceRequired();
        this.setCustomNameVisible(true);
    }

    // ==================== Attributes ====================

    public static AttributeSupplier.Builder createCitizenAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, BASE_PATHFINDING_RANGE)
                .add(Attributes.ATTACK_DAMAGE, 1.0D);
    }

    // ==================== Goals ====================

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    // ==================== Synched Data ====================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_LEVEL, 0);
        builder.define(DATA_TEXTURE, 0);
        builder.define(DATA_IS_FEMALE, 0);
        builder.define(DATA_COLONY_ID, 0);
        builder.define(DATA_CITIZEN_ID, 0);
        builder.define(DATA_RENDER_METADATA, "");
        builder.define(DATA_IS_ASLEEP, false);
        builder.define(DATA_IS_CHILD, false);
        builder.define(DATA_BED_POS, BlockPos.ZERO);
        builder.define(DATA_STYLE, "default");
        builder.define(DATA_TEXTURE_SUFFIX, "_a");
        builder.define(DATA_JOB, "");
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
        super.onSyncedDataUpdated(dataAccessor);
        if (citizenColonyHandler != null) {
            citizenColonyHandler.onSyncDataUpdate(dataAccessor);
        }
    }

    // ==================== Save/Load ====================

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(TAG_COLONY_ID, citizenColonyHandler.getColonyId());
        output.putInt(TAG_CITIZEN_ID, citizenId);
        output.putString(TAG_CITIZEN_NAME, citizenName);
        output.putBoolean(TAG_IS_FEMALE, female);
        output.putInt(TAG_TEXTURE_ID, textureId);
        output.putString("TextureSuffix", entityData.get(DATA_TEXTURE_SUFFIX));
        output.putBoolean(TAG_IS_CHILD, child);
        output.putFloat(TAG_XP, (float) totalXp);

        // Save owner/bridge fields
        if (ownerUUID != null) {
            output.putString(TAG_OWNER_UUID, ownerUUID.toString());
        }
        output.putInt(TAG_HUNGER, hunger);
        output.putInt(TAG_START_POS + "X", startPos.getX());
        output.putInt(TAG_START_POS + "Y", startPos.getY());
        output.putInt(TAG_START_POS + "Z", startPos.getZ());
        output.putInt(TAG_UPKEEP_CHEST_POS + "X", upkeepChestPos.getX());
        output.putInt(TAG_UPKEEP_CHEST_POS + "Y", upkeepChestPos.getY());
        output.putInt(TAG_UPKEEP_CHEST_POS + "Z", upkeepChestPos.getZ());
        output.putString(TAG_BUILD_ORDER_ID, buildOrderId);
        output.putInt(TAG_BUILD_PROGRESS, buildProgress);
        output.putString(TAG_BUILD_STATE, buildState);

        CitizenJob job = citizenJobHandler.getColonyJob();
        if (job != null) {
            output.putString(TAG_JOB, job.name());
        }

        // Save job AI data if present
        if (citizenJobHandler instanceof CitizenJobHandler jobHandler) {
            com.ultra.megamod.feature.citizen.job.IJob jobInstance = jobHandler.getJobInstance();
            if (jobInstance != null) {
                CompoundTag jobAiTag = new CompoundTag();
                jobInstance.saveToNBT(jobAiTag);
                output.store(TAG_JOB_AI_DATA, CompoundTag.CODEC, jobAiTag);
            }
        }

        // Save handler data via CompoundTag (handlers use CompoundTag internally)
        CompoundTag foodTag = new CompoundTag();
        citizenFoodHandler.write(foodTag);
        output.store(TAG_FOOD, CompoundTag.CODEC, foodTag);

        CompoundTag diseaseTag = new CompoundTag();
        citizenDiseaseHandler.write(diseaseTag);
        output.store(TAG_DISEASE, CompoundTag.CODEC, diseaseTag);

        CompoundTag happinessTag = new CompoundTag();
        citizenHappinessHandler.write(happinessTag);
        output.store(TAG_HAPPINESS, CompoundTag.CODEC, happinessTag);

        CompoundTag mournTag = new CompoundTag();
        citizenMournHandler.write(mournTag);
        output.store(TAG_MOURN, CompoundTag.CODEC, mournTag);

        // Save inventory
        ValueOutput.ValueOutputList inventoryList = output.childrenList("Inventory");
        for (int i = 0; i < citizenInventory.getContainerSize(); i++) {
            ItemStack stack = citizenInventory.getItem(i);
            if (!stack.isEmpty()) {
                ValueOutput slotOutput = inventoryList.addChild();
                slotOutput.putInt("Slot", i);
                slotOutput.store("Item", ItemStack.CODEC, stack);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        citizenColonyHandler.setColonyId(input.getIntOr(TAG_COLONY_ID, 0));
        citizenId = input.getIntOr(TAG_CITIZEN_ID, 0);
        citizenName = input.getStringOr(TAG_CITIZEN_NAME, "Citizen");
        female = input.getBooleanOr(TAG_IS_FEMALE, false);
        textureId = input.getIntOr(TAG_TEXTURE_ID, 0);
        String texSuffix = input.getStringOr("TextureSuffix", "_a");
        child = input.getBooleanOr(TAG_IS_CHILD, false);
        totalXp = input.getFloatOr(TAG_XP, 0.0f);

        // Load owner/bridge fields
        String ownerStr = input.getStringOr(TAG_OWNER_UUID, "");
        if (!ownerStr.isEmpty()) {
            try { ownerUUID = java.util.UUID.fromString(ownerStr); } catch (IllegalArgumentException ignored) {}
        }
        hunger = input.getIntOr(TAG_HUNGER, 20);
        startPos = new BlockPos(
            input.getIntOr(TAG_START_POS + "X", 0),
            input.getIntOr(TAG_START_POS + "Y", 0),
            input.getIntOr(TAG_START_POS + "Z", 0));
        upkeepChestPos = new BlockPos(
            input.getIntOr(TAG_UPKEEP_CHEST_POS + "X", 0),
            input.getIntOr(TAG_UPKEEP_CHEST_POS + "Y", 0),
            input.getIntOr(TAG_UPKEEP_CHEST_POS + "Z", 0));
        buildOrderId = input.getStringOr(TAG_BUILD_ORDER_ID, "");
        buildProgress = input.getIntOr(TAG_BUILD_PROGRESS, 0);
        buildState = input.getStringOr(TAG_BUILD_STATE, "IDLE");

        String jobStr = input.getStringOr(TAG_JOB, "");
        if (!jobStr.isEmpty()) {
            try {
                CitizenJob job = CitizenJob.valueOf(jobStr);
                ((CitizenJobHandler) citizenJobHandler).setJob(job);

                // Initialize the job AI and load saved AI data
                ((CitizenJobHandler) citizenJobHandler).initializeJobAI();
                com.ultra.megamod.feature.citizen.job.IJob jobInstance =
                        ((CitizenJobHandler) citizenJobHandler).getJobInstance();
                if (jobInstance != null) {
                    input.read(TAG_JOB_AI_DATA, CompoundTag.CODEC).ifPresent(jobInstance::loadFromNBT);
                }
            } catch (IllegalArgumentException ignored) {}
        }

        // Load handler data
        input.read(TAG_FOOD, CompoundTag.CODEC).ifPresent(citizenFoodHandler::read);
        input.read(TAG_DISEASE, CompoundTag.CODEC).ifPresent(citizenDiseaseHandler::read);
        input.read(TAG_HAPPINESS, CompoundTag.CODEC).ifPresent(citizenHappinessHandler::read);
        input.read(TAG_MOURN, CompoundTag.CODEC).ifPresent(citizenMournHandler::read);

        // Load inventory
        citizenInventory.clearContent();
        ValueInput.ValueInputList inventoryList = input.childrenListOrEmpty("Inventory");
        for (ValueInput slotInput : inventoryList) {
            int slot = slotInput.getIntOr("Slot", -1);
            if (slot >= 0 && slot < citizenInventory.getContainerSize()) {
                ItemStack stack = slotInput.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
                citizenInventory.setItem(slot, stack);
            }
        }

        // Sync entity data
        entityData.set(DATA_COLONY_ID, citizenColonyHandler.getColonyId());
        entityData.set(DATA_CITIZEN_ID, citizenId);
        entityData.set(DATA_IS_FEMALE, female ? 1 : 0);
        entityData.set(DATA_TEXTURE, textureId);
        entityData.set(DATA_TEXTURE_SUFFIX, texSuffix);
        entityData.set(DATA_IS_CHILD, child);

        // Sync the custom name so the nameplate is visible
        setCustomName(Component.literal(citizenName));
        setCustomNameVisible(true);

        setPose(Pose.STANDING);
    }

    // ==================== Tick ====================

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();

        if (collisionCounter > 0) {
            collisionCounter--;
        }

        if (interactionCooldown > 0) {
            interactionCooldown--;
        }

        if (callForHelpCooldown > 0) {
            callForHelpCooldown--;
        }

        // Server-side updates
        if (!level().isClientSide()) {
            // Tick the job AI every game tick (AI internally throttles itself)
            if (citizenJobHandler instanceof CitizenJobHandler jobHandler) {
                jobHandler.tickJobAI();
            }

            // Periodic updates every 20 ticks
            if (tickCount % TICKS_20 == 0) {
                citizenExperienceHandler.gatherXp();
                citizenDiseaseHandler.update(TICKS_20);
            }
        }

        // Client-side: colony update
        if (level().isClientSide() && tickCount % TICKS_20 == 0) {
            citizenColonyHandler.updateColonyClient();
        }
    }

    // ==================== Interaction ====================

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Golden apple cures disease
        ItemStack usedStack = player.getItemInHand(hand);
        if (usedStack.is(Items.GOLDEN_APPLE) && citizenDiseaseHandler.isSick()) {
            usedStack.shrink(1);
            if (getRandom().nextInt(3) == 0) {
                citizenDiseaseHandler.cure();
                playSound(SoundEvents.PLAYER_LEVELUP, 1.0f, 1.0f);
            }
            interactionCooldown = 20 * 60 * 5;
            return InteractionResult.CONSUME;
        }

        // Food interaction
        if (usedStack.has(net.minecraft.core.component.DataComponents.FOOD)) {
            citizenFoodHandler.addLastEaten(usedStack.getItem());
            playSound(SoundEvents.GENERIC_EAT.value(), 1.5f, 1.0f);
            usedStack.shrink(1);
            interactionCooldown = 100;
            return InteractionResult.CONSUME;
        }

        // Default: stop and look at player
        getNavigation().stop();
        getLookControl().setLookAt(player);

        // Greeting sound
        if (level().getGameTime() > nextPlayerCollisionTime) {
            nextPlayerCollisionTime = level().getGameTime() + TICKS_SECOND * 15;
            playSound(SoundEvents.VILLAGER_AMBIENT, 0.5f, 1.0f);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void push(@NotNull Entity entityIn) {
        if (entityIn instanceof ServerPlayer) {
            onPlayerCollide((Player) entityIn);
        }

        if ((collisionCounter += 2) > COLL_THRESHOLD) {
            if (collisionCounter > COLL_THRESHOLD * 2) {
                collisionCounter = 0;
            }
            return;
        }
        super.push(entityIn);
    }

    public void onPlayerCollide(Player player) {
        if (player.level().getGameTime() > nextPlayerCollisionTime) {
            nextPlayerCollisionTime = player.level().getGameTime() + TICKS_SECOND * 15;
            getNavigation().stop();
            getLookControl().setLookAt(player);
        }
    }

    // ==================== Combat ====================

    @Override
    public boolean hurtServer(ServerLevel level, @NotNull DamageSource damageSource, float damage) {
        // Damage cap: max 20% of max health per hit for more engaging gameplay
        float damageInc = Math.min(damage, getMaxHealth() * 0.2f);

        if (!super.hurtServer(level, damageSource, damageInc)) {
            return false;
        }

        // Track player damage time for XP drops
        if (damageSource.getEntity() instanceof Player) {
            lastHurtByPlayerTick = tickCount;
        }

        // Flee from non-guard jobs
        if (citizenJobHandler.shouldRunAvoidance() && damageSource.getEntity() != null) {
            // Simple flee: walk away from attacker
            Entity attacker = damageSource.getEntity();
            Vec3 fleeDir = this.position().subtract(attacker.position()).normalize();
            Vec3 fleeTarget = this.position().add(fleeDir.scale(10));
            this.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.2);
        }

        // Add damage happiness penalty
        citizenHappinessHandler.addModifier("damage", -0.5, 24000);

        return true;
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (!level().isClientSide()) {
            citizenExperienceHandler.dropExperience();
            // Drop inventory
            for (int i = 0; i < citizenInventory.getContainerSize(); i++) {
                ItemStack stack = citizenInventory.getItem(i);
                if (!stack.isEmpty()) {
                    spawnAtLocation((ServerLevel) level(), stack);
                }
            }
        }
        super.die(damageSource);
    }

    @Override
    public float getSpeed() {
        return (float) Math.min(MAX_SPEED_FACTOR, super.getSpeed());
    }

    // ==================== Display ====================

    @Override
    @NotNull
    public Component getDisplayName() {
        MutableComponent name = Component.literal(citizenName);
        CitizenJob job = citizenJobHandler.getColonyJob();
        if (job != null) {
            name = Component.literal(citizenName + " [" + job.getDisplayName() + "]");
        }
        return name;
    }

    @Override
    public boolean isBaby() {
        return child;
    }

    @Override
    public boolean isSleeping() {
        return citizenSleepHandler.isAsleep();
    }

    @Override
    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return !citizenSleepHandler.isAsleep();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean isNoAi() {
        return false;
    }

    // ==================== Accessors ====================

    public int getCitizenId() { return citizenId; }
    public void setCitizenId(int id) { this.citizenId = id; }

    public String getCitizenName() {
        // On client side, read from the custom name component since citizenName field
        // is only populated via readAdditionalSaveData (server-side only)
        if (level().isClientSide()) {
            Component custom = getCustomName();
            if (custom != null) {
                String s = custom.getString();
                if (!s.isEmpty()) return s;
            }
        }
        return citizenName;
    }
    public void setCitizenName(String name) {
        this.citizenName = name;
        // Keep the entity's custom name in sync so the nameplate renders
        this.setCustomName(Component.literal(name));
        this.setCustomNameVisible(true);
    }

    public boolean isFemale() {
        // On client side, read from synched entity data since fields aren't populated from save data
        if (level().isClientSide()) {
            return entityData.get(DATA_IS_FEMALE) != 0;
        }
        return female;
    }
    public void setFemale(boolean female) {
        this.female = female;
        entityData.set(DATA_IS_FEMALE, female ? 1 : 0);
    }

    public int getTextureId() {
        // On client side, read from synched entity data since fields aren't populated from save data
        if (level().isClientSide()) {
            return entityData.get(DATA_TEXTURE);
        }
        return textureId;
    }
    public void setTextureId(int textureId) {
        this.textureId = textureId;
        entityData.set(DATA_TEXTURE, textureId);
    }

    public String getRenderMetadata() { return renderMetadata; }
    public void setRenderMetadata(String metadata) {
        if (!metadata.equals(this.renderMetadata)) {
            this.renderMetadata = metadata;
            entityData.set(DATA_RENDER_METADATA, metadata);
        }
    }

    public String getTextureSuffix() {
        return entityData.get(DATA_TEXTURE_SUFFIX);
    }
    public void setTextureSuffix(String suffix) {
        entityData.set(DATA_TEXTURE_SUFFIX, suffix != null ? suffix : "_a");
    }

    public void setIsChild(boolean isChild) {
        this.child = isChild;
        entityData.set(DATA_IS_CHILD, isChild);
        refreshDimensions();
    }

    public void setTextureDirty() {
        this.textureDirty = true;
    }

    @Nullable
    public Identifier getTexture() {
        return texture;
    }

    public void setTexture(@Nullable Identifier texture) {
        this.texture = texture;
        this.textureDirty = false;
    }

    public boolean isTextureDirty() {
        return textureDirty;
    }

    public SimpleContainer getCitizenInventory() {
        return citizenInventory;
    }

    public double getTotalXp() { return totalXp; }
    public void addRawXp(double xp) { this.totalXp += xp; }

    public int getLastHurtByPlayerTime() {
        return lastHurtByPlayerTick;
    }

    // ---- Handler Accessors (MineColonies composition pattern) ----

    public ICitizenColonyHandler getCitizenColonyHandler() { return citizenColonyHandler; }
    public void setCitizenColonyHandler(ICitizenColonyHandler handler) { this.citizenColonyHandler = handler; }

    public ICitizenExperienceHandler getCitizenExperienceHandler() { return citizenExperienceHandler; }
    public void setCitizenExperienceHandler(ICitizenExperienceHandler handler) { this.citizenExperienceHandler = handler; }

    public ICitizenInventoryHandler getCitizenInventoryHandler() { return citizenInventoryHandler; }
    public void setCitizenInventoryHandler(ICitizenInventoryHandler handler) { this.citizenInventoryHandler = handler; }

    public ICitizenJobHandler getCitizenJobHandler() { return citizenJobHandler; }
    public void setCitizenJobHandler(ICitizenJobHandler handler) { this.citizenJobHandler = handler; }

    public ICitizenSleepHandler getCitizenSleepHandler() { return citizenSleepHandler; }
    public void setCitizenSleepHandler(ICitizenSleepHandler handler) { this.citizenSleepHandler = handler; }

    public ICitizenFoodHandler getCitizenFoodHandler() { return citizenFoodHandler; }
    public void setCitizenFoodHandler(ICitizenFoodHandler handler) { this.citizenFoodHandler = handler; }

    public ICitizenDiseaseHandler getCitizenDiseaseHandler() { return citizenDiseaseHandler; }
    public void setCitizenDiseaseHandler(ICitizenDiseaseHandler handler) { this.citizenDiseaseHandler = handler; }

    public ICitizenHappinessHandler getCitizenHappinessHandler() { return citizenHappinessHandler; }
    public void setCitizenHappinessHandler(ICitizenHappinessHandler handler) { this.citizenHappinessHandler = handler; }

    public ICitizenMournHandler getCitizenMournHandler() { return citizenMournHandler; }
    public void setCitizenMournHandler(ICitizenMournHandler handler) { this.citizenMournHandler = handler; }

    // ==================== Bridge / Convenience Methods ====================
    // These provide backward-compatible accessors used by command, network, and admin handlers.

    @Nullable
    public java.util.UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(@Nullable java.util.UUID uuid) { this.ownerUUID = uuid; }

    /** Convenience: get the current job from the job handler. */
    @Nullable
    public CitizenJob getCitizenJob() { return citizenJobHandler.getColonyJob(); }

    /** Convenience: set the current job via the job handler (with AI init). */
    public void setCitizenJob(@Nullable CitizenJob job) {
        citizenJobHandler.onJobChanged(job);
    }

    /** Hunger level (0-20). Managed independently of the food handler's diet tracking. */
    public int getHunger() { return hunger; }
    public void setHunger(int hunger) { this.hunger = Math.max(0, Math.min(20, hunger)); }

    /** Alias for getCitizenInventory(), used by admin/town handlers. */
    public SimpleContainer getInventory() { return citizenInventory; }

    /** Happiness data convenience: delegates to the happiness handler. */
    public ICitizenHappinessHandler getHappinessData() { return citizenHappinessHandler; }

    /** Start/work position for building assignment. */
    public BlockPos getStartPos() { return startPos; }
    public void setStartPos(BlockPos pos) { this.startPos = pos != null ? pos : BlockPos.ZERO; }

    /** Upkeep chest position for automatic feeding. */
    public BlockPos getUpkeepChestPos() { return upkeepChestPos; }
    public void setUpkeepChestPos(BlockPos pos) { this.upkeepChestPos = pos != null ? pos : BlockPos.ZERO; }

    /** Build order tracking for builder citizens. */
    public boolean hasBuildOrder() { return !buildOrderId.isEmpty(); }
    public String getBuildOrderId() { return buildOrderId; }
    public void setBuildOrderId(String id) { this.buildOrderId = id != null ? id : ""; }
    public int getBuildProgress() { return buildProgress; }
    public void setBuildProgress(int progress) { this.buildProgress = progress; }
    public String getBuildState() { return buildState; }
    public void setBuildState(String state) { this.buildState = state != null ? state : "IDLE"; }

    // ==================== Equals / HashCode ====================

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MCEntityCitizen other) {
            return other.citizenColonyHandler.getColonyId() == this.citizenColonyHandler.getColonyId()
                    && other.citizenId == this.citizenId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(citizenId, citizenColonyHandler.getColonyId());
    }

    @Override
    public String toString() {
        return "MCEntityCitizen{name=" + citizenName +
                ", id=" + citizenId +
                ", colony=" + citizenColonyHandler.getColonyId() +
                ", pos=" + blockPosition() +
                ", job=" + (citizenJobHandler.getColonyJob() != null ? citizenJobHandler.getColonyJob().name() : "none") +
                "}";
    }
}
