package com.physion.ovation.gui.ebuilder.datamodel;

import java.util.EventListener;


/**
 * Classes that want to be notified when a RowData expression
 * tree changes should implement this method and add themselves
 * as listeners to the expression tree's root RowData object.
 */
public interface RowDataListener
    extends EventListener {

    /**
     * This method is called when the RowData expression
     * tree changes.  Look in the rowDataEvent to get which
     * row was actually changed.
     */
    public void rowDataChanged(RowDataEvent rowDataEvent);
}
