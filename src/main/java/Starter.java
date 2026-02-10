import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLCanvasElement;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import com.im.flight.control.MainControl;
import com.im.flight.view.game.GameObject;

/*
 * TeaVM entry point for AirStrike browser port.
 * Replaces the original J2ME MIDlet Starter.
 *
 * All image resources are loaded asynchronously via XHR, then decoded
 * using an HTML <img> element's onload callback to guarantee the pixel
 * data is available before being drawn to an offscreen canvas.
 */
public class Starter {

    // PNG image resources (loaded via Image.createImage)
    private static final String[] PNG_RESOURCES = {
        "terrain.png", "fortress.png", "icon.png", "license.png",
        "background.png", "parachute.png"
    };

    // Extensionless image resources (raw PNG bytes via DirectUtils.createImage)
    private static final String[] RAW_IMAGE_RESOURCES = {
        "ae1", "ae2", "ae3", "ae4", "b1", "bc", "bs1", "bs2", "bs3", "bs4", "bs5",
        "bu1", "bub1", "bush", "but1", "c1", "cd1", "cl", "e1", "e2", "e3", "e4",
        "h1", "m1", "m2", "p1", "p2", "sc", "se1", "se2", "se3", "se4",
        "tb1", "tr1", "tt1", "w", "ff1", "po1", "pa1",
        "bo1", "l", "s1", "su1", "ss1", "ss2", "ss3", "ss4", "t1",
        "lo", "co", "hand1", "hand2"
    };

    // Level data files (binary, loaded via getResourceAsStream)
    private static final String[] LEVEL_DATA_RESOURCES = {
        "1.l", "2.l", "3.l", "4.l",
        "1.o", "2.o", "3.o", "4.o"
    };

    // Sound files (binary)
    private static final String[] SOUND_RESOURCES = {
        "0.ott", "1.ott", "3.ott", "4.ott", "5.ott"
    };

    private static int totalResources;
    private static int loadedResources;

    public static void main(String[] args) {
        try {
            System.out.println("AirStrike Web - Starting...");

            HTMLDocument document = Window.current().getDocument();
            HTMLCanvasElement canvas = (HTMLCanvasElement) document.getElementById("gameCanvas");

            if (canvas == null) {
                System.out.println("ERROR: Could not find gameCanvas element!");
                return;
            }

            // Hide loading indicator
            hideLoading();

            // Initialize the display system with the canvas
            Display.init(canvas);

            System.out.println("Display initialized. Preloading resources...");

            // Calculate total resources to preload
            totalResources = PNG_RESOURCES.length + RAW_IMAGE_RESOURCES.length
                           + LEVEL_DATA_RESOURCES.length + SOUND_RESOURCES.length;
            loadedResources = 0;

            // Preload all resources, then start the game
            preloadAllResources(new Runnable() {
                public void run() {
                    System.out.println("All resources preloaded (" + loadedResources + "/" + totalResources + "). Starting game...");
                    try {
                        new MainControl(null);
                    } catch (Throwable t) {
                        System.out.println("Error starting game: " + t.toString());
                        t.printStackTrace();
                    }
                }
            });

        } catch (Throwable t) {
            System.out.println("Fatal error in Starter: " + t.toString());
            t.printStackTrace();
        }
    }

    @JSBody(script = "var el = document.getElementById('loading'); if (el) el.style.display = 'none';")
    private static native void hideLoading();

    private static void preloadAllResources(final Runnable onComplete) {
        final int[] remaining = { totalResources };

        Runnable checkDone = new Runnable() {
            public void run() {
                remaining[0]--;
                loadedResources++;
                if (remaining[0] <= 0) {
                    onComplete.run();
                }
            }
        };

        // Load all image resources (both PNG and raw) using the JS-based async
        // image decoder that waits for onload before drawing to canvas.
        for (int i = 0; i < PNG_RESOURCES.length; i++) {
            preloadImageViaJS(PNG_RESOURCES[i], checkDone);
        }

        for (int i = 0; i < RAW_IMAGE_RESOURCES.length; i++) {
            preloadImageViaJS(RAW_IMAGE_RESOURCES[i], checkDone);
        }

        for (int i = 0; i < LEVEL_DATA_RESOURCES.length; i++) {
            preloadBinaryResource(LEVEL_DATA_RESOURCES[i], checkDone);
        }

        for (int i = 0; i < SOUND_RESOURCES.length; i++) {
            preloadBinaryResource(SOUND_RESOURCES[i], checkDone);
        }

        if (totalResources == 0) {
            onComplete.run();
        }
    }

    /**
     * Preload an image resource (PNG or raw) by:
     * 1. Fetching the raw bytes via XHR (arraybuffer)
     * 2. Creating a Blob URL from the bytes
     * 3. Loading an HTML Image element from the Blob URL (async onload)
     * 4. Drawing the decoded image onto an offscreen canvas
     * 5. Passing the canvas back to Java for caching
     *
     * This approach guarantees the image is fully decoded before use.
     */
    private static void preloadImageViaJS(final String name, final Runnable onDone) {
        final String path = "res/" + name;
        loadImageToCanvasJS(path, new CanvasCallback() {
            public void onComplete(HTMLCanvasElement canvas) {
                if (canvas != null) {
                    try {
                        // Create ONE Image and cache it under all key variants
                        Image.preloadImage(name, canvas);
                        // preloadImage already puts into cache, now retrieve it
                        Image img = Image.getImageCache().get(name);
                        if (img != null) {
                            // Cache under all lookup variants
                            Image.cacheImage("res/" + name, img);
                            Image.cacheImage("/res/" + name, img);
                            GameObject.cacheImage(name, img);
                        }
                        System.out.println("Preloaded image: " + name + " (" + canvas.getWidth() + "x" + canvas.getHeight() + ")");
                    } catch (Throwable t) {
                        System.out.println("Failed to cache image " + name + ": " + t);
                    }
                } else {
                    System.out.println("Failed to load image: " + name);
                }
                onDone.run();
            }
        });

        // Also fetch raw bytes for resources that need getResourceAsStream()
        fetchBytesJS(path, new FetchCallback() {
            public void onComplete(byte[] data) {
                if (data != null) {
                    Image.cacheResourceBytes("/res/" + name, data);
                    Image.cacheResourceBytes("res/" + name, data);
                }
            }
        });
    }

    private static void preloadBinaryResource(final String filename, final Runnable onDone) {
        final String path = "res/" + filename;
        fetchBytesJS(path, new FetchCallback() {
            public void onComplete(byte[] data) {
                if (data != null) {
                    try {
                        Image.cacheResourceBytes("/res/" + filename, data);
                        Image.cacheResourceBytes("res/" + filename, data);
                        System.out.println("Preloaded resource: " + filename);
                    } catch (Throwable t) {
                        System.out.println("Failed to cache resource " + filename + ": " + t);
                    }
                } else {
                    System.out.println("Failed to fetch resource: " + filename);
                }
                onDone.run();
            }
        });
    }

    /**
     * Fetch raw bytes of a resource file and then create an HTML Image from
     * a Blob URL, waiting for the onload event. Once loaded, draw onto an
     * offscreen canvas and return the canvas via callback.
     *
     * This is entirely done in JavaScript to ensure the image is fully
     * decoded before any Java code tries to use the pixel data.
     */
    @JSBody(params = { "path", "callback" }, script =
        "var xhr = new XMLHttpRequest();" +
        "xhr.open('GET', path, true);" +
        "xhr.responseType = 'arraybuffer';" +
        "xhr.onload = function() {" +
        "    if (xhr.status === 200) {" +
        "        var buffer = xhr.response;" +
        "        var blob = new Blob([buffer], {type: 'image/png'});" +
        "        var url = URL.createObjectURL(blob);" +
        "        var img = new window.Image();" +
        "        img.onload = function() {" +
        "            var canvas = document.createElement('canvas');" +
        "            canvas.width = img.naturalWidth;" +
        "            canvas.height = img.naturalHeight;" +
        "            var ctx = canvas.getContext('2d');" +
        "            ctx.drawImage(img, 0, 0);" +
        "            URL.revokeObjectURL(url);" +
        "            callback(canvas);" +
        "        };" +
        "        img.onerror = function() {" +
        "            URL.revokeObjectURL(url);" +
        "            console.log('Image decode error for: ' + path);" +
        "            callback(null);" +
        "        };" +
        "        img.src = url;" +
        "    } else {" +
        "        callback(null);" +
        "    }" +
        "};" +
        "xhr.onerror = function() { callback(null); };" +
        "xhr.send();")
    private static native void loadImageToCanvasJS(String path, CanvasCallback callback);

    /**
     * Fetch a binary file from the server as a byte array using native JavaScript XHR.
     * Uses @JSBody to avoid dependency on TeaVM's XMLHttpRequest wrapper API specifics.
     */
    @JSBody(params = { "path", "callback" }, script =
        "var xhr = new XMLHttpRequest();" +
        "xhr.open('GET', path, true);" +
        "xhr.responseType = 'arraybuffer';" +
        "xhr.onload = function() {" +
        "    if (xhr.status === 200) {" +
        "        var buffer = xhr.response;" +
        "        var uint8 = new Uint8Array(buffer);" +
        "        var arr = new Int8Array(uint8.length);" +
        "        for (var i = 0; i < uint8.length; i++) {" +
        "            arr[i] = uint8[i] > 127 ? uint8[i] - 256 : uint8[i];" +
        "        }" +
        "        callback(arr);" +
        "    } else {" +
        "        callback(null);" +
        "    }" +
        "};" +
        "xhr.onerror = function() { callback(null); };" +
        "xhr.send();")
    private static native void fetchBytesJS(String path, FetchCallback callback);

    @JSFunctor
    interface FetchCallback extends JSObject {
        void onComplete(byte[] data);
    }

    @JSFunctor
    interface CanvasCallback extends JSObject {
        void onComplete(HTMLCanvasElement canvas);
    }
}
