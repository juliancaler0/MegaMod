package com.ultra.megamod.feature.citizen.entity.mc;

import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum-like class for citizen status icons, resource location and translation.
 * Ported from MineColonies' VisibleCitizenStatus.
 */
public class VisibleCitizenStatus {

    private static Map<Integer, VisibleCitizenStatus> visibleStatusMap;
    private static int idCounter = 1;

    // General Icons
    public static final VisibleCitizenStatus EAT = new VisibleCitizenStatus(
            Identifier.fromNamespaceAndPath("megamod", "textures/icons/hungry.png"), "megamod.status.hungry", true);
    public static final VisibleCitizenStatus HOUSE = new VisibleCitizenStatus(
            Identifier.fromNamespaceAndPath("megamod", "textures/icons/house.png"), "megamod.status.idle", true);
    public static final VisibleCitizenStatus MOURNING = new VisibleCitizenStatus(
            Identifier.fromNamespaceAndPath("megamod", "textures/icons/mourning.png"), "megamod.status.mourning", true);
    public static final VisibleCitizenStatus SLEEP = new VisibleCitizenStatus(
            Identifier.fromNamespaceAndPath("megamod", "textures/icons/bed.png"), "megamod.status.sleeping", true);
    public static final VisibleCitizenStatus SICK = new VisibleCitizenStatus(
            Identifier.fromNamespaceAndPath("megamod", "textures/icons/sick.png"), "megamod.status.sick", true);
    public static final VisibleCitizenStatus WORKING = new VisibleCitizenStatus(
            Identifier.fromNamespaceAndPath("megamod", "textures/icons/working.png"), "megamod.status.working");

    private final int id;
    private final Identifier icon;
    private final String translationKey;
    private boolean render;

    public VisibleCitizenStatus(Identifier icon, String translationKey) {
        this.icon = icon;
        this.translationKey = translationKey;
        if (visibleStatusMap == null) {
            visibleStatusMap = new HashMap<>();
            idCounter = 1;
        }
        this.id = idCounter++;
        visibleStatusMap.put(id, this);
        this.render = false;
    }

    public VisibleCitizenStatus(Identifier icon, String translationKey, boolean render) {
        this(icon, translationKey);
        this.render = render;
    }

    public Identifier getIcon() { return icon; }
    public String getTranslationKey() { return translationKey; }
    public int getId() { return id; }
    public boolean shouldRender() { return render; }

    public static VisibleCitizenStatus getForId(int id) {
        return visibleStatusMap != null ? visibleStatusMap.get(id) : null;
    }

    public static Map<Integer, VisibleCitizenStatus> getVisibleStatus() {
        return visibleStatusMap != null ? Collections.unmodifiableMap(visibleStatusMap) : Collections.emptyMap();
    }
}
