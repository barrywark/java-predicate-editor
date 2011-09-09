//package com.physion.ovation.gui.ebuilder;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;

/**
 * Simple main program to test the EBuilder GUI.
 */
class TestEBuilder {

    /**
     * Set the look and feel to something specific.
     */
    public static void setLookAndFeel() {

        String lookAndFeel;

        lookAndFeel = UIManager.getLookAndFeel().toString();
        System.out.println("Current look and feel is: "+lookAndFeel);

        lookAndFeel = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
        //lookAndFeel = UIManager.getSystemLookAndFeelClassName();

        try {
            System.out.println("Setting look and feel to: "+lookAndFeel);
            UIManager.setLookAndFeel(lookAndFeel);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * This is the main program that starts everything else.
     */
    public static void main(String[] args) {

        System.out.println("TestEBuilder is starting...");

        setLookAndFeel();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TestEBuilderFrame frame = new TestEBuilderFrame();
                frame.setVisible(true);
            }
        });

        System.out.println("TestEBuilder is ending.");
    }
}
