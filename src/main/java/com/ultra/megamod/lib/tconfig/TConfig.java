package com.ultra.megamod.lib.tconfig;

import net.minecraft.resources.Identifier;
import com.ultra.megamod.lib.tconfig.gui.entries.TConfigEntryCategory;

public abstract class TConfig {

    public abstract TConfigEntryCategory getGUIOptions();

    public abstract Identifier getModIcon();

    public boolean doesGUI() {
        return true;
    }


    public static class NoGUI extends TConfig {
        @Override
        public TConfigEntryCategory getGUIOptions() {
            return null;
        }

        @Override
        public Identifier getModIcon() {
            return null;
        }

        @Override
        public boolean doesGUI() {
            return false;
        }
    }

}
