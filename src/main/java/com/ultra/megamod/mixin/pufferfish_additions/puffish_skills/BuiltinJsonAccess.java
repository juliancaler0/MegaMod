package com.ultra.megamod.mixin.pufferfish_additions.puffish_skills;

import com.ultra.megamod.lib.pufferfish_skills.api.json.BuiltinJson;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker mixin that exposes {@link BuiltinJson}'s private {@code parseSomethingOrSomethingTag}
 * helper so the ported {@code ExtendedJson} can reuse it verbatim (matches upstream pattern).
 */
@Mixin(value = BuiltinJson.class, remap = false)
public interface BuiltinJsonAccess {
    @Invoker("parseSomethingOrSomethingTag")
    static <T> Result<HolderSet<T>, Problem> pufferfish_unofficial_additions$parseSomethingOrSomethingTag(final JsonElement element, final Registry<T> registry, final String type) {
        throw new AssertionError();
    }
}
