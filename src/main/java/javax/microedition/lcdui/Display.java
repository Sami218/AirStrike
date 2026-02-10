package javax.microedition.lcdui;

import javax.microedition.midlet.MIDlet;

import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

/**
 * J2ME Display compatibility class.
 * Manages the current screen, rendering loop, and keyboard event dispatch.
 * Backed by an HTML5 Canvas element.
 */
public class Display {

    private static Display instance;
    private static HTMLCanvasElement mainCanvas;
    private static CanvasRenderingContext2D mainCtx;

    private Displayable currentDisplayable;
    private boolean renderLoopRunning = false;

    // Keyboard event listeners (stored for removal if needed)
    private EventListener<KeyboardEvent> keyDownListener;
    private EventListener<KeyboardEvent> keyUpListener;

    // Key repeat tracking
    private int lastKeyCode = 0;
    private boolean keyDown = false;
    private double lastKeyRepeatTime = 0;
    private static final double KEY_REPEAT_DELAY = 200; // ms before first repeat
    private static final double KEY_REPEAT_INTERVAL = 80; // ms between repeats

    private Display() {
    }

    /**
     * Initialize the Display with the main HTML canvas element.
     * Must be called once during application startup before getDisplay().
     */
    public static void init(HTMLCanvasElement canvas) {
        mainCanvas = canvas;
        mainCtx = (CanvasRenderingContext2D) canvas.getContext("2d");
    }

    /**
     * Get the Display instance (singleton).
     */
    public static Display getDisplay(MIDlet midlet) {
        if (instance == null) {
            instance = new Display();
        }
        return instance;
    }

    /**
     * Set the current displayable (screen).
     * If it is a Canvas, starts the rendering loop and hooks keyboard events.
     */
    public void setCurrent(Displayable d) {
        if (d == null) {
            return;
        }

        // Notify previous canvas that it's being hidden
        if (currentDisplayable instanceof Canvas && currentDisplayable != d) {
            ((Canvas) currentDisplayable).hideNotify();
        }

        currentDisplayable = d;

        if (d instanceof Canvas) {
            Canvas canvas = (Canvas) d;
            hookKeyboardEvents(canvas);
            canvas.repaint();
            startRenderLoop();
        }
    }

    /**
     * Show an alert, then switch to the given displayable.
     * In this simplified implementation, we log the alert and immediately show d.
     */
    public void setCurrent(Alert alert, Displayable d) {
        if (alert != null) {
            System.out.println("ALERT [" + alert.getTitle() + "]: " + alert.getText());
        }
        setCurrent(d);
    }

    /**
     * Get the current displayable.
     */
    public Displayable getCurrent() {
        return currentDisplayable;
    }

    /**
     * Schedule a Runnable to be executed asynchronously.
     * Uses Window.setTimeout with 0 delay.
     */
    public void callSerially(Runnable r) {
        if (r != null) {
            Window.setTimeout(() -> r.run(), 0);
        }
    }

    // --- Rendering loop ---

    private void startRenderLoop() {
        if (renderLoopRunning) {
            return;
        }
        renderLoopRunning = true;
        scheduleNextFrame();
    }

    private void scheduleNextFrame() {
        Window.requestAnimationFrame((timestamp) -> {
            renderFrame(timestamp);
            if (renderLoopRunning) {
                scheduleNextFrame();
            }
        });
    }

    private void renderFrame(double timestamp) {
        if (!(currentDisplayable instanceof Canvas)) {
            return;
        }

        Canvas canvas = (Canvas) currentDisplayable;

        // Handle key repeat
        if (keyDown && lastKeyCode != 0) {
            if (timestamp - lastKeyRepeatTime >= KEY_REPEAT_INTERVAL) {
                canvas.keyRepeated(lastKeyCode);
                lastKeyRepeatTime = timestamp;
            }
        }

        // Always repaint each frame for smooth game rendering
        if (mainCtx != null) {
            Graphics g = new Graphics(mainCtx, mainCanvas.getWidth(), mainCanvas.getHeight());
            canvas.paint(g);
            canvas.clearRepaintFlag();
        }
    }

    // --- Keyboard event handling ---

    private void hookKeyboardEvents(final Canvas canvas) {
        HTMLDocument doc = Window.current().getDocument();

        // Remove previous listeners if any
        if (keyDownListener != null) {
            doc.removeEventListener("keydown", keyDownListener);
        }
        if (keyUpListener != null) {
            doc.removeEventListener("keyup", keyUpListener);
        }

        keyDownListener = new EventListener<KeyboardEvent>() {
            @Override
            public void handleEvent(KeyboardEvent evt) {
                if (!(currentDisplayable instanceof Canvas)) {
                    return;
                }
                Canvas currentCanvas = (Canvas) currentDisplayable;

                int j2meKeyCode = mapBrowserKeyCode(evt);
                if (j2meKeyCode != 0) {
                    evt.preventDefault();
                    if (!keyDown || lastKeyCode != j2meKeyCode) {
                        // New key press
                        keyDown = true;
                        lastKeyCode = j2meKeyCode;
                        lastKeyRepeatTime = System.currentTimeMillis() + KEY_REPEAT_DELAY - KEY_REPEAT_INTERVAL;
                        currentCanvas.keyPressed(j2meKeyCode);
                    }
                    // Repeated presses are handled in the render loop
                }
            }
        };

        keyUpListener = new EventListener<KeyboardEvent>() {
            @Override
            public void handleEvent(KeyboardEvent evt) {
                if (!(currentDisplayable instanceof Canvas)) {
                    return;
                }
                Canvas currentCanvas = (Canvas) currentDisplayable;

                int j2meKeyCode = mapBrowserKeyCode(evt);
                if (j2meKeyCode != 0) {
                    evt.preventDefault();
                    if (lastKeyCode == j2meKeyCode) {
                        keyDown = false;
                        lastKeyCode = 0;
                    }
                    currentCanvas.keyReleased(j2meKeyCode);
                }
            }
        };

        doc.addEventListener("keydown", keyDownListener);
        doc.addEventListener("keyup", keyUpListener);
    }

    /**
     * Map browser keyboard event to J2ME key code.
     * Returns 0 if the key is not mapped.
     */
    private int mapBrowserKeyCode(KeyboardEvent evt) {
        String key = evt.getKey();
        String code = evt.getCode();

        // Arrow keys -> J2ME arrow key codes
        switch (key) {
            case "ArrowUp":
                return Canvas.KEY_UP_ARROW;
            case "ArrowDown":
                return Canvas.KEY_DOWN_ARROW;
            case "ArrowLeft":
                return Canvas.KEY_LEFT_ARROW;
            case "ArrowRight":
                return Canvas.KEY_RIGHT_ARROW;
        }

        // Enter/Return -> SOFTKEY1 (select/pause)
        if ("Enter".equals(key)) {
            return -6;  // KEY_SOFTKEY1
        }

        // Escape -> SOFTKEY2 (back/quit)
        if ("Escape".equals(key)) {
            return -7;  // KEY_SOFTKEY2
        }

        // P -> SOFTKEY3 (pause)
        if ("p".equals(key) || "P".equals(key)) {
            return -5;  // KEY_SOFTKEY3
        }

        // C -> STAR (cheat toggle)
        if ("c".equals(key) || "C".equals(key)) {
            return Canvas.KEY_STAR;
        }

        // Space -> FIRE (bomb)
        if (" ".equals(key) || "Space".equals(code)) {
            return Canvas.KEY_NUM5;
        }

        // Number keys 0-9
        if (key.length() == 1 && key.charAt(0) >= '0' && key.charAt(0) <= '9') {
            return Canvas.KEY_NUM0 + (key.charAt(0) - '0');
        }

        // Numpad keys
        if (code != null && code.startsWith("Numpad") && code.length() == 7) {
            char numChar = code.charAt(6);
            if (numChar >= '0' && numChar <= '9') {
                return Canvas.KEY_NUM0 + (numChar - '0');
            }
        }

        // Star (*) and pound (#)
        if ("*".equals(key)) {
            return Canvas.KEY_STAR;
        }
        if ("#".equals(key)) {
            return Canvas.KEY_POUND;
        }

        // Softkey mappings: F1 = SOFTKEY1, F2 = SOFTKEY2, F3 = SOFTKEY3
        // These map to the Nokia FullCanvas KEY_SOFTKEY constants
        // which are -6, -7, -5 respectively
        if ("F1".equals(key)) {
            return -6;  // KEY_SOFTKEY1
        }
        if ("F2".equals(key)) {
            return -7;  // KEY_SOFTKEY2
        }
        if ("F3".equals(key)) {
            return -5;  // KEY_SOFTKEY3
        }

        // WASD mapping for alternative controls
        switch (key) {
            case "w":
            case "W":
                return Canvas.KEY_UP_ARROW;
            case "s":
            case "S":
                return Canvas.KEY_DOWN_ARROW;
            case "a":
            case "A":
                return Canvas.KEY_LEFT_ARROW;
            case "d":
            case "D":
                return Canvas.KEY_RIGHT_ARROW;
        }

        // Q -> SOFTKEY1 (left soft key / Pause)
        if ("q".equals(key) || "Q".equals(key)) {
            return -6;
        }
        // E -> SOFTKEY2 (right soft key)
        if ("e".equals(key) || "E".equals(key)) {
            return -7;
        }

        // Z -> GAME_A (missile)
        if ("z".equals(key) || "Z".equals(key)) {
            return Canvas.KEY_NUM6;
        }

        // X -> FIRE (bomb)
        if ("x".equals(key) || "X".equals(key)) {
            return Canvas.KEY_NUM5;
        }

        return 0;
    }

    /**
     * Get the main canvas element.
     */
    public static HTMLCanvasElement getMainCanvas() {
        return mainCanvas;
    }
}
