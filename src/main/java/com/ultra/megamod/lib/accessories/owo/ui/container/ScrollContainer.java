package com.ultra.megamod.lib.accessories.owo.ui.container;

import com.ultra.megamod.lib.accessories.owo.ui.core.*;

import java.util.List;

/**
 * Adapter stub for io.wispforest.owo.ui.container.ScrollContainer.
 */
public class ScrollContainer<C extends Component> implements ParentComponent {

    public enum ScrollDirection {
        VERTICAL, HORIZONTAL;

        public double choose(double vertical, double horizontal) {
            return this == VERTICAL ? vertical : horizontal;
        }
    }

    protected ScrollDirection direction = ScrollDirection.VERTICAL;
    protected double scrollOffset = 0;
    protected double maxScroll = 0;
    protected int scrollbarThiccness = 4;
    protected int x, y, width, height;
    protected AnimatableProperty<Insets> padding = AnimatableProperty.of(Insets.none());

    private String componentId;

    public ScrollContainer() {}

    public ScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
        this.direction = direction;
    }

    public void layout(Size space) {}

    protected boolean isInScrollbar(double mouseX, double mouseY) { return false; }

    protected Size calculateChildSpace(Size thisSpace) { return Size.of(0, 0); }

    protected int childMountX() { return x; }

    protected int childMountY() { return y; }

    protected void scrollBy(double offset, boolean instant, boolean showScrollbar) {
        this.scrollOffset = Math.max(0, Math.min(scrollOffset + offset, maxScroll));
    }

    public boolean onMouseScroll(double mouseX, double mouseY, double amount) { return false; }

    protected void drawChildren(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends Component> children) {}

    public ScrollContainer<C> scrollbar(Scrollbar scrollbar) { return this; }

    public ScrollContainer<C> scrollbarThiccness(int thiccness) { this.scrollbarThiccness = thiccness; return this; }

    @Override
    public Component id(String id) { this.componentId = id; return this; }

    @Override
    @org.jetbrains.annotations.Nullable
    public String id() { return componentId; }

    @Override
    @org.jetbrains.annotations.Nullable
    public ParentComponent parent() { return null; }

    @Override
    @org.jetbrains.annotations.Nullable
    public <T extends Component> T childById(Class<T> clazz, String id) { return null; }

    @Override
    public void removeChild(Component child) {}

    @FunctionalInterface
    public interface Scrollbar {
        void draw(OwoUIDrawContext context, int x, int y, int width, int height, int trackX, int trackY, int trackWidth, int trackHeight, long lastInteractTime, ScrollDirection direction, boolean active);

        static Scrollbar vanilla() {
            return (ctx, x, y, w, h, tx, ty, tw, th, t, d, a) -> {};
        }

        static Scrollbar flat(Color color) {
            return (ctx, x, y, w, h, tx, ty, tw, th, t, d, a) -> {};
        }
    }

    public ScrollContainer<C> fixedScrollbarLength(int length) { return this; }
    public ScrollContainer<C> scrollStep(int step) { return this; }

    public boolean isInBoundingBox(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public Sizing horizontalSizing() { return Sizing.content(); }
    public Sizing verticalSizing() { return Sizing.content(); }
}
