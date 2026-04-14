package com.ultra.megamod.lib.emf.api;

import com.ultra.megamod.lib.emf.animation.EmfVariableContext;
import com.ultra.megamod.lib.emf.animation.math.variables.VariableRegistry;
import com.ultra.megamod.lib.emf.animation.math.methods.MethodRegistry;
import com.ultra.megamod.lib.emf.animation.math.variables.factories.UniqueVariableFactory;
import com.ultra.megamod.lib.emf.runtime.EmfActiveModel;
import com.ultra.megamod.lib.emf.runtime.EmfModelBinder;
import com.ultra.megamod.lib.emf.runtime.EmfModelManager;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import com.ultra.megamod.lib.etf.features.state.ETFEntityRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Public API surface for third-party mods that want to interoperate with EMF.
 * <p>
 * This mirrors upstream {@code EMFAnimationApi} and {@code IEMFAnimationApi}
 * 1:1 where the MegaMod port has an equivalent. Where upstream surface depends
 * on features we don't ship (TriFunction from Apache Commons, Physics Mod
 * patches, EmoteCraft mixin detection), those entries are adapted.
 * <p>
 * Usage (from another mod's client init):
 * <pre>
 *   EMFApi.registerSingletonAnimationVariable(
 *       "my_mod", "my_custom_flag", "Per-entity flag my mod provides.",
 *       () -&gt; someState.getAsBoolean());
 *   EMFApi.registerAnimationFunction(
 *       "my_mod", "my_smooth", "Custom easing curve.",
 *       t -&gt; 3*t*t - 2*t*t*t);
 * </pre>
 * Every registration is no-op-safe: null inputs are logged + ignored so mods
 * can call the API unconditionally from their setup events.
 */
public final class EMFApi {

    private EMFApi() {
    }

    /**
     * Current API version. Bumped on breaking changes; the port starts at
     * upstream's {@code v8} since we match its surface exactly.
     */
    public static int getApiVersion() {
        return 8;
    }

    // --- Variables registry ------------------------------------------------

    /**
     * Register a singleton boolean variable. The supplier is called once per
     * expression eval, so treat it as a cheap read.
     */
    public static void registerSingletonAnimationVariable(String sourceModId, String variableName,
                                                          String explanationText, BooleanSupplier valueSupplier) {
        if (validate(sourceModId, variableName, explanationText, valueSupplier)) {
            VariableRegistry.getInstance()
                    .registerSimpleBoolVariable(variableName, explanationText, valueSupplier);
            EMFUtils.log("Registered bool variable: " + variableName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid bool variable registration: " + variableName + " from mod " + sourceModId);
        }
    }

    /**
     * Register a singleton float variable.
     */
    public static void registerSingletonAnimationVariable(String sourceModId, String variableName,
                                                          String explanationText, Supplier<Float> valueSupplier) {
        if (validate(sourceModId, variableName, explanationText, valueSupplier)) {
            VariableRegistry.getInstance()
                    .registerSimpleFloatVariable(variableName, explanationText, valueSupplier::get);
            EMFUtils.log("Registered float variable: " + variableName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid float variable registration: " + variableName + " from mod " + sourceModId);
        }
    }

    /**
     * Plain {@code MathValue.ResultSupplier} overload for mods producing a float
     * directly without wrapping it in a {@link Supplier} first.
     */
    public static void registerSingletonAnimationVariable(String sourceModId, String variableName,
                                                          String explanationText,
                                                          com.ultra.megamod.lib.emf.animation.math.MathValue.ResultSupplier valueSupplier) {
        if (validate(sourceModId, variableName, explanationText, valueSupplier)) {
            VariableRegistry.getInstance()
                    .registerSimpleFloatVariable(variableName, explanationText, valueSupplier);
            EMFUtils.log("Registered float variable: " + variableName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid float variable registration: " + variableName + " from mod " + sourceModId);
        }
    }

    /**
     * Register a unique / contextual variable factory that can provide
     * per-entity variables whose name must be matched by a custom predicate.
     */
    public static void registerUniqueAnimationVariableFactory(String sourceModId, String variableName,
                                                              UniqueVariableFactory factory) {
        if (sourceModId != null && variableName != null && factory != null) {
            VariableRegistry.getInstance().registerContextVariable(factory);
            EMFUtils.log("Registered unique variable: " + variableName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid unique variable registration: " + variableName + " from mod " + sourceModId);
        }
    }

    // --- Methods registry --------------------------------------------------

    /** Register a single-argument math function. */
    public static void registerAnimationFunction(String sourceModId, String methodName, String explanationText,
                                                 Function<Float, Float> function) {
        if (validate(sourceModId, methodName, explanationText, function)) {
            MethodRegistry.getInstance().registerSimpleMethodFactory(methodName, explanationText, function);
            EMFUtils.log("Registered function: " + methodName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid function registration: " + methodName + " from mod " + sourceModId);
        }
    }

    /** Register a two-argument math function. */
    public static void registerAnimationBiFunction(String sourceModId, String methodName, String explanationText,
                                                   BiFunction<Float, Float, Float> biFunction) {
        if (validate(sourceModId, methodName, explanationText, biFunction)) {
            MethodRegistry.getInstance().registerSimpleMethodFactory(methodName, explanationText, biFunction);
            EMFUtils.log("Registered bi-function: " + methodName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid bi-function registration: " + methodName + " from mod " + sourceModId);
        }
    }

    /** Register a multi-argument animation function that takes a {@code List<Float>} of parameters. */
    public static void registerAnimationMultiFunction(String sourceModId, String methodName, String explanationText,
                                                      Function<java.util.List<Float>, Float> multiFunction) {
        if (validate(sourceModId, methodName, explanationText, multiFunction)) {
            MethodRegistry.getInstance().registerSimpleMultiMethodFactory(methodName, explanationText, multiFunction);
            EMFUtils.log("Registered multi-function: " + methodName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid multi-function registration: " + methodName + " from mod " + sourceModId);
        }
    }

    /** Register a custom factory when simple function signatures aren't enough. */
    public static void registerCustomFunctionFactory(String sourceModId, String methodName, String explanationText,
                                                     MethodRegistry.MethodFactory factory) {
        if (validate(sourceModId, methodName, explanationText, factory)) {
            MethodRegistry.getInstance().registerAndWrapMethodFactory(methodName, explanationText, factory);
            EMFUtils.log("Registered custom factory: " + methodName + " from mod " + sourceModId);
        } else {
            EMFUtils.logError("Invalid custom factory registration: " + methodName + " from mod " + sourceModId);
        }
    }

    // --- Query surface -----------------------------------------------------

    /**
     * Returns {@code true} if the given model currently has an EMF active binding.
     */
    public static boolean isModelCustomizedByEMF(EntityModel<?> model) {
        if (model == null) return false;
        try {
            var holder = (com.ultra.megamod.lib.emf.access.EmfEntityModelHolder) model;
            return holder.emf$getActiveModel() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Returns {@code true} if the given model currently has a binding that
     * declares at least one animation.
     */
    public static boolean isModelAnimatedByEMF(EntityModel<?> model) {
        if (!isModelCustomizedByEMF(model)) return false;
        try {
            var holder = (com.ultra.megamod.lib.emf.access.EmfEntityModelHolder) model;
            EmfActiveModel active = holder.emf$getActiveModel();
            return active != null && active.definition != null
                    && !active.definition.animationsByBone.isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Returns the currently bound EMF active model for {@code model}, or
     * {@code null} if none.
     */
    @Nullable
    public static EmfActiveModel getActiveModel(EntityModel<?> model) {
        if (model == null) return null;
        try {
            var holder = (com.ultra.megamod.lib.emf.access.EmfEntityModelHolder) model;
            return holder.emf$getActiveModel();
        } catch (Throwable t) {
            return null;
        }
    }

    // --- Pause / vanilla-model controls -----------------------------------

    private static final java.util.Set<UUID> PAUSED = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, ModelPart[]> PAUSED_PARTS = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> FORCED_VANILLA = ConcurrentHashMap.newKeySet();
    private static final CopyOnWriteArrayList<Function<UUID, Boolean>> PAUSE_LISTENERS = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Function<UUID, Boolean>> FORCE_VANILLA_LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * Register a predicate that, if it returns {@code true} for an entity's
     * UUID, causes EMF to pause its animations for that entity this frame.
     */
    public static boolean registerPauseCondition(Function<UUID, Boolean> shouldPause) {
        if (shouldPause == null) return false;
        PAUSE_LISTENERS.add(shouldPause);
        return true;
    }

    /** Registers a predicate that forces an entity to render its vanilla model. */
    public static boolean registerVanillaModelCondition(Function<UUID, Boolean> shouldUseVanilla) {
        if (shouldUseVanilla == null) return false;
        FORCE_VANILLA_LISTENERS.add(shouldUseVanilla);
        return true;
    }

    /** Marks an entity as EMF-paused until {@link #resumeEntity(UUID)} is called. */
    public static boolean pauseEntity(UUID uuid) {
        if (uuid == null) return false;
        PAUSED.add(uuid);
        return true;
    }

    /** Resumes EMF animations for a previously-paused entity. */
    public static boolean resumeEntity(UUID uuid) {
        if (uuid == null) return false;
        PAUSED.remove(uuid);
        PAUSED_PARTS.remove(uuid);
        return true;
    }

    /** Pause only a subset of bones for this entity. */
    public static boolean pausePartsOfEntity(UUID uuid, ModelPart... parts) {
        if (uuid == null || parts == null || parts.length == 0) return false;
        PAUSED_PARTS.put(uuid, parts);
        return true;
    }

    /** Force an entity onto its vanilla model. */
    public static boolean lockEntityToVanillaModel(UUID uuid) {
        if (uuid == null) return false;
        FORCED_VANILLA.add(uuid);
        return true;
    }

    /** Re-allow EMF on a previously-locked entity. */
    public static boolean unlockEntityToVanillaModel(UUID uuid) {
        if (uuid == null) return false;
        FORCED_VANILLA.remove(uuid);
        return true;
    }

    /**
     * Returns {@code true} if animations should be paused for this UUID. Called
     * from {@link EmfModelBinder} and {@code EmfBoneApplier} on hot paths.
     */
    public static boolean isEntityAnimationPaused(@Nullable UUID uuid) {
        if (uuid == null) return false;
        if (PAUSED.contains(uuid)) return true;
        for (Function<UUID, Boolean> f : PAUSE_LISTENERS) {
            try {
                Boolean r = f.apply(uuid);
                if (r != null && r) return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    /** Returns the subset of paused parts, or {@code null} for full pause / no pause. */
    @Nullable
    public static ModelPart[] pausedPartsFor(@Nullable UUID uuid) {
        if (uuid == null) return null;
        return PAUSED_PARTS.get(uuid);
    }

    /** Returns {@code true} if the entity is locked to the vanilla model. */
    public static boolean isEntityLockedToVanillaModel(@Nullable UUID uuid) {
        if (uuid == null) return false;
        if (FORCED_VANILLA.contains(uuid)) return true;
        for (Function<UUID, Boolean> f : FORCE_VANILLA_LISTENERS) {
            try {
                Boolean r = f.apply(uuid);
                if (r != null && r) return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    // --- Bone-apply pipeline hooks ----------------------------------------

    /**
     * Listener for every animation write EMF performs. Provides access to the
     * affected {@link ModelPart}, the bone name, the animation target key, and
     * the final computed value. Registered listeners run after the axis field
     * is written so observers can read the post-write state.
     */
    @FunctionalInterface
    public interface BoneApplyListener {
        void onApply(String boneName, String targetKey, ModelPart bone, float value);
    }

    private static final CopyOnWriteArrayList<BoneApplyListener> BONE_LISTENERS = new CopyOnWriteArrayList<>();

    public static boolean registerBoneApplyListener(BoneApplyListener listener) {
        if (listener == null) return false;
        BONE_LISTENERS.add(listener);
        return true;
    }

    public static boolean unregisterBoneApplyListener(BoneApplyListener listener) {
        if (listener == null) return false;
        return BONE_LISTENERS.remove(listener);
    }

    /** Invoked from {@code EmfBoneApplier} after every successful axis write. */
    public static void fireBoneApply(String boneName, String targetKey, ModelPart bone, float value) {
        if (BONE_LISTENERS.isEmpty()) return;
        for (BoneApplyListener l : BONE_LISTENERS) {
            try {
                l.onApply(boneName, targetKey, bone, value);
            } catch (Throwable ignored) {
            }
        }
    }

    // --- Frame variable context ------------------------------------------

    /**
     * Returns the active {@link EmfVariableContext} for the currently-rendering
     * entity, or {@code null} if none. Primarily useful for mods that want to
     * read EMF variables during their own render code.
     */
    @Nullable
    public static EmfVariableContext currentVariableContext() {
        return com.ultra.megamod.lib.emf.animation.EmfRuntime.current().context();
    }

    /** Cast helper: entities expose a UUID via {@code Entity#getUUID()}. */
    public static UUID uuidOf(Entity entity) {
        return entity == null ? null : entity.getUUID();
    }

    /** Block-entity variant. Block entities don't have UUIDs natively; we synthesise one from the block pos. */
    public static UUID uuidOf(BlockEntity be) {
        if (be == null) return null;
        var pos = be.getBlockPos();
        return new UUID(Objects.hash(pos.getX(), pos.getY(), pos.getZ()), pos.asLong());
    }

    // --- Built-in registration -------------------------------------------

    /**
     * Invoked during client setup to seed the registries with any EMF-port
     * specific built-ins. Currently the registries ship ~130 built-ins at
     * static-init; this hook is the documented entry point third-party mods
     * can call to ensure registries are warm before they try to register
     * their own entries.
     */
    public static void registerBuiltins() {
        // Touch the classes so their static initialisers run.
        try {
            Class.forName(VariableRegistry.class.getName());
            Class.forName(MethodRegistry.class.getName());
        } catch (Throwable t) {
            EMFUtils.logWarn("EMFApi.registerBuiltins could not touch registries: " + t);
        }
    }

    // --- Helpers ---------------------------------------------------------

    private static boolean validate(Object... any) {
        for (Object o : any) if (o == null) return false;
        return true;
    }

    /** Returns the registered model's active bound metadata, if known. */
    @Nullable
    public static EmfActiveModel activeModelForEntity(@Nullable ETFEntityRenderState state) {
        if (state == null) return null;
        String key = EmfModelBinder.deriveEntityTypeKey(state);
        if (key == null || key.isEmpty()) return null;
        return EmfModelManager.getInstance().bindForEntity(key, state);
    }
}
