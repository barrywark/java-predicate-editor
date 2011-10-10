package com.physion.ovation.gui.ebuilder;

import java.awt.GridLayout;
import javax.swing.JPanel;
import java.util.Iterator;
import java.util.Arrays;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataEvent;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataListener;


/**
 * This is the panel that contains a list of RowPanels.
 * Each RowPanel handles one row in the expression tree.
 * Each RowPanel displays/edits one RowData object.
 *
 * TODO: Remove the commented out "zebra striping" code if
 * Physion decides it does not want to do something like that.
 */
public class ExpressionPanel
    extends JPanel
    implements RowDataListener {

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

        if (this.rootRow != null)
            this.rootRow.removeRowDataListener(this);

        this.rootRow = rootRow;
        rootRow.addRowDataListener(this);
        createRowPanels();
    }


    public RowData getRootRow() {
        return(rootRow);
    }


    /**
     * Create all the RowPanels this ExpressionPanel contains.
     *
     * TODO: Make this code more clever so when rows are added
     * or deleted we don't remove all the rows and recreate
     * all the rows, but instead we only change what we need to.
     */
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
     * This is called when the expression tree we are displaying
     * is modified.
     */
    public void rowDataChanged(RowDataEvent event) {

        if (event.getTiming() == RowDataEvent.TIMING_AFTER)
            createRowPanels();
    }


    /**
     * This is just for debugging purposes.
     */
    public void print() {
        System.out.println("\nCurrent Value:\n"+rootRow.toString(false, ""));
        System.out.println("\nDebug Version:\n"+rootRow.toString());
    }
}
