package com.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.system.MemoryUtil;


public class AudioSystem {
  private long device;
  private long context;
  private int bufferId;
  private int sourceId;

  public void init() {
    device = ALC10.alcOpenDevice((CharSequence) null);
    if (device == MemoryUtil.NULL) {
      throw new IllegalStateException("Failed to open the default OpenAL device.");
    }
    context = ALC10.alcCreateContext(device, (int[]) null);
    if (context == MemoryUtil.NULL) {
      throw new IllegalStateException("Failed to create OpenAL context.");
    }
    ALC10.alcMakeContextCurrent(context);
    AL.createCapabilities(ALC.createCapabilities(device));
    
    System.out.println("OpenAL initialized." + 
            ALC10.alcGetString(device, ALC10.ALC_DEVICE_SPECIFIER));
  }

  public void destroy() {
    ALC10.alcDestroyContext(context);
    ALC10.alcCloseDevice(device);
  }

  public void setListenerPosition(Camera camera) {
    AL10.alListener3f(AL10.AL_POSITION, camera.position.x, camera.position.y, camera.position.z);
    AL10.alListener3f(AL10.AL_VELOCITY, 0f,0f,0f);
    AL10.alListener3f(AL10.AL_ORIENTATION, 0f,1f, 0f);
  }

}