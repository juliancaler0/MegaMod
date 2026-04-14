package com.ultra.megamod.lib.spellengine.client.input;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.InputConstants;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;

import java.util.ArrayList;
import java.util.List;

public class Keybindings {
    private static final KeyMapping.Category SPELL_CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("megamod", "spell_engine"));

    public static final List<KeyMapping> all() {
        return mutableAll;
    }
    public static final ArrayList<KeyMapping> mutableAll = new ArrayList<>();

    private static KeyMapping add(KeyMapping keyBinding) {
        mutableAll.add(keyBinding);
        return keyBinding;
    }

    private static KeyMapping hotbarKey(int number) {
        var key = new KeyMapping(
                "keybindings." + "megamod" + ".spell_hotbar_" + number,
                InputConstants.UNKNOWN.getValue(),
                SPELL_CATEGORY);
        add(key);
        return key;
    }

    public static KeyMapping bypass_spell_hotbar = add(new KeyMapping(
            "keybindings." + "megamod" + ".bypass_spell_hotbar",
            org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT,
            SPELL_CATEGORY));

    public static KeyMapping spell_hotbar_1 = hotbarKey(1);
    public static KeyMapping spell_hotbar_2 = hotbarKey(2);
    public static KeyMapping spell_hotbar_3 = hotbarKey(3);
    public static KeyMapping spell_hotbar_4 = hotbarKey(4);
    public static KeyMapping spell_hotbar_5 = hotbarKey(5);
    public static KeyMapping spell_hotbar_6 = hotbarKey(6);
    public static KeyMapping spell_hotbar_7 = hotbarKey(7);
    public static KeyMapping spell_hotbar_8 = hotbarKey(8);
    public static KeyMapping spell_hotbar_9 = hotbarKey(9);

    /**
     * NeoForge integration: register all SpellEngine keybindings on the
     * {@link net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent}.
     *
     * Call from MegaModClient via:
     *   modEventBus.addListener(Keybindings::onRegisterKeyMappings);
     */
    public static void onRegisterKeyMappings(
            net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        for (var keybinding : mutableAll) {
            event.register(keybinding);
        }
    }

    public static class Wrapped {
        public static List<WrappedKeybinding> all() {
            return List.of(
                    new WrappedKeybinding(Keybindings.spell_hotbar_1, SpellEngineClient.config.spell_hotbar_1_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_2, SpellEngineClient.config.spell_hotbar_2_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_3, SpellEngineClient.config.spell_hotbar_3_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_4, SpellEngineClient.config.spell_hotbar_4_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_5, SpellEngineClient.config.spell_hotbar_5_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_6, SpellEngineClient.config.spell_hotbar_6_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_7, SpellEngineClient.config.spell_hotbar_7_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_8, SpellEngineClient.config.spell_hotbar_8_defer),
                    new WrappedKeybinding(Keybindings.spell_hotbar_9, SpellEngineClient.config.spell_hotbar_9_defer)
            );
        }
    }
}
