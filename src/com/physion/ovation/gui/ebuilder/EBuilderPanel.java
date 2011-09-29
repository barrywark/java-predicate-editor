package com.physion.ovation.gui.ebuilder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
//import javax.swing.DefaultListModel;
import java.util.Iterator;
import java.util.Arrays;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;

public class EBuilderPanel
    extends JPanel {

    private ExpressionPanel expressionPanel;


    /**
     * Construct an EBuilderPanel that is intialized to
     * the passed in expression tree.  Please note, the
     * passed in rootRow will be modified by this
     * panel.
     */
    EBuilderPanel(RowData rootRow) {

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        expressionPanel = new ExpressionPanel(rootRow);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(expressionPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(panel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5,5,5,5);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        add(scrollPane, gc);
    }


    public void print() {
        expressionPanel.print();
    }
}
