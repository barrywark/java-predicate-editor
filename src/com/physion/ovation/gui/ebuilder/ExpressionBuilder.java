package com.physion.ovation.gui.ebuilder;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;


/**
 * This is the class that engineers who want to display the
 * Expression Builder GUI should use.
 *
 * The static method:
 *
 *      ExpressionBuilder.editExpression() 
 *
 * is the only method you will need to use.  For example the
 * code below will display an "empty" GUI for the user to
 * create an expression.  Once the user clicks Ok or Cancel,
 * the code prints the status and the expression tree the user
 * created.
 *
 *      RowData rootRow = RowData.createRootRow();
 *      int status = ExpressionBuilder.editExpression(rootRow);
 *      System.out.println("status = "+status);
 *      System.out.println("rootRow:\n"+rootRow);
 *
 * The code below will create an expression tree that has
 * some rows already in it and display that in the GUI.
 *
 *      RowData rootRow = RowData.createTestRowData();
 *      int status = ExpressionBuilder.editExpression(rootRow);
 * 
 * The returned status value tells the caller whether the
 * user pressed the Ok button or canceled out of the window.
 */
public class ExpressionBuilder
    extends JDialog 
    implements ActionListener {


    public static final int RETURN_STATUS_OK = 0;
    public static final int RETURN_STATUS_CANCEL = 1;

    private int returnStatus = RETURN_STATUS_CANCEL;
    private EBuilderPanel panel;
    private JButton okButton;
    private JButton cancelButton;


    /**
     * Create an ExpressionBuilder dialog that will let
     * the user edit the passed in expression tree.
     *
     * @param rootRow - The rootRow of the expression tree
     * you want to edit.
     */
    ExpressionBuilder(RowData rootRow) {

        super((Frame)null);
        setTitle("Physion ooExpression Builder");
        setModal(true);

        panel = new EBuilderPanel(rootRow);
        getContentPane().add(panel);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));

        GridBagConstraints gc;

        okButton = new JButton("Ok");
        okButton.addActionListener(this);
        gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(okButton, gc);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(cancelButton, gc);

        pack();
        int width = getSize().width;
        int height = getSize().height;
        if (width < 800)
            width = 800;
        height += 200;
        setSize(width, height);
    }


    /**
     * This returns the reason the window closed.
     *
     * @return Returns ExpressionBuilder.RETURN_STATUS_OK if the user
     * pressed the Ok button.  Returns ExpressionBuilder.RETURN_STATUS_CANCEL
     * if the user pressed the Cancel button or closed the window another
     * way.  It also returns ExpressionBuilder.RETURN_STATUS_CANCEL if
     * a null rootRow parameter was passed to the editExpression() method.
     */
    public int getReturnStatus() {
        return(returnStatus);
    }


    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton) {
            returnStatus = RETURN_STATUS_CANCEL;
            setVisible(false);
        }
        else if (e.getSource() == okButton) {
            returnStatus = RETURN_STATUS_OK;
            panel.print();
            setVisible(false);
        }
    }


    /**
     * This is the method you should use to display the GUI
     * to edit/create an expression.
     *
     * @param rootRow - This is the "root" RowData of the
     * expression you want the GUI to display and edit.
     * Pass an "empty" RowData if you want to create a new
     * expression from scratch.
     *
     * @return Returns ExpressionBuilder.RETURN_STATUS_OK if the user
     * pressed the Ok button.  Returns ExpressionBuilder.RETURN_STATUS_CANCEL
     * if the user pressed the Cancel button or closed the window another
     * way.  It also returns ExpressionBuilder.RETURN_STATUS_CANCEL if
     * a null rootRow parameter was passed to the editExpression() method.
     */
    public static int editExpression(RowData rootRow) {

        if (rootRow == null) {
            System.err.println("rootRow = null");
            return(RETURN_STATUS_CANCEL);
        }
        else if (rootRow.getRootRow() == null) {
            System.err.println("rootRow.getRootRow() = null");
            return(RETURN_STATUS_CANCEL);
        }

        ExpressionBuilder dialog = new ExpressionBuilder(rootRow);

        /**
         * Make the dialog visible.  It is modal, so
         * execution along this path stops at this point.
         * Only when the user hits Ok or Cancel will this
         * call to the setVisible() method return.
         */
        dialog.setVisible(true);

        int returnStatus = dialog.getReturnStatus();
        return(returnStatus);
    }


    /**
     * A simple test program to test this class.
     */
    public static void main(String[] args) {

        RowData rootRow = RowData.createRootRow();
        int status = ExpressionBuilder.editExpression(rootRow);
        System.out.println("\nstatus = "+status);
        System.out.println("rootRow:\n"+rootRow);

        rootRow = RowData.createTestRowData();
        status = ExpressionBuilder.editExpression(rootRow);
        System.out.println("\nstatus = "+status);
        System.out.println("rootRow:\n"+rootRow);
        System.exit(status);
    }
}
