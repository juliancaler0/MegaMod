package com.ultra.megamod.lib.emf.mod_compat;

import com.ultra.megamod.lib.emf.utils.EMFEntity;

/**
 * Stubbed compat for the Fabric "player_animation_library" mod.
 *
 * Upstream EMF wires into PAL's {@code IAnimatedAvatar} accessor so that EMF
 * stops driving the player model while a PAL animation is active. MegaMod
 * does not ship the Fabric PAL — we have an internal {@code playeranim} lib
 * that exposes its own equivalent — so PAL probing is permanently disabled.
 *
 * The {@link com.ultra.megamod.lib.emf.EMF#init} call site is gated behind a
 * mod-loaded check on {@code player_animation_library}, which will always be
 * false on a NeoForge install, so this stub is never invoked at runtime. It
 * exists only to keep the {@code shouldPauseEntityAnim} method reference
 * resolvable at compile time.
 */
public class PALCompat {
    public static boolean shouldPauseEntityAnim(EMFEntity entity) {
        return false;
    }
}
