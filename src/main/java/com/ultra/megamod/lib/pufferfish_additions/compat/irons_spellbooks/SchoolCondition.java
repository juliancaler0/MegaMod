package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks;

import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SchoolType;
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
