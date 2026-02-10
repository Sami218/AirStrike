package javax.microedition.lcdui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

public class Image {

    private HTMLCanvasElement canvas;
    private int width;
    private int height;
    private boolean mutable;

    // Cache for preloaded images (keyed by resource name)
    private static HashMap<String, Image> imageCache = new HashMap<>();

    // Cache for preloaded binary resources (level data, sounds, etc.)
    private static HashMap<String, byte[]> resourceBytesCache = new HashMap<>();

    private Image() {
    }

    /**
     * Create a mutable image backed by an offscreen canvas.
     */
    public static Image createImage(int width, int height) {
        HTMLDocument doc = Window.current().getDocument();
        HTMLCanvasElement canvas = (HTMLCanvasElement) doc.createElement("canvas");
        canvas.setWidth(width);
        canvas.setHeight(height);

        Image img = new Image();
        img.canvas = canvas;
        img.width = width;
        img.height = height;
        img.mutable = true;
        return img;
    }

    /**
     * Create an image from a classpath resource name.
     * Looks up the image from the preloaded cache.
     */
    public static Image createImage(String name) throws IOException {
        // Normalize the name: the game uses paths like "/res/license.png" or "/res/co"
        String key = name;
        if (key.startsWith("/")) {
            key = key.substring(1);
        }

        Image cached = imageCache.get(key);
        if (cached != null) {
            return cached;
        }

        // Also try with the original name
        cached = imageCache.get(name);
        if (cached != null) {
            return cached;
        }

        // If not found, create a small placeholder (1x1 transparent) to avoid crash
        System.out.println("WARNING: Image not found in cache: " + name);
        Image placeholder = createImage(1, 1);
        placeholder.mutable = false;
        return placeholder;
    }

    /**
     * Called by the resource loader to populate the image cache before the game starts.
     * The HTMLCanvasElement should already contain the decoded image data.
     */
    public static void preloadImage(String name, HTMLCanvasElement canvas) {
        Image img = new Image();
        img.canvas = canvas;
        img.width = canvas.getWidth();
        img.height = canvas.getHeight();
        img.mutable = false;
        imageCache.put(name, img);
    }

    /**
     * Get a Graphics context for drawing on this mutable image.
     */
    public Graphics getGraphics() {
        if (canvas == null) {
            throw new IllegalStateException("Image has no canvas backing");
        }
        CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
        return new Graphics(ctx, width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Get the underlying HTMLCanvasElement for use in drawImage operations.
     */
    public HTMLCanvasElement getCanvas() {
        return canvas;
    }

    /**
     * Returns the image cache (for diagnostics or external manipulation).
     */
    public static HashMap<String, Image> getImageCache() {
        return imageCache;
    }

    /**
     * Creates an Image from a data URL (e.g. base64-encoded PNG) with known dimensions.
     * Used by DirectUtils.createImage() for Nokia-style image loading from raw bytes.
     *
     * @param dataUrl the data:image/png;base64,... URL
     * @param width   the image width
     * @param height  the image height
     * @return a new immutable Image containing the decoded pixel data
     */
    public static Image createImageFromDataUrl(String dataUrl, int width, int height) {
        HTMLDocument doc = Window.current().getDocument();
        HTMLCanvasElement canvas = (HTMLCanvasElement) doc.createElement("canvas");
        canvas.setWidth(width);
        canvas.setHeight(height);

        // Draw the data URL image onto the canvas synchronously
        drawDataUrlToCanvas(canvas, dataUrl, width, height);

        Image img = new Image();
        img.canvas = canvas;
        img.width = width;
        img.height = height;
        img.mutable = false;
        return img;
    }

    /**
     * Draw a data URL image onto a canvas. This uses a synchronous attempt
     * first, and if that fails sets up an onload handler. For preloaded
     * resources (which use the Blob URL approach in Starter.java), this
     * method is not used. It serves as a fallback for runtime image creation.
     */
    @JSBody(params = { "canvas", "dataUrl", "width", "height" }, script =
        "var ctx = canvas.getContext('2d'); " +
        "var img = new window.Image(); " +
        "img.src = dataUrl; " +
        "if (img.complete && img.naturalWidth > 0) { " +
        "    ctx.drawImage(img, 0, 0); " +
        "} else { " +
        "    img.onload = function() { ctx.drawImage(img, 0, 0); }; " +
        "}")
    private static native void drawDataUrlToCanvas(HTMLCanvasElement canvas, String dataUrl, int width, int height);

    /**
     * Cache a preloaded image under the given path.
     * Called by Starter.java during resource preloading.
     * The path is stored both as-is and with leading "/" stripped so that
     * lookups via createImage("/res/foo.png") and createImage("res/foo.png") both work.
     */
    public static void cacheImage(String path, Image img) {
        if (path != null && img != null) {
            imageCache.put(path, img);
            // Also store without leading /
            if (path.startsWith("/")) {
                imageCache.put(path.substring(1), img);
            } else {
                imageCache.put("/" + path, img);
            }
        }
    }

    /**
     * Create an Image from raw PNG byte data.
     * Encodes the bytes as a base64 data URL and draws onto an offscreen canvas.
     */
    public static Image createImageFromBytes(byte[] data) {
        // Convert bytes to base64 data URL
        String base64 = bytesToBase64(data);
        String dataUrl = "data:image/png;base64," + base64;

        // Decode synchronously using a temporary JS Image
        int[] dims = getImageDimensions(dataUrl);
        int width = dims[0];
        int height = dims[1];

        if (width <= 0 || height <= 0) {
            // Fallback: try to decode the PNG header for dimensions
            width = getPngWidth(data);
            height = getPngHeight(data);
            if (width <= 0) width = 1;
            if (height <= 0) height = 1;
        }

        return createImageFromDataUrl(dataUrl, width, height);
    }

    /**
     * Cache binary resource bytes (level data, sound files, etc.).
     * These can later be retrieved via getResourceBytes() for use by
     * getResourceAsStream() in the compatibility layer.
     */
    public static void cacheResourceBytes(String path, byte[] data) {
        if (path != null && data != null) {
            resourceBytesCache.put(path, data);
            if (path.startsWith("/")) {
                resourceBytesCache.put(path.substring(1), data);
            } else {
                resourceBytesCache.put("/" + path, data);
            }
        }
    }

    /**
     * Get cached resource bytes for the given path.
     * Returns null if not found.
     */
    public static byte[] getResourceBytes(String path) {
        if (path == null) return null;
        byte[] data = resourceBytesCache.get(path);
        if (data == null && path.startsWith("/")) {
            data = resourceBytesCache.get(path.substring(1));
        }
        if (data == null && !path.startsWith("/")) {
            data = resourceBytesCache.get("/" + path);
        }
        return data;
    }

    /**
     * Get a resource as an InputStream from the preloaded byte cache.
     * This is used by the game code that calls getResourceAsStream().
     */
    public static InputStream getResourceAsStream(String path) {
        byte[] data = getResourceBytes(path);
        if (data != null) {
            return new ByteArrayInputStream(data);
        }
        return null;
    }

    // --- Helper: convert byte array to base64 ---
    private static String bytesToBase64(byte[] data) {
        return encodeBase64JS(data);
    }

    @JSBody(params = { "data" }, script =
        "var binary = ''; " +
        "var arr = data.data ? data.data : data; " +
        "for (var i = 0; i < arr.length; i++) { " +
        "    binary += String.fromCharCode(arr[i] & 0xff); " +
        "} " +
        "return btoa(binary);")
    private static native String encodeBase64JS(byte[] data);

    // --- Helper: get image dimensions from a data URL synchronously ---
    @JSBody(params = { "dataUrl" }, script =
        "var img = new window.Image();" +
        "img.src = dataUrl;" +
        "if (img.complete && img.naturalWidth > 0) {" +
        "    return [img.naturalWidth, img.naturalHeight];" +
        "}" +
        "return [0, 0];")
    private static native int[] getImageDimensions(String dataUrl);

    // --- Helper: read PNG width from header bytes ---
    private static int getPngWidth(byte[] data) {
        if (data.length >= 24 && data[0] == (byte)0x89 && data[1] == (byte)0x50) {
            return ((data[16] & 0xFF) << 24) | ((data[17] & 0xFF) << 16) |
                   ((data[18] & 0xFF) << 8) | (data[19] & 0xFF);
        }
        return 0;
    }

    // --- Helper: read PNG height from header bytes ---
    private static int getPngHeight(byte[] data) {
        if (data.length >= 24 && data[0] == (byte)0x89 && data[1] == (byte)0x50) {
            return ((data[20] & 0xFF) << 24) | ((data[21] & 0xFF) << 16) |
                   ((data[22] & 0xFF) << 8) | (data[23] & 0xFF);
        }
        return 0;
    }
}
