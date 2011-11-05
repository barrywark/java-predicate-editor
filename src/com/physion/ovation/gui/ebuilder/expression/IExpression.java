/**
 * Copyright (c) 2011. Physion Consulting LLC
 * All rights reserved.
 */
package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public interface IExpression {

    /**
     * 
     */
    public void accept(IExpressionVisitor expressionVisitor);
}
