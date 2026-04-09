package de.cadentem.pufferfish_unofficial_additions.compat.irons_spellbooks;

import de.cadentem.pufferfish_unofficial_additions.PUA;
import de.cadentem.pufferfish_unofficial_additions.prototypes.CustomPrototypes;
import net.minecraft.resources.ResourceLocation;
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

public class SpellCastingExperienceSource implements ExperienceSource {
    private static final ResourceLocation ID = PUA.location("spell_casting");
    private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

    static {
        PROTOTYPE.registerOperation(PUA.location("player"), BuiltinPrototypes.PLAYER, OperationFactory.create(Data::caster));
        PROTOTYPE.registerOperation(PUA.location("main_hand"), BuiltinPrototypes.ITEM_STACK, OperationFactory.create(Data::mainHand));
        PROTOTYPE.registerOperation(PUA.location("spellbook"), BuiltinPrototypes.ITEM_STACK, OperationFactory.create(Data::spellbook));
        PROTOTYPE.registerOperation(PUA.location("school"), ISPrototypes.SCHOOL, OperationFactory.create(Data::school));
        PROTOTYPE.registerOperation(PUA.location("spell"), ISPrototypes.SPELL, OperationFactory.create(Data::spell));

        PROTOTYPE.registerOperation(PUA.location("level"), BuiltinPrototypes.NUMBER, OperationFactory.create(Data::level));
        PROTOTYPE.registerOperation(PUA.location("min_level_rarity"), BuiltinPrototypes.NUMBER, OperationFactory.create(data -> (double) data.spell().value().getMinLevelForRarity(data.rarity())));

        PROTOTYPE.registerOperation(PUA.location("rarity_name"), CustomPrototypes.STRING, OperationFactory.create(data -> data.rarity().name()));
        PROTOTYPE.registerOperation(PUA.location("rarity"), BuiltinPrototypes.NUMBER, OperationFactory.create(data -> (double) data.rarity().ordinal()));

        PROTOTYPE.registerOperation(PUA.location("mana_cost"), BuiltinPrototypes.NUMBER, OperationFactory.create(Data::manaCost));
        PROTOTYPE.registerOperation(PUA.location("mana_cost_per_second"), BuiltinPrototypes.NUMBER, OperationFactory.create(Data::manaCostPerSecond));
        PROTOTYPE.registerOperation(PUA.location("cast_duration"), BuiltinPrototypes.NUMBER, OperationFactory.create(Data::castDuration));
        PROTOTYPE.registerOperation(PUA.location("cast_charge_time"), BuiltinPrototypes.NUMBER, OperationFactory.create(Data::castChargeTime));
        PROTOTYPE.registerOperation(PUA.location("cooldown"), BuiltinPrototypes.NUMBER, OperationFactory.create(Data::cooldown));
        PROTOTYPE.registerOperation(PUA.location("expected_ticks"), BuiltinPrototypes.NUMBER, OperationFactory.create(Data::expectedTicks));
    }

    private final Calculation<Data> calculation;

    private SpellCastingExperienceSource(final Calculation<Data> calculation) {
        this.calculation = calculation;
    }

    public static void register() {
        SkillsAPI.registerExperienceSource(ID, SpellCastingExperienceSource::parse);
    }

    private static Result<SpellCastingExperienceSource, Problem> parse(final ExperienceSourceConfigContext context) {
        return context.getData().andThen((rootElement -> LegacyCalculation.parse(rootElement, PROTOTYPE, context).mapSuccess(SpellCastingExperienceSource::new)));
    }

    public int getValue(final Data data) {
        return (int) Math.round(calculation.evaluate(data));
    }

    @Override
    public void dispose(final ExperienceSourceDisposeContext experienceSourceDisposeContext) { /* Nothing to do */ }
}