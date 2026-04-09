package com.ultra.megamod.feature.hud.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record TrackerSyncPayload(List<BountyInfo> bounties, List<QuestInfo> quests,
                                  List<ProgressQuestInfo> trackedQuests) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TrackerSyncPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "quest_tracker"));

    public record BountyInfo(String itemName, int quantity) {}
    public record QuestInfo(String title, int difficulty) {}
    public record ProgressQuestInfo(String title, String taskDesc, int progress, int target) {}

    public static volatile List<BountyInfo> clientBounties = List.of();
    public static volatile List<QuestInfo> clientQuests = List.of();
    public static volatile List<ProgressQuestInfo> clientTrackedQuests = List.of();

    public static final StreamCodec<FriendlyByteBuf, TrackerSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public TrackerSyncPayload decode(FriendlyByteBuf buf) {
            int bc = buf.readVarInt();
            List<BountyInfo> bounties = new ArrayList<>();
            for (int i = 0; i < bc; i++) bounties.add(new BountyInfo(buf.readUtf(), buf.readVarInt()));
            int qc = buf.readVarInt();
            List<QuestInfo> quests = new ArrayList<>();
            for (int i = 0; i < qc; i++) quests.add(new QuestInfo(buf.readUtf(), buf.readVarInt()));
            int tc = buf.readVarInt();
            List<ProgressQuestInfo> tracked = new ArrayList<>();
            for (int i = 0; i < tc; i++) tracked.add(new ProgressQuestInfo(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readVarInt()));
            return new TrackerSyncPayload(bounties, quests, tracked);
        }

        @Override
        public void encode(FriendlyByteBuf buf, TrackerSyncPayload payload) {
            buf.writeVarInt(payload.bounties().size());
            for (BountyInfo b : payload.bounties()) { buf.writeUtf(b.itemName()); buf.writeVarInt(b.quantity()); }
            buf.writeVarInt(payload.quests().size());
            for (QuestInfo q : payload.quests()) { buf.writeUtf(q.title()); buf.writeVarInt(q.difficulty()); }
            buf.writeVarInt(payload.trackedQuests().size());
            for (ProgressQuestInfo t : payload.trackedQuests()) { buf.writeUtf(t.title()); buf.writeUtf(t.taskDesc()); buf.writeVarInt(t.progress()); buf.writeVarInt(t.target()); }
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handleOnClient(TrackerSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            clientBounties = List.copyOf(payload.bounties());
            clientQuests = List.copyOf(payload.quests());
            clientTrackedQuests = List.copyOf(payload.trackedQuests());
        });
    }
}
