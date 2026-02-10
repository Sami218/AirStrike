package javax.microedition.lcdui;

import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;

public class Font {

    // Face constants
    public static final int FACE_SYSTEM = 0;
    public static final int FACE_MONOSPACE = 32;
    public static final int FACE_PROPORTIONAL = 64;

    // Style constants
    public static final int STYLE_PLAIN = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    public static final int STYLE_UNDERLINED = 4;

    // Size constants
    public static final int SIZE_SMALL = 8;
    public static final int SIZE_MEDIUM = 0;
    public static final int SIZE_LARGE = 16;

    private final int face;
    private final int style;
    private final int size;
    private final String cssString;
    private final int pixelHeight;

    // Shared offscreen canvas for text measurement
    private static CanvasRenderingContext2D measureCtx;

    private Font(int face, int style, int size) {
        this.face = face;
        this.style = style;
        this.size = size;

        // Determine pixel height from size constant
        if (size == SIZE_SMALL) {
            pixelHeight = 10;
        } else if (size == SIZE_LARGE) {
            pixelHeight = 16;
        } else {
            pixelHeight = 12; // SIZE_MEDIUM (0)
        }

        // Build CSS font string
        StringBuilder sb = new StringBuilder();
        if ((style & STYLE_ITALIC) != 0) {
            sb.append("italic ");
        }
        if ((style & STYLE_BOLD) != 0) {
            sb.append("bold ");
        }
        sb.append(pixelHeight);
        sb.append("px ");

        if (face == FACE_MONOSPACE) {
            sb.append("monospace");
        } else if (face == FACE_PROPORTIONAL) {
            sb.append("sans-serif");
        } else {
            sb.append("sans-serif");
        }

        cssString = sb.toString();
    }

    public static Font getFont(int face, int style, int size) {
        return new Font(face, style, size);
    }

    public static Font getDefaultFont() {
        return new Font(FACE_SYSTEM, STYLE_PLAIN, SIZE_MEDIUM);
    }

    public int getFace() {
        return face;
    }

    public int getStyle() {
        return style;
    }

    public int getSize() {
        return size;
    }

    public int getHeight() {
        return pixelHeight;
    }

    public int stringWidth(String str) {
        return charsWidth(str.toCharArray(), 0, str.length());
    }

    public int charsWidth(char[] ch, int offset, int length) {
        ensureMeasureContext();
        measureCtx.setFont(cssString);
        String text = new String(ch, offset, length);
        return (int) Math.ceil(measureCtx.measureText(text).getWidth());
    }

    public int charWidth(char ch) {
        return charsWidth(new char[]{ch}, 0, 1);
    }

    public int substringWidth(String str, int offset, int len) {
        return charsWidth(str.toCharArray(), offset, len);
    }

    public String toCssString() {
        return cssString;
    }

    public boolean isPlain() {
        return style == STYLE_PLAIN;
    }

    public boolean isBold() {
        return (style & STYLE_BOLD) != 0;
    }

    public boolean isItalic() {
        return (style & STYLE_ITALIC) != 0;
    }

    public boolean isUnderlined() {
        return (style & STYLE_UNDERLINED) != 0;
    }

    private static void ensureMeasureContext() {
        if (measureCtx == null) {
            HTMLDocument doc = Window.current().getDocument();
            HTMLCanvasElement canvas = (HTMLCanvasElement) doc.createElement("canvas");
            canvas.setWidth(1);
            canvas.setHeight(1);
            measureCtx = (CanvasRenderingContext2D) canvas.getContext("2d");
        }
    }
}
