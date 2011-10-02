package com.physion.ovation.gui.ebuilder;

import java.awt.GridLayout;
import javax.swing.JPanel;
import java.util.Iterator;
import java.util.Arrays;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;

public class ExpressionPanel
    extends JPanel {

    private RowData rootRow;


    ExpressionPanel(RowData rootRow) {

        this.rootRow = rootRow;

        GridLayout layout = new GridLayout(1,1);
        setLayout(layout);

        createRowPanels();
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
            getParent().getParent().validate();  // Temp hack
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
     * This method simply returns the value of the root row's
     * containsLegalValue() method if there is a root RowData object.
     * If there is no root RowData object, i.e. we are empty, this
     * method returns true.
     */
    public boolean containsLegalValue() {

        if (rootRow == null)
            return(true);

        return(rootRow.containsLegalValue());
    }


    public void print() {
        System.out.println("\nCurrent Value:\n"+rootRow.toString(false, ""));
        System.out.println("\nDebug Version:\n"+rootRow.toString());
        System.out.println("containsLegalValue() = "+containsLegalValue());
    }
}
