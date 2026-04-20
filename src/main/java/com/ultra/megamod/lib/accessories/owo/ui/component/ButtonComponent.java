package com.ultra.megamod.lib.accessories.owo.ui.component;

import com.ultra.megamod.lib.accessories.owo.Owo;
import com.ultra.megamod.mixin.accessories.owo.ui.access.AbstractWidgetAccessor;
import com.ultra.megamod.mixin.accessories.owo.ui.access.ButtonAccessor;
import com.ultra.megamod.lib.accessories.owo.ui.core.Color;
import com.ultra.megamod.lib.accessories.owo.ui.core.CursorStyle;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIGraphics;
import com.ultra.megamod.lib.accessories.owo.ui.core.Sizing;
import com.ultra.megamod.lib.accessories.owo.ui.parsing.UIModel;
import com.ultra.megamod.lib.accessories.owo.ui.parsing.UIModelParsingException;
import com.ultra.megamod.lib.accessories.owo.ui.parsing.UIParsing;
import com.ultra.megamod.lib.accessories.owo.ui.util.NinePatchTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;
import java.util.function.Consumer;

import com.ultra.megamod.lib.accessories.owo.ui.inject.UIComponentStub;

public class ButtonComponent extends Button implements UIComponentStub {

    public static final Identifier ACTIVE_TEXTURE = Owo.id("button/active");
    public static final Identifier HOVERED_TEXTURE = Owo.id("button/hovered");
    public static final Identifier DISABLED_TEXTURE = Owo.id("button/disabled");

    protected Renderer renderer = Renderer.VANILLA;
    protected boolean textShadow = true;

    protected ButtonComponent(Component message, Consumer<ButtonComponent> onPress) {
        super(0, 0, 0, 0, message, button -> onPress.accept((ButtonComponent) button), Button.DEFAULT_NARRATION);
        this.sizing(Sizing.content());
    }

    @Override
    public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderer.draw((OwoUIGraphics) context, this, delta);

        var textRenderer = Minecraft.getInstance().font;
        int color = this.active ? 0xffffffff : 0xffa0a0a0;

        if (this.textShadow) {
            context.drawCenteredString(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
        } else {
            context.drawString(textRenderer, this.getMessage(), (int) (this.getX() + this.width / 2f - textRenderer.width(this.getMessage()) / 2f), (int) (this.getY() + (this.height - 8) / 2f), color, false);
        }

        var tooltip = ((AbstractWidgetAccessor) this).owo$getTooltip();
        if (this.isHovered && tooltip.get() != null)
            context.setTooltipForNextFrame(textRenderer, tooltip.get().toCharSequence(Minecraft.getInstance()), DefaultTooltipPositioner.INSTANCE, mouseX, mouseY, false);
    }

    
    public ButtonComponent tooltip(Component tooltip) {
        this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(tooltip));
        return this;
    }

    public ButtonComponent onPress(Consumer<ButtonComponent> onPress) {
        ((ButtonAccessor) this).owo$setOnPress(button -> onPress.accept((ButtonComponent) button));
        return this;
    }

    public ButtonComponent renderer(Renderer renderer) {
        this.renderer = renderer;
        return this;
    }

    public Renderer renderer() {
        return this.renderer;
    }

    public ButtonComponent textShadow(boolean textShadow) {
        this.textShadow = textShadow;
        return this;
    }

    public boolean textShadow() {
        return this.textShadow;
    }

    public ButtonComponent active(boolean active) {
        this.active = active;
        return this;
    }

    public boolean active() {
        return this.active;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        // super.parseProperties not accessible at compile time
        UIParsing.apply(children, "text", UIParsing::parseText, this::setMessage);
        UIParsing.apply(children, "text-shadow", UIParsing::parseBool, this::textShadow);
        UIParsing.apply(children, "renderer", Renderer::parse, this::renderer);
    }

    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.HAND;
    }

    @FunctionalInterface
    public interface Renderer {
        Renderer VANILLA = (matrices, button, delta) -> {
            var texture = button.active
                    ? button.isHovered ? HOVERED_TEXTURE : ACTIVE_TEXTURE
                    : DISABLED_TEXTURE;
            NinePatchTexture.draw(texture, matrices, button.getX(), button.getY(), button.getWidth(), button.getHeight());
        };

        static Renderer flat(int color, int hoveredColor, int disabledColor) {
            return (context, button, delta) -> {
                if (button.active) {
                    if (button.isHovered) {
                        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), hoveredColor);
                    } else {
                        context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), color);
                    }
                } else {
                    context.fill(button.getX(), button.getY(), button.getX() + button.getWidth(), button.getY() + button.getHeight(), disabledColor);
                }
            };
        }

        static Renderer texture(Identifier texture, int u, int v, int textureWidth, int textureHeight) {
            return (context, button, delta) -> {
                int renderV = v;
                if (!button.active) {
                    renderV += button.getHeight() * 2;
                } else if (button.isHovered()) {
                    renderV += button.getHeight();
                }

                context.blit(RenderPipelines.GUI_TEXTURED, texture, button.getX(), button.getY(), u, renderV, button.getWidth(), button.getHeight(), textureWidth, textureHeight);
            };
        }

        void draw(OwoUIGraphics context, ButtonComponent button, float delta);

        static Renderer parse(Element element) {
            var children = UIParsing.<Element>allChildrenOfType(element, Node.ELEMENT_NODE);
            if (children.size() > 1)
                throw new UIModelParsingException("'renderer' declaration may only contain a single child");

            var rendererElement = children.get(0);
            return switch (rendererElement.getNodeName()) {
                case "vanilla" -> VANILLA;
                case "flat" -> {
                    UIParsing.expectAttributes(rendererElement, "color", "hovered-color", "disabled-color");
                    yield flat(
                            Color.parseAndPack(rendererElement.getAttributeNode("color")),
                            Color.parseAndPack(rendererElement.getAttributeNode("hovered-color")),
                            Color.parseAndPack(rendererElement.getAttributeNode("disabled-color"))
                    );
                }
                case "texture" -> {
                    UIParsing.expectAttributes(rendererElement, "texture", "u", "v", "texture-width", "texture-height");
                    yield texture(
                            UIParsing.parseIdentifier(rendererElement.getAttributeNode("texture")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("u")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("v")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("texture-width")),
                            UIParsing.parseUnsignedInt(rendererElement.getAttributeNode("texture-height"))
                    );
                }
                default ->
                        throw new UIModelParsingException("Unknown button renderer '" + rendererElement.getNodeName() + "'");
            };
        }
    }
}
