package com.physion.ovation.gui.ebuilder.expression;

import java.util.ArrayList;


/**
 */
public interface OperatorExpression
    extends Expression {

    /**
     * Get the operatorName for this OperatorExpression.
     */
    public String getOperatorName();


    /**
     * This method returns the operand list.
     *
     * Please note, the returned list is NOT a copy
     * of this OperatorExpression's operand list, so
     * don't mess with it.
     *
     * Please note, the returned list might be empty,
     * but it is never null.
     */
    public ArrayList<Expression> getOperandList();
}
