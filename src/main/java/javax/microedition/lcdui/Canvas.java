package javax.microedition.lcdui;

/**
 * J2ME LCDUI Canvas compatibility class.
 * Provides the base class for game screens with key handling and painting.
 * Screen size is fixed at 128x128 (Nokia 7210 native resolution).
 */
public abstract class Canvas extends Displayable {

    // Game action constants
    public static final int UP = 1;
    public static final int DOWN = 6;
    public static final int LEFT = 2;
    public static final int RIGHT = 5;
    public static final int FIRE = 8;
    public static final int GAME_A = 9;
    public static final int GAME_B = 10;
    public static final int GAME_C = 11;
    public static final int GAME_D = 12;

    // Key code constants
    public static final int KEY_NUM0 = 48;
    public static final int KEY_NUM1 = 49;
    public static final int KEY_NUM2 = 50;
    public static final int KEY_NUM3 = 51;
    public static final int KEY_NUM4 = 52;
    public static final int KEY_NUM5 = 53;
    public static final int KEY_NUM6 = 54;
    public static final int KEY_NUM7 = 55;
    public static final int KEY_NUM8 = 56;
    public static final int KEY_NUM9 = 57;
    public static final int KEY_STAR = 42;
    public static final int KEY_POUND = 35;

    // Arrow key codes (browser-mapped values)
    public static final int KEY_UP_ARROW = -1;
    public static final int KEY_DOWN_ARROW = -2;
    public static final int KEY_LEFT_ARROW = -3;
    public static final int KEY_RIGHT_ARROW = -4;

    // Repaint flag
    private boolean needsRepaint = false;

    public Canvas() {
    }

    /**
     * Abstract paint method to be implemented by game screens.
     */
    public abstract void paint(Graphics g);

    /**
     * Request a repaint. The Display rendering loop will call paint().
     */
    public void repaint() {
        needsRepaint = true;
    }

    /**
     * Force an immediate repaint (service any pending repaint request).
     * In the browser environment, this triggers paint directly.
     */
    public void serviceRepaints() {
        // In browser context, the Display handles rendering.
        // Mark and let the next frame pick it up.
        needsRepaint = true;
    }

    /**
     * Check if a repaint has been requested.
     */
    public boolean isRepaintNeeded() {
        return needsRepaint;
    }

    /**
     * Clear the repaint flag after painting.
     */
    public void clearRepaintFlag() {
        needsRepaint = false;
    }

    @Override
    public int getWidth() {
        return 128;
    }

    @Override
    public int getHeight() {
        return 128;
    }

    /**
     * Convert a browser key code to a J2ME game action.
     * Browser arrow keys and common mappings are handled here.
     * Subclasses (FullScreenInfo) may override for custom mappings.
     */
    public int getGameAction(int keyCode) {
        switch (keyCode) {
            case KEY_UP_ARROW:
                return UP;
            case KEY_DOWN_ARROW:
                return DOWN;
            case KEY_LEFT_ARROW:
                return LEFT;
            case KEY_RIGHT_ARROW:
                return RIGHT;
            case KEY_NUM5:
                return FIRE;
            case KEY_NUM2:
                return UP;
            case KEY_NUM8:
                return DOWN;
            case KEY_NUM4:
                return LEFT;
            case KEY_NUM6:
                return RIGHT;
            default:
                return 0;
        }
    }

    /**
     * Called when a key is pressed. Override in subclass.
     */
    public void keyPressed(int keyCode) {
    }

    /**
     * Called when a key is released. Override in subclass.
     */
    public void keyReleased(int keyCode) {
    }

    /**
     * Called when a key is repeated (held down). Override in subclass.
     */
    public void keyRepeated(int keyCode) {
    }

    /**
     * Called when the canvas is hidden. Override in subclass.
     */
    protected void hideNotify() {
    }

    /**
     * Called when the canvas is shown. Override in subclass.
     */
    protected void showNotify() {
    }

    /**
     * Check if the canvas has pointer events.
     */
    public boolean hasPointerEvents() {
        return false;
    }

    /**
     * Check if the canvas has pointer motion events.
     */
    public boolean hasPointerMotionEvents() {
        return false;
    }

    /**
     * Check if the canvas has repeat events.
     */
    public boolean hasRepeatEvents() {
        return true;
    }

    /**
     * Toggles full-screen mode.
     * In the browser port, the canvas is always full-screen,
     * so this is a no-op.
     *
     * @param fullScreen true for full screen, false otherwise
     */
    public void setFullScreenMode(boolean fullScreen) {
        // No-op in browser environment - always full screen
    }
}
