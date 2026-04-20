package com.ultra.megamod.lib.spellengine.client.input;


import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemUseAnimation;
import com.ultra.megamod.lib.spellengine.api.spell.Spell;
import com.ultra.megamod.lib.spellengine.api.spell.registry.SpellRegistry;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.client.gui.HudMessages;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import com.ultra.megamod.lib.spellengine.internals.container.SpellContainerSource;
import com.ultra.megamod.mixin.spellengine.client.control.KeybindingAccessor;
import com.ultra.megamod.lib.spellengine.network.Packets;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SpellHotbar {
    public static SpellHotbar INSTANCE = new SpellHotbar();

    public record Slot(Holder<Spell> spell, SpellCast.Mode castMode, @Nullable ItemStack itemStack, @Nullable WrappedKeybinding keybinding, @Nullable KeyMapping modifier) {
        @Nullable public KeyMapping getKeyMapping(Options options) {
            if (keybinding != null) {
                var unwrapped = keybinding.get(options);
                if (unwrapped != null) {
                    return unwrapped.keyBinding();
                }
            }
            return null;
        }
    }
    public List<Slot> slots = List.of();
    public StructuredSlots structuredSlots = new StructuredSlots(null, List.of());
    public record StructuredSlots(@Nullable Slot onUseKey, List<Slot> other) { }

    public boolean update(LocalPlayer player, Options options) {
        var changed = false;
        var initialSlotCount = slots.size();
        var mergedContainer = SpellContainerSource.activeContainerOf(player);
                //SpellContainerHelper.getAvailable(player);

        var slots = new ArrayList<Slot>();
        var otherSlots = new ArrayList<Slot>();
        Slot onUseKey = null;

        var allBindings = Keybindings.Wrapped.all();
        var useKey = ((KeybindingAccessor) options.keyUse).spellEngine_getBoundKey();
        var useKeyMapping = new WrappedKeybinding(options.keyUse, WrappedKeybinding.VanillaAlternative.USE_KEY);

        if (mergedContainer != null
                && !mergedContainer.spell_ids().isEmpty()) {
            var itemUseExpectation = expectedUseStack(player);
            // Don't create an ITEM_USE placeholder when the weapon is a spell weapon
            // (has SpellContainer via data component). The first resolved spell will
            // be bound to the use key directly at line 106-110 below because
            // onUseKey stays null here, which triggers the useKey override.
            // Only create the ITEM_USE placeholder for items that have a real vanilla
            // use animation (bows, shields, food) — those need ITEM_USE to let vanilla handle them.
            if (itemUseExpectation != null) {
                var stack = itemUseExpectation.itemStack;
                var hasSpellComponent = stack.has(com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents.SPELL_CONTAINER);
                if (!hasSpellComponent) {
                    // Real vanilla-use item (bow, shield, food) — create ITEM_USE placeholder
                    onUseKey = new Slot(null, SpellCast.Mode.ITEM_USE, stack, useKeyMapping, null);
                }
                // else: spell weapon — leave onUseKey null so spells bind to right-click
            }

            var spellIds = mergedContainer.spell_ids();
            var resolved = spellIds.stream()
                    .map(idString -> {
                        var id = Identifier.parse(idString);
                        return SpellRegistry.from(player.level()).get(id).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .toList();
            // R+scroll client selection: rotate the resolved list so the player-
            // selected spell becomes index 0 (the one bound to the use-key slot).
            int selected = SpellWeaponSelection.clampedIndex(resolved.size());
            List<Holder<Spell>> spellEntryList;
            if (selected == 0 || resolved.isEmpty()) {
                spellEntryList = List.copyOf(resolved);
            } else {
                var rotated = new ArrayList<Holder<Spell>>(resolved.size());
                rotated.addAll(resolved.subList(selected, resolved.size()));
                rotated.addAll(resolved.subList(0, selected));
                spellEntryList = rotated;
            }

            int keyBindingIndex = 0;
            for (Holder<Spell> spellEntry : spellEntryList) {
                var spell = spellEntry.value();
                if (spell == null) {
                    continue;
                }

                WrappedKeybinding keyBinding = null;
                if (keyBindingIndex < allBindings.size()) {
                    keyBinding = allBindings.get(keyBindingIndex);
                    keyBindingIndex += 1;
                } else {
                    continue;
                }

                // Override keybinding with UseKey if available
                if (SpellEngineClient.config.spellHotbarUseKey) {
                    if (onUseKey == null) {
                        keyBinding = useKeyMapping;
                    }
                }

                // Create slot
                var slot = new Slot(spellEntry, SpellCast.Mode.from(spell), null, keyBinding, null);

                // Try to categorize slot based on keybinding
                if (keyBinding != null) {
                    var unwrapped = keyBinding.get(options);
                    if (unwrapped != null) {
                        var hotbarKey = ((KeybindingAccessor) unwrapped.keyBinding()).spellEngine_getBoundKey();

                        if (hotbarKey.equals(useKey)) {
                            onUseKey = slot;
                        } else {
                            otherSlots.add(slot);
                        }
                    }
                }

                // Save to all slots
                slots.add(slot);
            }

            if (itemUseExpectation != null && onUseKey != null && !slots.contains(onUseKey)) {
                // Source always adds onUseKey here, but source always initializes it as a
                // separate ITEM_USE placeholder. Our port skips that placeholder for spell
                // weapons (the first spell takes the use key directly), so onUseKey is the
                // same Slot object already appended by the per-spell loop — adding it again
                // duplicates the icon. Guard against the dupe.
                if (itemUseExpectation.isMainHand()) {
                    slots.addFirst(onUseKey);
                } else {
                    slots.addLast(onUseKey);
                }
            }
        }

        changed = initialSlotCount != slots.size();
        this.structuredSlots = new StructuredSlots(onUseKey, otherSlots);
        this.slots = slots;
        return changed;
    }


    private @Nullable Handle handledThisTick = null;
    private @Nullable Handle lastPressed = null;
    private int itemUseCooldown = 0;
    public void prepare(int itemUseCooldown) {
        this.itemUseCooldown = itemUseCooldown;
        this.handledThisTick = null;
        if (lastPressed == null) {
            attemptedSpell = null;
        }
        this.updateDebounced();
    }

    @Nullable public Handle handleAll(LocalPlayer player, Options options, List<KeyMapping> exclude) {
        return handleSlotsInternal(player, this.slots, options, exclude);
    }

    @Nullable public Handle handleUseKey(LocalPlayer player, Options options) {
        return handleSlotsInternal(player, this.structuredSlots.onUseKey != null ? List.of(this.structuredSlots.onUseKey) : List.of(), options, List.of());
    }

    @Nullable public Handle handleOther(LocalPlayer player, Options options, List<KeyMapping> exclude) {
        return handleSlotsInternal(player, this.structuredSlots.other(), options, exclude);
    }

    @Nullable public Handle handleSome(LocalPlayer player, @Nullable Slot slot, Options options, List<KeyMapping> exclude) {
        if (slot == null) { return null; }
        return handleSlotsInternal(player, List.of(slot), options, exclude);
    }

    @Nullable public Handle lastHandled() {
        return handledThisTick;
    }

    public record Handle(Holder<Spell> spell, KeyMapping keyBinding, @Nullable WrappedKeybinding.Category category, SpellCast.Attempt attempt) {
        public static Handle from(Slot slot, KeyMapping keyBinding, @Nullable WrappedKeybinding.Category category) {
            return new Handle(slot.spell, keyBinding, category, null);
        }
        public Handle withAttempt(SpellCast.Attempt attempt) {
            return new Handle(this.spell, this.keyBinding, this.category, attempt);
        }
        public boolean isSuccessfulAttempt() {
            return attempt != null && attempt.isSuccess();
        }
        public boolean isUseKey(Options options) {
            return keyBinding == null ? false : keyBinding.equals(options.keyUse);
        }
    }

    @Nullable private Handle handleSlotsInternal(LocalPlayer player, List<Slot> slots, Options options, List<KeyMapping> exclude) {
        if (handledThisTick != null || player.isSpectator()) { return null; }
        if (Keybindings.bypass_spell_hotbar.isDown()
                || (SpellEngineClient.config.sneakingByPassSpellHotbar && options.keyShift.isDown())) {
            return null;
        }
//        if (itemUseCooldown > 0) {
//            return null;
//        }
        var caster = ((SpellCasterClient) player);
        var casted = caster.getSpellCastProgress();
        var casterStack = player.getMainHandItem();
        for(var slot: slots) {
            if (slot.keybinding != null) {
                var unwrapped = slot.keybinding.get(options);
                if (unwrapped == null) { continue; }
                var keyBinding = unwrapped.keyBinding();
                if (exclude.contains(keyBinding)) {
                    continue;
                }
                var pressed = keyBinding.isDown();
                var handle = Handle.from(slot, keyBinding, unwrapped.vanillaHandle());
                if (pressed) {
                    this.lastPressed = handle;
                }

                switch (slot.castMode()) {
                    case ITEM_USE -> {
                        if (options.keyUse.isDown()) {
                            return null;
                        }
                    }
                    case INSTANT -> {
                        if (pressed) {
                            var attempt = caster.startSpellCast(casterStack, slot.spell);
                            var handledWithAttempt = handle.withAttempt(attempt);
                            handledThisTick = handledWithAttempt;
                            displayAttempt(attempt, slot.spell);
                            return handledWithAttempt;
                        }
                    }
                    case CHARGE, CHANNEL -> {
                        if (casted != null && casted.process().id().equals(slot.spell.unwrapKey().get().identifier())) {
                            // The spell is already being casted
                            var needsToBeHeld = SpellHelper.isChanneled(casted.process().spell().value()) ?
                                    SpellEngineClient.config.holdToCastChannelled :
                                    SpellEngineClient.config.holdToCastCharged;
                            if (needsToBeHeld) {
                                if (!pressed) {
                                    caster.cancelSpellCast();
                                    handledThisTick = handle;
                                    return handle;
                                }
                            } else {
                                if (pressed && isReleased(keyBinding, UseCase.START)) {
                                    caster.cancelSpellCast();
                                    debounce(keyBinding, UseCase.STOP);
                                    handledThisTick = handle;
                                    return handle;
                                }
                            }
                        } else {
                            // A different spell or no spell is being casted
                            if (pressed && isReleased(keyBinding, UseCase.STOP)) {
                                var attempt = caster.startSpellCast(casterStack, slot.spell);
                                debounce(keyBinding, UseCase.START);
                                var handledWithAttempt = handle.withAttempt(attempt);
                                handledThisTick = handledWithAttempt;
                                displayAttempt(attempt, slot.spell);
                                return handledWithAttempt;
                            }
                        }
                    }
                }
                if (pressed) {
                    handledThisTick = handle;
                    return handle;
                }
            }
        }

        this.lastPressed = null;
        return null;
    }

    private Holder<Spell> attemptedSpell = null;
    private void displayAttempt(SpellCast.Attempt attempt, Holder<Spell> spell) {
        if (Objects.equals(spell, attemptedSpell)) {
            return;
        }
        if (attempt.isFail()) {
            HudMessages.INSTANCE.castAttemptError(attempt);
        }
        this.attemptedSpell = spell;
    }

    private Identifier lastSyncedSpellId = null;
    public void syncItemUseSkill(LocalPlayer player) {
        Identifier idToSync = null;
        if (!Objects.equals(idToSync, lastSyncedSpellId)) {
            // System.out.println("Syncing item use skill: " + idToSync);
            net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new Packets.SpellCastSync(idToSync, 1, 1000));
            lastSyncedSpellId = idToSync;
        }
    }

    private enum UseCase { START, STOP }
    private final HashMap<KeyMapping, UseCase> debounced = new HashMap<>();

    private boolean isReleased(KeyMapping keybinding, UseCase use) {
        return debounced.get(keybinding) != use;
    }

    private void debounce(KeyMapping keybinding, UseCase use) {
        debounced.put(keybinding, use);
    }

    private void updateDebounced() {
         debounced.entrySet().removeIf(entry -> !entry.getKey().isDown());
    }


    public record ItemUseExpectation(InteractionHand hand, ItemStack itemStack) {
        public boolean isMainHand() {
            return hand == InteractionHand.MAIN_HAND;
        }
    }

    public static ItemUseExpectation expectedUseStack(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.getUseAnimation() != ItemUseAnimation.NONE) {
                return new ItemUseExpectation(hand, itemStack);
            }
            // SpellEngine spell weapons don't override getUseAnimation (returns NONE)
            // because they must NOT enter vanilla "using" state (that would block
            // the SpellHotbar HEAD from processing further casts). Check for a
            // SpellContainer data component so the use-key slot populates and
            // right-click triggers the cast via handleUseKey.
            var container = itemStack.get(com.ultra.megamod.lib.spellengine.api.spell.SpellDataComponents.SPELL_CONTAINER);
            if (container != null && !container.spell_ids().isEmpty()) {
                return new ItemUseExpectation(hand, itemStack);
            }
        }
        return null;
    }

    public boolean isShowingItemUse() {
        return structuredSlots.onUseKey != null && structuredSlots.onUseKey.itemStack != null;
    }
}
