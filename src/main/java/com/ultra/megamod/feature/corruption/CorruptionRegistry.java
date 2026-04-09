package com.ultra.megamod.feature.corruption;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CorruptionRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");

    /**
     * Corruption Shard -- dropped by corrupted mobs, used in alchemy recipes and purge totem crafting.
     */
    public static final DeferredItem<Item> CORRUPTION_SHARD = ITEMS.registerItem("corruption_shard",
            props -> new Item(props.stacksTo(64)) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                            Consumer<Component> tooltip, TooltipFlag flag) {
                    super.appendHoverText(stack, context, display, tooltip, flag);
                    tooltip.accept(Component.literal("A fragment of concentrated corruption").withStyle(ChatFormatting.DARK_PURPLE));
                    tooltip.accept(Component.empty());
                    tooltip.accept(Component.literal("Dropped by corrupted mobs").withStyle(ChatFormatting.GRAY));
                    tooltip.accept(Component.literal("Used in alchemy and purge rituals").withStyle(ChatFormatting.DARK_GRAY));
                }

                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            });

    /**
     * Purge Totem -- consumable item that initiates a purge event when used in a corrupted zone.
     * Right-click to activate. Consumed on use.
     */
    public static final DeferredItem<Item> PURGE_TOTEM = ITEMS.registerItem("purge_totem",
            props -> new Item(props.stacksTo(1).rarity(Rarity.EPIC)) {
                @Override
                public InteractionResult use(Level level, Player player, InteractionHand hand) {
                    if (level.isClientSide()) return InteractionResult.SUCCESS;

                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    ServerLevel serverLevel = serverPlayer.level();

                    // Check if player is in a corrupted zone
                    CorruptionManager cm = CorruptionManager.get(serverLevel);
                    CorruptionManager.CorruptionZone targetZone = cm.getZoneAt(serverPlayer.blockPosition());

                    if (targetZone == null) {
                        serverPlayer.displayClientMessage(
                                Component.literal("You must be in a corrupted zone to use the Purge Totem!")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                        return InteractionResult.FAIL;
                    }

                    // Check if purge already active
                    PurgeManager pm = PurgeManager.get(serverLevel);
                    if (pm.hasPurgeActive()) {
                        serverPlayer.displayClientMessage(
                                Component.literal("A purge is already in progress! Wait for it to end.")
                                        .withStyle(ChatFormatting.YELLOW),
                                true
                        );
                        return InteractionResult.FAIL;
                    }

                    // Start the purge
                    PurgeManager.PurgeEvent purge = pm.startPurge(targetZone.zoneId, serverPlayer);

                    if (purge == null) {
                        serverPlayer.displayClientMessage(
                                Component.literal("Failed to start purge!")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                        return InteractionResult.FAIL;
                    }

                    // Consume the item
                    ItemStack stack = player.getItemInHand(hand);
                    stack.shrink(1);

                    // Play a dramatic sound
                    level.playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.TOTEM_USE,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.5f);

                    return InteractionResult.SUCCESS;
                }

                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                            Consumer<Component> tooltip, TooltipFlag flag) {
                    super.appendHoverText(stack, context, display, tooltip, flag);
                    tooltip.accept(Component.literal("An ancient totem of purification").withStyle(ChatFormatting.LIGHT_PURPLE));
                    tooltip.accept(Component.empty());
                    tooltip.accept(Component.literal("Right-click in a corrupted zone").withStyle(ChatFormatting.GRAY));
                    tooltip.accept(Component.literal("to begin a purge event!").withStyle(ChatFormatting.GRAY));
                    tooltip.accept(Component.empty());
                    tooltip.accept(Component.literal("Kill all corrupted mobs within").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.accept(Component.literal("the time limit to succeed.").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.accept(Component.empty());
                    tooltip.accept(Component.literal("Consumed on use").withStyle(ChatFormatting.RED));
                }

                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            });

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == ResourceKey.create(
                    (ResourceKey) Registries.CREATIVE_MODE_TAB,
                    (Identifier) Identifier.fromNamespaceAndPath("megamod", "megamod_tab"))) {
                event.accept((ItemLike) CORRUPTION_SHARD.get());
                event.accept((ItemLike) PURGE_TOTEM.get());
            }
        });
    }
}
