package de.cadentem.pufferfish_unofficial_additions.compat.irons_spellbooks;

import de.cadentem.pufferfish_unofficial_additions.misc.ExtendedJson;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
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

public class SchoolCondition implements Operation<Holder<SchoolType>, Boolean> {
    private final HolderSet<SchoolType> schoolEntries;

    private SchoolCondition(final HolderSet<SchoolType> schoolEntries) {
        this.schoolEntries = schoolEntries;
    }

    public static void register() {
        ISPrototypes.SCHOOL.registerOperation(SkillsMod.createIdentifier("test"), BuiltinPrototypes.BOOLEAN, SchoolCondition::parse);
    }

    public static Result<SchoolCondition, Problem> parse(final OperationConfigContext context) {
        return context.getData().andThen(JsonElement::getAsObject).andThen(SchoolCondition::parse);
    }

    public static Result<SchoolCondition, Problem> parse(final JsonObject rootObject) {
        ArrayList<Problem> problems = new ArrayList<>();
        Optional<HolderSet<SchoolType>> optional = rootObject.get("school").andThen(ExtendedJson::parseSchool).ifFailure(problems::add).getSuccess();
        return problems.isEmpty() ? Result.success(new SchoolCondition(optional.orElseThrow())) : Result.failure(Problem.combine(problems));
    }

    @Override
    public Optional<Boolean> apply(final Holder<SchoolType> school) {
        return Optional.of(schoolEntries.contains(school));
    }
}
