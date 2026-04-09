package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload: colony management actions.
 * Sent when a player performs colony-level operations from the Town Hall GUI.
 *
 * <p>Supported actions (via {@code action} field):
 * <ul>
 *   <li>{@code "rename"} - rename the colony. jsonData: {"name":"New Name"}</li>
 *   <li>{@code "toggle_setting"} - toggle a colony setting. jsonData: {"setting":"pvp","value":true}</li>
 *   <li>{@code "set_permission"} - change permission for a rank. jsonData: {"rank":"OFFICER","permission":"BUILD","enabled":true}</li>
 *   <li>{@code "add_member"} - add player to colony. jsonData: {"playerName":"Steve","rank":"MEMBER"}</li>
 *   <li>{@code "remove_member"} - remove player from colony. jsonData: {"playerUuid":"..."}</li>
 *   <li>{@code "change_rank"} - change a member's rank. jsonData: {"playerUuid":"...","rank":"OFFICER"}</li>
 *   <li>{@code "hire_mercenary"} - hire mercenaries for defense. jsonData: {"count":3}</li>
 *   <li>{@code "recall_citizens"} - recall all citizens to Town Hall. jsonData: {}</li>
 *   <li>{@code "abandon"} - abandon/delete the colony. jsonData: {}</li>
 *   <li>{@code "set_style"} - change colony building style. jsonData: {"style":"medieval"}</li>
 *   <li>{@code "set_flag"} - update colony banner/flag. jsonData: {"patterns":"..."}</li>
 * </ul>
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code action} - the action type identifier</li>
 *   <li>{@code jsonData} - action-specific parameters as JSON</li>
 * </ul>
 */
public record ColonyActionPayload(String colonyId, String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ColonyActionPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "colony_action"));

    public static final StreamCodec<FriendlyByteBuf, ColonyActionPayload> STREAM_CODEC =
        StreamCodec.of(ColonyActionPayload::write, ColonyActionPayload::read);

    private static void write(FriendlyByteBuf buf, ColonyActionPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 32767);
    }

    private static ColonyActionPayload read(FriendlyByteBuf buf) {
        return new ColonyActionPayload(buf.readUtf(256), buf.readUtf(256), buf.readUtf(32767));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
