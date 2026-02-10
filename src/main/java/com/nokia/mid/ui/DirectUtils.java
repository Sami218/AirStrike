package com.nokia.mid.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.teavm.jso.JSBody;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;

/**
 * Nokia-specific utility class for creating images from raw byte data
 * and obtaining DirectGraphics instances.
 * <p>
 * In the browser port, createImage converts raw PNG byte data to a
 * base64 data URL, loads it synchronously into an HTMLImageElement,
 * draws it onto an offscreen HTMLCanvasElement, and wraps the result
 * in a J2ME Image.
 */
public class DirectUtils {

    private DirectUtils() {
        // Utility class - no instances
    }

    /**
     * Creates an Image from raw PNG byte data.
     *
     * @param imageData   the raw image data (typically PNG)
     * @param imageOffset offset into the byte array
     * @param imageLength number of bytes to use
     * @return an Image containing the decoded pixel data
     */
    public static Image createImage(byte[] imageData, int imageOffset, int imageLength) {
        // Extract the relevant portion of the byte array
        byte[] data;
        if (imageOffset == 0 && imageLength == imageData.length) {
            data = imageData;
        } else {
            data = new byte[imageLength];
            System.arraycopy(imageData, imageOffset, data, 0, imageLength);
        }

        // Convert to base64 data URL
        String base64 = bytesToBase64(data);
        String dataUrl = "data:image/png;base64," + base64;

        // Use synchronous JavaScript interop to load the image onto a canvas
        // and extract width/height
        int[] dimensions = loadImageSync(dataUrl);
        int width = dimensions[0];
        int height = dimensions[1];

        if (width <= 0 || height <= 0) {
            // Fallback: create a 1x1 transparent image
            width = 1;
            height = 1;
        }

        // Create the Image wrapper using the data URL
        return Image.createImageFromDataUrl(dataUrl, width, height);
    }

    /**
     * Returns a DirectGraphics instance that wraps the given Graphics object.
     *
     * @param g the Graphics to wrap
     * @return a DirectGraphics that delegates to g with manipulation support
     */
    public static DirectGraphics getDirectGraphics(Graphics g) {
        return new DirectGraphicsImpl(g);
    }

    /**
     * Converts a byte array to a Base64 encoded string using JavaScript's btoa.
     */
    private static String bytesToBase64(byte[] data) {
        return bytesToBase64JS(data);
    }

    @JSBody(params = { "data" }, script =
        "var binary = ''; " +
        "var arr = data.data ? data.data : data; " +
        "for (var i = 0; i < arr.length; i++) { " +
        "    binary += String.fromCharCode(arr[i] & 0xFF); " +
        "} " +
        "return btoa(binary);")
    private static native String bytesToBase64JS(byte[] data);

    /**
     * Synchronously loads an image from a data URL and returns [width, height].
     * Since data URLs are decoded synchronously by the browser when assigned
     * to an Image element (no network request needed), this works even in
     * a single-threaded TeaVM context.
     */
    @JSBody(params = { "dataUrl" }, script =
        "var img = new Image(); " +
        "img.src = dataUrl; " +
        "return [img.naturalWidth || img.width || 0, img.naturalHeight || img.height || 0];")
    private static native int[] loadImageSync(String dataUrl);
}
