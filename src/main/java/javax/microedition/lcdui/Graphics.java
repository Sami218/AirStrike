package javax.microedition.lcdui;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;

public class Graphics {

    // Anchor constants
    public static final int TOP = 1;
    public static final int BOTTOM = 32;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    public static final int HCENTER = 2;
    public static final int VCENTER = 16;
    public static final int BASELINE = 64;

    private CanvasRenderingContext2D ctx;
    private int canvasWidth;
    private int canvasHeight;

    // Current state
    private int currentColor = 0x000000;
    private Font currentFont = Font.getDefaultFont();
    private int clipX;
    private int clipY;
    private int clipWidth;
    private int clipHeight;

    public Graphics(CanvasRenderingContext2D ctx, int width, int height) {
        this.ctx = ctx;
        this.canvasWidth = width;
        this.canvasHeight = height;
        this.clipX = 0;
        this.clipY = 0;
        this.clipWidth = width;
        this.clipHeight = height;

        // Set default font
        ctx.setFont(currentFont.toCssString());
    }

    // --- Color methods ---

    public void setColor(int rgb) {
        currentColor = rgb & 0xFFFFFF;
        String hex = "#" + padHex(currentColor);
        ctx.setFillStyle(hex);
        ctx.setStrokeStyle(hex);
    }

    public void setColor(int r, int g, int b) {
        currentColor = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        String hex = "#" + padHex(currentColor);
        ctx.setFillStyle(hex);
        ctx.setStrokeStyle(hex);
    }

    public int getColor() {
        return currentColor;
    }

    public int getRedComponent() {
        return (currentColor >> 16) & 0xFF;
    }

    public int getGreenComponent() {
        return (currentColor >> 8) & 0xFF;
    }

    public int getBlueComponent() {
        return currentColor & 0xFF;
    }

    // --- Font methods ---

    public void setFont(Font font) {
        if (font == null) {
            font = Font.getDefaultFont();
        }
        this.currentFont = font;
        ctx.setFont(font.toCssString());
    }

    public Font getFont() {
        return currentFont;
    }

    // --- Clipping methods ---

    public void setClip(int x, int y, int w, int h) {
        clipX = x;
        clipY = y;
        clipWidth = w;
        clipHeight = h;
        // Note: Canvas clip is cumulative and cannot be easily undone.
        // For a simple implementation, we track it but rely on save/restore
        // if needed. Most J2ME games use clip sparingly.
    }

    public int getClipX() {
        return clipX;
    }

    public int getClipY() {
        return clipY;
    }

    public int getClipWidth() {
        return clipWidth;
    }

    public int getClipHeight() {
        return clipHeight;
    }

    public void clipRect(int x, int y, int w, int h) {
        // Intersect with current clip
        int newX = Math.max(clipX, x);
        int newY = Math.max(clipY, y);
        int newRight = Math.min(clipX + clipWidth, x + w);
        int newBottom = Math.min(clipY + clipHeight, y + h);
        clipX = newX;
        clipY = newY;
        clipWidth = Math.max(0, newRight - newX);
        clipHeight = Math.max(0, newBottom - newY);
    }

    // --- Drawing methods ---

    public void fillRect(int x, int y, int width, int height) {
        ctx.fillRect(x, y, width, height);
    }

    public void drawRect(int x, int y, int width, int height) {
        ctx.strokeRect(x + 0.5, y + 0.5, width, height);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        ctx.beginPath();
        ctx.moveTo(x1 + 0.5, y1 + 0.5);
        ctx.lineTo(x2 + 0.5, y2 + 0.5);
        ctx.stroke();
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        ctx.save();
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;
        ctx.translate(cx, cy);
        ctx.scale(1, ry / rx);
        ctx.beginPath();
        double startRad = -startAngle * Math.PI / 180.0;
        double endRad = -(startAngle + arcAngle) * Math.PI / 180.0;
        ctx.arc(0, 0, rx, startRad, endRad, arcAngle > 0);
        ctx.restore();
        ctx.stroke();
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        ctx.save();
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;
        ctx.translate(cx, cy);
        ctx.scale(1, ry / rx);
        ctx.beginPath();
        double startRad = -startAngle * Math.PI / 180.0;
        double endRad = -(startAngle + arcAngle) * Math.PI / 180.0;
        ctx.arc(0, 0, rx, startRad, endRad, arcAngle > 0);
        ctx.lineTo(0, 0);
        ctx.closePath();
        ctx.restore();
        ctx.fill();
    }

    // --- Image drawing ---

    public void drawImage(Image img, int x, int y, int anchor) {
        if (img == null || img.getCanvas() == null) {
            return;
        }

        int imgW = img.getWidth();
        int imgH = img.getHeight();

        // Calculate actual draw position based on anchor flags
        int drawX = x;
        int drawY = y;

        // Horizontal anchor
        if ((anchor & HCENTER) != 0) {
            drawX = x - imgW / 2;
        } else if ((anchor & RIGHT) != 0) {
            drawX = x - imgW;
        }
        // else LEFT (default): drawX = x

        // Vertical anchor
        if ((anchor & VCENTER) != 0) {
            drawY = y - imgH / 2;
        } else if ((anchor & BOTTOM) != 0) {
            drawY = y - imgH;
        }
        // else TOP (default): drawY = y

        ctx.drawImage(img.getCanvas(), drawX, drawY);
    }

    // --- String drawing ---

    public void drawString(String str, int x, int y, int anchor) {
        if (str == null) {
            return;
        }

        // Set text alignment based on horizontal anchor
        if ((anchor & HCENTER) != 0) {
            ctx.setTextAlign("center");
        } else if ((anchor & RIGHT) != 0) {
            ctx.setTextAlign("right");
        } else {
            ctx.setTextAlign("left");
        }

        // Set text baseline based on vertical anchor
        if ((anchor & BASELINE) != 0) {
            ctx.setTextBaseline("alphabetic");
        } else if ((anchor & BOTTOM) != 0) {
            ctx.setTextBaseline("bottom");
        } else if ((anchor & VCENTER) != 0) {
            ctx.setTextBaseline("middle");
        } else {
            ctx.setTextBaseline("top");
        }

        ctx.fillText(str, x, y);
    }

    public void drawSubstring(String str, int offset, int len, int x, int y, int anchor) {
        drawString(str.substring(offset, offset + len), x, y, anchor);
    }

    public void drawChar(char ch, int x, int y, int anchor) {
        drawString(String.valueOf(ch), x, y, anchor);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y, int anchor) {
        drawString(new String(data, offset, length), x, y, anchor);
    }

    // --- Translation ---

    public void translate(int x, int y) {
        ctx.translate(x, y);
    }

    public int getTranslateX() {
        return 0; // Simplified; tracking translate would require extra state
    }

    public int getTranslateY() {
        return 0;
    }

    // --- Canvas context transform methods (used by DirectGraphicsImpl) ---

    /**
     * Save the current canvas context state (transforms, clip, etc.).
     */
    public void save() {
        ctx.save();
    }

    /**
     * Restore a previously saved canvas context state.
     */
    public void restore() {
        ctx.restore();
    }

    /**
     * Scale the canvas context.
     */
    public void scale(double sx, double sy) {
        ctx.scale(sx, sy);
    }

    /**
     * Rotate the canvas context by the given angle in radians.
     */
    public void rotate(double angle) {
        ctx.rotate(angle);
    }

    /**
     * Translate the canvas context by integer amounts.
     */
    public void translate(double x, double y) {
        ctx.translate(x, y);
    }

    /**
     * Draw an image at the specified position without anchor processing.
     * Used internally by DirectGraphicsImpl after it has already computed
     * the correct position and applied transforms.
     */
    public void drawImageRaw(Image img, int x, int y) {
        if (img != null && img.getCanvas() != null) {
            ctx.drawImage(img.getCanvas(), x, y);
        }
    }

    // --- Utility ---

    /**
     * Get the underlying canvas rendering context.
     * Used internally for advanced operations.
     */
    public CanvasRenderingContext2D getContext() {
        return ctx;
    }

    private static String padHex(int value) {
        String hex = Integer.toHexString(value);
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return hex;
    }
}
