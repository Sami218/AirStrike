package javax.microedition.rms;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of RecordEnumeration that iterates over
 * a snapshot of record IDs, optionally filtered and sorted.
 */
class RecordEnumerationImpl implements RecordEnumeration {

    private final RecordStore store;
    private final List<Integer> recordIds;
    private int cursor;

    /**
     * Creates an enumeration over the records in the given store.
     *
     * @param store      the RecordStore to enumerate
     * @param filter     optional filter (may be null)
     * @param comparator optional comparator for sort order (may be null)
     */
    RecordEnumerationImpl(RecordStore store, RecordFilter filter, RecordComparator comparator) {
        this.store = store;
        this.recordIds = new ArrayList<>();
        this.cursor = 0;

        // Build the list of matching record IDs
        List<Integer> allIds = store.getAllRecordIds();
        for (int id : allIds) {
            byte[] data = store.getRawRecord(id);
            if (data != null) {
                if (filter == null || filter.matches(data)) {
                    recordIds.add(id);
                }
            }
        }

        // Sort if a comparator is provided
        if (comparator != null) {
            sortRecords(comparator);
        }
    }

    private void sortRecords(RecordComparator comparator) {
        // Simple insertion sort; record counts are typically small in J2ME apps
        for (int i = 1; i < recordIds.size(); i++) {
            int keyId = recordIds.get(i);
            byte[] keyData = store.getRawRecord(keyId);
            int j = i - 1;
            while (j >= 0) {
                byte[] jData = store.getRawRecord(recordIds.get(j));
                if (comparator.compare(jData, keyData) == RecordComparator.FOLLOWS) {
                    recordIds.set(j + 1, recordIds.get(j));
                    j--;
                } else {
                    break;
                }
            }
            recordIds.set(j + 1, keyId);
        }
    }

    @Override
    public boolean hasNextElement() {
        return cursor < recordIds.size();
    }

    @Override
    public int nextRecordId() throws InvalidRecordIDException {
        if (cursor >= recordIds.size()) {
            throw new InvalidRecordIDException("No more records in enumeration");
        }
        return recordIds.get(cursor++);
    }

    @Override
    public byte[] nextRecord() throws InvalidRecordIDException, RecordStoreException, RecordStoreNotOpenException {
        if (cursor >= recordIds.size()) {
            throw new InvalidRecordIDException("No more records in enumeration");
        }
        int id = recordIds.get(cursor++);
        byte[] data = store.getRawRecord(id);
        if (data == null) {
            throw new InvalidRecordIDException("Record " + id + " not found");
        }
        // Return a copy to match J2ME semantics
        byte[] copy = new byte[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    @Override
    public int numRecords() {
        return recordIds.size();
    }

    @Override
    public void destroy() {
        recordIds.clear();
        cursor = 0;
    }
}
