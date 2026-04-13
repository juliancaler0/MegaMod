package com.ultra.megamod.lib.accessories.owo.ui.base;

import com.ultra.megamod.lib.accessories.impl.option.PlayerOption;
import com.ultra.megamod.lib.accessories.impl.option.PlayerOptionsAccess;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIAdapter;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIDrawContext;
import com.ultra.megamod.lib.accessories.owo.ui.core.ParentComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter stub for io.wispforest.owo.ui.base.BaseOwoHandledScreen.
 */
public abstract class BaseOwoHandledScreen<R extends ParentComponent, H extends AbstractContainerMenu> extends AbstractContainerScreen<H> implements PlayerOptionsAccess {

    protected OwoUIAdapter<R> uiAdapter;
    private final Map<PlayerOption<?>, Object> playerOptionData = new HashMap<>();

    public BaseOwoHandledScreen(H menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getData(PlayerOption<T> option) {
        return Optional.ofNullable((T) playerOptionData.get(option));
    }

    @Override
    public <T> void setData(PlayerOption<T> option, T data) {
        playerOptionData.put(option, data);
    }

    protected abstract @NotNull OwoUIAdapter<R> createAdapter();
    protected abstract R rootComponent();
    protected abstract void build(R rootComponent);

    @Override
    protected void init() {
        super.init();
        this.uiAdapter = createAdapter();
        build(uiAdapter.rootComponent);
    }

    @SuppressWarnings("unchecked")
    public <C extends com.ultra.megamod.lib.accessories.owo.ui.core.Component> C component(Class<C> expectedClass, String id) {
        if (uiAdapter != null && uiAdapter.rootComponent != null) {
            return uiAdapter.rootComponent.childById(expectedClass, id);
        }
        return null;
    }

    public void enableSlot(Slot slot) {
        // Restore slot position - no-op in stub
    }

    public void enableSlot(int index) {
        if (index >= 0 && index < this.menu.slots.size()) {
            enableSlot(this.menu.slots.get(index));
        }
    }

    public void disableSlot(Slot slot) {
        // Hide slot by moving off-screen
        slot.x = -300;
        slot.y = -300;
    }

    public void disableSlot(int index) {
        if (index >= 0 && index < this.menu.slots.size()) {
            disableSlot(this.menu.slots.get(index));
        }
    }

    @SuppressWarnings("unchecked")
    public com.ultra.megamod.lib.accessories.owo.ui.core.Component slotAsComponent(int index) {
        // In real OWO, this creates a visual component for the slot. Return a stub component.
        return new BaseComponent() {};
    }

    public boolean showCosmeticState() { return false; }
    public void showCosmeticState(boolean state) {}

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // No background rendering in stub
    }

    protected void renderSlotHighlightBack(GuiGraphics guiGraphics) {}
    protected void renderSlotHighlightFront(GuiGraphics guiGraphics) {}

    public abstract class SlotComponent extends BaseComponent {
        protected net.minecraft.world.inventory.Slot slot;
        protected boolean didDraw = false;
        public int x, y, width = 16, height = 16;

        protected SlotComponent(int index) {
            if (index >= 0 && index < menu.slots.size()) {
                this.slot = menu.slots.get(index);
            }
        }

        public boolean isActive() { return slot != null; }
        public void dismount(DismountReason reason) {}
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {}
        public void drawTooltip(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {}
        public void update(float delta, int mouseX, int mouseY) {}
    }

    public enum DismountReason {
        REMOVED, LAYOUT_INFLATION
    }
}
