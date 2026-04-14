package com.ultra.megamod.lib.emf.animation.math;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.methods.MethodRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all function-call AST nodes ({@code sin(...)}, {@code if(...)}, etc.).
 * <p>
 * Concrete method implementations set {@link #supplier} via {@link #setSupplierAndOptimize}
 * which also attempts to replace the whole method with a {@link MathConstant} when every
 * argument is constant.
 * <p>
 * Ported 1:1 from upstream.
 */
public abstract class MathMethod extends MathValue {

    protected MathComponent optimizedAlternativeToThis = null;
    protected ResultSupplier supplier = null;

    protected MathMethod(boolean isNegative, EmfParseContext parseCtx, int argCount) throws EMFMathException {
        super(isNegative);
        if (!hasCorrectArgCount(argCount)) {
            throw new EMFMathException("ERROR: wrong number of arguments [" + argCount + "] in ["
                    + this.getClass().getSimpleName() + "] for [" + parseCtx.animKey + "] in ["
                    + parseCtx.modelName + "].");
        }
    }

    protected static MathComponent parseArg(String arg, EmfParseContext parseCtx) throws EMFMathException {
        if (arg == null || arg.isBlank()) {
            throw new EMFMathException("Method argument parsing error [" + arg + "] in ["
                    + parseCtx.animKey + "] in [" + parseCtx.modelName + "].");
        }
        MathComponent ret = MathExpressionParser.getOptimizedExpression(arg, false, parseCtx);
        if (ret == MathExpressionParser.NULL_EXPRESSION) {
            throw new EMFMathException("Method argument parsing null [" + arg + "] in ["
                    + parseCtx.animKey + "] in [" + parseCtx.modelName + "].");
        }
        return ret;
    }

    protected static List<MathComponent> parseAllArgs(List<String> args, EmfParseContext parseCtx) throws EMFMathException {
        if (args == null || args.isEmpty()) {
            throw new EMFMathException("Method argument parsing error [" + args + "] in ["
                    + parseCtx.animKey + "] in [" + parseCtx.modelName + "].");
        }
        List<MathComponent> expressionList = new ArrayList<>();
        for (String arg : args) {
            expressionList.add(parseArg(arg, parseCtx));
        }
        return expressionList;
    }

    private static MathMethod of(String methodNameIn, String args, boolean isNegative, EmfParseContext parseCtx) throws EMFMathException {
        boolean booleanInvert = methodNameIn.startsWith("!");
        String methodName = booleanInvert ? methodNameIn.substring(1) : methodNameIn;

        if (!MethodRegistry.getInstance().containsMethod(methodName)) {
            throw new EMFMathException("ERROR: Unknown method [" + methodName
                    + "], rejecting animation expression for [" + parseCtx.animKey + "].");
        }

        List<String> argsList = getArgsList(args);
        MathMethod method = MethodRegistry.getInstance().getMethodFactory(methodName)
                .getMethod(argsList, isNegative, parseCtx);
        if (booleanInvert) {
            method.invertSupplierBoolean();
        }
        return method;
    }

    /**
     * Splits {@code args} on commas, respecting nested parentheses and backslash escapes.
     * Called by the parser after stripping the outer brackets of a method call.
     */
    @NotNull
    private static List<String> getArgsList(final String args) {
        List<String> argsList = new ArrayList<>();
        int openBracketCount = 0;
        StringBuilder builder = new StringBuilder();

        Iterator<Character> charIterator = args.chars().mapToObj(c -> (char) c).iterator();
        char lastChar = '\0';

        while (charIterator.hasNext()) {
            char ch = charIterator.next();
            if (lastChar == '\\') {
                builder.append(ch);
                lastChar = '\0';
                continue;
            }
            if (ch == '(') {
                openBracketCount++;
            } else if (ch == ')') {
                openBracketCount--;
            } else if (ch == ',' && openBracketCount == 0) {
                argsList.add(builder.toString().trim());
                builder.setLength(0);
                continue;
            }
            builder.append(ch);
            lastChar = ch;
        }
        if (builder.length() > 0) {
            argsList.add(builder.toString().trim());
        }
        return argsList;
    }

    public static MathComponent getOptimizedExpression(String methodName, String args, boolean isNegative, EmfParseContext parseCtx) throws EMFMathException {
        if (methodName.startsWith("-")) {
            isNegative = true;
            methodName = methodName.substring(1);
        }
        MathMethod method = of(methodName, args, isNegative, parseCtx);
        return Objects.requireNonNullElse(method.optimizedAlternativeToThis, method);
    }

    protected void setOptimizedAlternativeToThis(final MathComponent optimizedAlternativeToThis) {
        this.optimizedAlternativeToThis = optimizedAlternativeToThis;
    }

    protected boolean canOptimizeForConstantArgs() {
        return true;
    }

    protected void setSupplierAndOptimize(ResultSupplier supplier) {
        this.supplier = supplier;
    }

    protected void setSupplierAndOptimize(ResultSupplier supplier, MathComponent arg) {
        this.supplier = supplier;
        setOptimizedIfPossible(supplier, List.of(arg));
    }

    protected void setSupplierAndOptimize(ResultSupplier supplier, List<MathComponent> allArgs) {
        this.supplier = supplier;
        setOptimizedIfPossible(supplier, allArgs);
    }

    protected abstract boolean hasCorrectArgCount(int argCount);

    private void invertSupplierBoolean() {
        if (optimizedAlternativeToThis == null) {
            final ResultSupplier currentSupplier = supplier;
            supplier = () -> MathValue.invertBoolean(currentSupplier);
        } else {
            optimizedAlternativeToThis = new MathConstant(
                    MathValue.invertBoolean(optimizedAlternativeToThis.getResult()), isNegative);
        }
    }

    protected void setOptimizedIfPossible(ResultSupplier supplier, List<MathComponent> allComponents) {
        if (!canOptimizeForConstantArgs() || allComponents.isEmpty()) return;

        boolean foundNonConstant = allComponents.stream().anyMatch(comp -> !comp.isConstant());
        if (!foundNonConstant) {
            float constantResult = supplier.get();
            if (!Float.isNaN(constantResult)) {
                optimizedAlternativeToThis = new MathConstant(constantResult, isNegative);
            }
        }
    }

    @Override
    protected ResultSupplier getResultSupplier() {
        return supplier;
    }
}
