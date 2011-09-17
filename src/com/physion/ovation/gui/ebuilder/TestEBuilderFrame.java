package com.physion.ovation.gui.ebuilder;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

public class TestEBuilderFrame
    extends JFrame 
    implements ActionListener {

    private EBuilderPanel panel;
    private JButton okButton;
    private JButton cancelButton;


    TestEBuilderFrame() {

        super("Test EBuilder Frame");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new EBuilderPanel();
        getContentPane().add(panel);

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0,0,7,0));

        GridBagConstraints gc;

        okButton = new JButton("Print");
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
        setSize(1000, getSize().height);
    }


    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton)
            System.exit(0);

        if (e.getSource() == okButton)
            panel.print();
    }
}
