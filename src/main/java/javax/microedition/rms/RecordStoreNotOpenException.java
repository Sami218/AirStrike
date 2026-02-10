package javax.microedition.rms;

/**
 * Thrown when an operation is attempted on a RecordStore that
 * is not open.
 */
public class RecordStoreNotOpenException extends RecordStoreException {

    public RecordStoreNotOpenException() {
        super();
    }

    public RecordStoreNotOpenException(String message) {
        super(message);
    }
}
