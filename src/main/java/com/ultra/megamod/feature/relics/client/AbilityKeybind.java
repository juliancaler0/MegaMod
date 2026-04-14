package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.combat.spell.SpellBookCastPayload;
import com.ultra.megamod.feature.combat.spell.client.SpellBookHudOverlay;
import com.ultra.megamod.feature.combat.spell.client.SpellBookSelection;
import com.ultra.megamod.feature.relics.RelicSpellAssignments;
import com.ultra.megamod.feature.relics.network.RelicSpellCastPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Unified ability keybind handler.
 *
 * <p>Phase H: weapon right-click casting, weapon ability scrolling, and the legacy
 * AbilityCastPayload path are gone — all weapon casts (tomes, Arsenal uniques,
 * wands, staves) now go through SpellEngine's own active-cast controller.
 *
 * R key (ABILITY_CAST):
 *   - Tap R = cast selected accessory/relic ability (via SpellEngine)
 *     or cycle the spell book if one is held in the offhand.
 *
 * G key (ACCESSORY_KEY):
 *   - Hold G + Scroll = cycle through equipped accessories (or spell book spells)
 *   - Tap G = flip between abilities on selected accessory
 */
@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class AbilityKeybind {
    public static KeyMapping ABILITY_CAST;     // R key (82)
    public static KeyMapping ACCESSORY_KEY;    // G key (71)

    // State tracking for hold+scroll vs tap detection
    private static boolean rWasDown = false;
    private static boolean gWasDown = false;
    private static boolean gScrolledThisCycle = false;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ABILITY_CAST = new KeyMapping("key.megamod.ability_cast", 82, AccessoryKeybind.MEGAMOD_CATEGORY);
        ACCESSORY_KEY = new KeyMapping("key.megamod.accessory_select", 71, AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(ABILITY_CAST);
        event.register(ACCESSORY_KEY);
    }

    // ============================
    // R KEY: Cast accessory ability
    // G KEY: Select / flip accessory abilities
    // ============================

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            rWasDown = false;
            gWasDown = false;
            return;
        }

        // Update UnifiedAbilityBar (held-item change detection)
        UnifiedAbilityBar.clientTick();

        // --- R key: cast accessory ability ---
        if (ABILITY_CAST != null) {
            boolean rDown = ABILITY_CAST.isDown();

            // Detect key press start via consumeClick (catches fast press+release in same tick)
            if (ABILITY_CAST.consumeClick()) {
                if (!rDown) {
                    // Pressed and released within the same tick — instant tap
                    sendCast();
                } else {
                    // Key is now held — track for release detection
                    rWasDown = true;
                }
            }

            // Detect release after hold
            if (rWasDown && !rDown) {
                sendCast();
                rWasDown = false;
            }

            // Consume any additional queued clicks
            while (ABILITY_CAST.consumeClick()) {}
        }

        // --- G key: accessory control / spell book cycle ---
        if (ACCESSORY_KEY != null) {
            boolean gDown = ACCESSORY_KEY.isDown();

            if (ACCESSORY_KEY.consumeClick()) {
                if (!gDown) {
                    // Instant tap — cycle spell if book active, otherwise flip accessory ability
                    if (SpellBookHudOverlay.isSpellBookActive()) {
                        int count = SpellBookHudOverlay.getSpellCount();
                        if (count > 0) SpellBookSelection.cycleNext(count);
                    } else {
                        UnifiedAbilityBar.onTapG();
                    }
                } else {
                    gWasDown = true;
                    gScrolledThisCycle = false;
                }
            }

            if (gWasDown && !gDown) {
                if (!gScrolledThisCycle) {
                    if (SpellBookHudOverlay.isSpellBookActive()) {
                        int count = SpellBookHudOverlay.getSpellCount();
                        if (count > 0) SpellBookSelection.cycleNext(count);
                    } else {
                        UnifiedAbilityBar.onTapG();
                    }
                }
                gWasDown = false;
                gScrolledThisCycle = false;
            }

            while (ACCESSORY_KEY.consumeClick()) {}
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        int direction = event.getScrollDeltaY() > 0 ? -1 : 1;

        // G held + scroll = cycle spells (if book active) or cycle accessories
        if (ACCESSORY_KEY != null && ACCESSORY_KEY.isDown()) {
            if (SpellBookHudOverlay.isSpellBookActive()) {
                int count = SpellBookHudOverlay.getSpellCount();
                if (count > 0) {
                    if (direction > 0) {
                        SpellBookSelection.cycleNext(count);
                    } else {
                        SpellBookSelection.cyclePrev(count);
                    }
                    gScrolledThisCycle = true;
                    event.setCanceled(true);
                }
            } else if (UnifiedAbilityBar.hasAccessoryAbilities()) {
                UnifiedAbilityBar.rebuildAccessories();
                UnifiedAbilityBar.onScrollWithG(direction);
                gScrolledThisCycle = true;
                event.setCanceled(true);
            }
        }
    }

    /**
     * Send relic ability cast to server via SpellEngine's RelicSpellCastPayload.
     * If a SpellBookItem is held in the offhand, R casts the selected spell from the book instead.
     */
    private static void sendCast() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Spell book takes priority when held in offhand
        if (SpellBookHudOverlay.isSpellBookActive()) {
            String spellId = SpellBookHudOverlay.getSelectedSpellId();
            String spellName = SpellBookHudOverlay.getSelectedSpellName();
            if (spellId != null) {
                SpellBookCastPayload payload = new SpellBookCastPayload(spellId);
                ClientPacketDistributor.sendToServer((CustomPacketPayload) payload, (CustomPacketPayload[]) new CustomPacketPayload[0]);
                if (spellName != null) {
                    AbilityHudOverlay.showCastNotification(spellName);
                }
                mc.player.swing(InteractionHand.MAIN_HAND);
                return;
            }
            mc.player.displayClientMessage(Component.literal("\u00a76[\u00a7eMegaMod\u00a76] \u00a77No spell selected."), true);
            return;
        }

        if (UnifiedAbilityBar.hasAccessoryAbilities()) {
            UnifiedAbilityBar.rebuildAccessories();
            UnifiedAbilityBar.AccessoryAbilityEntry selected = UnifiedAbilityBar.getSelectedAccessory();
            if (selected == null) {
                mc.player.displayClientMessage(Component.literal("\u00a76[\u00a7eMegaMod\u00a76] \u00a77No castable relics found."), true);
                return;
            }
            int abilityIdx = UnifiedAbilityBar.getSelectedAbilityIndex(selected.slotName());
            if (!selected.castableAbilities().isEmpty()) {
                String spellId = selected.castableAbilities().get(
                        Math.min(abilityIdx, selected.castableAbilities().size() - 1));
                RelicSpellCastPayload payload = new RelicSpellCastPayload(spellId);
                ClientPacketDistributor.sendToServer((CustomPacketPayload)payload, (CustomPacketPayload[])new CustomPacketPayload[0]);
                RelicSpellAssignments.SpellMeta meta = RelicSpellAssignments.metaFor(spellId);
                AbilityHudOverlay.showCastNotification(meta != null ? meta.abilityName() : spellId);
                mc.player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }

        mc.player.displayClientMessage(Component.literal("\u00a76[\u00a7eMegaMod\u00a76] \u00a77No relics equipped."), true);
    }
}
