package com.ultra.megamod.lib.emf.runtime;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Captures a PoseStack transform at a custom {@code (x, y, z)} offset for a later
 * attachment write.
 * <p>
 * OptiFine CEM models can declare {@code attachments: {"left_handheld_item": [x,y,z]}}
 * on a part. When the part is rendered, we push that offset onto the current PoseStack,
 * snapshot the resulting pose, and later use the snapshot to position the held item so
 * the pack's custom geometry (e.g. a custom arm rig) matches the intended hand location.
 * <p>
 * Ported 1:1 from upstream {@code EMFAttachments}.
 */
public final class EmfAttachment {

    public PoseStack.Pose pose = null;
    public final float x;
    public final float y;
    public final float z;
    public final boolean right;

    public EmfAttachment(float x, float y, float z, boolean right) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.right = right;
    }

    /**
     * Pushes the attachment offset on {@code stack}, snapshots the resulting pose, and
     * pops back. Call sites invoke this while rendering the owning part so {@link #pose}
     * reflects the world-space transform of the attachment point.
     */
    public void setAttachment(PoseStack stack) {
        stack.pushPose();
        stack.translate(x / 16f, y / 16f, z / 16f);
        this.pose = stack.last().copy();
        stack.popPose();
    }
}
