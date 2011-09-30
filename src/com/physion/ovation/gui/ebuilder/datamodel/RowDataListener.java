package com.physion.ovation.gui.ebuilder.datamodel;

import java.util.EventListener;


/**
 */
public interface RowDataListener extends EventListener {

    public void changed(RowDataEvent rowDataEvent);
}
