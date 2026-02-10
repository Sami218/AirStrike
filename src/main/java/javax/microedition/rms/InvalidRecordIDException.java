package javax.microedition.rms;

/**
 * Thrown when an operation is attempted with an invalid record ID.
 */
public class InvalidRecordIDException extends RecordStoreException {

    public InvalidRecordIDException() {
        super();
    }

    public InvalidRecordIDException(String message) {
        super(message);
    }
}
