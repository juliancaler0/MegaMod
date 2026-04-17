package com.ultra.megamod.lib.pufferfish_additions;

import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.ISPrototypes;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.ISSEvents;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.SchoolCondition;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.SpellCastingExperienceSource;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.SpellCondition;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SpellOnCastEvent;
import com.ultra.megamod.lib.pufferfish_additions.conditions.StringCondition;
import com.ultra.megamod.lib.pufferfish_additions.experience.FishingExperienceSource;
import com.ultra.megamod.lib.pufferfish_additions.experience.HarvestExperienceSource;
import com.ultra.megamod.lib.pufferfish_additions.rewards.EffectReward;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Port of {@code net.cadentem.pufferfish_unofficial_additions.PUA}.
 *
 * <p>Wires the custom experience sources ({@code harvest_crops}, {@code fishing}) and reward type
 * ({@code effect}) into the ported Pufferfish Skills API, plus the {@code StringCondition} operation.</p>
 *
 * <p>Iron's Spellbooks compat (spell casting experience source + spell/school conditions) is
 * registered only when {@code irons_spellbooks} is present on the runtime classpath. The
 * compat classes are still ported 1:1 and always on the classpath; the guard prevents their
 * static initializers (and the stub Iron's Spellbooks types they reference) from being loaded
 * when the mod is absent.</p>
 */
public final class PufferfishAdditionsMod {
    private PufferfishAdditionsMod() { }

    /**
     * Invoke from {@code MegaMod} main-mod construction, after the Pufferfish Skills framework
     * ({@code NeoForgeMain}) has been constructed so that the SkillsAPI registries exist.
     */
    public static void init(final IEventBus modEventBus) {
        // Core (always registered — no Iron's Spellbooks dependency)
        StringCondition.register();
        HarvestExperienceSource.register();
        FishingExperienceSource.register();
        EffectReward.register();

        PUA.LOG.info("pufferfish_unofficial_additions: core registrations complete "
                + "(StringCondition, HarvestExperienceSource, FishingExperienceSource, EffectReward)");

        // Iron's Spellbooks compat — only hook into its events when the mod is actually loaded.
        // Without the mod, the SpellOnCastEvent class (a runtime dependency) won't exist and
        // touching these classes would explode.
        if (ModList.get().isLoaded("irons_spellbooks")) {
            try {
                NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SpellOnCastEvent.class, ISSEvents::grantSpellExperience);
                ISPrototypes.register();
                SpellCondition.register();
                SchoolCondition.register();
                SpellCastingExperienceSource.register();
                PUA.LOG.info("pufferfish_unofficial_additions: Iron's Spellbooks compat registered");
            } catch (final Throwable t) {
                PUA.LOG.error("pufferfish_unofficial_additions: Iron's Spellbooks compat failed to register", t);
            }
        } else {
            PUA.LOG.info("pufferfish_unofficial_additions: Iron's Spellbooks not present, skipping compat");
        }
    }
}
