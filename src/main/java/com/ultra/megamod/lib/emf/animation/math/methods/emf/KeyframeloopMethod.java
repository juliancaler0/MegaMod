package com.ultra.megamod.lib.emf.animation.math.methods.emf;

import com.ultra.megamod.lib.emf.animation.EmfParseContext;
import com.ultra.megamod.lib.emf.animation.math.EMFMathException;
import com.ultra.megamod.lib.emf.animation.math.MathComponent;
import com.ultra.megamod.lib.emf.animation.math.MathMethod;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code keyframeloop(delta, v0, v1, ..., vN)} — same as {@link KeyframeMethod} but
 * wraps the frame index instead of clamping. Ported 1:1.
 */
public class KeyframeloopMethod extends MathMethod {

    public KeyframeloopMethod(final List<String> args, final boolean isNegative, final EmfParseContext parseCtx) throws EMFMathException {
        super(isNegative, parseCtx, args.size());

        List<MathComponent> parsedArgs = parseAllArgs(args, parseCtx);
        final MathComponent delta = parsedArgs.get(0);
        List<MathComponent> frames = new ArrayList<>(parsedArgs.subList(1, parsedArgs.size()));
        final MathComponent[] frameArray = frames.toArray(new MathComponent[0]);
        final int frameCount = frameArray.length;

        ResultSupplier mySupplier = () -> {
            float deltaRaw = delta.getResult();
            int deltaFloor = Mth.floor(deltaRaw);

            MathComponent baseFrame = frameArray[(deltaFloor % frameCount + frameCount) % frameCount];
            MathComponent beforeFrame = frameArray[((deltaFloor - 1) % frameCount + frameCount) % frameCount];
            MathComponent nextFrame = frameArray[((deltaFloor + 1) % frameCount + frameCount) % frameCount];
            MathComponent afterFrame = frameArray[((deltaFloor + 2) % frameCount + frameCount) % frameCount];

            float individualFrameDelta = Mth.frac(deltaRaw);
            return Mth.catmullrom(individualFrameDelta,
                    beforeFrame.getResult(), baseFrame.getResult(),
                    nextFrame.getResult(), afterFrame.getResult());
        };

        if (delta.isConstant()) {
            float deltaRaw = delta.getResult();
            int deltaFloor = Mth.floor(deltaRaw);

            MathComponent baseFrame = frameArray[(deltaFloor % frameCount + frameCount) % frameCount];
            MathComponent beforeFrame = frameArray[((deltaFloor - 1) % frameCount + frameCount) % frameCount];
            MathComponent nextFrame = frameArray[((deltaFloor + 1) % frameCount + frameCount) % frameCount];
            MathComponent afterFrame = frameArray[((deltaFloor + 2) % frameCount + frameCount) % frameCount];

            float individualFrameDelta = Mth.frac(deltaRaw);
            setOptimizedAlternativeToThis(() -> Mth.catmullrom(individualFrameDelta,
                    beforeFrame.getResult(), baseFrame.getResult(),
                    nextFrame.getResult(), afterFrame.getResult()));
        }

        setSupplierAndOptimize(mySupplier, parsedArgs);
    }

    @Override
    protected boolean hasCorrectArgCount(final int argCount) {
        return argCount >= 3;
    }
}
