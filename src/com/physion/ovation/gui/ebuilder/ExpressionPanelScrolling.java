package com.physion.ovation.gui.ebuilder;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.BorderLayout;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;

/**
 * This panel contains a scrollPane that contains the
 * ExpressionPanel.  It really doesn't do anything besides
 * pass the set/getRootRow() calls on to the ExpressionPanel
 * that is in our scrollPane.
 */
public class ExpressionPanelScrolling
    extends JPanel {
	/**
	 * We never serialize this class, so this declaration is
	 * just to stop the compiler warning.
	 * You can suppress the warning instead if you want using
	 * @SuppressWarnings("serial")
	 */
	private static final long serialVersionUID = 1L;
	
    private ExpressionPanel expressionPanel;


    /**
     * Construct an ExpressionPanelScrolling that is intialized to
     * the passed in expression tree.  Please note, the
     * passed in rootRow will be modified by this
     * panel.
     */
    ExpressionPanelScrolling(RowData rootRow) {

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        expressionPanel = new ExpressionPanel(rootRow);

        /*
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(expressionPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(panel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        */
        JScrollPane scrollPane = new JScrollPane(expressionPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5,5,5,5);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        add(scrollPane, gc);
    }


    public void setRootRow(RowData rowData) {
        expressionPanel.setRootRow(rowData);
    }


    public RowData getRootRow() {
        return(expressionPanel.getRootRow());
    }


    /**
     * This is just for debugging.
     */
    public void print() {
        expressionPanel.print();
    }
}
