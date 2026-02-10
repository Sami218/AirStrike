package com.nokia.mid.sound;

/**
 * Nokia-specific Sound API stub.
 * The original game used Nokia OTT (One-Track Tones) sound format
 * which is not supported in browsers. All methods are no-ops.
 */
public class Sound {

    /** Format constant for OTT tone sequences. */
    public static final int FORMAT_TONE = 5;

    /** Format constant for WAV audio. */
    public static final int FORMAT_WAV = 32;

    /**
     * Creates a Sound with the specified frequency and duration.
     * No-op in the browser port.
     *
     * @param freq     the frequency in Hz
     * @param duration the duration in milliseconds
     */
    public Sound(int freq, long duration) {
        // No-op in browser environment
    }

    /**
     * Creates a Sound from the specified byte data.
     * No-op in the browser port.
     *
     * @param data the sound data bytes
     * @param type the sound format type (e.g. FORMAT_TONE)
     */
    public Sound(byte[] data, int type) {
        // No-op in browser environment
    }

    /**
     * Initializes the sound with new data.
     * No-op in the browser port.
     *
     * @param data the sound data bytes
     * @param type the sound format type
     */
    public void init(byte[] data, int type) {
        // No-op in browser environment
    }

    /**
     * Plays the sound the specified number of times.
     * No-op in the browser port.
     *
     * @param loop number of times to loop (1 = play once)
     */
    public void play(int loop) {
        // No-op in browser environment
    }

    /**
     * Stops the currently playing sound.
     * No-op in the browser port.
     */
    public void stop() {
        // No-op in browser environment
    }

    /**
     * Releases resources associated with this Sound.
     * No-op in the browser port.
     */
    public void release() {
        // No-op in browser environment
    }

    /**
     * Returns the current state of the sound.
     *
     * @return the sound state
     */
    public int getState() {
        return 0;
    }
}
