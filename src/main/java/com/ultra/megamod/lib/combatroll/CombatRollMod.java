package com.ultra.megamod.lib.combatroll;

import com.ultra.megamod.lib.combatroll.api.CombatRoll;
import com.ultra.megamod.lib.combatroll.config.ServerConfig;
import com.ultra.megamod.lib.combatroll.network.ServerNetwork;

public class CombatRollMod {
    public static final String ID = CombatRoll.NAMESPACE;
    public static ServerConfig config = new ServerConfig();

    public static void init() {
        ServerNetwork.initializeHandlers();
    }
}
