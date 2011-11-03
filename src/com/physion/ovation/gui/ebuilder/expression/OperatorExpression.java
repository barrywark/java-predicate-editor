package com.physion.ovation.gui.ebuilder.expression;

import java.io.Serializable;

import java.util.ArrayList;


/**
 */
public class OperatorExpression 
    extends Expression
    implements IOperatorExpression, Serializable {

    /**
     * The indent amount to use for each level of nesting
     * when displaying this object as a string value meant
     * to be sent to a console window.  E.g. for debugging.
     */
    private static final String INDENT = "  ";

    private String operatorName;
    private ArrayList<IExpression> operandList;


    /**
     * Get the operatorName for this OperatorExpression.
     *
     * This method is required by the IOperatorExpression
     * interface.
     */
    @Override
    public String getOperatorName() {
        return(operatorName);
    }


    /**
     * This method returns the operandList.
     *
     * Please note, the returned list is NOT a copy
     * of this OperatorExpression's operandList, so
     * don't mess with it.
     *
     * Please note, the returned list might be empty,
     * but it is never null.
     *
     * This method is required by the IOperatorExpression
     * interface.
     */
    @Override
    public ArrayList<IExpression> getOperandList() {
        return(operandList);
    }


    /**
     * Create an OperatorExpressionImp.
     * Before this object is of any use, you will need
     * to set the operatorName and add some operands
     * to the operandList.
     */
    public OperatorExpression() {
        operatorName = null;
        operandList = new ArrayList<IExpression>();
    }


    /**
     * Create an OperatorExpressionImp with the
     * specified operatorName.
     */
    public OperatorExpression(String operatorName) {
        this();
        this.operatorName = operatorName;
    }


    public void addOperand(IExpression expression) {

        if (expression == null) {
            (new Exception("expression == null")).printStackTrace();
        }

        operandList.add(expression);
    }


    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     */
    public String toString() {
        return(toString(""));
    }


    /**
     * Get a string version of this class that can be used
     * for debugging purposes.
     *
     * @param indent The lines of the returned string will
     * all be indented by (at least) this amount.
     */
    public String toString(String indent) {

        String string = indent;
        string += "OperatorExpression("+getOperatorName()+")";

        //System.out.println("string = "+string);
        for (IExpression expression : getOperandList()) {
            //System.out.println("expression = "+expression);
            string += "\n"+((Expression)expression).toString(indent+INDENT);
        }
        return(string);
    }
}
