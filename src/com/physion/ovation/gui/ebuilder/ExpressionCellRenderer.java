//package com.physion.ovation.gui.ebuilder;

import java.util.EventObject;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
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
    implements TableCellRenderer, TableCellEditor,
    ActionListener, ItemListener {

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
            JComboBox comboBox = new JComboBox();
            comboBoxes[count] = comboBox;
            comboBox.setEditable(false);
            comboBox.addItemListener(this);
            //comboBox.addActionListener(this);
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

        if ((row == 0) || rowData.isCompoundRow()) {
        //if (rowData.isCompoundRow()) {
            
            /**
             * The first/topmost row always has the
             * Class Under Qualification comboBox and the
             * Collection Operator comboBox, (and only those comboBoxes).
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
             * We are an Attribute Row, so the widgets we contain
             * are based on the values in this row's RowData object.
             */

            for (Attribute attribute : rowData.getAttributePath()) {

                gc = new GridBagConstraints();
                gc.gridx = gridx;
                //gc.anchor = GridBagConstraints.WEST;
                add(comboBoxes[gridx++], gc);
            }

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
         * The "root" row is a special case that is a Compound Row.
         */

        Attribute attribute = null;
        if (rowData != null) 
            attribute = rowData.getChildmostAttribute();

        ClassDescription classDescription = null;
        if (attribute != null)
            classDescription = attribute.getClassDescription();

        if (rowData == RowData.getRootRow())
            classDescription = RowData.getClassUnderQualification();

        if (classDescription != null) {
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

            DefaultComboBoxModel model = new DefaultComboBoxModel(values);
            comboBoxes[0].setModel(model);

            /**
             * We have set the data model for the Class Under Qualification
             * comboBox, i.e. what choices in contains,
             * now set the currently selected value to be the
             * value in this row's RowData object.
             */

            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification());
            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification().hashCode());
            comboBoxes[0].setSelectedItem(
                RowData.getClassUnderQualification());

            /**
             * Now do the same for the Collection Operator combobox.
             */
            model = new DefaultComboBoxModel(CollectionOperator.
                                             getCompoundCollectionOperators());
            comboBoxes[1].setModel(model);

            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification());
            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification().hashCode());
            comboBoxes[1].setSelectedItem(RowData.getRootRow().
                getCollectionOperator());
        }
        else {

            if (rowData.isCompoundRow()) {
            }
            else {
                /**
                 * This is an Attribute Row.
                 * The leftmost comboBox shows the list of attributes of
                 * this row's "parent" class.
                 */

                ClassDescription parentClass = rowData.getParentClass();
                ArrayList<Attribute> attributes =
                    parentClass.getAllAttributes();

                Attribute[] values = attributes.toArray(new Attribute[0]);
                System.out.println("values[3] = "+values[3]);
                System.out.println("values[3].hashCode() = "+
                                    values[3].hashCode());

                DefaultComboBoxModel model = new DefaultComboBoxModel(values);
                comboBoxes[0].setModel(model);

                /**
                 * Now set the value of the comboBox to reflect the value
                 * of this row's RowData object.
                 */

                attributes = rowData.getAttributePath();
                System.out.println("attributes.size() = "+attributes.size());
                if (attributes.size() > 0) {

                    /*
                    classDescription = attributes.get(0).getClassDescription();
                    System.out.println("classDescription = "+classDescription);
                    System.out.println("classDescription.hashCode() = "+
                        classDescription.hashCode());

                    if (classDescription != null)
                        comboBoxes[0].setSelectedItem(classDescription);
                    else
                        comboBoxes[0].setSelectedIndex(0);
                    */
                    System.out.println("attributes.get(0) = "+
                                       attributes.get(0));
                    System.out.println("attributes.get(0).hashCode() = "+
                                       attributes.get(0).hashCode());
                    comboBoxes[0].setSelectedItem(attributes.get(0));
                }
            }
        }

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

        System.out.println("Enter actionPerformed = "+e);
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
        else if (e.getSource() instanceof JComboBox) {
            comboBoxChanged((JComboBox)e.getSource());
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        System.out.println("Enter itemStateChanged = "+e);
        comboBoxChanged((JComboBox)e.getSource());
    }


    private void comboBoxChanged(JComboBox comboBox) {

        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        /**
         * Figure out which comboBox was changed.
         */
        int comboBoxIndex = -1;
        for (int index = 0; index < comboBoxes.length; index++) {
            //if (e.getSource() == comboBoxes[index]) {
            if (comboBox == comboBoxes[index]) {
                comboBoxIndex = index;
            }
        }

        if (comboBoxIndex < 0) {
            System.out.println("ERROR:  In comboBoxChanged.  "+
                "comboBoxIndex = "+comboBoxIndex+
                ".  This should never happen.");
            return;
        }

        if (selectedRow < 0) {
            System.out.println("ERROR:  In comboBoxChanged.  "+
                "selectedRow = "+selectedRow+
                ".  This should never happen.");
            return;
        }

        /**
         * At this point we know which row is being edited by the user,
         * and we know which comboBox within that row is being changed.
         *
         * So, now change the appropriate value in this row's RowData
         * object.
         */

        System.out.println("comboBoxIndex = "+comboBoxIndex);
        System.out.println("selectedRow = "+selectedRow);

        if (selectedRow == 0) {
            /**
             * The first/topmost row is being edited.  So we need to
             * adjust the value of the "root" row.  Also known as the
             * Class Under Qualification.
             */
            RowData rowData = RowData.getRootRow();

            if (comboBoxIndex == 0) {
                /**
                 * User is changing the value of the Class Under Qualification.
                 */
                ClassDescription classDescription =
                    (ClassDescription)comboBox.getSelectedItem();
                System.out.println("selected classDescription = "+
                    classDescription);
                System.out.println("selected classDescription = "+
                    classDescription.hashCode());
                if (!rowData.getClassUnderQualification().equals(
                    classDescription)) {
                    rowData.setClassUnderQualification(classDescription);
                    table.tableChanged(null);
                }
            }
            else if (comboBoxIndex == 1) {
                /**
                 * User is changing the value of the Collection Operator.
                 */
                CollectionOperator collectionOperator =
                    (CollectionOperator)comboBox.getSelectedItem();
                System.out.println("selected collectionOperator = "+
                    collectionOperator);
                System.out.println("selected collectionOperator = "+
                    collectionOperator.hashCode());
                if (!rowData.getCollectionOperator().equals(
                    collectionOperator)) {
                    rowData.setCollectionOperator(collectionOperator);
                    table.tableChanged(null);
                }
            }
        }
        else {
            System.out.println("TODO: write code to handle this comboBox.");
        }
    }

}
