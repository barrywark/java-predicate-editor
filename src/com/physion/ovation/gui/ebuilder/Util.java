/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;


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
         * Tell the viewport that contains the component to
         * make sure the specified rectangle is visible.
         */
        viewport.scrollRectToVisible(rect);
    }
}
