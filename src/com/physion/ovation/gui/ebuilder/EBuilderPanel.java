package com.physion.ovation.gui.ebuilder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
//import javax.swing.DefaultListModel;
import java.util.Iterator;
import java.util.Arrays;

import com.physion.ovation.gui.ebuilder.datamodel.EBuilderTableModel;

public class EBuilderPanel
    extends JPanel {

    private ExpressionTable table;


    EBuilderPanel() {

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        EBuilderTableModel tableModel = new EBuilderTableModel();

        table = new ExpressionTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            //ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5,5,5,5);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        add(scrollPane, gc);
    }


    public void print() {
        table.printModel();
    }
}
