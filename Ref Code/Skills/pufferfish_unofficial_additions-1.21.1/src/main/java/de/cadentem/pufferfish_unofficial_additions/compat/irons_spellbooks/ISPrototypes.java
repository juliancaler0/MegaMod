package de.cadentem.pufferfish_unofficial_additions.compat.irons_spellbooks;

import de.cadentem.pufferfish_unofficial_additions.PUA;
import de.cadentem.pufferfish_unofficial_additions.prototypes.CustomPrototypes;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.Holder;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;

public class ISPrototypes {
    public static final Prototype<Holder<AbstractSpell>> SPELL = Prototype.create(PUA.location("spell"));
    public static final Prototype<Holder<SchoolType>> SCHOOL = Prototype.create(PUA.location("school"));

    public static void register() {
        SPELL.registerOperation(PUA.location("min_level"), BuiltinPrototypes.NUMBER, OperationFactory.create(spell -> (double) spell.value().getMinLevel()));
        SPELL.registerOperation(PUA.location("max_level"), BuiltinPrototypes.NUMBER, OperationFactory.create(spell -> (double) spell.value().getMaxLevel()));
        SPELL.registerOperation(PUA.location("cast_type"), CustomPrototypes.STRING, OperationFactory.create(spell -> spell.value().getCastType().name()));
    }
}
