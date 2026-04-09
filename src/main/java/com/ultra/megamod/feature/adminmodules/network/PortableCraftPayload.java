package com.ultra.megamod.feature.adminmodules.network;

import com.ultra.megamod.feature.adminmodules.AdminModuleManager;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PortableCraftPayload(String workstationType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PortableCraftPayload> TYPE =
        new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "portable_craft"));

    public static final StreamCodec<FriendlyByteBuf, PortableCraftPayload> STREAM_CODEC =
        new StreamCodec<FriendlyByteBuf, PortableCraftPayload>() {
            public PortableCraftPayload decode(FriendlyByteBuf buf) {
                return new PortableCraftPayload(buf.readUtf());
            }

            public void encode(FriendlyByteBuf buf, PortableCraftPayload payload) {
                buf.writeUtf(payload.workstationType());
            }
        };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(PortableCraftPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!AdminSystem.isAdmin(player)) return;

            ServerLevel level = (ServerLevel) player.level();
            AdminModuleManager mgr = AdminModuleManager.get();
            mgr.loadFromDisk(level);
            if (!mgr.isModuleEnabled("portable_crafting")) return;

            ContainerLevelAccess access = ContainerLevelAccess.NULL;
            switch (payload.workstationType()) {
                case "crafting_table" -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new CraftingMenu(id, inv, access),
                    Component.translatable("container.crafting")));
                case "smithing_table" -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new SmithingMenu(id, inv, access),
                    Component.translatable("container.upgrade")));
                case "anvil" -> player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new AnvilMenu(id, inv, access),
                    Component.translatable("container.repair")));
            }
        });
    }
}
