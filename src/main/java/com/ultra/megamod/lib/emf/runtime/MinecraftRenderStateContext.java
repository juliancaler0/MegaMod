package com.ultra.megamod.lib.emf.runtime;

import com.ultra.megamod.lib.emf.animation.EmfVariableContext;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Render-state-backed {@link EmfVariableContext}.
 * <p>
 * Phase E binds one of these for the duration of a single entity's {@code setupAnim}
 * call; Phase D's expression evaluator calls back through the
 * {@link com.ultra.megamod.lib.emf.animation.EmfRuntime} thread-local to read values.
 * <p>
 * The variable surface that Fresh Animations packs actually hit is small — health,
 * limb_swing, age, yaw/pitch, is_sneak, is_swimming, is_riding, is_in_water, and a
 * handful of booleans. Upstream exposes ~90 names; the priorities here match what
 * packs use on ~95% of entities. Unknown names return 0 / false as upstream does.
 * <p>
 * Field access is against 1.21.11 {@code LivingEntityRenderState} /
 * {@code EntityRenderState} / {@code HumanoidRenderState}. Names checked by
 * {@code javap} against the deobf Minecraft jar shipped by the neoformruntime;
 * keep this list in sync if the mapping changes.
 */
public final class MinecraftRenderStateContext implements EmfVariableContext {

    @Nullable private final EntityRenderState vanilla;
    @Nullable private final ETFEntityRenderState etfState;
    @Nullable private final UUID uuid;
    private final EntityType<?> entityType;

    // Per-frame user variables (var.X / varb.X). Upstream persists these across
    // frames keyed by entity; the Phase E context resets each frame for simplicity —
    // Phase F upgrades to per-entity persistence via UUID.
    private final Map<String, Float> entityVariables = new HashMap<>();

    public MinecraftRenderStateContext(@Nullable EntityRenderState vanilla,
                                       @Nullable ETFEntityRenderState etfState,
                                       @Nullable UUID uuid,
                                       EntityType<?> entityType) {
        this.vanilla = vanilla;
        this.etfState = etfState;
        this.uuid = uuid;
        this.entityType = entityType;
    }

    @Override
    public float getFloat(String name) {
        if (name == null) return 0f;
        return switch (name) {
            case "age", "entity_age" -> age();
            case "death_time" -> deathTime();
            case "limb_swing", "limb_swing_speed" -> limbSwingSpeed();
            case "limb_swing_amount" -> limbSwingAmount();
            case "swim_amount" -> swimAmount();
            case "speed" -> speed();
            case "player_yaw", "yaw", "head_yaw" -> headYaw();
            case "player_pitch", "pitch", "head_pitch" -> headPitch();
            case "body_yaw" -> bodyYaw();
            case "id" -> entityId();
            case "scale" -> scale();
            case "age_scale" -> ageScale();
            default -> 0f;
        };
    }

    @Override
    public boolean getBoolean(String name) {
        if (name == null) return false;
        return switch (name) {
            case "is_alive" -> isAlive();
            case "is_sneaking", "is_sneak" -> isSneaking();
            case "is_crouching" -> isCrouching();
            case "is_riding" -> isPassenger();
            case "is_child", "is_baby" -> isBaby();
            case "is_in_water" -> isInWater();
            case "is_upside_down" -> isUpsideDown();
            case "is_fully_frozen" -> isFullyFrozen();
            case "is_invisible" -> isInvisible();
            case "is_glowing" -> isGlowing();
            case "is_burning", "is_on_fire" -> isOnFire();
            case "is_swimming", "is_visually_swimming" -> isSwimming();
            case "is_fall_flying", "is_elytra_flying" -> isFallFlying();
            case "is_auto_spin_attack" -> isAutoSpinAttack();
            case "has_red_overlay" -> hasRedOverlay();
            case "is_using_item" -> isUsingItem();
            default -> false;
        };
    }

    @Override
    public float getEntityVariable(String name) {
        Float f = entityVariables.get(name);
        return f == null ? 0f : f;
    }

    @Override
    public void setEntityVariable(String name, float value) {
        entityVariables.put(name, value);
    }

    // --- Float accessors --------------------------------------------------

    private float age() {
        if (vanilla != null) return vanilla.ageInTicks;
        return 0f;
    }

    private float deathTime() {
        if (vanilla instanceof LivingEntityRenderState living) return living.deathTime;
        return 0f;
    }

    private float limbSwingSpeed() {
        if (vanilla instanceof LivingEntityRenderState living) return living.walkAnimationSpeed;
        return 0f;
    }

    private float limbSwingAmount() {
        if (vanilla instanceof LivingEntityRenderState living) return living.walkAnimationPos;
        return 0f;
    }

    private float swimAmount() {
        if (vanilla instanceof HumanoidRenderState humanoid) return humanoid.swimAmount;
        return 0f;
    }

    private float speed() {
        if (vanilla instanceof HumanoidRenderState humanoid) return humanoid.speedValue;
        if (vanilla instanceof LivingEntityRenderState living) return living.walkAnimationSpeed;
        return 0f;
    }

    private float headYaw() {
        if (vanilla instanceof LivingEntityRenderState living) return living.yRot;
        return 0f;
    }

    private float headPitch() {
        if (vanilla instanceof LivingEntityRenderState living) return living.xRot;
        return 0f;
    }

    private float bodyYaw() {
        if (vanilla instanceof LivingEntityRenderState living) return living.bodyRot;
        return 0f;
    }

    private float entityId() {
        if (uuid != null) return uuid.hashCode();
        return 0f;
    }

    private float scale() {
        if (vanilla instanceof LivingEntityRenderState living) return living.scale;
        return 1f;
    }

    private float ageScale() {
        if (vanilla instanceof LivingEntityRenderState living) return living.ageScale;
        return 1f;
    }

    // --- Boolean accessors -----------------------------------------------

    private boolean isAlive() {
        // No direct health field in 1.21.11 LivingEntityRenderState; fall back to deathTime.
        if (vanilla instanceof LivingEntityRenderState living) return living.deathTime <= 0f;
        return true;
    }

    private boolean isSneaking() {
        if (vanilla != null) return vanilla.isDiscrete;
        return false;
    }

    private boolean isCrouching() {
        if (vanilla instanceof HumanoidRenderState humanoid) return humanoid.isCrouching;
        return false;
    }

    private boolean isPassenger() {
        if (vanilla instanceof HumanoidRenderState humanoid) return humanoid.isPassenger;
        return false;
    }

    private boolean isBaby() {
        if (vanilla instanceof LivingEntityRenderState living) return living.isBaby;
        return false;
    }

    private boolean isInWater() {
        if (vanilla instanceof LivingEntityRenderState living) return living.isInWater;
        return false;
    }

    private boolean isUpsideDown() {
        if (vanilla instanceof LivingEntityRenderState living) return living.isUpsideDown;
        return false;
    }

    private boolean isFullyFrozen() {
        if (vanilla instanceof LivingEntityRenderState living) return living.isFullyFrozen;
        return false;
    }

    private boolean isInvisible() {
        if (vanilla != null) return vanilla.isInvisible;
        return false;
    }

    private boolean isGlowing() {
        if (vanilla != null) return vanilla.appearsGlowing();
        return false;
    }

    private boolean isOnFire() {
        // 1.21.11 renames "onFire" to "displayFireAnimation" on EntityRenderState.
        if (vanilla != null) return vanilla.displayFireAnimation;
        return false;
    }

    private boolean isSwimming() {
        if (vanilla instanceof HumanoidRenderState humanoid) return humanoid.isVisuallySwimming;
        return false;
    }

    private boolean isFallFlying() {
        if (vanilla instanceof HumanoidRenderState humanoid) return humanoid.isFallFlying;
        return false;
    }

    private boolean isAutoSpinAttack() {
        if (vanilla instanceof LivingEntityRenderState living) return living.isAutoSpinAttack;
        return false;
    }

    private boolean hasRedOverlay() {
        if (vanilla instanceof LivingEntityRenderState living) return living.hasRedOverlay;
        return false;
    }

    private boolean isUsingItem() {
        if (vanilla instanceof HumanoidRenderState humanoid) return humanoid.isUsingItem;
        return false;
    }

    public EntityType<?> entityType() {
        return entityType;
    }
}
