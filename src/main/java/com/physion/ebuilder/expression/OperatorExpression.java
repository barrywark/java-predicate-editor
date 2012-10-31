/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ebuilder.expression;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;


/**
 * The com.physion.ebuilder.expression package consists
 * of a set of interfaces and a set of classes that implement those
 * interfaces.  There is almost a one-to-one mapping between interface
 * files and class files.  For example, there is an interface
 * IAttributeExpression and a class that implements that interface
 * called AttributeExpression.
 *
 * The set of interfaces are based on the already existing C++
 * interface to the Objectivity library.  It is assumed that at
 * some point in the future, this ...ebuilder.expression package
 * will be replaced with a Java version of the Objectivity library.
 */
public class OperatorExpression
    extends Expression
    implements IOperatorExpression, Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * The indent amount to use for each level of nesting
     * when displaying this object as a string value meant
     * to be sent to a console window.  E.g. for debugging.
     */
    private static final String INDENT = "  ";

    /**
     * This will be a value like "or", "and", ".".
     * Please see the ...ebuilder.translator.Translator
     * class for some of the constants we have defined
     * like:  OE_AND, OE_OR, OE_DOT.
     */
    private String operatorName;

    /**
     * This is the list of operands for this operator.
     */
    private List<IExpression> operandList;


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
    public List<IExpression> getOperandList() {
        return(operandList);
    }


    /**
     * Create an OperatorExpressionImp.
     * Before this object is of any use, you will need
     * to set the operatorName and add some operands
     * to the operandList.
     */
    public OperatorExpression() {
        this(null);
    }


    /**
     * Create an OperatorExpressionImp with the
     * specified operatorName.
     *
     * @param operatorName This is a String such as "and", "or", ".", "==".
     */
    public OperatorExpression(String operatorName) {
        this(operatorName, new ArrayList<IExpression>());
    }

    /**
    * Create an OperatorExpressionImp.
    * @param operatorName This is a String such as "and", "or", ".", "==".
    * @param operands list of operand expressions
    */
    public OperatorExpression(String operatorName, List<IExpression> operands)
    {
        this.operatorName = operatorName;
        operandList = operands;
    }


    /**
     * Add an operand to our list of operands.
     */
    public void addOperand(IExpression expression) {

        if (expression == null) {
            /**
             * This will only happen if there is a bug in the code.
             * We might want to throw an IllegalArgumentException here.
             */
            (new Exception("expression == null")).printStackTrace();
        }

        operandList.add(expression);
    }


    /**
     * Get a string version of this class that can be used
     * for testing/debugging purposes.
     *
     * @param indent The lines of the returned string will
     * all be indented by (at least) this amount.
     */
    public String toString(String indent) {

        String string = indent;
        string += "OperatorExpression("+getOperatorName()+")";

        for (IExpression expression : getOperandList()) {
            string += "\n"+((Expression)expression).toString(indent+INDENT);
        }
        return(string);
    }
}
