package com.ultra.megamod.feature.citizen.data;

import com.ultra.megamod.MegaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Manages recruit groups for all players.
 * Static singleton with manual NbtIo file persistence.
 */
public class GroupManager {
    private static GroupManager INSTANCE;
    private static final String FILE_NAME = "megamod_groups.dat";
    private static final int MAX_GROUPS_PER_PLAYER = 5;

    private final Map<String, GroupData> groups = new LinkedHashMap<>();
    private int nextGroupId = 1;
    private boolean dirty = false;

    public static GroupManager get(ServerLevel level) {
        if (INSTANCE == null) {
            INSTANCE = new GroupManager();
            INSTANCE.loadFromDisk(level);
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    public GroupData getGroup(String groupId) {
        return groups.get(groupId);
    }

    public GroupData getGroupForEntity(UUID entityUuid) {
        for (GroupData group : groups.values()) {
            if (group.getMemberEntityIds().contains(entityUuid)) {
                return group;
            }
        }
        return null;
    }

    public List<GroupData> getGroupsForOwner(UUID ownerUuid) {
        List<GroupData> result = new ArrayList<>();
        for (GroupData group : groups.values()) {
            if (group.getOwnerUuid().equals(ownerUuid)) {
                result.add(group);
            }
        }
        return result;
    }

    public GroupData createGroup(String name, UUID ownerUuid) {
        String id = "group_" + (nextGroupId++);
        GroupData group = new GroupData(id, name, ownerUuid);
        groups.put(id, group);
        dirty = true;
        return group;
    }

    public void deleteGroup(String groupId) {
        groups.remove(groupId);
        dirty = true;
    }

    public List<GroupData> getAllGroups() {
        return new ArrayList<>(groups.values());
    }

    /**
     * Splits a group in half, creating a new group with half the members.
     */
    public GroupData splitGroup(String groupId) {
        GroupData original = groups.get(groupId);
        if (original == null) return null;
        GroupData newGroup = createGroup(original.getGroupName() + " B", original.getOwnerUuid());
        List<UUID> members = new ArrayList<>(original.getMemberEntityIds());
        int half = members.size() / 2;
        for (int i = half; i < members.size(); i++) {
            UUID member = members.get(i);
            original.removeMember(member);
            newGroup.addMember(member);
        }
        dirty = true;
        return newGroup;
    }

    /**
     * Merges two groups. All members of groupB move to groupA. GroupB is deleted.
     */
    public boolean mergeGroups(String groupAId, String groupBId) {
        GroupData a = groups.get(groupAId);
        GroupData b = groups.get(groupBId);
        if (a == null || b == null) return false;
        for (UUID member : new ArrayList<>(b.getMemberEntityIds())) {
            a.addMember(member);
        }
        groups.remove(groupBId);
        dirty = true;
        return true;
    }

    private void markDirty() {
        dirty = true;
    }

    // ---- Persistence ----

    public void saveToDisk(ServerLevel level) {
        if (!dirty) return;
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT);
            File dataDir = worldDir.resolve("data").toFile();
            if (!dataDir.exists()) dataDir.mkdirs();
            File file = new File(dataDir, FILE_NAME);

            CompoundTag root = new CompoundTag();
            root.putInt("nextGroupId", nextGroupId);
            ListTag groupList = new ListTag();

            for (GroupData group : groups.values()) {
                CompoundTag gt = new CompoundTag();
                gt.putString("groupId", group.getGroupId());
                gt.putString("groupName", group.getGroupName());
                gt.putString("owner", group.getOwnerUuid().toString());
                gt.putString("formation", group.getFormation().name());
                gt.putString("aggroState", group.getAggroState().name());
                gt.putString("followState", group.getFollowState().name());

                ListTag members = new ListTag();
                for (UUID memberId : group.getMemberEntityIds()) {
                    CompoundTag mt = new CompoundTag();
                    mt.putString("uuid", memberId.toString());
                    members.add(mt);
                }
                gt.put("members", members);
                groupList.add(gt);
            }
            root.put("groups", groupList);
            NbtIo.writeCompressed(root, file.toPath());
            dirty = false;
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to save GroupManager", e);
        }
    }

    private void loadFromDisk(ServerLevel level) {
        try {
            Path worldDir = level.getServer().getWorldPath(LevelResource.ROOT);
            File file = worldDir.resolve("data").resolve(FILE_NAME).toFile();
            if (!file.exists()) return;

            CompoundTag root = NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap());
            nextGroupId = root.getIntOr("nextGroupId", 1);

            Tag tag = root.get("groups");
            if (tag instanceof ListTag groupList) {
                for (int i = 0; i < groupList.size(); i++) {
                    if (groupList.get(i) instanceof CompoundTag gt) {
                        String gid = gt.getStringOr("groupId", "");
                        String gname = gt.getStringOr("groupName", "");
                        UUID owner = UUID.fromString(gt.getStringOr("owner", UUID.randomUUID().toString()));
                        GroupData group = new GroupData(gid, gname, owner);

                        try {
                            group.setFormation(FormationType.valueOf(gt.getStringOr("formation", "LINE")));
                        } catch (Exception e) {
                            group.setFormation(FormationType.LINE);
                        }
                        try {
                            group.setAggroState(CitizenStatus.valueOf(gt.getStringOr("aggroState", "IDLE")));
                        } catch (Exception e) {
                            group.setAggroState(CitizenStatus.IDLE);
                        }
                        try {
                            group.setFollowState(CitizenStatus.valueOf(gt.getStringOr("followState", "FOLLOW")));
                        } catch (Exception e) {
                            group.setFollowState(CitizenStatus.FOLLOW);
                        }

                        Tag membersTag = gt.get("members");
                        if (membersTag instanceof ListTag memberList) {
                            for (int j = 0; j < memberList.size(); j++) {
                                if (memberList.get(j) instanceof CompoundTag mt) {
                                    group.addMember(UUID.fromString(mt.getStringOr("uuid", UUID.randomUUID().toString())));
                                }
                            }
                        }

                        groups.put(gid, group);
                    }
                }
            }
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to load GroupManager", e);
        }
    }
}
