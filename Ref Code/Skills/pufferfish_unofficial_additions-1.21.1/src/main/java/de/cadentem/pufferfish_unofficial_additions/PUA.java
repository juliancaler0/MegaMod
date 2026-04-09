package de.cadentem.pufferfish_unofficial_additions;

import com.mojang.logging.LogUtils;
import de.cadentem.pufferfish_unofficial_additions.compat.irons_spellbooks.*;
import de.cadentem.pufferfish_unofficial_additions.conditions.StringCondition;
import de.cadentem.pufferfish_unofficial_additions.experience.FishingExperienceSource;
import de.cadentem.pufferfish_unofficial_additions.experience.HarvestExperienceSource;
import de.cadentem.pufferfish_unofficial_additions.rewards.EffectReward;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PUA.MODID)
public class PUA {
    public static final String MODID = "pufferfish_unofficial_additions";
    public static final Logger LOG = LogUtils.getLogger();

    public PUA() {
        StringCondition.register();
        HarvestExperienceSource.register();
        FishingExperienceSource.register();
        EffectReward.register();

        if (ModList.get().isLoaded("irons_spellbooks")) {
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, ISSEvents::grantSpellExperience);
            ISPrototypes.register();
            SpellCondition.register();
            SchoolCondition.register();
            SpellCastingExperienceSource.register();
        }
    }

    public static ResourceLocation location(final String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
