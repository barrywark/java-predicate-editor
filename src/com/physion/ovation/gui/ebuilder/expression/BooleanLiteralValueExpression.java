package com.physion.ovation.gui.ebuilder.expression;


/**
 */
public class BooleanLiteralValueExpression
    extends Expression
    implements IBooleanLiteralValueExpression {

    private boolean value;


    @Override
    public Object getValue() {
        return(new Boolean(value));
    }


    BooleanLiteralValueExpression(Boolean value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"BooleanLiteralValueExpression("+value+")");
    }
}
