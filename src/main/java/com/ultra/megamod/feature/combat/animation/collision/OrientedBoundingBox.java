package com.ultra.megamod.feature.combat.animation.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;

/**
 * Oriented Bounding Box with SAT intersection testing.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.collision.OrientedBoundingBox).
 */
public class OrientedBoundingBox {

    public Vec3 center;
    public Vec3 extent;
    public Vec3 axisX, axisY, axisZ;

    // Derived
    public Vec3 scaledAxisX, scaledAxisY, scaledAxisZ;
    public Matrix3f rotation = new Matrix3f();
    public Vec3[] vertices;

    public OrientedBoundingBox(Vec3 center, double width, double height, double depth, float pitch, float yaw) {
        this.center = center;
        this.extent = new Vec3(width / 2.0, height / 2.0, depth / 2.0);
        this.axisZ = Vec3.directionFromRotation(pitch, yaw).normalize();
        this.axisY = Vec3.directionFromRotation(pitch + 90, yaw).scale(-1).normalize();
        this.axisX = axisZ.cross(axisY);
    }

    public OrientedBoundingBox(Vec3 center, Vec3 size, float pitch, float yaw) {
        this(center, size.x, size.y, size.z, pitch, yaw);
    }

    public OrientedBoundingBox(AABB box) {
        this.center = new Vec3((box.maxX + box.minX) / 2.0, (box.maxY + box.minY) / 2.0, (box.maxZ + box.minZ) / 2.0);
        this.extent = new Vec3(Math.abs(box.maxX - box.minX) / 2.0, Math.abs(box.maxY - box.minY) / 2.0, Math.abs(box.maxZ - box.minZ) / 2.0);
        this.axisX = new Vec3(1, 0, 0);
        this.axisY = new Vec3(0, 1, 0);
        this.axisZ = new Vec3(0, 0, 1);
    }

    public OrientedBoundingBox offsetAlongAxisZ(double offset) {
        this.center = this.center.add(axisZ.scale(offset));
        return this;
    }

    public OrientedBoundingBox updateVertex() {
        rotation.set(0, 0, (float) axisX.x); rotation.set(0, 1, (float) axisX.y); rotation.set(0, 2, (float) axisX.z);
        rotation.set(1, 0, (float) axisY.x); rotation.set(1, 1, (float) axisY.y); rotation.set(1, 2, (float) axisY.z);
        rotation.set(2, 0, (float) axisZ.x); rotation.set(2, 1, (float) axisZ.y); rotation.set(2, 2, (float) axisZ.z);

        scaledAxisX = axisX.scale(extent.x);
        scaledAxisY = axisY.scale(extent.y);
        scaledAxisZ = axisZ.scale(extent.z);

        vertices = new Vec3[]{
                center.subtract(scaledAxisZ).subtract(scaledAxisX).subtract(scaledAxisY),
                center.subtract(scaledAxisZ).add(scaledAxisX).subtract(scaledAxisY),
                center.subtract(scaledAxisZ).add(scaledAxisX).add(scaledAxisY),
                center.subtract(scaledAxisZ).subtract(scaledAxisX).add(scaledAxisY),
                center.add(scaledAxisZ).subtract(scaledAxisX).subtract(scaledAxisY),
                center.add(scaledAxisZ).add(scaledAxisX).subtract(scaledAxisY),
                center.add(scaledAxisZ).add(scaledAxisX).add(scaledAxisY),
                center.add(scaledAxisZ).subtract(scaledAxisX).add(scaledAxisY)
        };
        return this;
    }

    public boolean contains(Vec3 point) {
        var distance = point.subtract(center).toVector3f();
        distance.mulTranspose(rotation);
        return Math.abs(distance.x()) < extent.x
                && Math.abs(distance.y()) < extent.y
                && Math.abs(distance.z()) < extent.z;
    }

    public boolean intersects(AABB boundingBox) {
        var otherOBB = new OrientedBoundingBox(boundingBox).updateVertex();
        return intersects(this, otherOBB);
    }

    public static boolean intersects(OrientedBoundingBox a, OrientedBoundingBox b) {
        if (separated(a.vertices, b.vertices, a.scaledAxisX)) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisY)) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisZ)) return false;
        if (separated(a.vertices, b.vertices, b.scaledAxisX)) return false;
        if (separated(a.vertices, b.vertices, b.scaledAxisY)) return false;
        if (separated(a.vertices, b.vertices, b.scaledAxisZ)) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisX))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisY))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisX.cross(b.scaledAxisZ))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisX))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisY))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisY.cross(b.scaledAxisZ))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisX))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisY))) return false;
        if (separated(a.vertices, b.vertices, a.scaledAxisZ.cross(b.scaledAxisZ))) return false;
        return true;
    }

    private static boolean separated(Vec3[] vertsA, Vec3[] vertsB, Vec3 axis) {
        if (axis.equals(Vec3.ZERO)) return false;
        double aMin = Double.POSITIVE_INFINITY, aMax = Double.NEGATIVE_INFINITY;
        double bMin = Double.POSITIVE_INFINITY, bMax = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 8; i++) {
            double aDist = vertsA[i].dot(axis);
            aMin = Math.min(aMin, aDist); aMax = Math.max(aMax, aDist);
            double bDist = vertsB[i].dot(axis);
            bMin = Math.min(bMin, bDist); bMax = Math.max(bMax, bDist);
        }
        double longSpan = Math.max(aMax, bMax) - Math.min(aMin, bMin);
        double sumSpan = aMax - aMin + bMax - bMin;
        return longSpan >= sumSpan;
    }
}
