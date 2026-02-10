package javax.microedition.rms;

/**
 * Thrown when an operation is attempted on a RecordStore that
 * does not exist.
 */
public class RecordStoreNotFoundException extends RecordStoreException {

    public RecordStoreNotFoundException() {
        super();
    }

    public RecordStoreNotFoundException(String message) {
        super(message);
    }
}
