/**
 * Compile-time stubs mirroring the subset of Iron's Spellbooks API surface used by the ported
 * {@code pufferfish_unofficial_additions} compat module. None of the classes here are loaded at
 * runtime unless Iron's Spellbooks is installed — the {@link com.ultra.megamod.lib.pufferfish_additions.PufferfishAdditionsMod}
 * entry point guards class loading behind {@code ModList.get().isLoaded("irons_spellbooks")}.
 *
 * <p>When Iron's Spellbooks is present in the future these stubs should be replaced with direct
 * imports from {@code io.redspace.ironsspellbooks.*}. Until then they exist purely so the compat
 * code ports 1:1 without being deleted or structurally simplified.</p>
 */
package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub;
