package com.ultra.megamod.lib.pufferfish_additions.experience;

import com.ultra.megamod.lib.pufferfish_additions.PUA;
import com.ultra.megamod.lib.pufferfish_skills.api.SkillsAPI;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.Calculation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationFactory;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.Prototype;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSource;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.experience.source.ExperienceSourceDisposeContext;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.calculation.LegacyCalculation;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FishingExperienceSource implements ExperienceSource {
    private static final Identifier ID = PUA.location("fishing");
    private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

    static {
        PROTOTYPE.registerOperation(PUA.location("player"), BuiltinPrototypes.PLAYER, OperationFactory.create(Data::player));
        PROTOTYPE.registerOperation(PUA.location("tool"), BuiltinPrototypes.ITEM_STACK, OperationFactory.create(Data::tool));
        PROTOTYPE.registerOperation(PUA.location("fished"), BuiltinPrototypes.ITEM_STACK, OperationFactory.create(Data::fishedItem));
    }

    private final Calculation<Data> calculation;

    private FishingExperienceSource(final Calculation<Data> calculation) {
        this.calculation = calculation;
    }

    public static void register() {
        SkillsAPI.registerExperienceSource(ID, FishingExperienceSource::parse);
    }

    private static Result<FishingExperienceSource, Problem> parse(final ExperienceSourceConfigContext context) {
        return context.getData().andThen(rootElement -> LegacyCalculation.parse(rootElement, PROTOTYPE, context).mapSuccess(FishingExperienceSource::new));
    }

    private record Data(ServerPlayer player, ItemStack tool, ItemStack fishedItem) { }

    public int getValue(final ServerPlayer player, final ItemStack tool, final ItemStack fishedItem) {
        return (int) Math.round(calculation.evaluate(new Data(player, tool, fishedItem)));
    }

    @Override
    public void dispose(final ExperienceSourceDisposeContext experienceSourceDisposeContext) { /* Nothing to do */ }
}
