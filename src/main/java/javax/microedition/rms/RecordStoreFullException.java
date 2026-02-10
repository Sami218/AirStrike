package javax.microedition.rms;

/**
 * Thrown when an operation cannot be completed because the
 * RecordStore is full.
 */
public class RecordStoreFullException extends RecordStoreException {

    public RecordStoreFullException() {
        super();
    }

    public RecordStoreFullException(String message) {
        super(message);
    }
}
