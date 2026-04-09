package de.cadentem.pufferfish_unofficial_additions.experience;

import de.cadentem.pufferfish_unofficial_additions.PUA;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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

public class FishingExperienceSource implements ExperienceSource {
    private static final ResourceLocation ID = PUA.location("fishing");
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
