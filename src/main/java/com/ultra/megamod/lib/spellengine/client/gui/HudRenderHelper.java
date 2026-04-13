package com.ultra.megamod.lib.spellengine.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.rendertype.RenderType;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.client.input.SpellHotbar;
import com.ultra.megamod.lib.spellengine.client.util.Rect;
import com.ultra.megamod.lib.spellengine.client.util.SpellRender;
import com.ultra.megamod.lib.spellengine.client.util.TextureFile;
import com.ultra.megamod.lib.spellengine.config.HudConfig;
import com.ultra.megamod.lib.spellengine.internals.SpellHelper;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterClient;
import com.ultra.megamod.mixin.spellengine.client.control.KeybindingAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HudRenderHelper {

    public static void render(GuiGraphics context, float tickDelta) {
        render(context, tickDelta, false);
    }

    public static void render(GuiGraphics context, float tickDelta, boolean config) {
        var hudConfig = SpellEngineClient.hudConfig.value;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if ((player == null || player.isSpectator())
                && !config) {
            return;
        }

        var clientConfig = SpellEngineClient.config;

        var targetViewModel = TargetWidget.ViewModel.mock();
        boolean renderHotbar = true;
        var hotbarViewModel = SpellHotBarWidget.ViewModel.mock();
        var errorViewModel = ErrorMessageWidget.ViewModel.mock();
        CastBarWidget.ViewModel castBarViewModel = null;
        if (config) {
            castBarViewModel = CastBarWidget.ViewModel.mock();
        } else {
            targetViewModel = TargetWidget.ViewModel.from(player);
        }

        if (player != null) {
            var caster = (SpellCasterClient) player;

            if (SpellHotbar.INSTANCE.slots.isEmpty()) {
                hotbarViewModel = SpellHotBarWidget.ViewModel.empty;
            } else {
                var cooldownManager = caster.getCooldownManager();
                var spells = SpellHotbar.INSTANCE.slots.stream().map(slot -> {
                    var spellEntry = slot.spell();
                    var id = spellEntry != null ? spellEntry.unwrapKey().get().identifier() : null;
                    var itemStack = slot.itemStack();
                    var useItem = itemStack != null;
                    var cooldownProgress = 0F;
                    if (useItem) {
                        cooldownProgress = player.getCooldowns().isOnCooldown(itemStack) ? 1.0F : 0.0F;
                    } else if (spellEntry != null) {
                        cooldownProgress = cooldownManager.getCooldownProgress(spellEntry, tickDelta);
                    }
                    return new SpellHotBarWidget.SpellViewModel(
                            useItem ? null : SpellRender.iconTexture(id),
                            useItem ? itemStack : null,
                            cooldownProgress,
                            SpellHotBarWidget.KeyBindingViewModel.from(slot.getKeyMapping(client.options)),
                            slot.modifier() != null ? SpellHotBarWidget.KeyBindingViewModel.from(slot.modifier()) : null);
                }).collect(Collectors.toList());
                hotbarViewModel = new SpellHotBarWidget.ViewModel(spells);
            }
            renderHotbar = true;

            var spellCast = caster.getSpellCastProgress();
            if (spellCast != null) {
                castBarViewModel = new CastBarWidget.ViewModel(
                        spellCast.process().spell().value().school.color,
                        spellCast.ratio(),
                        spellCast.process().length(),
                        SpellRender.iconTexture(spellCast.process().id()),
                        true,
                        SpellHelper.isChanneled(spellCast.process().spell().value())
                );
            }

            if (!config) {
                var hudMessages = HudMessages.INSTANCE;
                var error = hudMessages.currentError();
                if (error != null && error.durationLeft > 0) {
                    errorViewModel = ErrorMessageWidget.ViewModel.from(error.message, error.durationLeft, error.fadeOut, tickDelta);
                } else {
                    errorViewModel = null;
                }
            }
        }

        var screenWidth = client.getWindow().getGuiScaledWidth();
        var screenHeight = client.getWindow().getGuiScaledHeight();
        var originPoint = hudConfig.castbar.base.origin.getPoint(screenWidth, screenHeight);
        var baseOffset = originPoint.add(hudConfig.castbar.base.offset);
        if (castBarViewModel != null) {
            CastBarWidget.render(context, tickDelta, hudConfig, baseOffset, castBarViewModel);
        }

        if (hudConfig.castbar.target.visible) {
            var targetOffset = baseOffset.add(hudConfig.castbar.target.offset);
            TargetWidget.render(context, tickDelta, targetOffset, targetViewModel);
        }

        if (renderHotbar || config) {
            if (config && (hotbarViewModel == null || hotbarViewModel.isEmpty())) {
                hotbarViewModel = SpellHotBarWidget.ViewModel.mock();
            }
            SpellHotBarWidget.render(context, screenWidth, screenHeight, hotbarViewModel);
        }

        if (errorViewModel != null) {
            ErrorMessageWidget.render(context, hudConfig, screenWidth, screenHeight, errorViewModel);
        }
    }

    public static class TargetWidget {
        public static void render(GuiGraphics context, float tickDelta, Vec2 starting, ViewModel viewModel) {
            Minecraft client = Minecraft.getInstance();
            var font = client.gui.getFont();

            int textWidth = font.width(viewModel.text);

            int x = (int) (starting.x - (textWidth / 2F));
            int y = (int) starting.y;
            int opacity = 255;

            // RenderSystem.enableBlend(); // Removed in 1.21.11
            // RenderSystem.defaultBlendFunc(); // Removed in 1.21.11
            // setShaderColor removed in 1.21.11
            context.fill(x - 2, y - 2, x + textWidth + 2, y + font.lineHeight + 2, 0x80000000);
            context.drawString(font, viewModel.text, x, y, 0xFFFFFF);
            // RenderSystem.disableBlend(); // Removed in 1.21.11
            // setShaderColor removed in 1.21.11
        }

        public record ViewModel(String text) {
            public static ViewModel mock() {
                return new ViewModel("Target name");
            }

            public static ViewModel from(LocalPlayer player) {
                var caster = (SpellCasterClient)player;
                var target = caster.getCurrentFirstTarget();
                var text = "";
                if (target != null
                        && (/* SpellEngineClient.config.showTargetNameWhenMultiple || */ caster.getCurrentTargets().size() == 1)) {
                    text = target.getName().getString();
                }
                return new ViewModel(text);
            }
        }
    }

    public static class CastBarWidget {
        public static Rect lastRendered;
        private static final float tailWidth = 5;
        public static final float minWidth = 2 * tailWidth;
        private static final int textureWidth = 182;
        private static final int textureHeight = 10;
        private static final int barHeight = textureHeight / 2;
        private static final Identifier CAST_BAR = Identifier.fromNamespaceAndPath("megamod", "textures/hud/castbar.png");
        private static final int spellIconSize = 16;

        public record ViewModel(int color, float progress, float castDuration, Identifier iconTexture, boolean allowTickDelta, boolean reverse) {
            public static ViewModel mock() {
                return new ViewModel(0xFF3300, 0.5F, 1, SpellRender.iconTexture(Identifier.fromNamespaceAndPath("megamod", "dummy_spell")), false, false);
            }
        }

        public static void render(GuiGraphics context, float tickDelta, HudConfig hudConfig, Vec2 starting, ViewModel viewModel) {
            var barWidth = hudConfig.castbar.width;
            var totalWidth = barWidth + minWidth;
            var totalHeight = barHeight;
            int x = (int) (starting.x - (totalWidth / 2));
            int y = (int) (starting.y - (totalHeight / 2));
            lastRendered = new Rect(new Vec2(x,y), new Vec2(x + totalWidth,y + totalHeight));

            // RenderSystem.enableBlend(); // Removed in 1.21.11
            // RenderSystem.defaultBlendFunc(); // Removed in 1.21.11

            float red = ((float) ((viewModel.color >> 16) & 0xFF)) / 255F;
            float green = ((float) ((viewModel.color >> 8) & 0xFF)) / 255F;
            float blue = ((float) (viewModel.color & 0xFF)) / 255F;

            // setShaderColor removed in 1.21.11 - color is applied via tinting in blit

            renderBar(context, barWidth, true, 1, x, y);
            float partialProgress = 0;
            if (viewModel.allowTickDelta && viewModel.castDuration > 0) {
                partialProgress = tickDelta / viewModel.castDuration;
            }
            var progress = viewModel.reverse() ? (1F - viewModel.progress - partialProgress) : (viewModel.progress + partialProgress);
            renderBar(context, barWidth, false, progress, x, y);
            // setShaderColor removed in 1.21.11

            if (hudConfig.castbar.icon.visible && viewModel.iconTexture != null) {
                x = (int) (starting.x + hudConfig.castbar.icon.offset.x);
                y = (int) (starting.y + hudConfig.castbar.icon.offset.y);

                context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, viewModel.iconTexture, x, y, 0f, 0f, spellIconSize, spellIconSize, spellIconSize, spellIconSize);
            }

            // RenderSystem.disableBlend(); // Removed in 1.21.11
        }

        private static void renderBar(GuiGraphics context, int barWidth, boolean isBackground, float progress, int x, int y) {
            var totalWidth = barWidth + minWidth;
            var centerWidth = totalWidth - minWidth;
            float leftRenderBegin = 0;
            float centerRenderBegin = tailWidth;
            float rightRenderBegin = totalWidth - tailWidth;

            renderBarPart(context, isBackground, PART.LEFT, progress, leftRenderBegin, tailWidth, x, y, totalWidth);
            renderBarPart(context, isBackground, PART.CENTER, progress, centerRenderBegin, centerRenderBegin + centerWidth, x, y, totalWidth);
            renderBarPart(context, isBackground, PART.RIGHT, progress, rightRenderBegin, totalWidth, x, y, totalWidth);
        }

        enum PART { LEFT, CENTER, RIGHT }
        private static void renderBarPart(GuiGraphics context, boolean isBackground, PART part, float progress, float renderBegin, float renderEnd, int x, int y, float totalWidth) {
            var u = 0;
            var partMaxWidth = renderEnd - renderBegin; //5
            var progressRange = (renderEnd - renderBegin) / totalWidth; //0.05
            var progressFloor = (renderBegin / totalWidth); // 0
            var adjustedProgress = Math.min(Math.max((progress - progressFloor), 0), progressRange) / progressRange;
            var width = Math.round(adjustedProgress * partMaxWidth);
            switch (part) {
                case LEFT -> {
                    u = 0;
                    // System.out.println(" partMaxWidth: " + partMaxWidth + " progressRange: " + progressRange + " progressFloor: " + progressFloor + " adjustedProgress: " + adjustedProgress + " width: " + width);
//                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.F, 0F, 0F, 0.5F);
                }
                case CENTER -> {
                    u = (int) tailWidth;
//                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(0.F, 1F, 0F, 0.5F);
                }
                case RIGHT -> {
                    u = (int) (textureWidth - tailWidth);
//                    com.mojang.blaze3d.systems.RenderSystem.setShaderColor(0.F, 0F, 1F, 0.5F);
                }
            }
            int v = isBackground ? 0 : barHeight;
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, CAST_BAR, (int) (x + renderBegin), y, (float) u, (float) v, width, barHeight, textureWidth, textureHeight);
            // DrawableHelper.drawTexture(matrixStack, (int) (x + renderBegin), y, u, v, width, barHeight, textureWidth, textureHeight);
        }
    }

    public class SpellHotBarWidget {
        public static Rect lastRendered;
        private static final TextureFile HOTBAR = new TextureFile(Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar.png"), 182, 22);
        private static final int slotHeight = 22;
        private static final int slotWidth = 20;

        private static final Map<Integer, String> customHudKeyLabels = Map.of(
                org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT, "Al",
                org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT, "Al",
                org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT, "↑",
                org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT, "↑"
        );

        public record KeyBindingViewModel(String label, @Nullable Drawable.Component drawable) {
            public static KeyBindingViewModel from(@Nullable KeyMapping keyBinding) {
                if (keyBinding == null) {
                    return new KeyBindingViewModel("", null);
                }
                var boundKey = ((KeybindingAccessor)keyBinding).spellEngine_getBoundKey();
                var key = boundKey.toString();
                var drawable = HudKeyVisuals.custom.get(key);
                if (drawable != null) {
                    return new KeyBindingViewModel("", drawable);
                }
                var customLabel = customHudKeyLabels.get(boundKey.getValue());
                if (customLabel != null) {
                    return new KeyBindingViewModel(customLabel, null);
                }
                var label = keyBinding.getTranslatedKeyMessage()
                        .getString()
                        .toUpperCase(Locale.US);
                label = acronym(label, 3);
                return new KeyBindingViewModel(label, null);
            }

            public int width(Font font) {
                if (drawable != null) {
                    return drawable.draw().width();
                } else {
                    return font.width(label);
                }
            }
        }

        private static String acronym(String phrase, int maxLength) {
            StringBuilder result = new StringBuilder();
            for (String token : phrase.split("\\s+")) {
                result.append(token.toUpperCase().charAt(0));
            }
            var resultString = result.toString();
            // Make the result at most 3 characters long
            if (resultString.length() > maxLength) {
                resultString = resultString.substring(0, maxLength);
            }
            return result.toString();
        }

        public record SpellViewModel(@Nullable Identifier iconId, @Nullable ItemStack itemStack, float cooldown, KeyBindingViewModel keybinding, @Nullable KeyBindingViewModel modifier) { }

        public record ViewModel(List<SpellViewModel> spells) {
            public static ViewModel mock() {
                return new ViewModel(
                        List.of(
                                new SpellViewModel(SpellRender.iconTexture(Identifier.fromNamespaceAndPath("megamod", "dummy_spell")), null, 0, new KeyBindingViewModel("1", null), null),
                                new SpellViewModel(SpellRender.iconTexture(Identifier.fromNamespaceAndPath("megamod", "dummy_spell")), null, 0, new KeyBindingViewModel("2", null), null),
                                new SpellViewModel(SpellRender.iconTexture(Identifier.fromNamespaceAndPath("megamod", "dummy_spell")), null, 0, new KeyBindingViewModel("3", null), null)
                        )
                );
            }

            public static final ViewModel empty = new ViewModel(List.of());

            public boolean isEmpty() {
                return spells.isEmpty();
            }
        }

        public static void render(GuiGraphics context, int screenWidth, int screenHeight, ViewModel viewModel) {
            var config = SpellEngineClient.hudConfig.value.hotbar;
            Minecraft client = Minecraft.getInstance();
            var font = client.gui.getFont();
            if (viewModel.spells.isEmpty()) {
                return;
            }
            float estimatedWidth = slotWidth * viewModel.spells.size();
            float estimatedHeight = slotHeight;
            var origin = config.origin
                    .getPoint(screenWidth, screenHeight)
                    .add(config.offset)
                    .add(new Vec2(estimatedWidth * (-0.5F), estimatedHeight * (-0.5F))); // Grow from center
            lastRendered = new Rect(origin, origin.add(new Vec2(estimatedWidth, estimatedHeight)));

            // RenderSystem.enableBlend(); // Removed in 1.21.11
            // RenderSystem.defaultBlendFunc(); // Removed in 1.21.11

            // float barOpacity = (SpellEngineClient.config.indicateActiveHotbar && InputHelper.isLocked) ? 1F : 0.5F;
            float barOpacity = 1F;

            // Background
            // setShaderColor removed in 1.21.11
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, HOTBAR.id(), (int) (origin.x), (int) (origin.y), 0f, 0f, slotWidth / 2, slotHeight, HOTBAR.width(), HOTBAR.height());
            int middleElements = viewModel.spells.size() - 1;
            for (int i = 0; i < middleElements; i++) {
                context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, HOTBAR.id(), (int) (origin.x) + (slotWidth / 2) + (i * slotWidth), (int) (origin.y), (float)(slotWidth / 2), 0f, slotWidth, slotHeight, HOTBAR.width(), HOTBAR.height());
            }
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, HOTBAR.id(), (int) (origin.x) + (slotWidth / 2) + (middleElements * slotWidth), (int) (origin.y), 170f, 0f, (slotHeight / 2) + 1, slotHeight, HOTBAR.width(), HOTBAR.height());

            // Icons
            // setShaderColor removed in 1.21.11
            var iconsOffset = new Vec2(3,3);
            int iconSize = 16;
            for (int i = 0; i < viewModel.spells.size(); i++) {
                var spell = viewModel.spells.get(i);
                int x = (int) (origin.x + iconsOffset.x) + ((slotWidth) * i);
                int y = (int) (origin.y + iconsOffset.y);


                // RenderSystem.enableBlend(); // Removed in 1.21.11

                // Icon
                if (spell.iconId != null) {
                    context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, spell.iconId, x, y, 0f, 0f, iconSize, iconSize, iconSize, iconSize);
                } else if (spell.itemStack != null) {
                    context.renderItem(spell.itemStack, x, y);
                }

                // Cooldown
                if (spell.cooldown > 0) {
                    renderCooldown(context, spell.cooldown, x, y);
                }

                // Keybinding
                if (spell.keybinding() != null) {
                    var keybindingX = x + (iconSize / 2);
                    var keybindingY = (int)origin.y + 2;
                    if (spell.modifier != null) {
                        keybindingX += 2; // Shifting to the right, because this will likely be the last
                        var spacing = 1;
                        var modifierWidth = spell.modifier().width(font);
                        var keybindingWidth = spell.keybinding().width(font);
                        var totalWidth = modifierWidth + keybindingWidth;

                        keybindingX -= (totalWidth / 2);
                        drawKeybinding(context, font, spell.modifier, keybindingX, keybindingY, Drawable.Anchor.LEADING, Drawable.Anchor.TRAILING);
                        keybindingX += modifierWidth + spacing;
                        drawKeybinding(context, font, spell.keybinding(), keybindingX, keybindingY, Drawable.Anchor.LEADING, Drawable.Anchor.TRAILING);
                    } else {
                        drawKeybinding(context, font, spell.keybinding(), keybindingX, keybindingY, Drawable.Anchor.CENTER, Drawable.Anchor.TRAILING);
                    }
                }
            }

            // RenderSystem.disableBlend(); // Removed in 1.21.11
            // setShaderColor removed in 1.21.11
        }

        private static void drawKeybinding(GuiGraphics context, Font font, KeyBindingViewModel keybinding, int x, int y,
                                           Drawable.Anchor horizontalAnchor, Drawable.Anchor verticalAnchor) {
            if (keybinding.drawable != null) {
                keybinding.drawable.draw(context, x, y, horizontalAnchor, verticalAnchor);
            } else {
                var textLength = font.width(keybinding.label);
                var xOffset = 0;
                switch (horizontalAnchor) {
                    case TRAILING -> xOffset = -textLength / 2;
                    case CENTER -> xOffset = 0;
                    case LEADING -> xOffset = textLength / 2;
                }
                x += xOffset;
                HudKeyVisuals.buttonLeading.draw(context, x - (textLength / 2), y, Drawable.Anchor.TRAILING, verticalAnchor);
                HudKeyVisuals.buttonCenter.drawFlexibleWidth(context, x - (textLength / 2), y, textLength, verticalAnchor);
                HudKeyVisuals.buttonTrailing.draw(context, x + (textLength / 2), y, Drawable.Anchor.LEADING, verticalAnchor);
                context.drawCenteredString(font, keybinding.label, x, y - 10, 0xFFFFFF);
            }
        }

        private static void renderCooldown(GuiGraphics context, float progress, int x, int y) {
            // Copied from GuiGraphics.drawItemInSlot
            var k = y + Mth.floor(16.0F * (1.0F - progress));
            var l = k + Mth.ceil(16.0F * progress);
            context.fill(x, k, x + 16, l, 0x80FFFFFF);
            // RenderSystem.enableBlend(); // Removed in 1.21.11
            // RenderSystem.defaultBlendFunc(); // Removed in 1.21.11
        }
    }

    public static class ErrorMessageWidget {
        public static Rect lastRendered;

        public record ViewModel(Component message, float opacity) {
            public static ViewModel mock() {
                return new ViewModel(Component.literal("Error Message!").withStyle(ChatFormatting.RED), 1F);
            }

            public static ViewModel from(Component message, int durationLeft, int fadeOut, float tickDelta) {
                float tick = ((float)durationLeft) - tickDelta;
                float opacity = tick > fadeOut ? 1F : (tick / fadeOut);
                return new ViewModel(message, opacity);
            }
        }

        public static void render(GuiGraphics context, HudConfig hudConfig, int screenWidth, int screenHeight, ViewModel viewModel) {
            int alpha = (int) (viewModel.opacity * 255);
            if (alpha < 10) { return; }
            // System.out.println("Rendering opacity: " + viewModel.opacity + " alpha: " + alpha);
            Minecraft client = Minecraft.getInstance();
            var font = client.gui.getFont();
            int textWidth = font.width(viewModel.message);
            int textHeight = font.lineHeight;
            var config = hudConfig.error_message;
            var origin = config.origin
                    .getPoint(screenWidth, screenHeight)
                    .add(config.offset);

            int x = (int) (origin.x - (textWidth / 2F));
            int y = (int) origin.y;
            lastRendered = new Rect(new Vec2(x ,y), new Vec2(x + textWidth,y + textHeight));
            // RenderSystem.enableBlend(); // Removed in 1.21.11
            // RenderSystem.defaultBlendFunc(); // Removed in 1.21.11
            context.fill(x - 2, y - 2, x + textWidth + 2, y + font.lineHeight + 2, 0x80000000);
            context.drawString(font, viewModel.message(), x, y, 0xFFFFFF + (alpha << 24)); // color is ARGB
            // RenderSystem.disableBlend(); // Removed in 1.21.11
        }
    }
}
