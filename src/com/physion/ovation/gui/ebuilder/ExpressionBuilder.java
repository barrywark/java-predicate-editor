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
 * is the only method you will need to use.  For example, the
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

    private RowData rootRow = null;
    private int returnStatus = RETURN_STATUS_CANCEL;
    private EBuilderPanel panel;
    private JButton okButton;
    private JButton cancelButton;


    /**
     * Create an ExpressionBuilder dialog with its expression
     * tree initialized to a copy of the passed in expression.
     * Pass null if you want to create a tree from scratch.
     *
     * @param originalRootRow - The rootRow of the expression tree
     * you want to edit.  (Please note, the GUI edits a copy
     * of the passed in rootRow.)
     */
    ExpressionBuilder(RowData originalRootRow) {

        super((Frame)null);
        setTitle("Physion ooExpression Builder");
        setModal(true);

        //this.rootRow = new RowData(originalRootRow);
        this.rootRow = originalRootRow;

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
    private int getReturnStatus() {
        return(returnStatus);
    }


    private RowData getRootRow() {
        return(rootRow);
    }


    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton) {
            returnStatus = RETURN_STATUS_CANCEL;
            rootRow = null;
            setVisible(false);
        }
        else if (e.getSource() == okButton) {
            returnStatus = RETURN_STATUS_OK;
            panel.print();
            setVisible(false);
        }
    }


    /**
     * Call this method to create a new expression tree
     * from scratch.  I.e. you are not editing an
     * already existing expression tree.
     *
     * This is simply a convenience function to call
     * editExpression(null).  See that method for more
     * information about the returned ReturnValue.
     *
     * An example of using this method is in this
     * class's main() method.
     */
    public static ReturnValue editExpression() {
        return(editExpression(null));
    }


    /**
     * This is the method you should use to display the GUI
     * to edit/create an expression.
     *
     * An example of using this method is in this
     * class's main() method.
     *
     * @param rootRow - This is the "root" RowData of the
     * expression you want the GUI to display and edit.
     * Please note, the GUI edits a COPY of this expression tree, so
     * the rootRow parameter you passed in is not ever modified
     * by the GUI.
     * Pass null if you want to create a new expression from scratch.
     *
     * @return Returns an ExpressionBuilder.ReturnValue object that
     * contains the "status" and "rootRow" values that your code
     * should look at.  Please see ExpressionBuilder.ReturnValue
     * for more information.
     */
    public static ReturnValue editExpression(RowData rootRow) {

        /**
         * Create a returnValue that is already initialized
         * to the values used if a user cancels out of the window.
         */
        ReturnValue returnValue = new ReturnValue();
        returnValue.status = RETURN_STATUS_CANCEL;
        returnValue.rootRow = null;

        if (rootRow == null) {
            rootRow = RowData.createRootRow();
        }

        if (rootRow.getRootRow() == null) {
            System.err.println("ERROR: rootRow.getRootRow() = null");
            return(returnValue);
        }

        ExpressionBuilder dialog = new ExpressionBuilder(rootRow);

        /**
         * Make the dialog visible.  It is modal, so
         * execution along this path stops at this point.
         * Only when the user hits Ok or Cancel will this
         * call to the setVisible() method return.
         */
        dialog.setVisible(true);

        returnValue.status = dialog.getReturnStatus();
        returnValue.rootRow = dialog.getRootRow();
        return(returnValue);
    }


    /**
     * This class holds the return values of the editExpression() method.
     *
     * status - Is set to ExpressionBuilder.RETURN_STATUS_OK if the user
     * pressed the Ok button.  It is set to
     * ExpressionBuilder.RETURN_STATUS_CANCEL if the user pressed the
     * Cancel button or closed the window another way.
     */
    public static class ReturnValue {
        public int status;
        public RowData rootRow;
    }


    /**
     * A simple example test program to test this class and show
     * how the editExpression() methods are used.
     */
    public static void main(String[] args) {

        /**
         * Create a new expression from scratch.
         */
        ReturnValue returnValue = ExpressionBuilder.editExpression();
        System.out.println("\nstatus = "+returnValue.status);
        System.out.println("rootRow:\n"+returnValue.rootRow);

        /**
         * After the user Oks or Cancels out of the window
         * created above, create another window. 
         * This time though, create an expression tree filled with
         * some values and have the window initialized to that
         * tree's value.
         */
        RowData rootRow = RowData.createTestRowData();
        returnValue = ExpressionBuilder.editExpression(rootRow);
        System.out.println("\nstatus = "+returnValue.status);
        System.out.println("Modified rootRow:\n"+returnValue.rootRow);
        System.out.println("Original rootRow:\n"+rootRow);
        System.exit(returnValue.status);
    }
}
