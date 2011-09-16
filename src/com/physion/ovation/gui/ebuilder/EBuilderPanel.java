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

    EBuilderPanel() {

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        //final EBuilderTree tree = new EBuilderTree();

        EBuilderTableModel tableModel = new EBuilderTableModel();

        //final JList list = new JList(listModel);
        //final JTable table = new JTable(tableModel);
        final ExpressionTable table = new ExpressionTable(tableModel);

        /*
        ExpressionCellRenderer expressionCellRenderer =
            new ExpressionCellRenderer();
        list.setCellRenderer(expressionCellRenderer);
        */

        /**
         * Create some filler strings for testing DnD.
         */
/*
        DefaultListModel defModel = new DefaultListModel();
        list.setModel (defModel);
        String[] listItems = {
            "Any of the following",
            "        <attribute> = foobar",
            "        <attribute> ~= 12",
            "        path.to.attribute >= 27",
            "        All of the following",
            "               <attribute> - ^",
            "               path.to.attribute >= 27",
            "        All of the following",
            "        path.to.attribute >= 56",
            "        <attribute> = 2"
        };
        Iterator it = Arrays.asList(listItems).iterator();
        while (it.hasNext())
            defModel.addElement (it.next());
*/

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
}
