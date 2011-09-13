//package com.physion.ovation.gui.ebuilder;

import java.util.EventObject;
import java.util.ArrayList;
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
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.DataModel;
import com.physion.ovation.gui.ebuilder.datamodel.CollectionOperator;
import com.physion.ovation.gui.ebuilder.datatypes.Attribute;
import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;

class ExpressionCellRenderer
    extends JPanel
    implements TableCellRenderer, TableCellEditor, ActionListener {

    private static final int MAX_NUM_COMBOBOXES = 20;

    //private DefaultCellEditor;


    private JLabel label;
    private JButton deleteButton;
    private JButton createCompoundRowButton;
    private JButton createAttributeRowButton;
    private JComboBox[] comboBoxes = new JComboBox[MAX_NUM_COMBOBOXES];

    private ExpressionTable table;


    /**
     * Create whatever components this renderer will need.
     * Other methods add or remove the components depending on
     * what RowData this row is displaying/editing.
     */
    public ExpressionCellRenderer() {

        setBackground(Color.green);
        setOpaque(true);
        //defaultCellEditor = new DefaultCellEditor();

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        label = new JLabel();
        label.setOpaque(true);

        deleteButton = new JButton("-");
        deleteButton.addActionListener(this);

        createCompoundRowButton = new JButton("++");
        createCompoundRowButton.addActionListener(this);

        createAttributeRowButton = new JButton("+");
        createAttributeRowButton.addActionListener(this);

        for (int count = 0; count < MAX_NUM_COMBOBOXES; count++) {
            comboBoxes[count] = new JComboBox();
            comboBoxes[count].setEditable(false);
        }
    }


    /**
     * Layout whatever components the RowData for this row needs.
     *
     * @param rowData - The RowData object this row will display and/or edit.
     *
     * @param row - Row index of this row.
     */
    private void layoutNeededComponents(RowData rowData, int row) {

        GridBagConstraints gc;


        /**
         * First, remove whatever components used to be in the
         * row.
         */
        removeAll();

        /**
         * Now add the components that are needed.
         */

        int gridx = 0;

        if (row == 0) {
            
            /**
             * The first/topmost row only, (and always), has the
             * Class Under Qualification comboBox and the
             * Collection Operator combobox.
             */

            gc = new GridBagConstraints();
            gc.gridx = gridx++;
            //gc.anchor = GridBagConstraints.WEST;
            add(comboBoxes[0], gc);

            gc = new GridBagConstraints();
            gc.gridx = gridx++;
            //gc.anchor = GridBagConstraints.WEST;
            add(comboBoxes[1], gc);

            gc = new GridBagConstraints();
            gc.gridx = gridx++;
            gc.weightx = 1;
            gc.anchor = GridBagConstraints.WEST;
            add(new JLabel(" of the following"), gc);
        }
        else {

            /**
             * Use a JLabel until such time as I have implemented
             * the other components for this RowData type.
             */
            gc = new GridBagConstraints();
            gc.gridx = gridx++;
            gc.weightx = 1;
            gc.anchor = GridBagConstraints.WEST;
            add(label, gc);
        }

        gc = new GridBagConstraints();
        gc.gridx = gridx++;
        add(createAttributeRowButton, gc);

        gc = new GridBagConstraints();
        gc.gridx = gridx++;
        add(createCompoundRowButton, gc);

        gc = new GridBagConstraints();
        gc.gridx = gridx++;
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

        /**
         * Cast the generic value Object that is passed in to
         * be a RowData object.
         */
        RowData rowData = (RowData)value;

        /**
         * Add/Remove whatever GUI components this row should have
         * based on the data it will be displaying.
         */
        layoutNeededComponents(rowData, row);

        /**
         * Now set the values of the components in this row.
         */

        String stringValue;

        if (rowData != null) {
            stringValue = rowData.getIndentString()+rowData.getRowString();
        }
        else {
            stringValue = "";
        }

        this.table = (ExpressionTable)table;

        /**
         * Temporarily using a JLabel until we implement all the
         * other components in the row.
         */
        label.setText(stringValue);

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

        /**
         * The very first row cannot be deleted.
         */
        if (row == 0) {
            //deleteButton.setVisible(false);
            //deleteButton.setOpaque(false);
            //deleteButton.setBackground(deleteButton.getParent().getBackground());
            //deleteButton.setForeground(deleteButton.getParent().getBackground());
            deleteButton.setEnabled(false);
        }
        else {
            //deleteButton.setVisible(true);
            deleteButton.setEnabled(true);
        }

        /**
         * See if this row can have child rows.
         * Compound Rows can have child rows.
         * Attribute Rows cannot have child rows.
         */
        Attribute attribute = null;
        if (rowData != null)
            attribute = rowData.getChildmostAttribute();
        if ((attribute != null) && (attribute.getClassDescription() != null)) {
            createCompoundRowButton.setEnabled(true);
            createAttributeRowButton.setEnabled(true);
        }
        else {
            createCompoundRowButton.setEnabled(false);
            createAttributeRowButton.setEnabled(false);
        }

        /**
         * Initialize the comboBoxes, if any, for this row.
         */
        if (row == 0) {

            /**
             * This is the first row, so it has two comboBoxes.
             * The leftmost comboBox contains the list of possible choices
             * for the Class Under Qualification.  The comboBox on the
             * right contains the Any/All/None CollectionOperator.
             *
             * TODO:  The list of CUQs must come from a configuration file.
             *
             * TODO:  Create the comboBox models only once and reuse them.
             */
            
            ClassDescription[] values =
                DataModel.getInstance().getPossibleCUQs().
                toArray(new ClassDescription[0]);

            /*
            ArrayList<String> values = new ArrayList<String>();
            for (ClassDescription classDescription :
                 DataModel.getInstance().getPossibleCUQs()) {
                values.add(classDescription.getName());
            }
            DefaultComboBoxModel model = new DefaultComboBoxModel(values.toArray(new String[0]));
            */
            DefaultComboBoxModel model = new DefaultComboBoxModel(values);
            comboBoxes[0].setModel(model);

            /*
            for (CollectionOperator operator : CollectionOperator.values()) {
                values
            }
            DefaultComboBoxModel model = new DefaultComboBoxModel(values);
            comboBoxes[0].setModel(model);
            */
            //model = new DefaultComboBoxModel(CollectionOperator.values());
            model = new DefaultComboBoxModel(CollectionOperator.
                                             getCompoundCollectionOperators());
            comboBoxes[1].setModel(model);
        }

        /**
         * The leftmost comboBox shows the list of attributes of
         * this row's "parent" class.
         */

        /*
        ClassDescription classDescription = rowData.getParentClass();

        for (Attribute att : rowData.getAttributePath()) {

            
        }
        */

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
        if (e.getSource() == createCompoundRowButton) {
            table.createCompoundRow();
        }
        else if (e.getSource() == createAttributeRowButton) {
            table.createAttributeRow();
        }
        else if (e.getSource() == deleteButton) {
            System.out.println("deleteButton pressed");
            table.deleteSelectedRow();
            //table.tableChanged(null);
        }
    }

}
