package com.physion.ovation.gui.ebuilder;

import java.awt.Graphics;
import javax.swing.JButton;


/**
 * TODO:  There must be an easier solution to this.
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
        if (getParent() != null)
            getParent().repaint();
    }
}
