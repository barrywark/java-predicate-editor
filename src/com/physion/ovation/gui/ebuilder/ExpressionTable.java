package com.physion.ovation.gui.ebuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
//import org.jdesktop.swingx.JXTable;
import javax.swing.JLabel;
import javax.swing.JButton;

import com.physion.ovation.gui.ebuilder.datamodel.EBuilderTableModel;

class ExpressionTable
    //extends JXTable {
    extends JTable {

    ExpressionCellRenderer expressionCellRenderer;
    ExpressionCellRenderer expressionCellEditor;

    public ExpressionTable(TableModel model) {
        super(model);

        setTableHeader(null);

        /**
         * Turn off auto resizing or the JTable will resize itself to
         * the available space in the scrollpane, making the horizontal
         * scrollbar useless.
         */
        //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        /**
         * Try out JXTable.
         */
        //setHorizontalScrollEnabled(true);  // JXTable

        expressionCellRenderer = new ExpressionCellRenderer();
        expressionCellEditor = new ExpressionCellRenderer();
    }


    /*
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return(false);
    }
    */

    /*
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        
        Dimension size = super.getPreferredScrollableViewportSize();
        return(new Dimension(Math.min(getPreferredSize().width, size.width),
               size.height));
    }
    */


    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {

        return(expressionCellRenderer);
    }


    @Override
    public TableCellEditor getCellEditor(int row, int column) {

        System.out.println("Enter getCellEditor()");

        return(expressionCellEditor);
    }


    @Override
    public int getRowHeight(int row) {
        return(getRowHeight());
    }


    @Override
    public int getRowHeight() {

        Component component = getCellRenderer(0,0).getTableCellRendererComponent(this, null, true, true, 0, 0);
        return(component.getPreferredSize().height);
    }


    /*
    @Override
    public boolean isCellEditable(int row, int column) {
    
        boolean editable = super.isCellEditable(row, column);
        System.out.println("isCellEditable = "+editable);
        //editable = true;
        return(editable);
    }
    */


    public void deleteSelectedRow() {

        int row = getSelectedRow();

        EBuilderTableModel model = (EBuilderTableModel)getModel();
        System.out.println("Calling model.deleteRow("+row+")");
        model.deleteRow(row);
        tableChanged(null);  // Should this be in the table or the model?
    }


    public void createCompoundRow() {

        int row = getSelectedRow();

        EBuilderTableModel model = (EBuilderTableModel)getModel();
        System.out.println("Calling model.createCompoundRow("+row+")");
        model.createCompoundRow(row);
        tableChanged(null);  // Should this be in the table or the model?
    }


    public void createAttributeRow() {

        int row = getSelectedRow();

        EBuilderTableModel model = (EBuilderTableModel)getModel();
        System.out.println("Calling model.createAttributeRow("+row+")");
        model.createAttributeRow(row);
        tableChanged(null);  // Should this be in the table or the model?
    }


    public void printModel() {
        
        EBuilderTableModel model = (EBuilderTableModel)getModel();
        System.out.println("\nCurrent Value:\n"+model.toStringDebug());
    }
}
