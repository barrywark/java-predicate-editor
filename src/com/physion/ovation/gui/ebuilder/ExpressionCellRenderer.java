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
import com.physion.ovation.gui.ebuilder.datatypes.Type;
import com.physion.ovation.gui.ebuilder.datatypes.ClassDescription;

class ExpressionCellRenderer
    extends JPanel
    implements TableCellRenderer, TableCellEditor,
    ActionListener, ItemListener {

    private static final int MAX_NUM_COMBOBOXES = 20;

    private static final String[] OPERATORS_BOOLEAN = {"is true", "is false"};
    private static final String[] OPERATORS_ARITHMATIC = {"==", "!=", "<", "<=",
        ">", ">="};
    private static final String[] OPERATORS_STRING = {"==", "!=", "<", "<=",
        ">", ">=", "~=", "~~="};

    private int modelRow; // Temp hack.

    //private DefaultCellEditor;


    private JLabel label;
    private JButton deleteButton;
    private JButton createCompoundRowButton;
    private JButton createAttributeRowButton;
    private JComboBox[] comboBoxes = new JComboBox[MAX_NUM_COMBOBOXES];
    private JLabel indentWidget;

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

        /**
         * TODO:  Perhaps change this to a JPanel that has a minimum size?
         */
        indentWidget = new JLabel();

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
         * The widget we use to indent rows is always the first/leftmost
         * widget.
         */

        int gridx = 0;

        gc = new GridBagConstraints();
        gc.gridx = gridx++;
        add(indentWidget, gc);
        if (rowData != null)
            indentWidget.setText(rowData.getIndentString());
        else
            indentWidget.setText("");

        /**
         * Now add the components that are needed.
         */

        System.out.println("Laying out components for row "+row);

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

            ArrayList<Attribute> attributes = rowData.getAttributePath();
            /**
             * We are an Attribute Row, so the widgets we contain
             * are based on the values in this row's RowData object.
             *
             * Add one comboBox for every Attribute on this row's
             * attributePath.
             */
            for (Attribute attribute : attributes) {

                System.out.println("Adding comboBox at gridx "+gridx);
                gc = new GridBagConstraints();
                gc.gridx = gridx;
                add(comboBoxes[gridx++], gc);
            }

            /**
             * If the last attribute in the row is a class,
             * (as opposed to "primitive" type such as int, float, string),
             * AND it is not set to "Select Attribute",
             * then we need to display another comboBox to the right that is
             * not yet set to an attribute.
             */
            /*
            Attribute rightmostAttribute = attributes.get(attributes.size()-1);
            if (!rightmostAttribute.isPrimitive() &&
                !rightmostAttribute.equals(Attribute.SELECT_ATTRIBUTE)) {
                System.out.println("Adding Attribute comboBox at gridx "+gridx);
                gc = new GridBagConstraints();
                gc.gridx = gridx;
                add(comboBoxes[gridx++], gc);
            }
            */

            /**
             * If the rightmost Attribute is a class, as opposed
             * to a "primitive" type such as int, float, string,
             * we need to display another comboBox to its right
             * that the user can use to choose an Attribute of
             * that class or chose a special item such as "is null",
             * "is not null", "Any Property", "My Property".
             *
             * If it is a primitive type, then we need to display
             * a comboBox that has a selection of operators such as
             * =, !=, <, >=, etc.
             */
            Attribute rightmostAttribute = attributes.get(attributes.size()-1);
            if (!rightmostAttribute.isPrimitive() &&
                !rightmostAttribute.equals(Attribute.SELECT_ATTRIBUTE)) {
                System.out.println("Adding Select Attribute comboBox at gridx "+
                    gridx);
                gc = new GridBagConstraints();
                gc.gridx = gridx;
                add(comboBoxes[gridx++], gc);
            }
            else if (rightmostAttribute.isPrimitive()) {
                /**
                 * Create a comboBox that will hold operators such
                 * as ==, !=, >, etc.
                 */
                System.out.println("Adding operator comboBox at gridx "+gridx);
                gc = new GridBagConstraints();
                gc.gridx = gridx;
                add(comboBoxes[gridx++], gc);
            }
            else {
                /**
                 * The last Attribute on the right is a primitive
                 * or it is the special "Select Attribute" Attribute.
                 * So we don't need any more comboBoxes to the right
                 * of the last one in this row.
                 */
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
         *
         * Based on the above, enable/disable the +, ++, - buttons.
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
        System.out.println("Setting the model for row "+row);
        modelRow = row;
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

            /*
            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification());
            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification().hashCode());
            */
            comboBoxes[0].setSelectedItem(
                RowData.getClassUnderQualification());

            /**
             * Now do the same for the Collection Operator combobox.
             */
            model = new DefaultComboBoxModel(CollectionOperator.
                                             getCompoundCollectionOperators());
            comboBoxes[1].setModel(model);

            /*
            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification());
            System.out.println("RowData.getClassUnderQualification() = "+
                RowData.getClassUnderQualification().hashCode());
            */
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

                /**
                 * Set the model for each comboBox.
                 * Here we iterate through the list of Attributes in this
                 * row, setting the data model for each comboBox.
                 */
                ArrayList<Attribute> attributes = rowData.getAttributePath();
                for (int index = 0; index < attributes.size(); index++) {
                    if (index == 0) {
                        /**
                         * The leftmost comboBox is filled with the
                         * attributes of the parentClass.  I.e. the
                         * class of its parent row.
                         */
                        ClassDescription parentClass = rowData.getParentClass();
                        setComboBoxModel(comboBoxes[index], parentClass, true);
                    }
                    else {
                        /**
                         * This is NOT the leftmost comboBox.
                         * Each comboBox is filled with the attributes of
                         * the class of the comboBox to its left.
                         */
                        Attribute att = attributes.get(index-1);
                        setComboBoxModel(comboBoxes[index],
                                         att.getClassDescription(), true);
                    }
                }

                /**
                 * If the rightmost Attribute is a class, as opposed
                 * to a "primitive" type such as int, float, string,
                 * we need to display another comboBox to its right
                 * that the user can use to choose an Attribute of
                 * that class or choose a special item such as "is null",
                 * "is not null", "Any Property", "My Property".
                 *
                 * If it is a primitive type, then we need to display
                 * a comboBox that has a selection of operators such as
                 * =, !=, <, >=, etc.
                 */
                Attribute rightmostAttribute =
                    attributes.get(attributes.size()-1);
                if (!rightmostAttribute.isPrimitive() &&
                    !rightmostAttribute.equals(Attribute.SELECT_ATTRIBUTE)) {
                    /**
                     * Set the comboBox model to hold attributes of
                     * the class that is selected in the comboBox to our left.
                     * Make sure it also has the "Select Attribute" in it.
                     */
                    setComboBoxModel(comboBoxes[attributes.size()],
                                     rightmostAttribute.getClassDescription(),
                                     true);
                }
                else if (rightmostAttribute.isPrimitive()) {
                    /**
                     * Set the comboBox model to hold operators such
                     * as ==, !=, >, etc.
                     */
                    DefaultComboBoxModel model;
                    if (rightmostAttribute.getType() == Type.BOOLEAN)
                        model = new DefaultComboBoxModel(
                            OPERATORS_BOOLEAN);
                    else if (rightmostAttribute.getType() == Type.UTF_8_STRING)
                        model = new DefaultComboBoxModel(
                            OPERATORS_STRING);
                    else
                        model = new DefaultComboBoxModel(
                            OPERATORS_ARITHMATIC);
                    comboBoxes[attributes.size()].setModel(model);
                }
                else {
                    /**
                     * The last Attribute on the right is a primitive
                     * or it is the special "Select Attribute" Attribute.
                     * So there aren't any more comboBoxes to the right
                     * of the last one in this row.
                     */
                }

                /**
                 * By this point, the comboBox models have been set.
                 * Now set the selected value of the comboBox to reflect
                 * the value of this row's RowData object.
                 *
                 * TODO: Perhaps set the model and the value in the
                 * same loop as opposed to setting the values in a
                 * separate loop AFTER setting all the models?
                 */

                attributes = rowData.getAttributePath();
                System.out.println("attributes.size() = "+attributes.size());

                int index = 0;
                for (Attribute att : attributes)
                    comboBoxes[index++].setSelectedItem(att);

                /**
                 * If this RowData's rightmost child attribute is a
                 * primitive type such as int, float, string,
                 * we need to set the value of the operator
                 * to be whatever this RowData's value is currently
                 * set to.
                 */
                if (rowData.getChildmostAttribute().isPrimitive()) {
                    String attributeOperator = rowData.getAttributeOperator();
                    if ((attributeOperator == null) ||
                        (attributeOperator.isEmpty())) {
                        System.err.println("ERROR:  operator not set.");
                        attributeOperator = "ERROR";
                    }
                    comboBoxes[attributes.size()].setSelectedItem(
                        attributeOperator);
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


    private void setComboBoxModel(JComboBox comboBox,
                                  ClassDescription classDescription,
                                  boolean hasSelectAttribute) {

        ArrayList<Attribute> attributes = classDescription.getAllAttributes();
        Attribute[] values;

        if (hasSelectAttribute) {
            ArrayList<Attribute> copy = new ArrayList<Attribute>(attributes);
            copy.add(0, Attribute.SELECT_ATTRIBUTE);
            values = copy.toArray(new Attribute[0]);
        }
        else {
            values = attributes.toArray(new Attribute[0]);
        }

        DefaultComboBoxModel model = new DefaultComboBoxModel(values);
        comboBox.setModel(model);
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
        //System.out.println("Enter itemStateChanged = "+e);
        if (e.getStateChange() != ItemEvent.SELECTED)
            return;
        comboBoxChanged((JComboBox)e.getSource());
    }


    private void comboBoxChanged(JComboBox comboBox) {

        System.out.println("Enter comboBoxChanged");
        //int selectedRow = table.getSelectedRow();
        /*
        if (selectedRow < 0) {
            return;
        }
        */
        int editingRow = table.getEditingRow();
        if (editingRow < 0) {
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
            System.err.println("ERROR:  In comboBoxChanged.  "+
                "comboBoxIndex = "+comboBoxIndex+
                ".  This should never happen.");
            return;
        }

        /*
        if (selectedRow < 0) {
            System.err.println("POSSIBLE ERROR:  In comboBoxChanged.  "+
                "selectedRow = "+selectedRow+
                ".  Should this ever happen???");
            //return;
        }
        */

        if (editingRow < 0) {
            System.err.println("ERROR:  In comboBoxChanged.  "+
                "editingRow = "+editingRow+
                ".  This should never happen.");
            return;
        }
        if (editingRow != modelRow)
            return;

        /**
         * At this point we know which row is being edited by the user,
         * and we know which comboBox within that row is being changed.
         *
         * So, now change the appropriate value in this row's RowData
         * object.
         */

        System.out.println("comboBoxIndex = "+comboBoxIndex);
        //System.out.println("selectedRow = "+selectedRow);
        System.out.println("editingRow = "+editingRow);

        RowData rowData = RowData.getRootRow().getChild(editingRow);
        if (editingRow == 0) {
            /**
             * The first/topmost row is being edited.  So we need to
             * adjust the value of the "root" row.  Also known as the
             * Class Under Qualification.
             */
            //RowData rowData = RowData.getRootRow();

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

            //ClassDescription classDescription =
            //    (ClassDescription)comboBox.getSelectedItem();

            ArrayList<Attribute> attributes = rowData.getAttributePath();
            //Attribute attribute = attributes.get(comboBoxIndex);

            Object selectedObject = comboBox.getSelectedItem();
            if (selectedObject instanceof Attribute) {

                Attribute selectedAttribute = (Attribute)selectedObject;

                if (attributes.size() > comboBoxIndex) {
                    /**
                     * The user is setting the value of an Attribute
                     * that is already in this RowData's attributePath.
                     */
                    attributes.set(comboBoxIndex, selectedAttribute);
                }
                else if (attributes.size() == comboBoxIndex) {
                    /**
                     * This is the rightmost comboBox and this RowData
                     * is having this entry in its attributePath set
                     * to an "initial" value.  I.e. the comboBox used
                     * to say "Select Attribute" before the user selected
                     * a value for the first time.
                     */
                    attributes.add(selectedAttribute);
                }
                else if (attributes.size() < comboBoxIndex) {
                    /**
                     * This should never happen.
                     */
                    System.err.println("ERROR: Coding error.  "+
                        "Too many comboBoxes "+
                        "or too few Attributes in the class's attributePath.");
                }

                /**
                 * If the user set the value of a primitive type,
                 * that means we need to be sure the operator is
                 * initialized to an appropriate value for that
                 * type.  E.g. "==" for an int or string, "is true" for
                 * a boolean.
                 *
                 * TODO:  Add methods to the RowData class that are
                 * used to access the attributePath that automatically
                 * handle this sort of business logic.
                 */
                Attribute childmostAttribute = rowData.getChildmostAttribute();
                if (childmostAttribute.isPrimitive()) {

                    String attributeOperator;
                    switch (childmostAttribute.getType()) {
                        case BOOLEAN:
                            attributeOperator = OPERATORS_BOOLEAN[0];
                        break;
                        case UTF_8_STRING:
                            attributeOperator = OPERATORS_STRING[0];
                        break;
                        case INT_16:
                        case INT_32:
                        //case FLOAT_32:
                        case FLOAT_64:
                        case DATE_TIME:
                            attributeOperator = OPERATORS_ARITHMATIC[0];
                        break;
                        default:
                            System.err.println("ERROR: Unhandled operator.");
                            attributeOperator = "ERROR";
                    }

                    rowData.setAttributeOperator(attributeOperator);
                }

                /**
                 * Set the entry in the RowData's attributePath to have
                 * the Attribute value the user selected.
                 */
                
                rowData.setAttributeOperator(null);
                rowData.setAttributeValue(null);

                /**
                 * Remove Attributes that are "after" the one being changed.
                 */
                //attributes.removeRange(comboBoxIndex+1, attributes.size());
                attributes.subList(comboBoxIndex+1, attributes.size()).clear();
            }
            else if ((selectedObject instanceof String) &&
                     rowData.getChildmostAttribute().isPrimitive()) {

                System.out.println("User selected primitive operator "+
                                   selectedObject);
                /**
                 * The user has selected a value in primitive operator
                 * comboBox.  E.g. ==, !=, >.
                 */
                rowData.setAttributeOperator((String)selectedObject);
            }

            /**
             * 
             */

            table.tableChanged(null);
        }
    }

}
