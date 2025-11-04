package com.audio.physics3d;

import org.joml.Vector3f;

/**
 * Simple 3D collider for spheres and AABBs.
 */
public class Collider3D {

    public enum Type {
        SPHERE, AABB
    }

    public Type type;
    public Vector3f position;
    public Vector3f size; // For AABB
    public float radius;  // For Sphere
    public String name;

    public Collider3D(Vector3f position, float radius, String name) {
        this.type = Type.SPHERE;
        this.position = position;
        this.radius = radius;
        this.name = name;
    }

    public Collider3D(Vector3f position, Vector3f size, String name) {
        this.type = Type.AABB;
        this.position = position;
        this.size = size;
        this.name = name;
    }

    public boolean isColliding(Collider3D other) {
        if (this.type == Type.SPHERE && other.type == Type.SPHERE)
            return sphereVsSphere(other);
        else if (this.type == Type.AABB && other.type == Type.AABB)
            return aabbVsAabb(other);
        else if (this.type == Type.SPHERE && other.type == Type.AABB)
            return sphereVsAabb(this, other);
        else
            return sphereVsAabb(other, this);
    }

    private boolean sphereVsSphere(Collider3D other) {
        float dx = position.x - other.position.x;
        float dy = position.y - other.position.y;
        float dz = position.z - other.position.z;
        float distSq = dx * dx + dy * dy + dz * dz;
        float radiusSum = radius + other.radius;
        return distSq <= radiusSum * radiusSum;
    }

    private boolean aabbVsAabb(Collider3D other) {
        return (Math.abs(position.x - other.position.x) <= (size.x + other.size.x)) &&
               (Math.abs(position.y - other.position.y) <= (size.y + other.size.y)) &&
               (Math.abs(position.z - other.position.z) <= (size.z + other.size.z));
    }

    private boolean sphereVsAabb(Collider3D sphere, Collider3D aabb) {
        float closestX = clamp(sphere.position.x, aabb.position.x - aabb.size.x, aabb.position.x + aabb.size.x);
        float closestY = clamp(sphere.position.y, aabb.position.y - aabb.size.y, aabb.position.y + aabb.size.y);
        float closestZ = clamp(sphere.position.z, aabb.position.z - aabb.size.z, aabb.position.z + aabb.size.z);

        float dx = sphere.position.x - closestX;
        float dy = sphere.position.y - closestY;
        float dz = sphere.position.z - closestZ;

        float distSq = dx * dx + dy * dy + dz * dz;
        return distSq <= sphere.radius * sphere.radius;
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }
}