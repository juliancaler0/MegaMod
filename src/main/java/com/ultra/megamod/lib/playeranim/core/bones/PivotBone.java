package com.ultra.megamod.lib.playeranim.core.bones;

import com.ultra.megamod.lib.playeranim.core.math.Vec3f;

public class PivotBone extends PlayerAnimBone {
    private final Vec3f pivot;

    public PivotBone(String name, Vec3f pivot) {
        super(name);
        this.pivot = pivot;
    }

    public Vec3f getPivot() {
        return this.pivot;
    }
}
