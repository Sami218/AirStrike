package javax.microedition.rms;

/**
 * An interface representing a bidirectional record enumeration
 * for iterating through a set of records in a RecordStore.
 */
public interface RecordEnumeration {

    /**
     * Returns true if there are more elements to iterate in the
     * forward direction.
     *
     * @return true if there is a next element
     */
    boolean hasNextElement();

    /**
     * Returns the record ID of the next record in this enumeration,
     * advancing the position.
     *
     * @return the next record ID
     * @throws InvalidRecordIDException if no more records
     */
    int nextRecordId() throws InvalidRecordIDException;

    /**
     * Returns a copy of the next record data in this enumeration,
     * advancing the position.
     *
     * @return a copy of the record data
     * @throws InvalidRecordIDException if no more records
     * @throws RecordStoreException if a general error occurs
     * @throws RecordStoreNotOpenException if the store has been closed
     */
    byte[] nextRecord() throws InvalidRecordIDException, RecordStoreException, RecordStoreNotOpenException;

    /**
     * Returns the number of records available in this enumeration.
     *
     * @return the number of records
     */
    int numRecords();

    /**
     * Frees internal resources used by this enumeration.
     */
    void destroy();
}
