package com.ultra.megamod.lib.pufferfish_additions.misc;

import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.AbstractSpell;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SchoolRegistry;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SchoolType;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SpellRegistry;
import com.ultra.megamod.lib.pufferfish_skills.api.json.JsonElement;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Problem;
import com.ultra.megamod.lib.pufferfish_skills.api.util.Result;
import com.ultra.megamod.mixin.pufferfish_additions.puffish_skills.BuiltinJsonAccess;
import net.minecraft.core.HolderSet;

public class ExtendedJson {
    public static Result<HolderSet<AbstractSpell>, Problem> parseSpell(final JsonElement element) {
        return BuiltinJsonAccess.pufferfish_unofficial_additions$parseSomethingOrSomethingTag(element, SpellRegistry.REGISTRY, "spell");
    }

    public static Result<HolderSet<SchoolType>, Problem> parseSchool(final JsonElement element) {
        return BuiltinJsonAccess.pufferfish_unofficial_additions$parseSomethingOrSomethingTag(element, SchoolRegistry.REGISTRY, "school");
    }
}
