package com.physion.ovation.gui.ebuilder;

import java.util.ArrayList;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.UIManager;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataEvent;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataListener;
import com.physion.ovation.gui.ebuilder.expression.ExpressionTranslator;
import com.physion.ovation.gui.ebuilder.expression.ExpressionTree;


/**
 * This is the class that engineers who want to display the
 * Expression Builder GUI should use.
 *
 * The public static method:
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
 * The code below will display an already existing ExpressionTree
 * in the GUI.
 *
 *      ExpressionTree expressionTree = someMethodToCreateExpressionTree();
 *      returnValue = ExpressionBuilder.editExpression(expressionTree);
 * 
 * The returned returnValue.status value tells the caller whether the
 * user pressed the Ok button or canceled out of the window.
 * If returnValue.status is RETURN_STATUS_CANCEL, then
 * returnValue.expressionTree is null.
 *
 * Please see the class's main() method to see example code
 * you can use to create and display the GUI.  It is currently
 * set up to serialize the ExpressionTree out to a file and
 * then read it back in next time main() is run.
 */
public class ExpressionBuilder
    extends JDialog 
    implements ActionListener, RowDataListener {

    /**
	 * We never serialize this class, so this declaration is
	 * just to stop the compiler warning.
	 * You can suppress the warning instead if you want using
	 * @SuppressWarnings("serial")
	 */
	private static final long serialVersionUID = 1L;

    private static final String SAVE_FILE_NAME_EXP_TREE = "saved.ExpTree";

	/**
     * Number of pixels used as a spacer.
     */
    private int INSET = 6;

    /**
     * Maximum number of expression tree "states" that are
     * saved.  I.e. the user can "undo" up to this many
     * previous states of the tree.
     */
    private int MAX_NUM_STATES_SAVED = 50;

    /**
     * This is the return status if the user closed the
     * the window by pressing the Ok button.
     */
    public static final int RETURN_STATUS_OK = 0;

    /**
     * This is the return status if the user closed the
     * window by pressing the Cancel button or closing
     * the window in some other way besides pressing Ok.
     */
    public static final int RETURN_STATUS_CANCEL = 1;

    private int returnStatus = RETURN_STATUS_CANCEL;
    private ExpressionPanelScrolling expressionPanelScrolling;
    private JButton okButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton cancelButton;

    private ArrayList<RowData> stateList = new ArrayList<RowData>();

    /**
     * This is the index into stateList that is used by the
     * Prev and Next buttons to move backwards and forwards
     * through the list of states.
     *
     * Think of this index always pointing to the entry in
     * the stateList that should be displayed if the user
     * hits the Prev button.  If the user hits the Next button,
     * we need to increment this by one and display the state
     * at that index.
     */
    private int stateIndex = -1;

    /**
     * Create an ExpressionBuilder dialog with its expression
     * tree initialized to a copy of the passed in expression.
     *
     * @param originalRootRow The rootRow of the expression tree
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


    /**
     * This method is called when the user presses the Next button.
     * It displays the "previous" version of the expression tree.
     */
    private void handlePrevButton() {

        /*
        System.out.println("Enter handlePrevButton().");
        System.out.println("stateIndex = "+stateIndex);
        System.out.println("stateList.size() = "+stateList.size());
        */

        if (stateIndex < 0) {
            System.err.println("ERROR: handlePrevButton() called when we "+
                               "are already at the first state in the "+
                               "stateList.  This is a programming error.");
            System.err.println("stateIndex = "+stateIndex);
            System.err.println("stateList.size() = "+stateList.size());
            return;
        }

        /**
         * The user wants to view the expression tree "previous"
         * to the one currently being displayed.
         * Possibly save the current state of the expression
         * tree before we display the previous version.
         */
        possiblySaveState(false);

        /**
         * Go back one version of the expression tree.
         */
        stateIndex--;
        //System.out.println("Display version at stateIndex = "+stateIndex);
        //System.out.println("stateList.size() = "+stateList.size());
        RowData rootRow = stateList.get(stateIndex);
        setRootRow(rootRow);
        //System.out.println("Exit handlePrevButton\n");
    }


    /**
     * This method is called when the user presses the Next button.
     * It displays the "next" version of the expression tree.
     */
    private void handleNextButton() {

        /*
        System.out.println("Enter handleNextButton().");
        System.out.println("stateIndex = "+stateIndex);
        System.out.println("stateList.size() = "+stateList.size());
        */

        if (stateIndex >= stateList.size()) {
            System.err.println("ERROR: handleNextButton() called when we "+
                               "are already at the last state in the "+
                               "stateList.  This is a programming error.");
            System.err.println("stateIndex = "+stateIndex);
            System.err.println("stateList.size() = "+stateList.size());
            return;
        }

        /**
         * Go to the next version of the expression tree.
         */
        stateIndex++;
        //System.out.println("Display version at stateIndex = "+stateIndex);
        //System.out.println("stateList.size() = "+stateList.size());
        RowData rootRow = stateList.get(stateIndex);
        setRootRow(rootRow);
        //System.out.println("Exit handleNextButton\n");
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
        return(editExpression((ExpressionTree)null));
    }
    /*
    private static ReturnValue editExpression() {
        return(editExpression((RowData)null));
    }
    */


    /**
     * This is the private method that actually displays the GUI.
     * Physion engineers probably should NOT be calling this method.
     * The public editExpression(ExpressionTree) method is the one
     * that other code will use to interface with this
     * ExpressionBuilder GUI.
     *
     * @param rootRow This is the "root" RowData of the
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
    private static ReturnValue editExpression(RowData rootRow) {

        /**
         * Create a returnValue that is already initialized
         * to the values used if a user cancels out of the window.
         * (The default ReturnValue constructor does that.)
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
        if (returnValue.status == RETURN_STATUS_OK) {
            returnValue.rootRow = dialog.getRootRow();
            returnValue.expressionTree = null;

            try {
                returnValue.expressionTree =
                    ExpressionTranslator.createExpressionTree(
                        dialog.getRootRow());
            }
            catch (Exception e) {
                System.err.println(
                    "ExpressionTranslator failed to translate "+
                    "the RowData tree into an ExpressionTree");
            }
        }
        else {
            /**
             * User canceled out of the window in some way,
             * so set the returned trees to be null.
             */
            returnValue.rootRow = null;
            returnValue.expressionTree = null;
        }

        return(returnValue);
    }


    /**
     * This is the public method Physion engineers should use
     * to display the ExpressionBuilder GUI to edit/create an
     * expression.
     *
     * An example of using this method is in this
     * class's main() method.
     *
     * @param expressionTree This tells us the Class Under Qualification
     * and gives us the IExpression tree that will be displayed and
     * edited.  Please note, this method creates a RowData expression
     * tree from this value.  (RowData is what the GUI understands.)
     * So this expressionTree parameter you passed in is not ever modified
     * by the GUI.
     * Pass null if you want to create a new expression from scratch.
     *
     * @return Returns an ExpressionBuilder.ReturnValue object that
     * contains the "status" and "expressionTree" values that your
     * code should look at.  Please see ExpressionBuilder.ReturnValue
     * for more information.
     */
    public static ReturnValue editExpression(ExpressionTree expressionTree) {

        if (expressionTree == null) {
            expressionTree = new ExpressionTree();
        }

        /**
         * Convert the passed in expressionTree into a RowData tree
         * that the GUI will work with.
         */
        RowData rootRow = null;
        try {
            rootRow = ExpressionTranslator.createRowData(expressionTree);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return(editExpression(rootRow));
    }


    /**
     * This method is called when the expression tree we are
     * displaying/editing changes.  Actually, it is called
     * both BEFORE the tree changes and AFTER the tree
     * changes.  We want to save the state of the tree
     * BEFORE it changes.
     *
     * AFTER the tree changes we enable/disable the Ok button
     * depending on whether the expression tree currently
     * contains a legal value.  I.e. we will force the user
     * to make the tree legal before we let him/her Ok out
     * of the window.
     */
    @Override
    public void rowDataChanged(RowDataEvent event) {

        if ((event.getTiming() == RowDataEvent.TIMING_BEFORE) &&
            changeIsSavable(event.getChangeType())) {
            //System.out.println("*** Got TIMING_BEFORE event.");
            possiblySaveState(true);
            stateIndex = stateList.size()-1;
        }
        else if (event.getTiming() == RowDataEvent.TIMING_AFTER) {
            //System.out.println("*** Got TIMING_AFTER event.");
            enableButtons();
        }
        else {
            /**
             * Currently, (Oct 2011), there is no other event type,
             * so there is nothing to do here.  But in the future,
             * we might want to know about "indirect" changes to
             * a RowData object.  E.g. changes caused by changes
             * to a child or parent row.
             */
        }
    }


    /**
     * This method returns true if the passed in changeType is
     * a change that should be saved.  Please note, we aren't
     * really saving the change, but are instead saving a copy
     * of the expression tree before the change is executed
     * on it.
     *
     * For example, if the user adds or deletes a row from the
     * expression tree, we want to save the state of the tree
     * in our stateList so that the user can use the Prev button
     * to go back to the state of the expression tree at that
     * time.
     *
     * But, if the user is typing a string value into an
     * attribute value text field, we don't want to save
     * the state of the tree after each character change.
     * So we would return false for that changeType.
     *
     * To alter what changeTypes are considered "save worthy",
     * just alter what values are in the if-statement in
     * this method.  As of October 2011, I have left a couple
     * commented out entries in the if-statement that you
     * might consider "save worthy".
     *
     * @param changeType One of the RowDataEvent.TYPE_* values.
     */
    private boolean changeIsSavable(int changeType) {

        if ((changeType == RowDataEvent.TYPE_CHILD_ADD) ||
            (changeType == RowDataEvent.TYPE_CHILD_DELETE) ||
            (changeType == RowDataEvent.TYPE_CUQ) ||
            //(changeType == RowDataEvent.TYPE_PROP_TYPE) ||
            //(changeType == RowDataEvent.TYPE_ATTRIBUTE_OPERATOR) ||
            (changeType == RowDataEvent.TYPE_COLLECTION_OPERATOR) ||
            (changeType == RowDataEvent.TYPE_ATTRIBUTE) ||
            (changeType == RowDataEvent.TYPE_ATTRIBUTE_PATH)) {
            return(true);
        }
        else {
            /**
             * The change is a minor one, so we don't want to save
             * a copy of the current state of the expression tree.
             */
            return(false);
        }
    }


    /**
     * This method will (possibly) save the state of the current
     * expression tree.
     *
     * Currently, this method is only called if the user is
     * changing the number of rows in the tree or changing
     * an attribute.  (We don't save the changes as the user
     * is typing into a text field for example.)
     *
     * Saving at this "granularity" should prevent the user from
     * shooting his/herself in the foot and losing many changes
     * s/he has made to the expression tree if s/he accidentally
     * changes a "top level" comboBox.
     *
     * @param calledDueToChangeEvent Pass true for this parameter
     * if you are calling it because of a change event.  (I.e. the
     * user is editing the expression tree.)  Pass false for this
     * parameter if you are calling it because you are navigating
     * through the stateList because the user is pressing the
     * Prev and Next buttons.
     */
    private void possiblySaveState(boolean calledDueToChangeEvent) {

        /*
        System.out.println("Enter possiblySaveState().");
        System.out.println("calledDueToChangeEvent = "+calledDueToChangeEvent);
        System.out.println("stateIndex = "+stateIndex);
        System.out.println("stateList.size() = "+stateList.size());
        for (RowData state : stateList) {
            System.out.println("---------");
            System.out.println("state:\n"+state);
            System.out.println("---------");
        }
        */

        if (getRootRow() == null)
            return;

        /**
         * If the the GUI is displaying an expression tree that
         * is in the stateList, don't save the state again.
         */
        if (stateList.contains(getRootRow())) {
            //System.out.println("State already saved.  Don't save again.");

            if (calledDueToChangeEvent) {
                /**
                 * The GUI is displaying an expression tree that
                 * is in the stateList, but the user is changing it,
                 * so we need to replace the one in the stateList
                 * with a copy.  That way we save the state of the
                 * expression BEFORE the user's latest change.
                 */
                //System.out.println("Replace tree in stateList.");
                stateList.set(stateIndex, new RowData(getRootRow()));
            }

            //System.out.println("Exit possiblySaveState.");
            return;
        }

        /**
         * There has been a user initiated change, so
         * save the state of the expression tree and
         * possibly "roll off" the oldest state that
         * we had saved.
         */
        //System.out.println("Add state to statelist.");
        stateList.add(new RowData(getRootRow()));
        if (stateList.size() > MAX_NUM_STATES_SAVED) {
            stateList.remove(0);
        }

        /**
         * Set the stateIndex to be pointing to
         * the last saved state.
         */
        stateIndex = stateList.size()-1;
        enableButtons();
        /*
        System.out.println("Upon leaving...\nstateIndex = "+stateIndex);
        System.out.println("stateList.size() = "+stateList.size());
        for (RowData state : stateList) {
            System.out.println("---------");
            System.out.println("state:\n"+state);
            System.out.println("---------");
        }
        System.out.println("Exit possiblySaveState.");
        */
    }


    /**
     * Enable/disable buttons.
     *
     * Enable/disable the Ok button depending on whether
     * the expression tree is currently a legal value.
     *
     * Enable/disable the Prev and Next buttons depending
     * on whether there are states previous to or after the
     * state in the stateList pointed to by stateIndex.
     * For example, if the stateList has two items in it,
     * and the stateIndex is 1, then the Prev button
     * is enabled because there is a previous state we can
     * go to.  The Next button is not enabled because there
     * is no state "after" the one stateIndex is
     * currently pointing to.
     */
    private void enableButtons() {

        /*
        System.out.println("Enter enableButtons().");
        System.out.println("stateIndex = "+stateIndex);
        System.out.println("stateList.size() = "+stateList.size());
        System.out.println("getRootRow().getIllegalRows().size() = "+
            getRootRow().getIllegalRows().size());
        */

        if (getRootRow() == null) {
            okButton.setEnabled(false);
            return;
        }

        /**
         * If any rows are illegal, disable the Ok button.
         */
        okButton.setEnabled(getRootRow().getIllegalRows().size() == 0);

        if ((stateIndex > 0) || (stateIndex == stateList.size()-1) &&
            (stateList.size() > 0))
            prevButton.setEnabled(true);
        else
            prevButton.setEnabled(false);

        if (stateIndex < (stateList.size()-1))
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
     * expressionTree - Is set to the new ExpressionTree that was created
     * and edited by the GUI.  This is the value Physion engineers should
     * use.
     *
     * rootRow - Is set to the RowData version of the expression tree
     * that was created by translating the original ExpressionTree
     * parameter that was passed to editExpression() into a RowData
     * object and then edited by the GUI.  Physion engineers probably do
     * NOT want this value for any reason.  It is returned for testing
     * and debugging purposes.
     *
     * Please see ExpressionBuilder.main() for some examples of how
     * this class is used.
     */
    public static class ReturnValue {
        public int status = RETURN_STATUS_CANCEL;
        public ExpressionTree expressionTree = null;
        public RowData rootRow = null;  // Delete this value eventually?
    }


    /**
     * Set the look and feel to something specific.
     * Currently, this is really only to test out some
     * different look and feels.
     * You need to comment/uncomment the setting of the
     * "lookAndFeel" variable below to what you want. 
     */
    public static void setLookAndFeel() {

        String lookAndFeel;

        /**
         * Print out the name of the current look and feel.
         */
        lookAndFeel = UIManager.getLookAndFeel().toString();
        System.out.println("\nCurrent default look and feel is:\n    "+
                           lookAndFeel);

        /**
         * Below is a selection of look and feels that you can
         * use.  Note that you cannot use the Windows on Mac and
         * vice verse.  So if you want to use the system's look
         * and feel, just use the line below that calls
         * getSystemLookAndFeelClassName().
         *
         * GTKLookAndFeel is a Linux look and feel.
         *
         * MotifLookAndFeel is ugly.
         *
         * The NimbusLookAndFeel would be a good starting point
         * to use.  I think with a few changes to margins/borders/sizes
         * it could look close to what you envisioned.
         */
        //lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

        /**
         * Below is the default Swing MetalLookAndFeel that you do not like.
         */
        //lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();

        /**
         * This will get the "system" look and feel for the OS
         * on which we are currently running and use that.
         */
        lookAndFeel = UIManager.getSystemLookAndFeelClassName();

        try {
            System.out.println("\nSetting look and feel to:\n    "+lookAndFeel);
            UIManager.setLookAndFeel(lookAndFeel);
        }
        catch(Exception e) {
            //e.printStackTrace();
            System.err.println("\nSetting look and feel failed:\n    "+
                               e.getMessage());
        }
    }


    /**
     * A simple example test program to test this class and show
     * how the editExpression() methods are used.
     *
     * Currently, if there is any argument on the command line
     * we will try to convert Expression objects into RowData
     * objects.  This is just for Steve's testing currently.
     */
    public static void main(String[] args) {

        /**
         * Create a default ReturnValue.
         */
        ExpressionBuilder.ReturnValue returnValue =
            new ExpressionBuilder.ReturnValue();

        /**
         * Set the look and feel to something.
         */
        setLookAndFeel();

        /**
         * Read in the ExpressionTree that we serialized out
         * to a file last time this application was run.
         * If no file exists, use a null value for the
         * expressionTree parameter.
         */
        ExpressionTree expressionTree = ExpressionTree.readExpressionTree(
            SAVE_FILE_NAME_EXP_TREE);
        if (expressionTree != null) {
            System.out.println("\nSerialized ExpressionTree Read In:\n"+
                               expressionTree);
        }
        returnValue.expressionTree = expressionTree;

        /**
         * Stay in a loop, displaying the GUI every time the
         * user hits the OK button.
         * Please note, the last line of this loop, which sets
         * the keepRunning flag to false, can be uncommented
         * to cause the GUI to only display once.
         */
        boolean keepRunning = true;
        while (keepRunning) {

            ExpressionTree originalExpressionTree = returnValue.expressionTree;

            /**
             * Here is where we create and display the GUI.
             * The GUI is "modal", so this method will not return
             * until the GUI is dismissed by the user OKing, Canceling,
             * or closing the window in some other way.
             */
            returnValue = ExpressionBuilder.editExpression(
                returnValue.expressionTree);

            /**
             * When we get here it means that the GUI has been
             * dismissed.  Let's look at the values that were
             * returned.
             */

            System.out.println("\nstatus = "+returnValue.status);
            System.out.println("\nOriginal originalExpressionTree:\n"+
                originalExpressionTree);
            System.out.println("\nModified rootRow:\n"+returnValue.rootRow);
            System.out.println("\nModified expressionTree:\n"+
                returnValue.expressionTree);

            if (returnValue.status == RETURN_STATUS_CANCEL) {
                /**
                 * User canceled or closed out of the window,
                 * so exit the GUI without saving the current
                 * ExpressionTree.
                 */
                System.out.println("User pressed Cancel or closed the window.");
                System.exit(returnValue.status);
            }
            /**
             * If we get here, the user pressed the OK button.
             */

            /**
             * Write out the serialized version of the 
             * ExpressionTree.  We will read it in when
             * this program is run again.
             *
             * This is just for testing the serialization.
             */
            if (returnValue.expressionTree != null) {
                returnValue.expressionTree.writeExpressionTree(
                    SAVE_FILE_NAME_EXP_TREE);
            }

            /**
             * Comment this line out if you want to keep running
             * in an infinite loop until the user presses Cancel.
             */
            keepRunning = false;
        }

        System.out.println("Calling System.exit().");
        System.exit(returnValue.status);
    }
}
