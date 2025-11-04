package com.audio;

import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.*;

import com.audio.physics3d.*;

import org.joml.*;

import java.nio.file.*;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game3D {

	private long window;
	private ShaderProgram shader;
	private Mesh groundMesh;
	private Mesh walkerMesh;
	public Vector3f walkerPosition = new Vector3f(-10f, -1f, 4f);
	public Vector3f walkerVelocity = new Vector3f(2f, 0f, 0f);
	public float walkerSize = 0.5f;
	private Mesh sphereMesh;
	private Camera camera;
	private PhysicsEngine3D physics;
	public AudioSystem audio = new AudioSystem();
	public Sound bounceSound;
	public Sound footstepsSound;

	public void run() {
		physics = new PhysicsEngine3D(this);
		init();
		loop();
		cleanup();
	}

	private void init() {
		if (!glfwInit())
			throw new IllegalStateException("GLFW init failed");

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

		window = glfwCreateWindow(800, 600, "LWJGL 3D Physics Demo", NULL, NULL);
		if (window == NULL)
			throw new RuntimeException("Failed to create window");

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);
		GL.createCapabilities();

		glEnable(GL_DEPTH_TEST);
		glClearColor(0.1f, 0.1f, 0.15f, 1f);

		// Load shaders
		try {
			String vert = Files.readString(Path.of("res/shaders/vertex.glsl"));
			String frag = Files.readString(Path.of("res/shaders/fragment.glsl"));
			shader = new ShaderProgram(vert, frag);
		} catch (IOException e) {
			throw new RuntimeException("Shader load failed", e);
		}

		// Setup camera
		camera = new Camera(new Vector3f(0, 2, 10));

		// Simple cube & sphere meshes
		

		setupScene();

		audio.init();
		bounceSound = new Sound("assets/sounds/boing.ogg");
		footstepsSound = new Sound("assets/sounds/footsteps.ogg");

	}

	private void setupScene() {
		
		groundMesh = Mesh.createCube(new Vector3f(50f, 1f, 50.f), "Ground");		
		RigidBody3D ground = new RigidBody3D(new Vector3f(0, -3, 0), 0, true, groundMesh);
		ground.setColour(0.5f, 0.5f, 0.5f);
		Collider3D groundCol = new Collider3D(new Vector3f(0, -3, 0), new Vector3f(50, 1, 50),"Ground");
		physics.addBody(ground, groundCol);

		sphereMesh = Mesh.createSphere(0.5f, 16, 16, "Sphere");
		RigidBody3D ball = new RigidBody3D(new Vector3f(0, 5f, 0), 1, false, sphereMesh);
		ball.setColour(0f,1f,0f);
		Collider3D ballCol = new Collider3D(new Vector3f(0, 5f, 0), 0.5f,"Ball");
		physics.addBody(ball, ballCol);

		walkerMesh = Mesh.createCube(new Vector3f(1f, 1f, 1f), "Walker");
		RigidBody3D walkerBod = new RigidBody3D(walkerPosition, 1f, false, walkerMesh);
		Collider3D groundCol2 = new Collider3D(walkerPosition, new Vector3f(1f, 1f, 1f),"Walker"	);
		walkerBod.setColour(1f,0f,1f);
		physics.addBody(walkerBod, groundCol2);
	}

	private void loop() {
		while (!glfwWindowShouldClose(window)) {

		//	bounceSound.play(camera.position, walkerPosition);

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			physics.update(0.016f);

			updateWalker(0.016f);

			shader.bind();
			shader.setUniformMat4("view", camera.getViewMatrix());
			shader.setUniformMat4("proj", camera.getProjectionMatrix(800f / 600f));

			for (int i = 0; i < physics.bodies.size(); i++) {
				RigidBody3D body = physics.bodies.get(i);
				

				Matrix4f model = new Matrix4f().translate(
						body.position.x, body.position.y, body.position.z);

				shader.setUniformMat4("model", model.get(new float[16]));
				shader.setUniformVec3("color", body.getColour());

				body.mesh.render();

			}

			shader.unbind();
			glfwSwapBuffers(window);
			glfwPollEvents();
		}
	}

	public void updateWalker(float dt) {
		walkerPosition.fma(dt, walkerVelocity);
		if (walkerPosition.x > 15f) {
			walkerPosition.x = 14f;
			walkerVelocity.x *= -1f;
		}
		if (walkerPosition.x < -15f) {
			walkerPosition.x = -14f;
			walkerVelocity.x *= -1f;
		}
	}

	public void playBounce(Vector3f ballPos){
		if ((bounceSound != null))
			bounceSound.stop();
			bounceSound.play(camera.position, ballPos);
	}

	private void cleanup() {
		footstepsSound.stop();
		bounceSound.cleanup();
		footstepsSound.cleanup();
		audio.destroy();		
		sphereMesh.cleanup();
		walkerMesh.cleanup();
		groundMesh.cleanup();
		shader.cleanup();
		glfwDestroyWindow(window);
		glfwTerminate();
	}

	public static void main(String[] args) {
		new Game3D().run();
	}
}
