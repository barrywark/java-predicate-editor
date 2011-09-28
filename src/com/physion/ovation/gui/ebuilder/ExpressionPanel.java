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
        layout.setRows(rootRow.getNumDescendents()+1);

        int count = 0;
        for (RowData rowData : rootRow.getRows()) {
            RowPanel rowPanel = new RowPanel(rowData);
            add(rowPanel);
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


    private RowPanel getRowPanel(int index) {
        return((RowPanel)getComponent(index));
    }


    public void print() {
        System.out.println("\nCurrent Value:\n"+rootRow.toString(false, ""));
        System.out.println("\nDebug Version:\n"+rootRow.toString());
    }
}
