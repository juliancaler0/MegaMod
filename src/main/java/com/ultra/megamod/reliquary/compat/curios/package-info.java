/**
 * Reliquary ↔ Curios compat layer.
 *
 * <p>Targets Curios NeoForge 14.0.0+1.21.11 at compile time (declared as a
 * {@code compileOnly} dependency in {@code build.gradle}). At runtime this
 * package is only invoked after
 * {@code ModList.get().isLoaded(Compatibility.ModIds.CURIOS)} succeeds in
 * {@link com.ultra.megamod.reliquary.init.ModCompat}, so Curios does not
 * need to be installed for the main mod to run.
 */
package com.ultra.megamod.reliquary.compat.curios;
