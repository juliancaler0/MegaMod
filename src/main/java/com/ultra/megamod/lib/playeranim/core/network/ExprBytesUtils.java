package com.ultra.megamod.lib.playeranim.core.network;

import com.ultra.megamod.lib.playeranim.core.molang.Expression;
import com.ultra.megamod.lib.playeranim.core.molang.FloatExpression;
import com.ultra.megamod.lib.playeranim.core.molang.MochaEngine;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stub for expression binary serialization.
 * Without Molang, all expressions are simple float constants.
 */
public final class ExprBytesUtils {
    private static final MochaEngine<Object> ENGINE = new MochaEngine<>();

    /**
     * Write a list of expressions to a byte buffer.
     * Since all expressions are float constants, we write the count followed by float values.
     */
    public static void writeExpressions(List<Expression> expressions, ByteBuf buf) {
        VarIntUtils.writeVarInt(buf, expressions.size());
        for (Expression expr : expressions) {
            buf.writeFloat(ENGINE.eval(expr));
        }
    }

    /**
     * Read a list of expressions from a byte buffer.
     */
    public static List<Expression> readExpressions(ByteBuf buf) {
        int count = VarIntUtils.readVarInt(buf);
        if (count == 0) return Collections.emptyList();
        if (count == 1) {
            return Collections.singletonList(FloatExpression.of(buf.readFloat()));
        }
        List<Expression> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(FloatExpression.of(buf.readFloat()));
        }
        return list;
    }

    private ExprBytesUtils() {}
}
