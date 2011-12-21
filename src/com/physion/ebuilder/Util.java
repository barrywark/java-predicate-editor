/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ebuilder;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import com.lavantech.gui.comp.DateTimePicker;


/**
 * Utility methods for the GUI.
 */
class Util {

    /**
     * Get the JScrollPane that contains the passed in component.
     * Returns null if the component is not in a JScrollPane.
     */
    public static JScrollPane getScrollPane(Component component) {
        return((JScrollPane)SwingUtilities.getAncestorOfClass(
            JScrollPane.class, component));
    }


    /**
     * Set up a focus listener on the passed in component that
     * will tell the scrollpane that contains it to make sure
     * the component is visible when the component gets the focus.
     */
    public static void setupAutoScrolling(JComponent component) {

        /**
         * JSpinners have to be handled specially.
         * The JSpinner does not get focus events.  The
         * text field inside the editor that is inside
         * the JSpinner gets the focus events.
         *
         * The up and down arrow buttons do not get the
         * focus.  A keyboard user can use the up and
         * down arrows on the keyboard to increment the
         * value.
         */
        if (component instanceof JSpinner) {
            JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor)
                ((JSpinner)component).getEditor();
            setupAutoScrolling(editor.getTextField());
            return;
        }

        /**
         * DateTimePickers have to be handled specially.
         * They contain two components.
         */
        if (component instanceof DateTimePicker) {
            setupAutoScrolling(((DateTimePicker)component).getRenderer());
            setupAutoScrolling(((DateTimePicker)component).getDropDownButton());
            return;
        }

        component.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {

                final JComponent component = (JComponent)e.getSource();

                /**
                 * Note, putting the call to
                 * Util.ensureComponentVisible()
                 * inside an invokeLater() call might not be needed.
                 * But, if we want to avoid the possibility that
                 * the scrolling geometry/location computations
                 * get made BEFORE the scrollpane child components
                 * get laid out, then it doesn't hurt.
                 * (Other developers seem to have run into this problem.)
                 *
                 * I'll put both versions of the code here, and
                 * you can use the invokeLater() version if need be.
                 */
                /*
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Util.ensureComponentVisible(component);
                    }
                });
                */
                Util.ensureComponentVisible(component);
            }
        });
    }


    /**
     * Tell the JScrollPane that contains the passed in component
     * to scroll itself to make the component visible, if necessary.
     * I.e. if the component is already visible in the JScrollPane's
     * JViewport, this method does nothing.
     *
     * Please note, this method does a bit more than make sure the
     * passed in component is visible.  If the passed in component
     * is inside a RowPanel, we try to make the row's full height
     * visible as well.
     */
    public static void ensureComponentVisible(JComponent component) {

        /**
         * Note, this single line of code works for every
         * component except JTextFields, which interpret the
         * call to scrollRectToVisible() as a command to
         * scroll their own contents.
         * So, instead of this one line of code, we have to
         * convert the location of the component to the
         * viewport's coordinate system and then
         * explicitly make the scrollRectToVisible() call
         * on the GUI's viewport.
         */
        //component.scrollRectToVisible(new Rectangle(0, 0,
        //    component.getWidth(), component.getHeight());

        Point location = component.getLocation();

        JScrollPane scrollPane = getScrollPane(component);

        JViewport viewport = scrollPane.getViewport();
        location = SwingUtilities.convertPoint(component.getParent(),
            location, viewport);

        Rectangle rect = new Rectangle(location.x, location.y,
            component.getWidth(), component.getHeight());

        /**
         * If the component is inside a RowPanel, make sure the whole
         * row's height is visible, not just the height needed for
         * the component in the row.  Just looks better this way.
         */
        RowPanel rowPanel = (RowPanel)SwingUtilities.getAncestorOfClass(
            RowPanel.class, component);
        if (rowPanel != null) {

            Point rpLocation = SwingUtilities.convertPoint(
                rowPanel.getParent(), rowPanel.getLocation(), viewport);
            rect.y = rpLocation.y;
            rect.height = rowPanel.getHeight();
        }

        /**
         * Tell the viewport that contains the component to
         * make sure the specified rectangle is visible.
         */
        viewport.scrollRectToVisible(rect);
    }
}
