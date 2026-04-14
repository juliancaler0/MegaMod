package com.ultra.megamod.lib.emf.animation.math.variables;

import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.EmfVariableContext;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathConstant;
import com.ultra.megamod.lib.emf.animation.math.MathValue;
import com.ultra.megamod.lib.emf.animation.math.MathVariable;
import com.ultra.megamod.lib.emf.animation.math.variables.factories.GlobalVariableFactory;
import com.ultra.megamod.lib.emf.animation.math.variables.factories.ModelPartVariableFactory;
import com.ultra.megamod.lib.emf.animation.math.variables.factories.ModelVariableFactory;
import com.ultra.megamod.lib.emf.animation.math.variables.factories.RenderVariableFactory;
import com.ultra.megamod.lib.emf.animation.math.variables.factories.UniqueVariableFactory;
import com.ultra.megamod.lib.emf.utils.EMFUtils;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Central registry of built-in animation variables.
 * <p>
 * Ported from upstream with a key rewire: where upstream routes every variable through
 * a static method on {@code EMFAnimationEntityContext}, we route through the active
 * {@link EmfVariableContext}. Variable names are preserved 1:1 so any {@code .jem} pack
 * that worked in upstream EMF works here.
 * <p>
 * Constants ({@code pi}, {@code e}, {@code true}, {@code false}) stay folded at parse
 * time. Context-dependent factories ({@code var.*}, {@code global_var.*}, {@code render.*},
 * {@code boneName.rx}, ...) plug into the sibling-variable map on {@link EmfParseContext}.
 */
public final class VariableRegistry {

    private static final VariableRegistry INSTANCE = new VariableRegistry();
    private final Map<String, MathComponent> singletonVariables = new HashMap<>();
    private final Map<String, String> singletonVariableExplanationTranslationKeys = new HashMap<>();
    private final List<UniqueVariableFactory> uniqueVariableFactories = new ArrayList<>();

    private VariableRegistry() {
        // --- Math constants (parse-time constant folded) ---
        singletonVariables.put("pi", new MathConstant(Mth.PI));
        singletonVariables.put("-pi", new MathConstant(-Mth.PI));
        singletonVariables.put("e", new MathConstant((float) Math.E));
        singletonVariables.put("-e", new MathConstant((float) -Math.E));
        singletonVariables.put("true", new MathConstant(MathValue.TRUE));
        singletonVariables.put("!true", new MathConstant(MathValue.FALSE));
        singletonVariables.put("false", new MathConstant(MathValue.FALSE));
        singletonVariables.put("!false", new MathConstant(MathValue.TRUE));
        explain("pi", "e", "true", "false");

        // --- Simple float variables (all dispatched through the context) ---
        registerSimpleFloatVariable("limb_swing");
        registerSimpleFloatVariable("limb_speed");
        registerSimpleFloatVariable("age");
        registerSimpleFloatVariable("head_pitch");
        registerSimpleFloatVariable("head_yaw");
        registerSimpleFloatVariable("swing_progress");
        registerSimpleFloatVariable("hurt_time");
        registerSimpleFloatVariable("dimension");
        registerSimpleFloatVariable("time");
        registerSimpleFloatVariable("frame_time");
        registerSimpleFloatVariable("frame_counter");
        registerSimpleFloatVariable("partial_ticks");
        registerSimpleFloatVariable("player_pos_x");
        registerSimpleFloatVariable("player_pos_y");
        registerSimpleFloatVariable("player_pos_z");
        registerSimpleFloatVariable("pos_x");
        registerSimpleFloatVariable("pos_y");
        registerSimpleFloatVariable("pos_z");
        registerSimpleFloatVariable("player_rot_x");
        registerSimpleFloatVariable("player_rot_y");
        registerSimpleFloatVariable("rot_x");
        registerSimpleFloatVariable("rot_y");
        registerSimpleFloatVariable("health");
        registerSimpleFloatVariable("death_time");
        registerSimpleFloatVariable("anger_time");
        registerSimpleFloatVariable("max_health");
        registerSimpleFloatVariable("health_ratio");
        registerSimpleFloatVariable("id");
        registerSimpleFloatVariable("day_time");
        registerSimpleFloatVariable("day_count");
        registerSimpleFloatVariable("rule_index");
        registerSimpleFloatVariable("anger_time_start");
        registerSimpleFloatVariable("move_forward");
        registerSimpleFloatVariable("move_strafing");
        registerSimpleFloatVariable("height_above_ground");
        registerSimpleFloatVariable("fluid_depth");
        registerSimpleFloatVariable("fluid_depth_down");
        registerSimpleFloatVariable("fluid_depth_up");
        registerSimpleFloatVariable("distance");
        registerSimpleFloatVariable("attack_time");
        registerSimpleFloatVariable("nan", () -> EmfRuntime.current().isValidationPhase() ? 0 : Float.NaN);

        // --- Simple boolean variables ---
        registerSimpleBoolVariable("is_hovered");
        registerSimpleBoolVariable("is_paused");
        registerSimpleBoolVariable("is_first_person_hand");
        registerSimpleBoolVariable("is_right_handed");
        registerSimpleBoolVariable("is_swinging_right_arm");
        registerSimpleBoolVariable("is_swinging_left_arm");
        registerSimpleBoolVariable("is_holding_item_right");
        registerSimpleBoolVariable("is_holding_item_left");
        registerSimpleBoolVariable("is_using_item");
        registerSimpleBoolVariable("is_swimming");
        registerSimpleBoolVariable("is_gliding");
        registerSimpleBoolVariable("is_blocking");
        registerSimpleBoolVariable("is_crawling");
        registerSimpleBoolVariable("is_climbing");
        registerSimpleBoolVariable("is_child");
        registerSimpleBoolVariable("is_in_water");
        registerSimpleBoolVariable("is_riding");
        registerSimpleBoolVariable("is_on_ground");
        registerSimpleBoolVariable("is_burning");
        registerSimpleBoolVariable("is_alive");
        registerSimpleBoolVariable("is_glowing");
        registerSimpleBoolVariable("is_aggressive");
        registerSimpleBoolVariable("is_hurt");
        registerSimpleBoolVariable("is_in_hand");
        registerSimpleBoolVariable("is_in_item_frame");
        registerSimpleBoolVariable("is_in_ground");
        registerSimpleBoolVariable("is_in_gui");
        registerSimpleBoolVariable("is_in_lava");
        registerSimpleBoolVariable("is_invisible");
        registerSimpleBoolVariable("is_on_head");
        registerSimpleBoolVariable("is_on_shoulder");
        registerSimpleBoolVariable("is_ridden");
        registerSimpleBoolVariable("is_sitting");
        registerSimpleBoolVariable("is_sneaking");
        registerSimpleBoolVariable("is_sprinting");
        registerSimpleBoolVariable("is_tamed");
        registerSimpleBoolVariable("is_wet");
        registerSimpleBoolVariable("is_jumping");

        // --- Context-dependent factories (order matters: longest/most-specific first) ---
        registerContextVariable(new ModelPartVariableFactory());
        registerContextVariable(new ModelVariableFactory());
        registerContextVariable(new RenderVariableFactory());
        registerContextVariable(new GlobalVariableFactory());

        // --- Common spelling-error warnings (silently returned as NaN) ---
        registerSpellingErrorWarning("is_agressive", "is_aggressive");
        registerSpellingErrorWarning("is_aggresive", "is_aggressive");
        registerSpellingErrorWarning("is_agresive", "is_aggressive");
        registerSpellingErrorWarning("is_riden", "is_ridden");
        registerSpellingErrorWarning("frame_count", "frame_counter");
    }

    private void explain(String... keys) {
        for (String key : keys) {
            singletonVariableExplanationTranslationKeys.put(key,
                    "entity_model_features.config.variable_explanation." + key);
        }
    }

    public static VariableRegistry getInstance() {
        return INSTANCE;
    }

    public Map<String, String> getSingletonVariableExplanationTranslationKeys() {
        return singletonVariableExplanationTranslationKeys;
    }

    public List<UniqueVariableFactory> getUniqueVariableFactories() {
        return uniqueVariableFactories;
    }

    public void registerContextVariable(UniqueVariableFactory factory) {
        if (factory == null) {
            EMFUtils.logWarn("Tried to register a null context variable factory");
            return;
        }
        if (uniqueVariableFactories.contains(factory)) {
            EMFUtils.logWarn("Tried to register a duplicate context variable factory: "
                    + factory.getClass().getName());
            return;
        }
        uniqueVariableFactories.add(factory);
    }

    private void registerSimpleFloatVariable(String variableName) {
        final String captured = variableName;
        registerSimpleFloatVariable(variableName, () -> EmfRuntime.current().context().getFloat(captured));
    }

    public void registerSimpleFloatVariable(String variableName, MathValue.ResultSupplier supplier) {
        if (singletonVariables.containsKey(variableName)) {
            EMFUtils.log("Duplicate variable: " + variableName + ". ignoring duplicate");
            return;
        }
        singletonVariables.put(variableName, new MathVariable(variableName, false, supplier));
        singletonVariables.put("-" + variableName, new MathVariable("-" + variableName, true, supplier));
        singletonVariableExplanationTranslationKeys.put(variableName,
                "entity_model_features.config.variable_explanation." + variableName);
    }

    private void registerSpellingErrorWarning(final String wrong, final String correct) {
        singletonVariables.put(wrong, new MathVariable(wrong, false, () -> {
            if (EMF.logModelCreationData) {
                EMFUtils.logError("Math spelling error: [" + wrong + "]. You probably meant: [" + correct + "].");
            }
            return Float.NaN;
        }));
    }

    private void registerSimpleBoolVariable(String variableName) {
        final String captured = variableName;
        registerSimpleBoolVariable(variableName, () -> EmfRuntime.current().context().getBoolean(captured));
    }

    public void registerSimpleBoolVariable(String variableName, BooleanSupplier boolGetter) {
        if (singletonVariables.containsKey(variableName)) {
            if (EMF.logModelCreationData) {
                EMFUtils.log("Duplicate variable: " + variableName + ". ignoring duplicate");
            }
            return;
        }
        singletonVariables.put(variableName, new MathVariable(variableName, () -> MathValue.fromBoolean(boolGetter)));
        singletonVariables.put("!" + variableName, new MathVariable("!" + variableName, () -> MathValue.invertBoolean(boolGetter)));
        singletonVariableExplanationTranslationKeys.put(variableName,
                "entity_model_features.config.variable_explanation." + variableName);
    }

    public MathComponent getVariable(String variableName, boolean isNegative, EmfParseContext parseCtx) {
        try {
            String variableWithNegative = isNegative ? "-" + variableName : variableName;
            if (singletonVariables.containsKey(variableWithNegative)) {
                return singletonVariables.get(variableWithNegative);
            }
            boolean invertBooleans = variableName.startsWith("!");
            String nameNoInvert = invertBooleans ? variableName.substring(1) : variableName;

            for (UniqueVariableFactory factory : uniqueVariableFactories) {
                if (factory.createsThisVariable(nameNoInvert)) {
                    MathValue.ResultSupplier supplier = factory.getSupplierOrNull(nameNoInvert, parseCtx);
                    if (supplier != null) {
                        return new MathVariable(variableName, isNegative,
                                invertBooleans ? () -> MathValue.invertBoolean(supplier) : supplier);
                    }
                }
            }

            if (EMF.logModelCreationData) {
                EMFUtils.logError("Variable [" + variableName + "] not found in animation ["
                        + parseCtx.animKey + "] of model [" + parseCtx.modelName
                        + "]. EMF will treat the variable as zero.");
            }
        } catch (Exception e) {
            if (EMF.logModelCreationData) {
                EMFUtils.logError("Error finding variable: [" + variableName + "] in animation ["
                        + parseCtx.animKey + "] of model [" + parseCtx.modelName
                        + "]. EMF will treat the variable as zero.");
            }
        }
        return variableName.startsWith("is_") ? MathConstant.FALSE_CONST : MathConstant.ZERO_CONST;
    }
}
