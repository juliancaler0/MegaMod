package com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks;

import com.ultra.megamod.lib.pufferfish_additions.PUA;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.AbstractSpell;
import com.ultra.megamod.lib.pufferfish_additions.compat.irons_spellbooks.stub.SchoolType;
import com.ultra.megamod.lib.pufferfish_additions.prototypes.CustomPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.operation.OperationFactory;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.BuiltinPrototypes;
import com.ultra.megamod.lib.pufferfish_skills.api.calculation.prototype.Prototype;
import net.minecraft.core.Holder;

public class ISPrototypes {
    public static final Prototype<Holder<AbstractSpell>> SPELL = Prototype.create(PUA.location("spell"));
    public static final Prototype<Holder<SchoolType>> SCHOOL = Prototype.create(PUA.location("school"));

    public static void register() {
        SPELL.registerOperation(PUA.location("min_level"), BuiltinPrototypes.NUMBER, OperationFactory.create(spell -> (double) spell.value().getMinLevel()));
        SPELL.registerOperation(PUA.location("max_level"), BuiltinPrototypes.NUMBER, OperationFactory.create(spell -> (double) spell.value().getMaxLevel()));
        SPELL.registerOperation(PUA.location("cast_type"), CustomPrototypes.STRING, OperationFactory.create(spell -> spell.value().getCastType().name()));
    }
}
