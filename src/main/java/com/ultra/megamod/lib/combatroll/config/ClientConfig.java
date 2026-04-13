package com.ultra.megamod.lib.combatroll.config;

public class ClientConfig {
    public boolean playCooldownSound = true;
    public boolean playCooldownFlash = true;
    public boolean playRollSound = true;
    public int hudArrowColor = 0x5488e3;
    public int hudBackgroundOpacity = 75;
    public boolean showWhenFull = true;
    public boolean showHUDInCreative = false;
    public boolean showKeybinding = true;
    public enum LabelPosition { TOP, LEFT }
    public LabelPosition keybindingLabelPosition = LabelPosition.LEFT;
}
