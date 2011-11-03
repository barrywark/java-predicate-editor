package com.physion.ovation.gui.ebuilder.expression;


import java.io.Serializable;


/**
 */
public class BooleanLiteralValueExpression
    extends Expression
    implements IBooleanLiteralValueExpression, Serializable {

    private boolean value;


    @Override
    public Object getValue() {
        return(new Boolean(value));
    }


    public BooleanLiteralValueExpression(Boolean value) {
        this.value = value;
    }


    public String toString(String indent) {
        return(indent+"BooleanLiteralValueExpression("+value+")");
    }
}
