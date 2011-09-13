//package com.physion.ovation.gui.ebuilder;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JButton;

import com.physion.ovation.gui.ebuilder.datamodel.EBuilderTableModel;

class ExpressionTable
    extends JTable {

    ExpressionCellRenderer expressionCellRenderer;
    ExpressionCellRenderer expressionCellEditor;

    public ExpressionTable(TableModel model) {
        super(model);

        setTableHeader(null);

        expressionCellRenderer = new ExpressionCellRenderer();
        expressionCellEditor = new ExpressionCellRenderer();
    }


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
}
