package com.physion.ovation.gui.ebuilder.datamodel;

import javax.swing.AbstractListModel;
/*
import java.util.ArrayList;

import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.Cardinality;
*/


/**
 * 
 */
public class EBuilderListModel 
    extends AbstractListModel {

    private RowData rootRow;

    public EBuilderListModel() {

        rootRow = RowData.createTestRowData();
    }


    public int getSize() {
        return(rootRow.getNumDescendents()+1);
    }


    public Object getElementAt(int index) {
    
        //RowData rowData = getRowDataForIndex(index);
        RowData rowData = rootRow.getDescendentAt(index);

        if (rowData == null) {
            String string = "ERROR:  In EBuilderListModel:getElementAt("+
                index+")";
            string += "EBuilderListModel:getRowDataForIndex("+index+") ";
            string += "returned null.  This should never happen.";
            return(string);
        }

        return(rowData.getIndentString()+rowData.getRowString());
    }


    private RowData getRowDataForIndex(int index) {

        if (index == 0)
            return(rootRow);

        RowData rowData = rootRow;

        return(rowData);
    }
}
