/**
 * Reliquary external-mod compat for the Accessories slot system.
 * <p>
 * MegaMod bundles its own source-level fork of Accessories under
 * {@code com.ultra.megamod.lib.accessories}, so this compat layer targets
 * the internal fork rather than the external {@code io.wispforest.accessories}
 * API. At runtime the compat is still gated behind
 * {@code ModList.isLoaded(Compatibility.ModIds.ACCESSORIES)} in
 * {@link com.ultra.megamod.reliquary.init.ModCompat}; when the fork is
 * mounted under that mod id (or when an external Accessories build is
 * installed alongside it) these registrations fire, otherwise they no-op.
 */
package com.ultra.megamod.reliquary.compat.accessories;
