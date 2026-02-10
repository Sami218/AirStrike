package javax.microedition.rms;

/**
 * Base exception class for RecordStore related errors.
 */
public class RecordStoreException extends Exception {

    public RecordStoreException() {
        super();
    }

    public RecordStoreException(String message) {
        super(message);
    }
}
