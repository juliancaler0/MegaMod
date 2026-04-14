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
            case "limb_swing_amount", "limb_speed" -> limbSwingAmount();
            case "swim_amount" -> swimAmount();
            case "speed" -> speed();
            case "player_yaw", "yaw", "head_yaw" -> headYaw();
            case "player_pitch", "pitch", "head_pitch" -> headPitch();
            case "body_yaw" -> bodyYaw();
            case "id" -> entityId();
            case "scale" -> scale();
            case "age_scale" -> ageScale();
            // Phase F: additional built-in variables resolved from live game state.
            case "pos_x" -> posX();
            case "pos_y" -> posY();
            case "pos_z" -> posZ();
            case "player_pos_x" -> playerPosX();
            case "player_pos_y" -> playerPosY();
            case "player_pos_z" -> playerPosZ();
            case "player_rot_x" -> playerRotX();
            case "player_rot_y" -> playerRotY();
            case "rot_x" -> headPitch();
            case "rot_y" -> headYaw();
            case "time" -> time();
            case "day_time" -> dayTime();
            case "day_count" -> dayCount();
            case "frame_time" -> frameTime();
            case "frame_counter" -> frameCounter();
            case "partial_ticks" -> partialTicks();
            case "dimension" -> dimensionIndex();
            case "distance" -> distance();
            case "rule_index" -> ruleIndex();
            default -> 0f;
        };
    }

    private float ruleIndex() {
        if (uuid == null) return 0f;
        try {
            int idx = com.ultra.megamod.lib.etf.features.ETFManager.getInstance()
                    .LAST_RULE_INDEX_OF_ENTITY.getInt(uuid);
            return idx < 0 ? 0f : idx;
        } catch (Throwable ignored) {
            return 0f;
        }
    }

    /**
     * Best-effort {@code nbt(key, query)} implementation. Walks the entity's
     * tag tree looking for a numeric value. Silently returns 0 on mismatch.
     * Matches OptiFine's tolerant syntax: separators may be any of space,
     * colon, dot, or slash.
     */
    @Override
    public float evaluateNbt(String expression) {
        if (expression == null || expression.isEmpty() || etfState == null) return 0f;
        net.minecraft.nbt.CompoundTag root;
        try {
            root = etfState.nbt();
        } catch (Throwable t) {
            return 0f;
        }
        if (root == null) return 0f;

        String q = expression.replace(':', ' ').replace('.', ' ').replace('/', ' ').trim();
        String[] parts = q.split("\\s+");
        if (parts.length == 0) return 0f;

        net.minecraft.nbt.Tag cursor = root;
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (cursor instanceof net.minecraft.nbt.CompoundTag ct) {
                cursor = ct.get(p);
                if (cursor == null) return 0f;
            } else {
                return 0f;
            }
        }
        if (cursor instanceof net.minecraft.nbt.NumericTag nt) {
            return nt.floatValue();
        }
        if (cursor instanceof net.minecraft.nbt.StringTag st) {
            try {
                return Float.parseFloat(st.value());
            } catch (NumberFormatException nfe) {
                return 0f;
            }
        }
        return 0f;
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
            // Phase F: render-context flags that upstream exposes as simple globals.
            case "is_in_hand" -> EmfRenderContextFlags.setInHand;
            case "is_in_item_frame" -> EmfRenderContextFlags.setInItemFrame;
            case "is_on_head" -> EmfRenderContextFlags.setIsOnHead;
            case "is_in_gui" -> EmfRenderContextFlags.setIsInGui;
            case "is_first_person_hand" -> EmfRenderContextFlags.isFirstPersonHand;
            case "is_in_ground" -> EmfRenderContextFlags.is_in_ground_override;
            case "is_paused" -> uuid != null
                    && com.ultra.megamod.lib.emf.api.EMFApi.isEntityAnimationPaused(uuid);
            default -> false;
        };
    }

    @Override
    public float getEntityVariable(String name) {
        // Try the frame-local map first, then fall back to the per-entity
        // persistent map so values written on a prior frame are still visible.
        Float f = entityVariables.get(name);
        if (f != null) return f;
        if (uuid != null) {
            return EmfPerEntityVariables.getEntity(uuid, name);
        }
        return 0f;
    }

    @Override
    public void setEntityVariable(String name, float value) {
        entityVariables.put(name, value);
        if (uuid != null) {
            EmfPerEntityVariables.setEntity(uuid, name, value);
        }
    }

    @Override
    public float getGlobalVariable(String name) {
        return EmfPerEntityVariables.getGlobal(name);
    }

    @Override
    public void setGlobalVariable(String name, float value) {
        EmfPerEntityVariables.setGlobal(name, value);
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

    // --- Phase F: extended float resolvers --------------------------------

    private float posX() {
        if (vanilla != null) return (float) vanilla.x;
        return 0f;
    }

    private float posY() {
        if (vanilla != null) return (float) vanilla.y;
        return 0f;
    }

    private float posZ() {
        if (vanilla != null) return (float) vanilla.z;
        return 0f;
    }

    private float playerPosX() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) return (float) mc.player.getX();
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float playerPosY() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) return (float) mc.player.getY();
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float playerPosZ() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) return (float) mc.player.getZ();
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float playerRotX() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) return mc.player.getXRot();
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float playerRotY() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) return mc.player.getYRot();
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float time() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) return (float) (mc.level.getGameTime() % 24000L);
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float dayTime() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) return (float) (mc.level.getDayTime() % 24000L);
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float dayCount() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) return (float) (mc.level.getDayTime() / 24000L);
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float frameTime() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            return mc.getDeltaTracker().getRealtimeDeltaTicks();
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    // Frame counter: upstream uses a monotonically-incrementing per-render counter.
    // Vanilla 1.21.11 doesn't expose a direct getter, so we track it ourselves via
    // the client-tick listener in EMFConfigKeybind and read the last-tick value.
    private float frameCounter() {
        return (float) EmfFrameCounter.current();
    }

    private float partialTicks() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            return mc.getDeltaTracker().getGameTimeDeltaTicks();
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float dimensionIndex() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level != null) {
                var dim = mc.level.dimension();
                if (dim == net.minecraft.world.level.Level.OVERWORLD) return 0f;
                if (dim == net.minecraft.world.level.Level.NETHER) return -1f;
                if (dim == net.minecraft.world.level.Level.END) return 1f;
                return 2f;
            }
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    private float distance() {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null && vanilla != null) {
                double dx = vanilla.x - mc.player.getX();
                double dy = vanilla.y - mc.player.getY();
                double dz = vanilla.z - mc.player.getZ();
                return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
            }
        } catch (Throwable ignored) {
        }
        return 0f;
    }

    public EntityType<?> entityType() {
        return entityType;
    }

    /** Phase F: expose the captured UUID for the pause / vanilla-lock fast path. */
    @Nullable
    public UUID uuid() {
        return uuid;
    }

    @Nullable
    public EntityRenderState vanilla() {
        return vanilla;
    }

    @Nullable
    public ETFEntityRenderState etfState() {
        return etfState;
    }
}
