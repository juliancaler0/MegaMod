package de.cadentem.pufferfish_unofficial_additions.experience;

import de.cadentem.pufferfish_unofficial_additions.PUA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceDisposeContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.calculation.LegacyCalculation;

import java.util.List;

public class HarvestExperienceSource implements ExperienceSource {
    private static final ResourceLocation ID = PUA.location("harvest_crops");
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
