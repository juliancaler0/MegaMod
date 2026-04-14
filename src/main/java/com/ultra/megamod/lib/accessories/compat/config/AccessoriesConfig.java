package com.ultra.megamod.lib.accessories.compat.config;

import com.ultra.megamod.lib.accessories.impl.PlayerEquipControl;
import com.ultra.megamod.lib.accessories.owo.config.Option;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Simplified replacement for the OWO-generated AccessoriesConfig wrapper.
 * Stores config values as static fields with getter/setter methods.
 */
public class AccessoriesConfig {

    public final ContentOptions contentOptions = new ContentOptions();
    public final ClientOptions clientOptions = new ClientOptions();
    public final ScreenOptions screenOptions = new ScreenOptions();
    public final Keys keys = new Keys();

    private final List<SlotAmountModifier> modifierList = new ArrayList<>();

    public List<SlotAmountModifier> modifiers() {
        return modifierList;
    }

    public static AccessoriesConfig createAndLoad(Consumer<SerializationBuilder> builderConsumer) {
        return new AccessoriesConfig();
    }

    public static class ContentOptions {
        private List<String> validGliderSlots = new ArrayList<>(List.of("cape", "back"));
        private boolean allowGliderEquip = false;
        private List<String> validTotemSlots = new ArrayList<>(List.of("charm"));
        private boolean allowTotemEquip = false;
        private List<String> validBannerSlots = new ArrayList<>(List.of("cape", "hat"));
        private boolean allowBannerEquip = true;

        public List<String> validGliderSlots() { return validGliderSlots; }
        public boolean allowGliderEquip() { return allowGliderEquip; }
        public void allowGliderEquip(boolean v) { allowGliderEquip = v; }
        public List<String> validTotemSlots() { return validTotemSlots; }
        public boolean allowTotemEquip() { return allowTotemEquip; }
        public void allowTotemEquip(boolean v) { allowTotemEquip = v; }
        public List<String> validBannerSlots() { return validBannerSlots; }
        public boolean allowBannerEquip() { return allowBannerEquip; }
        public void allowBannerEquip(boolean v) { allowBannerEquip = v; }

        public void subscribeToValidBannerSlots(Consumer<List<String>> c) {}
        public void subscribeToValidGliderSlots(Consumer<List<String>> c) {}
        public void subscribeToValidTotemSlots(Consumer<List<String>> c) {}
        public void subscribeToAllowBannerEquip(Consumer<Boolean> c) {}
        public void subscribeToAllowGliderEquip(Consumer<Boolean> c) {}
        public void subscribeToAllowTotemEquip(Consumer<Boolean> c) {}
    }

    public static class ClientOptions {
        private PlayerEquipControl equipControl = PlayerEquipControl.MUST_NOT_CROUCH;
        private boolean forceNullRenderReplacement = false;
        private boolean disableEmptySlotScreenError = false;
        private boolean showCosmeticAccessories = true;
        private List<DisabledDefaultRender> disabledDefaultRenders = new ArrayList<>();

        public PlayerEquipControl equipControl() { return equipControl; }
        public void equipControl(PlayerEquipControl v) { equipControl = v; }
        public boolean forceNullRenderReplacement() { return forceNullRenderReplacement; }
        public boolean disableEmptySlotScreenError() { return disableEmptySlotScreenError; }
        public boolean showCosmeticAccessories() { return showCosmeticAccessories; }
        public List<DisabledDefaultRender> disabledDefaultRenders() { return disabledDefaultRenders; }

        public void subscribeToEquipControl(Consumer<PlayerEquipControl> c) {}
    }

    public static class DisabledDefaultRender {
        public String slotType = "";
        public TargetType targetType = TargetType.ALL;

        public enum TargetType {
            ALL;
            public boolean isValid(net.minecraft.world.item.Item item) { return true; }
        }
    }

    public static class ScreenOptions {
        private boolean prioritizeCreativeScreen = false;
        private boolean keybindIgnoresOtherTargets = false;
        private boolean backButtonClosesScreen = false;
        private boolean showUnusedSlots = true;
        private boolean allowSlotScrolling = true;
        private boolean isDarkMode = false;
        private boolean showEquippedStackSlotType = true;
        private boolean entityLooksAtMouseCursor = false;
        private boolean alwaysShowCraftingGrid = false;
        private TooltipInfoType equipCheckTooltipType = TooltipInfoType.BASIC;
        private boolean showSlotDarkeningEffect = true;
        private com.ultra.megamod.lib.accessories.impl.option.AccessoriesPlayerOptionsHolder defaultValues = null;

        public final HoveredOptions hoveredOptions = new HoveredOptions();
        public final UnHoveredOptions unHoveredOptions = new UnHoveredOptions();

        public boolean prioritizeCreativeScreen() { return prioritizeCreativeScreen; }
        public boolean keybindIgnoresOtherTargets() { return keybindIgnoresOtherTargets; }
        public boolean backButtonClosesScreen() { return backButtonClosesScreen; }
        public boolean showUnusedSlots() { return showUnusedSlots; }
        public void showUnusedSlots(boolean v) { showUnusedSlots = v; }
        public boolean allowSlotScrolling() { return allowSlotScrolling; }
        public boolean isDarkMode() { return isDarkMode; }
        public void isDarkMode(boolean v) { isDarkMode = v; }
        public boolean showEquippedStackSlotType() { return showEquippedStackSlotType; }
        public void showEquippedStackSlotType(boolean v) { showEquippedStackSlotType = v; }
        public boolean entityLooksAtMouseCursor() { return entityLooksAtMouseCursor; }
        public void entityLooksAtMouseCursor(boolean v) { entityLooksAtMouseCursor = v; }
        public boolean alwaysShowCraftingGrid() { return alwaysShowCraftingGrid; }
        public void alwaysShowCraftingGrid(boolean v) { alwaysShowCraftingGrid = v; }
        public TooltipInfoType equipCheckTooltipType() { return equipCheckTooltipType; }
        public boolean showSlotDarkeningEffect() { return showSlotDarkeningEffect; }
        public com.ultra.megamod.lib.accessories.impl.option.AccessoriesPlayerOptionsHolder defaultValues() { return defaultValues; }

        public void subscribeToShowUnusedSlots(Consumer<Boolean> c) {}
        public void subscribeToShowUniqueSlots(Consumer<Boolean> c) {}
        public void subscribeToShowCraftingGrid(Consumer<Boolean> c) {}
        public void subscribeToAlwaysShowCraftingGrid(Consumer<Boolean> c) {}
        public void subscribeToIsDarkMode(Consumer<Boolean> c) {}
        public void subscribeToShowSlotDarkeningEffect(Consumer<Boolean> c) {}
        public void subscribeToEquipCheckTooltipType(Consumer<TooltipInfoType> c) {}
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void subscribeToMenuButtonInjections(Consumer c) {}

        public List<MenuButtonInjection> menuButtonInjections() {
            return List.of();
        }
    }

    public static class HoveredOptions {
        public boolean brightenHovered = true;
        public boolean cycleBrightness = true;
        public boolean line = false;
        public boolean clickbait = false;

        public boolean brightenHovered() { return brightenHovered; }
        public boolean cycleBrightness() { return cycleBrightness; }
        public boolean line() { return line; }
        public boolean clickbait() { return clickbait; }
    }

    public static class UnHoveredOptions {
        public boolean renderUnHovered = true;
        public boolean darkenUnHovered = true;
        public float darkenedBrightness = 0.5f;
        public float darkenedOpacity = 1f;

        public boolean renderUnHovered() { return renderUnHovered; }
        public boolean darkenUnHovered() { return darkenUnHovered; }
        public float darkenedBrightness() { return darkenedBrightness; }
        public float darkenedOpacity() { return darkenedOpacity; }
    }

    public static class Keys {
        public Option.Key contentOptions_validBannerSlots = new Option.Key("contentOptions.validBannerSlots");
        public Option.Key contentOptions_validGliderSlots = new Option.Key("contentOptions.validGliderSlots");
        public Option.Key contentOptions_validTotemSlots = new Option.Key("contentOptions.validTotemSlots");
        public Option.Key contentOptions_allowBannerEquip = new Option.Key("contentOptions.allowBannerEquip");
        public Option.Key contentOptions_allowGliderEquip = new Option.Key("contentOptions.allowGliderEquip");
        public Option.Key contentOptions_allowTotemEquip = new Option.Key("contentOptions.allowTotemEquip");
    }

    public void save() {
        // No-op in simplified config
    }

    public interface SerializationBuilder {
        default <T> void addEndec(Class<T> clazz, Object endec) {}
    }
}
