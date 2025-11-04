package com.audio;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Sound {
  private int bufferId;
  private int sourceId;

  public Sound(String filePath) {
    try (MemoryStack stack = stackPush()) {
      // Allocate space for output values
      IntBuffer channelsBuffer = stack.mallocInt(1);
      IntBuffer sampleRateBuffer = stack.mallocInt(1);

      // Decode the OGG file into PCM data
      ShortBuffer pcm = stb_vorbis_decode_filename(filePath, channelsBuffer, sampleRateBuffer);
      if (pcm == null) {
        throw new RuntimeException("Failed to load sound file: " + filePath);
      }

      int channels = channelsBuffer.get(0);
      int sampleRate = sampleRateBuffer.get(0);

      // Choose format (mono or stereo)
      int format;
      if (channels == 1) {
        format = AL_FORMAT_MONO16;
      } else if (channels == 2) {
        format = AL_FORMAT_STEREO16;
      } else {
        throw new RuntimeException("Unsupported number of channels: " + channels);
      }

      // Generate buffer and fill with PCM data
      bufferId = alGenBuffers();
      alBufferData(bufferId, format, pcm, sampleRate);

      // Free STB memory
      MemoryUtil.memFree(pcm);

      // Create a source and attach the buffer
      sourceId = alGenSources();
      alSourcei(sourceId, AL_BUFFER, bufferId);

                  // Default 3D sound settings
      alSourcef(sourceId, AL_REFERENCE_DISTANCE, 1.0f);
      alSourcef(sourceId, AL_ROLLOFF_FACTOR, 1.0f);
      alSourcef(sourceId, AL_MAX_DISTANCE, 50.0f);
      alSource3f(sourceId, AL_POSITION, 0f, 0f, 0f);
      alSourcef(sourceId, AL_GAIN, 2.0f);

      System.out.println("Sound loaded successfully.");
        
    }
  }

  public void play(Vector3f listenerPos, Vector3f sourcePos) {
    // Listener setup (the player/camera)
        //Vector3f listenerPos = new Vector3f(0f, 0f, 0f);
        Vector3f listenerVel = new Vector3f(0f, 0f, 0f);
        float[] orientation = {
                0f, 0f, -1f, // "at" vector
                0f, 1f, 0f   // "up" vector
        };

        alListener3f(AL_POSITION, listenerPos.x, listenerPos.y, listenerPos.z);
        alListener3f(AL_VELOCITY, listenerVel.x, listenerVel.y, listenerVel.z);
        alListenerfv(AL_ORIENTATION, orientation);

        // Start playback
        alSource3f(sourceId, AL_POSITION, sourcePos.x, sourcePos.y, sourcePos.z);
        if (!isPlaying()) {
            alSourcePlay(sourceId);
        }        
  }

  /** Check if the sound is currently playing */
  public boolean isPlaying() {
    int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
    return state == AL_PLAYING;
  }

  /** Pause the sound */
  public void pause() {
    alSourcePause(sourceId);
  }

  /** Resume a paused sound */
  public void resume() {
    alSourcePlay(sourceId);
  }

  public void stop() {
    alSourceStop(sourceId);
  }

  public void cleanup() {
    alDeleteSources(sourceId);
    alDeleteBuffers(bufferId);
  }
}
