package de.cadentem.pufferfish_unofficial_additions.compat.irons_spellbooks;

import de.cadentem.pufferfish_unofficial_additions.misc.ExtendedJson;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

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
