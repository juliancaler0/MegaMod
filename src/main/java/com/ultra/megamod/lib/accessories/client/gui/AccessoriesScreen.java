package com.ultra.megamod.lib.accessories.client.gui;

import com.ultra.megamod.lib.accessories.Accessories;
import com.ultra.megamod.lib.accessories.api.action.ActionResponseBuffer;
import com.ultra.megamod.lib.accessories.api.action.ValidationState;
import com.ultra.megamod.lib.accessories.api.client.tooltip.TooltipComponentBuilderImpl;
import com.ultra.megamod.lib.accessories.api.tooltip.*;
import com.ultra.megamod.lib.accessories.api.client.screen.AccessoriesScreenTransitionHelper;
import com.ultra.megamod.lib.accessories.api.menu.AccessoriesBasedSlot;
import com.ultra.megamod.lib.accessories.api.slot.SlotGroup;
import com.ultra.megamod.lib.accessories.api.slot.UniqueSlotHandling;
import com.ultra.megamod.lib.accessories.api.tooltip.impl.ListTooltipEntry;
import com.ultra.megamod.lib.accessories.api.tooltip.impl.TooltipEntry;
import com.ultra.megamod.lib.accessories.client.AccessoriesClient;
import com.ultra.megamod.lib.accessories.client.AccessoriesFunkyRenderingState;
import com.ultra.megamod.lib.accessories.client.DrawUtils;
import com.ultra.megamod.lib.accessories.client.gui.components.*;
import com.ultra.megamod.lib.accessories.client.gui.utils.Line3d;
import com.ultra.megamod.lib.accessories.compat.config.TooltipInfoType;
import com.ultra.megamod.lib.accessories.data.SlotGroupLoader;
import com.ultra.megamod.lib.accessories.data.SlotTypeLoader;
import com.ultra.megamod.lib.accessories.impl.option.PlayerOption;
import com.ultra.megamod.lib.accessories.impl.option.PlayerOptions;
import com.ultra.megamod.lib.accessories.impl.option.PlayerOptionsAccess;
import com.ultra.megamod.lib.accessories.impl.slot.ExtraSlotTypeProperties;
import com.ultra.megamod.lib.accessories.menu.ArmorSlotTypes;
import com.ultra.megamod.lib.accessories.menu.SlotTypeAccessible;
import com.ultra.megamod.lib.accessories.menu.networking.ToggledSlots;
import com.ultra.megamod.lib.accessories.menu.variants.AccessoriesMenu;
import com.ultra.megamod.mixin.accessories.client.AbstractContainerScreenAccessor;
import com.ultra.megamod.mixin.accessories.client.GuiGraphicsAccessor;
import com.ultra.megamod.lib.accessories.networking.AccessoriesNetworking;
import com.ultra.megamod.lib.accessories.networking.holder.SyncOptionChange;
import com.ultra.megamod.lib.accessories.pond.ContainerScreenExtension;
import com.ultra.megamod.lib.accessories.pond.DeferredTooltipGetter;
import com.ultra.megamod.lib.accessories.pond.TooltipFlagExtended;
import com.ultra.megamod.lib.accessories.utils.ComponentOps;
import com.ultra.megamod.lib.accessories.owo.ui.base.BaseOwoHandledScreen;
import com.ultra.megamod.lib.accessories.owo.ui.component.ButtonComponent;
import com.ultra.megamod.lib.accessories.owo.ui.component.Components;
import com.ultra.megamod.lib.accessories.owo.ui.container.Containers;
import com.ultra.megamod.lib.accessories.owo.ui.container.FlowLayout;
import com.ultra.megamod.lib.accessories.owo.ui.container.ScrollContainer;
import com.ultra.megamod.lib.accessories.owo.ui.container.StackLayout;
import com.ultra.megamod.lib.accessories.owo.ui.core.AnimatableProperty;
import com.ultra.megamod.lib.accessories.owo.ui.core.Color;
import com.ultra.megamod.lib.accessories.owo.ui.core.Easing;
import com.ultra.megamod.lib.accessories.owo.ui.core.HorizontalAlignment;
import com.ultra.megamod.lib.accessories.owo.ui.core.Insets;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIAdapter;
import com.ultra.megamod.lib.accessories.owo.ui.core.OwoUIDrawContext;
import com.ultra.megamod.lib.accessories.owo.ui.core.ParentComponent;
import com.ultra.megamod.lib.accessories.owo.ui.core.Positioning;
import com.ultra.megamod.lib.accessories.owo.ui.core.PositionedRectangle;
import com.ultra.megamod.lib.accessories.owo.ui.core.Sizing;
import com.ultra.megamod.lib.accessories.owo.ui.core.Surface;
import com.ultra.megamod.lib.accessories.owo.ui.core.VerticalAlignment;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.booleans.BooleanUnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccessoriesScreen extends BaseOwoHandledScreen<FlowLayout, AccessoriesMenu> implements AccessoriesScreenBase<AccessoriesMenu>, ContainerScreenExtension, PlayerOptionsAccess {

    private final @Nullable AbstractContainerScreen<AbstractContainerMenu> prevScreen;

    public AccessoriesScreen(AccessoriesMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        this.prevScreen = (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> prevScreen)
                ? (AbstractContainerScreen<AbstractContainerMenu>) prevScreen
                : null;

        this.inventoryLabelX = 42069;
    }

    //--

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    protected FlowLayout rootComponent() {
        return this.uiAdapter.rootComponent;
    }

    private boolean rebuildComponentRectangles = true;

    private List<PositionedRectangle> componentRectangles = List.of();

    public List<PositionedRectangle> getComponentRectangles() {
        if (rebuildComponentRectangles) {
            var unpackRules = new ArrayDeque<List<Pair<Boolean, String>>>();

            unpackRules.push(List.of());
            unpackRules.push(List.of(Pair.of(false, ARMOR_ENTITY.id()), Pair.of(true, "bottom_inventory_section")));
            unpackRules.push(List.of(Pair.of(false, "outer_accessories_layout")));

            var stream = rootComponent().children().stream();

            while (!unpackRules.isEmpty()) {
                var ids = unpackRules.pollLast();

                stream = stream.flatMap(component -> {
                    if (component instanceof ParentComponent parent) {
                        if (ids.isEmpty()) return parent.children().stream();

                        if (parent.id() != null) {
                            for (var pair : ids) {
                                if (pair.right().equals(parent.id())) {
                                    var componentStream = parent.children().stream();

                                    if (pair.left()) {
                                        componentStream = Stream.concat(componentStream, Stream.of(parent));
                                    }

                                    return componentStream;
                                }
                            }
                        }
                    }

                    return Stream.of(component);
                });
            }

            this.componentRectangles = stream
                    .map(component -> (PositionedRectangle) component)
                    .toList();

            this.rebuildComponentRectangles = false;
        }

        return componentRectangles;
    }

    @Override
    public <C extends com.ultra.megamod.lib.accessories.owo.ui.core.Component> C component(Class<C> expectedClass, String id) {
        return super.component(expectedClass, id);
    }

    //--

    private final Map<Integer, Boolean> changedSlots = new HashMap<>();

    @Override
    protected void containerTick() {
        super.containerTick();

        if (this.changedSlots.isEmpty()) return;

        var slots = this.getMenu().slots;

        var changes = this.changedSlots.entrySet().stream()
                .filter(entry -> entry.getKey() < slots.size())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        this.getMenu().sendMessage(new ToggledSlots(changes));

        this.changedSlots.clear();
    }

    public void hideSlot(int index) {
        hideSlot(this.menu.slots.get(index));
    }

    public void hideSlot(Slot slot) {
        slot.x = -300;
        slot.y = -300;
    }

    @Override
    public void disableSlot(Slot slot) {
        super.disableSlot(slot);

        var index = slot.index;

        // If present check result is enabled, else update as a changeed slot
        if (this.changedSlots.getOrDefault(index, false)) return;

        hideSlot(index);

        this.changedSlots.put(index, true);

        currentEquipCheckStack = ItemStack.EMPTY;
    }

    @Override
    public void enableSlot(Slot slot) {
        super.enableSlot(slot);

        var index = slot.index;

        // If present check result is disabled, else update as a changed slot
        if (!this.changedSlots.getOrDefault(index, true)) return;

        this.changedSlots.put(index, false);

        currentEquipCheckStack = ItemStack.EMPTY;
    }

    //--


    @Override
    public @Nullable SlotTypeAccessible getSelectedSlot() {
        return this.hoveredSlot instanceof SlotTypeAccessible slot ? slot : null;
    }

    @Override
    @Nullable
    public Boolean isHovering_Logical(Slot slot, double mouseX, double mouseY) {
        var accessories = this.topComponent;

        return (accessories != null) ? accessories.isHovering_Logical(slot, mouseX, mouseY) : null;
    }

    //--

    @Override
    public void onClose() {
        try {
            var selectedGroups = this.getMenu().selectedGroups().stream()
                    .map(SlotGroup::name)
                    .collect(Collectors.toSet());

            AccessoriesNetworking
                    .sendToServer(SyncOptionChange.of(PlayerOptions.FILTERED_GROUPS, selectedGroups));
        } catch (Throwable t) {
            // SyncOptionChange is intentionally un-registered on the channel right
            // now (its endec's codec throws; see AccessoriesNetworking.init). ESC
            // close would otherwise crash here with "record type SyncOptionChange
            // was never registered on channel accessories:main". Swallow so the
            // close flow survives until a real StructEndec exists for PlayerOption.
        }

        super.onClose();
    }

    @Override
    protected void init() {
        if (!menu.isValidMenu()) {
            Minecraft.getInstance().setScreen(
                    new ErrorScreen(
                            Component.literal("Accessories Screen Opening Error!"),
                            Component.literal("Unable to open Accessories Screen due to desync with the Server!")
                    ));

            return;
        }

        // Panel geometry must be configured BEFORE super.init() so the centering
        // math done by AbstractContainerScreen lands on the right origin.
        // Final layout (no crafting grid — accessories menu's 2x2 grid + result
        // slot are pushed off-screen):
        //   - left column: single-wide accessory column (base slot per type)
        //   - center:      player model preview
        //   - right:       armor column (4 vanilla armor slots)
        //   - bottom:      3x9 inventory + 9-slot hotbar + offhand to the right
        this.imageWidth = 212;
        this.imageHeight = 192;

        super.init();

        // OWO's build() pushes all slots to (-300, -300). Lay the slots out
        // natively to match the stock Accessories target layout. Cosmetic +
        // crafting slots are kept off-screen (but still interactive through
        // the menu) so they don't visually clutter the panel.
        layoutSlotsFallback();
    }

    /** Accessory column geometry (two columns, left edge of the panel). */
    private static final int ACC_COL_X = 8;
    private static final int ACC_COL2_X = 26;
    private static final int ACC_COL_Y = 18;
    private static final int ACC_ROW_STRIDE = 14;
    private static final int ACC_VISIBLE_ROW_STRIDE = 18;
    private static final int ACC_COLS = 2;

    /** Armor-column geometry (right side of the player preview). */
    private static final int ARMOR_COL_X = 156;
    private static final int ARMOR_COL_Y = 18;
    private static final int ARMOR_ROW_STRIDE = 18;

    /** Entity preview panel geometry (between accessory + armor columns). */
    private static final int ENTITY_X = 52;
    private static final int ENTITY_Y = 8;
    private static final int ENTITY_W = 96;
    private static final int ENTITY_H = 88;

    /** Player inventory geometry. */
    private static final int INV_X = 8;
    private static final int INV_Y = 108;
    private static final int HOTBAR_Y = 166;

    /** Offhand slot (to the right of the hotbar). */
    private static final int OFFHAND_X = 176;
    private static final int OFFHAND_Y = HOTBAR_Y;

    /** Off-screen coordinate for hidden slots. */
    private static final int HIDDEN = -1000;

    private int accRowStride = ACC_VISIBLE_ROW_STRIDE;

    private void layoutSlotsFallback() {
        var slots = this.getMenu().slots;
        if (slots.isEmpty()) return;

        var menu = this.getMenu();
        // startingAccessoriesSlot() returns the first armor-pair index (42 in a
        // vanilla player setup); accessories start after the armor block.
        int armorStart = menu.startingAccessoriesSlot();
        int accStart = armorStart + menu.addedArmorSlots();

        // ---- Hide crafting result + 2x2 grid (slots 0-4) — target layout
        //      does not show a crafting grid. They stay in the menu for
        //      state compat but render off-screen. ----
        for (int i = 0; i < 5 && i < slots.size(); i++) {
            var s = slots.get(i);
            s.x = HIDDEN;
            s.y = HIDDEN;
        }

        // ---- Player main inventory (slots 5-31, 3x9) ----
        for (int i = 5; i < 32 && i < slots.size(); i++) {
            var s = slots.get(i);
            int idx = i - 5;
            int row = idx / 9;
            int col = idx % 9;
            s.x = INV_X + col * 18;
            s.y = INV_Y + row * 18;
        }

        // ---- Hotbar (slots 32-40) ----
        for (int i = 32; i < 41 && i < slots.size(); i++) {
            var s = slots.get(i);
            int col = i - 32;
            s.x = INV_X + col * 18;
            s.y = HOTBAR_Y;
        }

        // ---- Offhand (slot 41) — to the right of the hotbar ----
        if (slots.size() > 41) {
            var s = slots.get(41);
            s.x = OFFHAND_X;
            s.y = OFFHAND_Y;
        }

        // ---- Armor pairs (slots 42 .. accStart-1) — each pair is
        //      (armor, cosmetic). We render the armor slot on the right
        //      column and hide cosmetics off-screen so the layout matches
        //      the target screenshot. ----
        int armorRow = 0;
        for (int i = armorStart; i + 1 < accStart && i + 1 < slots.size(); i += 2) {
            var armor = slots.get(i);
            var cosmetic = slots.get(i + 1);
            armor.x = ARMOR_COL_X;
            armor.y = ARMOR_COL_Y + armorRow * ARMOR_ROW_STRIDE;
            cosmetic.x = HIDDEN;
            cosmetic.y = HIDDEN;
            armorRow++;
        }

        // ---- Accessory slot pairs (slots accStart .. end) — menu adds
        //      them as (cosmetic, base). Target shows two columns of base
        //      slots on the far left. Cosmetics ride along off-screen. ----
        int pairCount = Math.max(0, (slots.size() - accStart) / 2);
        int rowsNeeded = Math.max(1, (pairCount + ACC_COLS - 1) / ACC_COLS);
        // Cap stride so the panel always fits above the inventory.
        int availableH = (INV_Y - 4) - ACC_COL_Y;
        int maxStrideForFit = availableH / rowsNeeded;
        this.accRowStride = Math.max(12, Math.min(ACC_VISIBLE_ROW_STRIDE, maxStrideForFit));
        int accIdx = 0;
        for (int i = accStart; i + 1 < slots.size(); i += 2) {
            var cosmetic = slots.get(i);
            var base = slots.get(i + 1);
            int col = accIdx % ACC_COLS;
            int row = accIdx / ACC_COLS;
            base.x = (col == 0) ? ACC_COL_X : ACC_COL2_X;
            base.y = ACC_COL_Y + row * this.accRowStride;
            cosmetic.x = HIDDEN;
            cosmetic.y = HIDDEN;
            accIdx++;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (this.getDefaultedData(PlayerOptions.ADVANCED_SETTINGS)) {
            toggleAdvancedOptions(this.component(ButtonComponent.class, "advanced_options_btn"));

            return false;
        }

        return super.shouldCloseOnEsc();
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (AccessoriesClient.OPEN_SCREEN.matches(input)) {
            this.onClose();
            return true;
        }

        if (input.key() == GLFW.GLFW_KEY_ESCAPE && this.getDefaultedData(PlayerOptions.ADVANCED_SETTINGS)) {
            toggleAdvancedOptions(this.component(ButtonComponent.class, "advanced_options_btn"));

            return false;
        }

        return super.keyPressed(input);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        // OWO component rectangles are empty in this port - fall back to the panel
        // bounding box so normal inventory clicks don't close the screen.
        if (mouseX >= guiLeft && mouseX <= guiLeft + this.imageWidth
                && mouseY >= guiTop && mouseY <= guiTop + this.imageHeight) {
            return false;
        }

        for (var rect : getComponentRectangles()) {
            if (rect.isInBoundingBox(mouseX, mouseY)) {
                return false;
            }
        }

        return true;
    }

    //-- Slot Darkening Logic

    private ItemStack currentEquipCheckStack = ItemStack.EMPTY;

    private boolean isOutsideInvArea = false;

    private final Map<Slot, SlotEquipCheckerState> slotToEquipCheck = new WeakHashMap<>();

    private static class SlotEquipCheckerState {
        public static final SlotEquipCheckerState EMPTY = new SlotEquipCheckerState(-1).isValid(true);

        private final int slotIndex;
        private final AnimatableProperty<Color> color = AnimatableProperty.of(Color.ofArgb(0x00000000));

        private boolean isValid = true;
        private State state = State.Light;

        public SlotEquipCheckerState(Slot slot) {
            this(slot.index);
        }

        public SlotEquipCheckerState(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        public SlotEquipCheckerState isValid(boolean value) {
            isValid = value;

            return this;
        }

        public boolean isValid() {
            return isValid;
        }

        public boolean isDarkened(Slot slot) {
            return state != State.Light && !(slot instanceof OwoSlotExtension owoSlot && owoSlot.owo$getDisabledOverride());
        }

        public Color getColor() {
            return color.get();
        }

        public void updateSlotDarkening(boolean isOutsideInvArea, boolean isHoveringASlot, @Nullable Slot hoveredSlot, boolean isCarryingStack, float partialTick) {
            color.update(partialTick);

            if ((isOutsideInvArea && isCarryingStack) || (hoveredSlot != null && slotIndex == hoveredSlot.index)) {
                if (isValid) {
                    startLightning();
                } else {
                    startDarkening(isHoveringASlot);
                }
            } else if (state.isDark()) {
                startLightning();
            }
        }

        private void startLightning() {
            if (state.isLight()) return;

            color.animate(500, Easing.QUADRATIC, Color.ofArgb(0x00000000))
                .forwards()
                .finished()
                .subscribe((dir, looping) -> state = State.Light);

            state = State.Lightning;
        }

        private void startDarkening(boolean fast) {
            if (fast ? state.isFastDark() : state.isDark()) return;

            color.animate(fast ? 250 : 650, Easing.QUADRATIC, Color.ofArgb(0x4D000000))
                .forwards()
                .finished()
                .subscribe((dir, looping) -> state = State.Dark);

            state = fast ? State.FastDarkening : State.Darkening;
        }

        public void render(GuiGraphics guiGraphics, Slot slot, boolean isSideBySideSlots) {
            int x = slot.x, y = slot.y;

            if (!isDarkened(slot)) return;

            var color = getColor().argb();

            if (!(slot instanceof AccessoriesBasedSlot basedSlot) || (basedSlot.isCosmetic && isSideBySideSlots)) {
                guiGraphics.fill(x, y, x + 16, y + 16, color);
            } else {
                guiGraphics.fill(x, y, x + 13, y + 2, color);
                guiGraphics.fill(x, y + 2, x + 14, y + 16, color);
                guiGraphics.fill(x + 14, y + 3, x + 16, y + 16, color);
            }
        }

        private enum State {
            Darkening,
            FastDarkening,
            Dark,
            Lightning,
            Light;

            public boolean isDark() { return isFastDark() || this == State.Darkening; }

            public boolean isFastDark() { return this == State.FastDarkening || this == State.Dark; }

            public boolean isLight() { return this == State.Lightning || this == State.Light; }
        }
    }

    public void updateSlotDarkening(float partialTick) {
        var isCarryingStack = !this.getMenu().getCarried().isEmpty();
        var isHoveringSlot = hoveredSlot instanceof SlotTypeAccessible;

        stateAccess.forEach((slot, state) -> {
            state.updateSlotDarkening(isOutsideInvArea, isHoveringSlot, this.hoveredSlot, isCarryingStack, partialTick);
        });
    }

    private interface SlotEquipStateAccess {
        void forEach(BiConsumer<Slot, SlotEquipCheckerState> consumer);

        SlotEquipCheckerState getOrDefault(Slot slot);

        SlotEquipCheckerState getOrCreate(Slot slot);
    }

    private final SlotEquipStateAccess stateAccess = new SlotEquipStateAccess() {
        @Override
        public void forEach(BiConsumer<Slot, SlotEquipCheckerState> consumer) {
            if (!Accessories.config().screenOptions.showSlotDarkeningEffect()) return;

            AccessoriesScreen.this.slotToEquipCheck.forEach(consumer);
        }

        @Override
        public SlotEquipCheckerState getOrDefault(Slot slot) {
            if (!Accessories.config().screenOptions.showSlotDarkeningEffect()) return SlotEquipCheckerState.EMPTY;

            return AccessoriesScreen.this.slotToEquipCheck.getOrDefault(slot, SlotEquipCheckerState.EMPTY);
        }

        public SlotEquipCheckerState getOrCreate(Slot slot) {
            if (!Accessories.config().screenOptions.showSlotDarkeningEffect()) return SlotEquipCheckerState.EMPTY;

            return AccessoriesScreen.this.slotToEquipCheck.computeIfAbsent(slot, SlotEquipCheckerState::new);
        }
    };

    public SlotEquipStateAccess getSlotToEquipCheckMap(ItemStack stack) {
        if (!ItemStack.matches(stack, currentEquipCheckStack) && Accessories.config().screenOptions.showSlotDarkeningEffect()) {
            var player = Minecraft.getInstance().player;

            for (var slot : this.getMenu().slots) {
                if (slot instanceof SlotTypeAccessible) {
                    slotToEquipCheck.computeIfAbsent(slot, SlotEquipCheckerState::new)
                        .isValid((stack.isEmpty() || slot.mayPlace(stack)) && slot.mayPickup(player));
                }
            }

            currentEquipCheckStack = stack.copy();
        }

        return stateAccess;
    }


    @Override
    protected void renderSlotHighlightBack(GuiGraphics guiGraphics) {
        var slotToEquipCheck = getSlotToEquipCheckMap(this.getMenu().getCarried());

        if (this.hoveredSlot == null || !this.hoveredSlot.isHighlightable() || !slotToEquipCheck.getOrDefault(hoveredSlot).isValid()) {
            return;
        }

        var texture = AbstractContainerScreenAccessor.accessories$SLOT_HIGHLIGHT_BACK_SPRITE();
        int x = this.hoveredSlot.x - 4, y = this.hoveredSlot.y - 4;

        renderWithClippingCheck(guiGraphics, x, y, (graphics) -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, 24, 24));
    }

    @Override
    protected void renderSlotHighlightFront(GuiGraphics guiGraphics) {
        var access = getSlotToEquipCheckMap(this.getMenu().getCarried());

        var isSideBySideSlots = this.getDefaultedData(PlayerOptions.SIDE_BY_SIDE_SLOTS);

        access.forEach((slot, data) -> data.render(guiGraphics, slot, isSideBySideSlots));

        if (this.hoveredSlot == null || !this.hoveredSlot.isHighlightable()) return;

        var data = access.getOrCreate(this.hoveredSlot);

        if (this.currentEquipCheckStack.isEmpty()) data.isValid(this.hoveredSlot.mayPickup(Minecraft.getInstance().player));
        if (!data.isValid()) return;

        var texture = AbstractContainerScreenAccessor.accessories$SLOT_HIGHLIGHT_FRONT_SPRITE();
        int x = this.hoveredSlot.x - 4, y = this.hoveredSlot.y - 4;

        renderWithClippingCheck(guiGraphics, x, y, (graphics) -> graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, x, y, 24, 24));
    }

    private void renderWithClippingCheck(GuiGraphics guiGraphics, int x, int y, Consumer<GuiGraphics> drawCall) {
        if (!(hoveredSlot instanceof AccessoriesBasedSlot basedSlot) || (basedSlot.isCosmetic && this.getDefaultedData(PlayerOptions.SIDE_BY_SIDE_SLOTS))) {
            drawCall.accept(guiGraphics);

            return;
        }

        guiGraphics.enableScissor(x, y, x + 17, y + 6);
        drawCall.accept(guiGraphics);
        guiGraphics.disableScissor();

        guiGraphics.enableScissor(x, y + 6, x + 18, y + 24);
        drawCall.accept(guiGraphics);
        guiGraphics.disableScissor();

        guiGraphics.enableScissor(x + 18, y + 7, x + 24, y + 24);
        drawCall.accept(guiGraphics);
        guiGraphics.disableScissor();
    }

    //---

    private TooltipCacheData tooltipStackKey = TooltipCacheData.EMPTY;
    private final TooltipComponentBuilderImpl validationTooltipComponents = (TooltipComponentBuilderImpl) TooltipComponentBuilder.of();

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        boolean bl = false;

        if(this.hoveredSlot != null) {
            if (this.hoveredSlot instanceof AccessoriesBasedSlot accessoriesInternalSlot) {
                bl = !ArmorSlotTypes.isArmorType(accessoriesInternalSlot.slotName()) && this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION);
            } else if (this.hoveredSlot instanceof ArmorSlot) {
                bl = true;
            } else if (this.hoveredSlot.container instanceof TransientCraftingContainer || this.hoveredSlot instanceof ResultSlot) {
                bl = !(boolean) this.getDefaultedData(PlayerOptions.SHOW_GROUP_FILTER) && !(boolean) this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION);
            }
        }

        AccessoriesScreenBase.setPositioner(bl);

        super.renderTooltip(guiGraphics, x, y);

        if (this.hoveredSlot instanceof AccessoriesBasedSlot slot && ExtraSlotTypeProperties.getProperty(slot.slotName(), true).allowTooltipInfo()) {
            var client = Minecraft.getInstance();

            var ctx = Item.TooltipContext.of(client.level);
            var flag = TooltipFlagExtended.create();

            var key = new TooltipCacheData(slot, this.getMenu().getCarried(), flag instanceof TooltipFlagExtended tfe ? tfe.getModifiers() : 0);

            var builder = TooltipComponentBuilder.of();
            var existingTooltip = ((DeferredTooltipGetter) guiGraphics).accessories$getTooltip();

            if (existingTooltip != null) {
                builder.add(existingTooltip);
            } else {
                builder.addAll(TooltipInfoProvider.gatherInfo(slot, TooltipEntry.of(), ctx, flag).entries());
            }

            if (this.menu.getCarried().isEmpty()) {
                if (slot.hasItem() && Accessories.config().screenOptions.showEquippedStackSlotType()) {
                    builder
                        .divider(4)
                        .add(
                            Component.translatable(Accessories.translationKey("tooltip.currently_equipped_in"))
                                .withStyle(ChatFormatting.GRAY)
                                .append(
                                    Component.translatable(slot.slotType().translation())
                                        .withStyle(ChatFormatting.BLUE)
                                )
                        );
                }
            }

            var tooltipType = Accessories.config().screenOptions.equipCheckTooltipType();

            if (!key.equals(tooltipStackKey) && tooltipType != TooltipInfoType.DISABLED) {
                tooltipStackKey = key;
                validationTooltipComponents.clear();

                if (!key.isEmpty()) {
                    var stackType = StackType.HELD;
                    var buffer = slot.checkInsertion(key.heldStack);

                    if (buffer.isEmpty() || (buffer.canPerformAction().isValid() && !slot.getItem().isEmpty())) {
                        var otherBuffer = slot.checkExtraction();

                        if (buffer.isEmpty() || (!otherBuffer.isEmpty() && otherBuffer.canPerformAction() != ValidationState.VALID)){
                            stackType = StackType.SLOT;
                            buffer = otherBuffer;
                        }
                    }

                    if (!buffer.isEmpty()) {
                        addValidationInfo(buffer, tooltipType, stackType, validationTooltipComponents, ctx, flag);
                    }
                }
            }

            if (!validationTooltipComponents.isEmpty()) {
                builder
                    .divider(4)
                    .add(validationTooltipComponents);
            }

            if (!builder.isEmpty()) {
                ((GuiGraphicsAccessor) guiGraphics).accessories$setTooltipForNextFrameInternal(
                    this.font,
                    ((TooltipComponentBuilderImpl) builder).build(TextWrapper.createWrapper(240)),
                    x, y,
                    DefaultTooltipPositioner.INSTANCE,
                    null,
                    true
                );
            }
        }

        AccessoriesScreenBase.setPositioner(false);
    }

    private enum StackType {
        HELD,
        SLOT
    }

    private static final class TooltipCacheData {
        private static final TooltipCacheData EMPTY = new TooltipCacheData(null, ItemStack.EMPTY, 0);

        private final WeakReference<AccessoriesBasedSlot> slotRef;
        private final ItemStack heldStack;
        private final ItemStack slotStack;
        private final int modifierBitmask;

        private TooltipCacheData(@Nullable AccessoriesBasedSlot slot, ItemStack heldStack, int modifierBitmask) {
            this.slotRef = new WeakReference<>(slot);
            this.heldStack = heldStack;
            this.slotStack = slot != null ? slot.getItem() : ItemStack.EMPTY;
            this.modifierBitmask = modifierBitmask;
        }

        public boolean isEmpty() {
            return heldStack.isEmpty() && slotStack.isEmpty();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TooltipCacheData otherKey)) return false;

            return ItemStack.isSameItemSameComponents(this.heldStack, otherKey.heldStack)
                && ItemStack.isSameItemSameComponents(this.slotStack, otherKey.slotStack)
                && Objects.equals(this.slotRef.get(), otherKey.slotRef.get())
                && modifierBitmask == otherKey.modifierBitmask;
        }
    }

    private void addValidationInfo(ActionResponseBuffer buffer, TooltipInfoType tooltipType, StackType type, TooltipComponentBuilder builder, Item.TooltipContext ctx, TooltipFlag flag) {
        if (tooltipType.equals(TooltipInfoType.ALL) && flag instanceof TooltipFlagExtended tfe) flag = tfe.withMask(Integer.MAX_VALUE);

        // TODO: SAVE TOOLTIP TO PREVENT REGATHERING INFO
        var withSuccess = collectTooltipInfo(buffer, false, ctx, flag);
        var withoutSuccess = collectTooltipInfo(buffer, true, ctx, flag);

        var splitData = (flag.hasShiftDown() || tooltipType.equals(TooltipInfoType.ADVANCED) ? withSuccess : withoutSuccess);

        var hasData = !splitData.isEmpty();

        ComponentOps.ExtraInfoFooter footerGenerator;

        var flagModifiers = flag instanceof TooltipFlagExtended tfe2 ? tfe2.getModifiers() : 0;
        if (flagModifiers != Integer.MAX_VALUE) {
            var sizes = new HashSet<>(List.of(collectTooltipInfo(buffer, false, ctx, flag instanceof TooltipFlagExtended tfe3 ? tfe3.withMask(0) : flag).hashCode()));

            footerGenerator = ComponentOps.attemptToAddExtraInfoFooter(
                new LinkedHashSet<>(splitData.hashCode() != withSuccess.hashCode() ? List.of("key.keyboard.left.shift") : List.of()),
                flag,
                newFlag -> {
                    return sizes.add(collectTooltipInfo(buffer, false, ctx, newFlag).hashCode());
                }
            );
        } else {
            footerGenerator = new ComponentOps.ExtraInfoFooter(new LinkedHashMap<>());
        }

        if (hasData) {
            builder
                .add(Accessories.translation("tooltip", (type == StackType.HELD ? "accessory_reasoning" : "equipment_reasoning")))
                .divider(2)
                .addAll(TextPrefixer.NONE, splitData);

            if (!footerGenerator.keyToFormattedText().isEmpty()) {
                builder.divider(4);
            }
        }

        footerGenerator.addTo(builder);
    }

    private ListTooltipAdder collectTooltipInfo(ActionResponseBuffer buffer, boolean filterSuccess, Item.TooltipContext ctx, TooltipFlag flag) {
        var responses = buffer.responses(filterSuccess);
        var adder = ListTooltipEntry.flatMap();

        if (!responses.isEmpty()) {
            for (var response : responses) {
                var entryAdder = adder.createListedEntry(
                    response.canPerformAction().asEntryComponent(),
                    Accessories.translation("tooltip.equipment_reasoning.indent_entry"));

                TooltipInfoProvider.gatherInfo(response, entryAdder, ctx, flag);
            }
        }

        return adder;
    }

    private final List<Vector3d> hoveredAccessoryPositons = new ArrayList<>();
    private final List<Line3d> linesToAccessoryPositions = new ArrayList<>();

    private static final net.minecraft.resources.Identifier ACC_PANEL_TEX =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("accessories", "textures/gui/accessories_panel.png");
    private static final net.minecraft.resources.Identifier SLOT_FRAME_TEX =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("accessories", "textures/gui/slot_frame.png");

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // BaseOwoHandledScreen provides no-op renderBg, no super call needed.
        // Draw the panel chrome to match the stock Accessories layout using
        // the real accessories_panel.png nine-patch + slot_frame.png overlay.
        int x = this.leftPos;
        int y = this.topPos;
        int w = this.imageWidth;
        int h = this.imageHeight;

        // Tone constants kept for fallback slot backdrops where the
        // nine-patch doesn't apply (inside slot rects).
        final int FRAME_DARK = 0xFF373737;
        final int FRAME_MID = 0xFF8B8B8B;
        final int FRAME_LIGHT = 0xFFC6C6C6;
        final int SLOT_DARK = 0xFF8B8B8B;

        // --- Accessory column sub-panel (left, two columns wide) ---
        int accRows = accessoryRowCount();
        if (accRows > 0) {
            int accPanelX = x + ACC_COL_X - 4;
            int accPanelY = y + ACC_COL_Y - 4;
            int accPanelW = ACC_COLS * 18 + 8;
            int accPanelH = accRows * this.accRowStride + 8;
            NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, accPanelX, accPanelY, accPanelW, accPanelH);
            var menu = this.getMenu();
            int accStart = menu.startingAccessoriesSlot() + menu.addedArmorSlots();
            int pairCount = Math.max(0, (menu.slots.size() - accStart) / 2);
            for (int idx = 0; idx < pairCount; idx++) {
                int col = idx % ACC_COLS;
                int row = idx / ACC_COLS;
                int sx = x + ((col == 0) ? ACC_COL_X : ACC_COL2_X);
                int sy = y + ACC_COL_Y + row * this.accRowStride;
                guiGraphics.fill(sx, sy, sx + 16, sy + 16, SLOT_DARK);
                NinePatchHelper.drawSlotFrame(guiGraphics, SLOT_FRAME_TEX, sx, sy);
            }
        }

        // --- Armor-column sub-panel (right of entity preview) ---
        int armorRows = this.getMenu().addedArmorSlots() / 2;
        if (armorRows > 0) {
            int armorPanelX = x + ARMOR_COL_X - 4;
            int armorPanelY = y + ARMOR_COL_Y - 4;
            int armorPanelH = armorRows * ARMOR_ROW_STRIDE + 8;
            NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, armorPanelX, armorPanelY, 18 + 8, armorPanelH);
            for (int r = 0; r < armorRows; r++) {
                int sx = x + ARMOR_COL_X;
                int sy = y + ARMOR_COL_Y + r * ARMOR_ROW_STRIDE;
                guiGraphics.fill(sx, sy, sx + 16, sy + 16, SLOT_DARK);
                NinePatchHelper.drawSlotFrame(guiGraphics, SLOT_FRAME_TEX, sx, sy);
            }
        }

        // --- Entity preview frame ---
        int ex = x + ENTITY_X;
        int ey = y + ENTITY_Y;
        NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, ex - 4, ey - 4, ENTITY_W + 8, ENTITY_H + 8);
        guiGraphics.fill(ex, ey, ex + ENTITY_W, ey + ENTITY_H, 0xFF1A1A1A);

        // Render player preview (mouse-follow like the vanilla inventory).
        // Signature in 1.21.11 is:
        //   renderEntityInInventoryFollowsMouse(gui, x1, y1, x2, y2, scale,
        //       yOffset, mouseX, mouseY, entity)
        // Pass raw mouse coords so the model tracks the cursor correctly.
        var preview = this.getMenu().targetEntityDefaulted();
        if (preview != null) {
            int x1 = ex + 2;
            int y1 = ey + 2;
            int x2 = ex + ENTITY_W - 2;
            int y2 = ey + ENTITY_H - 4;
            int scale = Math.max(24, (y2 - y1) / 2 - 4);
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    guiGraphics, x1, y1, x2, y2, scale,
                    0.0625F,
                    (float) mouseX, (float) mouseY,
                    preview
            );
        }

        // --- Inventory sub-panel ---
        int invPanelX = x + INV_X - 4;
        int invPanelY = y + INV_Y - 4;
        int invPanelW = 9 * 18 + 8;
        int invPanelH = 4 * 18 + 8 + 4; // 3 rows main + 1 hotbar + spacer
        NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, invPanelX, invPanelY, invPanelW, invPanelH);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                int sx = x + INV_X + c * 18;
                int sy = y + INV_Y + r * 18;
                guiGraphics.fill(sx, sy, sx + 16, sy + 16, SLOT_DARK);
            }
        }
        for (int c = 0; c < 9; c++) {
            int sx = x + INV_X + c * 18;
            int sy = y + HOTBAR_Y;
            guiGraphics.fill(sx, sy, sx + 16, sy + 16, SLOT_DARK);
        }

        // --- Offhand slot backdrop (right of hotbar) ---
        NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, x + OFFHAND_X - 4, y + OFFHAND_Y - 4, 18 + 8, 18 + 8);
        guiGraphics.fill(x + OFFHAND_X, y + OFFHAND_Y, x + OFFHAND_X + 16, y + OFFHAND_Y + 16, SLOT_DARK);
        NinePatchHelper.drawSlotFrame(guiGraphics, SLOT_FRAME_TEX, x + OFFHAND_X, y + OFFHAND_Y);

        // --- Top-right "customize target entity" detached tile (above top-right of main panel) ---
        int customizeX = x + w - 24;
        int customizeY = y - 20;
        NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, customizeX, customizeY, 24, 24);

        // --- Side toggle column (crafting + cosmetic icons) to the right of the inventory ---
        int toggleX = x + INV_X + 9 * 18 + 4;
        int toggleTopY = y + INV_Y - 4;
        NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, toggleX, toggleTopY, 22, 22);
        NinePatchHelper.drawPanel(guiGraphics, ACC_PANEL_TEX, toggleX, toggleTopY + 24, 22, 22);

        // --- Back button in top-right of entity preview frame ---
        int backBtnX = x + ENTITY_X + ENTITY_W - 14;
        int backBtnY = y + ENTITY_Y + 2;
        guiGraphics.fill(backBtnX, backBtnY, backBtnX + 12, backBtnY + 12, 0xFFC6C6C6);
        guiGraphics.fill(backBtnX + 1, backBtnY + 1, backBtnX + 11, backBtnY + 11, 0xFF8B8B8B);

        //--

        if (hoveredSlot != null && hoveredSlot instanceof AccessoriesBasedSlot slot && slot.isActive() && !slot.getItem().isEmpty()) {
            var positions = AccessoriesFunkyRenderingState.INSTANCE.getNotVeryNicePositions();

            var positionKey = slot.slotPath();

            if (positions.containsKey(positionKey)) {
                hoveredAccessoryPositons.add(positions.get(positionKey));

                var vec = positions.get(positionKey);

                if (!slot.isCosmetic && vec != null && (Accessories.config().screenOptions.hoveredOptions.line())) {
                    var start = new Vector3d(slot.x + this.leftPos + 17, slot.y + this.topPos + 9, 5000);
                    var vec3 = vec.add(0, 0, 5000);

                    linesToAccessoryPositions.add(new Line3d(start, vec3));}
            }
        }
    }

    /** Count of accessory slot pairs visible in the menu (one row per pair). */
    private int accessoryRowCount() {
        var menu = this.getMenu();
        int accStart = menu.startingAccessoriesSlot() + menu.addedArmorSlots();
        int pairCount = Math.max(0, (menu.slots.size() - accStart) / 2);
        return Math.max(1, (pairCount + ACC_COLS - 1) / ACC_COLS);
    }

    /** Paints a 1px vanilla-style inset frame (dark top/left, light bottom/right, mid border). */
    private static void drawInsetFrame(GuiGraphics g, int x, int y, int w, int h, int outer, int light, int mid) {
        // Outer dark border
        g.fill(x, y, x + w, y + 1, outer);
        g.fill(x, y + h - 1, x + w, y + h, outer);
        g.fill(x, y, x + 1, y + h, outer);
        g.fill(x + w - 1, y, x + w, y + h, outer);
        // Inner highlight on top/left, shadow on bottom/right for the inset look
        g.fill(x + 1, y + 1, x + w - 1, y + 2, mid);
        g.fill(x + 1, y + 1, x + 2, y + h - 1, mid);
        g.fill(x + 2, y + h - 2, x + w - 1, y + h - 1, light);
        g.fill(x + w - 2, y + 2, x + w - 1, y + h - 1, light);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateSlotDarkening(partialTick);

        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (Accessories.config().screenOptions.hoveredOptions.clickbait()) {
            hoveredAccessoryPositons.forEach(pos -> {
                        DrawUtils.blitSprite(
                                guiGraphics,
                                Accessories.of("highlight/clickbait"),
                                (int) pos.x - 128, (int) pos.y - 128, 450, 256, 256);
                    });
            hoveredAccessoryPositons.clear();
        }

        if (!linesToAccessoryPositions.isEmpty() || Accessories.config().screenOptions.hoveredOptions.line()) {
//            guiGraphics.drawSpecial(multiBufferSource -> {
//                var buf = multiBufferSource.getBuffer(RenderType.LINES);
//
//                var lastPose = guiGraphics.pose().last();
//
//                for (Line3d line : linesToAccessoryPositions) {
//                    var endPoint = line.p2();
//
//                    if (endPoint.x == 0 || endPoint.y == 0) continue;
//
//                    var normalVec = endPoint.sub(line.p1(), new Vector3d()).normalize().get(new Vector3f());
//
//                    double segments = Math.max(10, ((int) (line.p1().distance(line.p2()) * 10)) / 100);
//                    segments *= 2;
//
//                    var movement = (System.currentTimeMillis() / (segments * 1000) % 1);
//                    var delta = movement % (2 / (segments)) % segments;
//
//                    var firstVec = line.p1().get(new Vector3f());
//
//                    if (delta > 0.05) {
//                        DrawUtils.addToVertexBuffer(buf, firstVec, lastPose, normalVec);
//                        DrawUtils.addToVertexBuffer(buf, line.lerpPoint(delta - 0.05), lastPose, normalVec);
//                    }
//
//                    for (int i = 0; i < segments / 2; i++) {
//                        var delta1 = ((i * 2) / segments + movement) % 1;
//                        var delta2 = ((i * 2 + 1) / segments + movement) % 1;
//
//                        var pos1 = line.lerpPoint(delta1);
//                        var pos2 = (delta2 > delta1 ? line.lerpPoint(delta2) : line.p2().get(new Vector3f()));
//
//                        DrawUtils.addToVertexBuffer(buf, pos1, lastPose, normalVec);
//                        DrawUtils.addToVertexBuffer(buf, pos2, lastPose, normalVec);
//                    }
//                }
//            });

            minecraft.renderBuffers().bufferSource().endBatch();

            linesToAccessoryPositions.clear();
        }
    }

    //--

    public static final ComponentKey<FlowLayout> ARMOR_ENTITY = ComponentKey.of(FlowLayout.class, "armor_entity_layout");
    public static final ComponentKey<FlowLayout> SIDE_BAR = ComponentKey.of(FlowLayout.class, "side_bar_holder");
    public static final ComponentKey<FlowLayout> TOGGLE_PANEL = ComponentKey.of(FlowLayout.class, "accessories_toggle_panel");

    public static final ComponentKey<FlowLayout> GROUP_FILTER = ComponentKey.of(FlowLayout.class, "group_filter_holder");
    public static final ComponentKey<FlowLayout> BOTTOM_HOLDER = ComponentKey.of(FlowLayout.class, "bottom_component_holder");
    public static final ComponentKey<FlowLayout> CRAFTING_GRID = ComponentKey.of(FlowLayout.class, "crafting_grid_layout");
    public static final ComponentKey<com.ultra.megamod.lib.accessories.owo.ui.core.Component> CRAFTING_GRID_BTN = ComponentKey.of(com.ultra.megamod.lib.accessories.owo.ui.core.Component.class, "crafting_grid_btn");
    public static final ComponentKey<StackLayout> ENTITY_PANEL = ComponentKey.of(StackLayout.class, "entity_button_panel");

    @Override
    protected void build(FlowLayout rootComponent) {
        this.changedSlots.clear();

        this.getMenu().slots.forEach(this::disableSlot);

        //--

        rootComponent.allowOverflow(true)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .surface(Surface.VANILLA_TRANSLUCENT);

        var baseChildren = new ArrayList<com.ultra.megamod.lib.accessories.owo.ui.core.Component>();

        var accessoriesComponent = createAccessoriesComponent();

        //--

        var menu = this.getMenu();

        SlotGroupLoader.getValidGroups(this.getMenu().targetEntityDefaulted()).keySet().stream()
                .filter(group -> this.getDefaultedData(PlayerOptions.FILTERED_GROUPS).contains(group.name()))
                .forEach(menu::addSelectedGroup);

        //--

        var playerInv = ComponentUtils.createPlayerInv(5, menu.startingAccessoriesSlot(), this::slotAsComponent, this::enableSlot);

        this.setData(PlayerOptions.ADVANCED_SETTINGS, false);

        var offHandIndex = this.getMenu().startingAccessoriesSlot() - 1;

        this.enableSlot(offHandIndex);

        var offhandComponent = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(this.slotAsComponent(offHandIndex).margins(Insets.of(1)))
                .padding(Insets.of(7, 7, 7, 4))
                .allowOverflow(true);

        var craftingGridArea = CRAFTING_GRID.withId(
            Containers.verticalFlow(Sizing.content(), Sizing.content())
                .positioning(Positioning.absolute(162, -7))
                .configure((FlowLayout component) -> {
                    if (this.getDefaultedData(PlayerOptions.SHOW_CRAFTING_GRID)) {
                        component.child(createCraftingGrid());
                    }
                })
        );

        var bottomInvComponent = new FlowLayout(Sizing.content(), Sizing.content(), FlowLayout.Algorithm.HORIZONTAL){
            @Override
            public boolean isInBoundingBox(double x, double y) {
                if (super.isInBoundingBox(x, y) || offhandComponent.isInBoundingBox(x, y)) return true;

                if (!craftingGridArea.children().isEmpty()) {
                    return craftingGridArea.children().getFirst().isInBoundingBox(x, y);
                }

                return false;
            }

            @Override
            protected void parentUpdate(float delta, int mouseX, int mouseY) {
                AccessoriesScreen.this.isOutsideInvArea = !isInBoundingBox(mouseX, mouseY);
            }
        }.child(
                Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .child(offhandComponent)
                    .allowOverflow(true)
                    .positioning(Positioning.absolute(-(18 + 4 + 7), 51))
            )
            .child(
                BOTTOM_HOLDER.withId(
                    Containers.verticalFlow(Sizing.fixed(162), Sizing.fixed(76))
                        .child(playerInv)
                )
            )
            .child(craftingGridArea)
            .padding(Insets.of(7))
            .surface((ctx, component) -> {
//                            ComponentUtils.getPanelSurface().draw(ctx, component);
//                            ComponentUtils.BACKGROUND_SLOT_RENDERING_SURFACE.draw(ctx, component);

                var showCraftingGrid = this.getDefaultedData(PlayerOptions.SHOW_CRAFTING_GRID);
                var width = showCraftingGrid ? 238 : 198;

                DrawUtils.blit(
                        ctx,
                        Accessories.of("textures/gui/theme/" + ComponentUtils.checkMode("light", "dark") + "/player_inv/" + (showCraftingGrid ? "with" : "without") + "_crafting.png"),
                        component.x() - (18 + 4), component.y(), width, 90
                );
            })
            .allowOverflow(true)
            .id("bottom_inventory_section");

        baseChildren.add(bottomInvComponent);


        //--

        var primaryLayout = ARMOR_ENTITY.withId(
            (FlowLayout) Containers.horizontalFlow(Sizing.content(), Sizing.fixed(140))
                .gap(2)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        );

        {
            var armorSlotsLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                    .configure((FlowLayout layout) -> layout.allowOverflow(true));

            var outerLeftArmorLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(armorSlotsLayout);

            var cosmeticArmorSlotsLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                            .configure((FlowLayout layout) -> layout.allowOverflow(true));

            var outerRightArmorLayout = Containers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(cosmeticArmorSlotsLayout);

            for (int i = 0; i < menu.addedArmorSlots() / 2; i++) {
                var armor = menu.startingAccessoriesSlot() + (i * 2);
                var cosmeticArmor = armor + 1;

                this.enableSlot(armor);
                this.enableSlot(cosmeticArmor);

                armorSlotsLayout.child(this.slotAsComponent(armor).margins(Insets.of(1)));
                cosmeticArmorSlotsLayout.child(ComponentUtils.createSlotWithToggle((AccessoriesBasedSlot) this.menu.slots.get(cosmeticArmor), this::slotAsComponent).left());
            }

            //--

            var entityContainer = Containers.stack(Sizing.content(), Sizing.fixed(126 + 14))
                    .child(
                            Containers.verticalFlow(Sizing.content(), Sizing.content())
                                    .child(
                                            createEntityComponent()
                                    ).surface((ctx, component) -> {
                                        var sideBySideMode = this.getDefaultedData(PlayerOptions.SIDE_BY_SIDE_ENTITY);

                                        DrawUtils.blit(
                                                ctx,
                                                Accessories.of("textures/gui/theme/" + ComponentUtils.checkMode("light", "dark") + "/entity_view/" + (sideBySideMode ? "double" : "single") +  "/entity_background.png"),
                                                component.x(), component.y(), sideBySideMode ? 162 : 108, 126
                                        );
                                    })
                                    .id("entity_renderer_holder")
                    )
                    .child(
                            Containers.verticalFlow(Sizing.fixed(0), Sizing.fixed(0))
                                    .surface((ctx, component) -> {
                                        // TODO: MAKE NO EQUIPMENT SLOT VARIANT...
                                        var surfaceType = Math.max(1, Math.min((this.getMenu().addedArmorSlots() / 2), 4)) + "_slots";
                                        var sideBySideMode = this.getDefaultedData(PlayerOptions.SIDE_BY_SIDE_ENTITY);

                                        DrawUtils.blit(
                                                ctx,
                                                Accessories.of("textures/gui/theme/" + ComponentUtils.checkMode("light", "dark") + "/entity_view/" + (sideBySideMode ? "double" : "single") + "/" + surfaceType + ".png"),
                                                component.x() - 7, component.y() - 7, sideBySideMode ? 176 : 122, 140
                                        );
                                    })
                    )
                    .child(
                            outerLeftArmorLayout
                                    .configure((FlowLayout component) -> component.mouseScroll().subscribe((mouseX, mouseY, amount) -> true))
                                    .padding(Insets.of(7))
                                    .margins(Insets.left(-7))
                                    .positioning(Positioning.relative(0, 40))
                    )
                    .child(
                            outerRightArmorLayout
                                    .configure((FlowLayout component) -> component.mouseScroll().subscribe((mouseX, mouseY, amount) -> true))
                                    .surface(ComponentUtils.SPECTRUM_SLOT_OUTLINE)
                                    .padding(Insets.of(7))
                                    .margins(Insets.right(-7))
                                    .positioning(Positioning.relative(100, 40))
                    )
                    .child(
                            ComponentUtils.createIconButton(
                                    (btn) -> {
                                        showCosmeticState(!showCosmeticState());

                                        btn.tooltip(createToggleText("slot_cosmetics", false, showCosmeticState()));

                                        var component = rootComponent().childById(AccessoriesContainingLayout.class, AccessoriesContainingLayout.defaultID());

                                        if(component != null) component.onCosmeticToggle(showCosmeticState());
                                    },
                                    14,
                                    btn -> {
                                        btn.tooltip(createToggleText("slot_cosmetics", false, showCosmeticState()))
                                                .margins(Insets.of(2, 0, 3, 0));
                                    },
                                    (btn) -> {
                                        return Accessories.of("textures/gui/" + (showCosmeticState() ? "charm" : "cosmetic") + "_toggle_icon" + (btn.isHovered() ? "_hovered" : "") + ".png");
                                    }
                            ).positioning(Positioning.relative(0, 0))
                    )
                    .child(
                            ComponentUtils.createIconButton(
                                    btn -> {
                                        AccessoriesScreenTransitionHelper.openPrevScreen(Minecraft.getInstance().player, this.menu.targetEntityDefaulted(), prevScreen);
                                    },
                                    10,
                                    btn -> {
                                        btn.tooltip(Component.translatable(Accessories.translationKey("back.screen")))
                                                .margins(Insets.of(3, 0, 0, 3));
                                    },
                                    (btn) -> {
                                        return Accessories.of("textures/gui/accessories_back_icon" + (btn.isHovered() ? "_hovered" : "") + ".png");
                                    }).positioning(Positioning.relative(100, 0))
                    ).child(
                            ComponentUtils.createIconButton(
                                this::toggleAdvancedOptions,
                                14,
                                "advanced_options_btn",
                                btn -> {
                                    btn.tooltip(createToggleText("advanced_options", true, this.getDefaultedData(PlayerOptions.ADVANCED_SETTINGS)))
                                            .margins(Insets.of(0, 3, 0, 3));
                                },
                                (ctx, btn) -> {
                                    return Accessories.of("textures/gui/settings_icon" + (btn.isHovered() ? "_hovered" : "") + ".png");
                                }
                            ).positioning(Positioning.relative(100, 100))
                    );

            if(!Accessories.config().screenOptions.alwaysShowCraftingGrid()){
                entityContainer.child(
                    createCraftingToggleButton()
                );
            }

            primaryLayout.child(
                ENTITY_PANEL.withId(
                    entityContainer
                        .allowOverflow(true)
                        .padding(Insets.of(7))
                )
            );
        }

        baseChildren.add(primaryLayout);

        if(accessoriesComponent != null) {
            primaryLayout.child((this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION) ? 0 : 1), accessoriesComponent); //1,
        }

        var hasSideBar = false;

        if(accessoriesComponent != null || !this.getMenu().selectedGroups().isEmpty()) {
            var sideBarHolder = createSideBarOptions();

            if (sideBarHolder != null) {
                if ((boolean) this.getDefaultedData(PlayerOptions.SIDE_WIDGET_POSITION) == this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION)) {
                    primaryLayout.child(0, sideBarHolder);
                } else {
                    primaryLayout.child(sideBarHolder);
                }

                hasSideBar = true;
            }
        }

        setupPadding(accessoriesComponent, hasSideBar, primaryLayout);

        //--

        var baseLayout = Containers.verticalFlow(Sizing.content(), Sizing.content())
                .gap(2)
                .children(baseChildren.reversed())
                .allowOverflow(true);

        baseLayout.horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .positioning(Positioning.relative(50, 50));

        //--

        rootComponent.child(baseLayout);
    }

    public void setupPadding() {
        if (this.topComponent == null) return;

        var hasSideBar = SIDE_BAR.has(rootComponent());
        var primaryLayout = ARMOR_ENTITY.getFrom(rootComponent());

        setupPadding(this.topComponent, hasSideBar, primaryLayout);
    }

    public void setupPadding(AccessoriesContainingLayout<?> accessoriesComponent, boolean hasSideBar, ParentComponent primaryLayout) {
        if (this.getDefaultedData(PlayerOptions.ENTITY_CENTERED)) {
            // (((Accessories Component Width) + 3) | 0) + (120) + ((3 + (30)) | 0
            var padding = 0;

            var paddingOnTheRight = this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION);

            if (accessoriesComponent != null) {
                var operationValue = (this.getDefaultedData(PlayerOptions.SIDE_WIDGET_POSITION) ? 1 : -1);

                // 33

                int roundingOffset = 0;

                if (this.getDefaultedData(PlayerOptions.SIDE_WIDGET_POSITION)) {
                    roundingOffset = (this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION) ? -1 : -3);
                } else if(GROUP_FILTER.has(rootComponent()) && !this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION)) {
                    roundingOffset = -2;
                }

                padding = accessoriesComponent.getMaxPossibleWidth() + 3 + ((hasSideBar ? 35 : 0) * operationValue) + roundingOffset;

                primaryLayout.padding(
                        paddingOnTheRight
                                ? Insets.right(padding)
                                : Insets.left(padding)
                );
            }
        } else {
            primaryLayout.padding(Insets.none());
        }
    }

    @Nullable
    private AccessoriesContainingLayout<?> topComponent = null;

    public void rebuildEntityComponent() {
        var holder = this.component(FlowLayout.class, "entity_renderer_holder");

        holder.clearChildren();

        holder.child(createEntityComponent());

        rebuildComponentRectangles = true;
    }

    public com.ultra.megamod.lib.accessories.owo.ui.core.Component createEntityComponent() {
        var sideBySideView = this.getDefaultedData(PlayerOptions.SIDE_BY_SIDE_ENTITY);

        return InventoryEntityComponent.of(Sizing.fixed(sideBySideView ? 162 : 108), Sizing.fixed(126), this.getMenu().targetEntityDefaulted())
                .renderWrapping((ctx, component, renderCall) -> {
                    //ctx.enableScissor(component.x() + 24, component.y(), component.x() + component.width()  - 48, component.y() + component.height());
                    //ScissorStack.push(component.x() + 24, component.y(), component.width() - 48, component.height(), ctx);

                    AccessoriesFunkyRenderingState.INSTANCE.wrapEntityRendering(
                            component.x() + 24, component.y(), component.x() + component.width() - 24, component.y() + component.height(),
                            primaryEntityWrapCall -> {
                                primaryEntityWrapCall.accept(() -> {
                                    renderCall.getFirst().run();
                                });

                                if (renderCall.size() != 1) {
                                    renderCall.get(1).run();
                                }
                            });

                    //ScissorStack.pop();
                    //ctx.disableScissor();
                })
                .sideBySideMode(sideBySideView)
                .additionalOffset(sideBySideView ? 12 : 0)
                .startingRotation(this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION) ? -45 : 45)
                .scaleToFit(true)
                .allowMouseRotation(true)
                .lookAtCursor(Accessories.config().screenOptions.entityLooksAtMouseCursor())
                .id("entity_rendering_component");
    }

    public void rebuildAccessoriesComponent() {

        this.getMenu().getAccessoriesSlots().forEach(this::disableSlot);

        var primaryLayout = ARMOR_ENTITY.getFrom(rootComponent());

        //--

        var accessoriesLayout = primaryLayout.childById(AccessoriesContainingLayout.class, AccessoriesContainingLayout.defaultID());

        var accessoriesParent = accessoriesLayout.parent();

        if (accessoriesParent != null) {
            ComponentUtils.recursiveSearchSlots(accessoriesParent, slotComponent -> this.hideSlot(slotComponent.slot()));

            accessoriesParent.removeChild(accessoriesLayout);
        }

        //--

        var accessoriesComponent = createAccessoriesComponent();

        var hasSideBar = false;

        if (accessoriesComponent != null) {
            if(this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION)) {
                primaryLayout.child(0, accessoriesComponent);
            } else {
                primaryLayout.child(accessoriesComponent);
            }

            hasSideBar = swapOrCreateSideBarComponent();
        } else {
            if(this.getMenu().selectedGroups().isEmpty()) {
                var sideBarOptionsComponent = TOGGLE_PANEL.getFrom(primaryLayout);

                if (sideBarOptionsComponent != null) {
                    var sideParParent = sideBarOptionsComponent.parent();

                    if (sideParParent != null) sideParParent.removeChild(sideBarOptionsComponent);
                }
            } else {
                hasSideBar = swapOrCreateSideBarComponent();
            }
        }

        setupPadding(accessoriesComponent, hasSideBar, primaryLayout);

        toggleCraftingGrid();

        rebuildComponentRectangles = true;
    }

    public boolean swapOrCreateSideBarComponent() {
        if (this.topComponent == null && this.getMenu().selectedGroups().isEmpty()) return false;

        var armorAndEntityComp = ARMOR_ENTITY.getFrom(rootComponent());

        SIDE_BAR.removeFrom(armorAndEntityComp);

        var sideBarResultWidget = createSideBarOptions();

        rebuildComponentRectangles = true;

        if (sideBarResultWidget == null) return false;

        if ((boolean) this.getDefaultedData(PlayerOptions.SIDE_WIDGET_POSITION) == this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION)) {
            armorAndEntityComp.child(0, sideBarResultWidget);
        } else {
            armorAndEntityComp.child(sideBarResultWidget);
        }

        return true;
    }

    @Nullable
    private AccessoriesContainingLayout<?> createAccessoriesComponent() {
        this.topComponent = (this.getDefaultedData(PlayerOptions.WIDGET_TYPE) == 2)
                ? ScrollableAccessoriesLayout.createOrNull(this)
                : PaginatedAccessoriesLayout.createOrNull(this);

        return this.topComponent;
    }

    //--
    @Nullable
    private com.ultra.megamod.lib.accessories.owo.ui.core.Component createSideBarOptions() {
        var groupFilterComponent = createGroupFilters();

        if (groupFilterComponent == null) return null;

        var accessoriesTogglePanel = TOGGLE_PANEL.withId(Containers.verticalFlow(Sizing.content(), Sizing.content()));

        return SIDE_BAR.withId(
            Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(
                    accessoriesTogglePanel.child(groupFilterComponent)
                        .padding(Insets.of(7))
                        .surface((ctx, component) -> {
                            ComponentUtils.getPanelSurface().and(ComponentUtils.getPanelWithInset(6))
                                .draw(ctx, component);
                        })
                        .horizontalAlignment(HorizontalAlignment.CENTER)
                )
                .horizontalAlignment(this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION) ? HorizontalAlignment.LEFT : HorizontalAlignment.RIGHT)
        );
    }

    public void rebuildSideBarOptions() {
        if(this.getDefaultedData(PlayerOptions.SHOW_GROUP_FILTER)) {
            var panel = TOGGLE_PANEL.getFrom(rootComponent());

            if(panel != null) {
                var groupFilter = this.createGroupFilters();

                if (groupFilter != null) panel.child(groupFilter);
            }
        } else {
            GROUP_FILTER.removeFromRoot(rootComponent());
        }

        rebuildComponentRectangles = true;

        this.toggleCraftingGrid();
    }

    @Nullable
    private ExtendedScrollContainer groupFilterScrollable = null;

    @Nullable
    private com.ultra.megamod.lib.accessories.owo.ui.core.Component createGroupFilters() {
        if (!this.getDefaultedData(PlayerOptions.SHOW_GROUP_FILTER)) return null;

        var capability = ((com.ultra.megamod.lib.accessories.pond.AccessoriesAPIAccess) this.getMenu().targetEntityDefaulted()).accessoriesCapability();
        if (capability == null) return null;

        var groups = new ArrayList<>(SlotGroupLoader.getValidGroups(this.getMenu().targetEntityDefaulted()).keySet());
        if (groups.isEmpty()) return null;

        var usedSlots = this.getMenu().getUsedSlots();
        var groupButtons = new ArrayList<com.ultra.megamod.lib.accessories.owo.ui.core.Component>();

        // Filter Groups that have valid slots for the given target entity and are not from the unique slot API
        for (var group : groups) {
            var groupSlots = group.slots().stream()
                .filter(slotName -> {
                    if (UniqueSlotHandling.isUniqueSlot(slotName)) return false;

                    var container = capability.getContainers().get(slotName);

                    return container != null && container.getSize() > 0;
                })
                .map((slotName) -> SlotTypeLoader.INSTANCE.getSlotType(false, slotName))
                .collect(Collectors.toSet());

            if (groupSlots.isEmpty() || (usedSlots != null && groupSlots.stream().noneMatch(usedSlots::contains))) continue;

            groupButtons.add(ComponentUtils.createGroupToggle(this, group));
        }

        if (groupButtons.isEmpty()) return null;

        var baseButtonLayout = (ParentComponent) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .children(groupButtons)
                .gap(1);

        if(groupButtons.size() > 7) {
            this.groupFilterScrollable = new ExtendedScrollContainer<>(
                ScrollContainer.ScrollDirection.VERTICAL,
                Sizing.fixed(18 + 3),
                Sizing.fixed(108),
                baseButtonLayout.padding(Insets.of(1))
            ).configure((ExtendedScrollContainer<?> scrollContainer) -> {
                scrollContainer.oppositeScrollbar((boolean) this.getDefaultedData(PlayerOptions.SIDE_WIDGET_POSITION) == this.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION))
                    .scrollToAfterLayout(groupFilterScrollable != null ? groupFilterScrollable.getProgress() : 0)
                    .scrollbarThiccness(2)
                    .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0000000)))
                    .fixedScrollbarLength(16)
                    .padding(Insets.of(1, 2, 2, 1))
                    .id("group_filters_scrollable");
            });

            baseButtonLayout = this.groupFilterScrollable;
        } else {
            this.groupFilterScrollable = null;
            baseButtonLayout.padding(Insets.of(1, 2, 2, 2));
        }

        return GROUP_FILTER.withId(
            Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(
                    ComponentUtils.createIconButton(
                            (btn) -> {
                                this.getMenu().selectedGroups().clear();
                                this.rebuildAccessoriesComponent();
                            },
                            14,
                            (btn) -> {
                                btn.tooltip(Component.translatable(Accessories.translationKey("reset.group_filter")));
                            },
                            (btn) -> {
                                return Accessories.of("textures/gui/reset_icon" + (btn.isHovered() ? "_hovered" : "") + ".png");
                            })
                        .margins(Insets.of(3, 1, 0, 0))
                )
                .child(baseButtonLayout)
                .horizontalAlignment(HorizontalAlignment.CENTER)
        );
    }

    //--

    @Nullable
    private AccessoriesScreenSettingsLayout settingsLayout = null;

    private void swapBottomComponentHolder() {
        var holder = BOTTOM_HOLDER.getFrom(rootComponent());

        holder.clearChildren();

        if(this.getDefaultedData(PlayerOptions.ADVANCED_SETTINGS)) {
            for (int i = 0; i < menu.startingAccessoriesSlot() - 1; i++) this.disableSlot(i);

            settingsLayout = new AccessoriesScreenSettingsLayout(this, this::component).onChange(type -> {
                switch (type) {
                    case ACCESSORIES -> rebuildAccessoriesComponent();
                    case ENTITY -> rebuildEntityComponent();
                    case SIDE_BAR -> rebuildSideBarOptions();
                    case SLOTS -> getMenu().updateUsedSlots();
                }
            });

            holder.child(settingsLayout)
                    .surface(ComponentUtils.getInsetPanelSurface())
                    .padding(Insets.of(1));
        } else {
            settingsLayout = null;

            holder.child(ComponentUtils.createPlayerInv(5, menu.startingAccessoriesSlot(), this::slotAsComponent, this::enableSlot))
                    .surface(Surface.BLANK)
                    .padding(Insets.of(0));
        }

        rebuildComponentRectangles = true;
    }

    //--

    private void toggleCraftingGrid() {
        rebuildComponentRectangles = true;

        var gridHolder = CRAFTING_GRID.getFrom(rootComponent());

        if (gridHolder == null) throw new IllegalStateException("Unable to get crafting grid layout!");

        gridHolder.clearChildren();

        if (!this.getDefaultedData(PlayerOptions.SHOW_CRAFTING_GRID)) {
            gridHolder.margins(Insets.of(0));

            for (int i = 0; i < 5; i++) this.disableSlot(i);
        }

        if (!this.getDefaultedData(PlayerOptions.SHOW_CRAFTING_GRID)) return;

        gridHolder.child(createCraftingGrid());
    }

    private com.ultra.megamod.lib.accessories.owo.ui.core.Component createCraftingGrid() {
        return ComponentUtils.createCraftingComponent(0, this::slotAsComponent, this::enableSlot, true);
    }

    private com.ultra.megamod.lib.accessories.owo.ui.core.Component createCraftingToggleButton() {
        return ComponentUtils.createIconButton(
            btn -> {
                AccessoriesNetworking
                    .sendToServer(SyncOptionChange.of(PlayerOptions.SHOW_CRAFTING_GRID, this.menu.owner(), bl -> !bl));

                btn.tooltip(createToggleText("crafting_grid", true, this.setDataFrom(PlayerOptions.SHOW_CRAFTING_GRID, BooleanUnaryOperator.negation())));

                this.toggleCraftingGrid();
            },
            14,
            CRAFTING_GRID_BTN.id(),
            btn -> {
                btn.tooltip(createToggleText("crafting_grid", true, this.getDefaultedData(PlayerOptions.SHOW_CRAFTING_GRID)))
                    .margins(Insets.of(0, 3, 3, 0));
            },
            (ctx, btn) -> {
                return Accessories.of("textures/gui/crafting_toggle_icon" + (btn.isHovered() ? "_hovered" : "") + ".png");
            }).positioning(Positioning.relative(0, 100));
    }

    //--

    @Override
    public void onHolderChange(PlayerOption<?> option) {
        if (settingsLayout == null) return;

        settingsLayout.onHolderChange(option);

        if (!option.equals(PlayerOptions.SHOW_CRAFTING_GRID)) return;

        var buttonPanel = ENTITY_PANEL.getFrom(rootComponent());
        var craftingBtn = CRAFTING_GRID_BTN.getFrom(buttonPanel);

        if (craftingBtn != null && Accessories.config().screenOptions.alwaysShowCraftingGrid()) {
            buttonPanel.removeChild(craftingBtn);
        } else if (craftingBtn == null && Accessories.config().screenOptions.alwaysShowCraftingGrid()) {
            buttonPanel.child(buttonPanel.children().size() - 1, createCraftingToggleButton());
        }
    }

    private void toggleAdvancedOptions(ButtonComponent btn) {
        btn.tooltip(createToggleText("advanced_options", true, this.setDataFrom(PlayerOptions.ADVANCED_SETTINGS, BooleanUnaryOperator.negation())));

        this.swapBottomComponentHolder();
    }

    private static Component createToggleText(String type, boolean isTooltip, boolean value) {
        return Accessories.translation(type + ".toggle." + (value ? "enabled" : "disabled") + (isTooltip ? ".tooltip" : ""));
    }

    //--

    public ExtendedSlotComponent slotAsComponent(int index) {
        return new ExtendedSlotComponent(index);
    }

    public class ExtendedSlotComponent extends SlotComponent {

        protected final AccessoriesScreen screen;
        protected int index;

        protected ExtendedSlotComponent(int index) {
            super(index);

            this.screen = AccessoriesScreen.this;
            this.index = index;

            this.didDraw = true;
        }

        public final Slot slot() {
            return this.slot;
        }

        @Override
        public void dismount(DismountReason reason) {
            super.dismount(reason);

            if (reason == DismountReason.REMOVED) hideSlot(slot);
        }

        @Override
        public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            // DO NOT CALL SUPER AS SCISSOR ISSUES OCCUR

            this.didDraw = true;

            if (this.slot instanceof OwoSlotExtension owoSlot) owoSlot.owo$setDisabledOverride(false);
        }

        @Override
        public void drawTooltip(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
            var slot = this.slot();

            if(slot != null) {
                if (slot instanceof AccessoriesBasedSlot accessoriesInternalSlot) {
                    if (!ArmorSlotTypes.isArmorType(accessoriesInternalSlot.slotName())) {
                        AccessoriesScreenBase.FORCE_TOOLTIP_LEFT.setValue(screen.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION));
                    }
                } else if (slot instanceof ArmorSlot) {
                    AccessoriesScreenBase.FORCE_TOOLTIP_LEFT.setValue(true);
                } else if (slot.container instanceof TransientCraftingContainer || slot instanceof ResultSlot) {
                    if (!(boolean) screen.getDefaultedData(PlayerOptions.SHOW_GROUP_FILTER)) {
                        AccessoriesScreenBase.FORCE_TOOLTIP_LEFT.setValue(!(boolean) screen.getDefaultedData(PlayerOptions.MAIN_WIDGET_POSITION));
                    }
                }
            }

            super.drawTooltip(context, mouseX, mouseY, partialTicks, delta);

            AccessoriesScreenBase.FORCE_TOOLTIP_LEFT.setValue(false);
        }
    }

    //--

    public void showCosmeticState(boolean value) {
        this.setData(PlayerOptions.SHOW_COSMETIC_SLOTS, value);

        AccessoriesNetworking.sendToServer(PlayerOptions.SHOW_COSMETIC_SLOTS.toPacket(value));
    }

    public boolean showCosmeticState() {
        return this.getDefaultedData(PlayerOptions.SHOW_COSMETIC_SLOTS);
    }


    @Override
    public <T> Optional<T> getData(PlayerOption<T> option) {
        return option.getData(this.menu.owner())
            .or(() -> {
                var defaults = Accessories.config().screenOptions.defaultValues();

                return defaults != null ? defaults.getData(option) : Optional.<T>empty();
            });
    }

    @Override
    public <T> void setData(PlayerOption<T> option, T data) {
        option.setData(this.menu.owner(), data);
    }

    public <T> T setDataFrom(PlayerOption<T> option, UnaryOperator<T> operator) {
        var value = operator.apply(getData(option).orElseThrow());
        setData(option, value);
        return value;
    }
}
