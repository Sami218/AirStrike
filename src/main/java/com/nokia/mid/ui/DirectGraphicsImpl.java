package com.nokia.mid.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * Implementation of DirectGraphics that delegates to the underlying
 * Graphics object, applying rotation and flip transformations using
 * HTML Canvas 2D context save/restore/translate/scale/rotate via
 * the Graphics implementation.
 */
class DirectGraphicsImpl implements DirectGraphics {

    private final Graphics g;

    DirectGraphicsImpl(Graphics g) {
        this.g = g;
    }

    @Override
    public void drawImage(Image img, int x, int y, int anchor, int manipulation) {
        if (manipulation == 0) {
            // No manipulation - just draw normally
            g.drawImage(img, x, y, anchor);
            return;
        }

        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        // Calculate top-left position from anchor
        int dx = anchorX(x, imgWidth, imgHeight, anchor, manipulation);
        int dy = anchorY(y, imgWidth, imgHeight, anchor, manipulation);

        // Use the Graphics object's canvas context to apply transforms.
        // The Graphics class exposes save/restore/translate/scale/rotate
        // for this purpose.
        g.save();

        if (manipulation == ROTATE_90) {
            g.translate(dx + imgHeight, dy);
            g.rotate(Math.PI / 2);
        } else if (manipulation == ROTATE_180) {
            g.translate(dx + imgWidth, dy + imgHeight);
            g.rotate(Math.PI);
        } else if (manipulation == ROTATE_270) {
            g.translate(dx, dy + imgWidth);
            g.rotate(-Math.PI / 2);
        } else if (manipulation == FLIP_HORIZONTAL) {
            g.translate(dx + imgWidth, dy);
            g.scale(-1, 1);
        } else if (manipulation == FLIP_VERTICAL) {
            g.translate(dx, dy + imgHeight);
            g.scale(1, -1);
        } else if (manipulation == (FLIP_HORIZONTAL | FLIP_VERTICAL)) {
            g.translate(dx + imgWidth, dy + imgHeight);
            g.scale(-1, -1);
        } else {
            // Unknown manipulation, just draw at computed position
            g.translate(dx, dy);
        }

        // Draw the image at origin (transforms have positioned it)
        g.drawImageRaw(img, 0, 0);

        g.restore();
    }

    /**
     * Compute the top-left X coordinate from the anchor specification,
     * accounting for the fact that rotations swap width and height.
     */
    private int anchorX(int x, int imgWidth, int imgHeight, int anchor, int manipulation) {
        int effectiveWidth;
        if (manipulation == ROTATE_90 || manipulation == ROTATE_270) {
            effectiveWidth = imgHeight;
        } else {
            effectiveWidth = imgWidth;
        }

        if ((anchor & Graphics.HCENTER) != 0) {
            return x - effectiveWidth / 2;
        } else if ((anchor & Graphics.RIGHT) != 0) {
            return x - effectiveWidth;
        }
        // LEFT or default
        return x;
    }

    /**
     * Compute the top-left Y coordinate from the anchor specification,
     * accounting for the fact that rotations swap width and height.
     */
    private int anchorY(int y, int imgWidth, int imgHeight, int anchor, int manipulation) {
        int effectiveHeight;
        if (manipulation == ROTATE_90 || manipulation == ROTATE_270) {
            effectiveHeight = imgWidth;
        } else {
            effectiveHeight = imgHeight;
        }

        if ((anchor & Graphics.VCENTER) != 0) {
            return y - effectiveHeight / 2;
        } else if ((anchor & Graphics.BOTTOM) != 0) {
            return y - effectiveHeight;
        }
        // TOP or default
        return y;
    }
}
