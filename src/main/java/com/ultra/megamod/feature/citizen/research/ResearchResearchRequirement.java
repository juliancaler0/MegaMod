package com.ultra.megamod.feature.citizen.research;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Requires a specific parent research to be completed before the current
 * research can be started.
 */
public class ResearchResearchRequirement implements IResearchRequirement {

    private final Identifier requiredResearchId;

    public ResearchResearchRequirement(Identifier requiredResearchId) {
        this.requiredResearchId = requiredResearchId;
    }

    public Identifier getRequiredResearchId() {
        return requiredResearchId;
    }

    @Override
    public boolean isFulfilled(ResearchManager manager) {
        return manager.getLocalTree().isResearchComplete(requiredResearchId);
    }

    @Override
    public Component getDisplayText() {
        // Look up the research name from the global tree if available
        GlobalResearch research = GlobalResearchTree.INSTANCE.getResearch(requiredResearchId);
        String name = research != null ? research.getName() : requiredResearchId.getPath();
        return Component.literal("Research: " + name);
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("ReqType", "research");
        tag.putString("ResearchId", requiredResearchId.toString());
        return tag;
    }

    public static ResearchResearchRequirement fromNbt(CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr("ResearchId", "megamod:unknown"));
        if (id == null) id = Identifier.fromNamespaceAndPath("megamod", "unknown");
        return new ResearchResearchRequirement(id);
    }
}
