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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

public class HarvestExperienceSource implements ExperienceSource {
    private static final Identifier ID = PUA.location("harvest_crops");
    private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

    static {
        PROTOTYPE.registerOperation(PUA.location("player"), BuiltinPrototypes.PLAYER, OperationFactory.create(Data::player));
        PROTOTYPE.registerOperation(PUA.location("block"), BuiltinPrototypes.BLOCK_STATE, OperationFactory.create(Data::state));
        PROTOTYPE.registerOperation(PUA.location("tool"), BuiltinPrototypes.ITEM_STACK, OperationFactory.create(Data::tool));
        PROTOTYPE.registerOperation(PUA.location("dropped_seeds"), BuiltinPrototypes.NUMBER, OperationFactory.create(data -> (double) data.loot.stream().filter(item -> item.is(Tags.Items.SEEDS)).mapToInt(ItemStack::getCount).sum()));
        PROTOTYPE.registerOperation(PUA.location("dropped_crops"), BuiltinPrototypes.NUMBER, OperationFactory.create(data -> (double) data.loot.stream().filter(item -> item.is(Tags.Items.CROPS)).mapToInt(ItemStack::getCount).sum()));
    }

    private final Calculation<Data> calculation;

    private HarvestExperienceSource(final Calculation<Data> calculation) {
        this.calculation = calculation;
    }

    public static void register() {
        SkillsAPI.registerExperienceSource(ID, HarvestExperienceSource::parse);
    }

    private static Result<HarvestExperienceSource, Problem> parse(final ExperienceSourceConfigContext context) {
        return context.getData().andThen((rootElement -> LegacyCalculation.parse(rootElement, PROTOTYPE, context).mapSuccess(HarvestExperienceSource::new)));
    }

    private record Data(ServerPlayer player, BlockState state, ItemStack tool, List<ItemStack> loot) { }

    public int getValue(final ServerPlayer player, final BlockState state, final ItemStack tool, final List<ItemStack> generatedLoot) {
        return (int) calculation.evaluate(new Data(player, state, tool, generatedLoot));
    }

    @Override
    public void dispose(final ExperienceSourceDisposeContext experienceSourceDisposeContext) { /* Nothing to do */ }
}
