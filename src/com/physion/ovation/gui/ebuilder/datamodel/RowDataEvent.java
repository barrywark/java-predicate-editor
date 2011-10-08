package com.physion.ovation.gui.ebuilder.datamodel;


/**
 * This event is sent to RowDataListeners that are listening
 * to a RowData expression tree.
 *
 * Currently, October 2011, this event has no information about
 * what was changed.  At some point in the future, if the GUI
 * gets more complicated, we might want to have information
 * about what kind of change occurred.  E.g. row added/deleted,
 * attribute value changed, etc.
 */
public class RowDataEvent {

    /**
     * This event is being sent BEFORE the RowData object is
     * changed.  A GUI might want to listen for these types
     * of events if it wants to save the state of this RowData
     * object before it is changed.
     */
    public static final int BEFORE_CHANGE = 0;

    /**
     * This event is being sent AFTER the RowData object is
     * changed.  A GUI should probably listen for these types
     * of events and update itself to reflect this RowData's
     * new value.
     */
    public static final int AFTER_CHANGE = 1;

    /**
     * This is the RowData object that changed.
     */
    private RowData rowData;

    /**
     * This tells you what type of event you are
     * being sent.  For example:
     *
     *      RowDataEvent.BEFORE_CHANGE
     *      RowDataEvent.AFTER_CHANGE
     */
    private int eventType;


    public RowDataEvent(RowData rowData, int eventType) {
        this.rowData = rowData;
        this.eventType = eventType;
    }


    public RowData getRowData() {
        return(rowData);
    }


    public void setRowData(RowData rowData) {
        this.rowData = rowData;
    }


    public int getEventType() {
        return(eventType);
    }


    public void setEventType(int eventType) {
        this.eventType = eventType;
    }
}
