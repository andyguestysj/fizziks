package com.fizziks;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.joml.*;
import com.fizziks.physics3d.*;

import java.nio.file.*;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game3D {

    private long window;
    private ShaderProgram shader;
    private Mesh cubeMesh;
    private Mesh sphereMesh;
    private Camera camera;
    private PhysicsEngine3D physics = new PhysicsEngine3D();

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);

        window = glfwCreateWindow(800, 600, "LWJGL 3D Physics Demo", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create window");

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
        camera = new Camera(new Vector3f(0, 2, 8));

        // Simple cube & sphere meshes
        cubeMesh = Mesh.createCube();
        sphereMesh = Mesh.createSphere(0.5f, 16, 16);

        setupScene();
    }

    private void setupScene() {
        RigidBody3D ground = new RigidBody3D(new Vector3f(0, -3, 0), 0, true);
        Collider3D groundCol = new Collider3D(new Vector3f(0, -3, 0), new Vector3f(3, 1, 3));
        physics.addBody(ground, groundCol);

        RigidBody3D ball = new RigidBody3D(new Vector3f(0, 5f, 0), 1, false);
        Collider3D ballCol = new Collider3D(new Vector3f(0, 5f, 0), 0.5f);
        physics.addBody(ball, ballCol);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);            
            physics.update(0.016f);

            shader.bind();
            shader.setUniformMat4("view", camera.getViewMatrix());
            shader.setUniformMat4("proj", camera.getProjectionMatrix(800f / 600f));

            for (int i = 0; i < physics.bodies.size(); i++) {
                RigidBody3D body = physics.bodies.get(i);
                Collider3D col = physics.colliders.get(i);

                Matrix4f model = new Matrix4f().translate(                    
                    body.position.x, body.position.y, body.position.z
                );

                shader.setUniformMat4("model", model.get(new float[16]));


                if (col.type == Collider3D.Type.SPHERE) {                                        
                    shader.setUniformVec3("color", new float[]{0.3f, 0.7f, 1.0f});
                    sphereMesh.render();
                } else {
                    shader.setUniformVec3("color", new float[]{0.3f, 0.9f, 0.3f});
                    cubeMesh.render();
                }
            }

            shader.unbind();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {
        cubeMesh.cleanup();
        sphereMesh.cleanup();
        shader.cleanup();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public static void main(String[] args) {
        new Game3D().run();
    }
}
