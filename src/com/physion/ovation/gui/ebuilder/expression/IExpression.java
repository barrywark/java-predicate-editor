package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public interface IExpression {

    /**
     * 
     */
    public void accept(IExpressionVisitor expressionVisitor);
}
