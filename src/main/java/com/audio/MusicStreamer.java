package com.audio;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class MusicStreamer {

  private static final int BUFFER_COUNT = 4; // number of OpenAL buffers
  private static final int BUFFER_SAMPLES = 4096; // samples per buffer

  private long device;
  private long context;
  private int source;
  private int[] buffers;

  private long vorbis;
  private STBVorbisInfo info;
  private IntBuffer error;

  private int format;
  private int sampleRate;
  private String filename;

  private boolean looping = true; // toggle looping playback

  // ----------------------------------------------------
  // INITIALIZATION
  // ----------------------------------------------------
  public void init(AudioSystem audioSystem, String filename) {
    this.filename = filename;

    // Use shared context
    alcMakeContextCurrent(audioSystem.getContext());
    AL.createCapabilities(ALC.createCapabilities(audioSystem.getDevice()));

    // === Open OGG file ===
    error = MemoryUtil.memAllocInt(1);
    vorbis = stb_vorbis_open_filename(filename, error, null);
    if (vorbis == NULL) {
      throw new RuntimeException("Failed to open OGG file: " + filename + " (error " + error.get(0) + ")");
    }

    info = STBVorbisInfo.malloc();
    stb_vorbis_get_info(vorbis, info);

    sampleRate = info.sample_rate();
    format = (info.channels() == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

    // === OpenAL source + buffers ===
    source = alGenSources();
    buffers = new int[BUFFER_COUNT];
    for (int i = 0; i < BUFFER_COUNT; i++) {
      buffers[i] = alGenBuffers();
    }

    // Preload initial buffers
    for (int buf : buffers) {
      ShortBuffer data = decodeChunk();
      if (data == null)
        break;
      alBufferData(buf, format, data, sampleRate);
      MemoryUtil.memFree(data);
    }
    alSourceQueueBuffers(source, buffers);
  }

  // ----------------------------------------------------
  // MAIN STREAMING LOOP
  // ----------------------------------------------------
  public void streamLoop() {
    boolean playing = true;

    alSourcePlay(source);

    while (playing) {
      int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);

      while (processed-- > 0) {
        int buf = alSourceUnqueueBuffers(source);

        ShortBuffer data = decodeChunk();

        // If EOF reached or temporary decode gap
        if (data == null) {
          if (looping) {
            // Reopen file for looping
            stb_vorbis_close(vorbis);
            vorbis = stb_vorbis_open_filename(filename, error, null);
            if (vorbis == NULL) {
              System.err.println("Failed to reopen OGG for looping.");
              playing = false;
              break;
            }
            stb_vorbis_get_info(vorbis, info);
            data = decodeChunk();
            if (data == null)
              continue;
          } else {
            playing = false;
            break;
          }
        }

        alBufferData(buf, format, data, sampleRate);
        MemoryUtil.memFree(data);
        alSourceQueueBuffers(source, buf);
      }

      // Restart playback if it stopped unexpectedly
      int state = alGetSourcei(source, AL_SOURCE_STATE);
      if (state != AL_PLAYING && playing) {
        alSourcePlay(source);
      }

      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    alSourceStop(source);
    System.out.println("Music stream ended.");
  }

  // ----------------------------------------------------
  // DECODING
  // ----------------------------------------------------
  private ShortBuffer decodeChunk() {
    int channels = info.channels();
    ShortBuffer pcm = MemoryUtil.memAllocShort(BUFFER_SAMPLES * channels);
    int total = 0;

    while (total < BUFFER_SAMPLES) {
      int n = stb_vorbis_get_samples_short_interleaved(vorbis, channels, pcm.position(total * channels));
      if (n <= 0)
        break; // EOF
      total += n;
    }

    pcm.limit(total * channels);
    if (total == 0) {
      MemoryUtil.memFree(pcm);
      return null;
    }
    return pcm;
  }

  // ----------------------------------------------------
  // CLEANUP
  // ----------------------------------------------------
  public void cleanup() {
    alSourceStop(source);
    alDeleteSources(source);
    for (int buf : buffers)
      alDeleteBuffers(buf);
    stb_vorbis_close(vorbis);
    info.free();
    MemoryUtil.memFree(error);
    alcDestroyContext(context);
    alcCloseDevice(device);
  }

  public void playAsync() {
    Thread streamThread = new Thread(() -> {
      streamLoop();
      cleanup(); // optional auto-cleanup when done
    });
    streamThread.setDaemon(true); // optional — stops when game exits
    streamThread.start();
  }

  // ----------------------------------------------------
  // SETTERS
  // ----------------------------------------------------
  public void setLooping(boolean loop) {
    this.looping = loop;
  }


}
