package com.ultra.megamod.mixin.pufferfish_skills;

import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

/**
 * Spoofs {@link ModList#isLoaded(String)} to return true for the upstream mod IDs that MegaMod
 * has consolidated into its single jar under the {@code megamod} namespace. This lets the
 * Pufferfish Skills {@code skill_tree_rpgs} reference data pack resolve its per-node
 * {@code "required_mods": ["wizards"]} / {@code ["paladins"]} / etc. checks without polluting
 * the mods UI with 14 virtual sub-mod entries.
 *
 * <p>Each ID listed here is the original Fabric mod ID whose content was folded into MegaMod —
 * items, entities, attributes, data packs and assets live here even though the mod ID doesn't
 * have its own standalone {@code [[mods]]} entry in {@code neoforge.mods.toml}.
 */
@Mixin(ModList.class)
public abstract class ModListMixin {

    private static final Set<String> MEGAMOD_CONSOLIDATED_MOD_IDS = Set.of(
            "wizards",
            "paladins",
            "archers",
            "rogues",
            "arsenal",
            "jewelry",
            "runes",
            "relics_rpgs",
            "spell_engine",
            "spell_power",
            "ranged_weapon",
            "combat_roll",
            "critical_strike",
            "rpg_series"
    );

    @Inject(method = "isLoaded(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    private void megamod$spoofConsolidatedMods(String modId, CallbackInfoReturnable<Boolean> cir) {
        if (MEGAMOD_CONSOLIDATED_MOD_IDS.contains(modId)) {
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
