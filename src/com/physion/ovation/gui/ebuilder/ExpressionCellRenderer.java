//package com.physion.ovation.gui.ebuilder;

import java.util.EventObject;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JButton;

//import com.physion.ovation.gui.ebuilder.datamodel.RowData;

class ExpressionCellRenderer
    extends JPanel
    implements TableCellRenderer, TableCellEditor, ActionListener {

    //private DefaultCellEditor;


    private JLabel label;
    private JButton deleteButton;

    private ExpressionTable table;


    public ExpressionCellRenderer() {

        GridBagConstraints gc;

        setBackground(Color.green);
        setOpaque(true);
        //defaultCellEditor = new DefaultCellEditor();

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        label = new JLabel();
        label.setOpaque(true);

        deleteButton = new JButton("-");
        deleteButton.addActionListener(this);

        gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.WEST;
        add(label, gc);
        
        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.gridy = 0;
        add(deleteButton, gc);
    }


    /**
     */
    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean cellHasFocus,
                                                   int row,
                                                   int column) {

        this.table = (ExpressionTable)table;

        label.setText(value.toString());

        Color background;
        Color foreground;

        // check if this cell represents the current DnD drop location
        JTable.DropLocation dropLocation = table.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsertRow()
                && dropLocation.getRow() == row) {

            background = Color.BLUE;
            foreground = Color.WHITE;

        // check if this cell is selected
        } else if (isSelected) {

            background = Color.RED;
            foreground = Color.WHITE;

        // unselected, and not the DnD drop location
        } else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        };
/*
        if (isSelected) {
            background = Color.RED;
            foreground = Color.WHITE;
        }
        else {
            background = Color.WHITE;
            foreground = Color.BLACK;
        }
*/
        label.setBackground(background);
        //setBackground(background);
        label.setForeground(foreground);

        //label.repaint();
        //repaint();
        //table.tableChanged(null);

        /**
         * The very first row cannot be deleted.
         */
        if (row == 0) {
            deleteButton.setVisible(false);
            deleteButton.setEnabled(false);
        }
        else {
            deleteButton.setVisible(true);
            deleteButton.setEnabled(true);
        }

        return this;
    }


    /**
     */
    @Override
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {

        System.out.println("Enter getTableCellEditorComponent()");

        //return(getTableCellRendererComponent(table, value, isSelected,
        return(getTableCellRendererComponent(table, value, true,
                                             true, row, column));
    }

    @Override
    public boolean isCellEditable(EventObject event) {
        return(true);
    }
    @Override
    public Object getCellEditorValue() {
        return(true);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener listener) {
    }
    @Override
    public void addCellEditorListener(CellEditorListener listener) {
    }
    @Override
    public void cancelCellEditing() {
    }
    @Override
    public boolean stopCellEditing() {
        return(true);
    }
    @Override
    public boolean shouldSelectCell(EventObject event) {
        return(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //System.out.println("Value being edited = "+valueBeingEdited);
        if (e.getSource() == deleteButton) {
            System.out.println("deleteButton pressed");
            
            table.deleteSelectedRow();

            //table.tableChanged(null);
        }
    }

}
