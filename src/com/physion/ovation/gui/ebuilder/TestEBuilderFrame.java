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

import com.physion.ovation.gui.ebuilder.datamodel.RowData;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataEvent;
import com.physion.ovation.gui.ebuilder.datamodel.RowDataListener;

public class TestEBuilderFrame
    extends JFrame 
    implements ActionListener, RowDataListener {

    private RowData rootRow;
    private EBuilderPanel panel;
    private JButton okButton;
    private JButton printButton;
    private JButton cancelButton;


    TestEBuilderFrame() {

        super("Test EBuilder Frame");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        rootRow = RowData.createTestRowData();
        rootRow.addRowDataListener(this);
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

        printButton = new JButton("Print");
        printButton.addActionListener(this);
        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(printButton, gc);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        gc = new GridBagConstraints();
        gc.gridx = 2;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(cancelButton, gc);

        enableButtons();
        pack();
        //setSize(800, getSize().height+300);
    }


    public void rowDataChanged(RowDataEvent e) {

        System.out.println("Enter rowDataChanged");
        enableButtons();
    }


    private void enableButtons() {
        okButton.setEnabled(rootRow.containsLegalValue());
    }


    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton)
            System.exit(0);

        if (e.getSource() == printButton)
            panel.print();

        if (e.getSource() == okButton) {
            panel.print();
            System.exit(0);
        }
    }
}
