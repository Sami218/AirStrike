package javax.microedition.rms;

/**
 * An interface defining a comparator which compares two records
 * (in an implementation-defined manner) to determine their relative order.
 */
public interface RecordComparator {

    /** Indicates that the two records are equivalent with respect to sort order. */
    public static final int EQUIVALENT = 0;

    /** Indicates that record rec1 follows rec2 in sort order. */
    public static final int FOLLOWS = 1;

    /** Indicates that record rec1 precedes rec2 in sort order. */
    public static final int PRECEDES = -1;

    /**
     * Compares two records to determine sort ordering.
     *
     * @param rec1 the first record data
     * @param rec2 the second record data
     * @return PRECEDES, EQUIVALENT, or FOLLOWS
     */
    int compare(byte[] rec1, byte[] rec2);
}
