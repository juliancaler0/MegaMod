package com.ultra.megamod.mixin;

// SortAlgorithm and SortActionPayload accessed lazily to avoid class loading issues in mixin
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin into AbstractContainerScreen to add a visual sort button to all container GUIs.
 * The button appears at the top-right of the container area (next to the search bar)
 * and cycles through sort algorithms on each click.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenSortMixin {

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Shadow protected int imageWidth;

    @Unique
    private int megamod$algorithmIndex = 0;

    @Unique
    private static final String[] ALGORITHM_NAMES = {"NAME", "ID", "CATEGORY", "COUNT", "RARITY"};
    @Unique
    private static final String[] ALGORITHM_DISPLAY = {"Name", "ID", "Category", "Count", "Rarity"};

    @Unique
    private static final int BUTTON_W = 16;
    @Unique
    private static final int BUTTON_H = 14;

    @Unique
    private int megamod$btnX() { return leftPos + imageWidth - BUTTON_W - 2; }
    @Unique
    private int megamod$btnY() { return topPos - BUTTON_H - 4; }

    /**
     * Render the sort button at the top-right of the container area.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void megamod$renderSortButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // Skip creative inventory
        if ((Object) this instanceof CreativeModeInventoryScreen) return;

        int btnX = megamod$btnX();
        int btnY = megamod$btnY();

        boolean hovered = mouseX >= btnX && mouseX < btnX + BUTTON_W && mouseY >= btnY && mouseY < btnY + BUTTON_H;
        int bgColor = hovered ? 0xFF5A5A8A : 0xFF3A3A5A;
        int borderColor = hovered ? 0xFF8888CC : 0xFF6666AA;

        // Button background
        graphics.fill(btnX, btnY, btnX + BUTTON_W, btnY + BUTTON_H, bgColor);

        // Border
        graphics.fill(btnX, btnY, btnX + BUTTON_W, btnY + 1, borderColor);
        graphics.fill(btnX, btnY + BUTTON_H - 1, btnX + BUTTON_W, btnY + BUTTON_H, borderColor);
        graphics.fill(btnX, btnY, btnX + 1, btnY + BUTTON_H, borderColor);
        graphics.fill(btnX + BUTTON_W - 1, btnY, btnX + BUTTON_W, btnY + BUTTON_H, borderColor);

        // Sort icon
        Minecraft mc = Minecraft.getInstance();
        graphics.drawCenteredString(mc.font, "\u2195", btnX + BUTTON_W / 2, btnY + 3, 0xFFFFFFFF);

        // Tooltip on hover
        if (hovered) {
            String algoName = ALGORITHM_DISPLAY[megamod$algorithmIndex % ALGORITHM_DISPLAY.length];
            String line1 = "Sorting: " + algoName;
            String line2 = "Click to sort & cycle";
            int tw = Math.max(mc.font.width(line1), mc.font.width(line2)) + 6;
            int tx = mouseX + 8;
            int ty = mouseY - 24;
            // Clamp tooltip to screen bounds
            int screenW = mc.getWindow().getGuiScaledWidth();
            if (tx + tw + 1 > screenW) tx = mouseX - tw - 4;
            if (ty < 0) ty = mouseY + 12;
            graphics.fill(tx - 1, ty - 1, tx + tw + 1, ty + 23, 0xFF000000);
            graphics.fill(tx, ty, tx + tw, ty + 22, 0xFF1A1A2A);
            graphics.drawString(mc.font, line1, tx + 3, ty + 2, 0xFFFFD700, false);
            graphics.drawString(mc.font, line2, tx + 3, ty + 12, 0xFFAAAAAA, false);
        }
    }

    /**
     * Handle mouse click on the sort button.
     * Uses event.x()/y() for accurate GUI-scaled coordinates.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void megamod$onMouseClicked(MouseButtonEvent event, boolean consumed, CallbackInfoReturnable<Boolean> cir) {
        // Skip creative inventory
        if ((Object) this instanceof CreativeModeInventoryScreen) return;
        if (event.button() != 0) return; // Left click only

        double mouseX = event.x();
        double mouseY = event.y();

        int btnX = megamod$btnX();
        int btnY = megamod$btnY();

        if (mouseX >= btnX && mouseX < btnX + BUTTON_W && mouseY >= btnY && mouseY < btnY + BUTTON_H) {
            // Send sort action with current algorithm
            String sortType = ALGORITHM_NAMES[megamod$algorithmIndex % ALGORITHM_NAMES.length];

            ClientPacketDistributor.sendToServer(
                (CustomPacketPayload) new com.ultra.megamod.feature.sorting.network.SortActionPayload(sortType),
                (CustomPacketPayload[]) new CustomPacketPayload[0]
            );

            // Cycle to next algorithm
            megamod$algorithmIndex = (megamod$algorithmIndex + 1) % ALGORITHM_NAMES.length;

            // Play click sound
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
            }

            cir.setReturnValue(true);
        }
    }
}
