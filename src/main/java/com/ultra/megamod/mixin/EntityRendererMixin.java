package com.ultra.megamod.mixin;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into EntityRenderer to implement:
 * - Nametags through walls: force shouldShowName to return true,
 *   bypassing distance and line-of-sight checks.
 *
 * This enhances the existing Nametags module which already applies
 * Glowing via server-side effects. With this mixin, nametag labels
 * render regardless of obstruction or distance.
 *
 * Only forces nametags for Player entities to stay compatible with
 * Entity Culling, which skips rendering for non-visible entities.
 * Forcing nametags on ALL entities would cause Entity Culling to
 * render names for culled mobs that shouldn't be visible.
 */
@Mixin(value = EntityRenderer.class, priority = 1100)
public class EntityRendererMixin {

    /**
     * Force nametag rendering for players when the nametagThroughWalls flag is set.
     * Vanilla checks distance, line of sight, and team visibility.
     * We bypass all of that for players only — non-player entities are left
     * to vanilla logic so Entity Culling can still skip them when occluded.
     */
    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void megamod$forceShowNametag(Entity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        if (AdminModuleState.nametagThroughWallsEnabled && entity instanceof Player) {
            cir.setReturnValue(true);
        }
    }
}
