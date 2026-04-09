package de.cadentem.pufferfish_unofficial_additions.conditions;

import de.cadentem.pufferfish_unofficial_additions.prototypes.CustomPrototypes;
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
