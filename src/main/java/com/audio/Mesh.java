package com.audio;

import static org.lwjgl.opengl.GL11C.GL_FLOAT;
import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11C.glDrawElements;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15C.glBindBuffer;
import static org.lwjgl.opengl.GL15C.glBufferData;
import static org.lwjgl.opengl.GL15C.glDeleteBuffers;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20C.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;
import static org.lwjgl.opengl.GL33C.*;
import java.nio.*;
import java.util.Vector;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

public class Mesh {
    private final int vao, vbo, ebo;
    private final int indexCount;
    public String name;

    private Mesh(float[] vertices, int[] indices, String name) {
        indexCount = indices.length;
        this.name = name;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Vertex buffer
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Index buffer
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Vertex layout: position (x, y, z)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

    /** ✅ Creates a cube using indexed faces (8 unique vertices, 36 indices). */
    public static Mesh createCube(Vector3f size, String name) {
        float[] vertices = {
            -size.x, -size.y, -size.z,  // 0
             size.x, -size.y, -size.z,  // 1
             size.x,  size.y, -size.z,  // 2
            -size.x,  size.y, -size.z,  // 3
            -size.x, -size.y,  size.z,  // 4
             size.x, -size.y,  size.z,  // 5
             size.x,  size.y,  size.z,  // 6
            -size.x,  size.y,  size.z   // 7
        };

        int[] indices = {
            // Back face
            0, 1, 2, 2, 3, 0,
            // Front face
            4, 5, 6, 6, 7, 4,
            // Left face
            0, 4, 7, 7, 3, 0,
            // Right face
            1, 5, 6, 6, 2, 1,
            // Bottom face
            0, 1, 5, 5, 4, 0,
            // Top face
            3, 2, 6, 6, 7, 3
        };

        return new Mesh(vertices, indices, name);
    }

    /** Minimal placeholder for sphere (can be expanded later). */
    public static Mesh createSphere(float radius, int slices, int stacks, String name) {
        // Number of vertices
        int vertexCount = (stacks + 1) * (slices + 1);
        float[] vertices = new float[vertexCount * 3];
        int vertexPointer = 0;

        // Generate vertices
        for (int i = 0; i <= stacks; i++) {
            float v = (float) i / stacks;
            float phi = (float) (v * java.lang.Math.PI); // 0 to PI

            for (int j = 0; j <= slices; j++) {
                float u = (float) j / slices;
                float theta = (float) (u * 2.0 * java.lang.Math.PI); // 0 to 2PI

                float x = (float) ( java.lang.Math.cos(theta) * java.lang.Math.sin(phi));
                float y = (float) java.lang.Math.cos(phi);
                float z = (float) ( java.lang.Math.sin(theta) * java.lang.Math.sin(phi));

                vertices[vertexPointer++] = x * radius;
                vertices[vertexPointer++] = y * radius;
                vertices[vertexPointer++] = z * radius;
            }
        }

        // Generate indices
        int indexCount = stacks * slices * 6;
        int[] indices = new int[indexCount];
        int indexPointer = 0;

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = (i * (slices + 1)) + j;
                int second = first + slices + 1;

                indices[indexPointer++] = first;
                indices[indexPointer++] = second;
                indices[indexPointer++] = first + 1;

                indices[indexPointer++] = second;
                indices[indexPointer++] = second + 1;
                indices[indexPointer++] = first + 1;
            }
        }

        return new Mesh(vertices, indices, name);
    }

    public void render() {
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
    }
}
