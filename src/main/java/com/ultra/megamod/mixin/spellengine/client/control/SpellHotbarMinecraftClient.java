package com.ultra.megamod.mixin.spellengine.client.control;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Options;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Inventory;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.client.input.*;
import com.ultra.megamod.lib.spellengine.compat.CombatRollCompat;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of {@code net.spell_engine.mixin.client.control.SpellHotbarMinecraftClient}.
 *
 * <p>Source targeted Yarn's {@code handleInputEvents}. In NeoForge 1.21.11 the
 * equivalent method is {@code handleKeybinds} (see Minecraft.java:1886, called
 * from {@code runTick}). Using {@code tick} instead of {@code handleKeybinds}
 * broke the entire spell-cast input path: hotbar polling never ran alongside
 * the input dispatch, so pressing use/cast keys while holding a spell weapon
 * did nothing. We also split the original single mixin into two {@code tick}
 * hooks because the source used {@code handleInputEvents} HEAD + TAIL; this
 * port puts the HEAD / TAIL on {@code handleKeybinds} proper.</p>
 *
 * <p>{@code missTime} is used as the attack-pause cooldown because the
 * original {@code itemUseCooldown} field is not shadow-accessible under
 * 1.21.11 mappings; vanilla's attack-miss timer has identical tick semantics.</p>
 */
@Mixin(value = Minecraft.class, priority = 999)
public abstract class SpellHotbarMinecraftClient implements MinecraftClientExtension {
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Final public Options options;
    @Shadow protected int missTime;
    @Shadow @Nullable public Screen screen;

    // @Nullable private WrappedKeybinding.Category spellHotbarHandle = null;

    private boolean useKeySpellCastingLock = false;

    // Holds the list of keys that are currently being used for something else.
    private final List<KeyMapping> concurrentKeys = new ArrayList<>();

    @Inject(method = "handleKeybinds", at = @At(value = "HEAD"))
    private void handleKeybinds_HEAD_SpellHotbar(CallbackInfo ci) {
        if (player == null || options == null) { return; }

        var hotbarUpdated = SpellHotbar.INSTANCE.update(player, options);
        if (hotbarUpdated) {
            missTime = 4;
        }
        SpellHotbar.INSTANCE.prepare(missTime);

        if (player.isUsingItem()) {
            return;
        }
        var caster = (SpellCasterClient) player;
        if (caster.getCurrentSkillAttack() != null) {
            missTime = Math.max(missTime, 1);
            if (caster.getSpellCastProcess() == null) {
                return;
            }
        }

        concurrentKeys.removeIf(k -> !k.isDown());
        SpellHotbar.Handle handled;
        if (useKeySpellCastingLock || SpellEngineClient.config.useKeyHighPriority) {
            handled = SpellHotbar.INSTANCE.handleAll(player, options, concurrentKeys);
        } else {
            handled = SpellHotbar.INSTANCE.handleOther(player, options, concurrentKeys);
        }
        onSpellHotbarInputHandled(handled);
    }

    public void onSpellHotbarInputHandled(SpellHotbar.Handle handled) {
        if (handled != null) {
            // spellHotbarHandle = handled.category();
            if (player.isUsingItem()) {
                player.stopUsingItem();
                missTime = 1;
            }
            if ( (handled.isSuccessfulAttempt() || ((SpellCasterClient)player).isCastingSpell())
                    && handled.keyBinding() == options.keyUse) {
                useKeySpellCastingLock = true;
            }
        }
        if (useKeySpellCastingLock && !options.keyUse.isDown()) {
            useKeySpellCastingLock = false;
        }
        if (((SpellCasterClient)player).isCastingSpell()) {
            missTime = 2;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick_HEAD_SpellHotbar(CallbackInfo ci) {
        if (player == null || options == null) { return; }
        if (screen != null || CombatRollCompat.isRolling.apply(player)) {
            ((SpellCasterClient)player).cancelSpellCast();
        }
    }

    @Inject(method = "handleKeybinds", at = @At(value = "TAIL"))
    private void handleKeybinds_TAIL_SpellHotbar(CallbackInfo ci) {
        if (player == null || options == null) { return; }
        SpellHotbar.INSTANCE.syncItemUseSkill(player);
    }

    @WrapOperation(
            method = "handleKeybinds",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Inventory;selected:I", ordinal = 0, opcode = Opcodes.PUTFIELD),
            require = 0
    )
    private void selectSlot_Wrap(Inventory instance, int index, Operation<Void> original) {
        // FIX 1: Block hotbar scroll while R is held for spell cycling.
        // AbilityKeybind.onMouseScroll cancels InputEvent.MouseScrollingEvent, but by
        // the time that event fires the scroll delta has already been captured by
        // MouseHandler.onScroll and stored for handleKeybinds to process. This
        // WrapOperation is the last chance to prevent the inventory.selected write.
        if (com.ultra.megamod.feature.relics.client.AbilityKeybind.ABILITY_CAST != null
                && com.ultra.megamod.feature.relics.client.AbilityKeybind.ABILITY_CAST.isDown()) {
            var container = com.ultra.megamod.lib.spellengine.internals.container
                    .SpellContainerSource.activeContainerOf(player);
            if (container != null && container.spell_ids() != null && container.spell_ids().size() > 1) {
                // R is held and the weapon has multiple spells — suppress the hotbar slot change.
                return;
            }
        }

        var shouldControlSpellHotbar = false;
        if (!Keybindings.bypass_spell_hotbar.isDown()) {
            for (var slot: SpellHotbar.INSTANCE.slots) {
                var keyBinding = slot.getKeyMapping(options);
                if (options.keyHotbarSlots[index] == keyBinding) {
                    shouldControlSpellHotbar = true;
                    break;
                }
            }
        }

        if (shouldControlSpellHotbar) {
            // Do nothing
        } else {
            var trigger = this.options.keyHotbarSlots[index];
            concurrentKeys.add(trigger);
            original.call(instance, index);
        }
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void doItemUse_HEAD_autoSwap(CallbackInfo ci) {
        if (useKeySpellCastingLock
                || ((SpellCasterClient)player).isCastingSpell()) {
            ci.cancel();
            return;
        }

        // Auto swap right click is handled instead in ClientPlayerInteractionManagerMixin
        // to allow block interactions to be handled first
    }

    @Override public boolean isSpellCastLockActive() {
        return useKeySpellCastingLock;
    }
}
