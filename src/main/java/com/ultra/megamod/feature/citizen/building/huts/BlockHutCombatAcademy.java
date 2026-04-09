package com.ultra.megamod.feature.citizen.building.huts;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;

/**
 * Hut block for the combat_academy building.
 */
public class BlockHutCombatAcademy extends AbstractBlockHut<BlockHutCombatAcademy> {

    public static final MapCodec<BlockHutCombatAcademy> CODEC = simpleCodec(BlockHutCombatAcademy::new);

    public BlockHutCombatAcademy(Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<BlockHutCombatAcademy> codec() {
        return CODEC;
    }

    @Override
    public String getBuildingId() {
        return "combat_academy";
    }
}
