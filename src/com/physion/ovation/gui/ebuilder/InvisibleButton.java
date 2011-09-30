package com.physion.ovation.gui.ebuilder;

import java.awt.Graphics;
import javax.swing.JButton;


/**
 * This button takes up space in its parent layout, but does not draw
 * itself or accept focus.
 *
 * TODO:  There must be an easier solution to this.
 * I'd swear I've done this at some point in the past
 * without having to create a subclass.
 */
class InvisibleButton
    extends JButton {


    boolean draw;


    public InvisibleButton(String label) {
        super(label);
        draw = true;
    }

    @Override
    public void paint(Graphics g) {
        if (draw)
            super.paint(g);
    }


    public void setDraw(boolean draw) {

        this.draw = draw;

        /**
         * If we are not drawn, we also should not accept the focus.
         */
        setEnabled(draw);

        if (getParent() != null)
            getParent().repaint();
    }
}
