package com.ultra.megamod.feature.casino.wheel.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Render state for the casino wheel BER.
 * Populated from WheelSyncPayload data on the client side.
 */
public class WheelRenderState extends BlockEntityRenderState {
    /** Current phase name: BETTING, SPINNING, RESULT, COOLDOWN */
    public String phase = "BETTING";
    /** Spin angle in degrees — the final target angle the wheel should reach */
    public float spinAngle = 0f;
    /** Current timer countdown for the active phase */
    public int timer = 0;
    /** Maximum timer value for the active phase (for progress calculation) */
    public int maxTimer = 600;
    /** Index of the winning segment, or -1 if none */
    public int resultIndex = -1;
    /** Colors for each of the 7 segments (ARGB int) */
    public int[] segmentColors = new int[0];
    /** Partial tick for interpolation */
    public float partialTick = 0f;
}
