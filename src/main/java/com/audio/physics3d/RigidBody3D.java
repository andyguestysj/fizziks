package com.audio.physics3d;

import org.joml.Vector3f;

import com.audio.Mesh;

public class RigidBody3D {
  public Vector3f position;
  public Vector3f velocity;
  public Vector3f accumulatedForce;
  public float mass;      // mass == 0 => static body
  public boolean isStatic;
  public Mesh mesh;
  public float[] colour = new float[] { 0.5f, 0.5f, 0.5f };

  public RigidBody3D(Vector3f position, float mass, boolean isStatic, Mesh mesh) {
    this.position = position;
    this.velocity = new Vector3f(0, 0, 0);
    this.accumulatedForce = new Vector3f(0, 0, 0);
    this.mass = mass;
    this.isStatic = isStatic || mass <= 0f;
    this.mesh = mesh;
  }

  public void applyForce(Vector3f force) {
      if (isStatic) return;

      this.accumulatedForce.add(force);              

  }

  /** Integrate using semi-implicit Euler: v += a * dt; p += v * dt */
  public void integrate(float dt) {
    if (isStatic) {
        accumulatedForce = new Vector3f(0,0,0);
        return;
    }
    // acceleration = totalForce / mass
    Vector3f acceleration = accumulatedForce.mul(1.0f / mass, new Vector3f());
    
    // integrate velocity
    velocity.add(acceleration.mul(dt, new Vector3f()));
    
    // integrate position (use new velocity)
    position.add(velocity.mul(dt, new Vector3f()));

    // clear force accumulator for next step
    accumulatedForce = new Vector3f(0, 0, 0);
    
  }

  public void setColour(float r, float g, float b) {
      this.colour[0] = r;
      this.colour[1] = g;
      this.colour[2] = b;
  }

  public float[] getColour() {
      return this.colour;
  }
}
