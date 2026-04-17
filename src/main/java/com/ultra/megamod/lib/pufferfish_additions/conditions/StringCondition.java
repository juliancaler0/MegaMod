package com.ultra.megamod.lib.pufferfish_additions.conditions;

import com.ultra.megamod.lib.pufferfish_additions.prototypes.CustomPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.SkillsMod;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonObject;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class StringCondition implements Operation<String, Boolean> {
    private final String string;

    private StringCondition(final String string) {
        this.string = string;
    }

    public static void register() {
        CustomPrototypes.STRING.registerOperation(SkillsMod.createIdentifier("test"), BuiltinPrototypes.BOOLEAN, StringCondition::parse);
    }

    public static Result<StringCondition, Problem> parse(final OperationConfigContext context) {
        return context.getData().andThen(JsonElement::getAsObject).andThen(StringCondition::parse);
    }

    public static Result<StringCondition, Problem> parse(final JsonObject rootObject) {
        ArrayList<Problem> problems = new ArrayList<>();
        Optional<String> optional = rootObject.getString("value").ifFailure(problems::add).getSuccess();
        return problems.isEmpty() ? Result.success(new StringCondition(optional.orElseThrow())) : Result.failure(Problem.combine(problems));
    }

    @Override
    public Optional<Boolean> apply(final String string) {
        return Optional.of(this.string.equals(string));
    }
}
