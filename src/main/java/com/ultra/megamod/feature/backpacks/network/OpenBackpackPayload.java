package com.ultra.megamod.feature.backpacks.network;

import com.ultra.megamod.feature.backpacks.BackpackItem;
import com.ultra.megamod.feature.backpacks.BackpackTier;
import com.ultra.megamod.feature.backpacks.menu.BackpackMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenBackpackPayload(int slotIndex) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenBackpackPayload> TYPE =
        new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "open_backpack"));

    public static final StreamCodec<FriendlyByteBuf, OpenBackpackPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, OpenBackpackPayload>() {
            public OpenBackpackPayload decode(FriendlyByteBuf buf) {
                return new OpenBackpackPayload(buf.readInt());
            }

            public void encode(FriendlyByteBuf buf, OpenBackpackPayload payload) {
                buf.writeInt(payload.slotIndex());
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(OpenBackpackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            int slotIdx = payload.slotIndex();
            ItemStack backpackStack = ItemStack.EMPTY;

            if (slotIdx == -1) {
                // Open equipped backpack (worn on back)
                backpackStack = com.ultra.megamod.feature.backpacks.BackpackWearableManager.getEquipped(player.getUUID());
            } else if (slotIdx >= 0 && slotIdx < player.getInventory().getContainerSize()) {
                backpackStack = player.getInventory().getItem(slotIdx);
            }

            if (backpackStack.isEmpty() || !(backpackStack.getItem() instanceof BackpackItem bpItem)) return;

            BackpackItem.ensureInitialized(backpackStack);
            BackpackTier tier = bpItem.getTier(backpackStack);
            SimpleContainer container = BackpackMenu.loadFromItemStack(backpackStack, tier);
            SimpleContainer toolContainer = BackpackMenu.loadToolsFromItemStack(backpackStack, tier);
            com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager upgradeMgr = new com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager(tier);
            upgradeMgr.initializeFromStack(backpackStack);
            final int finalSlotIdx = slotIdx;

            player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new BackpackMenu(id, inv, container, toolContainer, upgradeMgr, tier.ordinal(), finalSlotIdx),
                Component.literal(bpItem.getVariant().getDisplayName() + " Backpack")
            ), buf -> buf.writeInt(tier.ordinal()));
        });
    }
}
