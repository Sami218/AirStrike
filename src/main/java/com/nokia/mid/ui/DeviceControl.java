package com.nokia.mid.ui;

/**
 * Nokia-specific device control API stub.
 * Vibration and backlight controls are not applicable in a browser
 * environment, so all methods are no-ops.
 */
public class DeviceControl {

    private DeviceControl() {
        // Utility class - no instances
    }

    /**
     * Starts the device vibrator.
     * No-op in the browser port.
     *
     * @param freq     vibration frequency
     * @param duration vibration duration in milliseconds
     */
    public static void startVibra(int freq, long duration) {
        // No-op in browser environment
    }

    /**
     * Sets the device backlight level.
     * No-op in the browser port.
     *
     * @param num   the light number
     * @param level the brightness level (0-100)
     */
    public static void setLights(int num, int level) {
        // No-op in browser environment
    }
}
