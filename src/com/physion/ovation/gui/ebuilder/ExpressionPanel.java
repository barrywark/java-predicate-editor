package com.physion.ovation.gui.ebuilder;

import java.awt.GridLayout;
import javax.swing.JPanel;
import java.util.Iterator;
import java.util.Arrays;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;


/**
 * This is the panel that contains a list of RowPanels.
 * Each RowPanel handles one row in the expression tree.
 * Each RowPanel displays/edits one RowData object.
 *
 * TODO: Remove the commented out "zebra striping" code if
 * Physion decides it does not want to do something like that.
 */
public class ExpressionPanel
    extends JPanel {

    private RowData rootRow;


    /**
     * Create an ExpressionPanel that will display and edit the passed
     * in expression tree.
     */
    ExpressionPanel(RowData rootRow) {

        super(new GridLayout(1,1));
        setRootRow(rootRow);
    }


    /**
     * Set the expression tree this ExpressionPanel will display.
     */
    public void setRootRow(RowData rootRow) {

        this.rootRow = rootRow;
        createRowPanels();
    }


    public RowData getRootRow() {
        return(rootRow);
    }


    public void createRowPanels() {

        removeAll();

        GridLayout layout = (GridLayout)getLayout();
        layout.setRows(rootRow.getDescendentCount()+1);

        //int count = 0;
        for (RowData rowData : rootRow.getRows()) {
            RowPanel rowPanel = new RowPanel(rowData);
            add(rowPanel);

            /**
             * Zebra stripe the rows.
             */
            /*
            if ((count % 2) == 0)
                rowPanel.setBackground(rowPanel.getBackground().darker());
            count++;
            */
        }

        /**
         * Make the scrollpane layout things again.
         */
        if ((getParent() != null) && (getParent().getParent() != null))
            getParent().getParent().validate();
    }
    

    /**
     * Get the RowPanel at the passed in index.
     * The root RowPanel, which is the one that is used
     * to select the Class Under Qualification, is at index 0.
     */
    private RowPanel getRowPanel(int index) {
        return((RowPanel)getComponent(index));
    }


    /**
     * This is just for debugging purposes.
     */
    public void print() {
        System.out.println("\nCurrent Value:\n"+rootRow.toString(false, ""));
        System.out.println("\nDebug Version:\n"+rootRow.toString());
    }
}
