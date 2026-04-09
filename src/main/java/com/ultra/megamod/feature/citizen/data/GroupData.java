package com.ultra.megamod.feature.citizen.data;

import java.util.*;

/**
 * Represents a named group of recruit citizens owned by a player.
 * Groups can have formations and shared aggro/follow states.
 */
public class GroupData {
    private final String groupId;
    private String groupName;
    private final UUID ownerUuid;
    private final Set<UUID> memberEntityIds = new LinkedHashSet<>();
    private FormationType formation = FormationType.LINE;
    private CitizenStatus aggroState = CitizenStatus.IDLE;
    private CitizenStatus followState = CitizenStatus.FOLLOW;

    public GroupData(String groupId, String groupName, UUID ownerUuid) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.ownerUuid = ownerUuid;
    }

    public String getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String name) { this.groupName = name; }
    public UUID getOwnerUuid() { return ownerUuid; }

    public Set<UUID> getMemberEntityIds() { return memberEntityIds; }
    public void addMember(UUID entityUuid) { memberEntityIds.add(entityUuid); }
    public void removeMember(UUID entityUuid) { memberEntityIds.remove(entityUuid); }
    public int getMemberCount() { return memberEntityIds.size(); }

    public FormationType getFormation() { return formation; }
    public void setFormation(FormationType formation) { this.formation = formation; }

    public CitizenStatus getAggroState() { return aggroState; }
    public void setAggroState(CitizenStatus state) { this.aggroState = state; }

    public CitizenStatus getFollowState() { return followState; }
    public void setFollowState(CitizenStatus state) { this.followState = state; }

    // Additional fields used by TownHandler
    private int imageIndex = 0;
    private boolean allowRanged = true;
    private boolean allowRest = true;

    public String getName() { return groupName; }
    public int getImageIndex() { return imageIndex; }
    public void setImageIndex(int index) { this.imageIndex = index; }
    public boolean isAllowRanged() { return allowRanged; }
    public void setAllowRanged(boolean allow) { this.allowRanged = allow; }
    public boolean isAllowRest() { return allowRest; }
    public void setAllowRest(boolean allow) { this.allowRest = allow; }
}
