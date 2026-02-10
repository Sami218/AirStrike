package javax.microedition.rms;

/**
 * An interface defining a filter which examines a record to see if it
 * matches (based on an application-defined criteria).
 */
public interface RecordFilter {

    /**
     * Returns true if the candidate record matches the implemented criterion.
     *
     * @param candidate the record data to check
     * @return true if the record is selected by this filter
     */
    boolean matches(byte[] candidate);
}
