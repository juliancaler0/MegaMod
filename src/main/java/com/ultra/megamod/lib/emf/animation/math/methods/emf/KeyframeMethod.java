package com.ultra.megamod.lib.emf.animation.math.methods.emf;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code keyframe(delta, v0, v1, ..., vN)} — catmull-rom interpolation across a
 * fixed frame list with clamped-boundary behaviour. Ported 1:1.
 */
public class KeyframeMethod extends MathMethod {

    public KeyframeMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        List<MathComponent> parsedArgs = parseAllArgs(args, parseCtx);
        final MathComponent delta = parsedArgs.get(0);
        List<MathComponent> frames = new ArrayList<>(parsedArgs.subList(1, parsedArgs.size()));
        final MathComponent[] frameArray = frames.toArray(new MathComponent[0]);
        final int frameEnd = frameArray.length - 1;

        ResultSupplier mySupplier = () -> {
            float deltaRaw = delta.getResult();
            int deltaFloor = Mth.floor(deltaRaw);

            if (deltaFloor >= frameEnd) return frameArray[frameEnd].getResult();
            if (deltaFloor <= 0) return frameArray[0].getResult();

            MathComponent baseFrame = frameArray[Mth.clamp(deltaFloor, 0, frameEnd)];
            MathComponent beforeFrame = frameArray[Mth.clamp(deltaFloor - 1, 0, frameEnd)];
            MathComponent nextFrame = frameArray[Mth.clamp(deltaFloor + 1, 0, frameEnd)];
            MathComponent afterFrame = frameArray[Mth.clamp(deltaFloor + 2, 0, frameEnd)];

            float individualFrameDelta = Mth.frac(deltaRaw);
            return Mth.catmullrom(individualFrameDelta,
                    beforeFrame.getResult(), baseFrame.getResult(),
                    nextFrame.getResult(), afterFrame.getResult());
        };

        if (delta.isConstant()) {
            float deltaRaw = delta.getResult();
            int deltaFloor = Mth.floor(deltaRaw);

            if (deltaFloor >= frameEnd) {
                setOptimizedAlternativeToThis(frameArray[frameEnd]);
            } else if (deltaFloor <= 0) {
                setOptimizedAlternativeToThis(frameArray[0]);
            } else {
                MathComponent baseFrame = frameArray[Mth.clamp(deltaFloor, 0, frameEnd)];
                MathComponent beforeFrame = frameArray[Mth.clamp(deltaFloor - 1, 0, frameEnd)];
                MathComponent nextFrame = frameArray[Mth.clamp(deltaFloor + 1, 0, frameEnd)];
                MathComponent afterFrame = frameArray[Mth.clamp(deltaFloor + 2, 0, frameEnd)];

                float individualFrameDelta = Mth.frac(deltaRaw);
                setOptimizedAlternativeToThis(() -> Mth.catmullrom(individualFrameDelta,
                        beforeFrame.getResult(), baseFrame.getResult(),
                        nextFrame.getResult(), afterFrame.getResult()));
            }
        }

        setSupplierAndOptimize(mySupplier, parsedArgs);
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount >= 3;
    }
}
