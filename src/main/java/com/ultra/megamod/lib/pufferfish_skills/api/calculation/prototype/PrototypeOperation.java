package com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.Operation;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.impl.calculation.prototype.PrototypeOperationImpl;

import java.util.Optional;

public interface PrototypeOperation<T, R> extends Operation<T, R> {
	static <U> PrototypeOperation<U, U> createIdentity(Prototype<U> prototype) {
		return new PrototypeOperationImpl<>(prototype, Optional::of);
	}

	Prototype<R> getReturnPrototype();

	<U> Optional<PrototypeOperation<T, U>> recoverReturnType(Prototype<U> prototype);

	Optional<Result<PrototypeOperation<T, ?>, Problem>> andThen(Identifier id, OperationConfigContext context);
}
