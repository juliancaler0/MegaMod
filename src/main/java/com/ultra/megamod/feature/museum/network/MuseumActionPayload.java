package com.ultra.megamod.feature.museum.network;

import com.ultra.megamod.feature.museum.MuseumData;
import com.ultra.megamod.feature.museum.paintings.MasterpiecePaintingItem;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MuseumActionPayload(String action, String data) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MuseumActionPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("megamod", "museum_action"));
    public static final StreamCodec<FriendlyByteBuf, MuseumActionPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, MuseumActionPayload>() {
        public MuseumActionPayload decode(FriendlyByteBuf buf) {
            return new MuseumActionPayload(buf.readUtf(), buf.readUtf());
        }
        public void encode(FriendlyByteBuf buf, MuseumActionPayload payload) {
            buf.writeUtf(payload.action());
            buf.writeUtf(payload.data());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(MuseumActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player p = context.player();
            if (!(p instanceof ServerPlayer)) return;
            ServerPlayer serverPlayer = (ServerPlayer) p;
            if ("quick_donate".equals(payload.action())) {
                handleQuickDonate(serverPlayer);
            }
        });
    }

    private static void handleQuickDonate(ServerPlayer player) {
        ServerLevel level = player.level();
        MuseumData data = MuseumData.get(level);
        UUID pid = player.getUUID();
        int donated = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();

            if (item instanceof SpawnEggItem spawnEgg) {
                String mobType = BuiltInRegistries.ENTITY_TYPE.getKey(spawnEgg.getType(stack)).toString();
                if (data.donateMob(pid, mobType)) {
                    stack.shrink(1);
                    donated++;
                }
            } else if (item instanceof MasterpiecePaintingItem masterpiece) {
                String artId = masterpiece.getVariantName();
                if (data.donateArt(pid, artId)) {
                    stack.shrink(1);
                    donated++;
                }
            } else if (item == Items.PAINTING) {
                if (data.donateArt(pid, "minecraft:painting")) {
                    stack.shrink(1);
                    donated++;
                }
            } else if (item == com.ultra.megamod.feature.museum.MuseumRegistry.CAPTURED_MOB_ITEM.get()) {
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag customTag = customData.copyTag();
                String mobType = customTag.getStringOr("megamod_captured_mob", "");
                if (!mobType.isEmpty() && data.donateMob(pid, mobType)) {
                    stack.shrink(1);
                    donated++;
                }
            } else {
                String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
                String bucketMob = getMobBucketType(itemId);
                if (bucketMob != null) {
                    if (data.donateMob(pid, bucketMob)) {
                        stack.shrink(1);
                        donated++;
                    }
                } else if (data.donateItem(pid, itemId)) {
                    stack.shrink(1);
                    donated++;
                }
            }
        }
        if (donated > 0) {
            player.sendSystemMessage(Component.literal("Quick donated " + donated + " new item(s) to your museum!").withStyle(ChatFormatting.GREEN));
            // Refresh the curator GUI with updated data
            String json = serializeMuseumData(data, pid);
            PacketDistributor.sendToPlayer(player, new OpenMuseumPayload(json));
        } else {
            player.sendSystemMessage(Component.literal("No new items to donate.").withStyle(ChatFormatting.YELLOW));
        }
    }

    private static String getMobBucketType(String itemId) {
        return switch (itemId) {
            case "minecraft:pufferfish_bucket" -> "minecraft:pufferfish";
            case "minecraft:salmon_bucket" -> "minecraft:salmon";
            case "minecraft:cod_bucket" -> "minecraft:cod";
            case "minecraft:tropical_fish_bucket" -> "minecraft:tropical_fish";
            case "minecraft:axolotl_bucket" -> "minecraft:axolotl";
            case "minecraft:tadpole_bucket" -> "minecraft:tadpole";
            default -> null;
        };
    }

    private static String serializeMuseumData(MuseumData data, UUID pid) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"items\":").append(setToJsonArray(data.getDonatedItems(pid))).append(",");
        sb.append("\"mobs\":").append(setToJsonArray(data.getDonatedMobs(pid))).append(",");
        sb.append("\"art\":").append(setToJsonArray(data.getDonatedArt(pid))).append(",");
        sb.append("\"achievements\":").append(setToJsonArray(data.getCompletedAchievements(pid)));
        sb.append("}");
        return sb.toString();
    }

    private static String setToJsonArray(Set<String> set) {
        if (set == null || set.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String s : set) {
            if (!first) sb.append(",");
            sb.append("\"").append(s).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
}
