package com.ultra.megamod.lib.emf.animation.math;

import com.ultra.megamod.lib.emf.EMF;
import com.ultra.megamod.lib.emf.EMFException;
import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.utils.EMFUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Single-pass expression parser + optimiser.
 * <p>
 * Ported 1:1 from {@code traben.entity_model_features.models.animation.math.MathExpressionParser}.
 * Operator precedence is fixed (multiplicative > additive > comparison > logical), matching
 * upstream. The returned {@link MathComponent} is fully constant-folded when possible.
 */
public class MathExpressionParser {

    /** Sentinel returned when parsing fails; calling {@link MathComponent#getResult()} yields NaN and logs. */
    public static final MathComponent NULL_EXPRESSION = new MathComponent() {
        @Override
        public float getResult() {
            EMFUtils.logError("ERROR: NULL_EXPRESSION was called, this should not happen.");
            return Float.NaN;
        }
    };

    private static final List<MathOperator> BOOLEAN_COMPARATOR_ACTIONS = List.of(
            MathOperator.EQUALS, MathOperator.SMALLER_THAN_OR_EQUALS, MathOperator.SMALLER_THAN,
            MathOperator.LARGER_THAN_OR_EQUALS, MathOperator.LARGER_THAN, MathOperator.NOT_EQUALS);
    private static final List<MathOperator> BOOLEAN_LOGICAL_ACTIONS = List.of(
            MathOperator.AND, MathOperator.OR);
    private static final List<MathOperator> MULTIPLICATION_ACTIONS = List.of(
            MathOperator.MULTIPLY, MathOperator.DIVIDE, MathOperator.DIVISION_REMAINDER);
    private static final List<MathOperator> ADDITION_ACTIONS = List.of(
            MathOperator.ADD, MathOperator.SUBTRACT);

    private final String originalExpression;
    private final boolean wasInvertedBooleanExpression;
    private final boolean isNegative;
    private final EmfParseContext parseCtx;
    private MathComponent optimizedComponent = null;
    private CalculationList components;
    private boolean nextValueIsNegative = false;
    private String caughtExceptionString = null;

    private MathExpressionParser(String expressionString, boolean isNegative, EmfParseContext parseCtx, boolean invertBoolean) {
        this.isNegative = isNegative;
        this.parseCtx = parseCtx;
        this.wasInvertedBooleanExpression = invertBoolean;

        expressionString = expressionString.trim();
        this.originalExpression = expressionString;

        components = new CalculationList();
        try {
            RollingReader rollingReader = new RollingReader();
            char[] chars = expressionString.toCharArray();
            int i = 0;

            Character firstBooleanChar = null;
            while (i < chars.length) {
                char currentChar = chars[i++];

                // ignore whitespace
                if (Character.isWhitespace(currentChar)) continue;

                MathOperator asAction = MathOperator.getAction(currentChar);

                // consume a possible second char for a two-char boolean op (==, !=, &&, ||, <=, >=)
                if (firstBooleanChar != null) {
                    if (asAction == MathOperator.BOOLEAN_CHAR) {
                        readDoubleBooleanAction(parseCtx, firstBooleanChar, currentChar);
                        if (i >= chars.length) {
                            throw new EMFMathException("ERROR: boolean operator [" + firstBooleanChar + currentChar
                                    + "] at end of expression for [" + parseCtx.animKey + "] in [" + parseCtx.modelName + "].");
                        }
                        currentChar = chars[i++];
                        asAction = MathOperator.getAction(currentChar);
                    } else {
                        // single-char boolean (!, =, &, |, <, >)
                        readLastSingleBooleanAction(parseCtx, firstBooleanChar, rollingReader);
                    }
                    firstBooleanChar = null;
                }

                if (asAction == MathOperator.BOOLEAN_CHAR) {
                    firstBooleanChar = currentChar;
                }
                // critical: no elif so the same char can progress normally too

                if (asAction == MathOperator.SUBTRACT &&
                        ((components.isEmpty() && rollingReader.isEmpty())
                                || (!components.isEmpty() && components.getLast() instanceof MathOperator && rollingReader.isEmpty()))) {
                    // unary minus
                    nextValueIsNegative = true;
                } else if (asAction == MathOperator.NONE) {
                    rollingReader.write(currentChar);
                } else {
                    if (asAction == MathOperator.OPEN_BRACKET) {
                        i = readMethodOrBrackets(rollingReader, chars, i);
                    } else {
                        readVariableOrConstant(rollingReader);
                        if (rollingReader.isEmpty() && asAction != MathOperator.BOOLEAN_CHAR) {
                            components.add(asAction);
                        }
                    }
                }
            }
            readVariableOrConstant(rollingReader);

            if (components.isEmpty()) {
                throw new EMFMathException("ERROR: math components found to be empty for [" + parseCtx.animKey
                        + "] in [" + parseCtx.modelName + "]");
            }

            // strip redundant '+' operators left behind by unary-minus handling
            CalculationList newComponents = new CalculationList();
            MathComponent lastComponent = null;
            for (MathComponent component : components) {
                if (lastComponent instanceof MathOperator && component instanceof MathOperator action) {
                    if (action != MathOperator.ADD) {
                        newComponents.add(component);
                    }
                } else {
                    newComponents.add(component);
                }
                lastComponent = component;
            }
            if (!newComponents.isEmpty() && newComponents.get(0) == MathOperator.ADD) {
                newComponents.remove(0);
            }
            if (newComponents.size() != components.size()) components = newComponents;

            validateAndOptimize();
        } catch (EMFMathException e) {
            caughtExceptionString = e.toString();
        } catch (Exception e) {
            caughtExceptionString = "EMF animation ERROR: for [" + parseCtx.animKey + "] in ["
                    + parseCtx.modelName + "] cause [" + e + "].";
        }
    }

    public static MathComponent getOptimizedExpression(String expressionString, boolean isNegative, EmfParseContext parseCtx) {
        return getOptimizedExpression(expressionString, isNegative, parseCtx, false);
    }

    private static MathComponent getOptimizedExpression(String expressionString, boolean isNegative, EmfParseContext parseCtx, boolean invertBoolean) {
        try {
            MathExpressionParser expression = new MathExpressionParser(expressionString, isNegative, parseCtx, invertBoolean);
            MathComponent optimized = expression.optimizedComponent;
            if (optimized == null) return NULL_EXPRESSION;

            if (expression.wasInvertedBooleanExpression) {
                return () -> MathValue.invertBoolean(optimized.getResult());
            }
            return optimized;
        } catch (Exception e) {
            EMFUtils.logError("EMF animation ERROR: for [" + parseCtx.animKey + "] in ["
                    + parseCtx.modelName + "] because [" + e + "].");
            return NULL_EXPRESSION;
        }
    }

    /**
     * Reads and returns the contents up to the matching close-bracket. Returns the index
     * after the closing bracket so the outer loop can resume there.
     */
    private static int readBracketContents(char[] chars, int start, StringBuilder bracketContents) {
        int nesting = 0;
        int i = start;
        while (i < chars.length) {
            char ch2 = chars[i++];
            if (ch2 == '(') {
                nesting++;
            } else if (ch2 == ')') {
                if (nesting == 0) return i;
                nesting--;
            }
            bracketContents.append(ch2);
        }
        return i;
    }

    private void readLastSingleBooleanAction(final EmfParseContext parseCtx, final Character firstBooleanChar,
                                             final RollingReader rollingReader) throws EMFMathException {
        if (firstBooleanChar == '!') {
            rollingReader.write('!');
        } else {
            components.add(switch (firstBooleanChar) {
                case '=' -> MathOperator.EQUALS;
                case '&' -> MathOperator.AND;
                case '|' -> MathOperator.OR;
                case '<' -> MathOperator.SMALLER_THAN;
                case '>' -> MathOperator.LARGER_THAN;
                default -> throw new EMFMathException("ERROR: with boolean processing for operator ["
                        + firstBooleanChar + "] for [" + parseCtx.animKey + "] in [" + parseCtx.modelName + "].");
            });
        }
    }

    private void readDoubleBooleanAction(final EmfParseContext parseCtx, final Character firstBooleanChar,
                                         final char currentChar) throws EMFMathException {
        MathOperator doubleAction = switch (firstBooleanChar + "" + currentChar) {
            case "==" -> MathOperator.EQUALS;
            case "!=" -> MathOperator.NOT_EQUALS;
            case "&&" -> MathOperator.AND;
            case "||" -> MathOperator.OR;
            case ">=" -> MathOperator.LARGER_THAN_OR_EQUALS;
            case "<=" -> MathOperator.SMALLER_THAN_OR_EQUALS;
            default -> throw new EMFMathException("ERROR: with boolean processing for operator ["
                    + firstBooleanChar + currentChar + "] for [" + parseCtx.animKey + "] in [" + parseCtx.modelName + "].");
        };
        components.add(doubleAction);
    }

    private int readMethodOrBrackets(final RollingReader rollingReader, final char[] chars, final int after) throws EMFMathException {
        String functionName = rollingReader.read();
        StringBuilder sb = new StringBuilder();
        int nextIdx = readBracketContents(chars, after, sb);
        String bracketContents = sb.toString();
        if (functionName.isEmpty() || "!".equals(functionName) || "-".equals(functionName)) {
            components.add(MathExpressionParser.getOptimizedExpression(bracketContents,
                    getNegativeNext() || "-".equals(functionName), this.parseCtx,
                    "!".equals(functionName)));
        } else {
            components.add(MathMethod.getOptimizedExpression(functionName, bracketContents,
                    getNegativeNext(), this.parseCtx));
        }
        return nextIdx;
    }

    private void readVariableOrConstant(final RollingReader rollingReader) throws EMFMathException {
        if (rollingReader.isEmpty()) return;

        String read = rollingReader.read();
        try {
            float number = Float.parseFloat(read);
            if (read.startsWith(".") && EMF.enforceOptiFineAnimSyntaxLimits) {
                throw new EMFMathException("ERROR: number [" + read + "] in expression [" + originalExpression
                        + "] for [" + parseCtx.animKey + "] in [" + parseCtx.modelName
                        + "] is not valid in OptiFine. It must not start with '.' please add a zero");
            }
            components.add(new MathConstant(number, getNegativeNext()));
        } catch (NumberFormatException ignored) {
            if (read.matches("^(\\d|_).*") && EMF.enforceOptiFineAnimSyntaxLimits) {
                throw new EMFMathException("ERROR: variable [" + read + "] in expression [" + originalExpression
                        + "] for [" + parseCtx.animKey + "] in [" + parseCtx.modelName
                        + "] is not valid in OptiFine. It must not start with '.' please add a zero");
            }
            components.add(MathVariable.getOptimizedVariable(read, getNegativeNext(), this.parseCtx));
        }
    }

    protected void validateAndOptimize() {
        if (caughtExceptionString != null) {
            EMFUtils.logWarn(caughtExceptionString);
            new EMFMathException(caughtExceptionString).record();
            return;
        }

        if (Float.isNaN(this.validateCalculationAndOptimize())) {
            EMFUtils.logWarn("result was NaN, expression not valid: " + originalExpression);
            new EMFMathException("result was NaN, expression not valid: " + originalExpression).record();
        }
    }

    private boolean getNegativeNext() {
        boolean neg = nextValueIsNegative;
        nextValueIsNegative = false;
        return neg;
    }

    private float validateCalculationAndOptimize() {
        if (components.size() == 1) {
            MathComponent comp = components.getLast();
            if (comp instanceof MathConstant constnt) {
                if (isNegative) comp = new MathConstant(-constnt.getResult());
            } else if (comp instanceof MathValue val) {
                val.isNegative = isNegative != val.isNegative;
            }
            optimizedComponent = comp;
            return comp.getResult();
        }

        try {
            // optimize expression into binary expression tree, following operator precedence
            CalculationList optimised =
                    optimiseTheseActionsIntoBinaryComponents(BOOLEAN_LOGICAL_ACTIONS,
                            optimiseTheseActionsIntoBinaryComponents(BOOLEAN_COMPARATOR_ACTIONS,
                                    optimiseTheseActionsIntoBinaryComponents(ADDITION_ACTIONS,
                                            optimiseTheseActionsIntoBinaryComponents(MULTIPLICATION_ACTIONS,
                                                    new CalculationList(components)))));

            if (optimised.size() == 1) {
                float result = optimised.getLast().getResult();
                if (Float.isNaN(result)) {
                    EMFUtils.logError(" result was NaN in [" + parseCtx.modelName
                            + "] for expression: " + originalExpression + " as " + components);
                } else {
                    optimizedComponent = optimised.getLast();
                    if (optimizedComponent instanceof MathValue value && this.isNegative) {
                        optimizedComponent = value.makeNegative();
                    }
                }
                return result;
            } else {
                EMFUtils.logError("ERROR: calculation did not result in 1 component, found: "
                        + optimised + " in [" + parseCtx.animKey + "] in [" + parseCtx.modelName + "].");
                EMFUtils.logError("\texpression was [" + originalExpression + "].");
            }
        } catch (Exception e) {
            String message = "EMF animation ERROR: expression error in [" + parseCtx.animKey + "] in ["
                    + parseCtx.modelName + "] caused by [" + e + "].";
            EMFUtils.logError(message);
            new EMFException(message).record();
        }
        return Float.NaN;
    }

    private CalculationList optimiseTheseActionsIntoBinaryComponents(List<MathOperator> actionsForThisPass, CalculationList componentsOptimized) {
        List<MathOperator> containedActions = actionsForThisPass.stream()
                .filter(componentsOptimized::contains)
                .toList();

        if (containedActions.isEmpty()) return componentsOptimized;

        CalculationList newComponents = new CalculationList();
        Iterator<MathComponent> compIterator = componentsOptimized.iterator();
        while (compIterator.hasNext()) {
            MathComponent component = compIterator.next();
            if (component instanceof MathOperator action && containedActions.contains(action)) {
                MathComponent last = newComponents.getLast();
                MathComponent next = compIterator.next();
                newComponents.remove(newComponents.size() - 1);
                newComponents.add(MathBinaryExpressionComponent.getOptimizedExpression(last, action, next));
            } else {
                newComponents.add(component);
            }
        }
        return newComponents;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (MathComponent comp : components) {
            builder.append(comp.toString()).append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    private static class RollingReader {
        private StringBuilder builder = new StringBuilder();

        void clear() {
            builder = new StringBuilder();
        }

        void write(char ch) {
            builder.append(ch);
        }

        String read() {
            String result = builder.toString();
            clear();
            return result;
        }

        @Override
        public String toString() {
            return builder.toString();
        }

        boolean isEmpty() {
            return builder.length() == 0;
        }
    }

    /** Extends {@link ArrayList} with a {@code getLast} helper; replaces upstream's fastutil variant. */
    private static class CalculationList extends ArrayList<MathComponent> {
        public CalculationList(CalculationList components) {
            super(components);
        }

        public CalculationList() {
        }

        public MathComponent getLast() {
            return get(size() - 1);
        }
    }
}
