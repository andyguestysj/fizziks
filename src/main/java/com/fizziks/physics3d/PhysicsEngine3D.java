package com.fizziks.physics3d;

import java.util.ArrayList;
import java.util.List;
import org.joml.Vector3f;

public class PhysicsEngine3D {
	public List<RigidBody3D> bodies = new ArrayList<>();
	public List<Collider3D> colliders = new ArrayList<>();

	// world gravity (m/s^2)
	public Vector3f gravity = new Vector3f(0f, -9.81f, 0f);

	// fixed timestep (in seconds)
	private final float FIXED_DT = 1.0f / 60.0f;

	// time accumulator for fixed-step
	private float accumulator = 0f;

	public void addBody(RigidBody3D body, Collider3D collider) {
		bodies.add(body);
		colliders.add(collider);
		// ensure collider position is initialised to body position
		collider.position = body.position;
	}

	/**
	 * Call this each frame with the frame's elapsed time (seconds).
	 */
	public void update(float frameDt) {
		if (frameDt <= 0)
			return;
		accumulator += frameDt;

		// clamp accumulator to avoid spiral of death
		if (accumulator > 0.25f)
			accumulator = 0.25f;

		while (accumulator >= FIXED_DT) {
			step(FIXED_DT);
			accumulator -= FIXED_DT;
		}
	}

	private void step(float dt) {
		// 1) apply gravity and integrate
		for (RigidBody3D body : bodies) {
			if (!body.isStatic) {
				// apply gravity as force = mass * g
				Vector3f gForce = gravity.mul(body.mass, new Vector3f());
				body.applyForce(gForce);
			}
			body.integrate(dt);
		}

		// 2) sync collider positions
		for (int i = 0; i < colliders.size(); i++) {
			colliders.get(i).position = bodies.get(i).position;
		}

		// 3) narrow-phase collision detection & resolution (naive O(n^2))
		int n = bodies.size();
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				Collider3D a = colliders.get(i);
				Collider3D b = colliders.get(j);
				if (a.isColliding(b)) {
					// resolve using your Collision3D helper
					Collision3D.resolve(bodies.get(i), a, bodies.get(j), b, 0.8f);
				}
			}
		}
	}
}
