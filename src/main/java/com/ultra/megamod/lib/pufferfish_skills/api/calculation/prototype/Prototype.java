package com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationConfigContext;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationFactory;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.lib.pufferfish_skills.impl.calculation.prototype.PrototypeImpl;

import java.util.Optional;

public interface Prototype<T> {
	static <T> Prototype<T> create(Identifier id) {
		return new PrototypeImpl<>(id);
	}

	Identifier getId();

	<R> void registerOperation(Identifier id, Prototype<R> prototype, OperationFactory<T, R> factory);

	Optional<Result<PrototypeOperation<T, ?>, Problem>> getOperation(Identifier id, OperationConfigContext context);
}
