package shiroroku.theaurorian.Util;

import net.minecraft.util.Mth;

public class SimpleTimer {

    private final float Duration;
    private final boolean ShouldLoop;
    private float Tick;
    private boolean IsActive = false;

    public SimpleTimer(float pDuration) {
        this(pDuration, false);
    }

    public SimpleTimer(float pDuration, boolean pShouldLoop) {
        Tick = pDuration;
        Duration = pDuration;
        ShouldLoop = pShouldLoop;
    }

    public void start() {
        Tick = Duration;
        IsActive = true;
    }

    public void tick(float pTick) {
        if (IsActive) {
            if (Tick > 0) {
                Tick -= pTick;
            } else {
                IsActive = false;
                if (ShouldLoop) {
                    start();
                }
            }
        }
    }

    public void stop() {
        IsActive = false;
        Tick = Duration;
    }

    public boolean isActive() {
        return IsActive;
    }

    public float getPercentageProgress() {
        return Mth.clamp(1.0f - Tick / Duration, 0.0f, 1.0f);
    }

    public float getTicks() {
        return Tick;
    }

    public void setTick(float tick) {
        Tick = tick;
    }

}