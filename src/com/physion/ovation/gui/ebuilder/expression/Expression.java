package com.physion.ovation.gui.ebuilder.expression;

//import java.util.ArrayList;


/**
 */
public interface Expression {

    /**
     * 
     */
    public void accept(ExpressionVisitor expressionVisitor);
}
