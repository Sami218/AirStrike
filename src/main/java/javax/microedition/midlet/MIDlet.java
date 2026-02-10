package javax.microedition.midlet;

/**
 * Abstract base class for MIDlet applications.
 * In the TeaVM browser port this serves as a stub so the original
 * game code that extends MIDlet can compile unchanged.
 */
public abstract class MIDlet {

    protected MIDlet() {
    }

    /**
     * Signals the MIDlet that it has entered the Active state.
     */
    protected abstract void startApp() throws MIDletStateChangeException;

    /**
     * Signals the MIDlet to enter the Paused state.
     */
    protected abstract void pauseApp();

    /**
     * Signals the MIDlet to terminate and enter the Destroyed state.
     *
     * @param unconditional if true, the MIDlet must clean up and release
     *                      all resources; if false, the MIDlet may throw
     *                      MIDletStateChangeException to indicate it does
     *                      not want to be destroyed at this time.
     */
    protected abstract void destroyApp(boolean unconditional) throws MIDletStateChangeException;

    /**
     * Notifies the application management software that the MIDlet has
     * entered the Destroyed state. In a browser context this is a no-op
     * because we cannot close a browser tab programmatically.
     */
    public void notifyDestroyed() {
        // No-op in browser environment
    }

    /**
     * Notifies the application management software that the MIDlet does
     * not want to be active and has entered the Paused state.
     * No-op in the browser port.
     */
    public void notifyPaused() {
        // No-op in browser environment
    }

    /**
     * Returns an application property from the application descriptor
     * (JAD) or manifest. Returns null in the browser port.
     */
    public String getAppProperty(String key) {
        return null;
    }
}
