package com.physion.ovation.gui.ebuilder.datamodel;


/**
 * This event is sent to RowDataListeners that are listening
 * to a RowData expression tree.
 */
public class RowDataEvent {

    /**
     * This event is being sent BEFORE the RowData object is
     * changed.  A GUI might want to listen for these types
     * of events if it wants to save the state of this RowData
     * object before it is changed.
     */
    public static final int TIMING_BEFORE = 0;

    /**
     * This event is being sent AFTER the RowData object is
     * changed.  A GUI should probably listen for these types
     * of events and update itself to reflect this RowData's
     * new value.
     */
    public static final int TIMING_AFTER = 1;

    public static final int TYPE_UNDEFINED = 0; // Undefined change(s).
    public static final int TYPE_CHILD_ADD = 1;
    public static final int TYPE_CHILD_DELETE = 2;

    /**
     * The attributePath is being changed via a call
     * to addAttribute() or trimAttributePath().
     */
    public static final int TYPE_ATTRIBUTE_PATH = 3;

    public static final int TYPE_ATTRIBUTE_VALUE = 4;
    public static final int TYPE_PROP_NAME = 5;
    public static final int TYPE_PROP_TYPE = 6;
    public static final int TYPE_CUQ = 7; // Class Under Qualification
    public static final int TYPE_PARENT = 8; // Row's parent row is being set.
    public static final int TYPE_COLLECTION_OPERATOR = 9;
    public static final int TYPE_ATTRIBUTE_OPERATOR = 10;

    /**
     * A single attribute value being set via the
     * setAttribute() method.  Please note, this might
     * cause the number of attributes in the attributePath
     * to change.
     */
    public static final int TYPE_ATTRIBUTE = 11;

    /**
     * This is the RowData object that changed.
     */
    private RowData rowData;

    /**
     * This tells you "when" the event is being sent.
     * For example:
     *
     *      RowDataEvent.TIMING_BEFORE
     *      RowDataEvent.TIMING_AFTER
     */
    private int timing;

    /**
     * This tells you what type of change will occur or has
     * occured.  For example:
     *
     *      RowDataEvent.TYPE_CHILD_ADD
     *      RowDataEvent.TYPE_CHILD_DELETE
     *      RowDataEvent.TYPE_ATTRIBUTE_PATH
     *      RowDataEvent.TYPE_ATTRIBUTE_VALUE
     *      RowDataEvent.TYPE_PROP_NAME
     */
    private int changeType;


    public RowDataEvent(RowData rowData, int timing, int changeType) {
        this.rowData = rowData;
        this.timing = timing;
        this.changeType = changeType;
    }


    public RowData getRowData() {
        return(rowData);
    }


    public void setRowData(RowData rowData) {
        this.rowData = rowData;
    }


    /**
     * Get the timing of this event.
     *
     * @return Returns Either RowDataEvent.TIMING_BEFORE or
     * RowDataEvent.TIMING_AFTER.
     */
    public int getTiming() {
        return(timing);
    }


    /**
     * Set the timing of this event.
     *
     * @param timing Either RowDataEvent.TIMING_BEFORE or
     * RowDataEvent.TIMING_AFTER.
     */
    public void setTiming(int timing) {
        this.timing = timing;
    }


    /**
     * Returns the changeType of this event.
     * For example: RowDataEvent.TYPE_CHILD_ADD
     */
    public int getChangeType() {
        return(changeType);
    }


    /**
     * Set the changeType of this event.
     *
     * @param changeType A RowDataEvent.TYPE_* value.
     */
    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }
}
