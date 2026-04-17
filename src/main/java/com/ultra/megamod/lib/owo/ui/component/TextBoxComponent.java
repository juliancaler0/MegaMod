package com.ultra.megamod.lib.owo.ui.component;

import com.ultra.megamod.lib.owo.mixin.ui.access.EditBoxAccessor;
import com.ultra.megamod.lib.owo.ui.core.CursorStyle;
import com.ultra.megamod.lib.owo.ui.core.OwoUIGraphics;
import com.ultra.megamod.lib.owo.ui.core.Sizing;
import com.ultra.megamod.lib.owo.ui.parsing.UIModel;
import com.ultra.megamod.lib.owo.ui.parsing.UIParsing;
import com.ultra.megamod.lib.owo.util.EventSource;
import com.ultra.megamod.lib.owo.util.EventStream;
import com.ultra.megamod.lib.owo.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.function.Consumer;

public class TextBoxComponent extends EditBox {

    protected final Observable<Boolean> showsBackground = Observable.of(((EditBoxAccessor) this).owo$bordered());

    protected final Observable<String> textValue = Observable.of("");
    protected final EventStream<OnChanged> changedEvents = OnChanged.newStream();

    protected TextBoxComponent(Sizing horizontalSizing) {
        super(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty());

        this.textValue.observe(this.changedEvents.sink()::onChanged);
        this.sizing(horizontalSizing, Sizing.content());

        this.showsBackground.observe(a -> this.widgetWrapper().notifyParentIfMounted());
    }

    /**
     * @deprecated Subscribe to {@link #onChanged()} instead
     */
    @Override
    @Deprecated(forRemoval = true)
    public void setResponder(Consumer<String> changedListener) {
        super.setResponder(changedListener);
    }

    @Override
    public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        // noop, since TextFieldWidget already does this
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean result = super.keyPressed(input);

        if (input.isCycleFocus()) {
            this.insertText("    ");
            return true;
        } else {
            return result;
        }
    }

    @Override
    public void updateX(int x) {
        super.updateX(x);
        ((EditBoxAccessor) this).owo$updateTextPosition();
    }

    @Override
    public void updateY(int y) {
        super.updateY(y);
        ((EditBoxAccessor) this).owo$updateTextPosition();
    }

    @Override
    public void setBordered(boolean drawsBackground) {
        super.setBordered(drawsBackground);
        this.showsBackground.set(drawsBackground);
    }

    public EventSource<OnChanged> onChanged() {
        return changedEvents.source();
    }

    public TextBoxComponent text(String text) {
        this.setValue(text);
        this.moveCursorToStart(false);
        return this;
    }

    @Override
    public void parseProperties(UIModel spec, Element element, Map<String, Element> children) {
        super.parseProperties(spec, element, children);
        UIParsing.apply(children, "show-background", UIParsing::parseBool, this::setBordered);
        UIParsing.apply(children, "max-length", UIParsing::parseUnsignedInt, this::setMaxLength);
        UIParsing.apply(children, "text", e -> e.getTextContent().strip(), this::text);
    }

    protected CursorStyle owo$preferredCursorStyle() {
        return CursorStyle.TEXT;
    }

    public interface OnChanged {
        void onChanged(String value);

        static EventStream<OnChanged> newStream() {
            return new EventStream<>(subscribers -> value -> {
                for (var subscriber : subscribers) {
                    subscriber.onChanged(value);
                }
            });
        }
    }
}
