package com.nokia.mid.ui;

import javax.microedition.lcdui.Image;

/**
 * Nokia-specific extended graphics interface that provides
 * additional drawing operations not available in standard J2ME
 * Graphics, such as image manipulation (rotation, flipping).
 */
public interface DirectGraphics {

    /** Flip the image horizontally. */
    public static final int FLIP_HORIZONTAL = 0x2000;

    /** Flip the image vertically. */
    public static final int FLIP_VERTICAL = 0x4000;

    /** Rotate the image 90 degrees clockwise. */
    public static final int ROTATE_90 = 90;

    /** Rotate the image 180 degrees. */
    public static final int ROTATE_180 = 180;

    /** Rotate the image 270 degrees clockwise (90 counter-clockwise). */
    public static final int ROTATE_270 = 270;

    /**
     * Draws an image with the specified manipulation (rotation/flip).
     *
     * @param img          the image to draw
     * @param x            the x coordinate of the anchor point
     * @param y            the y coordinate of the anchor point
     * @param anchor       the anchor point specification (using Graphics anchor constants)
     * @param manipulation the manipulation to apply (one of the FLIP_* or ROTATE_* constants,
     *                     or 0 for no manipulation)
     */
    void drawImage(Image img, int x, int y, int anchor, int manipulation);
}
