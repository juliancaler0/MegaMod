/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.neoforged.api.distmarker.Dist
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.client.event.ClientTickEvent$Post
 *  net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
 */
package com.ultra.megamod.feature.skills.client;

import com.ultra.megamod.feature.relics.client.AccessoryKeybind;
import com.ultra.megamod.feature.skills.client.SkillTreeScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class SkillTreeKeybind {
    public static KeyMapping OPEN_SKILL_TREE;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_SKILL_TREE = new KeyMapping("key.megamod.skill_tree", 75, AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(OPEN_SKILL_TREE);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (OPEN_SKILL_TREE != null && OPEN_SKILL_TREE.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null) {
                mc.setScreen((Screen)new SkillTreeScreen());
            } else if (mc.screen instanceof SkillTreeScreen) {
                mc.setScreen(null);
            }
        }
    }
}

