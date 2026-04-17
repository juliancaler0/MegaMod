package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks;

import com.ultra.megamod.lib.pufferfish_additions.PUA;
import com.ultra.megamod.lib.pufferfish_additions.prototypes.CustomPrototypes;
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

public class SpellCastingExperienceSource implements ExperienceSource {
    private static final Identifier ID = PUA.location("spell_casting");
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
