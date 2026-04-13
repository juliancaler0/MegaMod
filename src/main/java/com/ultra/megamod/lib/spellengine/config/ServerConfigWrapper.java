package com.ultra.megamod.lib.spellengine.config;

import com.ultra.megamod.lib.spellengine.SpellEngineMod;

/**
 * Server config wrapper.
 * PartitioningSerializer.GlobalData from Cloth Config is not available in NeoForge.
 * This is a simple POJO config container.
 */
public class ServerConfigWrapper {
    public ServerConfig server = new ServerConfig();
}
