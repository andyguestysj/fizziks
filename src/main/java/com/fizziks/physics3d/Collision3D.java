package com.fizziks.physics3d;

import org.joml.Vector3f;

/**
 * Basic 3D collision detection and resolution helper.
 *
 * Supports:
 * - Sphere–Sphere
 * - AABB–AABB (Axis-Aligned Bounding Boxes)
 * - AABB–Sphere (both directions)
 *
 * Educational version for clarity and teaching 3D physics fundamentals.
 */
public class Collision3D {

  /**
   * Resolves collisions between two rigid bodies based on collider type.
   */
  public static void resolve(RigidBody3D a, Collider3D colA,
      RigidBody3D b, Collider3D colB,
      float restitution) {

    if (colA.type == Collider3D.Type.SPHERE && colB.type == Collider3D.Type.SPHERE) {
      resolveSphereCollision(a, colA, b, colB, restitution);
    } else if (colA.type == Collider3D.Type.AABB && colB.type == Collider3D.Type.AABB) {
      resolveAABBCollision(a, colA, b, colB, restitution);
    } else if (colA.type == Collider3D.Type.SPHERE && colB.type == Collider3D.Type.AABB) {
      resolveSphereAABBCollision(a, colA, b, colB, restitution);
    } else if (colA.type == Collider3D.Type.AABB && colB.type == Collider3D.Type.SPHERE) {
      resolveSphereAABBCollision(b, colB, a, colA, restitution);
    }
  }

  // -------------------------------------------------------------
  // Sphere–Sphere Collision
  // -------------------------------------------------------------
  public static void resolveSphereCollision(RigidBody3D a, Collider3D colA,
      RigidBody3D b, Collider3D colB,
      float restitution) {

    Vector3f delta = new Vector3f(
        b.position.x - a.position.x,
        b.position.y - a.position.y,
        b.position.z - a.position.z);

    float distance = (float) Math.sqrt(delta.x * delta.x + delta.y * delta.y + delta.z * delta.z);
    float overlap = (colA.radius + colB.radius) - distance;
    if (overlap <= 0)
      return;

    Vector3f normal = new Vector3f(delta.x / distance, delta.y / distance, delta.z / distance);

    // Separate spheres
    Vector3f correction = normal.mul(overlap / 2f);
    if (!a.isStatic)
      a.position = a.position.add(correction.mul(-1));
    if (!b.isStatic)
      b.position = b.position.add(correction);

    applyImpulse(a, b, normal, restitution);
  }

  // -------------------------------------------------------------
  // AABB–AABB Collision
  // -------------------------------------------------------------
  public static void resolveAABBCollision(RigidBody3D a, Collider3D colA,
      RigidBody3D b, Collider3D colB,
      float restitution) {

    float dx = (a.position.x - b.position.x);
    float px = (colA.size.x + colB.size.x) - Math.abs(dx);
    if (px <= 0)
      return;

    float dy = (a.position.y - b.position.y);
    float py = (colA.size.y + colB.size.y) - Math.abs(dy);
    if (py <= 0)
      return;

    float dz = (a.position.z - b.position.z);
    float pz = (colA.size.z + colB.size.z) - Math.abs(dz);
    if (pz <= 0)
      return;

    // Determine minimum overlap axis
    Vector3f normal = new Vector3f(0, 0, 0);
    float overlap = px;
    if (px < py && px < pz) {
      normal.x = dx < 0 ? -1 : 1;
      overlap = px;
    } else if (py < pz) {
      normal.y = dy < 0 ? -1 : 1;
      overlap = py;
    } else {
      normal.z = dz < 0 ? -1 : 1;
      overlap = pz;
    }

    Vector3f correction = normal.mul(overlap / 2f);
    if (!a.isStatic)
      a.position = a.position.add(correction.mul(-1));
    if (!b.isStatic)
      b.position = b.position.add(correction);

    applyImpulse(a, b, normal, restitution);
  }

  // -------------------------------------------------------------
  // Sphere–AABB Collision
  // -------------------------------------------------------------
  public static void resolveSphereAABBCollision(RigidBody3D sphereBody, Collider3D sphereCol,
      RigidBody3D boxBody, Collider3D boxCol,
      float restitution) {

        /*
    // Clamp sphere center to AABB surface
    float closestX = clamp(sphereBody.position.x,
        boxBody.position.x - boxCol.size.x,
        boxBody.position.x + boxCol.size.x);
    float closestY = clamp(sphereBody.position.y,
        boxBody.position.y - boxCol.size.y,
        boxBody.position.y + boxCol.size.y);
    float closestZ = clamp(sphereBody.position.z,
        boxBody.position.z - boxCol.size.z,
        boxBody.position.z + boxCol.size.z);

    Vector3f closestPoint = new Vector3f(closestX, closestY, closestZ);*/

    Vector3f closestPoint = new Vector3f(
      Math.max(boxBody.position.x - boxCol.size.x, Math.min(sphereBody.position.x, boxBody.position.x + boxCol.size.x)),
      Math.max(boxBody.position.y - boxCol.size.y, Math.min(sphereBody.position.y, boxBody.position.y + boxCol.size.y)),
      Math.max(boxBody.position.z - boxCol.size.z, Math.min(sphereBody.position.z, boxBody.position.z + boxCol.size.z))
    );

    // Vector from closest point to sphere center
    Vector3f delta = sphereBody.position.sub(closestPoint, new Vector3f());
    float distance = delta.length();

    if (distance < sphereCol.radius) {
        Vector3f normal = delta.normalize();
        float penetration = sphereCol.radius - distance;

        sphereBody.position.fma(penetration, normal);

        float vDotN = sphereBody.velocity.dot(normal);
        if (vDotN < 0) {            
            Vector3f reflection = new Vector3f(normal).mul(-2f * vDotN);
            sphereBody.velocity.add(reflection).mul(restitution);
        }
        
        // Optional: friction
        //sphereBody.velocity.x *= 0.8f;
        //sphereBody.velocity.z *= 0.8f;
      }
  }

  // -------------------------------------------------------------
  // Common Helper Methods
  // -------------------------------------------------------------

  private static void applyImpulse(RigidBody3D a, RigidBody3D b, Vector3f normal, float restitution) {
    Vector3f relativeVelocity = new Vector3f(
        b.velocity.x - a.velocity.x,
        b.velocity.y - a.velocity.y,
        b.velocity.z - a.velocity.z);

    float separatingVelocity = relativeVelocity.x * normal.x +
        relativeVelocity.y * normal.y +
        relativeVelocity.z * normal.z;

    if (separatingVelocity > 0)
      return;

    float newSepVelocity = -separatingVelocity * restitution;
    float deltaVelocity = newSepVelocity - separatingVelocity;

    float totalInverseMass = (a.isStatic ? 0 : 1 / a.mass) + (b.isStatic ? 0 : 1 / b.mass);
    if (totalInverseMass <= 0)
      return;

    float impulse = deltaVelocity / totalInverseMass;
    Vector3f impulsePerMass = normal.mul(impulse, new Vector3f());

    if (!a.isStatic)
      a.velocity.add(impulsePerMass.mul(-1 / a.mass, new Vector3f()));
    if (!b.isStatic)
      b.velocity.add(impulsePerMass.mul(1 / b.mass, new Vector3f()));

  }

  private static float clamp(float val, float min, float max) {
    return Math.max(min, Math.min(max, val));
  }
}
