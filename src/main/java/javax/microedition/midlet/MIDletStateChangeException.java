package javax.microedition.midlet;

/**
 * Signals that a requested MIDlet state change failed.
 */
public class MIDletStateChangeException extends Exception {

    public MIDletStateChangeException() {
        super();
    }

    public MIDletStateChangeException(String message) {
        super(message);
    }
}
