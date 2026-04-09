package com.ultra.megamod.feature.relics.research;

import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import com.ultra.megamod.feature.relics.data.WeaponRarity;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Set;

public class RerollPayload {

    private static final Set<String> VALID_MATERIALS = Set.of(
        "megamod:cerulean_ingot",
        "megamod:crystalline_shard",
        "megamod:spectral_silk",
        "megamod:umbra_ingot",
        "megamod:void_shard"
    );

    public record OpenRerollPayload(boolean dummy) implements CustomPacketPayload {
        public static volatile OpenRerollPayload lastPayload = null;

        public static final CustomPacketPayload.Type<OpenRerollPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "open_reroll"));
        public static final StreamCodec<FriendlyByteBuf, OpenRerollPayload> STREAM_CODEC =
            new StreamCodec<>() {
                public OpenRerollPayload decode(FriendlyByteBuf buf) {
                    return new OpenRerollPayload(buf.readBoolean());
                }
                public void encode(FriendlyByteBuf buf, OpenRerollPayload payload) {
                    buf.writeBoolean(payload.dummy());
                }
            };

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handleOnClient(OpenRerollPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> lastPayload = payload);
        }
    }

    public record RerollActionPayload(String action) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<RerollActionPayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "reroll_action"));
        public static final StreamCodec<FriendlyByteBuf, RerollActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                public RerollActionPayload decode(FriendlyByteBuf buf) {
                    return new RerollActionPayload(buf.readUtf());
                }
                public void encode(FriendlyByteBuf buf, RerollActionPayload payload) {
                    buf.writeUtf(payload.action());
                }
            };

        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

        public static void handleOnServer(RerollActionPayload payload, IPayloadContext context) {
            context.enqueueWork(() -> {
                Player p = context.player();
                if (!(p instanceof ServerPlayer player)) return;
                if (!"reroll".equals(payload.action())) return;

                ItemStack heldItem = player.getMainHandItem();
                boolean isWeapon = WeaponStatRoller.isWeaponInitialized(heldItem);
                boolean isArmor = ArmorStatRoller.isArmorInitialized(heldItem);
                boolean isJewelry = heldItem.getItem() instanceof com.ultra.megamod.feature.relics.weapons.RpgJewelryItem;
                if (heldItem.isEmpty() || (!isWeapon && !isArmor && !isJewelry)) {
                    player.sendSystemMessage(Component.literal("Hold a weapon, armor, or jewelry with stat modifiers in your main hand!").withStyle(ChatFormatting.RED));
                    return;
                }

                WeaponRarity rarity;
                if (isWeapon) rarity = WeaponStatRoller.getRarity(heldItem);
                else if (isArmor) rarity = ArmorStatRoller.getRarity(heldItem);
                else rarity = WeaponRarity.fromOrdinal(
                    ((net.minecraft.world.item.component.CustomData) heldItem.getOrDefault(
                        net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY)).copyTag().getIntOr("jewelry_rarity", 0));
                int cost = getRerollCost(rarity);

                ServerLevel level = (ServerLevel) player.level();
                EconomyManager eco = EconomyManager.get(level);
                int wallet = eco.getWallet(player.getUUID());
                int bank = eco.getBank(player.getUUID());
                int totalCoins = wallet + bank;

                if (totalCoins < cost) {
                    player.sendSystemMessage(Component.literal("Not enough MegaCoins! Need " + cost + " MC.").withStyle(ChatFormatting.RED));
                    return;
                }

                // Find and consume material
                int materialSlot = findMaterialSlot(player);
                if (materialSlot < 0) {
                    player.sendSystemMessage(Component.literal("You need a dungeon material in your inventory!").withStyle(ChatFormatting.RED));
                    return;
                }

                // Deduct MegaCoins (from wallet first, then bank)
                int remaining = cost;
                if (wallet >= remaining) {
                    eco.addWallet(player.getUUID(), -remaining);
                } else {
                    eco.addWallet(player.getUUID(), -wallet);
                    remaining -= wallet;
                    eco.setBank(player.getUUID(), bank - remaining);
                }

                // Consume material
                ItemStack material = player.getInventory().getItem(materialSlot);
                material.shrink(1);
                player.getInventory().setItem(materialSlot, material);

                // Reroll stats (keep rarity, re-randomize bonuses)
                if (isWeapon) {
                    WeaponStatRoller.rerollBonuses(heldItem, level.random);
                } else if (isArmor) {
                    ArmorStatRoller.rerollBonuses(heldItem, level.random);
                } else if (isJewelry) {
                    // Clear initialized flag so RpgJewelryItem re-rolls on next tick
                    net.minecraft.nbt.CompoundTag tag = ((net.minecraft.world.item.component.CustomData) heldItem.getOrDefault(
                        net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.EMPTY)).copyTag();
                    tag.putBoolean("jewelry_stats_initialized", false);
                    heldItem.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                        net.minecraft.world.item.component.CustomData.of(tag));
                }

                String itemType = isWeapon ? "Weapon" : isArmor ? "Armor" : "Jewelry";
                player.sendSystemMessage(Component.literal(itemType + " stats rerolled!").withStyle(ChatFormatting.GREEN));
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0f, 1.2f);
            });
        }

        private static int findMaterialSlot(ServerPlayer player) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                if (VALID_MATERIALS.contains(itemId)) {
                    return i;
                }
            }
            return -1;
        }
    }

    public static int getRerollCost(WeaponRarity rarity) {
        return switch (rarity) {
            case COMMON -> 50;
            case UNCOMMON -> 150;
            case RARE -> 400;
            case MYTHIC -> 1000;
            case LEGENDARY -> 2500;
        };
    }
}
