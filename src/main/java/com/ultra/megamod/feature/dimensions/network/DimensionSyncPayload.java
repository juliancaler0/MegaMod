/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.resources.Identifier
 *  net.neoforged.neoforge.network.handling.IPayloadContext
 */
package com.ultra.megamod.feature.dimensions.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DimensionSyncPayload(String dimensionId, boolean inPocket) implements CustomPacketPayload
{
    public static volatile String clientDimensionId = "minecraft:overworld";
    public static volatile boolean clientInPocket = false;
    public static final CustomPacketPayload.Type<DimensionSyncPayload> TYPE = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath((String)"megamod", (String)"dimension_sync"));
    public static final StreamCodec<FriendlyByteBuf, DimensionSyncPayload> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, DimensionSyncPayload>(){

        public DimensionSyncPayload decode(FriendlyByteBuf buf) {
            String dimensionId = buf.readUtf();
            boolean inPocket = buf.readBoolean();
            return new DimensionSyncPayload(dimensionId, inPocket);
        }

        public void encode(FriendlyByteBuf buf, DimensionSyncPayload payload) {
            buf.writeUtf(payload.dimensionId());
            buf.writeBoolean(payload.inPocket());
        }
    };

    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Transition overlay data
    public static volatile String transitionTitle = "";
    public static volatile String transitionSubtitle = "";
    public static volatile int transitionColor = 0xFF000000;
    public static volatile long transitionStartMs = 0;
    public static final long TRANSITION_DURATION_MS = 3000; // 3 seconds

    public static void handleOnClient(DimensionSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            String oldDim = clientDimensionId;
            clientDimensionId = payload.dimensionId();
            clientInPocket = payload.inPocket();

            // Trigger transition overlay when dimension changes
            if (!payload.dimensionId().equals(oldDim)) {
                String dimPath = payload.dimensionId();
                if (dimPath.equals("arena")) {
                    transitionTitle = "Entering the Arena";
                    transitionSubtitle = "Fight for glory!";
                    transitionColor = 0xFF2E0A0A; // deep red
                } else if (dimPath.equals("boss_rush")) {
                    transitionTitle = "Boss Rush";
                    transitionSubtitle = "Defeat all 8 bosses. No mercy.";
                    transitionColor = 0xFF2E0A1A; // dark crimson
                } else if (dimPath.contains("dungeon")) {
                    transitionTitle = "Entering the Dungeon";
                    transitionSubtitle = "Prepare yourself...";
                    transitionColor = 0xFF1A0A2E; // deep purple
                } else if (dimPath.contains("museum")) {
                    transitionTitle = "Welcome to the Museum";
                    transitionSubtitle = "Your personal collection awaits";
                    transitionColor = 0xFF0A1A2E; // deep blue
                } else if (dimPath.contains("casino")) {
                    transitionTitle = "Entering the Casino";
                    transitionSubtitle = "Feeling lucky?";
                    transitionColor = 0xFF2E1A0A; // deep gold-brown
                } else if (dimPath.contains("trading")) {
                    transitionTitle = "Trade Floor";
                    transitionSubtitle = "Meet, barter, and make deals";
                    transitionColor = 0xFF2E1A00; // deep orange-brown
                } else if (dimPath.contains("resource")) {
                    transitionTitle = "Resource Dimension";
                    transitionSubtitle = "Gather what you can before it resets";
                    transitionColor = 0xFF0A2E0A; // deep green
                } else if (dimPath.contains("overworld")) {
                    transitionTitle = "Returning Home";
                    transitionSubtitle = "";
                    transitionColor = 0xFF0A0A0A; // dark
                } else {
                    transitionTitle = "";
                    transitionSubtitle = "";
                }
                if (!transitionTitle.isEmpty()) {
                    transitionStartMs = System.currentTimeMillis();
                }
            }
        });
    }
}

