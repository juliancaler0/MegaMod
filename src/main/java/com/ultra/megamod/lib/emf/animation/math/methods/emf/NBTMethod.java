package com.ultra.megamod.lib.emf.animation.math.methods.emf;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.EmfRuntime;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;

import java.util.List;

/**
 * {@code nbt(key, query)} — OptiFine-style NBT probe on the active entity.
 * <p>
 * Upstream calls straight into ETF's {@code NBTProperty} to parse the query. In Phase
 * D we defer the actual NBT fetch to {@link com.ultra.megamod.lib.emf.animation.EmfVariableContext#evaluateNbt(String)};
 * Phase E will plug in the ETF-backed implementation.
 */
public class NBTMethod extends MathMethod {

    public NBTMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        final String nbtKey = args.get(0);
        final String nbtQuery = args.get(1);
        final String combined = nbtKey + " " + nbtQuery;

        setSupplierAndOptimize(() -> EmfRuntime.current().context().evaluateNbt(combined));
    }

    @Override
    protected boolean canOptimizeForConstantArgs() {
        return false;
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount == 2;
    }
}
