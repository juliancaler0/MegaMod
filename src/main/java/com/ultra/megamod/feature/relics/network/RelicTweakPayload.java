/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.relics.network;

import com.ultra.megamod.feature.relics.RelicItem;
import com.ultra.megamod.feature.relics.data.RelicAbility;
import com.ultra.megamod.feature.relics.data.RelicData;
import com.ultra.megamod.feature.relics.data.RelicStat;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RelicTweakPayload(String slotName, String abilityName, String operation) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<RelicTweakPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"relic_tweak"));
    public static final StreamCodec<FriendlyByteBuf, RelicTweakPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, RelicTweakPayload>(){

        public RelicTweakPayload decode(FriendlyByteBuf buf) {
            String slotName = buf.readUtf();
            String abilityName = buf.readUtf();
            String operation = buf.readUtf();
            return new RelicTweakPayload(slotName, abilityName, operation);
        }

        public void encode(FriendlyByteBuf buf, RelicTweakPayload payload) {
            buf.writeUtf(payload.slotName());
            buf.writeUtf(payload.abilityName());
            buf.writeUtf(payload.operation());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(RelicTweakPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player patt0$temp = context.player();
            if (!(patt0$temp instanceof ServerPlayer)) {
                return;
            }
            ServerPlayer player = (ServerPlayer)patt0$temp;
            ItemStack stack = RelicTweakPayload.findRelicStack(player, payload.slotName());
            if (stack == null || stack.isEmpty()) {
                return;
            }
            Item patt1$temp = stack.getItem();
            if (!(patt1$temp instanceof RelicItem)) {
                return;
            }
            RelicItem relicItem = (RelicItem)patt1$temp;
            if (!RelicData.isInitialized(stack)) {
                return;
            }
            List<RelicAbility> abilities = relicItem.getAbilities();
            RelicAbility targetAbility = null;
            for (RelicAbility ability : abilities) {
                if (!ability.name().equals(payload.abilityName())) continue;
                targetAbility = ability;
                break;
            }
            if (targetAbility == null) {
                return;
            }
            int relicLevel = RelicData.getLevel(stack);
            if (!RelicData.isAbilityUnlocked(relicLevel, targetAbility, abilities)) {
                return;
            }
            switch (payload.operation()) {
                case "upgrade": {
                    RelicTweakPayload.handleUpgrade(player, stack, abilities, targetAbility);
                    break;
                }
                case "reroll": {
                    RelicTweakPayload.handleReroll(player, stack, targetAbility);
                    break;
                }
                case "reset": {
                    RelicTweakPayload.handleReset(player, stack, abilities, targetAbility);
                }
            }
        });
    }

    private static void handleUpgrade(ServerPlayer player, ItemStack stack, List<RelicAbility> abilities, RelicAbility ability) {
        int statCount;
        int unspentPoints = RelicData.getUnspentPoints(stack, abilities);
        if (unspentPoints <= 0) {
            return;
        }
        int currentPoints = RelicData.getAbilityPoints(stack, ability.name());
        int xpCost = (currentPoints + 1) * (statCount = Math.max(1, ability.stats().size())) * 15;
        if (player.totalExperience < xpCost) {
            return;
        }
        player.giveExperiencePoints(-xpCost);
        RelicData.setAbilityPoints(stack, ability.name(), currentPoints + 1);
    }

    private static void handleReroll(ServerPlayer player, ItemStack stack, RelicAbility ability) {
        int statCount = Math.max(1, ability.stats().size());
        int xpCost = 100 / statCount;
        if (player.totalExperience < xpCost) {
            return;
        }
        player.giveExperiencePoints(-xpCost);
        for (RelicStat stat : ability.stats()) {
            RelicData.rerollStat(stack, ability.name(), stat, player.level().random);
        }
    }

    private static void handleReset(ServerPlayer player, ItemStack stack, List<RelicAbility> abilities, RelicAbility ability) {
        int currentPoints = RelicData.getAbilityPoints(stack, ability.name());
        if (currentPoints <= 0) {
            return;
        }
        int xpCost = currentPoints * 50;
        if (player.totalExperience < xpCost) {
            return;
        }
        player.giveExperiencePoints(-xpCost);
        RelicData.setAbilityPoints(stack, ability.name(), 0);
    }

    private static ItemStack findRelicStack(ServerPlayer player, String slotName) {
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof RelicItem) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty() && offHand.getItem() instanceof RelicItem) {
            return offHand;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof RelicItem)) continue;
            return stack;
        }
        return null;
    }
}

