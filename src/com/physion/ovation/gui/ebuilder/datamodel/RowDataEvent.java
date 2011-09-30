package com.physion.ovation.gui.ebuilder.datamodel;

//import com.physion.ovation.gui.ebuilder.datamodel.RowData;


/**
 */
public class RowDataEvent {

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
