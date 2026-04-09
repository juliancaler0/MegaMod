package com.ultra.megamod.feature.relics.client;

import com.ultra.megamod.feature.combat.spell.SpellBookCastPayload;
import com.ultra.megamod.feature.combat.spell.SpellBookItem;
import com.ultra.megamod.feature.combat.spell.client.SpellBookHudOverlay;
import com.ultra.megamod.feature.combat.spell.client.SpellBookSelection;
import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.AccessorySlotType;
import com.ultra.megamod.feature.relics.network.AbilityCastPayload;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponItem;
import com.ultra.megamod.feature.relics.weapons.RpgWeaponRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Unified ability keybind handler.
 *
 * RIGHT-CLICK: Cast selected ability on held weapon (RelicItem NONE / RpgWeaponItem)
 *
 * R key (ABILITY_CAST):
 *   - Hold R + Scroll = cycle through weapon abilities (visual selection for right-click)
 *   - Tap R = cast selected accessory/relic ability (never weapon — right-click handles weapons)
 *
 * G key (ACCESSORY_KEY):
 *   - Hold G + Scroll = cycle through equipped accessories
 *   - Tap G = flip between abilities on selected accessory
 */
@EventBusSubscriber(modid="megamod", value={Dist.CLIENT})
public class AbilityKeybind {
    public static KeyMapping ABILITY_CAST;     // R key (82)
    public static KeyMapping ACCESSORY_KEY;    // G key (71)

    // State tracking for hold+scroll vs tap detection
    private static boolean rWasDown = false;
    private static boolean gWasDown = false;
    private static boolean rScrolledThisCycle = false;
    private static boolean gScrolledThisCycle = false;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ABILITY_CAST = new KeyMapping("key.megamod.ability_cast", 82, AccessoryKeybind.MEGAMOD_CATEGORY);
        ACCESSORY_KEY = new KeyMapping("key.megamod.accessory_select", 71, AccessoryKeybind.MEGAMOD_CATEGORY);
        event.register(ABILITY_CAST);
        event.register(ACCESSORY_KEY);
    }

    // ============================
    // RIGHT-CLICK: Cast held weapon ability
    // ============================

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getEntity().level().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        ItemStack held = event.getItemStack();
        if (held.isEmpty()) return;

        // Only intercept for ability weapons
        if (!isAbilityWeapon(held.getItem())) return;
        if (!UnifiedAbilityBar.hasWeaponAbilities()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Send cast payload for the currently selected weapon ability
        int selectedIdx = UnifiedAbilityBar.getSelectedWeaponAbilityIndex();
        java.util.List<UnifiedAbilityBar.WeaponAbilityEntry> wpnEntries = UnifiedAbilityBar.getWeaponEntries();
        if (selectedIdx < wpnEntries.size()) {
            AbilityHudOverlay.showCastNotification(wpnEntries.get(selectedIdx).displayName());
        }
        AbilityCastPayload payload = new AbilityCastPayload("MAINHAND", "__byindex__", selectedIdx);
        ClientPacketDistributor.sendToServer((CustomPacketPayload)payload, (CustomPacketPayload[])new CustomPacketPayload[0]);

        // Cancel the event so the item's use() doesn't also fire
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static boolean isAbilityWeapon(Item item) {
        if (item instanceof RelicItem relicItem && relicItem.getSlotType() == AccessorySlotType.NONE) {
            return true;
        }
        if (item instanceof RpgWeaponItem) {
            return true;
        }
        return RpgWeaponRegistry.isRpgWeapon(BuiltInRegistries.ITEM.getKey(item).toString());
    }

    // ============================
    // R KEY: Cast / cycle weapon abilities
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

        // --- R key: cast weapon/accessory ability ---
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
                    rScrolledThisCycle = false;
                }
            }

            // Detect release after hold
            if (rWasDown && !rDown) {
                if (!rScrolledThisCycle) {
                    sendCast();
                }
                rWasDown = false;
                rScrolledThisCycle = false;
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

        // R held + scroll = cycle weapon abilities
        if (ABILITY_CAST != null && ABILITY_CAST.isDown()) {
            if (UnifiedAbilityBar.hasWeaponAbilities()) {
                UnifiedAbilityBar.onScrollWithR(direction);
                rScrolledThisCycle = true;
                event.setCanceled(true);
            }
            return;
        }

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
            return;
        }
    }

    /**
     * Send relic ability cast to server.
     * R key always casts relic/accessory abilities. Weapon casting is handled by right-click.
     * If a SpellBookItem is in the offhand, R casts the selected spell from the book instead.
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
            String abilityName = abilityIdx < selected.castableAbilities().size()
                    ? selected.castableAbilities().get(abilityIdx) : selected.relicName();
            // Send the resolved ability name directly so the server doesn't re-index
            // (client and server filter differently — client skips PASSIVE only,
            //  server also skips level-locked, causing index mismatch)
            AbilityCastPayload payload = new AbilityCastPayload(selected.slotName(), abilityName, abilityIdx);
            ClientPacketDistributor.sendToServer((CustomPacketPayload)payload, (CustomPacketPayload[])new CustomPacketPayload[0]);
            AbilityHudOverlay.showCastNotification(abilityName);
            mc.player.swing(InteractionHand.MAIN_HAND);
            return;
        }

        mc.player.displayClientMessage(Component.literal("\u00a76[\u00a7eMegaMod\u00a76] \u00a77No relics equipped."), true);
    }
}
