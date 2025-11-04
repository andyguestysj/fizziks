package com.audio;

import static org.lwjgl.opengl.GL33C.*;
import java.nio.*;
import org.lwjgl.system.MemoryStack;

/**
 * Simple shader program helper for OpenGL 3.3+
 */
public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertexSrc, String fragmentSrc) {
        int vs = createShader(vertexSrc, GL_VERTEX_SHADER);
        int fs = createShader(fragmentSrc, GL_FRAGMENT_SHADER);
        programId = glCreateProgram();
        glAttachShader(programId, vs);
        glAttachShader(programId, fs);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader link error: " + glGetProgramInfoLog(programId));

        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    private int createShader(String src, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(shader));
        return shader;
    }

    public void bind() { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }
    public void cleanup() { glDeleteProgram(programId); }

    /** Uploads a 4x4 matrix uniform (e.g., model, view, projection). */
    public void setUniformMat4(String name, float[] mat4) {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1)
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                fb.put(mat4).flip();
                glUniformMatrix4fv(loc, false, fb);
            }
    }

    /** Uploads a vec3 uniform (e.g., color). */
    public void setUniformVec3(String name, float[] vec3) {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1)
            glUniform3f(loc, vec3[0], vec3[1], vec3[2]);
    }
}

