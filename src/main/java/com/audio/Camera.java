package com.audio;

import org.joml.*;

public class Camera {
    public final Vector3f position;

    public Camera(Vector3f position) {
        this.position = position;
    }

    public float[] getViewMatrix() {
        Matrix4f view = new Matrix4f().lookAt(
            position,
            new Vector3f(0, 0, 0),
            new Vector3f(0, 1, 0)
        );
        return view.get(new float[16]);
    }

    public float[] getProjectionMatrix(float aspect) {
        Matrix4f proj = new Matrix4f().perspective((float) java.lang.Math.toRadians(70.0f), aspect, 0.1f, 100.0f);
        return proj.get(new float[16]);
    }
}
