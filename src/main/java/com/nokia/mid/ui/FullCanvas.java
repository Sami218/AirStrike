package com.nokia.mid.ui;

import javax.microedition.lcdui.Canvas;

/**
 * Nokia-specific full-screen canvas extension.
 * On Nokia phones this provided a full-screen drawing surface
 * without the normal soft-key chrome. In the browser port the
 * entire canvas is always "full screen" so this simply extends
 * Canvas and adds the Nokia-specific key constants.
 */
public abstract class FullCanvas extends Canvas {

    // Nokia soft-key codes
    public static final int KEY_SOFTKEY1 = -6;
    public static final int KEY_SOFTKEY2 = -7;
    public static final int KEY_SOFTKEY3 = -5;

    // Nokia arrow key codes (same values as standard Canvas game actions)
    public static final int KEY_UP_ARROW = -1;
    public static final int KEY_DOWN_ARROW = -2;
    public static final int KEY_LEFT_ARROW = -3;
    public static final int KEY_RIGHT_ARROW = -4;

    // Nokia send/end key
    public static final int KEY_SEND = -10;

    // Game action aliases (matching Canvas.GAME_A and GAME_B)
    public static final int GAME_A = 9;
    public static final int GAME_B = 10;

    protected FullCanvas() {
        super();
        // Request full-screen mode (no-op in browser, but matches API)
        setFullScreenMode(true);
    }
}
