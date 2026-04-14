package com.ultra.megamod.lib.emf.animation.math.methods;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.emf.CatchMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.emf.IfBMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.emf.KeyframeMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.emf.KeyframeloopMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.emf.NBTMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.emf.RandomBMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.IfMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.InMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.MaxMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.MinMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.PrintBMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.PrintMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.optifine.RandomMethod;
import com.ultra.megamod.lib.emf.animation.math.methods.simple.BiFunctionMethods;
import com.ultra.megamod.lib.emf.animation.math.methods.simple.FunctionMethods;
import com.ultra.megamod.lib.emf.animation.math.methods.simple.MultiFunctionMethods;
import com.ultra.megamod.lib.emf.animation.math.methods.simple.TriFunction;
import com.ultra.megamod.lib.emf.animation.math.methods.simple.TriFunctionMethods;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.ultra.megamod.lib.emf.animation.math.MathValue.FALSE;
import static com.ultra.megamod.lib.emf.animation.math.MathValue.TRUE;

/**
 * Registry of all callable methods available to {@code .jem} animation expressions.
 * <p>
 * Ported from upstream with the full OptiFine + EMF method surface:
 * <ul>
 *     <li>Trigonometric: {@code sin, cos, tan, asin, acos, atan, atan2}</li>
 *     <li>Rounding / floor / abs / signum / frac / ceil / round</li>
 *     <li>Exponential / log / pow / sqrt / exp</li>
 *     <li>Conversion: {@code torad, todeg}, {@code wrapdeg, wraprad, degdiff, raddiff}</li>
 *     <li>Variadic: {@code max, min, if, ifb, in, between, equals}</li>
 *     <li>Interp: {@code clamp, lerp, fmod}, {@code catmullrom, quadbezier, cubicbezier, hermite}</li>
 *     <li>Easing (24 variants — see {@link TriFunctionMethods})</li>
 *     <li>Debug: {@code print, printb, catch}</li>
 *     <li>EMF extras: {@code random, randomb, nbt, keyframe, keyframeloop}</li>
 * </ul>
 */
public final class MethodRegistry {

    private static final MethodRegistry INSTANCE = new MethodRegistry();

    private final Map<String, MethodFactory> methodFactories = new HashMap<>();
    private final Map<String, String> methodExplanationTranslationKeys = new HashMap<>();

    private MethodRegistry() {
        // --- OptiFine-equivalent methods ---
        registerAndWrapMethodFactory("max", MaxMethod::new);
        registerAndWrapMethodFactory("min", MinMethod::new);
        registerAndWrapMethodFactory("random", RandomMethod::new);
        registerSimpleMethodFactory("sin", Mth::sin);
        registerSimpleMethodFactory("asin", (v) -> (float) Math.asin(v));
        registerSimpleMethodFactory("cos", Mth::cos);
        registerSimpleMethodFactory("acos", (v) -> (float) Math.acos(v));
        registerSimpleMethodFactory("tan", (v) -> (float) Math.tan(v));
        registerSimpleMethodFactory("atan", (v) -> (float) Math.atan(v));
        registerSimpleMethodFactory("abs", Mth::abs);
        registerSimpleMethodFactory("floor", (v) -> (float) Mth.floor(v));
        registerSimpleMethodFactory("ceil", (v) -> (float) Mth.ceil(v));
        registerSimpleMethodFactory("round", (v) -> (float) Math.round(v));
        registerSimpleMethodFactory("log", (v) -> v < 0 && EmfRuntime.current().isValidationPhase() ? 0 : (float) Math.log(v));
        registerSimpleMethodFactory("exp", (v) -> (float) Math.exp(v));
        registerSimpleMethodFactory("torad", (v) -> v * Mth.DEG_TO_RAD);
        registerSimpleMethodFactory("todeg", (v) -> v * Mth.RAD_TO_DEG);
        registerSimpleMethodFactory("frac", Mth::frac);
        registerSimpleMethodFactory("signum", Math::signum);
        registerSimpleMethodFactory("sqrt", (v) -> v < 0 && EmfRuntime.current().isValidationPhase() ? 0 : Mth.sqrt(v));
        registerSimpleMethodFactory("fmod", (v, w) -> (float) Math.floorMod((int) (float) v, (int) (float) w));
        registerSimpleMethodFactory("pow", (v, w) -> (float) Math.pow(v, w));
        registerSimpleMethodFactory("atan2", (v, w) -> (float) Mth.atan2(v, w));
        registerSimpleMethodFactory("clamp", Mth::clamp);
        registerSimpleMethodFactory("lerp", Mth::lerp);
        registerAndWrapMethodFactory("print", PrintMethod::new);
        registerAndWrapMethodFactory("printb", PrintBMethod::new);
        registerAndWrapMethodFactory("catch", CatchMethod::new);

        // --- Boolean helpers ---
        registerAndWrapMethodFactory("if", IfMethod::new);
        registerAndWrapMethodFactory("ifb", IfBMethod::new);
        registerAndWrapMethodFactory("randomb", RandomBMethod::new);
        registerAndWrapMethodFactory("in", InMethod::new);
        registerSimpleMethodFactory("between", (a, b, c) -> a > c ? FALSE : (a < b ? FALSE : TRUE));
        registerSimpleMethodFactory("equals", (x, y, epsilon) -> Math.abs(y - x) <= epsilon ? TRUE : FALSE);

        // --- EMF-specific ---
        registerAndWrapMethodFactory("nbt", NBTMethod::new);
        registerAndWrapMethodFactory("keyframe", KeyframeMethod::new);
        registerAndWrapMethodFactory("keyframeloop", KeyframeloopMethod::new);
        registerSimpleMethodFactory("wrapdeg", Mth::wrapDegrees);
        registerSimpleMethodFactory("wraprad", (v) -> (float) Math.toRadians(Mth.wrapDegrees(Math.toDegrees(v))));
        registerSimpleMethodFactory("degdiff", Mth::degreesDifferenceAbs);
        registerSimpleMethodFactory("raddiff", (v, w) -> (float) Math.toRadians(
                Mth.degreesDifferenceAbs((float) Math.toDegrees(v), (float) Math.toDegrees(w))));

        // --- Deprecated ease aliases (kept for pack compat) ---
        registerSimpleMethodFactory("easeinout", TriFunctionMethods::easeInOutSine);
        registerSimpleMethodFactory("easein", TriFunctionMethods::easeInSine);
        registerSimpleMethodFactory("easeout", TriFunctionMethods::easeOutSine);
        registerSimpleMethodFactory("cubiceaseinout", TriFunctionMethods::easeInOutCubic);
        registerSimpleMethodFactory("cubiceasein", TriFunctionMethods::easeInCubic);
        registerSimpleMethodFactory("cubiceaseout", TriFunctionMethods::easeOutCubic);

        // --- Splines ---
        registerSimpleMultiMethodFactory("catmullrom", (args) -> MultiFunctionMethods.catmullRom(
                args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));
        registerSimpleMultiMethodFactory("quadbezier", (args) -> MultiFunctionMethods.quadraticBezier(
                args.get(0), args.get(1), args.get(2), args.get(3)));
        registerSimpleMultiMethodFactory("cubicbezier", (args) -> MultiFunctionMethods.cubicBezier(
                args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));
        registerSimpleMultiMethodFactory("hermite", (args) -> MultiFunctionMethods.hermiteInterpolation(
                args.get(0), args.get(1), args.get(2), args.get(3), args.get(4)));

        // --- Full easing set (all 3-arg) ---
        registerSimpleMethodFactory("easeinoutexpo", TriFunctionMethods::easeInOutExpo);
        registerSimpleMethodFactory("easeinexpo", TriFunctionMethods::easeInExpo);
        registerSimpleMethodFactory("easeoutexpo", TriFunctionMethods::easeOutExpo);
        registerSimpleMethodFactory("easeinoutcirc", TriFunctionMethods::easeInOutCirc);
        registerSimpleMethodFactory("easeincirc", TriFunctionMethods::easeInCirc);
        registerSimpleMethodFactory("easeoutcirc", TriFunctionMethods::easeOutCirc);
        registerSimpleMethodFactory("easeinoutelastic", TriFunctionMethods::easeInOutElastic);
        registerSimpleMethodFactory("easeinelastic", TriFunctionMethods::easeInElastic);
        registerSimpleMethodFactory("easeoutelastic", TriFunctionMethods::easeOutElastic);
        registerSimpleMethodFactory("easeinoutback", TriFunctionMethods::easeInOutBack);
        registerSimpleMethodFactory("easeinback", TriFunctionMethods::easeInBack);
        registerSimpleMethodFactory("easeoutback", TriFunctionMethods::easeOutBack);
        registerSimpleMethodFactory("easeinoutbounce", TriFunctionMethods::easeInOutBounce);
        registerSimpleMethodFactory("easeinbounce", TriFunctionMethods::easeInBounce);
        registerSimpleMethodFactory("easeoutbounce", TriFunctionMethods::easeOutBounce);
        registerSimpleMethodFactory("easeinquad", TriFunctionMethods::easeInQuad);
        registerSimpleMethodFactory("easeoutquad", TriFunctionMethods::easeOutQuad);
        registerSimpleMethodFactory("easeinoutquad", TriFunctionMethods::easeInOutQuad);
        registerSimpleMethodFactory("easeincubic", TriFunctionMethods::easeInCubic);
        registerSimpleMethodFactory("easeoutcubic", TriFunctionMethods::easeOutCubic);
        registerSimpleMethodFactory("easeinoutcubic", TriFunctionMethods::easeInOutCubic);
        registerSimpleMethodFactory("easeinquart", TriFunctionMethods::easeInQuart);
        registerSimpleMethodFactory("easeoutquart", TriFunctionMethods::easeOutQuart);
        registerSimpleMethodFactory("easeinoutquart", TriFunctionMethods::easeInOutQuart);
        registerSimpleMethodFactory("easeinquint", TriFunctionMethods::easeInQuint);
        registerSimpleMethodFactory("easeoutquint", TriFunctionMethods::easeOutQuint);
        registerSimpleMethodFactory("easeinoutquint", TriFunctionMethods::easeInOutQuint);
        registerSimpleMethodFactory("easeinsine", TriFunctionMethods::easeInSine);
        registerSimpleMethodFactory("easeoutsine", TriFunctionMethods::easeOutSine);
        registerSimpleMethodFactory("easeinoutsine", TriFunctionMethods::easeInOutSine);
    }

    private static String emfTranslationKey(String key) {
        return "entity_model_features.config.function_explanation." + key;
    }

    public static MethodRegistry getInstance() {
        return INSTANCE;
    }

    public Map<String, String> getMethodExplanationTranslationKeys() {
        return methodExplanationTranslationKeys;
    }

    private void registerSimpleMethodFactory(String methodName, Function<Float, Float> function) {
        register(methodName, emfTranslationKey(methodName), FunctionMethods.makeFactory(methodName, function));
    }

    private void registerSimpleMethodFactory(String methodName, BiFunction<Float, Float, Float> function) {
        register(methodName, emfTranslationKey(methodName), BiFunctionMethods.makeFactory(methodName, function));
    }

    private void registerSimpleMethodFactory(String methodName, TriFunction<Float, Float, Float, Float> function) {
        register(methodName, emfTranslationKey(methodName), TriFunctionMethods.makeFactory(methodName, function));
    }

    private void registerSimpleMultiMethodFactory(String methodName, Function<List<Float>, Float> function) {
        register(methodName, emfTranslationKey(methodName), MultiFunctionMethods.makeFactory(methodName, function));
    }

    private void register(String methodName, String explanationTranslationKey, MethodFactory factory) {
        methodExplanationTranslationKeys.put(methodName, explanationTranslationKey);
        methodFactories.put(methodName, factory);
    }

    private void registerAndWrapMethodFactory(String methodName, MethodFactory factory) {
        final String displayName = methodName;
        register(methodName, emfTranslationKey(methodName), (a, b, c) -> {
            try {
                return factory.getMethod(a, b, c);
            } catch (Exception e) {
                throw new EMFMathException("Failed to create " + displayName + "() method, because: " + e);
            }
        });
    }

    // --- Phase F: public API surface for mod-registered methods ----------

    /**
     * Public mod-compat entry point for registering a single-argument animation function.
     * Used by {@code EMFApi.registerAnimationFunction}.
     */
    public void registerSimpleMethodFactory(String methodName, String explanationKey, Function<Float, Float> function) {
        register(methodName, explanationKey != null ? explanationKey : emfTranslationKey(methodName),
                FunctionMethods.makeFactory(methodName, function));
    }

    public void registerSimpleMethodFactory(String methodName, String explanationKey, BiFunction<Float, Float, Float> function) {
        register(methodName, explanationKey != null ? explanationKey : emfTranslationKey(methodName),
                BiFunctionMethods.makeFactory(methodName, function));
    }

    public void registerSimpleMethodFactory(String methodName, String explanationKey, TriFunction<Float, Float, Float, Float> function) {
        register(methodName, explanationKey != null ? explanationKey : emfTranslationKey(methodName),
                TriFunctionMethods.makeFactory(methodName, function));
    }

    public void registerSimpleMultiMethodFactory(String methodName, String explanationKey, Function<List<Float>, Float> function) {
        register(methodName, explanationKey != null ? explanationKey : emfTranslationKey(methodName),
                MultiFunctionMethods.makeFactory(methodName, function));
    }

    /** Public variant of the method-factory registrar for mod-compat code. */
    public void registerAndWrapMethodFactory(String methodName, String explanationKey, MethodFactory factory) {
        final String displayName = methodName;
        register(methodName, explanationKey != null ? explanationKey : emfTranslationKey(methodName), (a, b, c) -> {
            try {
                return factory.getMethod(a, b, c);
            } catch (Exception e) {
                throw new EMFMathException("Failed to create " + displayName + "() method, because: " + e);
            }
        });
    }

    public boolean containsMethod(String methodName) {
        return methodFactories.containsKey(methodName);
    }

    public MethodFactory getMethodFactory(String methodName) {
        return methodFactories.get(methodName);
    }

    /** Factory for producing a {@link MathMethod} instance given parsed args. */
    @FunctionalInterface
    public interface MethodFactory {
        MathMethod getMethod(List<String> args, boolean isNegative, EmfParseContext parseCtx) throws EMFMathException;
    }
}
