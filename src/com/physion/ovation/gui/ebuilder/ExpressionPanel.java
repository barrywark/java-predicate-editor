package com.physion.ovation.gui.ebuilder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Scrollable;
import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataEvent;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataListener;


/**
 * This is the panel that contains a list of RowPanels.
 * Each RowPanel handles one row in the expression tree.
 * Each RowPanel displays/edits one RowData object.
 *
 * TODO: Remove the commented out "zebra striping" code if
 * Physion decides it does not want to do something like that.
 */
public class ExpressionPanel
    extends JPanel
    implements RowDataListener, Scrollable {
	
	/**
	 * We never serialize this class, so this declaration is
	 * just to stop the compiler warning.
	 * You can suppress the warning instead if you want using
	 * @SuppressWarnings("serial")
	 */
	private static final long serialVersionUID = 1L;

	private RowData rootRow;


    /**
     * Create an ExpressionPanel that will display and edit the passed
     * in expression tree.
     */
    ExpressionPanel(RowData rootRow) {

        super(new GridLayout(1,1));
        setRootRow(rootRow);
    }


    /**
     * Set the expression tree this ExpressionPanel will display.
     */
    public void setRootRow(RowData rootRow) {

        if (this.rootRow != null)
            this.rootRow.removeRowDataListener(this);

        this.rootRow = rootRow;
        rootRow.addRowDataListener(this);
        createRowPanels();
    }


    public RowData getRootRow() {
        return(rootRow);
    }


    /**
     * Create all the RowPanels this ExpressionPanel contains.
     *
     * TODO: Make this code more clever so when rows are added
     * or deleted we don't remove all the rows and recreate
     * all the rows, but instead we only change what we need to.
     */
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
         * Temp hack.
         */
        //if ((getParent() != null) && (getParent().getParent() != null))
        //    getParent().getParent().validate();
    }
    

    /**
     * Get the RowPanel at the passed in index.
     * The root RowPanel, which is the one that is used
     * to select the Class Under Qualification, is at index 0.
     */
    /*
    private RowPanel getRowPanel(int index) {
        return((RowPanel)getComponent(index));
    }
    */


    /**
     * This method returns the RowPanel that is displaying/editing
     * the passed in rowData.  Returns null if no RowPanel in this
     * ExpressionPanel is handling the passed in rowData.
     */
    private RowPanel getRowPanel(RowData rowData) {

        for (Component component : getComponents()) {
            if (((RowPanel)component).getRowData() == rowData) {
                return((RowPanel)component);
            }
        }

        return(null);
    }


    /**
     * This is called when the expression tree we are displaying
     * is modified.
     *
     * If the change is one that changes the number of rows
     * in the GUI, recreate the RowPanels.
     *
     * TODO: Handle focus traversal properly if we want to have
     * the GUI usable from the keyboard.
     */
    public void rowDataChanged(RowDataEvent event) {

        //System.out.println("event.getChangeType() = "+event.getChangeType());
        if ((event.getTiming() == RowDataEvent.TIMING_AFTER) &&
            ((event.getChangeType() == RowDataEvent.TYPE_CHILD_ADD) ||
             (event.getChangeType() == RowDataEvent.TYPE_CHILD_DELETE) ||
             (event.getChangeType() == RowDataEvent.TYPE_ATTRIBUTE) ||
             (event.getChangeType() == RowDataEvent.TYPE_CUQ))) {

            createRowPanels();

            if (event.getChangeType() == RowDataEvent.TYPE_CHILD_ADD) {
                /**
                 * Set the focus to the new row.  Rows are always
                 * added to the end of the parent row's list of children.
                 * So, simply set the focus to the last child row of the
                 * row that sent the event.
                 */
                RowData rowData = event.getRowData();
                rowData = rowData.getChildRows().get(
                    rowData.getChildRows().size()-1);

                /**
                 * Get the RowPanel that handles that rowData
                 * and set the focus to it.
                 */
                RowPanel rowPanel = getRowPanel(rowData);
                rowPanel.setFocusToFirstFocusableComponent();
            }
            else if ((event.getChangeType() ==
                      RowDataEvent.TYPE_CHILD_DELETE) ||
                     (event.getChangeType() == RowDataEvent.TYPE_CUQ)) {

                /**
                 * Set the focus to the first row.
                 *
                 * TODO:  Figure out a more user friendly rule as
                 * to what row and component should get the focus
                 * when a row is deleted.
                 */
                RowPanel rowPanel = getRowPanel(rootRow);
                rowPanel.setFocusToFirstFocusableComponent();
            }
        }
    }


    @Override
    public boolean getScrollableTracksViewportHeight() {
        return(false);
    }


    /**
     * Make the RowPanels stretch to fill the width of the
     * scrollpane that contains us.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return(true);
    }


    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return(getPreferredSize());
    }


    /**
     * Return the number of pixels the JScrollPane that contains us
     * should scroll when the user does a "block" scroll.  E.g. the
     * user clicks above or below the scroll "thumb" or uses the
     * page up/down keys.
     *
     * This should scroll an entire "page" of rows.  Note, I think
     * it is unlikely that a user will have that many rows worth
     * of data in a real use case, but we should implement the
     * behavior anyway.
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation, int direction) {

        if (orientation == SwingConstants.HORIZONTAL) {
            /**
             * When scrolling horizontally, scroll sideways
             * by the width of the viewport.
             * Note, the GUI doesn't currently scroll horizontally.
             */
            return(visibleRect.width);
        }
        else {
            return(visibleRect.height);
        }
    }


    /**
     * Return the number of pixels the JScrollPane that contains us
     * should scroll when the user clicks on a scrolling arrow.
     * When scrolling vertically, this is the height of one row, or
     * a lesser amount necessary to get a row aligned with the top or
     * bottom of the scrollpane.
     *
     * When scrolling horizontally, there isn't really a good
     * answer for how much scrolling one "unit" is.
     * Note, we don't currently scroll horizontally.
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation, int direction) {

        if (orientation == SwingConstants.HORIZONTAL) {
            /**
             * When scrolling horizontally, there isn't really a good
             * answer for how much scrolling one "unit" is.
             * So, just scroll by a percentage of the viewport width.
             * Note, the GUI doesn't currently scroll horizontally.
             */
            return((int)(visibleRect.width * 0.25));
        }

        /**
         * If we get here, the user is scrolling the window vertically.
         */

        /**
         * Get the first RowPanel to use to calculate
         * the height of a "unit", (i.e. one row), increment.
         */
        RowPanel rowPanel = getRowPanel(getRootRow());

        /**
         * If we don't have any rows yet, just return a number.
         * This number will never get used for scrolling because
         * the GUI will always have at least one row in it when it
         * is displayed.
         */
        if (rowPanel == null)
            return(10);

        Dimension size = rowPanel.getPreferredSize();

        if (direction > 0) {
            /**
             * When scrolling down, i.e. clicking the down arrow which
             * moves the scrollpane contents up, we want to make the
             * bottommost visible row align with the bottom of the
             * scrollpane.  If the bottommost visible row is already
             * aligned with the bottom of the scrollpane then that
             * means the number we return will be the height of one row.
             *
             * If the bottommost row is NOT aligned with the bottom
             * of the scrollpane, return the number of pixels that
             * WILL make it aligned with the bottom of the scrollpane.
             */

            int bottomPixel = visibleRect.height + visibleRect.y;
            int leftOver = bottomPixel % size.height;

            if (leftOver == 0) {
                /**
                 * The bottommost row is aligned with the bottom of the
                 * scrollpane, so return the height of one row as the
                 * amount to scroll one unit.
                 */
                return(size.height);
            }
            else {
                return(size.height - leftOver);
            }
        }
        else {  // direction < 0
            /**
             * When scrolling up, i.e. clicking the up arrow which
             * moves the scrollpane contents down, we want to make the
             * topmost visible row align with the top of the
             * scrollpane.  If the topmost visible row is already
             * aligned with the top of the scrollpane then that
             * means the number we return will be the height of one row.
             *
             * If the topmost row is NOT aligned with the top
             * of the scrollpane, return the number of pixels that
             * WILL make it aligned with the top of the scrollpane.
             */

            int topPixel = visibleRect.y;
            int leftOver = topPixel % size.height;

            if (leftOver == 0) {
                /**
                 * The topmost row is aligned with the top of the
                 * scrollpane, so return the height of one row as the
                 * amount to scroll one unit.
                 */
                return(size.height);
            }
            else {
                return(leftOver);
            }
        }
    }


    /**
     * This is just for debugging purposes.
     */
    public void print() {
        System.out.println("\nCurrent Value:\n"+rootRow.toString(false, ""));
        System.out.println("\nDebug Version:\n"+rootRow.toString());
    }
}
