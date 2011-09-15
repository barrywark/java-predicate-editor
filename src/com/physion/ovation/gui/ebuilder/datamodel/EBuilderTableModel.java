package com.physion.ovation.gui.ebuilder.datamodel;

import javax.swing.table.AbstractTableModel;
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
public class EBuilderTableModel 
    extends AbstractTableModel {

    private RowData rootRow;

    public EBuilderTableModel() {

        rootRow = RowData.createTestRowData();
    }


    @Override
    public int getRowCount() {
        return(rootRow.getNumDescendents()+1);
    }


    /**
     */
    @Override 
    public Object getValueAt(int row, int column) {
        return(getElementAt(row));
    }


    /**
     * We are really a list, so we only have one column.
     */
    @Override 
    public int getColumnCount() {
        return(1);
    }


    public Object getElementAt(int index) {
    
        //RowData rowData = getRowDataForIndex(index);
        RowData rowData = rootRow.getChild(index);

        if (rowData == null) {
            String string = "ERROR:  In EBuilderListModel:getElementAt("+
                index+")";
            string += "EBuilderListModel:getRowDataForIndex("+index+") ";
            string += "returned null.  This should never happen.";
            return(string);
        }

        //return(rowData.getIndentString()+rowData.getRowString());
        return(rowData);
    }


    /*
    private RowData getRowDataAt(int index) {
        return(rootRow.getChild(index));
    }
    */


    @Override 
    public boolean isCellEditable(int row, int column) {
        return(true);
    }


    public void deleteRow(int index) {

        //RowData rowData = getRowDataAt(index);
        RowData rowData = rootRow.getChild(index);
        System.out.println("Calling rowData.removeFromParent(): "+rowData);
        rowData.removeFromParent();

        //fireTableDataChanged();  // Data AND structure changed, so no good.

        //fireTableChanged(null);  // Should this be in the model or the table?
    }


    public void createCompoundRow(int index) {

        RowData rowData = rootRow.getChild(index);
        System.out.println("Calling rowData.createCompoundRow(): "+rowData);
        rowData.createCompoundRow();
    }
    

    public void createAttributeRow(int index) {

        RowData rowData = rootRow.getChild(index);
        System.out.println("Calling rowData.createAttributeRow(): "+rowData);
        rowData.createAttributeRow();
    }
}
