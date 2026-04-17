package com.ultra.megamod.lib.owo.ui.component;

import com.ultra.megamod.lib.owo.Owo;
import com.ultra.megamod.lib.owo.ui.base.BaseUIComponent;
import com.ultra.megamod.lib.owo.ui.core.Color;
import com.ultra.megamod.lib.owo.ui.core.CursorStyle;
import com.ultra.megamod.lib.owo.ui.core.OwoUIGraphics;
import com.ultra.megamod.lib.owo.ui.core.Sizing;
import com.ultra.megamod.lib.owo.ui.parsing.UIModel;
import com.ultra.megamod.lib.owo.ui.parsing.UIParsing;
import com.ultra.megamod.lib.owo.ui.util.UISounds;
import com.ultra.megamod.lib.owo.util.EventSource;
import com.ultra.megamod.lib.owo.util.EventStream;
import com.ultra.megamod.lib.owo.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.Map;

public class SmallCheckboxComponent extends BaseUIComponent {

    public static final Identifier TEXTURE = Owo.id("textures/gui/smol_checkbox.png");

    protected final EventStream<OnChanged> checkedEvents = OnChanged.newStream();

    protected final Observable<@Nullable Component> label;
    protected boolean labelShadow = false;
    protected boolean checked = false;

    public SmallCheckboxComponent(Component label) {
        this.cursorStyle(CursorStyle.HAND);

        this.label = Observable.of(label);
        this.label.observe(text -> this.notifyParentIfMounted());
    }

    public SmallCheckboxComponent() {
        this(null);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.label.get() != null) {
            graphics.drawString(Minecraft.getInstance().font, this.label.get(), this.x + 13 + 2, this.y + 3, Color.WHITE.argb(), this.labelShadow);
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.x, this.y, 0, 0, 13, 13, 13, 13, 32, 16);
        if (this.checked) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.x, this.y, 16, 0, 13, 13, 13, 13, 32, 16);
        }
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return this.label.get() != null
                ? 13 + 2 + Minecraft.getInstance().font.width(this.label.get())
                : 13;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return 13;
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        boolean result = super.onMouseDown(click, doubled);

        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.toggle();
            return true;
        }

        return result;
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        boolean result = super.onKeyPress(input);

        if (input.isSelection()) {
            this.toggle();
            return true;
        }

        return result;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    public void toggle() {
        this.checked(!this.checked);
        UISounds.playInteractionSound();
    }

    public EventSource<OnChanged> onChanged() {
        return this.checkedEvents.source();
    }

    public SmallCheckboxComponent checked(boolean checked) {
        this.checked = checked;
        this.checkedEvents.sink().onChanged(this.checked);

        return this;
    }

    public boolean checked() {
        return checked;
    }

    public SmallCheckboxComponent label(Component label) {
        this.label.set(label);
        return this;
    }

    public Component label() {
        return this.label.get();
    }

    public SmallCheckboxComponent labelShadow(boolean labelShadow) {
        this.labelShadow = labelShadow;
        return this;
    }

    public boolean labelShadow() {
        return labelShadow;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(children, "label", UIParsing::parseText, this::label);
        UIParsing.apply(children, "label-shadow", UIParsing::parseBool, this::labelShadow);
        UIParsing.apply(children, "checked", UIParsing::parseBool, this::checked);
    }

    public interface OnChanged {
        void onChanged(boolean nowChecked);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}
