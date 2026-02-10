package javax.microedition.rms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory implementation of the J2ME RecordStore API.
 * Data persists for the duration of the browser session only.
 * <p>
 * Record IDs start at 1 and increment monotonically. Deleted
 * record IDs are not reused.
 */
public class RecordStore {

    /** Map of store name to RecordStore instance. */
    private static final HashMap<String, RecordStore> stores = new HashMap<>();

    /** The name of this store. */
    private final String name;

    /** Records indexed by their ID. Null entries represent deleted records. */
    private final HashMap<Integer, byte[]> records = new HashMap<>();

    /** Next record ID to assign. */
    private int nextRecordId = 1;

    /** Whether this store is currently open. */
    private boolean open;

    private RecordStore(String name) {
        this.name = name;
        this.open = true;
    }

    /**
     * Opens (and possibly creates) a record store.
     *
     * @param recordStoreName the name of the record store
     * @param createIfNecessary if true, create the store if it does not exist
     * @return the open RecordStore
     * @throws RecordStoreNotFoundException if the store does not exist and
     *         createIfNecessary is false
     * @throws RecordStoreException if a general error occurs
     */
    public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary)
            throws RecordStoreException, RecordStoreNotFoundException {
        RecordStore store = stores.get(recordStoreName);
        if (store != null) {
            store.open = true;
            return store;
        }
        if (!createIfNecessary) {
            throw new RecordStoreNotFoundException("RecordStore not found: " + recordStoreName);
        }
        store = new RecordStore(recordStoreName);
        stores.put(recordStoreName, store);
        return store;
    }

    /**
     * Deletes the named record store.
     *
     * @param recordStoreName the name of the store to delete
     * @throws RecordStoreNotFoundException if the store does not exist
     * @throws RecordStoreException if a general error occurs
     */
    public static void deleteRecordStore(String recordStoreName)
            throws RecordStoreException, RecordStoreNotFoundException {
        if (!stores.containsKey(recordStoreName)) {
            throw new RecordStoreNotFoundException("RecordStore not found: " + recordStoreName);
        }
        stores.remove(recordStoreName);
    }

    /**
     * Closes this record store. Further operations on it will fail
     * until it is reopened.
     *
     * @throws RecordStoreException if an error occurs
     * @throws RecordStoreNotOpenException if the store is not open
     */
    public void closeRecordStore() throws RecordStoreException, RecordStoreNotOpenException {
        checkOpen();
        open = false;
    }

    /**
     * Adds a new record to the store.
     *
     * @param data   the data to store
     * @param offset the offset into the data array
     * @param numBytes the number of bytes to store
     * @return the record ID assigned to this record
     * @throws RecordStoreException if an error occurs
     * @throws RecordStoreNotOpenException if the store is not open
     */
    public int addRecord(byte[] data, int offset, int numBytes)
            throws RecordStoreException, RecordStoreNotOpenException, RecordStoreFullException {
        checkOpen();
        int id = nextRecordId++;
        byte[] copy = new byte[numBytes];
        System.arraycopy(data, offset, copy, 0, numBytes);
        records.put(id, copy);
        return id;
    }

    /**
     * Returns a copy of the data stored in the given record.
     *
     * @param recordId the ID of the record to retrieve
     * @return a new byte array containing the record data
     * @throws RecordStoreException if a general error occurs
     * @throws InvalidRecordIDException if the record ID is invalid
     * @throws RecordStoreNotOpenException if the store is not open
     */
    public byte[] getRecord(int recordId)
            throws RecordStoreException, InvalidRecordIDException, RecordStoreNotOpenException {
        checkOpen();
        byte[] data = records.get(recordId);
        if (data == null) {
            throw new InvalidRecordIDException("Invalid record ID: " + recordId);
        }
        byte[] copy = new byte[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    /**
     * Sets the data in the given record.
     *
     * @param recordId the ID of the record to update
     * @param newData  the new data
     * @param offset   the offset into the data array
     * @param numBytes the number of bytes to store
     * @throws RecordStoreException if a general error occurs
     * @throws InvalidRecordIDException if the record ID is invalid
     * @throws RecordStoreNotOpenException if the store is not open
     */
    public void setRecord(int recordId, byte[] newData, int offset, int numBytes)
            throws RecordStoreException, InvalidRecordIDException, RecordStoreNotOpenException, RecordStoreFullException {
        checkOpen();
        if (!records.containsKey(recordId)) {
            throw new InvalidRecordIDException("Invalid record ID: " + recordId);
        }
        byte[] copy = new byte[numBytes];
        System.arraycopy(newData, offset, copy, 0, numBytes);
        records.put(recordId, copy);
    }

    /**
     * Deletes the specified record from the store.
     *
     * @param recordId the ID of the record to delete
     * @throws RecordStoreException if a general error occurs
     * @throws InvalidRecordIDException if the record ID is invalid
     * @throws RecordStoreNotOpenException if the store is not open
     */
    public void deleteRecord(int recordId)
            throws RecordStoreException, InvalidRecordIDException, RecordStoreNotOpenException {
        checkOpen();
        if (!records.containsKey(recordId)) {
            throw new InvalidRecordIDException("Invalid record ID: " + recordId);
        }
        records.remove(recordId);
    }

    /**
     * Returns an enumeration for traversing the set of records in
     * the store.
     *
     * @param filter     optional filter, or null
     * @param comparator optional comparator for ordering, or null
     * @param keepUpdated ignored in this implementation
     * @return a RecordEnumeration for iterating through the records
     * @throws RecordStoreNotOpenException if the store is not open
     */
    public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated)
            throws RecordStoreNotOpenException {
        checkOpen();
        return new RecordEnumerationImpl(this, filter, comparator);
    }

    /**
     * Returns the number of records currently in the store.
     *
     * @return the number of records
     * @throws RecordStoreNotOpenException if the store is not open
     */
    public int getNumRecords() throws RecordStoreNotOpenException {
        checkOpen();
        return records.size();
    }

    /**
     * Returns the name of this record store.
     *
     * @return the store name
     */
    public String getName() {
        return name;
    }

    // ---- Package-private methods used by RecordEnumerationImpl ----

    /**
     * Returns a list of all valid record IDs in this store.
     * Package-private for use by RecordEnumerationImpl.
     */
    List<Integer> getAllRecordIds() {
        return new ArrayList<>(records.keySet());
    }

    /**
     * Returns the raw record data for the given ID, or null if not found.
     * Package-private for use by RecordEnumerationImpl.
     * Does not copy the data (the enumeration handles copying).
     */
    byte[] getRawRecord(int recordId) {
        return records.get(recordId);
    }

    private void checkOpen() throws RecordStoreNotOpenException {
        if (!open) {
            throw new RecordStoreNotOpenException("RecordStore '" + name + "' is not open");
        }
    }
}
