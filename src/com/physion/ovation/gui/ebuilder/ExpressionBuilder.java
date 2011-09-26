package com.physion.ovation.gui.ebuilder;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;

import com.physion.ovation.gui.ebuilder.datamodel.RowData;

public class ExpressionBuilder
    extends JDialog 
    implements ActionListener {


    public static final int RETURN_STATUS_OK = 0;
    public static final int RETURN_STATUS_CANCEL = 1;

    private int returnStatus = RETURN_STATUS_CANCEL;
    private EBuilderPanel panel;
    private JButton okButton;
    private JButton cancelButton;


    ExpressionBuilder() {
        super((Frame)null);
        setTitle("Physion ooExpression Builder");
        setModal(true);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new EBuilderPanel();
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

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        buttonPanel.add(cancelButton, gc);

        pack();
        int width = getSize().width;
        int height = getSize().height;
        if (width < 800)
            width = 800;
        height += 200;
        setSize(width, height);
    }


    /**
     * This returns the reason the window closed.
     *
     * @return Returns ExpressionBuilder.RETURN_STATUS_OK if the user
     * pressed the Ok button.  Returns ExpressionBuilder.RETURN_STATUS_CANCEL
     * if the user pressed the Cancel button or closed the window another
     * way.  It also returns ExpressionBuilder.RETURN_STATUS_CANCEL if
     * a null rootRow parameter was passed to the editExpression() method.
     */
    public int getReturnStatus() {
        return(returnStatus);
    }


    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == cancelButton) {
            returnStatus = RETURN_STATUS_CANCEL;
            setVisible(false);
        }
        else if (e.getSource() == okButton) {
            returnStatus = RETURN_STATUS_OK;
            panel.print();
            setVisible(false);
        }
    }


    /**
     * This is the method you should use to edit/create
     * an expression.
     *
     * 
     * @param rootRow - This is the "root" RowData of the
     * expression you want the GUI to display and edit.
     * Pass an "empty" RowData if you want to create a new
     * expression from scratch.
     */
    public static int editExpression(RowData rootRow) {

        if (rootRow == null) {
            System.err.println("rootRow = null");
            return(RETURN_STATUS_CANCEL);
        }

        ExpressionBuilder dialog = new ExpressionBuilder();

        /**
         * Make the dialog visible.  It is modal, so
         * execution along this path stops at this point.
         * Only when the user hits Ok or Cancel will this
         * method return.
         */
        dialog.setVisible(true);

        int returnStatus = dialog.getReturnStatus();

        return(returnStatus);
    }


    public static void main(String[] args) {

        RowData rootRow = new RowData();
        int status = ExpressionBuilder.editExpression(rootRow);
        System.out.println("status = "+status);
        System.exit(status);
    }
}
