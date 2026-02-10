package javax.microedition.lcdui;

public class Displayable {

    private CommandListener commandListener;

    public void setCommandListener(CommandListener listener) {
        this.commandListener = listener;
    }

    public CommandListener getCommandListener() {
        return commandListener;
    }

    /**
     * Get the width of the displayable area.
     * Subclasses (Canvas) override this.
     */
    public int getWidth() {
        return 128;
    }

    /**
     * Get the height of the displayable area.
     * Subclasses (Canvas) override this.
     */
    public int getHeight() {
        return 128;
    }
}
