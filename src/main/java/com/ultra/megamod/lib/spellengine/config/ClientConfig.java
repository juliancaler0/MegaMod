package com.ultra.megamod.lib.spellengine.config;




import com.ultra.megamod.lib.spellengine.client.input.WrappedKeybinding;


public class ClientConfig  {
    public boolean renderBeamsHighLuminance = true;
    public boolean holdToCastChannelled = true;
    public boolean holdToCastCharged = true;
    public boolean spellHotbarHidesOffhand = true;
    public boolean spellHotbarUseKey = true;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_1_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_1;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_2_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_2;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_3_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_3;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_4_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_4;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_5_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_5;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_6_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_6;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_7_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_7;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_8_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_8;
    public WrappedKeybinding.VanillaAlternative spell_hotbar_9_defer = WrappedKeybinding.VanillaAlternative.HOTBAR_KEY_9;

    public boolean sneakingByPassSpellHotbar = false;
    public boolean useKeyHighPriority = true;
    public boolean highlightTarget = true;
    public boolean filterInvalidTargets = true;
    public boolean alwaysShowFullTooltip = false;
    public boolean showSpellBookSuppportTooltip = true;
    public boolean showSpellBindingTooltip = true;
    public boolean showSpellCastErrors = true;
    public boolean shoulderSurfingAdaptiveWhileUse = true;
    public TriStateAuto firstPersonAnimations = TriStateAuto.AUTO;
}
