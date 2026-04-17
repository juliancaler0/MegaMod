package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks;

import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.AbstractSpell;
import com.ultra.megamod.lib.pufferfish_additions.misc.ExtendedJson;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

import java.util.ArrayList;
import java.util.Optional;

public class SpellCondition implements Operation<Holder<AbstractSpell>, Boolean> {
    private final HolderSet<AbstractSpell> spellEntries;

    private SpellCondition(final HolderSet<AbstractSpell> spellEntries) {
        this.spellEntries = spellEntries;
    }

    public static void register() {
        ISPrototypes.SPELL.registerOperation(SkillsMod.createIdentifier("test"), BuiltinPrototypes.BOOLEAN, SpellCondition::parse);
    }

    public static Result<SpellCondition, Problem> parse(final OperationConfigContext context) {
        return context.getData().andThen(JsonElement::getAsObject).andThen(SpellCondition::parse);
    }

    public static Result<SpellCondition, Problem> parse(final JsonObject rootObject) {
        ArrayList<Problem> problems = new ArrayList<>();
        Optional<HolderSet<AbstractSpell>> optional = rootObject.get("spell").andThen(ExtendedJson::parseSpell).ifFailure(problems::add).getSuccess();
        return problems.isEmpty() ? Result.success(new SpellCondition(optional.orElseThrow())) : Result.failure(Problem.combine(problems));
    }

    @Override
    public Optional<Boolean> apply(final Holder<AbstractSpell> spell) {
        return Optional.of(spellEntries.contains(spell));
    }
}
