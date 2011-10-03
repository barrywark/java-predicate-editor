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
     * This is the RowData object that changed.
     */
    private RowData rowData;


    public RowDataEvent(RowData rowData) {
        this.rowData = rowData;
    }


    public RowData getRowData() {
        return(rowData);
    }


    public void setRowData(RowData rowData) {
        this.rowData = rowData;
    }
}
