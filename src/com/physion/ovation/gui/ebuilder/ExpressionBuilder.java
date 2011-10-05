package com.physion.ovation.gui.ebuilder;

import java.util.ArrayList;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataEvent;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataListener;


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
 *      ExpressionBuilder.ReturnValue returnValue;
 *      returnValue = ExpressionBuilder.editExpression(null);
 *
 * The code below will create an expression tree that has
 * some rows already in it and display that in the GUI.
 *
 *      RowData rootRow = RowData.createTestRowData();
 *      returnValue = ExpressionBuilder.editExpression(rootRow);
 * 
 * The returned returnValue.status value tells the caller whether the
 * user pressed the Ok button or canceled out of the window.
 * If returnValue.status is RETURN_STATUS_CANCEL, then
 * returnValue.rootRow is null.
 *
 * Please see the class's main() method to see example code
 * you can use to create and display the GUI.
 */
public class ExpressionBuilder
    extends JDialog 
    implements ActionListener, RowDataListener {

    private int INSET = 6;
    private int MAX_NUM_STATES_SAVED = 50;

    public static final int RETURN_STATUS_OK = 0;
    public static final int RETURN_STATUS_CANCEL = 1;

    private int returnStatus = RETURN_STATUS_CANCEL;
    private ExpressionPanelScrolling expressionPanelScrolling;
    private JButton okButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton cancelButton;

    private ArrayList<RowData> stateList = new ArrayList<RowData>();
    private int currentStateIndex = -1;
    //private int prevDescendentCount = -1;


    /**
     * Create an ExpressionBuilder dialog with its expression
     * tree initialized to a copy of the passed in expression.
     *
     * @param originalRootRow - The rootRow of the expression tree
     * you want to edit.  (Please note, the GUI edits a copy
     * of the passed in rootRow.)
     * Do NOT pass null.
     */
    ExpressionBuilder(RowData originalRootRow) {

        super((Frame)null);
        setTitle("Physion ooExpression Builder");
        setModal(true);

        if (originalRootRow == null) {
            System.out.println("ExpressionBuilder constructor was passed\n"+
                "a null value for the originalRootRow parameter.\n"+
                "That is not allowed.  Please see ExpressionBuilder.main() "+
                "for some example code showing you how to use it.");
            return;
        }

        /**
         * Make a copy of the passed in expression tree so that
         * way the GUI will not affect it.
         */
        RowData rootRow = new RowData(originalRootRow);

        expressionPanelScrolling = new ExpressionPanelScrolling(rootRow);
        getContentPane().add(expressionPanelScrolling);

        /**
         * Layout the buttons at the bottom of the panel
         * with Prev and Next grouped on the left and
         * Ok Cancel grouped on the right.
         *
         *  Prev Next           Ok Cancel
         */
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(
            0, INSET, INSET, INSET));

        GridBagConstraints gc;
        
        JPanel leftButtonPanel = new JPanel(new GridLayout(1, 2, INSET*2, 0));
        gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.WEST;
        buttonPanel.add(leftButtonPanel, gc);

        JPanel rightButtonPanel = new JPanel(new GridLayout(1, 2, INSET*2, 0));
        gc.gridx = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.EAST;
        buttonPanel.add(rightButtonPanel, gc);

        prevButton = new JButton("Prev");
        prevButton.addActionListener(this);
        leftButtonPanel.add(prevButton);

        nextButton = new JButton("Next");
        nextButton.addActionListener(this);
        leftButtonPanel.add(nextButton);

        okButton = new JButton("Ok");
        okButton.addActionListener(this);
        rightButtonPanel.add(okButton, gc);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        rightButtonPanel.add(cancelButton);

        /**
         * Set the size of the window.
         * This is just some crude logic to make sure
         * the window is a reasonable width and is
         * a little taller than the tree it currently
         * contains.
         */
        pack();
        int width = getSize().width;
        int height = getSize().height;
        if (width < 800)
            width = 800;
        if (height < 600)
            height += 200;
        setSize(width, height);

        /**
         * Save the current state of the tree.
         */
        possiblySaveState();

        /**
         * Add ourselves as a listener to the RowData expression tree
         * so we can enable/disable the Ok button depending on whether
         * the expression tree is currently legal.
         */
        rootRow.addRowDataListener(this);

        /**
         * Enable/disable the buttons for the first time.
         * Later, buttons will be enabled/disabled when the
         * user changes the expression tree.
         */
        enableButtons();
    }


    /**
     * Change the expression tree we are displaying.
     */
    private void setRootRow(RowData rowData) {

        /**
         * Stop listening to the old tree if there was an old tree.
         */
        if (getRootRow() != null)
            getRootRow().removeRowDataListener(this);

        expressionPanelScrolling.setRootRow(rowData);

        /**
         * Start listening to the new tree.
         */
        getRootRow().addRowDataListener(this);

        /**
         * Enable/disable buttons as appropriate for the new tree.
         */
        enableButtons();
    }


    /**
     * This returns the reason the window closed.
     * Engineers using the ExpressionBuilder.editExpression() method
     * should not be calling this method.  They should be using the
     * returned ReturnValue object to get this information.
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


    /**
     * This returns the new, (possibly modified), expression tree.
     * Engineers using the ExpressionBuilder.editExpression() method
     * should not be calling this method.  They should be using the
     * returned ReturnValue object to get this information.
     */
    private RowData getRootRow() {
        return(expressionPanelScrolling.getRootRow());
    }


    /**
     * This is called when the user clicks the Ok or Cancel button.
     */
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton) {
            returnStatus = RETURN_STATUS_CANCEL;
            setVisible(false);
        }
        else if (e.getSource() == okButton) {
            returnStatus = RETURN_STATUS_OK;
            expressionPanelScrolling.print();
            setVisible(false);
        }
        else if (e.getSource() == prevButton) {
            handlePrevButton();
        }
        else if (e.getSource() == nextButton) {
            handleNextButton();
        }
    }


    private void handlePrevButton() {

        if (currentStateIndex < 1) {
            System.err.println("ERROR: handlePrevButton() called when we "+
                               "are already at the first state in the "+
                               "stateList.  This is a programming error.");
            return;
        }

        /**
         * Go back to the previous version of the expression tree.
         */
        currentStateIndex--;
        RowData rootRow = stateList.get(currentStateIndex);
        setRootRow(rootRow);
    }


    private void handleNextButton() {

        if (currentStateIndex >= stateList.size()) {
            System.err.println("ERROR: handleNextButton() called when we "+
                               "are already at the last state in the "+
                               "stateList.  This is a programming error.");
            return;
        }

        /**
         * Go to the next version of the expression tree.
         */
        currentStateIndex++;
        RowData rootRow = stateList.get(currentStateIndex);
        setRootRow(rootRow);
    }


    /**
     * Engineers should call this method to create a new
     * expression tree from scratch.
     * I.e. you are not editing an already existing expression tree.
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

        if (rootRow == null) {
            rootRow = RowData.createRootRow();
        }

        if (rootRow.getRootRow() == null) {
            // This should never happen.
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
        if (returnValue.status == RETURN_STATUS_OK)
            returnValue.rootRow = dialog.getRootRow();
        else
            returnValue.rootRow = null;

        return(returnValue);
    }


    /**
     * This method is called when the expression tree we are
     * displaying/editing changes.
     * When the tree changes we enable/disable the Ok button
     * depending on whether the expression tree currently
     * contains a legal value.  I.e. we will force the user
     * to make the tree legal before we let him/her Ok out
     * of the window.
     */
    @Override
    public void rowDataChanged(RowDataEvent e) {

        possiblySaveState();
        currentStateIndex = stateList.size()-1;
        enableButtons();
    }


    /**
     * This method will (possibly) save the state of the current
     * expression tree.  Currently we are saving almost every
     * change.  That is overkill, but was the most simple thing
     * to implement.
     *
     * TODO: Only save the state if the user has changed the number
     * of rows in the tree.  (We don't save the changes the user
     * makes to the attributePath, for example.)
     *
     * Saving at this "granularity" should prevent the user from
     * shooting his/herself in the foot and loosing many changes
     * s/he has made to the expression tree if s/he accidentally
     * changes a "top level" comboBox.
     */
    private void possiblySaveState() {

        if (getRootRow() == null)
            return;

        /**
         * If the user is in the process of clicking the Next and Prev
         * buttons, don't save those states.
         */
        if (currentStateIndex != stateList.size()-1)
            return;

        /*
        if (getRootRow().getDescendentCount() != prevDescendentCount) {
            prevDescendentCount = getRootRow().getDescendentCount();
            stateList.add(new RowData(getRootRow()));
            currentStateIndex = stateList.size()-1;
            enableButtons();
        }
        */
        /**
         * There has been a user initiated change, so
         * save the state of the expression tree and
         * possibly "roll off" the oldest state that
         * we had saved.
         */
        stateList.add(new RowData(getRootRow()));
        if (stateList.size() > MAX_NUM_STATES_SAVED) {
            stateList.remove(0);
        }

        /**
         * Set the currentStateIndex to be pointing to
         * the current, (i.e. last), state.
         */
        currentStateIndex = stateList.size()-1;
        enableButtons();
    }


    /**
     * Enable/disable buttons.
     *
     * Enable/disable the Ok button depending on whether
     * the expression tree is currently a legal value.
     *
     * Enable/disable the Prev and Next buttons depending
     * on whether there are states previous to or after the
     * currentStateIndex in the stateList.
     * For example, if the stateList has two items in it,
     * and the currentStateIndex is 1, then the Prev button
     * is enabled because there is a previous state we can
     * go to.  The Next button is not enabled because there
     * is now state "after" the one currentStateIndex is
     * currently pointing to.
     */
    private void enableButtons() {

        //System.out.println("stateList.size() = "+stateList.size());
        //System.out.println("currentStateIndex = "+currentStateIndex);

        if (getRootRow() == null) {
            okButton.setEnabled(false);
            return;
        }

        okButton.setEnabled(getRootRow().containsLegalValue());

        if (currentStateIndex > 0)
            prevButton.setEnabled(true);
        else
            prevButton.setEnabled(false);

        if (currentStateIndex < (stateList.size()-1))
            nextButton.setEnabled(true);
        else
            nextButton.setEnabled(false);
    }


    /**
     * This class holds the return values of the editExpression() method.
     *
     * status - Is set to ExpressionBuilder.RETURN_STATUS_OK if the user
     * pressed the Ok button.  It is set to
     * ExpressionBuilder.RETURN_STATUS_CANCEL if the user pressed the
     * Cancel button or closed the window another way.
     *
     * rootRow - Is set to the new expression tree that was created
     * and edited by the GUI.
     *
     * Please see ExpressionBuilder.main() for some examples of how
     * this class is used.
     */
    public static class ReturnValue {
        public int status = RETURN_STATUS_CANCEL;
        public RowData rootRow = null;
    }


    /**
     * A simple example test program to test this class and show
     * how the editExpression() methods are used.
     *
     * This example first displays a GUI that is empty.  After
     * the user Oks or Cancels out of that GUI, we display a
     * GUI filled with an existing expression tree.
     */
    public static void main(String[] args) {

        ExpressionBuilder.ReturnValue returnValue;


        /**
         * Display a GUI that is empty.  I.e. the user must
         * create a new expression from scratch.
         */
        returnValue = ExpressionBuilder.editExpression();
        if (returnValue.status == ExpressionBuilder.RETURN_STATUS_OK) {
            System.out.println("User pressed OK.");
            System.out.println("rootRow:\n"+returnValue.rootRow);
        }
        else {
            System.out.println("User pressed Cancel or closed the window.");
        }

        /**
         * After the user Oks or Cancels out of the window
         * created above, create another window.  This time
         * though, create an expression tree filled with
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
