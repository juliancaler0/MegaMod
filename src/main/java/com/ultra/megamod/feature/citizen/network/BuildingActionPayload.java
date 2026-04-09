package com.ultra.megamod.feature.citizen.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Client-to-server payload: building-specific actions.
 * Sent when a player performs operations on a specific building from its GUI.
 *
 * <p>Supported actions (via {@code action} field):
 * <ul>
 *   <li>{@code "upgrade"} - request building upgrade. jsonData: {"builderPos":"x,y,z"}</li>
 *   <li>{@code "repair"} - request building repair. jsonData: {"builderPos":"x,y,z"}</li>
 *   <li>{@code "remove"} - request building removal. jsonData: {"builderPos":"x,y,z"}</li>
 *   <li>{@code "hire_worker"} - assign a citizen to this building. jsonData: {"citizenId":"uuid"}</li>
 *   <li>{@code "fire_worker"} - unassign a citizen. jsonData: {"citizenId":"uuid"}</li>
 *   <li>{@code "recall_worker"} - recall worker to this building. jsonData: {"citizenId":"uuid"}</li>
 *   <li>{@code "change_setting"} - toggle building-specific setting. jsonData: {"setting":"autoHire","value":true}</li>
 *   <li>{@code "teach_recipe"} - teach a recipe to the building. jsonData: {"recipeJson":"..."}</li>
 *   <li>{@code "remove_recipe"} - remove recipe from the building. jsonData: {"recipeIndex":0}</li>
 *   <li>{@code "change_recipe_priority"} - reorder recipes. jsonData: {"recipeIndex":0,"direction":"up"}</li>
 *   <li>{@code "toggle_recipe"} - enable/disable recipe. jsonData: {"recipeIndex":0,"enabled":true}</li>
 *   <li>{@code "set_priority"} - set delivery priority. jsonData: {"priority":5}</li>
 *   <li>{@code "rename"} - rename the building. jsonData: {"name":"My Bakery"}</li>
 *   <li>{@code "set_hiring_mode"} - set auto/manual hiring. jsonData: {"mode":"AUTO"}</li>
 *   <li>{@code "assign_field"} - assign a farm field. jsonData: {"fieldPos":"x,y,z"}</li>
 *   <li>{@code "set_stock"} - set minimum stock requirement. jsonData: {"item":"minecraft:wheat","count":64}</li>
 *   <li>{@code "remove_stock"} - remove minimum stock. jsonData: {"item":"minecraft:wheat"}</li>
 *   <li>{@code "set_filter"} - set item filter. jsonData: {"item":"minecraft:stone","allowed":true}</li>
 * </ul>
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code colonyId} - the faction/colony UUID string</li>
 *   <li>{@code buildingPos} - packed BlockPos as long (BlockPos.asLong)</li>
 *   <li>{@code action} - the action type identifier</li>
 *   <li>{@code jsonData} - action-specific parameters as JSON</li>
 * </ul>
 */
public record BuildingActionPayload(String colonyId, long buildingPos, String action, String jsonData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BuildingActionPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "building_action"));

    public static final StreamCodec<FriendlyByteBuf, BuildingActionPayload> STREAM_CODEC =
        StreamCodec.of(BuildingActionPayload::write, BuildingActionPayload::read);

    private static void write(FriendlyByteBuf buf, BuildingActionPayload payload) {
        buf.writeUtf(payload.colonyId, 256);
        buf.writeLong(payload.buildingPos);
        buf.writeUtf(payload.action, 256);
        buf.writeUtf(payload.jsonData, 32767);
    }

    private static BuildingActionPayload read(FriendlyByteBuf buf) {
        return new BuildingActionPayload(buf.readUtf(256), buf.readLong(), buf.readUtf(256), buf.readUtf(32767));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
